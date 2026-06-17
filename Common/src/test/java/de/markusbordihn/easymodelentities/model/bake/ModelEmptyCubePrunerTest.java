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

package de.markusbordihn.easymodelentities.model.bake;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import de.markusbordihn.easymodelentities.data.model.CubeFaceVisibility;
import de.markusbordihn.easymodelentities.data.model.ModelCubeFace;
import de.markusbordihn.easymodelentities.data.model.ModelCubeFaceUvs;
import de.markusbordihn.easymodelentities.data.model.Vec3f;
import de.markusbordihn.easymodelentities.data.model.bake.BakedModelCube;
import de.markusbordihn.easymodelentities.data.model.bake.BakedModelPart;
import java.util.List;
import org.junit.jupiter.api.Test;

class ModelEmptyCubePrunerTest {

  private static BakedModelCube cube(CubeFaceVisibility visibility) {
    int[] uvOffset = {0, 0};
    return new BakedModelCube(
        uvOffset,
        ModelCubeFaceUvs.fromBoxUv(uvOffset, new float[] {1.0f, 1.0f, 1.0f}),
        Vec3f.ZERO,
        new Vec3f(1.0f, 1.0f, 1.0f),
        false,
        0,
        visibility);
  }

  private static BakedModelPart part(
      String name, List<BakedModelCube> cubes, List<BakedModelPart> children) {
    return new BakedModelPart(name, Vec3f.ZERO, Vec3f.ZERO, cubes, children);
  }

  @Test
  void dropsFullyHiddenCubes() {
    BakedModelCube visible = cube(CubeFaceVisibility.ALL);
    BakedModelPart root =
        part(
            "root",
            List.of(visible, cube(CubeFaceVisibility.NONE), cube(CubeFaceVisibility.NONE)),
            List.of());

    ModelEmptyCubePruner.Result result = ModelEmptyCubePruner.prune(List.of(root));

    assertEquals(2, result.droppedCubes());
    assertEquals(List.of(visible), result.rootParts().get(0).cubes());
  }

  @Test
  void dropsHiddenCubesInChildParts() {
    BakedModelCube visible = cube(CubeFaceVisibility.ALL);
    BakedModelPart child = part("leg", List.of(cube(CubeFaceVisibility.NONE)), List.of());
    BakedModelPart root = part("root", List.of(visible), List.of(child));

    ModelEmptyCubePruner.Result result = ModelEmptyCubePruner.prune(List.of(root));

    assertEquals(1, result.droppedCubes());
    BakedModelPart prunedRoot = result.rootParts().get(0);
    assertEquals(1, prunedRoot.children().size());
    assertEquals(List.of(), prunedRoot.children().get(0).cubes());
  }

  @Test
  void keepsTreeUntouchedWhenNothingHidden() {
    BakedModelPart root = part("root", List.of(cube(CubeFaceVisibility.ALL)), List.of());
    List<BakedModelPart> rootParts = List.of(root);

    ModelEmptyCubePruner.Result result = ModelEmptyCubePruner.prune(rootParts);

    assertEquals(0, result.droppedCubes());
    assertSame(rootParts, result.rootParts());
  }

  @Test
  void keepsPartialVisibilityCubes() {
    BakedModelCube partial = cube(CubeFaceVisibility.ALL.without(ModelCubeFace.UP));
    BakedModelPart root = part("root", List.of(partial), List.of());

    ModelEmptyCubePruner.Result result = ModelEmptyCubePruner.prune(List.of(root));

    assertEquals(0, result.droppedCubes());
    assertEquals(1, result.rootParts().get(0).cubes().size());
  }
}
