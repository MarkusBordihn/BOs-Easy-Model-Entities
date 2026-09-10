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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyFloat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import de.markusbordihn.easymodelentities.api.client.EasyModelPartAnimator;
import de.markusbordihn.easymodelentities.api.client.EasyModelPartPoseListener;
import de.markusbordihn.easymodelentities.api.data.EasyModelAnimation;
import de.markusbordihn.easymodelentities.api.data.EasyModelDisplaySettings;
import de.markusbordihn.easymodelentities.api.data.EasyModelTextureBlend;
import de.markusbordihn.easymodelentities.api.data.EasyModelTextureSetting;
import de.markusbordihn.easymodelentities.api.data.client.EasyModelHeadLook;
import de.markusbordihn.easymodelentities.api.data.client.EasyModelPartAnimationContext;
import de.markusbordihn.easymodelentities.api.data.client.EasyModelPartAnimationMode;
import de.markusbordihn.easymodelentities.api.data.client.EasyModelPartPose;
import de.markusbordihn.easymodelentities.api.data.client.EasyModelPartTransform;
import de.markusbordihn.easymodelentities.data.model.CubeFaceVisibility;
import de.markusbordihn.easymodelentities.data.model.FaceUv;
import de.markusbordihn.easymodelentities.data.model.ModelAnimationBoneTrack;
import de.markusbordihn.easymodelentities.data.model.ModelAnimationClip;
import de.markusbordihn.easymodelentities.data.model.ModelAnimationClips;
import de.markusbordihn.easymodelentities.data.model.ModelAnimationKeyframe;
import de.markusbordihn.easymodelentities.data.model.ModelCubeFace;
import de.markusbordihn.easymodelentities.data.model.ModelCubeFaceUvs;
import de.markusbordihn.easymodelentities.data.model.Vec3f;
import de.markusbordihn.easymodelentities.data.model.bake.BakedModel;
import de.markusbordihn.easymodelentities.data.model.bake.BakedModelCube;
import de.markusbordihn.easymodelentities.data.model.bake.BakedModelPart;
import de.markusbordihn.easymodelentities.data.profile.ModelBodyType;
import de.markusbordihn.easymodelentities.data.render.EasyModelRenderState;
import de.markusbordihn.easymodelentities.data.renderprofile.ModelAnimationMode;
import de.markusbordihn.easymodelentities.data.renderprofile.ModelAnimationSettings;
import de.markusbordihn.easymodelentities.data.renderprofile.ModelGaitType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;
import org.junit.jupiter.api.DisplayName;
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
        ResourceLocation.fromNamespaceAndPath("example", "textures/entity/uv_model.png"),
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
            ResourceLocation.fromNamespaceAndPath("example", "chestling"),
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

  private static EasyModelPartTransform captureAutomaticTransform(
      String partName,
      ModelBodyType bodyType,
      ModelGaitType gait,
      float limbSwing,
      float limbSwingAmount,
      float ageInTicks,
      float airborneAmount) {
    BakedModel bakedModel =
        new BakedModel(
            ResourceLocation.fromNamespaceAndPath("example", "capture"),
            64,
            64,
            List.of(new BakedModelPart(partName, Vec3f.ZERO, Vec3f.ZERO, List.of(), List.of())));
    EasyModelRenderState renderState =
        new EasyModelRenderState(
            bakedModel,
            ResourceLocation.fromNamespaceAndPath("example", "textures/entity/uv_model.png"),
            1.0f,
            0.3f,
            bodyType,
            new ModelAnimationSettings(ModelAnimationMode.AUTOMATIC, 1.0f, 1.0f, 1.0f, gait),
            false,
            false,
            List.of());
    VertexConsumer vertexConsumer = mock(VertexConsumer.class, Answers.RETURNS_SELF);
    AtomicReference<EasyModelPartAnimationContext> context = new AtomicReference<>();

    EasyModelBakedModelRenderer.render(
        bakedModel,
        renderState,
        limbSwing,
        limbSwingAmount,
        ageInTicks,
        airborneAmount,
        animationContext -> {
          context.set(animationContext);
          return EasyModelPartTransform.NONE;
        },
        EasyModelPartAnimationMode.ADD,
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

  private static BakedModel singleCubeModel() {
    return new BakedModel(
        ResourceLocation.fromNamespaceAndPath("example", "transform_part"),
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
  }

  private static float forcedClipRotation(
      String clipName, EasyModelAnimation animation, float rotation) {
    ModelAnimationBoneTrack track =
        new ModelAnimationBoneTrack(
            List.of(new ModelAnimationKeyframe(0.0f, new Vec3f(rotation, 0.0f, 0.0f), false)),
            List.of());
    ModelAnimationClip clip = new ModelAnimationClip(clipName, 1.0f, false, Map.of("body", track));
    BakedModel bakedModel =
        new BakedModel(
            ResourceLocation.fromNamespaceAndPath("example", clipName),
            64,
            64,
            List.of(new BakedModelPart("body", Vec3f.ZERO, Vec3f.ZERO, List.of(), List.of())),
            Map.of(),
            false,
            Map.of(clipName, clip),
            null);
    AtomicReference<EasyModelPartAnimationContext> context = new AtomicReference<>();

    EasyModelBakedModelRenderer.render(
        bakedModel,
        renderState(bakedModel, ModelBodyType.BIPED, ModelAnimationMode.AUTOMATIC),
        0.0f,
        0.0f,
        0.0f,
        0.0f,
        0.0f,
        EasyModelHeadLook.NONE,
        animation,
        new PoseStack(),
        textureIndex -> mock(VertexConsumer.class, Answers.RETURNS_SELF),
        0,
        animationContext -> {
          context.set(animationContext);
          return EasyModelPartTransform.NONE;
        },
        EasyModelPartAnimationMode.ADD,
        EasyModelPartPoseListener.NONE);

    return context.get().automaticTransform().xRotation();
  }

  private static EasyModelPartTransform captureFallbackTransform(
      String partName, ModelBodyType bodyType, EasyModelAnimation animation, float animationTicks) {
    BakedModel bakedModel =
        new BakedModel(
            ResourceLocation.fromNamespaceAndPath("example", "fallback_animation"),
            64,
            64,
            List.of(new BakedModelPart(partName, Vec3f.ZERO, Vec3f.ZERO, List.of(), List.of())));
    AtomicReference<EasyModelPartAnimationContext> context = new AtomicReference<>();

    EasyModelBakedModelRenderer.render(
        bakedModel,
        renderState(bakedModel, bodyType, ModelAnimationMode.AUTOMATIC),
        0.0f,
        0.0f,
        animationTicks,
        0.0f,
        0.0f,
        EasyModelHeadLook.NONE,
        animation,
        new PoseStack(),
        textureIndex -> mock(VertexConsumer.class, Answers.RETURNS_SELF),
        0,
        animationContext -> {
          context.set(animationContext);
          return EasyModelPartTransform.NONE;
        },
        EasyModelPartAnimationMode.ADD,
        EasyModelPartPoseListener.NONE);

    return context.get().automaticTransform();
  }

  private static EasyModelPartTransform captureHeadLookTransform(
      String partName, ModelBodyType bodyType, EasyModelHeadLook headLook) {
    BakedModel bakedModel =
        new BakedModel(
            ResourceLocation.fromNamespaceAndPath("example", "head_look"),
            64,
            64,
            List.of(new BakedModelPart(partName, Vec3f.ZERO, Vec3f.ZERO, List.of(), List.of())));
    AtomicReference<EasyModelPartAnimationContext> context = new AtomicReference<>();

    EasyModelBakedModelRenderer.render(
        bakedModel,
        renderState(bakedModel, bodyType, ModelAnimationMode.NONE),
        0.0f,
        0.0f,
        0.0f,
        0.0f,
        0.0f,
        headLook,
        EasyModelAnimation.AUTO,
        new PoseStack(),
        textureIndex -> mock(VertexConsumer.class, Answers.RETURNS_SELF),
        0,
        animationContext -> {
          context.set(animationContext);
          return EasyModelPartTransform.NONE;
        },
        EasyModelPartAnimationMode.ADD,
        EasyModelPartPoseListener.NONE);

    return context.get().automaticTransform();
  }

  private static float forcedNamedClipRotation(
      String clipName, String selectedClipName, float rotation) {
    ModelAnimationBoneTrack track =
        new ModelAnimationBoneTrack(
            List.of(new ModelAnimationKeyframe(0.0f, new Vec3f(rotation, 0.0f, 0.0f), false)),
            List.of());
    ModelAnimationClip clip = new ModelAnimationClip(clipName, 1.0f, false, Map.of("body", track));
    BakedModel bakedModel =
        new BakedModel(
            ResourceLocation.fromNamespaceAndPath("example", clipName),
            64,
            64,
            List.of(new BakedModelPart("body", Vec3f.ZERO, Vec3f.ZERO, List.of(), List.of())),
            Map.of(),
            false,
            Map.of(clipName, clip),
            null);
    AtomicReference<EasyModelPartAnimationContext> context = new AtomicReference<>();

    EasyModelBakedModelRenderer.render(
        bakedModel,
        renderState(bakedModel, ModelBodyType.BIPED, ModelAnimationMode.AUTOMATIC),
        0.0f,
        0.0f,
        0.0f,
        0.0f,
        0.0f,
        EasyModelHeadLook.NONE,
        selectedClipName == null
            ? EasyModelAnimation.AUTO
            : EasyModelAnimation.named(selectedClipName),
        new PoseStack(),
        textureIndex -> mock(VertexConsumer.class, Answers.RETURNS_SELF),
        0,
        animationContext -> {
          context.set(animationContext);
          return EasyModelPartTransform.NONE;
        },
        EasyModelPartAnimationMode.ADD,
        EasyModelPartPoseListener.NONE);

    return context.get().automaticTransform().xRotation();
  }

  private static BakedModel staticClipModel() {
    ModelAnimationBoneTrack track =
        new ModelAnimationBoneTrack(
            List.of(new ModelAnimationKeyframe(0.0f, new Vec3f(0.4f, 0.0f, 0.0f), false)),
            List.of());
    ModelAnimationClip clip = new ModelAnimationClip("wave", 1.0f, false, Map.of("body", track));

    return new BakedModel(
        ResourceLocation.fromNamespaceAndPath("example", "statue"),
        64,
        64,
        List.of(
            new BakedModelPart("body", Vec3f.ZERO, Vec3f.ZERO, List.of(), List.of()),
            new BakedModelPart("head", Vec3f.ZERO, Vec3f.ZERO, List.of(), List.of())),
        Map.of(),
        false,
        Map.of("wave", clip),
        null);
  }

  private static EasyModelRenderState fallbackRenderState(BakedModel bakedModel) {
    return new EasyModelRenderState(
        bakedModel,
        ResourceLocation.fromNamespaceAndPath("example", "textures/entity/uv_model.png"),
        1.0f,
        0.3f,
        ModelBodyType.STATIC,
        new ModelAnimationSettings(ModelAnimationMode.NONE, 1.0f, 1.0f, 1.0f),
        true,
        false,
        List.of());
  }

  private static Map<String, EasyModelPartTransform> staticClipTransforms(
      EasyModelAnimation animation, float attackAmount) {
    BakedModel bakedModel = staticClipModel();
    return clipTransforms(renderState(bakedModel), bakedModel, animation, attackAmount);
  }

  private static Map<String, EasyModelPartTransform> clipTransforms(
      EasyModelRenderState renderState,
      BakedModel bakedModel,
      EasyModelAnimation animation,
      float attackAmount) {
    Map<String, EasyModelPartTransform> transforms = new HashMap<>();

    EasyModelBakedModelRenderer.render(
        bakedModel,
        renderState,
        0.0f,
        0.0f,
        0.0f,
        0.0f,
        attackAmount,
        EasyModelHeadLook.NONE,
        animation,
        new PoseStack(),
        textureIndex -> mock(VertexConsumer.class, Answers.RETURNS_SELF),
        0,
        animationContext -> {
          transforms.put(animationContext.partName(), animationContext.automaticTransform());
          return EasyModelPartTransform.NONE;
        },
        EasyModelPartAnimationMode.ADD,
        EasyModelPartPoseListener.NONE);

    return transforms;
  }

  private static BakedModel variantModel(float length, boolean loop, String... clipNames) {
    Map<String, ModelAnimationClip> clips = new HashMap<>();
    for (String clipName : clipNames) {
      clips.put(clipName, new ModelAnimationClip(clipName, length, loop, Map.of()));
    }

    return new BakedModel(
        ResourceLocation.fromNamespaceAndPath("example", "variants"),
        64,
        64,
        List.of(new BakedModelPart("body", Vec3f.ZERO, Vec3f.ZERO, List.of(), List.of())),
        Map.of(),
        false,
        clips,
        null);
  }

  private static void renderWithOpacity(
      BakedModel bakedModel, MultiBufferSource bufferSource, float opacity) {
    EasyModelBakedModelRenderer.render(
        bakedModel,
        renderState(bakedModel),
        0.0f,
        0.0f,
        0.0f,
        0.0f,
        0.0f,
        EasyModelHeadLook.NONE,
        EasyModelAnimation.AUTO,
        EasyModelTextureSetting.EMPTY,
        EasyModelPartAnimator.NONE,
        EasyModelPartAnimationMode.ADD,
        EasyModelPartPoseListener.NONE,
        new PoseStack(),
        bufferSource,
        0,
        OverlayTexture.NO_OVERLAY,
        opacity,
        EasyModelDisplaySettings.NO_LIGHT_LEVEL);
  }

  @Test
  void playbackLoopOverrideControlsGenericClipSampling() {
    BakedModel bakedModel =
        new BakedModel(
            ResourceLocation.fromNamespaceAndPath("example", "sampling"), 16, 16, List.of());
    EasyModelRenderState renderState = renderState(bakedModel);
    ModelAnimationClip looping = new ModelAnimationClip("wave", 2.0f, true, Map.of());
    ModelAnimationClip once = new ModelAnimationClip("wave", 2.0f, false, Map.of());

    assertEquals(
        2.0f,
        EasyModelBakedModelRenderer.clipTime(
            looping, renderState, EasyModelAnimation.named("wave"), 0.0f, 60.0f, 0.0f, false),
        0.0001f);
    assertEquals(
        1.0f,
        EasyModelBakedModelRenderer.clipTime(
            once, renderState, EasyModelAnimation.named("wave"), 0.0f, 60.0f, 0.0f, true),
        0.0001f);
    assertEquals(
        1.0f,
        EasyModelBakedModelRenderer.clipTime(
            looping, renderState, EasyModelAnimation.named("wave"), 0.0f, 60.0f, 0.0f, null),
        0.0001f);
  }

  @Test
  void headTurnsTowardsLookDirection() {
    EasyModelPartTransform head =
        captureHeadLookTransform(
            "head", ModelBodyType.FLOATING, EasyModelHeadLook.of(30.0f, -20.0f));

    assertEquals(30.0f * Mth.DEG_TO_RAD, head.yRotation(), 0.001f);
    assertEquals(-20.0f * Mth.DEG_TO_RAD, head.xRotation(), 0.001f);
  }

  @Test
  void cuboidHeadStaysClosedOnLookDirection() {
    EasyModelPartTransform lid =
        captureHeadLookTransform("head", ModelBodyType.CUBOID, EasyModelHeadLook.of(30.0f, -20.0f));

    assertEquals(0.0f, lid.yRotation(), 0.001f);
    assertEquals(0.0f, lid.xRotation(), 0.001f);
  }

  @Test
  void bodyKeepsItsOwnRotationOnLookDirection() {
    EasyModelPartTransform body =
        captureHeadLookTransform("body", ModelBodyType.BIPED, EasyModelHeadLook.of(30.0f, -20.0f));

    assertEquals(0.0f, body.yRotation(), 0.001f);
    assertEquals(0.0f, body.xRotation(), 0.001f);
  }

  @Test
  void headLookClampsToTheRangeOfAVanillaHead() {
    assertEquals(75.0f, EasyModelHeadLook.of(120.0f, 0.0f).yaw(), 0.001f);
    assertEquals(-60.0f, EasyModelHeadLook.of(0.0f, -90.0f).pitch(), 0.001f);
    assertEquals(EasyModelHeadLook.NONE, EasyModelHeadLook.of(Float.NaN, 0.0f));
  }

  @Test
  void wingsFoldAgainstBodyWhenGrounded() {
    EasyModelPartTransform leftWing =
        captureAutomaticTransform(
            "left_wing", ModelBodyType.WINGED, ModelGaitType.NATURAL, 0.0f, 0.0f, 0.0f, 0.0f);
    EasyModelPartTransform rightWing =
        captureAutomaticTransform(
            "right_wing", ModelBodyType.WINGED, ModelGaitType.NATURAL, 0.0f, 0.0f, 0.0f, 0.0f);

    assertEquals(-1.4f, leftWing.zRotation(), 0.001f);
    assertEquals(1.4f, rightWing.zRotation(), 0.001f);
  }

  @Test
  void wingedHumanoidWingsStaySpreadWhenGrounded() {
    EasyModelPartTransform leftWing =
        captureAutomaticTransform(
            "left_wing",
            ModelBodyType.WINGED_HUMANOID,
            ModelGaitType.NATURAL,
            0.0f,
            0.0f,
            0.0f,
            0.0f);

    assertEquals(0.0f, leftWing.zRotation(), 0.001f);
  }

  @Test
  void wingsSpreadAndFlapWhenAirborne() {
    EasyModelPartTransform grounded =
        captureAutomaticTransform(
            "left_wing", ModelBodyType.WINGED, ModelGaitType.NATURAL, 0.0f, 0.0f, 0.0f, 0.0f);
    EasyModelPartTransform airborne =
        captureAutomaticTransform(
            "left_wing", ModelBodyType.WINGED, ModelGaitType.NATURAL, 0.0f, 0.0f, 0.0f, 1.0f);

    assertEquals(-1.4f, grounded.zRotation(), 0.001f);
    assertEquals(0.9f, airborne.zRotation(), 0.001f);
  }

  @Test
  void quadrupedLegSwingStaysVisibleAtLowSpeed() {
    EasyModelPartTransform slowStep =
        captureAutomaticTransform(
            "front_left_leg",
            ModelBodyType.QUADRUPED,
            ModelGaitType.NATURAL,
            0.0f,
            0.05f,
            0.0f,
            0.0f);

    assertEquals((float) (Math.PI / 4.0), slowStep.xRotation(), 0.001f);
  }

  @Test
  void gaitScalesQuadrupedStride() {
    EasyModelPartTransform feline =
        captureAutomaticTransform(
            "front_left_leg",
            ModelBodyType.QUADRUPED,
            ModelGaitType.FELINE,
            0.0f,
            0.04f,
            0.0f,
            0.0f);
    EasyModelPartTransform ungulate =
        captureAutomaticTransform(
            "front_left_leg",
            ModelBodyType.QUADRUPED,
            ModelGaitType.UNGULATE,
            0.0f,
            0.04f,
            0.0f,
            0.0f);

    assertEquals(0.7f * (float) (Math.PI / 4.0), feline.xRotation(), 0.001f);
    assertEquals(1.3f * (float) (Math.PI / 4.0), ungulate.xRotation(), 0.001f);
  }

  @Test
  void culledFacesAreNotEmitted() {
    CubeFaceVisibility visibility =
        CubeFaceVisibility.ALL.without(ModelCubeFace.NORTH).without(ModelCubeFace.DOWN);
    BakedModel bakedModel =
        new BakedModel(
            ResourceLocation.fromNamespaceAndPath("example", "culled"),
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
                            false,
                            0,
                            visibility)),
                    List.of())));
    VertexConsumer vertexConsumer = mock(VertexConsumer.class, Answers.RETURNS_SELF);

    EasyModelBakedModelRenderer.render(
        bakedModel, renderState(bakedModel), 0.0f, 0.0f, new PoseStack(), vertexConsumer, 0);

    verify(vertexConsumer, times(16))
        .addVertex((Matrix4f) any(), anyFloat(), anyFloat(), anyFloat());
  }

  @Test
  void fullyCulledCubeEmitsNothing() {
    BakedModel bakedModel =
        new BakedModel(
            ResourceLocation.fromNamespaceAndPath("example", "empty"),
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
                            false,
                            0,
                            CubeFaceVisibility.NONE)),
                    List.of())));
    VertexConsumer vertexConsumer = mock(VertexConsumer.class, Answers.RETURNS_SELF);

    EasyModelBakedModelRenderer.render(
        bakedModel, renderState(bakedModel), 0.0f, 0.0f, new PoseStack(), vertexConsumer, 0);

    verify(vertexConsumer, times(0))
        .addVertex((Matrix4f) any(), anyFloat(), anyFloat(), anyFloat());
  }

  @Test
  void rendersDistinctFaceUvs() {
    BakedModel bakedModel =
        new BakedModel(
            ResourceLocation.fromNamespaceAndPath("example", "uv_model"),
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

    verify(vertexConsumer, times(24)).setUv(uCaptor.capture(), vCaptor.capture());
    verify(vertexConsumer, times(24))
        .setNormal(
            any(), normalXCaptor.capture(), normalYCaptor.capture(), normalZCaptor.capture());
    verify(vertexConsumer, times(24))
        .addVertex((Matrix4f) any(), anyFloat(), anyFloat(), zCaptor.capture());
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
            ResourceLocation.fromNamespaceAndPath("example", "uv_model"),
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

    verify(vertexConsumer, times(24)).setUv(uCaptor.capture(), vCaptor.capture());
    verify(vertexConsumer, times(24))
        .addVertex((Matrix4f) any(), xCaptor.capture(), anyFloat(), zCaptor.capture());
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
            ResourceLocation.fromNamespaceAndPath("example", "animated_part"),
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
  void forcedStatesSelectMatchingClips() {
    assertEquals(
        0.25f, forcedClipRotation(ModelAnimationClips.HURT, EasyModelAnimation.HURT, 0.25f));
    assertEquals(
        0.75f, forcedClipRotation(ModelAnimationClips.DEATH, EasyModelAnimation.DEATH, 0.75f));
    assertEquals(
        0.5f, forcedClipRotation(ModelAnimationClips.ATTACK, EasyModelAnimation.ATTACK, 0.5f));
    assertEquals(0.3f, forcedClipRotation(ModelAnimationClips.SIT, EasyModelAnimation.SIT, 0.3f));
  }

  @Test
  @DisplayName("A forced state finds its clip even when only numbered variants exist")
  void forcedStatesResolveVariantOnlyGroups() {
    BakedModel bakedModel = variantModel(1.0f, true, "idle_2", "idle_3");

    assertEquals(
        ModelAnimationClips.IDLE,
        EasyModelBakedModelRenderer.forcedClipName(EasyModelAnimation.IDLE));
    assertEquals(
        "idle_2",
        EasyModelBakedModelRenderer.groupClip(bakedModel, ModelAnimationClips.IDLE).name());
  }

  @Test
  @DisplayName("A walk variant stays driven by the limb swing instead of the animation clock")
  void movementVariantsKeepTheLimbSwingClock() {
    BakedModel bakedModel = variantModel(1.0f, true, "walk", "walk_2");
    EasyModelRenderState renderState =
        renderState(bakedModel, ModelBodyType.BIPED, ModelAnimationMode.AUTOMATIC);
    ModelAnimationClip walk = bakedModel.animations().get(ModelAnimationClips.WALK);
    ModelAnimationClip walkVariant = bakedModel.animations().get("walk_2");

    assertEquals(
        EasyModelBakedModelRenderer.clipTime(
            walk, renderState, EasyModelAnimation.AUTO, 3.0f, 999.0f, 0.0f, null),
        EasyModelBakedModelRenderer.clipTime(
            walkVariant, renderState, EasyModelAnimation.AUTO, 3.0f, 0.0f, 0.0f, null),
        0.0001f);
  }

  @Test
  @DisplayName("An attack variant stays scrubbed by the attack progress")
  void attackVariantsKeepTheAttackClock() {
    BakedModel bakedModel = variantModel(2.0f, false, "attack", "attack_2");
    EasyModelRenderState renderState =
        renderState(bakedModel, ModelBodyType.BIPED, ModelAnimationMode.AUTOMATIC);

    assertEquals(
        0.5f,
        EasyModelBakedModelRenderer.clipTime(
            bakedModel.animations().get("attack_2"),
            renderState,
            EasyModelAnimation.AUTO,
            0.0f,
            999.0f,
            0.25f,
            null),
        0.0001f);
  }

  @Test
  void forcedMovementStatesAnimateStationaryModelsWithoutClips() {
    EasyModelPartTransform walk =
        captureFallbackTransform("left_leg", ModelBodyType.BIPED, EasyModelAnimation.WALK, 0.0f);
    EasyModelPartTransform run =
        captureFallbackTransform("left_leg", ModelBodyType.BIPED, EasyModelAnimation.RUN, 1.0f);
    EasyModelPartTransform swim =
        captureFallbackTransform("tail", ModelBodyType.AQUATIC, EasyModelAnimation.SWIM, 4.0f);
    EasyModelPartTransform fly =
        captureFallbackTransform("left_wing", ModelBodyType.WINGED, EasyModelAnimation.FLY, 0.0f);

    assertNotEquals(0.0f, walk.xRotation());
    assertNotEquals(walk.xRotation(), run.xRotation());
    assertNotEquals(0.0f, swim.yRotation());
    assertNotEquals(0.0f, fly.zRotation());
  }

  @Test
  void forcedActionStatesUseFallbackPosesWithoutClips() {
    EasyModelPartTransform attack =
        captureFallbackTransform("right_arm", ModelBodyType.BIPED, EasyModelAnimation.ATTACK, 6.0f);
    EasyModelPartTransform hurt =
        captureFallbackTransform("body", ModelBodyType.QUADRUPED, EasyModelAnimation.HURT, 5.0f);
    EasyModelPartTransform death =
        captureFallbackTransform("root", ModelBodyType.CUBOID, EasyModelAnimation.DEATH, 20.0f);

    assertNotEquals(0.0f, attack.xRotation());
    assertNotEquals(0.0f, hurt.xRotation());
    assertEquals(Mth.HALF_PI, death.zRotation(), 0.001f);
  }

  @Test
  void sitFallbackUsesBodyTypeSpecificPose() {
    EasyModelPartTransform bipedRoot =
        captureFallbackTransform("root", ModelBodyType.BIPED, EasyModelAnimation.SIT, 0.0f);
    EasyModelPartTransform bipedLeg =
        captureFallbackTransform("left_leg", ModelBodyType.BIPED, EasyModelAnimation.SIT, 0.0f);
    EasyModelPartTransform quadrupedBackLeg =
        captureFallbackTransform(
            "back_left_leg", ModelBodyType.QUADRUPED, EasyModelAnimation.SIT, 0.0f);

    assertEquals(4.0f, bipedRoot.offsetY(), 0.001f);
    assertEquals(-1.25f, bipedLeg.xRotation(), 0.001f);
    assertEquals(-1.05f, quadrupedBackLeg.xRotation(), 0.001f);
  }

  @Test
  void forcedNamedClipPlaysByNameOverAutomaticSelection() {
    assertEquals(0.4f, forcedNamedClipRotation("talk", "talk", 0.4f));
  }

  @Test
  void forcedNamedClipFallsBackWhenNameIsMissing() {
    float automatic = forcedNamedClipRotation("talk", null, 0.4f);
    float fallback = forcedNamedClipRotation("talk", "missing", 0.4f);
    assertEquals(automatic, fallback);
  }

  @Test
  @DisplayName("A statue stays still until a clip is asked for by name")
  void staticModelPlaysOnlyExplicitlyRequestedClips() {
    Map<String, EasyModelPartTransform> automatic =
        staticClipTransforms(EasyModelAnimation.AUTO, 0.0f);
    Map<String, EasyModelPartTransform> requested =
        staticClipTransforms(EasyModelAnimation.named("wave"), 0.0f);

    assertEquals(EasyModelPartTransform.NONE, automatic.get("body"));
    assertEquals(0.4f, requested.get("body").xRotation(), 0.001f);
  }

  @Test
  @DisplayName("A bone without a track must not fall back to the procedural idle on a statue")
  void staticModelKeepsUntrackedBonesAtRest() {
    Map<String, EasyModelPartTransform> transforms =
        staticClipTransforms(EasyModelAnimation.named("wave"), 0.0f);

    assertEquals(EasyModelPartTransform.NONE, transforms.get("head"));
  }

  @Test
  @DisplayName("The procedural attack pose must not reach a statue either")
  void staticModelSuppressesTheAttackPose() {
    Map<String, EasyModelPartTransform> transforms =
        staticClipTransforms(EasyModelAnimation.named("wave"), 1.0f);

    assertEquals(EasyModelPartTransform.NONE, transforms.get("head"));
  }

  @Test
  @DisplayName("A fallback model has no matching bone names, so it stays unanimated")
  void fallbackModelIgnoresExplicitlyRequestedClips() {
    Map<String, EasyModelPartTransform> transforms =
        clipTransforms(
            fallbackRenderState(staticClipModel()),
            staticClipModel(),
            EasyModelAnimation.named("wave"),
            0.0f);

    assertEquals(EasyModelPartTransform.NONE, transforms.get("body"));
  }

  @Test
  void crossFadePreservesPreviousProceduralAnimationTime() {
    BakedModel bakedModel =
        new BakedModel(
            ResourceLocation.fromNamespaceAndPath("example", "procedural_crossfade"),
            64,
            64,
            List.of(new BakedModelPart("body", Vec3f.ZERO, Vec3f.ZERO, List.of(), List.of())));
    AtomicReference<EasyModelPartAnimationContext> context = new AtomicReference<>();
    float previousAnimationTicks = 10.0f;

    EasyModelBakedModelRenderer.render(
        bakedModel,
        renderState(bakedModel, ModelBodyType.BIPED, ModelAnimationMode.AUTOMATIC),
        0.0f,
        0.0f,
        0.0f,
        0.0f,
        EasyModelHeadLook.NONE,
        new EasyModelAnimationPlaybackFrame(
            EasyModelAnimation.named("wave"),
            0.0f,
            EasyModelAnimation.AUTO,
            previousAnimationTicks,
            0.0f,
            null,
            null,
            true),
        new PoseStack(),
        textureIndex -> mock(VertexConsumer.class, Answers.RETURNS_SELF),
        0,
        animationContext -> {
          context.set(animationContext);
          return EasyModelPartTransform.NONE;
        },
        EasyModelPartAnimationMode.ADD,
        EasyModelPartPoseListener.NONE);

    assertEquals(
        Math.sin(previousAnimationTicks * 0.12f) * 0.025f,
        context.get().automaticTransform().xRotation(),
        0.0001f);
  }

  @Test
  void capturesTransformedPartPose() {
    BakedModelPart hand =
        new BakedModelPart(
            "right_hand", new Vec3f(16.0f, 32.0f, 48.0f), Vec3f.ZERO, List.of(), List.of());
    BakedModel bakedModel =
        new BakedModel(
            ResourceLocation.fromNamespaceAndPath("example", "part_pose"), 64, 64, List.of(hand));
    AtomicReference<EasyModelPartPose> capturedPose = new AtomicReference<>();

    EasyModelBakedModelRenderer.render(
        bakedModel,
        renderState(bakedModel),
        0.0f,
        0.0f,
        0.0f,
        0.0f,
        0.0f,
        EasyModelHeadLook.NONE,
        EasyModelAnimation.AUTO,
        new PoseStack(),
        textureIndex -> mock(VertexConsumer.class, Answers.RETURNS_SELF),
        0,
        EasyModelPartAnimator.NONE,
        EasyModelPartAnimationMode.ADD,
        capturedPose::set);

    assertEquals("right_hand", capturedPose.get().partName());
    assertEquals(1.0f, capturedPose.get().poseMatrix().m30(), 0.0001f);
    assertEquals(2.0f, capturedPose.get().poseMatrix().m31(), 0.0001f);
    assertEquals(3.0f, capturedPose.get().poseMatrix().m32(), 0.0001f);
  }

  @Test
  void forwardsAutomaticTransformToCustomAnimator() {
    BakedModel bakedModel =
        new BakedModel(
            ResourceLocation.fromNamespaceAndPath("example", "animated_leg"),
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
            ResourceLocation.fromNamespaceAndPath("example", "replace_animation"),
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
        .addVertex(addPoseCaptor.capture(), anyFloat(), anyFloat(), anyFloat());
    verify(replaceVertexConsumer, times(24))
        .addVertex(replacePoseCaptor.capture(), anyFloat(), anyFloat(), anyFloat());
    assertEquals(Math.cos(1.65f), addPoseCaptor.getAllValues().get(0).m11(), 0.0001f);
    assertEquals(Math.cos(0.25f), replacePoseCaptor.getValue().m11(), 0.0001f);
  }

  @Test
  void animatorOffsetTranslatesPart() {
    BakedModel bakedModel = singleCubeModel();
    VertexConsumer vertexConsumer = mock(VertexConsumer.class, Answers.RETURNS_SELF);
    ArgumentCaptor<Matrix4f> poseCaptor = ArgumentCaptor.forClass(Matrix4f.class);

    EasyModelBakedModelRenderer.render(
        bakedModel,
        renderState(bakedModel),
        0.0f,
        0.0f,
        0.0f,
        context -> EasyModelPartTransform.NONE.withOffset(16.0f, 32.0f, 48.0f),
        EasyModelPartAnimationMode.ADD,
        new PoseStack(),
        vertexConsumer,
        0);

    verify(vertexConsumer, times(24))
        .addVertex(poseCaptor.capture(), anyFloat(), anyFloat(), anyFloat());
    Matrix4f pose = poseCaptor.getAllValues().get(0);
    assertEquals(1.0f, pose.m30(), 0.0001f);
    assertEquals(2.0f, pose.m31(), 0.0001f);
    assertEquals(3.0f, pose.m32(), 0.0001f);
  }

  @Test
  void animatorScaleScalesPart() {
    BakedModel bakedModel = singleCubeModel();
    VertexConsumer vertexConsumer = mock(VertexConsumer.class, Answers.RETURNS_SELF);
    ArgumentCaptor<Matrix4f> poseCaptor = ArgumentCaptor.forClass(Matrix4f.class);

    EasyModelBakedModelRenderer.render(
        bakedModel,
        renderState(bakedModel),
        0.0f,
        0.0f,
        0.0f,
        context -> EasyModelPartTransform.NONE.withScale(2.0f),
        EasyModelPartAnimationMode.ADD,
        new PoseStack(),
        vertexConsumer,
        0);

    verify(vertexConsumer, times(24))
        .addVertex(poseCaptor.capture(), anyFloat(), anyFloat(), anyFloat());
    Matrix4f pose = poseCaptor.getAllValues().get(0);
    assertEquals(2.0f, pose.m00(), 0.0001f);
    assertEquals(2.0f, pose.m11(), 0.0001f);
    assertEquals(2.0f, pose.m22(), 0.0001f);
  }

  @Test
  void invisibleAnimatorSuppressesPart() {
    BakedModel bakedModel = singleCubeModel();
    VertexConsumer vertexConsumer = mock(VertexConsumer.class, Answers.RETURNS_SELF);

    EasyModelBakedModelRenderer.render(
        bakedModel,
        renderState(bakedModel),
        0.0f,
        0.0f,
        0.0f,
        context -> EasyModelPartTransform.NONE.withVisible(false),
        EasyModelPartAnimationMode.ADD,
        new PoseStack(),
        vertexConsumer,
        0);

    verify(vertexConsumer, never()).addVertex((Matrix4f) any(), anyFloat(), anyFloat(), anyFloat());
  }

  @Test
  void consecutiveSameTextureCubesShareOneBufferFetch() {
    BakedModelCube cube =
        new BakedModelCube(
            new int[] {0, 0},
            faceUvs(),
            Vec3f.ZERO,
            new Vec3f(1.0f, 1.0f, 1.0f),
            false,
            0,
            CubeFaceVisibility.ALL);
    BakedModel bakedModel =
        new BakedModel(
            ResourceLocation.fromNamespaceAndPath("example", "multi_cube"),
            64,
            64,
            List.of(
                new BakedModelPart(
                    "root", Vec3f.ZERO, Vec3f.ZERO, List.of(cube, cube, cube), List.of())));
    VertexConsumer vertexConsumer = mock(VertexConsumer.class, Answers.RETURNS_SELF);
    AtomicInteger fetches = new AtomicInteger();

    EasyModelBakedModelRenderer.render(
        bakedModel,
        renderState(bakedModel),
        0.0f,
        0.0f,
        0.0f,
        0.0f,
        0.0f,
        EasyModelHeadLook.NONE,
        EasyModelAnimation.AUTO,
        new PoseStack(),
        textureIndex -> {
          fetches.incrementAndGet();
          return vertexConsumer;
        },
        0,
        EasyModelPartAnimator.NONE,
        EasyModelPartAnimationMode.ADD,
        EasyModelPartPoseListener.NONE);

    assertEquals(1, fetches.get(), "three same-texture cubes must fetch the buffer once");
    verify(vertexConsumer, times(3 * 24))
        .addVertex((Matrix4f) any(), anyFloat(), anyFloat(), anyFloat());
  }

  @Test
  void rendersCubeWhoseTextureReappearsAfterAnotherTexture() {
    BakedModelCube cube0 =
        new BakedModelCube(
            new int[] {0, 0},
            faceUvs(),
            Vec3f.ZERO,
            new Vec3f(1.0f, 1.0f, 1.0f),
            false,
            0,
            de.markusbordihn.easymodelentities.data.model.CubeFaceVisibility.ALL);
    BakedModelCube cube1 =
        new BakedModelCube(
            new int[] {0, 0},
            faceUvs(),
            Vec3f.ZERO,
            new Vec3f(1.0f, 1.0f, 1.0f),
            false,
            1,
            de.markusbordihn.easymodelentities.data.model.CubeFaceVisibility.ALL);
    BakedModelPart head =
        new BakedModelPart(
            "head",
            Vec3f.ZERO,
            Vec3f.ZERO,
            List.of(cube0),
            List.of(
                new BakedModelPart("Lid_r1", Vec3f.ZERO, Vec3f.ZERO, List.of(cube0), List.of()),
                new BakedModelPart("eyes", Vec3f.ZERO, Vec3f.ZERO, List.of(cube1), List.of())));
    BakedModelPart body =
        new BakedModelPart(
            "body",
            Vec3f.ZERO,
            Vec3f.ZERO,
            List.of(),
            List.of(
                new BakedModelPart("Base_r1", Vec3f.ZERO, Vec3f.ZERO, List.of(cube0), List.of())));
    BakedModel bakedModel =
        new BakedModel(
            ResourceLocation.fromNamespaceAndPath("example", "chestling"),
            64,
            64,
            List.of(
                new BakedModelPart(
                    "root", Vec3f.ZERO, Vec3f.ZERO, List.of(), List.of(head, body))));

    SharedBuilderBufferProvider bufferProvider = new SharedBuilderBufferProvider();
    EasyModelBakedModelRenderer.render(
        bakedModel,
        renderState(bakedModel),
        0.0f,
        0.0f,
        0.0f,
        0.0f,
        0.0f,
        EasyModelHeadLook.NONE,
        EasyModelAnimation.AUTO,
        new PoseStack(),
        bufferProvider,
        0,
        EasyModelPartAnimator.NONE,
        EasyModelPartAnimationMode.ADD,
        EasyModelPartPoseListener.NONE);

    assertEquals(0, bufferProvider.droppedVertices, "no vertices may be written to a stale buffer");
    assertEquals(4 * 24, bufferProvider.recordedVertices, "all four cubes must be rendered");
  }

  @Test
  void opacityReachesTheVertexColor() {
    BakedModel bakedModel = singleCubeModel();
    MultiBufferSource bufferSource = mock(MultiBufferSource.class);
    VertexConsumer vertexConsumer = mock(VertexConsumer.class, Answers.RETURNS_SELF);
    when(bufferSource.getBuffer(any())).thenReturn(vertexConsumer);

    renderWithOpacity(bakedModel, bufferSource, 0.4f);

    verify(vertexConsumer, times(24)).setColor(255, 255, 255, 102);
  }

  @Test
  void fullOpacityKeepsTheOpaqueVertexColor() {
    BakedModel bakedModel = singleCubeModel();
    MultiBufferSource bufferSource = mock(MultiBufferSource.class);
    VertexConsumer vertexConsumer = mock(VertexConsumer.class, Answers.RETURNS_SELF);
    when(bufferSource.getBuffer(any())).thenReturn(vertexConsumer);

    renderWithOpacity(bakedModel, bufferSource, EasyModelDisplaySettings.MAX_OPACITY);

    verify(vertexConsumer, times(24)).setColor(255, 255, 255, 255);
  }

  @Test
  @DisplayName("A cutout render type would drop the alpha, so a faded model must turn translucent")
  void partialOpacityForcesATranslucentRenderType() {
    BakedModel bakedModel = singleCubeModel();
    MultiBufferSource bufferSource = mock(MultiBufferSource.class);
    when(bufferSource.getBuffer(any()))
        .thenReturn(mock(VertexConsumer.class, Answers.RETURNS_SELF));
    ArgumentCaptor<RenderType> renderTypeCaptor = ArgumentCaptor.forClass(RenderType.class);

    renderWithOpacity(bakedModel, bufferSource, 0.4f);

    verify(bufferSource).getBuffer(renderTypeCaptor.capture());
    assertEquals(
        RenderType.entityTranslucent(
            ResourceLocation.fromNamespaceAndPath("example", "textures/entity/uv_model.png")),
        renderTypeCaptor.getValue());
  }

  @Test
  void fullOpacityKeepsTheCutoutRenderType() {
    BakedModel bakedModel = singleCubeModel();
    MultiBufferSource bufferSource = mock(MultiBufferSource.class);
    when(bufferSource.getBuffer(any()))
        .thenReturn(mock(VertexConsumer.class, Answers.RETURNS_SELF));
    ArgumentCaptor<RenderType> renderTypeCaptor = ArgumentCaptor.forClass(RenderType.class);

    renderWithOpacity(bakedModel, bufferSource, EasyModelDisplaySettings.MAX_OPACITY);

    verify(bufferSource).getBuffer(renderTypeCaptor.capture());
    assertEquals(
        RenderType.entityCutoutNoCull(
            ResourceLocation.fromNamespaceAndPath("example", "textures/entity/uv_model.png")),
        renderTypeCaptor.getValue());
  }

  @Test
  @DisplayName("An emissive slot picks a fullbright render type, a cutout slot does not")
  void emissiveBlendSelectsAnEmissiveRenderType() {
    ResourceLocation texture = ResourceLocation.fromNamespaceAndPath("example", "textures/entity/uv_model.png");

    assertEquals(
        RenderType.entityTranslucentEmissive(texture),
        EasyModelBakedModelRenderer.renderType(
            texture, EasyModelTextureBlend.EMISSIVE, false, false));
    assertNotEquals(
        EasyModelBakedModelRenderer.renderType(texture, EasyModelTextureBlend.CUTOUT, false, false),
        EasyModelBakedModelRenderer.renderType(
            texture, EasyModelTextureBlend.EMISSIVE, false, false));
  }

  @Test
  @DisplayName("Emissive has no cull variant, so backface culling cannot change its render type")
  void emissiveBlendIgnoresBackfaceCulling() {
    ResourceLocation texture = ResourceLocation.fromNamespaceAndPath("example", "textures/entity/uv_model.png");

    assertEquals(
        EasyModelBakedModelRenderer.renderType(
            texture, EasyModelTextureBlend.EMISSIVE, false, false),
        EasyModelBakedModelRenderer.renderType(
            texture, EasyModelTextureBlend.EMISSIVE, true, false));
  }

  @Test
  @DisplayName("A faded emissive slot keeps the emissive render type, which already blends alpha")
  void fadedEmissiveBlendStaysEmissive() {
    ResourceLocation texture = ResourceLocation.fromNamespaceAndPath("example", "textures/entity/uv_model.png");

    assertEquals(
        RenderType.entityTranslucentEmissive(texture),
        EasyModelBakedModelRenderer.renderType(
            texture, EasyModelTextureBlend.EMISSIVE, false, true));
  }

  @Test
  void zeroOpacityDrawsNothing() {
    BakedModel bakedModel = singleCubeModel();
    MultiBufferSource bufferSource = mock(MultiBufferSource.class);

    renderWithOpacity(bakedModel, bufferSource, EasyModelDisplaySettings.MIN_OPACITY);

    verify(bufferSource, never()).getBuffer(any());
  }

  @Test
  @DisplayName("The light level override raises the block light without touching the sky light")
  void lightLevelOverrideRaisesOnlyTheBlockLight() {
    int packedLight = LightTexture.pack(2, 11);

    int packedLightWithOverride =
        EasyModelBakedModelRenderer.packedLightWithOverride(packedLight, 9);

    assertEquals(9, LightTexture.block(packedLightWithOverride));
    assertEquals(11, LightTexture.sky(packedLightWithOverride));
  }

  @Test
  void lightLevelOverrideNeverDarkensTheModel() {
    int packedLight = LightTexture.pack(13, 4);

    assertEquals(packedLight, EasyModelBakedModelRenderer.packedLightWithOverride(packedLight, 9));
    assertEquals(
        packedLight,
        EasyModelBakedModelRenderer.packedLightWithOverride(
            packedLight, EasyModelDisplaySettings.NO_LIGHT_LEVEL));
  }

  private static final class SharedBuilderBufferProvider
      implements java.util.function.IntFunction<VertexConsumer> {

    private int activeTextureIndex = -1;
    private RecordingConsumer activeConsumer;
    private int recordedVertices;
    private int droppedVertices;

    @Override
    public VertexConsumer apply(int textureIndex) {
      if (textureIndex != this.activeTextureIndex) {
        if (this.activeConsumer != null) {
          this.activeConsumer.valid = false;
        }
        this.activeConsumer = new RecordingConsumer();
        this.activeTextureIndex = textureIndex;
      }
      return this.activeConsumer;
    }

    private final class RecordingConsumer implements VertexConsumer {
      private boolean valid = true;

      @Override
      public VertexConsumer addVertex(float x, float y, float z) {
        if (this.valid) {
          SharedBuilderBufferProvider.this.recordedVertices++;
        } else {
          SharedBuilderBufferProvider.this.droppedVertices++;
        }
        return this;
      }

      @Override
      public VertexConsumer setColor(int red, int green, int blue, int alpha) {
        return this;
      }

      @Override
      public VertexConsumer setUv(float u, float v) {
        return this;
      }

      @Override
      public VertexConsumer setUv2(int u, int v) {
        return this;
      }

      @Override
      public VertexConsumer setUv1(int u, int v) {
        return this;
      }

      @Override
      public VertexConsumer setNormal(float x, float y, float z) {
        return this;
      }
    }
  }
}
