package org.example.Ind_equ.method.ont.impl;

import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.google.gson.Gson;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;

import org.apache.jena.rdf.model.*;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.XSD;
import org.example.Ind_equ.ai.MacBERT.MacBERTMethod;
import org.example.Ind_equ.ai.MacBERT.impl.MacBERTMethodImpl;
import org.example.Ind_equ.ai.openSPG.OpenSPGMethod;
import org.example.Ind_equ.ai.openSPG.impl.OpenSPGMethodImpl;
import org.example.Ind_equ.method.ont.IndEquOnt;
import org.example.Ind_equ.method.strings.StringsMethod;
import org.example.Ind_equ.method.strings.TranslateMethod;
import org.example.Ind_equ.method.strings.impl.TranslateMethodImpl;
import org.example.ont.impl.OntMethodImpl;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import org.example.property_method.PropertyLoader;
import org.example.property_method.impl.PropertyLoaderImpl;
import org.json.JSONArray;
import org.json.JSONObject;
import org.python.antlr.ast.Str;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.example.Ind_equ.ai.baiLian.BaiLianMethod.*;
import static org.example.Ind_equ.method.strings.StringsMethod.normalizeToNfc;


public class IndEquOntImpl extends OntMethodImpl implements IndEquOnt {
    private final PropertyLoader property_loader = new PropertyLoaderImpl("industrial_equipment.properties");
    private final TranslateMethod translate_method = new TranslateMethodImpl();
    private final OpenSPGMethod open_spg_method = new OpenSPGMethodImpl();
    private final MacBERTMethod macbert_method = new MacBERTMethodImpl();
    private static final Logger logger = LoggerFactory.getLogger(IndEquOntImpl.class);
    private final Object processLock = new Object();
    private List<String> classification_list = new ArrayList<>();
    // 初始化常量
    private final String ind_equ_url = property_loader.get_key_value("INDUSTRIAL_EQUIPMENT_URL");
    private final String ont_root = property_loader.get_key_value("ONT_ROOT");
    private final String datatype = property_loader.get_key_value("DATATYPE");
    private final String model_path = property_loader.get_key_value("MODEL_PATH");
    private final int property_list_max_count = Integer.parseInt(property_loader.get_key_value("PROPERTY_LIST_MAX_COUNT"));
    private final int relation_list_max_count = Integer.parseInt(property_loader.get_key_value("RELATION_LIST_MAX_COUNT"));
    private final String model_file = model_path + "初始模型v1.owl";


    /**
    *
    * 初始化本体模型
    *
    **/
    // 创建初始资源
    Resource productClass = create_resource("产品", ind_equ_url,OWL.Class);
    Resource brandClass = create_resource("品牌", ind_equ_url,OWL.Class);
    Resource companyClass = create_resource("公司或专利方",ind_equ_url,OWL.Class);
    Resource personClass = create_resource("专利人",ind_equ_url,OWL.Class);

    // 创建初始属性(通用属性)
    Property brandProperty = create_property("产品的品牌", ind_equ_url);
    Property productCompanyProperty = create_property("产品的公司或专利方", ind_equ_url);
    Property brandCompanyProperty = create_property("品牌的公司或专利方", ind_equ_url);
    Property nameProperty = create_property("名称", ind_equ_url);
    Property productIntroProperty = create_property("产品简介", ind_equ_url);
    Property productImageProperty = create_property("产品图片", ind_equ_url);
    Property productIntroductionProperty = create_property("产品介绍", ind_equ_url);
    Property productLinkProperty = create_property("产品链接", ind_equ_url);


    // 设备间关系
    Property equipProperty = create_property("使用到", ind_equ_url);
    Property appliedToProperty = create_property("应用于", ind_equ_url);
    Property relevanceProperty = create_property("关联设备", ind_equ_url);


    // 提炼属性列表：提炼出的属性列表字符串
    Property extractedListProperty = create_property("提炼出的属性列表", ind_equ_url);
    // 设备间关系（使用、涉及）列表：从产品介绍中提炼出的设备间关系，这里是指单向的使用、涉及等关系
    Property extractedRelationListProperty = create_property("提炼出的使用关系的设备列表", ind_equ_url);
    // 产品名列表：实例产品列表集合，用于提出产品上位词获取更精确的本体
    Property productNameListProperty = create_property("产品名列表", ind_equ_url);


    public IndEquOntImpl() {
        load_init_model();
        init_method();
    }

