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

package de.markusbordihn.easymodelentities.gametest;

import de.markusbordihn.easymodelentities.Constants;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;

@SuppressWarnings("unused")
public class HostEntityGameTest {

  @GameTest(structure = Constants.MOD_ID + ":gametest.3x3x3")
  public void groundEntityCanBeCreated(GameTestHelper helper) {
    GameTestHelpers.runIfEnabled(helper, HostEntityGameTestCases::groundEntityCanBeCreated);
  }

  @GameTest(structure = Constants.MOD_ID + ":gametest.3x3x3")
  public void staticEntityCanBeCreated(GameTestHelper helper) {
    GameTestHelpers.runIfEnabled(helper, HostEntityGameTestCases::staticEntityCanBeCreated);
  }

  @GameTest(structure = Constants.MOD_ID + ":gametest.3x3x3")
  public void missingAndInvalidProfilesReturnEmpty(GameTestHelper helper) {
    GameTestHelpers.runIfEnabled(
        helper, HostEntityGameTestCases::missingAndInvalidProfilesReturnEmpty);
  }

  @GameTest(structure = Constants.MOD_ID + ":gametest.3x3x3")
  public void frozenBehaviorStopsHostEntityMovement(GameTestHelper helper) {
    GameTestHelpers.runIfEnabled(
        helper, HostEntityGameTestCases::frozenBehaviorStopsHostEntityMovement);
  }

  @GameTest(structure = Constants.MOD_ID + ":gametest.3x3x3")
  public void nbtPreservesRuntimeContractData(GameTestHelper helper) {
    GameTestHelpers.runIfEnabled(helper, HostEntityGameTestCases::nbtPreservesRuntimeContractData);
  }

  @GameTest(structure = Constants.MOD_ID + ":gametest.3x3x3")
  public void nbtPreservesTextureSetting(GameTestHelper helper) {
    GameTestHelpers.runIfEnabled(helper, HostEntityGameTestCases::nbtPreservesTextureSetting);
  }

  @GameTest(structure = Constants.MOD_ID + ":gametest.3x3x3")
  public void nbtPreservesDisplaySettings(GameTestHelper helper) {
    GameTestHelpers.runIfEnabled(helper, HostEntityGameTestCases::nbtPreservesDisplaySettings);
  }

  @GameTest(structure = Constants.MOD_ID + ":gametest.3x3x3")
  public void apiControlsHostEntityTextureAndAnimation(GameTestHelper helper) {
    GameTestHelpers.runIfEnabled(
        helper, HostEntityGameTestCases::apiControlsHostEntityTextureAndAnimation);
  }

  @GameTest(structure = Constants.MOD_ID + ":gametest.3x3x3")
  public void apiControlsMutableRenderableEntity(GameTestHelper helper) {
    GameTestHelpers.runIfEnabled(
        helper, HostEntityGameTestCases::apiControlsMutableRenderableEntity);
  }

  @GameTest(structure = Constants.MOD_ID + ":gametest.3x3x3")
  public void displayCommandChangesOnlyMutableRenderables(GameTestHelper helper) {
    GameTestHelpers.runIfEnabled(
        helper, HostEntityGameTestCases::displayCommandChangesOnlyMutableRenderables);
  }

  @GameTest(structure = Constants.MOD_ID + ":gametest.3x3x3")
  public void apiControlsHostBlockEntityTextureAndAnimation(GameTestHelper helper) {
    GameTestHelpers.runIfEnabled(
        helper, HostEntityGameTestCases::apiControlsHostBlockEntityTextureAndAnimation);
  }

  @GameTest(structure = Constants.MOD_ID + ":gametest.3x3x3")
  public void apiControlsHostEntityDisplaySettings(GameTestHelper helper) {
    GameTestHelpers.runIfEnabled(
        helper, HostEntityGameTestCases::apiControlsHostEntityDisplaySettings);
  }

  @GameTest(structure = Constants.MOD_ID + ":gametest.3x3x3")
  public void apiPlaysAnimationsOnHostEntity(GameTestHelper helper) {
    GameTestHelpers.runIfEnabled(helper, HostEntityGameTestCases::apiPlaysAnimationsOnHostEntity);
  }

  @GameTest(structure = Constants.MOD_ID + ":gametest.3x3x3")
  public void apiSitsHostEntityOnShoulder(GameTestHelper helper) {
    GameTestHelpers.runIfEnabled(helper, HostEntityGameTestCases::apiSitsHostEntityOnShoulder);
  }

  @GameTest(structure = Constants.MOD_ID + ":gametest.3x3x3")
  public void apiControlsHostBlockEntityProfileAndDisplay(GameTestHelper helper) {
    GameTestHelpers.runIfEnabled(
        helper, HostEntityGameTestCases::apiControlsHostBlockEntityProfileAndDisplay);
  }

  @GameTest(structure = Constants.MOD_ID + ":gametest.3x3x3")
  public void apiPlaysAnimationsOnHostBlockEntity(GameTestHelper helper) {
    GameTestHelpers.runIfEnabled(
        helper, HostEntityGameTestCases::apiPlaysAnimationsOnHostBlockEntity);
  }

  @GameTest(structure = Constants.MOD_ID + ":gametest.3x3x3")
  public void invalidNbtResourceLocationsFallBackSafely(GameTestHelper helper) {
    GameTestHelpers.runIfEnabled(
        helper, HostEntityGameTestCases::invalidNbtResourceLocationsFallBackSafely);
  }

  @GameTest(structure = Constants.MOD_ID + ":gametest.3x3x3")
  public void dimensionRefreshUsesProfileDimensions(GameTestHelper helper) {
    GameTestHelpers.runIfEnabled(
        helper, HostEntityGameTestCases::dimensionRefreshUsesProfileDimensions);
  }

