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
    registerTest(
        "animation_packets_skip_client_without_mod",
        ClientWithoutModGameTest::animationPacketsSkipClientWithoutMod);
    registerTest(
        "shoulder_packets_skip_client_without_mod",
        ClientWithoutModGameTest::shoulderPacketsSkipClientWithoutMod);

    GameTestHelpers.testFunctions(HostEntityGameTestCases.class)
        .forEach(EasyModelGameTests::registerTest);
    GameTestHelpers.testFunctions(CommandGameTestCases.class)
        .forEach(EasyModelGameTests::registerTest);
  }

  private EasyModelGameTests() {}

  public static void register(IEventBus modEventBus) {
    if (!isActive()) {
      return;
    }

    TEST_FUNCTIONS.register(modEventBus);
  }

  private static boolean isActive() {
    return !FMLEnvironment.isProduction() && GameTestHelpers.isEnabled();
  }

  private static void registerTest(String name, Consumer<GameTestHelper> testFunction) {
    REGISTERED_TEST_FUNCTIONS.add(TEST_FUNCTIONS.register(name, () -> testFunction));
  }

  @SubscribeEvent
  public static void registerGameTests(RegisterGameTestsEvent event) {
    if (!isActive()) {
      return;
    }

    Holder<TestEnvironmentDefinition<?>> environment =
        event.registerEnvironment(
            Identifier.fromNamespaceAndPath(Constants.MOD_ID, "default"),
            new TestEnvironmentDefinition.AllOf(List.of()));

    for (DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> testFunction :
        REGISTERED_TEST_FUNCTIONS) {
      event.registerTest(
          testFunction.getId(),
          new FunctionGameTestInstance(
              testFunction.key(), new TestData<>(environment, STRUCTURE, MAX_TICKS, 0, true)));
    }
  }
}
