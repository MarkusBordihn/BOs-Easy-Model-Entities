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

import static de.markusbordihn.easymodelentities.gametest.HostEntityGameTestCases.BLOCK_PROFILE_ID;
import static de.markusbordihn.easymodelentities.gametest.HostEntityGameTestCases.CRYSTAL_TEXTURE;
import static de.markusbordihn.easymodelentities.gametest.HostEntityGameTestCases.GROUND_PROFILE_ID;
import static de.markusbordihn.easymodelentities.gametest.HostEntityGameTestCases.SCREEN_TEXTURE;
import static de.markusbordihn.easymodelentities.gametest.HostEntityGameTestCases.commandSource;
import static de.markusbordihn.easymodelentities.gametest.HostEntityGameTestCases.installProfiles;
import static de.markusbordihn.easymodelentities.gametest.HostEntityGameTestCases.placeHostBlock;
import static de.markusbordihn.easymodelentities.gametest.HostEntityGameTestCases.runCommand;
import static de.markusbordihn.easymodelentities.gametest.HostEntityGameTestCases.spawnHostEntity;

import com.mojang.authlib.GameProfile;
import de.markusbordihn.easymodelentities.api.EasyModelBlockEntitiesApi;
import de.markusbordihn.easymodelentities.api.EasyModelEntitiesApi;
import de.markusbordihn.easymodelentities.api.data.EasyModelAnimation;
import de.markusbordihn.easymodelentities.api.data.EasyModelAnimationLoop;
import de.markusbordihn.easymodelentities.api.data.EasyModelAnimationSetting;
import de.markusbordihn.easymodelentities.api.data.EasyModelDisplaySettings;
import de.markusbordihn.easymodelentities.api.data.EasyModelTextureBlend;
import de.markusbordihn.easymodelentities.api.data.EasyModelTextureSetting;
import de.markusbordihn.easymodelentities.blockentity.EasyModelHostBlockEntity;
import de.markusbordihn.easymodelentities.entity.EasyModelHostEntity;
import de.markusbordihn.easymodelentities.runtime.EasyModelHostPersistence;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.IntSupplier;
import java.util.function.Supplier;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class CommandGameTestCases {

  private static final BlockPos HOST_BLOCK_POS = new BlockPos(1, 1, 1);
  private static final BlockPos EMPTY_BLOCK_POS = new BlockPos(0, 1, 0);

  private CommandGameTestCases() {}

  public static void profileCommandsReportLoadedProfiles(GameTestHelper helper) {
    installProfiles();
    int listedLines = runCommand(helper, "list_profiles");
    int validatedLines = runCommand(helper, "validate_profiles");
    int debugLines = runCommand(helper, "debug_profile " + GROUND_PROFILE_ID);
    GameTestHelpers.assertTrue(
        helper,
        "Profile commands must report the loaded test profiles: list "
            + listedLines
            + ", validate "
            + validatedLines
            + ", debug "
            + debugLines,
        listedLines > 0 && validatedLines > 0 && debugLines > 1);
  }

  public static void summonCommandSpawnsHostEntity(GameTestHelper helper) {
    installProfiles();
    Vec3 summonPosition = helper.absoluteVec(new Vec3(1.5, 1, 1.5));
    CommandSourceStack source = commandSource(helper).withPosition(summonPosition);
    int summoned = runCommand(helper, source, "summon " + GROUND_PROFILE_ID);
    List<EasyModelHostEntity> hostEntities =
        helper
            .getLevel()
            .getEntitiesOfClass(
                EasyModelHostEntity.class, new AABB(summonPosition, summonPosition).inflate(1));
    boolean spawnedWithProfile =
        hostEntities.stream()
            .anyMatch(hostEntity -> GROUND_PROFILE_ID.equals(hostEntity.getEasyModelProfileId()));
    hostEntities.forEach(EasyModelHostEntity::discard);
    GameTestHelpers.assertTrue(
        helper,
        "The summon command must spawn a host entity with the requested profile!",
        summoned == 1 && spawnedWithProfile);
  }

  public static void placeBlockCommandPlacesHostBlockEntity(GameTestHelper helper) {
    installProfiles();
    BlockPos absolutePos = helper.absolutePos(HOST_BLOCK_POS);
    int placed =
        runCommand(
            helper,
            "place_block "
                + BLOCK_PROFILE_ID
                + " "
                + absolutePos.getX()
                + " "
                + absolutePos.getY()
                + " "
                + absolutePos.getZ());
    boolean placedWithProfile =
        helper.getBlockEntity(HOST_BLOCK_POS) instanceof EasyModelHostBlockEntity hostBlockEntity
            && BLOCK_PROFILE_ID.equals(hostBlockEntity.getEasyModelProfileId());
    int rejected = runCommand(helper, "place_block " + GROUND_PROFILE_ID);
    GameTestHelpers.assertTrue(
        helper,
        "The place_block command must place a host block with the requested profile and reject an"
            + " entity profile: placed "
            + placed
            + ", rejected "
            + rejected,
        placed == 1 && placedWithProfile && rejected == 0);
  }

  public static void shoulderCommandSeatsHostEntityOnPlayer(GameTestHelper helper) {
    Optional<EasyModelHostEntity> spawnedEntity = spawnHostEntity(helper);
    if (spawnedEntity.isEmpty()) {
      helper.fail("Could not create host entity for the shoulder command.");
      return;
    }

    EasyModelHostEntity hostEntity = spawnedEntity.get();
    ServerLevel level = helper.getLevel();
    ServerPlayer player =
        new ServerPlayer(
            level.getServer(),
            level,
            new GameProfile(UUID.randomUUID(), "shoulder-test-player"),
            ClientInformation.createDefault());
    player.setOnGround(true);
    int seated =
        runCommand(
            helper,
            commandSource(helper).withEntity(player),
            "shoulder sit " + entityTarget(hostEntity) + " @s");
    boolean onShoulder =
        GROUND_PROFILE_ID
            .toString()
            .equals(
                player
                    .getShoulderEntityLeft()
                    .getString(EasyModelHostPersistence.PROFILE_ID_TAG));
    boolean removed = hostEntity.isRemoved();
    GameTestHelpers.assertTrue(
        helper,
        "The shoulder command must move a host entity onto the player's shoulder: seated "
            + seated
            + ", on shoulder "
            + onShoulder
            + ", removed "
            + removed,
        seated == 1 && onShoulder && removed);
  }

  public static void behaviorCommandFreezesAndRestoresHostEntity(GameTestHelper helper) {
    Optional<EasyModelHostEntity> spawnedEntity = spawnHostEntity(helper);
    if (spawnedEntity.isEmpty()) {
      helper.fail("Could not create host entity for the behavior command.");
      return;
    }

    EasyModelHostEntity hostEntity = spawnedEntity.get();
    String target = entityTarget(hostEntity);
    int frozenCount = runCommand(helper, "behavior set " + target + " frozen");
    boolean frozen = EasyModelEntitiesApi.isFrozen(hostEntity);
    int restoredCount = runCommand(helper, "behavior set " + target + " auto");
    boolean restored = !EasyModelEntitiesApi.isFrozen(hostEntity);
    hostEntity.discard();
    GameTestHelpers.assertTrue(
        helper,
        "The behavior command must freeze and restore a host entity!",
        frozenCount == 1 && frozen && restoredCount == 1 && restored);
  }

  public static void animationCommandsControlHostEntity(GameTestHelper helper) {
    Optional<EasyModelHostEntity> spawnedEntity = spawnHostEntity(helper);
    if (spawnedEntity.isEmpty()) {
      helper.fail("Could not create host entity for the animation command.");
      return;
    }

    EasyModelHostEntity hostEntity = spawnedEntity.get();
    String failure = animationCommandFailure(helper, entityTarget(hostEntity));
    EasyModelAnimationSetting animationSetting =
        EasyModelEntitiesApi.getAnimationSetting(hostEntity);
    hostEntity.discard();
    if (failure != null) {
      helper.fail(failure);
      return;
    }

    GameTestHelpers.assertTrue(
        helper,
        "The animation set command must store walk as a looping clip!",
        new EasyModelAnimationSetting(EasyModelAnimation.WALK, EasyModelAnimationLoop.LOOP)
            .equals(animationSetting));
  }

  public static void animationCommandsControlHostBlockEntity(GameTestHelper helper) {
    Optional<EasyModelHostBlockEntity> placedBlockEntity = placeHostBlock(helper);
    if (placedBlockEntity.isEmpty()) {
      helper.fail("Placed host block did not create an EasyModelHostBlockEntity.");
      return;
    }

    String failure = animationCommandFailure(helper, blockTarget(helper, HOST_BLOCK_POS));
    if (failure != null) {
      helper.fail(failure);
      return;
    }

    int missingBlockEntity =
        runCommand(helper, "animation restart " + blockTarget(helper, EMPTY_BLOCK_POS));
    GameTestHelpers.assertTrue(
        helper,
        "The animation set command must store walk on the block and skip an empty position!",
        new EasyModelAnimationSetting(EasyModelAnimation.WALK, EasyModelAnimationLoop.LOOP)
                .equals(EasyModelBlockEntitiesApi.getAnimationSetting(placedBlockEntity.get()))
            && missingBlockEntity == 0);
  }

  public static void textureCommandsControlHostEntity(GameTestHelper helper) {
    Optional<EasyModelHostEntity> spawnedEntity = spawnHostEntity(helper);
    if (spawnedEntity.isEmpty()) {
      helper.fail("Could not create host entity for the texture command.");
      return;
    }

    EasyModelHostEntity hostEntity = spawnedEntity.get();
    String failure =
        textureCommandFailure(
            helper,
            entityTarget(hostEntity),
            "screen",
            SCREEN_TEXTURE.toString(),
            () -> EasyModelEntitiesApi.getTextureSetting(hostEntity));
    hostEntity.discard();
    if (failure != null) {
      helper.fail(failure);
      return;
    }

    helper.succeed();
  }

  public static void textureCommandsControlHostBlockEntity(GameTestHelper helper) {
    Optional<EasyModelHostBlockEntity> placedBlockEntity = placeHostBlock(helper);
    if (placedBlockEntity.isEmpty()) {
      helper.fail("Placed host block did not create an EasyModelHostBlockEntity.");
      return;
    }

    String failure =
        textureCommandFailure(
            helper,
            blockTarget(helper, HOST_BLOCK_POS),
            "crystal",
            CRYSTAL_TEXTURE.toString(),
            () -> EasyModelBlockEntitiesApi.getTextureSetting(placedBlockEntity.get()));
    if (failure != null) {
      helper.fail(failure);
      return;
    }

    helper.succeed();
  }

  public static void displayCommandsControlHostEntity(GameTestHelper helper) {
    Optional<EasyModelHostEntity> spawnedEntity = spawnHostEntity(helper);
    if (spawnedEntity.isEmpty()) {
      helper.fail("Could not create host entity for the display command.");
      return;
    }

    EasyModelHostEntity hostEntity = spawnedEntity.get();
    String failure =
        displayCommandFailure(
            helper,
            entityTarget(hostEntity),
            () -> EasyModelEntitiesApi.getOpacity(hostEntity),
            () -> EasyModelEntitiesApi.getLightLevel(hostEntity));
    hostEntity.discard();
    if (failure != null) {
      helper.fail(failure);
      return;
    }

    helper.succeed();
  }

  public static void displayCommandsControlHostBlockEntity(GameTestHelper helper) {
    Optional<EasyModelHostBlockEntity> placedBlockEntity = placeHostBlock(helper);
    if (placedBlockEntity.isEmpty()) {
      helper.fail("Placed host block did not create an EasyModelHostBlockEntity.");
      return;
    }

    EasyModelHostBlockEntity hostBlockEntity = placedBlockEntity.get();
    String failure =
        displayCommandFailure(
            helper,
            blockTarget(helper, HOST_BLOCK_POS),
            () -> EasyModelBlockEntitiesApi.getOpacity(hostBlockEntity),
            () -> EasyModelBlockEntitiesApi.getLightLevel(hostBlockEntity));
    if (failure != null) {
      helper.fail(failure);
      return;
    }

    GameTestHelpers.assertTrue(
        helper,
        "The display command must reject a position without a block entity!",
        runCommand(helper, "display opacity set " + blockTarget(helper, EMPTY_BLOCK_POS) + " 40")
            == 0);
  }

  private static String animationCommandFailure(GameTestHelper helper, String target) {
    if (runCommand(helper, "animation play " + target + " walk once") != 1) {
      return "animation play failed for " + target;
    }
    if (runCommand(helper, "animation play " + target + " random \"walk,idle\"") != 1) {
      return "animation play random failed for " + target;
    }
    if (runCommand(helper, "animation play " + target + " sequence \"walk,attack\" fallback idle")
        != 1) {
      return "animation play sequence failed for " + target;
    }
    if (runCommand(helper, "animation play " + target + " sequence \"auto\"") != 0) {
      return "animation play sequence accepted a sequence without a playable clip";
    }
    if (runCommand(helper, "animation stop " + target + " after_current 4") != 1) {
      return "animation stop failed for " + target;
    }
    if (runCommand(helper, "animation restart " + target) != 1) {
      return "animation restart failed for " + target;
    }
    if (runCommand(helper, "animation set " + target + " walk loop") != 1) {
      return "animation set failed for " + target;
    }

    return null;
  }

  private static String textureCommandFailure(
      GameTestHelper helper,
      String target,
      String slot,
      String texture,
      Supplier<EasyModelTextureSetting> textureSetting) {
    if (runCommand(helper, "texture set " + target + " slot " + slot + " " + texture) != 1
        || !textureSetting.get().texture(slot).map(Object::toString).equals(Optional.of(texture))) {
      return "texture set did not store " + texture + " in slot " + slot;
    }
    if (runCommand(helper, "texture blend " + target + " slot " + slot + " translucent") != 1
        || textureSetting.get().blend(slot) != EasyModelTextureBlend.TRANSLUCENT) {
      return "texture blend did not switch slot " + slot + " to translucent";
    }
    if (runCommand(helper, "texture get " + target) != 1) {
      return "texture get failed for " + target;
    }
    if (runCommand(helper, "texture clear " + target + " slot " + slot) != 1
        || textureSetting.get().texture(slot).isPresent()) {
      return "texture clear did not remove slot " + slot;
    }
    if (runCommand(
                helper,
                "texture set "
                    + target
                    + " "
                    + EasyModelTextureSetting.DEFAULT_SLOT
                    + " "
                    + texture
                    + " emissive")
            != 1
        || textureSetting.get().blend(EasyModelTextureSetting.DEFAULT_SLOT)
            != EasyModelTextureBlend.EMISSIVE) {
      return "texture set default did not store an emissive default texture";
    }
    if (runCommand(helper, "texture clear " + target) != 1 || !textureSetting.get().isEmpty()) {
      return "texture clear did not remove every override";
    }

    return null;
  }

  private static String displayCommandFailure(
      GameTestHelper helper,
      String target,
      Supplier<Float> opacity,
      IntSupplier lightLevel) {
    if (runCommand(helper, "display opacity set " + target + " 40") != 1
        || Math.abs(opacity.get() - 0.4f) > 0.001f
        || runCommand(helper, "display opacity get " + target) != 40) {
      return "display opacity set did not store 40% for " + target;
    }
    if (runCommand(helper, "display opacity clear " + target) != 1
        || opacity.get() != EasyModelDisplaySettings.NO_OPACITY
        || runCommand(helper, "display opacity get " + target) != 100) {
      return "display opacity clear did not remove the override for " + target;
    }
    if (runCommand(helper, "display light set " + target + " 12") != 1
        || lightLevel.getAsInt() != 12
        || runCommand(helper, "display light get " + target) != 12) {
      return "display light set did not store level 12 for " + target;
    }
    if (runCommand(helper, "display light clear " + target) != 1
        || lightLevel.getAsInt() != EasyModelDisplaySettings.NO_LIGHT_LEVEL
        || runCommand(helper, "display light get " + target) != 0) {
      return "display light clear did not remove the override for " + target;
    }

    return null;
  }

  private static String entityTarget(EasyModelHostEntity hostEntity) {
    return "entity " + hostEntity.getStringUUID();
  }

  private static String blockTarget(GameTestHelper helper, BlockPos relativePos) {
    BlockPos blockPos = helper.absolutePos(relativePos);
    return "block " + blockPos.getX() + " " + blockPos.getY() + " " + blockPos.getZ();
  }
}
