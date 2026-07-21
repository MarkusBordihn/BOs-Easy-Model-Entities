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

package de.markusbordihn.easymodelentities.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mojang.brigadier.CommandDispatcher;
import de.markusbordihn.easymodelentities.Constants;
import de.markusbordihn.easymodelentities.data.model.Vec3f;
import de.markusbordihn.easymodelentities.data.profile.EasyModelEntityProfile;
import de.markusbordihn.easymodelentities.data.profile.ModelAttributes;
import de.markusbordihn.easymodelentities.data.profile.ModelBehaviorMode;
import de.markusbordihn.easymodelentities.data.profile.ModelBehaviorSettings;
import de.markusbordihn.easymodelentities.data.profile.ModelBlockEntityPresetType;
import de.markusbordihn.easymodelentities.data.profile.ModelBlockEntitySettings;
import de.markusbordihn.easymodelentities.data.profile.ModelBodyType;
import de.markusbordihn.easymodelentities.data.profile.ModelClientSettings;
import de.markusbordihn.easymodelentities.data.profile.ModelDimensions;
import de.markusbordihn.easymodelentities.data.profile.ModelEntitySettings;
import de.markusbordihn.easymodelentities.data.profile.ModelMovementSettings;
import de.markusbordihn.easymodelentities.data.profile.ModelMovementType;
import de.markusbordihn.easymodelentities.data.profile.ModelProfileStatus;
import de.markusbordihn.easymodelentities.data.profile.ModelProfileValidationIssue;
import de.markusbordihn.easymodelentities.data.profile.ModelType;
import de.markusbordihn.easymodelentities.data.renderprofile.EasyModelRenderProfile;
import de.markusbordihn.easymodelentities.data.renderprofile.ModelAnimationMode;
import de.markusbordihn.easymodelentities.data.renderprofile.ModelAnimationSettings;
import de.markusbordihn.easymodelentities.data.renderprofile.ModelRenderProfileStatus;
import de.markusbordihn.easymodelentities.data.renderprofile.ModelRenderProfileValidationIssue;
import de.markusbordihn.easymodelentities.data.renderprofile.ModelRenderSettings;
import de.markusbordihn.easymodelentities.diagnostics.DefaultEasyModelDiagnosticsService;
import de.markusbordihn.easymodelentities.profile.EasyModelProfileService;
import de.markusbordihn.easymodelentities.registry.EasyModelServices;
import de.markusbordihn.easymodelentities.registry.ModelBlockEntityTypeIds;
import de.markusbordihn.easymodelentities.registry.ModelBlockIds;
import de.markusbordihn.easymodelentities.registry.ModelEntityTypeIds;
import de.markusbordihn.easymodelentities.renderprofile.EasyModelRenderProfileService;
import de.markusbordihn.easymodelentities.runtime.EasyModelAnimationState;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class EasyModelEntitiesCommandTest {

  private static final ResourceLocation ACTIVE_PROFILE_ID =
      ResourceLocation.fromNamespaceAndPath("example", "alpha");
  private static final ResourceLocation INVALID_PROFILE_ID =
      ResourceLocation.fromNamespaceAndPath("example", "broken");
  private static final ResourceLocation MISSING_PROFILE_ID =
      ResourceLocation.fromNamespaceAndPath("example", "missing");
  private static final ResourceLocation BLOCK_PROFILE_ID =
      ResourceLocation.fromNamespaceAndPath("example", "block_alpha");

  private static EasyModelProfileService profileService(EasyModelEntityProfile... profiles) {
    Map<ResourceLocation, EasyModelEntityProfile> profilesById = new LinkedHashMap<>();
    for (EasyModelEntityProfile profile : profiles) {
      profilesById.put(profile.id(), profile);
    }
    return new EasyModelProfileService() {
      @Override
      public Optional<EasyModelEntityProfile> getProfile(ResourceLocation profileId) {
        return Optional.ofNullable(profilesById.get(profileId));
      }

      @Override
      public Collection<ResourceLocation> getProfileIds() {
        return profilesById.keySet();
      }

      @Override
      public Collection<EasyModelEntityProfile> getProfiles() {
        return profilesById.values();
      }
    };
  }

  private static EasyModelRenderProfileService renderProfileService(
      EasyModelRenderProfile renderProfile) {
    return new EasyModelRenderProfileService() {
      @Override
      public Optional<EasyModelRenderProfile> getRenderProfile(ResourceLocation renderProfileId) {
        return renderProfile.id().equals(renderProfileId)
            ? Optional.of(renderProfile)
            : Optional.empty();
      }

      @Override
      public Collection<EasyModelRenderProfile> getRenderProfiles() {
        return List.of(renderProfile);
      }
    };
  }

  private static EasyModelEntityProfile activeProfile(ResourceLocation profileId) {
    return profile(profileId, ModelProfileStatus.ACTIVE, List.of());
  }

  private static EasyModelEntityProfile blockEntityProfile(ResourceLocation profileId) {
    return blockEntityProfile(
        profileId,
        ModelBlockEntityTypeIds.ANIMATED_BLOCK_ENTITY,
        ModelBlockEntityPresetType.ANIMATED);
  }

  private static EasyModelEntityProfile blockEntityProfile(
      ResourceLocation profileId,
      ResourceLocation blockEntityType,
      ModelBlockEntityPresetType presetType) {
    return new EasyModelEntityProfile(
        profileId,
        Constants.SCHEMA_VERSION,
        "server-v1",
        ModelType.BLOCK_ENTITY,
        null,
        new ModelBlockEntitySettings(blockEntityType, presetType, ModelBodyType.STATIC),
        new ModelClientSettings(profileId),
        new ModelDimensions(1.0f, 1.0f, 0.5f),
        new ModelMovementSettings(0.0f, 0.0f, false),
        new ModelBehaviorSettings(ModelBehaviorMode.STATIC, false, false),
        new ModelAttributes(10.0f, 0.0f, 16.0f),
        ModelProfileStatus.ACTIVE,
        List.of());
  }

  private static EasyModelEntityProfile invalidProfile(ResourceLocation profileId) {
    return profile(
        profileId,
        ModelProfileStatus.INVALID_DIMENSIONS,
        List.of(
            new ModelProfileValidationIssue(
                ModelProfileStatus.INVALID_DIMENSIONS,
                "dimensions.height",
                "dimensions.height must be between 0.01 and 8.0.")));
  }

  private static EasyModelEntityProfile profile(
      ResourceLocation profileId,
      ModelProfileStatus status,
      List<ModelProfileValidationIssue> issues) {
    return new EasyModelEntityProfile(
        profileId,
        Constants.SCHEMA_VERSION,
        "server-v1",
        ModelType.ENTITY,
        new ModelEntitySettings(
            ModelEntityTypeIds.GROUND_ENTITY, ModelMovementType.GROUND, ModelBodyType.BIPED),
        null,
        new ModelClientSettings(profileId),
        new ModelDimensions(0.6f, 1.8f, 1.62f),
        new ModelMovementSettings(0.22f, 0.6f, true),
        new ModelBehaviorSettings(ModelBehaviorMode.IDLE_ONLY, true, false),
        new ModelAttributes(10.0f, 0.22f, 16.0f),
        status,
        issues);
  }

  private static EasyModelRenderProfile renderProfile(
      ResourceLocation renderProfileId,
      ModelBodyType bodyType,
      String version,
      List<ModelRenderProfileValidationIssue> issues) {
    return new EasyModelRenderProfile(
        renderProfileId,
        Constants.SCHEMA_VERSION,
        version,
        bodyType,
        ResourceLocation.fromNamespaceAndPath("example", "alpha"),
        ResourceLocation.fromNamespaceAndPath("example", "alpha"),
        new ModelRenderSettings(1.0f, 0.3f, 0.0f, 0.0f, Vec3f.ZERO),
        new ModelAnimationSettings(ModelAnimationMode.AUTOMATIC, 1.0f, 1.0f),
        ModelRenderProfileStatus.statusForIssues(issues),
        issues);
  }

  @AfterEach
  void resetServices() {
    EasyModelServices.reset();
  }

  @Test
  void commandRootIsEasyModelEntities() {
    CommandDispatcher<CommandSourceStack> dispatcher = new CommandDispatcher<>();

    EasyModelEntitiesCommand.register(dispatcher);

    assertEquals("easy_model_entities", Constants.MOD_COMMAND);
    assertNotNull(dispatcher.getRoot().getChild("easy_model_entities"));
    assertNotNull(dispatcher.getRoot().getChild("easy_model_entities").getChild("place_block"));
    assertNotNull(dispatcher.getRoot().getChild("easy_model_entities").getChild("set_animation"));
  }

  @Test
  void parseAnimationStateAcceptsSerializedNamesOnly() {
    assertEquals(
        Optional.of(EasyModelAnimationState.WALK),
        EasyModelEntitiesCommand.parseAnimationState("walk"));
    assertEquals(
        Optional.of(EasyModelAnimationState.SWIM),
        EasyModelEntitiesCommand.parseAnimationState("SWIM"));
    assertEquals(
        Optional.of(EasyModelAnimationState.AUTO),
        EasyModelEntitiesCommand.parseAnimationState("auto"));
    assertEquals(Optional.empty(), EasyModelEntitiesCommand.parseAnimationState("unknown"));
    assertEquals(Optional.empty(), EasyModelEntitiesCommand.parseAnimationState(""));
    assertEquals(Optional.empty(), EasyModelEntitiesCommand.parseAnimationState(null));
  }

  @Test
  void listProfilesReportsEmptyState() {
    assertEquals(
        List.of("No Easy Model Entities profiles loaded."),
        EasyModelEntitiesCommand.listProfileLines());
  }

  @Test
  void listProfilesReportsSortedActiveAndInvalidProfiles() {
    EasyModelServices.setProfileService(
        profileService(
            invalidProfile(INVALID_PROFILE_ID),
            activeProfile(ResourceLocation.fromNamespaceAndPath("example", "zeta")),
            activeProfile(ACTIVE_PROFILE_ID)));

    assertEquals(
        List.of(
            "example:alpha active",
            "example:broken invalid: INVALID_DIMENSIONS",
            "example:zeta active"),
        EasyModelEntitiesCommand.listProfileLines());
  }

  @Test
  void validateProfilesReportsSummaryAndActionableIssues() {
    EasyModelServices.setProfileService(
        profileService(activeProfile(ACTIVE_PROFILE_ID), invalidProfile(INVALID_PROFILE_ID)));

    List<String> lines = EasyModelEntitiesCommand.validateProfileLines();

    assertTrue(lines.contains("Easy Model Entities profiles: 1 active, 1 invalid"));
    assertTrue(
        lines.contains(
            "example:broken error INVALID_DIMENSIONS: dimensions.height must be between 0.01 and 8.0."));
  }

  @Test
  void validateProfilesIncludesRenderDiagnosticsWhenAvailable() {
    EasyModelServices.setProfileService(profileService(activeProfile(ACTIVE_PROFILE_ID)));
    EasyModelServices.setRenderProfileService(
        renderProfileService(
            renderProfile(
                ACTIVE_PROFILE_ID,
                ModelBodyType.QUADRUPED,
                "client-v1",
                List.of(
                    new ModelRenderProfileValidationIssue(
                        ModelRenderProfileStatus.MISSING_TEXTURE,
                        "texture",
                        "Missing texture asset example:textures/entity/alpha.png.")))));
    EasyModelServices.setDiagnosticsService(new DefaultEasyModelDiagnosticsService());

    List<String> lines = EasyModelEntitiesCommand.validateProfileLines();

    assertTrue(
        lines.contains(
            "example:alpha error CLIENT_BODY_TYPE_MISMATCH: Render profile body type quadruped does not match server profile body type biped."));
    assertTrue(
        lines.contains(
            "example:alpha error MISSING_TEXTURE: Missing texture asset example:textures/entity/alpha.png."));
  }

  @Test
  void debugProfileReportsServerFieldsAndDedicatedServerSafeRenderStatus() {
    EasyModelServices.setProfileService(profileService(activeProfile(ACTIVE_PROFILE_ID)));

    List<String> lines = EasyModelEntitiesCommand.debugProfileLines(ACTIVE_PROFILE_ID);

    assertTrue(lines.contains("server profile status: ACTIVE"));
    assertTrue(lines.contains("model type: entity"));
    assertTrue(lines.contains("entity type: easy_model_entities:ground_entity"));
    assertTrue(lines.contains("movement type: ground"));
    assertTrue(lines.contains("body type: biped"));
    assertTrue(lines.contains("dimensions: width=0.6 height=1.8 eye_height=1.62"));
    assertTrue(lines.contains("render profile id: example:alpha"));
    assertTrue(lines.contains("expected version: server-v1"));
    assertTrue(lines.contains("client render profile status: unavailable"));
  }

  @Test
  void summonRejectsMissingAndInvalidProfiles() {
    EasyModelServices.setProfileService(
        profileService(activeProfile(ACTIVE_PROFILE_ID), invalidProfile(INVALID_PROFILE_ID)));

    assertEquals(
        Optional.of("Unknown Easy Model Entities profile: example:missing"),
        EasyModelEntitiesCommand.summonRejectionMessage(MISSING_PROFILE_ID));
    assertEquals(
        Optional.of(
            "Cannot summon invalid Easy Model Entities profile example:broken: INVALID_DIMENSIONS."),
        EasyModelEntitiesCommand.summonRejectionMessage(INVALID_PROFILE_ID));
    assertTrue(EasyModelEntitiesCommand.summonRejectionMessage(ACTIVE_PROFILE_ID).isEmpty());
  }

  @Test
  void summonRejectsBlockEntityProfiles() {
    EasyModelServices.setProfileService(profileService(blockEntityProfile(BLOCK_PROFILE_ID)));

    assertEquals(
        Optional.of("Cannot summon non-entity Easy Model Entities profile example:block_alpha."),
        EasyModelEntitiesCommand.summonRejectionMessage(BLOCK_PROFILE_ID));
  }

  @Test
  void placeBlockRejectsMissingInvalidAndEntityProfiles() {
    EasyModelServices.setProfileService(
        profileService(activeProfile(ACTIVE_PROFILE_ID), invalidProfile(INVALID_PROFILE_ID)));

    assertEquals(
        Optional.of("Unknown Easy Model Entities profile: example:missing"),
        EasyModelEntitiesCommand.placeBlockRejectionMessage(MISSING_PROFILE_ID));
    assertEquals(
        Optional.of(
            "Cannot place invalid Easy Model Entities profile example:broken: INVALID_DIMENSIONS."),
        EasyModelEntitiesCommand.placeBlockRejectionMessage(INVALID_PROFILE_ID));
    assertEquals(
        Optional.of("Cannot place non-block-entity Easy Model Entities profile example:alpha."),
        EasyModelEntitiesCommand.placeBlockRejectionMessage(ACTIVE_PROFILE_ID));
  }

  @Test
  void placeBlockAcceptsBlockEntityProfilesAndResolvesHostBlock() {
    EasyModelEntityProfile blockProfile = blockEntityProfile(BLOCK_PROFILE_ID);
    EasyModelServices.setProfileService(profileService(blockProfile));

    assertTrue(EasyModelEntitiesCommand.placeBlockRejectionMessage(BLOCK_PROFILE_ID).isEmpty());
    assertEquals(
        Optional.of(ModelBlockIds.ANIMATED_BLOCK),
        EasyModelEntitiesCommand.blockIdForProfile(blockProfile));
  }

  @Test
  void placeBlockResolvesAnimatedRandomlyHostBlock() {
    EasyModelEntityProfile blockProfile =
        blockEntityProfile(
            BLOCK_PROFILE_ID,
            ModelBlockEntityTypeIds.ANIMATED_RANDOMLY_BLOCK_ENTITY,
            ModelBlockEntityPresetType.ANIMATED_RANDOMLY);

    assertEquals(
        Optional.of(ModelBlockIds.ANIMATED_RANDOMLY_BLOCK),
        EasyModelEntitiesCommand.blockIdForProfile(blockProfile));
  }
}
