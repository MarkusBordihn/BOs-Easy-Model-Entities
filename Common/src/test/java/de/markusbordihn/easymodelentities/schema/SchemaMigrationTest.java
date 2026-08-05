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

package de.markusbordihn.easymodelentities.schema;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonObject;
import de.markusbordihn.easymodelentities.Constants;
import de.markusbordihn.easymodelentities.data.profile.EasyModelEntityProfile;
import de.markusbordihn.easymodelentities.data.profile.ModelBodyType;
import de.markusbordihn.easymodelentities.data.profile.ModelProfileStatus;
import de.markusbordihn.easymodelentities.data.renderprofile.EasyModelRenderProfile;
import de.markusbordihn.easymodelentities.data.renderprofile.ModelRenderProfileStatus;
import de.markusbordihn.easymodelentities.profile.EasyModelProfileParser;
import de.markusbordihn.easymodelentities.renderprofile.ModelRenderProfileParser;
import java.io.StringReader;
import java.util.List;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

class SchemaMigrationTest {

  private static final ResourceLocation ID =
      ResourceLocation.fromNamespaceAndPath("example", "model");

  private static EasyModelRenderProfile parse(String json, SchemaMigrations migrations) {
    return ModelRenderProfileParser.parse(ID, new StringReader(json), migrations);
  }

  private static SchemaMigration migration(String from, String to) {
    return new SchemaMigration() {
      @Override
      public String from() {
        return from;
      }

      @Override
      public String to() {
        return to;
      }

      @Override
      public JsonObject apply(JsonObject input) {
        input.addProperty("schema_version", to);
        return input;
      }
    };
  }

  @Test
  void currentVersionLoadsActive() {
    EasyModelRenderProfile profile =
        parse(
            "{\"schema_version\":\"0.2.0\",\"preset_type\":\"static\"}", SchemaMigrations.DEFAULT);

    assertEquals(ModelRenderProfileStatus.ACTIVE, profile.status());
    assertEquals("0.2.0", profile.schemaVersion());
  }

  @Test
  void previousVersionIsMigratedByDefault() {
    EasyModelRenderProfile profile =
        parse(
            "{\"schema_version\":\"0.1.0\",\"preset_type\":\"static\"}", SchemaMigrations.DEFAULT);

    assertEquals(ModelRenderProfileStatus.ACTIVE, profile.status());
  }

  @Test
  void defaultLegacyConverterRemovesClientLinkAndPreservesInput() {
    JsonObject input = new JsonObject();
    input.addProperty("schema_version", "0.1.0");
    input.addProperty("client", "example:model_render");
    input.addProperty("preset_type", "static");

    JsonObject migrated = SchemaMigrations.DEFAULT.migrate(input, "0.1.0", "0.2.0").orElseThrow();

    assertEquals("0.2.0", migrated.get("schema_version").getAsString());
    assertFalse(migrated.has("client"));
    assertEquals("static", migrated.get("preset_type").getAsString());
    assertEquals("0.1.0", input.get("schema_version").getAsString());
    assertTrue(input.has("client"));
  }

  @Test
  void legacyServerProfileKeepsItsExplicitRenderProfileLink() {
    ResourceLocation legacyRenderProfile = ResourceLocation.fromNamespaceAndPath("example", "legacy_model");
    String json =
        """
        {
          "schema_version": "0.1.0",
          "model_type": "entity",
          "preset_type": "custom",
          "version": "legacy-v1",
          "entity": {
            "type": "easy_model_entities:ground_entity",
            "movement_type": "ground",
            "body_type": "quadruped"
          },
          "client": {
            "render_profile": "example:legacy_model"
          },
          "dimensions": {
            "width": 0.9,
            "height": 0.7,
            "eye_height": 0.5
          },
          "movement": {
            "speed": 0.2,
            "step_height": 0.6,
            "gravity": true
          },
          "behavior": {
            "mode": "ambient",
            "look_at_players": true,
            "random_stroll": true
          },
          "attributes": {
            "max_health": 14.0,
            "follow_range": 16.0
          }
        }
        """;

    EasyModelEntityProfile profile =
        EasyModelProfileParser.parse(ID, new StringReader(json), SchemaMigrations.DEFAULT);

    assertEquals(ModelProfileStatus.ACTIVE, profile.status());
    assertEquals("0.2.0", profile.schemaVersion());
    assertEquals(legacyRenderProfile, profile.renderProfileId());
    assertEquals("legacy-v1", profile.version());
    assertEquals(ModelBodyType.QUADRUPED, profile.bodyType());
    assertEquals(0.9f, profile.width());
    assertEquals(0.7f, profile.height());
    assertTrue(profile.behavior().lookAtPlayers());
    assertTrue(profile.behavior().randomStroll());
  }

  @Test
  void migrationTransportFieldCannotOverrideCurrentProfiles() {
    String json =
        """
        {
          "schema_version": "0.2.0",
          "model_type": "entity",
          "preset_type": "statue",
          "_legacy_render_profile": "example:injected"
        }
        """;

    EasyModelEntityProfile profile =
        EasyModelProfileParser.parse(ID, new StringReader(json), SchemaMigrations.DEFAULT);

    assertEquals(ID, profile.renderProfileId());
    assertTrue(
        profile.validationIssues().stream()
            .anyMatch(issue -> "_legacy_render_profile".equals(issue.field())));
  }

