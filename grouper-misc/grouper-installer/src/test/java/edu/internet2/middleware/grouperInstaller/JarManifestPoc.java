/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouperInstaller;

import java.io.File;

import edu.internet2.middleware.grouperInstaller.util.GrouperInstallerUtils;


/**
 *
 */
public class JarManifestPoc {

  /**
   * 
   */
  public JarManifestPoc() {
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    File file = new File("C:\\app\\grouper_2_2_0_installer\\grouper.apiBinary-2.2.0\\lib\\grouper\\ant.jar");
    String version = GrouperInstallerUtils.jarVersion(file);
    System.out.println(version);
  }

}
