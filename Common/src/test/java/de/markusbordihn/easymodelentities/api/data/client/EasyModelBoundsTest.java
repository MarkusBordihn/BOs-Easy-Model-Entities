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

package de.markusbordihn.easymodelentities.api.data.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import de.markusbordihn.easymodelentities.api.data.EasyModelVec3f;
import org.junit.jupiter.api.Test;

class EasyModelBoundsTest {

  @Test
  void exposesDimensionsCenterAndScaleWithApiVectors() {
    EasyModelBounds bounds =
        new EasyModelBounds(
            new EasyModelVec3f(-1.0f, 2.0f, 3.0f), new EasyModelVec3f(5.0f, 8.0f, 12.0f));

    assertEquals(6.0f, bounds.sizeX());
    assertEquals(6.0f, bounds.sizeY());
    assertEquals(9.0f, bounds.sizeZ());
    assertEquals(9.0f, bounds.maxSize());
    assertEquals(new EasyModelVec3f(2.0f, 5.0f, 7.5f), bounds.center());
    assertEquals(new EasyModelVec3f(-2.0f, 4.0f, 6.0f), bounds.scaled(2.0f).min());
  }

  @Test
  void rejectsInvalidBoundsAndScale() {
    assertThrows(
        IllegalArgumentException.class,
        () -> new EasyModelBounds(new EasyModelVec3f(1.0f, 0.0f, 0.0f), EasyModelVec3f.ZERO));
    assertThrows(IllegalArgumentException.class, () -> EasyModelBounds.EMPTY.scaled(-1.0f));
  }
}
