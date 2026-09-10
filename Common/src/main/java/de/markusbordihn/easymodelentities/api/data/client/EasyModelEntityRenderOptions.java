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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

public final class EasyModelEntityRenderOptions {

  public static final EasyModelEntityRenderOptions DEFAULT =
      new EasyModelEntityRenderOptions(
          null,
          null,
          EasyModelPartAnimator.NONE,
          EasyModelPartAnimationMode.ADD,
          EasyModelPartPoseListener.NONE,
          null,
          null,
          EasyModelTextureSetting.EMPTY,
          null,
          null,
          null,
          EasyModelHandItems.NONE);

  private final Float scale;
  private final Float animationTicks;
  private final EasyModelPartAnimator partAnimator;
  private final EasyModelPartAnimationMode partAnimationMode;
  private final EasyModelPartPoseListener partPoseListener;
  private final EasyModelAnimation animation;
  private final EasyModelHeadLook headLook;
  private final EasyModelTextureSetting textureSetting;
  private final Float opacity;
  private final Integer lightLevel;
  private final Integer packedOverlay;
  private final EasyModelHandItems handItems;

  private EasyModelEntityRenderOptions(
      Float scale,
      Float animationTicks,
      EasyModelPartAnimator partAnimator,
      EasyModelPartAnimationMode partAnimationMode,
      EasyModelPartPoseListener partPoseListener,
      EasyModelAnimation animation,
      EasyModelHeadLook headLook,
      EasyModelTextureSetting textureSetting,
      Float opacity,
      Integer lightLevel,
      Integer packedOverlay,
      EasyModelHandItems handItems) {
    this.scale = scale;
    this.animationTicks = animationTicks;
    this.partAnimator = Objects.requireNonNull(partAnimator, "partAnimator");
    this.partAnimationMode = Objects.requireNonNull(partAnimationMode, "partAnimationMode");
    this.partPoseListener = Objects.requireNonNull(partPoseListener, "partPoseListener");
    this.animation = animation;
    this.headLook = headLook;
    this.textureSetting = Objects.requireNonNull(textureSetting, "textureSetting");
    this.opacity = opacity;
    this.lightLevel = lightLevel;
    this.packedOverlay = packedOverlay;
    this.handItems = Objects.requireNonNull(handItems, "handItems");
  }

