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

class EasyModelBehaviorCommandTest {

  private static ParseResults<CommandSourceStack> parse(String command) {
    CommandDispatcher<CommandSourceStack> dispatcher = new CommandDispatcher<>();
    dispatcher.register(Commands.literal("easy_model_entities").then(EasyModelBehaviorCommand.register()));
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
  @DisplayName("Behavior set and get parse for entities, never for blocks")
  void parsesBehaviorCommands() {
    assertParses("easy_model_entities behavior set entity @e frozen");
    assertParses("easy_model_entities behavior set entity @e auto");
    assertParses("easy_model_entities behavior get entity @e");
    assertRejects("easy_model_entities behavior get block 1 64 -3");
  }
}
