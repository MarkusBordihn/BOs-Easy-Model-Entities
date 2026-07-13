/*
 * Copyright 2026 Markus Bordihn
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package de.markusbordihn.easymodelentities.model.bake;

import de.markusbordihn.easymodelentities.data.contract.ModelAssetBudgets;
import de.markusbordihn.easymodelentities.data.model.decoder.DecodedModel;
import de.markusbordihn.easymodelentities.data.model.decoder.DecodedModelCube;
import de.markusbordihn.easymodelentities.data.model.decoder.DecodedModelPart;
import de.markusbordihn.easymodelentities.data.model.decoder.DecodedTexture;
import de.markusbordihn.easymodelentities.data.renderprofile.EasyModelRenderProfile;
import de.markusbordihn.easymodelentities.data.renderprofile.ModelRenderProfileStatus;
import de.markusbordihn.easymodelentities.data.renderprofile.ModelRenderProfileValidationIssue;
import de.markusbordihn.easymodelentities.registry.ModelResourcePaths;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeSet;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

public final class ModelTextureResolver {

  public static final ResourceLocation FALLBACK_TEXTURE =
      new ResourceLocation("minecraft", "textures/block/pink_wool.png");
  private static final int MAX_TEXTURE_SIZE = ModelAssetBudgets.MAX_TEXTURE_SIZE;
  private static final int SOFT_TEXTURE_SIZE = ModelAssetBudgets.SOFT_TEXTURE_SIZE;
  private static final String TEXTURE_FOLDER = "textures";
  private static final String ENTITY_FOLDER = "entity";
  private static final String PNG_SUFFIX = ".png";

  private ModelTextureResolver() {}

  public static ResolvedTextures resolve(
      EasyModelRenderProfile renderProfile,
      DecodedModel decodedModel,
      ResourceManager resourceManager) {
    Map<Integer, DecodedTexture> decodedTextures = decodedTexturesByIndex(decodedModel);
    Map<Integer, ResourceLocation> textures = new LinkedHashMap<>();
    List<ModelRenderProfileValidationIssue> issues = new ArrayList<>();
    for (int index : usedTextureIndices(decodedModel)) {
      ResourceLocation candidate =
          candidateTexture(renderProfile, index, decodedTextures.get(index));
      if (candidate == null) {
        textures.put(index, FALLBACK_TEXTURE);
        issues.add(
            new ModelRenderProfileValidationIssue(
                ModelRenderProfileStatus.MISSING_TEXTURE,
                "texture",
                "No texture mapping for texture index " + index + "."));
        continue;
      }
      List<ModelRenderProfileValidationIssue> textureIssues =
          validateTexture(candidate, resourceManager);
      if (hasMissingTexture(textureIssues)) {
        textures.put(index, FALLBACK_TEXTURE);
        issues.addAll(textureIssues);
        continue;
      }
      textures.put(index, candidate);
      issues.addAll(textureIssues);
    }

    return new ResolvedTextures(textures, issues);
  }

  private static ResourceLocation candidateTexture(
      EasyModelRenderProfile renderProfile, int index, DecodedTexture decodedTexture) {
    ResourceLocation profileTexture = renderProfile.textures().get(index);
    if (profileTexture != null) {
      return ModelResourcePaths.textureResourceLocation(profileTexture);
    }
    if (index == 0) {
      return ModelResourcePaths.textureResourceLocation(renderProfile.texture());
    }
    return derivedTexture(decodedTexture);
  }

  private static ResourceLocation derivedTexture(DecodedTexture decodedTexture) {
    if (decodedTexture == null) {
      return null;
    }
    String relativePath = decodedTexture.relativePath().trim();
    if (relativePath.isBlank()) {
      relativePath = decodedTexture.name().trim();
    }
    if (relativePath.isBlank()) {
      return null;
    }

    String namespace = decodedTexture.namespace().trim();
    if (namespace.isBlank()) {
      namespace = ResourceLocation.DEFAULT_NAMESPACE;
    }
    String folder = decodedTexture.folder().trim();
    String path =
        folder.isBlank()
            ? joinPath(TEXTURE_FOLDER, ENTITY_FOLDER, withPngSuffix(relativePath))
            : joinPath(TEXTURE_FOLDER, folder, withPngSuffix(relativePath));
    return ResourceLocation.tryParse(namespace + ":" + path);
  }

  private static String withPngSuffix(String path) {
    return path.toLowerCase().endsWith(PNG_SUFFIX) ? path : path + PNG_SUFFIX;
  }

  private static String joinPath(String... segments) {
    return String.join("/", segments);
  }

  private static List<ModelRenderProfileValidationIssue> validateTexture(
      ResourceLocation textureResourceLocation, ResourceManager resourceManager) {
    Optional<Resource> textureResource = resourceManager.getResource(textureResourceLocation);
    if (textureResource.isEmpty()) {
      return List.of(
          new ModelRenderProfileValidationIssue(
              ModelRenderProfileStatus.MISSING_TEXTURE,
              "texture",
              "Missing texture asset " + textureResourceLocation + "."));
    }

    try (InputStream inputStream = textureResource.get().open();
        ImageInputStream imageInputStream = ImageIO.createImageInputStream(inputStream)) {
      if (imageInputStream == null) {
        return List.of(
            new ModelRenderProfileValidationIssue(
                ModelRenderProfileStatus.CLIENT_ASSET_MISMATCH,
                "texture",
                "Texture asset " + textureResourceLocation + " could not be decoded."));
      }
      Iterator<ImageReader> readers = ImageIO.getImageReaders(imageInputStream);
      if (!readers.hasNext()) {
        return List.of(
            new ModelRenderProfileValidationIssue(
                ModelRenderProfileStatus.CLIENT_ASSET_MISMATCH,
                "texture",
                "Texture asset " + textureResourceLocation + " could not be decoded."));
      }

      ImageReader reader = readers.next();
      int width;
      int height;
      try {
        reader.setInput(imageInputStream, true, true);
        width = reader.getWidth(0);
        height = reader.getHeight(0);
        if (width > MAX_TEXTURE_SIZE || height > MAX_TEXTURE_SIZE) {
          return List.of(
              new ModelRenderProfileValidationIssue(
                  ModelRenderProfileStatus.CLIENT_ASSET_MISMATCH,
                  "texture",
                  "Texture asset "
                      + textureResourceLocation
                      + " exceeds the 2048x2048 asset budget."));
        }
        BufferedImage image = reader.read(0);
        if (image == null) {
          return List.of(
              new ModelRenderProfileValidationIssue(
                  ModelRenderProfileStatus.CLIENT_ASSET_MISMATCH,
                  "texture",
                  "Texture asset " + textureResourceLocation + " could not be decoded."));
        }
      } finally {
        reader.dispose();
      }
      if (width > SOFT_TEXTURE_SIZE || height > SOFT_TEXTURE_SIZE) {
        return List.of(
            new ModelRenderProfileValidationIssue(
                ModelRenderProfileStatus.ACTIVE,
                "texture",
                "Texture asset "
                    + textureResourceLocation
                    + " is larger than the recommended 64x64 or 128x128 size."));
      }

      return List.of();
    } catch (IOException exception) {
      return List.of(
          new ModelRenderProfileValidationIssue(
              ModelRenderProfileStatus.CLIENT_ASSET_MISMATCH,
              "texture",
              "Could not read texture asset "
                  + textureResourceLocation
                  + ": "
                  + exception.getMessage()));
    }
  }

  private static boolean hasMissingTexture(List<ModelRenderProfileValidationIssue> issues) {
    return issues.stream()
        .anyMatch(issue -> issue.status() == ModelRenderProfileStatus.MISSING_TEXTURE);
  }

  private static Map<Integer, DecodedTexture> decodedTexturesByIndex(DecodedModel decodedModel) {
    Map<Integer, DecodedTexture> decodedTextures = new HashMap<>();
    for (DecodedTexture texture : decodedModel.textures()) {
      decodedTextures.putIfAbsent(texture.index(), texture);
    }

    return decodedTextures;
  }

  private static TreeSet<Integer> usedTextureIndices(DecodedModel decodedModel) {
    TreeSet<Integer> indices = new TreeSet<>();
    indices.add(0);
    collectTextureIndices(decodedModel.rootParts(), indices);
    return indices;
  }

  private static void collectTextureIndices(
      Collection<DecodedModelPart> parts, TreeSet<Integer> indices) {
    for (DecodedModelPart part : parts) {
      for (DecodedModelCube cube : part.cubes()) {
        indices.add(cube.textureIndex());
      }
      collectTextureIndices(part.children(), indices);
    }
  }

  public record ResolvedTextures(
      Map<Integer, ResourceLocation> textures, List<ModelRenderProfileValidationIssue> issues) {

    public ResolvedTextures {
      textures = Map.copyOf(textures);
      issues = List.copyOf(issues);
    }
  }
}
