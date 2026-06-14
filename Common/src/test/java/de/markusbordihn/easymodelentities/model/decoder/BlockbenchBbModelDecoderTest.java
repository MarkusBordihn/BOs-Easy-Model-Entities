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

import de.markusbordihn.easymodelentities.data.model.decoder.*;
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
