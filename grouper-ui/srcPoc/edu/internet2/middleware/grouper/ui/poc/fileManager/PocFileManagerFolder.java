package edu.internet2.middleware.grouper.ui.poc.fileManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.AttributeDefNameSave;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefFinder;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.util.GrouperUuid;
import edu.internet2.middleware.grouper.permissions.PermissionAllowed;
import edu.internet2.middleware.grouper.permissions.role.Role;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

/**
 * folder
 * @author mchyzer
 *
 */
public class PocFileManagerFolder implements Comparable<PocFileManagerFolder> {

  /**
   * files in this folder
   */
  private Set<PocFileManagerFile> files = new TreeSet<PocFileManagerFile>();
  
  /**
   * files in this folder
   * @return files in this folder
   */
  public Set<PocFileManagerFile> getFiles() {
    return this.files;
  }

  /**
   * call this after setting all parents of folders, and it will init
   */
  public void initParents() {
    
    if (this.parentFolder != null) {
      this.parentFolders = new ArrayList<PocFileManagerFolder>();
      
      PocFileManagerFolder parentFolder = this.getParentFolder();
      
      for (int i=0;i<100;i++) {
        
        //ttl
        if (i==99) {
          throw new RuntimeException("Do we have a circular reference or a folder structure more than 99 deep?");
        }

        //we are there
        if (parentFolder == null) {
          break;
        }
        
        this.parentFolders.add(0, parentFolder);
        
        parentFolder = parentFolder.getParentFolder();
      }
    }
    
    //lets set the display name
    {
      StringBuilder fullyQualifiedName = new StringBuilder();
  
      //add parents
      for (PocFileManagerFolder pocFileManagerFolder : GrouperUtil.nonNull(this.parentFolders)) {
        fullyQualifiedName.append(pocFileManagerFolder.getName()).append("/");
      }
      
      fullyQualifiedName.append(this.name);
      this.grouperDisplayExtension = fullyQualifiedName.toString();
    }    
  }
  
  /**
   * list of parent folders
   */
  private List<PocFileManagerFolder> parentFolders = null;
  
  /**
   * list of parent folders
   * @return list of parent folders
   */
  public List<PocFileManagerFolder> getParentFolders() {
    return this.parentFolders;
  }

  /**
   * list of parent folders
   * @param parentFolders1
   */
  public void setParentFolders(List<PocFileManagerFolder> parentFolders1) {
    this.parentFolders = parentFolders1;
  }

  /** sql pattern for row */
  private static Pattern folderSqlPattern = Pattern.compile("^(.*)||(.*)||(.*)$");
  
  /**
   * retrieve folders from DB
   * @return the folders
   */
  public static Set<PocFileManagerFolder> retrieveFolders() {
    Set<PocFileManagerFolder> pocFileManagerFolders = retrieveFoldersHelper();
    
    boolean foundRoot = false;
    //lets see if root is there
    for (PocFileManagerFolder pocFileManagerFolder : pocFileManagerFolders) {
      
      if (StringUtils.isBlank(pocFileManagerFolder.getParentFolderId()) && StringUtils.equals(pocFileManagerFolder.getName(), "Root")) {
        foundRoot = true;
        break;
      }
      
    }
    
    if (foundRoot) {
      return pocFileManagerFolders;
    }
    //lets add it
    PocFileManagerFolder pocFileManagerFolder = new PocFileManagerFolder();
    pocFileManagerFolder.setName("Root");
    //dont reset so no circular reference, we are resetting now
    pocFileManagerFolder.addFolder(false);

    //get them again
    return retrieveFoldersHelper();
  }

