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

package de.markusbordihn.easymodelentities.profile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.markusbordihn.easymodelentities.Constants;
import de.markusbordihn.easymodelentities.data.profile.*;
import de.markusbordihn.easymodelentities.registry.ModelBlockEntityTypeIds;
import de.markusbordihn.easymodelentities.registry.ModelEntityTypeIds;
import java.io.StringReader;
import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.Test;

class EasyModelProfileParserTest {

  private static final Identifier PROFILE_ID = Identifier.fromNamespaceAndPath("example", "lizard");

  private static EasyModelEntityProfile parse(String json) {
    return EasyModelProfileParser.parse(PROFILE_ID, new StringReader(json));
  }

  private static void assertPresetDefaults(
      String presetType, Identifier entityType, ModelBodyType bodyType) {
    EasyModelEntityProfile profile =
        parse("{\"model_type\":\"entity\",\"preset_type\":\"" + presetType + "\"}");

    assertEquals(ModelProfileStatus.ACTIVE, profile.status());
    assertEquals(ModelType.ENTITY, profile.modelType());
    assertEquals(entityType, profile.hostEntityType());
    assertEquals(bodyType, profile.bodyType());
  }

  @Test
  void parsesHumanoidWanderingPresetWithDefaults() {
    EasyModelEntityProfile profile =
        parse(
            """
            {
              "model_type": "entity",
              "preset_type": "humanoid_wandering",
              "version": "v1"
            }
            """);

    assertEquals(ModelProfileStatus.ACTIVE, profile.status());
    assertEquals(PROFILE_ID, profile.id());
    assertEquals(Constants.SCHEMA_VERSION, profile.schemaVersion());
    assertEquals("v1", profile.version());
    assertEquals(ModelEntityTypeIds.GROUND_ENTITY, profile.hostEntityType());
    assertEquals(ModelMovementType.GROUND, profile.movementType());
    assertEquals(ModelBodyType.BIPED, profile.bodyType());
    assertEquals(PROFILE_ID, profile.renderProfileId());
    assertEquals(0.6f, profile.width());
    assertEquals(1.8f, profile.height());
    assertEquals(1.62f, profile.eyeHeight());
    assertEquals(ModelBehaviorMode.AMBIENT, profile.behavior().mode());
    assertTrue(profile.behavior().randomStroll());
  }

  @Test
  void parsesStatuePreset() {
    EasyModelEntityProfile profile =
        parse(
            """
            {
              "model_type": "entity",
              "preset_type": "statue"
            }
            """);

    assertEquals(ModelProfileStatus.ACTIVE, profile.status());
    assertEquals(ModelEntityTypeIds.STATIC_ENTITY, profile.hostEntityType());
    assertEquals(ModelMovementType.STATIC, profile.movementType());
    assertEquals(ModelBodyType.STATIC, profile.bodyType());
    assertEquals(ModelBehaviorMode.STATIC, profile.behavior().mode());
    assertFalse(profile.movement().gravity());
  }

  @Test
  void parsesHumanoidStaticPreset() {
    EasyModelEntityProfile profile =
        parse(
            """
            {
              "model_type": "entity",
              "preset_type": "humanoid_still"
            }
            """);

    assertEquals(ModelProfileStatus.ACTIVE, profile.status());
    assertEquals(ModelEntityTypeIds.STATIC_ENTITY, profile.hostEntityType());
    assertEquals(ModelMovementType.STATIC, profile.movementType());
    assertEquals(ModelBodyType.BIPED, profile.bodyType());
    assertEquals(0.0f, profile.movement().speed());
    assertEquals(ModelBehaviorMode.IDLE_ONLY, profile.behavior().mode());
  }

  @Test
  void parsesQuadrupedWanderingPreset() {
    EasyModelEntityProfile profile =
        parse(
            """
            {
              "model_type": "entity",
              "preset_type": "quadruped_wandering"
            }
            """);

    assertEquals(ModelProfileStatus.ACTIVE, profile.status());
    assertEquals(ModelEntityTypeIds.GROUND_ENTITY, profile.hostEntityType());
    assertEquals(ModelBodyType.QUADRUPED, profile.bodyType());
    assertEquals(0.9f, profile.width());
    assertEquals(0.9f, profile.height());
    assertTrue(profile.behavior().randomStroll());
  }

  @Test
  void parsesStaticPresetAsMovableStaticHost() {
    EasyModelEntityProfile profile =
        parse(
            """
            {
              "model_type": "entity",
              "preset_type": "static"
            }
            """);

    assertEquals(ModelProfileStatus.ACTIVE, profile.status());
    assertEquals(ModelEntityTypeIds.GROUND_ENTITY, profile.hostEntityType());
    assertEquals(ModelMovementType.STATIC, profile.movementType());
    assertEquals(ModelBodyType.STATIC, profile.bodyType());
    assertEquals(ModelBehaviorMode.STATIC, profile.behavior().mode());
    assertTrue(profile.movement().gravity());
  }

