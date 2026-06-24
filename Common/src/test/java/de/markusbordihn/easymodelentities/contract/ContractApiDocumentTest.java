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

package de.markusbordihn.easymodelentities.contract;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class ContractApiDocumentTest {

  private static JsonElement committedReference() throws IOException {
    try (InputStream inputStream =
        ContractApiDocumentTest.class
            .getClassLoader()
            .getResourceAsStream(ContractApiDocument.RESOURCE_PATH)) {
      if (inputStream == null) {
        throw new IOException(
            "Missing committed contract reference " + ContractApiDocument.RESOURCE_PATH);
      }

      return JsonParser.parseString(new String(inputStream.readAllBytes(), StandardCharsets.UTF_8));
    }
  }

  @Test
  void generatedReferenceMatchesCommittedResource() throws Exception {
    assertEquals(committedReference(), ContractApiDocument.toJson());
  }

  @Test
  void generatedReferenceMatchesRuntimeConstants() {
    JsonObject reference = ContractApiDocument.toJson();

    assertEquals("0.1.0", reference.get("schema_version").getAsString());
    assertEquals(
        2 * 1024 * 1024,
        reference.getAsJsonObject("budgets").get("max_model_file_size_bytes").getAsInt());
    assertEquals(128, reference.getAsJsonObject("budgets").get("max_bone_count").getAsInt());
  }
}
