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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.markusbordihn.easymodelentities.Constants;
import de.markusbordihn.easymodelentities.api.data.EasyModelAnimation;
import de.markusbordihn.easymodelentities.api.data.EasyModelAnimationLoop;
import de.markusbordihn.easymodelentities.api.data.EasyModelAnimationSetting;
import de.markusbordihn.easymodelentities.data.model.ModelAnimationClip;
import de.markusbordihn.easymodelentities.data.model.Vec3f;
import de.markusbordihn.easymodelentities.data.model.bake.BakedModel;
import de.markusbordihn.easymodelentities.data.model.bake.BakedModelPart;
import de.markusbordihn.easymodelentities.data.profile.EasyModelEntityProfile;
import de.markusbordihn.easymodelentities.data.profile.ModelAttributes;
import de.markusbordihn.easymodelentities.data.profile.ModelBehaviorMode;
import de.markusbordihn.easymodelentities.data.profile.ModelBehaviorSettings;
import de.markusbordihn.easymodelentities.data.profile.ModelBodyType;
import de.markusbordihn.easymodelentities.data.profile.ModelClientSettings;
import de.markusbordihn.easymodelentities.data.profile.ModelDimensions;
import de.markusbordihn.easymodelentities.data.profile.ModelEntitySettings;
import de.markusbordihn.easymodelentities.data.profile.ModelMovementSettings;
import de.markusbordihn.easymodelentities.data.profile.ModelMovementType;
import de.markusbordihn.easymodelentities.data.profile.ModelProfileStatus;
import de.markusbordihn.easymodelentities.data.profile.ModelType;
import de.markusbordihn.easymodelentities.data.render.EasyModelRenderState;
import de.markusbordihn.easymodelentities.data.renderprofile.EasyModelRenderProfile;
import de.markusbordihn.easymodelentities.data.renderprofile.ModelAnimationMode;
import de.markusbordihn.easymodelentities.data.renderprofile.ModelAnimationSettings;
import de.markusbordihn.easymodelentities.data.renderprofile.ModelRenderProfileStatus;
import de.markusbordihn.easymodelentities.data.renderprofile.ModelRenderSettings;
import de.markusbordihn.easymodelentities.profile.EasyModelProfileService;
import de.markusbordihn.easymodelentities.registry.EasyModelServices;
import de.markusbordihn.easymodelentities.registry.ModelEntityTypeIds;
import de.markusbordihn.easymodelentities.renderprofile.EasyModelRenderProfileService;
import de.markusbordihn.easymodelentities.runtime.EasyModelRuntimeContract;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.SharedConstants;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class EasyModelEntityRenderBackendTest {

  private static final EasyModelAnimationSetting AUTO = EasyModelAnimationSetting.AUTO;
  private static final EasyModelAnimationSetting IDLE =
      EasyModelAnimationSetting.of(EasyModelAnimation.IDLE);

  private static final ResourceLocation PROFILE_ID =
      ResourceLocation.fromNamespaceAndPath("example", "entity/lizard");

  private static EasyModelProfileService profileService(EasyModelEntityProfile... profiles) {
    return new EasyModelProfileService() {
      @Override
      public Optional<EasyModelEntityProfile> getProfile(ResourceLocation profileId) {
        for (EasyModelEntityProfile profile : profiles) {
          if (profile.id().equals(profileId)) {
            return Optional.of(profile);
          }
        }
        return Optional.empty();
      }
    };
  }

  private static EasyModelRenderProfileService renderProfileService(
      EasyModelRenderProfile... renderProfiles) {
    return new EasyModelRenderProfileService() {
      @Override
      public Optional<EasyModelRenderProfile> getRenderProfile(ResourceLocation renderProfileId) {
        for (EasyModelRenderProfile renderProfile : renderProfiles) {
          if (renderProfile.id().equals(renderProfileId)) {
            return Optional.of(renderProfile);
          }
        }
        return Optional.empty();
      }
    };
  }

  private static EasyModelEntityProfile entityProfile(
      ResourceLocation profileId, ModelBodyType bodyType, ModelProfileStatus status) {
    return new EasyModelEntityProfile(
        profileId,
        Constants.SCHEMA_VERSION,
        "server-v1",
        ModelType.ENTITY,
        new ModelEntitySettings(
            ModelEntityTypeIds.GROUND_ENTITY, ModelMovementType.GROUND, bodyType),
        null,
        new ModelClientSettings(profileId),
        new ModelDimensions(0.6f, 1.8f, 1.62f),
        new ModelMovementSettings(0.22f, 0.6f, true),
        new ModelBehaviorSettings(ModelBehaviorMode.IDLE_ONLY, true, false),
        new ModelAttributes(10.0f, 0.22f, 16.0f),
        status,
        List.of());
  }

  private static EasyModelRenderProfile renderProfile(
      ResourceLocation renderProfileId, ModelBodyType bodyType, ModelRenderProfileStatus status) {
    return new EasyModelRenderProfile(
        renderProfileId,
        Constants.SCHEMA_VERSION,
        "render-v1",
        bodyType,
        ResourceLocation.fromNamespaceAndPath("example", "model"),
        ResourceLocation.fromNamespaceAndPath("example", "texture"),
        new ModelRenderSettings(1.0f, 0.3f, 0.0f, 0.0f, Vec3f.ZERO),
        new ModelAnimationSettings(ModelAnimationMode.AUTOMATIC, 1.0f, 1.0f),
        status,
        List.of());
  }

  @BeforeAll
  static void bootstrapMinecraft() {
    SharedConstants.tryDetectVersion();
    Bootstrap.bootStrap();
  }

  @AfterEach
  void resetServices() {
    EasyModelServices.reset();
  }

  @Test
  void resolvesFromActiveServerProfile() {
    EasyModelServices.setProfileService(
        profileService(
            entityProfile(PROFILE_ID, ModelBodyType.QUADRUPED, ModelProfileStatus.ACTIVE)));
    EasyModelServices.setRenderProfileService(
        renderProfileService(
            renderProfile(PROFILE_ID, ModelBodyType.STATIC, ModelRenderProfileStatus.ACTIVE)));

    Optional<EasyModelRuntimeContract> contract =
        EasyModelEntityRenderBackend.resolveContract(PROFILE_ID, AUTO);

    assertTrue(contract.isPresent());
    assertEquals(ModelBodyType.QUADRUPED, contract.get().bodyType());
    assertEquals(PROFILE_ID, contract.get().renderProfileId());
  }

  @Test
  void fallsBackToRenderProfileWhenServerProfileMissing() {
    EasyModelServices.setRenderProfileService(
        renderProfileService(
            renderProfile(PROFILE_ID, ModelBodyType.WINGED, ModelRenderProfileStatus.ACTIVE)));

    Optional<EasyModelRuntimeContract> contract =
        EasyModelEntityRenderBackend.resolveContract(PROFILE_ID, IDLE);

    assertTrue(contract.isPresent());
    assertEquals(ModelBodyType.WINGED, contract.get().bodyType());
    assertEquals(PROFILE_ID, contract.get().renderProfileId());
    assertEquals(IDLE, contract.get().animation());
  }

  @Test
  void fallsBackToRenderProfileWhenServerProfileInactive() {
    EasyModelServices.setProfileService(
        profileService(
            entityProfile(
                PROFILE_ID, ModelBodyType.QUADRUPED, ModelProfileStatus.INVALID_DIMENSIONS)));
    EasyModelServices.setRenderProfileService(
        renderProfileService(
            renderProfile(PROFILE_ID, ModelBodyType.WINGED, ModelRenderProfileStatus.ACTIVE)));

    Optional<EasyModelRuntimeContract> contract =
        EasyModelEntityRenderBackend.resolveContract(PROFILE_ID, AUTO);

    assertTrue(contract.isPresent());
    assertEquals(ModelBodyType.WINGED, contract.get().bodyType());
  }

  @Test
  @DisplayName("A render profile with a missing texture still resolves to its own body type")
  void fallsBackToRenderProfileWithMissingTexture() {
    EasyModelServices.setRenderProfileService(
        renderProfileService(
            renderProfile(
                PROFILE_ID, ModelBodyType.WINGED, ModelRenderProfileStatus.MISSING_TEXTURE)));

    Optional<EasyModelRuntimeContract> contract =
        EasyModelEntityRenderBackend.resolveContract(PROFILE_ID, AUTO);

    assertTrue(contract.isPresent());
    assertEquals(ModelBodyType.WINGED, contract.get().bodyType());
  }

  @Test
  void emptyWhenNeitherProfileAvailable() {
    assertTrue(EasyModelEntityRenderBackend.resolveContract(PROFILE_ID, AUTO).isEmpty());
  }

  @Test
  @DisplayName("Diving or surfacing never counts as airborne, so swim wins over fly")
  void verticalMotionInWaterIsNotAirborne() {
    LivingEntity diving = livingEntity();
    when(diving.isInWater()).thenReturn(true);
    when(diving.getDeltaMovement()).thenReturn(new Vec3(0.0, 0.8, 0.0));
    LivingEntity falling = livingEntity();
    when(falling.getDeltaMovement()).thenReturn(new Vec3(0.0, 0.8, 0.0));

    assertEquals(0.0f, EasyModelEntityRenderBackend.airborneAmount(diving));
    assertTrue(EasyModelEntityRenderBackend.airborneAmount(falling) > 0.5f);
  }

  @Test
  @DisplayName("A dying entity plays death over a played clip unless only explicit clips play")
  void deathOverridesExplicitClipsExceptWithModeNone() {
    LivingEntity dying = livingEntity();
    when(dying.isDeadOrDying()).thenReturn(true);
    EasyModelRenderState automatic =
        renderState(ModelBodyType.BIPED, ModelAnimationMode.AUTOMATIC, "idle", "death");
    EasyModelRenderState explicitOnly =
        renderState(ModelBodyType.BIPED, ModelAnimationMode.NONE, "idle", "death");
    EasyModelRenderState withoutDeathClip =
        renderState(ModelBodyType.BIPED, ModelAnimationMode.AUTOMATIC, "idle");

    assertTrue(EasyModelEntityRenderBackend.dying(dying, automatic));
    assertFalse(EasyModelEntityRenderBackend.dying(dying, explicitOnly));
    assertFalse(EasyModelEntityRenderBackend.dying(dying, withoutDeathClip));
    assertEquals(
        new EasyModelAnimationSetting(EasyModelAnimation.DEATH, EasyModelAnimationLoop.ONCE),
        EasyModelEntityRenderBackend.stateSetting(
            dying,
            automatic,
            EasyModelAnimationSetting.of(EasyModelAnimation.named("wave")),
            0.0f,
            true));
  }

  @Test
  @DisplayName("A hurt entity plays hurt once, but never over a played or set clip")
  void hurtClipReplacesOnlyAutomaticAnimation() {
    LivingEntity hurt = livingEntity();
    hurt.hurtTime = 5;
    EasyModelRenderState renderState =
        renderState(ModelBodyType.BIPED, ModelAnimationMode.AUTOMATIC, "idle", "hurt");
    EasyModelAnimationSetting wave = EasyModelAnimationSetting.of(EasyModelAnimation.named("wave"));

    assertEquals(
        new EasyModelAnimationSetting(EasyModelAnimation.HURT, EasyModelAnimationLoop.ONCE),
        EasyModelEntityRenderBackend.stateSetting(hurt, renderState, AUTO, 0.0f, false));
    assertEquals(
        wave, EasyModelEntityRenderBackend.stateSetting(hurt, renderState, wave, 0.0f, false));
    assertEquals(
        AUTO,
        EasyModelEntityRenderBackend.stateSetting(
            hurt,
            renderState(ModelBodyType.BIPED, ModelAnimationMode.NONE, "idle", "hurt"),
            AUTO,
            0.0f,
            false));
  }

  @Test
  @DisplayName("A riding entity sits, but an attack swing takes over")
  void ridingPlaysSitUntilAttacking() {
    LivingEntity riding = livingEntity();
    when(riding.isPassenger()).thenReturn(true);
    EasyModelRenderState renderState =
        renderState(ModelBodyType.BIPED, ModelAnimationMode.AUTOMATIC, "idle", "sit");

    assertEquals(
        EasyModelAnimationSetting.of(EasyModelAnimation.SIT),
        EasyModelEntityRenderBackend.stateSetting(riding, renderState, AUTO, 0.0f, false));
    assertEquals(
        AUTO, EasyModelEntityRenderBackend.stateSetting(riding, renderState, AUTO, 0.5f, false));
  }

  @Test
  @DisplayName("Only an aquatic model without a walk clip flops on land")
  void strandedNeedsAquaticModelWithoutWalkClipOnLand() {
    LivingEntity entity = livingEntity();
    when(entity.onGround()).thenReturn(true);
    EasyModelRenderState fish =
        renderState(ModelBodyType.AQUATIC, ModelAnimationMode.AUTOMATIC, "idle", "swim");
    EasyModelRenderState walkingFish =
        renderState(ModelBodyType.AQUATIC, ModelAnimationMode.AUTOMATIC, "idle", "walk");
    EasyModelRenderState biped =
        renderState(ModelBodyType.BIPED, ModelAnimationMode.AUTOMATIC, "idle");

    assertTrue(EasyModelEntityRenderBackend.stranded(entity, fish, false));
    assertFalse(EasyModelEntityRenderBackend.stranded(entity, fish, true));
    assertFalse(EasyModelEntityRenderBackend.stranded(entity, walkingFish, false));
    assertFalse(EasyModelEntityRenderBackend.stranded(entity, biped, false));
  }

  @Test
  @DisplayName("An airborne aquatic model keeps swimming instead of flopping")
  void strandedNeedsGroundContact() {
    EasyModelRenderState fish =
        renderState(ModelBodyType.AQUATIC, ModelAnimationMode.AUTOMATIC, "idle", "swim");

    assertFalse(EasyModelEntityRenderBackend.stranded(livingEntity(), fish, false));
  }

  private static LivingEntity livingEntity() {
    LivingEntity livingEntity = mock(LivingEntity.class);
    when(livingEntity.getDeltaMovement()).thenReturn(Vec3.ZERO);
    return livingEntity;
  }

  private static EasyModelRenderState renderState(
      ModelBodyType bodyType, ModelAnimationMode animationMode, String... clipNames) {
    Map<String, ModelAnimationClip> clips = new HashMap<>();
    for (String clipName : clipNames) {
      clips.put(clipName, new ModelAnimationClip(clipName, 1.0f, false, Map.of()));
    }
    BakedModel bakedModel =
        new BakedModel(
            ResourceLocation.fromNamespaceAndPath("example", "clips"),
            64,
            64,
            List.of(new BakedModelPart("body", Vec3f.ZERO, Vec3f.ZERO, List.of(), List.of())),
            Map.of(),
            false,
            clips,
            null);

    return new EasyModelRenderState(
        bakedModel,
        ResourceLocation.fromNamespaceAndPath("example", "textures/entity/clips.png"),
        1.0f,
        0.3f,
        bodyType,
        new ModelAnimationSettings(animationMode, 1.0f, 1.0f),
        false,
        false,
        List.of());
  }
}
