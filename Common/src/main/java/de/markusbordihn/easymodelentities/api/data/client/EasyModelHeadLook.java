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

import net.minecraft.util.Mth;

public record EasyModelHeadLook(float yaw, float pitch) {

  public static final EasyModelHeadLook NONE = new EasyModelHeadLook(0.0f, 0.0f);

  private static final float MAX_YAW = 75.0f;
  private static final float MAX_PITCH = 60.0f;

  public static EasyModelHeadLook of(float yaw, float pitch) {
    if (!Float.isFinite(yaw) || !Float.isFinite(pitch)) {
      return NONE;
    }
    return new EasyModelHeadLook(
        Mth.clamp(Mth.wrapDegrees(yaw), -MAX_YAW, MAX_YAW),
        Mth.clamp(pitch, -MAX_PITCH, MAX_PITCH));
  }

  public boolean isNeutral() {
    return this.yaw == 0.0f && this.pitch == 0.0f;
  }

  public EasyModelPartTransform toTransform() {
    return new EasyModelPartTransform(this.pitch * Mth.DEG_TO_RAD, this.yaw * Mth.DEG_TO_RAD, 0.0f);
  }
}
