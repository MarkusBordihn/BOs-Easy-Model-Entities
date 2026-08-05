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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.markusbordihn.easymodelentities.api.data.EasyModelAnimation;
import de.markusbordihn.easymodelentities.api.data.client.EasyModelAnimationPlayback;
import de.markusbordihn.easymodelentities.api.data.client.EasyModelAnimationPlaybackMode;
import de.markusbordihn.easymodelentities.api.data.client.EasyModelAnimationTransition;
import de.markusbordihn.easymodelentities.api.data.client.EasyModelPartTransform;
import de.markusbordihn.easymodelentities.data.model.ModelAnimationClip;
import java.util.Map;
import org.junit.jupiter.api.Test;

class EasyModelAnimationPlaybackTrackerTest {

  private static final EasyModelAnimation IDLE = EasyModelAnimation.named("idle");
  private static final EasyModelAnimation WAVE = EasyModelAnimation.named("wave");
  private static final Map<String, ModelAnimationClip> CLIPS =
      Map.of(
          "idle", new ModelAnimationClip("idle", 2.0f, true, Map.of()),
          "wave", new ModelAnimationClip("wave", 1.0f, false, Map.of()));

  @Test
  void immediateTransitionCanCrossFadeFromCurrentAnimation() {
    Object target = new Object();
    EasyModelAnimationPlaybackTracker<Object> tracker = new EasyModelAnimationPlaybackTracker<>();
    tracker.resolve(target, IDLE, 0.0, CLIPS);

    tracker.play(target, WAVE, EasyModelAnimationTransition.immediate(10.0f));
    EasyModelAnimationPlaybackFrame start = tracker.resolve(target, IDLE, 5.0, CLIPS);
    EasyModelAnimationPlaybackFrame middle = tracker.resolve(target, IDLE, 10.0, CLIPS);
    EasyModelAnimationPlaybackFrame end = tracker.resolve(target, IDLE, 15.0, CLIPS);

    assertEquals(WAVE, start.animation());
    assertEquals(IDLE, start.previousAnimation());
    assertEquals(0.0f, start.blendProgress(), 0.0001f);
    assertEquals(0.5f, middle.blendProgress(), 0.0001f);
    assertNull(end.previousAnimation());
  }

  @Test
  void afterCurrentWaitsForNonLoopingClipToFinish() {
    Object target = new Object();
    EasyModelAnimationPlaybackTracker<Object> tracker = new EasyModelAnimationPlaybackTracker<>();
    tracker.resolve(target, WAVE, 0.0, CLIPS);

    tracker.play(target, IDLE, EasyModelAnimationTransition.AFTER_CURRENT);

    assertEquals(WAVE, tracker.resolve(target, WAVE, 19.0, CLIPS).animation());
    EasyModelAnimationPlaybackFrame switched = tracker.resolve(target, WAVE, 20.0, CLIPS);
    assertEquals(IDLE, switched.animation());
    assertEquals(0.0f, switched.animationTicks(), 0.0001f);
  }

  @Test
  void afterCurrentOnLoopingClipUsesNextLoopBoundaryAndNewestRequest() {
    Object target = new Object();
    EasyModelAnimationPlaybackTracker<Object> tracker = new EasyModelAnimationPlaybackTracker<>();
    tracker.resolve(target, IDLE, 0.0, CLIPS);
    tracker.resolve(target, IDLE, 15.0, CLIPS);

    tracker.play(target, WAVE, EasyModelAnimationTransition.AFTER_CURRENT);
    tracker.play(target, EasyModelAnimation.AUTO, EasyModelAnimationTransition.AFTER_CURRENT);

    assertEquals(IDLE, tracker.resolve(target, IDLE, 39.0, CLIPS).animation());
    assertEquals(IDLE, tracker.resolve(target, IDLE, 40.0, CLIPS).animation());
  }

