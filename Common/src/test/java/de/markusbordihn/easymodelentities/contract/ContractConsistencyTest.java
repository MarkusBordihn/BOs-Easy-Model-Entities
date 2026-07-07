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

package de.markusbordihn.easymodelentities.contract;

import static org.junit.jupiter.api.Assertions.assertEquals;

import de.markusbordihn.easymodelentities.api.EasyModelApiContract;
import java.util.Set;
import org.junit.jupiter.api.Test;

class ContractConsistencyTest {

  @Test
  void schemaVersionMatchesDocumentedContract() {
    assertEquals("0.2.0", EasyModelApiContract.schemaVersion());
  }

  @Test
  void budgetsMatchDocumentedContract() {
    assertEquals(2 * 1024 * 1024, EasyModelApiContract.maxModelFileSizeBytes());
    assertEquals(1024 * 1024, EasyModelApiContract.softModelFileSizeBytes());
    assertEquals(2048, EasyModelApiContract.maxTextureSize());
    assertEquals(128, EasyModelApiContract.softTextureSize());
    assertEquals(128, EasyModelApiContract.maxBoneCount());
    assertEquals(96, EasyModelApiContract.softBoneCount());
    assertEquals(512, EasyModelApiContract.maxCubeCount());
    assertEquals(384, EasyModelApiContract.softCubeCount());
    assertEquals(32, EasyModelApiContract.maxHierarchyDepth());
    assertEquals(24, EasyModelApiContract.softHierarchyDepth());
    assertEquals(16, EasyModelApiContract.maxAnimationCount());
  }

  @Test
  void serverStatusSetMatchesDocumentedContract() {
    assertEquals(
        Set.of(
            "ACTIVE",
            "INVALID_JSON",
            "INVALID_SCHEMA_VERSION",
            "INVALID_MODEL_TYPE",
            "INVALID_RESOURCE_LOCATION",
            "INVALID_DIMENSIONS",
            "INVALID_HOST_ENTITY",
            "INVALID_HOST_BLOCK_ENTITY",
            "DISABLED"),
        Set.copyOf(EasyModelApiContract.serverStatuses()));
  }

  @Test
  void renderStatusSetMatchesDocumentedContract() {
    assertEquals(
        Set.of(
            "ACTIVE",
            "INVALID_JSON",
            "INVALID_SCHEMA_VERSION",
            "INVALID_RESOURCE_LOCATION",
            "INVALID_BODY_TYPE",
            "INVALID_ANIMATION_MODE",
            "INVALID_RENDER_SETTINGS",
            "MISSING_RENDER_PROFILE",
            "MISSING_MODEL",
            "MISSING_TEXTURE",
            "MODEL_DECODE_FAILED",
            "CLIENT_ASSET_MISMATCH",
            "CLIENT_BODY_TYPE_MISMATCH",
            "FALLBACK_ACTIVE"),
        Set.copyOf(EasyModelApiContract.renderStatuses()));
  }

  @Test
  void modelTypeSetMatchesDocumentedContract() {
    assertEquals(Set.of("entity", "block_entity"), Set.copyOf(EasyModelApiContract.modelTypes()));
  }

  @Test
  void bodyTypeSetMatchesDocumentedContract() {
    assertEquals(
        Set.of(
            "static",
            "biped",
            "quadruped",
            "aquatic",
            "amphibious",
            "winged",
            "winged_humanoid",
            "arthropod",
            "cuboid",
            "floating"),
        Set.copyOf(EasyModelApiContract.bodyTypes()));
  }

  @Test
  void movementTypeSetMatchesDocumentedContract() {
    assertEquals(
        Set.of("ground", "water", "amphibious", "static"),
        Set.copyOf(EasyModelApiContract.movementTypes()));
  }

  @Test
  void animationEnumSetsMatchDocumentedContract() {
    assertEquals(
        Set.of("automatic", "random_idle", "none"),
        Set.copyOf(EasyModelApiContract.animationModes()));
    assertEquals(Set.of("natural", "feline", "ungulate"), Set.copyOf(EasyModelApiContract.gaits()));
  }

  @Test
  void behaviorModeSetMatchesDocumentedContract() {
    assertEquals(
        Set.of("idle_only", "ambient", "static", "external_owner"),
        Set.copyOf(EasyModelApiContract.behaviorModes()));
  }

  @Test
  void blockEntityPresetTypeSetMatchesDocumentedContract() {
    assertEquals(
        Set.of("static", "ticking", "animated", "animated_randomly"),
        Set.copyOf(EasyModelApiContract.blockEntityPresetTypes()));
  }

  @Test
  void diagnosticSeveritySetMatchesDocumentedContract() {
    assertEquals(
        Set.of("INFO", "WARNING", "ERROR"),
        Set.copyOf(EasyModelApiContract.diagnosticSeverities()));
  }
}
