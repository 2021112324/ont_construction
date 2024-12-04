package org.example.person;

import org.apache.jena.rdf.model.Statement;
import org.example.person.method.impl.Person_ont_impl;
import org.example.person.method.Person_ont;

import java.util.List;

public class main {
    public static void main(String[] args) {
        Person_ont person_ont = new Person_ont_impl();
        person_ont.read_person_ont_string_map("data/test.txt");
        person_ont.print_ont("N3");
//        person_ont.write_ont("rdf/vc-db-5.rdf");

//        person_ont.read_ont("rdf/vc-db-2.rdf","");
////        person_ont.add_ont("rdf/vc-db-2.rdf","");
//        person_ont.add_ont("rdf/vc-db-3.rdf","");
////        person_ont.read_ont("rdf/vc-db-3.rdf","");
//        person_ont.add_ont("rdf/vc-db-4.rdf","");
//        person_ont.print_ont("N3");
//        List<Statement> statements = person_ont.get_all_stmt();
//        for (Statement statement : statements){
//            System.out.println(statement);
//        }
//        List<String> strings = person_ont.write_person_ont_string("data/test.txt");
//        for (String string : strings){
//            System.out.println(string);
//        }

//        person_ont.write_person_ont_string();
//        person_ont.print_ont();
//        List<Statement> statements = person_ont.get_all_stmt();
//        for (Statement statement : statements){
//            System.out.println(statement);
////            System.out.println(statement.getSubject().getLocalName() + " " + statement.getPredicate().getLocalName() + " " + statement.getObject().toString() + " .");
//        }
    }
}
