package org.example.Ind_equ.method.ont;

import org.example.Ind_equ.method.ont.impl.ModelOntImpl;

public class ModelTest {
    public static void main(String[] args) {
        ModelOnt modelOnt = new ModelOntImpl();
        modelOnt.init_method();
        modelOnt.generate_init_ont_from_datafile("初始模型_机箱机柜.csv",1);
        modelOnt.write_ont("初始模型_机箱机柜v1.owl");
        modelOnt.write_ont("初始模型_机箱机柜v1.jsonld");
    }
}



//    @Override
//    public Resource create_classification_resource(Resource resource, String classifications) {
//        // 提取分类值并去重
//        String[] categories = classifications.split("\\s+");
//        Set<String> uniqueCategories = new LinkedHashSet<>(Arrays.asList(categories));
//
//        // 转换为列表
//        List<String> uniqueCategoryList = new ArrayList<>(uniqueCategories);
//
//        Resource pre_category = productClass;
//        Resource post_category = null;
//
//        // 处理每个分类项
//        for (String category : uniqueCategoryList) {
//            // 处理分类,将其中的因为翻译中文标注
//            String tempWords = translate_method.processCategory(category);
//            if (tempWords!=null){
//                category = tempWords;
//            }
//            // 查找分类
//            post_category = find_classification(category);
//
//            if (post_category==null){
//                // 创建分类子类
//                post_category = create_resource(category, ind_equ_url, OWL.Class);
//                post_category.addProperty(nameProperty, category);
//                add_subclass_relation(post_category, pre_category);
//            }
//            pre_category = post_category;
//        }
//        //TODO
//
//        return pre_category;
//    }

//    @Override
//    public Resource find_product(String product_name) {
//        return find_resource_from_class_by_property_value(productClass, nameProperty, product_name);
//    }

//    @Override
//    public Resource find_brand(String brand_name) {
//        return find_resource_from_class_by_property_value(brandClass, nameProperty, brand_name);
//    }

//    @Override
//    public Resource find_company(String company_name) {
//        return find_resource_from_class_by_property_value(companyClass, nameProperty, company_name);
//    }

//    @Override
//    public boolean add_brand_relation(Resource resourceA, Resource resourceB) {
//        return add_property(resourceA, brandProperty, resourceB);
//    }

//    @Override
//    public boolean add_product_company_relation(Resource resourceA, Resource resourceB) {
//        return add_property(resourceA, productCompanyProperty, resourceB);
//    }

//    @Override
//    public boolean add_brand_company_relation(Resource resourceA, Resource resourceB) {
//        return add_property(resourceA, brandCompanyProperty, resourceB);
//    }

//    @Override
//    public boolean add_equip_relation(Resource product, Resource classification) {
//        return add_property(product, equipProperty, classification);
//    }

//    @Override
//    public boolean add_applied_relation(Resource product, Resource classification) {
//        return add_property(product, appliedToProperty, classification);
//    }


//    @Override
//    public boolean create_all_equip_relation(){
//        // 获取类和子类的所有实例
//        List<Resource> allSubclasses = findAllSubclasses(model, productClass);
//
////        List<Individual> allInstances = new ArrayList<>();
//        // 遍历所有子类
//        for (Resource subclass : allSubclasses) {
//            // 获取子类的所有实例
//            ExtendedIterator<Statement> statements = model.listStatements(null, RDF.type, subclass);
//
//            while (statements.hasNext()) {
//                Statement statement = statements.next();
//                RDFNode subject = statement.getSubject();
//
//                if (subject.isResource()) {
//                    Resource instance = subject.asResource();
//                    //建立实例间关系
//                    create_equip_relation(instance);
////                    System.out.println("Instance: " + instance.getProperty(nameProperty).getObject());
//                }
//            }
//        }
//
//        return false;
//    }

//    @Override
//    public List<String> find_all_product_name(){
//        List<String> productNames = new ArrayList<>();
//        // 获取类和子类的所有实例
//        List<Resource> allSubclasses = findAllSubclasses(model, productClass);
//
////        List<Individual> allInstances = new ArrayList<>();
//        // 遍历所有子类
//        for (Resource subclass : allSubclasses) {
//            // 获取子类的所有实例
//            ExtendedIterator<Statement> statements = model.listStatements(null, RDF.type, subclass);
//
//            while (statements.hasNext()) {
//                Statement statement = statements.next();
//                RDFNode subject = statement.getSubject();
//
//                if (subject.isResource()) {
//                    Resource instance = subject.asResource();
//                    //建立实例间关系
//                    productNames.add(instance.getProperty(nameProperty).getObject().toString());
////                    System.out.println("Instance: " + instance.getProperty(nameProperty).getObject());
//                }
//            }
//        }
//        return productNames;
//    }

// 将提炼出来的属性列表转为本体属性


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