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
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class EasyModelCreativeModeTabs {

  private static final DeferredRegister<CreativeModeTab> TABS =
      DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Constants.MOD_ID);

  public static final DeferredHolder<CreativeModeTab, CreativeModeTab> ENTITIES_TAB =
      TABS.register(
          EasyModelCreativeTabs.ENTITIES_TAB_ID.getPath(),
          () ->
              CreativeModeTab.builder()
                  .title(Component.translatable("itemGroup." + Constants.MOD_ID + ".entities"))
                  .icon(() -> new ItemStack(EasyModelItems.ENTITIES_ICON.get()))
                  .displayItems(
                      (parameters, output) ->
                          output.acceptAll(EasyModelCreativeTabs.entityTabStacks()))
                  .build());

  public static final DeferredHolder<CreativeModeTab, CreativeModeTab> BLOCK_ENTITIES_TAB =
      TABS.register(
          EasyModelCreativeTabs.BLOCK_ENTITIES_TAB_ID.getPath(),
          () ->
              CreativeModeTab.builder()
                  .title(
                      Component.translatable("itemGroup." + Constants.MOD_ID + ".block_entities"))
                  .icon(() -> new ItemStack(EasyModelItems.BLOCK_ENTITIES_ICON.get()))
                  .displayItems(
                      (parameters, output) ->
                          output.acceptAll(EasyModelCreativeTabs.blockTabStacks()))
                  .build());

  private EasyModelCreativeModeTabs() {}

  public static void register(IEventBus modEventBus) {
    TABS.register(modEventBus);
  }
}
