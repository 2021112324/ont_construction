package org.example.Ind_equ.method.ont.impl;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;

import org.apache.jena.rdf.model.*;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.example.Ind_equ.ai.MacBERT.MacBERTMethod;
import org.example.Ind_equ.ai.MacBERT.impl.MacBERTMethodImpl;
import org.example.Ind_equ.ai.openSPG.OpenSPGMethod;
import org.example.Ind_equ.ai.openSPG.impl.OpenSPGMethodImpl;
import org.example.Ind_equ.method.ont.IndEquOnt;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.example.Ind_equ.method.strings.StringsMethod.generate_resource_url_hashcode;
import static org.example.Ind_equ.method.strings.StringsMethod.normalizeToNfc;


public class IndEquOntImpl extends OntMethodImpl implements IndEquOnt {
    private final PropertyLoader property_loader = new PropertyLoaderImpl("industrial_equipment.properties");
    private final TranslateMethod translate_method = new TranslateMethodImpl();
    private final OpenSPGMethod open_spg_method = new OpenSPGMethodImpl();
    private final MacBERTMethod macbert_method = new MacBERTMethodImpl();
    private static final Logger logger = LoggerFactory.getLogger(IndEquOntImpl.class);
    // 初始化常量
    private final String ind_equ_url = property_loader.get_key_value("INDUSTRIAL_EQUIPMENT_URL");
    private final String ont_root = property_loader.get_key_value("ONT_ROOT");
    private final String datatype = property_loader.get_key_value("DATATYPE");


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
//    Property brandNameProperty = create_property("品牌名称", ind_equ_url);
//    Property companyNameProperty = create_property("公司或专利方名称", ind_equ_url);

    // 设备间关系
    Property equipProperty = create_property("使用到", ind_equ_url);
    Property appliedToProperty = create_property("应用于", ind_equ_url);



    public IndEquOntImpl() {
        init_method();
    }

    public IndEquOntImpl(Model model) {
        super(model);
        init_method();
    }

