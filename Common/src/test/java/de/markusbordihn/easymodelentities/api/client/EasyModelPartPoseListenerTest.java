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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.markusbordihn.easymodelentities.api.data.EasyModelVec3f;
import de.markusbordihn.easymodelentities.api.data.client.EasyModelPartPose;
import java.util.ArrayList;
import java.util.List;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.junit.jupiter.api.Test;

class EasyModelPartPoseListenerTest {

  private static EasyModelPartPose partPose(String partName) {
    return new EasyModelPartPose(
        partName, new Matrix4f(), new Matrix3f(), EasyModelVec3f.ZERO, EasyModelVec3f.ZERO);
  }

  private static EasyModelPartPoseListener collecting(String wantedPart, List<String> collected) {
    return new EasyModelPartPoseListener() {

      @Override
      public boolean wantsPart(String partName) {
        return wantedPart.equals(partName);
      }

      @Override
      public void onPartPose(EasyModelPartPose partPose) {
        collected.add(partPose.partName());
      }
    };
  }

  @Test
  void combinesWantedPartsOfBothListeners() {
    List<String> collected = new ArrayList<>();
    EasyModelPartPoseListener combined =
        collecting("head", collected).andThen(collecting("right_arm", collected));

    assertTrue(combined.wantsPart("head"));
    assertTrue(combined.wantsPart("right_arm"));
    assertFalse(combined.wantsPart("left_leg"));
  }

  @Test
  void routesEachPoseOnlyToTheListenerThatWantsIt() {
    List<String> first = new ArrayList<>();
    List<String> second = new ArrayList<>();
    EasyModelPartPoseListener combined =
        collecting("head", first).andThen(collecting("right_arm", second));

    combined.onPartPose(partPose("head"));
    combined.onPartPose(partPose("right_arm"));

    assertEquals(List.of("head"), first);
    assertEquals(List.of("right_arm"), second);
  }

  @Test
  void combiningWithNoneKeepsTheOtherListener() {
    EasyModelPartPoseListener listener = collecting("head", new ArrayList<>());

    assertSame(listener, listener.andThen(EasyModelPartPoseListener.NONE));
    assertSame(listener, listener.andThen(null));
    assertSame(listener, EasyModelPartPoseListener.NONE.andThen(listener));
  }
}