    public IndEquOntImpl(Model model) {
        super(model);
        init_method();
    }

    public IndEquOntImpl(String ont_filename, String base_url) {
        add_ont(ont_filename, base_url);
    }

    @Override
    public boolean init_method() {
        System.out.println("Industrial equipment ontology initializing.");
        classification_list = find_all_classification();

        /*
          初始化属性(通用属性)
         */
        //产品品牌
        brandProperty.addProperty(RDF.type, OWL.ObjectProperty);
        brandProperty.addProperty(RDFS.domain, productClass);
        brandProperty.addProperty(RDFS.range, brandClass);
        //产品公司/专利方
        productCompanyProperty.addProperty(RDF.type, OWL.ObjectProperty);
        productCompanyProperty.addProperty(RDFS.domain, productClass);
        productCompanyProperty.addProperty(RDFS.range, companyClass);
        //品牌公司/专利方
        brandCompanyProperty.addProperty(RDF.type, OWL.ObjectProperty);
        brandCompanyProperty.addProperty(RDFS.domain, brandClass);
        brandCompanyProperty.addProperty(RDFS.range, companyClass);
        //产品名称
        nameProperty.addProperty(RDF.type, OWL.DatatypeProperty);
        nameProperty.addProperty(RDFS.domain, productClass);
        //产品简介
        productIntroProperty.addProperty(RDF.type, OWL.DatatypeProperty);
        //品牌名称
        productImageProperty.addProperty(RDF.type, OWL.DatatypeProperty);
        //产品介绍
        productIntroductionProperty.addProperty(RDF.type, OWL.DatatypeProperty);
        //产品链接
        productLinkProperty.addProperty(RDF.type, OWL.DatatypeProperty);



        //设备间关系
        equipProperty.addProperty(RDF.type, OWL.ObjectProperty);
        equipProperty.addProperty(RDFS.domain, productClass);
        equipProperty.addProperty(RDFS.range, productClass);
        appliedToProperty.addProperty(RDF.type, OWL.ObjectProperty);
        appliedToProperty.addProperty(RDFS.domain, productClass);
        appliedToProperty.addProperty(RDFS.range, productClass);
        return false;
    }

    public boolean load_init_model(){
         /*
          获取初始化模型
         */
        // 判断是否有model_file文件，如果有，则添加模型
        if (Files.exists(Paths.get(ont_root + model_file))) {
            System.out.println("Loading init model exists.");
            if (!read_ont(model_file, "")){
                System.out.println("Init model loading failed.");
                return false;
            }
            return true;
        } else {
            System.out.println("Init model does not exist.");
            return false;
        }
    }

    @Override
    public boolean read_ont(String ont_filename, String base_url) {
        ont_filename = ont_root + ont_filename;
        return super.read_ont(ont_filename, base_url);
    }

    @Override
    public boolean add_ont(String ont_filename, String base_url) {
        ont_filename = ont_root + ont_filename;
        return super.add_ont(ont_filename, base_url);
    }


    @Override
    public boolean add_ont_from_data_dir(String data_dir) {
        // 验证和清理路径
        if (data_dir == null || data_dir.isEmpty() || !data_dir.matches("^[a-zA-Z0-9/._-]+$")) {
            logger.warn("Invalid or empty directory path: " + data_dir);
            return false;
        }

        File file = null;		// 获取其file对象并规范化路径
        try {
            file = new File(data_dir).getCanonicalFile();
            File[] fs = file.listFiles();							// 遍历path下的文件和目录，放在File数组中
            if (fs != null) {										// 检查是否为null
                for (File f : fs) {								// 遍历File[]数组
                    boolean isDirectory = f.isDirectory();		// 缓存目录检查结果
                    if (!isDirectory) {							// 若非目录(即文件)，则打印
                        add_ont_from_datafile(f.getAbsolutePath());
//                        System.out.println(f);

                    }
                }
            } else {
                logger.warn("Directory does not exist or cannot be accessed: " + data_dir + ". Directory: " + file);
                return false;
            }
        } catch (IOException e) {
            logger.error("Error getting canonical file for directory: " + data_dir + ". Directory: " + file, e);
            throw new RuntimeException(e);
        } catch (SecurityException e) {
            logger.error("Security error accessing directory: " + data_dir + ". Directory: " + file, e);
            return false;
        } catch (NullPointerException e) {
            logger.error("Null pointer error accessing directory: " + data_dir + ". Directory: " + file, e);
            return false;
        } catch (Exception e) {
            logger.error("Error listing files in directory: " + data_dir + ". Directory: " + file, e);	// 记录详细日志
            return false;
        }
        // 记录未翻译的分类
        translate_method.writeEnglishWord();
        return true; // 返回一个布尔值表示操作成功
    }

