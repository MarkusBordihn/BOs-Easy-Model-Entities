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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.markusbordihn.easymodelentities.data.model.bake.BakedModel;
import de.markusbordihn.easymodelentities.data.model.bake.ModelBakeResult;
import de.markusbordihn.easymodelentities.data.profile.EasyModelEntityProfile;
import de.markusbordihn.easymodelentities.data.renderprofile.EasyModelRenderProfile;
import de.markusbordihn.easymodelentities.data.renderprofile.ModelRenderProfileStatus;
import de.markusbordihn.easymodelentities.data.renderprofile.ModelRenderProfileValidationIssue;
import de.markusbordihn.easymodelentities.model.bake.ModelBakeService;
import de.markusbordihn.easymodelentities.model.bake.ModelTextureResolver;
import de.markusbordihn.easymodelentities.profile.EasyModelProfileParser;
import de.markusbordihn.easymodelentities.registry.ModelResourcePaths;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.imageio.ImageIO;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class ExportedEmeBundleTextureTest {

  private static final String NAMESPACE = "example_org";
  private static final String VANILLA_NAMESPACE = "minecraft";
  private static final String PACK_METADATA = "pack.mcmeta";
  private static final String ASSETS_ROOT = "assets";

  private static Stream<String> exportedBundles() {
    return Stream.of("test_dawn_sparrow", "test_coral_drifter", "test_disguised_chestling");
  }

  private static Map<String, byte[]> bundleEntries(String modelName) throws IOException {
    String bundle = "bundles/" + NAMESPACE + "_" + modelName + "_eme.zip";
    try (InputStream inputStream =
        ExportedEmeBundleTextureTest.class.getClassLoader().getResourceAsStream(bundle)) {
      assertNotNull(inputStream, "Missing test bundle " + bundle + ".");

      return zipEntries(inputStream.readAllBytes());
    }
  }

  private static Map<String, byte[]> dataPackEntries(String modelName) throws IOException {
    return zipEntries(bundleEntries(modelName).get(modelName + "_datapack.zip"));
  }

  private static Map<String, byte[]> resourcePackEntries(String modelName) throws IOException {
    return zipEntries(bundleEntries(modelName).get(modelName + "_resourcepack.zip"));
  }

  private static ResourceLocation entityProfileId(String modelName) {
    return ResourceLocation.fromNamespaceAndPath(NAMESPACE, "entity/" + modelName);
  }

  private static Map<String, byte[]> zipEntries(byte[] zipBytes) throws IOException {
    Map<String, byte[]> entries = new LinkedHashMap<>();
    try (ZipInputStream zipInputStream = new ZipInputStream(new ByteArrayInputStream(zipBytes))) {
      ZipEntry entry;
      while ((entry = zipInputStream.getNextEntry()) != null) {
        if (!entry.isDirectory()) {
          entries.put(entry.getName(), zipInputStream.readAllBytes());
        }
      }
    }

    return entries;
  }

  private static String packPath(ResourceLocation resourceLocation) {
    return ASSETS_ROOT + "/" + resourceLocation.getNamespace() + "/" + resourceLocation.getPath();
  }

  private static Resource resource(byte[] bytes) {
    PackResources packResources = mock(PackResources.class);
    return new Resource(packResources, () -> new ByteArrayInputStream(bytes));
  }

  private static ResourceManager resourceManager(Map<String, byte[]> resourcepackEntries) {
    ResourceManager resourceManager = mock(ResourceManager.class);
    when(resourceManager.getResource(any()))
        .thenAnswer(
            invocation -> {
              ResourceLocation resourceLocation = invocation.getArgument(0);
              byte[] bytes = resourcepackEntries.get(packPath(resourceLocation));
              if (bytes != null) {
                return Optional.of(resource(bytes));
              }

              return VANILLA_NAMESPACE.equals(resourceLocation.getNamespace())
                  ? Optional.of(resource(vanillaTexture()))
                  : Optional.empty();
            });

    return resourceManager;
  }

  private static byte[] vanillaTexture() throws IOException {
    BufferedImage image = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    ImageIO.write(image, "png", outputStream);
    return outputStream.toByteArray();
  }

  private static InputStreamReader reader(byte[] bytes) {
    return new InputStreamReader(new ByteArrayInputStream(bytes), StandardCharsets.UTF_8);
  }

  private static EasyModelRenderProfile parseRenderProfile(
      Map<String, byte[]> resourcepack, ResourceLocation profileId) {
    return ModelRenderProfileParser.parse(
        profileId, reader(resourcepack.get(ModelResourcePaths.renderProfilePath(profileId))));
  }

  private static void assertTextureExists(
      Map<String, byte[]> resourcepack, ResourceLocation texture) {
    if (VANILLA_NAMESPACE.equals(texture.getNamespace())) {
      return;
    }

    assertTrue(
        resourcepack.containsKey(packPath(texture)),
        "Resource pack does not contain the declared texture " + texture + ".");
  }

  private static String missingTextureMessages(ModelBakeResult bakeResult) {
    return bakeResult.validationIssues().stream()
        .filter(issue -> issue.status() == ModelRenderProfileStatus.MISSING_TEXTURE)
        .map(ModelRenderProfileValidationIssue::message)
        .toList()
        .toString();
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("exportedBundles")
  void exportedBundleContainsDataPackAndResourcePack(String modelName) throws Exception {
    ResourceLocation profileId = entityProfileId(modelName);
    Map<String, byte[]> datapack = dataPackEntries(modelName);
    Map<String, byte[]> resourcepack = resourcePackEntries(modelName);

    assertTrue(datapack.containsKey(PACK_METADATA), "Data pack is missing " + PACK_METADATA + ".");
    assertTrue(
        resourcepack.containsKey(PACK_METADATA), "Resource pack is missing " + PACK_METADATA + ".");
    assertTrue(
        datapack.containsKey(ModelResourcePaths.serverProfilePath(profileId)),
        "Data pack is missing the server profile for " + profileId + ".");
    assertTrue(
        resourcepack.containsKey(ModelResourcePaths.renderProfilePath(profileId)),
        "Resource pack is missing the render profile for " + profileId + ".");
    assertTrue(
        resourcepack.keySet().stream().anyMatch(path -> path.endsWith(".bbmodel")),
        "Resource pack is missing the model for " + profileId + ".");
    assertTrue(
        resourcepack.keySet().stream()
            .anyMatch(path -> path.startsWith(ASSETS_ROOT + "/" + NAMESPACE + "/textures/")),
        "Resource pack is missing the texture for " + profileId + ".");
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("exportedBundles")
  void declaredTexturesExistInTheResourcePack(String modelName) throws Exception {
    Map<String, byte[]> resourcepack = resourcePackEntries(modelName);
    EasyModelRenderProfile renderProfile =
        parseRenderProfile(resourcepack, entityProfileId(modelName));

    assertTrue(
        renderProfile.isActive(),
        "Render profile is not active: " + renderProfile.validationIssues());
    assertTextureExists(resourcepack, renderProfile.texture());
    for (ResourceLocation texture : renderProfile.textures().values()) {
      assertTextureExists(resourcepack, texture);
    }
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("exportedBundles")
  void bakingAssignsAnExistingTextureToEveryUsedIndex(String modelName) throws Exception {
    ResourceLocation profileId = entityProfileId(modelName);
    Map<String, byte[]> datapack = dataPackEntries(modelName);
    Map<String, byte[]> resourcepack = resourcePackEntries(modelName);

    EasyModelEntityProfile profile =
        EasyModelProfileParser.parse(
            profileId, reader(datapack.get(ModelResourcePaths.serverProfilePath(profileId))));
    EasyModelRenderProfile renderProfile = parseRenderProfile(resourcepack, profileId);

    assertTrue(profile.isActive(), "Server profile is not active: " + profile.validationIssues());
    ModelBakeResult bakeResult =
        ModelBakeService.createDefault().bake(renderProfile, resourceManager(resourcepack));

    assertTrue(bakeResult.successful(), "Baking fell back to the placeholder model.");
    assertFalse(
        bakeResult.validationIssues().stream()
            .anyMatch(issue -> issue.status() == ModelRenderProfileStatus.MISSING_TEXTURE),
        "Baking reported a missing texture: " + missingTextureMessages(bakeResult));

    BakedModel bakedModel = bakeResult.bakedModel();
    assertFalse(bakedModel.textures().isEmpty(), "Baked model has no texture assignment.");
    for (Map.Entry<Integer, ResourceLocation> texture : bakedModel.textures().entrySet()) {
      assertNotEquals(
          ModelTextureResolver.FALLBACK_TEXTURE,
          texture.getValue(),
          "Texture index " + texture.getKey() + " fell back to the placeholder texture.");
      if (!VANILLA_NAMESPACE.equals(texture.getValue().getNamespace())) {
        assertTextureExists(resourcepack, texture.getValue());
      }
    }
  }
}