  private static EasyModelEntityRenderOptions copy(
      Float scale,
      Float animationTicks,
      EasyModelPartAnimator partAnimator,
      EasyModelPartAnimationMode partAnimationMode,
      EasyModelPartPoseListener partPoseListener,
      EasyModelAnimation animation,
      EasyModelHeadLook headLook,
      EasyModelTextureSetting textureSetting,
      Float opacity,
      Integer lightLevel,
      Integer packedOverlay,
      EasyModelHandItems handItems) {
    return new EasyModelEntityRenderOptions(
        scale,
        animationTicks,
        partAnimator,
        partAnimationMode,
        partPoseListener,
        animation,
        headLook,
        textureSetting,
        opacity,
        lightLevel,
        packedOverlay,
        handItems);
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

  public EasyModelHandItems handItems() {
    return this.handItems;
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
        this.headLook,
        this.textureSetting,
        this.opacity,
        this.lightLevel,
        this.packedOverlay,
        this.handItems);
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
        this.headLook,
        this.textureSetting,
        this.opacity,
        this.lightLevel,
        this.packedOverlay,
        this.handItems);
  }

  public EasyModelEntityRenderOptions withPartAnimator(EasyModelPartAnimator partAnimator) {
    return copy(
        this.scale,
        this.animationTicks,
        Objects.requireNonNull(partAnimator, "partAnimator"),
        this.partAnimationMode,
        this.partPoseListener,
        this.animation,
        this.headLook,
        this.textureSetting,
        this.opacity,
        this.lightLevel,
        this.packedOverlay,
        this.handItems);
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
        this.headLook,
        this.textureSetting,
        this.opacity,
        this.lightLevel,
        this.packedOverlay,
        this.handItems);
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
        this.headLook,
        this.textureSetting,
        this.opacity,
        this.lightLevel,
        this.packedOverlay,
        this.handItems);
  }

  public EasyModelEntityRenderOptions withAnimation(EasyModelAnimation animation) {
    return copy(
        this.scale,
        this.animationTicks,
        this.partAnimator,
        this.partAnimationMode,
        this.partPoseListener,
        Objects.requireNonNull(animation, "animation"),
        this.headLook,
        this.textureSetting,
        this.opacity,
        this.lightLevel,
        this.packedOverlay,
        this.handItems);
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
        this.headLook,
        this.textureSetting,
        this.opacity,
        this.lightLevel,
        this.packedOverlay,
        this.handItems);
  }

  public EasyModelEntityRenderOptions withHeadLook(EasyModelHeadLook headLook) {
    return copy(
        this.scale,
        this.animationTicks,
        this.partAnimator,
        this.partAnimationMode,
        this.partPoseListener,
        this.animation,
        Objects.requireNonNull(headLook, "headLook"),
        this.textureSetting,
        this.opacity,
        this.lightLevel,
        this.packedOverlay,
        this.handItems);
  }

  public EasyModelEntityRenderOptions withTextureSetting(EasyModelTextureSetting textureSetting) {
    return copy(
        this.scale,
        this.animationTicks,
        this.partAnimator,
        this.partAnimationMode,
        this.partPoseListener,
        this.animation,
        this.headLook,
        Objects.requireNonNull(textureSetting, "textureSetting"),
        this.opacity,
        this.lightLevel,
        this.packedOverlay,
        this.handItems);
  }

  public EasyModelEntityRenderOptions withTexture(String slot, ResourceLocation texture) {
    return this.withTextureSetting(this.textureSetting.withSlot(slot, texture));
  }

  public EasyModelEntityRenderOptions withTexture(
      String slot, ResourceLocation texture, EasyModelTextureBlend blend) {
    return this.withTextureSetting(this.textureSetting.withSlot(slot, texture, blend));
  }

  public EasyModelEntityRenderOptions withTextureBlend(String slot, EasyModelTextureBlend blend) {
    return this.withTextureSetting(this.textureSetting.withBlend(slot, blend));
  }

  public EasyModelEntityRenderOptions withoutTextureOverride() {
    return this.withTextureSetting(EasyModelTextureSetting.EMPTY);
  }

  public EasyModelEntityRenderOptions withOpacity(float opacity) {
    EasyModelDisplaySettings.requireOpacity(opacity);
    return copy(
        this.scale,
        this.animationTicks,
        this.partAnimator,
        this.partAnimationMode,
        this.partPoseListener,
        this.animation,
        this.headLook,
        this.textureSetting,
        opacity,
        this.lightLevel,
        this.packedOverlay,
        this.handItems);
  }

  public EasyModelEntityRenderOptions withoutOpacityOverride() {
    return copy(
        this.scale,
        this.animationTicks,
        this.partAnimator,
        this.partAnimationMode,
        this.partPoseListener,
        this.animation,
        this.headLook,
        this.textureSetting,
        null,
        this.lightLevel,
        this.packedOverlay,
        this.handItems);
  }

  public EasyModelEntityRenderOptions withLightLevel(int lightLevel) {
    EasyModelDisplaySettings.requireLightLevel(lightLevel);
    return copy(
        this.scale,
        this.animationTicks,
        this.partAnimator,
        this.partAnimationMode,
        this.partPoseListener,
        this.animation,
        this.headLook,
        this.textureSetting,
        this.opacity,
        lightLevel,
        this.packedOverlay,
        this.handItems);
  }

  public EasyModelEntityRenderOptions withoutLightLevelOverride() {
    return copy(
        this.scale,
        this.animationTicks,
        this.partAnimator,
        this.partAnimationMode,
        this.partPoseListener,
        this.animation,
        this.headLook,
        this.textureSetting,
        this.opacity,
        null,
        this.packedOverlay,
        this.handItems);
  }

  public EasyModelEntityRenderOptions withOverlay(int packedOverlay) {
    return copy(
        this.scale,
        this.animationTicks,
        this.partAnimator,
        this.partAnimationMode,
        this.partPoseListener,
        this.animation,
        this.headLook,
        this.textureSetting,
        this.opacity,
        this.lightLevel,
        packedOverlay,
        this.handItems);
  }

  public EasyModelEntityRenderOptions withoutOverlayOverride() {
    return copy(
        this.scale,
        this.animationTicks,
        this.partAnimator,
        this.partAnimationMode,
        this.partPoseListener,
        this.animation,
        this.headLook,
        this.textureSetting,
        this.opacity,
        this.lightLevel,
        null,
        this.handItems);
  }

  public EasyModelEntityRenderOptions withHandItems(EasyModelHandItems handItems) {
    return copy(
        this.scale,
        this.animationTicks,
        this.partAnimator,
        this.partAnimationMode,
        this.partPoseListener,
        this.animation,
        this.headLook,
        this.textureSetting,
        this.opacity,
        this.lightLevel,
        this.packedOverlay,
        Objects.requireNonNull(handItems, "handItems"));
  }

  public EasyModelEntityRenderOptions withHandItems(LivingEntity holder) {
    return this.withHandItems(EasyModelHandItems.of(holder));
  }

  public EasyModelEntityRenderOptions withoutHandItems() {
    return this.withHandItems(EasyModelHandItems.NONE);
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
        && Objects.equals(this.headLook, options.headLook)
        && this.textureSetting.equals(options.textureSetting)
        && Objects.equals(this.opacity, options.opacity)
        && Objects.equals(this.lightLevel, options.lightLevel)
        && Objects.equals(this.packedOverlay, options.packedOverlay)
        && this.handItems.equals(options.handItems);
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
        this.headLook,
        this.textureSetting,
        this.opacity,
        this.lightLevel,
        this.packedOverlay,
        this.handItems);
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
        + ", textureSetting="
        + this.textureSetting
        + ", opacity="
        + this.opacity
        + ", lightLevel="
        + this.lightLevel
        + ", packedOverlay="
        + this.packedOverlay
        + ", handItems="
        + this.handItems
        + "]";
  }
}
