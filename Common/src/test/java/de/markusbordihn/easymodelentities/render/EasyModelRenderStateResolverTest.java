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

package de.markusbordihn.easymodelentities.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.markusbordihn.easymodelentities.data.model.Vec3f;
import de.markusbordihn.easymodelentities.data.profile.ModelBodyType;
import de.markusbordihn.easymodelentities.data.render.*;
import de.markusbordihn.easymodelentities.data.renderprofile.EasyModelRenderProfile;
import de.markusbordihn.easymodelentities.data.renderprofile.ModelAnimationMode;
import de.markusbordihn.easymodelentities.data.renderprofile.ModelAnimationSettings;
import de.markusbordihn.easymodelentities.data.renderprofile.ModelRenderProfileStatus;
import de.markusbordihn.easymodelentities.data.renderprofile.ModelRenderSettings;
import de.markusbordihn.easymodelentities.model.bake.ModelBakeService;
import de.markusbordihn.easymodelentities.registry.ModelResourcePaths;
import de.markusbordihn.easymodelentities.renderprofile.EasyModelRenderProfileService;
import de.markusbordihn.easymodelentities.runtime.EasyModelAnimationState;
import de.markusbordihn.easymodelentities.runtime.EasyModelRuntimeContract;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.junit.jupiter.api.Test;

class EasyModelRenderStateResolverTest {

  private static final ResourceLocation PROFILE_ID = new ResourceLocation("example", "model");
  private static final ResourceLocation MODEL_ID =
      new ResourceLocation("example", "easy_model_entities/models/model");
  private static final ResourceLocation TEXTURE_ID =
      new ResourceLocation("example", "textures/entity/model.png");

  private static EasyModelRuntimeContract contract(ModelBodyType bodyType, String version) {
    return new EasyModelRuntimeContract(
        PROFILE_ID, PROFILE_ID, version, 0.6f, 1.8f, 1.62f, bodyType, EasyModelAnimationState.AUTO);
  }

  private static EasyModelRenderProfile renderProfile(ModelBodyType bodyType, String version) {
    return new EasyModelRenderProfile(
        PROFILE_ID,
        "1.0",
        version,
        bodyType,
        MODEL_ID,
        TEXTURE_ID,
        new ModelRenderSettings(1.25f, 0.45f, 0.0f, 0.0f, Vec3f.ZERO),
        new ModelAnimationSettings(ModelAnimationMode.AUTOMATIC, 1.0f, 1.0f),
        ModelRenderProfileStatus.ACTIVE,
        List.of());
  }

  private static EasyModelRenderProfileService renderProfileService(
      EasyModelRenderProfile renderProfile) {
    return new EasyModelRenderProfileService() {
      @Override
      public Optional<EasyModelRenderProfile> getRenderProfile(ResourceLocation renderProfileId) {
        return PROFILE_ID.equals(renderProfileId) ? Optional.of(renderProfile) : Optional.empty();
      }

      @Override
      public Collection<EasyModelRenderProfile> getRenderProfiles() {
        return List.of(renderProfile);
      }
    };
  }

  private static ResourceManager resourceManager(boolean includeTexture) throws IOException {
    ResourceManager resourceManager = mock(ResourceManager.class);
    when(resourceManager.getResource(ModelResourcePaths.modelResourceLocation(MODEL_ID)))
        .thenReturn(Optional.of(resource(fixture("static_explicit_root.bbmodel"))));
    when(resourceManager.getResource(TEXTURE_ID))
        .thenReturn(includeTexture ? Optional.of(resource(new byte[] {1})) : Optional.empty());
    return resourceManager;
  }

  private static byte[] fixture(String fixtureName) throws IOException {
    try (InputStream inputStream =
        EasyModelRenderStateResolverTest.class
            .getClassLoader()
            .getResourceAsStream("bbmodel/" + fixtureName)) {
      if (inputStream == null) {
        throw new IOException("Missing fixture " + fixtureName);
      }

      return inputStream.readAllBytes();
    }
  }

  private static Resource resource(byte[] bytes) throws IOException {
    PackResources packResources = mock(PackResources.class);
    return new Resource(packResources, () -> new ByteArrayInputStream(bytes));
  }

  @Test
  void missingRenderProfileReturnsFallbackState() throws Exception {
    EasyModelRenderState renderState =
        EasyModelRenderStateResolver.resolve(
            contract(ModelBodyType.STATIC, "fingerprint"),
            EasyModelRenderProfileService.EMPTY,
            ModelBakeService.createDefault(),
            resourceManager(false));

    assertTrue(renderState.fallbackModel());
    assertTrue(renderState.fallbackTexture());
    assertEquals(
        ModelRenderProfileStatus.MISSING_RENDER_PROFILE,
        renderState.validationIssues().get(0).status());
  }

  @Test
  void bodyTypeMismatchReturnsFallbackState() throws Exception {
    EasyModelRenderState renderState =
        EasyModelRenderStateResolver.resolve(
            contract(ModelBodyType.BIPED, "fingerprint"),
            renderProfileService(renderProfile(ModelBodyType.STATIC, "fingerprint")),
            ModelBakeService.createDefault(),
            resourceManager(false));

    assertTrue(renderState.fallbackModel());
    assertEquals(
        ModelRenderProfileStatus.CLIENT_BODY_TYPE_MISMATCH,
        renderState.validationIssues().get(0).status());
  }

  @Test
  void versionMismatchReturnsFallbackState() throws Exception {
    EasyModelRenderState renderState =
        EasyModelRenderStateResolver.resolve(
            contract(ModelBodyType.STATIC, "server-fingerprint"),
            renderProfileService(renderProfile(ModelBodyType.STATIC, "client-fingerprint")),
            ModelBakeService.createDefault(),
            resourceManager(false));

    assertTrue(renderState.fallbackModel());
    assertEquals(
        ModelRenderProfileStatus.CLIENT_ASSET_MISMATCH,
        renderState.validationIssues().get(0).status());
  }

  @Test
  void missingTextureKeepsBakedModelAndUsesFallbackTexture() throws Exception {
    EasyModelRenderState renderState =
        EasyModelRenderStateResolver.resolve(
            contract(ModelBodyType.STATIC, "fingerprint"),
            renderProfileService(renderProfile(ModelBodyType.STATIC, "fingerprint")),
            ModelBakeService.createDefault(),
            resourceManager(false));

    assertFalse(renderState.fallbackModel());
    assertTrue(renderState.fallbackTexture());
    assertEquals(EasyModelRenderStateResolver.FALLBACK_TEXTURE, renderState.texture());
    assertEquals(1.25f, renderState.scale(), 0.01f);
    assertEquals(0.45f, renderState.shadowRadius(), 0.01f);
  }
}