    @Override
    public boolean add_ont_from_datafile(String datafile_name) {
        if (datafile_name == null || !datafile_name.contains(".")) {
            logger.error("Invalid file name");
            return false;
        }

        // 获取输出文件的类型
        String type = datafile_name.substring(datafile_name.lastIndexOf(".") + 1);

        // 根据文件扩展名自动推断输出格式
        try {
            switch (type) {
                case "csv":
                    add_ont_from_csv(datafile_name,datatype);
                    return true;
                default:
                    logger.warn("Unsupported file type");
                    return false;
            }
        } catch (Exception e) {
            logger.error("Error processing file: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean add_ont_from_csv(String filename,String datatype) {
        if (datatype.equals("production")){
            add_ont_from_production_csv(filename);
            return true;
        }else{
            return false;
        }
    }

    @Override
    public boolean add_ont_from_production_csv(String filename) {
        int count = 0;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(Files.newInputStream(
                Paths.get(filename)), StandardCharsets.UTF_8))) {
            // 读取第一行作为列名
            String firstLine = reader.readLine();
            if (firstLine == null) throw new IllegalArgumentException("CSV文件为空或格式不正确");

            // 去除 BOM
            if (firstLine.startsWith("\uFEFF")) firstLine = firstLine.substring(1);

            String[] headers = firstLine.split(",");
            if (headers == null) throw new IllegalArgumentException("CSV文件为空或格式不正确");

            // 读取剩余行
            CSVReader csvReader = new CSVReader(reader);
            String[] nextLine;
            while ((nextLine = csvReader.readNext()) != null) {
                count++;
                System.out.println("处理第" + count + "行数据");
                //处理每行资源
                Resource classificationResource = null;
                Map<String, String> record = new HashMap<>();//
                //先将数据储存到Map中，然后处理，可以实现元素间的关系添加
                for (int i = 0; i < headers.length; i++) {
                    String trimmedHeader = headers[i].trim();
                    String text = nextLine[i];
                    text = normalizeToNfc(text);
                    record.put(trimmedHeader, text);
//                    System.out.println(trimmedHeader + ":  " + nextLine[i]);
                }

                /*
                 * 产品分类处理
                 */
                // 字符串预处理
                String classification_value = record.get("产品分类");
                if (classification_value == null){
                    System.out.println("产品分类标签不存在！！！");
                    return false;
                }
                String classification = normalizeToNfc(classification_value.trim());
                classificationResource = processClassification(classification);
                if (classificationResource == null){
                    System.out.println("产品分类为空");
                    continue;
                }
                for (String key : record.keySet()) {
                    switch (key) {
                        case "产品名称":
                            String productNameValue = normalizeToNfc(record.get(key).trim());
                            if (productNameValue == null||productNameValue.trim().isEmpty()){
                                System.out.println("产品名称为空");
                                continue;
                            }
                            if (classificationResource == null){
                                System.out.println("产品分类为空");
                                continue;
                            }
                            add_property(classificationResource, productNameListProperty, productNameValue);
                            System.out.println("产品名称："+ productNameValue);
                        break;
                        case "产品品牌":
                        case "品牌":
                        case "产品分类":
                        case "分类":
                        case "产品公司":
                        case "公司":
                        case "产品简介":
                        case "简介":
                        case "产品图片":
                        case "产品链接":
                            break;
                        case "产品介绍":
                        case "介绍":
                            if (classificationResource == null){
                                System.out.println("产品分类为空");
                                continue;
                            }
                            int property_count = get_number_of_property_value(classificationResource, extractedListProperty);
                            int relation_count = get_number_of_property_value(classificationResource, extractedRelationListProperty);
                            if (property_count >= property_list_max_count && relation_count >= property_list_max_count){
                                System.out.println("属性组数量超过" + property_list_max_count + "" + "，关系组数量超过" + property_list_max_count + "，跳过");
                                System.out.println("产品类别："+ classificationResource.getProperty(nameProperty));
                                continue;
                            }
                            String productIntroductionValue = record.get(key);
                            if (productIntroductionValue == null || productIntroductionValue.trim().isEmpty()){
                                System.out.println("产品介绍为空");
                                break;
                            }
                            Map<String,Map<String,List<String>>> extractedPropertyAndRelation = get_generate_property_and_relation(productIntroductionValue);
                            Map<String,List<String>> extractedProperty = extractedPropertyAndRelation.get("属性");
                            Map<String,List<String>> extractedRelation = extractedPropertyAndRelation.get("关系");
                            if (extractedProperty != null){
                                // 获取所有属性键，并形成列表
                                String classificationExtractProperty = new JSONArray(extractedProperty.keySet()).toString();
//                                String classificationExtractProperty = String.join("\",\"", propertyKeys);
//                                classificationExtractProperty = "[\"" + classificationExtractProperty + "\"]";
                                System.out.println("产品介绍提取属性："+ classificationExtractProperty);
                                add_property(classificationResource, extractedListProperty, classificationExtractProperty);
                            }
                            if (extractedRelation != null){
                                JSONArray classificationExtractRelationJson = new JSONArray();
                                for (String relationKey : extractedRelation.keySet()){
                                    classificationExtractRelationJson.putAll(extractedRelation.get(relationKey));
                                }
                                String classificationExtractRelation = classificationExtractRelationJson.toString();
//                                String classificationExtractRelation = new JSONArray(extractedRelation.keySet()).toString();
                                System.out.println("产品介绍提取关系："+ classificationExtractRelation);
                                add_property(classificationResource, extractedRelationListProperty, classificationExtractRelation);
                            }
//                            if (property_count < property_list_max_count){
//                                String classificationExtractProperty = get_extract_property(productIntroductionValue);
//                                add_property(classificationResource, extractedListProperty, classificationExtractProperty);
//                                System.out.println("产品介绍提取属性："+ classificationExtractProperty);
//                            }
//                            if (relation_count < property_list_max_count){
//                                String extractRelationProperty = get_extract_relation(productIntroductionValue);
//                                add_property(classificationResource, extractedRelationListProperty, extractRelationProperty);
//                                System.out.println("产品介绍提取关系："+ extractRelationProperty);
//                            }
//                            System.out.println("产品介绍："+ productIntroductionValue);
                            break;
                        default:
                            System.out.println("Unsupported column name: " + key);
//                            return false;
                    }
                }

            }

            // 如果成功读取并存储数据，返回true
            return true;
        } catch (IOException | CsvValidationException e) {
            e.printStackTrace();
            return false;
        } catch (NoApiKeyException | InputRequiredException e) {
            throw new RuntimeException(e);
        }
//        catch (NoApiKeyException | InputRequiredException e) {
//            throw new RuntimeException(e);
//        }
    }

