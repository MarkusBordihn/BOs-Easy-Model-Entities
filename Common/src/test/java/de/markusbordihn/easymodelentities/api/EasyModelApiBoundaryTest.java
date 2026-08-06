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

package de.markusbordihn.easymodelentities.api;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.RecordComponent;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

class EasyModelApiBoundaryTest {

  private static final String PROJECT_PACKAGE = "de.markusbordihn.easymodelentities";
  private static final String API_PACKAGE = PROJECT_PACKAGE + ".api";
  private static final String API_PATH = API_PACKAGE.replace('.', '/');

  private static Set<Class<?>> apiClasses() throws Exception {
    ClassLoader classLoader = EasyModelApiBoundaryTest.class.getClassLoader();
    Enumeration<URL> resources = classLoader.getResources(API_PATH);
    Set<Class<?>> apiClasses = new HashSet<>();
    while (resources.hasMoreElements()) {
      URL resource = resources.nextElement();
      if (!"file".equals(resource.getProtocol())) {
        continue;
      }
      Path packageDirectory = Path.of(new URI(resource.toString()));
      try (Stream<Path> classFiles = Files.walk(packageDirectory)) {
        for (Path classFile : classFiles.filter(EasyModelApiBoundaryTest::isClassFile).toList()) {
          String relativeName = packageDirectory.relativize(classFile).toString();
          String className =
              API_PACKAGE
                  + "."
                  + relativeName
                      .substring(0, relativeName.length() - ".class".length())
                      .replace(File.separatorChar, '.');
          apiClasses.add(Class.forName(className, false, classLoader));
        }
      }
    }

    return apiClasses;
  }

  private static boolean isClassFile(Path path) {
    return Files.isRegularFile(path) && path.getFileName().toString().endsWith(".class");
  }

  private static void inspect(Class<?> apiClass, List<String> violations) {
    inspectType(apiClass.getGenericSuperclass(), apiClass.getName() + " superclass", violations);
    for (Type type : apiClass.getGenericInterfaces()) {
      inspectType(type, apiClass.getName() + " interface", violations);
    }
    for (TypeVariable<?> typeParameter : apiClass.getTypeParameters()) {
      inspectType(typeParameter, apiClass.getName() + " type parameter", violations);
    }
    for (Constructor<?> constructor : apiClass.getConstructors()) {
      for (Type type : constructor.getGenericParameterTypes()) {
        inspectType(type, constructor + " parameter", violations);
      }
      for (Type type : constructor.getGenericExceptionTypes()) {
        inspectType(type, constructor + " exception", violations);
      }
    }
    for (Method method : apiClass.getDeclaredMethods()) {
      if (!Modifier.isPublic(method.getModifiers())) {
        continue;
      }
      inspectType(method.getGenericReturnType(), method + " return type", violations);
      for (Type type : method.getGenericParameterTypes()) {
        inspectType(type, method + " parameter", violations);
      }
      for (Type type : method.getGenericExceptionTypes()) {
        inspectType(type, method + " exception", violations);
      }
    }
    for (Field field : apiClass.getDeclaredFields()) {
      if (Modifier.isPublic(field.getModifiers())) {
        inspectType(field.getGenericType(), field + " field", violations);
      }
    }
    RecordComponent[] recordComponents = apiClass.getRecordComponents();
    if (recordComponents != null) {
      for (RecordComponent component : recordComponents) {
        inspectType(component.getGenericType(), component + " record component", violations);
      }
    }
  }

  private static void inspectType(Type type, String location, List<String> violations) {
    inspectType(type, location, violations, new HashSet<>());
  }

  private static void inspectType(
      Type type, String location, List<String> violations, Set<Type> inspectedTypes) {
    if (type == null || !inspectedTypes.add(type)) {
      return;
    }
    if (type instanceof Class<?> typeClass) {
      Class<?> resolvedClass = typeClass.isArray() ? typeClass.getComponentType() : typeClass;
      String className = resolvedClass.getName();
      if (className.startsWith(PROJECT_PACKAGE + ".") && !className.startsWith(API_PACKAGE + ".")) {
        violations.add(location + " exposes " + className);
      }
      return;
    }
    if (type instanceof ParameterizedType parameterizedType) {
      inspectType(parameterizedType.getRawType(), location, violations, inspectedTypes);
      inspectType(parameterizedType.getOwnerType(), location, violations, inspectedTypes);
      for (Type argument : parameterizedType.getActualTypeArguments()) {
        inspectType(argument, location, violations, inspectedTypes);
      }
      return;
    }
    if (type instanceof GenericArrayType arrayType) {
      inspectType(arrayType.getGenericComponentType(), location, violations, inspectedTypes);
      return;
    }
    if (type instanceof TypeVariable<?> typeVariable) {
      for (Type bound : typeVariable.getBounds()) {
        inspectType(bound, location, violations, inspectedTypes);
      }
      return;
    }
    if (type instanceof WildcardType wildcardType) {
      for (Type bound : wildcardType.getLowerBounds()) {
        inspectType(bound, location, violations, inspectedTypes);
      }
      for (Type bound : wildcardType.getUpperBounds()) {
        inspectType(bound, location, violations, inspectedTypes);
      }
    }
  }

  @Test
  void publicApiDoesNotExposeInternalProjectTypes() throws Exception {
    Set<Class<?>> apiClasses = apiClasses();
    assertFalse(apiClasses.isEmpty(), "No API classes were discovered.");

    List<String> violations = new ArrayList<>();
    apiClasses.stream()
        .filter(apiClass -> Modifier.isPublic(apiClass.getModifiers()))
        .forEach(apiClass -> inspect(apiClass, violations));

    assertTrue(violations.isEmpty(), () -> String.join(System.lineSeparator(), violations));
  }
}
