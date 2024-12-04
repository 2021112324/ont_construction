package org.example.ont;

import org.apache.jena.rdf.model.*;

import java.util.List;

public interface OntMethod {
    /**
     * 读取本体文件并将其内容添加到模型中
     *
     * @param ont_filename 本体文件的路径
     * @param base_url 用于解析本体的基URL
     * @return 如果成功读取并加载本体文件，则返回true；如果文件未找到，则返回false
     */
    public boolean read_ont(String ont_filename, String base_url);

    /**
     * 将本体数据写入到指定文件中
     * 根据文件扩展名自动推断输出格式
     * 支持的格式包括：rdf, owl, ttl, jsonld, nt, n3
     * 如果文件类型不受支持，则会打印错误信息
     *
     * @param ont_filename 本体文件的路径和名称
     */
    public void write_ont(String ont_filename);

    /**
     * 打印模型信息到控制台
     * 此方法用于将模型的数据或结构信息输出到标准输出流（通常是控制台），以便于调试或记录模型状态
     * 它直接调用了model对象的write方法，并将System.out作为参数传递给它，这意味着模型信息将以文本形式被打印出来
     */
    public void print_ont();
    /**
     * 根据指定的格式打印模型数据到控制台
     *
     * @param type 输出格式类型，例如"turtle"、"rdf/xml"等
     */
    public void print_ont(String type);

    /**
     * 添加本体文件到模型中
     *
     * 本方法旨在将指定的本体文件添加到当前模型中它首先尝试打开并读取本体文件，
     * 如果文件不存在，则打印错误消息并返回false如果文件成功读取，它将本体数据
     * 与当前模型合并，以扩展当前模型的本体知识
     *
     * FIXME: model.union无法处理无命名空间的相同内容的资源的去重
     *
     * @param ont_filename 本体文件的路径和名称如果文件不在默认路径，需要提供完整路径
     * @param base_url 本体文件的基URL，用于解析相对URI
     * @return 如果本体文件成功添加到模型中，返回true；如果文件未找到，返回false
     */
    public boolean add_ont(String ont_filename, String base_url);
    /**
     * 合并两个模型对象
     *
     * 此方法的目的是将当前模型对象与另一个模型对象合并通过调用union方法来实现合并操作
     * 如果在合并过程中遇到异常，将会打印异常信息，但不会影响方法的继续执行
     *
     * @param model2 要与当前模型合并的模型对象
     * @return 总是返回true，表示合并操作已经尝试执行，无论成功与否
     */
    public boolean add_ont(Model model2);

    /**
     * 重写create_resource方法
     *
     * @param resource 资源名称，用于构成完整的资源URL
     * @param base_url 基础URL，资源URL的前缀部分
     * @return 创建的Resource对象
     *
     * 此方法根据给定的基础URL和资源名称，创建一个新的Resource对象
     * 它首先将基础URL和资源名称拼接成一个完整的资源URL，然后使用模型中的方法创建并返回这个资源
     */
    public Resource create_resource(String resource, String base_url);

    public Resource create_resource(String resource, String base_url, Resource type);
    /**
     * 重写get_resource方法，用于获取指定基础URL和资源名称的资源对象
     *
     * @param resource 资源名称，用于与基础URL组合以定位特定资源
     * @param base_url 基础URL，资源定位的起点URL
     * @return 返回组合URL对应的资源对象
     */
    public Resource get_resource(String resource, String base_url);
    /**
     * 获取资源的名称
     * 如果提供的资源对象为null，则返回null，否则返回资源的本地名称
     * 此方法主要用于简化资源名称的获取，确保代码的可读性和健壮性
     *
     * @param resource 资源对象，不应为null
     * @return 资源的本地名称，如果输入为null则返回null
     */
    public String get_resource_name(Resource resource);
    /**
     * 获取模型中的所有资源
     *
     * 此方法通过遍历模型中的所有主题（Subject），来收集并返回一个包含所有资源的列表
     * 它特别关注那些命名空间非空的资源，确保返回的资源列表中的每个资源都有一个有效的命名空间
     *
     * @return 包含所有符合命名空间非空条件的资源的列表
     */
    public List<Resource> get_all_resource();
    /**
     * 获取所有资源的名称列表
     *
     * 此方法旨在检索系统中所有资源的名称，通过调用get_all_resource方法获取资源列表，
     * 并从中提取每个资源的名称，如果资源列表为空或不存在，则返回null
     *
     * @return List<String> 包含所有资源名称的列表，如果资源列表为空或不存在，则返回null
     */
    public List<String> get_all_resource_name();
//    public boolean add_resource(Resource resource);
//    public boolean add_resource(String resource, String base_url);
    /**
     * 重写remove_resource方法以从模型中删除指定资源
     * 这个方法尝试删除模型中所有与指定资源相关的三元组，包括资源作为主语或宾语的情况
     *
     * @param resource 要从模型中删除的资源对象
     * @return 如果删除成功返回true，否则返回false
     */
    public boolean remove_resource(Resource resource);
    /**
     * 重写remove_resource方法，用于从指定的base_url中移除一个资源
     * 此方法首先通过get_resource方法找到需要移除的资源，如果找到则进一步移除
     *
     * @param resource 要移除的资源的标识符
     * @param base_url 资源所在的基URL
     * @return 如果资源成功移除则返回true，否则返回false
     */
    public boolean remove_resource(String resource, String base_url);


