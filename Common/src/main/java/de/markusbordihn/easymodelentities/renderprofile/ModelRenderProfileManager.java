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

import de.markusbordihn.easymodelentities.Constants;
import de.markusbordihn.easymodelentities.data.diagnostics.ModelResourceRejection;
import de.markusbordihn.easymodelentities.data.model.bake.ModelBakeResult;
import de.markusbordihn.easymodelentities.data.profile.ModelType;
import de.markusbordihn.easymodelentities.data.renderprofile.*;
import de.markusbordihn.easymodelentities.model.bake.EasyModelBakeService;
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
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class ModelRenderProfileManager implements EasyModelRenderProfileService {

  private static final Logger log = LogManager.getLogger(Constants.LOG_NAME);
  private static final String JSON_EXTENSION = ".json";
  private final Map<Identifier, EasyModelRenderProfile> renderProfilesById;
  private final List<ModelResourceRejection> rejectedResources;

  public ModelRenderProfileManager(Map<Identifier, EasyModelRenderProfile> renderProfilesById) {
    this(renderProfilesById, List.of());
  }

  public ModelRenderProfileManager(
      Map<Identifier, EasyModelRenderProfile> renderProfilesById,
      List<ModelResourceRejection> rejectedResources) {
    this.renderProfilesById =
        Collections.unmodifiableMap(
            new LinkedHashMap<>(Objects.requireNonNull(renderProfilesById, "renderProfilesById")));
    this.rejectedResources =
        List.copyOf(Objects.requireNonNull(rejectedResources, "rejectedResources"));
  }

  public static ModelRenderProfileManager load(ResourceManager resourceManager) {
    return load(resourceManager, null);
  }

  public static ModelRenderProfileManager load(
      ResourceManager resourceManager, EasyModelBakeService bakeService) {
    Objects.requireNonNull(resourceManager, "resourceManager");
    Map<Identifier, EasyModelRenderProfile> renderProfiles = new LinkedHashMap<>();
    List<ModelResourceRejection> rejectedResources = new ArrayList<>();
    Map<Identifier, Resource> resources =
        resourceManager.listResources(
            ModelResourcePaths.RENDER_PROFILE_DIRECTORY,
            id -> id.getPath().endsWith(JSON_EXTENSION));

    for (Map.Entry<Identifier, Resource> entry : resources.entrySet()) {
      Identifier identifier = entry.getKey();
      Optional<Identifier> renderProfileId = renderProfileIdFromResourceLocation(identifier);
      if (renderProfileId.isEmpty()) {
        String reason =
            "The file name cannot be used as a resource location, use lowercase letters, digits,"
                + " '_', '-' and '/' only.";
        rejectedResources.add(new ModelResourceRejection(identifier, reason));
        log.warn("Ignoring render profile {}: {}", identifier, reason);
        continue;
      }
      if (!ModelType.hasModelTypeDirectory(renderProfileId.get())) {
        String reason =
            "Render profiles must be placed in "
                + ModelResourcePaths.RENDER_PROFILE_DIRECTORY
                + "/entity/ or "
                + ModelResourcePaths.RENDER_PROFILE_DIRECTORY
                + "/block_entity/, otherwise they can never match a server profile.";
        rejectedResources.add(new ModelResourceRejection(identifier, reason));
        log.warn("Unusable render profile {}: {}", identifier, reason);
      }

      EasyModelRenderProfile renderProfile =
          parseRenderProfile(renderProfileId.get(), entry.getValue());
      EasyModelRenderProfile validatedRenderProfile =
          validateClientAssets(renderProfile, resourceManager, bakeService == null);
      EasyModelRenderProfile resolvedRenderProfile =
          bakeService == null
              ? validatedRenderProfile
              : validateBakedModel(validatedRenderProfile, resourceManager, bakeService);
      renderProfiles.put(renderProfileId.get(), resolvedRenderProfile);
      logRenderProfileIssues(renderProfileId.get(), resolvedRenderProfile);
    }
    log.info(
        "Loaded {} render profile(s) from {} resource(s), {} of them are misplaced or ignored.",
        renderProfiles.size(),
        resources.size(),
        rejectedResources.size());

    return new ModelRenderProfileManager(renderProfiles, rejectedResources);
  }

  public static Optional<Identifier> renderProfileIdFromResourceLocation(Identifier identifier) {
    Objects.requireNonNull(identifier, "identifier");
    String prefix = ModelResourcePaths.RENDER_PROFILE_DIRECTORY + "/";
    String path = identifier.getPath();
    if (!path.startsWith(prefix) || !path.endsWith(JSON_EXTENSION)) {
      return Optional.empty();
    }

    String renderProfilePath =
        path.substring(prefix.length(), path.length() - JSON_EXTENSION.length());
    Identifier renderProfileId =
        Identifier.tryParse(identifier.getNamespace() + ":" + renderProfilePath);
    return Optional.ofNullable(renderProfileId);
  }

  private static EasyModelRenderProfile parseRenderProfile(
      Identifier renderProfileId, Resource resource) {
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

  private static void logRenderProfileIssues(
      Identifier renderProfileId, EasyModelRenderProfile renderProfile) {
    for (ModelRenderProfileValidationIssue issue : renderProfile.validationIssues()) {
      if (issue.status() == ModelRenderProfileStatus.ACTIVE) {
        log.warn("Render profile {} [{}]: {}", renderProfileId, issue.field(), issue.message());
      } else {
        log.error(
            "Render profile {} is not usable [{}] {}: {}",
            renderProfileId,
            issue.status(),
            issue.field(),
            issue.message());
      }
    }
  }

  private static EasyModelRenderProfile validateClientAssets(
      EasyModelRenderProfile renderProfile,
      ResourceManager resourceManager,
      boolean validateModelResource) {
    if (!renderProfile.isActive()) {
      return renderProfile;
    }

    List<ModelRenderProfileValidationIssue> issues =
        new ArrayList<>(renderProfile.validationIssues());
    if (validateModelResource) {
      Identifier modelResourceLocation =
          ModelResourcePaths.modelResourceLocation(renderProfile.model());
      if (resourceManager.getResource(modelResourceLocation).isEmpty()) {
        issues.add(
            new ModelRenderProfileValidationIssue(
                ModelRenderProfileStatus.MISSING_MODEL,
                "model",
                "Missing model asset " + modelResourceLocation + "."));
      }
    }
    Identifier textureResourceLocation =
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

  private static EasyModelRenderProfile validateBakedModel(
      EasyModelRenderProfile renderProfile,
      ResourceManager resourceManager,
      EasyModelBakeService bakeService) {
    if (!renderProfile.isActive()) {
      return renderProfile;
    }

    ModelBakeResult bakeResult = bakeService.bake(renderProfile, resourceManager);
    if (bakeResult.validationIssues().isEmpty()) {
      return renderProfile;
    }

    List<ModelRenderProfileValidationIssue> issues =
        new ArrayList<>(renderProfile.validationIssues());
    issues.addAll(bakeResult.validationIssues());
    return renderProfile.withValidationIssues(issues);
  }

  @Override
  public Collection<ModelResourceRejection> getRejectedResources() {
    return this.rejectedResources;
  }

  @Override
  public boolean hasRenderProfile(Identifier renderProfileId) {
    return this.renderProfilesById.containsKey(renderProfileId);
  }

  @Override
  public Optional<EasyModelRenderProfile> getRenderProfile(Identifier renderProfileId) {
    return Optional.ofNullable(this.renderProfilesById.get(renderProfileId));
  }

  @Override
  public Collection<EasyModelRenderProfile> getRenderProfiles() {
    return this.renderProfilesById.values();
  }

  @Override
  public Collection<Identifier> getRenderProfileIds() {
    return this.renderProfilesById.keySet();
  }

  @Override
  public Collection<ModelRenderProfileValidationIssue> getValidationIssues(
      Identifier renderProfileId) {
    return getRenderProfile(renderProfileId)
        .map(EasyModelRenderProfile::validationIssues)
        .orElseGet(List::of);
  }

  @Override
  public boolean isActive(Identifier renderProfileId) {
    return getRenderProfile(renderProfileId).map(EasyModelRenderProfile::isActive).orElse(false);
  }
}
