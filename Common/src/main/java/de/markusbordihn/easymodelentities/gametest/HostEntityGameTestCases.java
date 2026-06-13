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

package de.markusbordihn.easymodelentities.gametest;

import de.markusbordihn.easymodelentities.entity.EasyModelGroundEntity;
import de.markusbordihn.easymodelentities.entity.EasyModelHostEntity;
import de.markusbordihn.easymodelentities.entity.EasyModelStaticEntity;
import de.markusbordihn.easymodelentities.profile.EasyModelEntityProfile;
import de.markusbordihn.easymodelentities.profile.EasyModelProfileParser;
import de.markusbordihn.easymodelentities.profile.EasyModelProfileService;
import de.markusbordihn.easymodelentities.profile.ModelBodyType;
import de.markusbordihn.easymodelentities.registry.EasyModelServices;
import de.markusbordihn.easymodelentities.runtime.EasyModelAnimationState;
import de.markusbordihn.easymodelentities.runtime.EasyModelRuntimeContract;
import java.io.StringReader;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.phys.Vec3;

public final class HostEntityGameTestCases {

  private static final ResourceLocation GROUND_PROFILE_ID =
      new ResourceLocation("example", "ground");
  private static final ResourceLocation STATIC_PROFILE_ID =
      new ResourceLocation("example", "static");
  private static final ResourceLocation INVALID_PROFILE_ID =
      new ResourceLocation("example", "invalid");

  private HostEntityGameTestCases() {}

  public static void groundEntityCanBeCreated(GameTestHelper helper) {
    installProfiles();
    Optional<Entity> entity =
        EasyModelServices.entityFactory()
            .createEntity(helper.getLevel(), GROUND_PROFILE_ID, new Vec3(1.0, 1.0, 1.0));
    if (entity.isEmpty() || !(entity.get() instanceof EasyModelGroundEntity groundEntity)) {
      helper.fail("Ground profile did not create an EasyModelGroundEntity.");
      return;
    }

    EasyModelRuntimeContract contract = groundEntity.getEasyModelRuntimeContract();
    if (!GROUND_PROFILE_ID.equals(contract.profileId())
        || !new ResourceLocation("example", "ground_render").equals(contract.renderProfileId())
        || !"ground-v1".equals(contract.version())
        || contract.bodyType() != ModelBodyType.QUADRUPED
        || contract.width() != 0.7f
        || contract.height() != 0.9f
        || contract.eyeHeight() != 0.55f) {
      helper.fail("Ground runtime contract was not initialized from the active profile.");
      return;
    }

    helper.succeed();
  }

  public static void staticEntityCanBeCreated(GameTestHelper helper) {
    installProfiles();
    Optional<Entity> entity =
        EasyModelServices.entityFactory()
            .createEntity(helper.getLevel(), STATIC_PROFILE_ID, new Vec3(1.0, 1.0, 1.0));
    if (entity.isEmpty() || !(entity.get() instanceof EasyModelStaticEntity staticEntity)) {
      helper.fail("Static profile did not create an EasyModelStaticEntity.");
      return;
    }

    if (staticEntity.isEasyModelRandomStrollEnabled()) {
      helper.fail("Static entity unexpectedly enabled random stroll.");
      return;
    }

    helper.succeed();
  }

  public static void missingAndInvalidProfilesReturnEmpty(GameTestHelper helper) {
    installProfiles();
    boolean missingProfileCreated =
        EasyModelServices.entityFactory()
            .createEntity(helper.getLevel(), new ResourceLocation("example", "missing"), Vec3.ZERO)
            .isPresent();
    boolean invalidProfileCreated =
        EasyModelServices.entityFactory()
            .createEntity(helper.getLevel(), INVALID_PROFILE_ID, Vec3.ZERO)
            .isPresent();
    if (missingProfileCreated || invalidProfileCreated) {
      helper.fail("Missing or invalid profiles must not create host entities.");
      return;
    }

    helper.succeed();
  }

  public static void nbtPreservesRuntimeContractData(GameTestHelper helper) {
    installProfiles();
    Optional<Entity> entity =
        EasyModelServices.entityFactory()
            .createEntity(helper.getLevel(), GROUND_PROFILE_ID, Vec3.ZERO);
    if (entity.isEmpty() || !(entity.get() instanceof EasyModelHostEntity hostEntity)) {
      helper.fail("Could not create host entity for NBT round trip.");
      return;
    }

    hostEntity.setEasyModelAnimationState(EasyModelAnimationState.RUN);
    CompoundTag compoundTag = new CompoundTag();
    hostEntity.addAdditionalSaveData(compoundTag);

    Optional<Entity> loadedEntity =
        EasyModelServices.entityFactory()
            .createEntity(helper.getLevel(), GROUND_PROFILE_ID, Vec3.ZERO);
    if (loadedEntity.isEmpty()
        || !(loadedEntity.get() instanceof EasyModelHostEntity loadedHostEntity)) {
      helper.fail("Could not create host entity for NBT load.");
      return;
    }

    loadedHostEntity.readAdditionalSaveData(compoundTag);
    EasyModelRuntimeContract contract = loadedHostEntity.getEasyModelRuntimeContract();
    if (!GROUND_PROFILE_ID.equals(contract.profileId())
        || contract.animationState() != EasyModelAnimationState.RUN
        || contract.bodyType() != ModelBodyType.QUADRUPED) {
      helper.fail("NBT load did not preserve the persisted runtime contract fields.");
      return;
    }

    helper.succeed();
  }

