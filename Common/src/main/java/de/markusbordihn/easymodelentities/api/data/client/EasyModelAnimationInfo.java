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

import java.util.Objects;

public record EasyModelAnimationInfo(
    String name,
    float durationSeconds,
    float durationTicks,
    boolean loop,
    float framesPerSecond,
    int frameCount,
    int keyframeCount,
    int animatedBoneCount) {

  public EasyModelAnimationInfo {
    name = Objects.requireNonNull(name, "name").trim();
    if (name.isEmpty()) {
      throw new IllegalArgumentException("name must not be blank.");
    }
    requireNonNegativeFinite(durationSeconds, "durationSeconds");
    requireNonNegativeFinite(durationTicks, "durationTicks");
    requireNonNegativeFinite(framesPerSecond, "framesPerSecond");
    requireNonNegative(frameCount, "frameCount");
    requireNonNegative(keyframeCount, "keyframeCount");
    requireNonNegative(animatedBoneCount, "animatedBoneCount");
  }

  private static void requireNonNegativeFinite(float value, String name) {
    if (!Float.isFinite(value) || value < 0.0f) {
      throw new IllegalArgumentException(name + " must be a finite non-negative value.");
    }
  }

  private static void requireNonNegative(int value, String name) {
    if (value < 0) {
      throw new IllegalArgumentException(name + " must not be negative.");
    }
  }

  public EasyModelAnimationType type() {
    return EasyModelAnimationType.fromName(this.name);
  }
}
