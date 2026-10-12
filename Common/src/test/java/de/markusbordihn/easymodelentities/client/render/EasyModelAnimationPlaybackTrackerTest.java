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
import de.markusbordihn.easymodelentities.api.data.client.EasyModelAnimationSequence;
import de.markusbordihn.easymodelentities.api.data.client.EasyModelAnimationStep;
import de.markusbordihn.easymodelentities.api.data.client.EasyModelAnimationTransition;
import de.markusbordihn.easymodelentities.api.data.client.EasyModelPartTransform;
import de.markusbordihn.easymodelentities.data.model.ModelAnimationClip;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
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
  @DisplayName("Double speed plays a clip in half the time and advances clip time twice as fast")
  void doubleSpeedHalvesTheClipLength() {
    Object target = new Object();
    EasyModelAnimationPlaybackTracker<Object> tracker = new EasyModelAnimationPlaybackTracker<>();
    tracker.resolve(target, IDLE, 0.0, CLIPS);
    tracker.play(
        target,
        WAVE,
        EasyModelAnimationPlayback.DEFAULT.withSpeed(2.0f),
        EasyModelAnimationTransition.IMMEDIATE);
    tracker.resolve(target, IDLE, 0.0, CLIPS);

    EasyModelAnimationPlaybackFrame middle = tracker.resolve(target, IDLE, 5.0, CLIPS);

    assertEquals(WAVE, middle.animation());
    assertEquals(10.0f, middle.animationTicks(), 0.0001f);
    assertEquals(WAVE, tracker.resolve(target, IDLE, 9.0, CLIPS).animation());
    assertEquals(IDLE, tracker.resolve(target, IDLE, 10.0, CLIPS).animation());
  }

  @Test
  @DisplayName("Half speed doubles every repeat, and the fallback plays at normal speed again")
  void halfSpeedDoublesEachRepeat() {
    Object target = new Object();
    EasyModelAnimationPlaybackTracker<Object> tracker = new EasyModelAnimationPlaybackTracker<>();
    tracker.resolve(target, IDLE, 0.0, CLIPS);
    tracker.play(
        target,
        WAVE,
        new EasyModelAnimationPlayback(EasyModelAnimationPlaybackMode.REPEAT, 2, 0.0f, 0.5f),
        EasyModelAnimationTransition.IMMEDIATE);
    tracker.resolve(target, IDLE, 0.0, CLIPS);

    assertEquals(19.0f, tracker.resolve(target, IDLE, 38.0, CLIPS).animationTicks(), 0.0001f);
    assertEquals(0.0f, tracker.resolve(target, IDLE, 40.0, CLIPS).animationTicks(), 0.0001f);
    assertEquals(WAVE, tracker.resolve(target, IDLE, 79.0, CLIPS).animation());
    assertEquals(IDLE, tracker.resolve(target, IDLE, 80.0, CLIPS).animation());
    assertEquals(4.0f, tracker.resolve(target, IDLE, 84.0, CLIPS).animationTicks(), 0.0001f);
  }

  @Test
  @DisplayName("Duration limits count real ticks, not scaled clip time")
  void durationIgnoresSpeed() {
    Object target = new Object();
    EasyModelAnimationPlaybackTracker<Object> tracker = new EasyModelAnimationPlaybackTracker<>();
    tracker.resolve(target, IDLE, 0.0, CLIPS);
    tracker.play(
        target,
        WAVE,
        new EasyModelAnimationPlayback(EasyModelAnimationPlaybackMode.LOOP, 0, 15.0f, 4.0f),
        EasyModelAnimationTransition.IMMEDIATE);
    tracker.resolve(target, IDLE, 0.0, CLIPS);

    assertEquals(WAVE, tracker.resolve(target, IDLE, 14.0, CLIPS).animation());
    assertEquals(IDLE, tracker.resolve(target, IDLE, 15.0, CLIPS).animation());
  }

  @Test
  @DisplayName("A cross-fade keeps the previous clip running at its own speed")
  void crossFadeKeepsPreviousSpeed() {
    Object target = new Object();
    EasyModelAnimationPlaybackTracker<Object> tracker = new EasyModelAnimationPlaybackTracker<>();
    tracker.resolve(target, IDLE, 0.0, CLIPS);
    tracker.play(
        target,
        WAVE,
        new EasyModelAnimationPlayback(EasyModelAnimationPlaybackMode.LOOP, 0, 0.0f, 2.0f),
        EasyModelAnimationTransition.IMMEDIATE);
    tracker.resolve(target, IDLE, 0.0, CLIPS);

    tracker.stop(target, EasyModelAnimationTransition.immediate(10.0f));
    tracker.resolve(target, IDLE, 4.0, CLIPS);
    EasyModelAnimationPlaybackFrame blending = tracker.resolve(target, IDLE, 6.0, CLIPS);

    assertEquals(IDLE, blending.animation());
    assertEquals(2.0f, blending.animationTicks(), 0.0001f);
    assertEquals(WAVE, blending.previousAnimation());
    assertEquals(12.0f, blending.previousAnimationTicks(), 0.0001f);
  }

  @Test
  @DisplayName("Hold keeps the finished clip on its last frame instead of reverting")
  void holdKeepsTheLastFrameAfterTheClipEnds() {
    Object target = new Object();
    EasyModelAnimationPlaybackTracker<Object> tracker = new EasyModelAnimationPlaybackTracker<>();
    tracker.resolve(target, IDLE, 0.0, CLIPS);
    tracker.play(
        target,
        WAVE,
        EasyModelAnimationPlayback.DEFAULT.withMode(EasyModelAnimationPlaybackMode.HOLD),
        EasyModelAnimationTransition.IMMEDIATE);
    tracker.resolve(target, IDLE, 0.0, CLIPS);

    EasyModelAnimationPlaybackFrame held = tracker.resolve(target, IDLE, 200.0, CLIPS);

    assertEquals(WAVE, held.animation());
    assertEquals(Boolean.FALSE, held.loopOverride());
    assertTrue(held.playbackDriven());
    assertTrue(tracker.hasPlayback(target));
  }

  @Test
  @DisplayName("Stop releases a held clip back to the fallback animation")
  void stopReleasesAHeldClip() {
    Object target = new Object();
    EasyModelAnimationPlaybackTracker<Object> tracker = new EasyModelAnimationPlaybackTracker<>();
    tracker.resolve(target, IDLE, 0.0, CLIPS);
    tracker.play(
        target,
        WAVE,
        EasyModelAnimationPlayback.DEFAULT.withMode(EasyModelAnimationPlaybackMode.HOLD),
        EasyModelAnimationTransition.IMMEDIATE);
    tracker.resolve(target, IDLE, 0.0, CLIPS);
    tracker.resolve(target, IDLE, 60.0, CLIPS);

    tracker.stop(target, EasyModelAnimationTransition.IMMEDIATE);

    assertEquals(IDLE, tracker.resolve(target, IDLE, 61.0, CLIPS).animation());
    assertFalse(tracker.hasPlayback(target));
  }

  @Test
  @DisplayName("A request after the current clip ends a hold right away")
  void afterCurrentEndsAHoldImmediately() {
    Object target = new Object();
    EasyModelAnimationPlaybackTracker<Object> tracker = new EasyModelAnimationPlaybackTracker<>();
    tracker.resolve(target, IDLE, 0.0, CLIPS);
    tracker.play(
        target,
        WAVE,
        EasyModelAnimationPlayback.DEFAULT.withMode(EasyModelAnimationPlaybackMode.HOLD),
        EasyModelAnimationTransition.IMMEDIATE);
    tracker.resolve(target, IDLE, 0.0, CLIPS);
    tracker.resolve(target, IDLE, 60.0, CLIPS);

    tracker.play(target, IDLE, EasyModelAnimationTransition.AFTER_CURRENT);
    EasyModelAnimationPlaybackFrame switched = tracker.resolve(target, IDLE, 61.0, CLIPS);

    assertEquals(IDLE, switched.animation());
    assertEquals(0.0f, switched.animationTicks(), 0.0001f);
  }

  @Test
  @DisplayName("A held step inside a sequence still hands over to the next step")
  void holdInsideASequenceAdvancesToTheNextStep() {
    Object target = new Object();
    EasyModelAnimationPlaybackTracker<Object> tracker = new EasyModelAnimationPlaybackTracker<>();
    tracker.resolve(target, IDLE, 0.0, CLIPS);
    tracker.playSequence(
        target,
        new EasyModelAnimationSequence(
            List.of(
                new EasyModelAnimationStep(
                    WAVE,
                    EasyModelAnimationPlayback.DEFAULT.withMode(
                        EasyModelAnimationPlaybackMode.HOLD),
                    EasyModelAnimationTransition.IMMEDIATE),
                new EasyModelAnimationStep(
                    IDLE,
                    EasyModelAnimationPlayback.DEFAULT,
                    EasyModelAnimationTransition.AFTER_CURRENT)),
            EasyModelAnimation.AUTO));
    tracker.resolve(target, IDLE, 0.0, CLIPS);

    assertEquals(WAVE, tracker.resolve(target, IDLE, 19.0, CLIPS).animation());
    assertEquals(IDLE, tracker.resolve(target, IDLE, 20.0, CLIPS).animation());
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

  @Test
  void sequenceAdvancesEachStepAtItsOwnClipLength() {
    Object target = new Object();
    EasyModelAnimationPlaybackTracker<Object> tracker = new EasyModelAnimationPlaybackTracker<>();
    tracker.resolve(target, WAVE, 0.0, CLIPS);

    tracker.playSequence(target, EasyModelAnimationSequence.of(WAVE, IDLE));
    tracker.resolve(target, WAVE, 0.0, CLIPS);

    assertEquals(WAVE, tracker.resolve(target, WAVE, 19.0, CLIPS).animation());
    assertEquals(IDLE, tracker.resolve(target, WAVE, 20.0, CLIPS).animation());
    assertEquals(IDLE, tracker.resolve(target, WAVE, 59.0, CLIPS).animation());
    assertEquals(WAVE, tracker.resolve(target, WAVE, 60.0, CLIPS).animation());
    tracker.resolve(target, WAVE, 65.0, CLIPS);
    assertFalse(tracker.hasPlayback(target));
  }

  @Test
  void sequenceKeepsRemainingStepsAcrossEverySwitch() {
    Object target = new Object();
    EasyModelAnimationPlaybackTracker<Object> tracker = new EasyModelAnimationPlaybackTracker<>();
    tracker.resolve(target, IDLE, 0.0, CLIPS);

    tracker.playSequence(target, EasyModelAnimationSequence.of(WAVE, IDLE, WAVE));
    tracker.resolve(target, IDLE, 0.0, CLIPS);

    assertEquals(IDLE, tracker.resolve(target, IDLE, 20.0, CLIPS).animation());
    assertEquals(WAVE, tracker.resolve(target, IDLE, 60.0, CLIPS).animation());
    assertEquals(IDLE, tracker.resolve(target, IDLE, 80.0, CLIPS).animation());
    tracker.resolve(target, IDLE, 85.0, CLIPS);
    assertFalse(tracker.hasPlayback(target));
  }

  @Test
  void sequenceFallbackLoopsAfterTheLastStep() {
    Object target = new Object();
    EasyModelAnimationPlaybackTracker<Object> tracker = new EasyModelAnimationPlaybackTracker<>();
    tracker.resolve(target, WAVE, 0.0, CLIPS);

    tracker.playSequence(target, EasyModelAnimationSequence.of(WAVE).withFallback(IDLE));
    tracker.resolve(target, WAVE, 0.0, CLIPS);

    assertEquals(WAVE, tracker.resolve(target, WAVE, 19.0, CLIPS).animation());
    EasyModelAnimationPlaybackFrame fallbackFrame = tracker.resolve(target, WAVE, 20.0, CLIPS);
    assertEquals(IDLE, fallbackFrame.animation());
    assertEquals(Boolean.TRUE, fallbackFrame.loopOverride());
    assertEquals(IDLE, tracker.resolve(target, WAVE, 200.0, CLIPS).animation());
    assertTrue(tracker.hasPlayback(target));
  }

  @Test
  void plainPlayDiscardsTheRemainingSequence() {
    Object target = new Object();
    EasyModelAnimationPlaybackTracker<Object> tracker = new EasyModelAnimationPlaybackTracker<>();
    tracker.resolve(target, WAVE, 0.0, CLIPS);
    tracker.playSequence(target, EasyModelAnimationSequence.of(WAVE, IDLE));
    tracker.resolve(target, WAVE, 0.0, CLIPS);

    tracker.play(target, WAVE, EasyModelAnimationTransition.IMMEDIATE);
    tracker.resolve(target, WAVE, 5.0, CLIPS);

    assertEquals(WAVE, tracker.resolve(target, WAVE, 24.0, CLIPS).animation());
    assertEquals(WAVE, tracker.resolve(target, WAVE, 25.0, CLIPS).animation());
    assertFalse(tracker.hasPlayback(target));
  }

  @Test
  @DisplayName("An overriding fallback cuts a held playback and starts from its first frame")
  void overridingFallbackCutsAHeldPlayback() {
    Object target = new Object();
    EasyModelAnimationPlaybackTracker<Object> tracker = new EasyModelAnimationPlaybackTracker<>();
    tracker.resolve(target, IDLE, 0.0, CLIPS);
    tracker.play(
        target,
        WAVE,
        EasyModelAnimationPlayback.DEFAULT.withMode(EasyModelAnimationPlaybackMode.HOLD),
        EasyModelAnimationTransition.IMMEDIATE);
    tracker.resolve(target, IDLE, 0.0, CLIPS);
    assertEquals(WAVE, tracker.resolve(target, IDLE, 30.0, CLIPS).animation());

    EasyModelAnimationPlaybackFrame deathFrame =
        tracker.resolve(target, EasyModelAnimation.DEATH, true, () -> "", 30.0, CLIPS);

    assertEquals(EasyModelAnimation.DEATH, deathFrame.animation());
    assertEquals(0.0f, deathFrame.animationTicks(), 0.0001f);
    assertFalse(deathFrame.playbackDriven());
    assertFalse(tracker.hasPlayback(target));
  }

  @Test
  @DisplayName("An overriding fallback ignores later requests and never restarts")
  void overridingFallbackIgnoresLaterRequests() {
    Object target = new Object();
    EasyModelAnimationPlaybackTracker<Object> tracker = new EasyModelAnimationPlaybackTracker<>();
    tracker.resolve(target, EasyModelAnimation.DEATH, true, () -> "", 0.0, CLIPS);

    tracker.play(target, WAVE, EasyModelAnimationTransition.IMMEDIATE);
    tracker.playSequence(target, EasyModelAnimationSequence.of(WAVE).withFallback(IDLE));
    EasyModelAnimationPlaybackFrame laterFrame =
        tracker.resolve(target, EasyModelAnimation.DEATH, true, () -> "", 5.0, CLIPS);

    assertEquals(EasyModelAnimation.DEATH, laterFrame.animation());
    assertEquals(5.0f, laterFrame.animationTicks(), 0.0001f);
    assertFalse(tracker.hasPlayback(target));
  }
}
