package de.markusbordihn.easymodelentities.model.decoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import de.markusbordihn.easymodelentities.data.model.FaceUv;
import de.markusbordihn.easymodelentities.data.model.ModelCubeFace;
import de.markusbordihn.easymodelentities.data.model.decoder.DecodedModel;
import de.markusbordihn.easymodelentities.data.model.decoder.DecodedModelCube;
import de.markusbordihn.easymodelentities.data.model.decoder.DecodedModelPart;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.resources.Resource;
import org.junit.jupiter.api.Test;

class PerFaceUvMirrorTest {

  private static byte[] fixture(String name) throws Exception {
    try (InputStream in =
        PerFaceUvMirrorTest.class.getClassLoader().getResourceAsStream("bbmodel/" + name)) {
      return in.readAllBytes();
    }
  }

  private static Resource resource(byte[] bytes) {
    return new Resource(mock(PackResources.class), () -> new ByteArrayInputStream(bytes));
  }

  private static DecodedModelCube firstCube(String fixture, String modelName) throws Exception {
    DecodedModel model =
        new BlockbenchBbModelDecoder()
            .decode(
                new ResourceLocation("example", modelName),
                resource(fixture("examples/" + fixture)));
    DecodedModelPart root = model.rootParts().get(0);
    return root.cubes().isEmpty() ? root.children().get(0).cubes().get(0) : root.cubes().get(0);
  }

  private static void assertFace(FaceUv face, float minU, float minV, float maxU, float maxV) {
    assertEquals(minU, face.minU(), 1e-4f, "minU");
    assertEquals(minV, face.minV(), 1e-4f, "minV");
    assertEquals(maxU, face.maxU(), 1e-4f, "maxU");
    assertEquals(maxV, face.maxV(), 1e-4f, "maxV");
  }

  @Test
  void perFaceFormatKeepsRawTopAndBottom() throws Exception {
    DecodedModelCube cube = firstCube("orientation_test.bbmodel", "orientation_test");

    assertFace(cube.faceUvs().uv(ModelCubeFace.NORTH), 0f, 0f, 32f, 21f);
    assertFace(cube.faceUvs().uv(ModelCubeFace.UP), 32f, 64f, 0f, 42.5f);
    assertFace(cube.faceUvs().uv(ModelCubeFace.DOWN), 64f, 42.5f, 32f, 64f);
  }

  @Test
  void boxUvFormatLeavesTopAndBottomUnchanged() throws Exception {
    DecodedModelCube cube = firstCube("text_example.bbmodel", "text_example");

    assertFace(cube.faceUvs().uv(ModelCubeFace.UP), 18f, 1f, 17f, 0f);
    assertFace(cube.faceUvs().uv(ModelCubeFace.DOWN), 19f, 0f, 18f, 1f);
  }
}
