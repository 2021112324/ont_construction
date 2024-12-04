package org.example.property_method.impl;

import org.example.property_method.PropertyLoader;

import java.io.InputStream;
import java.util.Properties;

public class PropertyLoaderImpl implements PropertyLoader {
    private Properties properties;

    public PropertyLoaderImpl(String properties_filename) {
        properties = new Properties();
        try (InputStream inputStream = PropertyLoaderImpl.class.getClassLoader().getResourceAsStream(properties_filename)) {
            if (inputStream != null) {
                properties.load(inputStream);
            } else {
                throw new RuntimeException("无法找到 industrial_equipment.properties 文件");
            }
        } catch (Exception e) {
            throw new RuntimeException("读取 industrial_equipment.properties 文件时发生错误", e);
        }
    }

    @Override
    public String get_key_value(String key) {
        return properties.getProperty(key);
    }
}