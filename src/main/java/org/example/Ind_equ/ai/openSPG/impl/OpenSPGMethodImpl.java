package org.example.Ind_equ.ai.openSPG.impl;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.example.Ind_equ.ai.openSPG.OpenSPGMethod;
import org.python.antlr.ast.Str;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class OpenSPGMethodImpl implements OpenSPGMethod {
     private static final String API_URL = "http://100.79.117.62:8008/v1/completions";
    private static final String MODEL_NAME = "OneKE"; // 替换为实际的模型名称

    @Override
    public String ExtractDeviceRelation(String text){
        String prompt = DeviceRelationPrompt(text);
        String output = extractInformation(text,prompt);
//        System.out.println(output);
        // 提取 "搭载"、"配备"、"采用" 的 object 放入集合1
//        String output = """
//                 {
//                  "配备": [{"subject": "AEx-2410防爆Box PC", "object": "SUS 316不锈钢材质"}, {"subject": "AEx-2410防爆Box PC", "object": "M12防水连接器"}],
//                   "采用": [{"subject": "AEx-2410防爆Box PC", "object": "SUS 316不锈钢材质"}], "应用领域": []}
//                """;
        // 如果output不是json格式，返回null
        try {
            Set<String> set1 = new HashSet<>();
            set1.addAll(extractObjectsFromJson(output, "搭载"));
            set1.addAll(extractObjectsFromJson(output, "配备"));
            set1.addAll(extractObjectsFromJson(output, "采用"));

            // 提取 "应用领域" 的 object 放入集合2
            Set<String> set2 = extractObjectsFromJson(output, "应用领域");

            // 打印结果
    //        System.out.println("集合1: " + set1);
    //        System.out.println("集合2: " + set2);
            List<Set<String>> setoutput = new ArrayList<>();
            setoutput.add(set1);
            setoutput.add(set2);
            String setString = setoutput.toString();
    //        System.out.println(setString);
            return setString;
        } catch (Exception e) {
            System.out.println("Error parsing JSON: " + e.getMessage());
            return null;
        }
    }

    @Override
    public String ExtractClassification(String text){
        String prompt = FindClassificationPrompt(text);
        return extractInformation(text,prompt);
    }

    private String extractInformation(String text,String prompt) {
        try {
            // 构建请求体
            JsonObject requestBody = new JsonObject();
            requestBody.addProperty("model", MODEL_NAME);
            requestBody.addProperty("prompt", prompt);
            requestBody.addProperty("max_tokens", 1024);
            requestBody.addProperty("temperature", 0);

            // 发送HTTP POST请求
            CloseableHttpClient httpClient = HttpClients.createDefault();
            HttpPost httpPost = new HttpPost(API_URL);
            httpPost.setHeader("Content-Type", "application/json");
            httpPost.setEntity(new StringEntity(requestBody.toString(), StandardCharsets.UTF_8));

            CloseableHttpResponse response = httpClient.execute(httpPost);
            //获取response的内容
            String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
            // 打印完整响应
//            System.out.println("\n\n\n\n\nAPI Response: " + responseBody);

            // 解析响应
            JsonObject jsonResponse = new Gson().fromJson(responseBody, JsonObject.class);
            if (jsonResponse.has("error")) {
                throw new RuntimeException("API returned an error: " + jsonResponse.get("error").getAsString());
            }
            if (!jsonResponse.has("choices")) {
                throw new RuntimeException("Response does not contain 'choices' field. Full response: " + responseBody);
            }
            JsonArray choices = jsonResponse.getAsJsonArray("choices");
            if (choices == null || choices.size() == 0) {
                throw new RuntimeException("Response does not contain any choices. Full response: " + responseBody);
            }
            String extractedInfoJson = choices.get(0).getAsJsonObject().get("text").getAsString();

            // 检查 extractedInfoJson 是否为空或无效
            if (extractedInfoJson == null || extractedInfoJson.trim().isEmpty()) {
                throw new RuntimeException("Extracted information is empty or invalid. Full response: " + responseBody);
            }

            // 将提取的信息解析为Map
//            Map<String, String> extractedInfo = parseExtractedInfo(extractedInfoJson);
//            System.out.println(extractedInfoJson);
            return extractedInfoJson;
//            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    private String DeviceRelationPrompt(String text) {
        return String.format("""
                [INST] <<SYS>>你是一个工业设备知识领域的专家，是乐于助人的助手。
                <</SYS>>{"instruction": "您是命名实体关系识别方面的专家。
                input内容是某一工业设备实体的产品描述，请从input中抽取出符合schema定义的关系三元组，不存在的关系三元组不输出，同个关系可能存在多个不同客体的三元组。
                请按照JSON字符串的格式回答。",
                 "schema": [{"搭载": "该关系描述了该工业设备A实体上面"搭载"/"板载"了另一个工业设备B，那么A的关系"搭载"的客体为"B""}，
                            {"配备": "该关系描述了该工业设备A实体上面"配备"了另一个工业设备B，那么A的关系"配备"的客体为"B"，注意："配备"的属性值一定是工业设备实体，排除不符合工业设备实体的客体"}，
                            {"采用": "该关系描述了该工业设备A实体上面"采用"/"使用"了另一个工业设备B，那么A的关系"采用"的客体为"B"，注意："采用"的属性值一定是工业设备实体，排除不符合工业设备实体的客体"}
                            {"应用领域": "该关系描述了该工业设备A能够使用的场景Z或工业设备B，那么A的关系"应用领域"的客体为"Z"或"B""}], "input": "%s"}[/INST]
                """, text);
//
    }

    private String FindClassificationPrompt(String text) {
        return String.format("""
            [INST] <<SYS>>你是一个工业设备知识领域的专家，是乐于助人的助手。
            <</SYS>>{"instruction": "您是命名实体分类识别方面的专家。
            input内容中，第一个方括号内为某一工业设备实体，第二个方括号内为产品分类标签列表。请从input中的产品分类标签列表中找到与第一个
            方括号内工业设备实体相符合的分类标签，如果有多个相符合的分类标签，则以列表形式输出；如果不存在的相符合的分类标签，则不输出。
            请按照JSON字符串的格式回答。",
             "schema": [{"分类标签": 分类标签能较为准确的描述工业设备实体的类别，工业设备实体与所对应的分类标签高度相关}], "input": "%s"}[/INST]
            """,text);
    }

    // 将json字符串output转换成 map
    private Set<String> extractObjectsFromJson(String jsonString, String key) {
        Set<String> objects = new HashSet<>();
        try {
            // 解析 JSON 字符串
            JsonObject jsonObject = new Gson().fromJson(jsonString, JsonObject.class);
            // 获取指定键的 JsonArray
            JsonArray jsonArray = jsonObject.getAsJsonArray(key);
            if (jsonArray != null) {
                for (JsonElement element : jsonArray) {
                    JsonObject obj = element.getAsJsonObject();
                    if (obj.has("object")) {
                        objects.add(obj.get("object").getAsString());
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Failed to parse JSON string: " + jsonString);
        }
        return objects;
    }

//    private Map<String, String> parseExtractedInfo(String extractedInfoJson) {
//        Map<String, String> extractedInfo = new HashMap<>();
//        try {
//            // 检查 extractedInfoJson 是否为有效的 JSON
//            JsonObject jsonObject = new Gson().fromJson(extractedInfoJson, JsonObject.class);
//            extractedInfo.put("公司", jsonObject.get("公司") != null ? jsonObject.get("公司").getAsString() : "");
//            extractedInfo.put("产品", jsonObject.get("产品") != null ? jsonObject.get("产品").getAsString() : "");
//            extractedInfo.put("设备", jsonObject.get("设备") != null ? jsonObject.get("设备").getAsString() : "");
//            extractedInfo.put("组件", jsonObject.get("组件") != null ? jsonObject.get("组件").getAsString() : "");
//        } catch (Exception e) {
//            e.printStackTrace();
//            System.err.println("Failed to parse extracted information: " + extractedInfoJson);
//        }
//        return extractedInfo;
//    }
}
//   return String.format("""
////                [INST] <<SYS>>你是一个工业设备知识领域的专家，是乐于助人的助手。
////                <</SYS>>{"instruction": "您是命名实体属性识别方面的专家。
////                请从input中抽取出符合工业设备的属性和属性值，并以列表形成存储。属性和属性值的格式可以如以下规范：
////                “属性：属性值”，“属性 属性值”
////                请按照JSON字符串的格式回答。",
////                 "schema": , "input": "%s"}[/INST]
////                """, text);