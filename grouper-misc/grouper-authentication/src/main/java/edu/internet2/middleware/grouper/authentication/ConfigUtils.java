package edu.internet2.middleware.grouper.authentication;

import edu.internet2.middleware.grouper.ui.util.GrouperUiConfig;
import org.pac4j.core.client.config.BaseClientConfiguration;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.time.Period;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ConfigUtils {
    public static void setProperties(BaseClientConfiguration configuration, String authMechanism) {
        Class<?> clazz = configuration.getClass();
        for (String name : GrouperUiConfig.retrieveConfig().propertyNames()) {
            if (name.startsWith("external.authentication." + authMechanism)) {
                try {
                    String fieldName = name.substring(name.lastIndexOf('.') + 1);
                    Field field = getField(clazz, fieldName);

                    //TODO: prefer setters

                    field.setAccessible(true);
                    field.set(configuration, getProperty(field.getType(), name));
                } catch (NoSuchFieldException e) {
                    throw new IllegalStateException("Unexpected property name: " + name);
                } catch (IllegalAccessException e) {
                    throw new IllegalStateException("Unable to access property name: " + name);
                }
            }
        }
    }

    private static Field getField(Class clazz, String name) throws NoSuchFieldException {
        try {
            return clazz.getDeclaredField(name);
        } catch (NoSuchFieldException e) {
            if (clazz.equals(Object.class)) {
                throw new NoSuchFieldException(name);
            }
            return getField(clazz.getSuperclass(), name);
        }
    }

    private static Object getProperty(Type type, String propName) {
        switch (type.getTypeName()) {
            case "java.lang.String" : {
                return GrouperUiConfig.retrieveConfig().propertyValueString(propName);
            }
            case "int" :
            case "java.lang.Integer" : {
                return GrouperUiConfig.retrieveConfig().propertyValueInt(propName);
            }
            case "long" :
            case "java.lang.Long" : {
                return Long.parseLong(GrouperUiConfig.retrieveConfig().propertyValueString(propName));
            }
            case "double" :
            case "java.lang.Double" : {
                return Double.parseDouble(GrouperUiConfig.retrieveConfig().propertyValueString(propName));
            }
            case "boolean" :
            case "java.lang.Boolean" : {
                return GrouperUiConfig.retrieveConfig().propertyValueBoolean(propName);
            }
            case "java.util.List" :
            case "java.util.Collection" :{
                return Arrays.asList(GrouperUiConfig.retrieveConfig().propertyValueString(propName).split(","));
            }
            case "java.util.Set" : {
                Set set = new HashSet();
                for (String prop : GrouperUiConfig.retrieveConfig().propertyValueString(propName).split(",")) {
                    set.add(prop);
                }
                return set;
            }
            case "java.util.Map" : {
                Map<String, String> map = new HashMap();
                for (String pairs : GrouperUiConfig.retrieveConfig().propertyValueString(propName).split(",")) {
                    String [] keyValue = pairs.split("=");
                    map.put(keyValue[0].trim(),keyValue[1].trim());
                }
                return map;
            }
            case "java.time.Period" : {
                return Period.parse(GrouperUiConfig.retrieveConfig().propertyValueString(propName));
            }
            default:
                throw new IllegalStateException("Unexpected type: " + type.getTypeName());
        }
    }
}
