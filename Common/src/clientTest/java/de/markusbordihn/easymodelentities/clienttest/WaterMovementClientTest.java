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
import de.markusbordihn.clientruntimeinterfacetoolkit.testrunner.data.TestArea;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class WaterMovementClientTest extends ClientTestBase {

  private static final TestArea BASIN =
      TestArea.between(-2, PLATFORM_Y + 1, 2, 3, PLATFORM_Y + 3, 6);
  private static final TestArea BASIN_WATER =
      TestArea.between(-1, PLATFORM_Y + 1, 3, 2, PLATFORM_Y + 2, 5);
  private static final long FLOPPING_PIXELS = 30;
  private static final long SUBMERGED_MODEL_PIXELS = 600;
  private static final long WATER_ANIMATION_PIXELS = 300;
  private static final long WATER_ANIMATION_CYCLE_TICKS = 64;
  private static final int WATER_PIXEL_TOLERANCE = 24;
  private static final double CLOSE_UP_DISTANCE = 2.0;
  private static final float CLOSE_UP_PITCH = 35.0f;
  private static final long FLOP_SAMPLE_TICKS = 3;

  private static void viewModelCloseUp() {
    client.teleport(MODEL_X, MODEL_Y, MODEL_Z - CLOSE_UP_DISTANCE, 0, CLOSE_UP_PITCH);
    client.await(Until.ticksElapsed(RENDER_TICKS));
  }

  @Test
  @DisplayName("An aquatic model stays visible inside water")
  void aquaticModelRendersInWater() {
    buildPlatform();
    client.fill(BASIN, "minecraft:barrier");
    client.fill(BASIN_WATER, "minecraft:water");
    summonFrozenModel(CORAL_DRIFTER, MODEL_X, MODEL_Y + 0.5, MODEL_Z + 0.5);
    viewModelCloseUp();
    client.saveScreenshot("coral_drifter_in_water");
    String submergedFrame = client.retainFrame();

    moveModelsBelowWorld();
    client.await(Until.ticksElapsed(WATER_ANIMATION_CYCLE_TICKS));
    long modelPixels = changedPixelsSince(submergedFrame, WATER_PIXEL_TOLERANCE);

    String waterOnlyFrame = client.retainFrame();
    client.await(Until.ticksElapsed(WATER_ANIMATION_CYCLE_TICKS));
    long waterAnimationPixels = changedPixelsSince(waterOnlyFrame, WATER_PIXEL_TOLERANCE);

    assertTrue(
        waterAnimationPixels <= WATER_ANIMATION_PIXELS,
        "water animation out of phase: " + waterAnimationPixels);
    assertTrue(
        modelPixels > SUBMERGED_MODEL_PIXELS, "model not visible in water: " + modelPixels);
  }

  @Test
  @DisplayName("A stranded aquatic model without a walk clip lies on its side and flops")
  void strandedAquaticModelFlopsOnLand() {
    buildPlatform();
    summonFrozenModel(CORAL_DRIFTER, MODEL_X, MODEL_Y, MODEL_Z);
    viewModelCloseUp();
    client.saveScreenshot("coral_drifter_on_land");

    String strandedFrame = client.retainFrame();
    client.await(Until.ticksElapsed(FLOP_SAMPLE_TICKS));
    long changedPixels = changedPixelsSince(strandedFrame);

    assertTrue(changedPixels > FLOPPING_PIXELS, "stranded model does not flop: " + changedPixels);
  }
}