    /**
     *
     *
     *
     **/
    @Override
    public Resource processClassification(String classification) {
        classification = normalizeToNfc(classification.trim());
        // 对于classification字符串里用空格分隔的分类，获取最后一项
        String[] parts = classification.split(" ");
        // 获取最后分类标签
        String classificationName = parts[parts.length - 1];
        if (classificationName.equals("产品分类")){
            return null;
        }
//        Resource classificationResource = null;

        if (classification.length() != 0) {
            // 使用String.join重新拼接去掉最后一项的字符串，并去掉空格，得到父类
            String subClassification = String.join(" ", Arrays.copyOfRange(parts, 0, parts.length - 1)).trim();
            String classificationUrl = StringsMethod.weekCleanForOWLIri(classification);
            Resource classificationResource = get_resource(classificationUrl, ind_equ_url);
            if (classificationResource != null){
                return classificationResource;
            }
            Resource subClassificationResource;
            if (subClassification.isEmpty()){
                subClassificationResource = productClass;
            }else {
                subClassificationResource = processClassification(subClassification);
            }
            classificationResource = create_resource(classificationUrl, ind_equ_url, OWL.Class);
            // 翻译分类名称
            String tempWords = translate_method.processCategory(classificationName);
            if (tempWords!=null){
                classificationName = tempWords;
            }
            // 添加名称属性
            classificationResource.addProperty(nameProperty, classificationName);
            // 添加父类
            add_subclass_relation(classificationResource, subClassificationResource);
            classification_list = find_all_classification();
            return classificationResource;
        }
        return null;
    }

