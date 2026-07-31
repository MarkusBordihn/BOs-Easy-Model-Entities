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

package de.markusbordihn.easymodelentities.model.bake;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.markusbordihn.easymodelentities.data.model.Vec3f;
import de.markusbordihn.easymodelentities.data.model.bake.BakedModelPart;
import de.markusbordihn.easymodelentities.data.model.bake.ModelBakeResult;
import de.markusbordihn.easymodelentities.data.profile.ModelBodyType;
import de.markusbordihn.easymodelentities.data.renderprofile.EasyModelRenderProfile;
import de.markusbordihn.easymodelentities.data.renderprofile.ModelAnimationMode;
import de.markusbordihn.easymodelentities.data.renderprofile.ModelAnimationSettings;
import de.markusbordihn.easymodelentities.data.renderprofile.ModelRenderProfileStatus;
import de.markusbordihn.easymodelentities.data.renderprofile.ModelRenderProfileValidationIssue;
import de.markusbordihn.easymodelentities.data.renderprofile.ModelRenderSettings;
import de.markusbordihn.easymodelentities.registry.ModelResourcePaths;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import javax.imageio.ImageIO;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class LargeCubeCountModelTest {

  private static final ResourceLocation PROFILE_ID =
      ResourceLocation.fromNamespaceAndPath("example", "model");
  private static final ResourceLocation MODEL_ID =
      ResourceLocation.fromNamespaceAndPath("example", "easy_model_entities/models/model");
  private static final ResourceLocation TEXTURE_ID =
      ResourceLocation.fromNamespaceAndPath("example", "textures/entity/model.png");

  private static String spacedCubeModel(int cubeCount) {
    StringBuilder elements = new StringBuilder();
    StringBuilder children = new StringBuilder();
    for (int index = 0; index < cubeCount; index++) {
      int x = (index % 8) * 2;
      int y = ((index / 8) % 8) * 2;
      int z = (index / 64) * 2;
      if (index > 0) {
        elements.append(',');
        children.append(',');
      }
      elements
          .append("{\"name\":\"cube_")
          .append(index)
          .append("\",\"from\":[")
          .append(x)
          .append(',')
          .append(y)
          .append(',')
          .append(z)
          .append("],\"to\":[")
          .append(x + 1)
          .append(',')
          .append(y + 1)
          .append(',')
          .append(z + 1)
          .append("],\"origin\":[0,0,0],\"uv_offset\":[0,0],\"type\":\"cube\",\"uuid\":\"element_")
          .append(index)
          .append("\"}");
      children.append("\"element_").append(index).append('"');
    }

    return "{\"meta\":{\"format_version\":\"5.0\",\"model_format\":\"modded_entity\"},"
        + "\"resolution\":{\"width\":64,\"height\":64},"
        + "\"elements\":["
        + elements
        + "],\"groups\":[{\"uuid\":\"group_root\",\"name\":\"root\",\"origin\":[0,0,0],"
        + "\"rotation\":[0,0,0]}],"
        + "\"outliner\":[{\"uuid\":\"group_root\",\"children\":["
        + children
        + "]}]}";
  }

  private static String solidBlockModel(int edgeLength) {
    StringBuilder elements = new StringBuilder();
    StringBuilder children = new StringBuilder();
    int index = 0;
    for (int x = 0; x < edgeLength; x++) {
      for (int y = 0; y < edgeLength; y++) {
        for (int z = 0; z < edgeLength; z++) {
          if (index > 0) {
            elements.append(',');
            children.append(',');
          }
          elements
              .append("{\"name\":\"cube_")
              .append(index)
              .append("\",\"from\":[")
              .append(x)
              .append(',')
              .append(y)
              .append(',')
              .append(z)
              .append("],\"to\":[")
              .append(x + 1)
              .append(',')
              .append(y + 1)
              .append(',')
              .append(z + 1)
              .append(
                  "],\"origin\":[0,0,0],\"uv_offset\":[0,0],\"type\":\"cube\",\"uuid\":\"element_")
              .append(index)
              .append("\"}");
          children.append("\"element_").append(index).append('"');
          index++;
        }
      }
    }

    return "{\"meta\":{\"format_version\":\"5.0\",\"model_format\":\"modded_entity\"},"
        + "\"resolution\":{\"width\":64,\"height\":64},"
        + "\"elements\":["
        + elements
        + "],\"groups\":[{\"uuid\":\"group_root\",\"name\":\"root\",\"origin\":[0,0,0],"
        + "\"rotation\":[0,0,0]}],"
        + "\"outliner\":[{\"uuid\":\"group_root\",\"children\":["
        + children
        + "]}]}";
  }

  private static EasyModelRenderProfile renderProfile(String version) {
    return new EasyModelRenderProfile(
        PROFILE_ID,
        "1.0",
        version,
        ModelBodyType.STATIC,
        MODEL_ID,
        TEXTURE_ID,
        new ModelRenderSettings(1.0f, 0.3f, 0.0f, 0.0f, Vec3f.ZERO),
        new ModelAnimationSettings(ModelAnimationMode.AUTOMATIC, 1.0f, 1.0f),
        ModelRenderProfileStatus.ACTIVE,
        List.of());
  }

  private static ResourceManager resourceManager(String model) throws IOException {
    ResourceManager resourceManager = mock(ResourceManager.class);
    when(resourceManager.getResource(ModelResourcePaths.modelResourceLocation(MODEL_ID)))
        .thenReturn(Optional.of(resource(model.getBytes(StandardCharsets.UTF_8))));
    when(resourceManager.getResource(TEXTURE_ID)).thenReturn(Optional.of(resource(png())));
    return resourceManager;
  }

  private static byte[] png() throws IOException {
    BufferedImage image = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    ImageIO.write(image, "png", outputStream);
    return outputStream.toByteArray();
  }

  private static Resource resource(byte[] bytes) {
    PackResources packResources = mock(PackResources.class);
    return new Resource(packResources, () -> new ByteArrayInputStream(bytes));
  }

  private static int cubeCount(List<BakedModelPart> parts) {
    int count = 0;
    for (BakedModelPart part : parts) {
      count += part.cubes().size();
      count += cubeCount(part.children());
    }
    return count;
  }

  @ParameterizedTest
  @ValueSource(ints = {41, 42, 64, 128, 256, 512})
  @DisplayName("Models with many separate cubes bake completely")
  void bakesModelsWithManyCubes(int cubeCount) throws Exception {
    ModelBakeResult result =
        ModelBakeService.createDefault()
            .bake(
                renderProfile("fingerprint_" + cubeCount),
                resourceManager(spacedCubeModel(cubeCount)));

    assertTrue(result.successful(), () -> "Bake failed: " + result.validationIssues());
    assertEquals(
        ModelRenderProfileStatus.ACTIVE,
        ModelRenderProfileStatus.statusForIssues(result.validationIssues()));
    assertEquals(cubeCount, cubeCount(result.bakedModel().rootParts()));
  }

  @Test
  @DisplayName("A solid cube stack keeps every visible cube and drops only fully hidden ones")
  void solidStackDropsOnlyFullyHiddenCubes() throws Exception {
    ModelBakeResult result =
        ModelBakeService.createDefault()
            .bake(renderProfile("solid"), resourceManager(solidBlockModel(4)));

    assertTrue(result.successful(), () -> "Bake failed: " + result.validationIssues());
    assertEquals(64 - 8, cubeCount(result.bakedModel().rootParts()));
  }

  @Test
  @DisplayName("Cubes outside of any group are reported instead of being dropped silently")
  void unusedCubesAreReported() throws Exception {
    String model =
        spacedCubeModel(64).replace(",\"element_62\",", ",").replace(",\"element_63\"]}]}", "]}]}");

    ModelBakeResult result =
        ModelBakeService.createDefault().bake(renderProfile("unused"), resourceManager(model));

    assertTrue(result.successful(), () -> "Bake failed: " + result.validationIssues());
    assertEquals(62, cubeCount(result.bakedModel().rootParts()));
    assertTrue(
        result.validationIssues().stream()
            .map(ModelRenderProfileValidationIssue::message)
            .anyMatch(message -> message.contains("not part of any group")),
        () -> "Missing warning about unused cubes: " + result.validationIssues());
  }

  @Test
  @DisplayName("A cube outside of the outliner groups fails with a clear message")
  void rootLevelCubeFailsWithClearMessage() throws Exception {
    String model =
        spacedCubeModel(8)
            .replace("\"outliner\":[{", "\"outliner\":[\"element_0\",{")
            .replace("\"element_0\",\"element_1\"", "\"element_1\"");

    ModelBakeResult result =
        ModelBakeService.createDefault().bake(renderProfile("root_cube"), resourceManager(model));

    assertEquals(
        ModelRenderProfileStatus.MODEL_DECODE_FAILED,
        ModelRenderProfileStatus.statusForIssues(result.validationIssues()));
    assertTrue(
        result.validationIssues().stream()
            .map(ModelRenderProfileValidationIssue::message)
            .anyMatch(message -> message.contains("must be placed inside a group")),
        () -> "Missing hint about groups: " + result.validationIssues());
  }
}
