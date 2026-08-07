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

package de.markusbordihn.easymodelentities.api.data;

import java.util.Objects;

public record EasyModelVec3f(float x, float y, float z) {

  public static final EasyModelVec3f ZERO = new EasyModelVec3f(0.0f, 0.0f, 0.0f);

  public EasyModelVec3f {
    requireFinite(x, "x");
    requireFinite(y, "y");
    requireFinite(z, "z");
  }

  private static void requireFinite(float value, String name) {
    if (!Float.isFinite(value)) {
      throw new IllegalArgumentException(name + " must be finite.");
    }
  }

  public boolean isZero() {
    return this.x == 0.0f && this.y == 0.0f && this.z == 0.0f;
  }

  public EasyModelVec3f add(EasyModelVec3f other) {
    Objects.requireNonNull(other, "other");
    return new EasyModelVec3f(this.x + other.x, this.y + other.y, this.z + other.z);
  }

  public EasyModelVec3f scale(float factor) {
    requireFinite(factor, "factor");
    return new EasyModelVec3f(this.x * factor, this.y * factor, this.z * factor);
  }
}
