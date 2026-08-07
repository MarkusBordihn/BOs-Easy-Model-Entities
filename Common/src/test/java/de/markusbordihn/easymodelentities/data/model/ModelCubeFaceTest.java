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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import de.markusbordihn.easymodelentities.data.model.bake.BakedModelCube;
import org.junit.jupiter.api.Test;

class ModelCubeFaceTest {

  @Test
  void tagNamesAreStable() {
    assertEquals("north", ModelCubeFace.NORTH.getTagName());
    assertEquals("east", ModelCubeFace.EAST.getTagName());
    assertEquals("south", ModelCubeFace.SOUTH.getTagName());
    assertEquals("west", ModelCubeFace.WEST.getTagName());
    assertEquals("up", ModelCubeFace.UP.getTagName());
    assertEquals("down", ModelCubeFace.DOWN.getTagName());
  }

  @Test
  void bakedCubeNormalizesMirroredUvsOnce() {
    ModelCubeFaceUvs faceUvs =
        new ModelCubeFaceUvs(
            new FaceUv(1.0f, 2.0f, 3.0f, 4.0f),
            new FaceUv(5.0f, 6.0f, 7.0f, 8.0f),
            new FaceUv(9.0f, 10.0f, 11.0f, 12.0f),
            new FaceUv(13.0f, 14.0f, 15.0f, 16.0f),
            new FaceUv(17.0f, 18.0f, 19.0f, 20.0f),
            new FaceUv(21.0f, 22.0f, 23.0f, 24.0f));

    BakedModelCube cube =
        new BakedModelCube(
            new int[] {0, 0}, faceUvs, Vec3f.ZERO, new Vec3f(1.0f, 1.0f, 1.0f), true);

    assertFalse(cube.mirror());
    assertEquals(faceUvs.mirrorU(), cube.faceUvs());
    assertEquals(new FaceUv(15.0f, 14.0f, 13.0f, 16.0f), cube.faceUvs().east());
    assertEquals(new FaceUv(7.0f, 6.0f, 5.0f, 8.0f), cube.faceUvs().west());
  }
}
