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

package de.markusbordihn.easymodelentities.data.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class Vec3fTest {

  @Test
  void scaleMultipliesEachAxis() {
    Vec3f scaled = new Vec3f(1.0f, -2.0f, 3.0f).scale(2.0f);
    assertEquals(2.0f, scaled.x());
    assertEquals(-4.0f, scaled.y());
    assertEquals(6.0f, scaled.z());
  }

  @Test
  void scaleByOneKeepsComponents() {
    Vec3f vector = new Vec3f(0.1f, 0.2f, 0.3f);
    Vec3f scaled = vector.scale(1.0f);
    assertEquals(vector.x(), scaled.x());
    assertEquals(vector.y(), scaled.y());
    assertEquals(vector.z(), scaled.z());
  }

  @Test
  void rejectsNonFiniteComponentsAndResults() {
    assertThrows(IllegalArgumentException.class, () -> new Vec3f(Float.NaN, 0.0f, 0.0f));
    assertThrows(IllegalArgumentException.class, () -> Vec3f.ZERO.scale(Float.POSITIVE_INFINITY));
  }
}
