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

import de.markusbordihn.easymodelentities.data.model.Vec3f;
import de.markusbordihn.easymodelentities.data.model.bake.ModelBakeResult;
import de.markusbordihn.easymodelentities.data.profile.EasyModelEntityProfile;
import de.markusbordihn.easymodelentities.data.profile.ModelBodyType;
import de.markusbordihn.easymodelentities.data.renderprofile.EasyModelRenderProfile;
import de.markusbordihn.easymodelentities.data.renderprofile.ModelRenderProfileStatus;
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
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.imageio.ImageIO;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.junit.jupiter.api.Test;

class ExportedEmeBundleTest {

  private static final String BUNDLE = "bundles/example_org_disguised_chestling_eme.zip";
  private static final String NAMESPACE = "example_org";
  private static final String DATAPACK = "disguised_chestling_datapack.zip";
  private static final String RESOURCEPACK = "disguised_chestling_resourcepack.zip";

  private static ResourceManager resourceManager(
      byte[] resourcepack, EasyModelRenderProfile renderProfile, boolean provideOverrideTextures)
      throws IOException {
    ResourceManager resourceManager = mock(ResourceManager.class);
    when(resourceManager.getResource(
            ModelResourcePaths.modelResourceLocation(renderProfile.model())))
        .thenReturn(
            Optional.of(
                resource(
                    zipEntry(
                        resourcepack,
                        "assets/example_org/easy_model_entities/models/disguised_chestling.bbmodel"))));
    when(resourceManager.getResource(renderProfile.texture()))
        .thenReturn(Optional.of(resource(png())));
    if (provideOverrideTextures) {
      for (ResourceLocation texture : renderProfile.textures().values()) {
        when(resourceManager.getResource(texture)).thenReturn(Optional.of(resource(png())));
      }
    }
    return resourceManager;
  }

  private static byte[] innerZip(String name) throws IOException {
    try (InputStream inputStream =
        ExportedEmeBundleTest.class.getClassLoader().getResourceAsStream(BUNDLE)) {
      if (inputStream == null) {
        throw new IOException("Missing bundle " + BUNDLE);
      }

      return zipEntry(inputStream.readAllBytes(), name);
    }
  }

  private static byte[] zipEntry(byte[] zipBytes, String entryName) throws IOException {
    try (ZipInputStream zipInputStream = new ZipInputStream(new ByteArrayInputStream(zipBytes))) {
      ZipEntry entry;
      while ((entry = zipInputStream.getNextEntry()) != null) {
        if (entry.getName().equals(entryName)) {
          return zipInputStream.readAllBytes();
        }
      }
    }

    throw new IOException("Missing zip entry " + entryName);
  }

  private static InputStreamReader reader(byte[] bytes) {
    return new InputStreamReader(new ByteArrayInputStream(bytes), StandardCharsets.UTF_8);
  }

  private static Resource resource(byte[] bytes) {
    PackResources packResources = mock(PackResources.class);
    return new Resource(packResources, () -> new ByteArrayInputStream(bytes));
  }

  private static byte[] png() throws IOException {
    BufferedImage image = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    ImageIO.write(image, "png", outputStream);
    return outputStream.toByteArray();
  }

  private static EasyModelEntityProfile parseProfile(byte[] datapack, ResourceLocation profileId)
      throws IOException {
    return EasyModelProfileParser.parse(
        profileId,
        reader(
            zipEntry(
                datapack,
                "data/example_org/easy_model_entities/profiles/entity/disguised_chestling.json")));
  }

  private static EasyModelRenderProfile parseRenderProfile(
      byte[] resourcepack, ResourceLocation renderProfileId) throws IOException {
    return ModelRenderProfileParser.parse(
        renderProfileId,
        reader(
            zipEntry(
                resourcepack,
                "assets/example_org/easy_model_entities/render_profiles/entity/disguised_chestling.json")));
  }

  @Test
  void exportedBundleParsesAndBakes() throws Exception {
    byte[] datapack = innerZip(DATAPACK);
    byte[] resourcepack = innerZip(RESOURCEPACK);

    ResourceLocation profileId =
        ResourceLocation.fromNamespaceAndPath(NAMESPACE, "entity/disguised_chestling");
    EasyModelEntityProfile profile = parseProfile(datapack, profileId);
    EasyModelRenderProfile renderProfile =
        parseRenderProfile(resourcepack, profile.renderProfileId());

    ModelBakeResult bakeResult =
        ModelBakeService.createDefault()
            .bake(renderProfile, resourceManager(resourcepack, renderProfile, true));

    assertTrue(profile.isActive());
    assertTrue(renderProfile.isActive());
    assertTrue(bakeResult.successful());
    assertEquals(profileId, profile.id());
    assertEquals(
        ResourceLocation.fromNamespaceAndPath(NAMESPACE, "entity/disguised_chestling"),
        profile.renderProfileId());
    assertEquals(profile.renderProfileId(), renderProfile.id());
    assertEquals(ModelBodyType.CUBOID, profile.bodyType());
    assertEquals(ModelBodyType.CUBOID, renderProfile.bodyType());
    assertEquals(profile.version(), renderProfile.version());

    assertTrue(renderProfile.hasVisibleBounds());
    assertEquals(0.969f, renderProfile.visibleBoundsWidth(), 1.0e-6f);
    assertEquals(0.938f, renderProfile.visibleBoundsHeight(), 1.0e-6f);
    assertEquals(new Vec3f(0.0f, 0.0f, -0.047f), renderProfile.visibleBoundsOffset());
    assertEquals(
        ResourceLocation.fromNamespaceAndPath(
            NAMESPACE, "textures/entity/disguised_chestling_1.png"),
        renderProfile.textures().get(1));
  }

  @Test
  void exportedBundleFallsBackOnMissingTexture() throws Exception {
    byte[] datapack = innerZip(DATAPACK);
    byte[] resourcepack = innerZip(RESOURCEPACK);

    ResourceLocation profileId =
        ResourceLocation.fromNamespaceAndPath(NAMESPACE, "entity/disguised_chestling");
    EasyModelEntityProfile profile = parseProfile(datapack, profileId);
    EasyModelRenderProfile renderProfile =
        parseRenderProfile(resourcepack, profile.renderProfileId());

    ModelBakeResult bakeResult =
        ModelBakeService.createDefault()
            .bake(renderProfile, resourceManager(resourcepack, renderProfile, false));

    assertTrue(renderProfile.isActive());
    assertTrue(bakeResult.successful());
    assertTrue(
        bakeResult.validationIssues().stream()
            .anyMatch(issue -> issue.status() == ModelRenderProfileStatus.MISSING_TEXTURE),
        "Expected a MISSING_TEXTURE issue when the override texture is unavailable.");
  }
}
