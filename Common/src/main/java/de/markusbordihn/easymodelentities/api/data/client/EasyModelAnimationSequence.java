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

import de.markusbordihn.easymodelentities.api.data.EasyModelAnimation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public record EasyModelAnimationSequence(
    List<EasyModelAnimationStep> steps, EasyModelAnimation fallback) {

  public static final int MAX_STEPS = 16;

  public EasyModelAnimationSequence {
    Objects.requireNonNull(steps, "steps");
    Objects.requireNonNull(fallback, "fallback");
    if (steps.isEmpty()) {
      throw new IllegalArgumentException("A sequence needs at least one step.");
    }
    if (steps.size() > MAX_STEPS) {
      throw new IllegalArgumentException("A sequence must not exceed " + MAX_STEPS + " steps.");
    }
    steps = normalize(steps);
  }

  public static EasyModelAnimationSequence of(EasyModelAnimation... animations) {
    return of(Arrays.asList(animations));
  }

  public static EasyModelAnimationSequence of(List<EasyModelAnimation> animations) {
    return new EasyModelAnimationSequence(
        Objects.requireNonNull(animations, "animations").stream()
            .map(EasyModelAnimationStep::of)
            .toList(),
        EasyModelAnimation.AUTO);
  }

  private static List<EasyModelAnimationStep> normalize(List<EasyModelAnimationStep> steps) {
    List<EasyModelAnimationStep> normalizedSteps = new ArrayList<>(steps.size());
    for (int i = 0; i < steps.size(); i++) {
      EasyModelAnimationStep step = Objects.requireNonNull(steps.get(i), "step");
      if (i > 0 && step.transition().timing() != EasyModelAnimationSwitchTiming.AFTER_CURRENT) {
        step =
            step.withTransition(
                EasyModelAnimationTransition.afterCurrent(step.transition().blendDurationTicks()));
      }
      if (i < steps.size() - 1 && step.playback().mode() == EasyModelAnimationPlaybackMode.LOOP) {
        step = step.withPlayback(step.playback().withMode(EasyModelAnimationPlaybackMode.ONCE));
      }
      normalizedSteps.add(step);
    }

    return List.copyOf(normalizedSteps);
  }

  public EasyModelAnimationSequence withFallback(EasyModelAnimation fallback) {
    return new EasyModelAnimationSequence(this.steps, fallback);
  }

  public EasyModelAnimationStep firstStep() {
    return this.steps.get(0);
  }
}
