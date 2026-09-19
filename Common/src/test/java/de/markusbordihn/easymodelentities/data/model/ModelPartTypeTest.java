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

package de.markusbordihn.easymodelentities.data.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Locale;
import org.junit.jupiter.api.Test;

class ModelPartTypeTest {

  @Test
  void tagNamesAreStable() {
    assertEquals("root", ModelPartType.ROOT.getTagName());
    assertEquals("body", ModelPartType.BODY.getTagName());
    assertEquals("head", ModelPartType.HEAD.getTagName());
    assertEquals("left_arm", ModelPartType.LEFT_ARM.getTagName());
    assertEquals("right_arm", ModelPartType.RIGHT_ARM.getTagName());
    assertEquals("left_hand", ModelPartType.LEFT_HAND.getTagName());
    assertEquals("right_hand", ModelPartType.RIGHT_HAND.getTagName());
    assertEquals("left_item", ModelPartType.LEFT_ITEM.getTagName());
    assertEquals("right_item", ModelPartType.RIGHT_ITEM.getTagName());
    assertEquals("left_leg", ModelPartType.LEFT_LEG.getTagName());
    assertEquals("right_leg", ModelPartType.RIGHT_LEG.getTagName());
    assertEquals("front_left_leg", ModelPartType.FRONT_LEFT_LEG.getTagName());
    assertEquals("front_right_leg", ModelPartType.FRONT_RIGHT_LEG.getTagName());
    assertEquals("back_left_leg", ModelPartType.BACK_LEFT_LEG.getTagName());
    assertEquals("back_right_leg", ModelPartType.BACK_RIGHT_LEG.getTagName());
    assertEquals("middle_front_left_leg", ModelPartType.MIDDLE_FRONT_LEFT_LEG.getTagName());
    assertEquals("middle_front_right_leg", ModelPartType.MIDDLE_FRONT_RIGHT_LEG.getTagName());
    assertEquals("middle_back_left_leg", ModelPartType.MIDDLE_BACK_LEFT_LEG.getTagName());
    assertEquals("middle_back_right_leg", ModelPartType.MIDDLE_BACK_RIGHT_LEG.getTagName());
    assertEquals("left_wing", ModelPartType.LEFT_WING.getTagName());
    assertEquals("right_wing", ModelPartType.RIGHT_WING.getTagName());
    assertEquals("tail", ModelPartType.TAIL.getTagName());
    assertEquals("tail_fin", ModelPartType.TAIL_FIN.getTagName());
  }

  @Test
  void tagNameMatchesLowerCasedEnumName() {
    for (ModelPartType partType : ModelPartType.values()) {
      if (partType == ModelPartType.UNKNOWN) {
        continue;
      }
      assertEquals(partType.name().toLowerCase(Locale.ROOT), partType.getTagName());
    }
  }

  @Test
  void getResolvesTagNames() {
    assertEquals(ModelPartType.RIGHT_LEG, ModelPartType.get("right_leg"));
    assertEquals(ModelPartType.MIDDLE_BACK_LEFT_LEG, ModelPartType.get("middle_back_left_leg"));
    assertEquals(ModelPartType.TAIL_FIN, ModelPartType.get("tail_fin"));
  }

  @Test
  void getIsCaseInsensitive() {
    assertEquals(ModelPartType.RIGHT_LEG, ModelPartType.get("RIGHT_LEG"));
    assertEquals(ModelPartType.RIGHT_LEG, ModelPartType.get("Right_Leg"));
  }

  @Test
  void getFallsBackToUnknown() {
    assertEquals(ModelPartType.UNKNOWN, ModelPartType.get("does_not_exist"));
    assertEquals(ModelPartType.UNKNOWN, ModelPartType.get(""));
    assertEquals(ModelPartType.UNKNOWN, ModelPartType.get(null));
  }
}
