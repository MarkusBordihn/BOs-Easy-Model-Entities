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

import de.markusbordihn.clientruntimeinterfacetoolkit.testrunner.GameClientBuilder;
import de.markusbordihn.clientruntimeinterfacetoolkit.testrunner.GameClientExtension;
import de.markusbordihn.clientruntimeinterfacetoolkit.testrunner.Until;
import de.markusbordihn.clientruntimeinterfacetoolkit.testrunner.data.CompareOptions;
import de.markusbordihn.clientruntimeinterfacetoolkit.testrunner.data.Parameters;
import de.markusbordihn.clientruntimeinterfacetoolkit.testrunner.data.TestArea;
import de.markusbordihn.clientruntimeinterfacetoolkit.testrunner.data.api.CaptureComparison;
import de.markusbordihn.clientruntimeinterfacetoolkit.testrunner.data.api.CaptureCrop;
import java.time.Duration;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.RegisterExtension;

abstract class ClientTestBase {

  @RegisterExtension
  static final GameClientExtension client =
      GameClientExtension.shared(ClientTestBase::configureLaunch);

  static final String COMMAND = "easy_model_entities ";
  static final String MODELS = "@e[type=!minecraft:player,distance=..32]";
  static final String NEAREST_MODEL = "@e[type=!minecraft:player,distance=..32,limit=1]";
  static final String LITTLE_EXPLORER = "easy_model_entities_examples:entity/little_explorer";
  static final String CORAL_DRIFTER = "easy_model_entities_examples:entity/coral_drifter";
  static final int PLATFORM_Y = 100;
  static final double MODEL_Y = PLATFORM_Y + 1;
  static final double MODEL_X = 0.5;
  static final double MODEL_Z = 3.5;
  static final long RENDER_TICKS = 20;
  static final long CLIP_HOLD_TICKS = 30;
  static final long UNCHANGED_PIXELS = 150;

  private static final String SUITE_NAME = "Easy Model Entities";
  private static final String WORLD_NAME = "EasyModelEntities Client Test";
  private static final TestArea VIEWING_PLATFORM =
      TestArea.between(-4, PLATFORM_Y, -1, 5, PLATFORM_Y, 8);
  private static final float VIEW_PITCH = 20.0f;
  private static final int BELOW_WORLD_Y = -200;
  private static final int PIXEL_TOLERANCE = 8;
  private static final int MODEL_AREA_X = 240;
  private static final int MODEL_AREA_Y = 40;
  private static final int MODEL_AREA_WIDTH = 160;
  private static final int MODEL_AREA_HEIGHT = 220;
  private static final CompareOptions MODEL_AREA_COMPARE =
      CompareOptions.DEFAULT
          .withTolerance(PIXEL_TOLERANCE)
          .withRegion(MODEL_AREA_X, MODEL_AREA_Y, MODEL_AREA_WIDTH, MODEL_AREA_HEIGHT);
  private static final int LEGS_AREA_X = 280;
  private static final int LEGS_AREA_Y = 182;
  private static final int LEGS_AREA_WIDTH = 80;
  private static final int LEGS_AREA_HEIGHT = 50;
  private static final CompareOptions LEGS_AREA_COMPARE =
      CompareOptions.DEFAULT
          .withTolerance(PIXEL_TOLERANCE)
          .withRegion(LEGS_AREA_X, LEGS_AREA_Y, LEGS_AREA_WIDTH, LEGS_AREA_HEIGHT);

  private static GameClientBuilder configureLaunch(GameClientBuilder builder) {
    return builder
        .withSuiteName(SUITE_NAME)
        .withWindowSize(1280, 720)
        .withGuiScale(2)
        .withAllowedCommands("easy_model_entities", "kill", "damage", "time", "tp")
        .withWorld(Parameters.of("name", WORLD_NAME))
        .withLayoutChecksOnScreenshot();
  }

  static void buildPlatform() {
    client.fill(VIEWING_PLATFORM, "minecraft:stone");
  }

  static void summonFrozenModel(String profileId, double x, double y, double z) {
    client.teleport(x, y, z);
    client.runCommand(COMMAND + "summon " + profileId);
    client.runCommand(COMMAND + "behavior set entity " + MODELS + " frozen");
    client.runCommand("tp " + MODELS + " " + x + " " + y + " " + z + " 180 0");
    viewModel();
  }

  static void viewModel() {
    client.teleport(MODEL_X, MODEL_Y, 0.5, 0, VIEW_PITCH);
    client.await(Until.ticksElapsed(RENDER_TICKS));
  }

  static void holdLastFrameOf(String clip) {
    client.runCommand(COMMAND + "animation play entity " + MODELS + " " + clip + " hold");
    client.await(Until.ticksElapsed(CLIP_HOLD_TICKS));
  }

  static void moveModelsBelowWorld() {
    client.runCommand("tp " + MODELS + " " + MODEL_X + " " + BELOW_WORLD_Y + " " + MODEL_Z);
  }

  static long changedPixelsSince(String baselineFrameId) {
    return client.compare(baselineFrameId, MODEL_AREA_COMPARE).comparison().changedPixels();
  }

  static long changedPixelsSince(String baselineFrameId, int pixelTolerance) {
    return client
        .compare(baselineFrameId, MODEL_AREA_COMPARE.withTolerance(pixelTolerance))
        .comparison()
        .changedPixels();
  }

  static Optional<CaptureCrop> changedBoundsSince(String baselineFrameId, int pixelTolerance) {
    return client
        .compare(baselineFrameId, MODEL_AREA_COMPARE.withTolerance(pixelTolerance))
        .comparison()
        .changedBounds();
  }

  static long changedLegPixelsSince(String baselineFrameId) {
    return client.compare(baselineFrameId, LEGS_AREA_COMPARE).comparison().changedPixels();
  }

  static CaptureComparison awaitChangedPixelsSince(
      String baselineFrameId, long minimumChangedPixels, Duration timeout) {
    double minimumChangedRatio =
        minimumChangedPixels / (double) (MODEL_AREA_WIDTH * MODEL_AREA_HEIGHT);
    return client
        .compare(
            baselineFrameId,
            MODEL_AREA_COMPARE.withAwaitChangedRatioAbove(minimumChangedRatio, timeout))
        .comparison();
  }

  @BeforeEach
  void hideChatAndToasts() {
    client.renderSettings(Parameters.of().with("chat", false).with("toasts", false));
  }

  @AfterEach
  void removeModels() {
    moveModelsBelowWorld();
    client.runCommand("time set day");
  }
}
