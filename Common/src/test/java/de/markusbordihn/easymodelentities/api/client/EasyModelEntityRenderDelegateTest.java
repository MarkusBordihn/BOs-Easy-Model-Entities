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
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import de.markusbordihn.easymodelentities.api.EasyModelRenderable;
import de.markusbordihn.easymodelentities.api.data.EasyModelAnimation;
import de.markusbordihn.easymodelentities.api.data.EasyModelAnimationSetting;
import de.markusbordihn.easymodelentities.api.data.client.EasyModelEntityRenderOptions;
import de.markusbordihn.easymodelentities.api.data.client.EasyModelPartAnimationContext;
import de.markusbordihn.easymodelentities.api.data.client.EasyModelPartAnimationMode;
import de.markusbordihn.easymodelentities.api.data.client.EasyModelPartDefinition;
import de.markusbordihn.easymodelentities.api.data.client.EasyModelPartTransform;
import de.markusbordihn.easymodelentities.client.render.EasyModelEntityRenderBackend;
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
import de.markusbordihn.easymodelentities.runtime.EasyModelRuntimeContract;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import net.minecraft.SharedConstants;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.resources.Identifier;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.level.Level;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;

class EasyModelEntityRenderDelegateTest {

  private static final Identifier PROFILE_ID = Identifier.fromNamespaceAndPath("example", "mimic");
  private static final Identifier RENDER_PROFILE_ID =
      Identifier.fromNamespaceAndPath("example", "mimic_render");

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

