package org.example.Ind_equ.method.ont.impl;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;

import org.apache.jena.rdf.model.*;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.XSD;
import org.example.Ind_equ.method.ont.ModelOnt;
import org.example.Ind_equ.method.strings.StringsMethod;
import org.example.Ind_equ.method.strings.TranslateMethod;
import org.example.Ind_equ.method.strings.impl.TranslateMethodImpl;
import org.example.ont.impl.OntMethodImpl;
import org.example.property_method.PropertyLoader;
import org.example.property_method.impl.PropertyLoaderImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;

import static org.example.Ind_equ.method.strings.StringsMethod.normalizeToNfc;

public class ModelOntImpl extends OntMethodImpl implements ModelOnt {
    private final PropertyLoader property_loader = new PropertyLoaderImpl("industrial_equipment.properties");
    private final TranslateMethod translate_method = new TranslateMethodImpl();
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

    /*
     * 初始资源
     */
    // 产品
    Resource productClass = create_resource("产品", ind_equ_url, OWL.Class);
    // 品牌
    Resource brandClass = create_resource("品牌", ind_equ_url,OWL.Class);
    // 公司或专利方
    Resource companyClass = create_resource("公司或专利方",ind_equ_url,OWL.Class);
    // 专利人
    Resource patentPersonClass = create_resource("专利人",ind_equ_url,OWL.Class);
    // 制造商
    Resource manufacturerClass = create_resource("制造商",ind_equ_url,OWL.Class);
    // 设备领域
    Resource equipmentDomainClass = create_resource("设备领域", ind_equ_url,OWL.Class);
    // 行业
    Resource industryClass = create_resource("行业", ind_equ_url,OWL.Class);
    // 专利
    Resource patentClass = create_resource("专利", ind_equ_url,OWL.Class);



    /*
     * 初始属性
     */
    // ObjectProperty
    // 产品品牌
    Property productBrandProperty = create_property("产品品牌", ind_equ_url);
    // 产品公司/专利方
    Property productCompanyProperty = create_property("产品公司或专利方", ind_equ_url);
    // 品牌公司/专利方
    Property brandCompanyProperty = create_property("品牌的公司或专利方", ind_equ_url);
    // 产品专利
    Property productPatentProperty = create_property("产品专利", ind_equ_url);
    // 专利专利人
    Property patentPersonProperty = create_property("专利人", ind_equ_url);
    // 专利公司或专利方
    Property patentCompanyProperty = create_property("专利公司或专利方", ind_equ_url);
    // 专利引用
    Property patentReferenceProperty = create_property("专利引用", ind_equ_url);
    // 专利应用
    Property patentApplicationProperty = create_property("专利应用", ind_equ_url);
    // 产品应用领域
    Property productApplicationDomainProperty = create_property("产品应用领域", ind_equ_url);
    // 产品的服务行业
    Property productServiceIndustryProperty = create_property("产品服务行业", ind_equ_url);
    // 产品制造商
    Property productManufacturerProperty = create_property("产品制造商", ind_equ_url);
    // 设备间关系--使用到
    Property equipProperty = create_property("使用到", ind_equ_url);
    // 设备间关系--应用到
    Property appliedToProperty = create_property("应用于", ind_equ_url);

    // DatatypeProperty
    Property nameProperty = create_property("名称", ind_equ_url);
    Property productNameProperty = create_property("产品名称", ind_equ_url);
    Property productIntroProperty = create_property("产品简介", ind_equ_url);
    Property productImageProperty = create_property("产品图片", ind_equ_url);
    Property productIntroductionProperty = create_property("产品介绍", ind_equ_url);
    Property productLinkProperty = create_property("产品链接", ind_equ_url);
    // 专利的专利号
    Property patentNumberProperty = create_property("专利号", ind_equ_url);
    // 专利标题
    Property patentTitleProperty = create_property("专利标题", ind_equ_url);
    // 专利摘要
    Property patentAbstractProperty = create_property("专利摘要", ind_equ_url);
    // 专利声明
    Property patentClaimProperty = create_property("专利声明", ind_equ_url);
    // 专利应用事件
    Property patentEventProperty = create_property("专利事件", ind_equ_url);
    // 公司名称
    Property companyNameProperty = create_property("公司名称", ind_equ_url);
    Property companyAddressProperty = create_property("公司地址", ind_equ_url);
    Property companyPhoneProperty = create_property("公司电话", ind_equ_url);
    Property companyWebsiteProperty = create_property("公司网址", ind_equ_url);
    Property companyIntroductionProperty = create_property("公司介绍", ind_equ_url);
    Property companyImageProperty = create_property("公司图片", ind_equ_url);
    Property companyLinkProperty = create_property("公司链接", ind_equ_url);
    // 品牌名称
    Property brandNameProperty = create_property("品牌名称", ind_equ_url);







