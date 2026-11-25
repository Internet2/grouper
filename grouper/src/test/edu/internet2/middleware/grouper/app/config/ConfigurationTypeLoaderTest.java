package edu.internet2.middleware.grouper.app.config;

import junit.framework.TestCase;
import junit.textui.TestRunner;

/**
 * SECURITY: the UI configuration screens name their configuration class in a request parameter.
 * ConfigurationTypeLoader only loads (without initializing) and creates a public concrete subclass of
 * the expected type; anything else is refused before its static initializer or constructor runs.
 * Before the fix the screens used GrouperUtil.forName + newInstance and cast afterwards.
 * No database or Grouper config needed.
 */
public class ConfigurationTypeLoaderTest extends TestCase {

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(ConfigurationTypeLoaderTest.class);
  }

  /**
   * @param name
   */
  public ConfigurationTypeLoaderTest(String name) {
    super(name);
  }

  /** set by the static initializer of {@link StaticInitMarker} (read here, so the marker is not initialized by the read) */
  static boolean staticInitMarkerInitialized = false;

  /** set by the constructor of {@link ConstructorMarker} */
  static boolean constructorMarkerConstructed = false;

  /**
   * the expected configuration base type in these tests
   */
  public static abstract class TestConfigurationBase {
  }

  /**
   * a good configuration type
   */
  public static class GoodConfiguration extends TestConfigurationBase {
  }

  /**
   * abstract subclass: refused
   */
  public static abstract class AbstractConfiguration extends TestConfigurationBase {
  }

  /**
   * not public: refused
   */
  static class NotPublicConfiguration extends TestConfigurationBase {
  }

  /**
   * not a configuration type, with a static initializer that must not run
   */
  public static class StaticInitMarker {
    static {
      staticInitMarkerInitialized = true;
    }
  }

  /**
   * not a configuration type, with a constructor that must not run
   */
  public static class ConstructorMarker {
    /**
     *
     */
    public ConstructorMarker() {
      constructorMarkerConstructed = true;
    }
  }

  /**
   * a public concrete subclass is created
   */
  public void testGoodTypeIsCreated() {
    TestConfigurationBase result = ConfigurationTypeLoader.newInstance(GoodConfiguration.class.getName(), TestConfigurationBase.class);
    assertTrue(result instanceof GoodConfiguration);
  }

  /**
   * a class that is not the expected type is refused, and neither initialized nor constructed
   */
  public void testOtherClassRefusedWithoutInitOrConstructor() {
    assertRefused(StaticInitMarker.class.getName());
    assertFalse("the static initializer of a refused class must not run", staticInitMarkerInitialized);

    assertRefused(ConstructorMarker.class.getName());
    assertFalse("a refused class must not be constructed", constructorMarkerConstructed);
  }

  /**
   * abstract, non-public, interface, missing, blank and non class names are refused
   */
  public void testRefusedNames() {
    assertRefused(AbstractConfiguration.class.getName());
    assertRefused(NotPublicConfiguration.class.getName());
    assertRefused(Runnable.class.getName());
    assertRefused("edu.internet2.middleware.grouper.NoSuchClass123");
    assertRefused(null);
    assertRefused("  ");

    String markup = "x\"><script>alert(1)</script>";
    String message = assertRefused(markup);
    assertFalse("a value that is not a class name is not echoed: " + message, message.contains("<script>"));
  }

  /**
   * @param className
   * @return the exception message
   */
  private static String assertRefused(String className) {
    try {
      ConfigurationTypeLoader.newInstance(className, TestConfigurationBase.class);
    } catch (RuntimeException re) {
      return re.getMessage();
    }
    fail("should be refused: " + className);
    return null;
  }

}
