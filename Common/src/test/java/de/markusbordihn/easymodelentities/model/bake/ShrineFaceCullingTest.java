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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.markusbordihn.easymodelentities.data.model.Vec3f;
import de.markusbordihn.easymodelentities.data.model.bake.BakedModel;
import de.markusbordihn.easymodelentities.data.model.bake.BakedModelCube;
import de.markusbordihn.easymodelentities.data.model.bake.BakedModelPart;
import de.markusbordihn.easymodelentities.data.model.bake.ModelBakeResult;
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
import java.util.List;
import java.util.Optional;
import javax.imageio.ImageIO;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.junit.jupiter.api.Test;

class ShrineFaceCullingTest {

  private static int visibleFaces(List<BakedModelPart> parts) {
    int total = 0;
    for (BakedModelPart part : parts) {
      for (BakedModelCube cube : part.cubes()) {
        total += cube.faceVisibility().visibleCount();
      }
      total += visibleFaces(part.children());
    }
    return total;
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
        new ModelRenderSettings(1.0f, 0.3f, 0.0f, 0.0f, Vec3f.ZERO),
        new ModelAnimationSettings(ModelAnimationMode.AUTOMATIC, 1.0f, 1.0f),
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

  private static byte[] fixture(String fixtureName) throws IOException {
    try (InputStream inputStream =
        ShrineFaceCullingTest.class
            .getClassLoader()
            .getResourceAsStream("bbmodel/examples/" + fixtureName + ".bbmodel")) {
      if (inputStream == null) {
        throw new IOException("Missing fixture " + fixtureName);
      }
      return inputStream.readAllBytes();
    }
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

  @Test
  void shrineLosesHiddenFacesAndEnablesBackfaceCulling() throws Exception {
    ResourceLocation modelId = new ResourceLocation("example", "shrine");
    byte[] modelBytes = fixture("shrine");
    ModelBakeResult bakeResult =
        ModelBakeService.createDefault()
            .bake(
                renderProfile(modelId, ModelBodyType.STATIC), resourceManager(modelId, modelBytes));

    assertTrue(bakeResult.successful());
    BakedModel bakedModel = bakeResult.bakedModel();
    assertTrue(bakedModel.cullBackfaces(), "static model should enable backface culling");

    int cubeCount = bakedModel.cubeCount();
    int visibleFaces = visibleFaces(bakedModel.rootParts());
    assertTrue(cubeCount > 0, "expected baked cubes");
    assertTrue(
        visibleFaces < cubeCount * 6,
        "expected hidden faces to be culled, got " + visibleFaces + " of " + (cubeCount * 6));
  }

  @Test
  void animatedModelKeepsBackfaceCullingDisabled() throws Exception {
    ResourceLocation modelId = new ResourceLocation("example", "little_explorer");
    byte[] modelBytes = fixture("little_explorer");
    ModelBakeResult bakeResult =
        ModelBakeService.createDefault()
            .bake(
                renderProfile(modelId, ModelBodyType.BIPED), resourceManager(modelId, modelBytes));

    assertTrue(bakeResult.successful());
    assertFalse(
        bakeResult.bakedModel().cullBackfaces(),
        "animated model should keep double-sided rendering");
  }
}
