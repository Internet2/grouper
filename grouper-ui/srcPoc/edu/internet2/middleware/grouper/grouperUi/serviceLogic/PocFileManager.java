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
  KEY `FK_file_mgr_file` (`folder_id`),
  CONSTRAINT `FK_file_mgr_file` FOREIGN KEY (`folder_id`) REFERENCES `file_mgr_folder` (`id`)
);


CREATE TABLE `file_mgr_folder` (
  `id` varchar(40) NOT NULL,
  `name` varchar(100) NOT NULL,
  `parent_folder_id` varchar(40) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_file_mgr_folder` (`parent_folder_id`),
  CONSTRAINT `FK_file_mgr_folder` FOREIGN KEY (`parent_folder_id`) REFERENCES `file_mgr_folder` (`id`)
);

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
