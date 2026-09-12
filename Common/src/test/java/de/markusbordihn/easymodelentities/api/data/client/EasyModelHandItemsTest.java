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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import net.minecraft.SharedConstants;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class EasyModelHandItemsTest {

  @BeforeAll
  static void bootstrapMinecraft() {
    SharedConstants.tryDetectVersion();
    Bootstrap.bootStrap();
    bindItemComponents();
  }

  private static void bindItemComponents() {
    for (Item item : BuiltInRegistries.ITEM) {
      Holder<?> holder = item.builtInRegistryHolder();
      if (holder instanceof Holder.Reference<?> reference && !reference.areComponentsBound()) {
        reference.bindComponents(DataComponentMap.EMPTY);
      }
    }
  }

  private static LivingEntity holder(
      ItemStack mainHandItem, ItemStack offHandItem, HumanoidArm arm) {
    LivingEntity holder = mock(LivingEntity.class);
    when(holder.getMainHandItem()).thenReturn(mainHandItem);
    when(holder.getOffhandItem()).thenReturn(offHandItem);
    when(holder.getMainArm()).thenReturn(arm);

    return holder;
  }

  @Test
  void noneHoldsNothing() {
    assertTrue(EasyModelHandItems.NONE.isEmpty());
    assertNull(EasyModelHandItems.NONE.holder());
    assertEquals(HumanoidArm.RIGHT, EasyModelHandItems.NONE.mainArm());
    assertEquals(HumanoidArm.LEFT, EasyModelHandItems.NONE.offArm());
  }

  @Test
  void nullComponentsFallBackToEmptyItemsAndTheRightArm() {
    EasyModelHandItems handItems = new EasyModelHandItems(null, null, null, null);

    assertSame(ItemStack.EMPTY, handItems.mainHandItem());
    assertSame(ItemStack.EMPTY, handItems.offHandItem());
    assertEquals(HumanoidArm.RIGHT, handItems.mainArm());
    assertTrue(handItems.isEmpty());
  }

  @Test
  @DisplayName("A single item in either hand is enough to render")
  void aSingleItemMakesTheHandItemsNonEmpty() {
    assertFalse(
        new EasyModelHandItems(null, new ItemStack(Items.STICK), ItemStack.EMPTY, HumanoidArm.RIGHT)
            .isEmpty());
    assertFalse(
        new EasyModelHandItems(
                null, ItemStack.EMPTY, new ItemStack(Items.SHIELD), HumanoidArm.RIGHT)
            .isEmpty());
  }

  @Test
  void ofReadsBothHandsAndTheMainArmFromTheHolder() {
    ItemStack mainHandItem = new ItemStack(Items.STICK);
    ItemStack offHandItem = new ItemStack(Items.SHIELD);
    LivingEntity holder = holder(mainHandItem, offHandItem, HumanoidArm.LEFT);

    EasyModelHandItems handItems = EasyModelHandItems.of(holder);

    assertSame(holder, handItems.holder());
    assertSame(mainHandItem, handItems.mainHandItem());
    assertSame(offHandItem, handItems.offHandItem());
    assertEquals(HumanoidArm.LEFT, handItems.mainArm());
    assertEquals(HumanoidArm.RIGHT, handItems.offArm());
  }

  @Test
  void ofWithoutHolderIsNone() {
    assertSame(EasyModelHandItems.NONE, EasyModelHandItems.of(null));
  }

  @Test
  void renderOptionsHoldNoHandItemsByDefault() {
    assertTrue(EasyModelEntityRenderOptions.DEFAULT.handItems().isEmpty());
  }

  @Test
  void withHandItemsKeepsOtherComponents() {
    EasyModelHandItems handItems =
        EasyModelHandItems.of(
            holder(new ItemStack(Items.STICK), ItemStack.EMPTY, HumanoidArm.RIGHT));

    EasyModelEntityRenderOptions options =
        EasyModelEntityRenderOptions.DEFAULT
            .withScale(2.0f)
            .withOpacity(0.4f)
            .withHandItems(handItems);

    assertSame(handItems, options.handItems());
    assertEquals(2.0f, options.scale());
    assertEquals(0.4f, options.opacity());
  }

  @Test
  void handItemsCanBeRemovedExplicitly() {
    EasyModelEntityRenderOptions options =
        EasyModelEntityRenderOptions.DEFAULT
            .withHandItems(holder(new ItemStack(Items.STICK), ItemStack.EMPTY, HumanoidArm.RIGHT))
            .withoutHandItems();

    assertSame(EasyModelHandItems.NONE, options.handItems());
  }

  @Test
  void withHandItemsRejectsNull() {
    assertThrows(
        NullPointerException.class,
        () -> EasyModelEntityRenderOptions.DEFAULT.withHandItems((EasyModelHandItems) null));
  }

  @Test
  void withHandItemsAcceptsAMissingHolder() {
    assertSame(
        EasyModelHandItems.NONE,
        EasyModelEntityRenderOptions.DEFAULT.withHandItems((LivingEntity) null).handItems());
  }
}
