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

class EasyModelTextureCommandTest {

  private static void assertParses(String command) {
    CommandDispatcher<CommandSourceStack> dispatcher = new CommandDispatcher<>();
    dispatcher.register(
        Commands.literal("easy_model_entities").then(EasyModelTextureCommand.register()));
    CommandSourceStack source = mock(CommandSourceStack.class);
    when(source.hasPermission(anyInt())).thenReturn(true);

    ParseResults<CommandSourceStack> result = dispatcher.parse(command, source);

    assertFalse(result.getReader().canRead(), result.getReader().getRemaining());
    assertTrue(result.getExceptions().isEmpty(), result.getExceptions().toString());
  }

  @Test
  void parsesEntityAndBlockSetCommands() {
    assertParses("easy_model_entities texture set entity @e default example:textures/entity/a.png");
    assertParses(
        "easy_model_entities texture set entity @e slot screen example:textures/entity/b.png");
    assertParses(
        "easy_model_entities texture set block 1 64 -3 default example:textures/block/a.png");
    assertParses(
        "easy_model_entities texture set block 1 64 -3 slot screen example:textures/block/b.png");
  }

  @Test
  @DisplayName("A blend can be set together with the texture or on its own")
  void parsesBlendCommands() {
    assertParses(
        "easy_model_entities texture set entity @e default example:textures/entity/a.png"
            + " translucent");
    assertParses(
        "easy_model_entities texture set entity @e slot screen example:textures/entity/b.png"
            + " cutout");
    assertParses(
        "easy_model_entities texture set block 1 64 -3 slot screen example:textures/block/b.png"
            + " translucent");
    assertParses("easy_model_entities texture blend entity @e default translucent");
    assertParses("easy_model_entities texture blend entity @e slot screen translucent");
    assertParses("easy_model_entities texture blend block 1 64 -3 default cutout");
    assertParses("easy_model_entities texture blend block 1 64 -3 slot screen cutout");
  }

  @Test
  @DisplayName("Clearing works for all slots, the default slot and a single named slot")
  void parsesEntityAndBlockClearCommands() {
    assertParses("easy_model_entities texture clear entity @e");
    assertParses("easy_model_entities texture clear entity @e default");
    assertParses("easy_model_entities texture clear entity @e slot screen");
    assertParses("easy_model_entities texture clear block 1 64 -3");
    assertParses("easy_model_entities texture clear block 1 64 -3 default");
    assertParses("easy_model_entities texture clear block 1 64 -3 slot screen");
  }

  @Test
  void parsesEntityAndBlockGetCommands() {
    assertParses("easy_model_entities texture get entity @e");
    assertParses("easy_model_entities texture get block 1 64 -3");
  }
}
