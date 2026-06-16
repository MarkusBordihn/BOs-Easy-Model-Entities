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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.markusbordihn.easymodelentities.data.model.Vec3f;
import de.markusbordihn.easymodelentities.data.model.bake.*;
import de.markusbordihn.easymodelentities.data.profile.ModelBodyType;
import de.markusbordihn.easymodelentities.data.renderprofile.EasyModelRenderProfile;
import de.markusbordihn.easymodelentities.data.renderprofile.ModelAnimationMode;
import de.markusbordihn.easymodelentities.data.renderprofile.ModelAnimationSettings;
import de.markusbordihn.easymodelentities.data.renderprofile.ModelRenderProfileStatus;
import de.markusbordihn.easymodelentities.data.renderprofile.ModelRenderSettings;
import de.markusbordihn.easymodelentities.registry.ModelResourcePaths;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.imageio.ImageIO;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.junit.jupiter.api.Test;

class BundledExampleGeometryTest {

  private static final float DELTA = 0.001f;

  private static void assertGeometry(String fixtureName, ModelBodyType bodyType) throws Exception {
    byte[] modelBytes = fixture(fixtureName);
    ExpectedModel expectedModel = expectedModel(modelBytes);
    ResourceLocation modelId = new ResourceLocation("example", fixtureName);
    ModelBakeResult bakeResult =
        ModelBakeService.createDefault()
            .bake(renderProfile(modelId, bodyType), resourceManager(modelId, modelBytes));

    assertTrue(bakeResult.successful());
    assertParts(expectedModel.rootParts(), bakeResult.bakedModel().rootParts(), fixtureName);
  }

  private static void assertParts(
      List<ExpectedPart> expectedParts, List<BakedModelPart> bakedParts, String path) {
    assertEquals(expectedParts.size(), bakedParts.size(), path + " part count");
    for (int index = 0; index < expectedParts.size(); index++) {
      ExpectedPart expectedPart = expectedParts.get(index);
      BakedModelPart bakedPart = bakedParts.get(index);
      String partPath = path + "/" + expectedPart.name();

      assertEquals(expectedPart.name(), bakedPart.name(), partPath + " name");
      assertVec(expectedPart.offset(), bakedPart.offset(), DELTA, partPath + " offset");
      assertVec(expectedPart.rotation(), bakedPart.rotation(), DELTA, partPath + " rotation");
      assertCubes(expectedPart.cubes(), bakedPart.cubes(), partPath);
      assertParts(expectedPart.children(), bakedPart.children(), partPath);
    }
  }

  private static void assertCubes(
      List<ExpectedCube> expectedCubes, List<BakedModelCube> bakedCubes, String path) {
    assertEquals(expectedCubes.size(), bakedCubes.size(), path + " cube count");
    for (int index = 0; index < expectedCubes.size(); index++) {
      ExpectedCube expectedCube = expectedCubes.get(index);
      BakedModelCube bakedCube = bakedCubes.get(index);
      String cubePath = path + "/cube[" + index + "]";

      assertVec(expectedCube.position(), bakedCube.position(), DELTA, cubePath + " position");
      assertVec(expectedCube.dimensions(), bakedCube.dimensions(), DELTA, cubePath + " dimensions");
    }
  }

  private static void assertVec(float[] expected, Vec3f actual, float delta, String message) {
    assertArrayEquals(expected, new float[] {actual.x(), actual.y(), actual.z()}, delta, message);
  }

  private static EasyModelRenderProfile renderProfile(
      ResourceLocation modelId, ModelBodyType bodyType) {
    return new EasyModelRenderProfile(
        modelId,
        "0.1.0",
        "test-version",
        bodyType,
        modelId,
        new ResourceLocation("example", "textures/entity/" + modelId.getPath() + ".png"),
        new ModelRenderSettings(1.0f, 0.3f, 1.0f, 1.0f, 0.0f, 0.5f, 0.0f),
        new ModelAnimationSettings(
            ModelAnimationMode.AUTOMATIC, "idle", "walk", "run", "hurt", "death", 1.0f, 1.0f),
        ModelRenderProfileStatus.ACTIVE,
        List.of());
  }

  private static ResourceManager resourceManager(ResourceLocation modelId, byte[] modelBytes)
      throws IOException {
    ResourceManager resourceManager = mock(ResourceManager.class);
    when(resourceManager.getResource(ModelResourcePaths.modelResourceLocation(modelId)))
        .thenReturn(Optional.of(resource(modelBytes)));
    when(resourceManager.getResource(
            new ResourceLocation("example", "textures/entity/" + modelId.getPath() + ".png")))
        .thenReturn(Optional.of(resource(png())));
    return resourceManager;
  }

  private static ExpectedModel expectedModel(byte[] modelBytes) throws IOException {
    JsonObject root;
    try (InputStreamReader reader =
        new InputStreamReader(new ByteArrayInputStream(modelBytes), StandardCharsets.UTF_8)) {
      root = JsonParser.parseReader(reader).getAsJsonObject();
    }

    Map<String, RawGroup> groupsByUuid = groups(root.getAsJsonArray("groups"));
    Map<String, RawElement> elementsByUuid = elements(root.getAsJsonArray("elements"));
    List<ExpectedPart> rootParts = new ArrayList<>();
    for (JsonElement element : root.getAsJsonArray("outliner")) {
      rootParts.add(expectedPart(element.getAsJsonObject(), groupsByUuid, elementsByUuid, null));
    }

    return new ExpectedModel(rootParts);
  }

