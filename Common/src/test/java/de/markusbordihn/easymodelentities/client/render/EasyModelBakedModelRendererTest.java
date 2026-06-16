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

package de.markusbordihn.easymodelentities.client.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyFloat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import de.markusbordihn.easymodelentities.api.data.client.EasyModelPartAnimationContext;
import de.markusbordihn.easymodelentities.api.data.client.EasyModelPartAnimationMode;
import de.markusbordihn.easymodelentities.api.data.client.EasyModelPartTransform;
import de.markusbordihn.easymodelentities.data.model.FaceUv;
import de.markusbordihn.easymodelentities.data.model.ModelCubeFaceUvs;
import de.markusbordihn.easymodelentities.data.model.Vec3f;
import de.markusbordihn.easymodelentities.data.model.bake.BakedModel;
import de.markusbordihn.easymodelentities.data.model.bake.BakedModelCube;
import de.markusbordihn.easymodelentities.data.model.bake.BakedModelPart;
import de.markusbordihn.easymodelentities.data.profile.ModelBodyType;
import de.markusbordihn.easymodelentities.data.render.EasyModelRenderState;
import de.markusbordihn.easymodelentities.data.renderprofile.ModelAnimationMode;
import de.markusbordihn.easymodelentities.data.renderprofile.ModelAnimationSettings;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;

class EasyModelBakedModelRendererTest {

  private static ModelCubeFaceUvs faceUvs() {
    return new ModelCubeFaceUvs(
        new FaceUv(0.0f, 0.0f, 1.0f, 1.0f),
        new FaceUv(2.0f, 0.0f, 3.0f, 1.0f),
        new FaceUv(4.0f, 0.0f, 5.0f, 1.0f),
        new FaceUv(6.0f, 0.0f, 7.0f, 1.0f),
        new FaceUv(8.0f, 0.0f, 9.0f, 1.0f),
        new FaceUv(10.0f, 0.0f, 11.0f, 1.0f));
  }

  private static EasyModelRenderState renderState(BakedModel bakedModel) {
    return renderState(bakedModel, ModelBodyType.STATIC, ModelAnimationMode.NONE);
  }

  private static EasyModelRenderState renderState(
      BakedModel bakedModel, ModelBodyType bodyType, ModelAnimationMode animationMode) {
    return renderState(bakedModel, bodyType, animationMode, 1.0f);
  }

  private static EasyModelRenderState renderState(
      BakedModel bakedModel,
      ModelBodyType bodyType,
      ModelAnimationMode animationMode,
      float idleStrength) {
    return new EasyModelRenderState(
        bakedModel,
        new ResourceLocation("example", "textures/entity/uv_model.png"),
        1.0f,
        0.3f,
        bodyType,
        new ModelAnimationSettings(animationMode, 1.0f, 1.0f, idleStrength),
        false,
        false,
        List.of());
  }

  private static EasyModelPartTransform captureCuboidHeadIdle(float idleStrength) {
    BakedModel bakedModel =
        new BakedModel(
            new ResourceLocation("example", "chestling"),
            64,
            64,
            List.of(new BakedModelPart("head", Vec3f.ZERO, Vec3f.ZERO, List.of(), List.of())));
    VertexConsumer vertexConsumer = mock(VertexConsumer.class, Answers.RETURNS_SELF);
    AtomicReference<EasyModelPartAnimationContext> context = new AtomicReference<>();

    EasyModelBakedModelRenderer.render(
        bakedModel,
        renderState(bakedModel, ModelBodyType.CUBOID, ModelAnimationMode.AUTOMATIC, idleStrength),
        0.0f,
        0.0f,
        15.0f,
        animationContext -> {
          context.set(animationContext);
          return EasyModelPartTransform.NONE;
        },
        new PoseStack(),
        vertexConsumer,
        0);

    return context.get().automaticTransform();
  }

