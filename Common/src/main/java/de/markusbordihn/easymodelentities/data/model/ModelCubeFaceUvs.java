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
    float[] north, float[] east, float[] south, float[] west, float[] up, float[] down) {

  public ModelCubeFaceUvs {
    north = copy(north, "north");
    east = copy(east, "east");
    south = copy(south, "south");
    west = copy(west, "west");
    up = copy(up, "up");
    down = copy(down, "down");
  }

  public static ModelCubeFaceUvs fromBoxUv(int[] uvOffset, float[] dimensions) {
    Objects.requireNonNull(uvOffset, "uvOffset");
    Objects.requireNonNull(dimensions, "dimensions");
    float u = uvOffset[0];
    float v = uvOffset[1];
    float width = dimensions[0];
    float height = dimensions[1];
    float depth = dimensions[2];
    return new ModelCubeFaceUvs(
        new float[] {u + depth, v + depth, u + depth + width, v + depth + height},
        new float[] {u, v + depth, u + depth, v + depth + height},
        new float[] {
          u + depth + width + depth,
          v + depth,
          u + depth + width + depth + width,
          v + depth + height
        },
        new float[] {u + depth + width, v + depth, u + depth + width + depth, v + depth + height},
        new float[] {u + depth + width, v + depth, u + depth, v},
        new float[] {u + depth + width + width, v, u + depth + width, v + depth});
  }

  private static float[] copy(float[] uv, String face) {
    Objects.requireNonNull(uv, face);
    if (uv.length != 4) {
      throw new IllegalArgumentException("UV face " + face + " must have 4 values.");
    }

    return uv.clone();
  }

  @Override
  public float[] north() {
    return this.north.clone();
  }

  @Override
  public float[] east() {
    return this.east.clone();
  }

  @Override
  public float[] south() {
    return this.south.clone();
  }

  @Override
  public float[] west() {
    return this.west.clone();
  }

  @Override
  public float[] up() {
    return this.up.clone();
  }

  @Override
  public float[] down() {
    return this.down.clone();
  }

  public float[] uv(ModelCubeFace face) {
    return switch (face) {
      case NORTH -> this.north.clone();
      case EAST -> this.east.clone();
      case SOUTH -> this.south.clone();
      case WEST -> this.west.clone();
      case UP -> this.up.clone();
      case DOWN -> this.down.clone();
    };
  }
}
