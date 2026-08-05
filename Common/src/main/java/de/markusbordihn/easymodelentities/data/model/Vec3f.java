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

public record Vec3f(float x, float y, float z) {

  public static final Vec3f ZERO = new Vec3f(0.0f, 0.0f, 0.0f);

  public Vec3f {
    requireFinite(x, "x");
    requireFinite(y, "y");
    requireFinite(z, "z");
  }

  public static Vec3f of(float[] values) {
    if (values == null || values.length != 3) {
      throw new IllegalArgumentException("Vec3f requires 3 values.");
    }

    return new Vec3f(values[0], values[1], values[2]);
  }

  private static void requireFinite(float value, String name) {
    if (!Float.isFinite(value)) {
      throw new IllegalArgumentException(name + " must be finite.");
    }
  }

  public boolean isZero() {
    return this.x == 0.0f && this.y == 0.0f && this.z == 0.0f;
  }

  public Vec3f add(Vec3f other) {
    return new Vec3f(this.x + other.x, this.y + other.y, this.z + other.z);
  }

  public Vec3f scale(float factor) {
    return new Vec3f(this.x * factor, this.y * factor, this.z * factor);
  }
}
