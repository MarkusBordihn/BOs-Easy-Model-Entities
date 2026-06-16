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
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package de.markusbordihn.easymodelentities.model.decoder;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.markusbordihn.easymodelentities.data.model.ModelCubeFace;
import de.markusbordihn.easymodelentities.data.model.decoder.DecodedTexture;
import de.markusbordihn.easymodelentities.data.renderprofile.ModelRenderProfileValidationIssue;
import java.util.ArrayList;
import java.util.List;

public final class EmeEntityBbModelParser extends AbstractBbModelParser {

  public static final String MODEL_FORMAT = "eme_entity";

  private static int parseTextureId(JsonObject textureObject, int positionalIndex)
      throws EasyModelDecodeException {
    String id = optionalString(textureObject, "id", "");
    if (id.isBlank()) {
      return positionalIndex;
    }
    try {
      return Math.max(Integer.parseInt(id.trim()), 0);
    } catch (NumberFormatException exception) {
      return positionalIndex;
    }
  }

  @Override
  protected int textureIndex(
      JsonObject elementObject, List<ModelRenderProfileValidationIssue> issues)
      throws EasyModelDecodeException {
    JsonElement facesElement = elementObject.get("faces");
    if (facesElement == null || !facesElement.isJsonObject()) {
      return 0;
    }

    JsonObject faces = facesElement.getAsJsonObject();
    Integer cubeIndex = null;
    boolean mixed = false;
    for (ModelCubeFace face : ModelCubeFace.values()) {
      JsonElement faceElement = faces.get(face.getTagName());
      if (faceElement == null || !faceElement.isJsonObject()) {
        continue;
      }
      JsonElement textureElement = faceElement.getAsJsonObject().get("texture");
      if (textureElement == null
          || textureElement.isJsonNull()
          || !textureElement.isJsonPrimitive()
          || !textureElement.getAsJsonPrimitive().isNumber()) {
        continue;
      }
      int faceIndex = Math.max(textureElement.getAsInt(), 0);
      if (cubeIndex == null) {
        cubeIndex = faceIndex;
      } else if (cubeIndex != faceIndex) {
        mixed = true;
      }
    }

    if (cubeIndex == null) {
      return 0;
    }
    if (mixed) {
      issues.add(
          warning(
              "texture",
              "Cube "
                  + optionalString(elementObject, "name", "cube")
                  + " references multiple texture indices; using "
                  + cubeIndex
                  + "."));
    }

    return cubeIndex;
  }

  @Override
  protected List<DecodedTexture> parseTextures(JsonObject root) throws EasyModelDecodeException {
    JsonElement texturesElement = root.get("textures");
    if (texturesElement == null || texturesElement.isJsonNull()) {
      return List.of();
    }
    if (!texturesElement.isJsonArray()) {
      throw new EasyModelDecodeException("Field textures must be an array.");
    }

    JsonArray texturesArray = texturesElement.getAsJsonArray();
    List<DecodedTexture> textures = new ArrayList<>();
    int positionalIndex = 0;
    for (JsonElement textureElement : texturesArray) {
      JsonObject textureObject = requireObjectElement(textureElement, "textures");
      int index = parseTextureId(textureObject, positionalIndex);
      textures.add(
          new DecodedTexture(
              index,
              optionalString(textureObject, "namespace", ""),
              optionalString(textureObject, "folder", ""),
              optionalString(textureObject, "relative_path", ""),
              optionalString(textureObject, "name", ""),
              optionalNonNegativeInt(textureObject, "uv_width", 0),
              optionalNonNegativeInt(textureObject, "uv_height", 0)));
      positionalIndex++;
    }

    return textures;
  }
}
