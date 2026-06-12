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

import de.markusbordihn.easymodelentities.diagnostics.ModelDiagnostic;
import de.markusbordihn.easymodelentities.profile.ModelBodyType;
import de.markusbordihn.easymodelentities.profile.ModelPackPair;
import de.markusbordihn.easymodelentities.runtime.EasyModelRuntimeContract;
import java.io.StringReader;
import java.util.List;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

class ModelRenderProfileValidatorTest {

  private static final ResourceLocation PROFILE_ID = new ResourceLocation("example", "lizard");
  private static final ResourceLocation RENDER_PROFILE_ID =
      new ResourceLocation("example", "lizard");

  private static EasyModelRenderProfile renderProfile(
      String assetFingerprint, ModelBodyType bodyType) {
    String json =
        RenderProfileTestFixtures.read(RenderProfileTestFixtures.RESOURCE_PACK_RENDER_PROFILE)
            .replace("2cbb2c6e-4f28-4f1d-b9f7-0d8c1f63d24a", "pair")
            .replace("sha256:abc123", assetFingerprint)
            .replace(
                "\"body_type\": \"quadruped\"",
                "\"body_type\": \"" + bodyType.getSerializedName() + "\"");
    return ModelRenderProfileParser.parse(RENDER_PROFILE_ID, new StringReader(json));
  }

  private static EasyModelRuntimeContract runtimeContract(
      String assetFingerprint, ModelBodyType bodyType) {
    return new EasyModelRuntimeContract(
        PROFILE_ID, RENDER_PROFILE_ID, assetFingerprint, 0.6f, 0.8f, 0.5f, bodyType, (byte) 0);
  }

  @Test
  void bodyTypeMismatchProducesDiagnosticAndFallbackStatus() {
    EasyModelRenderProfile renderProfile = renderProfile("sha256:abc123", ModelBodyType.QUADRUPED);
    EasyModelRuntimeContract runtimeContract =
        runtimeContract("sha256:abc123", ModelBodyType.BIPED);

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
  void fingerprintMismatchProducesDiagnosticAndFallbackStatus() {
    EasyModelRenderProfile renderProfile = renderProfile("sha256:client", ModelBodyType.QUADRUPED);
    EasyModelRuntimeContract runtimeContract =
        runtimeContract("sha256:server", ModelBodyType.QUADRUPED);

    List<ModelDiagnostic> diagnostics =
        ModelRenderProfileValidator.validateRuntimeContract(renderProfile, runtimeContract);

    assertTrue(
        diagnostics.stream()
            .anyMatch(
                diagnostic ->
                    ModelRenderProfileValidator.CLIENT_ASSET_MISMATCH_CODE.equals(
                        diagnostic.code())));
    assertEquals(
        ModelRenderProfileStatus.CLIENT_ASSET_MISMATCH,
        ModelRenderProfileValidator.runtimeStatus(renderProfile, runtimeContract));
  }

  @Test
  void emptyServerFingerprintSkipsMismatchValidation() {
    EasyModelRenderProfile renderProfile = renderProfile("sha256:client", ModelBodyType.QUADRUPED);
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
  void emptyClientFingerprintWithNonEmptyServerFingerprintActivatesFallback() {
    EasyModelRenderProfile renderProfile = renderProfile("", ModelBodyType.QUADRUPED);
    EasyModelRuntimeContract runtimeContract =
        runtimeContract("sha256:server", ModelBodyType.QUADRUPED);

    assertEquals(
        ModelRenderProfileStatus.CLIENT_ASSET_MISMATCH,
        ModelRenderProfileValidator.runtimeStatus(renderProfile, runtimeContract));
  }

  @Test
  void pairIdMismatchProducesWarningOnly() {
    List<ModelDiagnostic> diagnostics =
        ModelRenderProfileValidator.validatePackPair(
            RENDER_PROFILE_ID,
            new ModelPackPair("client", "sha256:abc123"),
            new ModelPackPair("server", "sha256:abc123"));

    assertEquals(1, diagnostics.size());
    assertEquals(ModelDiagnostic.Severity.WARNING, diagnostics.get(0).severity());
    assertEquals(
        ModelRenderProfileValidator.CLIENT_PAIR_ID_MISMATCH_CODE, diagnostics.get(0).code());
  }
}
