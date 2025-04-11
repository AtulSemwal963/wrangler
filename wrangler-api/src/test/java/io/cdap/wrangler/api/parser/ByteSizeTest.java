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
  * Unit tests for the ByteSize class.
  */
 public class ByteSizeTest {
 
   @Test
   public void testParseBytes() {
     ByteSize size = new ByteSize("10B");
     Assert.assertEquals(10L, size.getBytes());
   }
 
   @Test
   public void testParseKilobytes() {
     ByteSize size = new ByteSize("10KB");
     Assert.assertEquals(10L * 1024, size.getBytes());
   }
 
   @Test
   public void testParseMegabytes() {
     ByteSize size = new ByteSize("1.5MB");
     Assert.assertEquals((long) (1.5 * 1024 * 1024), size.getBytes());
   }
 
   @Test
   public void testParseGigabytes() {
     ByteSize size = new ByteSize("2GB");
     Assert.assertEquals(2L * 1024 * 1024 * 1024, size.getBytes());
   }
 
   @Test(expected = IllegalArgumentException.class)
   public void testInvalidInput() {
     new ByteSize("invalid"); // Should throw exception
   }
 
   @Test(expected = IllegalArgumentException.class)
   public void testNoUnit() {
     new ByteSize("10"); // Should throw exception due to missing unit
   }
 }