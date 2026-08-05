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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.markusbordihn.easymodelentities.api.data.EasyModelAnimation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class EasyModelFallbackAnimationTest {

  @Test
  void movementStatesLoopWithTheSameLength() {
    EasyModelAnimation.standardStates().stream()
        .filter(EasyModelFallbackAnimation::loops)
        .forEach(animation -> assertEquals(1.0f, EasyModelFallbackAnimation.length(animation)));
  }

  @Test
  void oneShotStatesHaveTheirOwnLengthAndDoNotLoop() {
    assertFalse(EasyModelFallbackAnimation.loops(EasyModelAnimation.ATTACK));
    assertFalse(EasyModelFallbackAnimation.loops(EasyModelAnimation.HURT));
    assertFalse(EasyModelFallbackAnimation.loops(EasyModelAnimation.DEATH));
    assertTrue(EasyModelFallbackAnimation.length(EasyModelAnimation.ATTACK) > 0.0f);
    assertTrue(EasyModelFallbackAnimation.length(EasyModelAnimation.HURT) > 0.0f);
    assertTrue(EasyModelFallbackAnimation.length(EasyModelAnimation.DEATH) > 0.0f);
  }

  @Test
  @DisplayName("Without a clip a named animation has no duration, so playback releases at once")
  void namedAnimationsHaveNoBuiltInDuration() {
    assertEquals(0.0f, EasyModelFallbackAnimation.length(EasyModelAnimation.named("wave")));
    assertFalse(EasyModelFallbackAnimation.loops(EasyModelAnimation.named("wave")));
    assertEquals(0.0f, EasyModelFallbackAnimation.length(EasyModelAnimation.named("walk")));
  }

  @Test
  void automaticSelectionHasNoDurationOfItsOwn() {
    assertEquals(0.0f, EasyModelFallbackAnimation.length(EasyModelAnimation.AUTO));
    assertFalse(EasyModelFallbackAnimation.loops(EasyModelAnimation.AUTO));
  }
}
