/*******************************************************************************
 * Copyright 2012 Internet2
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package edu.internet2.middleware.grouper.ui.poc.fileManager;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.subj.SubjectHelper;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

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
  private List<PocFileManagerFolder> allFolders = null;

  /**
   * all files in system, this isnt efficient, but just get for each request
   */
  private List<PocFileManagerFile> allFiles = null;

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
  public List<PocFileManagerFolder> getAllFolders() {
    this.initFromDbIfNeeded(false);
    return this.allFolders;
  }

  /**
   * 
   * @return all files
   */
  public List<PocFileManagerFile> getAllFiles() {
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
  
  /**
   * get all backdoor subjects
   * @return all backdoor subjects
   */
  public Set<Subject> getAllBackdoorSubjects() {
    Set<Subject> subjects = new LinkedHashSet<Subject>();
    subjects.add(SubjectFinder.findRootSubject());
    Set<Subject> testSubjects = GrouperUtil.nonNull(SubjectFinder.findAll("test.subject."));
    SubjectHelper.sortByDescription(testSubjects);
    subjects.addAll(testSubjects);
    return subjects;
  }
  
  /** logger */
  private static final Log LOG = GrouperUtil.getLog(PocFileManagerRequestContainer.class);

  /**
   * see if has allowed folders
   * @return if has allowed folders
   */
  public boolean isHasAllowedFolders() {
    return GrouperUtil.nonNull(this.getAllAllowedFolders()).size() > 0;
  }
  
  /**
   * see if has allowed create folders
   * @return if has allowed create folders
   */
  public boolean isHasAllowedCreateFolders() {
    return GrouperUtil.nonNull(this.getAllowedCreateFolders()).size() > 0;
  }
  
  /**
   * get all allowed create folders
   * @return all allowed create folders
   */
  public Set<PocFileManagerFolder> getAllowedCreateFolders() {
    
    this.initFromDbIfNeeded(false);
    
    Set<String> readSystemNames = PocFileManagerSessionContainer.retrieveFromSessionOrCreate().getGrouperPermissionsCreateSystemNames();
    
    Set<PocFileManagerFolder> result = new TreeSet<PocFileManagerFolder>();
    
    for (String systemName : GrouperUtil.nonNull(readSystemNames)) {
      PocFileManagerFolder pocFileManagerFolder = this.getAllFoldersSystemNameMap().get(systemName);
      if (pocFileManagerFolder == null) {
        LOG.error("Why is folder in a system name, but not in the local DB? " + systemName);
      } else {
        result.add(pocFileManagerFolder);
      }
    }
    
    return result;
  }
  

  /**
   * get all allowed folders
   * @return all allowed folders
   */
  public Set<PocFileManagerFolder> getAllAllowedFolders() {
    
    this.initFromDbIfNeeded(false);
    
    Set<String> readSystemNames = PocFileManagerSessionContainer.retrieveFromSessionOrCreate().getGrouperPermissionsReadSystemNames();
    
    Set<PocFileManagerFolder> result = new TreeSet<PocFileManagerFolder>();
    
    for (String systemName : GrouperUtil.nonNull(readSystemNames)) {
      PocFileManagerFolder pocFileManagerFolder = this.getAllFoldersSystemNameMap().get(systemName);
      if (pocFileManagerFolder == null) {
        LOG.error("Why is folder in a system name, but not in the local DB? " + systemName);
      } else {
        result.add(pocFileManagerFolder);
      }
    }
    
    return result;
  }
  
}
