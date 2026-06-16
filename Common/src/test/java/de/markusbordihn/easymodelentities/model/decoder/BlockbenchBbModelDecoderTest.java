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
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package de.markusbordihn.easymodelentities.model.decoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import de.markusbordihn.easymodelentities.data.model.FaceUv;
import de.markusbordihn.easymodelentities.data.model.ModelCubeFace;
import de.markusbordihn.easymodelentities.data.model.decoder.DecodedModel;
import de.markusbordihn.easymodelentities.data.model.decoder.DecodedModelCube;
import de.markusbordihn.easymodelentities.data.model.decoder.DecodedModelPart;
import de.markusbordihn.easymodelentities.data.renderprofile.ModelRenderProfileStatus;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.resources.Resource;
import org.junit.jupiter.api.Test;

class BlockbenchBbModelDecoderTest {

  private static final ResourceLocation MODEL_ID = new ResourceLocation("example", "model");

  private static byte[] fixture(String fixtureName) throws IOException {
    try (InputStream inputStream =
        BlockbenchBbModelDecoderTest.class
            .getClassLoader()
            .getResourceAsStream("bbmodel/" + fixtureName)) {
      if (inputStream == null) {
        throw new IOException("Missing fixture " + fixtureName);
      }

      return inputStream.readAllBytes();
    }
  }

  private static Resource resource(String value) {
    return resource(value.getBytes(StandardCharsets.UTF_8));
  }

  private static String generatedModel(int groupCount, int cubeCount, int hierarchyDepth) {
    StringBuilder groups = new StringBuilder();
    for (int index = 0; index < groupCount; index++) {
      if (index > 0) {
        groups.append(',');
      }
      groups.append(
          "{\"uuid\":\"group_"
              + index
              + "\",\"name\":\"part_"
              + index
              + "\",\"origin\":[0,0,0],\"rotation\":[0,0,0]}");
    }

    StringBuilder elements = new StringBuilder();
    for (int index = 0; index < cubeCount; index++) {
      if (index > 0) {
        elements.append(',');
      }
      elements.append(
          "{\"name\":\"cube_"
              + index
              + "\",\"from\":[0,0,0],\"to\":[1,1,1],\"origin\":[0,0,0],"
              + "\"uv_offset\":[0,0],\"type\":\"cube\",\"uuid\":\"element_"
              + index
              + "\"}");
    }

    return "{\"meta\":{\"format_version\":\"5.0\",\"model_format\":\"modded_entity\"},"
        + "\"resolution\":{\"width\":64,\"height\":64},"
        + "\"elements\":["
        + elements
        + "],\"groups\":["
        + groups
        + "],\"outliner\":["
        + nestedOutlinerNode(Math.max(1, hierarchyDepth), 0)
        + "]}";
  }

  private static String nestedOutlinerNode(int hierarchyDepth, int index) {
    if (index + 1 >= hierarchyDepth) {
      return "{\"uuid\":\"group_" + index + "\",\"children\":[]}";
    }

    return "{\"uuid\":\"group_"
        + index
        + "\",\"children\":["
        + nestedOutlinerNode(hierarchyDepth, index + 1)
        + "]}";
  }

  private static Resource resource(byte[] bytes) {
    PackResources packResources = mock(PackResources.class);
    return new Resource(packResources, () -> new ByteArrayInputStream(bytes));
  }

  @Test
  void defaultRegistryChoosesBlockbenchDecoder() {
    EasyModelDecoderRegistry registry = ModelDecoderRegistry.createDefault();

    assertTrue(registry.hasDecoder(BlockbenchBbModelDecoder.FORMAT));
    assertTrue(registry.getDecoder(BlockbenchBbModelDecoder.FORMAT).isPresent());
    Optional<EasyModelDecoder> decoder = registry.findDecoder(MODEL_ID, resource("{}"));

    assertTrue(decoder.isPresent());
    assertEquals(BlockbenchBbModelDecoder.class, decoder.get().getClass());
  }

  @Test
  void decodesValidMinimalBipedModel() throws Exception {
    DecodedModel model =
        new BlockbenchBbModelDecoder().decode(MODEL_ID, resource(fixture("minimal_biped.bbmodel")));

    assertEquals(64, model.textureWidth());
    assertEquals(64, model.textureHeight());
    assertEquals(7, model.boneCount());
    assertEquals(6, model.cubeCount());
    assertEquals("root", model.rootParts().get(0).name());
  }

