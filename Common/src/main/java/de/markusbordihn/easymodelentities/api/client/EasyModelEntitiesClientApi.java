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

package de.markusbordihn.easymodelentities.api.client;

import com.mojang.blaze3d.vertex.PoseStack;
import de.markusbordihn.easymodelentities.api.EasyModelRenderable;
import de.markusbordihn.easymodelentities.api.data.EasyModelAnimation;
import de.markusbordihn.easymodelentities.api.data.EasyModelAnimationSetting;
import de.markusbordihn.easymodelentities.api.data.EasyModelBodyType;
import de.markusbordihn.easymodelentities.api.data.EasyModelProfileType;
import de.markusbordihn.easymodelentities.api.data.client.EasyModelAnimationInfo;
import de.markusbordihn.easymodelentities.api.data.client.EasyModelAnimationPlayback;
import de.markusbordihn.easymodelentities.api.data.client.EasyModelAnimationPlaybackMode;
import de.markusbordihn.easymodelentities.api.data.client.EasyModelAnimationSequence;
import de.markusbordihn.easymodelentities.api.data.client.EasyModelAnimationTransition;
import de.markusbordihn.easymodelentities.api.data.client.EasyModelBounds;
import de.markusbordihn.easymodelentities.api.data.client.EasyModelEntityRenderOptions;
import de.markusbordihn.easymodelentities.api.data.client.EasyModelItemAnchor;
import de.markusbordihn.easymodelentities.client.render.EasyModelBlockEntityRenderBackend;
import de.markusbordihn.easymodelentities.client.render.EasyModelEntityRenderBackend;
import de.markusbordihn.easymodelentities.client.render.EasyModelItemAnchorResolver;
import de.markusbordihn.easymodelentities.client.render.EasyModelTextureOverrides;
import de.markusbordihn.easymodelentities.client.render.EasyModelTextureVariants;
import de.markusbordihn.easymodelentities.data.EasyModelApiMapper;
import de.markusbordihn.easymodelentities.data.model.ModelAnimationClip;
import de.markusbordihn.easymodelentities.data.model.ModelAnimationClips;
import de.markusbordihn.easymodelentities.data.model.bake.BakedModel;
import de.markusbordihn.easymodelentities.data.profile.ModelType;
import de.markusbordihn.easymodelentities.data.render.EasyModelRenderState;
import de.markusbordihn.easymodelentities.data.renderprofile.EasyModelRenderProfile;
import de.markusbordihn.easymodelentities.registry.EasyModelServices;
import de.markusbordihn.easymodelentities.runtime.EasyModelRuntimeContract;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.level.block.entity.BlockEntity;

public final class EasyModelEntitiesClientApi {

  private EasyModelEntitiesClientApi() {}

  public static <T extends Entity & EasyModelRenderable>
      EasyModelEntityRenderDelegate<T> createRenderDelegate() {
    return new EasyModelEntityRenderDelegate<>();
  }

  public static <T extends BlockEntity & EasyModelRenderable>
      EasyModelBlockEntityRenderDelegate<T> createBlockEntityRenderDelegate() {
    return new EasyModelBlockEntityRenderDelegate<>();
  }

  public static boolean render(
      ResourceLocation profileId,
      PoseStack poseStack,
      MultiBufferSource bufferSource,
      int packedLight,
      float yaw,
      EasyModelEntityRenderOptions options) {
    Objects.requireNonNull(profileId, "profileId");
    Objects.requireNonNull(poseStack, "poseStack");
    Objects.requireNonNull(bufferSource, "bufferSource");
    Optional<EasyModelRenderState> renderState =
        resolveRenderState(profileId, EasyModelAnimationSetting.AUTO);
    if (renderState.isEmpty()) {
      return false;
    }
    EasyModelEntityRenderBackend.render(
        renderState.get(),
        yaw,
        options == null ? EasyModelEntityRenderOptions.DEFAULT : options,
        poseStack,
        bufferSource,
        packedLight);
    return true;
  }

  public static boolean render(
      Entity entity,
      ResourceLocation profileId,
      PoseStack poseStack,
      MultiBufferSource bufferSource,
      int packedLight,
      float yaw,
      float partialTick,
      EasyModelEntityRenderOptions options) {
    Objects.requireNonNull(entity, "entity");
    Objects.requireNonNull(profileId, "profileId");
    Objects.requireNonNull(poseStack, "poseStack");
    Objects.requireNonNull(bufferSource, "bufferSource");
    Optional<EasyModelRenderState> renderState =
        resolveRenderState(profileId, EasyModelAnimationSetting.AUTO);
    if (renderState.isEmpty()) {
      return false;
    }
    EasyModelEntityRenderBackend.render(
        entity,
        renderState.get(),
        yaw,
        partialTick,
        options == null ? EasyModelEntityRenderOptions.DEFAULT : options,
        poseStack,
        bufferSource,
        packedLight);
    return true;
  }