  @Test
  void restartResetsPlaybackTime() {
    Object target = new Object();
    EasyModelAnimationPlaybackTracker<Object> tracker = new EasyModelAnimationPlaybackTracker<>();
    tracker.resolve(target, WAVE, 0.0, CLIPS);
    tracker.resolve(target, WAVE, 12.0, CLIPS);

    tracker.restart(target);

    assertEquals(0.0f, tracker.resolve(target, WAVE, 12.0, CLIPS).animationTicks(), 0.0001f);
  }

  @Test
  void stopReturnsControlToTheFallbackAnimation() {
    Object target = new Object();
    EasyModelAnimationPlaybackTracker<Object> tracker = new EasyModelAnimationPlaybackTracker<>();
    tracker.resolve(target, IDLE, 0.0, CLIPS);
    tracker.play(target, WAVE, EasyModelAnimationTransition.IMMEDIATE);
    tracker.resolve(target, IDLE, 5.0, CLIPS);

    tracker.stop(target, EasyModelAnimationTransition.IMMEDIATE);

    assertEquals(IDLE, tracker.resolve(target, IDLE, 10.0, CLIPS).animation());
    assertEquals(IDLE, tracker.resolve(target, IDLE, 11.0, CLIPS).animation());
  }

  @Test
  void stopTransitionCrossFadesToFallbackForItsFullDuration() {
    Object target = new Object();
    EasyModelAnimationPlaybackTracker<Object> tracker = new EasyModelAnimationPlaybackTracker<>();
    tracker.resolve(target, IDLE, 0.0, CLIPS);
    tracker.play(target, WAVE, EasyModelAnimationTransition.IMMEDIATE);
    tracker.resolve(target, IDLE, 5.0, CLIPS);

    tracker.stop(target, EasyModelAnimationTransition.immediate(10.0f));
    EasyModelAnimationPlaybackFrame start = tracker.resolve(target, IDLE, 5.0, CLIPS);
    EasyModelAnimationPlaybackFrame middle = tracker.resolve(target, IDLE, 10.0, CLIPS);
    EasyModelAnimationPlaybackFrame end = tracker.resolve(target, IDLE, 15.0, CLIPS);

    assertEquals(IDLE, start.animation());
    assertEquals(WAVE, start.previousAnimation());
    assertEquals(0.0f, start.blendProgress(), 0.0001f);
    assertEquals(WAVE, middle.previousAnimation());
    assertEquals(0.5f, middle.blendProgress(), 0.0001f);
    assertNull(end.previousAnimation());
    assertFalse(tracker.hasPlayback(target));
  }

  @Test
  void afterCurrentUsesResolvedAutomaticClipBoundary() {
    Object target = new Object();
    EasyModelAnimationPlaybackTracker<Object> tracker = new EasyModelAnimationPlaybackTracker<>();
    tracker.resolve(target, EasyModelAnimation.AUTO, "idle", 0.0, CLIPS);
    tracker.resolve(target, EasyModelAnimation.AUTO, "idle", 15.0, CLIPS);

    tracker.play(target, WAVE, EasyModelAnimationTransition.AFTER_CURRENT);

    assertEquals(
        EasyModelAnimation.AUTO,
        tracker.resolve(target, EasyModelAnimation.AUTO, "idle", 39.0, CLIPS).animation());
    assertEquals(
        WAVE, tracker.resolve(target, EasyModelAnimation.AUTO, "idle", 40.0, CLIPS).animation());
  }

  @Test
  void crossFadeUsesShortestRotationPath() {
    EasyModelPartTransform from =
        new EasyModelPartTransform((float) Math.toRadians(170.0), 0.0f, 0.0f);
    EasyModelPartTransform to =
        new EasyModelPartTransform((float) Math.toRadians(-170.0), 0.0f, 0.0f);

    EasyModelPartTransform middle = EasyModelBakedModelRenderer.interpolate(from, to, 0.5f);

    assertEquals((float) Math.PI, middle.xRotation(), 0.0001f);
  }

