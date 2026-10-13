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

import com.mojang.authlib.GameProfile;
import de.markusbordihn.easymodelentities.Constants;
import de.markusbordihn.easymodelentities.api.EasyModelBlockEntitiesApi;
import de.markusbordihn.easymodelentities.api.EasyModelEntitiesApi;
import de.markusbordihn.easymodelentities.api.data.EasyModelAnimation;
import de.markusbordihn.easymodelentities.api.data.EasyModelAnimationLoop;
import de.markusbordihn.easymodelentities.api.data.EasyModelAnimationSetting;
import de.markusbordihn.easymodelentities.api.data.EasyModelDisplaySettings;
import de.markusbordihn.easymodelentities.api.data.EasyModelTextureSetting;
import de.markusbordihn.easymodelentities.api.data.client.EasyModelAnimationPlayback;
import de.markusbordihn.easymodelentities.api.data.client.EasyModelAnimationSequence;
import de.markusbordihn.easymodelentities.api.data.client.EasyModelAnimationTransition;
import de.markusbordihn.easymodelentities.block.EasyModelHostBlock;
import de.markusbordihn.easymodelentities.blockentity.EasyModelHostBlockEntity;
import de.markusbordihn.easymodelentities.data.diagnostics.ModelResourceRejection;
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
import de.markusbordihn.easymodelentities.registry.ModelResourcePaths;
import de.markusbordihn.easymodelentities.runtime.EasyModelHostPersistence;
import de.markusbordihn.easymodelentities.runtime.EasyModelRuntimeContract;
import java.io.StringReader;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.LevelBasedPermissionSet;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.Marker;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

public final class HostEntityGameTestCases {

  static final String TEST_NAMESPACE = Constants.MOD_ID + "_gametest";
  static final Identifier GROUND_PROFILE_ID =
      ModelResourcePaths.identifier(TEST_NAMESPACE, "ground");
  private static final Identifier STATIC_PROFILE_ID =
      ModelResourcePaths.identifier(TEST_NAMESPACE, "static");
  private static final Identifier INVALID_PROFILE_ID =
      ModelResourcePaths.identifier(TEST_NAMESPACE, "invalid");
  static final Identifier BLOCK_PROFILE_ID =
      ModelResourcePaths.identifier(TEST_NAMESPACE, "animated_block");
  static final Identifier SCREEN_TEXTURE =
      ModelResourcePaths.identifier(TEST_NAMESPACE, "textures/entity/screen_sad.png");
  static final Identifier CRYSTAL_TEXTURE =
      ModelResourcePaths.identifier(TEST_NAMESPACE, "textures/block/shrine_glow.png");
  private static final Identifier ATTRIBUTES_PROFILE_ID =
      ModelResourcePaths.identifier(TEST_NAMESPACE, "attributes");

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
                ModelResourcePaths.identifier(TEST_NAMESPACE, "missing"),
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

