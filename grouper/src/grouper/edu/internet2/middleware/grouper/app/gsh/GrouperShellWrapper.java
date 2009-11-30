/**
 * @author mchyzer
 * $Id: GrouperShellWrapper.java,v 1.2 2009-11-30 17:28:43 tzeller Exp $
 */
package edu.internet2.middleware.grouper.app.gsh;



/**
 * Run GrouperShell only with the required Java version or higher.
 */
public class GrouperShellWrapper {

  public static final String REQUIRED_VERSION = "1.6";

  /**
   * @param args
   */
  public static void main(String[] args) {
    try {
      String javaVersion = System.getProperty("java.version");
      if (!javaVersion.startsWith(REQUIRED_VERSION)) {
        System.err.println("GSH requires Java " + REQUIRED_VERSION + "+. This is version " + javaVersion + ".");
      } else {
        GrouperShell.main(args);
      }
    } catch (NoClassDefFoundError cnfe) {
      System.err.println("There was a NoClassDefFoundError.  This could be because you are not using Java 1.6+ which is required for GSH.");
      throw cnfe;
    }

  }

}
