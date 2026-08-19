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

package de.markusbordihn.easymodelentities.client.render;

import static org.junit.jupiter.api.Assertions.assertEquals;

import de.markusbordihn.easymodelentities.data.model.Vec3f;
import net.minecraft.world.phys.AABB;
import org.junit.jupiter.api.Test;

class EasyModelCullingBoundsTest {

  private static void assertBounds(
      AABB bounds, double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
    assertEquals(minX, bounds.minX, 1.0e-6);
    assertEquals(minY, bounds.minY, 1.0e-6);
    assertEquals(minZ, bounds.minZ, 1.0e-6);
    assertEquals(maxX, bounds.maxX, 1.0e-6);
    assertEquals(maxY, bounds.maxY, 1.0e-6);
    assertEquals(maxZ, bounds.maxZ, 1.0e-6);
  }

  @Test
  void rotatesVisibleBoundsOffsetWithEntityYaw() {
    Vec3f offset = new Vec3f(2.0f, 1.0f, 3.0f);
    AABB unrotated =
        EasyModelCullingBounds.visibleBounds(10.0, 20.0, 30.0, 4.0f, 5.0f, offset, 0.0f);
    AABB quarterTurn =
        EasyModelCullingBounds.visibleBounds(10.0, 20.0, 30.0, 4.0f, 5.0f, offset, 90.0f);

    assertBounds(unrotated, 10.0, 21.0, 31.0, 14.0, 26.0, 35.0);
    assertBounds(quarterTurn, 5.0, 21.0, 30.0, 9.0, 26.0, 34.0);
  }
}
