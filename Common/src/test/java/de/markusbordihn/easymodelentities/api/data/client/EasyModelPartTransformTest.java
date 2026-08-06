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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class EasyModelPartTransformTest {

  @Test
  void threeArgConstructorYieldsIdentityOffsetScaleVisibility() {
    EasyModelPartTransform transform = new EasyModelPartTransform(0.1f, 0.2f, 0.3f);
    assertEquals(0.1f, transform.xRotation());
    assertEquals(0.2f, transform.yRotation());
    assertEquals(0.3f, transform.zRotation());
    assertEquals(0.0f, transform.offsetX());
    assertEquals(0.0f, transform.offsetY());
    assertEquals(0.0f, transform.offsetZ());
    assertEquals(1.0f, transform.scaleX());
    assertEquals(1.0f, transform.scaleY());
    assertEquals(1.0f, transform.scaleZ());
    assertTrue(transform.visible());
  }

  @Test
  void noneIsIdentity() {
    assertTrue(EasyModelPartTransform.NONE.isIdentity());
    assertTrue(new EasyModelPartTransform(0.0f, 0.0f, 0.0f).isIdentity());
    assertFalse(new EasyModelPartTransform(0.1f, 0.0f, 0.0f).isIdentity());
    assertFalse(EasyModelPartTransform.NONE.withOffset(1.0f, 0.0f, 0.0f).isIdentity());
    assertFalse(EasyModelPartTransform.NONE.withScale(2.0f).isIdentity());
    assertFalse(EasyModelPartTransform.NONE.withVisible(false).isIdentity());
  }

  @Test
  void addComposesRotationOffsetScaleVisibility() {
    EasyModelPartTransform a =
        EasyModelPartTransform.NONE
            .withOffset(1.0f, 2.0f, 3.0f)
            .withScale(2.0f, 2.0f, 2.0f)
            .withVisible(true);
    EasyModelPartTransform b =
        new EasyModelPartTransform(0.5f, 0.0f, 0.0f)
            .withOffset(4.0f, 0.0f, 0.0f)
            .withScale(3.0f)
            .withVisible(false);

    EasyModelPartTransform result = a.add(b);

    assertEquals(0.5f, result.xRotation());
    assertEquals(5.0f, result.offsetX());
    assertEquals(2.0f, result.offsetY());
    assertEquals(3.0f, result.offsetZ());
    assertEquals(6.0f, result.scaleX());
    assertEquals(6.0f, result.scaleY());
    assertEquals(6.0f, result.scaleZ());
    assertFalse(result.visible());
  }

  @Test
  void addRejectsNullTransform() {
    assertThrows(NullPointerException.class, () -> EasyModelPartTransform.NONE.add(null));
  }

  @Test
  void uniformScaleHelperSetsAllAxes() {
    EasyModelPartTransform transform = EasyModelPartTransform.NONE.withScale(1.5f);
    assertEquals(1.5f, transform.scaleX());
    assertEquals(1.5f, transform.scaleY());
    assertEquals(1.5f, transform.scaleZ());
  }

  @Test
  void withHelpersPreserveOtherComponents() {
    EasyModelPartTransform base =
        new EasyModelPartTransform(0.1f, 0.2f, 0.3f).withOffset(1.0f, 1.0f, 1.0f).withScale(2.0f);
    EasyModelPartTransform hidden = base.withVisible(false);
    assertEquals(base.xRotation(), hidden.xRotation());
    assertEquals(base.offsetX(), hidden.offsetX());
    assertEquals(base.scaleX(), hidden.scaleX());
    assertFalse(hidden.visible());
  }

  @Test
  void rejectsNonFiniteComponentsAtConstruction() {
    assertThrows(
        IllegalArgumentException.class, () -> new EasyModelPartTransform(Float.NaN, 0.0f, 0.0f));
    assertThrows(
        IllegalArgumentException.class,
        () -> EasyModelPartTransform.NONE.withOffset(Float.POSITIVE_INFINITY, 0.0f, 0.0f));
    assertThrows(
        IllegalArgumentException.class,
        () -> EasyModelPartTransform.NONE.withScale(Float.NEGATIVE_INFINITY));
  }
}
