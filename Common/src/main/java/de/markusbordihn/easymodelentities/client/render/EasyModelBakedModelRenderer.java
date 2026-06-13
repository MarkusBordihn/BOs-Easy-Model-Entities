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
import de.markusbordihn.easymodelentities.model.ModelCubeFaceUvs.Face;
import de.markusbordihn.easymodelentities.model.bake.BakedModel;
import de.markusbordihn.easymodelentities.model.bake.BakedModelCube;
import de.markusbordihn.easymodelentities.model.bake.BakedModelPart;
import de.markusbordihn.easymodelentities.profile.ModelBodyType;
import de.markusbordihn.easymodelentities.render.EasyModelRenderState;
import de.markusbordihn.easymodelentities.renderprofile.ModelAnimationMode;
import java.util.Objects;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;

public final class EasyModelBakedModelRenderer {

  private static final float PIXEL = 1.0f / 16.0f;
  private static final float WALK_SWING_FREQUENCY = 0.6662f;
  private static final float WALK_ROTATION_SCALE = 1.4f;
  private static final float MIN_QUADRUPED_WALK_ROTATION = Mth.PI / 4.0f;
  private static final float IDLE_BREATH_ROTATION = 0.025f;
  private static final float IDLE_WING_ROTATION = 0.12f;
  private static final float IDLE_TAIL_ROTATION = 0.18f;

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
      PoseStack poseStack,
      VertexConsumer vertexConsumer,
      int packedLight) {
    Objects.requireNonNull(bakedModel, "bakedModel");
    for (BakedModelPart part : bakedModel.rootParts()) {
      renderPart(
          part,
          renderState,
          limbSwing,
          limbSwingAmount,
          ageInTicks,
          poseStack,
          vertexConsumer,
          packedLight);
    }
  }

  private static void renderPart(
      BakedModelPart part,
      EasyModelRenderState renderState,
      float limbSwing,
      float limbSwingAmount,
      float ageInTicks,
      PoseStack poseStack,
      VertexConsumer vertexConsumer,
      int packedLight) {
    poseStack.pushPose();
    float[] offset = part.offset();
    float[] rotation = part.rotation();
    float[] animationRotation =
        animationRotation(part.name(), renderState, limbSwing, limbSwingAmount, ageInTicks);
    poseStack.translate(offset[0] * PIXEL, offset[1] * PIXEL, offset[2] * PIXEL);
    rotate(
        poseStack,
        rotation[0] + animationRotation[0],
        rotation[1] + animationRotation[1],
        rotation[2] + animationRotation[2]);

    for (BakedModelCube cube : part.cubes()) {
      renderCube(cube, poseStack, vertexConsumer, packedLight, renderState.bakedModel());
    }
    for (BakedModelPart child : part.children()) {
      renderPart(
          child,
          renderState,
          limbSwing,
          limbSwingAmount,
          ageInTicks,
          poseStack,
          vertexConsumer,
          packedLight);
    }

    poseStack.popPose();
  }

  private static float[] animationRotation(
      String partName,
      EasyModelRenderState renderState,
      float limbSwing,
      float limbSwingAmount,
      float ageInTicks) {
    if (renderState.fallbackModel()
        || renderState.animation().mode() == ModelAnimationMode.NONE
        || renderState.bodyType() == ModelBodyType.STATIC) {
      return noRotation();
    }

    if (limbSwingAmount > 0.01f) {
      return walkRotation(partName, renderState, limbSwing, limbSwingAmount);
    }

    return idleRotation(partName, renderState.bodyType(), ageInTicks);
  }

  private static float[] walkRotation(
      String partName, EasyModelRenderState renderState, float limbSwing, float limbSwingAmount) {
    ModelBodyType bodyType = renderState.bodyType();
    float phase =
        limbSwing * WALK_SWING_FREQUENCY * renderState.animation().swingSpeed()
            + animationPhase(partName, bodyType);
    float swing =
        Mth.cos(phase)
            * walkRotationScale(
                bodyType, limbSwingAmount, renderState.animation().walkSpeedMultiplier());

    if (animatesOnX(partName, bodyType)) {
      return new float[] {swing, 0.0f, 0.0f};
    }
    if (animatesOnY(partName, bodyType)) {
      return new float[] {0.0f, swing * 0.75f, 0.0f};
    }
    if (animatesOnZ(partName, bodyType)) {
      return new float[] {0.0f, 0.0f, wingSwing(partName, swing)};
    }
    return noRotation();
  }

  private static float walkRotationScale(
      ModelBodyType bodyType, float limbSwingAmount, float walkSpeedMultiplier) {
    float rotationScale = WALK_ROTATION_SCALE * limbSwingAmount * walkSpeedMultiplier;
    return bodyType == ModelBodyType.QUADRUPED
        ? Math.max(rotationScale, MIN_QUADRUPED_WALK_ROTATION)
        : rotationScale;
  }

  private static float[] idleRotation(String partName, ModelBodyType bodyType, float ageInTicks) {
    float breath = Mth.sin(ageInTicks * 0.12f) * IDLE_BREATH_ROTATION;
    if ("body".equals(partName)) {
      return new float[] {breath, 0.0f, 0.0f};
    }
    if ("head".equals(partName)) {
      return new float[] {breath * 0.5f, 0.0f, 0.0f};
    }
    if (animatesOnY(partName, bodyType)) {
      return new float[] {0.0f, Mth.sin(ageInTicks * 0.18f) * IDLE_TAIL_ROTATION, 0.0f};
    }
    if (animatesOnZ(partName, bodyType)) {
      return new float[] {
        0.0f, 0.0f, wingSwing(partName, Mth.sin(ageInTicks * 0.24f) * IDLE_WING_ROTATION)
      };
    }
    return noRotation();
  }

  private static boolean animatesOnX(String partName, ModelBodyType bodyType) {
    return switch (bodyType) {
      case BIPED, WINGED_HUMANOID ->
          "left_leg".equals(partName)
              || "right_leg".equals(partName)
              || "left_arm".equals(partName)
              || "right_arm".equals(partName);
      case QUADRUPED ->
          "front_left_leg".equals(partName)
              || "front_right_leg".equals(partName)
              || "back_left_leg".equals(partName)
              || "back_right_leg".equals(partName)
              || "left_leg".equals(partName)
              || "right_leg".equals(partName);
      case WINGED -> "left_leg".equals(partName) || "right_leg".equals(partName);
      case ARTHROPOD ->
          "front_left_leg".equals(partName)
              || "front_right_leg".equals(partName)
              || "middle_front_left_leg".equals(partName)
              || "middle_front_right_leg".equals(partName)
              || "middle_back_left_leg".equals(partName)
              || "middle_back_right_leg".equals(partName)
              || "back_left_leg".equals(partName)
              || "back_right_leg".equals(partName);
      case AQUATIC, CUBOID, FLOATING, STATIC -> false;
    };
  }

  private static boolean animatesOnY(String partName, ModelBodyType bodyType) {
    return bodyType != ModelBodyType.STATIC && isTailPart(partName);
  }

  private static boolean animatesOnZ(String partName, ModelBodyType bodyType) {
    return (bodyType == ModelBodyType.WINGED || bodyType == ModelBodyType.WINGED_HUMANOID)
        && ("left_wing".equals(partName) || "right_wing".equals(partName));
  }

  private static boolean isTailPart(String partName) {
    return "tail".equals(partName)
        || "tail_fin".equals(partName)
        || partName.startsWith("tail_")
        || partName.endsWith("_tail")
        || partName.contains("_tail_");
  }

  private static float wingSwing(String partName, float swing) {
    return "right_wing".equals(partName) ? -swing : swing;
  }

  private static float animationPhase(String partName, ModelBodyType bodyType) {
    return switch (bodyType) {
      case BIPED, WINGED_HUMANOID ->
          "right_leg".equals(partName) || "left_arm".equals(partName) ? Mth.PI : 0.0f;
      case QUADRUPED, WINGED ->
          "front_right_leg".equals(partName)
                  || "back_left_leg".equals(partName)
                  || "right_leg".equals(partName)
              ? Mth.PI
              : 0.0f;
      case ARTHROPOD ->
          "front_right_leg".equals(partName)
                  || "middle_front_left_leg".equals(partName)
                  || "middle_back_right_leg".equals(partName)
                  || "back_left_leg".equals(partName)
              ? Mth.PI
              : 0.0f;
      case AQUATIC, CUBOID, FLOATING, STATIC -> 0.0f;
    };
  }

  private static float[] noRotation() {
    return new float[] {0.0f, 0.0f, 0.0f};
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

  private static void renderCube(
      BakedModelCube cube,
      PoseStack poseStack,
      VertexConsumer vertexConsumer,
      int packedLight,
      BakedModel bakedModel) {
    float[] position = cube.position();
    float[] dimensions = cube.dimensions();
    float x1 = position[0] * PIXEL;
    float y1 = position[1] * PIXEL;
    float z1 = position[2] * PIXEL;
    float x2 = x1 + dimensions[0] * PIXEL;
    float y2 = y1 + dimensions[1] * PIXEL;
    float z2 = z1 + dimensions[2] * PIXEL;

    quad(
        vertexConsumer,
        poseStack,
        x1,
        y1,
        z2,
        x2,
        y1,
        z2,
        x2,
        y2,
        z2,
        x1,
        y2,
        z2,
        0,
        0,
        1,
        uv(cube, bakedModel, Face.SOUTH),
        packedLight);
    quad(
        vertexConsumer,
        poseStack,
        x2,
        y1,
        z1,
        x1,
        y1,
        z1,
        x1,
        y2,
        z1,
        x2,
        y2,
        z1,
        0,
        0,
        -1,
        uv(cube, bakedModel, Face.NORTH),
        packedLight);
    quad(
        vertexConsumer,
        poseStack,
        x1,
        y1,
        z1,
        x1,
        y1,
        z2,
        x1,
        y2,
        z2,
        x1,
        y2,
        z1,
        -1,
        0,
        0,
        uv(cube, bakedModel, Face.WEST),
        packedLight);
    quad(
        vertexConsumer,
        poseStack,
        x2,
        y1,
        z2,
        x2,
        y1,
        z1,
        x2,
        y2,
        z1,
        x2,
        y2,
        z2,
        1,
        0,
        0,
        uv(cube, bakedModel, Face.EAST),
        packedLight);
    quad(
        vertexConsumer,
        poseStack,
        x1,
        y1,
        z1,
        x2,
        y1,
        z1,
        x2,
        y1,
        z2,
        x1,
        y1,
        z2,
        0,
        -1,
        0,
        uv(cube, bakedModel, Face.UP),
        packedLight);
    quad(
        vertexConsumer,
        poseStack,
        x1,
        y2,
        z2,
        x2,
        y2,
        z2,
        x2,
        y2,
        z1,
        x1,
        y2,
        z1,
        0,
        1,
        0,
        uv(cube, bakedModel, Face.DOWN),
        packedLight);
  }

  private static float[] uv(BakedModelCube cube, BakedModel bakedModel, Face face) {
    float[] uv = cube.faceUvs().uv(face);
    return new float[] {
      uv[0] / bakedModel.textureWidth(),
      uv[1] / bakedModel.textureHeight(),
      uv[2] / bakedModel.textureWidth(),
      uv[3] / bakedModel.textureHeight()
    };
  }

  private static void quad(
      VertexConsumer vertexConsumer,
      PoseStack poseStack,
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
      float[] uv,
      int packedLight) {
    vertex(
        vertexConsumer,
        poseStack,
        x1,
        y1,
        z1,
        uv[0],
        uv[1],
        normalX,
        normalY,
        normalZ,
        packedLight);
    vertex(
        vertexConsumer,
        poseStack,
        x2,
        y2,
        z2,
        uv[2],
        uv[1],
        normalX,
        normalY,
        normalZ,
        packedLight);
    vertex(
        vertexConsumer,
        poseStack,
        x3,
        y3,
        z3,
        uv[2],
        uv[3],
        normalX,
        normalY,
        normalZ,
        packedLight);
    vertex(
        vertexConsumer,
        poseStack,
        x4,
        y4,
        z4,
        uv[0],
        uv[3],
        normalX,
        normalY,
        normalZ,
        packedLight);
  }

  private static void vertex(
      VertexConsumer vertexConsumer,
      PoseStack poseStack,
      float x,
      float y,
      float z,
      float u,
      float v,
      float normalX,
      float normalY,
      float normalZ,
      int packedLight) {
    PoseStack.Pose pose = poseStack.last();
    vertexConsumer
        .vertex(pose.pose(), x, y, z)
        .color(255, 255, 255, 255)
        .uv(u, v)
        .overlayCoords(OverlayTexture.NO_OVERLAY)
        .uv2(packedLight)
        .normal(pose.normal(), normalX, normalY, normalZ)
        .endVertex();
  }
}
