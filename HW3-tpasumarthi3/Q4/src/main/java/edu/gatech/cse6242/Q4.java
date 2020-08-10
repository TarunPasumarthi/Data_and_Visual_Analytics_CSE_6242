package edu.gatech.cse6242;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.*;
import org.apache.hadoop.util.*;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import java.io.IOException;

import java.util.StringTokenizer;

public class Q4 {

  public static class P1_mapper extends Mapper<Object, Text, IntWritable, IntWritable>{

    private final static IntWritable one = new IntWritable(1);
	  private final static IntWritable neg_one = new IntWritable(-1);
	  private IntWritable source = new IntWritable();
	  private IntWritable target = new IntWritable();

    public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
      String itr = value.toString();
      if (itr.length()<=1) {
        return;
      }
      String [] line= itr.split("\t");
      source.set(Integer.parseInt(line[0]));
      target.set(Integer.parseInt(line[1]));
      context.write(source, one);
      context.write(target, neg_one);
    }
  }

  public static class P1_reducer extends Reducer<IntWritable,IntWritable,IntWritable,IntWritable> {
    private IntWritable result = new IntWritable();

    public void reduce(IntWritable key, Iterable<IntWritable> values, Context context) 
  				throws IOException, InterruptedException{
  					int initTotal = 0;
  					for(IntWritable value: values)
  						initTotal += value.get();
  					context.write(key, new IntWritable(initTotal));}
  }

  public static class P2_mapper extends Mapper<Object, Text, IntWritable, IntWritable>{

    private final static IntWritable one = new IntWritable(1);
	  private IntWritable diff = new IntWritable();

    public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
      String itr = value.toString();
      if (itr.length()<=1) {
        return;
      }
      String [] line= itr.split("\t");
  		diff.set(Integer.parseInt(line[1]));
		  context.write(diff, one);
    }
  }

  public static class P2_reducer extends Reducer<IntWritable,IntWritable,IntWritable,IntWritable> {
    private IntWritable result = new IntWritable();

    public void reduce(IntWritable key, Iterable<IntWritable> values, Context context) 
  				throws IOException, InterruptedException{
  					int initTotal = 0;
  					for(IntWritable value: values)
  						initTotal += value.get();
  					context.write(key, new IntWritable(initTotal));}
  }

public static void main(String[] args) throws Exception {
  Configuration conf1 = new Configuration();
    Job job1 = Job.getInstance(conf1, "part1");
    job1.setJarByClass(Q4.class);
    job1.setMapperClass(P1_mapper.class);
    job1.setReducerClass(P1_reducer.class);
    job1.setOutputKeyClass(IntWritable.class);
    job1.setOutputValueClass(IntWritable.class);
    FileInputFormat.addInputPath(job1, new Path(args[0]));
    FileOutputFormat.setOutputPath(job1, new Path(args[1]+"_output_Dir"));
    job1.waitForCompletion(true);

	Configuration conf2 = new Configuration();
    Job job2 = Job.getInstance(conf2, "part2");
    job2.setJarByClass(Q4.class);
    job2.setMapperClass(P2_mapper.class);
    job2.setReducerClass(P2_reducer.class);
    job2.setOutputKeyClass(IntWritable.class);
    job2.setOutputValueClass(IntWritable.class);
    FileInputFormat.addInputPath(job2, new Path(args[1]+"_output_Dir"));
    FileOutputFormat.setOutputPath(job2, new Path(args[1]));
    System.exit(job2.waitForCompletion(true)?0:1);
  }
}