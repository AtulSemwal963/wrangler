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

 package io.cdap.directives.aggregates;

 import io.cdap.wrangler.TestingRig;
 import io.cdap.wrangler.api.Row;
 import org.junit.Assert;
 import org.junit.Test;
 
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.List;
 
 /**
  * Unit tests for the AggregateStats directive, following the Test Case Specification.
  */
 public class AggregateStatsTest {
 
   @Test
   public void testTotalAggregation() throws Exception {
     String[] recipe = new String[] {
       "aggregate-stats :data_transfer_size :response_time total_size_mb total_time_sec MB seconds total"
     };
     List<Row> rows = Arrays.asList(
       new Row("data_transfer_size", "10KB").add("response_time", "150ms"),
       new Row("data_transfer_size", "5MB").add("response_time", "2s")
     );
 
     List<Row> results = TestingRig.execute(recipe, rows);
     Assert.assertEquals(1, results.size());
     Row result = results.get(0);
     double totalSizeMb = (double) result.getValue("total_size_mb");
     double totalTimeSec = (double) result.getValue("total_time_sec");
 
     // 10KB = 0.01MB, 5MB = 5MB, Total = 5.01MB (1024-based)
     // 150ms = 0.15s, 2s = 2s, Total = 2.15s
     Assert.assertEquals(5.01, totalSizeMb, 0.001);
     Assert.assertEquals(2.15, totalTimeSec, 0.001);
   }
 
   @Test
   public void testAverageAggregation() throws Exception {
     String[] recipe = new String[] {
       "aggregate-stats :data_transfer_size :response_time total_size_mb total_time_sec MB seconds average"
     };
     List<Row> rows = Arrays.asList(
       new Row("data_transfer_size", "10KB").add("response_time", "150ms"),
       new Row("data_transfer_size", "5MB").add("response_time", "2s")
     );
 
     List<Row> results = TestingRig.execute(recipe, rows);
     Assert.assertEquals(1, results.size());
     Row result = results.get(0);
     double avgSizeMb = (double) result.getValue("total_size_mb");
     double avgTimeSec = (double) result.getValue("total_time_sec");
 
     // Total: 5.01MB / 2 = 2.505MB
     // Total: 2.15s / 2 = 1.075s
     Assert.assertEquals(2.505, avgSizeMb, 0.001);
     Assert.assertEquals(1.075, avgTimeSec, 0.001);
   }
 
   @Test
   public void testInvalidInputHandling() throws Exception {
     String[] recipe = new String[] {
       "aggregate-stats :data_transfer_size :response_time total_size_mb total_time_sec MB seconds total"
     };
     List<Row> rows = Arrays.asList(
       new Row("data_transfer_size", "invalid").add("response_time", "150ms"),
       new Row("data_transfer_size", "5MB").add("response_time", "invalid")
     );
 
     List<Row> results = TestingRig.execute(recipe, rows);
     Assert.assertEquals(1, results.size());
     Row result = results.get(0);
     double totalSizeMb = (double) result.getValue("total_size_mb");
     double totalTimeSec = (double) result.getValue("total_time_sec");
 
     // Only 5MB and 150ms counted
     Assert.assertEquals(5.0, totalSizeMb, 0.001);
     Assert.assertEquals(0.15, totalTimeSec, 0.001);
   }
 
   @Test
   public void testNoRows() throws Exception {
     String[] recipe = new String[] {
       "aggregate-stats :data_transfer_size :response_time total_size_mb total_time_sec MB seconds total"
     };
     List<Row> rows = Collections.emptyList();
 
     List<Row> results = TestingRig.execute(recipe, rows);
     Assert.assertEquals(0, results.size());
   }
 
   @Test(expected = Exception.class)
   public void testInvalidSyntax() throws Exception {
     String[] recipe = new String[] {
       "aggregate-stats :size :time total_size_mb" // Missing total_time_sec
     };
     List<Row> rows = Arrays.asList(new Row("size", "10KB").add("time", "150ms"));
     TestingRig.execute(recipe, rows); // Should throw parsing exception
   }
 }