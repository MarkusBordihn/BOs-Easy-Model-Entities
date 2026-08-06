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
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class EasyModelAnimationPlaybackTest {

  @Test
  void exposesEasyNpcCompatibleDefaults() {
    assertEquals(EasyModelAnimationPlaybackMode.ONCE, EasyModelAnimationPlayback.DEFAULT.mode());
    assertEquals(1, EasyModelAnimationPlayback.DEFAULT.repeatCount());
    assertEquals(0.0f, EasyModelAnimationPlayback.DEFAULT.durationTicks());
    assertEquals(
        EasyModelAnimationSwitchTiming.IMMEDIATE, EasyModelAnimationTransition.DEFAULT.timing());
    assertEquals(5.0f, EasyModelAnimationTransition.DEFAULT.blendDurationTicks());
  }

  @Test
  void withMethodsReturnValidatedCopies() {
    EasyModelAnimationPlayback playback = EasyModelAnimationPlayback.DEFAULT;
    EasyModelAnimationPlayback repeated =
        playback.withMode(EasyModelAnimationPlaybackMode.REPEAT).withRepeat(3).withDuration(40.0f);

    assertNotSame(playback, repeated);
    assertEquals(EasyModelAnimationPlaybackMode.REPEAT, repeated.mode());
    assertEquals(3, repeated.repeatCount());
    assertEquals(40.0f, repeated.durationTicks());
    assertEquals(EasyModelAnimationPlayback.DEFAULT, playback);
  }

  @Test
  void rejectsInvalidPlaybackValues() {
    assertThrows(NullPointerException.class, () -> new EasyModelAnimationPlayback(null, 1, 0.0f));
    assertThrows(
        IllegalArgumentException.class,
        () -> new EasyModelAnimationPlayback(EasyModelAnimationPlaybackMode.ONCE, -1, 0.0f));
    assertThrows(
        IllegalArgumentException.class,
        () -> new EasyModelAnimationPlayback(EasyModelAnimationPlaybackMode.REPEAT, 0, 0.0f));
    assertThrows(
        IllegalArgumentException.class,
        () -> new EasyModelAnimationPlayback(EasyModelAnimationPlaybackMode.LOOP, 0, -1.0f));
    assertThrows(
        IllegalArgumentException.class,
        () -> new EasyModelAnimationPlayback(EasyModelAnimationPlaybackMode.LOOP, 0, Float.NaN));
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new EasyModelAnimationPlayback(
                EasyModelAnimationPlaybackMode.LOOP, 0, Float.POSITIVE_INFINITY));
  }

  @Test
  void onceAndLoopAllowZeroRepeatCount() {
    assertEquals(
        0,
        new EasyModelAnimationPlayback(EasyModelAnimationPlaybackMode.ONCE, 0, 0.0f).repeatCount());
    assertEquals(
        0,
        new EasyModelAnimationPlayback(EasyModelAnimationPlaybackMode.LOOP, 0, 0.0f).repeatCount());
  }
}