  @Test
  void legacyRenderProfileKeepsItsModelAndTexture() {
    ResourceLocation model =
        ResourceLocation.fromNamespaceAndPath("example", "easy_model_entities/models/legacy_model");
    ResourceLocation texture = ResourceLocation.fromNamespaceAndPath("example", "textures/entity/legacy_model.png");
    String json =
        """
        {
          "schema_version": "0.1.0",
          "preset_type": "static",
          "version": "legacy-v1",
          "model": "example:easy_model_entities/models/legacy_model",
          "texture": "example:textures/entity/legacy_model.png",
          "rendering": {
            "scale": 1.25
          }
        }
        """;

    EasyModelRenderProfile profile = parse(json, SchemaMigrations.DEFAULT);

    assertEquals(ModelRenderProfileStatus.ACTIVE, profile.status());
    assertEquals("0.2.0", profile.schemaVersion());
    assertEquals("legacy-v1", profile.version());
    assertEquals(model, profile.model());
    assertEquals(texture, profile.texture());
    assertEquals(1.25f, profile.scale());
  }

  @Test
  void newerVersionIsDisabledWithInvalidSchemaVersion() {
    EasyModelRenderProfile profile =
        parse(
            "{\"schema_version\":\"0.3.0\",\"preset_type\":\"static\"}", SchemaMigrations.DEFAULT);

    assertEquals(ModelRenderProfileStatus.INVALID_SCHEMA_VERSION, profile.status());
  }

  @Test
  void unparseableVersionIsDisabledWithInvalidSchemaVersion() {
    EasyModelRenderProfile profile =
        parse(
            "{\"schema_version\":\"not-a-version\",\"preset_type\":\"static\"}",
            SchemaMigrations.DEFAULT);

    assertEquals(ModelRenderProfileStatus.INVALID_SCHEMA_VERSION, profile.status());
  }

  @Test
  void olderVersionWithoutMigrationIsDisabled() {
    EasyModelRenderProfile profile =
        parse(
            "{\"schema_version\":\"0.0.9\",\"preset_type\":\"static\"}", SchemaMigrations.DEFAULT);

    assertEquals(ModelRenderProfileStatus.INVALID_SCHEMA_VERSION, profile.status());
  }

  @Test
  void olderVersionIsRoutedThroughMigratorHook() {
    SchemaMigration stub =
        new SchemaMigration() {
          @Override
          public String from() {
            return "0.0.9";
          }

          @Override
          public String to() {
            return "0.2.0";
          }

          @Override
          public JsonObject apply(JsonObject input) {
            input.addProperty("schema_version", to());
            return input;
          }
        };

    EasyModelRenderProfile profile =
        parse(
            "{\"schema_version\":\"0.0.9\",\"preset_type\":\"static\"}",
            new SchemaMigrations(List.of(stub)));

    assertEquals(ModelRenderProfileStatus.ACTIVE, profile.status());
  }

  @Test
  void classifyDetectsVersionRelationships() {
    assertEquals(SchemaVersions.Classification.CURRENT, SchemaVersions.classify("0.1.0", "0.1.0"));
    assertEquals(SchemaVersions.Classification.OLDER, SchemaVersions.classify("0.0.9", "0.1.0"));
    assertEquals(SchemaVersions.Classification.NEWER, SchemaVersions.classify("0.2.0", "0.1.0"));
    assertEquals(
        SchemaVersions.Classification.UNPARSEABLE, SchemaVersions.classify("bogus", "0.1.0"));
  }

  @Test
  void emptyMigrationsCannotMigrateOlderVersion() {
    assertTrue(
        new SchemaMigrations(List.of()).migrate(new JsonObject(), "0.0.9", "0.1.0").isEmpty());
  }

  @Test
  void cyclicMigrationsTerminateWithoutMutatingInput() {
    SchemaMigration forward = migration("0.1.0", "0.2.0");
    SchemaMigration backward = migration("0.2.0", "0.1.0");
    JsonObject input = new JsonObject();
    input.addProperty("schema_version", "0.1.0");

    assertTrue(
        new SchemaMigrations(List.of(forward, backward))
            .migrate(input, "0.1.0", "0.3.0")
            .isEmpty());
    assertEquals("0.1.0", input.get("schema_version").getAsString());
  }

  @Test
  void nullMigrationResultIsRejected() {
    SchemaMigration invalidMigration =
        new SchemaMigration() {
          @Override
          public String from() {
            return "0.1.0";
          }

          @Override
          public String to() {
            return "0.2.0";
          }

          @Override
          public JsonObject apply(JsonObject input) {
            return null;
          }
        };

    assertTrue(
        new SchemaMigrations(List.of(invalidMigration))
            .migrate(new JsonObject(), "0.1.0", "0.2.0")
            .isEmpty());
  }

  @Test
  void supportedVersionsCoverEveryRegisteredMigrationStep() {
    assertEquals(
        List.of("0.1.0", "0.2.0"),
        SchemaMigrations.DEFAULT.supportedVersions(Constants.SCHEMA_VERSION));
  }
}
