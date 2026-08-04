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

package de.markusbordihn.easymodelentities.renderprofile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.markusbordihn.easymodelentities.Constants;
import de.markusbordihn.easymodelentities.data.model.Vec3f;
import de.markusbordihn.easymodelentities.data.profile.ModelBodyType;
import de.markusbordihn.easymodelentities.data.renderprofile.*;
import java.io.StringReader;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

class ModelRenderProfileParserTest {

  private static final ResourceLocation RENDER_PROFILE_ID =
      new ResourceLocation("example", "lizard");

  private static EasyModelRenderProfile parse(String json) {
    return ModelRenderProfileParser.parse(RENDER_PROFILE_ID, new StringReader(json));
  }

  private static void assertPresetBodyType(String presetType, ModelBodyType bodyType) {
    EasyModelRenderProfile renderProfile = parse("{\"preset_type\":\"" + presetType + "\"}");

    assertEquals(ModelRenderProfileStatus.ACTIVE, renderProfile.status());
    assertEquals(bodyType, renderProfile.bodyType());
    assertEquals(ModelAnimationMode.AUTOMATIC, renderProfile.animation().mode());
  }

  @Test
  void parsesPresetRenderProfileWithDefaults() {
    EasyModelRenderProfile renderProfile = parse("{\"preset_type\":\"humanoid_wandering\"}");

    assertEquals(ModelRenderProfileStatus.ACTIVE, renderProfile.status());
    assertTrue(renderProfile.isActive());
    assertEquals(RENDER_PROFILE_ID, renderProfile.id());
    assertEquals(Constants.SCHEMA_VERSION, renderProfile.schemaVersion());
    assertEquals("", renderProfile.version());
    assertEquals(ModelBodyType.BIPED, renderProfile.bodyType());
    assertEquals(
        new ResourceLocation("example", "easy_model_entities/models/lizard"),
        renderProfile.model());
    assertEquals(
        new ResourceLocation("example", "textures/entity/lizard.png"), renderProfile.texture());
    assertEquals(1.0f, renderProfile.scale());
    assertEquals(0.3f, renderProfile.shadowRadius());
    assertEquals(ModelAnimationMode.AUTOMATIC, renderProfile.animation().mode());
  }

  @Test
  void parsesAssetFingerprint() {
    EasyModelRenderProfile renderProfile =
        parse("{\"preset_type\":\"static\",\"asset_fingerprint\":\"abc123\"}");

    assertEquals(ModelRenderProfileStatus.ACTIVE, renderProfile.status());
    assertEquals("abc123", renderProfile.assetFingerprint());
  }

  @Test
  void defaultsAssetFingerprintToEmpty() {
    EasyModelRenderProfile renderProfile = parse("{\"preset_type\":\"static\"}");

    assertEquals("", renderProfile.assetFingerprint());
  }

  @Test
  void defaultsToNoVisibleBounds() {
    EasyModelRenderProfile renderProfile = parse("{\"preset_type\":\"static\"}");

    assertEquals(ModelRenderProfileStatus.ACTIVE, renderProfile.status());
    assertFalse(renderProfile.hasVisibleBounds());
    assertEquals(0.0f, renderProfile.visibleBoundsWidth());
    assertEquals(0.0f, renderProfile.visibleBoundsHeight());
    assertEquals(Vec3f.ZERO, renderProfile.visibleBoundsOffset());
  }

  @Test
  void parsesVisibleBounds() {
    EasyModelRenderProfile renderProfile =
        parse(
            """
            {
              "preset_type": "statue",
              "rendering": {
                "visible_bounds_width": 1.066,
                "visible_bounds_height": 1.031,
                "visible_bounds_offset": [0.0, 0.469, 0.0]
              }
            }
            """);

    assertEquals(ModelRenderProfileStatus.ACTIVE, renderProfile.status());
    assertTrue(renderProfile.hasVisibleBounds());
    assertEquals(1.066f, renderProfile.visibleBoundsWidth());
    assertEquals(1.031f, renderProfile.visibleBoundsHeight());
    assertEquals(new Vec3f(0.0f, 0.469f, 0.0f), renderProfile.visibleBoundsOffset());
  }

  @Test
  void rejectsMalformedVisibleBoundsOffset() {
    EasyModelRenderProfile renderProfile =
        parse(
            """
            {
              "preset_type": "statue",
              "rendering": {
                "visible_bounds_offset": [0.0, 1.0]
              }
            }
            """);

    assertEquals(ModelRenderProfileStatus.INVALID_RENDER_SETTINGS, renderProfile.status());
  }

