/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouperInstaller;

import java.io.File;
import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.Set;

import edu.internet2.middleware.grouperInstaller.GrouperInstaller.GrouperInstallerPatchStatus;
import edu.internet2.middleware.grouperInstaller.util.GrouperInstallerUtils;


/**
 * grouper installer merge properties files main
 */
public class GrouperInstallerMergePatchFiles {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    if (GrouperInstallerUtils.length(args) != 2) {
      System.out.println("Args must be 2: patch properties filename from, and patch properties filename to");
      System.exit(1);
    }
    
    String patchPropertiesFilenameFrom = args[0];
    String patchPropertiesFilenameTo = args[1];
    
    File patchPropertiesFileFrom = new File(patchPropertiesFilenameFrom);
    File patchPropertiesFileTo = new File(patchPropertiesFilenameTo);
    
    mergePatchFiles(patchPropertiesFileFrom, patchPropertiesFileTo, true);
    
  }
  
  /**
   * 
   */
  public GrouperInstallerMergePatchFiles() {
  }

  /**
   * 
   * @param patchPropertiesFileFrom
   * @param patchPropertiesFileTo
   * @param printResult
   */
  public static void mergePatchFiles(File patchPropertiesFileFrom, File patchPropertiesFileTo, boolean printResult) {
    
    //if the source file doesnt even exist, then we are all good
    if (!patchPropertiesFileFrom.exists()) {
      if (printResult) {
        System.out.println("No patches in source file since source file doesn't exist: " + patchPropertiesFileFrom.getAbsolutePath());
      }
      return;
    }
    
    //lets see which patches we need to merge...
    Properties patchPropertiesFrom = GrouperInstallerUtils.propertiesFromFile(patchPropertiesFileFrom);
    
    //set of properties to move over, just the base, for instance: grouper_v2_2_1_api_patch_0
    Set<String> propertyBasesApplied = new LinkedHashSet<String>();
    
    for (Object patchPropertyObject : patchPropertiesFrom.keySet()) {
      String patchProperty = (String)patchPropertyObject;
      if (patchProperty.endsWith(".state")) {
        String patchPropertyValue = patchPropertiesFrom.getProperty(patchProperty);
        GrouperInstallerPatchStatus status = GrouperInstallerPatchStatus.valueOfIgnoreCase(patchPropertyValue, false, false);
        if (status == GrouperInstallerPatchStatus.applied) {
          String propertyBase = patchProperty.substring(0, patchProperty.length() - ".state".length());
          propertyBasesApplied.add(propertyBase);
        }
      }
    }

    //at this point, maybe we have none
    if (propertyBasesApplied.size() == 0) {
      if (printResult) {
        System.out.println("No patches in source file: " + patchPropertiesFileFrom.getAbsolutePath());
      }
      return;
    }

    //lets see which patches are need to be moved over, based on whats in the destination
    if (!patchPropertiesFileTo.exists()) {
      GrouperInstallerUtils.fileCreate(patchPropertiesFileTo);
    }
    
    Properties patchPropertiesTo = GrouperInstallerUtils.propertiesFromFile(patchPropertiesFileTo);
    
    boolean hasPatchToMove = false;
    
    for (String propertyBaseApplied : propertyBasesApplied) {
      
      String patchPropertyValueTo = patchPropertiesTo.getProperty(propertyBaseApplied + ".state");
      GrouperInstallerPatchStatus statusTo = GrouperInstallerPatchStatus.valueOfIgnoreCase(patchPropertyValueTo, false, false);
      if (statusTo != GrouperInstallerPatchStatus.applied) {
        
        //found one
        hasPatchToMove = true;

        if (printResult) {
          System.out.println("Marking patch: " + propertyBaseApplied + " as applied in " + patchPropertiesFileTo.getAbsolutePath());
        }
        
        //lets move all properties that start with the prefix
        for (Object patchPropertyObject : patchPropertiesFrom.keySet()) {
          String patchProperty = (String)patchPropertyObject;
          if (patchProperty.startsWith(propertyBaseApplied)) {
            String patchPropertyFromValue = patchPropertiesFrom.getProperty(patchProperty);
            GrouperInstaller.editPropertiesFile(patchPropertiesFileTo, patchProperty, patchPropertyFromValue, true);
          }
        }
      }
    }
    
    //at this point, maybe we still had none
    if (!hasPatchToMove) {
      if (printResult) {
        System.out.println("All patches in source file were already applied in destination file: "  + patchPropertiesFileTo.getAbsolutePath());
      }
      return;
    }

  }
  
}