  @Test
  void reportsSoftTextureBudgetWarning() throws Exception {
    DecodedModel model =
        new BlockbenchBbModelDecoder()
            .decode(
                MODEL_ID,
                resource(generatedModel(1, 0, 1).replace("\"width\":64", "\"width\":129")));

    assertTrue(
        model.validationIssues().stream()
            .anyMatch(
                issue ->
                    issue.status() == ModelRenderProfileStatus.ACTIVE
                        && "texture".equals(issue.field())));
  }

  @Test
  void decodesEmeEntityMultiTextureModel() throws Exception {
    DecodedModel model =
        new BlockbenchBbModelDecoder()
            .decode(MODEL_ID, resource(fixture("eme_entity_multi_texture.bbmodel")));

    assertEquals(2, model.textures().size());
    assertEquals(0, model.textures().get(0).index());
    assertEquals("minecraft", model.textures().get(1).namespace());
    assertEquals(32, model.textures().get(1).uvWidth());

    DecodedModelPart root = model.rootParts().get(0);
    assertEquals(0, root.cubes().get(0).textureIndex());
    assertEquals(1, root.cubes().get(1).textureIndex());
  }

  @Test
  void emeEntityWarnsOnMixedFaceTextures() throws Exception {
    String mixed =
        new String(fixture("eme_entity_multi_texture.bbmodel"), StandardCharsets.UTF_8)
            .replaceFirst(
                "\"north\": \\{\"uv\": \\[2, 2, 4, 4\\], \"texture\": 0\\}",
                "\"north\": {\"uv\": [2, 2, 4, 4], \"texture\": 1}");
    DecodedModel model = new BlockbenchBbModelDecoder().decode(MODEL_ID, resource(mixed));

    assertTrue(
        model.validationIssues().stream()
            .anyMatch(
                issue ->
                    issue.status() == ModelRenderProfileStatus.ACTIVE
                        && "texture".equals(issue.field())));
  }

  /**
   * Verifies that per-face UV coordinates from the eme_entity format are preserved exactly as
   * defined in the bbmodel file. In particular, NORTH and SOUTH faces must NOT be horizontally
   * mirrored — a bug that existed when the renderer's vertex winding order was reversed for those
   * faces.
   *
   * <p>The fixture defines each face with a unique, non-overlapping UV region. After parsing and
   * scaling (uv_width=64, uv_height=32), each face's UV is verified against the expected normalised
   * values.
   */
  @Test
  void emeEntityPerFaceUvIsNotMirrored() throws Exception {
    DecodedModel model =
        new BlockbenchBbModelDecoder()
            .decode(MODEL_ID, resource(fixture("eme_entity_face_uv.bbmodel")));

    DecodedModelPart root = model.rootParts().get(0);
    DecodedModelCube cube = root.cubes().get(0);

    // The decoder returns raw pixel-space UV values (not normalised to [0,1]).
    // Normalisation to [0,1] happens later in ModelBakeService using uv_width/uv_height.
    // Fixture uv_width=64, uv_height=32.

    // NORTH: bbmodel uv [0, 0, 16, 8]
    FaceUv north = cube.faceUvs().uv(ModelCubeFace.NORTH);
    assertEquals(0f, north.minU(), 1e-4f, "north minU");
    assertEquals(0f, north.minV(), 1e-4f, "north minV");
    assertEquals(16f, north.maxU(), 1e-4f, "north maxU");
    assertEquals(8f, north.maxV(), 1e-4f, "north maxV");

    // SOUTH: bbmodel uv [16, 0, 32, 8]
    FaceUv south = cube.faceUvs().uv(ModelCubeFace.SOUTH);
    assertEquals(16f, south.minU(), 1e-4f, "south minU");
    assertEquals(0f, south.minV(), 1e-4f, "south minV");
    assertEquals(32f, south.maxU(), 1e-4f, "south maxU");
    assertEquals(8f, south.maxV(), 1e-4f, "south maxV");

    // EAST: bbmodel uv [32, 0, 48, 8]
    FaceUv east = cube.faceUvs().uv(ModelCubeFace.EAST);
    assertEquals(32f, east.minU(), 1e-4f, "east minU");
    assertEquals(0f, east.minV(), 1e-4f, "east minV");
    assertEquals(48f, east.maxU(), 1e-4f, "east maxU");
    assertEquals(8f, east.maxV(), 1e-4f, "east maxV");

    // WEST: bbmodel uv [48, 0, 64, 8]
    FaceUv west = cube.faceUvs().uv(ModelCubeFace.WEST);
    assertEquals(48f, west.minU(), 1e-4f, "west minU");
    assertEquals(0f, west.minV(), 1e-4f, "west minV");
    assertEquals(64f, west.maxU(), 1e-4f, "west maxU");
    assertEquals(8f, west.maxV(), 1e-4f, "west maxV");

    // UP: bbmodel uv [0, 8, 16, 16]
    FaceUv up = cube.faceUvs().uv(ModelCubeFace.UP);
    assertEquals(0f, up.minU(), 1e-4f, "up minU");
    assertEquals(8f, up.minV(), 1e-4f, "up minV");
    assertEquals(16f, up.maxU(), 1e-4f, "up maxU");
    assertEquals(16f, up.maxV(), 1e-4f, "up maxV");

    // DOWN: bbmodel uv [16, 8, 32, 16]
    FaceUv down = cube.faceUvs().uv(ModelCubeFace.DOWN);
    assertEquals(16f, down.minU(), 1e-4f, "down minU");
    assertEquals(8f, down.minV(), 1e-4f, "down minV");
    assertEquals(32f, down.maxU(), 1e-4f, "down maxU");
    assertEquals(16f, down.maxV(), 1e-4f, "down maxV");
  }

