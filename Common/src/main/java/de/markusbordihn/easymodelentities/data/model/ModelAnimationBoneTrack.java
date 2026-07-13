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

import java.util.List;
import java.util.Objects;
import net.minecraft.util.Mth;

public record ModelAnimationBoneTrack(
    List<ModelAnimationKeyframe> rotationKeyframes,
    List<ModelAnimationKeyframe> positionKeyframes) {

  public ModelAnimationBoneTrack {
    rotationKeyframes = List.copyOf(Objects.requireNonNull(rotationKeyframes, "rotationKeyframes"));
    positionKeyframes = List.copyOf(Objects.requireNonNull(positionKeyframes, "positionKeyframes"));
  }

  private static float lastTime(List<ModelAnimationKeyframe> keyframes) {
    return keyframes.isEmpty() ? 0.0f : keyframes.get(keyframes.size() - 1).time();
  }

  private static Vec3f sample(List<ModelAnimationKeyframe> keyframes, float time) {
    if (keyframes.isEmpty()) {
      return Vec3f.ZERO;
    }
    ModelAnimationKeyframe first = keyframes.get(0);
    if (time <= first.time()) {
      return first.value();
    }
    ModelAnimationKeyframe last = keyframes.get(keyframes.size() - 1);
    if (time >= last.time()) {
      return last.value();
    }
    int low = 1;
    int high = keyframes.size() - 1;
    while (low < high) {
      int middle = (low + high) >>> 1;
      if (time <= keyframes.get(middle).time()) {
        high = middle;
      } else {
        low = middle + 1;
      }
    }
    ModelAnimationKeyframe keyframe = keyframes.get(low);
    ModelAnimationKeyframe previous = keyframes.get(low - 1);
    if (keyframe.step()) {
      return time < keyframe.time() ? previous.value() : keyframe.value();
    }
    float span = keyframe.time() - previous.time();
    if (span <= 0.0f) {
      return keyframe.value();
    }
    return lerp(previous.value(), keyframe.value(), (time - previous.time()) / span);
  }

  private static Vec3f lerp(Vec3f from, Vec3f to, float progress) {
    return new Vec3f(
        Mth.lerp(progress, from.x(), to.x()),
        Mth.lerp(progress, from.y(), to.y()),
        Mth.lerp(progress, from.z(), to.z()));
  }

  public boolean isEmpty() {
    return rotationKeyframes.isEmpty() && positionKeyframes.isEmpty();
  }

  public float lastKeyframeTime() {
    return Math.max(lastTime(rotationKeyframes), lastTime(positionKeyframes));
  }

  public Vec3f rotationAt(float time) {
    return sample(rotationKeyframes, time);
  }

  public Vec3f positionAt(float time) {
    return sample(positionKeyframes, time);
  }
}
