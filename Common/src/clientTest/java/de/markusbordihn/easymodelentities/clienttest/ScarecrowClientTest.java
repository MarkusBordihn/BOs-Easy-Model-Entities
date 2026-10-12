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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ScarecrowClientTest extends ClientTestBase {

  private static final String SCARECROW = "easy_model_entities_examples:block_entity/scarecrow";
  private static final int SCARECROW_X = (int) Math.floor(MODEL_X);
  private static final int SCARECROW_Y = (int) Math.floor(MODEL_Y);
  private static final int SCARECROW_Z = (int) Math.floor(MODEL_Z);
  private static final TestArea SCARECROW_BLOCK =
      TestArea.between(
          SCARECROW_X, SCARECROW_Y, SCARECROW_Z, SCARECROW_X, SCARECROW_Y, SCARECROW_Z);
  private static final double FULL_HEIGHT_VIEW_DISTANCE = 4.0;
  private static final float FULL_HEIGHT_VIEW_PITCH = 15.0f;
  private static final double TUB_VIEW_OFFSET = 1.5;
  private static final double TUB_VIEW_HEIGHT = 0.5;
  private static final float TUB_VIEW_YAW = 45.0f;
  private static final float TUB_VIEW_PITCH = 40.0f;
  private static final String SCARECROW_TARGET =
      "block " + SCARECROW_X + " " + SCARECROW_Y + " " + SCARECROW_Z;
  private static final String OTHER_TEXTURE =
      "easy_model_entities_examples:textures/entity/stone_crawler.png";
  private static final long VISIBLE_MODEL_PIXELS = 500;
  private static final long GLOWING_CROW_PIXELS = 100;
  private static final long SYNC_TICKS = 5;
  private static final List<SideView> SIDE_VIEWS =
      List.of(
          new SideView("front", 0, -FULL_HEIGHT_VIEW_DISTANCE, 0),
          new SideView("back", 0, FULL_HEIGHT_VIEW_DISTANCE, 180),
          new SideView("east", FULL_HEIGHT_VIEW_DISTANCE, 0, 90),
          new SideView("west", -FULL_HEIGHT_VIEW_DISTANCE, 0, -90));

  @Test
  @DisplayName("The scarecrow shows all three textures from every side")
  void rendersAllTexturesFromEverySide() {
    buildPlatform();
    Map<SideView, String> emptyFrames = new HashMap<>();
    for (SideView sideView : SIDE_VIEWS) {
      this.viewFrom(sideView);
      emptyFrames.put(sideView, client.retainFrame());
    }
    this.viewFrom(SIDE_VIEWS.get(0));
    this.placeScarecrow();
    for (SideView sideView : SIDE_VIEWS) {
      this.viewFrom(sideView);
      client.saveScreenshot(sideView.name());
      long modelPixels = changedPixelsSince(emptyFrames.get(sideView));
      assertTrue(
          modelPixels > VISIBLE_MODEL_PIXELS,
          sideView.name() + " side not rendered: " + modelPixels);
    }
    client.teleport(
        MODEL_X + TUB_VIEW_OFFSET,
        MODEL_Y + TUB_VIEW_HEIGHT,
        MODEL_Z - TUB_VIEW_OFFSET,
        TUB_VIEW_YAW,
        TUB_VIEW_PITCH);
    client.await(Until.ticksElapsed(RENDER_TICKS));
    client.saveScreenshot("tub");
  }

  @Test
  @DisplayName("A slot texture override repaints the scarecrow body and clearing restores it")
  void slotTextureOverrideRepaintsAndClears() {
    String originalFrame = this.prepareScarecrow();

    this.runAndSync("texture set " + SCARECROW_TARGET + " slot scarecrow_body " + OTHER_TEXTURE);
    long overriddenPixels = changedPixelsSince(originalFrame);
    client.saveScreenshot("texture_slot_override");

    this.runAndSync("texture clear " + SCARECROW_TARGET + " slot scarecrow_body");
    long clearedPixels = changedPixelsSince(originalFrame);
    client.saveScreenshot("texture_slot_cleared");

    assertTrue(
        overriddenPixels > VISIBLE_MODEL_PIXELS, "slot did not change: " + overriddenPixels);
    assertTrue(clearedPixels <= UNCHANGED_PIXELS, "slot not restored: " + clearedPixels);
  }

  @Test
  @DisplayName("A lowered opacity fades the scarecrow and clearing restores it")
  void opacityFadesAndClears() {
    String opaqueFrame = this.prepareScarecrow();

    this.runAndSync("display opacity set " + SCARECROW_TARGET + " 30");
    long fadedPixels = changedPixelsSince(opaqueFrame);
    client.saveScreenshot("opacity_30");

    this.runAndSync("display opacity clear " + SCARECROW_TARGET);
    long clearedPixels = changedPixelsSince(opaqueFrame);
    client.saveScreenshot("opacity_cleared");

    assertTrue(fadedPixels > VISIBLE_MODEL_PIXELS, "scarecrow did not fade: " + fadedPixels);
    assertTrue(clearedPixels <= UNCHANGED_PIXELS, "opacity not restored: " + clearedPixels);
  }

  @Test
  @DisplayName("A minimum light level brightens the scarecrow at night")
  void lightLevelBrightensAtNight() {
    client.runCommand("time set midnight");
    String darkFrame = this.prepareScarecrow();
    client.saveScreenshot("light_default_night");

    this.runAndSync("display light set " + SCARECROW_TARGET + " 15");
    long brightenedPixels = changedPixelsSince(darkFrame);
    client.saveScreenshot("light_15_night");

    assertTrue(
        brightenedPixels > VISIBLE_MODEL_PIXELS,
        "scarecrow did not brighten: " + brightenedPixels);
  }

  @Test
  @DisplayName("An emissive blend lets the crow glow at night")
  void emissiveBlendLetsTheCrowGlow() {
    client.runCommand("time set midnight");
    String darkFrame = this.prepareScarecrow();

    this.runAndSync("texture blend " + SCARECROW_TARGET + " slot scarecrow_crow emissive");
    long glowingPixels = changedPixelsSince(darkFrame);
    client.saveScreenshot("crow_emissive_night");

    assertTrue(glowingPixels > GLOWING_CROW_PIXELS, "crow did not glow: " + glowingPixels);
  }

  @AfterEach
  void removeScarecrow() {
    client.fill(SCARECROW_BLOCK, "minecraft:air");
  }

  private String prepareScarecrow() {
    buildPlatform();
    this.viewFrom(SIDE_VIEWS.get(0));
    this.placeScarecrow();
    client.await(Until.ticksElapsed(RENDER_TICKS));
    return client.retainFrame();
  }

  private void placeScarecrow() {
    client.runCommand(
        COMMAND
            + "place_block "
            + SCARECROW
            + " "
            + SCARECROW_X
            + " "
            + SCARECROW_Y
            + " "
            + SCARECROW_Z);
  }

  private void runAndSync(String command) {
    client.runCommand(COMMAND + command);
    client.await(Until.ticksElapsed(SYNC_TICKS));
  }

  private void viewFrom(SideView sideView) {
    client.teleport(
        MODEL_X + sideView.xOffset(),
        MODEL_Y,
        MODEL_Z + sideView.zOffset(),
        sideView.yaw(),
        FULL_HEIGHT_VIEW_PITCH);
    client.await(Until.ticksElapsed(RENDER_TICKS));
  }

  private record SideView(String name, double xOffset, double zOffset, float yaw) {}
}