  @Test
  void rejectsNegativeRenderAndAnimationValuesWithoutKeepingThem() {
    EasyModelRenderProfile renderProfile =
        parse(
            """
            {
              "preset_type": "humanoid_wandering",
              "rendering": {
                "scale": -1.0,
                "shadow_radius": -1.0,
                "visible_bounds_width": -1.0
              },
              "animation": {
                "swing_speed": -1.0
              }
            }
            """);

    assertEquals(ModelRenderProfileStatus.INVALID_RENDER_SETTINGS, renderProfile.status());
    assertTrue(renderProfile.scale() > 0.0f);
    assertTrue(renderProfile.shadowRadius() >= 0.0f);
    assertTrue(renderProfile.visibleBoundsWidth() >= 0.0f);
    assertTrue(renderProfile.animation().swingSpeed() >= 0.0f);
  }

  @Test
  void settingsRecordsRejectInvalidDirectConstruction() {
    assertThrows(
        IllegalArgumentException.class,
        () -> new ModelRenderSettings(0.0f, 0.0f, 0.0f, 0.0f, Vec3f.ZERO));
    assertThrows(
        IllegalArgumentException.class,
        () -> new ModelAnimationSettings(ModelAnimationMode.AUTOMATIC, -1.0f, 1.0f));
  }

  @Test
  void parsesTexturesMap() {
    EasyModelRenderProfile renderProfile =
        parse(
            "{\"preset_type\":\"static\",\"textures\":{"
                + "\"0\":\"example:textures/entity/mimic.png\","
                + "\"1\":\"minecraft:textures/block/chest.png\"}}");

    assertEquals(ModelRenderProfileStatus.ACTIVE, renderProfile.status());
    assertEquals(
        new ResourceLocation("example", "textures/entity/mimic.png"),
        renderProfile.textures().get(0));
    assertEquals(
        new ResourceLocation("minecraft", "textures/block/chest.png"),
        renderProfile.textures().get(1));
  }

  @Test
  void defaultsToEmptyTexturesMap() {
    EasyModelRenderProfile renderProfile = parse("{\"preset_type\":\"static\"}");

    assertTrue(renderProfile.textures().isEmpty());
  }

  @Test
  void parsesStillPresetWithIdleAnimationDefaults() {
    EasyModelRenderProfile renderProfile = parse("{\"preset_type\":\"humanoid_still\"}");

    assertEquals(ModelRenderProfileStatus.ACTIVE, renderProfile.status());
    assertEquals(ModelBodyType.BIPED, renderProfile.bodyType());
    assertEquals(ModelAnimationMode.AUTOMATIC, renderProfile.animation().mode());
  }

  @Test
  void parsesStaticPresetWithoutAnimation() {
    EasyModelRenderProfile renderProfile = parse("{\"preset_type\":\"static\"}");

    assertEquals(ModelRenderProfileStatus.ACTIVE, renderProfile.status());
    assertEquals(ModelBodyType.STATIC, renderProfile.bodyType());
    assertEquals(ModelAnimationMode.NONE, renderProfile.animation().mode());
  }

  @Test
  void parsesRandomIdleAnimationMode() {
    EasyModelRenderProfile renderProfile =
        parse(
            """
            {
              "preset_type": "cuboid_still",
              "body_type": "cuboid",
              "animation": {
                "mode": "random_idle"
              }
            }
            """);

    assertEquals(ModelRenderProfileStatus.ACTIVE, renderProfile.status());
    assertEquals(ModelAnimationMode.RANDOM_IDLE, renderProfile.animation().mode());
  }

  @Test
  void parsesVanillaStylePresetFamilies() {
    assertPresetBodyType("aquatic_swimming", ModelBodyType.AQUATIC);
    assertPresetBodyType("winged_wandering", ModelBodyType.WINGED);
    assertPresetBodyType("winged_humanoid_wandering", ModelBodyType.WINGED_HUMANOID);
    assertPresetBodyType("arthropod_wandering", ModelBodyType.ARTHROPOD);
    assertPresetBodyType("cuboid_hopping", ModelBodyType.CUBOID);
    assertPresetBodyType("floating_still", ModelBodyType.FLOATING);
    assertPresetBodyType("quadruped_still", ModelBodyType.QUADRUPED);
  }

  @Test
  void parsesCustomRenderProfileWithRequiredBodyType() {
    EasyModelRenderProfile renderProfile =
        parse(
            """
            {
              "preset_type": "custom",
              "version": "v1",
              "body_type": "quadruped"
            }
            """);

    assertEquals(ModelRenderProfileStatus.ACTIVE, renderProfile.status());
    assertEquals("v1", renderProfile.version());
    assertEquals(ModelBodyType.QUADRUPED, renderProfile.bodyType());
    assertEquals(ModelAnimationMode.NONE, renderProfile.animation().mode());
  }

