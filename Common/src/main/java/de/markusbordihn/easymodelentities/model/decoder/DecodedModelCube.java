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

package de.markusbordihn.easymodelentities.model.decoder;

import java.util.Objects;

public record DecodedModelCube(
    int[] uvOffset,
    float[] position,
    float[] dimensions,
    boolean mirror,
    String name,
    float[] rotationOrigin,
    float[] rotation,
    float[] rotatedPosition) {

  public DecodedModelCube {
    Objects.requireNonNull(name, "name");
    uvOffset = uvOffset.clone();
    position = position.clone();
    dimensions = dimensions.clone();
    rotationOrigin = rotationOrigin.clone();
    rotation = rotation.clone();
    rotatedPosition = rotatedPosition.clone();
  }

  public DecodedModelCube(int[] uvOffset, float[] position, float[] dimensions, boolean mirror) {
    this(
        uvOffset,
        position,
        dimensions,
        mirror,
        "cube",
        new float[] {0.0f, 0.0f, 0.0f},
        new float[] {0.0f, 0.0f, 0.0f},
        position);
  }

  public boolean hasRotation() {
    return this.rotation[0] != 0.0f || this.rotation[1] != 0.0f || this.rotation[2] != 0.0f;
  }
}