  public static void frozenBehaviorStopsHostEntityMovement(GameTestHelper helper) {
    installProfiles();
    Optional<Entity> entity =
        EasyModelServices.entityFactory()
            .createEntity(helper.getLevel(), GROUND_PROFILE_ID, new Vec3(1.0, 1.0, 1.0));
    if (entity.isEmpty() || !(entity.get() instanceof EasyModelGroundEntity groundEntity)) {
      helper.fail("Ground profile did not create an EasyModelGroundEntity.");
      return;
    }

    groundEntity.setDeltaMovement(new Vec3(0.5, 0.0, 0.5));
    groundEntity.setZza(1.0f);
    if (!EasyModelEntitiesApi.setFrozen(groundEntity, true)
        || !EasyModelEntitiesApi.isFrozen(groundEntity)
        || groundEntity.zza != 0.0f
        || !groundEntity.getDeltaMovement().equals(Vec3.ZERO)) {
      helper.fail("Frozen behavior did not stop the host entity movement.");
      return;
    }

    if (!EasyModelEntitiesApi.setFrozen(groundEntity, false)
        || EasyModelEntitiesApi.isFrozen(groundEntity)) {
      helper.fail("Automatic behavior did not restore the host entity AI.");
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

    EasyModelAnimationSetting animationSetting =
        new EasyModelAnimationSetting(EasyModelAnimation.DEATH, EasyModelAnimationLoop.ONCE);
    hostEntity.setEasyModelAnimation(animationSetting);
    TagValueOutput output = TagValueOutput.createWithoutContext(ProblemReporter.DISCARDING);
    hostEntity.addAdditionalSaveData(output);
    CompoundTag compoundTag = output.buildResult();

    Optional<Entity> loadedEntity =
        EasyModelServices.entityFactory()
            .createEntity(helper.getLevel(), GROUND_PROFILE_ID, Vec3.ZERO);
    if (loadedEntity.isEmpty()
        || !(loadedEntity.get() instanceof EasyModelHostEntity loadedHostEntity)) {
      helper.fail("Could not create host entity for NBT load.");
      return;
    }

    loadedHostEntity.readAdditionalSaveData(
        TagValueInput.create(
            ProblemReporter.DISCARDING, helper.getLevel().registryAccess(), compoundTag));
    EasyModelRuntimeContract contract = loadedHostEntity.getEasyModelRuntimeContract();
    if (!GROUND_PROFILE_ID.equals(contract.profileId())
        || !animationSetting.equals(contract.animation())
        || contract.bodyType() != ModelBodyType.QUADRUPED) {
      helper.fail("NBT load did not preserve the persisted runtime contract fields.");
      return;
    }

    helper.succeed();
  }

  public static void nbtPreservesTextureSetting(GameTestHelper helper) {
    installProfiles();
    Optional<Entity> entity =
        EasyModelServices.entityFactory()
            .createEntity(helper.getLevel(), GROUND_PROFILE_ID, Vec3.ZERO);
    if (entity.isEmpty() || !(entity.get() instanceof EasyModelHostEntity hostEntity)) {
      helper.fail("Could not create host entity for texture round trip.");
      return;
    }

    TagValueOutput emptyOutput = TagValueOutput.createWithoutContext(ProblemReporter.DISCARDING);
    hostEntity.addAdditionalSaveData(emptyOutput);
    if (emptyOutput.buildResult().contains(EasyModelHostPersistence.TEXTURE_TAG)) {
      helper.fail("An empty texture setting was written to NBT.");
      return;
    }

    EasyModelTextureSetting textureSetting = EasyModelTextureSetting.of("screen", SCREEN_TEXTURE);
    hostEntity.setEasyModelTexture(textureSetting);
    if (!Optional.of(SCREEN_TEXTURE)
        .equals(hostEntity.getEasyModelTextureSetting().texture("screen"))) {
      helper.fail("Synched data did not return the texture setting.");
      return;
    }

    TagValueOutput output = TagValueOutput.createWithoutContext(ProblemReporter.DISCARDING);
    hostEntity.addAdditionalSaveData(output);
    CompoundTag compoundTag = output.buildResult();

    Optional<Entity> loadedEntity =
        EasyModelServices.entityFactory()
            .createEntity(helper.getLevel(), GROUND_PROFILE_ID, Vec3.ZERO);
    if (loadedEntity.isEmpty()
        || !(loadedEntity.get() instanceof EasyModelHostEntity loadedHostEntity)) {
      helper.fail("Could not create host entity for texture load.");
      return;
    }

    loadedHostEntity.readAdditionalSaveData(
        TagValueInput.create(
            ProblemReporter.DISCARDING, helper.getLevel().registryAccess(), compoundTag));
    if (!textureSetting.equals(loadedHostEntity.getEasyModelTextureSetting())) {
      helper.fail("NBT load did not preserve the texture setting.");
      return;
    }

    loadedHostEntity.setEasyModelTexture(EasyModelTextureSetting.EMPTY);
    if (!loadedHostEntity.getEasyModelTextureSetting().isEmpty()) {
      helper.fail("Clearing the texture setting did not reset the synched data.");
      return;
    }

    helper.succeed();
  }

  public static void nbtPreservesDisplaySettings(GameTestHelper helper) {
    installProfiles();
    Optional<Entity> entity =
        EasyModelServices.entityFactory()
            .createEntity(helper.getLevel(), GROUND_PROFILE_ID, Vec3.ZERO);
    if (entity.isEmpty() || !(entity.get() instanceof EasyModelHostEntity hostEntity)) {
      helper.fail("Could not create host entity for display setting round trip.");
      return;
    }

    TagValueOutput emptyOutput = TagValueOutput.createWithoutContext(ProblemReporter.DISCARDING);
    hostEntity.addAdditionalSaveData(emptyOutput);
    CompoundTag emptyTag = emptyOutput.buildResult();
    if (emptyTag.contains(EasyModelHostPersistence.OPACITY_TAG)
        || emptyTag.contains(EasyModelHostPersistence.LIGHT_LEVEL_TAG)) {
      helper.fail("An unset display setting was written to NBT.");
      return;
    }

    hostEntity.setEasyModelOpacity(0.4f);
    hostEntity.setEasyModelLightLevel(12);
    if (Math.abs(hostEntity.getEasyModelOpacity() - 0.4f) > 0.0001f
        || hostEntity.getEasyModelLightLevel() != 12) {
      helper.fail("Synched data did not return the display settings.");
      return;
    }

    TagValueOutput output = TagValueOutput.createWithoutContext(ProblemReporter.DISCARDING);
    hostEntity.addAdditionalSaveData(output);
    CompoundTag compoundTag = output.buildResult();

    Optional<Entity> loadedEntity =
        EasyModelServices.entityFactory()
            .createEntity(helper.getLevel(), GROUND_PROFILE_ID, Vec3.ZERO);
    if (loadedEntity.isEmpty()
        || !(loadedEntity.get() instanceof EasyModelHostEntity loadedHostEntity)) {
      helper.fail("Could not create host entity for display setting load.");
      return;
    }

    loadedHostEntity.readAdditionalSaveData(
        TagValueInput.create(
            ProblemReporter.DISCARDING, helper.getLevel().registryAccess(), compoundTag));
    if (Math.abs(loadedHostEntity.getEasyModelOpacity() - 0.4f) > 0.0001f
        || loadedHostEntity.getEasyModelLightLevel() != 12) {
      helper.fail("NBT load did not preserve the display settings.");
      return;
    }

    loadedHostEntity.setEasyModelOpacity(EasyModelDisplaySettings.NO_OPACITY);
    loadedHostEntity.setEasyModelLightLevel(EasyModelDisplaySettings.NO_LIGHT_LEVEL);
    if (EasyModelDisplaySettings.hasOpacityOverride(loadedHostEntity.getEasyModelOpacity())
        || EasyModelDisplaySettings.hasLightLevelOverride(
            loadedHostEntity.getEasyModelLightLevel())) {
      helper.fail("Clearing the display settings did not reset the synched data.");
      return;
    }

    helper.succeed();
  }

  public static void apiControlsHostEntityTextureAndAnimation(GameTestHelper helper) {
    installProfiles();
    Optional<Entity> entity =
        EasyModelServices.entityFactory()
            .createEntity(
                helper.getLevel(), GROUND_PROFILE_ID, helper.absoluteVec(new Vec3(1.5, 1, 1.5)));
    if (entity.isEmpty() || !(entity.get() instanceof EasyModelHostEntity hostEntity)) {
      helper.fail("Could not create host entity for the API round trip.");
      return;
    }
    helper.getLevel().addFreshEntity(hostEntity);

    EasyModelTextureSetting textureSetting = EasyModelTextureSetting.of("screen", SCREEN_TEXTURE);
    if (!EasyModelEntitiesApi.setTextureSetting(hostEntity, textureSetting)
        || !textureSetting.equals(EasyModelEntitiesApi.getTextureSetting(hostEntity))) {
      helper.fail("The API did not set the texture setting of the host entity.");
      return;
    }

    EasyModelAnimationSetting animationSetting =
        new EasyModelAnimationSetting(EasyModelAnimation.DEATH, EasyModelAnimationLoop.ONCE);
    if (!EasyModelEntitiesApi.setAnimation(hostEntity, animationSetting)
        || !animationSetting.equals(EasyModelEntitiesApi.getAnimationSetting(hostEntity))) {
      helper.fail("The API did not set the animation setting of the host entity.");
      return;
    }

    if (!EasyModelEntitiesApi.stopAnimation(hostEntity, EasyModelAnimationTransition.IMMEDIATE)
        || !EasyModelEntitiesApi.restartAnimation(hostEntity)) {
      helper.fail("The API did not send stop or restart for a server-side host entity.");
      return;
    }

    if (!EasyModelEntitiesApi.clearTextureSetting(hostEntity)
        || !EasyModelEntitiesApi.getTextureSetting(hostEntity).isEmpty()
        || !EasyModelEntitiesApi.setAnimation(hostEntity, EasyModelAnimationSetting.AUTO)
        || !EasyModelAnimationSetting.AUTO.equals(
            EasyModelEntitiesApi.getAnimationSetting(hostEntity))) {
      helper.fail("The API did not reset the texture and animation setting.");
      return;
    }

    Entity pig = EntityTypes.PIG.create(helper.getLevel(), EntitySpawnReason.LOAD);
    if (pig == null
        || EasyModelEntitiesApi.setTextureSetting(pig, textureSetting)
        || EasyModelEntitiesApi.setAnimation(pig, animationSetting)) {
      helper.fail("The API changed an entity that is not an EME host.");
      return;
    }

    hostEntity.discard();
    helper.succeed();
  }

  public static void apiControlsMutableRenderableEntity(GameTestHelper helper) {
    MutableRenderableTestEntity entity =
        new MutableRenderableTestEntity(helper.getLevel(), GROUND_PROFILE_ID);
    EasyModelTextureSetting textureSetting = EasyModelTextureSetting.of("screen", SCREEN_TEXTURE);
    EasyModelAnimationSetting animationSetting =
        new EasyModelAnimationSetting(EasyModelAnimation.ATTACK, EasyModelAnimationLoop.LOOP);
    if (!EasyModelEntitiesApi.setTextureSetting(entity, textureSetting)
        || !EasyModelEntitiesApi.setAnimation(entity, animationSetting)
        || !EasyModelEntitiesApi.setOpacity(entity, 0.5f)
        || !EasyModelEntitiesApi.setLightLevel(entity, 12)) {
      helper.fail("The API rejected an entity implementing the mutable renderable contract.");
      return;
    }

    if (!textureSetting.equals(EasyModelEntitiesApi.getTextureSetting(entity))
        || !animationSetting.equals(EasyModelEntitiesApi.getAnimationSetting(entity))
        || EasyModelEntitiesApi.getOpacity(entity) != 0.5f
        || EasyModelEntitiesApi.getLightLevel(entity) != 12) {
      helper.fail("The API did not hand the settings to the mutable renderable entity.");
      return;
    }

    if (!EasyModelEntitiesApi.clearTextureSetting(entity)
        || !EasyModelEntitiesApi.getTextureSetting(entity).isEmpty()) {
      helper.fail("The API did not clear the texture of the mutable renderable entity.");
      return;
    }

    helper.succeed();
  }

  public static void displayCommandChangesOnlyMutableRenderables(GameTestHelper helper) {
    MutableRenderableTestEntity mutableEntity =
        new MutableRenderableTestEntity(helper.getLevel(), GROUND_PROFILE_ID);
    Marker plainEntity = new Marker(EntityTypes.MARKER, helper.getLevel());
    Vec3 spawnPosition = helper.absoluteVec(new Vec3(1.5, 1, 1.5));
    mutableEntity.setPos(spawnPosition);
    plainEntity.setPos(spawnPosition);
    helper.getLevel().addFreshEntity(mutableEntity);
    helper.getLevel().addFreshEntity(plainEntity);

    int changedMutable =
        runCommand(helper, "display opacity set entity " + mutableEntity.getStringUUID() + " 40");
    runCommand(helper, "display light set entity " + mutableEntity.getStringUUID() + " 12");
    int changedPlain =
        runCommand(helper, "display opacity set entity " + plainEntity.getStringUUID() + " 40");

    mutableEntity.discard();
    plainEntity.discard();
    GameTestHelpers.assertTrue(
        helper,
        "The display command must change the mutable renderable and skip the plain entity!",
        changedMutable == 1
            && Math.abs(mutableEntity.getEasyModelOpacity() - 0.4f) < 0.001f
            && mutableEntity.getEasyModelLightLevel() == 12
            && changedPlain == 0);
  }

  static int runCommand(GameTestHelper helper, String command) {
    return runCommand(helper, commandSource(helper), command);
  }

  static int runCommand(GameTestHelper helper, CommandSourceStack source, String command) {
    AtomicInteger commandResult = new AtomicInteger();
    helper
        .getLevel()
        .getServer()
        .getCommands()
        .performPrefixedCommand(
            source.withCallback((success, result) -> commandResult.set(result)),
            Constants.MOD_ID + " " + command);
    return commandResult.get();
  }

  static CommandSourceStack commandSource(GameTestHelper helper) {
    return helper
        .getLevel()
        .getServer()
        .createCommandSourceStack()
        .withLevel(helper.getLevel())
        .withPermission(LevelBasedPermissionSet.OWNER)
        .withSuppressedOutput();
  }

  public static void apiControlsHostBlockEntityTextureAndAnimation(GameTestHelper helper) {
    installProfiles();
    Block block = BuiltInRegistries.BLOCK.getValue(ModelBlockIds.ANIMATED_BLOCK);
    BlockPos blockPos = new BlockPos(1, 1, 1);
    helper.setBlock(blockPos, block.defaultBlockState());
    if (!(helper.getBlockEntity(blockPos, BlockEntity.class)
        instanceof EasyModelHostBlockEntity hostBlockEntity)) {
      helper.fail("Placed host block did not create an EasyModelHostBlockEntity.");
      return;
    }
    hostBlockEntity.setEasyModelProfileId(BLOCK_PROFILE_ID);

    EasyModelTextureSetting textureSetting = EasyModelTextureSetting.of("crystal", CRYSTAL_TEXTURE);
    if (!EasyModelBlockEntitiesApi.setTextureSetting(hostBlockEntity, textureSetting)
        || !textureSetting.equals(EasyModelBlockEntitiesApi.getTextureSetting(hostBlockEntity))) {
      helper.fail("The API did not set the texture setting of the host block entity.");
      return;
    }

    EasyModelAnimationSetting animationSetting =
        new EasyModelAnimationSetting(EasyModelAnimation.ATTACK, EasyModelAnimationLoop.ONCE);
    if (!EasyModelBlockEntitiesApi.setAnimation(hostBlockEntity, animationSetting)
        || !animationSetting.equals(
            EasyModelBlockEntitiesApi.getAnimationSetting(hostBlockEntity))) {
      helper.fail("The API did not set the animation setting of the host block entity.");
      return;
    }

    if (!EasyModelBlockEntitiesApi.stopAnimation(
            hostBlockEntity, EasyModelAnimationTransition.IMMEDIATE)
        || !EasyModelBlockEntitiesApi.restartAnimation(hostBlockEntity)) {
      helper.fail("The API did not send stop or restart for a server-side host block entity.");
      return;
    }

    if (!EasyModelBlockEntitiesApi.clearTextureSetting(hostBlockEntity)
        || !EasyModelBlockEntitiesApi.getTextureSetting(hostBlockEntity).isEmpty()) {
      helper.fail("The API did not clear the texture setting of the host block entity.");
      return;
    }

    helper.succeed();
  }

  public static void apiControlsHostEntityDisplaySettings(GameTestHelper helper) {
    Optional<EasyModelHostEntity> spawnedEntity = spawnHostEntity(helper);
    if (spawnedEntity.isEmpty()) {
      helper.fail("Could not create host entity for the display API.");
      return;
    }

    EasyModelHostEntity hostEntity = spawnedEntity.get();
    boolean applied =
        EasyModelEntitiesApi.setOpacity(hostEntity, 0.25f)
            && EasyModelEntitiesApi.setLightLevel(hostEntity, 7)
            && EasyModelEntitiesApi.getOpacity(hostEntity) == 0.25f
            && EasyModelEntitiesApi.getLightLevel(hostEntity) == 7;
    boolean cleared =
        EasyModelEntitiesApi.clearOpacity(hostEntity)
            && EasyModelEntitiesApi.clearLightLevel(hostEntity)
            && EasyModelEntitiesApi.getOpacity(hostEntity) == EasyModelDisplaySettings.NO_OPACITY
            && EasyModelEntitiesApi.getLightLevel(hostEntity)
                == EasyModelDisplaySettings.NO_LIGHT_LEVEL;
    Entity pig = EntityTypes.PIG.create(helper.getLevel(), EntitySpawnReason.LOAD);
    boolean pigRejected =
        pig != null
            && !EasyModelEntitiesApi.setOpacity(pig, 0.25f)
            && !EasyModelEntitiesApi.setLightLevel(pig, 7);
    hostEntity.discard();
    GameTestHelpers.assertTrue(
        helper,
        "The display API must set and clear opacity and light only on host entities!",
        applied && cleared && pigRejected);
  }

  public static void apiPlaysAnimationsOnHostEntity(GameTestHelper helper) {
    Optional<EasyModelHostEntity> spawnedEntity = spawnHostEntity(helper);
    if (spawnedEntity.isEmpty()) {
      helper.fail("Could not create host entity for the animation API.");
      return;
    }

    EasyModelHostEntity hostEntity = spawnedEntity.get();
    boolean played =
        EasyModelEntitiesApi.playAnimation(
            hostEntity,
            EasyModelAnimation.WALK,
            EasyModelAnimationPlayback.DEFAULT,
            EasyModelAnimationTransition.DEFAULT);
    boolean randomPlayed =
        EasyModelEntitiesApi.playRandomAnimation(
            hostEntity,
            List.of(EasyModelAnimation.WALK, EasyModelAnimation.IDLE),
            EasyModelAnimationPlayback.DEFAULT,
            EasyModelAnimationTransition.DEFAULT);
    boolean emptyRandomRejected =
        !EasyModelEntitiesApi.playRandomAnimation(
            hostEntity,
            List.of(),
            EasyModelAnimationPlayback.DEFAULT,
            EasyModelAnimationTransition.DEFAULT);
    boolean sequencePlayed =
        EasyModelEntitiesApi.playAnimationSequence(
            hostEntity,
            EasyModelAnimationSequence.of(EasyModelAnimation.WALK, EasyModelAnimation.ATTACK)
                .withFallback(EasyModelAnimation.IDLE));
    hostEntity.discard();
    GameTestHelpers.assertTrue(
        helper,
        "The animation API must play single, random and sequenced clips on a host entity!",
        played && randomPlayed && emptyRandomRejected && sequencePlayed);
  }

  public static void apiSitsHostEntityOnShoulder(GameTestHelper helper) {
    Optional<EasyModelHostEntity> spawnedEntity = spawnHostEntity(helper);
    if (spawnedEntity.isEmpty()) {
      helper.fail("Could not create host entity for the shoulder API.");
      return;
    }

    EasyModelHostEntity hostEntity = spawnedEntity.get();
    ServerPlayer player = createShoulderTestPlayer(helper);
    Entity pig = EntityTypes.PIG.create(helper.getLevel(), EntitySpawnReason.LOAD);
    boolean pigRejected = pig != null && !EasyModelEntitiesApi.sitOnShoulder(pig, player);
    boolean seated = EasyModelEntitiesApi.sitOnShoulder(hostEntity, player);
    boolean onShoulder =
        GROUND_PROFILE_ID
            .toString()
            .equals(
                player
                    .getShoulderEntityLeft()
                    .getStringOr(EasyModelHostPersistence.PROFILE_ID_TAG, ""));
    GameTestHelpers.assertTrue(
        helper,
        "The shoulder API must move a host entity onto the player's shoulder and skip a pig!",
        pigRejected && seated && onShoulder && hostEntity.isRemoved());
  }

  public static void apiControlsHostBlockEntityProfileAndDisplay(GameTestHelper helper) {
    Optional<EasyModelHostBlockEntity> placedBlockEntity = placeHostBlock(helper);
    if (placedBlockEntity.isEmpty()) {
      helper.fail("Placed host block did not create an EasyModelHostBlockEntity.");
      return;
    }

    EasyModelHostBlockEntity hostBlockEntity = placedBlockEntity.get();
    boolean profileSet =
        EasyModelBlockEntitiesApi.setProfileId(hostBlockEntity, BLOCK_PROFILE_ID)
            && EasyModelBlockEntitiesApi.getProfileId(hostBlockEntity)
                .filter(BLOCK_PROFILE_ID::equals)
                .isPresent();
    BlockPos chestPos = new BlockPos(0, 1, 0);
    helper.setBlock(chestPos, Blocks.CHEST.defaultBlockState());
    BlockEntity chest = helper.getBlockEntity(chestPos, BlockEntity.class);
    boolean chestRejected =
        chest != null
            && !EasyModelBlockEntitiesApi.setProfileId(chest, BLOCK_PROFILE_ID)
            && EasyModelBlockEntitiesApi.getProfileId(chest).isEmpty()
            && !EasyModelBlockEntitiesApi.setOpacity(chest, 0.5f);
    boolean applied =
        EasyModelBlockEntitiesApi.setOpacity(hostBlockEntity, 0.5f)
            && EasyModelBlockEntitiesApi.setLightLevel(hostBlockEntity, 9)
            && EasyModelBlockEntitiesApi.getOpacity(hostBlockEntity) == 0.5f
            && EasyModelBlockEntitiesApi.getLightLevel(hostBlockEntity) == 9;
    boolean cleared =
        EasyModelBlockEntitiesApi.clearOpacity(hostBlockEntity)
            && EasyModelBlockEntitiesApi.clearLightLevel(hostBlockEntity)
            && EasyModelBlockEntitiesApi.getOpacity(hostBlockEntity)
                == EasyModelDisplaySettings.NO_OPACITY
            && EasyModelBlockEntitiesApi.getLightLevel(hostBlockEntity)
                == EasyModelDisplaySettings.NO_LIGHT_LEVEL;
    GameTestHelpers.assertTrue(
        helper,
        "The block API must set profile, opacity and light only on host block entities!",
        profileSet && chestRejected && applied && cleared);
  }

  public static void apiPlaysAnimationsOnHostBlockEntity(GameTestHelper helper) {
    Optional<EasyModelHostBlockEntity> placedBlockEntity = placeHostBlock(helper);
    if (placedBlockEntity.isEmpty()) {
      helper.fail("Placed host block did not create an EasyModelHostBlockEntity.");
      return;
    }

    EasyModelHostBlockEntity hostBlockEntity = placedBlockEntity.get();
    boolean played =
        EasyModelBlockEntitiesApi.playAnimation(
            hostBlockEntity,
            EasyModelAnimation.ATTACK,
            EasyModelAnimationPlayback.DEFAULT,
            EasyModelAnimationTransition.DEFAULT);
    boolean randomPlayed =
        EasyModelBlockEntitiesApi.playRandomAnimation(
            hostBlockEntity,
            List.of(EasyModelAnimation.ATTACK, EasyModelAnimation.IDLE),
            EasyModelAnimationPlayback.DEFAULT,
            EasyModelAnimationTransition.DEFAULT);
    boolean emptyRandomRejected =
        !EasyModelBlockEntitiesApi.playRandomAnimation(
            hostBlockEntity,
            List.of(),
            EasyModelAnimationPlayback.DEFAULT,
            EasyModelAnimationTransition.DEFAULT);
    boolean sequencePlayed =
        EasyModelBlockEntitiesApi.playAnimationSequence(
            hostBlockEntity,
            EasyModelAnimationSequence.of(EasyModelAnimation.ATTACK, EasyModelAnimation.IDLE)
                .withFallback(EasyModelAnimation.IDLE));
    GameTestHelpers.assertTrue(
        helper,
        "The block animation API must play single, random and sequenced clips!",
        played && randomPlayed && emptyRandomRejected && sequencePlayed);
  }

  static Optional<EasyModelHostEntity> spawnHostEntity(GameTestHelper helper) {
    installProfiles();
    Optional<Entity> entity =
        EasyModelServices.entityFactory()
            .createEntity(
                helper.getLevel(), GROUND_PROFILE_ID, helper.absoluteVec(new Vec3(1.5, 1, 1.5)));
    if (entity.isEmpty() || !(entity.get() instanceof EasyModelHostEntity hostEntity)) {
      return Optional.empty();
    }

    helper.getLevel().addFreshEntity(hostEntity);
    return Optional.of(hostEntity);
  }

  static ServerPlayer createShoulderTestPlayer(GameTestHelper helper) {
    ServerLevel level = helper.getLevel();
    ServerPlayer player =
        new ServerPlayer(
            level.getServer(),
            level,
            new GameProfile(UUID.randomUUID(), "shoulder-test-player"),
            ClientInformation.createDefault());
    player.setOnGround(true);
    return player;
  }

  static Optional<EasyModelHostBlockEntity> placeHostBlock(GameTestHelper helper) {
    installProfiles();
    BlockPos blockPos = new BlockPos(1, 1, 1);
    helper.setBlock(
        blockPos, BuiltInRegistries.BLOCK.getValue(ModelBlockIds.ANIMATED_BLOCK).defaultBlockState());
    if (!(helper.getBlockEntity(blockPos, BlockEntity.class)
        instanceof EasyModelHostBlockEntity hostBlockEntity)) {
      return Optional.empty();
    }

    hostBlockEntity.setEasyModelProfileId(BLOCK_PROFILE_ID);
    return Optional.of(hostBlockEntity);
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
    hostEntity.readAdditionalSaveData(
        TagValueInput.create(
            ProblemReporter.DISCARDING, helper.getLevel().registryAccess(), compoundTag));

    EasyModelRuntimeContract contract = hostEntity.getEasyModelRuntimeContract();
    if (!EasyModelHostEntity.MISSING_PROFILE_ID.equals(contract.profileId())
        || contract.bodyType() != ModelBodyType.STATIC
        || contract.animation() != EasyModelAnimationSetting.AUTO) {
      helper.fail("Invalid NBT Identifier values did not fall back safely.");
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
    Block block =
        BuiltInRegistries.BLOCK
            .get(ModelBlockIds.ANIMATED_BLOCK)
            .map(Holder.Reference::value)
            .orElse(null);
    if (block == null || block == Blocks.AIR) {
      helper.fail("Animated host block was not registered.");
      return;
    }

    BlockPos blockPos = new BlockPos(1, 1, 1);
    helper.setBlock(blockPos, block.defaultBlockState());
    if (!(helper.getBlockEntity(blockPos, BlockEntity.class)
        instanceof EasyModelHostBlockEntity hostBlockEntity)) {
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

    if (!(helper.getBlockEntity(floor.above(), BlockEntity.class)
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

  public static void blockSpawnItemTurnsHostBlockTowardsThePlayer(GameTestHelper helper) {
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
    Player player = helper.makeMockPlayer(GameType.CREATIVE);
    player.setYRot(90.0f);
    ItemStack stack = EasyModelEntitiesItems.forProfile(item, BLOCK_PROFILE_ID);
    item.useOn(
        new UseOnContext(helper.getLevel(), player, InteractionHand.MAIN_HAND, stack, hit) {});

    GameTestHelpers.assertTrue(
        helper,
        "The block spawn item must turn the host block towards the player!",
        helper.getBlockState(floor.above()).getValue(EasyModelHostBlock.FACING) == Direction.EAST);
  }

  public static void placeBlockCommandTurnsHostBlockTowardsTheExecutingEntity(
      GameTestHelper helper) {
    installProfiles();
    BlockPos turnedPos = new BlockPos(0, 1, 1);
    BlockPos consolePos = new BlockPos(2, 1, 1);
    CommandSourceStack entitySource =
        commandSource(helper)
            .withEntity(new Marker(EntityTypes.MARKER, helper.getLevel()))
            .withRotation(new Vec2(0.0f, 90.0f));
    runCommand(helper, entitySource, placeBlockCommand(helper.absolutePos(turnedPos)));
    runCommand(helper, placeBlockCommand(helper.absolutePos(consolePos)));

    GameTestHelpers.assertTrue(
        helper,
        "The place_block command must face the executing entity and keep the default otherwise!",
        helper.getBlockState(turnedPos).getValue(EasyModelHostBlock.FACING) == Direction.EAST
            && helper.getBlockState(consolePos).getValue(EasyModelHostBlock.FACING)
                == Direction.SOUTH);
  }

  private static String placeBlockCommand(BlockPos blockPos) {
    return "place_block "
        + BLOCK_PROFILE_ID
        + " "
        + blockPos.getX()
        + " "
        + blockPos.getY()
        + " "
        + blockPos.getZ();
  }

  static void installProfiles() {
    EasyModelProfileService currentProfileService = EasyModelServices.profileService();
    EasyModelProfileService fallbackProfileService =
        currentProfileService instanceof GameTestProfileService gameTestProfileService
            ? gameTestProfileService.fallback()
            : currentProfileService;
    EasyModelServices.setProfileService(
        new GameTestProfileService(activeProfiles(), fallbackProfileService));
  }

  private static Map<Identifier, EasyModelEntityProfile> activeProfiles() {
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

  private static EasyModelEntityProfile parse(Identifier profileId, String json) {
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

  private record GameTestProfileService(
      Map<Identifier, EasyModelEntityProfile> profiles, EasyModelProfileService fallback)
      implements EasyModelProfileService {

    @Override
    public Collection<ModelResourceRejection> getRejectedResources() {
      return this.fallback.getRejectedResources();
    }

    @Override
    public Optional<EasyModelEntityProfile> getProfile(Identifier profileId) {
      return Optional.ofNullable(this.profiles.get(profileId))
          .or(() -> this.fallback.getProfile(profileId));
    }

    @Override
    public List<EasyModelEntityProfile> getProfiles() {
      return Stream.concat(
              this.profiles.values().stream(),
              this.fallback.getProfiles().stream()
                  .filter(profile -> !this.profiles.containsKey(profile.id())))
          .toList();
    }
  }
}
