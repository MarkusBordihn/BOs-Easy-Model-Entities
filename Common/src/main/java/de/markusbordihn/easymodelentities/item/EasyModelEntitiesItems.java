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

package de.markusbordihn.easymodelentities.item;

import de.markusbordihn.easymodelentities.registry.ModelResourcePaths;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class EasyModelEntitiesItems {

  public static final ResourceLocation ENTITY_SPAWN_ID =
      ModelResourcePaths.modResourceLocation("easy_model_entity_spawn");
  public static final ResourceLocation BLOCK_SPAWN_ID =
      ModelResourcePaths.modResourceLocation("easy_model_block_spawn");
  public static final ResourceLocation ENTITIES_ICON_ID =
      ModelResourcePaths.modResourceLocation("easy_model_entities_icon");
  public static final ResourceLocation BLOCK_ENTITIES_ICON_ID =
      ModelResourcePaths.modResourceLocation("easy_model_block_entities_icon");

  public static final String TAG_PROFILE_ID = "EasyModelProfileId";

  private static volatile Item entitySpawnItem;
  private static volatile Item blockSpawnItem;

  private EasyModelEntitiesItems() {}

  public static void bind(Item entitySpawn, Item blockSpawn) {
    entitySpawnItem = entitySpawn;
    blockSpawnItem = blockSpawn;
  }

  public static Item entitySpawnItem() {
    return entitySpawnItem;
  }

  public static Item blockSpawnItem() {
    return blockSpawnItem;
  }

  public static ItemStack forProfile(Item item, ResourceLocation profileId) {
    Objects.requireNonNull(item, "item");
    Objects.requireNonNull(profileId, "profileId");
    ItemStack stack = new ItemStack(item);
    stack.getOrCreateTag().putString(TAG_PROFILE_ID, profileId.toString());
    return stack;
  }

  public static Optional<ResourceLocation> profileId(ItemStack stack) {
    if (stack == null || stack.isEmpty()) {
      return Optional.empty();
    }
    CompoundTag tag = stack.getTag();
    if (tag == null || !tag.contains(TAG_PROFILE_ID)) {
      return Optional.empty();
    }
    return Optional.ofNullable(ResourceLocation.tryParse(tag.getString(TAG_PROFILE_ID)));
  }

  public static String displayName(ResourceLocation profileId) {
    String path = profileId.getPath();
    int separator = path.lastIndexOf('/');
    String name = separator >= 0 ? path.substring(separator + 1) : path;
    String[] words = name.replace('_', ' ').trim().split(" ");
    StringBuilder builder = new StringBuilder();
    for (String word : words) {
      if (word.isEmpty()) {
        continue;
      }
      if (builder.length() > 0) {
        builder.append(' ');
      }
      builder
          .append(Character.toUpperCase(word.charAt(0)))
          .append(word.substring(1).toLowerCase(Locale.ROOT));
    }
    return builder.length() > 0 ? builder.toString() : profileId.toString();
  }
}
