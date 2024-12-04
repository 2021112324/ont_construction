package org.example.Ind_equ.method.ont;

import org.apache.jena.rdf.model.Resource;
import org.example.ont.OntMethod;

import java.util.List;

public interface IndEquOnt extends OntMethod {
    boolean init_method();

    /*
    *
    * 建立本体
    *
    */
    @Override
    boolean read_ont(String ont_filename, String base_url);
    @Override
    boolean add_ont(String ont_filename, String base_url);


    /*
    *
    * 输出本体
    *
    */
    @Override
    void write_ont(String ont_filename);

    /*
    *
    * 添加本体
    *
    */
    public boolean add_ont_from_csv(String filename, String type);
    public boolean add_ont_from_datafile(String filename);
    public boolean add_ont_from_data_dir(String data_dir);

    public boolean add_ont_from_production_csv(String filename);


    public Resource create_classification_resource(Resource resource,String classifications);

    public Resource find_product(String product_name);
    public Resource find_brand(String brand_name);
    public Resource find_company(String company_name);
    public Resource find_classification(String classification);


    /*
    *
    * 添加资源间关系
    *
    */
    // 资源A是资源B的子类
    public boolean add_subclass_relation(Resource resourceA, Resource resourceB);
    // 资源A的品牌是资源B
    public boolean add_brand_relation(Resource resourceA, Resource resourceB);
    // 资源A的产品公司是资源B
    public boolean add_product_company_relation(Resource resourceA, Resource resourceB);
    // 资源A的品牌公司是资源B
    public boolean add_brand_company_relation(Resource resourceA, Resource resourceB);

    boolean add_equip_relation(Resource product, Resource classification);

    boolean add_applied_relation(Resource product, Resource classification);

    public List<String> find_all_classification();
    boolean create_equip_relation(Resource product);

    boolean create_all_equip_relation();

    List<String> find_all_product_name();

//    public List<String> write_person_ont_string(String filename);
//
//    public void write_stmt_string(Resource object, List<String> strings);
//
//    public void read_person_ont_string_map(String filename);
}
