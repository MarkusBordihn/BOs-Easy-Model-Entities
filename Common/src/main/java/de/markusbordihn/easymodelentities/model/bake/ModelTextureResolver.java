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

import de.markusbordihn.easymodelentities.api.data.EasyModelTextureSetting;
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
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

public final class ModelTextureResolver {

  public static final ResourceLocation FALLBACK_TEXTURE =
      ModelResourcePaths.resourceLocation("minecraft", "textures/block/pink_wool.png");
  private static final int MAX_TEXTURE_SIZE = ModelAssetBudgets.MAX_TEXTURE_SIZE;
  private static final int SOFT_TEXTURE_SIZE = ModelAssetBudgets.SOFT_TEXTURE_SIZE;
  private static final String TEXTURE_FOLDER = "textures";
  private static final String TEXTURES_FIELD = "textures";
  private static final String PNG_SUFFIX = ".png";
  private static final Pattern ASSETS_TEXTURE_PATTERN =
      Pattern.compile("(?:^|/)assets/([a-z0-9_.-]+)/(textures/.+\\.png)$");

  private ModelTextureResolver() {}

  public static ResolvedTextures resolve(
      EasyModelRenderProfile renderProfile,
      DecodedModel decodedModel,
      ResourceManager resourceManager) {
    Map<Integer, DecodedTexture> decodedTextures = decodedTexturesByIndex(decodedModel);
    TreeSet<Integer> usedIndices = usedTextureIndices(decodedModel);
    Map<Integer, ResourceLocation> textures = new LinkedHashMap<>();
    List<ModelRenderProfileValidationIssue> issues = new ArrayList<>();
    for (int index : usedIndices) {
      textures.put(
          index,
          resolveTexture(
              renderProfile,
              decodedModel.modelId(),
              index,
              decodedTextures.get(index),
              resourceManager,
              issues));
    }
    warnAboutUnmatchedProfileTextures(renderProfile, decodedTextures, usedIndices, issues);

    return new ResolvedTextures(textures, slotNames(decodedTextures, usedIndices), issues);
  }

  private static ResourceLocation resolveTexture(
      EasyModelRenderProfile renderProfile,
      ResourceLocation modelId,
      int index,
      DecodedTexture decodedTexture,
      ResourceManager resourceManager,
      List<ModelRenderProfileValidationIssue> issues) {
    ResourceLocation profileTexture = renderProfile.textures().get(index);
    List<String> attemptedTextures = new ArrayList<>();
    for (String candidate : candidateTextures(renderProfile, modelId, index, decodedTexture)) {
      ResourceLocation candidateTexture = ResourceLocation.tryParse(candidate);
      if (candidateTexture == null) {
        attemptedTextures.add(candidate + " (not a valid resource location)");
        continue;
      }

      List<ModelRenderProfileValidationIssue> textureIssues =
          validateTexture(candidateTexture, resourceManager);
      if (hasMissingTexture(textureIssues)) {
        attemptedTextures.add(candidate);
        continue;
      }

      issues.addAll(textureIssues);
      if (profileTexture != null && !profileTexture.equals(candidateTexture)) {
        issues.add(
            new ModelRenderProfileValidationIssue(
                ModelRenderProfileStatus.ACTIVE,
                textureField(index),
                textureLabel(index, decodedTexture)
                    + " is mapped to the missing asset "
                    + profileTexture
                    + " in the render profile, using "
                    + candidateTexture
                    + " instead. Re-export the render profile."));
      }
      return candidateTexture;
    }

    issues.add(
        new ModelRenderProfileValidationIssue(
            ModelRenderProfileStatus.MISSING_TEXTURE,
            textureField(index),
            missingTextureMessage(index, decodedTexture, attemptedTextures)));
    return FALLBACK_TEXTURE;
  }

  private static Set<String> candidateTextures(
      EasyModelRenderProfile renderProfile,
      ResourceLocation modelId,
      int index,
      DecodedTexture decodedTexture) {
    Set<String> candidates = new LinkedHashSet<>();
    ResourceLocation profileTexture = renderProfile.textures().get(index);
    if (profileTexture != null) {
      candidates.add(profileTexture.toString());
    }
    if (index == 0) {
      candidates.add(renderProfile.texture().toString());
    }
    if (decodedTexture == null) {
      return candidates;
    }

    String namespacedTexture = namespacedModelTexture(decodedTexture);
    if (namespacedTexture != null) {
      candidates.add(namespacedTexture);
    }
    String relativeTexture = relativeModelTexture(modelId, decodedTexture);
    if (relativeTexture != null) {
      candidates.add(relativeTexture);
    }
    return candidates;
  }