  @Test
  void rejectsUnsupportedModelFormat() {
    BlockbenchBbModelDecoder decoder = new BlockbenchBbModelDecoder();
    String unsupported = generatedModel(1, 0, 1).replace("\"modded_entity\"", "\"bedrock\"");

    assertThrows(
        EasyModelDecodeException.class, () -> decoder.decode(MODEL_ID, resource(unsupported)));
  }

  @Test
  void rejectsCyclicHierarchy() {
    BlockbenchBbModelDecoder decoder = new BlockbenchBbModelDecoder();

    assertThrows(
        EasyModelDecodeException.class,
        () -> decoder.decode(MODEL_ID, resource(fixture("cyclic_hierarchy.bbmodel"))));
  }

  @Test
  void rejectsOversizedTextureMetadata() {
    BlockbenchBbModelDecoder decoder = new BlockbenchBbModelDecoder();

    assertThrows(
        EasyModelDecodeException.class,
        () -> decoder.decode(MODEL_ID, resource(fixture("oversized_texture_metadata.bbmodel"))));
  }

  @Test
  void rejectsExcessBoneBudget() {
    BlockbenchBbModelDecoder decoder = new BlockbenchBbModelDecoder();

    assertThrows(
        EasyModelDecodeException.class,
        () ->
            decoder.decode(
                MODEL_ID,
                resource(generatedModel(BlockbenchBbModelDecoder.MAX_BONE_COUNT + 1, 0, 1))));
  }

  @Test
  void rejectsExcessCubeBudget() {
    BlockbenchBbModelDecoder decoder = new BlockbenchBbModelDecoder();

    assertThrows(
        EasyModelDecodeException.class,
        () ->
            decoder.decode(
                MODEL_ID,
                resource(generatedModel(1, BlockbenchBbModelDecoder.MAX_CUBE_COUNT + 1, 1))));
  }

  @Test
  void rejectsExcessHierarchyDepth() {
    BlockbenchBbModelDecoder decoder = new BlockbenchBbModelDecoder();

    assertThrows(
        EasyModelDecodeException.class,
        () ->
            decoder.decode(
                MODEL_ID,
                resource(generatedModel(40, 0, BlockbenchBbModelDecoder.MAX_HIERARCHY_DEPTH + 1))));
  }

  @Test
  void rejectsOversizedModelFile() {
    byte[] oversizedModel = new byte[BlockbenchBbModelDecoder.MAX_MODEL_FILE_SIZE_BYTES + 1];
    BlockbenchBbModelDecoder decoder = new BlockbenchBbModelDecoder();

    assertThrows(
        EasyModelDecodeException.class, () -> decoder.decode(MODEL_ID, resource(oversizedModel)));
  }
}