  public static Optional<EasyModelBounds> getModelBounds(ResourceLocation profileId) {
    Objects.requireNonNull(profileId, "profileId");
    return resolveRenderState(profileId, EasyModelAnimationSetting.AUTO)
        .map(EasyModelRenderState::bakedModel)
        .map(BakedModel::bounds)
        .map(EasyModelApiMapper::bounds);
  }

  public static Optional<EasyModelBounds> getDisplayedBounds(ResourceLocation profileId) {
    Objects.requireNonNull(profileId, "profileId");
    return resolveRenderState(profileId, EasyModelAnimationSetting.AUTO)
        .map(renderState -> renderState.bakedModel().bounds().scaled(renderState.scale()))
        .map(EasyModelApiMapper::bounds);
  }

  public static Optional<EasyModelItemAnchor> getItemAnchor(
      ResourceLocation profileId, HumanoidArm arm) {
    Objects.requireNonNull(profileId, "profileId");
    Objects.requireNonNull(arm, "arm");
    return EasyModelItemAnchorResolver.getItemAnchor(profileId, arm);
  }

  public static Optional<EasyModelBodyType> getBodyType(ResourceLocation profileId) {
    Objects.requireNonNull(profileId, "profileId");
    return EasyModelEntityRenderBackend.resolveContract(profileId, EasyModelAnimationSetting.AUTO)
        .map(EasyModelRuntimeContract::bodyType)
        .map(EasyModelApiMapper::bodyType);
  }

  public static List<EasyModelAnimationInfo> listAnimations(ResourceLocation profileId) {
    Objects.requireNonNull(profileId, "profileId");
    return resolveRenderState(profileId, EasyModelAnimationSetting.AUTO)
        .map(EasyModelRenderState::bakedModel)
        .map(BakedModel::animations)
        .map(EasyModelEntitiesClientApi::animationInfoFromClips)
        .orElse(List.of());
  }

  public static Optional<EasyModelAnimationInfo> getAnimationInfo(
      ResourceLocation profileId, String animationName) {
    Objects.requireNonNull(profileId, "profileId");
    Objects.requireNonNull(animationName, "animationName");
    String normalizedName = ModelAnimationClips.normalize(animationName);
    return listAnimations(profileId).stream()
        .filter(animation -> animation.name().equals(normalizedName))
        .findFirst();
  }

  public static List<String> listAnimationVariants(ResourceLocation profileId, String baseName) {
    Objects.requireNonNull(profileId, "profileId");
    Objects.requireNonNull(baseName, "baseName");
    return resolveRenderState(profileId, EasyModelAnimationSetting.AUTO)
        .map(EasyModelRenderState::bakedModel)
        .map(bakedModel -> animationVariants(bakedModel, baseName))
        .orElse(List.of());
  }

  public static List<String> listTextureSlots(ResourceLocation profileId) {
    Objects.requireNonNull(profileId, "profileId");
    return resolveRenderState(profileId, EasyModelAnimationSetting.AUTO)
        .map(EasyModelRenderState::bakedModel)
        .map(EasyModelEntitiesClientApi::textureSlots)
        .orElse(List.of());
  }

  public static List<ResourceLocation> listTextureVariants(
      ResourceLocation profileId, String slot) {
    Objects.requireNonNull(profileId, "profileId");
    Objects.requireNonNull(slot, "slot");
    return resolveRenderState(profileId, EasyModelAnimationSetting.AUTO)
        .map(
            renderState ->
                textureVariants(renderState, slot, Minecraft.getInstance().getResourceManager()))
        .orElse(List.of());
  }

  static List<String> animationVariants(BakedModel bakedModel, String baseName) {
    return bakedModel
        .animationVariants()
        .variantsOf(ModelAnimationClips.baseName(ModelAnimationClips.normalize(baseName)));
  }

  static List<String> textureSlots(BakedModel bakedModel) {
    return bakedModel.textureNames().keySet().stream().sorted().toList();
  }

  static List<ResourceLocation> textureVariants(
      EasyModelRenderState renderState, String slot, ResourceManager resourceManager) {
    return EasyModelTextureOverrides.baseTexture(renderState, slot)
        .map(baseTexture -> EasyModelTextureVariants.variants(baseTexture, resourceManager))
        .orElse(List.of());
  }

