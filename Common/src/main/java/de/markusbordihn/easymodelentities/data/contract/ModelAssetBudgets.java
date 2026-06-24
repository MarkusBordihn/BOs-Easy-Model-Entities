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

package de.markusbordihn.easymodelentities.data.contract;

public final class ModelAssetBudgets {

  public static final int MAX_MODEL_FILE_SIZE_BYTES = 2 * 1024 * 1024;
  public static final int SOFT_MODEL_FILE_SIZE_BYTES = 1024 * 1024;
  public static final int MAX_TEXTURE_SIZE = 2048;
  public static final int SOFT_TEXTURE_SIZE = 128;
  public static final int MAX_BONE_COUNT = 128;
  public static final int SOFT_BONE_COUNT = 96;
  public static final int MAX_CUBE_COUNT = 512;
  public static final int SOFT_CUBE_COUNT = 384;
  public static final int MAX_HIERARCHY_DEPTH = 32;
  public static final int SOFT_HIERARCHY_DEPTH = 24;
  public static final int MAX_ANIMATION_COUNT = 16;

  private ModelAssetBudgets() {}
}
