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

package de.markusbordihn.easymodelentities.api.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import de.markusbordihn.easymodelentities.api.EasyModelAnimationStates;
import de.markusbordihn.easymodelentities.api.EasyModelRenderable;
import de.markusbordihn.easymodelentities.client.render.EasyModelBlockEntityRenderBackend;
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
import de.markusbordihn.easymodelentities.registry.ModelBlockEntityTypeIds;
import de.markusbordihn.easymodelentities.renderprofile.EasyModelRenderProfile;
import de.markusbordihn.easymodelentities.renderprofile.EasyModelRenderProfileService;
import de.markusbordihn.easymodelentities.renderprofile.ModelAnimationMode;
import de.markusbordihn.easymodelentities.renderprofile.ModelAnimationSettings;
import de.markusbordihn.easymodelentities.renderprofile.ModelRenderProfileStatus;
import de.markusbordihn.easymodelentities.renderprofile.ModelRenderSettings;
import de.markusbordihn.easymodelentities.runtime.EasyModelAnimationState;
import de.markusbordihn.easymodelentities.runtime.EasyModelRuntimeContract;
import java.util.List;
import java.util.Optional;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class EasyModelBlockEntityRenderDelegateTest {

  private static final ResourceLocation PROFILE_ID = new ResourceLocation("example", "bell");
  private static final ResourceLocation RENDER_PROFILE_ID =
      new ResourceLocation("example", "bell_render");

  @BeforeAll
  static void bootstrapMinecraft() {
    SharedConstants.tryDetectVersion();
    Bootstrap.bootStrap();
  }

  private static EasyModelRenderable renderable(String version, int animationState) {
    return new EasyModelRenderable() {
      @Override
      public ResourceLocation getEasyModelProfileId() {
        return PROFILE_ID;
      }

      @Override
      public ResourceLocation getEasyModelRenderProfileId() {
        return RENDER_PROFILE_ID;
      }

      @Override
      public String getEasyModelVersion() {
        return version;
      }

      @Override
      public int getEasyModelAnimationState() {
        return animationState;
      }
    };
  }

  private static EasyModelProfileService profileService(EasyModelEntityProfile profile) {
    return new EasyModelProfileService() {
      @Override
      public Optional<EasyModelEntityProfile> getProfile(ResourceLocation profileId) {
        return PROFILE_ID.equals(profileId) ? Optional.of(profile) : Optional.empty();
      }
    };
  }

  private static EasyModelRenderProfileService renderProfileService(
      EasyModelRenderProfile renderProfile) {
    return new EasyModelRenderProfileService() {
      @Override
      public Optional<EasyModelRenderProfile> getRenderProfile(ResourceLocation renderProfileId) {
        return RENDER_PROFILE_ID.equals(renderProfileId)
            ? Optional.of(renderProfile)
            : Optional.empty();
      }
    };
  }

  private static EasyModelEntityProfile profile(ModelBodyType bodyType) {
    return new EasyModelEntityProfile(
        PROFILE_ID,
        "0.1.0",
        "server-v1",
        ModelType.BLOCK_ENTITY,
        null,
        new ModelBlockEntitySettings(
            ModelBlockEntityTypeIds.ANIMATED_BLOCK_ENTITY,
            ModelBlockEntityPresetType.ANIMATED,
            bodyType),
        new ModelClientSettings(RENDER_PROFILE_ID),
        new ModelDimensions(1.0f, 1.5f, 0.5f),
        new ModelMovementSettings(0.0f, 0.0f, false),
        new ModelBehaviorSettings(ModelBehaviorMode.STATIC, false, false),
        new ModelAttributes(10.0f, 0.0f, 16.0f),
        ModelProfileStatus.ACTIVE,
        List.of());
  }

  private static EasyModelRenderProfile renderProfile(ModelBodyType bodyType) {
    return new EasyModelRenderProfile(
        RENDER_PROFILE_ID,
        "0.1.0",
        "client-v1",
        bodyType,
        new ResourceLocation("example", "easy_model_entities/models/bell"),
        new ResourceLocation("example", "textures/block/bell.png"),
        new ModelRenderSettings(1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 0.5f, 0.0f),
        new ModelAnimationSettings(ModelAnimationMode.AUTOMATIC, "", "", "", "", "", 1.0f, 1.0f),
        ModelRenderProfileStatus.ACTIVE,
        List.of());
  }

  @Test
  void createsDelegateForRenderableBlockEntityType() {
    EasyModelBlockEntityRenderDelegate<RenderableBlockEntity> delegate =
        EasyModelEntitiesClientApi.createBlockEntityRenderDelegate();

    assertNotNull(delegate);
  }

  @Test
  void runtimeContractUsesActiveBlockEntityProfileWhenAvailable() {
    EasyModelRuntimeContract contract =
        EasyModelBlockEntityRenderBackend.runtimeContract(
            renderable("client-v1", EasyModelAnimationStates.IDLE),
            profileService(profile(ModelBodyType.BIPED)),
            EasyModelRenderProfileService.EMPTY);

    assertEquals(PROFILE_ID, contract.profileId());
    assertEquals(RENDER_PROFILE_ID, contract.renderProfileId());
    assertEquals("server-v1", contract.version());
    assertEquals(ModelBodyType.BIPED, contract.bodyType());
    assertEquals(1.0f, contract.width());
    assertEquals(1.5f, contract.height());
    assertEquals(EasyModelAnimationState.IDLE, contract.animationState());
  }

  @Test
  void runtimeContractUsesRenderProfileBodyTypeWhenServerProfileIsMissing() {
    EasyModelRuntimeContract contract =
        EasyModelBlockEntityRenderBackend.runtimeContract(
            renderable("client-v1", EasyModelAnimationStates.AUTO),
            EasyModelProfileService.EMPTY,
            renderProfileService(renderProfile(ModelBodyType.QUADRUPED)));

    assertEquals(ModelBodyType.QUADRUPED, contract.bodyType());
    assertEquals(1.0f, contract.width());
    assertEquals(1.0f, contract.height());
    assertEquals(0.5f, contract.eyeHeight());
  }

  private abstract static class RenderableBlockEntity extends BlockEntity
      implements EasyModelRenderable {

    protected RenderableBlockEntity(
        BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
      super(blockEntityType, blockPos, blockState);
    }
  }
}
