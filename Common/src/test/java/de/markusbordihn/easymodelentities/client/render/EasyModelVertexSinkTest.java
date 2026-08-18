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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;

class EasyModelVertexSinkTest {

  @Test
  void forwardsPackedLightAndOverlay() {
    VertexConsumer vertexConsumer = mock(VertexConsumer.class, Answers.RETURNS_SELF);
    EasyModelVertexSink sink =
        new EasyModelVertexSink(vertexConsumer, new PoseStack(), 0x00120034, 0x00560078);
    sink.vertex(1.0f, 2.0f, 3.0f, 0.25f, 0.75f, 0.0f, 1.0f, 0.0f);

    verify(vertexConsumer).uv2(0x00120034);
    verify(vertexConsumer).overlayCoords(0x00560078);
  }
}
