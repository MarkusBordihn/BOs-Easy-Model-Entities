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
import de.markusbordihn.easymodelentities.api.data.EasyModelAnimation;
import java.util.Objects;

public final class EasyModelEntityRenderOptions {

  public static final EasyModelEntityRenderOptions DEFAULT =
      new EasyModelEntityRenderOptions(
          null,
          null,
          EasyModelPartAnimator.NONE,
          EasyModelPartAnimationMode.ADD,
          EasyModelPartPoseListener.NONE,
          null,
          null);

  private final Float scale;
  private final Float animationTicks;
  private final EasyModelPartAnimator partAnimator;
  private final EasyModelPartAnimationMode partAnimationMode;
  private final EasyModelPartPoseListener partPoseListener;
  private final EasyModelAnimation animation;
  private final EasyModelHeadLook headLook;

  private EasyModelEntityRenderOptions(
      Float scale,
      Float animationTicks,
      EasyModelPartAnimator partAnimator,
      EasyModelPartAnimationMode partAnimationMode,
      EasyModelPartPoseListener partPoseListener,
      EasyModelAnimation animation,
      EasyModelHeadLook headLook) {
    this.scale = scale;
    this.animationTicks = animationTicks;
    this.partAnimator = Objects.requireNonNull(partAnimator, "partAnimator");
    this.partAnimationMode = Objects.requireNonNull(partAnimationMode, "partAnimationMode");
    this.partPoseListener = Objects.requireNonNull(partPoseListener, "partPoseListener");
    this.animation = animation;
    this.headLook = headLook;
  }

  private static EasyModelEntityRenderOptions copy(
      Float scale,
      Float animationTicks,
      EasyModelPartAnimator partAnimator,
      EasyModelPartAnimationMode partAnimationMode,
      EasyModelPartPoseListener partPoseListener,
      EasyModelAnimation animation,
      EasyModelHeadLook headLook) {
    return new EasyModelEntityRenderOptions(
        scale,
        animationTicks,
        partAnimator,
        partAnimationMode,
        partPoseListener,
        animation,
        headLook);
  }

  private static void requirePositiveFinite(float value, String name) {
    if (!Float.isFinite(value) || value <= 0.0f) {
      throw new IllegalArgumentException(name + " must be a finite positive value.");
    }
  }

  private static void requireNonNegativeFinite(float value, String name) {
    if (!Float.isFinite(value) || value < 0.0f) {
      throw new IllegalArgumentException(name + " must be a finite non-negative value.");
    }
  }

  public Float scale() {
    return this.scale;
  }

  public Float animationTicks() {
    return this.animationTicks;
  }

  public EasyModelPartAnimator partAnimator() {
    return this.partAnimator;
  }

  public EasyModelPartAnimationMode partAnimationMode() {
    return this.partAnimationMode;
  }

  public EasyModelPartPoseListener partPoseListener() {
    return this.partPoseListener;
  }

  public EasyModelAnimation animation() {
    return this.animation;
  }

  public EasyModelHeadLook headLook() {
    return this.headLook;
  }

  public EasyModelEntityRenderOptions withScale(float scale) {
    requirePositiveFinite(scale, "scale");
    return copy(
        scale,
        this.animationTicks,
        this.partAnimator,
        this.partAnimationMode,
        this.partPoseListener,
        this.animation,
        this.headLook);
  }

  public EasyModelEntityRenderOptions withAnimationTicks(float animationTicks) {
    requireNonNegativeFinite(animationTicks, "animationTicks");
    return copy(
        this.scale,
        animationTicks,
        this.partAnimator,
        this.partAnimationMode,
        this.partPoseListener,
        this.animation,
        this.headLook);
  }

  public EasyModelEntityRenderOptions withPartAnimator(EasyModelPartAnimator partAnimator) {
    return copy(
        this.scale,
        this.animationTicks,
        Objects.requireNonNull(partAnimator, "partAnimator"),
        this.partAnimationMode,
        this.partPoseListener,
        this.animation,
        this.headLook);
  }

  public EasyModelEntityRenderOptions withPartAnimationMode(
      EasyModelPartAnimationMode partAnimationMode) {
    return copy(
        this.scale,
        this.animationTicks,
        this.partAnimator,
        Objects.requireNonNull(partAnimationMode, "partAnimationMode"),
        this.partPoseListener,
        this.animation,
        this.headLook);
  }

  public EasyModelEntityRenderOptions withPartPoseListener(
      EasyModelPartPoseListener partPoseListener) {
    return copy(
        this.scale,
        this.animationTicks,
        this.partAnimator,
        this.partAnimationMode,
        Objects.requireNonNull(partPoseListener, "partPoseListener"),
        this.animation,
        this.headLook);
  }

  public EasyModelEntityRenderOptions withAnimation(EasyModelAnimation animation) {
    return copy(
        this.scale,
        this.animationTicks,
        this.partAnimator,
        this.partAnimationMode,
        this.partPoseListener,
        Objects.requireNonNull(animation, "animation"),
        this.headLook);
  }

  public EasyModelEntityRenderOptions withAnimation(String animation) {
    return withAnimation(EasyModelAnimation.named(animation));
  }

  public EasyModelEntityRenderOptions withoutAnimationOverride() {
    return copy(
        this.scale,
        this.animationTicks,
        this.partAnimator,
        this.partAnimationMode,
        this.partPoseListener,
        null,
        this.headLook);
  }

  public EasyModelEntityRenderOptions withHeadLook(EasyModelHeadLook headLook) {
    return copy(
        this.scale,
        this.animationTicks,
        this.partAnimator,
        this.partAnimationMode,
        this.partPoseListener,
        this.animation,
        Objects.requireNonNull(headLook, "headLook"));
  }

  @Override
  public boolean equals(Object object) {
    if (this == object) {
      return true;
    }
    if (!(object instanceof EasyModelEntityRenderOptions options)) {
      return false;
    }

    return Objects.equals(this.scale, options.scale)
        && Objects.equals(this.animationTicks, options.animationTicks)
        && this.partAnimator.equals(options.partAnimator)
        && this.partAnimationMode == options.partAnimationMode
        && this.partPoseListener.equals(options.partPoseListener)
        && Objects.equals(this.animation, options.animation)
        && Objects.equals(this.headLook, options.headLook);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        this.scale,
        this.animationTicks,
        this.partAnimator,
        this.partAnimationMode,
        this.partPoseListener,
        this.animation,
        this.headLook);
  }

  @Override
  public String toString() {
    return "EasyModelEntityRenderOptions[scale="
        + this.scale
        + ", animationTicks="
        + this.animationTicks
        + ", partAnimator="
        + this.partAnimator
        + ", partAnimationMode="
        + this.partAnimationMode
        + ", partPoseListener="
        + this.partPoseListener
        + ", animation="
        + this.animation
        + ", headLook="
        + this.headLook
        + "]";
  }
}
