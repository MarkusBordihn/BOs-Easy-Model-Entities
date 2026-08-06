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

package de.markusbordihn.easymodelentities.data.model.bake;

import de.markusbordihn.easymodelentities.data.model.Vec3f;
import java.util.Objects;

public record ModelBounds(Vec3f min, Vec3f max) {

  public static final ModelBounds EMPTY = new ModelBounds(Vec3f.ZERO, Vec3f.ZERO);

  public ModelBounds {
    Objects.requireNonNull(min, "min");
    Objects.requireNonNull(max, "max");
    if (min.x() > max.x() || min.y() > max.y() || min.z() > max.z()) {
      throw new IllegalArgumentException("Minimum bounds must not exceed maximum bounds.");
    }
  }

  public float sizeX() {
    return this.max.x() - this.min.x();
  }

  public float sizeY() {
    return this.max.y() - this.min.y();
  }

  public float sizeZ() {
    return this.max.z() - this.min.z();
  }

  public float maxSize() {
    return Math.max(sizeX(), Math.max(sizeY(), sizeZ()));
  }

  public Vec3f center() {
    return new Vec3f(
        (this.min.x() + this.max.x()) / 2.0f,
        (this.min.y() + this.max.y()) / 2.0f,
        (this.min.z() + this.max.z()) / 2.0f);
  }

  public ModelBounds scaled(float factor) {
    if (!Float.isFinite(factor) || factor < 0.0f) {
      throw new IllegalArgumentException("factor must be a finite non-negative value.");
    }
    return factor == 1.0f ? this : new ModelBounds(this.min.scale(factor), this.max.scale(factor));
  }

  public boolean isEmpty() {
    return sizeX() <= 0.0f && sizeY() <= 0.0f && sizeZ() <= 0.0f;
  }
}
