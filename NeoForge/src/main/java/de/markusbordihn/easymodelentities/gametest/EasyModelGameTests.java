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
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

@EventBusSubscriber
public final class EasyModelGameTests {

  private static final DeferredRegister<Consumer<GameTestHelper>> TEST_FUNCTIONS =
      DeferredRegister.create(BuiltInRegistries.TEST_FUNCTION, Constants.MOD_ID);

  private static final List<DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>>>
      REGISTERED_TEST_FUNCTIONS = new ArrayList<>();

  private static final int MAX_TICKS = 100;
  private static final Identifier STRUCTURE =
      Identifier.fromNamespaceAndPath(Constants.MOD_ID, "gametest.3x3x3");

  static {
    registerTest("mod_registered", SmokeTest::testModRegistered);
    registerTest(
        "entity_data_serializers_are_registered",
        EntityDataSerializerGameTest::serializersAreNeoForgeRegistered);

    registerTest("ground_entity_can_be_created", HostEntityGameTestCases::groundEntityCanBeCreated);
    registerTest("static_entity_can_be_created", HostEntityGameTestCases::staticEntityCanBeCreated);
    registerTest(
        "frozen_behavior_stops_host_entity_movement",
        HostEntityGameTestCases::frozenBehaviorStopsHostEntityMovement);
    registerTest(
        "missing_and_invalid_profiles_return_empty",
        HostEntityGameTestCases::missingAndInvalidProfilesReturnEmpty);
    registerTest(
        "nbt_preserves_runtime_contract_data",
        HostEntityGameTestCases::nbtPreservesRuntimeContractData);
    registerTest(
        "nbt_preserves_texture_setting", HostEntityGameTestCases::nbtPreservesTextureSetting);
    registerTest(
        "invalid_nbt_resource_locations_fall_back_safely",
        HostEntityGameTestCases::invalidNbtResourceLocationsFallBackSafely);
    registerTest(
        "dimension_refresh_uses_profile_dimensions",
        HostEntityGameTestCases::dimensionRefreshUsesProfileDimensions);
    registerTest(
        "attributes_are_applied_from_profile",
        HostEntityGameTestCases::attributesAreAppliedFromProfile);
    registerTest(
        "block_entity_can_be_placed_and_initialized",
        HostEntityGameTestCases::blockEntityCanBePlacedAndInitialized);
    registerTest(
        "entity_spawn_item_spawns_host_entity",
        HostEntityGameTestCases::entitySpawnItemSpawnsHostEntity);
    registerTest(
        "block_spawn_item_places_host_block",
        HostEntityGameTestCases::blockSpawnItemPlacesHostBlock);
  }

  private EasyModelGameTests() {}

  public static void register(IEventBus modEventBus) {
    if (FMLEnvironment.isProduction()) {
      return;
    }

    TEST_FUNCTIONS.register(modEventBus);
  }

  private static void registerTest(String name, Consumer<GameTestHelper> testFunction) {
    REGISTERED_TEST_FUNCTIONS.add(TEST_FUNCTIONS.register(name, () -> testFunction));
  }

  @SubscribeEvent
  public static void registerGameTests(RegisterGameTestsEvent event) {
    Holder<TestEnvironmentDefinition<?>> environment =
        event.registerEnvironment(
            Identifier.fromNamespaceAndPath(Constants.MOD_ID, "default"),
            new TestEnvironmentDefinition.AllOf(List.of()));

    for (DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> testFunction :
        REGISTERED_TEST_FUNCTIONS) {
      event.registerTest(
          testFunction.getId(),
          new FunctionGameTestInstance(
              testFunction.getKey(), new TestData<>(environment, STRUCTURE, MAX_TICKS, 0, true)));
    }
  }
}
