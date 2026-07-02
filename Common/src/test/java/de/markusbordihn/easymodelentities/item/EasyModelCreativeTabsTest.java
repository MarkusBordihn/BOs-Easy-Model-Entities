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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.markusbordihn.easymodelentities.Constants;
import de.markusbordihn.easymodelentities.data.profile.EasyModelEntityProfile;
import de.markusbordihn.easymodelentities.data.profile.ModelAttributes;
import de.markusbordihn.easymodelentities.data.profile.ModelBehaviorMode;
import de.markusbordihn.easymodelentities.data.profile.ModelBehaviorSettings;
import de.markusbordihn.easymodelentities.data.profile.ModelBlockEntityPresetType;
import de.markusbordihn.easymodelentities.data.profile.ModelBlockEntitySettings;
import de.markusbordihn.easymodelentities.data.profile.ModelBodyType;
import de.markusbordihn.easymodelentities.data.profile.ModelClientSettings;
import de.markusbordihn.easymodelentities.data.profile.ModelDimensions;
import de.markusbordihn.easymodelentities.data.profile.ModelEntitySettings;
import de.markusbordihn.easymodelentities.data.profile.ModelMovementSettings;
import de.markusbordihn.easymodelentities.data.profile.ModelMovementType;
import de.markusbordihn.easymodelentities.data.profile.ModelProfileStatus;
import de.markusbordihn.easymodelentities.data.profile.ModelType;
import de.markusbordihn.easymodelentities.profile.EasyModelProfileService;
import de.markusbordihn.easymodelentities.registry.EasyModelServices;
import de.markusbordihn.easymodelentities.registry.ModelBlockEntityTypeIds;
import de.markusbordihn.easymodelentities.registry.ModelEntityTypeIds;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.SharedConstants;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class EasyModelCreativeTabsTest {

  @BeforeAll
  static void bootstrapMinecraft() {
    SharedConstants.tryDetectVersion();
    Bootstrap.bootStrap();
    bindItemComponents();
  }

  private static void bindItemComponents() {
    // Bootstrap.bootStrap() in MC 26.1.2 does not bind DataComponents to item holders, so
    // new ItemStack(item) throws "Components not bound yet". Holder.Reference.bindComponents()
    // is a public method — call it directly on every registered item holder.
    for (Item item : BuiltInRegistries.ITEM) {
      Holder<?> holder = item.builtInRegistryHolder();
      if (holder instanceof Holder.Reference<?> reference && !reference.areComponentsBound()) {
        reference.bindComponents(DataComponentMap.EMPTY);
      }
    }
  }

  private static EasyModelProfileService profileService(EasyModelEntityProfile... profiles) {
    Map<Identifier, EasyModelEntityProfile> profilesById = new LinkedHashMap<>();
    for (EasyModelEntityProfile profile : profiles) {
      profilesById.put(profile.id(), profile);
    }
    return new EasyModelProfileService() {
      @Override
      public Optional<EasyModelEntityProfile> getProfile(Identifier profileId) {
        return Optional.ofNullable(profilesById.get(profileId));
      }

      @Override
      public Collection<EasyModelEntityProfile> getProfiles() {
        return profilesById.values();
      }
    };
  }

  private static EasyModelEntityProfile entityProfile(Identifier profileId) {
    return new EasyModelEntityProfile(
        profileId,
        Constants.SCHEMA_VERSION,
        "server-v1",
        ModelType.ENTITY,
        new ModelEntitySettings(
            ModelEntityTypeIds.GROUND_ENTITY, ModelMovementType.GROUND, ModelBodyType.BIPED),
        null,
        new ModelClientSettings(profileId),
        new ModelDimensions(0.6f, 1.8f, 1.62f),
        new ModelMovementSettings(0.22f, 0.6f, true),
        new ModelBehaviorSettings(ModelBehaviorMode.IDLE_ONLY, true, false),
        new ModelAttributes(10.0f, 0.22f, 16.0f),
        ModelProfileStatus.ACTIVE,
        List.of());
  }

  private static EasyModelEntityProfile blockProfile(
      Identifier profileId, Identifier blockEntityType) {
    return new EasyModelEntityProfile(
        profileId,
        Constants.SCHEMA_VERSION,
        "server-v1",
        ModelType.BLOCK_ENTITY,
        null,
        new ModelBlockEntitySettings(
            blockEntityType, ModelBlockEntityPresetType.ANIMATED, ModelBodyType.STATIC),
        new ModelClientSettings(profileId),
        new ModelDimensions(1.0f, 1.0f, 0.5f),
        new ModelMovementSettings(0.0f, 0.0f, false),
        new ModelBehaviorSettings(ModelBehaviorMode.STATIC, false, false),
        new ModelAttributes(10.0f, 0.0f, 16.0f),
        ModelProfileStatus.ACTIVE,
        List.of());
  }

  @AfterEach
  void resetServices() {
    EasyModelServices.reset();
    EasyModelEntitiesItems.bind(null, null);
  }

  @Test
  void entityTabListsSortedActiveEntityProfilesWithProfileNbt() {
    EasyModelEntitiesItems.bind(Items.STICK, Items.STONE);
    EasyModelServices.setProfileService(
        profileService(
            entityProfile(Identifier.fromNamespaceAndPath("example", "zeta")),
            entityProfile(Identifier.fromNamespaceAndPath("example", "alpha")),
            blockProfile(
                Identifier.fromNamespaceAndPath("example", "block"),
                ModelBlockEntityTypeIds.ANIMATED_BLOCK_ENTITY)));

    List<ItemStack> stacks = EasyModelCreativeTabs.entityTabStacks();

    assertEquals(2, stacks.size());
    assertTrue(stacks.get(0).is(Items.STICK));
    assertEquals(
        Optional.of(Identifier.fromNamespaceAndPath("example", "alpha")),
        EasyModelEntitiesItems.profileId(stacks.get(0)));
    assertEquals(
        Optional.of(Identifier.fromNamespaceAndPath("example", "zeta")),
        EasyModelEntitiesItems.profileId(stacks.get(1)));
  }

  @Test
  void blockTabSkipsProfilesWithoutResolvableHostBlock() {
    EasyModelEntitiesItems.bind(Items.STICK, Items.STONE);
    EasyModelServices.setProfileService(
        profileService(
            blockProfile(
                Identifier.fromNamespaceAndPath("example", "supported"),
                ModelBlockEntityTypeIds.ANIMATED_BLOCK_ENTITY),
            blockProfile(
                Identifier.fromNamespaceAndPath("example", "unsupported"),
                Identifier.fromNamespaceAndPath("example", "nope"))));

    List<ItemStack> stacks = EasyModelCreativeTabs.blockTabStacks();

    assertEquals(1, stacks.size());
    assertTrue(stacks.get(0).is(Items.STONE));
    assertEquals(
        Optional.of(Identifier.fromNamespaceAndPath("example", "supported")),
        EasyModelEntitiesItems.profileId(stacks.get(0)));
  }

  @Test
  void displayNamePrettifiesProfilePath() {
    assertEquals(
        "Coral Drifter",
        EasyModelEntitiesItems.displayName(
            Identifier.fromNamespaceAndPath("example", "entity/coral_drifter")));
  }

  @Test
  void tabsAreEmptyWhenItemsAreNotBound() {
    EasyModelServices.setProfileService(
        profileService(entityProfile(Identifier.fromNamespaceAndPath("example", "alpha"))));

    assertEquals(List.of(), EasyModelCreativeTabs.entityTabStacks());
    assertEquals(List.of(), EasyModelCreativeTabs.blockTabStacks());
  }
}
