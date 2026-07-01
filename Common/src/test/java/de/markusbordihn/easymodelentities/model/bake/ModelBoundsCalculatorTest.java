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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.markusbordihn.easymodelentities.data.model.ModelCubeFaceUvs;
import de.markusbordihn.easymodelentities.data.model.Vec3f;
import de.markusbordihn.easymodelentities.data.model.bake.BakedModelCube;
import de.markusbordihn.easymodelentities.data.model.bake.BakedModelPart;
import de.markusbordihn.easymodelentities.data.model.bake.ModelBounds;
import java.util.List;
import org.junit.jupiter.api.Test;

class ModelBoundsCalculatorTest {

  private static final float DELTA = 1.0e-4f;

  private static BakedModelCube cube(Vec3f position, Vec3f dimensions) {
    return new BakedModelCube(
        new int[] {0, 0},
        ModelCubeFaceUvs.fromBoxUv(new int[] {0, 0}, new float[] {0.0f, 0.0f, 0.0f}),
        position,
        dimensions,
        false);
  }

  private static BakedModelPart part(Vec3f offset, Vec3f rotation, List<BakedModelCube> cubes) {
    return new BakedModelPart("part", offset, rotation, cubes, List.of());
  }

  @Test
  void emptyModelHasEmptyBounds() {
    assertTrue(ModelBoundsCalculator.compute(List.of()).isEmpty());
  }

  @Test
  void axisAlignedCubeMapsPixelsToBlocks() {
    ModelBounds bounds =
        ModelBoundsCalculator.compute(
            List.of(
                part(
                    Vec3f.ZERO,
                    Vec3f.ZERO,
                    List.of(cube(Vec3f.ZERO, new Vec3f(16.0f, 8.0f, 4.0f))))));

    assertEquals(0.0f, bounds.min().x(), DELTA);
    assertEquals(0.0f, bounds.min().y(), DELTA);
    assertEquals(1.0f, bounds.max().x(), DELTA);
    assertEquals(0.5f, bounds.max().y(), DELTA);
    assertEquals(0.25f, bounds.max().z(), DELTA);
    assertEquals(1.0f, bounds.maxSize(), DELTA);
    assertEquals(0.5f, bounds.center().x(), DELTA);
  }

  @Test
  void partOffsetShiftsBounds() {
    ModelBounds bounds =
        ModelBoundsCalculator.compute(
            List.of(
                part(
                    new Vec3f(8.0f, 0.0f, 0.0f),
                    Vec3f.ZERO,
                    List.of(cube(Vec3f.ZERO, new Vec3f(8.0f, 8.0f, 8.0f))))));

    assertEquals(0.5f, bounds.min().x(), DELTA);
    assertEquals(1.0f, bounds.max().x(), DELTA);
  }

  @Test
  void zRotationIsAppliedToCubeCorners() {
    ModelBounds bounds =
        ModelBoundsCalculator.compute(
            List.of(
                part(
                    Vec3f.ZERO,
                    new Vec3f(0.0f, 0.0f, (float) (Math.PI / 2.0)),
                    List.of(cube(Vec3f.ZERO, new Vec3f(16.0f, 4.0f, 4.0f))))));

    assertEquals(-0.25f, bounds.min().x(), DELTA);
    assertEquals(0.0f, bounds.max().x(), DELTA);
    assertEquals(0.0f, bounds.min().y(), DELTA);
    assertEquals(1.0f, bounds.max().y(), DELTA);
  }
}
