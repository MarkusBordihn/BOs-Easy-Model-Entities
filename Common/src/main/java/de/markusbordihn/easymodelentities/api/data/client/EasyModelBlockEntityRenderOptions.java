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
import de.markusbordihn.easymodelentities.api.client.EasyModelPartPoseListener;

public record EasyModelBlockEntityRenderOptions(
    Float yawDegrees,
    Float scale,
    Float animationTicks,
    EasyModelPartAnimator partAnimator,
    EasyModelPartAnimationMode partAnimationMode,
    EasyModelPartPoseListener partPoseListener,
    Integer animationState) {

  public static final EasyModelBlockEntityRenderOptions DEFAULT =
      new EasyModelBlockEntityRenderOptions(
          null,
          null,
          null,
          EasyModelPartAnimator.NONE,
          EasyModelPartAnimationMode.ADD,
          EasyModelPartPoseListener.NONE,
          null);

  public EasyModelBlockEntityRenderOptions(
      Float yawDegrees, Float animationTicks, EasyModelPartAnimator partAnimator) {
    this(yawDegrees, animationTicks, partAnimator, EasyModelPartAnimationMode.ADD);
  }

  public EasyModelBlockEntityRenderOptions(
      Float yawDegrees,
      Float animationTicks,
      EasyModelPartAnimator partAnimator,
      EasyModelPartAnimationMode partAnimationMode) {
    this(
        yawDegrees,
        null,
        animationTicks,
        partAnimator,
        partAnimationMode,
        EasyModelPartPoseListener.NONE,
        null);
  }

  public EasyModelBlockEntityRenderOptions {
    partAnimator = partAnimator == null ? EasyModelPartAnimator.NONE : partAnimator;
    partAnimationMode =
        partAnimationMode == null ? EasyModelPartAnimationMode.ADD : partAnimationMode;
    partPoseListener = partPoseListener == null ? EasyModelPartPoseListener.NONE : partPoseListener;
  }

  public EasyModelBlockEntityRenderOptions withYawDegrees(float yawDegrees) {
    return new EasyModelBlockEntityRenderOptions(
        yawDegrees,
        this.scale,
        this.animationTicks,
        this.partAnimator,
        this.partAnimationMode,
        this.partPoseListener,
        this.animationState);
  }

  public EasyModelBlockEntityRenderOptions withScale(float scale) {
    return new EasyModelBlockEntityRenderOptions(
        this.yawDegrees,
        scale,
        this.animationTicks,
        this.partAnimator,
        this.partAnimationMode,
        this.partPoseListener,
        this.animationState);
  }

  public EasyModelBlockEntityRenderOptions withAnimationTicks(float animationTicks) {
    return new EasyModelBlockEntityRenderOptions(
        this.yawDegrees,
        this.scale,
        animationTicks,
        this.partAnimator,
        this.partAnimationMode,
        this.partPoseListener,
        this.animationState);
  }

  public EasyModelBlockEntityRenderOptions withPartAnimator(EasyModelPartAnimator partAnimator) {
    return new EasyModelBlockEntityRenderOptions(
        this.yawDegrees,
        this.scale,
        this.animationTicks,
        partAnimator,
        this.partAnimationMode,
        this.partPoseListener,
        this.animationState);
  }

  public EasyModelBlockEntityRenderOptions withPartAnimationMode(
      EasyModelPartAnimationMode partAnimationMode) {
    return new EasyModelBlockEntityRenderOptions(
        this.yawDegrees,
        this.scale,
        this.animationTicks,
        this.partAnimator,
        partAnimationMode,
        this.partPoseListener,
        this.animationState);
  }

  public EasyModelBlockEntityRenderOptions withPartPoseListener(
      EasyModelPartPoseListener partPoseListener) {
    return new EasyModelBlockEntityRenderOptions(
        this.yawDegrees,
        this.scale,
        this.animationTicks,
        this.partAnimator,
        this.partAnimationMode,
        partPoseListener,
        this.animationState);
  }

  public EasyModelBlockEntityRenderOptions withAnimationState(int animationState) {
    return new EasyModelBlockEntityRenderOptions(
        this.yawDegrees,
        this.scale,
        this.animationTicks,
        this.partAnimator,
        this.partAnimationMode,
        this.partPoseListener,
        animationState);
  }
}