  private static String namespacedModelTexture(DecodedTexture decodedTexture) {
    String namespace = decodedTexture.namespace().trim().toLowerCase(Locale.ROOT);
    String name = withoutPngSuffix(decodedTexture.name().trim()).toLowerCase(Locale.ROOT);
    if (namespace.isEmpty() || name.isEmpty()) {
      return null;
    }

    String folder = trimSlashes(decodedTexture.folder().trim()).toLowerCase(Locale.ROOT);
    if (folder.isEmpty()) {
      return namespace + ":" + joinPath(TEXTURE_FOLDER, name) + PNG_SUFFIX;
    }

    return namespace + ":" + joinPath(TEXTURE_FOLDER, folder, name) + PNG_SUFFIX;
  }

  private static String relativeModelTexture(
      ResourceLocation modelId, DecodedTexture decodedTexture) {
    String relativePath = decodedTexture.relativePath().trim().replace('\\', '/');
    if (relativePath.isEmpty()) {
      return null;
    }

    Matcher assetsMatcher = ASSETS_TEXTURE_PATTERN.matcher(relativePath);
    if (assetsMatcher.find()) {
      return assetsMatcher.group(1) + ":" + assetsMatcher.group(2);
    }

    Deque<String> segments = new ArrayDeque<>(List.of(modelId.getPath().split("/")));
    segments.removeLast();
    for (String segment : relativePath.split("/")) {
      if (segment.isEmpty() || segment.equals(".")) {
        continue;
      }
      if (!segment.equals("..")) {
        segments.addLast(segment);
        continue;
      }
      if (segments.isEmpty()) {
        return null;
      }

      segments.removeLast();
    }
    String path = String.join("/", segments);
    if (!path.startsWith(TEXTURE_FOLDER + "/") || !path.endsWith(PNG_SUFFIX)) {
      return null;
    }

    return modelId.getNamespace() + ":" + path;
  }

  private static void warnAboutUnmatchedProfileTextures(
      EasyModelRenderProfile renderProfile,
      Map<Integer, DecodedTexture> decodedTextures,
      TreeSet<Integer> usedIndices,
      List<ModelRenderProfileValidationIssue> issues) {
    for (Map.Entry<Integer, ResourceLocation> profileTexture :
        new TreeMap<>(renderProfile.textures()).entrySet()) {
      int index = profileTexture.getKey();
      if (usedIndices.contains(index) || decodedTextures.containsKey(index)) {
        continue;
      }

      issues.add(
          new ModelRenderProfileValidationIssue(
              ModelRenderProfileStatus.ACTIVE,
              textureField(index),
              "The render profile maps texture index "
                  + index
                  + " to "
                  + profileTexture.getValue()
                  + ", but the model has no texture with this index. Re-export the render"
                  + " profile."));
    }
  }

  private static String missingTextureMessage(
      int index, DecodedTexture decodedTexture, List<String> attemptedTextures) {
    if (attemptedTextures.isEmpty()) {
      return textureLabel(index, decodedTexture)
          + " has no texture location, neither the render profile nor the model names one."
          + " Re-export the render profile.";
    }

    return textureLabel(index, decodedTexture)
        + " is missing, tried "
        + String.join(", ", attemptedTextures)
        + ".";
  }

  private static String textureLabel(int index, DecodedTexture decodedTexture) {
    String name = decodedTexture == null ? "" : withoutPngSuffix(decodedTexture.name().trim());
    if (name.isEmpty()) {
      return "Texture index " + index;
    }

    return "Texture \"" + name + "\" (index " + index + ")";
  }

  private static String textureField(int index) {
    return TEXTURES_FIELD + "." + index;
  }

  private static String withoutPngSuffix(String name) {
    int suffixStart = name.length() - PNG_SUFFIX.length();
    if (suffixStart >= 0
        && name.regionMatches(true, suffixStart, PNG_SUFFIX, 0, PNG_SUFFIX.length())) {
      return name.substring(0, suffixStart);
    }

    return name;
  }

