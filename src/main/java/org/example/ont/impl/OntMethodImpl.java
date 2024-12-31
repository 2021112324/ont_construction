package org.example.ont.impl;

import org.apache.jena.rdf.model.*;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.example.Ind_equ.method.strings.StringsMethod;
import org.example.ont.OntMethod;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class OntMethodImpl implements OntMethod {
//    private final Property_loader property_loader = new Property_loader_impl("industrial_equipment.properties");
//    // 初始化常量
//    private final String rdf_type = property_loader.get_key_value("RDF_TYPE");
    protected Model model = ModelFactory.createDefaultModel();
    protected Dataset dataset = DatasetFactory.createTxnMem();

    public Model getModel() {
        return model;
    }

    public OntMethodImpl() {
        // 定义命名空间
        model.setNsPrefix("owl", "http://www.w3.org/2002/07/owl#");
        model.setNsPrefix("rdf", "http://www.w3.org/2002/07/owl#");
        model.setNsPrefix("xsd", "http://www.w3.org/2001/XMLSchema#");
        model.setNsPrefix("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
    }

    public OntMethodImpl(Model model) {
        this.model = model;
    }

    public boolean read_ont(String ont_filename, String base_url) {
        InputStream in = RDFDataMgr.open(ont_filename);
        if (in == null){
            System.out.println("File not found");
            return false;
        }

        model.read(in, base_url);
        return true;
    }

    @Override
    public boolean add_ont(String ont_filename, String base_url) {
        //创建model
        Model model2 = ModelFactory.createDefaultModel();
        InputStream in = RDFDataMgr.open(ont_filename);
        if (in == null){
            System.out.println("File not found");
            return false;
        }
        model2.read(in, base_url);

        //合并model
        try {
            model = model.union(model2);
        }catch (Exception e){
            e.printStackTrace();
        }
        return true;
    }

     public void write_ont(String ont_filename) {
         // 获取输出文件的类型
         String type = ont_filename.substring(ont_filename.lastIndexOf(".") + 1);

         try {
             // 获取文件路径
             Path file_path = Paths.get(ont_filename);

             // 检查并创建目录
             Path directory = file_path.getParent();
             if (directory != null && !Files.exists(directory)) {
                 Files.createDirectories(directory);
             }

             // 根据文件扩展名自动推断输出格式
             switch (type) {
                 case "rdf":
                 case "owl":
                     model.write(Files.newOutputStream(file_path), "RDF/XML-ABBREV");
                     break;
                 case "ttl":
                     model.write(Files.newOutputStream(file_path), "Turtle");
                     break;
                 case "jsonld":
                     model.write(Files.newOutputStream(file_path), "JSON-LD");
                     break;
                 case "nt":
                     model.write(Files.newOutputStream(file_path), "N-Triples");
                     break;
                 case "n3":
                     model.write(Files.newOutputStream(file_path), "N3");
                     break;
                 default:
                     System.out.println("Unsupported file type");
             }

         } catch (IOException e) {
             e.printStackTrace();
         }
    }

    @Override
    public void print_ont() {
        model.write(System.out);
    }

    @Override
    public void print_ont(String type) {
        model.write(System.out, type);
    }



    @Override
    public boolean add_ont(Model model2) {
        //合并model
        try {
            model = model.union(model2);
        }catch (Exception e){
            e.printStackTrace();
        }
        return true;
    }

    @Override
    public Resource create_resource(String resource, String base_url) {
        // 空值检查
        if (base_url == null || resource == null) {
            throw new IllegalArgumentException("base_url and resource cannot be null");
        }
//        // 路径注入防护
//        if (resource.contains("..") || resource.contains("/") || resource.contains("\\")) {
//            throw new IllegalArgumentException("Invalid resource path: " + resource);
//        }
        //处理分类里的特俗符号
        resource = StringsMethod.weekCleanForOWLIri(resource);
        String resource_url = String.format("%s#%s", base_url, resource);
        // 清除零格字符
        resource_url = resource_url.replaceAll("\\u200B", "");
        try {
            return model.createResource(resource_url);
//            // 异常处理
//            Resource tempResource= model.createResource(resource_url);
//            // 确保模型资源更新
//            // 提交事务
//            dataset.begin(ReadWrite.WRITE);
//            try {
//                dataset.commit();
//            } finally {
//                dataset.end();
//            }
//            return tempResource;
        } catch (Exception e) {
            // 处理异常
            throw new RuntimeException("Failed to create resource: " + resource_url, e);
        }
    }

    @Override
    public Resource create_resource(String resource, String base_url, Resource type) {
        // 空值检查
        if (base_url == null || resource == null) {
            throw new IllegalArgumentException("base_url and resource cannot be null");
        }
//        // 路径注入防护
//        if (resource.contains("..") || resource.contains("/") || resource.contains("\\")) {
//            throw new IllegalArgumentException("Invalid resource path: " + resource);
//        }
        resource = StringsMethod.weekCleanForOWLIri(resource);
        // 避免递归调用
        String resource_url = String.format("%s#%s", base_url, resource);
        try {
            // 异常处理
            return type != null ? model.createResource(resource_url, type) : model.createResource(resource_url);
        } catch (Exception e) {
            // 处理异常
            throw new RuntimeException("Failed to create resource: " + resource_url, e);
        }
    }

    @Override
    public Resource get_resource(String resource, String base_url) {
        String resource_url = String.format("%s#%s", base_url, resource);
        Resource temp_resource = model.getResource(resource_url);
        if (resource == null || resource.equals("") || !model.containsResource(temp_resource)){
            return null;
        }
        return temp_resource;
    }

    @Override
    public String get_resource_name(Resource resource) {
        if (resource == null){
            return null;
        }
        return resource.getLocalName();
    }

    @Override
    public List<Resource> get_all_resource() {
        List<Resource> resources = new ArrayList<>();

        // 获取模型中的所有资源
        ExtendedIterator<Resource> iterator = model.listSubjects();

        // 遍历并添加到列表中
        while (iterator.hasNext()) {
            Resource resource = iterator.next();
            if (resource.getNameSpace() != null && !resource.getNameSpace().isEmpty()) {
                resources.add(resource);
            }
        }

        return resources;
    }

    @Override
    public List<String> get_all_resource_name() {
        List<String> resource_names = new ArrayList<>();
        List<Resource> resources = get_all_resource();
        if (resources == null || resources.isEmpty()){
            return null;
        }

        for (Resource resource : resources) {
            resource_names.add(get_resource_name(resource));
        }
        return resource_names;
    }

//    @Override
//    public boolean add_resource(Resource resource) {
//        try {
//
//        }catch (Exception e){
//            e.printStackTrace();
//            return false;
//        }
//    }
//
//    @Override
//    public boolean add_resource(String resource, String base_url) {
//        return false;
//    }

    @Override
    public boolean remove_resource(Resource resource) {
        try {
            //删除模型中的资源
            // 删除与特定资源作为主语的所有三元组
            model.removeAll(resource, null, null);
            // 删除与特定资源作为宾语的所有三元组
            model.removeAll(null, null, resource);
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean remove_resource(String resource, String base_url) {
        Resource temp_resource = get_resource(resource, base_url);
        if (temp_resource == null){
            return false;
        }
        return remove_resource(temp_resource);
    }

    @Override
    public Property create_property(String property, String base_url) {
        //创建属性
        String property_url = String.format("%s#%s", base_url, property);
        return model.createProperty(property_url);
    }

    @Override
    public Property get_property(Resource resource, String property, String base_url) {
        if (resource == null || property == null || property.equals("")){
            return null;
        }
        //获取属性
        String property_url = String.format("%s#%s", base_url, property);
        Property temp_property = model.getProperty(property_url);
        if (temp_property == null){
            return null;
        }
        if (!model.contains(resource, temp_property)){
            return null;
        }
        return temp_property;
    }

    @Override
    public Property get_property_with_range(Resource resource, String property, String base_url) {
        if (resource == null || property == null || property.equals("")){
            return null;
        }
        //获取属性
        String property_url = String.format("%s#%s", base_url, property);
        Property temp_property = model.getProperty(property_url);
        if (temp_property == null){
            return null;
        }
        if (!model.contains(temp_property, RDFS.domain, resource)){
            return null;
        }
        return temp_property;
    }

    @Override
    public String get_property_name(Property property) {
        if (property == null){
            return null;
        }
        return property.getLocalName();
    }

    @Override
    public List<Property> get_all_property_from_resource(Resource resource) {
        List<Property> properties = new ArrayList<>();
        StmtIterator stmtIterator = resource.listProperties();
        while (stmtIterator.hasNext()) {
            Statement stmt = stmtIterator.next();
            Property property = stmt.getPredicate();
            if (property.getNameSpace() != null && !property.getNameSpace().isEmpty()) {
                properties.add(property);
            }
        }
        return properties;
    }

    @Override
    public List<String> get_all_property_name_from_resource(Resource resource) {
        List<String> property_names = new ArrayList<>();
        List<Property> properties = get_all_property_from_resource(resource);
        if (properties == null || properties.isEmpty()){
            return null;
        }
        for (Property property : properties) {
            property_names.add(get_property_name(property));
        }
        return property_names;
    }

    @Override
    public List<RDFNode> get_property_values(Resource resource, String property, String base_url) {
        // 获取属性
        Property temp_property = get_property(resource, property, base_url);
        if (temp_property == null){
            return null;
        }
        return get_property_values(resource, temp_property);
    }


    @Override
    public List<RDFNode> get_property_values(Resource resource, Property property) {
        // 检查输入参数是否为空
        if (resource == null || property == null) {
            throw new IllegalArgumentException("Resource or property cannot be null");
        }

        List<RDFNode> values = new ArrayList<>();

        try {
            // 获取所有匹配的属性值
            StmtIterator iterator = resource.listProperties(property);

            while (iterator.hasNext()) {
                Statement statement = iterator.next();
                values.add(statement.getObject());
            }

            if (values.isEmpty()) {
                return null;
            }

            return values;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    @Override
    public boolean add_property(Resource resource, Property property, String value) {
        try{
            resource.addProperty(property, value);
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean add_property(Resource resource, String property, String base_url, String value) {
        Property temp_property = get_property(resource, property, base_url);
        if (temp_property == null){
            return false;
        }
        return add_property(resource, temp_property, value);
    }

    @Override
    public boolean add_property(Resource resource, Property property, Resource resource2) {
        try {
            resource.addProperty(property, resource2);
            return true;
        }catch (Exception e){
            e.printStackTrace();
            System.out.println(resource+" "+property+" "+resource2);
            return false;
        }
    }

    @Override
    public boolean add_property(Resource resource, String property, String base_url, Resource resource2) {
        Property temp_property = get_property(resource, property, base_url);
        if (temp_property == null){
            return false;
        }
        return add_property(resource, temp_property, resource2);
    }

    @Override
    public boolean remove_property(Resource resource, Property property) {
        if (resource == null || property == null) {
            // 参数校验
            return false;
        }
        // 删除属性
        model.remove(resource, property, null);
        // 检查属性是否已删除
        return resource.getProperty(property) == null;
    }

    @Override
    public boolean remove_property(Resource resource, String property, String base_url) {
        Property temp_property = get_property(resource, property, base_url);
        return remove_property(resource, temp_property);
    }

    @Override
    public boolean remove_property_value(Resource resource, Property property, Resource resource2) {
        if (resource == null || property == null || resource2 == null) {
            // 参数校验
            return false;
        }

        // 删除指定的三元组 (resource, property, resource2)
        model.remove(resource, property, resource2);

        // 检查属性值是否已删除
        Statement stmt = model.createStatement(resource, property, resource2);
        return !model.contains(stmt);
    }

    @Override
    public boolean remove_property_value(Resource resource, String property, String base_url, Resource resource2) {
        Property temp_property = get_property(resource, property, base_url);
        return remove_property_value(resource, temp_property, resource2);
    }

    @Override
    public boolean remove_property_value(Resource resource, Property property, String value) {
        // 检查输入参数是否为空
        if (resource == null || property == null || value == null) {
            return false;
        }
        boolean removed = false;
        try {
            // 获取所有匹配的属性值
            StmtIterator iterator = resource.listProperties(property);
            while (iterator.hasNext()) {
                Statement statement = iterator.next();
                RDFNode object = statement.getObject();
                // 检查对象是否为 Literal 并且值匹配
                if (object.isLiteral() && value.equals(((Literal) object).getString())) {
                    model.remove(statement);
                    removed = true;
                }
            }
            return removed;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean remove_property_value(Resource resource, String property, String base_url, String value) {
        Property temp_property = get_property(resource, property, base_url);
        return remove_property_value(resource, temp_property, value);
    }

    @Override
    public List<Statement> get_all_stmt() {
        return model.listStatements().toList();
    }

    @Override
    public List<Statement> get_all_stmt_by_subject(Resource subject) {
        // 检查输入的主体是否为null，如果为null，则抛出异常
        if (subject == null) {
            throw new IllegalArgumentException("Subject cannot be null");
        }

        // 初始化一个空的陈述列表，用于存储找到的陈述
        List<Statement> statements = new ArrayList<>();
        // 声明一个声明迭代器，用于遍历模型中的陈述
        StmtIterator iterator = null;

        // 尝试块，用于处理声明的检索和迭代
        try {
            // 使用模型的listStatements方法获取与指定主体相关的所有陈述的迭代器
            iterator = model.listStatements(subject, null, (RDFNode) null);
            // 遍历迭代器，将每个陈述添加到陈述列表中
            while (iterator.hasNext()) {
                statements.add(iterator.next());
            }
        } catch (Exception e) {
            // 捕获在检索陈述过程中发生的任何异常，并重新抛出RuntimeException
            throw new RuntimeException("Failed to retrieve statements for subject: " + subject, e);
        } finally {
            // finally块，确保在所有情况下迭代器都被正确关闭
            if (iterator != null) {
                try {
                    // 关闭迭代器以释放资源
                    iterator.close();
                } catch (Exception e) {
                    // 记录在关闭迭代器过程中发生的任何异常
                    e.printStackTrace();
                }
            }
        }

        // 返回包含所有相关陈述的列表
        return statements;
    }


    @Override
    public List<Statement> get_all_stmt_by_object(Resource object) {
        // 检查输入对象是否为null，如果为null则抛出异常
        if (object == null) {
            throw new IllegalArgumentException("Object cannot be null");
        }

        // 初始化一个声明列表用于存储查询结果
        List<Statement> statements = new ArrayList<>();
        // 声明一个声明迭代器，用于遍历查询结果
        StmtIterator iterator = null;

        try {
            // 使用模型的listStatements方法获取所有以指定对象结束的声明迭代器
            iterator = model.listStatements(null, null, object);

            // 遍历迭代器，将每个声明添加到结果列表中
            while (iterator.hasNext()) {
                statements.add(iterator.next());
            }
        } catch (Exception e) {
            // 处理异常，例如记录日志或抛出自定义异常
            throw new RuntimeException("Failed to retrieve statements for object: " + object, e);
        }
        finally {
            // 确保迭代器被正确关闭以释放资源
            if (iterator != null) {
                try {
                    iterator.close();
                } catch (Exception e) {
                    // 记录关闭迭代器时的异常
                    e.printStackTrace();
                }
            }
        }
        // 返回结果列表
        return statements;
    }

    @Override
    public Resource find_resource_from_class_by_property_value(Resource resource_class, Property property, String value) {
        // 获取类和子类的所有实例
        List<Resource> allSubclasses = findAllSubclasses(model, resource_class);
        // 遍历所有类及其子类
        for (Resource subclass : allSubclasses) {
            ResIterator instances = model.listSubjectsWithProperty(RDF.type, subclass);
            // 遍历类的所有实例
            while (instances.hasNext()) {
                Resource instance = instances.next().asResource();
                // 获取实例属性的值
                StmtIterator propertyStatements = model.listStatements(instance, property, (RDFNode) null);
                while (propertyStatements.hasNext()) {
                    Statement statement = propertyStatements.next();
                    String propertyValue = statement.getObject().asLiteral().getString();
//                    System.out.println(propertyValue);
                    // 检查属性值是否匹配
                    if (propertyValue.equals(value)) {
                        return instance;
                    }
                }
            }
        }

        return null;
    }


    public List<Resource> findAllSubclasses(Model model, Resource superClass) {
        // 使用栈来存储待处理的类
        Stack<Resource> stack = new Stack<>();
        stack.push(superClass);
        // 使用集合来存储已经处理过的类，避免重复处理
        Set<Resource> visited = new HashSet<>();
        // 存储所有子类的列表
        List<Resource> subclasses = new ArrayList<>();
        while (!stack.isEmpty()) {
            Resource currentClass = stack.pop();
            if (visited.contains(currentClass)) {
                continue;
            }
            visited.add(currentClass);
            // 将当前类添加到子类列表中
            subclasses.add(currentClass);
            // 获取当前类的所有子类
            ResIterator subClasses = model.listSubjectsWithProperty(RDFS.subClassOf, currentClass);
            while (subClasses.hasNext()) {
                Resource subClass = subClasses.next();
                stack.push(subClass);
            }
        }
        return subclasses;
    }

    @Override
    public List<Resource> find_resource_by_name(String name) {
        return null;
    }

    @Override
    public List<Resource> find_resource_by_url(String url) {
        return null;
    }

    @Override
    public List<Property> find_property_by_name_from_resource(Resource resource, String name) {
        return null;
    }

    @Override
    public List<Property> find_property_by_url_from_resource(Resource resource, String url) {
        return null;
    }

    @Override
    public List<Statement> find_statement_by_subject_resource(Resource resource) {
        // 空值检查
        if (resource == null) {
            return Collections.emptyList(); // 返回空列表
            // 或者抛出自定义异常
            // throw new IllegalArgumentException("Resource cannot be null");
        }

        List<Statement> statements = new ArrayList<>();
        StmtIterator iterator = null;

        try {
            iterator = model.listStatements(resource, null, (RDFNode) null);
            while (iterator.hasNext()) {
                Statement stmt = iterator.nextStatement();
                statements.add(stmt);
            }
        } catch (Exception e) {
            // 异常处理
            e.printStackTrace();
            // 或者记录日志
            // logger.error("Error while listing statements", e);
        } finally {
            // 手动关闭 StmtIterator
            if (iterator != null) {
                iterator.close();
            }
        }

        return statements;
    }

    @Override
    public boolean is_resource_equal(Resource resource1, Resource resource2) {
        return false;
    }

}
