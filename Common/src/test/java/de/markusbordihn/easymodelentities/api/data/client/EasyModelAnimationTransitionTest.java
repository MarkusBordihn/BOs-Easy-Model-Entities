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
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class EasyModelAnimationTransitionTest {

  @Test
  @DisplayName("Both named hard-cut constants must agree on their blend duration")
  void hardCutConstantsDoNotBlend() {
    assertEquals(0.0f, EasyModelAnimationTransition.IMMEDIATE.blendDurationTicks());
    assertEquals(0.0f, EasyModelAnimationTransition.AFTER_CURRENT.blendDurationTicks());
  }

  @Test
  void bothDefaultsShareTheSameBlendDuration() {
    assertEquals(
        EasyModelAnimationTransition.DEFAULT_BLEND_DURATION_TICKS,
        EasyModelAnimationTransition.DEFAULT.blendDurationTicks());
    assertEquals(
        EasyModelAnimationTransition.DEFAULT_BLEND_DURATION_TICKS,
        EasyModelAnimationTransition.DEFAULT_AFTER_CURRENT.blendDurationTicks());
  }

  @Test
  void defaultsDifferOnlyInTheirSwitchTiming() {
    assertEquals(
        EasyModelAnimationSwitchTiming.IMMEDIATE, EasyModelAnimationTransition.DEFAULT.timing());
    assertEquals(
        EasyModelAnimationSwitchTiming.AFTER_CURRENT,
        EasyModelAnimationTransition.DEFAULT_AFTER_CURRENT.timing());
  }

  @Test
  void rejectsInvalidBlendDurations() {
    assertThrows(
        IllegalArgumentException.class, () -> EasyModelAnimationTransition.immediate(-1.0f));
    assertThrows(
        IllegalArgumentException.class, () -> EasyModelAnimationTransition.afterCurrent(Float.NaN));
    assertThrows(NullPointerException.class, () -> new EasyModelAnimationTransition(null, 1.0f));
  }
}
