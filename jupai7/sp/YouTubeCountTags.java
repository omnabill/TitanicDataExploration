
package jupai7.sp;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class YouTubeCountTags {

    private static final String COMMA_DELIMITER = ",";
    public static void main(String[] args) throws IOException {
        Logger.getLogger ("org").setLevel (Level.ERROR);
        // CREATE SPARK CONTEXT
        SparkConf conf = new SparkConf ().setAppName ("wordCounts").setMaster ("local[2]");
        JavaSparkContext sparkContext = new JavaSparkContext (conf);
        // LOAD DATASETS
        JavaRDD<String> videos = sparkContext.textFile ("src/main/resources/data/USvideos.csv");
        // TRANSFORMATIONS
        countTags(videos);
    }
    public static String extractTag(String videoLine) {
        try {
            return videoLine.split (COMMA_DELIMITER)[6];
        } catch (ArrayIndexOutOfBoundsException e) {
            return "";
        }
    }


    public static void countTags(JavaRDD<String> videos){
        LocalTime start= LocalTime.now ();
        JavaRDD<String> tags = videos
                .map (YouTubeCountTags::extractTag)
                .filter (StringUtils::isNotBlank);
        // JavaRDD<String>
        JavaRDD<String> words = tags.flatMap (tag -> Arrays.asList (tag
                .toLowerCase ()
                .trim ()
                .replaceAll ("\\p{Punct}", " ")
                .split (" ")).iterator ());
        System.out.println(words.toString ());
        // COUNTING
        Map<String, Long> wordCounts = words.countByValue ();
        List<Map.Entry> sorted = wordCounts.entrySet ().stream ()
                .sorted (Map.Entry.comparingByValue ()).collect (Collectors.toList ());
        // DISPLAY
        for (Map.Entry entry : sorted) {
            System.out.println (entry.getKey () + " : " + entry.getValue ());
        }
        LocalTime end= LocalTime.now ();
        Duration duration= Duration.between (start,end);
        System.out.println("processing time is :"+duration.getNano ());
    }

}