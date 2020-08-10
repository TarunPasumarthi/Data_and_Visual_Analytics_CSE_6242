package edu.gatech.cse6242;

import java.io.IOException;
import java.util.StringTokenizer;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class Q1 {

  public static class TokenizerMapper extends Mapper<Object, Text, Text, Text>{

    private Text word = new Text();

    public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
      StringTokenizer itr = new StringTokenizer(value.toString(),"\n");
      while (itr.hasMoreTokens()) {
        String[] line= itr.nextToken().split("\t");
        word.set(line[0]);
        Text v = new Text(line[1]+","+line[2]);
        context.write(word, v);
      }
    }
  }

  public static class IntSumReducer extends Reducer<Text,Text,Text,Text> {
    
    private Text result = new Text();

    public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
    
      int target=0;
      int best=0;

      for (Text val : values) {
        String[] line= val.toString().split(",");
        Integer node= Integer.parseInt(line[0]);
        Integer weight= Integer.parseInt(line[1]);
        if(weight>best || (weight==best && node<target)){
          target=node;
          best=weight;
        }
      }
      String res=target+","+best;
      result.set(res);
      context.write(key, result);
    }
  }

  public static void main(String[] args) throws Exception {
    Configuration conf = new Configuration();
    Job job = Job.getInstance(conf, "word count");
    job.setJarByClass(Q1.class);
    job.setMapperClass(TokenizerMapper.class);
    job.setCombinerClass(IntSumReducer.class);
    job.setReducerClass(IntSumReducer.class);
    job.setOutputKeyClass(Text.class);
    job.setOutputValueClass(Text.class);
    FileInputFormat.addInputPath(job, new Path(args[0]));
    FileOutputFormat.setOutputPath(job, new Path(args[1]));
    System.exit(job.waitForCompletion(true) ? 0 : 1);
  }
}