  private static ExpectedPart expectedPart(
      JsonObject node,
      Map<String, RawGroup> groupsByUuid,
      Map<String, RawElement> elementsByUuid,
      RawGroup parentGroup) {
    RawGroup group = groupsByUuid.get(node.get("uuid").getAsString());
    List<ExpectedCube> cubes = new ArrayList<>();
    List<ExpectedPart> children = new ArrayList<>();
    JsonArray childElements = node.getAsJsonArray("children");
    if (childElements != null) {
      for (JsonElement childElement : childElements) {
        if (childElement.isJsonPrimitive()) {
          cubes.add(expectedCube(elementsByUuid.get(childElement.getAsString()), group));
        } else {
          children.add(
              expectedPart(childElement.getAsJsonObject(), groupsByUuid, elementsByUuid, group));
        }
      }
    }

    return new ExpectedPart(
        group.name(),
        groupOffset(group, parentGroup),
        rotationRadians(group.rotation()),
        cubes,
        children);
  }

  private static ExpectedCube expectedCube(RawElement element, RawGroup group) {
    float[] from = element.from();
    float[] to = element.to();
    float[] groupOrigin = group.origin();
    return new ExpectedCube(
        new float[] {groupOrigin[0] - to[0], groupOrigin[1] - to[1], from[2] - groupOrigin[2]},
        new float[] {to[0] - from[0], to[1] - from[1], to[2] - from[2]});
  }

  private static float[] groupOffset(RawGroup group, RawGroup parentGroup) {
    float[] groupOrigin = group.origin();
    if (parentGroup == null) {
      return new float[] {-groupOrigin[0], 24.0f - groupOrigin[1], groupOrigin[2]};
    }

    float[] parentOrigin = parentGroup.origin();
    return new float[] {
      parentOrigin[0] - groupOrigin[0],
      parentOrigin[1] - groupOrigin[1],
      groupOrigin[2] - parentOrigin[2]
    };
  }

  private static float[] rotationRadians(float[] rotation) {
    return new float[] {
      (float) Math.toRadians(rotation[0]),
      (float) Math.toRadians(rotation[1]),
      (float) Math.toRadians(rotation[2])
    };
  }

  private static Map<String, RawGroup> groups(JsonArray groupsArray) {
    Map<String, RawGroup> groups = new HashMap<>();
    for (JsonElement element : groupsArray) {
      JsonObject group = element.getAsJsonObject();
      RawGroup rawGroup =
          new RawGroup(
              group.get("name").getAsString(),
              floatArray(group.getAsJsonArray("origin")),
              optionalFloatArray(group.getAsJsonArray("rotation")));
      groups.put(group.get("uuid").getAsString(), rawGroup);
    }

    return groups;
  }

  private static Map<String, RawElement> elements(JsonArray elementsArray) {
    Map<String, RawElement> elements = new HashMap<>();
    for (JsonElement element : elementsArray) {
      JsonObject cube = element.getAsJsonObject();
      elements.put(
          cube.get("uuid").getAsString(),
          new RawElement(
              floatArray(cube.getAsJsonArray("from")), floatArray(cube.getAsJsonArray("to"))));
    }

    return elements;
  }

  private static float[] floatArray(JsonArray jsonArray) {
    float[] values = new float[3];
    for (int index = 0; index < values.length; index++) {
      values[index] = jsonArray.get(index).getAsFloat();
    }

    return values;
  }

  private static float[] optionalFloatArray(JsonArray jsonArray) {
    return jsonArray == null ? new float[] {0.0f, 0.0f, 0.0f} : floatArray(jsonArray);
  }

  private static byte[] fixture(String fixtureName) throws IOException {
    try (InputStream inputStream =
        BundledExampleGeometryTest.class
            .getClassLoader()
            .getResourceAsStream("bbmodel/examples/" + fixtureName + ".bbmodel")) {
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

  private static byte[] png() throws IOException {
    BufferedImage image = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    ImageIO.write(image, "png", outputStream);
    return outputStream.toByteArray();
  }

  @Test
  void bundledExamplesMatchGeneratedMinecraftGeometry() throws Exception {
    assertGeometry("training_dummy", ModelBodyType.STATIC);
    assertGeometry("little_explorer", ModelBodyType.BIPED);
    assertGeometry("stone_turtle", ModelBodyType.QUADRUPED);
  }

  private record ExpectedModel(List<ExpectedPart> rootParts) {}

  private record ExpectedPart(
      String name,
      float[] offset,
      float[] rotation,
      List<ExpectedCube> cubes,
      List<ExpectedPart> children) {}

  private record ExpectedCube(float[] position, float[] dimensions) {}

  private record RawGroup(String name, float[] origin, float[] rotation) {}

  private record RawElement(float[] from, float[] to) {}
}
