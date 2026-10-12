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

package de.markusbordihn.easymodelentities.api.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import de.markusbordihn.easymodelentities.api.data.EasyModelBodyType;
import de.markusbordihn.easymodelentities.api.data.client.EasyModelPartAnimationContext;
import de.markusbordihn.easymodelentities.api.data.client.EasyModelPartTransform;
import net.minecraft.util.Mth;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class EasyModelPartAnimatorTest {

  private static EasyModelPartAnimationContext context(String partName) {
    return new EasyModelPartAnimationContext(
        partName, EasyModelBodyType.BIPED, 0.0f, 0.0f, 0.0f, 0.0f, EasyModelPartTransform.NONE);
  }

  @Test
  @DisplayName("Part look turns only the named part, ignoring case")
  void partLookRotatesOnlyTheNamedPart() {
    EasyModelPartAnimator look = EasyModelPartAnimator.partLook("Right_Arm", 30.0f, -45.0f);

    EasyModelPartTransform arm = look.animate(context("right_arm"));

    assertEquals(-45.0f * Mth.DEG_TO_RAD, arm.xRotation(), 0.0001f);
    assertEquals(30.0f * Mth.DEG_TO_RAD, arm.yRotation(), 0.0001f);
    assertEquals(EasyModelPartTransform.NONE, look.animate(context("left_arm")));
  }

  @Test
  @DisplayName("Part look wraps angles to -180..180 degrees")
  void partLookWrapsAngles() {
    EasyModelPartTransform transform =
        EasyModelPartAnimator.partLook("head", 270.0f, 0.0f).animate(context("head"));

    assertEquals(-90.0f * Mth.DEG_TO_RAD, transform.yRotation(), 0.0001f);
  }

  @Test
  @DisplayName("Chained animators add their transforms per part")
  void andThenAddsBothTransforms() {
    EasyModelPartAnimator combined =
        EasyModelPartAnimator.partLook("head", 10.0f, 0.0f)
            .andThen(EasyModelPartAnimator.partLook("head", 5.0f, 20.0f))
            .andThen(EasyModelPartAnimator.partLook("body", 90.0f, 0.0f));

    EasyModelPartTransform head = combined.animate(context("head"));

    assertEquals(15.0f * Mth.DEG_TO_RAD, head.yRotation(), 0.0001f);
    assertEquals(20.0f * Mth.DEG_TO_RAD, head.xRotation(), 0.0001f);
    assertEquals(90.0f * Mth.DEG_TO_RAD, combined.animate(context("body")).yRotation(), 0.0001f);
  }

  @Test
  @DisplayName("Part look rejects a blank part name and non-finite angles")
  void partLookRejectsInvalidInput() {
    assertThrows(
        IllegalArgumentException.class, () -> EasyModelPartAnimator.partLook(" ", 0.0f, 0.0f));
    assertThrows(
        IllegalArgumentException.class,
        () -> EasyModelPartAnimator.partLook("head", Float.NaN, 0.0f));
  }
}
