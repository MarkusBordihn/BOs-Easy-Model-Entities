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

import java.util.Objects;

public record EasyModelAnimationPlayback(
    EasyModelAnimationPlaybackMode mode, int repeatCount, float durationTicks) {

  public static final EasyModelAnimationPlayback DEFAULT =
      new EasyModelAnimationPlayback(EasyModelAnimationPlaybackMode.ONCE, 1, 0.0f);

  public EasyModelAnimationPlayback {
    Objects.requireNonNull(mode, "mode");
    if (repeatCount < 0) {
      throw new IllegalArgumentException("Repeat count must be non-negative.");
    }
    if (mode == EasyModelAnimationPlaybackMode.REPEAT && repeatCount < 1) {
      throw new IllegalArgumentException("REPEAT playback requires at least one play.");
    }
    if (!Float.isFinite(durationTicks) || durationTicks < 0.0f) {
      throw new IllegalArgumentException("Duration must be a finite non-negative value.");
    }
  }

  public EasyModelAnimationPlayback withMode(EasyModelAnimationPlaybackMode mode) {
    return new EasyModelAnimationPlayback(mode, this.repeatCount, this.durationTicks);
  }

  public EasyModelAnimationPlayback withRepeat(int repeatCount) {
    return new EasyModelAnimationPlayback(this.mode, repeatCount, this.durationTicks);
  }

  public EasyModelAnimationPlayback withDuration(float durationTicks) {
    return new EasyModelAnimationPlayback(this.mode, this.repeatCount, durationTicks);
  }
}
