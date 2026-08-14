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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.markusbordihn.easymodelentities.api.data.EasyModelAnimation;
import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.Test;

class EasyModelBlockEntityRenderOptionsTest {

  private static final Identifier SCREEN =
      Identifier.fromNamespaceAndPath("example", "textures/block/lantern/screen_sad.png");

  @Test
  void buildsValidatedOptions() {
    EasyModelBlockEntityRenderOptions options =
        EasyModelBlockEntityRenderOptions.DEFAULT
            .withYawDegrees(90.0f)
            .withScale(1.5f)
            .withAnimationTicks(8.0f)
            .withAnimation(EasyModelAnimation.ATTACK);

    assertEquals(90.0f, options.yawDegrees());
    assertEquals(1.5f, options.scale());
    assertEquals(8.0f, options.animationTicks());
    assertEquals(EasyModelAnimation.ATTACK, options.animation());
    assertNull(options.withoutAnimationOverride().animation());
  }

  @Test
  void rejectsInvalidNumericOptions() {
    assertThrows(
        IllegalArgumentException.class,
        () -> EasyModelBlockEntityRenderOptions.DEFAULT.withYawDegrees(Float.NaN));
    assertThrows(
        IllegalArgumentException.class,
        () -> EasyModelBlockEntityRenderOptions.DEFAULT.withScale(0.0f));
    assertThrows(
        IllegalArgumentException.class,
        () -> EasyModelBlockEntityRenderOptions.DEFAULT.withAnimationTicks(-1.0f));
  }

  @Test
  void rejectsNullCollaboratorsAndOverrides() {
    assertThrows(
        NullPointerException.class,
        () -> EasyModelBlockEntityRenderOptions.DEFAULT.withPartAnimator(null));
    assertThrows(
        NullPointerException.class,
        () -> EasyModelBlockEntityRenderOptions.DEFAULT.withPartAnimationMode(null));
    assertThrows(
        NullPointerException.class,
        () -> EasyModelBlockEntityRenderOptions.DEFAULT.withPartPoseListener(null));
    assertThrows(
        NullPointerException.class,
        () -> EasyModelBlockEntityRenderOptions.DEFAULT.withAnimation((EasyModelAnimation) null));
    assertThrows(
        NullPointerException.class,
        () -> EasyModelBlockEntityRenderOptions.DEFAULT.withTextureSetting(null));
  }

  @Test
  void defaultHasNoTextureOverride() {
    assertTrue(EasyModelBlockEntityRenderOptions.DEFAULT.textureSetting().isEmpty());
  }

  @Test
  void withTextureAddsSlotAndPreservesOtherComponents() {
    EasyModelBlockEntityRenderOptions options =
        EasyModelBlockEntityRenderOptions.DEFAULT
            .withYawDegrees(90.0f)
            .withTexture("screen", SCREEN);

    assertEquals(SCREEN, options.textureSetting().texture("screen").orElse(null));
    assertEquals(90.0f, options.yawDegrees());
    assertTrue(options.withoutTextureOverride().textureSetting().isEmpty());
  }
}
