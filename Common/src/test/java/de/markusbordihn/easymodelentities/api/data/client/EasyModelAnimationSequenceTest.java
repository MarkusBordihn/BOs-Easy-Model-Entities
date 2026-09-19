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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import de.markusbordihn.easymodelentities.api.data.EasyModelAnimation;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

class EasyModelAnimationSequenceTest {

  private static final EasyModelAnimation IDLE = EasyModelAnimation.named("idle");
  private static final EasyModelAnimation WAVE = EasyModelAnimation.named("wave");

  @Test
  void immediateFollowUpStepsWaitForTheirPredecessor() {
    EasyModelAnimationSequence sequence =
        new EasyModelAnimationSequence(
            List.of(
                EasyModelAnimationStep.of(WAVE),
                new EasyModelAnimationStep(
                    IDLE,
                    EasyModelAnimationPlayback.DEFAULT,
                    EasyModelAnimationTransition.immediate(3.0f))),
            EasyModelAnimation.AUTO);

    assertEquals(
        EasyModelAnimationSwitchTiming.IMMEDIATE, sequence.firstStep().transition().timing());
    assertEquals(
        EasyModelAnimationSwitchTiming.AFTER_CURRENT,
        sequence.steps().get(1).transition().timing());
    assertEquals(3.0f, sequence.steps().get(1).transition().blendDurationTicks(), 0.0001f);
  }

  @Test
  void loopingIsReservedForTheLastStep() {
    EasyModelAnimationPlayback looping =
        EasyModelAnimationPlayback.DEFAULT.withMode(EasyModelAnimationPlaybackMode.LOOP);
    EasyModelAnimationSequence sequence =
        new EasyModelAnimationSequence(
            List.of(
                EasyModelAnimationStep.of(WAVE).withPlayback(looping),
                EasyModelAnimationStep.of(IDLE).withPlayback(looping)),
            EasyModelAnimation.AUTO);

    assertEquals(EasyModelAnimationPlaybackMode.ONCE, sequence.firstStep().playback().mode());
    assertEquals(EasyModelAnimationPlaybackMode.LOOP, sequence.steps().get(1).playback().mode());
  }

  @Test
  void rejectsEmptyAndOversizedStepLists() {
    assertThrows(
        IllegalArgumentException.class,
        () -> new EasyModelAnimationSequence(List.of(), EasyModelAnimation.AUTO));
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new EasyModelAnimationSequence(
                Collections.nCopies(
                    EasyModelAnimationSequence.MAX_STEPS + 1, EasyModelAnimationStep.of(WAVE)),
                EasyModelAnimation.AUTO));
  }

  @Test
  void rejectsTheAutomaticAnimationAsStep() {
    assertThrows(
        IllegalArgumentException.class, () -> EasyModelAnimationStep.of(EasyModelAnimation.AUTO));
  }

  @Test
  void defaultsToTheAutomaticFallback() {
    EasyModelAnimationSequence sequence = EasyModelAnimationSequence.of(WAVE, IDLE);

    assertEquals(EasyModelAnimation.AUTO, sequence.fallback());
    assertEquals(WAVE, sequence.firstStep().animation());
    assertEquals(IDLE, sequence.withFallback(IDLE).fallback());
    assertEquals(sequence.steps(), sequence.withFallback(IDLE).steps());
  }
}
