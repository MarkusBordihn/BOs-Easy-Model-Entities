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

package de.markusbordihn.easymodelentities.data.model.decoder;

import de.markusbordihn.easymodelentities.data.model.CubeFaceVisibility;
import de.markusbordihn.easymodelentities.data.model.ModelCubeFaceUvs;
import de.markusbordihn.easymodelentities.data.model.Vec3f;
import java.util.Objects;

public record DecodedModelCube(
    int[] uvOffset,
    ModelCubeFaceUvs faceUvs,
    Vec3f position,
    Vec3f dimensions,
    boolean mirror,
    String name,
    Vec3f rotationOrigin,
    Vec3f rotation,
    Vec3f rotatedPosition,
    int textureIndex,
    CubeFaceVisibility faceVisibility) {

  public DecodedModelCube {
    Objects.requireNonNull(name, "name");
    Objects.requireNonNull(faceUvs, "faceUvs");
    Objects.requireNonNull(position, "position");
    Objects.requireNonNull(dimensions, "dimensions");
    Objects.requireNonNull(rotationOrigin, "rotationOrigin");
    Objects.requireNonNull(rotation, "rotation");
    Objects.requireNonNull(rotatedPosition, "rotatedPosition");
    Objects.requireNonNull(faceVisibility, "faceVisibility");
    uvOffset = uvOffset.clone();
  }

  public DecodedModelCube(int[] uvOffset, Vec3f position, Vec3f dimensions, boolean mirror) {
    this(
        uvOffset,
        ModelCubeFaceUvs.fromBoxUv(
            uvOffset, new float[] {dimensions.x(), dimensions.y(), dimensions.z()}),
        position,
        dimensions,
        mirror,
        "cube",
        Vec3f.ZERO,
        Vec3f.ZERO,
        position,
        0,
        CubeFaceVisibility.ALL);
  }

  public boolean hasRotation() {
    return !this.rotation.isZero();
  }
}
