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

package de.markusbordihn.easymodelentities.spawn;

import de.markusbordihn.easymodelentities.data.profile.EasyModelEntityProfile;
import de.markusbordihn.easymodelentities.data.profile.ModelType;
import de.markusbordihn.easymodelentities.registry.EasyModelServices;
import de.markusbordihn.easymodelentities.registry.ModelBlockEntityTypeIds;
import de.markusbordihn.easymodelentities.registry.ModelBlockIds;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;

public final class EasyModelSpawnSupport {

  private EasyModelSpawnSupport() {}

  public static Optional<ResourceLocation> hostBlockId(EasyModelEntityProfile profile) {
    if (profile.modelType() != ModelType.BLOCK_ENTITY) {
      return Optional.empty();
    }

    ResourceLocation blockEntityType = profile.hostBlockEntityType();
    if (ModelBlockEntityTypeIds.STATIC_BLOCK_ENTITY.equals(blockEntityType)) {
      return Optional.of(ModelBlockIds.STATIC_BLOCK);
    }
    if (ModelBlockEntityTypeIds.TICKING_BLOCK_ENTITY.equals(blockEntityType)) {
      return Optional.of(ModelBlockIds.TICKING_BLOCK);
    }
    if (ModelBlockEntityTypeIds.ANIMATED_BLOCK_ENTITY.equals(blockEntityType)) {
      return Optional.of(ModelBlockIds.ANIMATED_BLOCK);
    }
    if (ModelBlockEntityTypeIds.ANIMATED_RANDOMLY_BLOCK_ENTITY.equals(blockEntityType)) {
      return Optional.of(ModelBlockIds.ANIMATED_RANDOMLY_BLOCK);
    }

    return Optional.empty();
  }

  public static Optional<String> entitySpawnRejection(ResourceLocation profileId) {
    Optional<EasyModelEntityProfile> profile =
        EasyModelServices.profileService().getProfile(profileId);
    if (profile.isEmpty()) {
      return Optional.of("Unknown Easy Model Entities profile: " + profileId);
    }
    if (!profile.get().isActive()) {
      return Optional.of(
          "Cannot summon invalid Easy Model Entities profile "
              + profileId
              + ": "
              + profile.get().status().name()
              + ".");
    }
    if (profile.get().modelType() != ModelType.ENTITY) {
      return Optional.of("Cannot summon non-entity Easy Model Entities profile " + profileId + ".");
    }

    return Optional.empty();
  }

  public static Optional<String> blockPlacementRejection(ResourceLocation profileId) {
    Optional<EasyModelEntityProfile> profile =
        EasyModelServices.profileService().getProfile(profileId);
    if (profile.isEmpty()) {
      return Optional.of("Unknown Easy Model Entities profile: " + profileId);
    }
    if (!profile.get().isActive()) {
      return Optional.of(
          "Cannot place invalid Easy Model Entities profile "
              + profileId
              + ": "
              + profile.get().status().name()
              + ".");
    }
    if (profile.get().modelType() != ModelType.BLOCK_ENTITY) {
      return Optional.of(
          "Cannot place non-block-entity Easy Model Entities profile " + profileId + ".");
    }
    if (hostBlockId(profile.get()).isEmpty()) {
      return Optional.of("Could not resolve Easy Model Entities host block for " + profileId + ".");
    }

    return Optional.empty();
  }
}
