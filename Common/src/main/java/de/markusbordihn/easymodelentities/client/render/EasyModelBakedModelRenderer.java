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
import de.markusbordihn.easymodelentities.api.client.EasyModelPartAnimator;
import de.markusbordihn.easymodelentities.api.data.client.EasyModelPartAnimationContext;
import de.markusbordihn.easymodelentities.api.data.client.EasyModelPartAnimationMode;
import de.markusbordihn.easymodelentities.api.data.client.EasyModelPartTransform;
import de.markusbordihn.easymodelentities.data.model.CubeFaceVisibility;
import de.markusbordihn.easymodelentities.data.model.FaceUv;
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
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.IntFunction;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

public final class EasyModelBakedModelRenderer {

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

  private EasyModelBakedModelRenderer() {}

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
      SubmitNodeCollector submitNodeCollector,
      int packedLight) {
    Objects.requireNonNull(renderState, "renderState");
    Objects.requireNonNull(submitNodeCollector, "submitNodeCollector");
    boolean cullBackfaces = bakedModel.cullBackfaces();
    for (int textureIndex : textureIndices(bakedModel)) {
      Identifier texture = textureFor(renderState, textureIndex);
      RenderType renderType =
          cullBackfaces ? RenderTypes.entityCutoutCull(texture) : RenderTypes.entityCutout(texture);
      submitNodeCollector.submitCustomGeometry(
          poseStack,
          renderType,
          (pose, vertexConsumer) -> {
            PoseStack innerStack = new PoseStack();
            innerStack.last().pose().set(pose.pose());
            innerStack.last().normal().set(pose.normal());
            render(
                bakedModel,
                renderState,
                limbSwing,
                limbSwingAmount,
                ageInTicks,
                airborneAmount,
                innerStack,
                renderTextureIndex -> renderTextureIndex == textureIndex ? vertexConsumer : null,
                packedLight,
                partAnimator,
                partAnimationMode);
          });
    }
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
    Objects.requireNonNull(renderState, "renderState");
    Objects.requireNonNull(bufferSource, "bufferSource");
    boolean cullBackfaces = bakedModel.cullBackfaces();
    IntFunction<VertexConsumer> bufferProvider =
        textureIndex -> {
          Identifier texture = textureFor(renderState, textureIndex);
          return bufferSource.getBuffer(
              cullBackfaces
                  ? RenderTypes.entityCutoutCull(texture)
                  : RenderTypes.entityCutout(texture));
        };
    render(
        bakedModel,
        renderState,
        limbSwing,
        limbSwingAmount,
        ageInTicks,
        airborneAmount,
        poseStack,
        bufferProvider,
        packedLight,
        partAnimator,
        partAnimationMode);
  }

  private static Identifier textureFor(EasyModelRenderState renderState, int textureIndex) {
    return renderState.textures().getOrDefault(textureIndex, renderState.texture());
  }

  private static Set<Integer> textureIndices(BakedModel bakedModel) {
    Set<Integer> indices = new TreeSet<>();
    for (BakedModelPart part : bakedModel.rootParts()) {
      collectTextureIndices(part, indices);
    }
    if (indices.isEmpty()) {
      indices.add(0);
    }
    return indices;
  }

  private static void collectTextureIndices(BakedModelPart part, Set<Integer> indices) {
    for (BakedModelCube cube : part.cubes()) {
      indices.add(cube.textureIndex());
    }
    for (BakedModelPart child : part.children()) {
      collectTextureIndices(child, indices);
    }
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
        ageInTicks,
        airborneAmount,
        poseStack,
        textureIndex -> vertexConsumer,
        packedLight,
        partAnimator,
        partAnimationMode);
  }

  static void render(
      BakedModel bakedModel,
      EasyModelRenderState renderState,
      float limbSwing,
      float limbSwingAmount,
      float ageInTicks,
      float airborneAmount,
      PoseStack poseStack,
      IntFunction<VertexConsumer> bufferProvider,
      int packedLight,
      EasyModelPartAnimator partAnimator,
      EasyModelPartAnimationMode partAnimationMode) {
    Objects.requireNonNull(bakedModel, "bakedModel");
    Objects.requireNonNull(partAnimator, "partAnimator");
    Objects.requireNonNull(partAnimationMode, "partAnimationMode");
    VertexSinks sinks = new VertexSinks(bufferProvider, poseStack, packedLight);
    for (BakedModelPart part : bakedModel.rootParts()) {
      renderPart(
          part,
          renderState,
          limbSwing,
          limbSwingAmount,
          ageInTicks,
          airborneAmount,
          poseStack,
          sinks,
          partAnimator,
          partAnimationMode);
    }
  }

  private static void renderPart(
      BakedModelPart part,
      EasyModelRenderState renderState,
      float limbSwing,
      float limbSwingAmount,
      float ageInTicks,
      float airborneAmount,
      PoseStack poseStack,
      VertexSinks sinks,
      EasyModelPartAnimator partAnimator,
      EasyModelPartAnimationMode partAnimationMode) {
    poseStack.pushPose();
    Vec3f offset = part.offset();
    Vec3f rotation = part.rotation();
    EasyModelPartTransform automaticTransform =
        animationRotation(
            part.name(), renderState, limbSwing, limbSwingAmount, ageInTicks, airborneAmount);
    EasyModelPartTransform animationTransform;
    if (partAnimator == EasyModelPartAnimator.NONE) {
      animationTransform =
          partAnimationMode == EasyModelPartAnimationMode.REPLACE
              ? EasyModelPartTransform.NONE
              : automaticTransform;
    } else {
      EasyModelPartTransform animatorTransform =
          sanitize(
              partAnimator.animate(
                  new EasyModelPartAnimationContext(
                      part.name(),
                      renderState.bodyType(),
                      limbSwing,
                      limbSwingAmount,
                      ageInTicks,
                      airborneAmount,
                      automaticTransform)));
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
      for (BakedModelCube cube : part.cubes()) {
        renderCube(cube, sinks);
      }
      for (BakedModelPart child : part.children()) {
        renderPart(
            child,
            renderState,
            limbSwing,
            limbSwingAmount,
            ageInTicks,
            airborneAmount,
            poseStack,
            sinks,
            partAnimator,
            partAnimationMode);
      }
    }

    poseStack.popPose();
  }

  private static EasyModelPartTransform animationRotation(
      String partName,
      EasyModelRenderState renderState,
      float limbSwing,
      float limbSwingAmount,
      float ageInTicks,
      float airborneAmount) {
    if (renderState.fallbackModel()
        || renderState.animation().mode() == ModelAnimationMode.NONE
        || renderState.bodyType() == ModelBodyType.STATIC) {
      return noRotation();
    }

    ModelPartType part = ModelPartType.get(partName);
    if (limbSwingAmount > 0.01f) {
      return walkRotation(partName, part, renderState, limbSwing, limbSwingAmount, airborneAmount);
    }

    return idleRotation(
        partName,
        part,
        renderState.bodyType(),
        ageInTicks,
        airborneAmount,
        renderState.animation().idleStrength());
  }

  private static EasyModelPartTransform walkRotation(
      String partName,
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
    if (animatesOnY(partName, bodyType)) {
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
      String partName,
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
    if (animatesOnY(partName, bodyType)) {
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

  private static boolean animatesOnY(String partName, ModelBodyType bodyType) {
    return bodyType != ModelBodyType.STATIC && isTailPart(partName);
  }

  private static boolean animatesOnZ(ModelPartType part, ModelBodyType bodyType) {
    return (bodyType == ModelBodyType.WINGED || bodyType == ModelBodyType.WINGED_HUMANOID)
        && (part == ModelPartType.LEFT_WING || part == ModelPartType.RIGHT_WING);
  }

  private static boolean isTailPart(String partName) {
    return ModelPartType.TAIL.getTagName().equals(partName)
        || ModelPartType.TAIL_FIN.getTagName().equals(partName)
        || partName.startsWith("tail_")
        || partName.endsWith("_tail")
        || partName.contains("_tail_");
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

  private static EasyModelPartTransform sanitize(EasyModelPartTransform transform) {
    if (Float.isFinite(transform.xRotation())
        && Float.isFinite(transform.yRotation())
        && Float.isFinite(transform.zRotation())
        && Float.isFinite(transform.offsetX())
        && Float.isFinite(transform.offsetY())
        && Float.isFinite(transform.offsetZ())
        && Float.isFinite(transform.scaleX())
        && Float.isFinite(transform.scaleY())
        && Float.isFinite(transform.scaleZ())) {
      return transform;
    }
    return new EasyModelPartTransform(
        finiteOr(transform.xRotation(), 0.0f),
        finiteOr(transform.yRotation(), 0.0f),
        finiteOr(transform.zRotation(), 0.0f),
        finiteOr(transform.offsetX(), 0.0f),
        finiteOr(transform.offsetY(), 0.0f),
        finiteOr(transform.offsetZ(), 0.0f),
        finiteOr(transform.scaleX(), 1.0f),
        finiteOr(transform.scaleY(), 1.0f),
        finiteOr(transform.scaleZ(), 1.0f),
        transform.visible());
  }

  private static float finiteOr(float value, float fallback) {
    return Float.isFinite(value) ? value : fallback;
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

    if (cube.mirror()) {
      southUv = mirrorU(southUv);
      northUv = mirrorU(northUv);
      FaceUv tempEast = eastUv;
      eastUv = mirrorU(westUv);
      westUv = mirrorU(tempEast);
      upUv = mirrorU(upUv);
      downUv = mirrorU(downUv);
    }

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

  private static FaceUv mirrorU(FaceUv uv) {
    return new FaceUv(uv.maxU(), uv.minV(), uv.minU(), uv.maxV());
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
