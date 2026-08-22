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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.markusbordihn.easymodelentities.api.data.EasyModelAnimation;
import de.markusbordihn.easymodelentities.api.data.EasyModelTextureSetting;
import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.Test;

class EasyModelEntityRenderOptionsTest {

  private static final Identifier SCREEN =
      Identifier.fromNamespaceAndPath("example", "textures/entity/echo/screen_sad.png");

  @Test
  void defaultHasNullScale() {
    assertNull(EasyModelEntityRenderOptions.DEFAULT.scale());
    assertNull(EasyModelEntityRenderOptions.DEFAULT.animation());
  }

  @Test
  void withScaleSetsScaleAndPreservesOtherComponents() {
    EasyModelEntityRenderOptions options =
        EasyModelEntityRenderOptions.DEFAULT
            .withAnimationTicks(12.0f)
            .withPartAnimationMode(EasyModelPartAnimationMode.REPLACE)
            .withScale(2.5f);

    assertEquals(2.5f, options.scale());
    assertEquals(12.0f, options.animationTicks());
    assertEquals(EasyModelPartAnimationMode.REPLACE, options.partAnimationMode());
  }

  @Test
  void withAnimationUsesSingleTypedSelection() {
    EasyModelEntityRenderOptions options =
        EasyModelEntityRenderOptions.DEFAULT.withAnimation("talk");

    assertEquals(EasyModelAnimation.named("talk"), options.animation());
  }

  @Test
  void withAnimationRejectsBlanks() {
    assertThrows(
        IllegalArgumentException.class,
        () -> EasyModelEntityRenderOptions.DEFAULT.withAnimation(""));
    assertThrows(
        IllegalArgumentException.class,
        () -> EasyModelEntityRenderOptions.DEFAULT.withAnimation("   "));
  }

  @Test
  void withAnimationOverwritesPreviousAnimation() {
    EasyModelEntityRenderOptions options =
        EasyModelEntityRenderOptions.DEFAULT.withAnimation("talk").withAnimation("wait");

    assertEquals(EasyModelAnimation.named("wait"), options.animation());
  }

  @Test
  void animationOverrideCanBeRemovedExplicitly() {
    EasyModelEntityRenderOptions options =
        EasyModelEntityRenderOptions.DEFAULT
            .withAnimation(EasyModelAnimation.ATTACK)
            .withoutAnimationOverride();

    assertNull(options.animation());
  }

  @Test
  void rejectsInvalidNumericOptions() {
    assertThrows(
        IllegalArgumentException.class, () -> EasyModelEntityRenderOptions.DEFAULT.withScale(0.0f));
    assertThrows(
        IllegalArgumentException.class,
        () -> EasyModelEntityRenderOptions.DEFAULT.withScale(Float.NaN));
    assertThrows(
        IllegalArgumentException.class,
        () -> EasyModelEntityRenderOptions.DEFAULT.withAnimationTicks(-1.0f));
    assertThrows(
        IllegalArgumentException.class,
        () -> EasyModelEntityRenderOptions.DEFAULT.withAnimationTicks(Float.POSITIVE_INFINITY));
  }

  @Test
  void rejectsNullCollaboratorsAndOverrides() {
    assertThrows(
        NullPointerException.class,
        () -> EasyModelEntityRenderOptions.DEFAULT.withPartAnimator(null));
    assertThrows(
        NullPointerException.class,
        () -> EasyModelEntityRenderOptions.DEFAULT.withPartAnimationMode(null));
    assertThrows(
        NullPointerException.class,
        () -> EasyModelEntityRenderOptions.DEFAULT.withPartPoseListener(null));
    assertThrows(
        NullPointerException.class,
        () -> EasyModelEntityRenderOptions.DEFAULT.withAnimation((EasyModelAnimation) null));
    assertThrows(
        NullPointerException.class,
        () -> EasyModelEntityRenderOptions.DEFAULT.withTextureSetting(null));
  }

  @Test
  void defaultHasNoTextureOverride() {
    assertTrue(EasyModelEntityRenderOptions.DEFAULT.textureSetting().isEmpty());
  }

