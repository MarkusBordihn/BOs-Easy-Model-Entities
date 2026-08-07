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

package de.markusbordihn.easymodelentities.data.renderprofile;

import de.markusbordihn.easymodelentities.data.model.Vec3f;

public record ModelRenderSettings(
    float scale,
    float shadowRadius,
    float visibleBoundsWidth,
    float visibleBoundsHeight,
    Vec3f visibleBoundsOffset) {

  public ModelRenderSettings {
    requirePositiveFinite(scale, "scale");
    requireNonNegativeFinite(shadowRadius, "shadowRadius");
    requireNonNegativeFinite(visibleBoundsWidth, "visibleBoundsWidth");
    requireNonNegativeFinite(visibleBoundsHeight, "visibleBoundsHeight");
    if (visibleBoundsOffset == null) {
      visibleBoundsOffset = Vec3f.ZERO;
    }
  }

  private static void requirePositiveFinite(float value, String name) {
    if (!Float.isFinite(value) || value <= 0.0f) {
      throw new IllegalArgumentException(name + " must be a finite positive value.");
    }
  }

  private static void requireNonNegativeFinite(float value, String name) {
    if (!Float.isFinite(value) || value < 0.0f) {
      throw new IllegalArgumentException(name + " must be a finite non-negative value.");
    }
  }

  public boolean hasVisibleBounds() {
    return this.visibleBoundsWidth > 0.0f && this.visibleBoundsHeight > 0.0f;
  }
}
