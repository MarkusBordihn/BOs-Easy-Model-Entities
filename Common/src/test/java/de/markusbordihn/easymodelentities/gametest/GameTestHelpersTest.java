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

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.Map;
import java.util.function.Consumer;
import net.minecraft.gametest.framework.GameTestHelper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class GameTestHelpersTest {

  @AfterEach
  void clearEnabledProperty() {
    System.clearProperty(GameTestHelpers.ENABLED_PROPERTY);
  }

  @Test
  @DisplayName("Passes game tests without running them unless explicitly enabled")
  void passesWithoutOptIn() {
    GameTestHelper helper = mock(GameTestHelper.class);

    GameTestHelpers.testFunctions(HostEntityGameTestCases.class)
        .get("ground_entity_can_be_created")
        .accept(helper);

    verify(helper).succeed();
    verifyNoMoreInteractions(helper);
  }

  @Test
  @DisplayName("Registers every public static test case under its snake case name")
  void registersTestCasesBySnakeCaseName() {
    Map<String, Consumer<GameTestHelper>> testFunctions =
        GameTestHelpers.testFunctions(HostEntityGameTestCases.class);

    assertTrue(testFunctions.containsKey("ground_entity_can_be_created"));
    assertTrue(testFunctions.containsKey("nbt_preserves_texture_setting"));
    assertTrue(
        GameTestHelpers.testFunctions(CommandGameTestCases.class)
            .containsKey("profile_commands_report_loaded_profiles"));
  }
}
