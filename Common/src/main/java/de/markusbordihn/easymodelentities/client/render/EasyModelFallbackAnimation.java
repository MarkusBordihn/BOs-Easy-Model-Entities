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

package de.markusbordihn.easymodelentities.client.render;

import de.markusbordihn.easymodelentities.api.data.EasyModelAnimation;

final class EasyModelFallbackAnimation {

  private static final float MOVEMENT_LENGTH = 1.0f;
  private static final float ATTACK_LENGTH = 0.6f;
  private static final float HURT_LENGTH = 0.5f;
  private static final float DEATH_LENGTH = 1.0f;

  private EasyModelFallbackAnimation() {}

  static float length(EasyModelAnimation animation) {
    if (animation == EasyModelAnimation.ATTACK) {
      return ATTACK_LENGTH;
    }
    if (animation == EasyModelAnimation.HURT) {
      return HURT_LENGTH;
    }
    if (animation == EasyModelAnimation.DEATH) {
      return DEATH_LENGTH;
    }
    if (animation == EasyModelAnimation.IDLE
        || animation == EasyModelAnimation.WALK
        || animation == EasyModelAnimation.RUN
        || animation == EasyModelAnimation.SWIM
        || animation == EasyModelAnimation.FLY
        || animation == EasyModelAnimation.SIT) {
      return MOVEMENT_LENGTH;
    }

    return 0.0f;
  }

  static boolean loops(EasyModelAnimation animation) {
    return animation == EasyModelAnimation.IDLE
        || animation == EasyModelAnimation.WALK
        || animation == EasyModelAnimation.RUN
        || animation == EasyModelAnimation.SWIM
        || animation == EasyModelAnimation.FLY
        || animation == EasyModelAnimation.SIT;
  }
}
