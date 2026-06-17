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
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package de.markusbordihn.easymodelentities.data.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class CubeFaceVisibilityTest {

  @Test
  void allHasEverySideVisible() {
    assertTrue(CubeFaceVisibility.ALL.isAll());
    assertEquals(6, CubeFaceVisibility.ALL.visibleCount());
    for (ModelCubeFace face : ModelCubeFace.values()) {
      assertTrue(CubeFaceVisibility.ALL.isVisible(face));
    }
  }

  @Test
  void noneHasNoVisibleSide() {
    assertTrue(CubeFaceVisibility.NONE.isEmpty());
    assertEquals(0, CubeFaceVisibility.NONE.visibleCount());
    for (ModelCubeFace face : ModelCubeFace.values()) {
      assertFalse(CubeFaceVisibility.NONE.isVisible(face));
    }
  }

  @Test
  void withoutClearsOnlyOneSide() {
    CubeFaceVisibility visibility = CubeFaceVisibility.ALL.without(ModelCubeFace.UP);
    assertFalse(visibility.isVisible(ModelCubeFace.UP));
    assertTrue(visibility.isVisible(ModelCubeFace.DOWN));
    assertEquals(5, visibility.visibleCount());
  }

  @Test
  void eachSideMapsToADistinctBit() {
    for (ModelCubeFace face : ModelCubeFace.values()) {
      CubeFaceVisibility cleared = CubeFaceVisibility.ALL.without(face);
      assertEquals(5, cleared.visibleCount());
      assertFalse(cleared.isVisible(face));
      for (ModelCubeFace other : ModelCubeFace.values()) {
        if (other != face) {
          assertTrue(cleared.isVisible(other));
        }
      }
    }
  }
}