  static List<EasyModelAnimationInfo> animationInfoFromClips(
      Map<String, ModelAnimationClip> clips) {
    return clips.values().stream()
        .sorted(Comparator.comparing(ModelAnimationClip::name))
        .map(EasyModelEntitiesClientApi::animationInfo)
        .toList();
  }

  private static EasyModelAnimationInfo animationInfo(ModelAnimationClip clip) {
    int keyframeCount =
        clip.boneTracks().values().stream()
            .mapToInt(track -> track.rotationKeyframes().size() + track.positionKeyframes().size())
            .sum();
    int frameCount =
        clip.framesPerSecond() > 0.0f ? (int) Math.ceil(clip.length() * clip.framesPerSecond()) : 0;
    return new EasyModelAnimationInfo(
        clip.name(),
        clip.length(),
        clip.length() * 20.0f,
        clip.loop(),
        clip.framesPerSecond(),
        frameCount,
        keyframeCount,
        clip.boneTracks().size());
  }

  public static void playAnimation(Entity entity, EasyModelAnimation animation) {
    playAnimation(
        entity,
        animation,
        EasyModelAnimationPlayback.DEFAULT,
        EasyModelAnimationTransition.DEFAULT);
  }

  public static void playAnimation(Entity entity, String animationName) {
    playAnimation(entity, resolveAnimationName(animationName));
  }

  public static void playAnimation(
      Entity entity, EasyModelAnimation animation, EasyModelAnimationTransition transition) {
    playAnimation(entity, animation, EasyModelAnimationPlayback.DEFAULT, transition);
  }

  public static void playAnimation(
      Entity entity,
      EasyModelAnimation animation,
      EasyModelAnimationPlayback playback,
      EasyModelAnimationTransition transition) {
    EasyModelEntityRenderBackend.playAnimation(
        Objects.requireNonNull(entity, "entity"),
        Objects.requireNonNull(animation, "animation"),
        Objects.requireNonNull(playback, "playback"),
        Objects.requireNonNull(transition, "transition"));
  }

  public static void playAnimation(
      Entity entity,
      EasyModelAnimation animation,
      EasyModelAnimationPlaybackMode playbackMode,
      EasyModelAnimationTransition transition) {
    playAnimation(
        entity,
        animation,
        new EasyModelAnimationPlayback(
            Objects.requireNonNull(playbackMode, "playbackMode"), 1, 0.0f),
        transition);
  }

  public static void playAnimation(
      Entity entity, String animationName, EasyModelAnimationTransition transition) {
    playAnimation(entity, resolveAnimationName(animationName), transition);
  }

  public static void playAnimation(
      Entity entity,
      String animationName,
      EasyModelAnimationPlayback playback,
      EasyModelAnimationTransition transition) {
    playAnimation(entity, resolveAnimationName(animationName), playback, transition);
  }

  public static void playAnimation(
      Entity entity,
      String animationName,
      EasyModelAnimationPlaybackMode playbackMode,
      EasyModelAnimationTransition transition) {
    playAnimation(entity, resolveAnimationName(animationName), playbackMode, transition);
  }

  public static void playAnimationSequence(Entity entity, EasyModelAnimationSequence sequence) {
    EasyModelEntityRenderBackend.playAnimationSequence(
        Objects.requireNonNull(entity, "entity"), Objects.requireNonNull(sequence, "sequence"));
  }

  public static void restartAnimation(Entity entity) {
    EasyModelEntityRenderBackend.restartAnimation(Objects.requireNonNull(entity, "entity"));
  }

  public static void stopAnimation(Entity entity) {
    stopAnimation(entity, EasyModelAnimationTransition.IMMEDIATE);
  }

  public static void stopAnimation(Entity entity, EasyModelAnimationTransition transition) {
    EasyModelEntityRenderBackend.stopAnimation(
        Objects.requireNonNull(entity, "entity"), Objects.requireNonNull(transition, "transition"));
  }

  public static void playAnimation(BlockEntity blockEntity, EasyModelAnimation animation) {
    playAnimation(
        blockEntity,
        animation,
        EasyModelAnimationPlayback.DEFAULT,
        EasyModelAnimationTransition.DEFAULT);
  }

  public static void playAnimation(BlockEntity blockEntity, String animationName) {
    playAnimation(blockEntity, resolveAnimationName(animationName));
  }

