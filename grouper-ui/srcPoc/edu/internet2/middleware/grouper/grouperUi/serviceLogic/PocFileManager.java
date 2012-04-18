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
package edu.internet2.middleware.grouper.grouperUi.serviceLogic;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiResponseJs;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouper.ui.poc.fileManager.PocFileManagerFile;
import edu.internet2.middleware.grouper.ui.poc.fileManager.PocFileManagerFolder;
import edu.internet2.middleware.grouper.ui.poc.fileManager.PocFileManagerRequestContainer;
import edu.internet2.middleware.grouper.ui.poc.fileManager.PocFileManagerSessionContainer;
import edu.internet2.middleware.subject.Subject;

/**
 * file POC for penn state
 * @author mchyzer
 *
 */
public class PocFileManager {

  /*


CREATE TABLE `file_mgr_file` (
  `id` varchar(40) NOT NULL,
  `name` varchar(100) NOT NULL,
  `folder_id` varchar(40) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `NewIndex1` (`name`,`folder_id`),
  KEY `FK_file_mgr_file` (`folder_id`),
  CONSTRAINT `FK_file_mgr_file` FOREIGN KEY (`folder_id`) REFERENCES `file_mgr_folder` (`id`)
);


CREATE TABLE `file_mgr_folder` (
  `id` varchar(40) NOT NULL,
  `name` varchar(100) NOT NULL,
  `parent_folder_id` varchar(40) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `NewIndex1` (`name`,`parent_folder_id`),
  KEY `FK_file_mgr_folder` (`parent_folder_id`),
  CONSTRAINT `FK_file_mgr_folder` FOREIGN KEY (`parent_folder_id`) REFERENCES `file_mgr_folder` (`id`)
);

insert into `subject` (`subjectId`, `subjectTypeId`, `name`) values('test.subject.0','person','my name is test.subject.0');
insert into `subject` (`subjectId`, `subjectTypeId`, `name`) values('test.subject.1','person','my name is test.subject.1');
insert into `subject` (`subjectId`, `subjectTypeId`, `name`) values('test.subject.2','person','my name is test.subject.2');
insert into `subject` (`subjectId`, `subjectTypeId`, `name`) values('test.subject.3','person','my name is test.subject.3');
insert into `subject` (`subjectId`, `subjectTypeId`, `name`) values('test.subject.4','person','my name is test.subject.4');
insert into `subject` (`subjectId`, `subjectTypeId`, `name`) values('test.subject.5','person','my name is test.subject.5');
insert into `subject` (`subjectId`, `subjectTypeId`, `name`) values('test.subject.6','person','my name is test.subject.6');
insert into `subject` (`subjectId`, `subjectTypeId`, `name`) values('test.subject.7','person','my name is test.subject.7');
insert into `subject` (`subjectId`, `subjectTypeId`, `name`) values('test.subject.8','person','my name is test.subject.8');
insert into `subject` (`subjectId`, `subjectTypeId`, `name`) values('test.subject.9','person','my name is test.subject.9');
insert into `subjectattribute` (`subjectId`, `name`, `value`, `searchValue`) values('test.subject.0','description','description.test.subject.0','description.test.subject.0');
insert into `subjectattribute` (`subjectId`, `name`, `value`, `searchValue`) values('test.subject.0','email','test.subject.0@somewhere.someSchool.edu','test.subject.0@somewhere.someschool.edu');
insert into `subjectattribute` (`subjectId`, `name`, `value`, `searchValue`) values('test.subject.0','loginid','id.test.subject.0','id.test.subject.0');
insert into `subjectattribute` (`subjectId`, `name`, `value`, `searchValue`) values('test.subject.0','name','name.test.subject.0','name.test.subject.0');
insert into `subjectattribute` (`subjectId`, `name`, `value`, `searchValue`) values('test.subject.1','description','description.test.subject.1','description.test.subject.1');
insert into `subjectattribute` (`subjectId`, `name`, `value`, `searchValue`) values('test.subject.1','email','test.subject.1@somewhere.someSchool.edu','test.subject.1@somewhere.someschool.edu');
insert into `subjectattribute` (`subjectId`, `name`, `value`, `searchValue`) values('test.subject.1','loginid','id.test.subject.1','id.test.subject.1');
insert into `subjectattribute` (`subjectId`, `name`, `value`, `searchValue`) values('test.subject.1','name','name.test.subject.1','name.test.subject.1');
insert into `subjectattribute` (`subjectId`, `name`, `value`, `searchValue`) values('test.subject.2','description','description.test.subject.2','description.test.subject.2');
insert into `subjectattribute` (`subjectId`, `name`, `value`, `searchValue`) values('test.subject.2','email','test.subject.2@somewhere.someSchool.edu','test.subject.2@somewhere.someschool.edu');
insert into `subjectattribute` (`subjectId`, `name`, `value`, `searchValue`) values('test.subject.2','loginid','id.test.subject.2','id.test.subject.2');
insert into `subjectattribute` (`subjectId`, `name`, `value`, `searchValue`) values('test.subject.2','name','name.test.subject.2','name.test.subject.2');
insert into `subjectattribute` (`subjectId`, `name`, `value`, `searchValue`) values('test.subject.3','description','description.test.subject.3','description.test.subject.3');
insert into `subjectattribute` (`subjectId`, `name`, `value`, `searchValue`) values('test.subject.3','email','test.subject.3@somewhere.someSchool.edu','test.subject.3@somewhere.someschool.edu');
insert into `subjectattribute` (`subjectId`, `name`, `value`, `searchValue`) values('test.subject.3','loginid','id.test.subject.3','id.test.subject.3');
insert into `subjectattribute` (`subjectId`, `name`, `value`, `searchValue`) values('test.subject.3','name','name.test.subject.3','name.test.subject.3');
insert into `subjectattribute` (`subjectId`, `name`, `value`, `searchValue`) values('test.subject.4','description','description.test.subject.4','description.test.subject.4');
insert into `subjectattribute` (`subjectId`, `name`, `value`, `searchValue`) values('test.subject.4','email','test.subject.4@somewhere.someSchool.edu','test.subject.4@somewhere.someschool.edu');
insert into `subjectattribute` (`subjectId`, `name`, `value`, `searchValue`) values('test.subject.4','loginid','id.test.subject.4','id.test.subject.4');
insert into `subjectattribute` (`subjectId`, `name`, `value`, `searchValue`) values('test.subject.4','name','name.test.subject.4','name.test.subject.4');
insert into `subjectattribute` (`subjectId`, `name`, `value`, `searchValue`) values('test.subject.5','description','description.test.subject.5','description.test.subject.5');
insert into `subjectattribute` (`subjectId`, `name`, `value`, `searchValue`) values('test.subject.5','email','test.subject.5@somewhere.someSchool.edu','test.subject.5@somewhere.someschool.edu');
insert into `subjectattribute` (`subjectId`, `name`, `value`, `searchValue`) values('test.subject.5','loginid','id.test.subject.5','id.test.subject.5');
insert into `subjectattribute` (`subjectId`, `name`, `value`, `searchValue`) values('test.subject.5','name','name.test.subject.5','name.test.subject.5');
insert into `subjectattribute` (`subjectId`, `name`, `value`, `searchValue`) values('test.subject.6','description','description.test.subject.6','description.test.subject.6');
insert into `subjectattribute` (`subjectId`, `name`, `value`, `searchValue`) values('test.subject.6','email','test.subject.6@somewhere.someSchool.edu','test.subject.6@somewhere.someschool.edu');
insert into `subjectattribute` (`subjectId`, `name`, `value`, `searchValue`) values('test.subject.6','loginid','id.test.subject.6','id.test.subject.6');
insert into `subjectattribute` (`subjectId`, `name`, `value`, `searchValue`) values('test.subject.6','name','name.test.subject.6','name.test.subject.6');
insert into `subjectattribute` (`subjectId`, `name`, `value`, `searchValue`) values('test.subject.7','description','description.test.subject.7','description.test.subject.7');
insert into `subjectattribute` (`subjectId`, `name`, `value`, `searchValue`) values('test.subject.7','email','test.subject.7@somewhere.someSchool.edu','test.subject.7@somewhere.someschool.edu');
insert into `subjectattribute` (`subjectId`, `name`, `value`, `searchValue`) values('test.subject.7','loginid','id.test.subject.7','id.test.subject.7');
insert into `subjectattribute` (`subjectId`, `name`, `value`, `searchValue`) values('test.subject.7','name','name.test.subject.7','name.test.subject.7');
insert into `subjectattribute` (`subjectId`, `name`, `value`, `searchValue`) values('test.subject.8','description','description.test.subject.8','description.test.subject.8');
insert into `subjectattribute` (`subjectId`, `name`, `value`, `searchValue`) values('test.subject.8','email','test.subject.8@somewhere.someSchool.edu','test.subject.8@somewhere.someschool.edu');
insert into `subjectattribute` (`subjectId`, `name`, `value`, `searchValue`) values('test.subject.8','loginid','id.test.subject.8','id.test.subject.8');
insert into `subjectattribute` (`subjectId`, `name`, `value`, `searchValue`) values('test.subject.8','name','name.test.subject.8','name.test.subject.8');
insert into `subjectattribute` (`subjectId`, `name`, `value`, `searchValue`) values('test.subject.9','description','description.test.subject.9','description.test.subject.9');
insert into `subjectattribute` (`subjectId`, `name`, `value`, `searchValue`) values('test.subject.9','email','test.subject.9@somewhere.someSchool.edu','test.subject.9@somewhere.someschool.edu');
insert into `subjectattribute` (`subjectId`, `name`, `value`, `searchValue`) values('test.subject.9','loginid','id.test.subject.9','id.test.subject.9');
insert into `subjectattribute` (`subjectId`, `name`, `value`, `searchValue`) values('test.subject.9','name','name.test.subject.9','name.test.subject.9');


   */
  
