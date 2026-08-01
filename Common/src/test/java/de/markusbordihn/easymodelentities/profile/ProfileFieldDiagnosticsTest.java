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
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.markusbordihn.easymodelentities.data.profile.EasyModelEntityProfile;
import de.markusbordihn.easymodelentities.data.profile.ModelProfileStatus;
import de.markusbordihn.easymodelentities.data.profile.ModelProfileValidationIssue;
import java.io.StringReader;
import java.util.List;
import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ProfileFieldDiagnosticsTest {

  private static final Identifier PROFILE_ID =
      Identifier.fromNamespaceAndPath("example", "entity/lizard");

  private static EasyModelEntityProfile parse(String json) {
    return EasyModelProfileParser.parse(PROFILE_ID, new StringReader(json));
  }

  private static List<ModelProfileValidationIssue> warnings(EasyModelEntityProfile profile) {
    return profile.validationIssues().stream()
        .filter(issue -> issue.status() == ModelProfileStatus.ACTIVE)
        .toList();
  }

  @Test
  @DisplayName("An unknown root field is a warning and keeps the profile usable")
  void unknownRootFieldIsReported() {
    EasyModelEntityProfile profile =
        parse(
            """
            {
              "schema_version": "0.2.0",
              "model_type": "entity",
              "preset_type": "humanoid_wandering",
              "shadow_radus": 0.5
            }
            """);

    assertEquals(ModelProfileStatus.ACTIVE, profile.status());
    assertTrue(profile.isActive());
    assertEquals(1, warnings(profile).size());
    assertEquals("shadow_radus", warnings(profile).get(0).field());
  }

  @Test
  @DisplayName("An unknown field inside a nested object is reported with its full path")
  void unknownNestedFieldIsReported() {
    EasyModelEntityProfile profile =
        parse(
            """
            {
              "schema_version": "0.2.0",
              "model_type": "entity",
              "preset_type": "humanoid_wandering",
              "dimensions": {
                "width": 0.6,
                "height": 1.8,
                "eye_hight": 1.62
              }
            }
            """);

    assertEquals(ModelProfileStatus.ACTIVE, profile.status());
    assertEquals(1, warnings(profile).size());
    assertEquals("dimensions.eye_hight", warnings(profile).get(0).field());
  }

  @Test
  @DisplayName("A missing schema_version is reported as a warning")
  void missingSchemaVersionIsReported() {
    EasyModelEntityProfile profile =
        parse(
            """
            {
              "model_type": "entity",
              "preset_type": "humanoid_wandering"
            }
            """);

    assertEquals(ModelProfileStatus.ACTIVE, profile.status());
    assertEquals(1, warnings(profile).size());
    assertEquals("schema_version", warnings(profile).get(0).field());
  }

  @Test
  @DisplayName("A warning does not mask a blocking issue")
  void blockingIssueWinsOverWarning() {
    EasyModelEntityProfile profile =
        parse(
            """
            {
              "model_type": "entity",
              "preset_type": "humanoid_wandering",
              "dimensions": {
                "width": 99.0
              }
            }
            """);

    assertEquals(ModelProfileStatus.INVALID_DIMENSIONS, profile.status());
    assertTrue(warnings(profile).size() >= 1);
  }
}