    public ModelOntImpl() {
        init_method();
    }

    public ModelOntImpl(Model model) {
        super(model);
        init_method();
    }

    @Override
    public boolean init_method() {
        System.out.println("Industrial equipment ontology initialized.");
//        System.out.println(ind_equ_url);

        // 制造商初始化
        {
            /*
              制造商的五种分类: 原始设备制造商OEM、原始设计制造商ODM、原始品牌制造商OBM、共同设计制造JDM、来料加工CMT
             */
            Resource OEMClass = create_resource("原始设备制造商OEM", ind_equ_url, OWL.Class);
            add_subclass_relation(OEMClass, manufacturerClass);
            Resource ODMClass = create_resource("原始设计制造商ODM", ind_equ_url, OWL.Class);
            add_subclass_relation(ODMClass, manufacturerClass);
            Resource OBMClass = create_resource("原始品牌制造商OBM", ind_equ_url, OWL.Class);
            add_subclass_relation(OBMClass, manufacturerClass);
            Resource JDMClass = create_resource("共同设计制造JDM", ind_equ_url, OWL.Class);
            add_subclass_relation(JDMClass, manufacturerClass);
        }

        /*

          初始化属性(通用属性)

         */
        // ObjectProperty
        // 产品品牌
        productBrandProperty.addProperty(RDF.type, OWL.ObjectProperty);
        productBrandProperty.addProperty(RDFS.domain, productClass);
        productBrandProperty.addProperty(RDFS.range, brandClass);
        // 产品公司/专利方
        productCompanyProperty.addProperty(RDF.type, OWL.ObjectProperty);
        productCompanyProperty.addProperty(RDFS.domain, productClass);
        productCompanyProperty.addProperty(RDFS.range, companyClass);
        // 品牌公司/专利方
        brandCompanyProperty.addProperty(RDF.type, OWL.ObjectProperty);
        brandCompanyProperty.addProperty(RDFS.domain, brandClass);
        brandCompanyProperty.addProperty(RDFS.range, companyClass);
        // 产品专利
        productPatentProperty.addProperty(RDF.type, OWL.ObjectProperty);
        productPatentProperty.addProperty(RDFS.domain, productClass);
        productPatentProperty.addProperty(RDFS.range, patentClass);
        // 专利专利人
        patentPersonProperty.addProperty(RDF.type, OWL.ObjectProperty);
        patentPersonProperty.addProperty(RDFS.domain, patentClass);
        patentPersonProperty.addProperty(RDFS.range, patentPersonClass);
        // 专利公司或专利方
        patentCompanyProperty.addProperty(RDF.type, OWL.ObjectProperty);
        patentCompanyProperty.addProperty(RDFS.domain, patentClass);
        patentCompanyProperty.addProperty(RDFS.range, companyClass);
        // 专利引用
        patentReferenceProperty.addProperty(RDF.type, OWL.ObjectProperty);
        patentReferenceProperty.addProperty(RDFS.domain, patentClass);
        patentReferenceProperty.addProperty(RDFS.range, patentClass);
        // 专利应用
        patentApplicationProperty.addProperty(RDF.type, OWL.ObjectProperty);
        patentApplicationProperty.addProperty(RDFS.domain, patentClass);
        patentApplicationProperty.addProperty(RDFS.range, patentClass);
        // 产品应用领域
        productApplicationDomainProperty.addProperty(RDF.type, OWL.ObjectProperty);
        productApplicationDomainProperty.addProperty(RDFS.domain, productClass);
        productApplicationDomainProperty.addProperty(RDFS.range, equipmentDomainClass);
        // 产品的服务行业
        productServiceIndustryProperty.addProperty(RDF.type, OWL.ObjectProperty);
        productServiceIndustryProperty.addProperty(RDFS.domain, productClass);
        productServiceIndustryProperty.addProperty(RDFS.range, industryClass);
        // 产品制造商
        productManufacturerProperty.addProperty(RDF.type, OWL.ObjectProperty);
        productManufacturerProperty.addProperty(RDFS.domain, productClass);
        productManufacturerProperty.addProperty(RDFS.range, manufacturerClass);
        // 设备间关系--使用到
        equipProperty.addProperty(RDF.type, OWL.ObjectProperty);
        equipProperty.addProperty(RDFS.domain, productClass);
        equipProperty.addProperty(RDFS.range, productClass);
        // 设备间关系--应用到
        appliedToProperty.addProperty(RDF.type, OWL.ObjectProperty);
        appliedToProperty.addProperty(RDFS.domain, productClass);
        appliedToProperty.addProperty(RDFS.range, productClass);



        // DatatypeProperty
        // 名称，必备属性
        nameProperty.addProperty(RDF.type, OWL.DatatypeProperty);
        nameProperty.addProperty(RDFS.range, XSD.normalizedString);
        // 产品简介
        productIntroProperty.addProperty(RDF.type, OWL.DatatypeProperty);
        productIntroProperty.addProperty(RDFS.domain, productClass);
        productIntroProperty.addProperty(RDFS.range, XSD.xstring);
        // 产品图片
        productImageProperty.addProperty(RDF.type, OWL.DatatypeProperty);
        productImageProperty.addProperty(RDFS.domain, productClass);
        productImageProperty.addProperty(RDFS.range, XSD.anyURI);
        // 产品介绍
        productIntroductionProperty.addProperty(RDF.type, OWL.DatatypeProperty);
        productIntroductionProperty.addProperty(RDFS.domain, productClass);
        productIntroductionProperty.addProperty(RDFS.range, XSD.xstring);
        // 产品链接
        productLinkProperty.addProperty(RDF.type, OWL.DatatypeProperty);
        productLinkProperty.addProperty(RDFS.domain, productClass);
        productLinkProperty.addProperty(RDFS.range, XSD.anyURI);
        // 专利的专利号
        patentNumberProperty.addProperty(RDF.type, OWL.DatatypeProperty);
        patentNumberProperty.addProperty(RDFS.domain, patentClass);
        patentNumberProperty.addProperty(RDFS.range, XSD.normalizedString);
        // 专利标题
        patentTitleProperty.addProperty(RDF.type, OWL.DatatypeProperty);
        patentTitleProperty.addProperty(RDFS.domain, patentClass);
        patentTitleProperty.addProperty(RDFS.range, XSD.xstring);
        // 专利摘要
        patentAbstractProperty.addProperty(RDF.type, OWL.DatatypeProperty);
        patentAbstractProperty.addProperty(RDFS.domain, patentClass);
        patentAbstractProperty.addProperty(RDFS.range, XSD.xstring);
        // 专利声明
        patentClaimProperty.addProperty(RDF.type, OWL.DatatypeProperty);
        patentClaimProperty.addProperty(RDFS.domain, patentClass);
        patentClaimProperty.addProperty(RDFS.range, XSD.xstring);
        // 专利应用事件
        patentEventProperty.addProperty(RDF.type, OWL.DatatypeProperty);
        patentEventProperty.addProperty(RDFS.domain, patentClass);
        patentEventProperty.addProperty(RDFS.range, XSD.xstring);
        // 产品名称
        productNameProperty.addProperty(RDF.type, OWL.DatatypeProperty);
        productNameProperty.addProperty(RDFS.domain, productClass);
        productNameProperty.addProperty(RDFS.range, XSD.normalizedString);
        // 公司名称
        companyNameProperty.addProperty(RDF.type, OWL.DatatypeProperty);
        companyNameProperty.addProperty(RDFS.domain, companyClass);
        companyNameProperty.addProperty(RDFS.range, XSD.normalizedString);
        // 品牌名称
        brandNameProperty.addProperty(RDF.type, OWL.DatatypeProperty);
        brandNameProperty.addProperty(RDFS.domain, brandClass);
        brandNameProperty.addProperty(RDFS.range, XSD.normalizedString);

        return false;
    }