  @Test
  void rejectsInvalidBlendDurations() {
    assertThrows(
        IllegalArgumentException.class, () -> EasyModelAnimationTransition.immediate(-1.0f));
    assertThrows(
        IllegalArgumentException.class, () -> EasyModelAnimationTransition.afterCurrent(Float.NaN));
  }

  @Test
  void onceAutoRevertsToFallbackWithBlend() {
    Object target = new Object();
    EasyModelAnimationPlaybackTracker<Object> tracker = new EasyModelAnimationPlaybackTracker<>();
    tracker.resolve(target, IDLE, 0.0, CLIPS);
    tracker.play(
        target, WAVE, EasyModelAnimationPlayback.DEFAULT, EasyModelAnimationTransition.DEFAULT);

    EasyModelAnimationPlaybackFrame started = tracker.resolve(target, IDLE, 0.0, CLIPS);
    EasyModelAnimationPlaybackFrame reverted = tracker.resolve(target, IDLE, 20.0, CLIPS);
    EasyModelAnimationPlaybackFrame finished = tracker.resolve(target, IDLE, 25.0, CLIPS);

    assertEquals(WAVE, started.animation());
    assertEquals(Boolean.FALSE, started.loopOverride());
    assertEquals(IDLE, reverted.animation());
    assertEquals(WAVE, reverted.previousAnimation());
    assertEquals(Boolean.FALSE, reverted.previousLoopOverride());
    assertNull(finished.previousAnimation());
    assertFalse(tracker.hasPlayback(target));
  }

  @Test
  void loopPlaysUntilStopped() {
    Object target = new Object();
    EasyModelAnimationPlaybackTracker<Object> tracker = new EasyModelAnimationPlaybackTracker<>();
    tracker.resolve(target, IDLE, 0.0, CLIPS);
    tracker.play(
        target,
        WAVE,
        new EasyModelAnimationPlayback(EasyModelAnimationPlaybackMode.LOOP, 0, 0.0f),
        EasyModelAnimationTransition.IMMEDIATE);

    EasyModelAnimationPlaybackFrame frame = tracker.resolve(target, IDLE, 80.0, CLIPS);

    assertEquals(WAVE, frame.animation());
    assertEquals(Boolean.TRUE, frame.loopOverride());
    assertTrue(tracker.hasPlayback(target));
  }

  @Test
  void repeatPlaysRequestedCountThenReverts() {
    Object target = new Object();
    EasyModelAnimationPlaybackTracker<Object> tracker = new EasyModelAnimationPlaybackTracker<>();
    tracker.resolve(target, IDLE, 0.0, CLIPS);
    tracker.play(
        target,
        WAVE,
        new EasyModelAnimationPlayback(EasyModelAnimationPlaybackMode.REPEAT, 3, 0.0f),
        EasyModelAnimationTransition.IMMEDIATE);
    tracker.resolve(target, IDLE, 0.0, CLIPS);

    assertEquals(0.0f, tracker.resolve(target, IDLE, 20.0, CLIPS).animationTicks(), 0.0001f);
    assertEquals(0.0f, tracker.resolve(target, IDLE, 40.0, CLIPS).animationTicks(), 0.0001f);
    assertEquals(IDLE, tracker.resolve(target, IDLE, 60.0, CLIPS).animation());
  }

  @Test
  void durationEndsLoopBeforeItsNextBoundary() {
    Object target = new Object();
    EasyModelAnimationPlaybackTracker<Object> tracker = new EasyModelAnimationPlaybackTracker<>();
    tracker.resolve(target, IDLE, 0.0, CLIPS);
    tracker.play(
        target,
        WAVE,
        new EasyModelAnimationPlayback(EasyModelAnimationPlaybackMode.LOOP, 0, 15.0f),
        EasyModelAnimationTransition.IMMEDIATE);
    tracker.resolve(target, IDLE, 0.0, CLIPS);

    assertEquals(WAVE, tracker.resolve(target, IDLE, 14.0, CLIPS).animation());
    assertEquals(IDLE, tracker.resolve(target, IDLE, 15.0, CLIPS).animation());
  }

