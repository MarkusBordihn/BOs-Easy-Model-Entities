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

public record EasyModelPartTransform(
    float xRotation,
    float yRotation,
    float zRotation,
    float offsetX,
    float offsetY,
    float offsetZ,
    float scaleX,
    float scaleY,
    float scaleZ,
    boolean visible) {

  public static final EasyModelPartTransform NONE = new EasyModelPartTransform(0.0f, 0.0f, 0.0f);

  public EasyModelPartTransform {
    requireFinite(xRotation, "xRotation");
    requireFinite(yRotation, "yRotation");
    requireFinite(zRotation, "zRotation");
    requireFinite(offsetX, "offsetX");
    requireFinite(offsetY, "offsetY");
    requireFinite(offsetZ, "offsetZ");
    requireFinite(scaleX, "scaleX");
    requireFinite(scaleY, "scaleY");
    requireFinite(scaleZ, "scaleZ");
  }

  public EasyModelPartTransform(float xRotation, float yRotation, float zRotation) {
    this(xRotation, yRotation, zRotation, 0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f, true);
  }

  private static void requireFinite(float value, String name) {
    if (!Float.isFinite(value)) {
      throw new IllegalArgumentException(name + " must be finite.");
    }
  }

  public EasyModelPartTransform add(EasyModelPartTransform transform) {
    java.util.Objects.requireNonNull(transform, "transform");
    if (NONE.equals(transform)) {
      return this;
    }

    if (NONE.equals(this)) {
      return transform;
    }

    return new EasyModelPartTransform(
        this.xRotation + transform.xRotation,
        this.yRotation + transform.yRotation,
        this.zRotation + transform.zRotation,
        this.offsetX + transform.offsetX,
        this.offsetY + transform.offsetY,
        this.offsetZ + transform.offsetZ,
        this.scaleX * transform.scaleX,
        this.scaleY * transform.scaleY,
        this.scaleZ * transform.scaleZ,
        this.visible && transform.visible);
  }

  public EasyModelPartTransform withOffset(float x, float y, float z) {
    return new EasyModelPartTransform(
        this.xRotation,
        this.yRotation,
        this.zRotation,
        x,
        y,
        z,
        this.scaleX,
        this.scaleY,
        this.scaleZ,
        this.visible);
  }

  public EasyModelPartTransform withScale(float x, float y, float z) {
    return new EasyModelPartTransform(
        this.xRotation,
        this.yRotation,
        this.zRotation,
        this.offsetX,
        this.offsetY,
        this.offsetZ,
        x,
        y,
        z,
        this.visible);
  }

  public EasyModelPartTransform withScale(float scale) {
    return withScale(scale, scale, scale);
  }

  public EasyModelPartTransform withVisible(boolean visible) {
    return new EasyModelPartTransform(
        this.xRotation,
        this.yRotation,
        this.zRotation,
        this.offsetX,
        this.offsetY,
        this.offsetZ,
        this.scaleX,
        this.scaleY,
        this.scaleZ,
        visible);
  }

  public boolean isIdentity() {
    return this.xRotation == 0.0f
        && this.yRotation == 0.0f
        && this.zRotation == 0.0f
        && this.offsetX == 0.0f
        && this.offsetY == 0.0f
        && this.offsetZ == 0.0f
        && this.scaleX == 1.0f
        && this.scaleY == 1.0f
        && this.scaleZ == 1.0f
        && this.visible;
  }
}
