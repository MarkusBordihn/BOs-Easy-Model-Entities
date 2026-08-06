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

package de.markusbordihn.easymodelentities.gametest;

import de.markusbordihn.easymodelentities.Constants;
import java.util.function.Consumer;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraftforge.eventbus.api.bus.BusGroup;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.registries.DeferredRegister;

public final class EasyModelGameTests {

  private static final DeferredRegister<Consumer<GameTestHelper>> TEST_FUNCTIONS =
      DeferredRegister.create(Registries.TEST_FUNCTION, Constants.MOD_ID);

  static {
    TEST_FUNCTIONS.register("mod_registered", () -> SmokeTest::testModRegistered);

    TEST_FUNCTIONS.register(
        "ground_entity_can_be_created", () -> HostEntityGameTestCases::groundEntityCanBeCreated);
    TEST_FUNCTIONS.register(
        "static_entity_can_be_created", () -> HostEntityGameTestCases::staticEntityCanBeCreated);
    TEST_FUNCTIONS.register(
        "frozen_behavior_stops_host_entity_movement",
        () -> HostEntityGameTestCases::frozenBehaviorStopsHostEntityMovement);
    TEST_FUNCTIONS.register(
        "missing_and_invalid_profiles_return_empty",
        () -> HostEntityGameTestCases::missingAndInvalidProfilesReturnEmpty);
    TEST_FUNCTIONS.register(
        "nbt_preserves_runtime_contract_data",
        () -> HostEntityGameTestCases::nbtPreservesRuntimeContractData);
    TEST_FUNCTIONS.register(
        "invalid_nbt_resource_locations_fall_back_safely",
        () -> HostEntityGameTestCases::invalidNbtResourceLocationsFallBackSafely);
    TEST_FUNCTIONS.register(
        "dimension_refresh_uses_profile_dimensions",
        () -> HostEntityGameTestCases::dimensionRefreshUsesProfileDimensions);
    TEST_FUNCTIONS.register(
        "attributes_are_applied_from_profile",
        () -> HostEntityGameTestCases::attributesAreAppliedFromProfile);
    TEST_FUNCTIONS.register(
        "block_entity_can_be_placed_and_initialized",
        () -> HostEntityGameTestCases::blockEntityCanBePlacedAndInitialized);
    TEST_FUNCTIONS.register(
        "entity_spawn_item_spawns_host_entity",
        () -> HostEntityGameTestCases::entitySpawnItemSpawnsHostEntity);
    TEST_FUNCTIONS.register(
        "block_spawn_item_places_host_block",
        () -> HostEntityGameTestCases::blockSpawnItemPlacesHostBlock);
  }

  private EasyModelGameTests() {}

  public static void register(BusGroup modBusGroup) {
    if (FMLLoader.isProduction()) {
      return;
    }

    TEST_FUNCTIONS.register(modBusGroup);
  }
}
