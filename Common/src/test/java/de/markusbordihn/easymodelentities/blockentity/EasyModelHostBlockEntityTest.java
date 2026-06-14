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
import static org.mockito.Mockito.mock;

import de.markusbordihn.easymodelentities.profile.EasyModelEntityProfile;
import de.markusbordihn.easymodelentities.profile.EasyModelProfileService;
import de.markusbordihn.easymodelentities.profile.ModelAttributes;
import de.markusbordihn.easymodelentities.profile.ModelBehaviorMode;
import de.markusbordihn.easymodelentities.profile.ModelBehaviorSettings;
import de.markusbordihn.easymodelentities.profile.ModelBlockEntityPresetType;
import de.markusbordihn.easymodelentities.profile.ModelBlockEntitySettings;
import de.markusbordihn.easymodelentities.profile.ModelBodyType;
import de.markusbordihn.easymodelentities.profile.ModelClientSettings;
import de.markusbordihn.easymodelentities.profile.ModelDimensions;
import de.markusbordihn.easymodelentities.profile.ModelMovementSettings;
import de.markusbordihn.easymodelentities.profile.ModelProfileStatus;
import de.markusbordihn.easymodelentities.profile.ModelType;
import de.markusbordihn.easymodelentities.registry.EasyModelServices;
import de.markusbordihn.easymodelentities.registry.ModelBlockEntityTypeIds;
import de.markusbordihn.easymodelentities.runtime.EasyModelAnimationState;
import java.util.List;
import java.util.Optional;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class EasyModelHostBlockEntityTest {

  private static final ResourceLocation PROFILE_ID = new ResourceLocation("example", "lantern");
  private static final ResourceLocation RENDER_PROFILE_ID =
      new ResourceLocation("example", "lantern_render");

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
      public Optional<EasyModelEntityProfile> getProfile(ResourceLocation profileId) {
        return PROFILE_ID.equals(profileId) ? Optional.of(profile) : Optional.empty();
      }
    };
  }

  @AfterEach
  void resetServices() {
    EasyModelServices.reset();
  }

  @Test
  void appliesActiveProfileAndPersistsRuntimeContractToUpdateTag() {
    EasyModelServices.setProfileService(profileService(profile()));
    TestBlockEntity blockEntity = new TestBlockEntity();

    blockEntity.setEasyModelProfileId(PROFILE_ID);
    blockEntity.setEasyModelAnimationState(EasyModelAnimationState.IDLE);
    CompoundTag updateTag = blockEntity.getUpdateTag();

    TestBlockEntity loadedBlockEntity = new TestBlockEntity();
    loadedBlockEntity.load(updateTag);

    assertEquals(PROFILE_ID, loadedBlockEntity.getEasyModelProfileId());
    assertEquals(RENDER_PROFILE_ID, loadedBlockEntity.getEasyModelRenderProfileId());
    assertEquals("server-v1", loadedBlockEntity.getEasyModelVersion());
    assertEquals(
        EasyModelAnimationState.IDLE.getApiState(), loadedBlockEntity.getEasyModelAnimationState());
    assertEquals(ModelBodyType.BIPED, loadedBlockEntity.getEasyModelRuntimeContract().bodyType());
  }

  @Test
  void clientTickAdvancesAnimationTicks() {
    TestBlockEntity blockEntity = new TestBlockEntity();

    blockEntity.clientTick(null, BlockPos.ZERO, mock(BlockState.class));

    assertEquals(1, blockEntity.getEasyModelAnimationTicks());
  }

  private static class TestBlockEntity extends EasyModelHostBlockEntity {

    TestBlockEntity() {
      super(mock(BlockEntityType.class), BlockPos.ZERO, mock(BlockState.class));
    }
  }
}
