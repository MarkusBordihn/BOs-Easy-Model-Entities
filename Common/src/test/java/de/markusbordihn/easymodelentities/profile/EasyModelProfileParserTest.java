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

import de.markusbordihn.easymodelentities.registry.ModelEntityTypeIds;
import java.io.StringReader;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

class EasyModelProfileParserTest {

  private static final ResourceLocation PROFILE_ID = new ResourceLocation("example", "lizard");

  private static EasyModelEntityProfile parse(String json) {
    return EasyModelProfileParser.parse(PROFILE_ID, new StringReader(json));
  }

  private static String validProfileJson() {
    return """
        {
          "schema_version": "0.1.0",
          "id": "example:lizard",
          "pack_pair": {
            "pair_id": "2cbb2c6e-4f28-4f1d-b9f7-0d8c1f63d24a",
            "asset_fingerprint": "sha256:abc123"
          },
          "host": {
            "entity_type": "easy_model_entities:ground_entity",
            "movement_type": "ground",
            "body_type": "quadruped"
          },
          "client": {
            "render_profile": "example:lizard"
          },
          "dimensions": {
            "width": 0.6,
            "height": 0.8,
            "eye_height": 0.5
          },
          "movement": {
            "speed": 0.22,
            "step_height": 0.6,
            "gravity": true
          },
          "behavior": {
            "mode": "idle_only",
            "look_at_players": true,
            "random_stroll": false
          },
          "attributes": {
            "max_health": 10.0,
            "movement_speed": 0.22,
            "follow_range": 16.0
          },
          "traits": ["easy_model_entities:living"]
        }
        """;
  }

  @Test
  void parsesValidGroundQuadrupedProfile() {
    EasyModelEntityProfile profile = parse(validProfileJson());

    assertEquals(ModelProfileStatus.ACTIVE, profile.status());
    assertTrue(profile.isActive());
    assertEquals(PROFILE_ID, profile.id());
    assertEquals(ModelEntityTypeIds.GROUND_ENTITY, profile.hostEntityType());
    assertEquals(ModelMovementType.GROUND, profile.movementType());
    assertEquals(ModelBodyType.QUADRUPED, profile.bodyType());
    assertEquals(new ResourceLocation("example", "lizard"), profile.renderProfileId());
    assertEquals("sha256:abc123", profile.assetFingerprint());
    assertEquals(0.6f, profile.width());
    assertEquals(0.8f, profile.height());
    assertEquals(0.5f, profile.eyeHeight());
    assertTrue(profile.hasTrait(new ResourceLocation("easy_model_entities", "living")));
  }

  @Test
  void parsesValidBipedProfile() {
    EasyModelEntityProfile profile =
        parse(
            validProfileJson().replace("\"body_type\": \"quadruped\"", "\"body_type\": \"biped\""));

    assertEquals(ModelProfileStatus.ACTIVE, profile.status());
    assertEquals(ModelBodyType.BIPED, profile.bodyType());
  }

  @Test
  void parsesValidStaticProfileWithDefaults() {
    EasyModelEntityProfile profile =
        parse(
            """
            {
              "schema_version": "0.1.0",
              "id": "example:lizard",
              "host": {
                "entity_type": "easy_model_entities:static_entity",
                "movement_type": "static",
                "body_type": "static"
              },
              "client": {
                "render_profile": "example:lizard"
              },
              "dimensions": {
                "width": 0.6,
                "height": 0.8,
                "eye_height": 0.0
              }
            }
            """);

    assertEquals(ModelProfileStatus.ACTIVE, profile.status());
    assertEquals(ModelEntityTypeIds.STATIC_ENTITY, profile.hostEntityType());
    assertEquals(0.0f, profile.movement().speed());
    assertEquals(0.0f, profile.movement().stepHeight());
    assertFalse(profile.movement().gravity());
    assertEquals(ModelBehaviorMode.STATIC, profile.behavior().mode());
    assertFalse(profile.behavior().lookAtPlayers());
    assertTrue(profile.traits().isEmpty());
  }

  @Test
  void parsesExternalOwnerProfile() {
    EasyModelEntityProfile profile =
        parse(
            validProfileJson().replace("\"mode\": \"idle_only\"", "\"mode\": \"external_owner\""));

    assertEquals(ModelProfileStatus.ACTIVE, profile.status());
    assertEquals(ModelBehaviorMode.EXTERNAL_OWNER, profile.behavior().mode());
  }

