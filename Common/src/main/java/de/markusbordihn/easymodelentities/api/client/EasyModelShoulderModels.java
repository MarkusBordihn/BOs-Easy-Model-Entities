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

package de.markusbordihn.easymodelentities.api.client;

import de.markusbordihn.easymodelentities.api.data.EasyModelAnimation;
import de.markusbordihn.easymodelentities.api.data.EasyModelDisplaySettings;
import de.markusbordihn.easymodelentities.api.data.client.EasyModelEntityRenderOptions;
import de.markusbordihn.easymodelentities.api.data.client.EasyModelShoulderModel;
import de.markusbordihn.easymodelentities.registry.ModelEntityTypeIds;
import de.markusbordihn.easymodelentities.runtime.EasyModelHostPersistence;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

public final class EasyModelShoulderModels {

  private static final String ENTITY_TYPE_TAG = "id";
  private static final Map<ResourceLocation, EasyModelShoulderModelResolver> RESOLVERS =
      new ConcurrentHashMap<>();

  static {
    for (ResourceLocation hostEntityTypeId : ModelEntityTypeIds.SUPPORTED_HOST_ENTITY_TYPES) {
      RESOLVERS.put(hostEntityTypeId, EasyModelShoulderModels::resolveHostEntity);
    }
  }

  private EasyModelShoulderModels() {}

  public static void register(
      ResourceLocation entityTypeId, EasyModelShoulderModelResolver resolver) {
    RESOLVERS.put(
        Objects.requireNonNull(entityTypeId, "entityTypeId"),
        Objects.requireNonNull(resolver, "resolver"));
  }

  public static Optional<EasyModelShoulderModel> resolve(CompoundTag entityTag) {
    if (entityTag == null || entityTag.isEmpty()) {
      return Optional.empty();
    }

    ResourceLocation entityTypeId = ResourceLocation.tryParse(entityTag.getString(ENTITY_TYPE_TAG));
    if (entityTypeId == null) {
      return Optional.empty();
    }

    EasyModelShoulderModelResolver resolver = RESOLVERS.get(entityTypeId);
    return resolver == null ? Optional.empty() : resolver.resolve(entityTag);
  }

  private static Optional<EasyModelShoulderModel> resolveHostEntity(CompoundTag entityTag) {
    EasyModelHostPersistence.State state = EasyModelHostPersistence.read(entityTag);
    if (state.profileId() == null) {
      return Optional.empty();
    }

    EasyModelEntityRenderOptions options =
        EasyModelEntityRenderOptions.DEFAULT.withTextureSetting(state.texture());
    EasyModelAnimation animation = state.animation().animation();
    if (!EasyModelAnimation.AUTO.equals(animation)) {
      options = options.withAnimation(animation);
    }
    if (EasyModelDisplaySettings.hasOpacityOverride(state.opacity())) {
      options = options.withOpacity(state.opacity());
    }
    if (EasyModelDisplaySettings.hasLightLevelOverride(state.lightLevel())) {
      options = options.withLightLevel(state.lightLevel());
    }
    return Optional.of(
        new EasyModelShoulderModel(state.profileId(), options, state.shoulderOffset()));
  }
}
