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
  AUTO,
  IDLE,
  WALK,
  RUN,
  HURT,
  DEATH;

  public static EasyModelAnimationState bySerializedName(String serializedName) {
    if (serializedName == null) {
      return AUTO;
    }

    String normalizedName = serializedName.toLowerCase(Locale.ROOT);
    for (EasyModelAnimationState animationState : values()) {
      if (animationState.getSerializedName().equals(normalizedName)) {
        return animationState;
      }
    }

    return AUTO;
  }

  public static EasyModelAnimationState byApiState(int animationState) {
    return switch (animationState) {
      case EasyModelAnimationStates.IDLE -> IDLE;
      case EasyModelAnimationStates.WALK -> WALK;
      case EasyModelAnimationStates.RUN -> RUN;
      case EasyModelAnimationStates.HURT -> HURT;
      case EasyModelAnimationStates.DEATH -> DEATH;
      default -> AUTO;
    };
  }

  public String getSerializedName() {
    return this.name().toLowerCase(Locale.ROOT);
  }

  public int getApiState() {
    return switch (this) {
      case IDLE -> EasyModelAnimationStates.IDLE;
      case WALK -> EasyModelAnimationStates.WALK;
      case RUN -> EasyModelAnimationStates.RUN;
      case HURT -> EasyModelAnimationStates.HURT;
      case DEATH -> EasyModelAnimationStates.DEATH;
      case AUTO -> EasyModelAnimationStates.AUTO;
    };
  }
}
