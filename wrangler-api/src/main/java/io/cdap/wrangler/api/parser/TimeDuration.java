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
 * Represents a parsed time duration token (e.g., "150ms", "2s").
 */
@PublicEvolving
public class TimeDuration implements Token {
  private final long milliseconds;

  public TimeDuration(String input) {
    input = input.trim().toLowerCase();
    long multiplier;
    if (input.endsWith("ms")) {
      multiplier = 1L;
      input = input.substring(0, input.length() - 2);
    } else if (input.endsWith("s")) {
      multiplier = 1000L;
      input = input.substring(0, input.length() - 1);
    } else if (input.endsWith("m")) {
      multiplier = 60L * 1000L;
      input = input.substring(0, input.length() - 1);
    } else if (input.endsWith("h")) {
      multiplier = 60L * 60L * 1000L;
      input = input.substring(0, input.length() - 1);
    } else {
      throw new IllegalArgumentException("Invalid time duration format: " + input);
    }
    this.milliseconds = (long)(Double.parseDouble(input) * multiplier);
  }

  public long getMilliseconds() {
    return milliseconds;
  }

  @Override
  public Object value() {
    return milliseconds;
  }

  @Override
  public TokenType type() {
    return TokenType.TIME_DURATION;
  }

  @Override
  public JsonElement toJson() {
    return new JsonPrimitive(milliseconds);
  }
}