    private String get_file_type(String file_name){
        return file_name.substring(file_name.lastIndexOf(".") + 1);
    }

    @Override
    public boolean read_ont(String ont_filename, String base_url) {
        ont_filename = ont_root + get_file_type(ont_filename) + "/" + ont_filename;
        return super.read_ont(ont_filename, base_url);
    }

    @Override
    public boolean add_ont(String ont_filename, String base_url) {
        ont_filename = ont_root + get_file_type(ont_filename) + "/" + ont_filename;
        return super.add_ont(ont_filename, base_url);
    }

    @Override
    public void write_ont(String ont_filename) {
        ont_filename = ont_root + get_file_type(ont_filename) + "/indEqu/AI_method/keywords/BERT/model/" + ont_filename;
        super.write_ont(ont_filename);
    }

    @Override
    public boolean add_subclass_relation(Resource resourceA, Resource resourceB) {
        return add_property(resourceA, RDFS.subClassOf, resourceB);
    }

    @Override
    public boolean generate_init_ont_from_datafile(String datafile_name, int init_ont_case) {
        if (datafile_name == null || !datafile_name.contains(".")) {
            logger.error("Invalid file name");
            return false;
        }
        String type = get_file_type(datafile_name);
        String file_path = ont_root + type + "/indEqu/AI_method/keywords/BERT/model/" + datafile_name;
        switch (init_ont_case) {
            case 1:
                if (type.equals("csv")){
                    return add_init_ont_case1(file_path);
                }
            default:
                logger.warn("Unsupported init_ont_case");
        }
        return false;
    }

