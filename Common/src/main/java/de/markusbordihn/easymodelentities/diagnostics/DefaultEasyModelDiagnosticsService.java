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

package de.markusbordihn.easymodelentities.diagnostics;

import de.markusbordihn.easymodelentities.profile.EasyModelEntityProfile;
import de.markusbordihn.easymodelentities.profile.ModelProfileValidationIssue;
import de.markusbordihn.easymodelentities.registry.EasyModelServices;
import de.markusbordihn.easymodelentities.renderprofile.EasyModelRenderProfile;
import de.markusbordihn.easymodelentities.renderprofile.ModelRenderProfileStatus;
import de.markusbordihn.easymodelentities.renderprofile.ModelRenderProfileValidationIssue;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;

public final class DefaultEasyModelDiagnosticsService implements EasyModelDiagnosticsService {

  private static ModelDiagnostic.Severity severity(ModelRenderProfileStatus status) {
    return status == ModelRenderProfileStatus.MISSING_TEXTURE
        ? ModelDiagnostic.Severity.WARNING
        : ModelDiagnostic.Severity.ERROR;
  }

  @Override
  public List<ModelDiagnostic> getDiagnostics(ResourceLocation profileId) {
    return EasyModelServices.profileService()
        .getProfile(profileId)
        .map(this::diagnosticsForProfile)
        .orElseGet(List::of);
  }

  @Override
  public List<ModelDiagnostic> getDiagnostics() {
    return EasyModelServices.profileService().getProfiles().stream()
        .sorted(Comparator.comparing(profile -> profile.id().toString()))
        .flatMap(profile -> diagnosticsForProfile(profile).stream())
        .toList();
  }

  private List<ModelDiagnostic> diagnosticsForProfile(EasyModelEntityProfile profile) {
    List<ModelDiagnostic> diagnostics = new ArrayList<>();
    for (ModelProfileValidationIssue issue : profile.validationIssues()) {
      diagnostics.add(
          new ModelDiagnostic(
              ModelDiagnostic.Severity.ERROR,
              issue.status().name(),
              issue.message(),
              Optional.of(profile.id())));
    }

    Optional<EasyModelRenderProfile> renderProfile =
        EasyModelServices.renderProfileService().getRenderProfile(profile.renderProfileId());
    if (renderProfile.isEmpty()) {
      if (!EasyModelServices.renderProfileService().getRenderProfiles().isEmpty()) {
        diagnostics.add(
            new ModelDiagnostic(
                ModelDiagnostic.Severity.ERROR,
                ModelRenderProfileStatus.MISSING_RENDER_PROFILE.name(),
                "Missing render profile " + profile.renderProfileId() + ".",
                Optional.of(profile.id())));
      }
      return diagnostics;
    }

    EasyModelRenderProfile renderProfileValue = renderProfile.get();
    if (renderProfileValue.bodyType() != profile.bodyType()) {
      diagnostics.add(
          new ModelDiagnostic(
              ModelDiagnostic.Severity.ERROR,
              ModelRenderProfileStatus.CLIENT_BODY_TYPE_MISMATCH.name(),
              "Render profile body type "
                  + renderProfileValue.bodyType().getSerializedName()
                  + " does not match server profile body type "
                  + profile.bodyType().getSerializedName()
                  + ".",
              Optional.of(profile.id())));
    }
    if (!profile.version().equals(renderProfileValue.version())) {
      diagnostics.add(
          new ModelDiagnostic(
              ModelDiagnostic.Severity.WARNING,
              ModelRenderProfileStatus.CLIENT_ASSET_MISMATCH.name(),
              "Render profile version does not match server profile version.",
              Optional.of(profile.id())));
    }
    for (ModelRenderProfileValidationIssue issue : renderProfileValue.validationIssues()) {
      diagnostics.add(
          new ModelDiagnostic(
              severity(issue.status()),
              issue.status().name(),
              issue.message(),
              Optional.of(profile.id())));
    }

    return diagnostics;
  }
}
