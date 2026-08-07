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

package de.markusbordihn.easymodelentities.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import de.markusbordihn.easymodelentities.api.data.EasyModelBodyType;
import de.markusbordihn.easymodelentities.api.data.EasyModelProfileInfo;
import de.markusbordihn.easymodelentities.api.data.EasyModelProfileType;
import de.markusbordihn.easymodelentities.data.profile.EasyModelEntityProfile;
import de.markusbordihn.easymodelentities.data.profile.ModelAttributes;
import de.markusbordihn.easymodelentities.data.profile.ModelBehaviorMode;
import de.markusbordihn.easymodelentities.data.profile.ModelBehaviorSettings;
import de.markusbordihn.easymodelentities.data.profile.ModelClientSettings;
import de.markusbordihn.easymodelentities.data.profile.ModelDimensions;
import de.markusbordihn.easymodelentities.data.profile.ModelEntitySettings;
import de.markusbordihn.easymodelentities.data.profile.ModelMovementSettings;
import de.markusbordihn.easymodelentities.data.profile.ModelMovementType;
import de.markusbordihn.easymodelentities.data.profile.ModelProfileStatus;
import de.markusbordihn.easymodelentities.data.profile.ModelType;
import de.markusbordihn.easymodelentities.entity.EasyModelEntityFactory;
import de.markusbordihn.easymodelentities.entity.EasyModelHostEntity;
import de.markusbordihn.easymodelentities.profile.EasyModelProfileService;
import de.markusbordihn.easymodelentities.registry.EasyModelServices;
import de.markusbordihn.easymodelentities.registry.ModelEntityTypeIds;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.SharedConstants;
import net.minecraft.resources.Identifier;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class EasyModelEntitiesApiTest {

  private static final Identifier ACTIVE_PROFILE_ID =
      Identifier.fromNamespaceAndPath("example", "fox");
  private static final Identifier INVALID_PROFILE_ID =
      Identifier.fromNamespaceAndPath("example", "invalid");
  private static final Identifier MISSING_PROFILE_ID =
      Identifier.fromNamespaceAndPath("example", "missing");

  @BeforeAll
  static void bootstrapMinecraft() {
    SharedConstants.tryDetectVersion();
    Bootstrap.bootStrap();
  }

  private static EasyModelProfileService profileService(EasyModelEntityProfile... profiles) {
    Map<Identifier, EasyModelEntityProfile> profilesById =
        java.util.Arrays.stream(profiles)
            .collect(
                java.util.stream.Collectors.toMap(EasyModelEntityProfile::id, profile -> profile));
    return new EasyModelProfileService() {
      @Override
      public Optional<EasyModelEntityProfile> getProfile(Identifier profileId) {
        return Optional.ofNullable(profilesById.get(profileId));
      }

      @Override
      public boolean hasProfile(Identifier profileId) {
        return profilesById.containsKey(profileId);
      }

      @Override
      public Collection<EasyModelEntityProfile> getProfiles() {
        return profilesById.values();
      }
    };
  }

  private static EasyModelEntityFactory activeOnlyFactory(Entity entity) {
    return new EasyModelEntityFactory() {
      @Override
      public Optional<Entity> createEntity(Level level, Identifier profileId, Vec3 position) {
        return EasyModelServices.profileService()
            .getProfile(profileId)
            .filter(EasyModelEntityProfile::isActive)
            .map(profile -> entity);
      }
    };
  }

  private static EasyModelEntityProfile profile(Identifier profileId, ModelProfileStatus status) {
    return new EasyModelEntityProfile(
        profileId,
        "0.1.0",
        "server-v1",
        ModelType.ENTITY,
        new ModelEntitySettings(
            ModelEntityTypeIds.GROUND_ENTITY,
            ModelMovementType.GROUND,
            de.markusbordihn.easymodelentities.data.profile.ModelBodyType.QUADRUPED),
        null,
        new ModelClientSettings(profileId),
        new ModelDimensions(0.6f, 0.8f, 0.5f),
        new ModelMovementSettings(0.22f, 0.6f, true),
        new ModelBehaviorSettings(ModelBehaviorMode.IDLE_ONLY, true, false),
        new ModelAttributes(10.0f, 0.22f, 16.0f),
        status,
        List.of());
  }

  @AfterEach
  void resetServices() {
    EasyModelServices.reset();
  }

  @Test
  void hasProfileReturnsTrueForLoadedValidAndInvalidProfiles() {
    EasyModelServices.setProfileService(
        profileService(
            profile(ACTIVE_PROFILE_ID, ModelProfileStatus.ACTIVE),
            profile(INVALID_PROFILE_ID, ModelProfileStatus.DISABLED)));

    assertTrue(EasyModelEntitiesApi.hasProfile(ACTIVE_PROFILE_ID));
    assertTrue(EasyModelEntitiesApi.hasProfile(INVALID_PROFILE_ID));
    assertFalse(EasyModelEntitiesApi.hasProfile(MISSING_PROFILE_ID));
  }

  @Test
  void getProfileInfoReturnsStableProfileData() {
    EasyModelEntityProfile profile = profile(ACTIVE_PROFILE_ID, ModelProfileStatus.ACTIVE);
    EasyModelServices.setProfileService(profileService(profile));

    EasyModelProfileInfo profileInfo =
        EasyModelEntitiesApi.getProfileInfo(ACTIVE_PROFILE_ID).orElseThrow();

    assertEquals(ACTIVE_PROFILE_ID, profileInfo.id());
    assertEquals(EasyModelProfileType.ENTITY, profileInfo.modelType());
    assertEquals(EasyModelBodyType.QUADRUPED, profileInfo.bodyType());
    assertEquals(0.6f, profileInfo.dimensions().width());
    assertEquals(0.8f, profileInfo.dimensions().height());
    assertEquals(0.5f, profileInfo.standingEyeHeight());
  }

  @Test
  void listProfileInfosReturnsOnlyActiveProfilesFilteredByBodyType() {
    EasyModelServices.setProfileService(
        profileService(
            profile(ACTIVE_PROFILE_ID, ModelProfileStatus.ACTIVE),
            profile(INVALID_PROFILE_ID, ModelProfileStatus.DISABLED)));

    assertEquals(List.of(ACTIVE_PROFILE_ID), EasyModelEntitiesApi.listProfileIds());
    assertEquals(1, EasyModelEntitiesApi.listProfileInfos().size());
    assertEquals(ACTIVE_PROFILE_ID, EasyModelEntitiesApi.listProfileInfos().get(0).id());
    assertEquals(1, EasyModelEntitiesApi.listProfileInfos(EasyModelBodyType.QUADRUPED).size());
    assertTrue(EasyModelEntitiesApi.listProfileInfos(EasyModelBodyType.BIPED).isEmpty());
  }

  @Test
  void createEntityDelegatesToRegisteredFactory() {
    Entity entity = mock(Entity.class);
    Level level = mock(Level.class);
    Vec3 position = new Vec3(1.0, 2.0, 3.0);
    EasyModelServices.setEntityFactory(
        new EasyModelEntityFactory() {
          @Override
          public Optional<Entity> createEntity(
              Level factoryLevel, Identifier profileId, Vec3 factoryPosition) {
            assertSame(level, factoryLevel);
            assertEquals(ACTIVE_PROFILE_ID, profileId);
            assertSame(position, factoryPosition);
            return Optional.of(entity);
          }
        });

    assertSame(
        entity,
        EasyModelEntitiesApi.createEntity(level, ACTIVE_PROFILE_ID, position).orElseThrow());
  }

  @Test
  void createEntityReturnsEmptyForMissingAndInactiveProfiles() {
    Level level = mock(Level.class);
    Vec3 position = new Vec3(1.0, 2.0, 3.0);
    EasyModelServices.setProfileService(
        profileService(profile(INVALID_PROFILE_ID, ModelProfileStatus.DISABLED)));
    EasyModelServices.setEntityFactory(activeOnlyFactory(mock(Entity.class)));

    assertTrue(EasyModelEntitiesApi.createEntity(level, MISSING_PROFILE_ID, position).isEmpty());
    assertTrue(EasyModelEntitiesApi.createEntity(level, INVALID_PROFILE_ID, position).isEmpty());
  }

  @Test
  void getProfileIdUsesHostEntityBeforeRenderable() {
    EasyModelHostEntity hostEntity = mock(EasyModelHostEntity.class);
    when(hostEntity.getEasyModelProfileId()).thenReturn(ACTIVE_PROFILE_ID);

    assertEquals(ACTIVE_PROFILE_ID, EasyModelEntitiesApi.getProfileId(hostEntity).orElseThrow());
  }

  @Test
  void getProfileIdUsesRenderableEntities() {
    Entity entity = mock(Entity.class, withSettings().extraInterfaces(EasyModelRenderable.class));
    EasyModelRenderable renderable = (EasyModelRenderable) entity;
    when(renderable.getEasyModelProfileId()).thenReturn(ACTIVE_PROFILE_ID);

    assertEquals(ACTIVE_PROFILE_ID, EasyModelEntitiesApi.getProfileId(entity).orElseThrow());
  }
}
