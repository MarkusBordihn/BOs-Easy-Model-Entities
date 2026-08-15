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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ModelAnimationClipsTest {

  @Test
  void underscoreAndSpaceSeparateTheVariantNumber() {
    assertEquals("idle", ModelAnimationClips.baseName("idle_2"));
    assertEquals("idle", ModelAnimationClips.baseName("idle 2"));
    assertEquals(2, ModelAnimationClips.variantIndex("idle_2"));
    assertEquals(2, ModelAnimationClips.variantIndex("idle 2"));
  }

  @Test
  @DisplayName("A clip that only looks numbered keeps its whole name")
  void namesWithoutAVariantSuffixStayIntact() {
    assertEquals("idle", ModelAnimationClips.baseName("idle"));
    assertEquals("idle2", ModelAnimationClips.baseName("idle2"));
    assertEquals("idle_0", ModelAnimationClips.baseName("idle_0"));
    assertEquals("idle_02", ModelAnimationClips.baseName("idle_02"));
    assertEquals("_2", ModelAnimationClips.baseName("_2"));
    assertEquals(0, ModelAnimationClips.variantIndex("idle"));
    assertEquals(0, ModelAnimationClips.variantIndex("idle_0"));
  }

  @Test
  @DisplayName("Only the last number is the variant, so idle_2_3 belongs to idle_2")
  void onlyTheTrailingNumberIsTheVariant() {
    assertEquals("idle_2", ModelAnimationClips.baseName("idle_2_3"));
    assertEquals(3, ModelAnimationClips.variantIndex("idle_2_3"));
  }

  @Test
  @DisplayName("A variant number beyond int range sorts last instead of throwing")
  void anUnparseableVariantNumberSortsLast() {
    assertEquals("idle", ModelAnimationClips.baseName("idle_99999999999"));
    assertEquals(Integer.MAX_VALUE, ModelAnimationClips.variantIndex("idle_99999999999"));
  }

  @Test
  void nullNamesAreAnsweredWithEmptyValues() {
    assertEquals("", ModelAnimationClips.baseName(null));
    assertEquals("", ModelAnimationClips.normalize(null));
    assertEquals(0, ModelAnimationClips.variantIndex(null));
    assertFalse(ModelAnimationClips.isStandardBase(null));
  }

  @Test
  @DisplayName("A numbered variant of a standard state is standard, not a custom clip")
  void variantsOfStandardStatesStayStandard() {
    assertTrue(ModelAnimationClips.isStandardBase("walk_2"));
    assertTrue(ModelAnimationClips.isStandardBase("WALK_2"));
    assertTrue(ModelAnimationClips.isStandardBase("sit"));
    assertFalse(ModelAnimationClips.isStandardBase("wave"));
    assertFalse(ModelAnimationClips.isStandardBase("wave_2"));
  }

  @Test
  @DisplayName("The family decides which clock rotates a variant group")
  void movementAndAttackClipsHaveTheirOwnFamily() {
    assertEquals(
        ModelAnimationFamily.MOVEMENT, ModelAnimationClips.familyOf(ModelAnimationClips.WALK));
    assertEquals(
        ModelAnimationFamily.MOVEMENT, ModelAnimationClips.familyOf(ModelAnimationClips.SWIM));
    assertEquals(
        ModelAnimationFamily.ATTACK, ModelAnimationClips.familyOf(ModelAnimationClips.ATTACK));
    assertEquals(ModelAnimationFamily.AGE, ModelAnimationClips.familyOf(ModelAnimationClips.IDLE));
    assertEquals(ModelAnimationFamily.AGE, ModelAnimationClips.familyOf("wave"));
  }
}
