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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.markusbordihn.easymodelentities.data.model.FaceUv;
import de.markusbordihn.easymodelentities.data.model.Vec3f;
import de.markusbordihn.easymodelentities.data.model.bake.*;
import de.markusbordihn.easymodelentities.data.model.decoder.DecodedModel;
import de.markusbordihn.easymodelentities.data.model.decoder.DecodedModelCube;
import de.markusbordihn.easymodelentities.data.model.decoder.DecodedModelPart;
import de.markusbordihn.easymodelentities.data.profile.ModelBodyType;
import de.markusbordihn.easymodelentities.data.renderprofile.EasyModelRenderProfile;
import de.markusbordihn.easymodelentities.data.renderprofile.ModelAnimationMode;
import de.markusbordihn.easymodelentities.data.renderprofile.ModelAnimationSettings;
import de.markusbordihn.easymodelentities.data.renderprofile.ModelRenderProfileStatus;
import de.markusbordihn.easymodelentities.data.renderprofile.ModelRenderSettings;
import de.markusbordihn.easymodelentities.model.decoder.EasyModelDecodeException;
import de.markusbordihn.easymodelentities.model.decoder.EasyModelDecoder;
import de.markusbordihn.easymodelentities.model.decoder.ModelDecoderRegistry;
import de.markusbordihn.easymodelentities.registry.ModelResourcePaths;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.imageio.ImageIO;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.junit.jupiter.api.Test;

class ModelBakeServiceTest {

  private static final Identifier PROFILE_ID = Identifier.fromNamespaceAndPath("example", "model");
  private static final Identifier MODEL_ID =
      Identifier.fromNamespaceAndPath("example", "easy_model_entities/models/model");
  private static final Identifier TEXTURE_ID =
      Identifier.fromNamespaceAndPath("example", "textures/entity/model.png");

