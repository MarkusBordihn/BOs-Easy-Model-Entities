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

import de.markusbordihn.easymodelentities.data.model.bake.ModelBakeResult;
import de.markusbordihn.easymodelentities.data.profile.EasyModelEntityProfile;
import de.markusbordihn.easymodelentities.data.profile.ModelBodyType;
import de.markusbordihn.easymodelentities.data.renderprofile.*;
import de.markusbordihn.easymodelentities.model.bake.ModelBakeService;
import de.markusbordihn.easymodelentities.profile.EasyModelProfileParser;
import de.markusbordihn.easymodelentities.registry.ModelResourcePaths;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import javax.imageio.ImageIO;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.junit.jupiter.api.Test;

class BundledDemoProfilesTest {

  private static final List<Demo> DEMOS =
      List.of(
          new Demo("entity/training_dummy", "training_dummy", ModelBodyType.STATIC),
          new Demo("entity/little_explorer", "little_explorer", ModelBodyType.BIPED),
          new Demo("entity/stone_turtle", "stone_turtle", ModelBodyType.QUADRUPED),
          new Demo("entity/coral_drifter", "coral_drifter", ModelBodyType.AQUATIC),
          new Demo("entity/dawn_sparrow", "dawn_sparrow", ModelBodyType.WINGED),
          new Demo("entity/skybound_wanderer", "skybound_wanderer", ModelBodyType.WINGED_HUMANOID),
          new Demo("entity/dust_skitter", "dust_skitter", ModelBodyType.ARTHROPOD),
          new Demo("entity/rune_cube", "rune_cube", ModelBodyType.CUBOID),
          new Demo("entity/wisp_lantern", "wisp_lantern", ModelBodyType.FLOATING),
          new Demo("entity/orientation_test", "orientation_test", ModelBodyType.STATIC),
          new Demo("block_entity/shrine", "shrine", ModelBodyType.STATIC));

  private static void assertDemo(String path, String renderPath, ModelBodyType bodyType)
      throws Exception {
    ResourceLocation id = new ResourceLocation("easy_model_entities_examples", path);
    ResourceLocation renderProfileId =
        new ResourceLocation("easy_model_entities_examples", renderPath);
    EasyModelEntityProfile profile = parseProfile(id);
    EasyModelRenderProfile renderProfile = parseRenderProfile(profile.renderProfileId());
    ModelBakeResult bakeResult =
        ModelBakeService.createDefault().bake(renderProfile, resourceManager(renderProfile));

    assertTrue(profile.isActive());
    assertTrue(renderProfile.isActive());
    assertTrue(bakeResult.successful());
    assertEquals(id, profile.id());
    assertEquals(renderProfileId, profile.renderProfileId());
    assertEquals(profile.renderProfileId(), renderProfile.id());
    assertEquals(bodyType, profile.bodyType());
    assertEquals(bodyType, renderProfile.bodyType());
    assertEquals(profile.version(), renderProfile.version());
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
    for (Demo demo : DEMOS) {
      assertDemo(demo.path(), demo.renderPath(), demo.bodyType());
    }
  }

  @Test
  void bundledDemosCoverEveryBodyType() {
    Set<ModelBodyType> coveredBodyTypes = EnumSet.noneOf(ModelBodyType.class);
    for (Demo demo : DEMOS) {
      coveredBodyTypes.add(demo.bodyType());
    }

    assertEquals(EnumSet.allOf(ModelBodyType.class), coveredBodyTypes);
  }

  private record Demo(String path, String renderPath, ModelBodyType bodyType) {}
}
