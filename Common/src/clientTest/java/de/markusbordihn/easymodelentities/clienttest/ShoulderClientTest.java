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
import de.markusbordihn.clientruntimeinterfacetoolkit.testrunner.data.Parameters;
import de.markusbordihn.clientruntimeinterfacetoolkit.testrunner.data.api.CaptureCrop;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ShoulderClientTest extends ClientTestBase {

  private static final String PERCHED_SPARROW =
      "easy_model_entities_examples:entity/perched_sparrow";
  private static final double BEHIND_CAMERA_Z = 20.5;
  private static final double FALL_HEIGHT = 3.0;
  private static final long VISIBLE_CHANGE_PIXELS = 500;
  private static final long SYNC_TICKS = 5;
  private static final long LANDING_TICKS = 20;
  private static final double SHOULDER_OFFSET_UP = 0.25;
  private static final double SHOULDER_OFFSET_DOWN = -0.25;
  private static final int SKY_DRIFT_PIXEL_TOLERANCE = 32;

  // Vanilla Player.aiStep ignores fall triggers for 20 ticks after an entity landed on the
  // shoulder.
  private static final long SHOULDER_GUARD_TICKS = 30;

  private static void moveModelsBehindCamera() {
    client.runCommand(
        "tp " + MODELS + " " + MODEL_X + " " + MODEL_Y + " " + BEHIND_CAMERA_Z + " 180 0");
    client.await(Until.ticksElapsed(SYNC_TICKS));
  }

  private static String preparePlayerFrontView() {
    buildPlatform();
    client.teleport(MODEL_X, MODEL_Y, MODEL_Z, 0, 0);
    client.runCommand(COMMAND + "summon " + PERCHED_SPARROW);
    client.runCommand(COMMAND + "behavior set entity " + MODELS + " frozen");
    moveModelsBehindCamera();
    client.renderSettings(Parameters.of().with("perspective", "front"));
    client.await(Until.ticksElapsed(RENDER_TICKS));
    return client.retainFrame();
  }

  private static void dropModelsFromShoulder() {
    client.teleport(MODEL_X, MODEL_Y + FALL_HEIGHT, MODEL_Z);
    client.await(Until.ticksElapsed(LANDING_TICKS));
    moveModelsBehindCamera();
  }

  private static long seatedModelTop(double shoulderOffsetY, String screenshotName) {
    String playerOnlyFrame = client.retainFrame();
    client.runCommand(
        COMMAND + "shoulder offset set entity " + MODELS + " 0 " + shoulderOffsetY + " 0");
    client.runCommand(COMMAND + "shoulder sit entity " + MODELS + " @s");
    client.await(Until.ticksElapsed(SHOULDER_GUARD_TICKS));
    CaptureCrop seatedBounds =
        changedBoundsSince(playerOnlyFrame, SKY_DRIFT_PIXEL_TOLERANCE).orElseThrow();
    client.saveScreenshot(screenshotName);
    dropModelsFromShoulder();
    return seatedBounds.y();
  }

  @Test
  @DisplayName("A shoulder offset moves the seated model up or down on the shoulder")
  void shoulderOffsetMovesSeatedModel() {
    preparePlayerFrontView();

    long defaultTop = seatedModelTop(0.0, "shoulder_offset_default");
    long raisedTop = seatedModelTop(SHOULDER_OFFSET_UP, "shoulder_offset_up");
    long loweredTop = seatedModelTop(SHOULDER_OFFSET_DOWN, "shoulder_offset_down");

    String tops = "default " + defaultTop + ", up " + raisedTop + ", down " + loweredTop;
    assertTrue(raisedTop < defaultTop, "offset up did not raise the model: " + tops);
    assertTrue(loweredTop > defaultTop, "offset down did not lower the model: " + tops);
  }

  @Test
  @DisplayName("A model seated on the shoulder renders on the player and drops off after a fall")
  void shoulderModelRendersAndDropsOffAfterFall() {
    String playerOnlyFrame = preparePlayerFrontView();
    client.saveScreenshot("shoulder_empty");

    client.runCommand(COMMAND + "shoulder sit entity " + MODELS + " @s");
    client.await(Until.ticksElapsed(SHOULDER_GUARD_TICKS));
    long seatedPixels = changedPixelsSince(playerOnlyFrame);
    String seatedFrame = client.retainFrame();
    client.saveScreenshot("shoulder_seated");

    dropModelsFromShoulder();
    long droppedPixels = changedPixelsSince(seatedFrame);
    client.saveScreenshot("shoulder_dropped");

    assertTrue(seatedPixels > VISIBLE_CHANGE_PIXELS, "model not on shoulder: " + seatedPixels);
    assertTrue(droppedPixels > VISIBLE_CHANGE_PIXELS, "model still on shoulder: " + droppedPixels);
  }
}
