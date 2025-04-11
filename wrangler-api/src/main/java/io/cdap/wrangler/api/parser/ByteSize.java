/*
 * Copyright © 2017-2019 Cask Data, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package io.cdap.wrangler.api.parser;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import io.cdap.wrangler.api.annotations.PublicEvolving;

/**
 * Represents a parsed byte size token (e.g., "10KB", "5MB").
 */
@PublicEvolving
public class ByteSize implements Token {
  private final long bytes;

  public ByteSize(String input) {
    input = input.trim().toUpperCase();
    long multiplier;
    if (input.endsWith("KB")) {
      multiplier = 1024L;
      input = input.substring(0, input.length() - 2);
    } else if (input.endsWith("MB")) {
      multiplier = 1024L * 1024L;
      input = input.substring(0, input.length() - 2);
    } else if (input.endsWith("GB")) {
      multiplier = 1024L * 1024L * 1024L;
      input = input.substring(0, input.length() - 2);
    } else if (input.endsWith("B")) {
      multiplier = 1L;
      input = input.substring(0, input.length() - 1);
    } else {
      throw new IllegalArgumentException("Invalid byte size format: " + input);
    }
    this.bytes = (long)(Double.parseDouble(input) * multiplier);
  }

  public long getBytes() {
    return bytes;
  }

  @Override
  public Object value() {
    return bytes;
  }

  @Override
  public TokenType type() {
    return TokenType.BYTE_SIZE;
  }

  @Override
  public JsonElement toJson() {
    return new JsonPrimitive(bytes);
  }
}
