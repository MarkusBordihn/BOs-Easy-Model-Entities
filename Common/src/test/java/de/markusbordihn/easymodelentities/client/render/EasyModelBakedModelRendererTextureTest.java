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
import java.util.List;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class EasyModelBakedModelRendererTextureTest {

  private static final ResourceLocation BODY =
      new ResourceLocation("example", "textures/entity/echo.png");
  private static final ResourceLocation SCREEN =
      new ResourceLocation("example", "textures/entity/echo/screen_default.png");
  private static final ResourceLocation SAD =
      new ResourceLocation("example", "textures/entity/echo/screen_sad.png");

  private static EasyModelRenderState renderState(Map<Integer, ResourceLocation> textures) {
    BakedModel bakedModel =
        new BakedModel(
            new ResourceLocation("example", "echo"),
            64,
            64,
            List.of(new BakedModelPart("root", Vec3f.ZERO, Vec3f.ZERO, List.of(), List.of())),
            textures,
            true,
            Map.of(),
            ModelBounds.EMPTY,
            Map.of(EasyModelTextureSetting.DEFAULT_SLOT, 0, "screen", 1));
    return new EasyModelRenderState(
        bakedModel,
        BODY,
        textures,
        1.0f,
        0.3f,
        0.0f,
        0.0f,
        Vec3f.ZERO,
        ModelBodyType.STATIC,
        new ModelAnimationSettings(ModelAnimationMode.NONE, 1.0f, 1.0f),
        false,
        false,
        List.of());
  }

  @Test
  void withoutAnOverrideEveryTextureIndexKeepsItsBaseTexture() {
    EasyModelRenderState renderState = renderState(Map.of(0, BODY, 1, SCREEN));

    assertEquals(
        BODY,
        EasyModelBakedModelRenderer.textureFor(EasyModelResolvedTextures.NONE, renderState, 0));
    assertEquals(
        SCREEN,
        EasyModelBakedModelRenderer.textureFor(EasyModelResolvedTextures.NONE, renderState, 1));
    assertEquals(
        BODY,
        EasyModelBakedModelRenderer.textureFor(EasyModelResolvedTextures.NONE, renderState, 7));
  }

  @Test
  @DisplayName("An override replaces only the texture of its own index")
  void overrideReplacesOnlyItsOwnTextureIndex() {
    EasyModelRenderState renderState = renderState(Map.of(0, BODY, 1, SCREEN));
    EasyModelResolvedTextures resolvedTextures =
        EasyModelResolvedTextures.of(Map.of(1, new EasyModelTextureSlot(SAD)));

    assertEquals(BODY, EasyModelBakedModelRenderer.textureFor(resolvedTextures, renderState, 0));
    assertEquals(SAD, EasyModelBakedModelRenderer.textureFor(resolvedTextures, renderState, 1));
  }

  @Test
  @DisplayName("A blend without a texture keeps the texture of the model, only the blend changes")
  void blendOnlyOverrideKeepsTheBaseTexture() {
    EasyModelRenderState renderState = renderState(Map.of(0, BODY, 1, SCREEN));
    EasyModelResolvedTextures resolvedTextures =
        EasyModelResolvedTextures.of(
            Map.of(1, EasyModelTextureSlot.of(EasyModelTextureBlend.TRANSLUCENT)));

    assertEquals(SCREEN, EasyModelBakedModelRenderer.textureFor(resolvedTextures, renderState, 1));
    assertEquals(EasyModelTextureBlend.TRANSLUCENT, resolvedTextures.blend(1));
    assertEquals(EasyModelTextureBlend.CUTOUT, resolvedTextures.blend(0));
  }

  @Test
  @DisplayName("A single texture model keeps working, with and without an override")
  void singleTextureModelUsesItsOnlyTexture() {
    EasyModelRenderState renderState = renderState(Map.of());

    assertEquals(
        BODY,
        EasyModelBakedModelRenderer.textureFor(EasyModelResolvedTextures.NONE, renderState, 0));
    assertEquals(
        SAD,
        EasyModelBakedModelRenderer.textureFor(
            EasyModelResolvedTextures.of(Map.of(0, new EasyModelTextureSlot(SAD))),
            renderState,
            0));
  }
}
