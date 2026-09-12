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

import static java.util.stream.Collectors.joining;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import de.markusbordihn.easymodelentities.api.data.EasyModelAnimation;
import java.util.List;
import java.util.stream.IntStream;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class EasyModelAnimationCommandTest {

  private static void assertParses(String command) {
    CommandDispatcher<CommandSourceStack> dispatcher = new CommandDispatcher<>();
    dispatcher.register(
        Commands.literal("easy_model_entities").then(EasyModelAnimationCommand.register()));
    CommandSourceStack source = mock(CommandSourceStack.class);
    when(source.permissions()).thenReturn(permission -> true);

    ParseResults<CommandSourceStack> result = dispatcher.parse(command, source);

    assertFalse(result.getReader().canRead(), result.getReader().getRemaining());
    assertTrue(result.getExceptions().isEmpty(), result.getExceptions().toString());
  }

  private static String clipList(int count) {
    return IntStream.range(0, count).mapToObj(index -> "clip" + index).collect(joining(","));
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

  @Test
  void parsesRandomPlaybackCommands() {
    assertParses("easy_model_entities animation play entity @e random \"idle_2,idle_3\"");
    assertParses(
        "easy_model_entities animation play entity @e random \"idle_2,idle_3\" loop after_current 5");
    assertParses("easy_model_entities animation play block 1 64 -3 random \"wave,bow\" once");
    assertParses("easy_model_entities animation play entity @e random idle_2");
  }

  @Test
  void parsesSequencePlaybackCommands() {
    assertParses("easy_model_entities animation play entity @e sequence \"wake,ready\"");
    assertParses(
        "easy_model_entities animation play entity @e sequence \"wake,ready\" fallback idle");
    assertParses("easy_model_entities animation play block 1 64 -3 sequence \"wake,ready\"");
  }

  @Test
  @DisplayName("A sequence keeps its order and repetitions and drops the automatic state")
  void parsesSequenceClipLists() {
    assertEquals(
        List.of(
            EasyModelAnimation.named("idle_2"),
            EasyModelAnimation.named("idle_3"),
            EasyModelAnimation.named("idle_2"),
            EasyModelAnimation.ATTACK),
        EasyModelAnimationCommand.parseAnimationSequence(" idle_2 , idle_3 ,idle_2,, attack "));
    assertEquals(
        List.of(EasyModelAnimation.IDLE),
        EasyModelAnimationCommand.parseAnimationSequence("auto,idle"));
    assertEquals(16, EasyModelAnimationCommand.parseAnimationSequence(clipList(20)).size());
    assertTrue(EasyModelAnimationCommand.parseAnimationSequence(" , ").isEmpty());
    assertTrue(EasyModelAnimationCommand.parseAnimationSequence(null).isEmpty());
  }

  @Test
  @DisplayName("A clip list is trimmed, deduplicated and capped")
  void parsesClipLists() {
    assertEquals(
        List.of(
            EasyModelAnimation.named("idle_2"),
            EasyModelAnimation.named("idle_3"),
            EasyModelAnimation.ATTACK),
        EasyModelAnimationCommand.parseAnimations(" idle_2 , idle_3 ,idle_2,, attack "));
    assertEquals(16, EasyModelAnimationCommand.parseAnimations(clipList(20)).size());
    assertTrue(EasyModelAnimationCommand.parseAnimations(" , ").isEmpty());
    assertTrue(EasyModelAnimationCommand.parseAnimations(null).isEmpty());
  }
}
