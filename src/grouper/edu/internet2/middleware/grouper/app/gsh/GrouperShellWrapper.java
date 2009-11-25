/**
 * @author mchyzer
 * $Id: GrouperShellWrapper.java,v 1.1 2009-11-25 16:23:45 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.app.gsh;


/**
 *
 */
public class GrouperShellWrapper {

  /**
   * @param args
   */
  public static void main(String[] args) {
    try {
      GrouperShell.main(args);
    } catch (NoClassDefFoundError cnfe) {
      System.err.println("There was a NoClassDefFoundError.  This could be because you are not using Java 1.6+ which is required for GSH.");
      throw cnfe;
    }

  }

}
