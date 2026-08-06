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

public record EasyModelAnimationTransition(
    EasyModelAnimationSwitchTiming timing, float blendDurationTicks) {

  public static final float DEFAULT_BLEND_DURATION_TICKS = 5.0f;
  public static final EasyModelAnimationTransition DEFAULT =
      new EasyModelAnimationTransition(
          EasyModelAnimationSwitchTiming.IMMEDIATE, DEFAULT_BLEND_DURATION_TICKS);
  public static final EasyModelAnimationTransition DEFAULT_AFTER_CURRENT =
      new EasyModelAnimationTransition(
          EasyModelAnimationSwitchTiming.AFTER_CURRENT, DEFAULT_BLEND_DURATION_TICKS);
  public static final EasyModelAnimationTransition IMMEDIATE =
      new EasyModelAnimationTransition(EasyModelAnimationSwitchTiming.IMMEDIATE, 0.0f);
  public static final EasyModelAnimationTransition AFTER_CURRENT =
      new EasyModelAnimationTransition(EasyModelAnimationSwitchTiming.AFTER_CURRENT, 0.0f);

  public EasyModelAnimationTransition {
    Objects.requireNonNull(timing, "timing");
    if (!Float.isFinite(blendDurationTicks) || blendDurationTicks < 0.0f) {
      throw new IllegalArgumentException("Blend duration must be a finite non-negative value.");
    }
  }

  public static EasyModelAnimationTransition immediate(float blendDurationTicks) {
    return new EasyModelAnimationTransition(
        EasyModelAnimationSwitchTiming.IMMEDIATE, blendDurationTicks);
  }

  public static EasyModelAnimationTransition afterCurrent(float blendDurationTicks) {
    return new EasyModelAnimationTransition(
        EasyModelAnimationSwitchTiming.AFTER_CURRENT, blendDurationTicks);
  }
}