  private static void assertUv(
      List<Float> uValues, List<Float> vValues, int index, float expectedU, float expectedV) {
    assertEquals(expectedU, uValues.get(index), 0.0001f);
    assertEquals(expectedV, vValues.get(index), 0.0001f);
  }

  private static void assertNormal(
      List<Float> xValues,
      List<Float> yValues,
      List<Float> zValues,
      int index,
      float expectedX,
      float expectedY,
      float expectedZ) {
    assertEquals(expectedX, xValues.get(index), 0.0001f);
    assertEquals(expectedY, yValues.get(index), 0.0001f);
    assertEquals(expectedZ, zValues.get(index), 0.0001f);
  }

  @Test
  void rendersDistinctFaceUvs() {
    BakedModel bakedModel =
        new BakedModel(
            new ResourceLocation("example", "uv_model"),
            64,
            64,
            List.of(
                new BakedModelPart(
                    "root",
                    Vec3f.ZERO,
                    Vec3f.ZERO,
                    List.of(
                        new BakedModelCube(
                            new int[] {0, 0},
                            faceUvs(),
                            Vec3f.ZERO,
                            new Vec3f(1.0f, 1.0f, 1.0f),
                            false)),
                    List.of())));
    VertexConsumer vertexConsumer = mock(VertexConsumer.class, Answers.RETURNS_SELF);
    ArgumentCaptor<Float> uCaptor = ArgumentCaptor.forClass(Float.class);
    ArgumentCaptor<Float> vCaptor = ArgumentCaptor.forClass(Float.class);
    ArgumentCaptor<Float> normalXCaptor = ArgumentCaptor.forClass(Float.class);
    ArgumentCaptor<Float> normalYCaptor = ArgumentCaptor.forClass(Float.class);
    ArgumentCaptor<Float> normalZCaptor = ArgumentCaptor.forClass(Float.class);
    ArgumentCaptor<Float> zCaptor = ArgumentCaptor.forClass(Float.class);

    EasyModelBakedModelRenderer.render(
        bakedModel, renderState(bakedModel), 0.0f, 0.0f, new PoseStack(), vertexConsumer, 0);

    verify(vertexConsumer, times(24)).uv(uCaptor.capture(), vCaptor.capture());
    verify(vertexConsumer, times(24))
        .normal(any(), normalXCaptor.capture(), normalYCaptor.capture(), normalZCaptor.capture());
    verify(vertexConsumer, times(24)).vertex(any(), anyFloat(), anyFloat(), zCaptor.capture());
    assertUv(uCaptor.getAllValues(), vCaptor.getAllValues(), 0, 5.0f, 0.0f);
    assertUv(uCaptor.getAllValues(), vCaptor.getAllValues(), 1, 4.0f, 0.0f);
    assertUv(uCaptor.getAllValues(), vCaptor.getAllValues(), 4, 1.0f, 0.0f);
    assertUv(uCaptor.getAllValues(), vCaptor.getAllValues(), 5, 0.0f, 0.0f);
    assertUv(uCaptor.getAllValues(), vCaptor.getAllValues(), 8, 3.0f, 0.0f);
    assertUv(uCaptor.getAllValues(), vCaptor.getAllValues(), 12, 7.0f, 0.0f);
    assertUv(uCaptor.getAllValues(), vCaptor.getAllValues(), 16, 9.0f, 0.0f);
    assertUv(uCaptor.getAllValues(), vCaptor.getAllValues(), 20, 11.0f, 0.0f);
    assertEquals(0.0f, zCaptor.getAllValues().get(16), 0.0001f);
    assertEquals(0.0625f, zCaptor.getAllValues().get(20), 0.0001f);
    assertNormal(
        normalXCaptor.getAllValues(),
        normalYCaptor.getAllValues(),
        normalZCaptor.getAllValues(),
        16,
        0.0f,
        -1.0f,
        0.0f);
    assertNormal(
        normalXCaptor.getAllValues(),
        normalYCaptor.getAllValues(),
        normalZCaptor.getAllValues(),
        20,
        0.0f,
        1.0f,
        0.0f);
  }

