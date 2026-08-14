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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import de.markusbordihn.easymodelentities.api.EasyModelRenderable;
import de.markusbordihn.easymodelentities.api.data.EasyModelAnimation;
import de.markusbordihn.easymodelentities.api.data.EasyModelAnimationSetting;
import de.markusbordihn.easymodelentities.api.data.client.EasyModelAnimationPlayback;
import de.markusbordihn.easymodelentities.api.data.client.EasyModelAnimationTransition;
import de.markusbordihn.easymodelentities.api.data.client.EasyModelBlockEntityRenderOptions;
import de.markusbordihn.easymodelentities.api.data.client.EasyModelPartAnimationContext;
import de.markusbordihn.easymodelentities.api.data.client.EasyModelPartAnimationMode;
import de.markusbordihn.easymodelentities.api.data.client.EasyModelPartDefinition;
import de.markusbordihn.easymodelentities.api.data.client.EasyModelPartTransform;
import de.markusbordihn.easymodelentities.client.render.EasyModelBlockEntityRenderBackend;
import de.markusbordihn.easymodelentities.data.model.ModelAnimationBoneTrack;
import de.markusbordihn.easymodelentities.data.model.ModelAnimationClip;
import de.markusbordihn.easymodelentities.data.model.ModelAnimationKeyframe;
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
import de.markusbordihn.easymodelentities.runtime.EasyModelRuntimeContract;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import net.minecraft.SharedConstants;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
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

  private static EasyModelRenderable renderable(
      String version, EasyModelAnimationSetting animation) {
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
      public EasyModelAnimationSetting getEasyModelAnimationSetting() {
        return animation;
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

  private static EasyModelRenderState animatedRenderState(BakedModel bakedModel) {
    return new EasyModelRenderState(
        bakedModel,
        ResourceLocation.fromNamespaceAndPath("example", "textures/block/bell.png"),
        1.0f,
        0.0f,
        ModelBodyType.BIPED,
        new ModelAnimationSettings(ModelAnimationMode.AUTOMATIC, 1.0f, 1.0f),
        false,
        false,
        List.of());
  }

  private static BakedModel timedAnimatedBakedModel() {
    ModelAnimationBoneTrack track =
        new ModelAnimationBoneTrack(
            List.of(
                new ModelAnimationKeyframe(0.0f, Vec3f.ZERO, false),
                new ModelAnimationKeyframe(1.0f, new Vec3f(1.0f, 0.0f, 0.0f), false)),
            List.of());
    ModelAnimationClip clip = new ModelAnimationClip("talk", 1.0f, false, Map.of("body", track));
    return new BakedModel(
        ResourceLocation.fromNamespaceAndPath("example", "timed_bell"),
        64,
        64,
        List.of(new BakedModelPart("body", Vec3f.ZERO, Vec3f.ZERO, List.of(), List.of())),
        Map.of(),
        false,
        Map.of("talk", clip),
        null);
  }

  private static float renderNamedClipRotation(
      RenderableBlockEntity blockEntity, BakedModel bakedModel) {
    MultiBufferSource bufferSource = mock(MultiBufferSource.class);
    VertexConsumer vertexConsumer = mock(VertexConsumer.class, Answers.RETURNS_SELF);
    when(bufferSource.getBuffer(any())).thenReturn(vertexConsumer);
    AtomicReference<EasyModelPartAnimationContext> context = new AtomicReference<>();
    EasyModelBlockEntityRenderOptions options =
        EasyModelBlockEntityRenderOptions.DEFAULT.withPartAnimator(
            animationContext -> {
              context.set(animationContext);
              return EasyModelPartTransform.NONE;
            });

    EasyModelBlockEntityRenderBackend.render(
        blockEntity,
        animatedRenderState(bakedModel),
        0.0f,
        options,
        new PoseStack(),
        bufferSource,
        0);
    return context.get().automaticTransform().xRotation();
  }

  private static float renderAnimationTicks(
      RenderableBlockEntity blockEntity, BakedModel bakedModel) {
    MultiBufferSource bufferSource = mock(MultiBufferSource.class);
    VertexConsumer vertexConsumer = mock(VertexConsumer.class, Answers.RETURNS_SELF);
    when(bufferSource.getBuffer(any())).thenReturn(vertexConsumer);
    AtomicReference<EasyModelPartAnimationContext> context = new AtomicReference<>();
    EasyModelBlockEntityRenderOptions options =
        EasyModelBlockEntityRenderOptions.DEFAULT.withPartAnimator(
            animationContext -> {
              context.set(animationContext);
              return EasyModelPartTransform.NONE;
            });

    EasyModelBlockEntityRenderBackend.render(
        blockEntity,
        animatedRenderState(bakedModel),
        0.0f,
        options,
        new PoseStack(),
        bufferSource,
        0);
    return context.get().ageInTicks();
  }

  @Test
  @DisplayName("A finished playback must not rewind the animation clock")
  void animationTicksNeverRewindAfterPlaybackCompletes() {
    RenderableBlockEntity blockEntity =
        mock(RenderableBlockEntity.class, Answers.CALLS_REAL_METHODS);
    when(blockEntity.getEasyModelAnimationSetting())
        .thenReturn(EasyModelAnimationSetting.of(EasyModelAnimation.named("talk")));
    Level level = mock(Level.class);
    blockEntity.setLevel(level);
    BakedModel bakedModel = timedAnimatedBakedModel();

    when(level.getGameTime()).thenReturn(1_000L);
    renderAnimationTicks(blockEntity, bakedModel);
    EasyModelBlockEntityRenderBackend.playAnimation(
        blockEntity,
        EasyModelAnimation.named("talk"),
        EasyModelAnimationPlayback.DEFAULT,
        EasyModelAnimationTransition.IMMEDIATE);

    float previousTicks = -1.0f;
    for (long gameTime = 1_001L; gameTime <= 1_120L; gameTime += 5L) {
      when(level.getGameTime()).thenReturn(gameTime);
      float animationTicks = renderAnimationTicks(blockEntity, bakedModel);
      assertTrue(
          animationTicks >= 0.0f && animationTicks < 200.0f,
          "animationTicks left its expected range: " + animationTicks);
      previousTicks = animationTicks;
    }
    assertTrue(previousTicks >= 0.0f);
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
  void renderOptionsExposeOnlyValidatedFactories() {
    assertEquals(0, EasyModelBlockEntityRenderOptions.class.getConstructors().length);
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
  void nullRenderableAnimationFallsBackToAutomaticSelection() {
    RenderableBlockEntity blockEntity =
        mock(RenderableBlockEntity.class, Answers.CALLS_REAL_METHODS);
    when(blockEntity.getEasyModelAnimationSetting()).thenReturn(null);
    BakedModel bakedModel =
        new BakedModel(
            ResourceLocation.fromNamespaceAndPath("example", "empty"), 64, 64, List.of());
    MultiBufferSource bufferSource = mock(MultiBufferSource.class);
    when(bufferSource.getBuffer(any()))
        .thenReturn(mock(VertexConsumer.class, Answers.RETURNS_SELF));

    assertDoesNotThrow(
        () ->
            EasyModelBlockEntityRenderBackend.render(
                blockEntity,
                renderState(bakedModel),
                0.0f,
                EasyModelBlockEntityRenderOptions.DEFAULT,
                new PoseStack(),
                bufferSource,
                0));
  }

  @Test
  void namedClipAdvancesForExternalBlockEntity() {
    RenderableBlockEntity blockEntity =
        mock(RenderableBlockEntity.class, Answers.CALLS_REAL_METHODS);
    when(blockEntity.getEasyModelAnimationSetting())
        .thenReturn(EasyModelAnimationSetting.of(EasyModelAnimation.named("talk")));
    Level level = mock(Level.class);
    blockEntity.setLevel(level);
    BakedModel bakedModel = timedAnimatedBakedModel();

    when(level.getGameTime()).thenReturn(100L);
    assertEquals(0.0f, renderNamedClipRotation(blockEntity, bakedModel), 0.0001f);

    when(level.getGameTime()).thenReturn(110L);
    assertEquals(0.5f, renderNamedClipRotation(blockEntity, bakedModel), 0.0001f);
  }

  @Test
  void runtimeContractUsesActiveBlockEntityProfileWhenAvailable() {
    EasyModelRuntimeContract contract =
        EasyModelBlockEntityRenderBackend.runtimeContract(
            renderable("client-v1", EasyModelAnimationSetting.of(EasyModelAnimation.IDLE)),
            profileService(profile(ModelBodyType.BIPED)),
            EasyModelRenderProfileService.EMPTY);

    assertEquals(PROFILE_ID, contract.profileId());
    assertEquals(RENDER_PROFILE_ID, contract.renderProfileId());
    assertEquals("server-v1", contract.version());
    assertEquals(ModelBodyType.BIPED, contract.bodyType());
    assertEquals(1.0f, contract.width());
    assertEquals(1.5f, contract.height());
    assertEquals(EasyModelAnimationSetting.of(EasyModelAnimation.IDLE), contract.animation());
  }

  @Test
  void runtimeContractUsesRenderProfileBodyTypeWhenServerProfileIsMissing() {
    EasyModelRuntimeContract contract =
        EasyModelBlockEntityRenderBackend.runtimeContract(
            renderable("client-v1", EasyModelAnimationSetting.of(EasyModelAnimation.AUTO)),
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