  private static ModelRenderProfileStatus status(ModelBakeResult result) {
    return ModelRenderProfileStatus.statusForIssues(result.validationIssues());
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

  private static EasyModelRenderProfile renderProfile(
      ModelBodyType bodyType, String version, String assetFingerprint) {
    return new EasyModelRenderProfile(
        PROFILE_ID,
        "1.0",
        version,
        bodyType,
        MODEL_ID,
        TEXTURE_ID,
        Map.of(),
        new ModelRenderSettings(1.0f, 0.3f, 0.0f, 0.0f, Vec3f.ZERO),
        new ModelAnimationSettings(ModelAnimationMode.AUTOMATIC, 1.0f, 1.0f),
        ModelRenderProfileStatus.ACTIVE,
        List.of(),
        assetFingerprint);
  }

  private static ResourceManager resourceManager(String modelFixture, byte[] textureBytes)
      throws IOException {
    return resourceManager(fixture(modelFixture), textureBytes);
  }

  private static ResourceManager resourceManager(byte[] modelBytes, byte[] textureBytes)
      throws IOException {
    ResourceManager resourceManager = mock(ResourceManager.class);
    when(resourceManager.getResource(ModelResourcePaths.modelResourceLocation(MODEL_ID)))
        .thenReturn(Optional.of(resource(modelBytes)));
    when(resourceManager.getResource(TEXTURE_ID)).thenReturn(Optional.of(resource(textureBytes)));
    return resourceManager;
  }

  private static byte[] fixture(String fixtureName) throws IOException {
    try (InputStream inputStream =
        ModelBakeServiceTest.class.getClassLoader().getResourceAsStream("bbmodel/" + fixtureName)) {
      if (inputStream == null) {
        throw new IOException("Missing fixture " + fixtureName);
      }

      return inputStream.readAllBytes();
    }
  }

  private static byte[] textureFixture(String fixtureName) throws IOException {
    try (InputStream inputStream =
        ModelBakeServiceTest.class
            .getClassLoader()
            .getResourceAsStream("textures/" + fixtureName)) {
      if (inputStream == null) {
        throw new IOException("Missing fixture " + fixtureName);
      }

      return inputStream.readAllBytes();
    }
  }

  private static byte[] png(int width, int height) throws IOException {
    BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    ImageIO.write(image, "png", outputStream);
    return outputStream.toByteArray();
  }

  private static Resource resource(byte[] bytes) throws IOException {
    PackResources packResources = mock(PackResources.class);
    return new Resource(packResources, () -> new ByteArrayInputStream(bytes));
  }

  private static String rotatedElementModel() {
    return "{\"meta\":{\"format_version\":\"5.0\",\"model_format\":\"modded_entity\"},"
        + "\"resolution\":{\"width\":64,\"height\":64},"
        + "\"elements\":[{\"name\":\"tilted\",\"from\":[-1,0,-1],\"to\":[1,2,1],"
        + "\"origin\":[0,1,0],\"rotation\":[90,0,0],\"uv_offset\":[2,4],"
        + "\"type\":\"cube\",\"uuid\":\"element_tilted\"}],"
        + "\"groups\":[{\"uuid\":\"group_root\",\"name\":\"root\","
        + "\"origin\":[0,0,0],\"rotation\":[0,0,0]}],"
        + "\"outliner\":[{\"uuid\":\"group_root\",\"children\":[\"element_tilted\"]}]}";
  }

  private static BakedModelCube cube(BakedModelPart rootPart, String partName, int cubeIndex) {
    return part(rootPart, partName).cubes().get(cubeIndex);
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

  private static void assertVec(float x, float y, float z, Vec3f actual) {
    assertEquals(x, actual.x(), 0.01f);
    assertEquals(y, actual.y(), 0.01f);
    assertEquals(z, actual.z(), 0.01f);
  }

  @Test
  void bakesValidBipedModel() throws Exception {
    ModelBakeService bakeService = new ModelBakeService(ModelDecoderRegistry.createDefault());

    ModelBakeResult result =
        bakeService.bake(
            renderProfile(ModelBodyType.BIPED, "fingerprint"),
            resourceManager("minimal_biped.bbmodel", png(64, 64)));

    assertTrue(result.successful());
    assertEquals(ModelRenderProfileStatus.ACTIVE, status(result));
    assertEquals(7, result.bakedModel().partCount());
    assertEquals(6, result.bakedModel().cubeCount());
    assertTrue(bakeService.getCached(MODEL_ID, "fingerprint").isPresent());
  }

  @Test
  void bakesBipedModelWithMixedCaseBoneNames() throws Exception {
    ModelBakeResult result =
        ModelBakeService.createDefault()
            .bake(
                renderProfile(ModelBodyType.BIPED, "fingerprint"),
                resourceManager("mixed_case_biped.bbmodel", png(64, 64)));

    assertTrue(result.successful());
    assertEquals(ModelRenderProfileStatus.ACTIVE, status(result));
    BakedModelPart root = result.bakedModel().rootParts().get(0);
    assertEquals("root", root.name());
    assertEquals("body", part(root, "body").name());
    assertEquals("left_arm", part(root, "left_arm").name());
    assertEquals("right_arm", part(root, "right_arm").name());
  }

  @Test
  void bakesValidQuadrupedModel() throws Exception {
    ModelBakeResult result =
        ModelBakeService.createDefault()
            .bake(
                renderProfile(ModelBodyType.QUADRUPED, "fingerprint"),
                resourceManager("minimal_quadruped.bbmodel", png(64, 64)));

    assertTrue(result.successful());
    assertEquals(7, result.bakedModel().partCount());
  }

  @Test
  void bakesStaticModelWithExplicitRoot() throws Exception {
    ModelBakeResult result =
        ModelBakeService.createDefault()
            .bake(
                renderProfile(ModelBodyType.STATIC, "fingerprint"),
                resourceManager("static_explicit_root.bbmodel", png(64, 64)));

    assertTrue(result.successful());
    assertEquals("root", result.bakedModel().rootParts().get(0).name());
  }

  @Test
  void staticModelWithoutRootCreatesVirtualRoot() throws Exception {
    ModelBakeResult result =
        ModelBakeService.createDefault()
            .bake(
                renderProfile(ModelBodyType.STATIC, "fingerprint"),
                resourceManager("static_virtual_root.bbmodel", png(64, 64)));

    assertTrue(result.successful());
    assertEquals("root", result.bakedModel().rootParts().get(0).name());
    assertEquals("crystal", result.bakedModel().rootParts().get(0).children().get(0).name());
  }

  @Test
  void missingRequiredBipedPartReturnsFallbackResult() throws Exception {
    ModelBakeResult result =
        ModelBakeService.createDefault()
            .bake(
                renderProfile(ModelBodyType.BIPED, "fingerprint"),
                resourceManager("missing_biped_part.bbmodel", png(64, 64)));

    assertFalse(result.successful());
    assertEquals(ModelRenderProfileStatus.CLIENT_BODY_TYPE_MISMATCH, status(result));
  }

  @Test
  void missingRequiredQuadrupedPartReturnsFallbackResult() throws Exception {
    ModelBakeResult result =
        ModelBakeService.createDefault()
            .bake(
                renderProfile(ModelBodyType.QUADRUPED, "fingerprint"),
                resourceManager("minimal_biped.bbmodel", png(64, 64)));

    assertFalse(result.successful());
    assertEquals(ModelRenderProfileStatus.CLIENT_BODY_TYPE_MISMATCH, status(result));
  }

  @Test
  void oversizedTextureReturnsFallbackResult() throws Exception {
    ModelBakeResult result =
        ModelBakeService.createDefault()
            .bake(
                renderProfile(ModelBodyType.STATIC, "fingerprint"),
                resourceManager("static_explicit_root.bbmodel", png(2049, 1)));

    assertFalse(result.successful());
    assertEquals(ModelRenderProfileStatus.CLIENT_ASSET_MISMATCH, status(result));
  }

  @Test
  void resolvesModelResourceThroughDecoderFormats() throws Exception {
    Identifier customModelResource = ModelResourcePaths.modelResourceLocation(MODEL_ID, "custom");
    ResourceManager resourceManager = mock(ResourceManager.class);
    when(resourceManager.getResource(customModelResource))
        .thenReturn(Optional.of(resource(new byte[] {1})));
    when(resourceManager.getResource(TEXTURE_ID)).thenReturn(Optional.of(resource(png(64, 64))));
    ModelBakeService bakeService =
        new ModelBakeService(new ModelDecoderRegistry(Map.of("custom", new StaticModelDecoder())));

    ModelBakeResult result =
        bakeService.bake(renderProfile(ModelBodyType.STATIC, "fingerprint"), resourceManager);

    assertTrue(result.successful());
    assertEquals(MODEL_ID, result.bakedModel().modelId());
  }

  @Test
  void decodeFailureIsCached() throws Exception {
    ModelBakeService bakeService = ModelBakeService.createDefault();

    ModelBakeResult result =
        bakeService.bake(
            renderProfile(ModelBodyType.STATIC, "fingerprint"),
            resourceManager("{}".getBytes(StandardCharsets.UTF_8), png(64, 64)));

    assertFalse(result.successful());
    assertEquals(ModelRenderProfileStatus.MODEL_DECODE_FAILED, status(result));
    assertTrue(bakeService.getCached(MODEL_ID, "fingerprint").isPresent());
  }

  @Test
  void bakesRotatedElementsAsChildParts() throws Exception {
    ModelBakeResult result =
        ModelBakeService.createDefault()
            .bake(
                renderProfile(ModelBodyType.STATIC, "fingerprint"),
                resourceManager(
                    rotatedElementModel().getBytes(StandardCharsets.UTF_8), png(64, 64)));

    BakedModelPart root = result.bakedModel().rootParts().get(0);
    BakedModelPart rotatedPart = root.children().get(0);
    BakedModelCube cube = rotatedPart.cubes().get(0);

    assertTrue(result.successful());
    assertEquals(0, root.cubes().size());
    assertEquals("tilted_r1", rotatedPart.name());
    assertVec(0.0f, -1.0f, 0.0f, rotatedPart.offset());
    assertVec(-1.5708f, 0.0f, 0.0f, rotatedPart.rotation());
    assertArrayEquals(new int[] {2, 4}, cube.uvOffset());
    assertVec(-1.0f, -1.0f, -1.0f, cube.position());
    assertVec(2.0f, 2.0f, 2.0f, cube.dimensions());
  }

  @Test
  void halfTurnCreatesRotatedChildPart() throws Exception {
    String model =
        "{\"meta\":{\"format_version\":\"5.0\",\"model_format\":\"modded_entity\"},"
            + "\"resolution\":{\"width\":64,\"height\":64},"
            + "\"elements\":[{\"name\":\"tilted\",\"from\":[-1,0,-1],\"to\":[1,2,1],"
            + "\"origin\":[0,1,0],\"rotation\":[-180,0,0],\"uv_offset\":[2,4],"
            + "\"type\":\"cube\",\"uuid\":\"element_tilted\"}],"
            + "\"groups\":[{\"uuid\":\"group_root\",\"name\":\"root\","
            + "\"origin\":[0,0,0],\"rotation\":[0,0,0]}],"
            + "\"outliner\":[{\"uuid\":\"group_root\",\"children\":[\"element_tilted\"]}]}";

    ModelBakeResult result =
        ModelBakeService.createDefault()
            .bake(
                renderProfile(ModelBodyType.STATIC, "fingerprint"),
                resourceManager(model.getBytes(StandardCharsets.UTF_8), png(64, 64)));

    BakedModelPart root = result.bakedModel().rootParts().get(0);
    BakedModelPart rotatedPart = root.children().get(0);
    BakedModelCube cube = rotatedPart.cubes().get(0);

    assertTrue(result.successful());
    assertEquals(0, root.cubes().size());
    assertEquals(1, root.children().size());
    assertEquals("tilted_r1", rotatedPart.name());
    assertVec(0.0f, -1.0f, 0.0f, rotatedPart.offset());
    assertVec(3.1416f, 0.0f, 0.0f, rotatedPart.rotation());
    assertVec(-1.0f, -1.0f, -1.0f, cube.position());
    assertVec(2.0f, 2.0f, 2.0f, cube.dimensions());
  }

  @Test
  void preservesBlockbenchFaceUvsFromTextureExport() throws Exception {
    ModelBakeResult result =
        ModelBakeService.createDefault()
            .bake(
                renderProfile(ModelBodyType.BIPED, "fingerprint"),
                resourceManager(
                    "little_explorer_texture.bbmodel",
                    textureFixture("little_explorer_texture.png")));

    BakedModelCube headCube = cube(result.bakedModel().rootParts().get(0), "head", 0);

    assertTrue(result.successful());
    assertEquals(new FaceUv(0.125f, 0.125f, 0.25f, 0.25f), headCube.faceUvs().north());
    assertEquals(new FaceUv(0.0f, 0.125f, 0.125f, 0.25f), headCube.faceUvs().east());
    assertEquals(new FaceUv(0.375f, 0.125f, 0.5f, 0.25f), headCube.faceUvs().south());
    assertEquals(new FaceUv(0.25f, 0.125f, 0.375f, 0.25f), headCube.faceUvs().west());
    assertEquals(new FaceUv(0.25f, 0.125f, 0.125f, 0.0f), headCube.faceUvs().up());
    assertEquals(new FaceUv(0.375f, 0.0f, 0.25f, 0.125f), headCube.faceUvs().down());
  }

  @Test
  void assetFingerprintIsUsedAsCacheDiscriminator() throws Exception {
    ModelBakeService bakeService = ModelBakeService.createDefault();

    ModelBakeResult result =
        bakeService.bake(
            renderProfile(ModelBodyType.STATIC, "v1", "fp1"),
            resourceManager("static_explicit_root.bbmodel", png(64, 64)));

    assertTrue(result.successful());
    assertTrue(bakeService.getCached(MODEL_ID, "fp1").isPresent());
    assertTrue(bakeService.getCached(MODEL_ID, "v1").isEmpty());
  }

  @Test
  void differentFingerprintsProduceSeparateCacheEntries() throws Exception {
    ModelBakeService bakeService = ModelBakeService.createDefault();

    bakeService.bake(
        renderProfile(ModelBodyType.STATIC, "v1", "fp1"),
        resourceManager("static_explicit_root.bbmodel", png(64, 64)));
    bakeService.bake(
        renderProfile(ModelBodyType.STATIC, "v1", "fp2"),
        resourceManager("static_explicit_root.bbmodel", png(64, 64)));

    assertEquals(2, bakeService.cachedResultCount());
  }

  @Test
  void blankFingerprintFallsBackToVersionDiscriminator() throws Exception {
    ModelBakeService bakeService = ModelBakeService.createDefault();

    bakeService.bake(
        renderProfile(ModelBodyType.STATIC, "v1", ""),
        resourceManager("static_explicit_root.bbmodel", png(64, 64)));

    assertTrue(bakeService.getCached(MODEL_ID, "v1").isPresent());
  }

  @Test
  void cacheKeyUsesNoFingerprintSentinel() {
    ModelCacheKey cacheKey = ModelBakeService.createDefault().cacheKey(MODEL_ID, "");

    assertEquals(ModelCacheKey.NO_FINGERPRINT, cacheKey.assetFingerprint());
  }

  @Test
  void cacheClearsOnReloadSimulation() throws Exception {
    ModelBakeService bakeService = ModelBakeService.createDefault();
    bakeService.bake(
        renderProfile(ModelBodyType.STATIC, "fingerprint"),
        resourceManager("static_explicit_root.bbmodel", png(64, 64)));

    assertEquals(1, bakeService.cachedResultCount());

    bakeService.clearCache();

    assertEquals(0, bakeService.cachedResultCount());
    assertTrue(bakeService.getCached(MODEL_ID, "fingerprint").isEmpty());
  }

  private static final class StaticModelDecoder implements EasyModelDecoder {

    @Override
    public boolean supports(Identifier modelId, Resource resource) {
      return true;
    }

    @Override
    public DecodedModel decode(Identifier modelId, Resource resource)
        throws EasyModelDecodeException {
      return new DecodedModel(
          modelId,
          64,
          64,
          List.of(
              new DecodedModelPart(
                  "root",
                  new Vec3f(0.0f, 24.0f, 0.0f),
                  Vec3f.ZERO,
                  List.of(
                      new DecodedModelCube(
                          new int[] {0, 0},
                          new Vec3f(-1.0f, -1.0f, -1.0f),
                          new Vec3f(2.0f, 2.0f, 2.0f),
                          false)),
                  List.of())),
          List.of());
    }
  }
}