  @Test
  void afterCurrentInterruptsRemainingRepeatsAtNextBoundary() {
    Object target = new Object();
    EasyModelAnimationPlaybackTracker<Object> tracker = new EasyModelAnimationPlaybackTracker<>();
    tracker.resolve(target, IDLE, 0.0, CLIPS);
    tracker.play(
        target,
        WAVE,
        new EasyModelAnimationPlayback(EasyModelAnimationPlaybackMode.REPEAT, 3, 0.0f),
        EasyModelAnimationTransition.IMMEDIATE);
    tracker.resolve(target, IDLE, 0.0, CLIPS);
    tracker.play(target, IDLE, EasyModelAnimationTransition.AFTER_CURRENT);

    assertEquals(WAVE, tracker.resolve(target, IDLE, 19.0, CLIPS).animation());
    assertEquals(IDLE, tracker.resolve(target, IDLE, 20.0, CLIPS).animation());
  }

  @Test
  void missingClipReleasesPlayback() {
    Object target = new Object();
    EasyModelAnimation missing = EasyModelAnimation.named("missing");
    EasyModelAnimationPlaybackTracker<Object> tracker = new EasyModelAnimationPlaybackTracker<>();
    tracker.resolve(target, IDLE, 0.0, CLIPS);
    tracker.play(target, missing, EasyModelAnimationTransition.IMMEDIATE);

    assertEquals(IDLE, tracker.resolve(target, IDLE, 0.0, CLIPS).animation());
    assertFalse(tracker.hasPlayback(target));
  }

  @Test
  void standardFallbackAnimationUsesItsBuiltInDuration() {
    Object target = new Object();
    EasyModelAnimationPlaybackTracker<Object> tracker = new EasyModelAnimationPlaybackTracker<>();
    tracker.resolve(target, IDLE, 0.0, Map.of());
    tracker.play(target, EasyModelAnimation.DEATH, EasyModelAnimationTransition.IMMEDIATE);

    assertEquals(
        EasyModelAnimation.DEATH, tracker.resolve(target, IDLE, 0.0, Map.of()).animation());
    assertEquals(
        EasyModelAnimation.DEATH, tracker.resolve(target, IDLE, 19.0, Map.of()).animation());
    assertEquals(IDLE, tracker.resolve(target, IDLE, 20.0, Map.of()).animation());
  }

  @Test
  void durationCompletionStartsQueuedAnimationEarly() {
    Object target = new Object();
    EasyModelAnimationPlaybackTracker<Object> tracker = new EasyModelAnimationPlaybackTracker<>();
    tracker.resolve(target, WAVE, 0.0, CLIPS);
    tracker.play(
        target,
        IDLE,
        new EasyModelAnimationPlayback(EasyModelAnimationPlaybackMode.LOOP, 0, 10.0f),
        EasyModelAnimationTransition.IMMEDIATE);
    tracker.resolve(target, WAVE, 0.0, CLIPS);
    tracker.play(target, WAVE, EasyModelAnimationTransition.AFTER_CURRENT);

    assertEquals(IDLE, tracker.resolve(target, WAVE, 9.0, CLIPS).animation());
    assertEquals(WAVE, tracker.resolve(target, WAVE, 10.0, CLIPS).animation());
  }

  @Test
  void restartRestoresRepeatCountAndDurationStart() {
    Object target = new Object();
    EasyModelAnimationPlaybackTracker<Object> tracker = new EasyModelAnimationPlaybackTracker<>();
    tracker.resolve(target, IDLE, 0.0, CLIPS);
    tracker.play(
        target,
        WAVE,
        new EasyModelAnimationPlayback(EasyModelAnimationPlaybackMode.REPEAT, 2, 0.0f),
        EasyModelAnimationTransition.IMMEDIATE);
    tracker.resolve(target, IDLE, 0.0, CLIPS);
    tracker.resolve(target, IDLE, 20.0, CLIPS);

    tracker.restart(target);

    assertEquals(0.0f, tracker.resolve(target, IDLE, 20.0, CLIPS).animationTicks(), 0.0001f);
    assertEquals(WAVE, tracker.resolve(target, IDLE, 40.0, CLIPS).animation());
    assertEquals(IDLE, tracker.resolve(target, IDLE, 60.0, CLIPS).animation());
  }

