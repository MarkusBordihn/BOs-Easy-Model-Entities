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

package de.markusbordihn.easymodelentities.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.markusbordihn.easymodelentities.data.model.Vec3f;
import de.markusbordihn.easymodelentities.data.model.bake.BakedModelPart;
import de.markusbordihn.easymodelentities.data.model.bake.ModelBakeResult;
import de.markusbordihn.easymodelentities.data.profile.EasyModelEntityProfile;
import de.markusbordihn.easymodelentities.data.profile.ModelBodyType;
import de.markusbordihn.easymodelentities.data.profile.ModelProfileStatus;
import de.markusbordihn.easymodelentities.data.profile.ModelProfileValidationIssue;
import de.markusbordihn.easymodelentities.data.renderprofile.EasyModelRenderProfile;
import de.markusbordihn.easymodelentities.data.renderprofile.ModelAnimationMode;
import de.markusbordihn.easymodelentities.data.renderprofile.ModelAnimationSettings;
import de.markusbordihn.easymodelentities.data.renderprofile.ModelRenderProfileStatus;
import de.markusbordihn.easymodelentities.data.renderprofile.ModelRenderProfileValidationIssue;
import de.markusbordihn.easymodelentities.data.renderprofile.ModelRenderSettings;
import de.markusbordihn.easymodelentities.model.bake.ModelBakeService;
import de.markusbordihn.easymodelentities.model.bake.ModelTextureResolver;
import de.markusbordihn.easymodelentities.profile.EasyModelProfileManager;
import de.markusbordihn.easymodelentities.profile.EasyModelProfileParser;
import de.markusbordihn.easymodelentities.registry.ModelResourcePaths;
import de.markusbordihn.easymodelentities.renderprofile.ModelRenderProfileManager;
import de.markusbordihn.easymodelentities.renderprofile.ModelRenderProfileParser;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import javax.imageio.ImageIO;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SpecialCharacterAssetTest {

  private static final Identifier PROFILE_ID = Identifier.fromNamespaceAndPath("example", "lizard");
  private static final Identifier MODEL_ID =
      Identifier.fromNamespaceAndPath("example", "easy_model_entities/models/model");
  private static final Identifier TEXTURE_ID =
      Identifier.fromNamespaceAndPath("example", "textures/entity/model.png");

  private static InputStream fixture(String resourcePath) {
    return Objects.requireNonNull(
        SpecialCharacterAssetTest.class.getClassLoader().getResourceAsStream(resourcePath),
        "Missing test fixture " + resourcePath);
  }

  private static byte[] fixtureBytes(String resourcePath) throws IOException {
    try (InputStream inputStream = fixture(resourcePath)) {
      return inputStream.readAllBytes();
    }
  }

  private static EasyModelEntityProfile parseProfile(String resourcePath) {
    return EasyModelProfileParser.parse(
        PROFILE_ID, new InputStreamReader(fixture(resourcePath), StandardCharsets.UTF_8));
  }

  private static EasyModelRenderProfile parseRenderProfile(String resourcePath) {
    return ModelRenderProfileParser.parse(
        PROFILE_ID, new InputStreamReader(fixture(resourcePath), StandardCharsets.UTF_8));
  }

  private static EasyModelRenderProfile renderProfile(ModelBodyType bodyType, String version) {
    return new EasyModelRenderProfile(
        PROFILE_ID,
        "1.0",
        version,
        bodyType,
        MODEL_ID,
        TEXTURE_ID,
        new ModelRenderSettings(1.0f, 0.3f, 0.0f, 0.0f, Vec3f.ZERO),
        new ModelAnimationSettings(ModelAnimationMode.AUTOMATIC, 1.0f, 1.0f),
        ModelRenderProfileStatus.ACTIVE,
        List.of());
  }

  private static ResourceManager resourceManager(String modelFixture) throws IOException {
    ResourceManager resourceManager = mock(ResourceManager.class);
    when(resourceManager.getResource(ModelResourcePaths.modelResourceLocation(MODEL_ID)))
        .thenReturn(Optional.of(resource(fixtureBytes(modelFixture))));
    when(resourceManager.getResource(TEXTURE_ID)).thenReturn(Optional.of(resource(png())));
    return resourceManager;
  }

  private static byte[] png() throws IOException {
    BufferedImage image = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    ImageIO.write(image, "png", outputStream);
    return outputStream.toByteArray();
  }

  private static Resource resource(byte[] bytes) {
    PackResources packResources = mock(PackResources.class);
    return new Resource(packResources, () -> new ByteArrayInputStream(bytes));
  }

  private static BakedModelPart part(BakedModelPart part, String partName) {
    if (part.name().equals(partName)) {
      return part;
    }
    for (BakedModelPart child : part.children()) {
      BakedModelPart match = part(child, partName);
      if (match != null) {
        return match;
      }
    }

    return null;
  }

  @Test
  @DisplayName("Bone names in any language are rendered and matched in lower case")
  void specialCharacterPartNamesAreKept() throws Exception {
    ModelBakeResult result =
        ModelBakeService.createDefault()
            .bake(
                renderProfile(ModelBodyType.STATIC, "names"),
                resourceManager("bbmodel/special_character_names.bbmodel"));

    assertTrue(result.successful(), () -> "Bake failed: " + result.validationIssues());
    assertEquals(
        ModelRenderProfileStatus.ACTIVE,
        ModelRenderProfileStatus.statusForIssues(result.validationIssues()));
    assertEquals(2, result.bakedModel().cubeCount());
    BakedModelPart root = result.bakedModel().rootParts().get(0);
    assertEquals("körper ärmel", part(root, "körper ärmel").name());
    assertEquals("модель", part(root, "модель").name());
  }

  @Test
  @DisplayName("A texture name with special characters falls back and is reported")
  void specialCharacterTextureNameIsReported() throws Exception {
    ModelBakeResult result =
        ModelBakeService.createDefault()
            .bake(
                renderProfile(ModelBodyType.STATIC, "texture"),
                resourceManager("bbmodel/special_character_texture.bbmodel"));

    assertEquals(
        ModelRenderProfileStatus.MISSING_TEXTURE,
        ModelRenderProfileStatus.statusForIssues(result.validationIssues()));
    assertEquals(ModelTextureResolver.FALLBACK_TEXTURE, result.bakedModel().textures().get(1));
    assertTrue(
        result.validationIssues().stream()
            .map(ModelRenderProfileValidationIssue::message)
            .anyMatch(message -> message.contains("Bär Textur.png")),
        () -> "Expected the texture name in the message: " + result.validationIssues());
  }

  @Test
  @DisplayName("An entity type with special characters deactivates the profile")
  void specialCharacterEntityTypeIsRejected() {
    EasyModelEntityProfile profile = parseProfile("profile/special_character_entity_type.json");

    assertNotEquals(ModelProfileStatus.ACTIVE, profile.status());
    assertTrue(
        profile.validationIssues().stream()
            .map(ModelProfileValidationIssue::status)
            .anyMatch(status -> status == ModelProfileStatus.INVALID_RESOURCE_LOCATION),
        () -> "Expected an invalid resource location: " + profile.validationIssues());
  }

  @Test
  @DisplayName("Free text profile fields keep their original characters")
  void specialCharacterFreeTextIsKept() {
    EasyModelEntityProfile profile = parseProfile("profile/special_character_free_text.json");

    assertEquals(ModelProfileStatus.ACTIVE, profile.status());
    assertEquals("Fassung 1.0 – Räuber 🐢 モデル", profile.version());
  }

  @Test
  @DisplayName("Model and texture ids with special characters deactivate the render profile")
  void specialCharacterAssetIdsAreRejected() {
    EasyModelRenderProfile renderProfile =
        parseRenderProfile("renderprofile/special_character_asset_ids.json");

    assertEquals(ModelRenderProfileStatus.INVALID_RESOURCE_LOCATION, renderProfile.status());
    assertEquals(
        2,
        renderProfile.validationIssues().stream()
            .filter(issue -> issue.status() == ModelRenderProfileStatus.INVALID_RESOURCE_LOCATION)
            .count());
  }

  @Test
  @DisplayName("Profile files outside of the supported folders are ignored")
  void profileFilesOutsideOfSupportedFoldersAreIgnored() {
    assertTrue(
        EasyModelProfileManager.profileIdFromResourceLocation(
                Identifier.fromNamespaceAndPath(
                    "example", ModelResourcePaths.SERVER_PROFILE_DIRECTORY + "/vehicle/car.json"))
            .isEmpty());
    assertTrue(
        EasyModelProfileManager.profileIdFromResourceLocation(
                Identifier.fromNamespaceAndPath("example", "other_mod/entity/car.json"))
            .isEmpty());
    assertEquals(
        Optional.of(Identifier.fromNamespaceAndPath("example", "entity/car")),
        EasyModelProfileManager.profileIdFromResourceLocation(
            Identifier.fromNamespaceAndPath(
                "example", ModelResourcePaths.SERVER_PROFILE_DIRECTORY + "/entity/car.json")));
    assertEquals(
        Optional.of(Identifier.fromNamespaceAndPath("example", "entity/car")),
        ModelRenderProfileManager.renderProfileIdFromResourceLocation(
            Identifier.fromNamespaceAndPath(
                "example", ModelResourcePaths.RENDER_PROFILE_DIRECTORY + "/entity/car.json")));
  }
}
