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

package de.markusbordihn.easymodelentities.data.model;

public record CubeFaceVisibility(int mask) {

  public static final CubeFaceVisibility NONE = new CubeFaceVisibility(0);
  private static final int ALL_MASK = 0b111111;
  public static final CubeFaceVisibility ALL = new CubeFaceVisibility(ALL_MASK);

  public CubeFaceVisibility {
    mask &= ALL_MASK;
  }

  private static int bit(ModelCubeFace face) {
    return 1 << face.ordinal();
  }

  public boolean isVisible(ModelCubeFace face) {
    return (this.mask & bit(face)) != 0;
  }

  public CubeFaceVisibility without(ModelCubeFace face) {
    return new CubeFaceVisibility(this.mask & ~bit(face));
  }

  public int visibleCount() {
    return Integer.bitCount(this.mask);
  }

  public boolean isAll() {
    return this.mask == ALL_MASK;
  }

  public boolean isEmpty() {
    return this.mask == 0;
  }
}
