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

package de.markusbordihn.easymodelentities.renderprofile;

import de.markusbordihn.easymodelentities.registry.ModelResourcePaths;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
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

public final class ModelRenderProfileManager implements EasyModelRenderProfileService {

  private static final String JSON_EXTENSION = ".json";
  private final Map<ResourceLocation, EasyModelRenderProfile> renderProfilesById;

  public ModelRenderProfileManager(
      Map<ResourceLocation, EasyModelRenderProfile> renderProfilesById) {
    this.renderProfilesById =
        Collections.unmodifiableMap(
            new LinkedHashMap<>(Objects.requireNonNull(renderProfilesById, "renderProfilesById")));
  }

  public static ModelRenderProfileManager load(ResourceManager resourceManager) {
    Objects.requireNonNull(resourceManager, "resourceManager");
    Map<ResourceLocation, EasyModelRenderProfile> renderProfiles = new LinkedHashMap<>();
    Map<ResourceLocation, Resource> resources =
        resourceManager.listResources(
            ModelResourcePaths.RENDER_PROFILE_DIRECTORY,
            resourceLocation -> resourceLocation.getPath().endsWith(JSON_EXTENSION));

    for (Map.Entry<ResourceLocation, Resource> entry : resources.entrySet()) {
      ResourceLocation resourceLocation = entry.getKey();
      Optional<ResourceLocation> renderProfileId =
          renderProfileIdFromResourceLocation(resourceLocation);
      if (renderProfileId.isEmpty()) {
        continue;
      }

      EasyModelRenderProfile renderProfile =
          parseRenderProfile(renderProfileId.get(), entry.getValue());
      renderProfiles.put(
          renderProfileId.get(), validateClientAssets(renderProfile, resourceManager));
    }

    return new ModelRenderProfileManager(renderProfiles);
  }

  public static Optional<ResourceLocation> renderProfileIdFromResourceLocation(
      ResourceLocation resourceLocation) {
    Objects.requireNonNull(resourceLocation, "resourceLocation");
    String prefix = ModelResourcePaths.RENDER_PROFILE_DIRECTORY + "/";
    String path = resourceLocation.getPath();
    if (!path.startsWith(prefix) || !path.endsWith(JSON_EXTENSION)) {
      return Optional.empty();
    }

    String renderProfilePath =
        path.substring(prefix.length(), path.length() - JSON_EXTENSION.length());
    ResourceLocation renderProfileId =
        ResourceLocation.tryParse(resourceLocation.getNamespace() + ":" + renderProfilePath);
    return Optional.ofNullable(renderProfileId);
  }

  private static EasyModelRenderProfile parseRenderProfile(
      ResourceLocation renderProfileId, Resource resource) {
    try (InputStreamReader reader =
        new InputStreamReader(resource.open(), StandardCharsets.UTF_8)) {
      return ModelRenderProfileParser.parse(renderProfileId, reader);
    } catch (IOException exception) {
      return ModelRenderProfileParser.invalidFallback(
          renderProfileId,
          ModelRenderProfileStatus.INVALID_JSON,
          "json",
          "Could not read render profile JSON: " + exception.getMessage());
    }
  }

  private static EasyModelRenderProfile validateClientAssets(
      EasyModelRenderProfile renderProfile, ResourceManager resourceManager) {
    if (!renderProfile.isActive()) {
      return renderProfile;
    }

    List<ModelRenderProfileValidationIssue> issues =
        new ArrayList<>(renderProfile.validationIssues());
    ResourceLocation modelResourceLocation =
        ModelResourcePaths.modelResourceLocation(renderProfile.model());
    if (resourceManager.getResource(modelResourceLocation).isEmpty()) {
      issues.add(
          new ModelRenderProfileValidationIssue(
              ModelRenderProfileStatus.MISSING_MODEL,
              "model",
              "Missing model asset " + modelResourceLocation + "."));
    }
    ResourceLocation textureResourceLocation =
        ModelResourcePaths.textureResourceLocation(renderProfile.texture());
    if (resourceManager.getResource(textureResourceLocation).isEmpty()) {
      issues.add(
          new ModelRenderProfileValidationIssue(
              ModelRenderProfileStatus.MISSING_TEXTURE,
              "texture",
              "Missing texture asset " + textureResourceLocation + "."));
    }

    return renderProfile.withValidationIssues(issues);
  }

  @Override
  public boolean hasRenderProfile(ResourceLocation renderProfileId) {
    return this.renderProfilesById.containsKey(renderProfileId);
  }

  @Override
  public Optional<EasyModelRenderProfile> getRenderProfile(ResourceLocation renderProfileId) {
    return Optional.ofNullable(this.renderProfilesById.get(renderProfileId));
  }

  @Override
  public Collection<EasyModelRenderProfile> getRenderProfiles() {
    return this.renderProfilesById.values();
  }

  @Override
  public Collection<ResourceLocation> getRenderProfileIds() {
    return this.renderProfilesById.keySet();
  }

  @Override
  public Collection<ModelRenderProfileValidationIssue> getValidationIssues(
      ResourceLocation renderProfileId) {
    return getRenderProfile(renderProfileId)
        .map(EasyModelRenderProfile::validationIssues)
        .orElseGet(List::of);
  }

  @Override
  public boolean isActive(ResourceLocation renderProfileId) {
    return getRenderProfile(renderProfileId).map(EasyModelRenderProfile::isActive).orElse(false);
  }
}