  @GameTest(structure = Constants.MOD_ID + ":gametest.3x3x3")
  public void attributesAreAppliedFromProfile(GameTestHelper helper) {
    GameTestHelpers.runIfEnabled(helper, HostEntityGameTestCases::attributesAreAppliedFromProfile);
  }

  @GameTest(structure = Constants.MOD_ID + ":gametest.3x3x3")
  public void blockEntityCanBePlacedAndInitialized(GameTestHelper helper) {
    GameTestHelpers.runIfEnabled(
        helper, HostEntityGameTestCases::blockEntityCanBePlacedAndInitialized);
  }

  @GameTest(structure = Constants.MOD_ID + ":gametest.3x3x3")
  public void entitySpawnItemSpawnsHostEntity(GameTestHelper helper) {
    GameTestHelpers.runIfEnabled(helper, HostEntityGameTestCases::entitySpawnItemSpawnsHostEntity);
  }

  @GameTest(structure = Constants.MOD_ID + ":gametest.3x3x3")
  public void blockSpawnItemPlacesHostBlock(GameTestHelper helper) {
    GameTestHelpers.runIfEnabled(helper, HostEntityGameTestCases::blockSpawnItemPlacesHostBlock);
  }

  @GameTest(structure = Constants.MOD_ID + ":gametest.3x3x3")
  public void blockSpawnItemTurnsHostBlockTowardsThePlayer(GameTestHelper helper) {
    GameTestHelpers.runIfEnabled(
        helper, HostEntityGameTestCases::blockSpawnItemTurnsHostBlockTowardsThePlayer);
  }

  @GameTest(structure = Constants.MOD_ID + ":gametest.3x3x3")
  public void placeBlockCommandTurnsHostBlockTowardsTheExecutingEntity(GameTestHelper helper) {
    GameTestHelpers.runIfEnabled(
        helper,
        HostEntityGameTestCases::placeBlockCommandTurnsHostBlockTowardsTheExecutingEntity);
  }

  @GameTest(structure = Constants.MOD_ID + ":gametest.3x3x3")
  public void profileCommandsReportLoadedProfiles(GameTestHelper helper) {
    GameTestHelpers.runIfEnabled(helper, CommandGameTestCases::profileCommandsReportLoadedProfiles);
  }

  @GameTest(structure = Constants.MOD_ID + ":gametest.3x3x3")
  public void summonCommandSpawnsHostEntity(GameTestHelper helper) {
    GameTestHelpers.runIfEnabled(helper, CommandGameTestCases::summonCommandSpawnsHostEntity);
  }

  @GameTest(structure = Constants.MOD_ID + ":gametest.3x3x3")
  public void placeBlockCommandPlacesHostBlockEntity(GameTestHelper helper) {
    GameTestHelpers.runIfEnabled(
        helper, CommandGameTestCases::placeBlockCommandPlacesHostBlockEntity);
  }

  @GameTest(structure = Constants.MOD_ID + ":gametest.3x3x3")
  public void shoulderCommandSeatsHostEntityOnPlayer(GameTestHelper helper) {
    GameTestHelpers.runIfEnabled(
        helper, CommandGameTestCases::shoulderCommandSeatsHostEntityOnPlayer);
  }

  @GameTest(structure = Constants.MOD_ID + ":gametest.3x3x3")
  public void shoulderOffsetCommandTravelsOntoShoulder(GameTestHelper helper) {
    GameTestHelpers.runIfEnabled(
        helper, CommandGameTestCases::shoulderOffsetCommandTravelsOntoShoulder);
  }

  @GameTest(structure = Constants.MOD_ID + ":gametest.3x3x3")
  public void behaviorCommandFreezesAndRestoresHostEntity(GameTestHelper helper) {
    GameTestHelpers.runIfEnabled(
        helper, CommandGameTestCases::behaviorCommandFreezesAndRestoresHostEntity);
  }

  @GameTest(structure = Constants.MOD_ID + ":gametest.3x3x3")
  public void animationCommandsControlHostEntity(GameTestHelper helper) {
    GameTestHelpers.runIfEnabled(helper, CommandGameTestCases::animationCommandsControlHostEntity);
  }

  @GameTest(structure = Constants.MOD_ID + ":gametest.3x3x3")
  public void animationCommandsControlHostBlockEntity(GameTestHelper helper) {
    GameTestHelpers.runIfEnabled(
        helper, CommandGameTestCases::animationCommandsControlHostBlockEntity);
  }

  @GameTest(structure = Constants.MOD_ID + ":gametest.3x3x3")
  public void textureCommandsControlHostEntity(GameTestHelper helper) {
    GameTestHelpers.runIfEnabled(helper, CommandGameTestCases::textureCommandsControlHostEntity);
  }

  @GameTest(structure = Constants.MOD_ID + ":gametest.3x3x3")
  public void textureCommandsControlHostBlockEntity(GameTestHelper helper) {
    GameTestHelpers.runIfEnabled(
        helper, CommandGameTestCases::textureCommandsControlHostBlockEntity);
  }

  @GameTest(structure = Constants.MOD_ID + ":gametest.3x3x3")
  public void displayCommandsControlHostEntity(GameTestHelper helper) {
    GameTestHelpers.runIfEnabled(helper, CommandGameTestCases::displayCommandsControlHostEntity);
  }

  @GameTest(structure = Constants.MOD_ID + ":gametest.3x3x3")
  public void displayCommandsControlHostBlockEntity(GameTestHelper helper) {
    GameTestHelpers.runIfEnabled(
        helper, CommandGameTestCases::displayCommandsControlHostBlockEntity);
  }
}