  public static void playAnimation(
      BlockEntity blockEntity,
      EasyModelAnimation animation,
      EasyModelAnimationTransition transition) {
    playAnimation(blockEntity, animation, EasyModelAnimationPlayback.DEFAULT, transition);
  }

  public static void playAnimation(
      BlockEntity blockEntity,
      EasyModelAnimation animation,
      EasyModelAnimationPlayback playback,
      EasyModelAnimationTransition transition) {
    EasyModelBlockEntityRenderBackend.playAnimation(
        Objects.requireNonNull(blockEntity, "blockEntity"),
        Objects.requireNonNull(animation, "animation"),
        Objects.requireNonNull(playback, "playback"),
        Objects.requireNonNull(transition, "transition"));
  }

  public static void playAnimation(
      BlockEntity blockEntity,
      EasyModelAnimation animation,
      EasyModelAnimationPlaybackMode playbackMode,
      EasyModelAnimationTransition transition) {
    playAnimation(
        blockEntity,
        animation,
        new EasyModelAnimationPlayback(
            Objects.requireNonNull(playbackMode, "playbackMode"), 1, 0.0f),
        transition);
  }

  public static void playAnimation(
      BlockEntity blockEntity, String animationName, EasyModelAnimationTransition transition) {
    playAnimation(blockEntity, resolveAnimationName(animationName), transition);
  }

  public static void playAnimation(
      BlockEntity blockEntity,
      String animationName,
      EasyModelAnimationPlayback playback,
      EasyModelAnimationTransition transition) {
    playAnimation(blockEntity, resolveAnimationName(animationName), playback, transition);
  }

  public static void playAnimation(
      BlockEntity blockEntity,
      String animationName,
      EasyModelAnimationPlaybackMode playbackMode,
      EasyModelAnimationTransition transition) {
    playAnimation(blockEntity, resolveAnimationName(animationName), playbackMode, transition);
  }

  public static void playAnimationSequence(
      BlockEntity blockEntity, EasyModelAnimationSequence sequence) {
    EasyModelBlockEntityRenderBackend.playAnimationSequence(
        Objects.requireNonNull(blockEntity, "blockEntity"),
        Objects.requireNonNull(sequence, "sequence"));
  }

  public static void restartAnimation(BlockEntity blockEntity) {
    EasyModelBlockEntityRenderBackend.restartAnimation(
        Objects.requireNonNull(blockEntity, "blockEntity"));
  }

  public static void stopAnimation(BlockEntity blockEntity) {
    stopAnimation(blockEntity, EasyModelAnimationTransition.IMMEDIATE);
  }

  public static void stopAnimation(
      BlockEntity blockEntity, EasyModelAnimationTransition transition) {
    EasyModelBlockEntityRenderBackend.stopAnimation(
        Objects.requireNonNull(blockEntity, "blockEntity"),
        Objects.requireNonNull(transition, "transition"));
  }

  public static List<ResourceLocation> listRenderableProfileIds() {
    return renderableProfileIds(profileId -> true);
  }

  public static List<ResourceLocation> listRenderableProfileIds(EasyModelProfileType modelType) {
    Objects.requireNonNull(modelType, "modelType");
    return renderableProfileIds(
        profileId ->
            EasyModelApiMapper.profileType(ModelType.fromProfileId(profileId)) == modelType);
  }

  public static List<ResourceLocation> listRenderableEntityProfileIds() {
    return listRenderableProfileIds(EasyModelProfileType.ENTITY);
  }

  public static List<ResourceLocation> listRenderableBlockEntityProfileIds() {
    return listRenderableProfileIds(EasyModelProfileType.BLOCK_ENTITY);
  }

  static EasyModelAnimation resolveAnimationName(String animationName) {
    Objects.requireNonNull(animationName, "animationName");
    return EasyModelAnimation.parse(animationName)
        .orElseGet(() -> EasyModelAnimation.named(animationName));
  }

  private static List<ResourceLocation> renderableProfileIds(
      Predicate<ResourceLocation> profileFilter) {
    return EasyModelServices.renderProfileService().getRenderProfiles().stream()
        .filter(EasyModelRenderProfile::isRenderable)
        .map(EasyModelRenderProfile::id)
        .filter(profileFilter)
        .sorted(Comparator.comparing(ResourceLocation::toString))
        .toList();
  }

  private static Optional<EasyModelRenderState> resolveRenderState(
      ResourceLocation profileId, EasyModelAnimationSetting animation) {
    return EasyModelEntityRenderBackend.resolveContract(profileId, animation)
        .map(EasyModelEntityRenderBackend::resolveRenderState);
  }
}
