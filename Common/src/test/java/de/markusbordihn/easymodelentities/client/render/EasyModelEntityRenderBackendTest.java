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
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.markusbordihn.easymodelentities.Constants;
import de.markusbordihn.easymodelentities.data.model.Vec3f;
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
import de.markusbordihn.easymodelentities.data.renderprofile.EasyModelRenderProfile;
import de.markusbordihn.easymodelentities.data.renderprofile.ModelAnimationMode;
import de.markusbordihn.easymodelentities.data.renderprofile.ModelAnimationSettings;
import de.markusbordihn.easymodelentities.data.renderprofile.ModelRenderProfileStatus;
import de.markusbordihn.easymodelentities.data.renderprofile.ModelRenderSettings;
import de.markusbordihn.easymodelentities.profile.EasyModelProfileService;
import de.markusbordihn.easymodelentities.registry.EasyModelServices;
import de.markusbordihn.easymodelentities.registry.ModelEntityTypeIds;
import de.markusbordihn.easymodelentities.renderprofile.EasyModelRenderProfileService;
import de.markusbordihn.easymodelentities.runtime.EasyModelAnimationState;
import de.markusbordihn.easymodelentities.runtime.EasyModelRuntimeContract;
import java.util.List;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class EasyModelEntityRenderBackendTest {

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
        EasyModelEntityRenderBackend.resolveContract(PROFILE_ID, EasyModelAnimationState.AUTO);

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
        EasyModelEntityRenderBackend.resolveContract(PROFILE_ID, EasyModelAnimationState.IDLE);

    assertTrue(contract.isPresent());
    assertEquals(ModelBodyType.WINGED, contract.get().bodyType());
    assertEquals(PROFILE_ID, contract.get().renderProfileId());
    assertEquals(EasyModelAnimationState.IDLE, contract.get().animationState());
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
        EasyModelEntityRenderBackend.resolveContract(PROFILE_ID, EasyModelAnimationState.AUTO);

    assertTrue(contract.isPresent());
    assertEquals(ModelBodyType.WINGED, contract.get().bodyType());
  }

  @Test
  void emptyWhenNeitherProfileAvailable() {
    assertTrue(
        EasyModelEntityRenderBackend.resolveContract(PROFILE_ID, EasyModelAnimationState.AUTO)
            .isEmpty());
  }
}
