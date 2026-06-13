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
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package de.markusbordihn.easymodelentities.model.bake;

import de.markusbordihn.easymodelentities.model.decoder.DecodedModel;
import de.markusbordihn.easymodelentities.model.decoder.DecodedModelCube;
import de.markusbordihn.easymodelentities.model.decoder.DecodedModelPart;
import de.markusbordihn.easymodelentities.model.decoder.EasyModelDecodeException;
import de.markusbordihn.easymodelentities.model.decoder.EasyModelDecoder;
import de.markusbordihn.easymodelentities.model.decoder.EasyModelDecoderRegistry;
import de.markusbordihn.easymodelentities.model.decoder.ModelDecoderRegistry;
import de.markusbordihn.easymodelentities.profile.ModelBodyType;
import de.markusbordihn.easymodelentities.registry.ModelResourcePaths;
import de.markusbordihn.easymodelentities.renderprofile.EasyModelRenderProfile;
import de.markusbordihn.easymodelentities.renderprofile.ModelRenderProfileStatus;
import de.markusbordihn.easymodelentities.renderprofile.ModelRenderProfileValidationIssue;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import javax.imageio.ImageIO;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

public final class ModelBakeService implements EasyModelBakeService {

  private final EasyModelDecoderRegistry decoderRegistry;
  private final ModelCache cache = new ModelCache();

  public ModelBakeService(EasyModelDecoderRegistry decoderRegistry) {
    this.decoderRegistry = Objects.requireNonNull(decoderRegistry, "decoderRegistry");
  }

  public static ModelBakeService createDefault() {
    return new ModelBakeService(ModelDecoderRegistry.createDefault());
  }

