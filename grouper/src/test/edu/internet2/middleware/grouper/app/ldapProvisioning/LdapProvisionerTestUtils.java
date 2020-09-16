package edu.internet2.middleware.grouper.app.ldapProvisioning;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.IOUtils;

import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.ldap.LdapSessionUtils;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * @author shilen
 */
public class LdapProvisionerTestUtils {
  
  private static String dockerPath = null;

  public static void stopAndRemoveLdapContainer() {
    
    Process pDockerList = null;
    Process pDockerStop = null;
    Process pDockerRm = null;
    
    try {
      pDockerList = new ProcessBuilder(getDockerPath(), "ps", "-a").start();
      pDockerList.waitFor();
      
      if (pDockerList.exitValue() != 0) {
        throw new RuntimeException("Failed to list containers: " + IOUtils.toString(pDockerList.getErrorStream(), "UTF-8"));
      }
      
      String dockerContainersString = IOUtils.toString(pDockerList.getInputStream(), "UTF-8");
      if (dockerContainersString.contains("openldap-dinkel-grouper")) {
        pDockerStop = new ProcessBuilder(getDockerPath(), "stop", "openldap-dinkel-grouper").start();
        pDockerStop.waitFor();
        
        if (pDockerStop.exitValue() != 0) {
          throw new RuntimeException("Failed to stop container: " + IOUtils.toString(pDockerStop.getErrorStream(), "UTF-8"));
        }
        
        pDockerRm = new ProcessBuilder(getDockerPath(), "rm", "openldap-dinkel-grouper").start();
        pDockerRm.waitFor();
        
        if (pDockerRm.exitValue() != 0) {
          throw new RuntimeException("Failed to delete container: " + IOUtils.toString(pDockerRm.getErrorStream(), "UTF-8"));
        } 
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }
  
  public static void startLdapContainer() {
    
    Process pDockerList = null;
    Process pDockerCreateImage = null;
    Process pDockerRun = null;
    
    try {
      pDockerList = new ProcessBuilder(getDockerPath(), "images").start();
      pDockerList.waitFor();
      
      if (pDockerList.exitValue() != 0) {
        throw new RuntimeException("Failed to list images: " + IOUtils.toString(pDockerList.getErrorStream(), "UTF-8"));
      }
      
      String dockerImagesString = IOUtils.toString(pDockerList.getInputStream(), "UTF-8");
      if (!dockerImagesString.contains("openldap-dinkel-grouper")) {
        
        pDockerCreateImage = new ProcessBuilder(getDockerPath(), "build", "-t", "openldap-dinkel-grouper", GrouperUtil.getGrouperHome() + "/../grouper-misc/openldap-dinkel-grouper").start();
        pDockerCreateImage.waitFor();
        
        if (pDockerCreateImage.exitValue() != 0) {
          throw new RuntimeException("Failed to create image: " + IOUtils.toString(pDockerCreateImage.getErrorStream(), "UTF-8"));
        }
      }
      
      pDockerRun = new ProcessBuilder(getDockerPath(), "run", "-d", "-p", "389:389", "--name", "openldap-dinkel-grouper", "--mount", "type=bind,source=" + GrouperUtil.getGrouperHome() + "/../grouper-misc/openldap-dinkel-grouper/ldap-seed-data,target=/etc/ldap/prepopulate", "-e", "SLAPD_PASSWORD=secret", "-e", "SLAPD_CONFIG_PASSWORD=secret", "-e", "SLAPD_DOMAIN=example.edu", "openldap-dinkel-grouper").start();
      pDockerRun.waitFor();
      
      if (pDockerRun.exitValue() != 0) {
        throw new RuntimeException("Failed to run container: " + IOUtils.toString(pDockerRun.getErrorStream(), "UTF-8"));
      }
      
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("ldap.personLdap.url", "ldap://localhost:389");
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("ldap.personLdap.user", "cn=admin,dc=example,dc=edu");
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("ldap.personLdap.pass", "secret");
      
      RuntimeException lastException = null;;
      for (int i = 0; i < 20; i++) {
        try {
          if (LdapSessionUtils.ldapSession().testConnection("personLdap")) {
            return;
          } else {
            Thread.sleep(1000);
          }
        } catch (RuntimeException e) {
          lastException = e;
          Thread.sleep(1000);
        }
      }
      
      throw lastException;
    } catch (IOException e) {
      throw new RuntimeException(e);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
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
        }
      }
    }
    
    if (dockerPath == null) {
      dockerPath = "docker";
    }
    
    return dockerPath;
  }
}
