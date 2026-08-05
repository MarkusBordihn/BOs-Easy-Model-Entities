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

import de.markusbordihn.easymodelentities.api.data.EasyModelAnimation;
import java.util.Objects;

record EasyModelAnimationPlaybackFrame(
    EasyModelAnimation animation,
    float animationTicks,
    EasyModelAnimation previousAnimation,
    float previousAnimationTicks,
    float blendProgress,
    Boolean loopOverride,
    Boolean previousLoopOverride,
    boolean playbackDriven) {

  EasyModelAnimationPlaybackFrame {
    Objects.requireNonNull(animation, "animation");
    requireNonNegativeFinite(animationTicks, "animationTicks");
    requireNonNegativeFinite(previousAnimationTicks, "previousAnimationTicks");
    if (!Float.isFinite(blendProgress) || blendProgress < 0.0f || blendProgress > 1.0f) {
      throw new IllegalArgumentException("blendProgress must be finite and between 0.0 and 1.0.");
    }
    if (previousAnimation == null && blendProgress < 1.0f) {
      throw new IllegalArgumentException("blendProgress requires a previous animation.");
    }
  }

  static EasyModelAnimationPlaybackFrame single(
      EasyModelAnimation animation, float animationTicks) {
    return single(animation, animationTicks, null, false);
  }

  static EasyModelAnimationPlaybackFrame single(
      EasyModelAnimation animation,
      float animationTicks,
      Boolean loopOverride,
      boolean playbackDriven) {
    return new EasyModelAnimationPlaybackFrame(
        animation, animationTicks, null, 0.0f, 1.0f, loopOverride, null, playbackDriven);
  }

  private static void requireNonNegativeFinite(float value, String name) {
    if (!Float.isFinite(value) || value < 0.0f) {
      throw new IllegalArgumentException(name + " must be a finite non-negative value.");
    }
  }
}
