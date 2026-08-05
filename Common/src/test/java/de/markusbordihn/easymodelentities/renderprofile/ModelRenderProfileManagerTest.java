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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.markusbordihn.easymodelentities.data.renderprofile.*;
import de.markusbordihn.easymodelentities.model.bake.ModelBakeService;
import de.markusbordihn.easymodelentities.model.decoder.ModelDecoderRegistry;
import de.markusbordihn.easymodelentities.registry.ModelResourcePaths;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;
import javax.imageio.ImageIO;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ModelRenderProfileManagerTest {

  private static final ResourceLocation RENDER_PROFILE_ID =
      ResourceLocation.fromNamespaceAndPath("example", "lizard");
  private static final ResourceLocation RENDER_PROFILE_RESOURCE =
      ResourceLocation.fromNamespaceAndPath(
          "example", "easy_model_entities/render_profiles/lizard.json");
  private static final ResourceLocation MODEL_RESOURCE =
      ResourceLocation.fromNamespaceAndPath("example", "easy_model_entities/models/lizard.bbmodel");
  private static final ResourceLocation TEXTURE_RESOURCE =
      ResourceLocation.fromNamespaceAndPath("example", "textures/entity/lizard.png");

  private static ResourceManager resourceManager(boolean hasModel, boolean hasTexture)
      throws IOException {
    ResourceManager resourceManager = mock(ResourceManager.class);
    Resource renderProfileResource = mock(Resource.class);
    when(renderProfileResource.open())
        .thenReturn(
            new ByteArrayInputStream(
                RenderProfileTestFixtures.read(
                        RenderProfileTestFixtures.RESOURCE_PACK_RENDER_PROFILE)
                    .getBytes(StandardCharsets.UTF_8)));
    when(resourceManager.listResources(eq(ModelResourcePaths.RENDER_PROFILE_DIRECTORY), any()))
        .thenReturn(Map.of(RENDER_PROFILE_RESOURCE, renderProfileResource));
    when(resourceManager.getResource(MODEL_RESOURCE))
        .thenReturn(hasModel ? Optional.of(mock(Resource.class)) : Optional.empty());
    when(resourceManager.getResource(TEXTURE_RESOURCE))
        .thenReturn(hasTexture ? Optional.of(mock(Resource.class)) : Optional.empty());
    return resourceManager;
  }

  private static ResourceManager resourceManagerForBake(byte[] modelBytes, byte[] textureBytes)
      throws IOException {
    ResourceManager resourceManager = mock(ResourceManager.class);
    Resource renderProfileResource =
        resource(
            RenderProfileTestFixtures.read(RenderProfileTestFixtures.RESOURCE_PACK_RENDER_PROFILE)
                .getBytes(StandardCharsets.UTF_8));
    when(resourceManager.listResources(eq(ModelResourcePaths.RENDER_PROFILE_DIRECTORY), any()))
        .thenReturn(Map.of(RENDER_PROFILE_RESOURCE, renderProfileResource));
    when(resourceManager.getResource(MODEL_RESOURCE)).thenReturn(Optional.of(resource(modelBytes)));
    when(resourceManager.getResource(TEXTURE_RESOURCE))
        .thenReturn(Optional.of(resource(textureBytes)));
    return resourceManager;
  }

  private static byte[] png(int width, int height) throws IOException {
    BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    ImageIO.write(image, "png", outputStream);
    return outputStream.toByteArray();
  }

  private static Resource resource(byte[] bytes) throws IOException {
    PackResources packResources = mock(PackResources.class);
    return new Resource(packResources, () -> new ByteArrayInputStream(bytes));
  }

  @Test
  void derivesRenderProfileIdFromResourceLocation() {
    Optional<ResourceLocation> renderProfileId =
        ModelRenderProfileManager.renderProfileIdFromResourceLocation(RENDER_PROFILE_RESOURCE);

    assertEquals(Optional.of(RENDER_PROFILE_ID), renderProfileId);
  }

  @Test
  @DisplayName("A render profile outside of the entity and block entity folders is reported")
  void misplacedRenderProfileIsReported() throws IOException {
    ModelRenderProfileManager manager = ModelRenderProfileManager.load(resourceManager(true, true));

    assertEquals(1, manager.getRejectedResources().size());
    assertEquals(
        RENDER_PROFILE_RESOURCE, manager.getRejectedResources().iterator().next().resource());
  }

  @Test
  void loadsValidRenderProfileWithExistingAssets() throws IOException {
    ModelRenderProfileManager manager = ModelRenderProfileManager.load(resourceManager(true, true));

    EasyModelRenderProfile renderProfile =
        manager.getRenderProfile(RENDER_PROFILE_ID).orElseThrow();
    assertEquals(ModelRenderProfileStatus.ACTIVE, renderProfile.status());
    assertTrue(manager.isActive(RENDER_PROFILE_ID));
    assertTrue(manager.getRenderProfileIds().contains(RENDER_PROFILE_ID));
    assertEquals(MODEL_RESOURCE, ModelResourcePaths.modelResourceLocation(renderProfile.model()));
  }

  @Test
  void missingModelActivatesFallbackStatus() throws IOException {
    ModelRenderProfileManager manager =
        ModelRenderProfileManager.load(resourceManager(false, true));

    EasyModelRenderProfile renderProfile =
        manager.getRenderProfile(RENDER_PROFILE_ID).orElseThrow();
    assertEquals(ModelRenderProfileStatus.MISSING_MODEL, renderProfile.status());
    assertTrue(renderProfile.usesFallbackModel());
    assertTrue(renderProfile.canResolveRenderState());
    assertFalse(renderProfile.isActive());
  }

  @Test
  void missingTextureRecordsTextureFallbackStatus() throws IOException {
    ModelRenderProfileManager manager =
        ModelRenderProfileManager.load(resourceManager(true, false));

    EasyModelRenderProfile renderProfile =
        manager.getRenderProfile(RENDER_PROFILE_ID).orElseThrow();
    assertEquals(ModelRenderProfileStatus.MISSING_TEXTURE, renderProfile.status());
    assertTrue(renderProfile.usesFallbackTexture());
    assertFalse(renderProfile.usesFallbackModel());
  }

  @Test
  void decodeFailureDoesNotThrowOutOfReloadPipeline() throws IOException {
    ModelBakeService bakeService = new ModelBakeService(ModelDecoderRegistry.createDefault());
    ModelRenderProfileManager manager =
        ModelRenderProfileManager.load(
            resourceManagerForBake("{}".getBytes(StandardCharsets.UTF_8), png(64, 64)),
            bakeService);

    EasyModelRenderProfile renderProfile =
        manager.getRenderProfile(RENDER_PROFILE_ID).orElseThrow();
    assertEquals(ModelRenderProfileStatus.MODEL_DECODE_FAILED, renderProfile.status());
    assertTrue(renderProfile.usesFallbackModel());
    assertTrue(renderProfile.canResolveRenderState());
  }
}
