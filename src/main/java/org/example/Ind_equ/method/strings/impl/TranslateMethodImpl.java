package org.example.Ind_equ.method.strings.impl;

import org.example.Ind_equ.method.strings.TranslateMethod;
import org.example.property_method.PropertyLoader;
import org.example.property_method.impl.PropertyLoaderImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 实现翻译方法的类，专注于设备分类的翻译
 */
public class TranslateMethodImpl implements TranslateMethod {
    // 使用属性文件加载器来加载配置文件
    private final PropertyLoader property_loader = new PropertyLoaderImpl("industrial_equipment.properties");
    // 日志记录器
    private static final Logger logger = LoggerFactory.getLogger(TranslateMethodImpl.class);

    // 存储分类翻译映射的Map
    private final Map<String, String> classificationTranslateMap;
    // 需要翻译的英文分类列表
    private Set<String> needTranslateList = new HashSet<>();

    // 从配置文件中获取的分类翻译文件根目录
    String classificationTranslateRoot = property_loader.get_key_value("CLASS_TRANS_FILE_ROOT");

    /**
     * 构造方法，初始化分类翻译映射
     */
    public TranslateMethodImpl() {
        this.classificationTranslateMap = loadClassificationTranslateMap();
    }

    /**
     * 加载分类翻译映射
     * 从CSV文件中读取中英文对照，并存入Map中
     * @return 包含分类翻译的Map
     */
    private Map<String, String> loadClassificationTranslateMap() {
        Map<String, String> map = new HashMap<>();
        String csvFile = classificationTranslateRoot + "translateMap.csv";

        // 验证文件路径是否合法
        Path path = Paths.get(csvFile);
        //如果文件不存在，则创建文件
        if (!Files.exists(path)){
            try {
                Files.createFile(path);
            } catch (IOException e) {
                throw new RuntimeException("Failed to create file: " + csvFile, e);
            }
        }
        if (!Files.isRegularFile(path)) {
            throw new IllegalArgumentException("Invalid file path: " + csvFile);
        }

        try{
            List<String> lines = Files.readAllLines(path);
            for (String line : lines) {
                String[] parts = line.split(",", -1); // 确保保留空字段
                if (parts.length == 2) {
                    String english = parts[0].trim();
                    String chinese = parts[1].trim();
                    map.put(english, chinese);
                } else {
                    // 记录不合规的行
                    logger.error("Invalid line: " + line);
                }
            }
        } catch (IOException e) {
            logger.error("Failed to write to file: " + csvFile, e);
        }

        return map;
    }

    /**
     * 根据英文分类返回其中文翻译
     * 如果没有找到翻译，则将英文分类添加到需要翻译的列表中
     * @param english 英文分类
     * @return 中文翻译，如果没有找到则返回null
     */
    @Override
    public String translateClassification(String english) {
        if (english == null) return null;
        String translateWords = classificationTranslateMap.getOrDefault(english, null);
        if (translateWords == null){
            needTranslateList.add(english);
        }
        return translateWords;
    }

    /**
     * 将需要翻译的英文单词写入CSV文件
     * 这些单词是之前没有翻译映射的单词
     */
    @Override
    public void writeEnglishWord() {
        String csvFile = classificationTranslateRoot + "needTranslate.csv";

        StringBuilder content = new StringBuilder();
        for (String english : needTranslateList) {
            content.append(english.trim()).append(System.lineSeparator());
        }

        try (BufferedWriter bw = Files.newBufferedWriter(Paths.get(csvFile), StandardCharsets.UTF_8, StandardOpenOption.APPEND)) {
            bw.write(content.toString());
        } catch (IOException e) {
            logger.error("Failed to write to file: " + csvFile, e);
        }
    }

    @Override
    public String processCategory(String category) {
        if (category != null) {
            boolean flag = false;
            // 使用正则表达式匹配英文单词
            Pattern pattern = Pattern.compile("\\b[a-zA-Z]+\\b");
            Matcher matcher = pattern.matcher(category);


            // 使用 StringBuffer 来构建最终的结果
            StringBuffer result = new StringBuffer();

            while (matcher.find()) {
                // 获取匹配的英文单词
                String englishWord = matcher.group();
                // 调用翻译函数
                String translatedWord = translateClassification(englishWord);
                // 如果翻译结果不为空，则替换原英文单词
                if (translatedWord != null) {
                    matcher.appendReplacement(result, translatedWord);
                    flag = true;
                } else {
                    matcher.appendReplacement(result, englishWord); // 如果翻译失败，保留原英文单词
                }
            }

            // 完成所有替换后，追加剩余的部分
            matcher.appendTail(result);

            // 输出或使用结果
//            System.out.println(result.toString());
            if (flag){
                return String.valueOf(result);
            }
            else {
                return null;
            }

        } else {
            System.out.println("类别为空");
        }
        return null;
    }

}
