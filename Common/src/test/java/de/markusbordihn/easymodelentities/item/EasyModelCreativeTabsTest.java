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
import de.markusbordihn.easymodelentities.data.model.Vec3f;
import de.markusbordihn.easymodelentities.data.profile.ModelBodyType;
import de.markusbordihn.easymodelentities.data.renderprofile.EasyModelRenderProfile;
import de.markusbordihn.easymodelentities.data.renderprofile.ModelAnimationMode;
import de.markusbordihn.easymodelentities.data.renderprofile.ModelAnimationSettings;
import de.markusbordihn.easymodelentities.data.renderprofile.ModelRenderProfileStatus;
import de.markusbordihn.easymodelentities.data.renderprofile.ModelRenderSettings;
import de.markusbordihn.easymodelentities.registry.EasyModelServices;
import de.markusbordihn.easymodelentities.renderprofile.EasyModelRenderProfileService;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.SharedConstants;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.Bootstrap;
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
  }

  private static EasyModelRenderProfileService renderProfileService(
      EasyModelRenderProfile... renderProfiles) {
    Map<ResourceLocation, EasyModelRenderProfile> byId = new LinkedHashMap<>();
    for (EasyModelRenderProfile renderProfile : renderProfiles) {
      byId.put(renderProfile.id(), renderProfile);
    }
    return new EasyModelRenderProfileService() {
      @Override
      public Optional<EasyModelRenderProfile> getRenderProfile(ResourceLocation renderProfileId) {
        return Optional.ofNullable(byId.get(renderProfileId));
      }

      @Override
      public Collection<EasyModelRenderProfile> getRenderProfiles() {
        return byId.values();
      }
    };
  }

  private static EasyModelRenderProfile renderProfile(
      ResourceLocation renderProfileId, ModelRenderProfileStatus status) {
    return new EasyModelRenderProfile(
        renderProfileId,
        Constants.SCHEMA_VERSION,
        "render-v1",
        ModelBodyType.BIPED,
        new ResourceLocation("example", "model"),
        new ResourceLocation("example", "texture"),
        new ModelRenderSettings(1.0f, 0.3f, 0.0f, 0.0f, Vec3f.ZERO),
        new ModelAnimationSettings(ModelAnimationMode.AUTOMATIC, 1.0f, 1.0f),
        status,
        List.of());
  }

  private static EasyModelRenderProfile active(ResourceLocation renderProfileId) {
    return renderProfile(renderProfileId, ModelRenderProfileStatus.ACTIVE);
  }

  @AfterEach
  void resetServices() {
    EasyModelServices.reset();
    EasyModelEntitiesItems.bind(null, null);
  }

  @Test
  void entityTabListsSortedActiveEntityRenderProfilesWithProfileNbt() {
    EasyModelEntitiesItems.bind(Items.STICK, Items.STONE);
    EasyModelServices.setRenderProfileService(
        renderProfileService(
            active(new ResourceLocation("example", "entity/zeta")),
            active(new ResourceLocation("example", "entity/alpha")),
            active(new ResourceLocation("example", "block_entity/shrine"))));

    List<ItemStack> stacks = EasyModelCreativeTabs.entityTabStacks();

    assertEquals(2, stacks.size());
    assertTrue(stacks.get(0).is(Items.STICK));
    assertEquals(
        Optional.of(new ResourceLocation("example", "entity/alpha")),
        EasyModelEntitiesItems.profileId(stacks.get(0)));
    assertEquals(
        Optional.of(new ResourceLocation("example", "entity/zeta")),
        EasyModelEntitiesItems.profileId(stacks.get(1)));
  }

  @Test
  void blockTabListsBlockEntityRenderProfiles() {
    EasyModelEntitiesItems.bind(Items.STICK, Items.STONE);
    EasyModelServices.setRenderProfileService(
        renderProfileService(
            active(new ResourceLocation("example", "block_entity/shrine")),
            active(new ResourceLocation("example", "entity/alpha"))));

    List<ItemStack> stacks = EasyModelCreativeTabs.blockTabStacks();

    assertEquals(1, stacks.size());
    assertTrue(stacks.get(0).is(Items.STONE));
    assertEquals(
        Optional.of(new ResourceLocation("example", "block_entity/shrine")),
        EasyModelEntitiesItems.profileId(stacks.get(0)));
  }

  @Test
  void unprefixedRenderProfileFallsBackToEntityTab() {
    EasyModelEntitiesItems.bind(Items.STICK, Items.STONE);
    EasyModelServices.setRenderProfileService(
        renderProfileService(active(new ResourceLocation("example", "legacy"))));

    assertEquals(1, EasyModelCreativeTabs.entityTabStacks().size());
    assertEquals(List.of(), EasyModelCreativeTabs.blockTabStacks());
  }

  @Test
  void tabsSkipInactiveRenderProfiles() {
    EasyModelEntitiesItems.bind(Items.STICK, Items.STONE);
    EasyModelServices.setRenderProfileService(
        renderProfileService(
            active(new ResourceLocation("example", "entity/active")),
            renderProfile(
                new ResourceLocation("example", "entity/broken"),
                ModelRenderProfileStatus.MISSING_MODEL)));

    List<ItemStack> stacks = EasyModelCreativeTabs.entityTabStacks();

    assertEquals(1, stacks.size());
    assertEquals(
        Optional.of(new ResourceLocation("example", "entity/active")),
        EasyModelEntitiesItems.profileId(stacks.get(0)));
  }

  @Test
  void displayNamePrettifiesProfilePath() {
    assertEquals(
        "Coral Drifter",
        EasyModelEntitiesItems.displayName(
            new ResourceLocation("example", "entity/coral_drifter")));
  }

  @Test
  void tabsAreEmptyWhenItemsAreNotBound() {
    EasyModelServices.setRenderProfileService(
        renderProfileService(active(new ResourceLocation("example", "entity/alpha"))));

    assertEquals(List.of(), EasyModelCreativeTabs.entityTabStacks());
    assertEquals(List.of(), EasyModelCreativeTabs.blockTabStacks());
  }
}
