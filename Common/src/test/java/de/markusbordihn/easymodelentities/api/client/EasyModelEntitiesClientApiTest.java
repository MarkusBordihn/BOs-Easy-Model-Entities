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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.markusbordihn.easymodelentities.api.data.EasyModelAnimation;
import de.markusbordihn.easymodelentities.api.data.EasyModelProfileType;
import de.markusbordihn.easymodelentities.api.data.client.EasyModelAnimationInfo;
import de.markusbordihn.easymodelentities.api.data.client.EasyModelAnimationType;
import de.markusbordihn.easymodelentities.data.model.ModelAnimationBoneTrack;
import de.markusbordihn.easymodelentities.data.model.ModelAnimationClip;
import de.markusbordihn.easymodelentities.data.model.ModelAnimationKeyframe;
import de.markusbordihn.easymodelentities.data.model.Vec3f;
import de.markusbordihn.easymodelentities.data.renderprofile.EasyModelRenderProfile;
import de.markusbordihn.easymodelentities.registry.EasyModelServices;
import de.markusbordihn.easymodelentities.renderprofile.EasyModelRenderProfileService;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class EasyModelEntitiesClientApiTest {

  private static ModelAnimationBoneTrack idleTrack() {
    return new ModelAnimationBoneTrack(
        List.of(new ModelAnimationKeyframe(0.0f, new Vec3f(0.0f, 0.0f, 0.0f), false)), List.of());
  }

  private static EasyModelRenderProfile renderProfile(
      ResourceLocation profileId, boolean renderable) {
    EasyModelRenderProfile renderProfile = mock(EasyModelRenderProfile.class);
    when(renderProfile.id()).thenReturn(profileId);
    when(renderProfile.isRenderable()).thenReturn(renderable);
    return renderProfile;
  }

  @AfterEach
  void resetServices() {
    EasyModelServices.reset();
  }

  @Test
  void animationInfoFromClipsExposesSortedAuthoringAndRuntimeDetails() {
    ModelAnimationClip talk =
        new ModelAnimationClip("talk", 2.5f, true, 12.0f, Map.of("body", idleTrack()));
    ModelAnimationClip wave =
        new ModelAnimationClip("wave", 1.0f, false, 20.0f, Map.of("body", idleTrack()));
    Map<String, ModelAnimationClip> clips = new LinkedHashMap<>();
    clips.put("wave", wave);
    clips.put("talk", talk);

    List<EasyModelAnimationInfo> animations =
        EasyModelEntitiesClientApi.animationInfoFromClips(clips);

    assertEquals(
        List.of("talk", "wave"), animations.stream().map(EasyModelAnimationInfo::name).toList());
    EasyModelAnimationInfo talkInfo = animations.get(0);
    assertEquals(2.5f, talkInfo.durationSeconds(), 0.0001f);
    assertEquals(50.0f, talkInfo.durationTicks(), 0.0001f);
    assertEquals(12.0f, talkInfo.framesPerSecond(), 0.0001f);
    assertEquals(30, talkInfo.frameCount());
    assertEquals(1, talkInfo.keyframeCount());
    assertEquals(1, talkInfo.animatedBoneCount());
    assertTrue(talkInfo.loop());
    assertEquals(EasyModelAnimationType.CUSTOM, talkInfo.type());
    assertFalse(animations.get(1).loop());
  }

  @Test
  void animationTypeIsDerivedFromTheClipName() {
    ModelAnimationClip idle =
        new ModelAnimationClip("idle", 1.0f, true, Map.of("body", idleTrack()));

    EasyModelAnimationInfo info =
        EasyModelEntitiesClientApi.animationInfoFromClips(Map.of("idle", idle)).get(0);

    assertEquals(EasyModelAnimationType.STANDARD, info.type());
  }

  @Test
  void renderableProfileListsAreSortedAndFilterByPublicModelType() {
    EasyModelRenderProfile entityProfile =
        renderProfile(ResourceLocation.fromNamespaceAndPath("example", "entity/zebra"), true);
    EasyModelRenderProfile blockEntityProfile =
        renderProfile(ResourceLocation.fromNamespaceAndPath("example", "block_entity/altar"), true);
    EasyModelRenderProfile fallbackProfile =
        renderProfile(ResourceLocation.fromNamespaceAndPath("example", "entity/fallback"), false);
    EasyModelServices.setRenderProfileService(
        new EasyModelRenderProfileService() {
          @Override
          public Collection<EasyModelRenderProfile> getRenderProfiles() {
            return List.of(entityProfile, fallbackProfile, blockEntityProfile);
          }
        });

    assertEquals(
        List.of(blockEntityProfile.id(), entityProfile.id()),
        EasyModelEntitiesClientApi.listRenderableProfileIds());
    assertEquals(
        List.of(entityProfile.id()), EasyModelEntitiesClientApi.listRenderableEntityProfileIds());
    assertEquals(
        List.of(blockEntityProfile.id()),
        EasyModelEntitiesClientApi.listRenderableProfileIds(EasyModelProfileType.BLOCK_ENTITY));
  }

  @Test
  @DisplayName("A standard state passed as a string must not become a named clip")
  void animationNamesResolveToStandardStatesBeforeNamedClips() {
    assertEquals(EasyModelAnimation.WALK, EasyModelEntitiesClientApi.resolveAnimationName("walk"));
    assertEquals(EasyModelAnimation.WALK, EasyModelEntitiesClientApi.resolveAnimationName("WALK"));
    assertFalse(EasyModelEntitiesClientApi.resolveAnimationName("walk").isNamed());
  }

  @Test
  void unknownAnimationNamesBecomeNamedClips() {
    EasyModelAnimation animation = EasyModelEntitiesClientApi.resolveAnimationName("Wave Hello");

    assertTrue(animation.isNamed());
    assertEquals("wave hello", animation.name());
    assertEquals("named:wave hello", animation.serializedName());
  }

  @Test
  void explicitNamedPrefixKeepsItsClipName() {
    EasyModelAnimation animation = EasyModelEntitiesClientApi.resolveAnimationName("named:walk");

    assertTrue(animation.isNamed());
    assertEquals("walk", animation.name());
  }
}