    @Override
    public int get_number_of_property_value(Resource resource, Property property){
        int count = 0;
        StmtIterator stmtIter = resource.listProperties(property);
        while (stmtIter.hasNext()) {
            stmtIter.nextStatement();
            count++;
        }
        return count;
    }

    @Override
    public String get_value_of_property(Resource resource, Property property) {
        List<String> valuesList = new ArrayList<>();

        // 获取所有与给定属性相关的陈述
        StmtIterator stmtIterator = resource.listProperties(property);

        while (stmtIterator.hasNext()) {
            Statement stmt = stmtIterator.nextStatement();
            // 获取陈述的对象并转换为字符串
            if (stmt.getObject().isLiteral()) {
                valuesList.add(stmt.getLiteral().getString());
            } else if (stmt.getObject().isURIResource()) {
                valuesList.add(stmt.getResource().getURI());
            } else {
                // 如果对象既不是字面量也不是资源 URI，则跳过或根据需求处理
                valuesList.add(stmt.getObject().toString());
            }
        }

        return valuesList.toString();
    }

    @Override
    public String process_python(String python_file, String input){
        // 打印传入的字符串以确保其格式正确
//        System.out.println("Input to Python script: " + input);
        // 将"替换成\"
        input = input.replace("\"", "\\\"");
        ProcessBuilder processBuilder = new ProcessBuilder("venv/Scripts/python.exe", python_file, input);
        processBuilder.environment().put("PYTHONIOENCODING", "utf-8");
        StringBuilder output = new StringBuilder();
        processBuilder.redirectErrorStream(true);
        try {
            Process process = processBuilder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8));

            String line;
            while ((line = reader.readLine()) != null) {
//                output.append(line);
                output.append(line).append(System.lineSeparator());
            }

            int exitCode = process.waitFor();
//            System.out.println("Output from Python script: " + output.toString());
//            System.out.println("Exit Code: " + exitCode);
            if (exitCode != 0){
                throw new RuntimeException("Python script execution failed with exit code: " + exitCode);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return output.toString();
    }

    @Override
    public boolean generated_attribute(Resource resource){
        String extract_property = get_value_of_property(resource,extractedListProperty);
//        System.out.println(resource + " " + extract_property);
        if (extract_property == null || extract_property.isEmpty() || extract_property.equals("[]")){
            System.out.println("本体: " + resource.getLocalName() + " 无待提取属性");
            return false;
        }
        System.out.println("本体: " + resource.getLocalName() + " 待提取属性："+ extract_property);
        String pythonScriptPath = "pythonCode/python/indEqu/AI_method/extract_ont_property.py"; // 替换为你的Python脚本路径
        String output = process_python(pythonScriptPath, extract_property);
//        String output = "[]";
        System.out.println("Python输出：" + output);
        // 将单引号替换为双引号，以符合 JSON 标准
        output = output.replace("'", "\"");
        try {
            // 创建 JSONArray 对象
            JSONArray jsonArray = new JSONArray(output);
            for (int i = 0; i < jsonArray.length(); i++) {
                String extractedPropertyName = jsonArray.getString(i);
                // 添加提取属性
                Property extractedProperty = get_property_with_range(resource, extractedPropertyName, ind_equ_url);
//                System.out.println("本体" + resource.getNameSpace() + "添加提取属性：" + extractedProperty);
                if (extractedProperty == null){
                    // TOFIX
                    extractedProperty = create_property(extractedPropertyName, ind_equ_url);
                    extractedProperty.addProperty(RDF.type, OWL.DatatypeProperty);
                    extractedProperty.addProperty(RDFS.domain, resource);
                    extractedProperty.addProperty(RDFS.range, XSD.xstring);
                    System.out.println("本体 " + resource.getLocalName() + " 添加提取属性：" + extractedPropertyName);
                }
                else {
                    System.out.println("本体 " + resource.getLocalName() + " 提取属性已存在：" + extractedPropertyName);
                }
            }
            // 打印 JSONArray 对象
        } catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    }

