package edu.internet2.middleware.grouper.app.gsh.template;

import java.util.Map;

/**
 * Classloader that defines a set of Java classes from in-memory bytecode.
 * Used by GshTemplateClassLoaderRegistry as the per-template loader for a
 * single compiled GSH Java template version.
 *
 * Parent delegation is standard parent-first: when asked for a class, the
 * default loadClass() implementation in ClassLoader first asks the parent
 * (which is Grouper's app classloader, holding GshTemplateV2, the Grouper
 * API, JDK classes, and any helper jars), and only falls through to this
 * loader's findClass() for classes the parent does not have. That guarantees
 * GshTemplateV2, helper-jar classes, and JDK types are all resolved to a
 * single Class object shared across the JVM, while the template's own
 * bytecode is loaded here.
 *
 * One instance per template version. On a source change, the registry drops
 * its reference to this loader and constructs a new one; the old loader
 * becomes unreferenced and is eligible for GC once any in-flight executions
 * holding references drain.
 *
 * GRP-7010
 */
public class ByteArrayClassLoader extends ClassLoader {

  /**
   * Map from fully-qualified class name to compiled bytecode. Includes the
   * top-level template class plus any inner / anonymous / lambda-generated
   * classes that the Java compiler emitted alongside it.
   */
  private final Map<String, byte[]> classNameToBytecode;

  /**
   * @param parent typically GshTemplateV2.class.getClassLoader() — the loader
   *   that has Grouper's app classes, so the template's references to the
   *   Grouper API resolve through delegation
   * @param classNameToBytecode bytecode for the template's class(es), keyed
   *   by FQN. Caller retains ownership of the map; this loader does not copy
   *   it but also does not mutate it.
   */
  public ByteArrayClassLoader(ClassLoader parent, Map<String, byte[]> classNameToBytecode) {
    super(parent);
    if (classNameToBytecode == null) {
      throw new IllegalArgumentException("classNameToBytecode must not be null");
    }
    this.classNameToBytecode = classNameToBytecode;
  }

  /**
   * Define a class from this loader's bytecode map. Called by the JDK's
   * default loadClass() only after the parent loader has been asked first
   * and returned ClassNotFoundException — i.e. for classes the parent does
   * not have, which for our purposes are exactly the template's own classes.
   */
  @Override
  protected Class<?> findClass(String name) throws ClassNotFoundException {
    byte[] bytecode = this.classNameToBytecode.get(name);
    if (bytecode == null) {
      throw new ClassNotFoundException(name);
    }
    return defineClass(name, bytecode, 0, bytecode.length);
  }

}
