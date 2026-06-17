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

import de.markusbordihn.easymodelentities.data.model.bake.BakedModelCube;
import de.markusbordihn.easymodelentities.data.model.bake.BakedModelPart;
import java.util.ArrayList;
import java.util.List;

final class ModelEmptyCubePruner {

  private ModelEmptyCubePruner() {}

  static Result prune(List<BakedModelPart> rootParts) {
    int[] dropped = {0};
    List<BakedModelPart> pruned = pruneParts(rootParts, dropped);
    return dropped[0] == 0 ? new Result(rootParts, 0) : new Result(pruned, dropped[0]);
  }

  private static List<BakedModelPart> pruneParts(List<BakedModelPart> parts, int[] dropped) {
    List<BakedModelPart> result = new ArrayList<>(parts.size());
    for (BakedModelPart part : parts) {
      List<BakedModelCube> cubes = new ArrayList<>(part.cubes().size());
      for (BakedModelCube cube : part.cubes()) {
        if (cube.faceVisibility().isEmpty()) {
          dropped[0]++;
        } else {
          cubes.add(cube);
        }
      }
      result.add(
          new BakedModelPart(
              part.name(),
              part.offset(),
              part.rotation(),
              cubes,
              pruneParts(part.children(), dropped)));
    }
    return result;
  }

  record Result(List<BakedModelPart> rootParts, int droppedCubes) {}
}
