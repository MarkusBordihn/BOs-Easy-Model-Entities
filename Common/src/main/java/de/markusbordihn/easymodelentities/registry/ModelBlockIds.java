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

package de.markusbordihn.easymodelentities.registry;

import de.markusbordihn.easymodelentities.Constants;
import net.minecraft.resources.ResourceLocation;

public final class ModelBlockIds {

  public static final ResourceLocation STATIC_BLOCK =
      new ResourceLocation(Constants.MOD_ID, "static_block");
  public static final ResourceLocation TICKING_BLOCK =
      new ResourceLocation(Constants.MOD_ID, "ticking_block");
  public static final ResourceLocation ANIMATED_BLOCK =
      new ResourceLocation(Constants.MOD_ID, "animated_block");
  public static final ResourceLocation ANIMATED_RANDOMLY_BLOCK =
      new ResourceLocation(Constants.MOD_ID, "animated_randomly_block");

  private ModelBlockIds() {}
}
