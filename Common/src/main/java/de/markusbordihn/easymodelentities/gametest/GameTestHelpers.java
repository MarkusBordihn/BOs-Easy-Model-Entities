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
import java.util.List;
import java.util.Locale;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestFunction;

public final class GameTestHelpers {

  public static final String ENABLED_PROPERTY = Constants.MOD_ID + ".gametest";
  private static final String BATCH_NAME = Constants.MOD_ID;
  private static final String TEMPLATE_NAME = Constants.MOD_ID + ":gametest.3x3x3";
  private static final int TIMEOUT_TICKS = 100;

  private GameTestHelpers() {}

  public static void assertTrue(GameTestHelper helper, String message, boolean condition) {
    if (condition) {
      helper.succeed();
      return;
    }

    helper.fail(message);
  }

  public static List<TestFunction> testFunctions(Class<?> testCasesClass) {
    if (!Boolean.getBoolean(ENABLED_PROPERTY)) {
      return List.of();
    }

    String testClassName = testCasesClass.getSimpleName().toLowerCase(Locale.ROOT);
    return Arrays.stream(testCasesClass.getDeclaredMethods())
        .filter(GameTestHelpers::isTestCase)
        .sorted(Comparator.comparing(Method::getName))
        .map(
            testCase ->
                new TestFunction(
                    BATCH_NAME,
                    testClassName + "." + testCase.getName().toLowerCase(Locale.ROOT),
                    TEMPLATE_NAME,
                    TIMEOUT_TICKS,
                    0L,
                    true,
                    helper -> invokeTestCase(testCase, helper)))
        .toList();
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