    @Override
    public boolean init_method() {
        System.out.println("Industrial equipment ontology initialized.");
//        System.out.println(ind_equ_url);

        /**
         *
         * 初始化属性(通用属性)
         *
         **/
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

    @Override
    public boolean read_ont(String ont_filename, String base_url) {
        return false;
    }

    @Override
    public boolean add_ont(String ont_filename, String base_url) {
        ont_filename = ont_root + ont_filename;
        return super.add_ont(ont_filename, base_url);
    }

//    @Override
//    public void write_ont(String ont_filename) {
//
//    }

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
    public boolean add_ont_from_production_csv(String filename) {
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
//                System.out.println("0");
                //处理每行资源
                Resource product_resource = null;
                Map<String, String> record = new HashMap<>();//
                //先将数据储存到Map中，然后处理，可以实现元素间的关系添加
                for (int i = 0; i < headers.length; i++) {
                    String trimmedHeader = headers[i].trim();
                    String text = nextLine[i];
                    text = normalizeToNfc(text);
                    record.put(trimmedHeader, text);
//                    System.out.println(trimmedHeader + ":  " + nextLine[i]);
                }

                /**
                 *
                 * 产品数据处理
                 *
                 **/
                // 产品名称必须首先处理
                System.out.println("\n");
                String productNameValue = record.get("产品名称");
                if (productNameValue == null||productNameValue.trim().isEmpty()){
                    System.out.println("产品名称为空");
                    continue;
                }
                product_resource = find_product(productNameValue);
                //如果已有该产品
                if (product_resource!=null){
                    System.out.println("已有该产品");
                }
                else {
                    String product_posturl = generate_resource_url_hashcode(productNameValue);
                    product_resource = create_resource(product_posturl, ind_equ_url);
                    add_property(product_resource, nameProperty, productNameValue);
                }
                System.out.println("产品名称："+ productNameValue);
                for (String key : record.keySet()) {
                    switch (key) {
                        case "产品分类":
                        case "分类":
//                            System.out.println("3");
                            String productCategoryValue = record.get(key);
                            if (productCategoryValue == null || productCategoryValue.trim().isEmpty()){
                                System.out.println("产品分类为空");
                                productCategoryValue = "未分类";
                            }
                            Resource product_classification = create_classification_resource(product_resource,productCategoryValue);
                            product_resource.addProperty(RDF.type, product_classification);
                            System.out.println("产品分类: " + productCategoryValue);
                            break;
                        case "产品品牌":
                        case "品牌":
//                            System.out.println("4");
                            String brandValue = record.get(key);
                            if (brandValue == null || brandValue.trim().isEmpty()){
                                System.out.println("产品品牌为空");
                                break;
                            }
                            //查询是否有该品牌
                            Resource brand_resource = find_brand(brandValue);
                            if (brand_resource == null){
                                brand_resource = create_resource(brandValue, ind_equ_url);
                                add_property(brand_resource, nameProperty, brandValue);
                                add_property(brand_resource, RDF.type, brandClass);
                            }
                            add_property(product_resource, brandProperty, brand_resource);
                            break;
                        case "产品公司":
//                        case "公司":
                            String productCompanyValue = record.get(key);
                            if (productCompanyValue == null || productCompanyValue.trim().isEmpty()){
                                System.out.println("产品公司为空");
                                break;
                            }
                            Resource productCompany_resource = find_company(productCompanyValue);
                            if (productCompany_resource == null){
                                productCompany_resource = create_resource(productCompanyValue, ind_equ_url);
                                add_property(productCompany_resource, nameProperty, productCompanyValue);
                                add_property(productCompany_resource, RDF.type, companyClass);
                            }
                            add_property(product_resource, productCompanyProperty, productCompany_resource);
                        case "产品简介":
//                        case "简介":
//                            System.out.println("2");
                            String productIntroValue = record.get(key);
                            if (productIntroValue == null||productIntroValue.trim().isEmpty()){
                                System.out.println("产品简介为空");
                                productIntroValue = "";
                            }
                            add_property(product_resource, productIntroProperty, productIntroValue);
                            System.out.println("产品简介："+ productIntroValue);
                            break;
                        case "产品图片":
//                            System.out.println("5");
                            String productImageValue = record.get(key);
                            if (productImageValue == null || productImageValue.trim().isEmpty()){
                                System.out.println("产品图片为空");
                                productImageValue = "";
                            }
                            add_property(product_resource, productImageProperty, productImageValue);
                            System.out.println("产品图片："+ productImageValue);
                            break;
                        case "产品介绍":
//                        case "介绍":
//                            System.out.println("6");
                            String productIntroductionValue = record.get(key);
                            if (productIntroductionValue == null || productIntroductionValue.trim().isEmpty()){
                                System.out.println("产品介绍为空");
                                productIntroductionValue = "";
                            }
                            add_property(product_resource, productIntroductionProperty, productIntroductionValue);
                            System.out.println("产品介绍："+ productIntroductionValue);
                            break;
                        case "产品链接":
                            String productLinkValue = record.get(key);
                            if (productLinkValue == null || productLinkValue.trim().isEmpty()){
                                System.out.println("产品链接为空");
                                productLinkValue = "";
                            }
                            add_property(product_resource, productLinkProperty, productLinkValue);
                            System.out.println("产品链接："+ productLinkValue);
                            break;
                        default:
                            System.out.println("Unsupported column name: " + key);
//                            return false;
                    }
                }

                /**
                 *
                 * 补充资源间关系
                 *
                 **/
                //添加品牌的公司
                String productCompanyValue = record.get("产品公司");
                String productBrandValue = record.get("产品品牌");
                if (productCompanyValue == null || productCompanyValue.trim().isEmpty()){
                    System.out.println("产品公司为空");
                }
                else if (productBrandValue == null || productBrandValue.trim().isEmpty()){
                    System.out.println("产品品牌为空");
                }
                else {
                    Resource productCompany_resource = find_company(productCompanyValue);
                    Resource productBrand_resource = find_brand(productBrandValue);
                    if (productCompany_resource != null && productBrand_resource != null){
                        add_property(productBrand_resource, brandCompanyProperty, productCompany_resource);
                    }
                }
            }

            // 如果成功读取并存储数据，返回true
            return true;
        } catch (IOException | CsvValidationException e) {
            e.printStackTrace();
            return false;
        }
    }

