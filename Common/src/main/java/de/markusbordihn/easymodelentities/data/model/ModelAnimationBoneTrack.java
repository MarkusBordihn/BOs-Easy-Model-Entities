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
    requireSorted(rotationKeyframes, "rotationKeyframes");
    requireSorted(positionKeyframes, "positionKeyframes");
  }

  private static void requireSorted(List<ModelAnimationKeyframe> keyframes, String name) {
    for (int index = 1; index < keyframes.size(); index++) {
      if (keyframes.get(index).time() < keyframes.get(index - 1).time()) {
        throw new IllegalArgumentException(name + " must be sorted by time.");
      }
    }
  }

  private static float lastTime(List<ModelAnimationKeyframe> keyframes) {
    return keyframes.isEmpty() ? 0.0f : keyframes.get(keyframes.size() - 1).time();
  }

  private static Vec3f sample(List<ModelAnimationKeyframe> keyframes, float time) {
    int keyframeIndex = keyframeIndex(keyframes, time);
    if (keyframeIndex < 0) {
      return Vec3f.ZERO;
    }
    ModelAnimationKeyframe keyframe = keyframes.get(keyframeIndex);
    if (keyframeIndex == 0 || time >= keyframe.time()) {
      return keyframe.value();
    }

    return new Vec3f(
        componentAt(keyframes, keyframeIndex, time, 0),
        componentAt(keyframes, keyframeIndex, time, 1),
        componentAt(keyframes, keyframeIndex, time, 2));
  }

  private static int keyframeIndex(List<ModelAnimationKeyframe> keyframes, float time) {
    if (keyframes.isEmpty()) {
      return -1;
    }
    if (time <= keyframes.get(0).time()) {
      return 0;
    }
    int lastIndex = keyframes.size() - 1;
    if (time >= keyframes.get(lastIndex).time()) {
      return lastIndex;
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
    return low;
  }

  private static float componentAt(
      List<ModelAnimationKeyframe> keyframes, int keyframeIndex, float time, int component) {
    ModelAnimationKeyframe keyframe = keyframes.get(keyframeIndex);
    if (keyframeIndex == 0 || time >= keyframe.time()) {
      return component(keyframe.value(), component);
    }
    ModelAnimationKeyframe previous = keyframes.get(keyframeIndex - 1);
    if (keyframe.step()) {
      return component(previous.value(), component);
    }
    float span = keyframe.time() - previous.time();
    if (span <= 0.0f) {
      return component(keyframe.value(), component);
    }
    return Mth.lerp(
        (time - previous.time()) / span,
        component(previous.value(), component),
        component(keyframe.value(), component));
  }

  private static float component(Vec3f vector, int component) {
    return switch (component) {
      case 0 -> vector.x();
      case 1 -> vector.y();
      default -> vector.z();
    };
  }

  private static void sample(
      List<ModelAnimationKeyframe> keyframes, float time, float[] destination, int offset) {
    int keyframeIndex = keyframeIndex(keyframes, time);
    if (keyframeIndex < 0) {
      destination[offset] = 0.0f;
      destination[offset + 1] = 0.0f;
      destination[offset + 2] = 0.0f;
      return;
    }
    destination[offset] = componentAt(keyframes, keyframeIndex, time, 0);
    destination[offset + 1] = componentAt(keyframes, keyframeIndex, time, 1);
    destination[offset + 2] = componentAt(keyframes, keyframeIndex, time, 2);
  }

  private static void requireFiniteTime(float time) {
    if (!Float.isFinite(time)) {
      throw new IllegalArgumentException("time must be finite.");
    }
  }

  public boolean isEmpty() {
    return rotationKeyframes.isEmpty() && positionKeyframes.isEmpty();
  }

  public float lastKeyframeTime() {
    return Math.max(lastTime(rotationKeyframes), lastTime(positionKeyframes));
  }

  public Vec3f rotationAt(float time) {
    requireFiniteTime(time);
    return sample(rotationKeyframes, time);
  }

  public Vec3f positionAt(float time) {
    requireFiniteTime(time);
    return sample(positionKeyframes, time);
  }

  public void sample(float time, float[] destination, int offset) {
    requireFiniteTime(time);
    Objects.requireNonNull(destination, "destination");
    if (offset < 0 || destination.length - offset < 6) {
      throw new IllegalArgumentException("Destination must have space for six values.");
    }

    sample(this.rotationKeyframes, time, destination, offset);
    sample(this.positionKeyframes, time, destination, offset + 3);
  }
}
