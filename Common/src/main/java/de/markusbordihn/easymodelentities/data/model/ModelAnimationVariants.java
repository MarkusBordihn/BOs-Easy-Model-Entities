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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

public record ModelAnimationVariants(Map<String, List<String>> groups) {

  public static final ModelAnimationVariants EMPTY = new ModelAnimationVariants(Map.of());

  private static final Comparator<String> VARIANT_ORDER =
      Comparator.comparingInt(ModelAnimationClips::variantIndex)
          .thenComparing(Comparator.naturalOrder());

  public ModelAnimationVariants {
    Objects.requireNonNull(groups, "groups");
    Map<String, List<String>> copy = new LinkedHashMap<>();
    groups.forEach((baseName, variants) -> copy.put(baseName, List.copyOf(variants)));
    groups = Map.copyOf(copy);
  }

  public static ModelAnimationVariants of(Map<String, ModelAnimationClip> clips) {
    if (clips == null || clips.isEmpty()) {
      return EMPTY;
    }

    Map<String, List<String>> groups = new TreeMap<>();
    for (String clipName : clips.keySet()) {
      groups
          .computeIfAbsent(ModelAnimationClips.baseName(clipName), ignored -> new ArrayList<>())
          .add(clipName);
    }
    groups.values().forEach(variants -> variants.sort(VARIANT_ORDER));

    return new ModelAnimationVariants(groups);
  }

  public List<String> variantsOf(String baseName) {
    List<String> variants = this.groups.get(baseName);
    return variants == null ? List.of() : variants;
  }

  public boolean hasVariants(String baseName) {
    return this.variantsOf(baseName).size() > 1;
  }
}
