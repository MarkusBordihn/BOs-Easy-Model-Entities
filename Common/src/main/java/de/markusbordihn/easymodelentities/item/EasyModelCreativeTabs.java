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

import de.markusbordihn.easymodelentities.Constants;
import de.markusbordihn.easymodelentities.data.profile.ModelType;
import de.markusbordihn.easymodelentities.data.renderprofile.EasyModelRenderProfile;
import de.markusbordihn.easymodelentities.registry.EasyModelServices;
import java.util.Comparator;
import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class EasyModelCreativeTabs {

  public static final ResourceLocation ENTITIES_TAB_ID =
      new ResourceLocation(Constants.MOD_ID, "entities");
  public static final ResourceLocation BLOCK_ENTITIES_TAB_ID =
      new ResourceLocation(Constants.MOD_ID, "block_entities");

  private EasyModelCreativeTabs() {}

  public static List<ItemStack> entityTabStacks() {
    return spawnStacks(EasyModelEntitiesItems.entitySpawnItem(), ModelType.ENTITY);
  }

  public static List<ItemStack> blockTabStacks() {
    return spawnStacks(EasyModelEntitiesItems.blockSpawnItem(), ModelType.BLOCK_ENTITY);
  }

  private static List<ItemStack> spawnStacks(Item item, ModelType modelType) {
    if (item == null) {
      return List.of();
    }
    return EasyModelServices.renderProfileService().getRenderProfiles().stream()
        .filter(EasyModelRenderProfile::isActive)
        .map(EasyModelRenderProfile::id)
        .filter(id -> ModelType.fromProfileId(id) == modelType)
        .sorted(Comparator.comparing(ResourceLocation::toString))
        .map(id -> EasyModelEntitiesItems.forProfile(item, id))
        .toList();
  }
}
