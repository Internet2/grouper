/**
 * @author mchyzer
 * $Id: GrouperShellWrapper.java,v 1.3 2009-11-30 17:57:38 tzeller Exp $
 */
package edu.internet2.middleware.grouper.app.gsh;



public class GrouperShellWrapper {

  /**
   * @param args
   */
  public static void main(String[] args) {
    try {
        GrouperShell.main(args);
    } catch (NoClassDefFoundError cnfe) {
      System.err.println("There was a NoClassDefFoundError.  This could be because you are not using Java 1.6+ which is required for GSH.  This is version " + System.getProperty("java.version") + ".");
      throw cnfe;
    }

  }

}
