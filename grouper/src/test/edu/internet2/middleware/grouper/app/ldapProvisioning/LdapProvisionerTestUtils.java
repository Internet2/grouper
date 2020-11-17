package edu.internet2.middleware.grouper.app.ldapProvisioning;

import java.io.File;

import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.ldap.LdapSessionUtils;
import edu.internet2.middleware.grouper.misc.GrouperStartup;
import edu.internet2.middleware.grouper.util.CommandLineExec;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * @author shilen
 */
public class LdapProvisionerTestUtils {
  
  public static void main(String args[]) throws Exception {
    GrouperStartup.startup();
    stopAndRemoveLdapContainer();
    startLdapContainer();
  }
  
  private static String dockerPath = null;

  public static void stopAndRemoveLdapContainer() {

    String dockerProcesses = new CommandLineExec().assignCommand(getDockerPath() + " ps -a")
        .assignErrorOnNonZero(true).execute().getStdout().getAllLines();
    
    if (dockerProcesses.contains("openldap-dinkel-grouper")) {

      new CommandLineExec().assignCommand(getDockerPath() + " stop openldap-dinkel-grouper")
          .assignErrorOnNonZero(true).execute();

      new CommandLineExec().assignCommand(getDockerPath() + " rm openldap-dinkel-grouper")
        .assignErrorOnNonZero(true).execute();
    }
  }
  
  public static void startLdapContainer() {
    
    String dockerImages = new CommandLineExec().assignCommand(getDockerPath() + " images")
        .assignErrorOnNonZero(true).execute().getStdout().getAllLines();

    String grouperMiscHome = GrouperUtil.getGrouperHome();
    
    if (grouperMiscHome.endsWith("grouper-pspng")) {
      grouperMiscHome = grouperMiscHome + File.separator + "..";
    } else {
      grouperMiscHome = grouperMiscHome + File.separator + ".." + File.separator + "grouper-misc";
    }

    // binds need a full path
    grouperMiscHome = new File(grouperMiscHome).getAbsolutePath();

    if (!dockerImages.contains("openldap-dinkel-grouper")) {
      
      new CommandLineExec().assignCommand(getDockerPath() + " build -t openldap-dinkel-grouper '"
          + grouperMiscHome + File.separator + "openldap-dinkel-grouper'")
        .assignErrorOnNonZero(true)
        .execute();
    }

    new CommandLineExec().assignCommand(getDockerPath() + " run -d -p 389:389 --name openldap-dinkel-grouper --mount type=bind,source='" 
        + grouperMiscHome + File.separator + "openldap-dinkel-grouper" + File.separator + "ldap-seed-data',target=/etc/ldap/prepopulate "
            + "-e SLAPD_PASSWORD=secret -e SLAPD_CONFIG_PASSWORD=secret -e SLAPD_DOMAIN=example.edu openldap-dinkel-grouper")
      .assignErrorOnNonZero(true)
      .execute();

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("ldap.personLdap.url", "ldap://localhost:389");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("ldap.personLdap.user", "cn=admin,dc=example,dc=edu");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("ldap.personLdap.pass", "secret");

    // abstract ldap class logs the errors, so just sleep 10 to wait until testing
    GrouperUtil.sleep(14000);
    
    RuntimeException lastException = null;
    for (int i = 0; i < 100; i++) {
      try {
        if (LdapSessionUtils.ldapSession().testConnection("personLdap")) {
          return;
        } else {
          GrouperUtil.sleep(1000);
        }
      } catch (RuntimeException e) {
        lastException = e;
        GrouperUtil.sleep(1000);
      }
    }
  }
  
  private static String getDockerPath() {
    if (dockerPath == null) {
      synchronized(LdapProvisionerTestUtils.class) {
        if (dockerPath == null) {
          String[] filesToCheck = { "/usr/bin/docker", "/usr/local/bin/docker", "/bin/docker" };
          for (String fileToCheck : filesToCheck) {
            if (new File(fileToCheck).exists()) {
              dockerPath = fileToCheck;
              break;
            } 
          }
          if (dockerPath == null) {
            dockerPath = "docker";
          }
        }
      }
    }
    
    
    return dockerPath;
  }
}
