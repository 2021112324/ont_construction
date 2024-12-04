package org.example.person.method.impl;

import org.apache.jena.rdf.model.*;
import org.example.ont.impl.OntMethodImpl;
import org.example.person.method.Person_ont;
import org.example.property_method.PropertyLoader;
import org.example.property_method.impl.PropertyLoaderImpl;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Person_ont_impl extends OntMethodImpl implements Person_ont {
    private List<Statement> model_stmts = new ArrayList<>();
    private Map<Statement, Boolean> model_stmts_map = new HashMap<>();

    public Map<Statement, Boolean> getModel_stmts_map() {
        return model_stmts_map;
    }

    public void setModel_stmts_map() {
        setModel_stmts();
        List<Statement> stmts = getModel_stmts();
        for (Statement stmt : stmts) {
            this.model_stmts_map.put(stmt, true);
        }
    }

    public List<Statement> getModel_stmts() {
        return model_stmts;
    }

    public void setModel_stmts() {
        this.model_stmts = get_all_stmt();
    }

    private final PropertyLoader property_loader = new PropertyLoaderImpl("person.properties");
    private final String person_url = property_loader.get_key_value("PERSON_URL");
    private final String ont_root = property_loader.get_key_value("ONT_ROOT");
    public Person_ont_impl() {
        super();
    }
    public Person_ont_impl(Model model) {
        super(model);
    }
    public String getPersonUrl() {
        return person_url;
    }
    public String getRdfRoot() {
        return ont_root;
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
    public void write_ont(String ont_filename) {
        ont_filename = ont_root + ont_filename;
        super.write_ont(ont_filename);
    }

    @Override
    public List<String> write_person_ont_string(String filename) {
        setModel_stmts_map();
        List<String> strings = new ArrayList<>();

        // 使用 entrySet 迭代器遍历 map
        Iterator<Map.Entry<Statement, Boolean>> iterator = model_stmts_map.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Statement, Boolean> entry = iterator.next();
            Statement stmt = entry.getKey();
            Boolean value = entry.getValue();
            Resource subject = stmt.getSubject();

            // 检查主语资源是否有命名空间
            if (subject.getNameSpace() != null) {
                String subject_name = subject.getLocalName();
                String property_name = get_property_name(stmt.getPredicate());

                // 如果三元组的宾语是 Resource 类型，则打印资源；如果为 Literal 类型，则打印值
                if (stmt.getObject().isResource()) {
                    Resource object = stmt.getObject().asResource();
                    strings.add(subject_name + "的" + property_name + "包括：(");
                    // 从 stmts 列表中删除该三元组
                    model_stmts_map.replace(stmt, false);
                    write_stmt_string(object, strings);
                    strings.add(")");
                } else {
                    Literal object = stmt.getObject().asLiteral();
                    String object_value = object.getValue().toString();
                    strings.add(subject_name + "的" + property_name + "是" + object_value);
                    // 从 stmts 列表中删除该三元组
                    model_stmts_map.replace(stmt, false);
                }
                // 输出该三元组
            }
        }

        // 将strings输出到文件中
        try (FileWriter writer = new FileWriter(filename)) {
            for (String string : strings) {
                writer.write(string + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return strings;
    }

    @Override
    public void write_stmt_string(Resource object, List<String> strings) {
        // 空值检查
        if (object == null || strings == null) {
            throw new IllegalArgumentException("Resource and strings list cannot be null");
        }

        Iterator<Map.Entry<Statement, Boolean>> iterator = model_stmts_map.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Statement, Boolean> entry = iterator.next();
            Statement stmt = entry.getKey();
            Boolean value = entry.getValue();
//            // 如果三元组stmt的主语是 object，则将其输出
            if (stmt.getSubject().equals(object) && value) {
                Resource subject = stmt.getSubject();
                String subject_name = subject.getLocalName();
                String property_name = get_property_name(stmt.getPredicate());

                // 如果三元组的宾语是 Resource 类型，则打印资源；如果为 Literal 类型，则打印值
                if (stmt.getObject().isResource()) {
                    Resource object2 = stmt.getObject().asResource();
                    strings.add(property_name + "包括：(");
                    // 从 stmts 列表中删除该三元组
                    model_stmts_map.replace(stmt, false);
                    write_stmt_string(object, strings);
                    strings.add(")");
                } else {
                    Literal object2 = stmt.getObject().asLiteral();
                    String object_value = object2.getValue().toString();
                    strings.add(property_name + ",其值为" + object_value);
                    // 从 stmts 列表中删除该三元组
                    model_stmts_map.replace(stmt, false);
                }
            }
        }
    }

    @Override
    public void read_person_ont_string_map(String filename) {
        // 按行读入文件，将每行字符串保存到 strings 列表中
        List<String> strings = new ArrayList<>();
        Stack<Resource> stack = new Stack<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                strings.add(line);
            }
            for (String string : strings) {
//                System.out.println(string);
                //无名资源出栈
                if (string.equals(")")) {
                    if (!stack.empty()){
                        Resource resource = stack.pop();
//                        System.out.println("出栈");
//                        continue;
                    }
//                    System.out.println("栈为空");
                    continue;
                }

                //主谓宾，宾语为资源
                Pattern pattern1 = Pattern.compile("(.*?)的(.*?)包括：\\(");
                Matcher matcher1 = pattern1.matcher(string);
                if (matcher1.find()) {
                    // 获取三元组
                    Resource subject = get_resource(matcher1.group(1), person_url);
                    if (subject == null){
                        subject = create_resource(matcher1.group(1), person_url);
                    }
                    Resource object = model.createResource();
                    Property property = create_property(matcher1.group(2), person_url);

                    //将宾语资源压栈
                    add_property(subject, property, object);
                    stack.push(object);
//                    System.out.println("入栈");
//                    System.out.println(matcher1.group(1) + "有" + matcher1.group(2));
                    continue;
                }

                //主语省略，无名资源
                Pattern pattern2 = Pattern.compile("(.*?),其值为(.*)");
                Matcher matcher2 = pattern2.matcher(string);
                if (matcher2.find()) {
                    // 从栈顶获取主语资源
                    Resource subject = stack.peek();
                    if (subject == null){
                        System.out.println("栈为空");
                    }
                    Property property = create_property(matcher2.group(1), person_url);
                    add_property(subject, property, matcher2.group(2));
//                    System.out.println(matcher2.group(0));
//                    System.out.println(matcher2.group(1) + "是" + matcher2.group(2));
                    continue;
                }

                // 主语省略，无名资源，宾语为资源
                Pattern pattern3 = Pattern.compile("(.*?)包括：\\(");
                Matcher matcher3 = pattern3.matcher(string);
                if (matcher3.find()) {
                    //从栈顶获取主语资源
                    Resource subject = stack.peek();
                    if (subject == null){
                        System.out.println("栈为空");
                    }
                    Resource object = model.createResource();
                    Property property = create_property(matcher3.group(1), person_url);
                    add_property(subject, property, object);
                    stack.push(object);
//                    System.out.println("入栈");
//                    System.out.println(matcher3.group(1));
                    continue;
                }

                //主谓宾
                Pattern pattern4 = Pattern.compile("(.*?)的(.*?)是(.*)");
                Matcher matcher4 = pattern4.matcher(string);
                if (matcher4.find()) {
                    // 获取三元组
                    Resource subject = get_resource(matcher4.group(1), person_url);
                    if (subject == null){
                        subject = create_resource(matcher4.group(1), person_url);
                    }
                    Property property = create_property(matcher4.group(2), person_url);
                    add_property(subject, property, matcher4.group(3));
//                    System.out.println(matcher4.group(0));
                    continue;
                }
            }
            model.write(System.out);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
