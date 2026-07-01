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

package de.markusbordihn.easymodelentities.model.bake;

import de.markusbordihn.easymodelentities.data.model.Vec3f;
import de.markusbordihn.easymodelentities.data.model.bake.BakedModelCube;
import de.markusbordihn.easymodelentities.data.model.bake.BakedModelPart;
import de.markusbordihn.easymodelentities.data.model.bake.ModelBounds;
import java.util.List;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public final class ModelBoundsCalculator {

  private static final float PIXEL = 1.0f / 16.0f;

  private ModelBoundsCalculator() {}

  public static ModelBounds compute(List<BakedModelPart> rootParts) {
    float[] box = {
      Float.POSITIVE_INFINITY,
      Float.POSITIVE_INFINITY,
      Float.POSITIVE_INFINITY,
      Float.NEGATIVE_INFINITY,
      Float.NEGATIVE_INFINITY,
      Float.NEGATIVE_INFINITY
    };
    Matrix4f base = new Matrix4f();
    for (BakedModelPart part : rootParts) {
      accumulate(part, base, box);
    }
    if (box[0] > box[3]) {
      return ModelBounds.EMPTY;
    }
    return new ModelBounds(
        new Vec3f(box[0] * PIXEL, box[1] * PIXEL, box[2] * PIXEL),
        new Vec3f(box[3] * PIXEL, box[4] * PIXEL, box[5] * PIXEL));
  }

  private static void accumulate(BakedModelPart part, Matrix4f parent, float[] box) {
    Matrix4f matrix = new Matrix4f(parent);
    Vec3f offset = part.offset();
    matrix.translate(offset.x(), offset.y(), offset.z());
    Vec3f rotation = part.rotation();
    if (rotation.z() != 0.0f) {
      matrix.rotateZ(rotation.z());
    }
    if (rotation.y() != 0.0f) {
      matrix.rotateY(rotation.y());
    }
    if (rotation.x() != 0.0f) {
      matrix.rotateX(rotation.x());
    }

    for (BakedModelCube cube : part.cubes()) {
      Vec3f position = cube.position();
      Vec3f dimensions = cube.dimensions();
      for (int xi = 0; xi < 2; xi++) {
        for (int yi = 0; yi < 2; yi++) {
          for (int zi = 0; zi < 2; zi++) {
            Vector3f corner =
                new Vector3f(
                    position.x() + (xi == 1 ? dimensions.x() : 0.0f),
                    position.y() + (yi == 1 ? dimensions.y() : 0.0f),
                    position.z() + (zi == 1 ? dimensions.z() : 0.0f));
            matrix.transformPosition(corner);
            box[0] = Math.min(box[0], corner.x);
            box[1] = Math.min(box[1], corner.y);
            box[2] = Math.min(box[2], corner.z);
            box[3] = Math.max(box[3], corner.x);
            box[4] = Math.max(box[4], corner.y);
            box[5] = Math.max(box[5], corner.z);
          }
        }
      }
    }

    for (BakedModelPart child : part.children()) {
      accumulate(child, matrix, box);
    }
  }
}