  public static void invalidNbtResourceLocationsFallBackSafely(GameTestHelper helper) {
    installProfiles();
    Optional<Entity> entity =
        EasyModelServices.entityFactory()
            .createEntity(helper.getLevel(), GROUND_PROFILE_ID, Vec3.ZERO);
    if (entity.isEmpty() || !(entity.get() instanceof EasyModelHostEntity hostEntity)) {
      helper.fail("Could not create host entity for invalid NBT load.");
      return;
    }

    CompoundTag compoundTag = new CompoundTag();
    compoundTag.putString("ProfileId", "bad id");
    compoundTag.putString("RenderProfileId", "also bad");
    compoundTag.putString("Version", "stale");
    compoundTag.putString("BodyType", "unknown");
    compoundTag.putString("AnimationState", "unknown");
    hostEntity.readAdditionalSaveData(compoundTag);

    EasyModelRuntimeContract contract = hostEntity.getEasyModelRuntimeContract();
    if (!EasyModelHostEntity.MISSING_PROFILE_ID.equals(contract.profileId())
        || contract.bodyType() != ModelBodyType.STATIC
        || contract.animationState() != EasyModelAnimationState.AUTO) {
      helper.fail("Invalid NBT ResourceLocation values did not fall back safely.");
      return;
    }

    helper.succeed();
  }

  public static void dimensionRefreshUsesProfileDimensions(GameTestHelper helper) {
    installProfiles();
    Optional<Entity> entity =
        EasyModelServices.entityFactory()
            .createEntity(helper.getLevel(), GROUND_PROFILE_ID, Vec3.ZERO);
    if (entity.isEmpty() || !(entity.get() instanceof EasyModelHostEntity hostEntity)) {
      helper.fail("Could not create host entity for dimension refresh test.");
      return;
    }

    if (hostEntity.getDimensions(Pose.STANDING).width != 0.7f
        || hostEntity.getDimensions(Pose.STANDING).height != 0.9f) {
      helper.fail("Host entity dimensions were not refreshed from the active profile.");
      return;
    }

    helper.succeed();
  }

  private static void installProfiles() {
    EasyModelServices.setProfileService(new StaticProfileService(activeProfiles()));
  }

  private static Map<ResourceLocation, EasyModelEntityProfile> activeProfiles() {
    return Map.of(
        GROUND_PROFILE_ID,
        parse(GROUND_PROFILE_ID, groundProfileJson()),
        STATIC_PROFILE_ID,
        parse(STATIC_PROFILE_ID, staticProfileJson()),
        INVALID_PROFILE_ID,
        parse(INVALID_PROFILE_ID, invalidProfileJson()));
  }

  private static EasyModelEntityProfile parse(ResourceLocation profileId, String json) {
    return EasyModelProfileParser.parse(profileId, new StringReader(json));
  }

  private static String groundProfileJson() {
    return """
        {
          "preset_type": "quadruped_wandering",
          "version": "ground-v1",
          "host": {
            "body_type": "quadruped"
          },
          "client": {
            "render_profile": "example:ground_render"
          },
          "dimensions": {
            "width": 0.7,
            "height": 0.9,
            "eye_height": 0.55
          },
          "movement": {
            "speed": 0.22,
            "step_height": 0.6,
            "gravity": true
          },
          "behavior": {
            "mode": "ambient",
            "look_at_players": true,
            "random_stroll": true
          }
        }
        """;
  }

  private static String staticProfileJson() {
    return """
        {
          "preset_type": "statue",
          "client": {
            "render_profile": "example:static_render"
          },
          "dimensions": {
            "width": 0.6,
            "height": 1.2,
            "eye_height": 0.8
          }
        }
        """;
  }

  private static String invalidProfileJson() {
    return """
        {
          "schema_version": "9.0.0",
          "preset_type": "quadruped_wandering"
        }
        """;
  }

  private record StaticProfileService(Map<ResourceLocation, EasyModelEntityProfile> profiles)
      implements EasyModelProfileService {

    @Override
    public Optional<EasyModelEntityProfile> getProfile(ResourceLocation profileId) {
      return Optional.ofNullable(this.profiles.get(profileId));
    }

    @Override
    public List<EasyModelEntityProfile> getProfiles() {
      return List.copyOf(this.profiles.values());
    }
  }
}
