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

package de.markusbordihn.easymodelentities.api.data.client;

import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public record EasyModelHandItems(
    LivingEntity holder, ItemStack mainHandItem, ItemStack offHandItem, HumanoidArm mainArm) {

  public static final EasyModelHandItems NONE =
      new EasyModelHandItems(null, ItemStack.EMPTY, ItemStack.EMPTY, HumanoidArm.RIGHT);

  public EasyModelHandItems {
    mainHandItem = mainHandItem == null ? ItemStack.EMPTY : mainHandItem;
    offHandItem = offHandItem == null ? ItemStack.EMPTY : offHandItem;
    mainArm = mainArm == null ? HumanoidArm.RIGHT : mainArm;
  }

  public static EasyModelHandItems of(LivingEntity holder) {
    if (holder == null) {
      return NONE;
    }

    return new EasyModelHandItems(
        holder, holder.getMainHandItem(), holder.getOffhandItem(), holder.getMainArm());
  }

  public boolean isEmpty() {
    return this.mainHandItem.isEmpty() && this.offHandItem.isEmpty();
  }

  public HumanoidArm offArm() {
    return this.mainArm == HumanoidArm.RIGHT ? HumanoidArm.LEFT : HumanoidArm.RIGHT;
  }
}