//    @Override
//    public String generate_resource_url_hashcode(String str) {
//        try {
//            String input = System.currentTimeMillis() + "-" + new Random().nextInt(1000000) + str;
//            MessageDigest md = MessageDigest.getInstance("SHA-256");
//            byte[] hash = md.digest(input.getBytes());
//            StringBuilder hexString = new StringBuilder();
//            for (byte b : hash) {
//                String hex = Integer.toHexString(0xff & b);
//                if (hex.length() == 1) hexString.append('0');
//                hexString.append(hex);
//            }
//            return hexString.toString();
//        } catch (NoSuchAlgorithmException e) {
//            throw new RuntimeException(e);
//        }
//    }

    @Override
    public Resource create_classification_resource(Resource resource, String classifications) {
        // 提取分类值并去重
        String[] categories = classifications.split("\\s+");
        Set<String> uniqueCategories = new LinkedHashSet<>(Arrays.asList(categories));

        // 转换为列表
        List<String> uniqueCategoryList = new ArrayList<>(uniqueCategories);

        Resource pre_category = productClass;
        Resource post_category = null;

        // 处理每个分类项
        for (String category : uniqueCategoryList) {
            // 处理分类,将其中的因为翻译中文标注
            String tempWords = translate_method.processCategory(category);
            if (tempWords!=null){
                category = tempWords;
            }
            // 查找分类
            post_category = find_classification(category);

            if (post_category==null){
                // 创建分类子类
                post_category = create_resource(category, ind_equ_url);
                post_category.addProperty(nameProperty, category);
                add_subclass_relation(post_category, pre_category);
            }
            pre_category = post_category;
        }
        //TODO

        return pre_category;
    }

    @Override
    public Resource find_product(String product_name) {
        return find_resource_from_class_by_property_value(productClass, nameProperty, product_name);
    }

    @Override
    public Resource find_brand(String brand_name) {
        return find_resource_from_class_by_property_value(brandClass, nameProperty, brand_name);
    }

    @Override
    public Resource find_company(String company_name) {
        return find_resource_from_class_by_property_value(companyClass, nameProperty, company_name);
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
    public boolean add_brand_relation(Resource resourceA, Resource resourceB) {
        return add_property(resourceA, brandProperty, resourceB);
    }

    @Override
    public boolean add_product_company_relation(Resource resourceA, Resource resourceB) {
        return add_property(resourceA, productCompanyProperty, resourceB);
    }

    @Override
    public boolean add_brand_company_relation(Resource resourceA, Resource resourceB) {
        return add_property(resourceA, brandCompanyProperty, resourceB);
    }

    @Override
    public boolean add_equip_relation(Resource product, Resource classification) {
        return add_property(product, equipProperty, classification);
    }

    @Override
    public boolean add_applied_relation(Resource product, Resource classification) {
        return add_property(product, appliedToProperty, classification);
    }

    @Override
    public List<String> find_all_classification() {
        List<Resource> allSubclasses = findAllSubclasses(model, productClass);
        List<String> resourceNames = allSubclasses.stream()
                .map(this::get_resource_name)
                .collect(Collectors.toList());
        return resourceNames;
    }

    @Override
    public boolean create_equip_relation(Resource product){
        List<String> allSubclassesStrings = find_all_classification();
        // 检查列表是否为空或只有一个元素
        if (allSubclassesStrings.size()!= 0 && allSubclassesStrings.size()!= 1) {
            allSubclassesStrings.remove(0);
            // 将 allSubclassesStrings 拼成一个字符串，且每个字符串间用 ", " 分隔
            String classificationString = String.join(", ", allSubclassesStrings);
            String productName = product.getProperty(nameProperty).getObject().toString();
            String productInformation = product.getProperty(productIntroductionProperty).getObject().toString();
            String inputText = "'" + productName + "'的产品介绍如下: \n" + productInformation;

            String relationString = open_spg_method.ExtractDeviceRelation(inputText);
            // 打印或使用 concatenatedString
//            System.out.println(concatenatedString);
//            System.out.println(inputText);
//            System.out.println(relationString);

            List<String> equipList;
            List<String> usedList;
            //提取设备关系
            {
                if (relationString.length() <= 2) {
                    return false;
                }
                // 去掉外层的 [ 和 ]
                relationString = relationString.substring(1, relationString.length() - 1);
                // 按照 ], [ 分割外层集合
                String[] outerSets = relationString.split("\\], \\[");
                if (outerSets.length != 2) {
                    return false;
                }
                // 去掉内层的 [ 和 ]
                String outerSet1 = outerSets[0];
                outerSet1 = outerSet1.replace("[", "").replace("]", "");
                // 按照 , 分割内层集合
                String[] innerElements1 = outerSet1.split(", ");
                equipList = new ArrayList<>(Arrays.asList(innerElements1));
                String outerSet2 = outerSets[1];
                outerSet2 = outerSet2.replace("[", "").replace("]", "");
                // 按照 , 分割内层集合
                String[] innerElements2 = outerSet2.split(", ");
                usedList = new ArrayList<>(Arrays.asList(innerElements2));
                System.out.println(equipList + "\n" + usedList);
            }

            for (String equip : equipList) {
                List<String> equipClassificationList = macbert_method.runMacBERT(equip, classificationString);
                String category = "";
                if (equipClassificationList.size() > 0){
                    category = equipClassificationList.get(0);
                    Resource categoryResource = find_classification(category);
//                    System.out.println(categoryResource);
                    if (categoryResource != null) {
                        add_equip_relation(product, categoryResource);
//                        System.out.println(product.getProperty(equipProperty));
                    }
                }
                System.out.println(equip + "关联分类标签：" + category);
            }

            for (String used : usedList) {
                List<String> usedClassificationList = macbert_method.runMacBERT(used, classificationString);
                String category = "";
                if (usedClassificationList.size() > 0){
                    category = usedClassificationList.get(0);
                    Resource categoryResource = find_classification(category);
                    System.out.println(categoryResource);
                    if (categoryResource != null) {
                        add_applied_relation(product, categoryResource);
                        System.out.println(product.getProperty(appliedToProperty));
                    }
                }
                System.out.println(used + "关联分类标签：" + category);
            }

        }
        return false;
    }

    @Override
    public boolean create_all_equip_relation(){
        // 获取类和子类的所有实例
        List<Resource> allSubclasses = findAllSubclasses(model, productClass);

//        List<Individual> allInstances = new ArrayList<>();
        // 遍历所有子类
        for (Resource subclass : allSubclasses) {
            // 获取子类的所有实例
            ExtendedIterator<Statement> statements = model.listStatements(null, RDF.type, subclass);

            while (statements.hasNext()) {
                Statement statement = statements.next();
                RDFNode subject = statement.getSubject();

                if (subject.isResource()) {
                    Resource instance = subject.asResource();
                    //建立实例间关系
                    create_equip_relation(instance);
//                    System.out.println("Instance: " + instance.getProperty(nameProperty).getObject());
                }
            }
        }

        return false;
    }

    @Override
    public List<String> find_all_product_name(){
        List<String> productNames = new ArrayList<>();
        // 获取类和子类的所有实例
        List<Resource> allSubclasses = findAllSubclasses(model, productClass);

//        List<Individual> allInstances = new ArrayList<>();
        // 遍历所有子类
        for (Resource subclass : allSubclasses) {
            // 获取子类的所有实例
            ExtendedIterator<Statement> statements = model.listStatements(null, RDF.type, subclass);

            while (statements.hasNext()) {
                Statement statement = statements.next();
                RDFNode subject = statement.getSubject();

                if (subject.isResource()) {
                    Resource instance = subject.asResource();
                    //建立实例间关系
                    productNames.add(instance.getProperty(nameProperty).getObject().toString());
//                    System.out.println("Instance: " + instance.getProperty(nameProperty).getObject());
                }
            }
        }
        return productNames;
    }


//    @Override
//    public List<String> write_person_ont_string(String filename) {
//        return null;
//    }
//
//    @Override
//    public void write_stmt_string(Resource object, List<String> strings) {
//
//    }
//
//    @Override
//    public void read_person_ont_string_map(String filename) {
//
//    }
}
