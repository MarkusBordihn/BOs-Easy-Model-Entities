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

public record EasyModelEntityRenderOptions(
    Float scale,
    Float animationTicks,
    EasyModelPartAnimator partAnimator,
    EasyModelPartAnimationMode partAnimationMode,
    EasyModelPartPoseListener partPoseListener,
    Integer animationState) {

  public static final EasyModelEntityRenderOptions DEFAULT =
      new EasyModelEntityRenderOptions(
          null,
          null,
          EasyModelPartAnimator.NONE,
          EasyModelPartAnimationMode.ADD,
          EasyModelPartPoseListener.NONE,
          null);

  public EasyModelEntityRenderOptions(Float animationTicks, EasyModelPartAnimator partAnimator) {
    this(null, animationTicks, partAnimator, EasyModelPartAnimationMode.ADD);
  }

  public EasyModelEntityRenderOptions(
      Float animationTicks,
      EasyModelPartAnimator partAnimator,
      EasyModelPartAnimationMode partAnimationMode) {
    this(null, animationTicks, partAnimator, partAnimationMode);
  }

  public EasyModelEntityRenderOptions(
      Float scale,
      Float animationTicks,
      EasyModelPartAnimator partAnimator,
      EasyModelPartAnimationMode partAnimationMode) {
    this(
        scale,
        animationTicks,
        partAnimator,
        partAnimationMode,
        EasyModelPartPoseListener.NONE,
        null);
  }

  public EasyModelEntityRenderOptions {
    partAnimator = partAnimator == null ? EasyModelPartAnimator.NONE : partAnimator;
    partAnimationMode =
        partAnimationMode == null ? EasyModelPartAnimationMode.ADD : partAnimationMode;
    partPoseListener = partPoseListener == null ? EasyModelPartPoseListener.NONE : partPoseListener;
  }

  public EasyModelEntityRenderOptions withScale(float scale) {
    return new EasyModelEntityRenderOptions(
        scale,
        this.animationTicks,
        this.partAnimator,
        this.partAnimationMode,
        this.partPoseListener,
        this.animationState);
  }

  public EasyModelEntityRenderOptions withAnimationTicks(float animationTicks) {
    return new EasyModelEntityRenderOptions(
        this.scale,
        animationTicks,
        this.partAnimator,
        this.partAnimationMode,
        this.partPoseListener,
        this.animationState);
  }

  public EasyModelEntityRenderOptions withPartAnimator(EasyModelPartAnimator partAnimator) {
    return new EasyModelEntityRenderOptions(
        this.scale,
        this.animationTicks,
        partAnimator,
        this.partAnimationMode,
        this.partPoseListener,
        this.animationState);
  }

  public EasyModelEntityRenderOptions withPartAnimationMode(
      EasyModelPartAnimationMode partAnimationMode) {
    return new EasyModelEntityRenderOptions(
        this.scale,
        this.animationTicks,
        this.partAnimator,
        partAnimationMode,
        this.partPoseListener,
        this.animationState);
  }

  public EasyModelEntityRenderOptions withPartPoseListener(
      EasyModelPartPoseListener partPoseListener) {
    return new EasyModelEntityRenderOptions(
        this.scale,
        this.animationTicks,
        this.partAnimator,
        this.partAnimationMode,
        partPoseListener,
        this.animationState);
  }

  public EasyModelEntityRenderOptions withAnimationState(int animationState) {
    return new EasyModelEntityRenderOptions(
        this.scale,
        this.animationTicks,
        this.partAnimator,
        this.partAnimationMode,
        this.partPoseListener,
        animationState);
  }
}
