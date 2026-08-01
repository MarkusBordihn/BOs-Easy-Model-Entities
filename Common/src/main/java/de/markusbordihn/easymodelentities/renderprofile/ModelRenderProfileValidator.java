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
import de.markusbordihn.easymodelentities.data.diagnostics.ModelDiagnostic;
import de.markusbordihn.easymodelentities.data.diagnostics.ModelDiagnosticSeverity;
import de.markusbordihn.easymodelentities.data.renderprofile.EasyModelRenderProfile;
import de.markusbordihn.easymodelentities.data.renderprofile.ModelRenderProfileStatus;
import de.markusbordihn.easymodelentities.event.EasyModelReloadDispatcher;
import de.markusbordihn.easymodelentities.runtime.AssetPairing;
import de.markusbordihn.easymodelentities.runtime.EasyModelRuntimeContract;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.resources.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class ModelRenderProfileValidator {

  public static final String CLIENT_ASSET_MISMATCH_CODE = "CLIENT_ASSET_MISMATCH";
  public static final String CLIENT_BODY_TYPE_MISMATCH_CODE = "CLIENT_BODY_TYPE_MISMATCH";
  private static final Logger log = LogManager.getLogger(Constants.LOG_NAME);
  private static final Set<String> LOGGED_DIAGNOSTICS = ConcurrentHashMap.newKeySet();

  static {
    EasyModelReloadDispatcher.addProfileReloadListener(LOGGED_DIAGNOSTICS::clear);
    EasyModelReloadDispatcher.addRenderProfileReloadListener(LOGGED_DIAGNOSTICS::clear);
  }

  private ModelRenderProfileValidator() {}

  public static List<ModelDiagnostic> validateRuntimeContract(
      EasyModelRenderProfile renderProfile, EasyModelRuntimeContract runtimeContract) {
    Objects.requireNonNull(renderProfile, "renderProfile");
    Objects.requireNonNull(runtimeContract, "runtimeContract");

    List<ModelDiagnostic> diagnostics = new ArrayList<>();
    if (renderProfile.bodyType() != runtimeContract.bodyType()) {
      diagnostics.add(
          diagnostic(
              ModelDiagnosticSeverity.ERROR,
              CLIENT_BODY_TYPE_MISMATCH_CODE,
              "Render profile body type "
                  + renderProfile.bodyType().getSerializedName()
                  + " does not match runtime body type "
                  + runtimeContract.bodyType().getSerializedName()
                  + ".",
              renderProfile.id()));
    }

    String serverVersion = runtimeContract.version();
    String clientVersion = renderProfile.version();
    if (!AssetPairing.matches(serverVersion, clientVersion)) {
      diagnostics.add(
          diagnostic(
              ModelDiagnosticSeverity.WARNING,
              CLIENT_ASSET_MISMATCH_CODE,
              "Render profile version does not match runtime version.",
              renderProfile.id()));
    }

    logOnce(diagnostics);
    return List.copyOf(diagnostics);
  }

  public static ModelRenderProfileStatus runtimeStatus(
      EasyModelRenderProfile renderProfile, EasyModelRuntimeContract runtimeContract) {
    List<ModelDiagnostic> diagnostics = validateRuntimeContract(renderProfile, runtimeContract);
    if (diagnostics.stream()
        .anyMatch(diagnostic -> CLIENT_BODY_TYPE_MISMATCH_CODE.equals(diagnostic.code()))) {
      return ModelRenderProfileStatus.CLIENT_BODY_TYPE_MISMATCH;
    }
    if (diagnostics.stream()
        .anyMatch(diagnostic -> CLIENT_ASSET_MISMATCH_CODE.equals(diagnostic.code()))) {
      return ModelRenderProfileStatus.CLIENT_ASSET_MISMATCH;
    }

    return renderProfile.status();
  }

  private static ModelDiagnostic diagnostic(
      ModelDiagnosticSeverity severity, String code, String message, Identifier renderProfileId) {
    return new ModelDiagnostic(severity, code, message, Optional.of(renderProfileId));
  }

  private static void logOnce(List<ModelDiagnostic> diagnostics) {
    for (ModelDiagnostic diagnostic : diagnostics) {
      if (diagnostic.severity() != ModelDiagnosticSeverity.ERROR) {
        continue;
      }
      String profileId = diagnostic.profileId().map(Object::toString).orElse("");
      String logKey = profileId + ":" + diagnostic.code();
      if (LOGGED_DIAGNOSTICS.add(logKey)) {
        log.warn("{}: {}", diagnostic.code(), diagnostic.message());
      }
    }
  }
}
