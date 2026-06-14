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

package de.markusbordihn.easymodelentities.data.model.bake;

import java.util.List;
import java.util.Objects;

public record BakedModelPart(
    String name,
    float[] offset,
    float[] rotation,
    List<BakedModelCube> cubes,
    List<BakedModelPart> children) {

  public BakedModelPart {
    Objects.requireNonNull(name, "name");
    offset = Objects.requireNonNull(offset, "offset").clone();
    rotation = Objects.requireNonNull(rotation, "rotation").clone();
    cubes = List.copyOf(Objects.requireNonNull(cubes, "cubes"));
    children = List.copyOf(Objects.requireNonNull(children, "children"));
  }

  public int partCount() {
    return 1 + children.stream().mapToInt(BakedModelPart::partCount).sum();
  }

  public int cubeCount() {
    return cubes.size() + children.stream().mapToInt(BakedModelPart::cubeCount).sum();
  }
}
