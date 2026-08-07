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

import de.markusbordihn.easymodelentities.data.model.CubeFaceVisibility;
import de.markusbordihn.easymodelentities.data.model.ModelCubeFaceUvs;
import de.markusbordihn.easymodelentities.data.model.Vec3f;
import java.util.Objects;

public record BakedModelCube(
    int[] uvOffset,
    ModelCubeFaceUvs faceUvs,
    Vec3f position,
    Vec3f dimensions,
    boolean mirror,
    int textureIndex,
    CubeFaceVisibility faceVisibility) {

  public BakedModelCube {
    Objects.requireNonNull(faceUvs, "faceUvs");
    Objects.requireNonNull(position, "position");
    Objects.requireNonNull(dimensions, "dimensions");
    Objects.requireNonNull(faceVisibility, "faceVisibility");
    uvOffset = Objects.requireNonNull(uvOffset, "uvOffset").clone();
    if (mirror) {
      faceUvs = faceUvs.mirrorU();
      mirror = false;
    }
  }

  public BakedModelCube(
      int[] uvOffset, ModelCubeFaceUvs faceUvs, Vec3f position, Vec3f dimensions, boolean mirror) {
    this(uvOffset, faceUvs, position, dimensions, mirror, 0, CubeFaceVisibility.ALL);
  }

  public BakedModelCube withFaceVisibility(CubeFaceVisibility faceVisibility) {
    return new BakedModelCube(
        this.uvOffset,
        this.faceUvs,
        this.position,
        this.dimensions,
        this.mirror,
        this.textureIndex,
        faceVisibility);
  }
}
