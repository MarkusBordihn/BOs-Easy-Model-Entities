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

package de.markusbordihn.easymodelentities.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import de.markusbordihn.easymodelentities.Constants;
import de.markusbordihn.easymodelentities.api.client.EasyModelPartAnimator;
import de.markusbordihn.easymodelentities.api.client.EasyModelPartPoseListener;
import de.markusbordihn.easymodelentities.api.data.EasyModelAnimation;
import de.markusbordihn.easymodelentities.api.data.EasyModelTextureBlend;
import de.markusbordihn.easymodelentities.api.data.EasyModelTextureSetting;
import de.markusbordihn.easymodelentities.api.data.EasyModelVec3f;
import de.markusbordihn.easymodelentities.api.data.client.EasyModelHeadLook;
import de.markusbordihn.easymodelentities.api.data.client.EasyModelPartAnimationContext;
import de.markusbordihn.easymodelentities.api.data.client.EasyModelPartAnimationMode;
import de.markusbordihn.easymodelentities.api.data.client.EasyModelPartPose;
import de.markusbordihn.easymodelentities.api.data.client.EasyModelPartTransform;
import de.markusbordihn.easymodelentities.data.EasyModelApiMapper;
import de.markusbordihn.easymodelentities.data.model.CubeFaceVisibility;
import de.markusbordihn.easymodelentities.data.model.FaceUv;
import de.markusbordihn.easymodelentities.data.model.ModelAnimationBoneTrack;
import de.markusbordihn.easymodelentities.data.model.ModelAnimationClip;
import de.markusbordihn.easymodelentities.data.model.ModelAnimationClips;
import de.markusbordihn.easymodelentities.data.model.ModelCubeFace;
import de.markusbordihn.easymodelentities.data.model.ModelPartType;
import de.markusbordihn.easymodelentities.data.model.Vec3f;
import de.markusbordihn.easymodelentities.data.model.bake.BakedModel;
import de.markusbordihn.easymodelentities.data.model.bake.BakedModelCube;
import de.markusbordihn.easymodelentities.data.model.bake.BakedModelPart;
import de.markusbordihn.easymodelentities.data.profile.ModelBodyType;
import de.markusbordihn.easymodelentities.data.render.EasyModelRenderState;
import de.markusbordihn.easymodelentities.data.renderprofile.ModelAnimationMode;
import de.markusbordihn.easymodelentities.data.renderprofile.ModelGaitType;
import de.markusbordihn.easymodelentities.event.EasyModelReloadDispatcher;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.IntFunction;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public final class EasyModelBakedModelRenderer {

  private static final Logger log = LogManager.getLogger(Constants.LOG_NAME);
  private static final int MISSING_CLIP_WARN_CACHE_LIMIT = 512;
  private static final ThreadLocal<float[]> ANIMATION_SAMPLE =
      ThreadLocal.withInitial(() -> new float[6]);
  private static final Map<String, Boolean> MISSING_CLIP_WARNINGS =
      Collections.synchronizedMap(
          new LinkedHashMap<>() {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, Boolean> eldest) {
              return size() > MISSING_CLIP_WARN_CACHE_LIMIT;
            }
          });
  private static final float PIXEL = 1.0f / 16.0f;
  private static final float WALK_SWING_FREQUENCY = 0.6662f;
  private static final float WALK_ROTATION_SCALE = 1.4f;
  private static final float MIN_QUADRUPED_WALK_ROTATION = Mth.PI / 4.0f;
  private static final float WING_FOLD_ANGLE = 1.4f;
  private static final float WING_AIR_FLAP_AMPLITUDE = 0.9f;
  private static final float WING_AIR_FLAP_SPEED = 0.7f;
  private static final float IDLE_BREATH_ROTATION = 0.025f;
  private static final float IDLE_WING_ROTATION = 0.12f;
  private static final float IDLE_TAIL_ROTATION = 0.18f;
  private static final float CUBOID_IDLE_SPEED = 0.1f;
  private static final float CUBOID_LID_ROTATION = 0.16f;
  private static final float CUBOID_BODY_ROTATION = 0.03f;

  static {
    EasyModelReloadDispatcher.addProfileReloadListener(EasyModelBakedModelRenderer::clearCaches);
    EasyModelReloadDispatcher.addRenderProfileReloadListener(
        EasyModelBakedModelRenderer::clearCaches);
  }

  private EasyModelBakedModelRenderer() {}

  private static void clearCaches() {
    MISSING_CLIP_WARNINGS.clear();
    EasyModelEntityRenderBackend.clearAnimationVariants();
    EasyModelBlockEntityRenderBackend.clearAnimationVariants();
  }

  public static void render(
      BakedModel bakedModel,
      EasyModelRenderState renderState,
      float limbSwing,
      float limbSwingAmount,
      PoseStack poseStack,
      VertexConsumer vertexConsumer,
      int packedLight) {
    render(
        bakedModel,
        renderState,
        limbSwing,
        limbSwingAmount,
        0.0f,
        poseStack,
        vertexConsumer,
        packedLight);
  }

  public static void render(
      BakedModel bakedModel,
      EasyModelRenderState renderState,
      float limbSwing,
      float limbSwingAmount,
      float ageInTicks,
      EasyModelPartAnimator partAnimator,
      PoseStack poseStack,
      VertexConsumer vertexConsumer,
      int packedLight) {
    render(
        bakedModel,
        renderState,
        limbSwing,
        limbSwingAmount,
        ageInTicks,
        partAnimator,
        EasyModelPartAnimationMode.ADD,
        poseStack,
        vertexConsumer,
        packedLight);
  }

  public static void render(
      BakedModel bakedModel,
      EasyModelRenderState renderState,
      float limbSwing,
      float limbSwingAmount,
      float ageInTicks,
      EasyModelPartAnimator partAnimator,
      EasyModelPartAnimationMode partAnimationMode,
      PoseStack poseStack,
      VertexConsumer vertexConsumer,
      int packedLight) {
    render(
        bakedModel,
        renderState,
        limbSwing,
        limbSwingAmount,
        ageInTicks,
        0.0f,
        poseStack,
        vertexConsumer,
        packedLight,
        partAnimator,
        partAnimationMode);
  }

  public static void render(
      BakedModel bakedModel,
      EasyModelRenderState renderState,
      float limbSwing,
      float limbSwingAmount,
      float ageInTicks,
      float airborneAmount,
      EasyModelPartAnimator partAnimator,
      EasyModelPartAnimationMode partAnimationMode,
      PoseStack poseStack,
      VertexConsumer vertexConsumer,
      int packedLight) {
    render(
        bakedModel,
        renderState,
        limbSwing,
        limbSwingAmount,
        ageInTicks,
        airborneAmount,
        poseStack,
        vertexConsumer,
        packedLight,
        partAnimator,
        partAnimationMode);
  }

  public static void render(
      BakedModel bakedModel,
      EasyModelRenderState renderState,
      float limbSwing,
      float limbSwingAmount,
      float ageInTicks,
      PoseStack poseStack,
      VertexConsumer vertexConsumer,
      int packedLight) {
    render(
        bakedModel,
        renderState,
        limbSwing,
        limbSwingAmount,
        ageInTicks,
        EasyModelPartAnimator.NONE,
        EasyModelPartAnimationMode.ADD,
        poseStack,
        vertexConsumer,
        packedLight);
  }

  public static void render(
      BakedModel bakedModel,
      EasyModelRenderState renderState,
      float limbSwing,
      float limbSwingAmount,
      float ageInTicks,
      float airborneAmount,
      EasyModelPartAnimator partAnimator,
      EasyModelPartAnimationMode partAnimationMode,
      PoseStack poseStack,
      MultiBufferSource bufferSource,
      int packedLight) {
    render(
        bakedModel,
        renderState,
        limbSwing,
        limbSwingAmount,
        ageInTicks,
        airborneAmount,
        EasyModelAnimation.AUTO,
        partAnimator,
        partAnimationMode,
        poseStack,
        bufferSource,
        packedLight);
  }

  public static void render(
      BakedModel bakedModel,
      EasyModelRenderState renderState,
      float limbSwing,
      float limbSwingAmount,
      float ageInTicks,
      float airborneAmount,
      EasyModelAnimation animation,
      EasyModelPartAnimator partAnimator,
      EasyModelPartAnimationMode partAnimationMode,
      PoseStack poseStack,
      MultiBufferSource bufferSource,
      int packedLight) {
    render(
        bakedModel,
        renderState,
        limbSwing,
        limbSwingAmount,
        ageInTicks,
        airborneAmount,
        0.0f,
        animation,
        partAnimator,
        partAnimationMode,
        EasyModelPartPoseListener.NONE,
        poseStack,
        bufferSource,
        packedLight);
  }

  public static void render(
      BakedModel bakedModel,
      EasyModelRenderState renderState,
      float limbSwing,
      float limbSwingAmount,
      float ageInTicks,
      float airborneAmount,
      float attackAmount,
      EasyModelAnimation animation,
      EasyModelPartAnimator partAnimator,
      EasyModelPartAnimationMode partAnimationMode,
      EasyModelPartPoseListener partPoseListener,
      PoseStack poseStack,
      MultiBufferSource bufferSource,
      int packedLight) {
    render(
        bakedModel,
        renderState,
        limbSwing,
        limbSwingAmount,
        ageInTicks,
        airborneAmount,
        attackAmount,
        EasyModelHeadLook.NONE,
        animation,
        partAnimator,
        partAnimationMode,
        partPoseListener,
        poseStack,
        bufferSource,
        packedLight);
  }

  public static void render(
      BakedModel bakedModel,
      EasyModelRenderState renderState,
      float limbSwing,
      float limbSwingAmount,
      float ageInTicks,
      float airborneAmount,
      float attackAmount,
      EasyModelHeadLook headLook,
      EasyModelAnimation animation,
      EasyModelPartAnimator partAnimator,
      EasyModelPartAnimationMode partAnimationMode,
      EasyModelPartPoseListener partPoseListener,
      PoseStack poseStack,
      MultiBufferSource bufferSource,
      int packedLight) {
    render(
        bakedModel,
        renderState,
        limbSwing,
        limbSwingAmount,
        ageInTicks,
        airborneAmount,
        attackAmount,
        headLook,
        animation,
        EasyModelTextureSetting.EMPTY,
        partAnimator,
        partAnimationMode,
        partPoseListener,
        poseStack,
        bufferSource,
        packedLight);
  }

  public static void render(
      BakedModel bakedModel,
      EasyModelRenderState renderState,
      float limbSwing,
      float limbSwingAmount,
      float ageInTicks,
      float airborneAmount,
      float attackAmount,
      EasyModelHeadLook headLook,
      EasyModelAnimation animation,
      EasyModelTextureSetting textureSetting,
      EasyModelPartAnimator partAnimator,
      EasyModelPartAnimationMode partAnimationMode,
      EasyModelPartPoseListener partPoseListener,
      PoseStack poseStack,
      MultiBufferSource bufferSource,
      int packedLight) {
    Objects.requireNonNull(renderState, "renderState");
    Objects.requireNonNull(bufferSource, "bufferSource");
    boolean cullBackfaces = bakedModel.cullBackfaces();
    EasyModelResolvedTextures resolvedTextures =
        EasyModelTextureOverrides.resolve(renderState, textureSetting);
    IntFunction<VertexConsumer> bufferProvider =
        textureIndex -> {
          ResourceLocation texture = textureFor(resolvedTextures, renderState, textureIndex);
          return bufferSource.getBuffer(
              renderType(texture, resolvedTextures.blend(textureIndex), cullBackfaces));
        };
    render(
        bakedModel,
        renderState,
        limbSwing,
        limbSwingAmount,
        airborneAmount,
        attackAmount,
        headLook,
        EasyModelAnimationPlaybackFrame.single(animation, ageInTicks),
        EasyModelAnimationVariantFrame.NONE,
        poseStack,
        bufferProvider,
        packedLight,
        partAnimator,
        partAnimationMode,
        partPoseListener);
  }

  static void render(
      BakedModel bakedModel,
      EasyModelRenderState renderState,
      float limbSwing,
      float limbSwingAmount,
      float ageInTicks,
      float airborneAmount,
      float attackAmount,
      EasyModelHeadLook headLook,
      EasyModelAnimation animation,
      PoseStack poseStack,
      IntFunction<VertexConsumer> bufferProvider,
      int packedLight,
      EasyModelPartAnimator partAnimator,
      EasyModelPartAnimationMode partAnimationMode,
      EasyModelPartPoseListener partPoseListener) {
    render(
        bakedModel,
        renderState,
        limbSwing,
        limbSwingAmount,
        airborneAmount,
        attackAmount,
        headLook,
        EasyModelAnimationPlaybackFrame.single(animation, ageInTicks),
        EasyModelAnimationVariantFrame.NONE,
        poseStack,
        bufferProvider,
        packedLight,
        partAnimator,
        partAnimationMode,
        partPoseListener);
  }

  static void render(
      BakedModel bakedModel,
      EasyModelRenderState renderState,
      float limbSwing,
      float limbSwingAmount,
      float airborneAmount,
      float attackAmount,
      EasyModelHeadLook headLook,
      EasyModelAnimationPlaybackFrame playbackFrame,
      EasyModelPartAnimator partAnimator,
      EasyModelPartAnimationMode partAnimationMode,
      EasyModelPartPoseListener partPoseListener,
      PoseStack poseStack,
      MultiBufferSource bufferSource,
      int packedLight) {
    render(
        bakedModel,
        renderState,
        limbSwing,
        limbSwingAmount,
        airborneAmount,
        attackAmount,
        headLook,
        playbackFrame,
        EasyModelAnimationVariantFrame.NONE,
        partAnimator,
        partAnimationMode,
        partPoseListener,
        poseStack,
        bufferSource,
        packedLight);
  }

  static void render(
      BakedModel bakedModel,
      EasyModelRenderState renderState,
      float limbSwing,
      float limbSwingAmount,
      float airborneAmount,
      float attackAmount,
      EasyModelHeadLook headLook,
      EasyModelAnimationPlaybackFrame playbackFrame,
      EasyModelAnimationVariantFrame variantFrame,
      EasyModelPartAnimator partAnimator,
      EasyModelPartAnimationMode partAnimationMode,
      EasyModelPartPoseListener partPoseListener,
      PoseStack poseStack,
      MultiBufferSource bufferSource,
      int packedLight) {
    render(
        bakedModel,
        renderState,
        limbSwing,
        limbSwingAmount,
        airborneAmount,
        attackAmount,
        headLook,
        playbackFrame,
        variantFrame,
        EasyModelTextureSetting.EMPTY,
        partAnimator,
        partAnimationMode,
        partPoseListener,
        poseStack,
        bufferSource,
        packedLight);
  }

  static void render(
      BakedModel bakedModel,
      EasyModelRenderState renderState,
      float limbSwing,
      float limbSwingAmount,
      float airborneAmount,
      float attackAmount,
      EasyModelHeadLook headLook,
      EasyModelAnimationPlaybackFrame playbackFrame,
      EasyModelAnimationVariantFrame variantFrame,
      EasyModelTextureSetting textureSetting,
      EasyModelPartAnimator partAnimator,
      EasyModelPartAnimationMode partAnimationMode,
      EasyModelPartPoseListener partPoseListener,
      PoseStack poseStack,
      MultiBufferSource bufferSource,
      int packedLight) {
    Objects.requireNonNull(renderState, "renderState");
    Objects.requireNonNull(bufferSource, "bufferSource");
    boolean cullBackfaces = bakedModel.cullBackfaces();
    EasyModelResolvedTextures resolvedTextures =
        EasyModelTextureOverrides.resolve(renderState, textureSetting);
    IntFunction<VertexConsumer> bufferProvider =
        textureIndex -> {
          ResourceLocation texture = textureFor(resolvedTextures, renderState, textureIndex);
          return bufferSource.getBuffer(
              renderType(texture, resolvedTextures.blend(textureIndex), cullBackfaces));
        };
    render(
        bakedModel,
        renderState,
        limbSwing,
        limbSwingAmount,
        airborneAmount,
        attackAmount,
        headLook,
        playbackFrame,
        variantFrame,
        poseStack,
        bufferProvider,
        packedLight,
        partAnimator,
        partAnimationMode,
        partPoseListener);
  }

  private static RenderType renderType(
      ResourceLocation texture, EasyModelTextureBlend blend, boolean cullBackfaces) {
    if (blend == EasyModelTextureBlend.TRANSLUCENT) {
      return cullBackfaces
          ? RenderType.entityTranslucentCull(texture)
          : RenderType.entityTranslucent(texture);
    }

    return cullBackfaces
        ? RenderType.entityCutout(texture)
        : RenderType.entityCutoutNoCull(texture);
  }

  static ResourceLocation textureFor(
      EasyModelResolvedTextures resolvedTextures,
      EasyModelRenderState renderState,
      int textureIndex) {
    ResourceLocation override = resolvedTextures.texture(textureIndex);
    if (override != null) {
      return override;
    }
    if (renderState.textures().isEmpty()) {
      return renderState.texture();
    }

    return renderState.textures().getOrDefault(textureIndex, renderState.texture());
  }

  private static void render(
      BakedModel bakedModel,
      EasyModelRenderState renderState,
      float limbSwing,
      float limbSwingAmount,
      float ageInTicks,
      float airborneAmount,
      PoseStack poseStack,
      VertexConsumer vertexConsumer,
      int packedLight,
      EasyModelPartAnimator partAnimator,
      EasyModelPartAnimationMode partAnimationMode) {
    render(
        bakedModel,
        renderState,
        limbSwing,
        limbSwingAmount,
        airborneAmount,
        0.0f,
        EasyModelHeadLook.NONE,
        EasyModelAnimationPlaybackFrame.single(EasyModelAnimation.AUTO, ageInTicks),
        EasyModelAnimationVariantFrame.NONE,
        poseStack,
        textureIndex -> vertexConsumer,
        packedLight,
        partAnimator,
        partAnimationMode,
        EasyModelPartPoseListener.NONE);
  }

  static void render(
      BakedModel bakedModel,
      EasyModelRenderState renderState,
      float limbSwing,
      float limbSwingAmount,
      float airborneAmount,
      float attackAmount,
      EasyModelHeadLook headLook,
      EasyModelAnimationPlaybackFrame playbackFrame,
      PoseStack poseStack,
      IntFunction<VertexConsumer> bufferProvider,
      int packedLight,
      EasyModelPartAnimator partAnimator,
      EasyModelPartAnimationMode partAnimationMode,
      EasyModelPartPoseListener partPoseListener) {
    render(
        bakedModel,
        renderState,
        limbSwing,
        limbSwingAmount,
        airborneAmount,
        attackAmount,
        headLook,
        playbackFrame,
        EasyModelAnimationVariantFrame.NONE,
        poseStack,
        bufferProvider,
        packedLight,
        partAnimator,
        partAnimationMode,
        partPoseListener);
  }

  static void render(
      BakedModel bakedModel,
      EasyModelRenderState renderState,
      float limbSwing,
      float limbSwingAmount,
      float airborneAmount,
      float attackAmount,
      EasyModelHeadLook headLook,
      EasyModelAnimationPlaybackFrame playbackFrame,
      EasyModelAnimationVariantFrame variantFrame,
      PoseStack poseStack,
      IntFunction<VertexConsumer> bufferProvider,
      int packedLight,
      EasyModelPartAnimator partAnimator,
      EasyModelPartAnimationMode partAnimationMode,
      EasyModelPartPoseListener partPoseListener) {
    Objects.requireNonNull(bakedModel, "bakedModel");
    Objects.requireNonNull(playbackFrame, "playbackFrame");
    Objects.requireNonNull(variantFrame, "variantFrame");
    Objects.requireNonNull(partAnimator, "partAnimator");
    Objects.requireNonNull(partAnimationMode, "partAnimationMode");
    Objects.requireNonNull(partPoseListener, "partPoseListener");
    ModelAnimationClip clip =
        applyVariant(
            bakedModel,
            selectClip(
                bakedModel,
                renderState,
                playbackFrame.animation(),
                limbSwingAmount,
                airborneAmount,
                attackAmount),
            variantFrame.clipName());
    float clipTime =
        clip == null
            ? 0.0f
            : clipTime(
                clip,
                renderState,
                playbackFrame.animation(),
                limbSwing,
                variantFrame.hasClipTicks()
                    ? variantFrame.clipTicks()
                    : playbackFrame.animationTicks(),
                attackAmount,
                playbackFrame.loopOverride());
    ModelAnimationClip previousClip =
        playbackFrame.previousAnimation() == null
            ? null
            : applyVariant(
                bakedModel,
                selectClip(
                    bakedModel,
                    renderState,
                    playbackFrame.previousAnimation(),
                    limbSwingAmount,
                    airborneAmount,
                    attackAmount),
                variantFrame.previousClipName());
    float previousClipTime =
        previousClip == null
            ? 0.0f
            : clipTime(
                previousClip,
                renderState,
                playbackFrame.previousAnimation(),
                limbSwing,
                playbackFrame.previousAnimationTicks(),
                attackAmount,
                playbackFrame.previousLoopOverride());
    VertexSinks sinks = new VertexSinks(bufferProvider, poseStack, packedLight);
    float[] animationSample = ANIMATION_SAMPLE.get();
    for (BakedModelPart part : bakedModel.rootParts()) {
      renderPart(
          part,
          renderState,
          limbSwing,
          limbSwingAmount,
          playbackFrame.animation(),
          playbackFrame.animationTicks(),
          airborneAmount,
          attackAmount,
          headLook,
          clip,
          clipTime,
          playbackFrame.previousAnimation(),
          previousClip,
          previousClipTime,
          playbackFrame.previousAnimationTicks(),
          playbackFrame.blendProgress(),
          poseStack,
          sinks,
          partAnimator,
          partAnimationMode,
          partPoseListener,
          animationSample);
    }
  }

  private static void renderPart(
      BakedModelPart part,
      EasyModelRenderState renderState,
      float limbSwing,
      float limbSwingAmount,
      EasyModelAnimation animation,
      float animationTicks,
      float airborneAmount,
      float attackAmount,
      EasyModelHeadLook headLook,
      ModelAnimationClip clip,
      float clipTime,
      EasyModelAnimation previousAnimation,
      ModelAnimationClip previousClip,
      float previousClipTime,
      float previousAgeInTicks,
      float blendProgress,
      PoseStack poseStack,
      VertexSinks sinks,
      EasyModelPartAnimator partAnimator,
      EasyModelPartAnimationMode partAnimationMode,
      EasyModelPartPoseListener partPoseListener,
      float[] animationSample) {
    poseStack.pushPose();
    Vec3f offset = part.offset();
    Vec3f rotation = part.rotation();
    EasyModelPartTransform automaticTransform =
        animationRotation(
            part,
            renderState,
            limbSwing,
            limbSwingAmount,
            animation,
            animationTicks,
            airborneAmount,
            attackAmount,
            clip,
            clipTime,
            previousAnimation,
            previousClip,
            previousClipTime,
            previousAgeInTicks,
            blendProgress,
            animationSample);
    if (looksWithHead(part.partType(), renderState.bodyType()) && !headLook.isNeutral()) {
      automaticTransform = automaticTransform.add(headLook.toTransform());
    }
    EasyModelPartTransform animationTransform;
    if (partAnimator == EasyModelPartAnimator.NONE) {
      animationTransform =
          partAnimationMode == EasyModelPartAnimationMode.REPLACE
              ? EasyModelPartTransform.NONE
              : automaticTransform;
    } else {
      EasyModelPartTransform animatorTransform =
          Objects.requireNonNull(
              partAnimator.animate(
                  new EasyModelPartAnimationContext(
                      part.name(),
                      EasyModelApiMapper.bodyType(renderState.bodyType()),
                      limbSwing,
                      limbSwingAmount,
                      animationTicks,
                      airborneAmount,
                      automaticTransform)),
              "partAnimator result");
      animationTransform =
          partAnimationMode == EasyModelPartAnimationMode.REPLACE
              ? animatorTransform
              : automaticTransform.add(animatorTransform);
    }
    poseStack.translate(
        (offset.x() + animationTransform.offsetX()) * PIXEL,
        (offset.y() + animationTransform.offsetY()) * PIXEL,
        (offset.z() + animationTransform.offsetZ()) * PIXEL);
    rotate(
        poseStack,
        rotation.x() + animationTransform.xRotation(),
        rotation.y() + animationTransform.yRotation(),
        rotation.z() + animationTransform.zRotation());
    if (animationTransform.scaleX() != 1.0f
        || animationTransform.scaleY() != 1.0f
        || animationTransform.scaleZ() != 1.0f) {
      poseStack.scale(
          animationTransform.scaleX(), animationTransform.scaleY(), animationTransform.scaleZ());
    }

    if (animationTransform.visible()) {
      if (partPoseListener != EasyModelPartPoseListener.NONE
          && partPoseListener.wantsPart(part.name())) {
        partPoseListener.onPartPose(buildPartPose(part, poseStack));
      }
      for (BakedModelCube cube : part.cubes()) {
        renderCube(cube, sinks);
      }
      for (BakedModelPart child : part.children()) {
        renderPart(
            child,
            renderState,
            limbSwing,
            limbSwingAmount,
            animation,
            animationTicks,
            airborneAmount,
            attackAmount,
            headLook,
            clip,
            clipTime,
            previousAnimation,
            previousClip,
            previousClipTime,
            previousAgeInTicks,
            blendProgress,
            poseStack,
            sinks,
            partAnimator,
            partAnimationMode,
            partPoseListener,
            animationSample);
      }
    }

    poseStack.popPose();
  }

  private static EasyModelPartPose buildPartPose(BakedModelPart part, PoseStack poseStack) {
    EasyModelVec3f cubeMin = EasyModelVec3f.ZERO;
    EasyModelVec3f cubeMax = EasyModelVec3f.ZERO;
    if (!part.cubes().isEmpty()) {
      float minX = Float.MAX_VALUE;
      float minY = Float.MAX_VALUE;
      float minZ = Float.MAX_VALUE;
      float maxX = -Float.MAX_VALUE;
      float maxY = -Float.MAX_VALUE;
      float maxZ = -Float.MAX_VALUE;
      for (BakedModelCube cube : part.cubes()) {
        Vec3f position = cube.position();
        Vec3f dimensions = cube.dimensions();
        minX = Math.min(minX, position.x());
        minY = Math.min(minY, position.y());
        minZ = Math.min(minZ, position.z());
        maxX = Math.max(maxX, position.x() + dimensions.x());
        maxY = Math.max(maxY, position.y() + dimensions.y());
        maxZ = Math.max(maxZ, position.z() + dimensions.z());
      }
      cubeMin = new EasyModelVec3f(minX, minY, minZ);
      cubeMax = new EasyModelVec3f(maxX, maxY, maxZ);
    }
    return new EasyModelPartPose(
        part.name(),
        new Matrix4f(poseStack.last().pose()),
        new Matrix3f(poseStack.last().normal()),
        cubeMin,
        cubeMax);
  }

  private static EasyModelPartTransform animationRotation(
      BakedModelPart modelPart,
      EasyModelRenderState renderState,
      float limbSwing,
      float limbSwingAmount,
      EasyModelAnimation animation,
      float animationTicks,
      float airborneAmount,
      float attackAmount,
      ModelAnimationClip clip,
      float clipTime,
      EasyModelAnimation previousAnimation,
      ModelAnimationClip previousClip,
      float previousClipTime,
      float previousAgeInTicks,
      float blendProgress,
      float[] animationSample) {
    if (renderState.fallbackModel()
        || renderState.animation().mode() == ModelAnimationMode.NONE
        || renderState.bodyType() == ModelBodyType.STATIC) {
      return noRotation();
    }

    EasyModelPartTransform currentTransform =
        automaticAnimationTransform(
            modelPart,
            renderState,
            limbSwing,
            limbSwingAmount,
            animation,
            animationTicks,
            airborneAmount,
            attackAmount,
            clip,
            clipTime,
            animationSample);
    if (blendProgress >= 1.0f) {
      return currentTransform;
    }

    EasyModelPartTransform previousTransform =
        automaticAnimationTransform(
            modelPart,
            renderState,
            limbSwing,
            limbSwingAmount,
            previousAnimation,
            previousAgeInTicks,
            airborneAmount,
            attackAmount,
            previousClip,
            previousClipTime,
            animationSample);
    return interpolate(previousTransform, currentTransform, blendProgress);
  }

  private static EasyModelPartTransform automaticAnimationTransform(
      BakedModelPart modelPart,
      EasyModelRenderState renderState,
      float limbSwing,
      float limbSwingAmount,
      EasyModelAnimation animation,
      float animationTicks,
      float airborneAmount,
      float attackAmount,
      ModelAnimationClip clip,
      float clipTime,
      float[] animationSample) {
    ModelPartType part = modelPart.partType();
    boolean tailPart = modelPart.tailPart();
    EasyModelPartTransform baseTransform = null;
    if (clip != null) {
      ModelAnimationBoneTrack track = clip.track(modelPart.name());
      if (track != null) {
        baseTransform = keyframeTransform(track, clipTime, animationSample);
      }
    }
    if (baseTransform == null) {
      baseTransform =
          clip == null && animation != EasyModelAnimation.AUTO && !animation.isNamed()
              ? fallbackAnimationTransform(tailPart, part, renderState, animation, animationTicks)
              : limbSwingAmount > 0.01f
                  ? walkRotation(
                      tailPart, part, renderState, limbSwing, limbSwingAmount, airborneAmount)
                  : idleRotation(
                      tailPart,
                      part,
                      renderState.bodyType(),
                      animationTicks,
                      airborneAmount,
                      renderState.animation().idleStrength());
    }

    if (attackAmount > 0.0f && (clip == null || !isAttackClip(clip))) {
      EasyModelPartTransform attackTransform =
          attackRotation(part, renderState.bodyType(), attackAmount);
      if (attackTransform != EasyModelPartTransform.NONE) {
        baseTransform = baseTransform.add(attackTransform);
      }
    }
    return baseTransform;
  }

  static EasyModelPartTransform interpolate(
      EasyModelPartTransform from, EasyModelPartTransform to, float progress) {
    float clampedProgress = Mth.clamp(progress, 0.0f, 1.0f);
    return new EasyModelPartTransform(
        interpolateAngle(from.xRotation(), to.xRotation(), clampedProgress),
        interpolateAngle(from.yRotation(), to.yRotation(), clampedProgress),
        interpolateAngle(from.zRotation(), to.zRotation(), clampedProgress),
        Mth.lerp(clampedProgress, from.offsetX(), to.offsetX()),
        Mth.lerp(clampedProgress, from.offsetY(), to.offsetY()),
        Mth.lerp(clampedProgress, from.offsetZ(), to.offsetZ()),
        Mth.lerp(clampedProgress, from.scaleX(), to.scaleX()),
        Mth.lerp(clampedProgress, from.scaleY(), to.scaleY()),
        Mth.lerp(clampedProgress, from.scaleZ(), to.scaleZ()),
        clampedProgress < 0.5f ? from.visible() : to.visible());
  }

  private static float interpolateAngle(float from, float to, float progress) {
    float deltaDegrees = Mth.wrapDegrees((to - from) * Mth.RAD_TO_DEG);
    return from + deltaDegrees * Mth.DEG_TO_RAD * progress;
  }

  private static EasyModelPartTransform attackRotation(
      ModelPartType part, ModelBodyType bodyType, float attackAmount) {
    float bodySwing = Mth.sin(Mth.sqrt(attackAmount) * Mth.TWO_PI);
    float strike = Mth.sin(attackAmount * Mth.PI);
    if (bodyType == ModelBodyType.BIPED || bodyType == ModelBodyType.WINGED_HUMANOID) {
      if (part == ModelPartType.RIGHT_ARM) {
        float raise = Mth.sin((1.0f - (1.0f - attackAmount) * (1.0f - attackAmount)) * Mth.PI);
        return new EasyModelPartTransform(-(strike * 1.2f + raise * 0.4f), -bodySwing * 0.2f, 0.0f);
      }
      if (part == ModelPartType.BODY) {
        return new EasyModelPartTransform(0.0f, bodySwing * 0.2f, 0.0f);
      }
      return EasyModelPartTransform.NONE;
    }
    if (part == ModelPartType.HEAD) {
      return new EasyModelPartTransform(-strike * 0.55f, 0.0f, 0.0f);
    }
    if (part == ModelPartType.BODY) {
      return new EasyModelPartTransform(strike * 0.18f, 0.0f, 0.0f);
    }
    if (part == ModelPartType.FRONT_LEFT_LEG || part == ModelPartType.FRONT_RIGHT_LEG) {
      return new EasyModelPartTransform(-strike * 0.35f, 0.0f, 0.0f);
    }

    return EasyModelPartTransform.NONE;
  }

  private static ModelAnimationClip selectClip(
      BakedModel bakedModel,
      EasyModelRenderState renderState,
      EasyModelAnimation animation,
      float limbSwingAmount,
      float airborneAmount,
      float attackAmount) {
    Map<String, ModelAnimationClip> clips = bakedModel.animations();
    if (clips.isEmpty()) {
      return null;
    }
    if (animation.isNamed()) {
      ModelAnimationClip namedClip = clips.get(animation.name());
      if (namedClip != null) {
        return namedClip;
      }
      warnMissingClipOnce(bakedModel, animation.name());
    } else {
      ModelAnimationClip forcedClip = forcedClip(bakedModel, animation);
      if (forcedClip != null) {
        return forcedClip;
      }
      if (animation != EasyModelAnimation.AUTO) {
        return null;
      }
    }
    if (attackAmount > 0.0f) {
      ModelAnimationClip attackClip = groupClip(bakedModel, ModelAnimationClips.ATTACK);
      if (attackClip != null) {
        return attackClip;
      }
    }
    if (airborneAmount > 0.5f) {
      ModelAnimationClip flyClip = groupClip(bakedModel, ModelAnimationClips.FLY);
      if (flyClip != null) {
        return flyClip;
      }
    }
    if (limbSwingAmount > 0.01f) {
      ModelBodyType bodyType = renderState.bodyType();
      if (bodyType == ModelBodyType.AQUATIC || bodyType == ModelBodyType.AMPHIBIOUS) {
        ModelAnimationClip swimClip = groupClip(bakedModel, ModelAnimationClips.SWIM);
        if (swimClip != null) {
          return swimClip;
        }
      }
      return groupClip(bakedModel, ModelAnimationClips.WALK);
    }
    return groupClip(bakedModel, ModelAnimationClips.IDLE);
  }

  static String automaticClipName(
      EasyModelRenderState renderState,
      float limbSwingAmount,
      float airborneAmount,
      float attackAmount) {
    ModelAnimationClip clip =
        selectClip(
            renderState.bakedModel(),
            renderState,
            EasyModelAnimation.AUTO,
            limbSwingAmount,
            airborneAmount,
            attackAmount);
    return clip == null ? "" : clip.name();
  }

  static float clipTime(
      ModelAnimationClip clip,
      EasyModelRenderState renderState,
      EasyModelAnimation animation,
      float limbSwing,
      float ageInTicks,
      float attackAmount,
      Boolean loopOverride) {
    boolean automatic = animation == EasyModelAnimation.AUTO;
    if (automatic && isAttackClip(clip) && clip.length() > 0.0f) {
      return Mth.clamp(attackAmount, 0.0f, 1.0f) * clip.length();
    }
    if (automatic && isMovementClip(clip) && clip.length() > 0.0f) {
      return Mth.positiveModulo(walkCycles(renderState, limbSwing), 1.0f) * clip.length();
    }
    if (loopOverride != null && clip.length() > 0.0f) {
      float seconds = ageInTicks / 20.0f;
      return loopOverride
          ? Mth.positiveModulo(seconds, clip.length())
          : Math.min(seconds, clip.length());
    }
    return clip.clipTime(ageInTicks);
  }

  static float walkCycles(EasyModelRenderState renderState, float limbSwing) {
    return limbSwing * WALK_SWING_FREQUENCY * renderState.animation().swingSpeed() / Mth.TWO_PI;
  }

  private static boolean isMovementClip(ModelAnimationClip clip) {
    String baseName = ModelAnimationClips.baseName(clip.name());
    return ModelAnimationClips.WALK.equals(baseName) || ModelAnimationClips.SWIM.equals(baseName);
  }

  private static boolean isAttackClip(ModelAnimationClip clip) {
    return ModelAnimationClips.ATTACK.equals(ModelAnimationClips.baseName(clip.name()));
  }

  private static void warnMissingClipOnce(BakedModel bakedModel, String clipName) {
    String key = bakedModel.modelId() + "|" + clipName;
    if (MISSING_CLIP_WARNINGS.put(key, Boolean.TRUE) == null) {
      log.warn(
          "Animation clip '{}' not found for model {}; falling back to automatic selection.",
          clipName,
          bakedModel.modelId());
    }
  }

  static String forcedClipName(EasyModelAnimation animation) {
    if (animation == EasyModelAnimation.IDLE) {
      return ModelAnimationClips.IDLE;
    }
    if (animation == EasyModelAnimation.WALK || animation == EasyModelAnimation.RUN) {
      return ModelAnimationClips.WALK;
    }
    if (animation == EasyModelAnimation.SWIM) {
      return ModelAnimationClips.SWIM;
    }
    if (animation == EasyModelAnimation.FLY) {
      return ModelAnimationClips.FLY;
    }
    if (animation == EasyModelAnimation.HURT) {
      return ModelAnimationClips.HURT;
    }
    if (animation == EasyModelAnimation.DEATH) {
      return ModelAnimationClips.DEATH;
    }
    if (animation == EasyModelAnimation.ATTACK) {
      return ModelAnimationClips.ATTACK;
    }
    if (animation == EasyModelAnimation.SIT) {
      return ModelAnimationClips.SIT;
    }

    return null;
  }

  private static ModelAnimationClip forcedClip(
      BakedModel bakedModel, EasyModelAnimation animation) {
    String clipName = forcedClipName(animation);
    return clipName == null ? null : groupClip(bakedModel, clipName);
  }

  private static ModelAnimationClip applyVariant(
      BakedModel bakedModel, ModelAnimationClip clip, String variantClipName) {
    if (variantClipName == null) {
      return clip;
    }

    ModelAnimationClip variantClip = bakedModel.animations().get(variantClipName);
    return variantClip == null ? clip : variantClip;
  }

  static ModelAnimationClip groupClip(BakedModel bakedModel, String baseName) {
    ModelAnimationClip clip = bakedModel.animations().get(baseName);
    if (clip != null) {
      return clip;
    }

    List<String> variants = bakedModel.animationVariants().variantsOf(baseName);
    return variants.isEmpty() ? null : bakedModel.animations().get(variants.get(0));
  }

  private static EasyModelPartTransform fallbackAnimationTransform(
      boolean tailPart,
      ModelPartType part,
      EasyModelRenderState renderState,
      EasyModelAnimation animation,
      float animationTicks) {
    ModelBodyType bodyType = renderState.bodyType();
    if (animation == EasyModelAnimation.IDLE) {
      return idleRotation(
          tailPart, part, bodyType, animationTicks, 0.0f, renderState.animation().idleStrength());
    }
    if (animation == EasyModelAnimation.WALK || animation == EasyModelAnimation.RUN) {
      float speed = animation == EasyModelAnimation.RUN ? 1.65f : 1.0f;
      return walkRotation(tailPart, part, renderState, animationTicks * speed, 1.0f, 0.0f);
    }
    if (animation == EasyModelAnimation.SWIM) {
      return swimTransform(tailPart, part, bodyType, animationTicks);
    }
    if (animation == EasyModelAnimation.FLY) {
      return flyTransform(part, animationTicks);
    }
    if (animation == EasyModelAnimation.ATTACK) {
      return attackRotation(
          part,
          bodyType,
          animationProgress(animationTicks, EasyModelFallbackAnimation.length(animation)));
    }
    if (animation == EasyModelAnimation.HURT) {
      return hurtTransform(
          part, animationProgress(animationTicks, EasyModelFallbackAnimation.length(animation)));
    }
    if (animation == EasyModelAnimation.DEATH) {
      return deathTransform(
          part,
          Mth.clamp(
              animationTicks / (EasyModelFallbackAnimation.length(animation) * 20.0f), 0.0f, 1.0f));
    }
    if (animation == EasyModelAnimation.SIT) {
      return sitTransform(part, bodyType);
    }

    return noRotation();
  }

  private static EasyModelPartTransform swimTransform(
      boolean tailPart, ModelPartType part, ModelBodyType bodyType, float animationTicks) {
    float wave = Mth.sin(animationTicks * 0.35f);
    if (tailPart) {
      return new EasyModelPartTransform(0.0f, wave * 0.7f, 0.0f);
    }
    if (part == ModelPartType.BODY) {
      float pitch =
          bodyType == ModelBodyType.BIPED || bodyType == ModelBodyType.WINGED_HUMANOID
              ? -0.35f
              : wave * 0.08f;
      return new EasyModelPartTransform(
          pitch, 0.0f, 0.0f, 0.0f, wave * 0.35f, 0.0f, 1.0f, 1.0f, 1.0f, true);
    }
    if (part == ModelPartType.LEFT_ARM || part == ModelPartType.RIGHT_ARM) {
      float side = part == ModelPartType.RIGHT_ARM ? -wave : wave;
      return new EasyModelPartTransform(-1.15f + side * 0.45f, 0.0f, 0.0f);
    }
    if (isLeg(part)) {
      return new EasyModelPartTransform(wave * legSide(part) * 0.45f, 0.0f, 0.0f);
    }

    return noRotation();
  }

  private static EasyModelPartTransform flyTransform(ModelPartType part, float animationTicks) {
    float flap = 0.55f + Mth.cos(animationTicks * 0.7f) * 0.85f;
    if (part == ModelPartType.LEFT_WING || part == ModelPartType.RIGHT_WING) {
      return new EasyModelPartTransform(0.0f, 0.0f, wingSwing(part, flap));
    }
    if (part == ModelPartType.BODY) {
      float bob = Mth.sin(animationTicks * 0.35f);
      return new EasyModelPartTransform(
          0.12f + bob * 0.04f, 0.0f, 0.0f, 0.0f, bob * 0.4f, 0.0f, 1.0f, 1.0f, 1.0f, true);
    }
    if (part == ModelPartType.LEFT_LEG || part == ModelPartType.RIGHT_LEG) {
      return new EasyModelPartTransform(0.35f, 0.0f, 0.0f);
    }

    return noRotation();
  }

  private static EasyModelPartTransform hurtTransform(ModelPartType part, float progress) {
    float impact = Mth.sin(progress * Mth.PI);
    if (part == ModelPartType.ROOT || part == ModelPartType.BODY) {
      return new EasyModelPartTransform(-impact * 0.18f, 0.0f, impact * 0.14f);
    }
    if (part == ModelPartType.HEAD) {
      return new EasyModelPartTransform(impact * 0.28f, 0.0f, -impact * 0.1f);
    }
    if (isLeg(part) || part == ModelPartType.LEFT_ARM || part == ModelPartType.RIGHT_ARM) {
      return new EasyModelPartTransform(impact * 0.16f * legSide(part), 0.0f, 0.0f);
    }

    return noRotation();
  }

  private static EasyModelPartTransform deathTransform(ModelPartType part, float progress) {
    float easedProgress = progress * progress * (3.0f - 2.0f * progress);
    if (part == ModelPartType.ROOT) {
      return new EasyModelPartTransform(0.0f, 0.0f, easedProgress * Mth.HALF_PI);
    }
    if (part == ModelPartType.HEAD) {
      return new EasyModelPartTransform(easedProgress * 0.35f, 0.0f, 0.0f);
    }
    if (isLeg(part) || part == ModelPartType.LEFT_ARM || part == ModelPartType.RIGHT_ARM) {
      return new EasyModelPartTransform(easedProgress * 0.2f, 0.0f, 0.0f);
    }

    return noRotation();
  }

  private static EasyModelPartTransform sitTransform(ModelPartType part, ModelBodyType bodyType) {
    if (bodyType == ModelBodyType.BIPED || bodyType == ModelBodyType.WINGED_HUMANOID) {
      if (part == ModelPartType.ROOT) {
        return new EasyModelPartTransform(
            0.08f, 0.0f, 0.0f, 0.0f, 4.0f, 0.0f, 1.0f, 1.0f, 1.0f, true);
      }
      if (part == ModelPartType.LEFT_LEG || part == ModelPartType.RIGHT_LEG) {
        return new EasyModelPartTransform(-1.25f, 0.0f, 0.0f);
      }
      if (part == ModelPartType.LEFT_ARM || part == ModelPartType.RIGHT_ARM) {
        return new EasyModelPartTransform(-0.18f, 0.0f, 0.0f);
      }
      return noRotation();
    }
    if (bodyType == ModelBodyType.QUADRUPED || bodyType == ModelBodyType.AMPHIBIOUS) {
      if (part == ModelPartType.ROOT) {
        return new EasyModelPartTransform(
            -0.08f, 0.0f, 0.0f, 0.0f, 2.5f, 0.0f, 1.0f, 1.0f, 1.0f, true);
      }
      if (part == ModelPartType.BACK_LEFT_LEG || part == ModelPartType.BACK_RIGHT_LEG) {
        return new EasyModelPartTransform(-1.05f, 0.0f, 0.0f);
      }
      if (part == ModelPartType.FRONT_LEFT_LEG || part == ModelPartType.FRONT_RIGHT_LEG) {
        return new EasyModelPartTransform(-0.2f, 0.0f, 0.0f);
      }
      return noRotation();
    }
    if (bodyType == ModelBodyType.WINGED) {
      if (part == ModelPartType.ROOT) {
        return new EasyModelPartTransform(
            0.15f, 0.0f, 0.0f, 0.0f, 1.5f, 0.0f, 1.0f, 1.0f, 1.0f, true);
      }
      if (part == ModelPartType.LEFT_LEG || part == ModelPartType.RIGHT_LEG) {
        return new EasyModelPartTransform(0.45f, 0.0f, 0.0f);
      }
    }

    return noRotation();
  }

  private static float animationProgress(float animationTicks, float lengthSeconds) {
    if (lengthSeconds <= 0.0f) {
      return 0.0f;
    }
    float lengthTicks = lengthSeconds * 20.0f;
    return Mth.positiveModulo(animationTicks, lengthTicks) / lengthTicks;
  }

  private static boolean isLeg(ModelPartType part) {
    return part == ModelPartType.LEFT_LEG
        || part == ModelPartType.RIGHT_LEG
        || part == ModelPartType.FRONT_LEFT_LEG
        || part == ModelPartType.FRONT_RIGHT_LEG
        || part == ModelPartType.MIDDLE_FRONT_LEFT_LEG
        || part == ModelPartType.MIDDLE_FRONT_RIGHT_LEG
        || part == ModelPartType.MIDDLE_BACK_LEFT_LEG
        || part == ModelPartType.MIDDLE_BACK_RIGHT_LEG
        || part == ModelPartType.BACK_LEFT_LEG
        || part == ModelPartType.BACK_RIGHT_LEG;
  }

  private static float legSide(ModelPartType part) {
    return part == ModelPartType.RIGHT_LEG
            || part == ModelPartType.FRONT_RIGHT_LEG
            || part == ModelPartType.MIDDLE_FRONT_RIGHT_LEG
            || part == ModelPartType.MIDDLE_BACK_RIGHT_LEG
            || part == ModelPartType.BACK_RIGHT_LEG
        ? -1.0f
        : 1.0f;
  }

  private static EasyModelPartTransform keyframeTransform(
      ModelAnimationBoneTrack track, float clipTime, float[] animationSample) {
    track.sample(clipTime, animationSample, 0);
    return new EasyModelPartTransform(
        animationSample[0],
        animationSample[1],
        animationSample[2],
        animationSample[3],
        animationSample[4],
        animationSample[5],
        1.0f,
        1.0f,
        1.0f,
        true);
  }

  private static EasyModelPartTransform walkRotation(
      boolean tailPart,
      ModelPartType part,
      EasyModelRenderState renderState,
      float limbSwing,
      float limbSwingAmount,
      float airborneAmount) {
    ModelBodyType bodyType = renderState.bodyType();
    boolean quadruped = bodyType == ModelBodyType.QUADRUPED;
    ModelGaitType gait = renderState.animation().gait();
    float cadence = quadruped ? gait.cadenceScale() : 1.0f;
    float phase =
        limbSwing * WALK_SWING_FREQUENCY * renderState.animation().swingSpeed() * cadence
            + animationPhase(part, bodyType);
    float swing =
        Mth.cos(phase)
            * walkRotationScale(
                bodyType, limbSwingAmount, renderState.animation().walkSpeedMultiplier(), gait);

    if (animatesOnX(part, bodyType)) {
      return new EasyModelPartTransform(swing, 0.0f, 0.0f);
    }
    if (animatesOnY(tailPart, bodyType)) {
      return new EasyModelPartTransform(0.0f, swing * 0.75f, 0.0f);
    }
    if (animatesOnZ(part, bodyType)) {
      if (bodyType == ModelBodyType.WINGED) {
        return wingTransform(part, airborneAmount, swing * airborneAmount);
      }
      return new EasyModelPartTransform(0.0f, 0.0f, wingSwing(part, swing));
    }
    return noRotation();
  }

  private static float walkRotationScale(
      ModelBodyType bodyType,
      float limbSwingAmount,
      float walkSpeedMultiplier,
      ModelGaitType gait) {
    float rotationScale = WALK_ROTATION_SCALE * limbSwingAmount * walkSpeedMultiplier;
    if (bodyType == ModelBodyType.QUADRUPED) {
      return gait.strideScale() * Math.max(rotationScale, MIN_QUADRUPED_WALK_ROTATION);
    }
    return rotationScale;
  }

  private static EasyModelPartTransform idleRotation(
      boolean tailPart,
      ModelPartType part,
      ModelBodyType bodyType,
      float ageInTicks,
      float airborneAmount,
      float idleStrength) {
    if (bodyType == ModelBodyType.CUBOID) {
      float cuboidIdle = Mth.sin(ageInTicks * CUBOID_IDLE_SPEED) * idleStrength;
      if (part == ModelPartType.HEAD) {
        return new EasyModelPartTransform(cuboidIdle * CUBOID_LID_ROTATION, 0.0f, 0.0f);
      }
      if (part == ModelPartType.BODY) {
        return new EasyModelPartTransform(cuboidIdle * CUBOID_BODY_ROTATION, 0.0f, 0.0f);
      }
      return noRotation();
    }

    if (animatesOnZ(part, bodyType)) {
      float idleFlap = Mth.sin(ageInTicks * 0.24f) * IDLE_WING_ROTATION * idleStrength;
      if (bodyType == ModelBodyType.WINGED) {
        float airFlap =
            Mth.cos(ageInTicks * WING_AIR_FLAP_SPEED) * WING_AIR_FLAP_AMPLITUDE * airborneAmount;
        return wingTransform(part, airborneAmount, idleFlap + airFlap);
      }
      return new EasyModelPartTransform(0.0f, 0.0f, wingSwing(part, idleFlap));
    }

    float breath = Mth.sin(ageInTicks * 0.12f) * IDLE_BREATH_ROTATION * idleStrength;
    if (part == ModelPartType.BODY) {
      return new EasyModelPartTransform(breath, 0.0f, 0.0f);
    }
    if (part == ModelPartType.HEAD) {
      return new EasyModelPartTransform(breath * 0.5f, 0.0f, 0.0f);
    }
    if (animatesOnY(tailPart, bodyType)) {
      return new EasyModelPartTransform(
          0.0f, Mth.sin(ageInTicks * 0.18f) * IDLE_TAIL_ROTATION * idleStrength, 0.0f);
    }
    return noRotation();
  }

  private static EasyModelPartTransform wingTransform(
      ModelPartType part, float airborneAmount, float flap) {
    float fold = -WING_FOLD_ANGLE * (1.0f - airborneAmount);
    return new EasyModelPartTransform(0.0f, 0.0f, wingSwing(part, fold + flap));
  }

  private static boolean animatesOnX(ModelPartType part, ModelBodyType bodyType) {
    return switch (bodyType) {
      case BIPED, WINGED_HUMANOID ->
          part == ModelPartType.LEFT_LEG
              || part == ModelPartType.RIGHT_LEG
              || part == ModelPartType.LEFT_ARM
              || part == ModelPartType.RIGHT_ARM;
      case QUADRUPED ->
          part == ModelPartType.FRONT_LEFT_LEG
              || part == ModelPartType.FRONT_RIGHT_LEG
              || part == ModelPartType.BACK_LEFT_LEG
              || part == ModelPartType.BACK_RIGHT_LEG
              || part == ModelPartType.LEFT_LEG
              || part == ModelPartType.RIGHT_LEG;
      case WINGED -> part == ModelPartType.LEFT_LEG || part == ModelPartType.RIGHT_LEG;
      case ARTHROPOD ->
          part == ModelPartType.FRONT_LEFT_LEG
              || part == ModelPartType.FRONT_RIGHT_LEG
              || part == ModelPartType.MIDDLE_FRONT_LEFT_LEG
              || part == ModelPartType.MIDDLE_FRONT_RIGHT_LEG
              || part == ModelPartType.MIDDLE_BACK_LEFT_LEG
              || part == ModelPartType.MIDDLE_BACK_RIGHT_LEG
              || part == ModelPartType.BACK_LEFT_LEG
              || part == ModelPartType.BACK_RIGHT_LEG;
      case AQUATIC, CUBOID, FLOATING, STATIC, AMPHIBIOUS -> false;
    };
  }

  private static boolean looksWithHead(ModelPartType part, ModelBodyType bodyType) {
    return part == ModelPartType.HEAD
        && bodyType != ModelBodyType.STATIC
        && bodyType != ModelBodyType.CUBOID;
  }

  private static boolean animatesOnY(boolean tailPart, ModelBodyType bodyType) {
    return bodyType != ModelBodyType.STATIC && tailPart;
  }

  private static boolean animatesOnZ(ModelPartType part, ModelBodyType bodyType) {
    return (bodyType == ModelBodyType.WINGED || bodyType == ModelBodyType.WINGED_HUMANOID)
        && (part == ModelPartType.LEFT_WING || part == ModelPartType.RIGHT_WING);
  }

  private static float wingSwing(ModelPartType part, float swing) {
    return part == ModelPartType.RIGHT_WING ? -swing : swing;
  }

  private static float animationPhase(ModelPartType part, ModelBodyType bodyType) {
    return switch (bodyType) {
      case BIPED, WINGED_HUMANOID ->
          part == ModelPartType.RIGHT_LEG || part == ModelPartType.LEFT_ARM ? Mth.PI : 0.0f;
      case QUADRUPED, WINGED ->
          part == ModelPartType.FRONT_RIGHT_LEG
                  || part == ModelPartType.BACK_LEFT_LEG
                  || part == ModelPartType.RIGHT_LEG
              ? Mth.PI
              : 0.0f;
      case ARTHROPOD ->
          part == ModelPartType.FRONT_RIGHT_LEG
                  || part == ModelPartType.MIDDLE_FRONT_LEFT_LEG
                  || part == ModelPartType.MIDDLE_BACK_RIGHT_LEG
                  || part == ModelPartType.BACK_LEFT_LEG
              ? Mth.PI
              : 0.0f;
      case AMPHIBIOUS, AQUATIC, CUBOID, FLOATING, STATIC -> 0.0f;
    };
  }

  private static EasyModelPartTransform noRotation() {
    return EasyModelPartTransform.NONE;
  }

  private static void rotate(
      PoseStack poseStack, float xRotation, float yRotation, float zRotation) {
    if (zRotation != 0.0f) {
      poseStack.mulPose(Axis.ZP.rotation(zRotation));
    }
    if (yRotation != 0.0f) {
      poseStack.mulPose(Axis.YP.rotation(yRotation));
    }
    if (xRotation != 0.0f) {
      poseStack.mulPose(Axis.XP.rotation(xRotation));
    }
  }

  private static void renderCube(BakedModelCube cube, VertexSinks sinks) {
    EasyModelVertexSink sink = sinks.sink(cube.textureIndex());
    Vec3f position = cube.position();
    Vec3f dimensions = cube.dimensions();
    float x1 = position.x() * PIXEL;
    float y1 = position.y() * PIXEL;
    float z1 = position.z() * PIXEL;
    float x2 = x1 + dimensions.x() * PIXEL;
    float y2 = y1 + dimensions.y() * PIXEL;
    float z2 = z1 + dimensions.z() * PIXEL;

    FaceUv southUv = uv(cube, ModelCubeFace.SOUTH);
    FaceUv northUv = uv(cube, ModelCubeFace.NORTH);
    FaceUv westUv = uv(cube, ModelCubeFace.WEST);
    FaceUv eastUv = uv(cube, ModelCubeFace.EAST);
    FaceUv upUv = uv(cube, ModelCubeFace.UP);
    FaceUv downUv = uv(cube, ModelCubeFace.DOWN);

    CubeFaceVisibility visibility = cube.faceVisibility();
    if (visibility.isVisible(ModelCubeFace.SOUTH)) {
      quad(sink, x1, y1, z2, x2, y1, z2, x2, y2, z2, x1, y2, z2, 0, 0, 1, southUv);
    }
    if (visibility.isVisible(ModelCubeFace.NORTH)) {
      quad(sink, x2, y1, z1, x1, y1, z1, x1, y2, z1, x2, y2, z1, 0, 0, -1, northUv);
    }
    if (visibility.isVisible(ModelCubeFace.EAST)) {
      quad(sink, x1, y1, z1, x1, y1, z2, x1, y2, z2, x1, y2, z1, -1, 0, 0, eastUv);
    }
    if (visibility.isVisible(ModelCubeFace.WEST)) {
      quad(sink, x2, y1, z2, x2, y1, z1, x2, y2, z1, x2, y2, z2, 1, 0, 0, westUv);
    }
    if (visibility.isVisible(ModelCubeFace.UP)) {
      quad(sink, x1, y1, z1, x2, y1, z1, x2, y1, z2, x1, y1, z2, 0, -1, 0, upUv);
    }
    if (visibility.isVisible(ModelCubeFace.DOWN)) {
      quad(sink, x1, y2, z2, x2, y2, z2, x2, y2, z1, x1, y2, z1, 0, 1, 0, downUv);
    }
  }

  private static FaceUv uv(BakedModelCube cube, ModelCubeFace face) {
    return cube.faceUvs().uv(face);
  }

  private static void quad(
      EasyModelVertexSink sink,
      float x1,
      float y1,
      float z1,
      float x2,
      float y2,
      float z2,
      float x3,
      float y3,
      float z3,
      float x4,
      float y4,
      float z4,
      float normalX,
      float normalY,
      float normalZ,
      FaceUv uv) {
    sink.vertex(x1, y1, z1, uv.maxU(), uv.minV(), normalX, normalY, normalZ);
    sink.vertex(x2, y2, z2, uv.minU(), uv.minV(), normalX, normalY, normalZ);
    sink.vertex(x3, y3, z3, uv.minU(), uv.maxV(), normalX, normalY, normalZ);
    sink.vertex(x4, y4, z4, uv.maxU(), uv.maxV(), normalX, normalY, normalZ);
  }

  private static final class VertexSinks {

    private final IntFunction<VertexConsumer> bufferProvider;
    private final PoseStack poseStack;
    private final int packedLight;
    private int lastTextureIndex = -1;
    private EasyModelVertexSink lastSink;

    private VertexSinks(
        IntFunction<VertexConsumer> bufferProvider, PoseStack poseStack, int packedLight) {
      this.bufferProvider = bufferProvider;
      this.poseStack = poseStack;
      this.packedLight = packedLight;
    }

    private EasyModelVertexSink sink(int textureIndex) {
      if (this.lastSink == null || textureIndex != this.lastTextureIndex) {
        this.lastSink =
            new EasyModelVertexSink(
                this.bufferProvider.apply(textureIndex), this.poseStack, this.packedLight);
        this.lastTextureIndex = textureIndex;
      }
      return this.lastSink;
    }
  }
}
