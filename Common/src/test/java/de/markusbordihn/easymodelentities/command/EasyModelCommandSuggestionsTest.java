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

import com.mojang.brigadier.context.StringRange;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import java.util.List;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class EasyModelCommandSuggestionsTest {

  @Test
  @DisplayName("Player selectors and player names never match a host entity and are not suggested")
  void dropsPlayerOnlyTargetsFromSuggestions() {
    StringRange range = StringRange.between(0, 2);
    Suggestions suggestions =
        new Suggestions(
            range,
            List.of(
                new Suggestion(range, "@a"),
                new Suggestion(range, "@e"),
                new Suggestion(range, "@p"),
                new Suggestion(range, "@r"),
                new Suggestion(range, "@s"),
                new Suggestion(range, "Steve")));

    assertEquals(
        List.of("@e", "@s"),
        EasyModelCommandSuggestions.withoutPlayerTargets(suggestions, List.of("Steve"))
            .getList()
            .stream()
            .map(Suggestion::getText)
            .toList());
  }

  @Test
  void prefersEntityDirectlyUnderCrosshair() {
    AABB directTarget = new AABB(-0.25, -0.25, 2.0, 0.25, 0.25, 2.5);
    AABB nearbyTarget = new AABB(0.5, -0.25, 1.0, 0.75, 0.25, 1.5);

    assertEquals(
        directTarget,
        EasyModelCommandSuggestions.findCrosshairTarget(
                Vec3.ZERO,
                new Vec3(0.0, 0.0, 5.0),
                List.of(nearbyTarget, directTarget),
                bounds -> bounds)
            .orElseThrow());
  }

  @Test
  void fallsBackToEntityWithinOneBlockOfCrosshair() {
    AABB nearbyTarget = new AABB(0.5, -0.25, 2.0, 0.75, 0.25, 2.5);

    assertEquals(
        nearbyTarget,
        EasyModelCommandSuggestions.findCrosshairTarget(
                Vec3.ZERO, new Vec3(0.0, 0.0, 5.0), List.of(nearbyTarget), bounds -> bounds)
            .orElseThrow());
  }
}
