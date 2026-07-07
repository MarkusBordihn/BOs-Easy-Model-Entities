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

import de.markusbordihn.easymodelentities.blockentity.EasyModelHostBlockEntity;
import de.markusbordihn.easymodelentities.data.profile.EasyModelEntityProfile;
import de.markusbordihn.easymodelentities.data.profile.ModelBodyType;
import de.markusbordihn.easymodelentities.entity.EasyModelGroundEntity;
import de.markusbordihn.easymodelentities.entity.EasyModelHostEntity;
import de.markusbordihn.easymodelentities.entity.EasyModelStaticEntity;
import de.markusbordihn.easymodelentities.item.EasyModelEntitiesItems;
import de.markusbordihn.easymodelentities.profile.EasyModelProfileParser;
import de.markusbordihn.easymodelentities.profile.EasyModelProfileService;
import de.markusbordihn.easymodelentities.registry.EasyModelServices;
import de.markusbordihn.easymodelentities.registry.ModelBlockIds;
import de.markusbordihn.easymodelentities.runtime.EasyModelAnimationState;
import de.markusbordihn.easymodelentities.runtime.EasyModelRuntimeContract;
import java.io.StringReader;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public final class HostEntityGameTestCases {

  private static final ResourceLocation GROUND_PROFILE_ID =
      ResourceLocation.fromNamespaceAndPath("example", "ground");
  private static final ResourceLocation STATIC_PROFILE_ID =
      ResourceLocation.fromNamespaceAndPath("example", "static");
  private static final ResourceLocation INVALID_PROFILE_ID =
      ResourceLocation.fromNamespaceAndPath("example", "invalid");
  private static final ResourceLocation BLOCK_PROFILE_ID =
      ResourceLocation.fromNamespaceAndPath("example", "animated_block");
  private static final ResourceLocation ATTRIBUTES_PROFILE_ID =
      ResourceLocation.fromNamespaceAndPath("example", "attributes");

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
        || !GROUND_PROFILE_ID.equals(contract.renderProfileId())
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
            .createEntity(
                helper.getLevel(),
                ResourceLocation.fromNamespaceAndPath("example", "missing"),
                Vec3.ZERO)
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

    if (hostEntity.getDimensions(Pose.STANDING).width() != 0.7f
        || hostEntity.getDimensions(Pose.STANDING).height() != 0.9f) {
      helper.fail("Host entity dimensions were not refreshed from the active profile.");
      return;
    }

    helper.succeed();
  }

  public static void attributesAreAppliedFromProfile(GameTestHelper helper) {
    installProfiles();
    Optional<Entity> entity =
        EasyModelServices.entityFactory()
            .createEntity(helper.getLevel(), ATTRIBUTES_PROFILE_ID, Vec3.ZERO);
    if (entity.isEmpty() || !(entity.get() instanceof EasyModelHostEntity hostEntity)) {
      helper.fail("Could not create host entity for attribute test.");
      return;
    }

    if (hostEntity.getMaxHealth() != 30.0f
        || hostEntity.getHealth() != 30.0f
        || hostEntity.getAttributeValue(Attributes.FOLLOW_RANGE) != 32.0
        || hostEntity.getAttributeValue(Attributes.MOVEMENT_SPEED) != 0.25) {
      helper.fail("Profile attributes were not applied to the host entity.");
      return;
    }

    helper.succeed();
  }

  public static void blockEntityCanBePlacedAndInitialized(GameTestHelper helper) {
    installProfiles();
    Block block = BuiltInRegistries.BLOCK.get(ModelBlockIds.ANIMATED_BLOCK);
    if (block == Blocks.AIR) {
      helper.fail("Animated host block was not registered.");
      return;
    }

    BlockPos blockPos = new BlockPos(1, 1, 1);
    helper.setBlock(blockPos, block.defaultBlockState());
    if (!(helper.getBlockEntity(blockPos) instanceof EasyModelHostBlockEntity hostBlockEntity)) {
      helper.fail("Placed host block did not create an EasyModelHostBlockEntity.");
      return;
    }

    hostBlockEntity.setEasyModelProfileId(BLOCK_PROFILE_ID);
    EasyModelRuntimeContract contract = hostBlockEntity.getEasyModelRuntimeContract();
    if (!BLOCK_PROFILE_ID.equals(contract.profileId())
        || !BLOCK_PROFILE_ID.equals(contract.renderProfileId())
        || !"block-v1".equals(contract.version())
        || contract.bodyType() != ModelBodyType.STATIC
        || contract.width() != 1.0f
        || contract.height() != 1.0f
        || contract.eyeHeight() != 0.5f) {
      helper.fail("BlockEntity runtime contract was not initialized from the active profile.");
      return;
    }

    helper.succeed();
  }

  public static void entitySpawnItemSpawnsHostEntity(GameTestHelper helper) {
    installProfiles();
    Item item = EasyModelEntitiesItems.entitySpawnItem();
    if (item == null) {
      helper.fail("Entity spawn item was not registered.");
      return;
    }

    BlockPos floor = new BlockPos(1, 1, 1);
    helper.setBlock(floor, Blocks.STONE);
    BlockPos absoluteFloor = helper.absolutePos(floor);
    BlockHitResult hit =
        new BlockHitResult(
            Vec3.atCenterOf(absoluteFloor).add(0.0, 0.5, 0.0), Direction.UP, absoluteFloor, false);
    ItemStack stack = EasyModelEntitiesItems.forProfile(item, GROUND_PROFILE_ID);
    item.useOn(new UseOnContext(helper.getLevel(), null, InteractionHand.MAIN_HAND, stack, hit) {});

    BlockPos expected = absoluteFloor.above();
    List<EasyModelGroundEntity> spawned =
        helper
            .getLevel()
            .getEntitiesOfClass(EasyModelGroundEntity.class, new AABB(expected).inflate(1.0));
    if (spawned.isEmpty()) {
      helper.fail("Entity spawn item did not spawn a host entity.");
      return;
    }
    if (!GROUND_PROFILE_ID.equals(spawned.get(0).getEasyModelProfileId())) {
      helper.fail("Spawned host entity did not carry the item profile id.");
      return;
    }

    helper.succeed();
  }

  public static void blockSpawnItemPlacesHostBlock(GameTestHelper helper) {
    installProfiles();
    Item item = EasyModelEntitiesItems.blockSpawnItem();
    if (item == null) {
      helper.fail("Block spawn item was not registered.");
      return;
    }

    BlockPos floor = new BlockPos(1, 1, 1);
    helper.setBlock(floor, Blocks.STONE);
    BlockPos absoluteFloor = helper.absolutePos(floor);
    BlockHitResult hit =
        new BlockHitResult(
            Vec3.atCenterOf(absoluteFloor).add(0.0, 0.5, 0.0), Direction.UP, absoluteFloor, false);
    ItemStack stack = EasyModelEntitiesItems.forProfile(item, BLOCK_PROFILE_ID);
    item.useOn(new UseOnContext(helper.getLevel(), null, InteractionHand.MAIN_HAND, stack, hit) {});

    if (!(helper.getBlockEntity(floor.above())
        instanceof EasyModelHostBlockEntity hostBlockEntity)) {
      helper.fail("Block spawn item did not place an EasyModelHostBlockEntity.");
      return;
    }
    if (!BLOCK_PROFILE_ID.equals(hostBlockEntity.getEasyModelProfileId())) {
      helper.fail("Placed host block did not carry the item profile id.");
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
        BLOCK_PROFILE_ID,
        parse(BLOCK_PROFILE_ID, blockEntityProfileJson()),
        ATTRIBUTES_PROFILE_ID,
        parse(ATTRIBUTES_PROFILE_ID, attributesProfileJson()),
        INVALID_PROFILE_ID,
        parse(INVALID_PROFILE_ID, invalidProfileJson()));
  }

  private static EasyModelEntityProfile parse(ResourceLocation profileId, String json) {
    return EasyModelProfileParser.parse(profileId, new StringReader(json));
  }

  private static String groundProfileJson() {
    return """
        {
          "model_type": "entity",
          "preset_type": "quadruped_wandering",
          "version": "ground-v1",
          "entity": {
            "body_type": "quadruped"
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
          "model_type": "entity",
          "preset_type": "statue",
          "dimensions": {
            "width": 0.6,
            "height": 1.2,
            "eye_height": 0.8
          }
        }
        """;
  }

  private static String attributesProfileJson() {
    return """
        {
          "model_type": "entity",
          "preset_type": "quadruped_wandering",
          "version": "attributes-v1",
          "dimensions": {
            "width": 0.7,
            "height": 0.9,
            "eye_height": 0.55
          },
          "movement": {
            "speed": 0.25,
            "step_height": 0.6,
            "gravity": true
          },
          "attributes": {
            "max_health": 30.0,
            "follow_range": 32.0
          }
        }
        """;
  }

  private static String blockEntityProfileJson() {
    return """
        {
          "model_type": "block_entity",
          "preset_type": "animated",
          "version": "block-v1",
          "dimensions": {
            "width": 1.0,
            "height": 1.0,
            "eye_height": 0.5
          }
        }
        """;
  }

  private static String invalidProfileJson() {
    return """
        {
          "schema_version": "9.0.0",
          "model_type": "entity",
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