  private static EasyModelRenderable renderable(
      String version, EasyModelAnimationSetting animation) {
    return new EasyModelRenderable() {
      @Override
      public Identifier getEasyModelProfileId() {
        return PROFILE_ID;
      }

      @Override
      public Identifier getEasyModelRenderProfileId() {
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
      public Optional<EasyModelEntityProfile> getProfile(Identifier profileId) {
        return PROFILE_ID.equals(profileId) ? Optional.of(profile) : Optional.empty();
      }
    };
  }

  private static EasyModelRenderProfileService renderProfileService(
      EasyModelRenderProfile renderProfile) {
    return new EasyModelRenderProfileService() {
      @Override
      public Optional<EasyModelRenderProfile> getRenderProfile(Identifier renderProfileId) {
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
        Identifier.fromNamespaceAndPath("example", "easy_model_entities/models/mimic"),
        Identifier.fromNamespaceAndPath("example", "textures/entity/mimic.png"),
        new ModelRenderSettings(1.0f, 0.3f, 0.0f, 0.0f, Vec3f.ZERO),
        new ModelAnimationSettings(ModelAnimationMode.AUTOMATIC, 1.0f, 1.0f),
        ModelRenderProfileStatus.ACTIVE,
        List.of());
  }

  private static EasyModelRenderState renderState(BakedModel bakedModel) {
    return new EasyModelRenderState(
        bakedModel,
        Identifier.fromNamespaceAndPath("example", "textures/entity/mimic.png"),
        1.0f,
        0.3f,
        ModelBodyType.STATIC,
        new ModelAnimationSettings(ModelAnimationMode.NONE, 1.0f, 1.0f),
        false,
        false,
        List.of());
  }

  private static BakedModel animatedBakedModel() {
    ModelAnimationBoneTrack talkTrack =
        new ModelAnimationBoneTrack(
            List.of(new ModelAnimationKeyframe(0.0f, new Vec3f(0.5f, 0.0f, 0.0f), false)),
            List.of());
    ModelAnimationBoneTrack idleTrack =
        new ModelAnimationBoneTrack(
            List.of(new ModelAnimationKeyframe(0.0f, new Vec3f(0.2f, 0.0f, 0.0f), false)),
            List.of());
    ModelAnimationClip talkClip =
        new ModelAnimationClip("talk", 1.0f, true, Map.of("body", talkTrack));
    ModelAnimationClip idleClip =
        new ModelAnimationClip("idle", 1.0f, true, Map.of("body", idleTrack));
    return new BakedModel(
        Identifier.fromNamespaceAndPath("example", "talker"),
        64,
        64,
        List.of(new BakedModelPart("body", Vec3f.ZERO, Vec3f.ZERO, List.of(), List.of())),
        Map.of(),
        false,
        Map.of("talk", talkClip, "idle", idleClip),
        null);
  }

  private static BakedModel timedAnimatedBakedModel() {
    return new BakedModel(
        Identifier.fromNamespaceAndPath("example", "timed_talker"),
        64,
        64,
        List.of(new BakedModelPart("body", Vec3f.ZERO, Vec3f.ZERO, List.of(), List.of())),
        Map.of(),
        false,
        Map.of("talk", timedClip("talk", 0.0f), "wave", timedClip("wave", 2.0f)),
        null);
  }

  private static ModelAnimationClip timedClip(String name, float startRotation) {
    ModelAnimationBoneTrack track =
        new ModelAnimationBoneTrack(
            List.of(
                new ModelAnimationKeyframe(0.0f, new Vec3f(startRotation, 0.0f, 0.0f), false),
                new ModelAnimationKeyframe(
                    1.0f, new Vec3f(startRotation + 1.0f, 0.0f, 0.0f), false)),
            List.of());
    return new ModelAnimationClip(name, 1.0f, false, Map.of("body", track));
  }

  private static EasyModelRenderState animatedRenderState(BakedModel bakedModel) {
    return new EasyModelRenderState(
        bakedModel,
        Identifier.fromNamespaceAndPath("example", "textures/entity/talker.png"),
        1.0f,
        0.3f,
        ModelBodyType.BIPED,
        new ModelAnimationSettings(ModelAnimationMode.AUTOMATIC, 1.0f, 1.0f),
        false,
        false,
        List.of());
  }

  private static Entity renderableEntity(String animation) {
    Entity entity = mock(Entity.class, withSettings().extraInterfaces(EasyModelRenderable.class));
    when(((EasyModelRenderable) entity).getEasyModelAnimationSetting())
        .thenReturn(EasyModelAnimationSetting.of(EasyModelAnimation.named(animation)));
    return entity;
  }

  private static SubmitNodeCollector submitNodeCollectorRunningGeometry() {
    SubmitNodeCollector submitNodeCollector = mock(SubmitNodeCollector.class);
    doAnswer(
            invocation -> {
              SubmitNodeCollector.CustomGeometryRenderer geometryRenderer =
                  invocation.getArgument(2);
              geometryRenderer.render(
                  new PoseStack().last(), mock(VertexConsumer.class, Answers.RETURNS_SELF));
              return null;
            })
        .when(submitNodeCollector)
        .submitCustomGeometry(any(), any(), any());
    return submitNodeCollector;
  }

  private static float renderNamedClipRotation(
      Entity entity, BakedModel bakedModel, float partialTick) {
    SubmitNodeCollector submitNodeCollector = submitNodeCollectorRunningGeometry();
    AtomicReference<EasyModelPartAnimationContext> context = new AtomicReference<>();
    EasyModelEntityRenderOptions options =
        EasyModelEntityRenderOptions.DEFAULT.withPartAnimator(
            animationContext -> {
              context.set(animationContext);
              return EasyModelPartTransform.NONE;
            });

    EasyModelEntityRenderBackend.render(
        entity,
        animatedRenderState(bakedModel),
        0.0f,
        partialTick,
        options,
        new PoseStack(),
        submitNodeCollector,
        0);
    return context.get().automaticTransform().xRotation();
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
        new BakedModel(Identifier.fromNamespaceAndPath("example", "empty"), 64, 64, List.of());
    SubmitNodeCollector submitNodeCollector = submitNodeCollectorRunningGeometry();

    assertDoesNotThrow(
        () ->
            EasyModelEntityRenderBackend.render(
                entity,
                renderState(bakedModel),
                0.0f,
                0.0f,
                null,
                new PoseStack(),
                submitNodeCollector,
                0));
  }

  @Test
  void entityAnimationGetterForcesNamedClipWhenOptionsOmitAnimation() {
    BakedModel bakedModel = animatedBakedModel();
    Entity entity = renderableEntity("talk");
    SubmitNodeCollector submitNodeCollector = submitNodeCollectorRunningGeometry();
    AtomicReference<EasyModelPartAnimationContext> context = new AtomicReference<>();
    EasyModelEntityRenderOptions options =
        EasyModelEntityRenderOptions.DEFAULT.withPartAnimator(
            animationContext -> {
              context.set(animationContext);
              return EasyModelPartTransform.NONE;
            });

    EasyModelEntityRenderBackend.render(
        entity,
        animatedRenderState(bakedModel),
        0.0f,
        0.0f,
        options,
        new PoseStack(),
        submitNodeCollector,
        0);

    assertEquals(0.5f, context.get().automaticTransform().xRotation(), 0.0001f);
  }

  @Test
  void optionsAnimationWinsOverEntityGetter() {
    BakedModel bakedModel = animatedBakedModel();
    Entity entity = renderableEntity("talk");
    SubmitNodeCollector submitNodeCollector = submitNodeCollectorRunningGeometry();
    AtomicReference<EasyModelPartAnimationContext> context = new AtomicReference<>();
    EasyModelEntityRenderOptions options =
        EasyModelEntityRenderOptions.DEFAULT
            .withAnimation("idle")
            .withPartAnimator(
                animationContext -> {
                  context.set(animationContext);
                  return EasyModelPartTransform.NONE;
                });

    EasyModelEntityRenderBackend.render(
        entity,
        animatedRenderState(bakedModel),
        0.0f,
        0.0f,
        options,
        new PoseStack(),
        submitNodeCollector,
        0);

    assertEquals(0.2f, context.get().automaticTransform().xRotation(), 0.0001f);
  }

  @Test
  void namedNonLoopingClipStartsAtZeroAndRestartsWhenNameChanges() {
    AtomicReference<String> animation = new AtomicReference<>("talk");
    Entity entity = mock(Entity.class, withSettings().extraInterfaces(EasyModelRenderable.class));
    when(((EasyModelRenderable) entity).getEasyModelAnimationSetting())
        .thenAnswer(
            call -> EasyModelAnimationSetting.of(EasyModelAnimation.named(animation.get())));
    BakedModel bakedModel = timedAnimatedBakedModel();

    entity.tickCount = 100;
    assertEquals(0.0f, renderNamedClipRotation(entity, bakedModel, 0.0f), 0.0001f);

    entity.tickCount = 110;
    assertEquals(0.5f, renderNamedClipRotation(entity, bakedModel, 0.0f), 0.0001f);

    animation.set("wave");
    assertEquals(2.0f, renderNamedClipRotation(entity, bakedModel, 0.0f), 0.0001f);
  }

  @Test
  void nullRenderableAnimationFallsBackToAutomaticSelection() {
    Entity entity = mock(Entity.class, withSettings().extraInterfaces(EasyModelRenderable.class));
    when(((EasyModelRenderable) entity).getEasyModelAnimationSetting()).thenReturn(null);
    BakedModel bakedModel = animatedBakedModel();
    SubmitNodeCollector submitNodeCollector = submitNodeCollectorRunningGeometry();

    assertDoesNotThrow(
        () ->
            EasyModelEntityRenderBackend.render(
                entity,
                animatedRenderState(bakedModel),
                0.0f,
                0.0f,
                EasyModelEntityRenderOptions.DEFAULT,
                new PoseStack(),
                submitNodeCollector,
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
            renderable("client-v1", EasyModelAnimationSetting.of(EasyModelAnimation.RUN)),
            profileService(profile(ModelBodyType.BIPED)),
            EasyModelRenderProfileService.EMPTY);

    assertEquals(PROFILE_ID, contract.profileId());
    assertEquals(RENDER_PROFILE_ID, contract.renderProfileId());
    assertEquals("server-v1", contract.version());
    assertEquals(ModelBodyType.BIPED, contract.bodyType());
    assertEquals(0.6f, contract.width());
    assertEquals(0.8f, contract.height());
    assertEquals(0.5f, contract.eyeHeight());
    assertEquals(EasyModelAnimationSetting.of(EasyModelAnimation.RUN), contract.animation());
  }

  @Test
  void runtimeContractUsesRenderProfileBodyTypeWhenServerProfileIsMissing() {
    Entity entity = entity(0.9f, 1.2f, 0.8f);
    EasyModelRuntimeContract contract =
        EasyModelEntityRenderBackend.runtimeContract(
            entity,
            renderable("client-v1", EasyModelAnimationSetting.of(EasyModelAnimation.WALK)),
            EasyModelProfileService.EMPTY,
            renderProfileService(renderProfile(ModelBodyType.QUADRUPED)));

    assertEquals(PROFILE_ID, contract.profileId());
    assertEquals(RENDER_PROFILE_ID, contract.renderProfileId());
    assertEquals("client-v1", contract.version());
    assertEquals(ModelBodyType.QUADRUPED, contract.bodyType());
    assertEquals(0.9f, contract.width());
    assertEquals(1.2f, contract.height());
    assertEquals(0.8f, contract.eyeHeight());
    assertEquals(EasyModelAnimationSetting.of(EasyModelAnimation.WALK), contract.animation());
  }

  @Test
  void runtimeContractFallsBackToStaticBodyTypeForMissingRenderProfile() {
    Entity entity = entity(0.9f, 1.2f, 0.8f);
    EasyModelRuntimeContract contract =
        EasyModelEntityRenderBackend.runtimeContract(
            entity,
            renderable("", EasyModelAnimationSetting.of(EasyModelAnimation.named("unknown"))),
            EasyModelProfileService.EMPTY,
            EasyModelRenderProfileService.EMPTY);

    assertEquals(ModelBodyType.STATIC, contract.bodyType());
    assertEquals("", contract.version());
    assertEquals(
        EasyModelAnimationSetting.of(EasyModelAnimation.named("unknown")), contract.animation());
  }

  private abstract static class MimicsStyleEntity extends PathfinderMob
      implements EasyModelRenderable {

    protected MimicsStyleEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
      super(entityType, level);
    }
  }
}
