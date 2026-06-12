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

package de.markusbordihn.easymodelentities.profile;

import de.markusbordihn.easymodelentities.registry.ModelResourcePaths;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

public final class EasyModelProfileManager implements EasyModelProfileService {

  private static final String JSON_EXTENSION = ".json";
  private final Map<ResourceLocation, EasyModelEntityProfile> profilesById;

  public EasyModelProfileManager(Map<ResourceLocation, EasyModelEntityProfile> profilesById) {
    this.profilesById =
        Collections.unmodifiableMap(
            new LinkedHashMap<>(Objects.requireNonNull(profilesById, "profilesById")));
  }

  public static EasyModelProfileManager load(ResourceManager resourceManager) {
    Objects.requireNonNull(resourceManager, "resourceManager");
    Map<ResourceLocation, EasyModelEntityProfile> profiles = new LinkedHashMap<>();
    Map<ResourceLocation, Resource> resources =
        resourceManager.listResources(
            ModelResourcePaths.SERVER_PROFILE_DIRECTORY,
            resourceLocation -> resourceLocation.getPath().endsWith(JSON_EXTENSION));

    for (Map.Entry<ResourceLocation, Resource> entry : resources.entrySet()) {
      ResourceLocation resourceLocation = entry.getKey();
      Optional<ResourceLocation> profileId = profileIdFromResourceLocation(resourceLocation);
      if (profileId.isEmpty()) {
        continue;
      }

      profiles.put(profileId.get(), parseProfile(profileId.get(), entry.getValue()));
    }

    return new EasyModelProfileManager(profiles);
  }

  public static Optional<ResourceLocation> profileIdFromResourceLocation(
      ResourceLocation resourceLocation) {
    Objects.requireNonNull(resourceLocation, "resourceLocation");
    String prefix = ModelResourcePaths.SERVER_PROFILE_DIRECTORY + "/";
    String path = resourceLocation.getPath();
    if (!path.startsWith(prefix) || !path.endsWith(JSON_EXTENSION)) {
      return Optional.empty();
    }

    String profilePath = path.substring(prefix.length(), path.length() - JSON_EXTENSION.length());
    ResourceLocation profileId =
        ResourceLocation.tryParse(resourceLocation.getNamespace() + ":" + profilePath);
    return Optional.ofNullable(profileId);
  }

  private static EasyModelEntityProfile parseProfile(
      ResourceLocation profileId, Resource resource) {
    try (InputStreamReader reader =
        new InputStreamReader(resource.open(), StandardCharsets.UTF_8)) {
      return EasyModelProfileParser.parse(profileId, reader);
    } catch (IOException exception) {
      return EasyModelProfileParser.invalidFallback(
          profileId,
          ModelProfileStatus.INVALID_JSON,
          "json",
          "Could not read server profile JSON: " + exception.getMessage());
    }
  }

  @Override
  public boolean hasProfile(ResourceLocation profileId) {
    return this.profilesById.containsKey(profileId);
  }

  @Override
  public Optional<EasyModelEntityProfile> getProfile(ResourceLocation profileId) {
    return Optional.ofNullable(this.profilesById.get(profileId));
  }

  @Override
  public Collection<ResourceLocation> getProfileIds() {
    return this.profilesById.keySet();
  }

  @Override
  public Collection<EasyModelEntityProfile> getProfiles() {
    return this.profilesById.values();
  }

  @Override
  public Collection<ModelProfileValidationIssue> getValidationIssues(ResourceLocation profileId) {
    return getProfile(profileId).map(EasyModelEntityProfile::validationIssues).orElseGet(List::of);
  }

  @Override
  public boolean isActive(ResourceLocation profileId) {
    return getProfile(profileId).map(EasyModelEntityProfile::isActive).orElse(false);
  }
}
