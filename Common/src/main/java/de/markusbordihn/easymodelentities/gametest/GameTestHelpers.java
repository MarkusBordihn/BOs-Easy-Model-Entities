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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;
import net.minecraft.gametest.framework.GameTestHelper;

public final class GameTestHelpers {

  public static final String ENABLED_PROPERTY = Constants.MOD_ID + ".gametest";

  private GameTestHelpers() {}

  public static boolean isEnabled() {
    return Boolean.getBoolean(ENABLED_PROPERTY);
  }

  public static void assertTrue(GameTestHelper helper, String message, boolean condition) {
    if (condition) {
      helper.succeed();
      return;
    }

    helper.fail(message);
  }

  public static Map<String, Consumer<GameTestHelper>> testFunctions(Class<?> testCasesClass) {
    Map<String, Consumer<GameTestHelper>> testFunctions = new LinkedHashMap<>();
    Arrays.stream(testCasesClass.getDeclaredMethods())
        .filter(GameTestHelpers::isTestCase)
        .sorted(Comparator.comparing(Method::getName))
        .forEach(
            testCase ->
                testFunctions.put(
                    snakeCase(testCase.getName()),
                    helper ->
                        runIfEnabled(
                            helper, enabledHelper -> invokeTestCase(testCase, enabledHelper))));
    return testFunctions;
  }

  // Forge and Fabric take their test instances from data files and annotations that a mod cannot
  // withhold, so a run without opt-in passes instead of failing on a missing test function.
  public static void runIfEnabled(GameTestHelper helper, Consumer<GameTestHelper> testCase) {
    if (!isEnabled()) {
      helper.succeed();
      return;
    }

    testCase.accept(helper);
  }

  private static String snakeCase(String methodName) {
    return methodName.replaceAll("([a-z0-9])([A-Z])", "$1_$2").toLowerCase(Locale.ROOT);
  }

  private static boolean isTestCase(Method method) {
    int modifiers = method.getModifiers();
    return Modifier.isPublic(modifiers)
        && Modifier.isStatic(modifiers)
        && method.getReturnType() == void.class
        && Arrays.equals(method.getParameterTypes(), new Class<?>[] {GameTestHelper.class});
  }

  private static void invokeTestCase(Method testCase, GameTestHelper helper) {
    try {
      testCase.invoke(null, helper);
    } catch (InvocationTargetException e) {
      if (e.getCause() instanceof RuntimeException runtimeException) {
        throw runtimeException;
      }
      throw new IllegalStateException(e.getCause());
    } catch (IllegalAccessException e) {
      throw new IllegalStateException(e);
    }
  }
}
