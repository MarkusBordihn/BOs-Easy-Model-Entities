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
import de.markusbordihn.easymodelentities.profile.EasyModelProfileService;
import de.markusbordihn.easymodelentities.registry.EasyModelServices;
import de.markusbordihn.easymodelentities.registry.ModelBlockEntityTypeIds;
import de.markusbordihn.easymodelentities.runtime.EasyModelAnimationState;
import java.util.List;
import java.util.Optional;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.Bootstrap;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class EasyModelHostBlockEntityTest {

  private static final Identifier PROFILE_ID =
      Identifier.fromNamespaceAndPath("example", "lantern");
  private static final Identifier RENDER_PROFILE_ID =
      Identifier.fromNamespaceAndPath("example", "lantern_render");

  @BeforeAll
  static void bootstrapMinecraft() {
    SharedConstants.tryDetectVersion();
    Bootstrap.bootStrap();
  }

  private static EasyModelEntityProfile profile() {
    return new EasyModelEntityProfile(
        PROFILE_ID,
        "0.1.0",
        "server-v1",
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
      public Optional<EasyModelEntityProfile> getProfile(Identifier profileId) {
        return PROFILE_ID.equals(profileId) ? Optional.of(profile) : Optional.empty();
      }
    };
  }

  @SuppressWarnings("unchecked")
  private static <T extends net.minecraft.world.level.block.entity.BlockEntity>
      BlockEntityType<T> mockValidType() {
    BlockEntityType<T> type = mock(BlockEntityType.class);
    Mockito.lenient().when(type.isValid(Mockito.any())).thenReturn(true);
    return type;
  }

  @AfterEach
  void resetServices() {
    EasyModelServices.reset();
  }

  @Test
  void persistsRuntimeContractAcrossSaveAndLoad() {
    EasyModelServices.setProfileService(profileService(profile()));
    TestBlockEntity blockEntity = new TestBlockEntity();

    blockEntity.setEasyModelProfileId(PROFILE_ID);
    blockEntity.setEasyModelAnimationState(EasyModelAnimationState.IDLE);
    TagValueOutput output = TagValueOutput.createWithoutContext(ProblemReporter.DISCARDING);
    blockEntity.saveAdditional(output);
    CompoundTag savedTag = output.buildResult();

    TestBlockEntity loadedBlockEntity = new TestBlockEntity();
    loadedBlockEntity.loadAdditional(
        TagValueInput.create(ProblemReporter.DISCARDING, RegistryAccess.EMPTY, savedTag));

    assertEquals(PROFILE_ID, loadedBlockEntity.getEasyModelProfileId());
    assertEquals(RENDER_PROFILE_ID, loadedBlockEntity.getEasyModelRenderProfileId());
    assertEquals("server-v1", loadedBlockEntity.getEasyModelVersion());
    assertEquals(
        EasyModelAnimationState.IDLE.getApiState(), loadedBlockEntity.getEasyModelAnimationState());
    assertEquals(ModelBodyType.BIPED, loadedBlockEntity.getEasyModelRuntimeContract().bodyType());
  }

  @Test
  void syncsRuntimeContractThroughClientUpdateTag() {
    EasyModelServices.setProfileService(profileService(profile()));
    TestBlockEntity serverBlockEntity = new TestBlockEntity();

    serverBlockEntity.setEasyModelProfileId(PROFILE_ID);
    serverBlockEntity.setEasyModelAnimationState(EasyModelAnimationState.IDLE);

    CompoundTag updateTag = serverBlockEntity.getUpdateTag(RegistryAccess.EMPTY);

    TestBlockEntity clientBlockEntity = new TestBlockEntity();
    clientBlockEntity.loadAdditional(
        TagValueInput.create(ProblemReporter.DISCARDING, RegistryAccess.EMPTY, updateTag));

    assertEquals(PROFILE_ID, clientBlockEntity.getEasyModelProfileId());
    assertEquals(RENDER_PROFILE_ID, clientBlockEntity.getEasyModelRenderProfileId());
    assertEquals("server-v1", clientBlockEntity.getEasyModelVersion());
    assertEquals(
        EasyModelAnimationState.IDLE.getApiState(), clientBlockEntity.getEasyModelAnimationState());
    assertEquals(ModelBodyType.BIPED, clientBlockEntity.getEasyModelRuntimeContract().bodyType());
  }

  @Test
  void clientTickAdvancesAnimationTicks() {
    TestBlockEntity blockEntity = new TestBlockEntity();

    blockEntity.clientTick(null, BlockPos.ZERO, mock(BlockState.class));

    assertEquals(1, blockEntity.getEasyModelAnimationTicks());
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
