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

import com.mojang.serialization.MapCodec;
import de.markusbordihn.easymodelentities.Constants;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

public record EasyModelSpawnItemSpecialRendererUnbaked()
    implements SpecialModelRenderer.Unbaked<ItemStack> {

  public static final Identifier ID =
      Identifier.fromNamespaceAndPath(Constants.MOD_ID, "spawn_item_renderer");

  public static final MapCodec<EasyModelSpawnItemSpecialRendererUnbaked> MAP_CODEC =
      MapCodec.unit(new EasyModelSpawnItemSpecialRendererUnbaked());

  @Override
  public MapCodec<EasyModelSpawnItemSpecialRendererUnbaked> type() {
    return MAP_CODEC;
  }

  @Override
  public SpecialModelRenderer<ItemStack> bake(SpecialModelRenderer.BakingContext context) {
    return EasyModelSpawnItemSpecialRenderer.INSTANCE;
  }
}
