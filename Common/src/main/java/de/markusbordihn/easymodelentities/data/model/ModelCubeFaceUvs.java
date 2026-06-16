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

package de.markusbordihn.easymodelentities.data.model;

import java.util.Objects;

public record ModelCubeFaceUvs(
    FaceUv north, FaceUv east, FaceUv south, FaceUv west, FaceUv up, FaceUv down) {

  public static ModelCubeFaceUvs fromBoxUv(int[] uvOffset, float[] dimensions) {
    Objects.requireNonNull(uvOffset, "uvOffset");
    Objects.requireNonNull(dimensions, "dimensions");
    float u = uvOffset[0];
    float v = uvOffset[1];
    float width = dimensions[0];
    float height = dimensions[1];
    float depth = dimensions[2];
    return new ModelCubeFaceUvs(
        new FaceUv(u + depth, v + depth, u + depth + width, v + depth + height),
        new FaceUv(u, v + depth, u + depth, v + depth + height),
        new FaceUv(
            u + depth + width + depth,
            v + depth,
            u + depth + width + depth + width,
            v + depth + height),
        new FaceUv(u + depth + width, v + depth, u + depth + width + depth, v + depth + height),
        new FaceUv(u + depth, v + depth, u + depth + width, v),
        new FaceUv(u + depth + width, v, u + depth + width + width, v + depth));
  }

  public ModelCubeFaceUvs scale(float textureWidth, float textureHeight) {
    return new ModelCubeFaceUvs(
        north.scale(textureWidth, textureHeight),
        east.scale(textureWidth, textureHeight),
        south.scale(textureWidth, textureHeight),
        west.scale(textureWidth, textureHeight),
        up.scale(textureWidth, textureHeight),
        down.scale(textureWidth, textureHeight));
  }

  public FaceUv uv(ModelCubeFace face) {
    return switch (face) {
      case NORTH -> this.north;
      case EAST -> this.east;
      case SOUTH -> this.south;
      case WEST -> this.west;
      case UP -> this.up;
      case DOWN -> this.down;
    };
  }
}
