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

import de.markusbordihn.easymodelentities.api.client.EasyModelPartAnimator;

public record EasyModelBlockEntityRenderOptions(
    Float yawDegrees,
    Float animationTicks,
    EasyModelPartAnimator partAnimator,
    EasyModelPartAnimationMode partAnimationMode) {

  public static final EasyModelBlockEntityRenderOptions DEFAULT =
      new EasyModelBlockEntityRenderOptions(
          null, null, EasyModelPartAnimator.NONE, EasyModelPartAnimationMode.ADD);

  public EasyModelBlockEntityRenderOptions(
      Float yawDegrees, Float animationTicks, EasyModelPartAnimator partAnimator) {
    this(yawDegrees, animationTicks, partAnimator, EasyModelPartAnimationMode.ADD);
  }

  public EasyModelBlockEntityRenderOptions {
    partAnimator = partAnimator == null ? EasyModelPartAnimator.NONE : partAnimator;
    partAnimationMode =
        partAnimationMode == null ? EasyModelPartAnimationMode.ADD : partAnimationMode;
  }

  public EasyModelBlockEntityRenderOptions withYawDegrees(float yawDegrees) {
    return new EasyModelBlockEntityRenderOptions(
        yawDegrees, this.animationTicks, this.partAnimator, this.partAnimationMode);
  }

  public EasyModelBlockEntityRenderOptions withAnimationTicks(float animationTicks) {
    return new EasyModelBlockEntityRenderOptions(
        this.yawDegrees, animationTicks, this.partAnimator, this.partAnimationMode);
  }

  public EasyModelBlockEntityRenderOptions withPartAnimator(EasyModelPartAnimator partAnimator) {
    return new EasyModelBlockEntityRenderOptions(
        this.yawDegrees, this.animationTicks, partAnimator, this.partAnimationMode);
  }

  public EasyModelBlockEntityRenderOptions withPartAnimationMode(
      EasyModelPartAnimationMode partAnimationMode) {
    return new EasyModelBlockEntityRenderOptions(
        this.yawDegrees, this.animationTicks, this.partAnimator, partAnimationMode);
  }
}
