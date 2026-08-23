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
import de.markusbordihn.easymodelentities.api.data.EasyModelDisplaySettings;
import de.markusbordihn.easymodelentities.api.data.EasyModelTextureBlend;
import de.markusbordihn.easymodelentities.api.data.EasyModelTextureSetting;
import java.util.Objects;
import net.minecraft.resources.Identifier;

public final class EasyModelBlockEntityRenderOptions {

  public static final EasyModelBlockEntityRenderOptions DEFAULT =
      new EasyModelBlockEntityRenderOptions(
          null,
          null,
          null,
          EasyModelPartAnimator.NONE,
          EasyModelPartAnimationMode.ADD,
          EasyModelPartPoseListener.NONE,
          null,
          EasyModelTextureSetting.EMPTY,
          null,
          null,
          null);

  private final Float yawDegrees;
  private final Float scale;
  private final Float animationTicks;
  private final EasyModelPartAnimator partAnimator;
  private final EasyModelPartAnimationMode partAnimationMode;
  private final EasyModelPartPoseListener partPoseListener;
  private final EasyModelAnimation animation;
  private final EasyModelTextureSetting textureSetting;
  private final Float opacity;
  private final Integer lightLevel;
  private final Integer packedOverlay;

  private EasyModelBlockEntityRenderOptions(
      Float yawDegrees,
      Float scale,
      Float animationTicks,
      EasyModelPartAnimator partAnimator,
      EasyModelPartAnimationMode partAnimationMode,
      EasyModelPartPoseListener partPoseListener,
      EasyModelAnimation animation,
      EasyModelTextureSetting textureSetting,
      Float opacity,
      Integer lightLevel,
      Integer packedOverlay) {
    this.yawDegrees = yawDegrees;
    this.scale = scale;
    this.animationTicks = animationTicks;
    this.partAnimator = Objects.requireNonNull(partAnimator, "partAnimator");
    this.partAnimationMode = Objects.requireNonNull(partAnimationMode, "partAnimationMode");
    this.partPoseListener = Objects.requireNonNull(partPoseListener, "partPoseListener");
    this.animation = animation;
    this.textureSetting = Objects.requireNonNull(textureSetting, "textureSetting");
    this.opacity = opacity;
    this.lightLevel = lightLevel;
    this.packedOverlay = packedOverlay;
  }

  private static EasyModelBlockEntityRenderOptions copy(
      Float yawDegrees,
      Float scale,
      Float animationTicks,
      EasyModelPartAnimator partAnimator,
      EasyModelPartAnimationMode partAnimationMode,
      EasyModelPartPoseListener partPoseListener,
      EasyModelAnimation animation,
      EasyModelTextureSetting textureSetting,
      Float opacity,
      Integer lightLevel,
      Integer packedOverlay) {
    return new EasyModelBlockEntityRenderOptions(
        yawDegrees,
        scale,
        animationTicks,
        partAnimator,
        partAnimationMode,
        partPoseListener,
        animation,
        textureSetting,
        opacity,
        lightLevel,
        packedOverlay);
  }

