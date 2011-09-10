package edu.internet2.middleware.poc_secureUserData;


/**
 * @author mchyzer
 * $Id$
 */

/**
 * main class that syncs the secure user data
 */
public class SudFullSync {

  /**
   * @param args
   */
  public static void main(String[] args) {
    for (SudColPermission sudColPermission : SudColPermission.retrieveAllColPermissions()) {
      System.out.println(sudColPermission);
    }
    
    
  }

}
