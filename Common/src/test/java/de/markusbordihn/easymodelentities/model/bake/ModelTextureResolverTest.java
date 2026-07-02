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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.markusbordihn.easymodelentities.data.model.ModelCubeFaceUvs;
import de.markusbordihn.easymodelentities.data.model.Vec3f;
import de.markusbordihn.easymodelentities.data.model.decoder.DecodedModel;
import de.markusbordihn.easymodelentities.data.model.decoder.DecodedModelCube;
import de.markusbordihn.easymodelentities.data.model.decoder.DecodedModelPart;
import de.markusbordihn.easymodelentities.data.model.decoder.DecodedTexture;
import de.markusbordihn.easymodelentities.data.profile.ModelBodyType;
import de.markusbordihn.easymodelentities.data.renderprofile.EasyModelRenderProfile;
import de.markusbordihn.easymodelentities.data.renderprofile.ModelAnimationMode;
import de.markusbordihn.easymodelentities.data.renderprofile.ModelAnimationSettings;
import de.markusbordihn.easymodelentities.data.renderprofile.ModelRenderProfileStatus;
import de.markusbordihn.easymodelentities.data.renderprofile.ModelRenderSettings;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.imageio.ImageIO;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.junit.jupiter.api.Test;

class ModelTextureResolverTest {

  private static final Identifier PROFILE_ID = Identifier.fromNamespaceAndPath("example", "model");
  private static final Identifier MODEL_ID =
      Identifier.fromNamespaceAndPath("example", "easy_model_entities/models/model");
  private static final Identifier DEFAULT_TEXTURE =
      Identifier.fromNamespaceAndPath("example", "textures/entity/model.png");

  private static boolean hasMissingTexture(ModelTextureResolver.ResolvedTextures resolved) {
    return resolved.issues().stream()
        .anyMatch(issue -> issue.status() == ModelRenderProfileStatus.MISSING_TEXTURE);
  }

  private static DecodedModel decodedModel(DecodedTexture secondTexture) {
    DecodedModelPart root =
        new DecodedModelPart(
            "root", new Vec3f(0.0f, 24.0f, 0.0f), Vec3f.ZERO, List.of(cube(0), cube(1)), List.of());
    return new DecodedModel(
        MODEL_ID,
        16,
        16,
        List.of(root),
        List.of(
            new DecodedTexture(0, "", "block", "model_a.png", "model_a.png", 16, 16),
            secondTexture),
        List.of());
  }

  private static DecodedModelCube cube(int textureIndex) {
    int[] uvOffset = {0, 0};
    return new DecodedModelCube(
        uvOffset,
        ModelCubeFaceUvs.fromBoxUv(uvOffset, new float[] {2.0f, 2.0f, 2.0f}),
        new Vec3f(-1.0f, -1.0f, -1.0f),
        new Vec3f(2.0f, 2.0f, 2.0f),
        false,
        "cube",
        Vec3f.ZERO,
        Vec3f.ZERO,
        new Vec3f(-1.0f, -1.0f, -1.0f),
        textureIndex,
        de.markusbordihn.easymodelentities.data.model.CubeFaceVisibility.ALL);
  }

  private static EasyModelRenderProfile profile(Map<Integer, Identifier> textures) {
    return new EasyModelRenderProfile(
        PROFILE_ID,
        "1.0",
        "",
        ModelBodyType.STATIC,
        MODEL_ID,
        DEFAULT_TEXTURE,
        textures,
        new ModelRenderSettings(1.0f, 0.3f, 0.0f, 0.0f, Vec3f.ZERO),
        new ModelAnimationSettings(ModelAnimationMode.NONE, 1.0f, 1.0f),
        ModelRenderProfileStatus.ACTIVE,
        List.of());
  }

  private static ResourceManager resourceManager(Identifier... existing) throws IOException {
    ResourceManager resourceManager = mock(ResourceManager.class);
    for (Identifier location : existing) {
      when(resourceManager.getResource(location)).thenReturn(Optional.of(resource(png())));
    }
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

  @Test
  void resolvesProfileMappingAndDerivedTexture() throws IOException {
    Identifier customTexture =
        Identifier.fromNamespaceAndPath("example", "textures/entity/custom.png");
    DecodedModel model =
        decodedModel(new DecodedTexture(1, "minecraft", "block", "chest.png", "chest.png", 16, 16));
    EasyModelRenderProfile profile = profile(Map.of(1, customTexture));
    ResourceManager resourceManager = resourceManager(DEFAULT_TEXTURE, customTexture);

    ModelTextureResolver.ResolvedTextures resolved =
        ModelTextureResolver.resolve(profile, model, resourceManager);

    assertEquals(DEFAULT_TEXTURE, resolved.textures().get(0));
    assertEquals(customTexture, resolved.textures().get(1));
    assertFalse(hasMissingTexture(resolved));
  }

  @Test
  void derivesTextureFromBbmodelWhenProfileMappingMissing() throws IOException {
    Identifier derived = Identifier.fromNamespaceAndPath("minecraft", "textures/block/chest.png");
    DecodedModel model =
        decodedModel(new DecodedTexture(1, "minecraft", "block", "chest.png", "chest.png", 16, 16));
    EasyModelRenderProfile profile = profile(Map.of());
    ResourceManager resourceManager = resourceManager(DEFAULT_TEXTURE, derived);

    ModelTextureResolver.ResolvedTextures resolved =
        ModelTextureResolver.resolve(profile, model, resourceManager);

    assertEquals(derived, resolved.textures().get(1));
    assertFalse(hasMissingTexture(resolved));
  }

  @Test
  void fallsBackAndWarnsWhenTextureMissing() throws IOException {
    DecodedModel model =
        decodedModel(new DecodedTexture(1, "minecraft", "block", "chest.png", "chest.png", 16, 16));
    EasyModelRenderProfile profile = profile(Map.of());
    ResourceManager resourceManager = resourceManager(DEFAULT_TEXTURE);

    ModelTextureResolver.ResolvedTextures resolved =
        ModelTextureResolver.resolve(profile, model, resourceManager);

    assertEquals(ModelTextureResolver.FALLBACK_TEXTURE, resolved.textures().get(1));
    assertTrue(hasMissingTexture(resolved));
  }
}
