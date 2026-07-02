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
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ModelAnimationClipTest {

  private static final float DELTA = 1e-4f;

  private static ModelAnimationBoneTrack rotationTrack(ModelAnimationKeyframe... keyframes) {
    return new ModelAnimationBoneTrack(List.of(keyframes), List.of());
  }

  @Test
  void interpolatesLinearlyBetweenKeyframes() {
    ModelAnimationBoneTrack track =
        rotationTrack(
            new ModelAnimationKeyframe(0.0f, new Vec3f(0.0f, 0.0f, 0.0f), false),
            new ModelAnimationKeyframe(1.0f, new Vec3f(2.0f, -4.0f, 6.0f), false));

    Vec3f value = track.rotationAt(0.5f);
    assertEquals(1.0f, value.x(), DELTA);
    assertEquals(-2.0f, value.y(), DELTA);
    assertEquals(3.0f, value.z(), DELTA);
  }

  @Test
  void holdsFirstAndLastKeyframeValuesOutsideRange() {
    ModelAnimationBoneTrack track =
        rotationTrack(
            new ModelAnimationKeyframe(0.5f, new Vec3f(1.0f, 0.0f, 0.0f), false),
            new ModelAnimationKeyframe(1.0f, new Vec3f(3.0f, 0.0f, 0.0f), false));

    assertEquals(1.0f, track.rotationAt(0.0f).x(), DELTA);
    assertEquals(3.0f, track.rotationAt(2.0f).x(), DELTA);
  }

  @Test
  void stepKeyframeHoldsPreviousValueUntilItsTime() {
    ModelAnimationBoneTrack track =
        rotationTrack(
            new ModelAnimationKeyframe(0.0f, new Vec3f(1.0f, 0.0f, 0.0f), false),
            new ModelAnimationKeyframe(1.0f, new Vec3f(5.0f, 0.0f, 0.0f), true));

    assertEquals(1.0f, track.rotationAt(0.9f).x(), DELTA);
    assertEquals(5.0f, track.rotationAt(1.0f).x(), DELTA);
  }

  @Test
  void positionDefaultsToZeroWithoutKeyframes() {
    ModelAnimationBoneTrack track =
        rotationTrack(new ModelAnimationKeyframe(0.0f, new Vec3f(1.0f, 0.0f, 0.0f), false));

    assertEquals(Vec3f.ZERO, track.positionAt(0.5f));
  }

  @Test
  void loopingClipWrapsTime() {
    ModelAnimationClip clip = new ModelAnimationClip("idle", 2.0f, true, Map.of());

    assertEquals(0.5f, clip.clipTime(10.0f), DELTA);
    assertEquals(0.0f, clip.clipTime(40.0f), DELTA);
  }

  @Test
  void nonLoopingClipClampsToLength() {
    ModelAnimationClip clip = new ModelAnimationClip("idle", 2.0f, false, Map.of());

    assertEquals(2.0f, clip.clipTime(100.0f), DELTA);
    assertEquals(1.0f, clip.clipTime(20.0f), DELTA);
  }

  @Test
  void trackLookupUsesNormalizedPartNames() {
    ModelAnimationBoneTrack track =
        rotationTrack(new ModelAnimationKeyframe(0.0f, new Vec3f(1.0f, 0.0f, 0.0f), false));
    ModelAnimationClip clip = new ModelAnimationClip("idle", 1.0f, true, Map.of("body", track));

    assertEquals(track, clip.track("body"));
    assertNull(clip.track("head"));
    assertNull(clip.track(null));
  }
}
