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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DisplayClientTest extends ClientTestBase {

  private static final String OTHER_TEXTURE =
      "easy_model_entities_examples:textures/entity/stone_crawler.png";
  private static final long VISIBLE_CHANGE_PIXELS = 500;
  private static final long SYNC_TICKS = 5;

  private static String prepareHeldExplorer() {
    buildPlatform();
    summonFrozenModel(LITTLE_EXPLORER, MODEL_X, MODEL_Y, MODEL_Z);
    holdLastFrameOf("walk");
    return client.retainFrame();
  }

  private static void runAndSync(String command) {
    client.runCommand(COMMAND + command);
    client.await(Until.ticksElapsed(SYNC_TICKS));
  }

  @Test
  @DisplayName("A texture override repaints the model and clearing it restores the original")
  void textureOverrideRepaintsAndClears() {
    String originalFrame = prepareHeldExplorer();

    runAndSync("texture set entity " + MODELS + " default " + OTHER_TEXTURE);
    long overriddenPixels = changedPixelsSince(originalFrame);
    client.saveScreenshot("texture_override");

    runAndSync("texture clear entity " + MODELS);
    long clearedPixels = changedLegPixelsSince(originalFrame);
    client.saveScreenshot("texture_cleared");

    assertTrue(
        overriddenPixels > VISIBLE_CHANGE_PIXELS, "texture did not change: " + overriddenPixels);
    assertTrue(clearedPixels <= UNCHANGED_PIXELS, "texture not restored: " + clearedPixels);
  }

  @Test
  @DisplayName("A lowered opacity fades the model into the background")
  void opacityFadesTheModel() {
    String opaqueFrame = prepareHeldExplorer();

    runAndSync("display opacity set entity " + MODELS + " 30");
    long fadedPixels = changedPixelsSince(opaqueFrame);
    client.saveScreenshot("opacity_30");

    runAndSync("display opacity clear entity " + MODELS);
    long clearedPixels = changedLegPixelsSince(opaqueFrame);
    client.saveScreenshot("opacity_cleared");

    assertTrue(fadedPixels > VISIBLE_CHANGE_PIXELS, "model did not fade: " + fadedPixels);
    assertTrue(clearedPixels <= UNCHANGED_PIXELS, "opacity not restored: " + clearedPixels);
  }

  @Test
  @DisplayName("A minimum light level brightens the model at night")
  void lightLevelBrightensTheModelAtNight() {
    client.runCommand("time set midnight");
    String darkFrame = prepareHeldExplorer();
    client.saveScreenshot("light_default_night");

    runAndSync("display light set entity " + MODELS + " 15");
    long brightenedPixels = changedPixelsSince(darkFrame);
    client.saveScreenshot("light_15_night");

    runAndSync("display light clear entity " + MODELS);
    long clearedPixels = changedLegPixelsSince(darkFrame);
    client.saveScreenshot("light_cleared_night");

    assertTrue(
        brightenedPixels > VISIBLE_CHANGE_PIXELS, "model did not brighten: " + brightenedPixels);
    assertTrue(clearedPixels <= UNCHANGED_PIXELS, "light not restored: " + clearedPixels);
  }
}