  @Test
  void withTextureAddsSlotAndPreservesOtherComponents() {
    EasyModelEntityRenderOptions options =
        EasyModelEntityRenderOptions.DEFAULT
            .withAnimation("talk")
            .withScale(2.0f)
            .withTexture("screen", SCREEN);

    assertEquals(SCREEN, options.textureSetting().texture("screen").orElse(null));
    assertEquals(EasyModelAnimation.named("talk"), options.animation());
    assertEquals(2.0f, options.scale());
  }

  @Test
  void textureOverrideCanBeRemovedExplicitly() {
    EasyModelEntityRenderOptions options =
        EasyModelEntityRenderOptions.DEFAULT.withTexture("screen", SCREEN).withoutTextureOverride();

    assertTrue(options.textureSetting().isEmpty());
  }

  @Test
  void optionsWithTheSameTextureOverrideAreEqual() {
    assertEquals(
        EasyModelEntityRenderOptions.DEFAULT.withTexture("screen", SCREEN),
        EasyModelEntityRenderOptions.DEFAULT.withTextureSetting(
            EasyModelTextureSetting.of("screen", SCREEN)));
    assertNotEquals(
        EasyModelEntityRenderOptions.DEFAULT.withTexture("screen", SCREEN),
        EasyModelEntityRenderOptions.DEFAULT);
  }

  @Test
  void defaultHasNoDisplayOverrides() {
    assertNull(EasyModelEntityRenderOptions.DEFAULT.opacity());
    assertNull(EasyModelEntityRenderOptions.DEFAULT.lightLevel());
    assertNull(EasyModelEntityRenderOptions.DEFAULT.packedOverlay());
  }

  @Test
  void withOpacityKeepsOtherComponents() {
    EasyModelEntityRenderOptions options =
        EasyModelEntityRenderOptions.DEFAULT.withScale(2.0f).withOpacity(0.4f);

    assertEquals(0.4f, options.opacity());
    assertEquals(2.0f, options.scale());
  }

  @Test
  void rejectsOpacityOutsideTheAllowedRange() {
    assertThrows(
        IllegalArgumentException.class,
        () -> EasyModelEntityRenderOptions.DEFAULT.withOpacity(-0.1f));
    assertThrows(
        IllegalArgumentException.class,
        () -> EasyModelEntityRenderOptions.DEFAULT.withOpacity(1.1f));
    assertThrows(
        IllegalArgumentException.class,
        () -> EasyModelEntityRenderOptions.DEFAULT.withOpacity(Float.NaN));
  }

  @Test
  void rejectsLightLevelOutsideTheAllowedRange() {
    assertThrows(
        IllegalArgumentException.class,
        () -> EasyModelEntityRenderOptions.DEFAULT.withLightLevel(-1));
    assertThrows(
        IllegalArgumentException.class,
        () -> EasyModelEntityRenderOptions.DEFAULT.withLightLevel(16));
  }

  @Test
  void displayOverridesCanBeRemovedExplicitly() {
    EasyModelEntityRenderOptions options =
        EasyModelEntityRenderOptions.DEFAULT
            .withOpacity(0.4f)
            .withLightLevel(15)
            .withOverlay(0x00560078)
            .withoutOpacityOverride()
            .withoutLightLevelOverride()
            .withoutOverlayOverride();

    assertNull(options.opacity());
    assertNull(options.lightLevel());
    assertNull(options.packedOverlay());
  }

  @Test
  void optionsWithTheSameDisplayOverridesAreEqual() {
    assertEquals(
        EasyModelEntityRenderOptions.DEFAULT.withOpacity(0.4f).withLightLevel(15),
        EasyModelEntityRenderOptions.DEFAULT.withOpacity(0.4f).withLightLevel(15));
    assertNotEquals(
        EasyModelEntityRenderOptions.DEFAULT.withOpacity(0.4f),
        EasyModelEntityRenderOptions.DEFAULT.withOpacity(0.5f));
  }
}