  /**
   * index page
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void index(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    try {
      grouperSession = GrouperSession.start(loggedInSubject);
      
      PocFileManagerRequestContainer.retrieveFromRequestOrCreate();
      
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#topDiv", 
        "/WEB-INF/grouperUi/templates/poc/fileManagerTop.jsp"));

      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#bodyDiv", 
        "/WEB-INF/grouperUi/templates/poc/fileManagerIndex.jsp"));

      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#fileManagerEditPanel", 
        "/WEB-INF/grouperUi/templates/poc/fileManagerEditPanel.jsp"));

      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#filesAndFolders", 
        "/WEB-INF/grouperUi/templates/poc/fileManagerFilesAndFolders.jsp"));

    } finally {
      GrouperSession.stopQuietly(grouperSession); 
    }

  }

  /**
   * assign backdoor user
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void assignBackdoorUser(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {

    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    try {
      grouperSession = GrouperSession.start(loggedInSubject);
      

      PocFileManagerRequestContainer pocFileManagerRequestContainer = PocFileManagerRequestContainer.retrieveFromRequestOrCreate();
  
      String backdoorSubjectId = httpServletRequest.getParameter("backdoorSubjectId");
  
      pocFileManagerRequestContainer.setActAsSubjectId(StringUtils.trimToNull(backdoorSubjectId));
  
      //refresh the security, cache in session
      PocFileManagerSessionContainer.retrieveFromSessionOrCreate().initFromDbIfNeeded(true);
  
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#fileManagerEditPanel", 
        "/WEB-INF/grouperUi/templates/poc/fileManagerEditPanel.jsp"));

      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#filesAndFolders", 
        "/WEB-INF/grouperUi/templates/poc/fileManagerFilesAndFolders.jsp"));
    } finally {
      GrouperSession.stopQuietly(grouperSession); 
    }
  }

  /**
   * create folder
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void createFolder(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    try {
      grouperSession = GrouperSession.start(loggedInSubject);

      PocFileManagerRequestContainer pocFileManagerRequestContainer = PocFileManagerRequestContainer.retrieveFromRequestOrCreate();
  
      String folderId = httpServletRequest.getParameter("folderId");
      
      if (StringUtils.isBlank(folderId)) {
        throw new RuntimeException("Why is folderId blank?");
      }
      
      PocFileManagerFolder parentFolder = pocFileManagerRequestContainer.getAllFoldersIdMap().get(folderId);
      
      if (parentFolder == null || !pocFileManagerRequestContainer.getAllowedCreateFolders().contains(parentFolder)) {
        throw new RuntimeException("Why not allowed to edit?");
      }
            
      String objectName = httpServletRequest.getParameter("objectName");

      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      
      if (StringUtils.isBlank(objectName) || !objectName.matches("^[a-zA-Z0-9_]+$") || objectName.contains("__")) {
        
        guiResponseJs.addAction(GuiScreenAction.newAlert("Name must be non blank, alphanumeric, underscore, but not more than one underscore in a row"));
        return;
      }
      
      PocFileManagerFolder pocFileManagerFolder = new PocFileManagerFolder();
      pocFileManagerFolder.setParentFolderId(folderId);
      pocFileManagerFolder.setParentFolder(parentFolder);
      pocFileManagerFolder.setName(objectName);
      
      pocFileManagerFolder.initParents();
      
      pocFileManagerFolder.addFolder(true);
      
      //refresh the security, cache in session
      PocFileManagerSessionContainer.retrieveFromSessionOrCreate().initFromDbIfNeeded(true);
  
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#fileManagerEditPanel", 
        "/WEB-INF/grouperUi/templates/poc/fileManagerEditPanel.jsp"));

      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#filesAndFolders", 
        "/WEB-INF/grouperUi/templates/poc/fileManagerFilesAndFolders.jsp"));
      
      guiResponseJs.addAction(GuiScreenAction.newScript("$('#fileManagerMessageId').addClass('noteMessage')"));
      guiResponseJs.addAction(GuiScreenAction.newInnerHtml("#fileManagerMessageId", "Success: folder added"));

      //scroll to it and hide it after 5 seconds
      guiResponseJs.addAction(GuiScreenAction.newScript(
          "guiScrollTo('#fileManagerMessageId'); setTimeout('hideFileManagerMessage()', 5000);"));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession); 
    }

  }

  /**
   * create file
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void createFile(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    try {
      grouperSession = GrouperSession.start(loggedInSubject);
  
      PocFileManagerRequestContainer pocFileManagerRequestContainer = PocFileManagerRequestContainer.retrieveFromRequestOrCreate();
  
      String folderId = httpServletRequest.getParameter("folderId");
      
      if (StringUtils.isBlank(folderId)) {
        throw new RuntimeException("Why is folderId blank?");
      }
      
      PocFileManagerFolder parentFolder = pocFileManagerRequestContainer.getAllFoldersIdMap().get(folderId);
      
      if (parentFolder == null || !pocFileManagerRequestContainer.getAllowedCreateFolders().contains(parentFolder)) {
        throw new RuntimeException("Why not allowed to edit?");
      }
            
      String objectName = httpServletRequest.getParameter("objectName");
  
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      
      if (StringUtils.isBlank(objectName) || !objectName.matches("^[a-zA-Z0-9_.]+$") || objectName.contains("__")) {
        
        guiResponseJs.addAction(GuiScreenAction.newAlert("Name must be non blank, alphanumeric, underscore, but not more than one underscore in a row"));
        return;
      }
      
      PocFileManagerFile pocFileManagerFile = new PocFileManagerFile();
      pocFileManagerFile.setFolderId(folderId);
      pocFileManagerFile.setFolder(parentFolder);
      pocFileManagerFile.setName(objectName);
      
      pocFileManagerFile.addFile(true);
      
      //refresh the security, cache in session
      PocFileManagerSessionContainer.retrieveFromSessionOrCreate().initFromDbIfNeeded(true);
  
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#fileManagerEditPanel", 
        "/WEB-INF/grouperUi/templates/poc/fileManagerEditPanel.jsp"));
  
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#filesAndFolders", 
        "/WEB-INF/grouperUi/templates/poc/fileManagerFilesAndFolders.jsp"));
      
      guiResponseJs.addAction(GuiScreenAction.newScript("$('#fileManagerMessageId').addClass('noteMessage')"));
      guiResponseJs.addAction(GuiScreenAction.newInnerHtml("#fileManagerMessageId", "Success: file added"));
  
      //scroll to it and hide it after 5 seconds
      guiResponseJs.addAction(GuiScreenAction.newScript(
          "guiScrollTo('#fileManagerMessageId'); setTimeout('hideFileManagerMessage()', 5000);"));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession); 
    }
  
  }
}
