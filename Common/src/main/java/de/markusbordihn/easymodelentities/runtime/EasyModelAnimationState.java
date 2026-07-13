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

import de.markusbordihn.easymodelentities.api.EasyModelAnimationStates;
import java.util.Locale;

public enum EasyModelAnimationState {
  AUTO(EasyModelAnimationStates.AUTO),
  IDLE(EasyModelAnimationStates.IDLE),
  WALK(EasyModelAnimationStates.WALK),
  RUN(EasyModelAnimationStates.RUN),
  HURT(EasyModelAnimationStates.HURT),
  DEATH(EasyModelAnimationStates.DEATH),
  SWIM(EasyModelAnimationStates.SWIM),
  FLY(EasyModelAnimationStates.FLY),
  ATTACK(EasyModelAnimationStates.ATTACK);

  private static final EasyModelAnimationState[] VALUES = values();

  private final String serializedName = this.name().toLowerCase(Locale.ROOT);
  private final int apiState;

  EasyModelAnimationState(int apiState) {
    this.apiState = apiState;
  }

  public static EasyModelAnimationState bySerializedName(String serializedName) {
    if (serializedName == null) {
      return AUTO;
    }

    String normalizedName = serializedName.toLowerCase(Locale.ROOT);
    for (EasyModelAnimationState animationState : VALUES) {
      if (animationState.getSerializedName().equals(normalizedName)) {
        return animationState;
      }
    }

    return AUTO;
  }

  public static EasyModelAnimationState byApiState(int animationState) {
    for (EasyModelAnimationState state : VALUES) {
      if (state.apiState == animationState) {
        return state;
      }
    }

    return AUTO;
  }

  public String getSerializedName() {
    return this.serializedName;
  }

  public int getApiState() {
    return this.apiState;
  }
}
