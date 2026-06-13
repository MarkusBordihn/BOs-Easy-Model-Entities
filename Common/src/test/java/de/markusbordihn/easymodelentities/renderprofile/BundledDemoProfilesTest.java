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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.markusbordihn.easymodelentities.model.bake.ModelBakeResult;
import de.markusbordihn.easymodelentities.model.bake.ModelBakeService;
import de.markusbordihn.easymodelentities.profile.EasyModelEntityProfile;
import de.markusbordihn.easymodelentities.profile.EasyModelProfileParser;
import de.markusbordihn.easymodelentities.profile.ModelBodyType;
import de.markusbordihn.easymodelentities.registry.ModelResourcePaths;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import javax.imageio.ImageIO;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.junit.jupiter.api.Test;

class BundledDemoProfilesTest {

  private static void assertDemo(String path, ModelBodyType bodyType) throws Exception {
    ResourceLocation id = new ResourceLocation("easy_model_entities_examples", path);
    EasyModelEntityProfile profile = parseProfile(id);
    EasyModelRenderProfile renderProfile = parseRenderProfile(id);
    ModelBakeResult bakeResult =
        ModelBakeService.createDefault().bake(renderProfile, resourceManager(renderProfile));

    assertTrue(profile.isActive());
    assertTrue(renderProfile.isActive());
    assertTrue(bakeResult.successful());
    assertEquals(bodyType, profile.host().bodyType());
    assertEquals(bodyType, renderProfile.bodyType());
    assertEquals(profile.assetFingerprint(), renderProfile.assetFingerprint());
  }

  private static EasyModelEntityProfile parseProfile(ResourceLocation id) throws IOException {
    try (InputStream inputStream =
            resourceStream(
                "data/"
                    + id.getNamespace()
                    + "/easy_model_entities/profiles/"
                    + id.getPath()
                    + ".json");
        InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
      return EasyModelProfileParser.parse(id, reader);
    }
  }

  private static EasyModelRenderProfile parseRenderProfile(ResourceLocation id) throws IOException {
    try (InputStream inputStream =
            resourceStream(
                "assets/"
                    + id.getNamespace()
                    + "/easy_model_entities/render_profiles/"
                    + id.getPath()
                    + ".json");
        InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
      return ModelRenderProfileParser.parse(id, reader);
    }
  }

  private static ResourceManager resourceManager(EasyModelRenderProfile renderProfile)
      throws IOException {
    ResourceManager resourceManager = mock(ResourceManager.class);
    ResourceLocation modelResourceLocation =
        ModelResourcePaths.modelResourceLocation(renderProfile.model());
    when(resourceManager.getResource(modelResourceLocation))
        .thenReturn(
            Optional.of(
                resource(
                    resourceBytes(
                        "assets/"
                            + modelResourceLocation.getNamespace()
                            + "/"
                            + modelResourceLocation.getPath()))));
    when(resourceManager.getResource(renderProfile.texture()))
        .thenReturn(Optional.of(resource(png())));
    return resourceManager;
  }

  private static InputStream resourceStream(String path) throws IOException {
    InputStream inputStream =
        BundledDemoProfilesTest.class.getClassLoader().getResourceAsStream(path);
    if (inputStream == null) {
      throw new IOException("Missing resource " + path);
    }

    return inputStream;
  }

  private static byte[] resourceBytes(String path) throws IOException {
    try (InputStream inputStream = resourceStream(path)) {
      return inputStream.readAllBytes();
    }
  }

  private static Resource resource(byte[] bytes) throws IOException {
    PackResources packResources = mock(PackResources.class);
    return new Resource(packResources, () -> new ByteArrayInputStream(bytes));
  }

  private static byte[] png() throws IOException {
    BufferedImage image = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    ImageIO.write(image, "png", outputStream);
    return outputStream.toByteArray();
  }

  @Test
  void bundledDemoProfilesParseAndBake() throws Exception {
    assertDemo("training_dummy", ModelBodyType.STATIC);
    assertDemo("little_explorer", ModelBodyType.BIPED);
    assertDemo("stone_turtle", ModelBodyType.QUADRUPED);
  }
}
