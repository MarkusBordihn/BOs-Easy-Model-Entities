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

package de.markusbordihn.easymodelentities.api.data.client;

import com.mojang.blaze3d.vertex.PoseStack;
import de.markusbordihn.easymodelentities.api.data.EasyModelVec3f;
import java.util.Objects;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public record EasyModelPartPose(
    String partName,
    Matrix4f poseMatrix,
    Matrix3f normalMatrix,
    EasyModelVec3f cubeMin,
    EasyModelVec3f cubeMax) {

  public EasyModelPartPose {
    partName = Objects.requireNonNull(partName, "partName").trim();
    if (partName.isEmpty()) {
      throw new IllegalArgumentException("partName must not be blank.");
    }
    poseMatrix = new Matrix4f(Objects.requireNonNull(poseMatrix, "poseMatrix"));
    normalMatrix = new Matrix3f(Objects.requireNonNull(normalMatrix, "normalMatrix"));
    Objects.requireNonNull(cubeMin, "cubeMin");
    Objects.requireNonNull(cubeMax, "cubeMax");
  }

  public void applyTo(PoseStack poseStack) {
    Objects.requireNonNull(poseStack, "poseStack");
    poseStack.last().pose().set(this.poseMatrix);
    poseStack.last().normal().set(this.normalMatrix);
  }

  @Override
  public Matrix4f poseMatrix() {
    return new Matrix4f(this.poseMatrix);
  }

  @Override
  public Matrix3f normalMatrix() {
    return new Matrix3f(this.normalMatrix);
  }
}
