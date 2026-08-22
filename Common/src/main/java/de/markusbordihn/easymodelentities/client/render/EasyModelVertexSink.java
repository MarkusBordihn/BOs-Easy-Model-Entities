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

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

final class EasyModelVertexSink {

  private final VertexConsumer vertexConsumer;
  private final PoseStack poseStack;
  private final int packedLight;
  private final int packedOverlay;
  private final int alpha;

  EasyModelVertexSink(
      VertexConsumer vertexConsumer,
      PoseStack poseStack,
      int packedLight,
      int packedOverlay,
      int alpha) {
    this.vertexConsumer = vertexConsumer;
    this.poseStack = poseStack;
    this.packedLight = packedLight;
    this.packedOverlay = packedOverlay;
    this.alpha = alpha;
  }

  void vertex(
      float x, float y, float z, float u, float v, float normalX, float normalY, float normalZ) {
    PoseStack.Pose pose = this.poseStack.last();
    this.vertexConsumer.addVertex(pose.pose(), x, y, z);
    this.vertexConsumer.setColor(255, 255, 255, this.alpha);
    this.vertexConsumer.setUv(u, v);
    this.vertexConsumer.setOverlay(this.packedOverlay);
    this.vertexConsumer.setLight(this.packedLight);
    this.vertexConsumer.setNormal(pose, normalX, normalY, normalZ);
  }
}