  @Test
  void parsesVanillaStylePresetFamilies() {
    assertPresetDefaults(
        "aquatic_swimming", ModelEntityTypeIds.AQUATIC_ENTITY, ModelBodyType.AQUATIC);
    assertPresetDefaults("aquatic_still", ModelEntityTypeIds.AQUATIC_ENTITY, ModelBodyType.AQUATIC);
    assertPresetDefaults(
        "amphibious_wandering", ModelEntityTypeIds.AMPHIBIOUS_ENTITY, ModelBodyType.AMPHIBIOUS);
    assertPresetDefaults(
        "amphibious_still", ModelEntityTypeIds.AMPHIBIOUS_ENTITY, ModelBodyType.AMPHIBIOUS);
    assertPresetDefaults(
        "winged_wandering", ModelEntityTypeIds.GROUND_ENTITY, ModelBodyType.WINGED);
    assertPresetDefaults(
        "winged_humanoid_wandering",
        ModelEntityTypeIds.GROUND_ENTITY,
        ModelBodyType.WINGED_HUMANOID);
    assertPresetDefaults(
        "arthropod_wandering", ModelEntityTypeIds.GROUND_ENTITY, ModelBodyType.ARTHROPOD);
    assertPresetDefaults("cuboid_hopping", ModelEntityTypeIds.GROUND_ENTITY, ModelBodyType.CUBOID);
    assertPresetDefaults(
        "floating_still", ModelEntityTypeIds.STATIC_ENTITY, ModelBodyType.FLOATING);
    assertPresetDefaults(
        "quadruped_still", ModelEntityTypeIds.STATIC_ENTITY, ModelBodyType.QUADRUPED);
  }

  @Test
  void explicitFieldsOverridePresetDefaults() {
    EasyModelEntityProfile profile =
        parse(
            """
            {
              "model_type": "entity",
              "preset_type": "quadruped_wandering",
              "dimensions": {
                "width": 1.2,
                "height": 0.85,
                "eye_height": 0.6
              },
              "movement": {
                "speed": 0.06,
                "step_height": 0.4
              },
              "attributes": {
                "max_health": 12.0,
                "follow_range": 12.0
              }
            }
            """);

    assertEquals(ModelProfileStatus.ACTIVE, profile.status());
    assertEquals(PROFILE_ID, profile.renderProfileId());
    assertEquals(1.2f, profile.width());
    assertEquals(0.85f, profile.height());
    assertEquals(0.6f, profile.eyeHeight());
    assertEquals(0.06f, profile.movement().speed());
    assertEquals(0.4f, profile.movement().stepHeight());
    assertEquals(12.0f, profile.attributes().maxHealth());
    assertEquals(12.0f, profile.attributes().followRange());
  }

  @Test
  void parsesCustomProfileWithRequiredFields() {
    EasyModelEntityProfile profile =
        parse(
            """
            {
              "model_type": "entity",
              "preset_type": "custom",
              "entity": {
                "type": "easy_model_entities:ground_entity",
                "movement_type": "ground",
                "body_type": "biped"
              },
              "dimensions": {
                "width": 0.75,
                "height": 1.75,
                "eye_height": 1.5
              }
            }
            """);

    assertEquals(ModelProfileStatus.ACTIVE, profile.status());
    assertEquals(ModelEntityTypeIds.GROUND_ENTITY, profile.hostEntityType());
    assertEquals(ModelMovementType.GROUND, profile.movementType());
    assertEquals(ModelBodyType.BIPED, profile.bodyType());
    assertEquals(0.75f, profile.width());
  }

  @Test
  void customProfileRequiresHostAndDimensions() {
    EasyModelEntityProfile profile =
        parse(
            """
            {
              "model_type": "entity",
              "preset_type": "custom"
            }
            """);

    assertEquals(ModelProfileStatus.INVALID_DIMENSIONS, profile.status());
    assertFalse(profile.isActive());
  }

  @Test
  void rejectsMissingPresetType() {
    EasyModelEntityProfile profile = parse("{\"model_type\":\"entity\"}");

    assertEquals(ModelProfileStatus.DISABLED, profile.status());
  }

  @Test
  void rejectsMissingModelType() {
    EasyModelEntityProfile profile = parse("{\"preset_type\":\"statue\"}");

    assertEquals(ModelProfileStatus.INVALID_MODEL_TYPE, profile.status());
  }