  /**
   * add a folder to the DB
   * @param name
   * @param parent
   * @param resetRegistry true to reset registry
   */
  public void addFolder(boolean resetRegistry) {
    
    //note, in reality you will need to deal with transactions (if second thing fails, roll first thing back)
    this.id = StringUtils.isBlank(this.id) ? GrouperUuid.getUuid() : this.id;
    
    //add to the database
    HibernateSession.bySqlStatic().executeSql("insert into file_mgr_folder (id, name, parent_folder_id) values (?, ?, ?)", 
        GrouperUtil.toListObject(this.id, this.name, this.parentFolderId));
    
    //add a folder to Grouper
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    try {
      
      AttributeDef permissionsDef = AttributeDefFinder.findByName(
          PocFileManagerUtils.PSU_APPS_FILE_MANAGER_PERMISSIONS_PERMISSION_DEFINITION_NAME, true);
      
      //lets add a permission name
      AttributeDefName folderPermissionName = new AttributeDefNameSave(grouperSession, permissionsDef).assignName(this.getGrouperSystemName())
        .assignDisplayExtension(this.getGrouperDisplayExtension()).save();
      
      //lets make sure the current user can use the new folder
      String actAsSubjectId = PocFileManagerRequestContainer.retrieveFromRequestOrCreate().getActAsSubjectId();
      if (!StringUtils.isBlank(actAsSubjectId)) {
        Subject subject = SubjectFinder.findById(actAsSubjectId, true);
        Role user = GroupFinder.findByName(grouperSession, PocFileManagerUtils.PSU_APPS_FILE_MANAGER_ROLES_FILE_MANAGER_USER, true);
        user.addMember(subject, false);
        user.getPermissionRoleDelegate().assignSubjectRolePermission(PocFileManagerUtils.ACTION_CREATE, folderPermissionName, subject, PermissionAllowed.ALLOWED);
        user.getPermissionRoleDelegate().assignSubjectRolePermission(PocFileManagerUtils.ACTION_READ, folderPermissionName, subject, PermissionAllowed.ALLOWED);
      }
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
    if (resetRegistry) {
      PocFileManagerRequestContainer.retrieveFromRequestOrCreate().initFromDbIfNeeded(true);
    }
  }
  
  /**
   * 
   * @return parents
   */
  private static Set<PocFileManagerFolder> retrieveFoldersHelper() {
    //lets get all folders
    List<String> rows = HibernateSession.bySqlStatic().listSelect(String.class, 
        "SELECT CONCAT(id, CONCAT('||', CONCAT(NAME, CONCAT('||', parent_folder_id)))) AS the_row FROM file_mgr_folder", null);

    Set<PocFileManagerFolder> pocFileManagerFolders = new TreeSet<PocFileManagerFolder>();
    
    for (String row : GrouperUtil.nonNull(rows)) {
      
      //this is id||name||folder_id
      Matcher matcher = folderSqlPattern.matcher(row);
      matcher.matches();
      
      String id = matcher.group(1);
      String name = matcher.group(2);
      String folderId = matcher.group(3);
      
      PocFileManagerFolder pocFileManagerFolder = new PocFileManagerFolder();
      pocFileManagerFolder.setId(id);
      pocFileManagerFolder.setName(name);
      pocFileManagerFolder.setParentFolderId(folderId);
      
      pocFileManagerFolders.add(pocFileManagerFolder);
      
    }
    
    return pocFileManagerFolders;
  }
  
  /**
   * get system name
   * @return system name
   */
  public String getGrouperSystemName() {
    StringBuilder systemName = new StringBuilder(PocFileManagerUtils.PSU_APPS_FILE_MANAGER_PERMISSIONS_STEM);
    systemName.append(":");

    //add parents
    for (PocFileManagerFolder pocFileManagerFolder : GrouperUtil.nonNull(this.parentFolders)) {
      systemName.append(pocFileManagerFolder.getName()).append("__");
    }
    
    systemName.append(this.name);
    return systemName.toString();
  }

  /**
   * get friendly name
   * @return friendly name
   */
  public String getGrouperDisplayName() {
    StringBuilder friendlyName = new StringBuilder(PocFileManagerUtils.PSU_APPS_FILE_MANAGER_PERMISSIONS_STEM);
    friendlyName.append(":");
    friendlyName.append(this.getGrouperDisplayExtension());
    return friendlyName.toString();
  }
  
  /** folder id */
  private String parentFolderId;
  /** fully qualified name with slashes */
  private String grouperDisplayExtension;
  /** uuid */
  private String id;
  /** name (not fully qualified) */
  private String name;

  /**
   * folder id that the folder is in
   * @return folder id
   */
  public String getParentFolderId() {
    return this.parentFolderId;
  }
  
  /** parent folder */
  private PocFileManagerFolder parentFolder;
  
  /**
   * folder that the folder is in
   * @return folder
   */
  public PocFileManagerFolder getParentFolder() {
    return this.parentFolder;
  }
  
  /**
   * folder that the folder is in
   * @param parentFolder
   */
  public void setParentFolder(PocFileManagerFolder parentFolder) {
    this.parentFolder = parentFolder;
  }

  /**
   * fully qualified name with slashes
   * @return fully qualified name with slashes
   */
  public String getGrouperDisplayExtension() {
    return this.grouperDisplayExtension;
  }

  /**
   * uuid
   * @return uuid
   */
  public String getId() {
    return this.id;
  }

  /**
   * name (not fully qualified)
   * @return name (not fully qualified)
   */
  public String getName() {
    return this.name;
  }

  /**
   * folder id
   * @param folderId1
   */
  public void setParentFolderId(String folderId1) {
    this.parentFolderId = folderId1;
  }

  /**
   * uuid
   * @param id1
   */
  public void setId(String id1) {
    this.id = id1;
  }

  /**
   * name (not fully qualified)
   * @param name1
   */
  public void setName(String name1) {
    this.name = name1;
  }

  /**
   * @see Comparable#compareTo(Object)
   */
  @Override
  public int compareTo(PocFileManagerFolder o) {
    return StringUtils.defaultString(this.grouperDisplayExtension).compareTo(StringUtils.defaultString(o.grouperDisplayExtension));
  }

}
