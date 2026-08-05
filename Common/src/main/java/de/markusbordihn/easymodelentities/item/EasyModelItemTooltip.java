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

import de.markusbordihn.easymodelentities.data.profile.EasyModelEntityProfile;
import de.markusbordihn.easymodelentities.data.profile.ModelType;
import de.markusbordihn.easymodelentities.registry.EasyModelServices;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public final class EasyModelItemTooltip {

  private EasyModelItemTooltip() {}

  public static void append(ItemStack stack, List<Component> tooltip) {
    Optional<ResourceLocation> profileId = EasyModelEntitiesItems.profileId(stack);
    if (profileId.isEmpty()) {
      return;
    }

    tooltip.add(Component.literal(profileId.get().toString()).withStyle(ChatFormatting.DARK_GRAY));

    Optional<EasyModelEntityProfile> profile =
        EasyModelServices.profileService().getProfile(profileId.get());
    if (profile.isEmpty()) {
      return;
    }
    EasyModelEntityProfile value = profile.get();

    tooltip.add(
        Component.translatable(
                "tooltip.easy_model_entities.type",
                value.modelType().getSerializedName(),
                value.bodyType().getSerializedName())
            .withStyle(ChatFormatting.GRAY));
    if (value.modelType() == ModelType.BLOCK_ENTITY) {
      tooltip.add(
          Component.translatable(
                  "tooltip.easy_model_entities.preset",
                  value.blockEntityPresetType().getSerializedName())
              .withStyle(ChatFormatting.GRAY));
    } else {
      tooltip.add(
          Component.translatable(
                  "tooltip.easy_model_entities.behavior",
                  value.behavior().mode().getSerializedName(),
                  value.movementType().getSerializedName())
              .withStyle(ChatFormatting.GRAY));
    }
    tooltip.add(
        Component.translatable(
                "tooltip.easy_model_entities.size", format(value.width()), format(value.height()))
            .withStyle(ChatFormatting.GRAY));
    if (!value.version().isBlank()) {
      tooltip.add(
          Component.translatable("tooltip.easy_model_entities.version", value.version())
              .withStyle(ChatFormatting.DARK_GRAY));
    }
  }

  private static String format(float value) {
    return String.format(Locale.ROOT, "%.2f", value);
  }
}
