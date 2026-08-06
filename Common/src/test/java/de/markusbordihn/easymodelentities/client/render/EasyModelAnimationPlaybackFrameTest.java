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
import static org.junit.jupiter.api.Assertions.assertThrows;

import de.markusbordihn.easymodelentities.api.data.EasyModelAnimation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class EasyModelAnimationPlaybackFrameTest {

  @Test
  @DisplayName("A blend without a previous animation would render an undefined pose")
  void rejectsBlendProgressWithoutPreviousAnimation() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new EasyModelAnimationPlaybackFrame(
                EasyModelAnimation.IDLE, 0.0f, null, 0.0f, 0.5f, null, null, true));
  }

  @Test
  void rejectsNegativeAndNonFiniteTicks() {
    assertThrows(
        IllegalArgumentException.class,
        () -> EasyModelAnimationPlaybackFrame.single(EasyModelAnimation.IDLE, -1.0f));
    assertThrows(
        IllegalArgumentException.class,
        () -> EasyModelAnimationPlaybackFrame.single(EasyModelAnimation.IDLE, Float.NaN));
  }

  @Test
  void singleFrameIsNotPlaybackDrivenByDefault() {
    EasyModelAnimationPlaybackFrame frame =
        EasyModelAnimationPlaybackFrame.single(EasyModelAnimation.IDLE, 3.0f);

    assertEquals(1.0f, frame.blendProgress());
    assertFalse(frame.playbackDriven());
  }
}
