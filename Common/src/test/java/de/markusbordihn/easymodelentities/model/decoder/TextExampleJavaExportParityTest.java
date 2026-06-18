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
import static org.mockito.Mockito.mock;

import de.markusbordihn.easymodelentities.data.model.Vec3f;
import de.markusbordihn.easymodelentities.data.model.decoder.DecodedModel;
import de.markusbordihn.easymodelentities.data.model.decoder.DecodedModelCube;
import de.markusbordihn.easymodelentities.data.model.decoder.DecodedModelPart;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.resources.Resource;
import org.junit.jupiter.api.Test;

class TextExampleJavaExportParityTest {

  private static byte[] fixture(String name) throws Exception {
    try (InputStream in =
        TextExampleJavaExportParityTest.class
            .getClassLoader()
            .getResourceAsStream("bbmodel/" + name)) {
      return in.readAllBytes();
    }
  }

  private static Resource resource(byte[] bytes) {
    return new Resource(mock(PackResources.class), () -> new ByteArrayInputStream(bytes));
  }

  private static void assertVec(Vec3f actual, float x, float y, float z) {
    assertEquals(x, actual.x(), 1e-4f, "x");
    assertEquals(y, actual.y(), 1e-4f, "y");
    assertEquals(z, actual.z(), 1e-4f, "z");
  }

  private static void assertCubeOrigins(DecodedModelPart part, float[]... origins) {
    List<DecodedModelCube> cubes = part.cubes();
    assertEquals(origins.length, cubes.size(), part.name() + " cube count");
    for (int index = 0; index < origins.length; index++) {
      Vec3f position = cubes.get(index).position();
      float[] expected = origins[index];
      assertEquals(expected[0], position.x(), 1e-4f, part.name() + " cube " + index + " x");
      assertEquals(expected[1], position.y(), 1e-4f, part.name() + " cube " + index + " y");
      assertEquals(expected[2], position.z(), 1e-4f, part.name() + " cube " + index + " z");
    }
  }

  @Test
  void matchesBlockbenchJavaExport() throws Exception {
    DecodedModel model =
        new BlockbenchBbModelDecoder()
            .decode(
                ResourceLocation.fromNamespaceAndPath("example", "text_example"),
                resource(fixture("examples/text_example.bbmodel")));

    DecodedModelPart root = model.rootParts().get(0);
    assertEquals("root", root.name());
    assertVec(root.offset(), -3.0f, 24.0f, 0.0f);
    assertCubeOrigins(root, new float[] {2.5f, -1.0f, -8.0f});

    Map<String, DecodedModelPart> letters =
        root.children().stream().collect(Collectors.toMap(DecodedModelPart::name, part -> part));

    assertVec(letters.get("h").offset(), 0.0f, 0.0f, 0.0f);
    assertCubeOrigins(
        letters.get("h"),
        new float[] {-8.0f, -6.0f, -1.0f},
        new float[] {-5.0f, -6.0f, -1.0f},
        new float[] {-7.0f, -3.75f, -1.0f});

    assertVec(letters.get("e").offset(), 2.0f, 0.0f, 0.0f);
    assertCubeOrigins(
        letters.get("e"),
        new float[] {-5.0f, -6.0f, -1.0f},
        new float[] {-4.0f, -6.0f, -1.0f},
        new float[] {-4.0f, -1.0f, -1.0f},
        new float[] {-4.0f, -3.75f, -1.0f});

    assertVec(letters.get("l").offset(), 4.0f, 0.0f, 0.0f);
    assertCubeOrigins(
        letters.get("l"), new float[] {-2.0f, -1.0f, -1.0f}, new float[] {-3.0f, -6.0f, -1.0f});

    assertVec(letters.get("l2").offset(), 8.0f, 0.0f, 0.0f);
    assertCubeOrigins(
        letters.get("l2"), new float[] {-2.0f, -1.0f, -1.0f}, new float[] {-3.0f, -6.0f, -1.0f});

    assertVec(letters.get("o").offset(), 17.0f, 0.0f, 0.0f);
    assertCubeOrigins(
        letters.get("o"),
        new float[] {-8.0f, -6.0f, -1.0f},
        new float[] {-5.0f, -6.0f, -1.0f},
        new float[] {-7.0f, -1.0f, -1.0f},
        new float[] {-7.0f, -6.0f, -1.0f});
  }
}
