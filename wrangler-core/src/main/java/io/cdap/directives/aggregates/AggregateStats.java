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

 import io.cdap.wrangler.api.Arguments;
 import io.cdap.wrangler.api.Directive;
 import io.cdap.wrangler.api.ExecutorContext;
 import io.cdap.wrangler.api.Row;
 import io.cdap.wrangler.api.TransientVariableScope;
 import io.cdap.wrangler.api.annotations.Categories;
 import io.cdap.wrangler.api.parser.ByteSize;
 import io.cdap.wrangler.api.parser.ColumnName;
 import io.cdap.wrangler.api.parser.Text;
 import io.cdap.wrangler.api.parser.TimeDuration;
 import io.cdap.wrangler.api.parser.TokenType;
 import io.cdap.wrangler.api.parser.UsageDefinition;
 
 import java.util.Collections;
 import java.util.List;
 
 /**
  * A directive that aggregates byte sizes and time durations from source columns into target columns.
  * Usage: aggregate-stats <size_col> <time_col> <total_size> <total_time> [size_unit] [time_unit] [type]
  * Example: aggregate-stats :data_size :response_time total_size_mb total_time_sec MB seconds total
  */
 @Categories(categories = {"aggregates"})
 public class AggregateStats implements Directive {
   private String sizeColumn;
   private String timeColumn;
   private String totalSizeColumn;
   private String totalTimeColumn;
   private String sizeUnit;
   private String timeUnit;
   private String aggregationType;
   private static final String STORE_KEY = "aggregate-stats-totals";
 
   @Override
   public UsageDefinition define() {
     UsageDefinition.Builder builder = UsageDefinition.builder("aggregate-stats");
     builder.define("sizeColumn", TokenType.COLUMN_NAME);
     builder.define("timeColumn", TokenType.COLUMN_NAME);
     builder.define("totalSizeColumn", TokenType.COLUMN_NAME);
     builder.define("totalTimeColumn", TokenType.COLUMN_NAME);
     builder.define("sizeUnit", TokenType.TEXT, true); // Changed to TEXT
     builder.define("timeUnit", TokenType.TEXT, true); // Changed to TEXT
     builder.define("aggregationType", TokenType.TEXT, true);
     return builder.build();
   }
 
   @Override
   public void initialize(Arguments args) {
     sizeColumn = ((ColumnName) args.value("sizeColumn")).value();
     timeColumn = ((ColumnName) args.value("timeColumn")).value();
     totalSizeColumn = ((ColumnName) args.value("totalSizeColumn")).value();
     totalTimeColumn = ((ColumnName) args.value("totalTimeColumn")).value();
     sizeUnit = args.contains("sizeUnit") ? ((Text) args.value("sizeUnit")).value() : "bytes";
     timeUnit = args.contains("timeUnit") ? ((Text) args.value("timeUnit")).value() : "ns";
     aggregationType = args.contains("aggregationType") ? ((Text) args.value("aggregationType")).value() : "total";
     if (!"total".equals(aggregationType) && !"average".equals(aggregationType)) {
       throw new IllegalArgumentException("Aggregation type must be 'total' or 'average', got: " + aggregationType);
     }
   }
 
   @Override
   public List<Row> execute(List<Row> rows, ExecutorContext context) {
     if (context == null) {
       throw new IllegalStateException("ExecutorContext is null; aggregation not supported.");
     }
 
     Totals totals = (Totals) context.getTransientStore().get(STORE_KEY);
     if (totals == null) {
       totals = new Totals();
       context.getTransientStore().set(TransientVariableScope.GLOBAL, STORE_KEY, totals);
     }
 
     int rowCount = 0;
     for (Row row : rows) {
       rowCount++;
       Object sizeValue = row.getValue(sizeColumn);
       Object timeValue = row.getValue(timeColumn);
 
       if (sizeValue instanceof String) {
         try {
           ByteSize byteSize = new ByteSize((String) sizeValue);
           totals.totalBytes += byteSize.getBytes();
         } catch (IllegalArgumentException e) {
           // Silent skip
         }
       }
 
       if (timeValue instanceof String) {
         try {
           TimeDuration timeDuration = new TimeDuration((String) timeValue);
           totals.totalNanos += timeDuration.getMilliseconds() * 1000000L;
         } catch (IllegalArgumentException e) {
           // Silent skip
         }
       }
     }
 
     context.getTransientStore().set(TransientVariableScope.GLOBAL, STORE_KEY, totals);
 
     if (rows.isEmpty() && rowCount > 0) {
       double sizeResult = convertBytes(totals.totalBytes, sizeUnit);
       double timeResult = convertNanos(totals.totalNanos, timeUnit);
 
       if ("average".equals(aggregationType) && rowCount > 0) {
         sizeResult /= rowCount;
         timeResult /= rowCount;
       }
 
       Row result = new Row();
       result.add(totalSizeColumn, sizeResult);
       result.add(totalTimeColumn, timeResult);
       return Collections.singletonList(result);
     }
 
     return Collections.emptyList();
   }
 
   @Override
   public void destroy() {
     // No-op
   }
 
   private static class Totals {
     long totalBytes = 0;
     long totalNanos = 0;
   }
 
   private double convertBytes(long bytes, String unit) {
     String u = unit.trim().toUpperCase();
     switch (u) {
       case "B":
       case "BYTES": return bytes;
       case "KB": return bytes / 1024.0;
       case "MB": return bytes / (1024.0 * 1024.0);
       case "GB": return bytes / (1024.0 * 1024.0 * 1024.0);
       case "TB": return bytes / (1024.0 * 1024.0 * 1024.0 * 1024.0);
       default: throw new IllegalArgumentException("Unsupported size unit: " + unit);
     }
   }
 
   private double convertNanos(long nanos, String unit) {
     String u = unit.trim().toLowerCase();
     switch (u) {
       case "ns": return nanos;
       case "us": return nanos / 1000.0;
       case "ms": return nanos / 1000000.0;
       case "s":
       case "seconds": return nanos / 1000000000.0;
       case "m":
       case "minutes": return nanos / (60.0 * 1000000000.0);
       case "h": return nanos / (3600.0 * 1000000000.0);
       default: throw new IllegalArgumentException("Unsupported time unit: " + unit);
     }
   }
 }
