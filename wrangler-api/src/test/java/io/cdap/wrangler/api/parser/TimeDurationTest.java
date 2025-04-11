/*
 * Copyright © 2025 [Your Name or Organization]
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

 import org.junit.Assert;
 import org.junit.Test;
 
 /**
  * Unit tests for the TimeDuration class.
  */
 public class TimeDurationTest {
 
   @Test
   public void testParseMilliseconds() {
     TimeDuration duration = new TimeDuration("5ms");
     Assert.assertEquals(5L, duration.getMilliseconds());
   }
 
   @Test
   public void testParseSeconds() {
     TimeDuration duration = new TimeDuration("2.1s");
     Assert.assertEquals(2100L, duration.getMilliseconds());
   }
 
   @Test
   public void testParseMinutes() {
     TimeDuration duration = new TimeDuration("1m");
     Assert.assertEquals(60000L, duration.getMilliseconds());
   }
 
   @Test
   public void testParseHours() {
     TimeDuration duration = new TimeDuration("1h");
     Assert.assertEquals(3600000L, duration.getMilliseconds());
   }
 
   @Test(expected = IllegalArgumentException.class)
   public void testInvalidInput() {
     new TimeDuration("invalid"); // Should throw exception
   }
 
   @Test(expected = IllegalArgumentException.class)
   public void testNoUnit() {
     new TimeDuration("5"); // Should throw exception due to missing unit
   }
 }