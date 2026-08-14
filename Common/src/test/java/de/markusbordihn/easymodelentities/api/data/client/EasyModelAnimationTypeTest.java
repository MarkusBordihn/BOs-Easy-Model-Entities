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

package de.markusbordihn.easymodelentities.api.data.client;

import static org.junit.jupiter.api.Assertions.assertEquals;

import de.markusbordihn.easymodelentities.data.model.ModelAnimationClips;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class EasyModelAnimationTypeTest {

  @Test
  @DisplayName("Every clip name the mod ships with counts as a standard animation")
  void standardClipNamesResolveToStandard() {
    ModelAnimationClips.STANDARD.forEach(
        clipName ->
            assertEquals(
                EasyModelAnimationType.STANDARD, EasyModelAnimationType.fromName(clipName)));
  }

  @Test
  void clipNamesAreMatchedCaseInsensitively() {
    assertEquals(EasyModelAnimationType.STANDARD, EasyModelAnimationType.fromName("IDLE"));
    assertEquals(EasyModelAnimationType.STANDARD, EasyModelAnimationType.fromName(" Walk "));
  }

  @Test
  void authoredClipNamesResolveToCustom() {
    assertEquals(EasyModelAnimationType.CUSTOM, EasyModelAnimationType.fromName("wave"));
    assertEquals(EasyModelAnimationType.CUSTOM, EasyModelAnimationType.fromName(""));
  }

  @Test
  @DisplayName("A variant is classified by its base name")
  void variantsInheritTheClassificationOfTheirBase() {
    assertEquals(EasyModelAnimationType.STANDARD, EasyModelAnimationType.fromName("idle_2"));
    assertEquals(EasyModelAnimationType.STANDARD, EasyModelAnimationType.fromName("Attack 3"));
    assertEquals(EasyModelAnimationType.CUSTOM, EasyModelAnimationType.fromName("wave_2"));
    assertEquals(EasyModelAnimationType.CUSTOM, EasyModelAnimationType.fromName("idle_special"));
  }
}
