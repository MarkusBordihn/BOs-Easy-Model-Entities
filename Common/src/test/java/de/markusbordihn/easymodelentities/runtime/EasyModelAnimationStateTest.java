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

package de.markusbordihn.easymodelentities.runtime;

import static org.junit.jupiter.api.Assertions.assertEquals;

import de.markusbordihn.easymodelentities.api.EasyModelAnimationStates;
import org.junit.jupiter.api.Test;

class EasyModelAnimationStateTest {

  @Test
  void serializedNamesAreStable() {
    assertEquals("auto", EasyModelAnimationState.AUTO.getSerializedName());
    assertEquals("idle", EasyModelAnimationState.IDLE.getSerializedName());
    assertEquals("walk", EasyModelAnimationState.WALK.getSerializedName());
    assertEquals("run", EasyModelAnimationState.RUN.getSerializedName());
    assertEquals("hurt", EasyModelAnimationState.HURT.getSerializedName());
    assertEquals("death", EasyModelAnimationState.DEATH.getSerializedName());
    assertEquals("swim", EasyModelAnimationState.SWIM.getSerializedName());
    assertEquals("fly", EasyModelAnimationState.FLY.getSerializedName());
  }

  @Test
  void unknownSerializedNamesFallBackToAuto() {
    assertEquals(EasyModelAnimationState.AUTO, EasyModelAnimationState.bySerializedName("unknown"));
    assertEquals(EasyModelAnimationState.AUTO, EasyModelAnimationState.bySerializedName(""));
    assertEquals(EasyModelAnimationState.AUTO, EasyModelAnimationState.bySerializedName(null));
  }

  @Test
  void apiStateMappingIsStable() {
    assertEquals(EasyModelAnimationState.AUTO, EasyModelAnimationState.byApiState(999));
    assertEquals(
        EasyModelAnimationState.AUTO,
        EasyModelAnimationState.byApiState(EasyModelAnimationStates.AUTO));
    assertEquals(
        EasyModelAnimationState.IDLE,
        EasyModelAnimationState.byApiState(EasyModelAnimationStates.IDLE));
    assertEquals(
        EasyModelAnimationState.WALK,
        EasyModelAnimationState.byApiState(EasyModelAnimationStates.WALK));
    assertEquals(
        EasyModelAnimationState.RUN,
        EasyModelAnimationState.byApiState(EasyModelAnimationStates.RUN));
    assertEquals(
        EasyModelAnimationState.HURT,
        EasyModelAnimationState.byApiState(EasyModelAnimationStates.HURT));
    assertEquals(
        EasyModelAnimationState.DEATH,
        EasyModelAnimationState.byApiState(EasyModelAnimationStates.DEATH));
    assertEquals(
        EasyModelAnimationState.SWIM,
        EasyModelAnimationState.byApiState(EasyModelAnimationStates.SWIM));
    assertEquals(
        EasyModelAnimationState.FLY,
        EasyModelAnimationState.byApiState(EasyModelAnimationStates.FLY));
    assertEquals(EasyModelAnimationStates.WALK, EasyModelAnimationState.WALK.getApiState());
    assertEquals(EasyModelAnimationStates.SWIM, EasyModelAnimationState.SWIM.getApiState());
    assertEquals(EasyModelAnimationStates.FLY, EasyModelAnimationState.FLY.getApiState());
  }
}
