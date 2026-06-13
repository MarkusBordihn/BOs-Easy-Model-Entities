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

package de.markusbordihn.easymodelentities.client.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import de.markusbordihn.easymodelentities.model.ModelCubeFaceUvs;
import de.markusbordihn.easymodelentities.model.bake.BakedModel;
import de.markusbordihn.easymodelentities.model.bake.BakedModelCube;
import de.markusbordihn.easymodelentities.model.bake.BakedModelPart;
import de.markusbordihn.easymodelentities.profile.ModelBodyType;
import de.markusbordihn.easymodelentities.render.EasyModelRenderState;
import de.markusbordihn.easymodelentities.renderprofile.ModelAnimationMode;
import de.markusbordihn.easymodelentities.renderprofile.ModelAnimationSettings;
import java.util.List;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;

class EasyModelBakedModelRendererTest {

  private static ModelCubeFaceUvs faceUvs() {
    return new ModelCubeFaceUvs(
        new float[] {0.0f, 0.0f, 1.0f, 1.0f},
        new float[] {2.0f, 0.0f, 3.0f, 1.0f},
        new float[] {4.0f, 0.0f, 5.0f, 1.0f},
        new float[] {6.0f, 0.0f, 7.0f, 1.0f},
        new float[] {8.0f, 0.0f, 9.0f, 1.0f},
        new float[] {10.0f, 0.0f, 11.0f, 1.0f});
  }

  private static EasyModelRenderState renderState(BakedModel bakedModel) {
    return new EasyModelRenderState(
        bakedModel,
        new ResourceLocation("example", "textures/entity/uv_model.png"),
        1.0f,
        0.3f,
        ModelBodyType.STATIC,
        new ModelAnimationSettings(ModelAnimationMode.NONE, "", "", "", "", "", 1.0f, 1.0f),
        false,
        false,
        List.of());
  }

  private static void assertUv(
      List<Float> uValues, List<Float> vValues, int index, float expectedU, float expectedV) {
    assertEquals(expectedU / 64.0f, uValues.get(index), 0.0001f);
    assertEquals(expectedV / 64.0f, vValues.get(index), 0.0001f);
  }

  private static void assertNormal(
      List<Float> xValues,
      List<Float> yValues,
      List<Float> zValues,
      int index,
      float expectedX,
      float expectedY,
      float expectedZ) {
    assertEquals(expectedX, xValues.get(index), 0.0001f);
    assertEquals(expectedY, yValues.get(index), 0.0001f);
    assertEquals(expectedZ, zValues.get(index), 0.0001f);
  }

  @Test
  void rendersDistinctFaceUvs() {
    BakedModel bakedModel =
        new BakedModel(
            new ResourceLocation("example", "uv_model"),
            64,
            64,
            List.of(
                new BakedModelPart(
                    "root",
                    new float[] {0.0f, 0.0f, 0.0f},
                    new float[] {0.0f, 0.0f, 0.0f},
                    List.of(
                        new BakedModelCube(
                            new int[] {0, 0},
                            faceUvs(),
                            new float[] {0.0f, 0.0f, 0.0f},
                            new float[] {1.0f, 1.0f, 1.0f},
                            false)),
                    List.of())));
    VertexConsumer vertexConsumer = mock(VertexConsumer.class, Answers.RETURNS_SELF);
    ArgumentCaptor<Float> uCaptor = ArgumentCaptor.forClass(Float.class);
    ArgumentCaptor<Float> vCaptor = ArgumentCaptor.forClass(Float.class);
    ArgumentCaptor<Float> normalXCaptor = ArgumentCaptor.forClass(Float.class);
    ArgumentCaptor<Float> normalYCaptor = ArgumentCaptor.forClass(Float.class);
    ArgumentCaptor<Float> normalZCaptor = ArgumentCaptor.forClass(Float.class);

    EasyModelBakedModelRenderer.render(
        bakedModel, renderState(bakedModel), 0.0f, 0.0f, new PoseStack(), vertexConsumer, 0);

    verify(vertexConsumer, times(24)).uv(uCaptor.capture(), vCaptor.capture());
    verify(vertexConsumer, times(24))
        .normal(any(), normalXCaptor.capture(), normalYCaptor.capture(), normalZCaptor.capture());
    assertUv(uCaptor.getAllValues(), vCaptor.getAllValues(), 0, 4.0f, 0.0f);
    assertUv(uCaptor.getAllValues(), vCaptor.getAllValues(), 4, 0.0f, 0.0f);
    assertUv(uCaptor.getAllValues(), vCaptor.getAllValues(), 8, 6.0f, 0.0f);
    assertUv(uCaptor.getAllValues(), vCaptor.getAllValues(), 12, 2.0f, 0.0f);
    assertUv(uCaptor.getAllValues(), vCaptor.getAllValues(), 16, 8.0f, 0.0f);
    assertUv(uCaptor.getAllValues(), vCaptor.getAllValues(), 20, 10.0f, 0.0f);
    assertNormal(
        normalXCaptor.getAllValues(),
        normalYCaptor.getAllValues(),
        normalZCaptor.getAllValues(),
        16,
        0.0f,
        -1.0f,
        0.0f);
    assertNormal(
        normalXCaptor.getAllValues(),
        normalYCaptor.getAllValues(),
        normalZCaptor.getAllValues(),
        20,
        0.0f,
        1.0f,
        0.0f);
  }
}