  private static List<ModelRenderProfileValidationIssue> validateTexture(
      EasyModelRenderProfile renderProfile, ResourceManager resourceManager) {
    ResourceLocation textureResourceLocation =
        ModelResourcePaths.textureResourceLocation(renderProfile.texture());
    Optional<Resource> textureResource = resourceManager.getResource(textureResourceLocation);
    if (textureResource.isEmpty()) {
      return List.of(
          new ModelRenderProfileValidationIssue(
              ModelRenderProfileStatus.MISSING_TEXTURE,
              "texture",
              "Missing texture asset " + textureResourceLocation + "."));
    }

    try (InputStream inputStream = textureResource.get().open()) {
      BufferedImage image = ImageIO.read(inputStream);
      if (image == null) {
        return List.of(
            new ModelRenderProfileValidationIssue(
                ModelRenderProfileStatus.CLIENT_ASSET_MISMATCH,
                "texture",
                "Texture asset " + textureResourceLocation + " could not be decoded."));
      }
      if (image.getWidth() > 2048 || image.getHeight() > 2048) {
        return List.of(
            new ModelRenderProfileValidationIssue(
                ModelRenderProfileStatus.CLIENT_ASSET_MISMATCH,
                "texture",
                "Texture asset "
                    + textureResourceLocation
                    + " exceeds the 2048x2048 asset budget."));
      }
      if (image.getWidth() > 128 || image.getHeight() > 128) {
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

  private static List<ModelRenderProfileValidationIssue> validateBodyType(
      ModelBodyType bodyType, DecodedModel decodedModel) {
    Set<String> partNames = new HashSet<>();
    collectPartNames(decodedModel.rootParts(), partNames);
    List<String> requiredParts = requiredParts(bodyType);
    List<ModelRenderProfileValidationIssue> issues = new ArrayList<>();
    for (String requiredPart : requiredParts) {
      if (!partNames.contains(requiredPart)) {
        issues.add(
            new ModelRenderProfileValidationIssue(
                ModelRenderProfileStatus.CLIENT_BODY_TYPE_MISMATCH,
                "body_type",
                "Missing required "
                    + bodyType.getSerializedName()
                    + " part "
                    + requiredPart
                    + "."));
      }
    }

    return issues;
  }

  private static List<String> requiredParts(ModelBodyType bodyType) {
    return switch (bodyType) {
      case BIPED ->
          List.of("root", "head", "body", "left_arm", "right_arm", "left_leg", "right_leg");
      case QUADRUPED ->
          List.of(
              "root",
              "body",
              "head",
              "front_left_leg",
              "front_right_leg",
              "back_left_leg",
              "back_right_leg");
      case STATIC -> List.of();
    };
  }

  private static void collectPartNames(Collection<DecodedModelPart> parts, Set<String> partNames) {
    for (DecodedModelPart part : parts) {
      partNames.add(part.name());
      collectPartNames(part.children(), partNames);
    }
  }

  private static boolean hasHardFailure(List<ModelRenderProfileValidationIssue> issues) {
    return issues.stream()
        .anyMatch(
            issue ->
                issue.status() != ModelRenderProfileStatus.ACTIVE
                    && issue.status() != ModelRenderProfileStatus.MISSING_TEXTURE);
  }

  private static BakedModel bakeDecoded(ModelBodyType bodyType, DecodedModel decodedModel) {
    List<BakedModelPart> rootParts =
        decodedModel.rootParts().stream().map(ModelBakeService::bakePart).toList();
    if (bodyType == ModelBodyType.STATIC
        && rootParts.stream().noneMatch(part -> "root".equals(part.name()))) {
      rootParts =
          List.of(
              new BakedModelPart(
                  "root",
                  new float[] {0.0f, 24.0f, 0.0f},
                  new float[] {0.0f, 0.0f, 0.0f},
                  List.of(),
                  rootParts));
    }

    return new BakedModel(
        decodedModel.modelId(),
        decodedModel.textureWidth(),
        decodedModel.textureHeight(),
        rootParts);
  }

  private static BakedModelPart bakePart(DecodedModelPart decodedPart) {
    Map<String, List<DecodedModelCube>> rotatedCubeGroups = new LinkedHashMap<>();
    List<DecodedModelCube> directCubes = new ArrayList<>();
    for (DecodedModelCube decodedCube : decodedPart.cubes()) {
      if (decodedCube.hasRotation()) {
        rotatedCubeGroups
            .computeIfAbsent(rotationKey(decodedCube), key -> new ArrayList<>())
            .add(decodedCube);
      } else {
        directCubes.add(decodedCube);
      }
    }

    List<BakedModelPart> children = new ArrayList<>();
    Map<String, Integer> rotatedPartIndices = new HashMap<>();
    rotatedCubeGroups.values().stream()
        .sorted(
            (left, right) ->
                Float.compare(left.get(0).rotationOrigin()[1], right.get(0).rotationOrigin()[1]))
        .forEach(
            rotatedCubes -> {
              BakedModelPart rotatedPart = bakeRotatedPart(rotatedCubes, rotatedPartIndices);
              children.add(rotatedPart);
            });
    children.addAll(decodedPart.children().stream().map(ModelBakeService::bakePart).toList());

    return new BakedModelPart(
        decodedPart.name(),
        decodedPart.offset(),
        decodedPart.rotation(),
        directCubes.stream().map(ModelBakeService::bakeCube).toList(),
        children);
  }

  private static String rotationKey(DecodedModelCube decodedCube) {
    float[] rotationOrigin = decodedCube.rotationOrigin();
    float[] rotation = decodedCube.rotation();
    return decodedCube.name()
        + "_"
        + rotation[0]
        + "_"
        + rotation[1]
        + "_"
        + rotation[2]
        + "_"
        + rotationOrigin[0]
        + "_"
        + rotationOrigin[1]
        + "_"
        + rotationOrigin[2];
  }

  private static BakedModelPart bakeRotatedPart(
      List<DecodedModelCube> rotatedCubes, Map<String, Integer> rotatedPartIndices) {
    DecodedModelCube firstCube = rotatedCubes.get(0);
    String baseName = firstCube.name();
    int childIndex = rotatedPartIndices.getOrDefault(baseName, 0) + 1;
    rotatedPartIndices.put(baseName, childIndex);
    return new BakedModelPart(
        baseName + "_r" + childIndex,
        firstCube.rotationOrigin(),
        firstCube.rotation(),
        rotatedCubes.stream().map(ModelBakeService::bakeRotatedCube).toList(),
        List.of());
  }

  private static BakedModelCube bakeCube(DecodedModelCube decodedCube) {
    return new BakedModelCube(
        decodedCube.uvOffset(),
        decodedCube.position(),
        decodedCube.dimensions(),
        decodedCube.mirror());
  }

  private static BakedModelCube bakeRotatedCube(DecodedModelCube decodedCube) {
    return new BakedModelCube(
        decodedCube.uvOffset(),
        decodedCube.rotatedPosition(),
        decodedCube.dimensions(),
        decodedCube.mirror());
  }

  private static ModelBakeResult failure(
      ModelCacheKey cacheKey, ModelRenderProfileStatus status, String field, String message) {
    return ModelBakeResult.failure(
        cacheKey, List.of(new ModelRenderProfileValidationIssue(status, field, message)));
  }

  private ModelResourceLookup findModelResource(
      ResourceLocation modelId, ResourceManager resourceManager) {
    boolean foundModelResource = false;
    for (String format : this.decoderRegistry.getDecoderFormats()) {
      Optional<EasyModelDecoder> decoder = this.decoderRegistry.getDecoder(format);
      if (decoder.isEmpty()) {
        continue;
      }

      ResourceLocation modelResourceLocation =
          ModelResourcePaths.modelResourceLocation(modelId, format);
      Optional<Resource> modelResource = resourceManager.getResource(modelResourceLocation);
      if (modelResource.isEmpty()) {
        continue;
      }

      foundModelResource = true;
      if (decoder.get().supports(modelId, modelResource.get())) {
        return new ModelResourceLookup(
            Optional.of(
                new ModelResourceCandidate(
                    modelResourceLocation, decoder.get(), modelResource.get())),
            true);
      }
    }

    return new ModelResourceLookup(Optional.empty(), foundModelResource);
  }

  @Override
  public ModelBakeResult bake(
      EasyModelRenderProfile renderProfile, ResourceManager resourceManager) {
    Objects.requireNonNull(renderProfile, "renderProfile");
    Objects.requireNonNull(resourceManager, "resourceManager");
    ModelCacheKey cacheKey = cacheKey(renderProfile.model(), renderProfile.assetFingerprint());
    Optional<ModelBakeResult> cachedResult = this.cache.get(cacheKey);
    if (cachedResult.isPresent()) {
      return cachedResult.get();
    }

    ModelBakeResult result = bakeUncached(renderProfile, resourceManager, cacheKey);
    this.cache.put(result);
    return result;
  }

  @Override
  public Optional<ModelBakeResult> getCached(ResourceLocation modelId, String assetFingerprint) {
    return this.cache.get(cacheKey(modelId, assetFingerprint));
  }

  @Override
  public void clearCache() {
    this.cache.clear();
  }

  int cachedResultCount() {
    return this.cache.size();
  }

  private ModelBakeResult bakeUncached(
      EasyModelRenderProfile renderProfile,
      ResourceManager resourceManager,
      ModelCacheKey cacheKey) {
    ModelResourceLookup modelResourceLookup =
        findModelResource(renderProfile.model(), resourceManager);
    if (modelResourceLookup.candidate().isEmpty() && !modelResourceLookup.foundModelResource()) {
      return failure(
          cacheKey,
          ModelRenderProfileStatus.MISSING_MODEL,
          "model",
          "Missing model asset for " + renderProfile.model() + ".");
    }

    if (modelResourceLookup.candidate().isEmpty()) {
      return failure(
          cacheKey,
          ModelRenderProfileStatus.MODEL_DECODE_FAILED,
          "model",
          "No decoder found for model " + renderProfile.model() + ".");
    }

    ModelResourceCandidate modelResource = modelResourceLookup.candidate().get();
    try {
      DecodedModel decodedModel =
          modelResource.decoder().decode(renderProfile.model(), modelResource.resource());
      List<ModelRenderProfileValidationIssue> issues =
          new ArrayList<>(decodedModel.validationIssues());
      issues.addAll(validateTexture(renderProfile, resourceManager));
      issues.addAll(validateBodyType(renderProfile.bodyType(), decodedModel));
      if (hasHardFailure(issues)) {
        return ModelBakeResult.failure(cacheKey, issues);
      }

      return ModelBakeResult.success(
          cacheKey, bakeDecoded(renderProfile.bodyType(), decodedModel), issues);
    } catch (EasyModelDecodeException exception) {
      return failure(
          cacheKey,
          ModelRenderProfileStatus.MODEL_DECODE_FAILED,
          "model",
          "Could not decode model "
              + modelResource.resourceLocation()
              + ": "
              + exception.getMessage());
    }
  }

  private record ModelResourceCandidate(
      ResourceLocation resourceLocation, EasyModelDecoder decoder, Resource resource) {}

  private record ModelResourceLookup(
      Optional<ModelResourceCandidate> candidate, boolean foundModelResource) {}
}
