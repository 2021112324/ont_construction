package org.example.Ind_equ.method.ont.impl;

import org.apache.jena.rdf.model.*;
import org.example.property_method.PropertyLoader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;

class Ind_equOntImplTest {
    @Mock
    private Model model;

    @Mock
    private PropertyLoader property_loader;

    @InjectMocks
    private IndEquOntImpl ind_equOntImpl;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void is_product_exits_ProductExists() {

    }

    @Test
    void is_product_exits_ProductDoesNotExist() {

    }

    @org.junit.jupiter.api.Test
    void init_method() {
    }

    @org.junit.jupiter.api.Test
    void read_ont() {
    }

    @org.junit.jupiter.api.Test
    void add_ont() {
    }

    @org.junit.jupiter.api.Test
    void write_ont() {
    }

    @org.junit.jupiter.api.Test
    void add_ont_from_csv() {
    }

    @org.junit.jupiter.api.Test
    void add_ont_from_datafile() {
    }

    @org.junit.jupiter.api.Test
    void add_ont_from_data_dir() {
    }

    @org.junit.jupiter.api.Test
    void add_ont_from_production_csv() {
    }

    @org.junit.jupiter.api.Test
    void generate_resource_url_hashcode() {
    }

    @org.junit.jupiter.api.Test
    void create_classification_resource() {
    }

    @org.junit.jupiter.api.Test
    void is_product_exits() {
    }

    @org.junit.jupiter.api.Test
    void add_subclass_relation() {
    }

    @org.junit.jupiter.api.Test
    void add_brand_relation() {
    }

    @org.junit.jupiter.api.Test
    void add_product_company_relation() {
    }

    @org.junit.jupiter.api.Test
    void add_brand_company_relation() {
    }
}