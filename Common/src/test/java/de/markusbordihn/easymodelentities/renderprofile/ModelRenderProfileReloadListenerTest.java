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
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.markusbordihn.easymodelentities.api.EasyModelReloadEvents;
import de.markusbordihn.easymodelentities.api.EasyModelReloadEvents.Listener;
import de.markusbordihn.easymodelentities.data.model.bake.ModelBakeResult;
import de.markusbordihn.easymodelentities.data.renderprofile.ModelRenderProfileStatus;
import de.markusbordihn.easymodelentities.model.bake.EasyModelBakeService;
import de.markusbordihn.easymodelentities.model.bake.ModelBakeService;
import de.markusbordihn.easymodelentities.model.decoder.ModelDecoderRegistry;
import de.markusbordihn.easymodelentities.registry.EasyModelServices;
import de.markusbordihn.easymodelentities.registry.ModelResourcePaths;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import javax.imageio.ImageIO;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ModelRenderProfileReloadListenerTest {

  private static final Identifier RENDER_PROFILE_RESOURCE =
      Identifier.fromNamespaceAndPath(
          "example", "easy_model_entities/render_profiles/entity/pillar.json");
  private static final Identifier RENDER_PROFILE_ID =
      Identifier.fromNamespaceAndPath("example", "entity/pillar");
  private static final Identifier MODEL_ID =
      Identifier.fromNamespaceAndPath("example", "easy_model_entities/models/pillar");
  private static final Identifier TEXTURE_ID =
      Identifier.fromNamespaceAndPath("example", "textures/entity/pillar.png");
  private static final String PROFILE_VERSION = "v1";

  private static ResourceManager resourceManager(float pillarHeight) throws IOException {
    return resourceManager(Float.toString(pillarHeight));
  }

  private static ResourceManager resourceManager(String pillarHeight) throws IOException {
    ResourceManager resourceManager = mock(ResourceManager.class);
    when(resourceManager.listResources(eq(ModelResourcePaths.RENDER_PROFILE_DIRECTORY), any()))
        .thenReturn(Map.of(RENDER_PROFILE_RESOURCE, resource(renderProfileJson())));
    when(resourceManager.getResource(ModelResourcePaths.modelResourceLocation(MODEL_ID)))
        .thenReturn(Optional.of(resource(modelJson(pillarHeight))));
    when(resourceManager.getResource(TEXTURE_ID)).thenReturn(Optional.of(resource(png())));
    return resourceManager;
  }

  private static byte[] renderProfileJson() {
    return ("{\"schema_version\":\"0.2.0\",\"preset_type\":\"statue\",\"version\":\""
            + PROFILE_VERSION
            + "\",\"model\":\""
            + MODEL_ID
            + "\",\"texture\":\""
            + TEXTURE_ID
            + "\"}")
        .getBytes(StandardCharsets.UTF_8);
  }

  private static byte[] modelJson(String pillarHeight) {
    return ("{\"meta\":{\"format_version\":\"5.0\",\"model_format\":\"modded_entity\"},"
            + "\"resolution\":{\"width\":64,\"height\":64},"
            + "\"elements\":[{\"name\":\"pillar\",\"from\":[-1,0,-1],\"to\":[1,"
            + pillarHeight
            + ",1],\"origin\":[0,0,0],\"uv_offset\":[0,0],"
            + "\"type\":\"cube\",\"uuid\":\"element_pillar\"}],"
            + "\"groups\":[{\"uuid\":\"group_root\",\"name\":\"root\","
            + "\"origin\":[0,0,0],\"rotation\":[0,0,0]}],"
            + "\"outliner\":[{\"uuid\":\"group_root\",\"children\":[\"element_pillar\"]}]}")
        .getBytes(StandardCharsets.UTF_8);
  }

  private static byte[] png() throws IOException {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    ImageIO.write(new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB), "png", outputStream);
    return outputStream.toByteArray();
  }

  private static Resource resource(byte[] bytes) {
    return new Resource(mock(PackResources.class), () -> new ByteArrayInputStream(bytes));
  }

  private static float bakedPillarHeight() {
    ModelBakeResult bakeResult =
        EasyModelServices.bakeService().getCached(MODEL_ID, PROFILE_VERSION).orElseThrow();
    return bakeResult.bakedModel().rootParts().get(0).cubes().get(0).dimensions().y();
  }

  private static void reload(ResourceManager resourceManager) {
    ModelRenderProfileReloadListener listener = new ModelRenderProfileReloadListener();
    ProfilerFiller profilerFiller = mock(ProfilerFiller.class);
    listener.apply(
        listener.prepare(resourceManager, profilerFiller), resourceManager, profilerFiller);
  }

  @AfterEach
  void resetServices() {
    EasyModelServices.reset();
  }

  @Test
  void applyPublishesServicesAndFiresRenderReloadEvent() {
    ModelRenderProfileReloadListener listener = new ModelRenderProfileReloadListener();
    ModelDecoderRegistry decoderRegistry = ModelDecoderRegistry.createDefault();
    ModelBakeService bakeService = new ModelBakeService(decoderRegistry);
    ModelRenderProfileManager renderProfileManager = new ModelRenderProfileManager(Map.of());
    ModelRenderProfileReloadListener.ReloadState reloadState =
        new ModelRenderProfileReloadListener.ReloadState(
            renderProfileManager, decoderRegistry, bakeService);
    AtomicReference<Object> renderServiceInCallback = new AtomicReference<>();
    Listener reloadListener =
        () -> renderServiceInCallback.set(EasyModelServices.renderProfileService());
    EasyModelReloadEvents.onRenderProfileReload(reloadListener);

    try {
      listener.apply(reloadState, mock(ResourceManager.class), mock(ProfilerFiller.class));

      assertSame(renderProfileManager, EasyModelServices.renderProfileService());
      assertSame(bakeService, EasyModelServices.bakeService());
      assertSame(decoderRegistry, EasyModelServices.decoderRegistry());
      assertSame(renderProfileManager, renderServiceInCallback.get());
    } finally {
      assertTrue(EasyModelReloadEvents.removeRenderProfileReloadListener(reloadListener));
    }
  }

  @Test
  @DisplayName("A reload bakes the changed model of a resource pack instead of the cached one")
  void reloadRebakesChangedModelGeometry() throws IOException {
    reload(resourceManager(2.0f));
    EasyModelBakeService firstBakeService = EasyModelServices.bakeService();
    assertEquals(2.0f, bakedPillarHeight(), 0.001f);

    reload(resourceManager(6.0f));

    assertNotSame(firstBakeService, EasyModelServices.bakeService());
    assertEquals(6.0f, bakedPillarHeight(), 0.001f);
  }

  @Test
  @DisplayName("A corrected non-finite model value is loaded by the next resource reload")
  void reloadRecoversFromNonFiniteModelValue() throws IOException {
    reload(resourceManager("NaN"));

    assertEquals(
        ModelRenderProfileStatus.MODEL_DECODE_FAILED,
        EasyModelServices.renderProfileService()
            .getRenderProfile(RENDER_PROFILE_ID)
            .orElseThrow()
            .status());
    assertTrue(
        EasyModelServices.renderProfileService()
            .getRenderProfile(RENDER_PROFILE_ID)
            .orElseThrow()
            .canResolveRenderState());

    reload(resourceManager(6.0f));

    assertEquals(
        ModelRenderProfileStatus.ACTIVE,
        EasyModelServices.renderProfileService()
            .getRenderProfile(RENDER_PROFILE_ID)
            .orElseThrow()
            .status());
    assertEquals(6.0f, bakedPillarHeight(), 0.001f);
  }
}
