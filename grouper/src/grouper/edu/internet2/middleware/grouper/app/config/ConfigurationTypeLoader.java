package edu.internet2.middleware.grouper.app.config;

import java.lang.reflect.Modifier;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * <p>SECURITY: the UI configuration screens (add and edit forms for trusted JWTs, custom UIs, entity
 * data fields, GSH templates, provisioner start-with, SQL sync, user lifecycle, ...) are told which
 * configuration class to use by a request parameter, and used to load it with
 * GrouperUtil.forName (which runs the class's static initializer) and create it with its no-arg
 * constructor before casting it to the expected type.  So a link could make a sysadmin's browser
 * load and construct any class on the classpath.</p>
 *
 * <p>This loads the class WITHOUT initializing it, refuses it unless it is a public, concrete
 * subclass of the type the screen expects, and only then creates it.  A refused class is never
 * initialized or constructed.</p>
 */
public class ConfigurationTypeLoader {

  /**
   * a java class name: identifiers separated by dots (inner classes with $)
   */
  private static final Pattern CLASS_NAME = Pattern.compile(
      "^[A-Za-z_$][A-Za-z0-9_$]*(\\.[A-Za-z_$][A-Za-z0-9_$]*)*$");

  /**
   * no instances
   */
  private ConfigurationTypeLoader() {
  }

  /**
   * load a configuration class named by a request (or config) and create an instance, only if it is
   * a public, concrete subclass of the expected type
   * @param <T> expected type
   * @param className the class name from the request
   * @param expectedType the configuration base class the screen works with
   * @return the new instance
   * @throws RuntimeException if the name is blank or not a class name, the class does not exist, or
   * it is not a public concrete subclass of the expected type
   */
  public static <T> T newInstance(String className, Class<T> expectedType) {
    Class<? extends T> theClass = loadClass(className, expectedType);
    return GrouperUtil.newInstance(theClass);
  }

  /**
   * load a configuration class without initializing it, and check it is a public, concrete subclass
   * of the expected type
   * @param <T> expected type
   * @param className the class name
   * @param expectedType the configuration base class
   * @return the class (not yet initialized)
   */
  public static <T> Class<? extends T> loadClass(String className, Class<T> expectedType) {

    String typeLabel = expectedType.getSimpleName();

    if (StringUtils.isBlank(className)) {
      throw new RuntimeException("A " + typeLabel + " type is required");
    }

    // a value that is not a class name might be markup, so it is not echoed
    if (!CLASS_NAME.matcher(className).matches()) {
      throw new RuntimeException("Invalid " + typeLabel + " type (not a class name, " + className.length() + " characters)");
    }

    Class<?> theClass = null;
    try {
      // false: do not run the static initializer of a class that is about to be refused
      theClass = Class.forName(className, false, ConfigurationTypeLoader.class.getClassLoader());
    } catch (ClassNotFoundException cnfe) {
      throw new RuntimeException("Invalid " + typeLabel + " type, class not found: " + className);
    } catch (LinkageError le) {
      throw new RuntimeException("Invalid " + typeLabel + " type, class cannot be loaded: " + className);
    }

    if (!expectedType.isAssignableFrom(theClass)) {
      throw new RuntimeException("Invalid " + typeLabel + " type, not a " + expectedType.getName() + ": " + className);
    }

    int modifiers = theClass.getModifiers();
    if (theClass.isInterface() || Modifier.isAbstract(modifiers) || !Modifier.isPublic(modifiers)) {
      throw new RuntimeException("Invalid " + typeLabel + " type, not a public concrete class: " + className);
    }

    @SuppressWarnings("unchecked")
    Class<? extends T> result = (Class<? extends T>) theClass;
    return result;
  }

}
