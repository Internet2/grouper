package edu.internet2.middleware.grouper.ui.poc.fileManager;

import java.util.HashSet;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.permissions.PermissionEntry;
import edu.internet2.middleware.grouper.permissions.PermissionFinder;
import edu.internet2.middleware.grouper.permissions.PermissionProcessor;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

/**
 * file manager session container
 * @author mchyzer
 *
 */
public class PocFileManagerSessionContainer {

  /**
   * retrieveFromSession, cannot be null
   * @return the app state in request scope
   */
  public static PocFileManagerSessionContainer retrieveFromSessionOrCreate() {
    HttpServletRequest httpServletRequest = GrouperUiFilter.retrieveHttpServletRequest();
    HttpSession httpSession = httpServletRequest.getSession();
    PocFileManagerSessionContainer pocFileManagerSessionContainer = 
      (PocFileManagerSessionContainer)httpSession.getAttribute("pocFileManagerSessionContainer");
    if (pocFileManagerSessionContainer == null) {
      pocFileManagerSessionContainer = new PocFileManagerSessionContainer();
      pocFileManagerSessionContainer.storeToSession();
    }
    return pocFileManagerSessionContainer;
  }

  /**
   * store to request scope
   */
  public void storeToSession() {
    HttpServletRequest httpServletRequest = GrouperUiFilter.retrieveHttpServletRequest();
    HttpSession httpSession = httpServletRequest.getSession();
    httpSession.setAttribute("pocFileManagerSessionContainer", this);
  }

  /** folders permissions this act as user is allowed to read */
  private Set<String> grouperPermissionsReadSystemNames = null;

  /** folders permissions this act as user is allowed to create */
  private Set<String> grouperPermissionsCreateSystemNames = null;

  /**
   * folders permissions this act as user is allowed to read
   * @return folders permissions this act as user is allowed to read
   */
  public Set<String> getGrouperPermissionsReadSystemNames() {
    return this.grouperPermissionsReadSystemNames;
  }

  /**
   * folders permissions this act as user is allowed to read
   * @return folders permissions this act as user is allowed to read
   */
  public Set<String> getGrouperPermissionsCreateSystemNames() {
    return this.grouperPermissionsCreateSystemNames;
  }

  /**
   * init from db if needed
   * @param resetEvenIfNotNeeded true to reset anyways e.g. if something was updated
   */
  public void initFromDbIfNeeded(boolean resetEvenIfNotNeeded) {
    
    if (this.grouperPermissionsCreateSystemNames == null || resetEvenIfNotNeeded) {
  
      PocFileManagerRequestContainer pocFileManagerRequestContainer = PocFileManagerRequestContainer.retrieveFromRequestOrCreate();
      
      //make sure request is initted
      pocFileManagerRequestContainer.getAllFiles();
      
      this.grouperPermissionsCreateSystemNames = new HashSet<String>();
      this.grouperPermissionsReadSystemNames = new HashSet<String>();
      
      //see if we are acting as no one or are root
      if (StringUtils.isBlank(pocFileManagerRequestContainer.getActAsSubjectId()) 
          || StringUtils.equals(SubjectFinder.findRootSubject().getId(), pocFileManagerRequestContainer.getActAsSubjectId())) {
        
        //lets just do all
        for (PocFileManagerFolder pocFileManagerFolder : pocFileManagerRequestContainer.getAllFolders()) {
          this.grouperPermissionsCreateSystemNames.add(pocFileManagerFolder.getGrouperSystemName());
        }
        
      } else {
        
        GrouperSession grouperSession = GrouperSession.startRootSession();
        
        try {
          String actAsSubjectId = PocFileManagerRequestContainer.retrieveFromRequestOrCreate().getActAsSubjectId();
          Subject subject = SubjectFinder.findById(actAsSubjectId, true);

          //get the permissions we are allowed to see for this act as user
          Set<PermissionEntry> permissionEntries = new PermissionFinder().addSubject(subject).addRole(PocFileManagerUtils.PSU_APPS_FILE_MANAGER_ROLES_FILE_MANAGER_USER)
            .addPermissionDef(PocFileManagerUtils.PSU_APPS_FILE_MANAGER_PERMISSIONS_PERMISSION_DEFINITION_NAME)
            .assignPermissionProcessor(PermissionProcessor.FILTER_REDUNDANT_PERMISSIONS_AND_ROLES_AND_PROCESS_LIMITS)
            .findPermissions();

          for (PermissionEntry permissionEntry : GrouperUtil.nonNull(permissionEntries)) {
            
            if (permissionEntry.isAllowedOverall()) {
              if (StringUtils.equals(PocFileManagerUtils.ACTION_READ, permissionEntry.getAction())) {
                this.grouperPermissionsReadSystemNames.add(permissionEntry.getAttributeDefNameName());
              }
              if (StringUtils.equals(PocFileManagerUtils.ACTION_CREATE, permissionEntry.getAction())) {
                this.grouperPermissionsCreateSystemNames.add(permissionEntry.getAttributeDefNameName());
              }
            }
            
          }
          
        } finally {
          GrouperSession.stopQuietly(grouperSession);
        }
        
      }
    }  
  }
  
  
  
}
