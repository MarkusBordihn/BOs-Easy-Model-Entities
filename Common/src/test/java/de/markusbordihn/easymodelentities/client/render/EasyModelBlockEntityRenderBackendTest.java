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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import de.markusbordihn.easymodelentities.api.data.EasyModelAnimation;
import de.markusbordihn.easymodelentities.api.data.client.EasyModelPartAnimationContext;
import de.markusbordihn.easymodelentities.api.data.client.EasyModelPartTransform;
import de.markusbordihn.easymodelentities.data.model.CubeFaceVisibility;
import de.markusbordihn.easymodelentities.data.model.FaceUv;
import de.markusbordihn.easymodelentities.data.model.ModelAnimationBoneTrack;
import de.markusbordihn.easymodelentities.data.model.ModelAnimationClip;
import de.markusbordihn.easymodelentities.data.model.ModelAnimationKeyframe;
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
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import net.minecraft.SharedConstants;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.resources.Identifier;
import net.minecraft.server.Bootstrap;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;

class EasyModelBlockEntityRenderBackendTest {

  @BeforeAll
  static void bootstrapMinecraft() {
    SharedConstants.tryDetectVersion();
    Bootstrap.bootStrap();
  }

  private static ModelCubeFaceUvs faceUvs() {
    return new ModelCubeFaceUvs(
        new FaceUv(0.0f, 0.0f, 1.0f, 1.0f),
        new FaceUv(2.0f, 0.0f, 3.0f, 1.0f),
        new FaceUv(4.0f, 0.0f, 5.0f, 1.0f),
        new FaceUv(6.0f, 0.0f, 7.0f, 1.0f),
        new FaceUv(8.0f, 0.0f, 9.0f, 1.0f),
        new FaceUv(10.0f, 0.0f, 11.0f, 1.0f));
  }

  private static BakedModel animatedBakedModel() {
    ModelAnimationBoneTrack track =
        new ModelAnimationBoneTrack(
            List.of(
                new ModelAnimationKeyframe(0.0f, Vec3f.ZERO, false),
                new ModelAnimationKeyframe(1.0f, new Vec3f(1.0f, 0.0f, 0.0f), false)),
            List.of());
    BakedModelCube cube =
        new BakedModelCube(
            new int[] {0, 0},
            faceUvs(),
            Vec3f.ZERO,
            new Vec3f(1.0f, 1.0f, 1.0f),
            false,
            0,
            CubeFaceVisibility.ALL);
    return new BakedModel(
        Identifier.fromNamespaceAndPath("example", "animated_bell"),
        64,
        64,
        List.of(new BakedModelPart("body", Vec3f.ZERO, Vec3f.ZERO, List.of(cube), List.of())),
        Map.of(),
        false,
        Map.of("talk", new ModelAnimationClip("talk", 1.0f, false, Map.of("body", track))),
        null);
  }

  private static EasyModelRenderState renderState(BakedModel bakedModel) {
    return new EasyModelRenderState(
        bakedModel,
        Identifier.fromNamespaceAndPath("example", "textures/block/bell.png"),
        1.0f,
        0.0f,
        ModelBodyType.BIPED,
        new ModelAnimationSettings(ModelAnimationMode.AUTOMATIC, 1.0f, 1.0f),
        false,
        false,
        List.of());
  }

  private static float submitAutomaticRotation(EasyModelAnimationPlaybackFrame playbackFrame) {
    BakedModel bakedModel = animatedBakedModel();
    AtomicReference<EasyModelPartAnimationContext> capturedContext = new AtomicReference<>();
    EasyModelBlockEntityRenderState renderState = new EasyModelBlockEntityRenderState();
    renderState.easyModelRenderState = renderState(bakedModel);
    renderState.playbackFrame = playbackFrame;
    renderState.partAnimator =
        animationContext -> {
          capturedContext.set(animationContext);
          return EasyModelPartTransform.NONE;
        };

    SubmitNodeCollector submitNodeCollector = mock(SubmitNodeCollector.class);
    ArgumentCaptor<SubmitNodeCollector.CustomGeometryRenderer> rendererCaptor =
        ArgumentCaptor.forClass(SubmitNodeCollector.CustomGeometryRenderer.class);

    EasyModelBlockEntityRenderBackend.render(renderState, new PoseStack(), submitNodeCollector, 0);

    verify(submitNodeCollector, times(1))
        .submitCustomGeometry(any(), any(), rendererCaptor.capture());
    rendererCaptor
        .getValue()
        .render(new PoseStack().last(), mock(VertexConsumer.class, Answers.RETURNS_SELF));
    assertNotNull(capturedContext.get());
    return capturedContext.get().automaticTransform().xRotation();
  }

  @Test
  @DisplayName("The submitted geometry follows the playback frame of the render state")
  void submitFollowsPlaybackFrame() {
    float automaticRotation =
        submitAutomaticRotation(
            EasyModelAnimationPlaybackFrame.single(EasyModelAnimation.AUTO, 0.0f));
    float namedClipRotation =
        submitAutomaticRotation(
            EasyModelAnimationPlaybackFrame.single(EasyModelAnimation.named("talk"), 10.0f));

    assertEquals(0.0f, automaticRotation, 0.0001f);
    assertTrue(
        Math.abs(namedClipRotation) > 0.0001f,
        "the named clip of the playback frame must reach the submitted geometry");
  }
}
