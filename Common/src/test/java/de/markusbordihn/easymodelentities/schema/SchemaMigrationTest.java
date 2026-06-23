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
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonObject;
import de.markusbordihn.easymodelentities.data.renderprofile.EasyModelRenderProfile;
import de.markusbordihn.easymodelentities.data.renderprofile.ModelRenderProfileStatus;
import de.markusbordihn.easymodelentities.renderprofile.ModelRenderProfileParser;
import java.io.StringReader;
import java.util.List;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

class SchemaMigrationTest {

  private static final ResourceLocation ID = new ResourceLocation("example", "model");

  private static EasyModelRenderProfile parse(String json, SchemaMigrations migrations) {
    return ModelRenderProfileParser.parse(ID, new StringReader(json), migrations);
  }

  @Test
  void currentVersionLoadsActive() {
    EasyModelRenderProfile profile =
        parse(
            "{\"schema_version\":\"0.1.0\",\"preset_type\":\"static\"}", SchemaMigrations.DEFAULT);

    assertEquals(ModelRenderProfileStatus.ACTIVE, profile.status());
  }

  @Test
  void newerVersionIsDisabledWithInvalidSchemaVersion() {
    EasyModelRenderProfile profile =
        parse(
            "{\"schema_version\":\"0.2.0\",\"preset_type\":\"static\"}", SchemaMigrations.DEFAULT);

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
            return "0.1.0";
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
    assertTrue(SchemaMigrations.DEFAULT.migrate(new JsonObject(), "0.0.9", "0.1.0").isEmpty());
  }
}
