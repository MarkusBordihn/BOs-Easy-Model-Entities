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

package de.markusbordihn.easymodelentities.client.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.markusbordihn.easymodelentities.api.data.EasyModelTextureBlend;
import de.markusbordihn.easymodelentities.api.data.EasyModelTextureSetting;
import de.markusbordihn.easymodelentities.api.data.EasyModelTextureSlot;
import de.markusbordihn.easymodelentities.data.model.Vec3f;
import de.markusbordihn.easymodelentities.data.model.bake.BakedModel;
import de.markusbordihn.easymodelentities.data.model.bake.BakedModelPart;
import de.markusbordihn.easymodelentities.data.model.bake.ModelBounds;
import de.markusbordihn.easymodelentities.data.profile.ModelBodyType;
import de.markusbordihn.easymodelentities.data.render.EasyModelRenderState;
import de.markusbordihn.easymodelentities.data.renderprofile.ModelAnimationMode;
import de.markusbordihn.easymodelentities.data.renderprofile.ModelAnimationSettings;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.imageio.ImageIO;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class EasyModelTextureOverridesTest {

  private static final ResourceLocation MODEL_ID =
      ResourceLocation.fromNamespaceAndPath("example", "echo");
  private static final ResourceLocation BODY =
      ResourceLocation.fromNamespaceAndPath("example", "textures/entity/echo.png");
  private static final ResourceLocation SCREEN =
      ResourceLocation.fromNamespaceAndPath("example", "textures/entity/echo/screen_default.png");
  private static final ResourceLocation SAD =
      ResourceLocation.fromNamespaceAndPath("example", "textures/entity/echo/screen_sad.png");
  private static final ResourceLocation HUGE =
      ResourceLocation.fromNamespaceAndPath("example", "textures/entity/echo/screen_huge.png");
  private static final ResourceLocation UNKNOWN =
      ResourceLocation.fromNamespaceAndPath("example", "textures/entity/echo/missing.png");

  private static EasyModelRenderState renderState(boolean fallbackModel, boolean fallbackTexture) {
    BakedModel bakedModel =
        new BakedModel(
            MODEL_ID,
            64,
            64,
            List.of(new BakedModelPart("root", Vec3f.ZERO, Vec3f.ZERO, List.of(), List.of())),
            Map.of(0, BODY, 1, SCREEN),
            false,
            Map.of(),
            ModelBounds.EMPTY,
            Map.of(EasyModelTextureSetting.DEFAULT_SLOT, 0, "screen", 1));
    return new EasyModelRenderState(
        bakedModel,
        BODY,
        Map.of(0, BODY, 1, SCREEN),
        1.0f,
        0.3f,
        0.0f,
        0.0f,
        Vec3f.ZERO,
        ModelBodyType.STATIC,
        new ModelAnimationSettings(ModelAnimationMode.NONE, 1.0f, 1.0f),
        fallbackModel,
        fallbackTexture,
        List.of());
  }

  private static ResourceManager resourceManager() throws IOException {
    ResourceManager resourceManager = mock(ResourceManager.class);
    when(resourceManager.getResource(SAD)).thenReturn(Optional.of(resource(png(16, 16))));
    when(resourceManager.getResource(HUGE)).thenReturn(Optional.of(resource(png(4096, 16))));
    return resourceManager;
  }

  private static byte[] png(int width, int height) throws IOException {
    BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    ImageIO.write(image, "png", outputStream);
    return outputStream.toByteArray();
  }

  private static Resource resource(byte[] bytes) {
    PackResources packResources = mock(PackResources.class);
    return new Resource(packResources, () -> new ByteArrayInputStream(bytes));
  }

  @BeforeEach
  void resetCache() {
    EasyModelTextureOverrides.clear();
  }

  @Test
  @DisplayName("Without an override the renderer keeps its untouched base textures")
  void emptySettingAllocatesNothing() throws IOException {
    assertSame(
        EasyModelResolvedTextures.NONE,
        EasyModelTextureOverrides.resolve(
            renderState(false, false), EasyModelTextureSetting.EMPTY, resourceManager()));
  }

  @Test
  void namedSlotHitsItsTextureIndex() throws IOException {
    EasyModelResolvedTextures resolved =
        EasyModelTextureOverrides.resolve(
            renderState(false, false),
            EasyModelTextureSetting.of("screen", SAD),
            resourceManager());

    assertEquals(Map.of(1, new EasyModelTextureSlot(SAD)), resolved.byIndex());
  }

  @Test
  void defaultSlotHitsIndexZero() throws IOException {
    EasyModelResolvedTextures resolved =
        EasyModelTextureOverrides.resolve(
            renderState(false, false),
            EasyModelTextureSetting.of(EasyModelTextureSetting.DEFAULT_SLOT, SAD),
            resourceManager());

    assertEquals(Map.of(0, new EasyModelTextureSlot(SAD)), resolved.byIndex());
  }

  @Test
  @DisplayName("A model without slot names stays addressable by texture index")
  void indexEscapesHitTheirTextureIndex() throws IOException {
    assertEquals(
        Map.of(1, new EasyModelTextureSlot(SAD)),
        EasyModelTextureOverrides.resolve(
                renderState(false, false), EasyModelTextureSetting.of("#1", SAD), resourceManager())
            .byIndex());
    assertEquals(
        Map.of(1, new EasyModelTextureSlot(SAD)),
        EasyModelTextureOverrides.resolve(
                renderState(false, false), EasyModelTextureSetting.of("1", SAD), resourceManager())
            .byIndex());
  }

  @Test
  void unknownSlotAndUnknownIndexAreIgnored() throws IOException {
    assertTrue(
        EasyModelTextureOverrides.resolve(
                renderState(false, false),
                EasyModelTextureSetting.of("visor", SAD),
                resourceManager())
            .isEmpty());
    assertTrue(
        EasyModelTextureOverrides.resolve(
                renderState(false, false), EasyModelTextureSetting.of("#7", SAD), resourceManager())
            .isEmpty());
  }

  @Test
  @DisplayName("A fallback render state keeps its diagnostic look instead of an override")
  void fallbackStateIsNeverOverridden() throws IOException {
    assertSame(
        EasyModelResolvedTextures.NONE,
        EasyModelTextureOverrides.resolve(
            renderState(true, false),
            EasyModelTextureSetting.of("screen", SAD),
            resourceManager()));
    assertSame(
        EasyModelResolvedTextures.NONE,
        EasyModelTextureOverrides.resolve(
            renderState(false, true),
            EasyModelTextureSetting.of("screen", SAD),
            resourceManager()));
  }

  @Test
  void missingAndOversizedTexturesKeepTheBaseTexture() throws IOException {
    assertTrue(
        EasyModelTextureOverrides.resolve(
                renderState(false, false),
                EasyModelTextureSetting.of("screen", UNKNOWN),
                resourceManager())
            .isEmpty());
    assertTrue(
        EasyModelTextureOverrides.resolve(
                renderState(false, false),
                EasyModelTextureSetting.of("screen", HUGE),
                resourceManager())
            .isEmpty());
  }

  @Test
  void repeatedResolutionIsCached() throws IOException {
    EasyModelTextureSetting setting = EasyModelTextureSetting.of("screen", SAD);
    ResourceManager resourceManager = resourceManager();

    assertSame(
        EasyModelTextureOverrides.resolve(renderState(false, false), setting, resourceManager),
        EasyModelTextureOverrides.resolve(renderState(false, false), setting, resourceManager));
  }

  @Test
  void blendReachesTheTextureIndexOfItsSlot() throws IOException {
    EasyModelResolvedTextures resolved =
        EasyModelTextureOverrides.resolve(
            renderState(false, false),
            EasyModelTextureSetting.EMPTY.withSlot(
                "screen", SAD, EasyModelTextureBlend.TRANSLUCENT),
            resourceManager());

    assertEquals(EasyModelTextureBlend.TRANSLUCENT, resolved.blend(1));
    assertEquals(EasyModelTextureBlend.CUTOUT, resolved.blend(0));
  }

  @Test
  @DisplayName("A blend without a texture reaches the renderer, so a slot can stay as it is")
  void blendWithoutATextureIsResolved() throws IOException {
    EasyModelResolvedTextures resolved =
        EasyModelTextureOverrides.resolve(
            renderState(false, false),
            EasyModelTextureSetting.EMPTY.withBlend("screen", EasyModelTextureBlend.TRANSLUCENT),
            resourceManager());

    assertEquals(EasyModelTextureBlend.TRANSLUCENT, resolved.blend(1));
    assertNull(resolved.texture(1));
  }

  @Test
  @DisplayName("A rejected texture keeps its blend, so the slot does not lose both at once")
  void rejectedTextureKeepsItsBlend() throws IOException {
    EasyModelResolvedTextures resolved =
        EasyModelTextureOverrides.resolve(
            renderState(false, false),
            EasyModelTextureSetting.EMPTY.withSlot(
                "screen", HUGE, EasyModelTextureBlend.TRANSLUCENT),
            resourceManager());

    assertEquals(EasyModelTextureBlend.TRANSLUCENT, resolved.blend(1));
    assertNull(resolved.texture(1));
  }

  @Test
  @DisplayName("Variant discovery starts at the texture the slot currently shows")
  void baseTextureResolvesPerSlot() {
    EasyModelRenderState renderState = renderState(false, false);

    assertEquals(Optional.of(SCREEN), EasyModelTextureOverrides.baseTexture(renderState, "screen"));
    assertEquals(Optional.of(SCREEN), EasyModelTextureOverrides.baseTexture(renderState, "#1"));
    assertEquals(
        Optional.of(BODY),
        EasyModelTextureOverrides.baseTexture(renderState, EasyModelTextureSetting.DEFAULT_SLOT));
  }

  @Test
  void baseTextureOfAnUnknownSlotIsEmpty() {
    EasyModelRenderState renderState = renderState(false, false);

    assertTrue(EasyModelTextureOverrides.baseTexture(renderState, "visor").isEmpty());
    assertTrue(EasyModelTextureOverrides.baseTexture(renderState, "#7").isEmpty());
    assertTrue(EasyModelTextureOverrides.baseTexture(renderState, "").isEmpty());
  }
}