  private static String trimSlashes(String path) {
    int start = 0;
    int end = path.length();
    while (start < end && path.charAt(start) == '/') {
      start++;
    }
    while (end > start && path.charAt(end - 1) == '/') {
      end--;
    }
    return path.substring(start, end);
  }

  private static Map<String, Integer> slotNames(
      Map<Integer, DecodedTexture> decodedTextures, TreeSet<Integer> usedIndices) {
    Map<String, Integer> slotNames = new LinkedHashMap<>();
    slotNames.put(EasyModelTextureSetting.DEFAULT_SLOT, 0);
    for (int index : usedIndices) {
      DecodedTexture decodedTexture = decodedTextures.get(index);
      if (decodedTexture == null) {
        continue;
      }
      String slotName = normalizeSlotName(decodedTexture.name());
      if (!slotName.isEmpty()) {
        slotNames.putIfAbsent(slotName, index);
      }
    }

    return Map.copyOf(slotNames);
  }

  private static String normalizeSlotName(String textureName) {
    if (textureName == null) {
      return "";
    }

    String name = textureName.trim().toLowerCase(Locale.ROOT);
    int suffixStart = name.length() - PNG_SUFFIX.length();
    if (suffixStart > 0 && name.startsWith(PNG_SUFFIX, suffixStart)) {
      name = name.substring(0, suffixStart);
    }
    StringBuilder slotName = new StringBuilder(name.length());
    for (int i = 0; i < name.length(); i++) {
      char character = name.charAt(i);
      boolean allowed =
          (character >= 'a' && character <= 'z')
              || (character >= '0' && character <= '9')
              || character == '_'
              || character == '-';
      slotName.append(allowed ? character : '_');
    }
    return EasyModelTextureSetting.normalizeSlot(slotName.toString()).orElse("");
  }

  public static Optional<ModelRenderProfileValidationIssue> validateTextureDimensions(
      ResourceLocation textureResourceLocation, ResourceManager resourceManager) {
    Optional<Resource> textureResource = resourceManager.getResource(textureResourceLocation);
    if (textureResource.isEmpty()) {
      return Optional.of(
          new ModelRenderProfileValidationIssue(
              ModelRenderProfileStatus.MISSING_TEXTURE,
              "texture",
              "Missing texture asset " + textureResourceLocation + "."));
    }

    try (InputStream inputStream = textureResource.get().open();
        ImageInputStream imageInputStream = ImageIO.createImageInputStream(inputStream)) {
      if (imageInputStream == null) {
        return Optional.of(undecodableTexture(textureResourceLocation));
      }
      Iterator<ImageReader> readers = ImageIO.getImageReaders(imageInputStream);
      if (!readers.hasNext()) {
        return Optional.of(undecodableTexture(textureResourceLocation));
      }

      ImageReader reader = readers.next();
      try {
        reader.setInput(imageInputStream, true, true);
        if (reader.getWidth(0) > MAX_TEXTURE_SIZE || reader.getHeight(0) > MAX_TEXTURE_SIZE) {
          return Optional.of(
              new ModelRenderProfileValidationIssue(
                  ModelRenderProfileStatus.CLIENT_ASSET_MISMATCH,
                  "texture",
                  "Texture asset "
                      + textureResourceLocation
                      + " exceeds the 2048x2048 asset budget."));
        }
      } finally {
        reader.dispose();
      }

      return Optional.empty();
    } catch (IOException exception) {
      return Optional.of(
          new ModelRenderProfileValidationIssue(
              ModelRenderProfileStatus.CLIENT_ASSET_MISMATCH,
              "texture",
              "Could not read texture asset "
                  + textureResourceLocation
                  + ": "
                  + exception.getMessage()));
    }
  }

  private static ModelRenderProfileValidationIssue undecodableTexture(
      ResourceLocation textureResourceLocation) {
    return new ModelRenderProfileValidationIssue(
        ModelRenderProfileStatus.CLIENT_ASSET_MISMATCH,
        "texture",
        "Texture asset " + textureResourceLocation + " could not be decoded.");
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
      Map<Integer, ResourceLocation> textures,
      Map<String, Integer> slotNames,
      List<ModelRenderProfileValidationIssue> issues) {

    public ResolvedTextures {
      textures = Map.copyOf(textures);
      slotNames = Map.copyOf(slotNames);
      issues = List.copyOf(issues);
    }
  }
}