  @Test
  void upAndDownFacesMapWestEdgeToMaxU() {
    BakedModel bakedModel =
        new BakedModel(
            new ResourceLocation("example", "uv_model"),
            64,
            64,
            List.of(
                new BakedModelPart(
                    "root",
                    Vec3f.ZERO,
                    Vec3f.ZERO,
                    List.of(
                        new BakedModelCube(
                            new int[] {0, 0},
                            faceUvs(),
                            Vec3f.ZERO,
                            new Vec3f(1.0f, 1.0f, 1.0f),
                            false)),
                    List.of())));
    VertexConsumer vertexConsumer = mock(VertexConsumer.class, Answers.RETURNS_SELF);
    ArgumentCaptor<Float> uCaptor = ArgumentCaptor.forClass(Float.class);
    ArgumentCaptor<Float> vCaptor = ArgumentCaptor.forClass(Float.class);
    ArgumentCaptor<Float> xCaptor = ArgumentCaptor.forClass(Float.class);
    ArgumentCaptor<Float> zCaptor = ArgumentCaptor.forClass(Float.class);

    EasyModelBakedModelRenderer.render(
        bakedModel, renderState(bakedModel), 0.0f, 0.0f, new PoseStack(), vertexConsumer, 0);

    verify(vertexConsumer, times(24)).uv(uCaptor.capture(), vCaptor.capture());
    verify(vertexConsumer, times(24))
        .vertex(any(), xCaptor.capture(), anyFloat(), zCaptor.capture());
    List<Float> uValues = uCaptor.getAllValues();
    List<Float> vValues = vCaptor.getAllValues();
    List<Float> xValues = xCaptor.getAllValues();
    List<Float> zValues = zCaptor.getAllValues();

    assertEquals(0.0f, xValues.get(16), 0.0001f);
    assertEquals(0.0f, zValues.get(16), 0.0001f);
    assertUv(uValues, vValues, 16, 9.0f, 0.0f);
    assertEquals(0.0625f, xValues.get(17), 0.0001f);
    assertUv(uValues, vValues, 17, 8.0f, 0.0f);

    assertEquals(0.0f, xValues.get(20), 0.0001f);
    assertUv(uValues, vValues, 20, 11.0f, 0.0f);
    assertEquals(0.0625f, xValues.get(21), 0.0001f);
    assertUv(uValues, vValues, 21, 10.0f, 0.0f);
  }

  @Test
  void forwardsPartsToCustomAnimator() {
    BakedModel bakedModel =
        new BakedModel(
            new ResourceLocation("example", "animated_part"),
            64,
            64,
            List.of(new BakedModelPart("crystal", Vec3f.ZERO, Vec3f.ZERO, List.of(), List.of())));
    VertexConsumer vertexConsumer = mock(VertexConsumer.class, Answers.RETURNS_SELF);
    AtomicReference<EasyModelPartAnimationContext> context = new AtomicReference<>();

    EasyModelBakedModelRenderer.render(
        bakedModel,
        renderState(bakedModel),
        0.0f,
        0.0f,
        12.0f,
        animationContext -> {
          context.set(animationContext);
          return new EasyModelPartTransform(0.0f, 0.25f, 0.0f);
        },
        new PoseStack(),
        vertexConsumer,
        0);

    assertEquals("crystal", context.get().partName());
    assertEquals(12.0f, context.get().ageInTicks());
  }

