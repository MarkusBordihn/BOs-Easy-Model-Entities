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
import java.util.Objects;

public record EasyModelAnimationStep(
    EasyModelAnimation animation,
    EasyModelAnimationPlayback playback,
    EasyModelAnimationTransition transition) {

  public EasyModelAnimationStep {
    Objects.requireNonNull(animation, "animation");
    Objects.requireNonNull(playback, "playback");
    Objects.requireNonNull(transition, "transition");
    if (animation.equals(EasyModelAnimation.AUTO)) {
      throw new IllegalArgumentException("A sequence step must not be the automatic animation.");
    }
  }

  public static EasyModelAnimationStep of(EasyModelAnimation animation) {
    return new EasyModelAnimationStep(
        animation, EasyModelAnimationPlayback.DEFAULT, EasyModelAnimationTransition.DEFAULT);
  }

  public EasyModelAnimationStep withPlayback(EasyModelAnimationPlayback playback) {
    return new EasyModelAnimationStep(this.animation, playback, this.transition);
  }

  public EasyModelAnimationStep withTransition(EasyModelAnimationTransition transition) {
    return new EasyModelAnimationStep(this.animation, this.playback, transition);
  }
}