    @Override
    public boolean add_init_ont_case1(String filename) {
        try (CSVReader reader = new CSVReader(new FileReader(filename))) {
            String[] line;
            while ((line = reader.readNext()) != null) {
                if (line.length < 3) {
                    logger.warn("Skipping incomplete line: " + Arrays.toString(line));
                    continue;
                }
                String classification = line[0];
                String applicationDomain = line[1];
                String serviceIndustry = line[2];

                // 处理每列数据，这里先简单地输出数据
                /*
                 * 产品分类处理
                 */
                // 字符串预处理
                classification = normalizeToNfc(classification.trim());
                // 对于classification字符串里用空格分隔的分类，获取最后一项
                String[] parts = classification.split(" ");
                String classificationName = parts[parts.length - 1];
                Resource classificationResource = null;

                if (classificationName.length() != 0) {
                    // 使用String.join重新拼接去掉最后一项的字符串
                    String subClassification = String.join(" ", Arrays.copyOfRange(parts, 0, parts.length - 1)).trim();
                    String classificationUrl = StringsMethod.weekCleanForOWLIri(classification);
                    classificationResource = create_resource(classificationUrl, ind_equ_url, OWL.Class);
                    // 翻译分类名称
                    String tempWords = translate_method.processCategory(classificationName);
                    if (tempWords!=null){
                        classificationName = tempWords;
                    }
                    // 添加名称属性
                    classificationResource.addProperty(nameProperty, classificationName);
                    // 添加父类
                    if (!subClassification.isEmpty()){
                        String subClassificationUrl = StringsMethod.weekCleanForOWLIri(subClassification);
//                    System.out.println("父类：" + subClassificationUrl);
                        Resource parentClassificationResource = get_resource(subClassificationUrl, ind_equ_url);
                        if (parentClassificationResource==null){
                            System.out.println("\u001B[31m父类" + subClassificationUrl + "不存在，无法添加\u001B[0m");
                            // 将 subClassificationUrl 写入 undefinitionClassification.csv
                            try (FileWriter writer = new FileWriter(ont_root + "unDefinitionClassification.csv", true)) {
                                writer.append(subClassificationUrl);
                                writer.append(System.lineSeparator());
                            } catch (IOException e) {
                                System.err.println("Error writing to CSV file: " + e.getMessage());
                            }
                        }
                        else {
                            add_subclass_relation(classificationResource, parentClassificationResource);
                        }
//                    System.out.println("父类关系创建成功！");
                    }
                    else {
                        add_subclass_relation(classificationResource, productClass);
                    }
                }

                /*
                 * 产品应用领域处理
                 */
                // 字符串预处理
                applicationDomain = normalizeToNfc(applicationDomain.trim());
                // 将应用领域字符串里用空格分隔的领域，获取列表
                String[] applicationDomainList = applicationDomain.split(" ");
                for (String applicationDomainItem : applicationDomainList) {
                    if (applicationDomainItem.length() != 0) {
                        String applicationDomainUrl = StringsMethod.weekCleanForOWLIri(applicationDomainItem.trim());
                        // 判断applicationDomainUrl资源是否已经存在，若不存在则创建资源，若存在则判断该资源是否是设备领域的子类（存在属性subClassof为设备领域），若不存在则创建设备领域的子类的属性
                        Resource applicationDomainResource = get_resource(applicationDomainUrl, ind_equ_url);
                        if (applicationDomainResource == null) {
                            // 创建应用领域类
                            applicationDomainResource = create_resource(applicationDomainUrl, ind_equ_url, OWL.Class);
                            // 添加名称属性
                            applicationDomainResource.addProperty(nameProperty, applicationDomainItem);
                            // 添加父类
                            add_subclass_relation(applicationDomainResource, equipmentDomainClass);
//                        System.out.println("应用领域类创建成功！");
                        } else {
                            if (!applicationDomainResource.hasProperty(RDFS.subClassOf, equipmentDomainClass)) {
                                // 添加父类
                                add_subclass_relation(applicationDomainResource, equipmentDomainClass);
//                            System.out.println("应用领域类添加父类成功！");
                            }
                        }
                        if (classificationResource != null){
                            add_property(classificationResource, productApplicationDomainProperty, applicationDomainResource);
                        }
                    }
                }

                /*
                 * 产品服务行业处理
                 */
                serviceIndustry = normalizeToNfc(serviceIndustry.trim());
                String[] serviceIndustryList = serviceIndustry.split(" ");
                for (String serviceIndustryItem : serviceIndustryList) {
                    if (serviceIndustryItem.length() != 0) {
                        String serviceIndustryUrl = StringsMethod.weekCleanForOWLIri(serviceIndustryItem.trim());
                        // 判断serviceIndustryUrl资源是否已经存在，若不存在则创建资源，若存在则判断该资源是否是设备行业的子类（存在属性subClassof为设备行业），若不存在则创建设备行业的子类的属性
                        Resource serviceIndustryResource = get_resource(serviceIndustryUrl, ind_equ_url);
                        if (serviceIndustryResource == null) {
                            //创建服务行业类
                            serviceIndustryResource = create_resource(serviceIndustryUrl, ind_equ_url, OWL.Class);
                            // 添加名称属性
                            serviceIndustryResource.addProperty(nameProperty, serviceIndustryItem);
                            // 添加父类
                            add_subclass_relation(serviceIndustryResource, industryClass);
//                        System.out.println("服务行业类创建成功！");
                        }
                        else {
                            if (!serviceIndustryResource.hasProperty(RDFS.subClassOf, industryClass)) {
                                // 添加父类
                                add_subclass_relation(serviceIndustryResource, industryClass);
//                            System.out.println("服务行业类添加父类成功！");
                            }
                        }
                        if (classificationResource != null){
                            add_property(classificationResource, productServiceIndustryProperty, serviceIndustryResource);
                        }
                    }
                }

//                System.out.println("Classification: " + classification);
//                System.out.println("Application Domain: " + applicationDomain);
//                System.out.println("Service Industry: " + serviceIndustry);
//                System.out.println("-----------------------------");
            }
            return true;
        } catch (IOException | CsvValidationException e) {
            e.printStackTrace();
            return false;
        }
    }


    public static void main(String[] args) {
        ModelOnt modelOnt = new ModelOntImpl();
        Model model = ModelFactory.createDefaultModel();
        String resource_url = "http://example.org/resource";

        // 获取资源
        model.createResource(resource_url, OWL.Class);
        Resource resource = model.getResource(resource_url);
        System.out.println(model.containsResource(resource));
        modelOnt.create_resource("resource", "http://example.org/", OWL.Class);
        System.out.println(modelOnt.get_resource("resource", "http://example.org/"));


    //    // 检查资源是否在模型中存在
    //    if (model.containsResource(resource)) {
    //        System.out.println("Resource exists in the model.");
    //        // 检查资源是否有属性
    //        if (resource.listProperties().hasNext()) {
    //            System.out.println("Resource has properties.");
    //        } else {
    //            System.out.println("Resource exists but has no properties (empty resource).");
    //        }
    //    } else {
    //        System.out.println("Resource does not exist in the model.");
    //    }
    }
}
