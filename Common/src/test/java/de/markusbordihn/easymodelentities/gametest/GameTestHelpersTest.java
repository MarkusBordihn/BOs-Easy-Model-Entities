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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import net.minecraft.gametest.framework.TestFunction;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class GameTestHelpersTest {

  @AfterEach
  void clearEnabledProperty() {
    System.clearProperty(GameTestHelpers.ENABLED_PROPERTY);
  }

  @Test
  @DisplayName("Registers no game tests unless explicitly enabled")
  void registersNothingWithoutOptIn() {
    assertTrue(GameTestHelpers.testFunctions(HostEntityGameTestCases.class).isEmpty());
  }

  @Test
  @DisplayName("Registers every public static test case once enabled")
  void registersTestCasesWhenEnabled() {
    System.setProperty(GameTestHelpers.ENABLED_PROPERTY, "true");

    List<TestFunction> testFunctions =
        GameTestHelpers.testFunctions(HostEntityGameTestCases.class);

    assertTrue(
        testFunctions.stream()
            .anyMatch(
                testFunction ->
                    testFunction
                        .testName()
                        .equals("hostentitygametestcases.groundentitycanbecreated")));
    assertTrue(
        testFunctions.stream()
            .allMatch(
                testFunction ->
                    testFunction.structureName().equals("easy_model_entities:gametest.3x3x3")));
    assertEquals(
        testFunctions.size(),
        testFunctions.stream().map(TestFunction::testName).distinct().count());
  }
}