  @Test
  void rejectsInvalidSchemaVersion() {
    EasyModelEntityProfile profile =
        parse(
            validProfileJson()
                .replace("\"schema_version\": \"0.1.0\"", "\"schema_version\": \"9.0.0\""));

    assertEquals(ModelProfileStatus.INVALID_SCHEMA_VERSION, profile.status());
    assertFalse(profile.isActive());
  }

  @Test
  void rejectsMissingSchemaVersion() {
    EasyModelEntityProfile profile =
        parse(validProfileJson().replace("  \"schema_version\": \"0.1.0\",\n", ""));

    assertEquals(ModelProfileStatus.INVALID_SCHEMA_VERSION, profile.status());
  }

  @Test
  void rejectsInvalidProfileId() {
    EasyModelEntityProfile profile =
        parse(validProfileJson().replace("\"id\": \"example:lizard\"", "\"id\": \"Invalid Id\""));

    assertEquals(ModelProfileStatus.INVALID_RESOURCE_LOCATION, profile.status());
  }

  @Test
  void rejectsPathIdMismatch() {
    EasyModelEntityProfile profile =
        parse(
            validProfileJson().replace("\"id\": \"example:lizard\"", "\"id\": \"example:gecko\""));

    assertEquals(ModelProfileStatus.INVALID_RESOURCE_LOCATION, profile.status());
  }

  @Test
  void rejectsUnsupportedHostEntityType() {
    EasyModelEntityProfile profile =
        parse(
            validProfileJson()
                .replace(
                    "\"entity_type\": \"easy_model_entities:ground_entity\"",
                    "\"entity_type\": \"minecraft:pig\""));

    assertEquals(ModelProfileStatus.INVALID_HOST_ENTITY, profile.status());
  }

  @Test
  void rejectsMissingMovementType() {
    EasyModelEntityProfile profile =
        parse(validProfileJson().replace("\"movement_type\": \"ground\",", ""));

    assertEquals(ModelProfileStatus.INVALID_HOST_ENTITY, profile.status());
    assertEquals(ModelMovementType.STATIC, profile.movementType());
  }

  @Test
  void rejectsUnsupportedBodyTypeWithoutStaticFallback() {
    EasyModelEntityProfile profile =
        parse(
            validProfileJson()
                .replace("\"body_type\": \"quadruped\"", "\"body_type\": \"serpentine\""));

    assertEquals(ModelProfileStatus.INVALID_HOST_ENTITY, profile.status());
    assertFalse(profile.isActive());
  }

  @Test
  void rejectsInvalidClientRenderProfile() {
    EasyModelEntityProfile profile =
        parse(
            validProfileJson()
                .replace(
                    "\"render_profile\": \"example:lizard\"", "\"render_profile\": \"bad id\""));

    assertEquals(ModelProfileStatus.INVALID_RESOURCE_LOCATION, profile.status());
  }

  @Test
  void rejectsInvalidDimensions() {
    EasyModelEntityProfile profile =
        parse(validProfileJson().replace("\"width\": 0.6", "\"width\": -1.0"));

    assertEquals(ModelProfileStatus.INVALID_DIMENSIONS, profile.status());
  }

  @Test
  void rejectsInvalidEyeHeight() {
    EasyModelEntityProfile profile =
        parse(validProfileJson().replace("\"eye_height\": 0.5", "\"eye_height\": 2.0"));

    assertEquals(ModelProfileStatus.INVALID_DIMENSIONS, profile.status());
  }

  @Test
  void rejectsInvalidMovementNumericValues() {
    EasyModelEntityProfile profile =
        parse(validProfileJson().replace("\"speed\": 0.22", "\"speed\": 3.0"));

    assertEquals(ModelProfileStatus.DISABLED, profile.status());
    assertFalse(profile.isActive());
  }

  @Test
  void rejectsInvalidTraitId() {
    EasyModelEntityProfile profile =
        parse(
            validProfileJson().replace("\"easy_model_entities:living\"", "\"not a valid trait\""));

    assertEquals(ModelProfileStatus.DISABLED, profile.status());
  }

  @Test
  void rejectsTooManyTraits() {
    String traits =
        IntStream.range(0, 65)
            .mapToObj(index -> "\"example:trait_" + index + "\"")
            .collect(Collectors.joining(", "));
    EasyModelEntityProfile profile =
        parse(validProfileJson().replace("\"easy_model_entities:living\"", traits));

    assertEquals(ModelProfileStatus.DISABLED, profile.status());
  }

  @Test
  void malformedJsonProducesInvalidJsonStatus() {
    EasyModelEntityProfile profile = parse("{");

    assertEquals(ModelProfileStatus.INVALID_JSON, profile.status());
    assertFalse(profile.validationIssues().isEmpty());
  }
}
