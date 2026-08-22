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
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraftforge.gametest.GameTestHolder;

@SuppressWarnings("unused")
@GameTestHolder(Constants.MOD_ID)
public class HostEntityGameTest {

  @GameTest(template = Constants.MOD_ID + ":gametest.3x3x3")
  public void groundEntityCanBeCreated(GameTestHelper helper) {
    HostEntityGameTestCases.groundEntityCanBeCreated(helper);
  }

  @GameTest(template = Constants.MOD_ID + ":gametest.3x3x3")
  public void staticEntityCanBeCreated(GameTestHelper helper) {
    HostEntityGameTestCases.staticEntityCanBeCreated(helper);
  }

  @GameTest(template = Constants.MOD_ID + ":gametest.3x3x3")
  public void frozenBehaviorStopsHostEntityMovement(GameTestHelper helper) {
    HostEntityGameTestCases.frozenBehaviorStopsHostEntityMovement(helper);
  }

  @GameTest(template = Constants.MOD_ID + ":gametest.3x3x3")
  public void missingAndInvalidProfilesReturnEmpty(GameTestHelper helper) {
    HostEntityGameTestCases.missingAndInvalidProfilesReturnEmpty(helper);
  }

  @GameTest(template = Constants.MOD_ID + ":gametest.3x3x3")
  public void nbtPreservesRuntimeContractData(GameTestHelper helper) {
    HostEntityGameTestCases.nbtPreservesRuntimeContractData(helper);
  }

  @GameTest(template = Constants.MOD_ID + ":gametest.3x3x3")
  public void nbtPreservesTextureSetting(GameTestHelper helper) {
    HostEntityGameTestCases.nbtPreservesTextureSetting(helper);
  }

  @GameTest(template = Constants.MOD_ID + ":gametest.3x3x3")
  public void nbtPreservesDisplaySettings(GameTestHelper helper) {
    HostEntityGameTestCases.nbtPreservesDisplaySettings(helper);
  }

  @GameTest(template = Constants.MOD_ID + ":gametest.3x3x3")
  public void invalidNbtResourceLocationsFallBackSafely(GameTestHelper helper) {
    HostEntityGameTestCases.invalidNbtResourceLocationsFallBackSafely(helper);
  }

  @GameTest(template = Constants.MOD_ID + ":gametest.3x3x3")
  public void dimensionRefreshUsesProfileDimensions(GameTestHelper helper) {
    HostEntityGameTestCases.dimensionRefreshUsesProfileDimensions(helper);
  }

  @GameTest(template = Constants.MOD_ID + ":gametest.3x3x3")
  public void blockEntityCanBePlacedAndInitialized(GameTestHelper helper) {
    HostEntityGameTestCases.blockEntityCanBePlacedAndInitialized(helper);
  }

  @GameTest(template = Constants.MOD_ID + ":gametest.3x3x3")
  public void attributesAreAppliedFromProfile(GameTestHelper helper) {
    HostEntityGameTestCases.attributesAreAppliedFromProfile(helper);
  }

  @GameTest(template = Constants.MOD_ID + ":gametest.3x3x3")
  public void entitySpawnItemSpawnsHostEntity(GameTestHelper helper) {
    HostEntityGameTestCases.entitySpawnItemSpawnsHostEntity(helper);
  }

  @GameTest(template = Constants.MOD_ID + ":gametest.3x3x3")
  public void blockSpawnItemPlacesHostBlock(GameTestHelper helper) {
    HostEntityGameTestCases.blockSpawnItemPlacesHostBlock(helper);
  }
}
