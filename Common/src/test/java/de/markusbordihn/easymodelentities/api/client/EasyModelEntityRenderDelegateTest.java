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
import de.markusbordihn.easymodelentities.api.data.client.EasyModelEntityRenderOptions;
import de.markusbordihn.easymodelentities.api.data.client.EasyModelPartAnimationMode;
import de.markusbordihn.easymodelentities.api.data.client.EasyModelPartDefinition;
import de.markusbordihn.easymodelentities.api.data.client.EasyModelPartTransform;
import de.markusbordihn.easymodelentities.client.render.EasyModelEntityRenderBackend;
import de.markusbordihn.easymodelentities.data.model.Vec3f;
import de.markusbordihn.easymodelentities.data.model.bake.BakedModel;
import de.markusbordihn.easymodelentities.data.model.bake.BakedModelPart;
import de.markusbordihn.easymodelentities.data.profile.EasyModelEntityProfile;
import de.markusbordihn.easymodelentities.data.profile.ModelAttributes;
import de.markusbordihn.easymodelentities.data.profile.ModelBehaviorMode;
import de.markusbordihn.easymodelentities.data.profile.ModelBehaviorSettings;
import de.markusbordihn.easymodelentities.data.profile.ModelBodyType;
import de.markusbordihn.easymodelentities.data.profile.ModelClientSettings;
import de.markusbordihn.easymodelentities.data.profile.ModelDimensions;
import de.markusbordihn.easymodelentities.data.profile.ModelEntitySettings;
import de.markusbordihn.easymodelentities.data.profile.ModelMovementSettings;
import de.markusbordihn.easymodelentities.data.profile.ModelMovementType;
import de.markusbordihn.easymodelentities.data.profile.ModelProfileStatus;
import de.markusbordihn.easymodelentities.data.profile.ModelType;
import de.markusbordihn.easymodelentities.data.render.EasyModelRenderState;
import de.markusbordihn.easymodelentities.data.renderprofile.EasyModelRenderProfile;
import de.markusbordihn.easymodelentities.data.renderprofile.ModelAnimationMode;
import de.markusbordihn.easymodelentities.data.renderprofile.ModelAnimationSettings;
import de.markusbordihn.easymodelentities.data.renderprofile.ModelRenderProfileStatus;
import de.markusbordihn.easymodelentities.data.renderprofile.ModelRenderSettings;
import de.markusbordihn.easymodelentities.profile.EasyModelProfileService;
import de.markusbordihn.easymodelentities.registry.ModelEntityTypeIds;
import de.markusbordihn.easymodelentities.renderprofile.EasyModelRenderProfileService;
import de.markusbordihn.easymodelentities.runtime.EasyModelAnimationState;
import de.markusbordihn.easymodelentities.runtime.EasyModelRuntimeContract;
import java.util.List;
import java.util.Optional;
import net.minecraft.SharedConstants;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.level.Level;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;

class EasyModelEntityRenderDelegateTest {

  private static final ResourceLocation PROFILE_ID = new ResourceLocation("example", "mimic");
  private static final ResourceLocation RENDER_PROFILE_ID =
      new ResourceLocation("example", "mimic_render");

  @BeforeAll
  static void bootstrapMinecraft() {
    SharedConstants.tryDetectVersion();
    Bootstrap.bootStrap();
  }

