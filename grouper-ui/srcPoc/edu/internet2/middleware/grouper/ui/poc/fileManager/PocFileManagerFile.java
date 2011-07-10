package edu.internet2.middleware.grouper.ui.poc.fileManager;

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
 * file in the penn state file manager 
 * @author mchyzer
 *
 */
public class PocFileManagerFile implements Comparable<PocFileManagerFile>{

  /** sql pattern for row */
  private static Pattern fileSqlPattern = Pattern.compile("^(.*)||(.*)||(.*)$");
  
  /**
   * retrieve files from DB
   * @return the files
   */
  public static Set<PocFileManagerFile> retrieveFiles() {
    
    //lets get all files
    List<String> rows = HibernateSession.bySqlStatic().listSelect(String.class, 
        "SELECT CONCAT(id, CONCAT('||', CONCAT(NAME, CONCAT('||', folder_id)))) AS the_row FROM file_mgr_file", null);

    Set<PocFileManagerFile> pocFileManagerFiles = new TreeSet<PocFileManagerFile>();
    
    for (String row : GrouperUtil.nonNull(rows)) {
      
      //this is id||name||folder_id
      Matcher matcher = fileSqlPattern.matcher(row);
      matcher.matches();
      
      String id = matcher.group(1);
      String name = matcher.group(2);
      String folderId = matcher.group(3);
      
      PocFileManagerFile pocFileManagerFile = new PocFileManagerFile();
      pocFileManagerFile.setId(id);
      pocFileManagerFile.setName(name);
      pocFileManagerFile.setFolderId(folderId);
      
      pocFileManagerFiles.add(pocFileManagerFile);
      
    }
    
    return pocFileManagerFiles;
  }
  
  /**
   * @see Comparable#compareTo(Object)
   */
  @Override
  public int compareTo(PocFileManagerFile o) {
    return StringUtils.defaultString(this.fullyQualifiedName).compareTo(StringUtils.defaultString(o.fullyQualifiedName));
  }

  
  /** uuid */
  private String id;
  
  /** name (not fully qualified) */
  private String name;
  
  /** folder id */
  private String folderId;
  
  /** fully qualified name with slashes */
  private String fullyQualifiedName;

  /** parent folder */
  private PocFileManagerFolder folder;

  /**
   * uuid
   * @return uuid
   */
  public String getId() {
    return this.id;
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
   * @return name (not fully qualified)
   */
  public String getName() {
    return this.name;
  }

  /**
   * name (not fully qualified)
   * @param name1
   */
  public void setName(String name1) {
    this.name = name1;
  }

  /**
   * folder id
   * @return folder id
   */
  public String getFolderId() {
    return this.folderId;
  }

  /**
   * folder id
   * @param folderId1
   */
  public void setFolderId(String folderId1) {
    this.folderId = folderId1;
  }

  /**
   * fully qualified name with slashes
   * @return fully qualified name with slashes
   */
  public String getFullyQualifiedName() {
    return this.fullyQualifiedName;
  }

  /**
   * fully qualified name with slashes
   * @param fullyQualifiedName1
   */
  public void setFullyQualifiedName(String fullyQualifiedName1) {
    this.fullyQualifiedName = fullyQualifiedName1;
  }

  /**
   * folder that the folder is in
   * @return folder
   */
  public PocFileManagerFolder getFolder() {
    return this.folder;
  }

  /**
   * folder that the folder is in
   * @param parentFolder
   */
  public void setFolder(PocFileManagerFolder parentFolder) {
    this.folder = parentFolder;
  }

  /**
   * add a file to the DB
   * @param name
   * @param parent
   * @param resetRegistry true to reset registry
   */
  public void addFile(boolean resetRegistry) {
    
    //note, in reality you will need to deal with transactions (if second thing fails, roll first thing back)
    this.id = StringUtils.isBlank(this.id) ? GrouperUuid.getUuid() : this.id;
    
    //add to the database
    HibernateSession.bySqlStatic().executeSql("insert into file_mgr_file (id, name, folder_id) values (?, ?, ?)", 
        GrouperUtil.toListObject(this.id, this.name, this.folderId));
        
    if (resetRegistry) {
      PocFileManagerRequestContainer.retrieveFromRequestOrCreate().initFromDbIfNeeded(true);
    }
  }
  
  
  
}
