// Databricks notebook source
// Q2 [25 pts]: Analyzing a Large Graph with Spark/Scala on Databricks

// STARTER CODE - DO NOT EDIT THIS CELL
import org.apache.spark.sql.functions.desc
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types._
import spark.implicits._

// COMMAND ----------

// STARTER CODE - DO NOT EDIT THIS CELL
// Definfing the data schema
val customSchema = StructType(Array(StructField("answerer", IntegerType, true), StructField("questioner", IntegerType, true),
    StructField("timestamp", LongType, true)))

// COMMAND ----------

// STARTER CODE - YOU CAN LOAD ANY FILE WITH A SIMILAR SYNTAX.
// MAKE SURE THAT YOU REPLACE THE examplegraph.csv WITH THE mathoverflow.csv FILE BEFORE SUBMISSION.
val df = spark.read
   .format("com.databricks.spark.csv")
   .option("header", "false") // Use first line of all files as header
   .option("nullValue", "null")
   .schema(customSchema)
   .load("/FileStore/tables/mathoverflow.csv")
   .withColumn("date", from_unixtime($"timestamp"))
   .drop($"timestamp")

// COMMAND ----------

//display(df)
df.show()

// COMMAND ----------

// PART 1: Remove the pairs where the questioner and the answerer are the same person.
// ALL THE SUBSEQUENT OPERATIONS MUST BE PERFORMED ON THIS FILTERED DATA

val df_rem= df.where("questioner != answerer")

// COMMAND ----------

// PART 2: The top-3 individuals who answered the most number of questions - sorted in descending order - if tie, the one with lower node-id gets listed first : the nodes with the highest out-degrees.
import org.graphframes._
import org.apache.spark.sql._
import org.apache.spark.sql.functions._


val edges= df_rem.selectExpr("cast(questioner as long) src", " cast(answerer as long) dst", "cast(date as date)")
val vert= df_rem.selectExpr("cast(questioner as long) id")
val graph= GraphFrame(vert, edges)
display(graph.inDegrees.orderBy(desc("inDegree")).limit(3))


// COMMAND ----------

// PART 3: The top-3 individuals who asked the most number of questions - sorted in descending order - if tie, the one with lower node-id gets listed first : the nodes with the highest in-degree.

val mostqs = graph.outDegrees.orderBy(desc("outDegree")).show(3)


// COMMAND ----------

// PART 4: The top-5 most common asker-answerer pairs - sorted in descending order - if tie, the one with lower value node-id in the first column (u->v edge, u value) gets listed first.

val pairs= graph.edges.groupBy("src", "dst").count().orderBy(desc("count"), asc("dst"), asc("src")).limit(5)
display(pairs)

// COMMAND ----------

// PART 5: Number of interactions (questions asked/answered) over the months of September-2010 to December-2010 (i.e. from September 1, 2010 to December 31, 2010). List the entries by month from September to December.

// Reference: https://www.obstkel.com/blog/spark-sql-date-functions
// Read in the data and extract the month and year from the date column.
// Hint: Check how we extracted the date from the timestamp.

import org.apache.spark.sql.functions.to_date 
import org.apache.spark.sql.functions.to_timestamp

val monthly_interactions= graph
    .edges
    .groupBy(month(to_timestamp($"date")) as "m", year(to_timestamp($"date")) as "y")
    .count() 
    .filter("m >= 9")
    .filter("y == 2010")
    .drop($"y")
    .orderBy(asc("m"))

 display(monthly_interactions) 


// COMMAND ----------

// PART 6: List the top-3 individuals with the maximum overall activity, i.e. total questions asked and questions answered.

val most_activity= graph.inDegrees.join(graph.outDegrees, graph.inDegrees.col("id")=== graph.outDegrees.col("id"))
  .drop(graph.outDegrees.col("id"))
  .selectExpr("id", " inDegree+outDegree as activity")
  .orderBy(desc("activity"))
  .limit(3)
display(most_activity)

