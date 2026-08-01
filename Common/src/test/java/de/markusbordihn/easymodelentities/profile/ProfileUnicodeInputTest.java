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
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.markusbordihn.easymodelentities.data.profile.EasyModelEntityProfile;
import de.markusbordihn.easymodelentities.data.profile.ModelProfileStatus;
import de.markusbordihn.easymodelentities.data.profile.ModelProfileValidationIssue;
import java.io.StringReader;
import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ProfileUnicodeInputTest {

  private static final Identifier PROFILE_ID = Identifier.fromNamespaceAndPath("example", "lizard");

  private static EasyModelEntityProfile parse(String json) {
    return EasyModelProfileParser.parse(PROFILE_ID, new StringReader(json));
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "example:bär",
        "example:モデル",
        "Example:Lizard",
        "example:café",
        "example:🐢",
        "example:lizard mit leerzeichen",
        "пример:ящерица",
        ":",
        "example:a/../b"
      })
  @DisplayName("Resource locations with any script or special character fail with a clear issue")
  void invalidResourceLocationsAreReported(String rawEntityType) {
    EasyModelEntityProfile profile =
        parse(
            """
            {
              "model_type": "entity",
              "preset_type": "custom",
              "entity": {
                "type": "%s",
                "movement_type": "ground",
                "body_type": "biped"
              },
              "dimensions": {"width": 0.6, "height": 1.8, "eye_height": 1.62}
            }
            """
                .formatted(rawEntityType));

    assertNotEquals(ModelProfileStatus.ACTIVE, profile.status());
    assertTrue(
        profile.validationIssues().stream()
            .map(ModelProfileValidationIssue::status)
            .anyMatch(
                status ->
                    status == ModelProfileStatus.INVALID_RESOURCE_LOCATION
                        || status == ModelProfileStatus.INVALID_HOST_ENTITY),
        () -> "Expected an issue for the host entity type: " + profile.validationIssues());
  }

  @ParameterizedTest
  @ValueSource(strings = {"Bär", "モデル", "🐢", "v1.0 – Räuber", "", "  "})
  @DisplayName("Free text fields accept any language without breaking the profile")
  void freeTextFieldsAcceptAnyLanguage(String version) {
    EasyModelEntityProfile profile =
        parse(
            """
            {
              "model_type": "entity",
              "preset_type": "humanoid_wandering",
              "version": "%s"
            }
            """
                .formatted(version));

    assertEquals(ModelProfileStatus.ACTIVE, profile.status());
    assertEquals(version, profile.version());
  }
}