    /**
     * 重写create_property方法
     *
     * @param property 属性名称
     * @param base_url 基础URL，用于构建属性的完整URL
     * @return 创建的Property对象
     *
     * 此方法根据给定的属性名称和基础URL，创建并返回一个Property对象
     * 它首先通过格式化字符串将属性名称附加到基础URL上，以生成属性的完整URL
     * 然后使用这个URL通过model.createProperty方法创建并返回一个Property对象
     */
    public Property create_property(String property, String base_url);
    /**
     * 根据资源和属性名称获取属性
     *
     * 此方法用于从一个给定的资源中获取特定的属性它通过组合基础URL和属性名称来构造属性的URL，
     * 然后使用模型（model）的getProperty方法来获取属性对象
     *
     * @param resource 资源对象，代表一个数据源
     * @param property 属性名称，用于指定所需属性
     * @param base_url 基础URL，用于构造属性的完整URL
     * @return 返回一个Property对象，代表所获取的属性
     */
    public Property get_property(Resource resource, String property, String base_url);
    /**
     * 获取属性名称
     * 如果传入的属性对象为null，则返回null；否则返回属性的本地名称
     * 此方法主要用于简化属性名称的获取，提高代码可读性和维护性
     *
     * @param property The property object to get the name from Can be null
     * @return The local name of the property if the property is not null; otherwise returns null
     */
    public String get_property_name(Property property);
    /**
     * 从给定的资源中获取所有的属性
     * 此方法用于遍历与资源相关的所有属性，并将它们的谓词（属性）收集到一个列表中
     *
     * @param resource 资源对象，代表一个数据源或实体，从中提取属性
     * @return 返回一个包含资源所有属性的列表
     */
    public List<Property> get_all_property_from_resource(Resource resource);
    /**
     * 从给定的资源中获取所有属性名称
     * 此方法用于处理资源对象，提取出其中包含的所有属性名称
     *
     * @param resource 资源对象，从中提取属性名称
     * @return 包含所有属性名称的列表如果资源中没有属性或资源为空，则返回null
     */
    public List<String> get_all_property_name_from_resource(Resource resource);
    /**
     * 获取指定资源的属性值列表
     *
     * @param resource 资源对象，代表一个RDF资源
     * @param property 属性字符串，表示要查询的属性
     * @param base_url 基础URL字符串，用于解析属性
     * @return 属性值列表，如果属性不存在则返回null
     */
    public List<RDFNode> get_property_values(Resource resource, String property, String base_url);
    /**
     * 获取资源的指定属性值列表
     *
     * @param resource 资源对象，代表一个RDF资源
     * @param property 属性对象，代表一个RDF属性
     * @return 包含指定属性值的列表如果找不到任何属性值，则返回null
     * @throws IllegalArgumentException 如果输入的资源或属性为null，则抛出此异常
     */
    public List<RDFNode> get_property_values(Resource resource, Property property);
    /**
     * 向资源对象中添加一个新的属性
     *
     * @param resource 要添加属性的资源对象
     * @param property 要添加的属性对象
     * @param value 属性的值
     * @return 如果属性成功添加，返回true；如果发生异常，返回false
     */
    public boolean add_property(Resource resource, Property property, String value);
    /**
     * 添加属性值到指定资源中
     * 此方法首先尝试根据提供的资源、属性名和基础URL获取一个属性对象如果该属性对象不存在，则方法返回false，
     * 表示添加属性值失败如果属性对象存在，则进一步尝试将属性值添加到该资源中
     *
     * @param resource 要添加属性的资源对象，不能为空
     * @param property 要添加的属性名，不能为空
     * @param base_url 属性的基础URL，用于定位属性，不能为空
     * @param value 要添加的属性值，不能为空
     * @return 如果成功添加属性值，则返回true；否则返回false
     */
    public boolean add_property(Resource resource, String property, String base_url, String value);
    /**
     * 向资源对象中添加属性
     *
     * 此方法用于在资源对象中添加一个属性及其对应的资源值
     * 它通过调用Resource对象的addProperty方法来实现属性的添加
     * 如果添加成功，返回true；如果在添加过程中发生异常，打印异常信息并返回false
     *
     * @param resource 要添加属性的资源对象
     * @param property 要添加的属性对象
     * @param resource2 与属性关联的资源值对象
     * @return 添加属性成功返回true，否则返回false
     */
    public boolean add_property(Resource resource, Property property, Resource resource2);
    /**
     * 添加属性到资源
     *
     * 此方法用于将一个属性添加到特定资源中它首先尝试根据提供的参数获取一个属性对象如果该属性对象不存在，
     * 则方法返回false，表示添加失败如果属性对象存在，则将其添加到指定的资源中
     *
     * @param resource 要添加属性的资源
     * @param property 要添加的属性名称
     * @param base_url 属性的基础URL，用于定位属性
     * @param resource2 与属性关联的另一个资源，具体关联方式依赖于具体实现
     * @return 如果属性成功添加到资源中，返回true；否则返回false
     */
    public boolean add_property(Resource resource, String property, String base_url, Resource resource2);
    /**
     * 从资源中删除指定属性
     *
     * @param resource 要从中删除属性的资源对象，不可为null
     * @param property 要删除的属性对象，不可为null
     * @return 如果删除成功返回true，否则返回false
     *
     * 此方法首先校验输入参数是否合法（即不为null），然后通过调用model的remove方法来删除资源的指定属性
     * 最后，通过检查资源的指定属性是否已为空来验证删除操作是否成功
     */
    public boolean remove_property(Resource resource, Property property);
    /**
     * 从资源中移除指定的属性
     * 此方法首先通过调用get_property方法获取资源的当前属性，然后将其移除
     *
     * @param resource 要从中移除属性的资源对象
     * @param property 要移除的属性名称
     * @param base_url 属性的基础URL，用于定位或识别属性
     * @return 如果属性成功移除，则返回true；否则返回false
     */
    public boolean remove_property(Resource resource, String property, String base_url);
    /**
     * 从模型中移除指定的属性值
     *
     * @param resource    主体资源，表示三元组中的主体
     * @param property    属性，表示三元组中的谓语
     * @param resource2   客体资源，表示三元组中的宾语
     * @return            如果成功移除属性值，则返回true；否则返回false
     */
    public boolean remove_property_value(Resource resource, Property property, Resource resource2);
    /**
     * 从一个资源中移除特定属性的值
     * 此方法首先通过资源、属性名和基础URL获取一个属性对象，然后从该资源中移除这个属性及其值
     *
     * @param resource  要从中移除属性值的资源对象
     * @param property  要移除的属性名
     * @param base_url  用于解析属性的基础URL
     * @param resource2 与要移除属性相关的另一个资源对象
     * @return 如果成功移除属性值，则返回true；否则返回false
     */
    public boolean remove_property_value(Resource resource, String property, String base_url, Resource resource2);
    /**
     * 重写 remove_property_value 方法
     *
     * @param resource 资源对象，代表一个数据资源
     * @param property 属性对象，代表资源的一个属性
     * @param value 要移除的属性值
     * @return 如果成功移除属性值，则返回 true；否则返回 false
     */
    public boolean remove_property_value(Resource resource, Property property, String value);
    /**
     * 移除资源的特定属性值
     * 此方法首先根据资源、属性名和基础URL获取对应的属性对象，然后移除该属性中的指定值
     *
     * @param resource 资源对象，表示一个数据源或实体
     * @param property 属性名，指定要移除值的属性
     * @param base_url 基础URL，用于解析属性
     * @param value 要移除的属性值
     * @return 如果成功移除属性值，则返回true；否则返回false
     */
    public boolean remove_property_value(Resource resource, String property, String base_url, String value);