  @Test
  void stopWithoutPlaybackDoesNotCreateState() {
    Object target = new Object();
    EasyModelAnimationPlaybackTracker<Object> tracker = new EasyModelAnimationPlaybackTracker<>();

    tracker.stop(target, EasyModelAnimationTransition.DEFAULT);

    assertFalse(tracker.hasPlayback(target));
  }

  @Test
  void afterCurrentUsesForcedLoopBoundaryForAuthoredOneShot() {
    Object target = new Object();
    EasyModelAnimationPlaybackTracker<Object> tracker = new EasyModelAnimationPlaybackTracker<>();
    tracker.resolve(target, IDLE, 0.0, CLIPS);
    tracker.play(
        target,
        WAVE,
        new EasyModelAnimationPlayback(EasyModelAnimationPlaybackMode.LOOP, 0, 0.0f),
        EasyModelAnimationTransition.IMMEDIATE);
    tracker.resolve(target, IDLE, 0.0, CLIPS);
    tracker.resolve(target, IDLE, 25.0, CLIPS);

    tracker.play(target, IDLE, EasyModelAnimationTransition.AFTER_CURRENT);

    assertEquals(WAVE, tracker.resolve(target, IDLE, 39.0, CLIPS).animation());
    assertEquals(IDLE, tracker.resolve(target, IDLE, 40.0, CLIPS).animation());
  }

  @Test
  void loopingPlaybackKeepsAnimationTicksWithinFloatPrecision() {
    Object target = new Object();
    EasyModelAnimationPlaybackTracker<Object> tracker = new EasyModelAnimationPlaybackTracker<>();
    tracker.resolve(target, IDLE, 0.0, CLIPS);
    tracker.play(
        target,
        IDLE,
        EasyModelAnimationPlayback.DEFAULT.withMode(EasyModelAnimationPlaybackMode.LOOP),
        EasyModelAnimationTransition.IMMEDIATE);
    tracker.resolve(target, IDLE, 1.0, CLIPS);

    EasyModelAnimationPlaybackFrame frame = tracker.resolve(target, IDLE, 5_000_000.0, CLIPS);

    assertTrue(frame.animationTicks() < 40.0f);
    assertEquals(IDLE, frame.animation());
  }

  @Test
  void queuedRequestDoesNotClaimTheOverrideBeforeItsBoundary() {
    Object target = new Object();
    EasyModelAnimationPlaybackTracker<Object> tracker = new EasyModelAnimationPlaybackTracker<>();
    tracker.resolve(target, IDLE, 0.0, CLIPS);
    tracker.play(target, WAVE, EasyModelAnimationTransition.AFTER_CURRENT);

    EasyModelAnimationPlaybackFrame frame = tracker.resolve(target, IDLE, 1.0, CLIPS);

    assertEquals(IDLE, frame.animation());
    assertFalse(frame.playbackDriven());
    assertTrue(tracker.hasPlayback(target));
  }

  @Test
  void playbackDrivenMarksOverriddenFramesOnly() {
    Object target = new Object();
    EasyModelAnimationPlaybackTracker<Object> tracker = new EasyModelAnimationPlaybackTracker<>();
    assertFalse(tracker.resolve(target, IDLE, 0.0, CLIPS).playbackDriven());

    tracker.play(target, WAVE, EasyModelAnimationTransition.IMMEDIATE);

    assertTrue(tracker.resolve(target, IDLE, 1.0, CLIPS).playbackDriven());
  }
}
