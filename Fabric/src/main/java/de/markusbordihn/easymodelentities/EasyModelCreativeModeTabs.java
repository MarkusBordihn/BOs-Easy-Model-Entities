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

package de.markusbordihn.easymodelentities;

import de.markusbordihn.easymodelentities.item.EasyModelCreativeTabs;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public final class EasyModelCreativeModeTabs {

  public static final CreativeModeTab ENTITIES_TAB =
      Registry.register(
          BuiltInRegistries.CREATIVE_MODE_TAB,
          EasyModelCreativeTabs.ENTITIES_TAB_ID,
          FabricItemGroup.builder()
              .title(Component.translatable("itemGroup." + Constants.MOD_ID + ".entities"))
              .icon(() -> new ItemStack(EasyModelItems.ENTITIES_ICON))
              .displayItems(
                  (parameters, output) -> output.acceptAll(EasyModelCreativeTabs.entityTabStacks()))
              .build());

  public static final CreativeModeTab BLOCK_ENTITIES_TAB =
      Registry.register(
          BuiltInRegistries.CREATIVE_MODE_TAB,
          EasyModelCreativeTabs.BLOCK_ENTITIES_TAB_ID,
          FabricItemGroup.builder()
              .title(Component.translatable("itemGroup." + Constants.MOD_ID + ".block_entities"))
              .icon(() -> new ItemStack(EasyModelItems.BLOCK_ENTITIES_ICON))
              .displayItems(
                  (parameters, output) -> output.acceptAll(EasyModelCreativeTabs.blockTabStacks()))
              .build());

  private EasyModelCreativeModeTabs() {}

  public static void register() {}
}