  @Test
  void explicitFieldsOverridePresetDefaults() {
    EasyModelRenderProfile renderProfile =
        RenderProfileTestFixtures.parse(RenderProfileTestFixtures.RESOURCE_PACK_RENDER_PROFILE);

    assertEquals(ModelRenderProfileStatus.ACTIVE, renderProfile.status());
    assertEquals("v1", renderProfile.version());
    assertEquals(ModelBodyType.QUADRUPED, renderProfile.bodyType());
    assertEquals(1.25f, renderProfile.scale());
    assertEquals(0.4f, renderProfile.shadowRadius());
    assertEquals(1.1f, renderProfile.animation().swingSpeed());
    assertEquals(0.9f, renderProfile.animation().walkSpeedMultiplier());
  }

  @Test
  void parsesIdleStrength() {
    EasyModelRenderProfile renderProfile =
        parse("{\"preset_type\":\"cuboid_hopping\",\"animation\":{\"idle_strength\":2.5}}");

    assertEquals(2.5f, renderProfile.animation().idleStrength());
  }

  @Test
  void defaultsIdleStrengthToOne() {
    EasyModelRenderProfile renderProfile = parse("{\"preset_type\":\"cuboid_hopping\"}");

    assertEquals(1.0f, renderProfile.animation().idleStrength());
  }

  @Test
  void defaultsGaitToNatural() {
    EasyModelRenderProfile renderProfile = parse("{\"preset_type\":\"quadruped_wandering\"}");

    assertEquals(ModelGaitType.NATURAL, renderProfile.animation().gait());
  }

  @Test
  void parsesGait() {
    EasyModelRenderProfile renderProfile =
        parse("{\"preset_type\":\"quadruped_wandering\",\"animation\":{\"gait\":\"ungulate\"}}");

    assertEquals(ModelRenderProfileStatus.ACTIVE, renderProfile.status());
    assertEquals(ModelGaitType.UNGULATE, renderProfile.animation().gait());
  }

  @Test
  void rejectsInvalidGait() {
    EasyModelRenderProfile renderProfile =
        parse("{\"preset_type\":\"quadruped_wandering\",\"animation\":{\"gait\":\"hover\"}}");

    assertEquals(ModelRenderProfileStatus.INVALID_ANIMATION_MODE, renderProfile.status());
    assertEquals(ModelGaitType.NATURAL, renderProfile.animation().gait());
  }

  @Test
  void customRenderProfileRequiresBodyType() {
    EasyModelRenderProfile renderProfile = parse("{\"preset_type\":\"custom\"}");

    assertEquals(ModelRenderProfileStatus.INVALID_BODY_TYPE, renderProfile.status());
    assertFalse(renderProfile.isActive());
  }

  @Test
  void rejectsMissingPresetType() {
    EasyModelRenderProfile renderProfile = parse("{}");

    assertEquals(ModelRenderProfileStatus.INVALID_RENDER_SETTINGS, renderProfile.status());
  }

  @Test
  void rejectsInvalidSchemaVersion() {
    EasyModelRenderProfile renderProfile =
        RenderProfileTestFixtures.parse("renderprofile/invalid_schema_version.json");

    assertEquals(ModelRenderProfileStatus.INVALID_SCHEMA_VERSION, renderProfile.status());
    assertFalse(renderProfile.isActive());
  }

  @Test
  void rejectsInvalidBodyType() {
    EasyModelRenderProfile renderProfile =
        RenderProfileTestFixtures.parse("renderprofile/invalid_body_type.json");

    assertEquals(ModelRenderProfileStatus.INVALID_BODY_TYPE, renderProfile.status());
  }

  @Test
  void rejectsInvalidModelId() {
    EasyModelRenderProfile renderProfile =
        RenderProfileTestFixtures.parse("renderprofile/invalid_model_id.json");

    assertEquals(ModelRenderProfileStatus.INVALID_RESOURCE_LOCATION, renderProfile.status());
  }

  @Test
  void rejectsInvalidTextureId() {
    EasyModelRenderProfile renderProfile =
        RenderProfileTestFixtures.parse("renderprofile/invalid_texture_id.json");

    assertEquals(ModelRenderProfileStatus.INVALID_RESOURCE_LOCATION, renderProfile.status());
  }

  @Test
  void rejectsInvalidAnimationMode() {
    EasyModelRenderProfile renderProfile =
        RenderProfileTestFixtures.parse("renderprofile/invalid_animation_mode.json");

    assertEquals(ModelRenderProfileStatus.INVALID_ANIMATION_MODE, renderProfile.status());
  }

  @Test
  void malformedJsonProducesInvalidJsonStatus() {
    EasyModelRenderProfile renderProfile =
        ModelRenderProfileParser.parse(RENDER_PROFILE_ID, new StringReader("{"));

    assertEquals(ModelRenderProfileStatus.INVALID_JSON, renderProfile.status());
    assertFalse(renderProfile.validationIssues().isEmpty());
  }

  @Test
  void unknownFieldsAreIgnoredSafely() {
    EasyModelRenderProfile renderProfile =
        RenderProfileTestFixtures.parse("renderprofile/unknown_fields.json");

    assertEquals(ModelRenderProfileStatus.ACTIVE, renderProfile.status());
  }
}