    @Override
    public boolean generated_relation(Resource resource){
        String extract_relation = get_value_of_property(resource,extractedRelationListProperty);
//        System.out.println(resource + " " + extract_property);
        if (extract_relation == null || extract_relation.isEmpty() || extract_relation.equals("[]")){
            System.out.println("本体: " + resource.getLocalName() + " 无待提取关系");
            return false;
        }
        System.out.println("本体: " + resource.getLocalName() + " 待提取属性："+ extract_relation);
        List<String> classificationList = find_all_classification();
        Gson gson = new Gson();
        // 将列表转换为 JSON 字符串
        String extract_property = gson.toJson(classificationList);
        // 创建 JSON 对象
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("关系标签组", new JSONArray(extract_relation));
        jsonObject.put("分类标签列表", new JSONArray(extract_property));
        System.out.println(jsonObject.toString());
        try {
            String output = get_generate_relation(jsonObject.toString());
            System.out.println("result: " + output);
            // 创建 JSONArray 对象
            JSONArray jsonArray = null;
            if (output != null) {
                jsonArray = new JSONArray(output);
            }
            if (jsonArray != null) {
                List<Resource> resources = new ArrayList<>();
                for (int i = 0; i < jsonArray.length(); i++) {
                    String extractRelation = jsonArray.getString(i);
                    // 获取nameProperty属性值为extractRelation的资源

                    StmtIterator stmtIter = model.listStatements(null, nameProperty, extractRelation);
                    while (stmtIter.hasNext()) {
                        Statement stmt = stmtIter.nextStatement();
                        resources.add(stmt.getSubject());
                    }
                }
                for (Resource tempResource : resources){
                    add_property(resource, relevanceProperty, tempResource);
                    System.out.println("本体 " + resource.getLocalName() + " 添加关联关系：" + tempResource.getLocalName());
                }
            }
            // 打印 JSONArray 对象
        } catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    }

    @Override
    public boolean extracted_all_property(){
//        System.out.println(get_value_of_property(get_resource("PLC", ind_equ_url), extractedListProperty));
        List<Resource> allSubclasses = findAllSubclasses(model, productClass);
        for (Resource subclass : allSubclasses){
//            System.out.println("本体：" + subclass);
            generated_attribute(subclass);
        }
        return false;
    }

    @Override
    public boolean generate_all_relation(){
        List<Resource> allSubclasses = findAllSubclasses(model, productClass);
        for (Resource subclass : allSubclasses){
//            System.out.println("本体：" + subclass);
            generated_relation(subclass);
        }
        return false;
    }

    @Override
    public Resource find_classification(String classification) {
        // 获取类和子类的所有实例
        List<Resource> allSubclasses = findAllSubclasses(model, productClass);

        // 遍历所有类及其子类
        for (Resource subclass : allSubclasses) {
                // 获取实例属性的值
                StmtIterator propertyStatements = model.listStatements(subclass, nameProperty, (RDFNode) null);

                while (propertyStatements.hasNext()) {
                    Statement statement = propertyStatements.next();
                    String propertyValue = statement.getObject().asLiteral().getString();
//                    System.out.println(propertyValue);

                    // 检查属性值是否匹配
                    if (propertyValue.equals(classification)) {
                        return subclass;
                    }
                }
            }
        return null;
    }

    @Override
    public boolean add_subclass_relation(Resource resourceA, Resource resourceB) {
        return add_property(resourceA, RDFS.subClassOf, resourceB);
    }

    @Override
    public String get_resource_name(Resource resource) {
        if (resource == null){
            return null;
        }
        String name = "";
        StmtIterator statements = model.listStatements(resource, nameProperty, (RDFNode) null);
        if (statements.hasNext()){
            name = statements.next().getObject().asLiteral().getString();
        }
        return name;
    }

