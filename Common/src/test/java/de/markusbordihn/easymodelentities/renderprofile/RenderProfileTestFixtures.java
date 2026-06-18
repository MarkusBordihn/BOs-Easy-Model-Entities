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

package de.markusbordihn.easymodelentities.renderprofile;

import de.markusbordihn.easymodelentities.data.renderprofile.*;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.stream.Collectors;
import net.minecraft.resources.ResourceLocation;

final class RenderProfileTestFixtures {

  static final ResourceLocation RENDER_PROFILE_ID =
      ResourceLocation.fromNamespaceAndPath("example", "lizard");
  static final String RESOURCE_PACK_RENDER_PROFILE =
      "assets/example/easy_model_entities/render_profiles/lizard.json";

  private RenderProfileTestFixtures() {}

  static EasyModelRenderProfile parse(String resourcePath) {
    return ModelRenderProfileParser.parse(RENDER_PROFILE_ID, reader(resourcePath));
  }

  static Reader reader(String resourcePath) {
    return new InputStreamReader(inputStream(resourcePath), StandardCharsets.UTF_8);
  }

  static String read(String resourcePath) {
    try (BufferedReader reader =
        new BufferedReader(
            new InputStreamReader(inputStream(resourcePath), StandardCharsets.UTF_8))) {
      return reader.lines().collect(Collectors.joining("\n"));
    } catch (Exception exception) {
      throw new IllegalStateException("Could not read test fixture " + resourcePath, exception);
    }
  }

  private static InputStream inputStream(String resourcePath) {
    return Objects.requireNonNull(
        RenderProfileTestFixtures.class.getClassLoader().getResourceAsStream(resourcePath),
        "Missing test fixture " + resourcePath);
  }
}