  @Test
  void rejectsInvalidSchemaVersionWhenPresent() {
    EasyModelEntityProfile profile =
        parse(
            """
            {
              "model_type": "entity",
              "schema_version": "9.0.0",
              "preset_type": "statue"
            }
            """);

    assertEquals(ModelProfileStatus.INVALID_SCHEMA_VERSION, profile.status());
  }

  @Test
  void rejectsInvalidDimensions() {
    EasyModelEntityProfile profile =
        parse(
            """
            {
              "model_type": "entity",
              "preset_type": "statue",
              "dimensions": {
                "width": -1.0
              }
            }
            """);

    assertEquals(ModelProfileStatus.INVALID_DIMENSIONS, profile.status());
  }

  @Test
  void rejectsInvalidMovementNumericValues() {
    EasyModelEntityProfile profile =
        parse(
            """
            {
              "model_type": "entity",
              "preset_type": "humanoid_wandering",
              "movement": {
                "speed": 3.0
              }
            }
            """);

    assertEquals(ModelProfileStatus.DISABLED, profile.status());
  }

  @Test
  void parsesStaticBlockEntityPreset() {
    EasyModelEntityProfile profile =
        parse(
            """
            {
              "model_type": "block_entity",
              "preset_type": "static"
            }
            """);

    assertEquals(ModelProfileStatus.ACTIVE, profile.status());
    assertEquals(ModelType.BLOCK_ENTITY, profile.modelType());
    assertEquals(ModelBlockEntityTypeIds.STATIC_BLOCK_ENTITY, profile.hostBlockEntityType());
    assertEquals(ModelBlockEntityPresetType.STATIC, profile.blockEntityPresetType());
    assertEquals(ModelBodyType.STATIC, profile.bodyType());
    assertFalse(profile.blockEntityPresetType().hasClientTick());
    assertFalse(profile.blockEntityPresetType().hasServerTick());
  }

  @Test
  void parsesTickingBlockEntityPreset() {
    EasyModelEntityProfile profile =
        parse(
            """
            {
              "model_type": "block_entity",
              "preset_type": "ticking"
            }
            """);

    assertEquals(ModelProfileStatus.ACTIVE, profile.status());
    assertEquals(ModelBlockEntityTypeIds.TICKING_BLOCK_ENTITY, profile.hostBlockEntityType());
    assertEquals(ModelBlockEntityPresetType.TICKING, profile.blockEntityPresetType());
    assertTrue(profile.blockEntityPresetType().hasClientTick());
    assertTrue(profile.blockEntityPresetType().hasServerTick());
  }

  @Test
  void parsesAnimatedBlockEntityPreset() {
    EasyModelEntityProfile profile =
        parse(
            """
            {
              "model_type": "block_entity",
              "preset_type": "animated",
              "block_entity": {
                "body_type": "biped"
              }
            }
            """);

    assertEquals(ModelProfileStatus.ACTIVE, profile.status());
    assertEquals(ModelBlockEntityTypeIds.ANIMATED_BLOCK_ENTITY, profile.hostBlockEntityType());
    assertEquals(ModelBlockEntityPresetType.ANIMATED, profile.blockEntityPresetType());
    assertEquals(ModelBodyType.BIPED, profile.bodyType());
    assertTrue(profile.blockEntityPresetType().hasClientTick());
    assertFalse(profile.blockEntityPresetType().hasServerTick());
  }

  @Test
  void parsesAnimatedRandomlyBlockEntityPreset() {
    EasyModelEntityProfile profile =
        parse(
            """
            {
              "model_type": "block_entity",
              "preset_type": "animated_randomly"
            }
            """);

    assertEquals(ModelProfileStatus.ACTIVE, profile.status());
    assertEquals(
        ModelBlockEntityTypeIds.ANIMATED_RANDOMLY_BLOCK_ENTITY, profile.hostBlockEntityType());
    assertEquals(ModelBlockEntityPresetType.ANIMATED_RANDOMLY, profile.blockEntityPresetType());
    assertTrue(profile.blockEntityPresetType().hasClientTick());
    assertFalse(profile.blockEntityPresetType().hasServerTick());
    assertTrue(profile.blockEntityPresetType().hasRandomIdleAnimation());
  }

  @Test
  void rejectsEntityPresetForBlockEntityModelType() {
    EasyModelEntityProfile profile =
        parse(
            """
            {
              "model_type": "block_entity",
              "preset_type": "quadruped_wandering"
            }
            """);

    assertEquals(ModelProfileStatus.INVALID_HOST_BLOCK_ENTITY, profile.status());
  }

  @Test
  void malformedJsonProducesInvalidJsonStatus() {
    EasyModelEntityProfile profile = parse("{");

    assertEquals(ModelProfileStatus.INVALID_JSON, profile.status());
    assertFalse(profile.validationIssues().isEmpty());
  }
}