  private static Entity entity(float width, float height, float eyeHeight) {
    Entity entity = mock(Entity.class);
    when(entity.getBbWidth()).thenReturn(width);
    when(entity.getBbHeight()).thenReturn(height);
    when(entity.getEyeHeight()).thenReturn(eyeHeight);
    return entity;
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
        ModelType.ENTITY,
        new ModelEntitySettings(
            ModelEntityTypeIds.GROUND_ENTITY, ModelMovementType.GROUND, bodyType),
        null,
        new ModelClientSettings(RENDER_PROFILE_ID),
        new ModelDimensions(0.6f, 0.8f, 0.5f),
        new ModelMovementSettings(0.22f, 0.6f, true),
        new ModelBehaviorSettings(ModelBehaviorMode.IDLE_ONLY, true, false),
        new ModelAttributes(10.0f, 0.22f, 16.0f),
        ModelProfileStatus.ACTIVE,
        List.of());
  }

  private static EasyModelRenderProfile renderProfile(ModelBodyType bodyType) {
    return new EasyModelRenderProfile(
        RENDER_PROFILE_ID,
        "0.1.0",
        "client-v1",
        bodyType,
        new ResourceLocation("example", "easy_model_entities/models/mimic"),
        new ResourceLocation("example", "textures/entity/mimic.png"),
        new ModelRenderSettings(1.0f, 0.3f, 1.0f, 1.0f, 0.0f, 0.5f, 0.0f),
        new ModelAnimationSettings(ModelAnimationMode.AUTOMATIC, "", "", "", "", "", 1.0f, 1.0f),
        ModelRenderProfileStatus.ACTIVE,
        List.of());
  }

  private static EasyModelRenderState renderState(BakedModel bakedModel) {
    return new EasyModelRenderState(
        bakedModel,
        new ResourceLocation("example", "textures/entity/mimic.png"),
        1.0f,
        0.3f,
        ModelBodyType.STATIC,
        new ModelAnimationSettings(ModelAnimationMode.NONE, "", "", "", "", "", 1.0f, 1.0f),
        false,
        false,
        List.of());
  }

  @Test
  void createsDelegateForMimicsStyleEntityType() {
    EasyModelEntityRenderDelegate<MimicsStyleEntity> delegate =
        EasyModelEntitiesClientApi.createRenderDelegate();

    assertNotNull(delegate);
  }

  @Test
  void createsEntityRenderOptionsWithCustomAnimator() {
    EasyModelEntityRenderOptions options =
        EasyModelEntityRenderOptions.DEFAULT
            .withAnimationTicks(12.0f)
            .withPartAnimator(context -> EasyModelPartTransform.NONE)
            .withPartAnimationMode(EasyModelPartAnimationMode.REPLACE);

    assertEquals(12.0f, options.animationTicks(), 0.0001f);
    assertNotNull(options.partAnimator());
    assertEquals(EasyModelPartAnimationMode.REPLACE, options.partAnimationMode());
  }

  @Test
  void renderBackendUsesDefaultOptionsWhenOptionsAreNull() {
    Entity entity = entity(0.9f, 1.2f, 0.8f);
    BakedModel bakedModel =
        new BakedModel(new ResourceLocation("example", "empty"), 64, 64, List.of());
    MultiBufferSource bufferSource = mock(MultiBufferSource.class);
    VertexConsumer vertexConsumer = mock(VertexConsumer.class, Answers.RETURNS_SELF);
    when(bufferSource.getBuffer(any())).thenReturn(vertexConsumer);

    assertDoesNotThrow(
        () ->
            EasyModelEntityRenderBackend.render(
                entity,
                renderState(bakedModel),
                0.0f,
                0.0f,
                null,
                new PoseStack(),
                bufferSource,
                0));
  }

  @Test
  void partDefinitionsCanBeFlattenedForEntities() {
    EasyModelPartDefinition root =
        EasyModelPartDefinitions.fromBakedPart(
            new BakedModelPart(
                "root",
                Vec3f.ZERO,
                Vec3f.ZERO,
                List.of(),
                List.of(
                    new BakedModelPart(
                        "head", new Vec3f(0.0f, 1.0f, 0.0f), Vec3f.ZERO, List.of(), List.of()))));

    List<EasyModelPartDefinition> flattenedParts = EasyModelPartDefinitions.flatten(List.of(root));

    assertEquals(
        List.of("root", "head"),
        flattenedParts.stream().map(EasyModelPartDefinition::name).toList());
  }

  @Test
  void runtimeContractUsesActiveServerProfileWhenAvailable() {
    Entity entity = entity(0.9f, 1.2f, 0.8f);
    EasyModelRuntimeContract contract =
        EasyModelEntityRenderBackend.runtimeContract(
            entity,
            renderable("client-v1", EasyModelAnimationStates.RUN),
            profileService(profile(ModelBodyType.BIPED)),
            EasyModelRenderProfileService.EMPTY);

    assertEquals(PROFILE_ID, contract.profileId());
    assertEquals(RENDER_PROFILE_ID, contract.renderProfileId());
    assertEquals("server-v1", contract.version());
    assertEquals(ModelBodyType.BIPED, contract.bodyType());
    assertEquals(0.6f, contract.width());
    assertEquals(0.8f, contract.height());
    assertEquals(0.5f, contract.eyeHeight());
    assertEquals(EasyModelAnimationState.RUN, contract.animationState());
  }

  @Test
  void runtimeContractUsesRenderProfileBodyTypeWhenServerProfileIsMissing() {
    Entity entity = entity(0.9f, 1.2f, 0.8f);
    EasyModelRuntimeContract contract =
        EasyModelEntityRenderBackend.runtimeContract(
            entity,
            renderable("client-v1", EasyModelAnimationStates.WALK),
            EasyModelProfileService.EMPTY,
            renderProfileService(renderProfile(ModelBodyType.QUADRUPED)));

    assertEquals(PROFILE_ID, contract.profileId());
    assertEquals(RENDER_PROFILE_ID, contract.renderProfileId());
    assertEquals("client-v1", contract.version());
    assertEquals(ModelBodyType.QUADRUPED, contract.bodyType());
    assertEquals(0.9f, contract.width());
    assertEquals(1.2f, contract.height());
    assertEquals(0.8f, contract.eyeHeight());
    assertEquals(EasyModelAnimationState.WALK, contract.animationState());
  }

  @Test
  void runtimeContractFallsBackToStaticBodyTypeForMissingRenderProfile() {
    Entity entity = entity(0.9f, 1.2f, 0.8f);
    EasyModelRuntimeContract contract =
        EasyModelEntityRenderBackend.runtimeContract(
            entity,
            renderable("", 999),
            EasyModelProfileService.EMPTY,
            EasyModelRenderProfileService.EMPTY);

    assertEquals(ModelBodyType.STATIC, contract.bodyType());
    assertEquals("", contract.version());
    assertEquals(EasyModelAnimationState.AUTO, contract.animationState());
  }

  private abstract static class MimicsStyleEntity extends PathfinderMob
      implements EasyModelRenderable {

    protected MimicsStyleEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
      super(entityType, level);
    }
  }
}
