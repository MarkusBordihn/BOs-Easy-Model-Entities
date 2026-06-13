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

  private EasyModelBakedModelRenderer() {}

  public static void render(
      BakedModel bakedModel,
      EasyModelRenderState renderState,
      float limbSwing,
      float limbSwingAmount,
      PoseStack poseStack,
      VertexConsumer vertexConsumer,
      int packedLight) {
    Objects.requireNonNull(bakedModel, "bakedModel");
    for (BakedModelPart part : bakedModel.rootParts()) {
      renderPart(
          part, renderState, limbSwing, limbSwingAmount, poseStack, vertexConsumer, packedLight);
    }
  }

  private static void renderPart(
      BakedModelPart part,
      EasyModelRenderState renderState,
      float limbSwing,
      float limbSwingAmount,
      PoseStack poseStack,
      VertexConsumer vertexConsumer,
      int packedLight) {
    poseStack.pushPose();
    float[] offset = part.offset();
    float[] rotation = part.rotation();
    poseStack.translate(offset[0] * PIXEL, offset[1] * PIXEL, offset[2] * PIXEL);
    rotate(
        poseStack,
        rotation[0] + animationRotation(part.name(), renderState, limbSwing, limbSwingAmount),
        rotation[1],
        rotation[2]);

    for (BakedModelCube cube : part.cubes()) {
      renderCube(cube, poseStack, vertexConsumer, packedLight, renderState.bakedModel());
    }
    for (BakedModelPart child : part.children()) {
      renderPart(
          child, renderState, limbSwing, limbSwingAmount, poseStack, vertexConsumer, packedLight);
    }

    poseStack.popPose();
  }

  private static float animationRotation(
      String partName, EasyModelRenderState renderState, float limbSwing, float limbSwingAmount) {
    if (renderState.fallbackModel()
        || renderState.animation().mode() == ModelAnimationMode.NONE
        || renderState.bodyType() == ModelBodyType.STATIC
        || limbSwingAmount <= 0.01f) {
      return 0.0f;
    }

    float swing =
        Mth.cos(limbSwing * 0.6662f + animationPhase(partName, renderState.bodyType()))
            * 1.4f
            * limbSwingAmount
            * renderState.animation().walkSpeedMultiplier();
    return animatesPart(partName, renderState.bodyType()) ? swing : 0.0f;
  }

  private static boolean animatesPart(String partName, ModelBodyType bodyType) {
    return switch (bodyType) {
      case BIPED ->
          "left_leg".equals(partName)
              || "right_leg".equals(partName)
              || "left_arm".equals(partName)
              || "right_arm".equals(partName);
      case QUADRUPED ->
          "front_left_leg".equals(partName)
              || "front_right_leg".equals(partName)
              || "back_left_leg".equals(partName)
              || "back_right_leg".equals(partName);
      case STATIC -> false;
    };
  }

  private static float animationPhase(String partName, ModelBodyType bodyType) {
    return switch (bodyType) {
      case BIPED -> "right_leg".equals(partName) || "left_arm".equals(partName) ? Mth.PI : 0.0f;
      case QUADRUPED ->
          "front_right_leg".equals(partName) || "back_left_leg".equals(partName) ? Mth.PI : 0.0f;
      case STATIC -> 0.0f;
    };
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
    float[] uv = uv(cube, bakedModel);

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
        uv,
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
        uv,
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
        uv,
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
        uv,
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
        uv,
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
        uv,
        packedLight);
  }

  private static float[] uv(BakedModelCube cube, BakedModel bakedModel) {
    int[] uvOffset = cube.uvOffset();
    float[] dimensions = cube.dimensions();
    float u0 = uvOffset[0] / (float) bakedModel.textureWidth();
    float v0 = uvOffset[1] / (float) bakedModel.textureHeight();
    float u1 = (uvOffset[0] + Math.max(dimensions[0], dimensions[2])) / bakedModel.textureWidth();
    float v1 = (uvOffset[1] + Math.max(dimensions[1], dimensions[2])) / bakedModel.textureHeight();
    return new float[] {u0, v0, u1, v1};
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
