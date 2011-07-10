package edu.internet2.middleware.grouper.ui.poc.fileManager;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * file manager request container
 * @author mchyzer
 *
 */
public class PocFileManagerRequestContainer {

  /** act as subject id */
  private String actAsSubjectId;
  
  /**
   * act as subject id
   * @return act as subject id
   */
  public String getActAsSubjectId() {
    return this.actAsSubjectId;
  }

  /**
   * act as subject id
   * @param actAsSubjectId1
   */
  public void setActAsSubjectId(String actAsSubjectId1) {
    this.actAsSubjectId = actAsSubjectId1;
  }

  /**
   * retrieveFromSession, cannot be null
   * @return the app state in request scope
   */
  public static PocFileManagerRequestContainer retrieveFromRequestOrCreate() {
    HttpServletRequest httpServletRequest = GrouperUiFilter.retrieveHttpServletRequest();
    PocFileManagerRequestContainer pocFileManagerRequestContainer = 
      (PocFileManagerRequestContainer)httpServletRequest.getAttribute("pocFileManagerRequestContainer");
    if (pocFileManagerRequestContainer == null) {
      pocFileManagerRequestContainer = new PocFileManagerRequestContainer();
      pocFileManagerRequestContainer.storeToRequest();
    }
    return pocFileManagerRequestContainer;
  }

  /**
   * store to request scope
   */
  public void storeToRequest() {
    HttpServletRequest httpServletRequest = GrouperUiFilter.retrieveHttpServletRequest();
    httpServletRequest.setAttribute("pocFileManagerRequestContainer", this);
  }

  /**
   * all folders in system, this isnt efficient, but just get for each request
   */
  private Set<PocFileManagerFolder> allFolders = null;

  /**
   * all files in system, this isnt efficient, but just get for each request
   */
  private Set<PocFileManagerFile> allFiles = null;

  /**
   * all folders in system, this isnt efficient, but just get for each request, key is folder id
   */
  private Map<String, PocFileManagerFolder> allFoldersIdMap = null;

  /**
   * all folders in system, this isnt efficient, but just get for each request, key is grouper system name
   */
  private Map<String, PocFileManagerFolder> allFoldersSystemNameMap = null;

  
  
  /**
   * all folders in system, this isnt efficient, but just get for each request, key is grouper system name
   * @return all folders in system, this isnt efficient, but just get for each request, key is grouper system name
   */
  public Map<String, PocFileManagerFolder> getAllFoldersSystemNameMap() {
    return this.allFoldersSystemNameMap;
  }

  /**
   * all files in system, map from id to file
   */
  private Map<String, PocFileManagerFile> allFilesIdMap = null;

  /**
   * init from db if needed
   * @param resetEvenIfNotNeeded true to reset anyways e.g. if something was updated
   */
  public void initFromDbIfNeeded(boolean resetEvenIfNotNeeded) {
    
    if (this.allFiles == null || resetEvenIfNotNeeded) {

      //make sure grouper has its stuff
      PocFileManagerUtils.initGrouperIfNotInitted();
      
      this.allFolders = PocFileManagerFolder.retrieveFolders();
      this.allFiles = PocFileManagerFile.retrieveFiles();
      
      this.allFoldersIdMap = new HashMap<String, PocFileManagerFolder>();
      this.allFoldersSystemNameMap = new HashMap<String, PocFileManagerFolder>();
      this.allFilesIdMap = new HashMap<String, PocFileManagerFile>();
      
      //put in maps
      for (PocFileManagerFolder pocFileManagerFolder : GrouperUtil.nonNull(this.allFolders)) {
        this.allFoldersIdMap.put(pocFileManagerFolder.getId(), pocFileManagerFolder);
      }
      
      for (PocFileManagerFile pocFileManagerFile : GrouperUtil.nonNull(this.allFiles)) {
        this.allFilesIdMap.put(pocFileManagerFile.getId(), pocFileManagerFile);
      }
      //init parent folder folder
      for (PocFileManagerFolder pocFileManagerFolder : this.allFolders) {
        if (!StringUtils.isBlank(pocFileManagerFolder.getParentFolderId())) {
          pocFileManagerFolder.setParentFolder(this.allFoldersIdMap.get(pocFileManagerFolder.getParentFolderId()));
        }
      }
      //init parent file folder
      for (PocFileManagerFile pocFileManagerFile : this.allFiles) {
        pocFileManagerFile.setFolder(this.allFoldersIdMap.get(pocFileManagerFile.getFolderId()));
      }
      //lets init folders
      for (PocFileManagerFolder pocFileManagerFolder : this.allFolders) {
        pocFileManagerFolder.initParents();
      }
      //lets grouper system name map
      for (PocFileManagerFolder pocFileManagerFolder : this.allFolders) {
        this.allFoldersSystemNameMap.put(pocFileManagerFolder.getGrouperSystemName(), pocFileManagerFolder);
      }
      //lets add files to folder file list
      for (PocFileManagerFile pocFileManagerFile : this.allFiles) {
        pocFileManagerFile.getFolder().getFiles().add(pocFileManagerFile);
      }
    }
  }

  /**
   * 
   * @return all folders
   */
  public Set<PocFileManagerFolder> getAllFolders() {
    this.initFromDbIfNeeded(false);
    return this.allFolders;
  }

  /**
   * 
   * @return all files
   */
  public Set<PocFileManagerFile> getAllFiles() {
    this.initFromDbIfNeeded(false);
    return this.allFiles;
  }

  /**
   * all files in system, map from id to file
   * @return all files in system, map from id to file
   */
  public Map<String, PocFileManagerFolder> getAllFoldersIdMap() {
    this.initFromDbIfNeeded(false);
    return this.allFoldersIdMap;
  }

  /**
   * all files in system, map from id to file
   * @return all files in system, map from id to file
   */
  public Map<String, PocFileManagerFile> getAllFilesIdMap() {
    this.initFromDbIfNeeded(false);
    return this.allFilesIdMap;
  }
  
  
  
}
