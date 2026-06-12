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

package de.markusbordihn.easymodelentities.registry;

import de.markusbordihn.easymodelentities.diagnostics.EasyModelDiagnosticsService;
import de.markusbordihn.easymodelentities.entity.EasyModelEntityFactory;
import de.markusbordihn.easymodelentities.model.bake.EasyModelBakeService;
import de.markusbordihn.easymodelentities.model.decoder.EasyModelDecoderRegistry;
import de.markusbordihn.easymodelentities.profile.EasyModelProfileService;
import de.markusbordihn.easymodelentities.renderprofile.EasyModelRenderProfileService;
import de.markusbordihn.easymodelentities.validation.EasyModelValidationService;
import java.util.Objects;

public final class EasyModelServices {

  private static volatile EasyModelProfileService profileService;
  private static volatile EasyModelRenderProfileService renderProfileService;
  private static volatile EasyModelEntityFactory entityFactory;
  private static volatile EasyModelDecoderRegistry decoderRegistry;
  private static volatile EasyModelBakeService bakeService;
  private static volatile EasyModelValidationService validationService;
  private static volatile EasyModelDiagnosticsService diagnosticsService;

  static {
    reset();
  }

  private EasyModelServices() {}

  public static EasyModelProfileService profileService() {
    return profileService;
  }

  public static void setProfileService(EasyModelProfileService profileService) {
    EasyModelServices.profileService = Objects.requireNonNull(profileService, "profileService");
  }

  public static EasyModelRenderProfileService renderProfileService() {
    return renderProfileService;
  }

  public static void setRenderProfileService(EasyModelRenderProfileService renderProfileService) {
    EasyModelServices.renderProfileService =
        Objects.requireNonNull(renderProfileService, "renderProfileService");
  }

  public static EasyModelEntityFactory entityFactory() {
    return entityFactory;
  }

  public static void setEntityFactory(EasyModelEntityFactory entityFactory) {
    EasyModelServices.entityFactory = Objects.requireNonNull(entityFactory, "entityFactory");
  }

  public static EasyModelDecoderRegistry decoderRegistry() {
    return decoderRegistry;
  }

  public static void setDecoderRegistry(EasyModelDecoderRegistry decoderRegistry) {
    EasyModelServices.decoderRegistry = Objects.requireNonNull(decoderRegistry, "decoderRegistry");
  }

  public static EasyModelBakeService bakeService() {
    return bakeService;
  }

  public static void setBakeService(EasyModelBakeService bakeService) {
    EasyModelServices.bakeService = Objects.requireNonNull(bakeService, "bakeService");
  }

  public static EasyModelValidationService validationService() {
    return validationService;
  }

  public static void setValidationService(EasyModelValidationService validationService) {
    EasyModelServices.validationService =
        Objects.requireNonNull(validationService, "validationService");
  }

  public static EasyModelDiagnosticsService diagnosticsService() {
    return diagnosticsService;
  }

  public static void setDiagnosticsService(EasyModelDiagnosticsService diagnosticsService) {
    EasyModelServices.diagnosticsService =
        Objects.requireNonNull(diagnosticsService, "diagnosticsService");
  }

  public static void reset() {
    profileService = EasyModelProfileService.EMPTY;
    renderProfileService = EasyModelRenderProfileService.EMPTY;
    entityFactory = EasyModelEntityFactory.EMPTY;
    decoderRegistry = EasyModelDecoderRegistry.EMPTY;
    bakeService = EasyModelBakeService.EMPTY;
    validationService = EasyModelValidationService.EMPTY;
    diagnosticsService = EasyModelDiagnosticsService.EMPTY;
  }
}
