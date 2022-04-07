package edu.internet2.middleware.grouper.authentication.plugin;

import edu.internet2.middleware.grouper.cfg.GrouperHibernateConfig;
import edu.internet2.middleware.grouperClient.config.ConfigPropertiesCascadeBase;
import org.pac4j.core.client.config.BaseClientConfiguration;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.time.Period;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ConfigUtils {
    final static ResourceLoader resourceLoader = new DefaultResourceLoader();

    public static ConfigPropertiesCascadeBase getBestGrouperConfiguration() {
        if (isGrouperUi()) {
            return getConfigPropertiesCascadeBase("ui");
        } else if (isGrouperWs()) {
            return getConfigPropertiesCascadeBase("ws");
        } else if (isGrouperDaemon()) {
            return getConfigPropertiesCascadeBase("daemon");
        } else {
            throw new RuntimeException("no appropriate configuration found");
        }
    }

    public static ConfigPropertiesCascadeBase getConfigPropertiesCascadeBase(String type) {
        try {
            switch (type) {
                case "ui":
                    return (ConfigPropertiesCascadeBase) Class.forName("edu.internet2.middleware.grouper.ui.util.GrouperUiConfigInApi").getMethod("retrieveConfig").invoke(null);
                case "ws":
                    return (ConfigPropertiesCascadeBase) Class.forName("edu.internet2.middleware.grouper.ws.GrouperWsConfigInApi").getMethod("retrieveConfig").invoke(null);
                case "daemon":
                    return (ConfigPropertiesCascadeBase) Class.forName("edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig").getMethod("retrieveConfig").invoke(null);
                default:
                    throw new RuntimeException("no appropriate type found");
            }
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e ) {
            throw new RuntimeException(e);
        }
    }

    public static void setProperties(BaseClientConfiguration configuration, String authMechanism) {
        ConfigPropertiesCascadeBase grouperConfig = getBestGrouperConfiguration();

        Class<?> clazz = configuration.getClass();
        for (String name : grouperConfig.propertyNames()) {
            if (name.startsWith("external.authentication." + authMechanism)) {
                try {
                    String fieldName = name.substring(name.lastIndexOf('.') + 1);
                    Field field = getField(clazz, fieldName);

                    //TODO: prefer setters

                    field.setAccessible(true);
                    field.set(configuration, getProperty(grouperConfig, field.getType(), name));
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

    private static Object getProperty(ConfigPropertiesCascadeBase configPropertiesCascadeBase, Type type, String propName) {
        switch (type.getTypeName()) {
            case "java.lang.String" : {
                return configPropertiesCascadeBase.propertyValueString(propName);
            }
            case "int" :
            case "java.lang.Integer" : {
                return configPropertiesCascadeBase.propertyValueInt(propName);
            }
            case "long" :
            case "java.lang.Long" : {
                return Long.parseLong(configPropertiesCascadeBase.propertyValueString(propName));
            }
            case "double" :
            case "java.lang.Double" : {
                return Double.parseDouble(configPropertiesCascadeBase.propertyValueString(propName));
            }
            case "boolean" :
            case "java.lang.Boolean" : {
                return configPropertiesCascadeBase.propertyValueBoolean(propName);
            }
            case "java.util.List" :
            case "java.util.Collection" :{
                return Arrays.asList(configPropertiesCascadeBase.propertyValueString(propName).split(","));
            }
            case "java.util.Set" : {
                Set set = new HashSet();
                for (String prop : configPropertiesCascadeBase.propertyValueString(propName).split(",")) {
                    set.add(prop);
                }
                return set;
            }
            case "java.util.Map" : {
                Map<String, String> map = new HashMap();
                for (String pairs : configPropertiesCascadeBase.propertyValueString(propName).split(",")) {
                    String [] keyValue = pairs.split("=");
                    map.put(keyValue[0].trim(),keyValue[1].trim());
                }
                return map;
            }
            case "java.time.Period" : {
                return Period.parse(configPropertiesCascadeBase.propertyValueString(propName));
            }
            case "org.springframework.core.io.WritableResource":
            case "org.springframework.core.io.Resource": {
                return resourceLoader.getResource(configPropertiesCascadeBase.propertyValueString(propName));
            }
            default:
                throw new IllegalStateException("Unexpected type: " + type.getTypeName());
        }
    }

    public static boolean isGrouperUi() {
        return GrouperHibernateConfig.retrieveConfig().propertyValueBoolean("grouper.is.ui", false);
    }

    public static boolean isGrouperWs() {
        return GrouperHibernateConfig.retrieveConfig().propertyValueBoolean("grouper.is.ws", false);
    }

    public static boolean isGrouperDaemon() {
        return GrouperHibernateConfig.retrieveConfig().propertyValueBoolean("grouper.is.daemon", false);
    }
}