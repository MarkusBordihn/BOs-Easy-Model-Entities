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

public record FaceUv(float minU, float minV, float maxU, float maxV) {

  public FaceUv {
    requireFinite(minU, "minU");
    requireFinite(minV, "minV");
    requireFinite(maxU, "maxU");
    requireFinite(maxV, "maxV");
  }

  public static FaceUv of(float[] uv) {
    if (uv == null || uv.length != 4) {
      throw new IllegalArgumentException("UV must have 4 values.");
    }

    return new FaceUv(uv[0], uv[1], uv[2], uv[3]);
  }

  private static void requireFinite(float value, String name) {
    if (!Float.isFinite(value)) {
      throw new IllegalArgumentException(name + " must be finite.");
    }
  }

  public FaceUv scale(float textureWidth, float textureHeight) {
    if (!Float.isFinite(textureWidth) || textureWidth <= 0.0f) {
      throw new IllegalArgumentException("textureWidth must be a finite positive value.");
    }
    if (!Float.isFinite(textureHeight) || textureHeight <= 0.0f) {
      throw new IllegalArgumentException("textureHeight must be a finite positive value.");
    }
    return new FaceUv(
        minU / textureWidth, minV / textureHeight, maxU / textureWidth, maxV / textureHeight);
  }

  public FaceUv mirrorU() {
    return new FaceUv(this.maxU, this.minV, this.minU, this.maxV);
  }
}
