package edu.internet2.middleware.grouper.permissions;

import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.Stem.Scope;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

/**
 * bean that has helper methods to process the permissions
 * @author mchyzer
 *
 */
public class PermissionResult {

  /**
   * constructor
   */
  public PermissionResult() {
    
  }
  
  /**
   * permission entries to process
   */
  private Set<PermissionEntry> permissionEntries;

  /**
   * 
   * @param thePermissionEntries
   */
  public PermissionResult(Set<PermissionEntry> thePermissionEntries) {
    this.permissionEntries = thePermissionEntries;
  }

  /**
   * get allowed extensions for this action
   * @param stemName
   * @param action
   * @param subject
   * @param scope
   * @return the extensions allowed, never null
   */
  public Set<String> permissionNameExtensions(String stemName, String action, Subject subject, Scope scope) {
    
    Set<String> extensions = new TreeSet<String>();
    
    if (this.permissionEntries != null) {
      
      for (PermissionEntry permissionEntry : this.permissionEntries) {
        
        if (permissionEntry.isAllowedOverall() && StringUtils.equals(action, permissionEntry.getAction())) {
          extensions.add(GrouperUtil.extensionFromName(permissionEntry.getAttributeDefNameName()));
        }
      }
    }
    return extensions;
  }
  
}
