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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import de.markusbordihn.easymodelentities.api.EasyModelAnimationStates;
import de.markusbordihn.easymodelentities.api.EasyModelRenderable;
import de.markusbordihn.easymodelentities.api.data.client.EasyModelBlockEntityRenderOptions;
import de.markusbordihn.easymodelentities.api.data.client.EasyModelPartAnimationMode;
import de.markusbordihn.easymodelentities.api.data.client.EasyModelPartDefinition;
import de.markusbordihn.easymodelentities.api.data.client.EasyModelPartTransform;
import de.markusbordihn.easymodelentities.client.render.EasyModelBlockEntityRenderBackend;
import de.markusbordihn.easymodelentities.data.model.Vec3f;
import de.markusbordihn.easymodelentities.data.model.bake.BakedModel;
import de.markusbordihn.easymodelentities.data.model.bake.BakedModelPart;
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
import de.markusbordihn.easymodelentities.data.render.EasyModelRenderState;
import de.markusbordihn.easymodelentities.data.renderprofile.EasyModelRenderProfile;
import de.markusbordihn.easymodelentities.data.renderprofile.ModelAnimationMode;
import de.markusbordihn.easymodelentities.data.renderprofile.ModelAnimationSettings;
import de.markusbordihn.easymodelentities.data.renderprofile.ModelRenderProfileStatus;
import de.markusbordihn.easymodelentities.data.renderprofile.ModelRenderSettings;
import de.markusbordihn.easymodelentities.profile.EasyModelProfileService;
import de.markusbordihn.easymodelentities.registry.ModelBlockEntityTypeIds;
import de.markusbordihn.easymodelentities.renderprofile.EasyModelRenderProfileService;
import de.markusbordihn.easymodelentities.runtime.EasyModelAnimationState;
import de.markusbordihn.easymodelentities.runtime.EasyModelRuntimeContract;
import java.util.List;
import java.util.Optional;
import net.minecraft.SharedConstants;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;

class EasyModelBlockEntityRenderDelegateTest {

  private static final ResourceLocation PROFILE_ID =
      ResourceLocation.fromNamespaceAndPath("example", "bell");
  private static final ResourceLocation RENDER_PROFILE_ID =
      ResourceLocation.fromNamespaceAndPath("example", "bell_render");

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
        ResourceLocation.fromNamespaceAndPath("example", "easy_model_entities/models/bell"),
        ResourceLocation.fromNamespaceAndPath("example", "textures/block/bell.png"),
        new ModelRenderSettings(1.0f, 0.0f, 0.0f, 0.0f, Vec3f.ZERO),
        new ModelAnimationSettings(ModelAnimationMode.AUTOMATIC, 1.0f, 1.0f),
        ModelRenderProfileStatus.ACTIVE,
        List.of());
  }

  private static EasyModelRenderState renderState(BakedModel bakedModel) {
    return new EasyModelRenderState(
        bakedModel,
        ResourceLocation.fromNamespaceAndPath("example", "textures/block/bell.png"),
        1.0f,
        0.0f,
        ModelBodyType.STATIC,
        new ModelAnimationSettings(ModelAnimationMode.NONE, 1.0f, 1.0f),
        false,
        false,
        List.of());
  }

  @Test
  void createsDelegateForRenderableBlockEntityType() {
    EasyModelBlockEntityRenderDelegate<RenderableBlockEntity> delegate =
        EasyModelEntitiesClientApi.createBlockEntityRenderDelegate();

    assertNotNull(delegate);
  }

  @Test
  void createsBlockEntityRenderOptionsWithCustomAnimatorMode() {
    EasyModelBlockEntityRenderOptions options =
        EasyModelBlockEntityRenderOptions.DEFAULT
            .withYawDegrees(90.0f)
            .withAnimationTicks(12.0f)
            .withPartAnimator(context -> EasyModelPartTransform.NONE)
            .withPartAnimationMode(EasyModelPartAnimationMode.REPLACE);

    assertEquals(90.0f, options.yawDegrees(), 0.0001f);
    assertEquals(12.0f, options.animationTicks(), 0.0001f);
    assertNotNull(options.partAnimator());
    assertEquals(EasyModelPartAnimationMode.REPLACE, options.partAnimationMode());
  }

  @Test
  void renderBackendUsesDefaultOptionsWhenOptionsAreNull() {
    RenderableBlockEntity blockEntity =
        mock(RenderableBlockEntity.class, Answers.CALLS_REAL_METHODS);
    BakedModel bakedModel =
        new BakedModel(
            ResourceLocation.fromNamespaceAndPath("example", "empty"), 64, 64, List.of());
    MultiBufferSource bufferSource = mock(MultiBufferSource.class);
    VertexConsumer vertexConsumer = mock(VertexConsumer.class, Answers.RETURNS_SELF);
    when(bufferSource.getBuffer(any())).thenReturn(vertexConsumer);

    assertDoesNotThrow(
        () ->
            EasyModelBlockEntityRenderBackend.render(
                blockEntity,
                renderState(bakedModel),
                0.0f,
                null,
                new PoseStack(),
                bufferSource,
                0));
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

  @Test
  void partDefinitionsCanBeFlattened() {
    EasyModelPartDefinition root =
        EasyModelPartDefinitions.fromBakedPart(
            new BakedModelPart(
                "root",
                Vec3f.ZERO,
                Vec3f.ZERO,
                List.of(),
                List.of(
                    new BakedModelPart(
                        "crystal",
                        new Vec3f(0.0f, 1.0f, 0.0f),
                        Vec3f.ZERO,
                        List.of(),
                        List.of()))));

    List<EasyModelPartDefinition> flattenedParts = EasyModelPartDefinitions.flatten(List.of(root));

    assertEquals(
        List.of("root", "crystal"),
        flattenedParts.stream().map(EasyModelPartDefinition::name).toList());
  }

  private abstract static class RenderableBlockEntity extends BlockEntity
      implements EasyModelRenderable {

    protected RenderableBlockEntity(
        BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
      super(blockEntityType, blockPos, blockState);
    }
  }
}
