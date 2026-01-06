import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Jsonparser {

    public static Map<String, String> parseJson(String input) {
        // 1. 语法修正：需要加 'new' 关键字
        Map<String, String> parsed = new HashMap<>();
        
        // 2. 补全正则表达式
        // 逻辑：寻找成对的 "键": "值"
        // \"(.*?)\"  -> 捕获组1：匹配被双引号包围的 Key (使用非贪婪匹配 .*?)
        // \\s*:\\s* -> 匹配冒号，以及冒号前后的任意空白字符
        // \"(.*?)\"  -> 捕获组2：匹配被双引号包围的 Value
        String regex = "\"(.*?)\"\\s*:\\s*\"(.*?)\"";
        
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(input);

        // 3. 循环查找并存入 Map
        while (m.find()) {
            String key = m.group(1);   // 提取正则表达式中第 1 个括号里的内容 (键)
            String value = m.group(2); // 提取正则表达式中第 2 个括号里的内容 (值)
            parsed.put(key, value);
        }
        return parsed;
    }

    public static void main(String[] args) {
        // 测试代码
        String jsonStr = "{\"name\": \"oftheloneiness\", \"role\": \"admin\", \"id\": \"9527\"}";
        
        System.out.println("原始 JSON: " + jsonStr);
        Map<String, String> result = parseJson(jsonStr);
        
        System.out.println("解析结果 Map: " + result);
        System.out.println("获取 name: " + result.get("name"));
    }
}