  @Test
  void forwardsAutomaticTransformToCustomAnimator() {
    BakedModel bakedModel =
        new BakedModel(
            new ResourceLocation("example", "animated_leg"),
            64,
            64,
            List.of(new BakedModelPart("left_leg", Vec3f.ZERO, Vec3f.ZERO, List.of(), List.of())));
    VertexConsumer vertexConsumer = mock(VertexConsumer.class, Answers.RETURNS_SELF);
    AtomicReference<EasyModelPartAnimationContext> context = new AtomicReference<>();

    EasyModelBakedModelRenderer.render(
        bakedModel,
        renderState(bakedModel, ModelBodyType.BIPED, ModelAnimationMode.AUTOMATIC),
        1.0f,
        1.0f,
        12.0f,
        animationContext -> {
          context.set(animationContext);
          return new EasyModelPartTransform(0.0f, 0.25f, 0.0f);
        },
        new PoseStack(),
        vertexConsumer,
        0);

    assertNotEquals(EasyModelPartTransform.NONE, context.get().automaticTransform());
  }

  @Test
  void cuboidIdleRotatesHeadLidScaledByIdleStrength() {
    EasyModelPartTransform singleStrength = captureCuboidHeadIdle(1.0f);
    EasyModelPartTransform doubleStrength = captureCuboidHeadIdle(2.0f);

    assertNotEquals(0.0f, singleStrength.xRotation());
    assertEquals(0.0f, singleStrength.yRotation(), 0.0001f);
    assertEquals(0.0f, singleStrength.zRotation(), 0.0001f);
    assertEquals(2.0f * singleStrength.xRotation(), doubleStrength.xRotation(), 0.0001f);
  }

  @Test
  void replacePartAnimationModeSuppressesAutomaticTransform() {
    BakedModel bakedModel =
        new BakedModel(
            new ResourceLocation("example", "replace_animation"),
            64,
            64,
            List.of(
                new BakedModelPart(
                    "left_leg",
                    Vec3f.ZERO,
                    Vec3f.ZERO,
                    List.of(
                        new BakedModelCube(
                            new int[] {0, 0},
                            faceUvs(),
                            Vec3f.ZERO,
                            new Vec3f(1.0f, 1.0f, 1.0f),
                            false)),
                    List.of())));
    VertexConsumer addVertexConsumer = mock(VertexConsumer.class, Answers.RETURNS_SELF);
    VertexConsumer replaceVertexConsumer = mock(VertexConsumer.class, Answers.RETURNS_SELF);
    EasyModelPartTransform animatorTransform = new EasyModelPartTransform(0.25f, 0.0f, 0.0f);
    ArgumentCaptor<Matrix4f> addPoseCaptor = ArgumentCaptor.forClass(Matrix4f.class);
    ArgumentCaptor<Matrix4f> replacePoseCaptor = ArgumentCaptor.forClass(Matrix4f.class);

    EasyModelBakedModelRenderer.render(
        bakedModel,
        renderState(bakedModel, ModelBodyType.BIPED, ModelAnimationMode.AUTOMATIC),
        0.0f,
        1.0f,
        12.0f,
        context -> animatorTransform,
        EasyModelPartAnimationMode.ADD,
        new PoseStack(),
        addVertexConsumer,
        0);
    EasyModelBakedModelRenderer.render(
        bakedModel,
        renderState(bakedModel, ModelBodyType.BIPED, ModelAnimationMode.AUTOMATIC),
        0.0f,
        1.0f,
        12.0f,
        context -> animatorTransform,
        EasyModelPartAnimationMode.REPLACE,
        new PoseStack(),
        replaceVertexConsumer,
        0);

    verify(addVertexConsumer, times(24))
        .vertex(addPoseCaptor.capture(), anyFloat(), anyFloat(), anyFloat());
    verify(replaceVertexConsumer, times(24))
        .vertex(replacePoseCaptor.capture(), anyFloat(), anyFloat(), anyFloat());
    assertEquals(Math.cos(1.65f), addPoseCaptor.getAllValues().get(0).m11(), 0.0001f);
    assertEquals(Math.cos(0.25f), replacePoseCaptor.getValue().m11(), 0.0001f);
  }
}