    /**
     * 获取模型中的所有声明
     *
     * 此方法返回模型中的所有声明，以列表的形式组织这些声明有助于进一步的处理和分析
     *
     * @return 包含模型中所有声明的列表
     */
    public List<Statement> get_all_stmt();
    /**
     * 根据指定的资源主体获取所有相关的陈述
     * 此方法用于从RDF模型中检索与特定资源主体相关的所有陈述
     * 它通过遍历模型中的陈述来构建一个包含所有相关陈述的列表
     *
     * @param subject 资源主体，不能为null如果为null，则抛出IllegalArgumentException异常
     * @return 包含与指定资源主体相关的所有陈述的列表
     * @throws RuntimeException 如果在检索陈述过程中发生任何异常，则抛出此异常
     */
    public List<Statement> get_all_stmt_by_subject(Resource subject);
    /**
     * 根据对象获取所有相关的声明
     * 该方法用于从RDF模型中获取所有以指定对象结束的声明
     * 主要用于需要根据对象来查询所有相关声明的场景
     *
     * @param object Resource类型，表示查询的声明对象，不能为null
     * @return 返回一个Statement列表，包含所有以指定对象结束的声明
     * @throws IllegalArgumentException 如果输入的对象为null，则抛出此异常
     * @throws RuntimeException 如果获取声明过程中发生错误，则抛出此异常
     */
    public List<Statement> get_all_stmt_by_object(Resource object);

    public Resource find_resource_from_class_by_property_value(Resource resource_class, Property property, String value);


    public List<Resource> findAllSubclasses(Model model, Resource superClass);

    public List<Resource> find_resource_by_name(String name);
    public List<Resource> find_resource_by_url(String url);
    public List<Property> find_property_by_name_from_resource(Resource resource, String name);
    public List<Property> find_property_by_url_from_resource(Resource resource, String url);
    public List<Statement> find_statement_by_subject_resource(Resource resource);

    public boolean is_resource_equal(Resource resource1, Resource resource2);
}
