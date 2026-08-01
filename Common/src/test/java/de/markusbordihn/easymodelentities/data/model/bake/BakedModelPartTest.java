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

package de.markusbordihn.easymodelentities.data.model.bake;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.markusbordihn.easymodelentities.data.model.ModelPartType;
import de.markusbordihn.easymodelentities.data.model.Vec3f;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class BakedModelPartTest {

  private static BakedModelPart part(String name) {
    return new BakedModelPart(name, Vec3f.ZERO, Vec3f.ZERO, List.of(), List.of());
  }

  @Test
  @DisplayName("The part type is derived from the bone name")
  void partTypeIsDerivedFromName() {
    assertEquals(ModelPartType.HEAD, part("head").partType());
    assertEquals(ModelPartType.LEFT_WING, part("left_wing").partType());
    assertEquals(ModelPartType.UNKNOWN, part("head_r1").partType());
    assertEquals(ModelPartType.UNKNOWN, part("something_else").partType());
  }

  @Test
  @DisplayName("Tail bones are recognized by name at bake time")
  void tailPartIsDerivedFromName() {
    assertTrue(part("tail").tailPart());
    assertTrue(part("tail_fin").tailPart());
    assertTrue(part("tail_tip").tailPart());
    assertTrue(part("lizard_tail").tailPart());
    assertTrue(part("lizard_tail_tip").tailPart());
    assertFalse(part("head").tailPart());
    assertFalse(part("tailored").tailPart());
  }
}
