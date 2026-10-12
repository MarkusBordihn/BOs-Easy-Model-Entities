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

package de.markusbordihn.easymodelentities.clienttest;

import static org.junit.jupiter.api.Assertions.assertTrue;

import de.markusbordihn.clientruntimeinterfacetoolkit.testrunner.Until;
import de.markusbordihn.clientruntimeinterfacetoolkit.testrunner.data.api.CaptureComparison;
import java.time.Duration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AnimationClientTest extends ClientTestBase {

  private static final long WALK_CLIP_TICKS = 20;
  private static final long MOVING_PIXELS = 150;
  private static final long OVERLAY_PIXELS = 500;
  private static final Duration OVERLAY_TIMEOUT = Duration.ofSeconds(3);
  private static final long DEATH_PARTICLES_GONE_TICKS = 60;
  private static final long HELD_POSE_SAMPLE_TICKS = 10;
  private static final long MOTION_SAMPLE_TICKS = 5;

  @Test
  @DisplayName("A held clip stops on its last frame instead of looping")
  void heldClipStopsOnItsLastFrame() {
    buildPlatform();
    summonFrozenModel(LITTLE_EXPLORER, MODEL_X, MODEL_Y, MODEL_Z);

    holdLastFrameOf("walk");
    String heldFrame = client.retainFrame();
    client.await(Until.ticksElapsed(HELD_POSE_SAMPLE_TICKS));

    long changedPixels = changedLegPixelsSince(heldFrame);
    client.saveScreenshot("walk_hold");
    assertTrue(changedPixels <= UNCHANGED_PIXELS, "held pose still moves: " + changedPixels);
  }

  @Test
  @DisplayName("A looping clip keeps moving the model")
  void loopingClipKeepsMoving() {
    buildPlatform();
    summonFrozenModel(LITTLE_EXPLORER, MODEL_X, MODEL_Y, MODEL_Z);

    client.runCommand(COMMAND + "animation play entity " + MODELS + " walk loop");
    client.await(Until.ticksElapsed(WALK_CLIP_TICKS + 5));
    String loopFrame = client.retainFrame();
    client.await(Until.ticksElapsed(MOTION_SAMPLE_TICKS));

    long changedPixels = changedPixelsSince(loopFrame);
    client.saveScreenshot("walk_loop");
    assertTrue(changedPixels > MOVING_PIXELS, "looping clip did not move: " + changedPixels);
  }

  @Test
  @DisplayName("A faster clip reaches its held last frame before the normal clip length")
  void fasterClipReachesItsLastFrameEarly() {
    buildPlatform();
    summonFrozenModel(LITTLE_EXPLORER, MODEL_X, MODEL_Y, MODEL_Z);

    client.runCommand(
        COMMAND + "animation play entity " + MODELS + " walk hold immediate 0 0 4.0");
    client.await(Until.ticksElapsed(WALK_CLIP_TICKS / 2));
    String heldFrame = client.retainFrame();
    client.await(Until.ticksElapsed(MOTION_SAMPLE_TICKS));

    long changedPixels = changedLegPixelsSince(heldFrame);
    client.saveScreenshot("walk_hold_speed_4");
    assertTrue(changedPixels <= UNCHANGED_PIXELS, "fast clip still moves: " + changedPixels);
  }

  @Test
  @DisplayName("A stopped clip returns the legs to a still pose")
  void stoppedClipReturnsToAStillPose() {
    buildPlatform();
    summonFrozenModel(LITTLE_EXPLORER, MODEL_X, MODEL_Y, MODEL_Z);
    client.runCommand(COMMAND + "animation play entity " + MODELS + " walk loop");
    client.await(Until.ticksElapsed(WALK_CLIP_TICKS));

    client.runCommand(COMMAND + "animation stop entity " + MODELS);
    client.await(Until.ticksElapsed(WALK_CLIP_TICKS));
    String stoppedFrame = client.retainFrame();
    client.await(Until.ticksElapsed(HELD_POSE_SAMPLE_TICKS));

    long changedPixels = changedLegPixelsSince(stoppedFrame);
    client.saveScreenshot("walk_stopped");
    assertTrue(changedPixels <= UNCHANGED_PIXELS, "stopped clip still moves: " + changedPixels);
  }

  @Test
  @DisplayName("Restarting a held clip plays it again from the first frame")
  void restartReplaysAHeldClip() {
    buildPlatform();
    summonFrozenModel(LITTLE_EXPLORER, MODEL_X, MODEL_Y, MODEL_Z);
    holdLastFrameOf("walk");
    String heldFrame = client.retainFrame();

    client.runCommand(COMMAND + "animation restart entity " + MODELS);
    CaptureComparison comparison =
        awaitChangedPixelsSince(heldFrame, MOVING_PIXELS, OVERLAY_TIMEOUT);

    client.saveScreenshot("walk_restarted");
    assertTrue(
        comparison.ratioReached().orElse(false),
        "restart did not replay the clip, changed pixels " + comparison.changedPixels());
  }

  @Test
  @DisplayName("A forced animation state keeps the legs walking")
  void forcedAnimationStateKeepsWalking() {
    buildPlatform();
    summonFrozenModel(LITTLE_EXPLORER, MODEL_X, MODEL_Y, MODEL_Z);

    client.runCommand(COMMAND + "animation set entity " + MODELS + " walk loop");
    assertLegsMove("walk_set");
  }

  @Test
  @DisplayName("A random pick from the server plays the listed clip")
  void randomPickPlaysTheListedClip() {
    buildPlatform();
    summonFrozenModel(LITTLE_EXPLORER, MODEL_X, MODEL_Y, MODEL_Z);

    client.runCommand(COMMAND + "animation play entity " + MODELS + " random \"walk\" loop");
    assertLegsMove("walk_random");
  }

  @Test
  @DisplayName("A clip sequence plays its clips one after another")
  void sequencePlaysItsClips() {
    buildPlatform();
    summonFrozenModel(LITTLE_EXPLORER, MODEL_X, MODEL_Y, MODEL_Z);

    client.runCommand(
        COMMAND + "animation play entity " + MODELS + " sequence \"walk,walk,walk\" fallback idle");
    assertLegsMove("walk_sequence");
  }

  @Test
  @DisplayName("Damage tints the model with the hurt overlay")
  void damageShowsTheHurtOverlay() {
    buildPlatform();
    summonFrozenModel(LITTLE_EXPLORER, MODEL_X, MODEL_Y, MODEL_Z);
    holdLastFrameOf("walk");
    String unhurtFrame = client.retainFrame();

    client.runCommand("damage " + NEAREST_MODEL + " 1");
    CaptureComparison comparison =
        awaitChangedPixelsSince(unhurtFrame, OVERLAY_PIXELS, OVERLAY_TIMEOUT);

    client.saveScreenshot("hurt");
    assertTrue(
        comparison.ratioReached().orElse(false),
        "no hurt overlay, changed pixels " + comparison.changedPixels());
  }

  @Test
  @DisplayName("A killed model renders with the death overlay")
  void killedModelRendersWithTheDeathOverlay() {
    buildPlatform();
    summonFrozenModel(LITTLE_EXPLORER, MODEL_X, MODEL_Y, MODEL_Z);
    holdLastFrameOf("walk");
    String aliveFrame = client.retainFrame();

    client.runCommand("kill " + MODELS);
    CaptureComparison comparison =
        awaitChangedPixelsSince(aliveFrame, OVERLAY_PIXELS, OVERLAY_TIMEOUT);

    client.saveScreenshot("death");
    client.await(Until.ticksElapsed(DEATH_PARTICLES_GONE_TICKS));
    assertTrue(
        comparison.ratioReached().orElse(false),
        "no death overlay, changed pixels " + comparison.changedPixels());
  }

  private static void assertLegsMove(String screenshotName) {
    client.await(Until.ticksElapsed(WALK_CLIP_TICKS + 5));
    String walkFrame = client.retainFrame();
    client.await(Until.ticksElapsed(MOTION_SAMPLE_TICKS));

    long changedPixels = changedLegPixelsSince(walkFrame);
    client.saveScreenshot(screenshotName);
    assertTrue(changedPixels > MOVING_PIXELS, "legs did not walk: " + changedPixels);
  }
}
