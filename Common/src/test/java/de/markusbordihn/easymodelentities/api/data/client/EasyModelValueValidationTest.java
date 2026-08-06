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
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import de.markusbordihn.easymodelentities.api.data.EasyModelBodyType;
import de.markusbordihn.easymodelentities.api.data.EasyModelVec3f;
import org.junit.jupiter.api.Test;

class EasyModelValueValidationTest {

  @Test
  void directHeadLookConstructionNormalizesAngles() {
    EasyModelHeadLook headLook = new EasyModelHeadLook(370.0f, 90.0f);

    assertEquals(10.0f, headLook.yaw());
    assertEquals(60.0f, headLook.pitch());
    assertSame(EasyModelHeadLook.NONE, EasyModelHeadLook.of(Float.NaN, 0.0f));
    assertThrows(IllegalArgumentException.class, () -> new EasyModelHeadLook(Float.NaN, 0.0f));
  }

  @Test
  void itemAnchorRequiresAUsablePart() {
    EasyModelItemAnchor anchor = new EasyModelItemAnchor(" hand ", EasyModelVec3f.ZERO);

    assertEquals("hand", anchor.partName());
    assertThrows(
        NullPointerException.class, () -> new EasyModelItemAnchor(null, EasyModelVec3f.ZERO));
    assertThrows(
        IllegalArgumentException.class, () -> new EasyModelItemAnchor(" ", EasyModelVec3f.ZERO));
    assertThrows(NullPointerException.class, () -> new EasyModelItemAnchor("hand", null));
  }

  @Test
  void animationInfoRejectsImpossibleMetrics() {
    assertThrows(
        IllegalArgumentException.class,
        () -> new EasyModelAnimationInfo("walk", -1.0f, 0.0f, true, 0.0f, 0, 0, 0));
    assertThrows(
        IllegalArgumentException.class,
        () -> new EasyModelAnimationInfo("walk", 0.0f, 0.0f, true, 0.0f, -1, 0, 0));
    assertThrows(
        IllegalArgumentException.class,
        () -> new EasyModelAnimationInfo(" ", 0.0f, 0.0f, true, 0.0f, 0, 0, 0));
  }

  @Test
  void partAnimationContextRequiresCompleteFiniteInput() {
    assertThrows(
        NullPointerException.class,
        () ->
            new EasyModelPartAnimationContext(
                "head", null, 0.0f, 0.0f, 0.0f, 0.0f, EasyModelPartTransform.NONE));
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new EasyModelPartAnimationContext(
                "head",
                EasyModelBodyType.BIPED,
                Float.NaN,
                0.0f,
                0.0f,
                0.0f,
                EasyModelPartTransform.NONE));
  }
}
