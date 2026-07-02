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

import de.markusbordihn.easymodelentities.item.EasyModelBlockSpawnItem;
import de.markusbordihn.easymodelentities.item.EasyModelEntitiesItems;
import de.markusbordihn.easymodelentities.item.EasyModelEntitySpawnItem;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;

public final class EasyModelItems {

  public static final Item ENTITY_SPAWN =
      Registry.register(
          BuiltInRegistries.ITEM,
          EasyModelEntitiesItems.ENTITY_SPAWN_ID,
          new EasyModelEntitySpawnItem(properties(EasyModelEntitiesItems.ENTITY_SPAWN_ID)));
  public static final Item BLOCK_SPAWN =
      Registry.register(
          BuiltInRegistries.ITEM,
          EasyModelEntitiesItems.BLOCK_SPAWN_ID,
          new EasyModelBlockSpawnItem(properties(EasyModelEntitiesItems.BLOCK_SPAWN_ID)));
  public static final Item ENTITIES_ICON =
      Registry.register(
          BuiltInRegistries.ITEM,
          EasyModelEntitiesItems.ENTITIES_ICON_ID,
          new Item(properties(EasyModelEntitiesItems.ENTITIES_ICON_ID)));
  public static final Item BLOCK_ENTITIES_ICON =
      Registry.register(
          BuiltInRegistries.ITEM,
          EasyModelEntitiesItems.BLOCK_ENTITIES_ICON_ID,
          new Item(properties(EasyModelEntitiesItems.BLOCK_ENTITIES_ICON_ID)));

  private EasyModelItems() {}

  public static void register() {
    EasyModelEntitiesItems.bind(ENTITY_SPAWN, BLOCK_SPAWN);
  }

  private static Item.Properties properties(Identifier id) {
    return new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id));
  }
}
