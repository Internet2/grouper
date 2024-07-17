package edu.internet2.middleware.grouper.authentication.plugin;

import edu.internet2.middleware.grouperClient.config.ConfigPropertiesCascadeBase;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.pac4j.core.client.config.BaseClientConfiguration;
import org.pac4j.core.context.HttpConstants;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.UrlResource;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Period;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.List;

public class ConfigUtils {
    private static final Map<String, String> PROPERTY_RENAMES = new HashMap<>();
    static {
        PROPERTY_RENAMES.put("external.authentication.saml.keyStoreAlias", "external.authentication.saml.keystoreAlias");
        PROPERTY_RENAMES.put("external.authentication.saml.keyStoreType", "external.authentication.saml.keystoreType");
    }

    private final static ResourceLoader resourceLoader = new DefaultResourceLoader();

    private final static BundleContext bundleContext = FrameworkUtil.getBundle(GrouperAuthentication.class).getBundleContext();

    private final static Log LOG = GrouperAuthentication.getLogFactory().getInstance(ConfigUtils.class);

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
            List<ServiceReference<ConfigPropertiesCascadeBase>> serviceReferenceList = (List<ServiceReference<ConfigPropertiesCascadeBase>>) bundleContext.getServiceReferences(ConfigPropertiesCascadeBase.class, "(type=" + type + ")");
            ServiceReference<?> serviceReference = serviceReferenceList.get(0);
            return (ConfigPropertiesCascadeBase) bundleContext.getService(serviceReference);
        } catch (InvalidSyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public static void setProperties(BaseClientConfiguration configuration, String authMechanism) {
        checkConfig();
        ConfigPropertiesCascadeBase grouperConfig = getBestGrouperConfiguration();

        Class<?> clazz = configuration.getClass();
        for (String name : grouperConfig.propertyNames()
                .stream()
                .filter( p -> p.startsWith("external.authentication." + authMechanism))
                .toList()) {
            // map name to realname if needed (e.g., changing case)
            String realName = ConfigUtils.propertyNameRename(name);
            String fieldName = realName.substring(name.lastIndexOf('.') + 1);
            try {
                Method method = getSetter(clazz, getMethodNameFromFieldName(fieldName));
                method.invoke(configuration, getProperty(grouperConfig, method.getParameterTypes()[0], name));
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException |
                     ClassNotFoundException | MalformedURLException e) {
                throw new RuntimeException("could not set " + fieldName, e);
            }
        }
    }

    /**
     * method to check configuration from various states
     */
    private static void checkConfig() {
        ConfigPropertiesCascadeBase grouperConfig = getBestGrouperConfiguration();

        // check renames
        for (Map.Entry<String, String> rename: PROPERTY_RENAMES.entrySet()) {
            if (grouperConfig.containsKey(rename.getKey())) {
                LOG.warn("you are using the config key `" + rename.getKey() + "`; this should be changed to `" + rename.getValue() + "`");
            }
        }
    }

    /**
     * method to rewrite property names in the case of deprecation, etc
     *
     * @param propertyName
     * @return
     */
    private static String propertyNameRename(String propertyName) {
        return PROPERTY_RENAMES.getOrDefault(propertyName, propertyName);
    }

    private static String getMethodNameFromFieldName(String fieldName) {
        return "set" + StringUtils.capitalize(fieldName);
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

    private static Method getSetter(Class clazz, String name) throws NoSuchMethodException {
        //TODO: this is dangerous. currently there are no overloaded methods, but there could be in the future. need to decide best way to handle this (parameter type precedence?)
        return Arrays.stream(clazz.getMethods())
                .filter(m -> m.getName().equals(name))
                .findFirst()
                .orElseThrow(NoSuchMethodException::new);
    }

    private static Object getProperty(ConfigPropertiesCascadeBase configPropertiesCascadeBase, Type type, String propName) throws ClassNotFoundException, MalformedURLException {
        if (Enum.class.isAssignableFrom((Class<?>) type)) {
            // there are a few properties that are enums (e.g., CAS protocol)
            // TODO: can this be checked?
            @SuppressWarnings("unchecked")
            Enum<?> e = Enum.valueOf((Class) type, configPropertiesCascadeBase.propertyValueString(propName));
            return e;
        } else {
            switch (type.getTypeName()) {
                case "java.lang.String": {
                    return configPropertiesCascadeBase.propertyValueString(propName);
                }
                case "int":
                case "java.lang.Integer": {
                    return configPropertiesCascadeBase.propertyValueInt(propName);
                }
                case "long":
                case "java.lang.Long": {
                    return Long.parseLong(configPropertiesCascadeBase.propertyValueString(propName));
                }
                case "double":
                case "java.lang.Double": {
                    return Double.parseDouble(configPropertiesCascadeBase.propertyValueString(propName));
                }
                case "boolean":
                case "java.lang.Boolean": {
                    return configPropertiesCascadeBase.propertyValueBoolean(propName);
                }
                case "java.util.List":
                case "java.util.Collection": {
                    return Arrays.asList(configPropertiesCascadeBase.propertyValueString(propName).split(","));
                }
                case "java.util.Set": {
                    //TODO: hopefully string is all that is needed, but might need to revisit
                    Set<String> set = new HashSet<>();
                    Collections.addAll(set, configPropertiesCascadeBase.propertyValueString(propName).split(","));
                    return set;
                }
                case "java.util.Map": {
                    Map<String, String> map = new HashMap<>();
                    for (String pairs : configPropertiesCascadeBase.propertyValueString(propName).split(",")) {
                        String[] keyValue = pairs.split("=");
                        map.put(keyValue[0].trim(), keyValue[1].trim());
                    }
                    return map;
                }
                case "java.time.Period": {
                    return Period.parse(configPropertiesCascadeBase.propertyValueString(propName));
                }
                case "org.springframework.core.io.WritableResource": {
                    return resourceLoader.getResource(configPropertiesCascadeBase.propertyValueString(propName));
                }
                case "org.springframework.core.io.Resource": {
                    /* Spring's getResource treats file:* as a FileUrlResource, while Pac4j expects a FileSystemResource,
                     * and treats any UrlResource as http which causes failure. It is recommended to use the *Path
                     * properties instead of the *Resource ones, since that uses the Pac4j logic to construct the correct
                     * resource type.
                     */
                    //return resourceLoader.getResource(configPropertiesCascadeBase.propertyValueString(propName));

                    String path = configPropertiesCascadeBase.propertyValueString(propName);
                    if (path.startsWith("resource:")) {
                        return new ClassPathResource(path.substring("resource:".length()));
                    }
                    if (path.startsWith("classpath:")) {
                        return new ClassPathResource(path.substring("classpath:".length()));
                    }
                    if (path.startsWith(HttpConstants.SCHEME_HTTP) || path.startsWith(HttpConstants.SCHEME_HTTPS)) {
                        return new UrlResource(new URL(path));
                    }
                    if (path.startsWith("file:")) {
                        return new FileSystemResource(path.substring("file:".length()));
                    }
                    return new FileSystemResource(path);

                }
                default:
                    throw new IllegalStateException("Unexpected type: " + type.getTypeName());
            }
        }
    }

    public static boolean isGrouperUi() {
        return getConfigPropertiesCascadeBase("hibernate").propertyValueBoolean("grouper.is.ui", false);
    }

    public static boolean isGrouperWs() {
        return getConfigPropertiesCascadeBase("hibernate").propertyValueBoolean("grouper.is.ws", false);
    }

    public static boolean isGrouperDaemon() {
        return getConfigPropertiesCascadeBase("hibernate").propertyValueBoolean("grouper.is.daemon", false);
    }
}
