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

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Predicate;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

final class EasyModelCommandSuggestions {

  static final double ENTITY_SELECTION_DISTANCE = 5.0;
  static final double NEAR_CROSSHAIR_RADIUS = 1.0;

  private static final Set<String> PLAYER_ONLY_SELECTORS = Set.of("@a", "@p", "@r");

  private EasyModelCommandSuggestions() {}

  static CompletableFuture<Suggestions> suggestEntities(
      CommandContext<CommandSourceStack> context,
      SuggestionsBuilder builder,
      Predicate<Entity> predicate) {
    CommandSourceStack source = context.getSource();
    CompletableFuture<Suggestions> selectorSuggestions =
        EntityArgument.entities()
            .listSuggestions(context, builder)
            .thenApply(
                suggestions -> withoutPlayerTargets(suggestions, source.getOnlinePlayerNames()));
    Optional<Entity> crosshairEntity = findCrosshairEntity(source, predicate);
    if (crosshairEntity.isEmpty()) {
      return selectorSuggestions;
    }

    SuggestionsBuilder crosshairBuilder = builder.restart();
    SharedSuggestionProvider.suggest(
        List.of(crosshairEntity.get().getStringUUID()), crosshairBuilder);
    return selectorSuggestions.thenApply(
        suggestions ->
            Suggestions.merge(builder.getInput(), List.of(crosshairBuilder.build(), suggestions)));
  }

  static Suggestions withoutPlayerTargets(
      Suggestions suggestions, Collection<String> onlinePlayerNames) {
    List<Suggestion> entityTargets =
        suggestions.getList().stream()
            .filter(suggestion -> !PLAYER_ONLY_SELECTORS.contains(suggestion.getText()))
            .filter(suggestion -> !onlinePlayerNames.contains(suggestion.getText()))
            .toList();

    return entityTargets.size() == suggestions.getList().size()
        ? suggestions
        : new Suggestions(suggestions.getRange(), entityTargets);
  }

  static Optional<Entity> findCrosshairEntity(
      CommandSourceStack source, Predicate<Entity> predicate) {
    Entity viewer = source.getEntity();
    if (viewer == null) {
      return Optional.empty();
    }

    Vec3 start = viewer.getEyePosition();
    Vec3 viewVector = viewer.getViewVector(1.0f).scale(ENTITY_SELECTION_DISTANCE);
    Vec3 end = start.add(viewVector);
    HitResult blockHit = viewer.pick(ENTITY_SELECTION_DISTANCE, 1.0f, false);
    if (blockHit.getType() != HitResult.Type.MISS) {
      end = blockHit.getLocation();
    }

    AABB searchArea =
        viewer.getBoundingBox().expandTowards(viewVector).inflate(NEAR_CROSSHAIR_RADIUS);
    Collection<Entity> candidates =
        source
            .getLevel()
            .getEntities(
                viewer, searchArea, entity -> entity.isPickable() && predicate.test(entity));
    return findCrosshairEntity(start, end, candidates);
  }

  static Optional<Entity> findCrosshairEntity(
      Vec3 start, Vec3 end, Collection<? extends Entity> candidates) {
    return findCrosshairTarget(
        start, end, candidates, entity -> entity.getBoundingBox().inflate(entity.getPickRadius()));
  }

  static <T> Optional<T> findCrosshairTarget(
      Vec3 start, Vec3 end, Collection<? extends T> candidates, Function<T, AABB> boundsFunction) {
    Optional<T> directTarget = findClosestIntersection(start, end, candidates, boundsFunction, 0.0);
    return directTarget.isPresent()
        ? directTarget
        : findClosestIntersection(start, end, candidates, boundsFunction, NEAR_CROSSHAIR_RADIUS);
  }

  private static <T> Optional<T> findClosestIntersection(
      Vec3 start,
      Vec3 end,
      Collection<? extends T> candidates,
      Function<T, AABB> boundsFunction,
      double inflation) {
    return candidates.stream()
        .map(
            candidate ->
                intersection(start, end, candidate, boundsFunction.apply(candidate), inflation))
        .flatMap(Optional::stream)
        .min(Comparator.comparingDouble(TargetIntersection::distanceSquared))
        .map(TargetIntersection::target);
  }

  private static <T> Optional<TargetIntersection<T>> intersection(
      Vec3 start, Vec3 end, T target, AABB targetBounds, double inflation) {
    AABB bounds = targetBounds.inflate(inflation);
    if (bounds.contains(start)) {
      return Optional.of(new TargetIntersection<>(target, 0.0));
    }
    return bounds
        .clip(start, end)
        .map(location -> new TargetIntersection<>(target, start.distanceToSqr(location)));
  }

  private record TargetIntersection<T>(T target, double distanceSquared) {}
}