    @Override
    public List<String> find_all_classification() {
        List<Resource> allSubclasses = findAllSubclasses(model, productClass);
        List<String> resourceNames = allSubclasses.stream()
                .map(this::get_resource_name)
                .collect(Collectors.toList());
        return resourceNames;
    }


//    @Override
//    public String get_extract_relation(String productIntroductionValue){
////        List<String> allSubclassesStrings = find_all_classification();
////        // 检查列表是否为空或只有一个元素
////        if (allSubclassesStrings.size()!= 0 && allSubclassesStrings.size()!= 1) {
////            allSubclassesStrings.remove(0);
////            // 将 allSubclassesStrings 拼成一个字符串，且每个字符串间用 ", " 分隔
////            String classificationString = String.join(", ", allSubclassesStrings);
////            String productName = product.getProperty(nameProperty).getObject().toString();
////            String productInformation = product.getProperty(productIntroductionProperty).getObject().toString();
//            String inputText = "'" + productName + "'的产品介绍如下: \n" + productInformation;
//
//            String relationString = open_spg_method.ExtractDeviceRelation(inputText);
//            // 打印或使用 concatenatedString
////            System.out.println(concatenatedString);
////            System.out.println(inputText);
////            System.out.println(relationString);
//
//            List<String> equipList;
//            List<String> usedList;
//            //提取设备关系
//            {
//                if (relationString.length() <= 2) {
//                    return false;
//                }
//                // 去掉外层的 [ 和 ]
//                relationString = relationString.substring(1, relationString.length() - 1);
//                // 按照 ], [ 分割外层集合
//                String[] outerSets = relationString.split("\\], \\[");
//                if (outerSets.length != 2) {
//                    return false;
//                }
//                // 去掉内层的 [ 和 ]
//                String outerSet1 = outerSets[0];
//                outerSet1 = outerSet1.replace("[", "").replace("]", "");
//                // 按照 , 分割内层集合
//                String[] innerElements1 = outerSet1.split(", ");
//                equipList = new ArrayList<>(Arrays.asList(innerElements1));
//                String outerSet2 = outerSets[1];
//                outerSet2 = outerSet2.replace("[", "").replace("]", "");
//                // 按照 , 分割内层集合
//                String[] innerElements2 = outerSet2.split(", ");
//                usedList = new ArrayList<>(Arrays.asList(innerElements2));
//                System.out.println(equipList + "\n" + usedList);
//            }
//
//            for (String equip : equipList) {
//                List<String> equipClassificationList = macbert_method.runMacBERT(equip, classificationString);
//                String category = "";
//                if (equipClassificationList.size() > 0){
//                    category = equipClassificationList.get(0);
//                    Resource categoryResource = find_classification(category);
////                    System.out.println(categoryResource);
//                    if (categoryResource != null) {
//                        add_equip_relation(product, categoryResource);
////                        System.out.println(product.getProperty(equipProperty));
//                    }
//                }
//                System.out.println(equip + "关联分类标签：" + category);
//            }
//
//            for (String used : usedList) {
//                List<String> usedClassificationList = macbert_method.runMacBERT(used, classificationString);
//                String category = "";
//                if (usedClassificationList.size() > 0){
//                    category = usedClassificationList.get(0);
//                    Resource categoryResource = find_classification(category);
//                    System.out.println(categoryResource);
//                    if (categoryResource != null) {
//                        add_applied_relation(product, categoryResource);
//                        System.out.println(product.getProperty(appliedToProperty));
//                    }
//                }
//                System.out.println(used + "关联分类标签：" + category);
//            }
//
//        }
//        return false;
//    }


