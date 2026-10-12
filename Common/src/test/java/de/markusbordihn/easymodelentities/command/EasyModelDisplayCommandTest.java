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

package de.markusbordihn.easymodelentities.command;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class EasyModelDisplayCommandTest {

  private static ParseResults<CommandSourceStack> parse(String command) {
    CommandDispatcher<CommandSourceStack> dispatcher = new CommandDispatcher<>();
    dispatcher.register(
        Commands.literal("easy_model_entities").then(EasyModelDisplayCommand.register()));
    CommandSourceStack source = mock(CommandSourceStack.class);
    when(source.hasPermission(anyInt())).thenReturn(true);

    return dispatcher.parse(command, source);
  }

  private static void assertParses(String command) {
    ParseResults<CommandSourceStack> result = parse(command);

    assertFalse(result.getReader().canRead(), result.getReader().getRemaining());
    assertTrue(result.getExceptions().isEmpty(), result.getExceptions().toString());
  }

  private static void assertRejects(String command) {
    ParseResults<CommandSourceStack> result = parse(command);

    assertTrue(
        result.getReader().canRead() || !result.getExceptions().isEmpty(),
        "Accepted: " + command);
  }

  @Test
  @DisplayName("Opacity set, clear and get parse for entities and blocks")
  void parsesOpacityCommands() {
    assertParses("easy_model_entities display opacity set entity @e 40");
    assertParses("easy_model_entities display opacity set block 1 64 -3 100");
    assertParses("easy_model_entities display opacity clear entity @e");
    assertParses("easy_model_entities display opacity clear block 1 64 -3");
    assertParses("easy_model_entities display opacity get entity @e");
    assertParses("easy_model_entities display opacity get block 1 64 -3");
  }

  @Test
  @DisplayName("Light set, clear and get parse for entities and blocks")
  void parsesLightCommands() {
    assertParses("easy_model_entities display light set entity @e 15");
    assertParses("easy_model_entities display light set block 1 64 -3 0");
    assertParses("easy_model_entities display light clear entity @e");
    assertParses("easy_model_entities display light clear block 1 64 -3");
    assertParses("easy_model_entities display light get entity @e");
    assertParses("easy_model_entities display light get block 1 64 -3");
  }

  @Test
  @DisplayName("Opacity is a percentage and light a block light level, nothing outside")
  void rejectsValuesOutsideTheirRange() {
    assertRejects("easy_model_entities display opacity set entity @e 101");
    assertRejects("easy_model_entities display opacity set entity @e -1");
    assertRejects("easy_model_entities display light set entity @e 16");
    assertRejects("easy_model_entities display light set entity @e -1");
  }
}
