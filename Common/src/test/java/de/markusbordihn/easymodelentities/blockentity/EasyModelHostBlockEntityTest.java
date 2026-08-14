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

package de.markusbordihn.easymodelentities.blockentity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import de.markusbordihn.easymodelentities.api.data.EasyModelAnimation;
import de.markusbordihn.easymodelentities.api.data.EasyModelAnimationLoop;
import de.markusbordihn.easymodelentities.api.data.EasyModelAnimationSetting;
import de.markusbordihn.easymodelentities.api.data.EasyModelTextureSetting;
import de.markusbordihn.easymodelentities.data.profile.EasyModelEntityProfile;
import de.markusbordihn.easymodelentities.data.profile.ModelAttributes;
import de.markusbordihn.easymodelentities.data.profile.ModelBehaviorMode;
import de.markusbordihn.easymodelentities.data.profile.ModelBehaviorSettings;
import de.markusbordihn.easymodelentities.data.profile.ModelBlockEntityPresetType;
import de.markusbordihn.easymodelentities.data.profile.ModelBlockEntitySettings;
import de.markusbordihn.easymodelentities.data.profile.ModelBodyType;
import de.markusbordihn.easymodelentities.data.profile.ModelClientSettings;
import de.markusbordihn.easymodelentities.data.profile.ModelDimensions;
import de.markusbordihn.easymodelentities.data.profile.ModelMovementSettings;
import de.markusbordihn.easymodelentities.data.profile.ModelProfileStatus;
import de.markusbordihn.easymodelentities.data.profile.ModelType;
import de.markusbordihn.easymodelentities.event.EasyModelReloadDispatcher;
import de.markusbordihn.easymodelentities.profile.EasyModelProfileService;
import de.markusbordihn.easymodelentities.registry.EasyModelServices;
import de.markusbordihn.easymodelentities.registry.ModelBlockEntityTypeIds;
import java.util.List;
import java.util.Optional;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class EasyModelHostBlockEntityTest {

  private static final ResourceLocation PROFILE_ID =
      ResourceLocation.fromNamespaceAndPath("example", "lantern");
  private static final ResourceLocation RENDER_PROFILE_ID =
      ResourceLocation.fromNamespaceAndPath("example", "lantern_render");
  private static final ResourceLocation SCREEN_TEXTURE =
      ResourceLocation.fromNamespaceAndPath("example", "textures/entity/lantern/screen_sad.png");

  @BeforeAll
  static void bootstrapMinecraft() {
    SharedConstants.tryDetectVersion();
    Bootstrap.bootStrap();
  }

  private static EasyModelEntityProfile profile() {
    return profile("server-v1");
  }

  private static EasyModelEntityProfile profile(String version) {
    return new EasyModelEntityProfile(
        PROFILE_ID,
        "0.1.0",
        version,
        ModelType.BLOCK_ENTITY,
        null,
        new ModelBlockEntitySettings(
            ModelBlockEntityTypeIds.ANIMATED_BLOCK_ENTITY,
            ModelBlockEntityPresetType.ANIMATED,
            ModelBodyType.BIPED),
        new ModelClientSettings(RENDER_PROFILE_ID),
        new ModelDimensions(1.0f, 1.5f, 0.5f),
        new ModelMovementSettings(0.0f, 0.0f, false),
        new ModelBehaviorSettings(ModelBehaviorMode.STATIC, false, false),
        new ModelAttributes(10.0f, 0.0f, 16.0f),
        ModelProfileStatus.ACTIVE,
        List.of());
  }

  private static EasyModelProfileService profileService(EasyModelEntityProfile profile) {
    return new EasyModelProfileService() {
      @Override
      public Optional<EasyModelEntityProfile> getProfile(ResourceLocation profileId) {
        return PROFILE_ID.equals(profileId) ? Optional.of(profile) : Optional.empty();
      }
    };
  }

  @SuppressWarnings("unchecked")
  private static <T extends BlockEntity> BlockEntityType<T> mockValidType() {
    BlockEntityType<T> type = mock(BlockEntityType.class);
    Mockito.lenient().when(type.isValid(Mockito.any())).thenReturn(true);
    return type;
  }

  @AfterEach
  void resetServices() {
    EasyModelServices.reset();
  }

  @Test
  @DisplayName("A looping named clip is scenery, so it must survive a world reload")
  void persistsNamedLoopingAnimationAcrossSaveAndLoad() {
    EasyModelServices.setProfileService(profileService(profile()));
    TestBlockEntity blockEntity = new TestBlockEntity();

    blockEntity.setEasyModelProfileId(PROFILE_ID);
    blockEntity.setEasyModelAnimation(
        new EasyModelAnimationSetting(
            EasyModelAnimation.named("spin"), EasyModelAnimationLoop.LOOP));
    CompoundTag savedTag = new CompoundTag();
    blockEntity.saveAdditional(savedTag, null);

    TestBlockEntity loadedBlockEntity = new TestBlockEntity();
    loadedBlockEntity.loadAdditional(savedTag, null);

    EasyModelAnimationSetting animation = loadedBlockEntity.getEasyModelAnimationSetting();
    assertEquals("spin", animation.animation().name());
    assertTrue(animation.animation().isNamed());
    assertEquals(EasyModelAnimationLoop.LOOP, animation.loop());
  }

  @Test
  void syncsNamedLoopingAnimationThroughTheClientUpdateTag() {
    EasyModelServices.setProfileService(profileService(profile()));
    TestBlockEntity blockEntity = new TestBlockEntity();

    blockEntity.setEasyModelProfileId(PROFILE_ID);
    blockEntity.setEasyModelAnimation(
        new EasyModelAnimationSetting(
            EasyModelAnimation.named("spin"), EasyModelAnimationLoop.LOOP));

    TestBlockEntity clientBlockEntity = new TestBlockEntity();
    clientBlockEntity.loadAdditional(blockEntity.getUpdateTag(null), null);

    assertEquals(
        new EasyModelAnimationSetting(
            EasyModelAnimation.named("spin"), EasyModelAnimationLoop.LOOP),
        clientBlockEntity.getEasyModelAnimationSetting());
  }

  @Test
  void persistsRuntimeContractAcrossSaveAndLoad() {
    EasyModelServices.setProfileService(profileService(profile()));
    TestBlockEntity blockEntity = new TestBlockEntity();

    blockEntity.setEasyModelProfileId(PROFILE_ID);
    blockEntity.setEasyModelAnimation(EasyModelAnimationSetting.of(EasyModelAnimation.IDLE));
    CompoundTag savedTag = new CompoundTag();
    blockEntity.saveAdditional(savedTag, null);

    TestBlockEntity loadedBlockEntity = new TestBlockEntity();
    loadedBlockEntity.loadAdditional(savedTag, null);

    assertEquals(PROFILE_ID, loadedBlockEntity.getEasyModelProfileId());
    assertEquals(RENDER_PROFILE_ID, loadedBlockEntity.getEasyModelRenderProfileId());
    assertEquals("server-v1", loadedBlockEntity.getEasyModelVersion());
    assertEquals(
        EasyModelAnimationSetting.of(EasyModelAnimation.IDLE),
        loadedBlockEntity.getEasyModelAnimationSetting());
    assertEquals(ModelBodyType.BIPED, loadedBlockEntity.getEasyModelRuntimeContract().bodyType());
  }

  @Test
  void syncsRuntimeContractThroughClientUpdateTag() {
    EasyModelServices.setProfileService(profileService(profile()));
    TestBlockEntity serverBlockEntity = new TestBlockEntity();

    serverBlockEntity.setEasyModelProfileId(PROFILE_ID);
    serverBlockEntity.setEasyModelAnimation(EasyModelAnimationSetting.of(EasyModelAnimation.IDLE));

    CompoundTag updateTag = serverBlockEntity.getUpdateTag(null);

    TestBlockEntity clientBlockEntity = new TestBlockEntity();
    clientBlockEntity.loadAdditional(updateTag, null);

    assertEquals(PROFILE_ID, clientBlockEntity.getEasyModelProfileId());
    assertEquals(RENDER_PROFILE_ID, clientBlockEntity.getEasyModelRenderProfileId());
    assertEquals("server-v1", clientBlockEntity.getEasyModelVersion());
    assertEquals(
        EasyModelAnimationSetting.of(EasyModelAnimation.IDLE),
        clientBlockEntity.getEasyModelAnimationSetting());
    assertEquals(ModelBodyType.BIPED, clientBlockEntity.getEasyModelRuntimeContract().bodyType());
  }

  @Test
  void persistsTextureOverrideAcrossSaveAndLoad() {
    EasyModelServices.setProfileService(profileService(profile()));
    TestBlockEntity blockEntity = new TestBlockEntity();

    blockEntity.setEasyModelProfileId(PROFILE_ID);
    blockEntity.setEasyModelTexture(EasyModelTextureSetting.of("screen", SCREEN_TEXTURE));
    CompoundTag savedTag = new CompoundTag();
    blockEntity.saveAdditional(savedTag, null);

    TestBlockEntity loadedBlockEntity = new TestBlockEntity();
    loadedBlockEntity.loadAdditional(savedTag, null);

    assertEquals(
        Optional.of(SCREEN_TEXTURE),
        loadedBlockEntity.getEasyModelTextureSetting().texture("screen"));
  }

  @Test
  void syncsTextureOverrideThroughClientUpdateTag() {
    EasyModelServices.setProfileService(profileService(profile()));
    TestBlockEntity serverBlockEntity = new TestBlockEntity();

    serverBlockEntity.setEasyModelProfileId(PROFILE_ID);
    serverBlockEntity.setEasyModelTexture(EasyModelTextureSetting.of("screen", SCREEN_TEXTURE));

    TestBlockEntity clientBlockEntity = new TestBlockEntity();
    clientBlockEntity.loadAdditional(serverBlockEntity.getUpdateTag(null), null);

    assertEquals(
        Optional.of(SCREEN_TEXTURE),
        clientBlockEntity.getEasyModelTextureSetting().texture("screen"));
  }

  @Test
  @DisplayName("A block entity without a texture override loads as empty")
  void withoutATextureOverrideTheSettingStaysEmpty() {
    EasyModelServices.setProfileService(profileService(profile()));
    TestBlockEntity blockEntity = new TestBlockEntity();

    blockEntity.setEasyModelProfileId(PROFILE_ID);
    CompoundTag savedTag = new CompoundTag();
    blockEntity.saveAdditional(savedTag, null);

    TestBlockEntity loadedBlockEntity = new TestBlockEntity();
    loadedBlockEntity.loadAdditional(savedTag, null);

    assertEquals(EasyModelTextureSetting.EMPTY, loadedBlockEntity.getEasyModelTextureSetting());
  }

  @Test
  void clientTickAdvancesAnimationTicks() {
    TestBlockEntity blockEntity = new TestBlockEntity();

    blockEntity.clientTick(null, BlockPos.ZERO, mock(BlockState.class));

    assertEquals(1, blockEntity.getEasyModelAnimationTicks());
  }

  @Test
  void profileReloadRebindsLoadedServerBlockEntity() {
    EasyModelServices.setProfileService(profileService(profile("server-v1")));
    TestBlockEntity blockEntity = new TestBlockEntity();
    blockEntity.setLevel(mock(Level.class));
    blockEntity.setEasyModelProfileId(PROFILE_ID);

    EasyModelServices.setProfileService(profileService(profile("server-v2")));
    EasyModelReloadDispatcher.fireProfileReload();

    assertEquals("server-v2", blockEntity.getEasyModelVersion());
  }

  @Test
  void randomlyAnimatedClientTickRestsBetweenBurstsAndEventuallyAnimates() {
    EasyModelServices.setBlockEntityTypeProvider(
        new EasyModelHostBlockEntityTypeProvider() {
          @Override
          public BlockEntityType<EasyModelStaticBlockEntity> staticBlockEntityType() {
            return mockValidType();
          }

          @Override
          public BlockEntityType<EasyModelTickingBlockEntity> tickingBlockEntityType() {
            return mockValidType();
          }

          @Override
          public BlockEntityType<EasyModelAnimatedBlockEntity> animatedBlockEntityType() {
            return mockValidType();
          }

          @Override
          public BlockEntityType<EasyModelRandomlyAnimatedBlockEntity>
              animatedRandomlyBlockEntityType() {
            return mockValidType();
          }
        });
    RandomlyAnimatedTestBlockEntity blockEntity = new RandomlyAnimatedTestBlockEntity();

    assertEquals(0.0f, blockEntity.getEasyModelAnimationTicks(0.5f));
    for (int tick = 0; tick < 450 && blockEntity.getEasyModelAnimationTicks(0.0f) == 0.0f; tick++) {
      blockEntity.clientTick(null, BlockPos.ZERO, mock(BlockState.class));
    }

    assertTrue(blockEntity.getEasyModelAnimationTicks(0.5f) > 0.0f);
  }

  private static class TestBlockEntity extends EasyModelHostBlockEntity {

    TestBlockEntity() {
      super(mockValidType(), BlockPos.ZERO, mock(BlockState.class));
    }
  }

  private static class RandomlyAnimatedTestBlockEntity
      extends EasyModelRandomlyAnimatedBlockEntity {

    RandomlyAnimatedTestBlockEntity() {
      super(BlockPos.ZERO, mock(BlockState.class));
    }
  }
}