    public static void main(String[] args) {
        IndEquOntImpl indEquOnt = new IndEquOntImpl("rdf/testForRelation1.owl","");
//        indEquOnt.extracted_all_property();
//        indEquOnt.write_ont("rdf/testForProperty3.jsonld");
        Resource classification = indEquOnt.get_resource("PLC","http://industrial/equipment");
//        Resource classification2 = indEquOnt.get_resource("网关_PLC_小型PLC_通讯模块","http://industrial/equipment");
//        indEquOnt.generated_attribute(classification);
//        System.out.println(indEquOnt.generated_relation(classification));
//        System.out.println(indEquOnt.get_value_of_property(classification,indEquOnt.extractedRelationListProperty));
//        System.out.println(indEquOnt.find_all_classification());
//        System.out.println(indEquOnt.get_value_of_property(classification2,indEquOnt.extractedListProperty));
//        indEquOnt.generated_attribute(classification);
//        System.out.println(indEquOnt.get_property_with_range(indEquOnt.productClass,"产品简介",indEquOnt.ind_equ_url));
//        System.out.println(classification);
//        String classificationString = indEquOnt.get_value_of_property(classification,indEquOnt.extractedListProperty);
////        System.out.println(classificationString);
////        String classificationString = "[[\"数字量输入\",\"继电器\",\"模拟量输入\",\"电流\",\"数字量输出\",\"应用领域\",\"电压\",\"温度传感器\",\"模拟量输出\",\"晶体管\"]]";
//        String classificationString = "[[\"数字量输入\",\"继电器\",\"模拟量输入\",\"电流\",\"数字量输出\",\"应用领域\",\"电压\",\"温度传感器\",\"模拟量输出\",\"晶体管\"], [\"输出类型\",\"输入类型\",\"应用场景\",\"工作温度范围\",\"通讯功能\",\"功能特性\"], [\"PLC控制\",\"应用领域\",\"工业互联网\",\"存储容量\",\"短信报警\",\"协议转换\",\"网络连接方式\",\"人机界面\",\"边缘计算\",\"远程监控\",\"接口类型\",\"超级网关\",\"远程编程\"], [\"数字量\",\"设计\",\"处理速度\",\"应用领域\",\"通信能力\",\"分布式控制\",\"性能\",\"处理器\",\"模拟量\",\"I/O能力\",\"扩展能力\",\"类型\"], [\"转换时间\",\"输出热电流\",\"输入特性\",\"IB...IG 用作模拟量输入电路\",\"控制和编程\",\"净重\",\"无负荷\",\"物理特性\",\"操作频率\",\"输入兼容\",\"离散量输入数量\",\"反极保护\",\"不带扩展装置\",\"电压状态0担保\",\"额定电源电压\",\"控制线路数量\",\"品牌\",\"离散量输入电压\",\"模拟量输入数量\",\"模拟量输入分辨率\",\"LSB 值\",\"AC\",\"离散量输入类型\",\"继电器输出\",\"在 Ie\",\"基本信息\",\"重复精度\",\"模拟量输入类型\",\"触点类型和构成\",\"测温探头类型\",\"输出电压限制\",\"对 2 个输出\",\"型号\",\"程序内存检查\",\"开关能力以 mA\",\"电源电压范围\",\"计数频率\",\"I1...IA和 IH...IR 离散量输入电路\",\"供电电流\",\"输出特性\",\"电压状态1担保\",\"功耗\",\"模拟量输入范围\",\"带有扩展器\",\"模拟量输入特性\",\"转换错误\",\"相关型号\",\"对 8 个输出\",\"电源参数\",\"周期时间\",\"当前状态保证0\",\"操作距离\",\"输入阻抗\",\"输出通道数量\",\"离散量输入电流\",\"当前状态保证1\",\"最大允许电压\",\"系列\",\"类型\",\"DC\"], [\"数据采集模块支持\",\"通讯协议支持\",\"适用场景\",\"频率\",\"应用领域\",\"快速开发\",\"图形组态功能\",\"显示效果\",\"PLC支持\",\"刷新率\"], [\"超时登录注销\",\"远程调试\",\"产品类型\",\"数字量输出\",\"内置模块\",\"应用场景\",\"模拟量输出\",\"响应速度\",\"数字量输入\",\"模拟量输入\",\"身份验证\",\"系列\",\"连接方式\",\"制造商\",\"通信加密\",\"访问控制\",\"实时监控\",\"数据展示\",\"界面类型\"], [\"范围\",\"语言\",\"软件功能\",\"应用领域\",\"电源设计\",\"自诊断功能\",\"结构设计\",\"可扩展性\",\"通讯接口\",\"外壳材质\",\"抗干扰能力\",\"通讯协议\",\"电路设计\",\"标准\",\"环境适应性\",\"抗冲击振动能力\",\"防护等级\",\"自主知识产权\",\"可靠性\",\"编程支持\",\"故障处理能力\"], [\"软件功能\",\"应用领域\",\"扩展性\",\"知识产权\",\"机械性能\",\"电路设计\",\"环境适应性\",\"防护等级\",\"器件和种类\",\"编程支持\",\"电源特性\",\"外壳设计\",\"外壳材料\",\"诊断和维护\"], [\"品牌\",\"输入通道数\",\"电流输入范围\",\"转换速度\",\"其他功能\",\"绝缘方式\",\"电压输入范围\",\"型号\",\"供电电压\",\"通道之间\",\"输入端子～内部电路之间\",\"供电电流\",\"电流输入阻抗\",\"输出通道数\",\"电压输入阻抗\",\"最大分辨率\"], [\"处理器类型\",\"应用领域\",\"通讯接口\",\"外壳材质\",\"认证\",\"隔离能力\",\"自诊断与故障处理\",\"处理速度\",\"I/O模块\",\"冗余设计\",\"防护等级\",\"处理能力\",\"自主知识产权\",\"可靠性\"]]";
////
//        String python_file = "pythonCode/python/indEqu/AI_method/extract_ont_property.py";
//        System.out.println(indEquOnt.process_python(python_file,classificationString));
        Set<String> classification_set = new HashSet<>();
        List<String> classification_list = indEquOnt.find_all_classification();
        for (String classification_name : classification_list){
            classification_set.add(classification_name);
        }
        System.out.println(classification_set);
    }
}
