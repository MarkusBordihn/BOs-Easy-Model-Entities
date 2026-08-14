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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ModelAnimationVariantsTest {

  private static ModelAnimationVariants variantsOf(String... clipNames) {
    Map<String, ModelAnimationClip> clips = new HashMap<>();
    for (String clipName : clipNames) {
      clips.put(clipName, new ModelAnimationClip(clipName, 1.0f, true, Map.of()));
    }

    return ModelAnimationVariants.of(clips);
  }

  @Test
  @DisplayName("Variants are ordered by their numeric suffix, not lexicographically")
  void variantsAreOrderedNumerically() {
    ModelAnimationVariants variants = variantsOf("idle_10", "idle", "idle_2");

    assertEquals(List.of("idle", "idle_2", "idle_10"), variants.variantsOf("idle"));
    assertTrue(variants.hasVariants("idle"));
  }

  @Test
  @DisplayName("A group without the bare base name keeps its variants")
  void groupWithoutBaseNameIsKept() {
    ModelAnimationVariants variants = variantsOf("idle_2", "idle_3");

    assertEquals(List.of("idle_2", "idle_3"), variants.variantsOf("idle"));
  }

  @Test
  @DisplayName("A blank separator is accepted, other suffixes stay independent clips")
  void suffixGrammarIsStrict() {
    ModelAnimationVariants variants =
        variantsOf("idle", "idle 2", "idle_0", "idle_007", "idle_2_3", "idle_special");

    assertEquals(List.of("idle", "idle 2"), variants.variantsOf("idle"));
    assertEquals(List.of("idle_0"), variants.variantsOf("idle_0"));
    assertEquals(List.of("idle_007"), variants.variantsOf("idle_007"));
    assertEquals(List.of("idle_2_3"), variants.variantsOf("idle_2"));
    assertEquals(List.of("idle_special"), variants.variantsOf("idle_special"));
  }

  @Test
  @DisplayName("Custom clips form their own groups")
  void customClipsFormOwnGroups() {
    ModelAnimationVariants variants = variantsOf("wave", "wave_2", "attack");

    assertEquals(List.of("wave", "wave_2"), variants.variantsOf("wave"));
    assertFalse(variants.hasVariants("attack"));
    assertTrue(variants.variantsOf("missing").isEmpty());
  }

  @Test
  void emptyClipsProduceEmptyVariants() {
    assertEquals(ModelAnimationVariants.EMPTY, ModelAnimationVariants.of(Map.of()));
    assertEquals(ModelAnimationVariants.EMPTY, ModelAnimationVariants.of(null));
  }
}
