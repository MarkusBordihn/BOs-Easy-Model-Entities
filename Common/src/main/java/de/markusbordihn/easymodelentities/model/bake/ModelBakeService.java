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

import de.markusbordihn.easymodelentities.data.model.ModelPartType;
import de.markusbordihn.easymodelentities.data.model.Vec3f;
import de.markusbordihn.easymodelentities.data.model.bake.*;
import de.markusbordihn.easymodelentities.data.model.decoder.DecodedModel;
import de.markusbordihn.easymodelentities.data.model.decoder.DecodedModelCube;
import de.markusbordihn.easymodelentities.data.model.decoder.DecodedModelPart;
import de.markusbordihn.easymodelentities.data.profile.ModelBodyType;
import de.markusbordihn.easymodelentities.data.renderprofile.EasyModelRenderProfile;
import de.markusbordihn.easymodelentities.data.renderprofile.ModelRenderProfileStatus;
import de.markusbordihn.easymodelentities.data.renderprofile.ModelRenderProfileValidationIssue;
import de.markusbordihn.easymodelentities.model.decoder.EasyModelDecodeException;
import de.markusbordihn.easymodelentities.model.decoder.EasyModelDecoder;
import de.markusbordihn.easymodelentities.model.decoder.EasyModelDecoderRegistry;
import de.markusbordihn.easymodelentities.model.decoder.ModelDecoderRegistry;
import de.markusbordihn.easymodelentities.registry.ModelResourcePaths;
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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class ModelBakeService implements EasyModelBakeService {

  private static final Logger log =
      LogManager.getLogger(de.markusbordihn.easymodelentities.Constants.LOG_NAME);

  private final EasyModelDecoderRegistry decoderRegistry;
  private final ModelCache cache = new ModelCache();

  public ModelBakeService(EasyModelDecoderRegistry decoderRegistry) {
    this.decoderRegistry = Objects.requireNonNull(decoderRegistry, "decoderRegistry");
  }

  public static ModelBakeService createDefault() {
    return new ModelBakeService(ModelDecoderRegistry.createDefault());
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
          List.of(
              ModelPartType.ROOT.getTagName(),
              ModelPartType.HEAD.getTagName(),
              ModelPartType.BODY.getTagName(),
              ModelPartType.LEFT_ARM.getTagName(),
              ModelPartType.RIGHT_ARM.getTagName(),
              ModelPartType.LEFT_LEG.getTagName(),
              ModelPartType.RIGHT_LEG.getTagName());
      case QUADRUPED ->
          List.of(
              ModelPartType.ROOT.getTagName(),
              ModelPartType.BODY.getTagName(),
              ModelPartType.HEAD.getTagName(),
              ModelPartType.FRONT_LEFT_LEG.getTagName(),
              ModelPartType.FRONT_RIGHT_LEG.getTagName(),
              ModelPartType.BACK_LEFT_LEG.getTagName(),
              ModelPartType.BACK_RIGHT_LEG.getTagName());
      case AQUATIC, AMPHIBIOUS ->
          List.of(ModelPartType.ROOT.getTagName(), ModelPartType.BODY.getTagName());
      case WINGED ->
          List.of(
              ModelPartType.ROOT.getTagName(),
              ModelPartType.BODY.getTagName(),
              ModelPartType.HEAD.getTagName(),
              ModelPartType.LEFT_WING.getTagName(),
              ModelPartType.RIGHT_WING.getTagName());
      case WINGED_HUMANOID ->
          List.of(
              ModelPartType.ROOT.getTagName(),
              ModelPartType.BODY.getTagName(),
              ModelPartType.HEAD.getTagName(),
              ModelPartType.LEFT_ARM.getTagName(),
              ModelPartType.RIGHT_ARM.getTagName(),
              ModelPartType.LEFT_WING.getTagName(),
              ModelPartType.RIGHT_WING.getTagName());
      case ARTHROPOD ->
          List.of(
              ModelPartType.ROOT.getTagName(),
              ModelPartType.BODY.getTagName(),
              ModelPartType.HEAD.getTagName(),
              ModelPartType.FRONT_LEFT_LEG.getTagName(),
              ModelPartType.FRONT_RIGHT_LEG.getTagName(),
              ModelPartType.MIDDLE_FRONT_LEFT_LEG.getTagName(),
              ModelPartType.MIDDLE_FRONT_RIGHT_LEG.getTagName(),
              ModelPartType.MIDDLE_BACK_LEFT_LEG.getTagName(),
              ModelPartType.MIDDLE_BACK_RIGHT_LEG.getTagName(),
              ModelPartType.BACK_LEFT_LEG.getTagName(),
              ModelPartType.BACK_RIGHT_LEG.getTagName());
      case CUBOID, FLOATING ->
          List.of(ModelPartType.ROOT.getTagName(), ModelPartType.BODY.getTagName());
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

  private static BakedModel bakeDecoded(
      ModelBodyType bodyType, DecodedModel decodedModel, Map<Integer, ResourceLocation> textures) {
    float textureWidth = decodedModel.textureWidth();
    float textureHeight = decodedModel.textureHeight();
    List<BakedModelPart> rootParts =
        decodedModel.rootParts().stream()
            .map(part -> bakePart(part, textureWidth, textureHeight))
            .toList();
    if (bodyType == ModelBodyType.STATIC
        && rootParts.stream()
            .noneMatch(part -> ModelPartType.ROOT.getTagName().equals(part.name()))) {
      rootParts =
          List.of(
              new BakedModelPart(
                  ModelPartType.ROOT.getTagName(),
                  new Vec3f(0.0f, 24.0f, 0.0f),
                  Vec3f.ZERO,
                  List.of(),
                  rootParts));
    }

    ModelFaceOcclusionCuller.Result occlusion = ModelFaceOcclusionCuller.cull(rootParts, bodyType);
    rootParts = occlusion.rootParts();
    ModelEmptyCubePruner.Result pruned = ModelEmptyCubePruner.prune(rootParts);
    rootParts = pruned.rootParts();
    boolean cullBackfaces = bodyType == ModelBodyType.STATIC;
    if (log.isDebugEnabled()) {
      log.debug(
          "Baked model {} (body type {}): occlusion-culled {} faces, dropped {} fully hidden cubes,"
              + " backface culling {}.",
          decodedModel.modelId(),
          bodyType.getSerializedName(),
          occlusion.culledFaces(),
          pruned.droppedCubes(),
          cullBackfaces ? "enabled" : "disabled");
    }

    return new BakedModel(
        decodedModel.modelId(),
        decodedModel.textureWidth(),
        decodedModel.textureHeight(),
        rootParts,
        textures,
        cullBackfaces,
        ModelBoundsCalculator.compute(rootParts));
  }

  private static BakedModelPart bakePart(
      DecodedModelPart decodedPart, float textureWidth, float textureHeight) {
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
                Float.compare(left.get(0).rotationOrigin().y(), right.get(0).rotationOrigin().y()))
        .forEach(
            rotatedCubes -> {
              BakedModelPart rotatedPart =
                  bakeRotatedPart(rotatedCubes, rotatedPartIndices, textureWidth, textureHeight);
              children.add(rotatedPart);
            });
    children.addAll(
        decodedPart.children().stream()
            .map(part -> bakePart(part, textureWidth, textureHeight))
            .toList());

    return new BakedModelPart(
        decodedPart.name(),
        decodedPart.offset(),
        decodedPart.rotation(),
        directCubes.stream().map(cube -> bakeCube(cube, textureWidth, textureHeight)).toList(),
        children);
  }

  private static String rotationKey(DecodedModelCube decodedCube) {
    Vec3f rotationOrigin = decodedCube.rotationOrigin();
    Vec3f rotation = decodedCube.rotation();
    return decodedCube.name()
        + "_"
        + rotation.x()
        + "_"
        + rotation.y()
        + "_"
        + rotation.z()
        + "_"
        + rotationOrigin.x()
        + "_"
        + rotationOrigin.y()
        + "_"
        + rotationOrigin.z();
  }

  private static BakedModelPart bakeRotatedPart(
      List<DecodedModelCube> rotatedCubes,
      Map<String, Integer> rotatedPartIndices,
      float textureWidth,
      float textureHeight) {
    DecodedModelCube firstCube = rotatedCubes.get(0);
    String baseName = firstCube.name();
    int childIndex = rotatedPartIndices.getOrDefault(baseName, 0) + 1;
    rotatedPartIndices.put(baseName, childIndex);
    return new BakedModelPart(
        baseName + "_r" + childIndex,
        firstCube.rotationOrigin(),
        firstCube.rotation(),
        rotatedCubes.stream()
            .map(cube -> bakeRotatedCube(cube, textureWidth, textureHeight))
            .toList(),
        List.of());
  }

  private static BakedModelCube bakeCube(
      DecodedModelCube decodedCube, float textureWidth, float textureHeight) {
    return new BakedModelCube(
        decodedCube.uvOffset(),
        decodedCube.faceUvs().scale(textureWidth, textureHeight),
        decodedCube.position(),
        decodedCube.dimensions(),
        decodedCube.mirror(),
        decodedCube.textureIndex(),
        decodedCube.faceVisibility());
  }

  private static BakedModelCube bakeRotatedCube(
      DecodedModelCube decodedCube, float textureWidth, float textureHeight) {
    return new BakedModelCube(
        decodedCube.uvOffset(),
        decodedCube.faceUvs().scale(textureWidth, textureHeight),
        decodedCube.rotatedPosition(),
        decodedCube.dimensions(),
        decodedCube.mirror(),
        decodedCube.textureIndex(),
        decodedCube.faceVisibility());
  }

  private static ModelBakeResult failure(
      ModelCacheKey cacheKey, ModelRenderProfileStatus status, String field, String message) {
    return ModelBakeResult.failure(
        cacheKey,
        ModelFallbackFactory.createFallback(cacheKey.modelId()),
        List.of(new ModelRenderProfileValidationIssue(status, field, message)));
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
    ModelCacheKey cacheKey = cacheKey(renderProfile.model(), cacheDiscriminator(renderProfile));
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
      ModelTextureResolver.ResolvedTextures resolvedTextures =
          ModelTextureResolver.resolve(renderProfile, decodedModel, resourceManager);
      issues.addAll(resolvedTextures.issues());
      issues.addAll(validateBodyType(renderProfile.bodyType(), decodedModel));
      if (hasHardFailure(issues)) {
        return ModelBakeResult.failure(
            cacheKey, ModelFallbackFactory.createFallback(cacheKey.modelId()), issues);
      }

      return ModelBakeResult.success(
          cacheKey,
          bakeDecoded(renderProfile.bodyType(), decodedModel, resolvedTextures.textures()),
          issues);
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
