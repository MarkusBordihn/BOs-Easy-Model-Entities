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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.markusbordihn.easymodelentities.data.diagnostics.ModelDiagnostic;
import de.markusbordihn.easymodelentities.data.diagnostics.ModelDiagnosticSeverity;
import de.markusbordihn.easymodelentities.data.profile.ModelBodyType;
import de.markusbordihn.easymodelentities.data.renderprofile.*;
import de.markusbordihn.easymodelentities.runtime.EasyModelAnimationState;
import de.markusbordihn.easymodelentities.runtime.EasyModelRuntimeContract;
import java.io.StringReader;
import java.util.List;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

class ModelRenderProfileValidatorTest {

  private static final ResourceLocation PROFILE_ID =
      ResourceLocation.fromNamespaceAndPath("example", "lizard");
  private static final ResourceLocation RENDER_PROFILE_ID =
      ResourceLocation.fromNamespaceAndPath("example", "lizard");

  private static EasyModelRenderProfile renderProfile(String version, ModelBodyType bodyType) {
    String json =
        """
        {
          "preset_type": "quadruped_wandering",
          "version": "%s",
          "body_type": "%s"
        }
        """
            .formatted(version, bodyType.getSerializedName());
    return ModelRenderProfileParser.parse(RENDER_PROFILE_ID, new StringReader(json));
  }

  private static EasyModelRuntimeContract runtimeContract(String version, ModelBodyType bodyType) {
    return new EasyModelRuntimeContract(
        PROFILE_ID,
        RENDER_PROFILE_ID,
        version,
        0.6f,
        0.8f,
        0.5f,
        bodyType,
        EasyModelAnimationState.AUTO);
  }

  @Test
  void bodyTypeMismatchProducesDiagnosticAndFallbackStatus() {
    EasyModelRenderProfile renderProfile = renderProfile("v1", ModelBodyType.QUADRUPED);
    EasyModelRuntimeContract runtimeContract = runtimeContract("v1", ModelBodyType.BIPED);

    List<ModelDiagnostic> diagnostics =
        ModelRenderProfileValidator.validateRuntimeContract(renderProfile, runtimeContract);

    assertTrue(
        diagnostics.stream()
            .anyMatch(
                diagnostic ->
                    ModelRenderProfileValidator.CLIENT_BODY_TYPE_MISMATCH_CODE.equals(
                        diagnostic.code())));
    assertEquals(
        ModelRenderProfileStatus.CLIENT_BODY_TYPE_MISMATCH,
        ModelRenderProfileValidator.runtimeStatus(renderProfile, runtimeContract));
  }

  @Test
  void versionMismatchProducesDiagnosticAndFallbackStatus() {
    EasyModelRenderProfile renderProfile = renderProfile("client-v1", ModelBodyType.QUADRUPED);
    EasyModelRuntimeContract runtimeContract =
        runtimeContract("server-v1", ModelBodyType.QUADRUPED);

    List<ModelDiagnostic> diagnostics =
        ModelRenderProfileValidator.validateRuntimeContract(renderProfile, runtimeContract);

    ModelDiagnostic mismatch =
        diagnostics.stream()
            .filter(
                diagnostic ->
                    ModelRenderProfileValidator.CLIENT_ASSET_MISMATCH_CODE.equals(
                        diagnostic.code()))
            .findFirst()
            .orElseThrow();

    assertEquals(ModelDiagnosticSeverity.WARNING, mismatch.severity());
    assertEquals(
        ModelRenderProfileStatus.CLIENT_ASSET_MISMATCH,
        ModelRenderProfileValidator.runtimeStatus(renderProfile, runtimeContract));
  }

  @Test
  void emptyVersionsSkipMismatchValidation() {
    EasyModelRenderProfile renderProfile = renderProfile("", ModelBodyType.QUADRUPED);
    EasyModelRuntimeContract runtimeContract = runtimeContract("", ModelBodyType.QUADRUPED);

    List<ModelDiagnostic> diagnostics =
        ModelRenderProfileValidator.validateRuntimeContract(renderProfile, runtimeContract);

    assertTrue(
        diagnostics.stream()
            .noneMatch(
                diagnostic ->
                    ModelRenderProfileValidator.CLIENT_ASSET_MISMATCH_CODE.equals(
                        diagnostic.code())));
  }

  @Test
  void oneSidedVersionUsesWildcardMatching() {
    EasyModelRenderProfile renderProfile = renderProfile("", ModelBodyType.QUADRUPED);
    EasyModelRuntimeContract runtimeContract =
        runtimeContract("server-v1", ModelBodyType.QUADRUPED);

    assertEquals(
        ModelRenderProfileStatus.ACTIVE,
        ModelRenderProfileValidator.runtimeStatus(renderProfile, runtimeContract));
  }
}
