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

import de.markusbordihn.easymodelentities.data.model.Vec3f;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public record DecodedModelPart(
    String name,
    Vec3f offset,
    Vec3f rotation,
    List<DecodedModelCube> cubes,
    List<DecodedModelPart> children) {

  public DecodedModelPart {
    Objects.requireNonNull(name, "name");
    Objects.requireNonNull(offset, "offset");
    Objects.requireNonNull(rotation, "rotation");
    name = normalizeName(name);
    cubes = List.copyOf(Objects.requireNonNull(cubes, "cubes"));
    children = List.copyOf(Objects.requireNonNull(children, "children"));
  }

  public static String normalizeName(String name) {
    return name.trim().toLowerCase(Locale.ROOT);
  }

  public int partCount() {
    return 1 + children.stream().mapToInt(DecodedModelPart::partCount).sum();
  }

  public int cubeCount() {
    return cubes.size() + children.stream().mapToInt(DecodedModelPart::cubeCount).sum();
  }

  public int hierarchyDepth() {
    return 1 + children.stream().mapToInt(DecodedModelPart::hierarchyDepth).max().orElse(0);
  }
}
