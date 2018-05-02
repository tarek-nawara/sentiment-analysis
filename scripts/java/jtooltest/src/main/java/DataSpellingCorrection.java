import com.fasterxml.jackson.databind.ObjectMapper;
import org.languagetool.JLanguageTool;
import org.languagetool.language.AmericanEnglish;
import org.languagetool.rules.RuleMatch;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DataSpellingCorrection {

    private static PrintWriter printWriter;
    private static JLanguageTool langTool = new JLanguageTool(new AmericanEnglish());
    private static List<String> buffer = new ArrayList<>();
    private static int idx = 0;

    public static void main(String[] args) throws IOException {
        printWriter = new PrintWriter(new File("correct-tweets/tweet" + System.currentTimeMillis() + ".json"));
        correctTweets();
    }

    private static void correctTweets() throws IOException {
        Files.walk(Paths.get("labeled-tweets"))
                .peek(System.out::println)
                .filter(p -> !p.toString().equals("labeled-tweets"))
                .filter(p -> !p.toString().equals("labeled-tweets/instructions.txt"))
                .filter(p -> !p.toString().equals("labeled-tweets/.DS_Store"))
                .forEach(p -> readFile(p.toString()));
        printWriter.close();

    }

    private static void writeToFile(Tweet tweet) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            buffer.add(objectMapper.writeValueAsString(tweet));
            if(idx++ % 1000 == 0) {
                System.out.println(idx);
                for(String s: buffer) {
                    printWriter.println(s);
                }
                buffer.clear();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Optional<Tweet> correct(Tweet tweet) {
        String input = tweet.getMsg();
        try {
            List<RuleMatch> matches = langTool.check(input)
                    .stream()
                    .filter(l -> !l.getSuggestedReplacements().isEmpty())
                    .sorted(Comparator.comparingInt(RuleMatch::getFromPos))
                    .collect(Collectors.toList());
            StringBuilder stringBuilder = new StringBuilder();
            int idx = 0;
            for (int i = 0; i < input.length(); ) {
                if (idx < matches.size() && i == matches.get(idx).getFromPos()) {
                    i = matches.get(idx).getToPos() + 1;
                    stringBuilder.append(matches.get(idx).getSuggestedReplacements().get(0));
                    ++idx;
                } else {
                    stringBuilder.append(input.charAt(i));
                    ++i;
                }
            }
            return Optional.of(new Tweet(stringBuilder.toString(), tweet.getLabel()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    private static void readFile(String fileName) {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(fileName)));
            reader.lines().forEach(l -> {
                Optional<Tweet> tweet = correct(parseCSV(l));
                tweet.ifPresent(DataSpellingCorrection::writeToFile);
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static Tweet parseCSV(String line) {
        Pattern pattern = Pattern.compile("\"(.+)\",\".+\",\".+\",\".+\",\".+\",\"(.+)\"");
        Matcher matcher = pattern.matcher(line);
        if (matcher.find()) {
            return new Tweet(matcher.group(2), Double.parseDouble(matcher.group(1)));
        }
        return null;
    }
}

class Tweet {
    private String msg;
    private double label;

    public Tweet() {
    }

    public Tweet(String msg, double label) {
        this.msg = msg;
        this.label = label;
    }

    public String getMsg() {
        return msg;
    }

    public double getLabel() {
        return label;
    }
}
