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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import de.markusbordihn.easymodelentities.api.data.EasyModelAnimation;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import org.junit.jupiter.api.Test;

class EasyModelAnimationCommandTest {

  private static void assertParses(String command) {
    CommandDispatcher<CommandSourceStack> dispatcher = new CommandDispatcher<>();
    dispatcher.register(
        Commands.literal("easy_model_entities").then(EasyModelAnimationCommand.register()));
    CommandSourceStack source = mock(CommandSourceStack.class);
    when(source.hasPermission(anyInt())).thenReturn(true);

    ParseResults<CommandSourceStack> result = dispatcher.parse(command, source);

    assertFalse(result.getReader().canRead(), result.getReader().getRemaining());
    assertTrue(result.getExceptions().isEmpty(), result.getExceptions().toString());
  }

  @Test
  void parsesEntityAndBlockPlaybackCommands() {
    assertParses("easy_model_entities animation play entity @e wave repeat 3 after_current 5 40");
    assertParses("easy_model_entities animation play block 1 64 -3 idle loop immediate 5 0");
    assertParses("easy_model_entities animation stop entity @e after_current 5");
    assertParses("easy_model_entities animation restart block 1 64 -3");
    assertParses("easy_model_entities animation set entity @e attack");
  }

  @Test
  void normalizesStandardAndCustomAnimationNames() {
    assertEquals(
        EasyModelAnimation.ATTACK,
        EasyModelAnimationCommand.parseAnimation("ATTACK").orElseThrow());
    assertEquals(
        EasyModelAnimation.named("wave"),
        EasyModelAnimationCommand.parseAnimation("wave").orElseThrow());
    assertEquals(
        EasyModelAnimation.named("wave"),
        EasyModelAnimationCommand.parseAnimation("named:wave").orElseThrow());
    assertTrue(EasyModelAnimationCommand.parseAnimation("named:").isEmpty());
  }
}
