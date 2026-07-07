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

package de.markusbordihn.easymodelentities.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class EasyModelApiContractTest {

  @Test
  void exposesSchemaVersion() {
    assertEquals("0.2.0", EasyModelApiContract.schemaVersion());
  }

  @Test
  void exposesHardAndSoftBudgets() {
    assertTrue(EasyModelApiContract.maxBoneCount() > EasyModelApiContract.softBoneCount());
    assertTrue(EasyModelApiContract.maxCubeCount() > EasyModelApiContract.softCubeCount());
    assertTrue(
        EasyModelApiContract.maxHierarchyDepth() > EasyModelApiContract.softHierarchyDepth());
    assertTrue(
        EasyModelApiContract.maxModelFileSizeBytes()
            > EasyModelApiContract.softModelFileSizeBytes());
    assertEquals(16, EasyModelApiContract.maxAnimationCount());
  }

  @Test
  void exposesValueEnumNames() {
    assertTrue(EasyModelApiContract.bodyTypes().contains("biped"));
    assertTrue(EasyModelApiContract.movementTypes().contains("ground"));
    assertTrue(EasyModelApiContract.modelTypes().contains("block_entity"));
    assertTrue(EasyModelApiContract.animationModes().contains("automatic"));
    assertTrue(EasyModelApiContract.gaits().contains("natural"));
    assertTrue(EasyModelApiContract.presetTypes().contains("custom"));
    assertTrue(EasyModelApiContract.blockEntityPresetTypes().contains("ticking"));
    assertTrue(EasyModelApiContract.behaviorModes().contains("ambient"));
  }

  @Test
  void exposesStatusAndDiagnosticCodes() {
    assertTrue(EasyModelApiContract.serverStatuses().contains("ACTIVE"));
    assertTrue(EasyModelApiContract.renderStatuses().contains("CLIENT_ASSET_MISMATCH"));
    assertTrue(EasyModelApiContract.diagnosticSeverities().contains("ERROR"));
  }

  @Test
  void exposesImmutableLists() {
    assertFalse(EasyModelApiContract.bodyTypes().isEmpty());
    assertEquals(EasyModelApiContract.bodyTypes(), EasyModelApiContract.bodyTypes());
  }
}