  private static void requireFinite(float value, String name) {
    if (!Float.isFinite(value)) {
      throw new IllegalArgumentException(name + " must be finite.");
    }
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

  public Float yawDegrees() {
    return this.yawDegrees;
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

  public EasyModelTextureSetting textureSetting() {
    return this.textureSetting;
  }

  public Float opacity() {
    return this.opacity;
  }

  public Integer lightLevel() {
    return this.lightLevel;
  }

  public Integer packedOverlay() {
    return this.packedOverlay;
  }

  public EasyModelBlockEntityRenderOptions withYawDegrees(float yawDegrees) {
    requireFinite(yawDegrees, "yawDegrees");
    return copy(
        yawDegrees,
        this.scale,
        this.animationTicks,
        this.partAnimator,
        this.partAnimationMode,
        this.partPoseListener,
        this.animation,
        this.textureSetting,
        this.opacity,
        this.lightLevel,
        this.packedOverlay);
  }

  public EasyModelBlockEntityRenderOptions withScale(float scale) {
    requirePositiveFinite(scale, "scale");
    return copy(
        this.yawDegrees,
        scale,
        this.animationTicks,
        this.partAnimator,
        this.partAnimationMode,
        this.partPoseListener,
        this.animation,
        this.textureSetting,
        this.opacity,
        this.lightLevel,
        this.packedOverlay);
  }

  public EasyModelBlockEntityRenderOptions withAnimationTicks(float animationTicks) {
    requireNonNegativeFinite(animationTicks, "animationTicks");
    return copy(
        this.yawDegrees,
        this.scale,
        animationTicks,
        this.partAnimator,
        this.partAnimationMode,
        this.partPoseListener,
        this.animation,
        this.textureSetting,
        this.opacity,
        this.lightLevel,
        this.packedOverlay);
  }

  public EasyModelBlockEntityRenderOptions withPartAnimator(EasyModelPartAnimator partAnimator) {
    return copy(
        this.yawDegrees,
        this.scale,
        this.animationTicks,
        Objects.requireNonNull(partAnimator, "partAnimator"),
        this.partAnimationMode,
        this.partPoseListener,
        this.animation,
        this.textureSetting,
        this.opacity,
        this.lightLevel,
        this.packedOverlay);
  }

  public EasyModelBlockEntityRenderOptions withPartAnimationMode(
      EasyModelPartAnimationMode partAnimationMode) {
    return copy(
        this.yawDegrees,
        this.scale,
        this.animationTicks,
        this.partAnimator,
        Objects.requireNonNull(partAnimationMode, "partAnimationMode"),
        this.partPoseListener,
        this.animation,
        this.textureSetting,
        this.opacity,
        this.lightLevel,
        this.packedOverlay);
  }

  public EasyModelBlockEntityRenderOptions withPartPoseListener(
      EasyModelPartPoseListener partPoseListener) {
    return copy(
        this.yawDegrees,
        this.scale,
        this.animationTicks,
        this.partAnimator,
        this.partAnimationMode,
        Objects.requireNonNull(partPoseListener, "partPoseListener"),
        this.animation,
        this.textureSetting,
        this.opacity,
        this.lightLevel,
        this.packedOverlay);
  }

  public EasyModelBlockEntityRenderOptions withAnimation(EasyModelAnimation animation) {
    return copy(
        this.yawDegrees,
        this.scale,
        this.animationTicks,
        this.partAnimator,
        this.partAnimationMode,
        this.partPoseListener,
        Objects.requireNonNull(animation, "animation"),
        this.textureSetting,
        this.opacity,
        this.lightLevel,
        this.packedOverlay);
  }

  public EasyModelBlockEntityRenderOptions withAnimation(String animation) {
    return withAnimation(EasyModelAnimation.named(animation));
  }

  public EasyModelBlockEntityRenderOptions withoutAnimationOverride() {
    return copy(
        this.yawDegrees,
        this.scale,
        this.animationTicks,
        this.partAnimator,
        this.partAnimationMode,
        this.partPoseListener,
        null,
        this.textureSetting,
        this.opacity,
        this.lightLevel,
        this.packedOverlay);
  }

  public EasyModelBlockEntityRenderOptions withTextureSetting(
      EasyModelTextureSetting textureSetting) {
    return copy(
        this.yawDegrees,
        this.scale,
        this.animationTicks,
        this.partAnimator,
        this.partAnimationMode,
        this.partPoseListener,
        this.animation,
        Objects.requireNonNull(textureSetting, "textureSetting"),
        this.opacity,
        this.lightLevel,
        this.packedOverlay);
  }

  public EasyModelBlockEntityRenderOptions withTexture(String slot, Identifier texture) {
    return this.withTextureSetting(this.textureSetting.withSlot(slot, texture));
  }

  public EasyModelBlockEntityRenderOptions withTexture(
      String slot, Identifier texture, EasyModelTextureBlend blend) {
    return this.withTextureSetting(this.textureSetting.withSlot(slot, texture, blend));
  }

  public EasyModelBlockEntityRenderOptions withTextureBlend(
      String slot, EasyModelTextureBlend blend) {
    return this.withTextureSetting(this.textureSetting.withBlend(slot, blend));
  }

  public EasyModelBlockEntityRenderOptions withoutTextureOverride() {
    return this.withTextureSetting(EasyModelTextureSetting.EMPTY);
  }

  public EasyModelBlockEntityRenderOptions withOpacity(float opacity) {
    EasyModelDisplaySettings.requireOpacity(opacity);
    return copy(
        this.yawDegrees,
        this.scale,
        this.animationTicks,
        this.partAnimator,
        this.partAnimationMode,
        this.partPoseListener,
        this.animation,
        this.textureSetting,
        opacity,
        this.lightLevel,
        this.packedOverlay);
  }

  public EasyModelBlockEntityRenderOptions withoutOpacityOverride() {
    return copy(
        this.yawDegrees,
        this.scale,
        this.animationTicks,
        this.partAnimator,
        this.partAnimationMode,
        this.partPoseListener,
        this.animation,
        this.textureSetting,
        null,
        this.lightLevel,
        this.packedOverlay);
  }

  public EasyModelBlockEntityRenderOptions withLightLevel(int lightLevel) {
    EasyModelDisplaySettings.requireLightLevel(lightLevel);
    return copy(
        this.yawDegrees,
        this.scale,
        this.animationTicks,
        this.partAnimator,
        this.partAnimationMode,
        this.partPoseListener,
        this.animation,
        this.textureSetting,
        this.opacity,
        lightLevel,
        this.packedOverlay);
  }

  public EasyModelBlockEntityRenderOptions withoutLightLevelOverride() {
    return copy(
        this.yawDegrees,
        this.scale,
        this.animationTicks,
        this.partAnimator,
        this.partAnimationMode,
        this.partPoseListener,
        this.animation,
        this.textureSetting,
        this.opacity,
        null,
        this.packedOverlay);
  }

  public EasyModelBlockEntityRenderOptions withOverlay(int packedOverlay) {
    return copy(
        this.yawDegrees,
        this.scale,
        this.animationTicks,
        this.partAnimator,
        this.partAnimationMode,
        this.partPoseListener,
        this.animation,
        this.textureSetting,
        this.opacity,
        this.lightLevel,
        packedOverlay);
  }

  public EasyModelBlockEntityRenderOptions withoutOverlayOverride() {
    return copy(
        this.yawDegrees,
        this.scale,
        this.animationTicks,
        this.partAnimator,
        this.partAnimationMode,
        this.partPoseListener,
        this.animation,
        this.textureSetting,
        this.opacity,
        this.lightLevel,
        null);
  }

  @Override
  public boolean equals(Object object) {
    if (this == object) {
      return true;
    }
    if (!(object instanceof EasyModelBlockEntityRenderOptions options)) {
      return false;
    }

    return Objects.equals(this.yawDegrees, options.yawDegrees)
        && Objects.equals(this.scale, options.scale)
        && Objects.equals(this.animationTicks, options.animationTicks)
        && this.partAnimator.equals(options.partAnimator)
        && this.partAnimationMode == options.partAnimationMode
        && this.partPoseListener.equals(options.partPoseListener)
        && Objects.equals(this.animation, options.animation)
        && this.textureSetting.equals(options.textureSetting)
        && Objects.equals(this.opacity, options.opacity)
        && Objects.equals(this.lightLevel, options.lightLevel)
        && Objects.equals(this.packedOverlay, options.packedOverlay);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        this.yawDegrees,
        this.scale,
        this.animationTicks,
        this.partAnimator,
        this.partAnimationMode,
        this.partPoseListener,
        this.animation,
        this.textureSetting,
        this.opacity,
        this.lightLevel,
        this.packedOverlay);
  }

  @Override
  public String toString() {
    return "EasyModelBlockEntityRenderOptions[yawDegrees="
        + this.yawDegrees
        + ", scale="
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
        + ", textureSetting="
        + this.textureSetting
        + ", opacity="
        + this.opacity
        + ", lightLevel="
        + this.lightLevel
        + ", packedOverlay="
        + this.packedOverlay
        + "]";
  }
}
