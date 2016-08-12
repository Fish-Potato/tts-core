package test;

import com.tts.util.JsonUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by zhaoqi on 2016/8/12 0012.
 */
public class RegexTest {
    public static void main(String[] args) {
        String expression = "anyWords#{productId}anyWords#{ProductName}anyWords";
        String regex = "\\{.*\\}";
        Pattern pattern = Pattern.compile(regex);

        List<String> arguments = new ArrayList<>();
            String[] splitStr = expression.split("#");
            for (String str : splitStr) {
                Matcher mat = pattern.matcher(str);
                if (mat.find()) {
                    arguments.add(mat.group(0).substring(1,mat.group(0).length()-1));
                }

            }
            System.out.println(JsonUtil.toString(arguments));

//        String expression = "anyWords#{productId}anyWords#{ProductName}anyWords";
//        String regex = "^([\\{.*\\}])";
//        Pattern pattern = Pattern.compile(regex);
//
//        List<String> arguments = new ArrayList<>();
//        String[] splitStr = expression.split("#");
//        for (String str : splitStr) {
//            Matcher mat = pattern.matcher(str);
//            if (mat.find()) {
//                arguments.add(mat.replaceAll(""));
//            }
//
//        }
//        System.out.println(JsonUtil.toString(arguments));
//        String str = "Java目前的发展史是由{0}年-{1}年";
//        String[][] object={new String[]{"\\{0\\}","1995"},new String[]{"\\{1\\}","2007"}};
//        System.out.println(replace(str,object));
    }

    public static String replace(final String sourceString,Object[] object) {
        String temp=sourceString;
        for(int i=0;i<object.length;i++){
            String[] result=(String[])object[i];
            Pattern    pattern = Pattern.compile(result[0]);
            Matcher matcher = pattern.matcher(temp);
            temp=matcher.replaceAll(result[1]);
        }
        return temp;
    }

}
