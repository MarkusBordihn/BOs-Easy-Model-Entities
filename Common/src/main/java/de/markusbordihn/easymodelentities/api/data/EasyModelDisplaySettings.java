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

public final class EasyModelDisplaySettings {

  public static final float MIN_OPACITY = 0.0f;
  public static final float MAX_OPACITY = 1.0f;
  public static final float DEFAULT_OPACITY = MAX_OPACITY;
  public static final float NO_OPACITY = -1.0f;
  public static final int MIN_LIGHT_LEVEL = 0;
  public static final int MAX_LIGHT_LEVEL = 15;
  public static final int NO_LIGHT_LEVEL = -1;

  private EasyModelDisplaySettings() {}

  public static void requireOpacity(float opacity) {
    if (!Float.isFinite(opacity) || opacity < MIN_OPACITY || opacity > MAX_OPACITY) {
      throw new IllegalArgumentException(
          "opacity must be a finite value between " + MIN_OPACITY + " and " + MAX_OPACITY + ".");
    }
  }

  public static void requireLightLevel(int lightLevel) {
    if (lightLevel < MIN_LIGHT_LEVEL || lightLevel > MAX_LIGHT_LEVEL) {
      throw new IllegalArgumentException(
          "lightLevel must be between " + MIN_LIGHT_LEVEL + " and " + MAX_LIGHT_LEVEL + ".");
    }
  }

  public static float clampOpacity(float opacity) {
    if (!Float.isFinite(opacity)) {
      return DEFAULT_OPACITY;
    }

    return Math.min(MAX_OPACITY, Math.max(MIN_OPACITY, opacity));
  }

  public static float clampOpacityOverride(float opacity) {
    if (!Float.isFinite(opacity) || opacity < MIN_OPACITY) {
      return NO_OPACITY;
    }

    return Math.min(MAX_OPACITY, opacity);
  }

  public static boolean hasOpacityOverride(float opacity) {
    return Float.isFinite(opacity) && opacity >= MIN_OPACITY;
  }

  public static boolean hasLightLevelOverride(int lightLevel) {
    return lightLevel >= MIN_LIGHT_LEVEL;
  }

  public static int clampLightLevel(int lightLevel) {
    if (lightLevel <= NO_LIGHT_LEVEL) {
      return NO_LIGHT_LEVEL;
    }

    return Math.min(MAX_LIGHT_LEVEL, lightLevel);
  }
}
