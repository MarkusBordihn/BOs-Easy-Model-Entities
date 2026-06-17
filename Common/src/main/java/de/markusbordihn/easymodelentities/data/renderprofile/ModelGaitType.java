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

import java.util.Locale;
import java.util.Optional;

public enum ModelGaitType {
  NATURAL(1.0f, 1.0f),
  FELINE(0.7f, 1.35f),
  UNGULATE(1.3f, 0.8f);

  private final String serializedName = this.name().toLowerCase(Locale.ROOT);
  private final float strideScale;
  private final float cadenceScale;

  ModelGaitType(float strideScale, float cadenceScale) {
    this.strideScale = strideScale;
    this.cadenceScale = cadenceScale;
  }

  public static Optional<ModelGaitType> bySerializedName(String serializedName) {
    if (serializedName == null) {
      return Optional.empty();
    }

    String normalizedName = serializedName.toLowerCase(Locale.ROOT);
    for (ModelGaitType gaitType : values()) {
      if (gaitType.serializedName.equals(normalizedName)) {
        return Optional.of(gaitType);
      }
    }

    return Optional.empty();
  }

  public String getSerializedName() {
    return this.serializedName;
  }

  public float strideScale() {
    return this.strideScale;
  }

  public float cadenceScale() {
    return this.cadenceScale;
  }
}
