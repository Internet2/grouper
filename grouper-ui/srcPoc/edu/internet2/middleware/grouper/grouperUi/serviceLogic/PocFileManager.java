package edu.internet2.middleware.grouper.grouperUi.serviceLogic;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiResponseJs;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouper.ui.poc.fileManager.PocFileManagerRequestContainer;
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
   * delete a limit
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void index(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    try {
      grouperSession = GrouperSession.start(loggedInSubject);
      
      PocFileManagerRequestContainer pocFileManagerRequestContainer = PocFileManagerRequestContainer.retrieveFromRequestOrCreate();


      
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#topDiv", 
        "/WEB-INF/grouperUi/templates/poc/fileManagerTop.jsp"));

      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#bodyDiv", 
        "/WEB-INF/grouperUi/templates/poc/fileManagerIndex.jsp"));

    } finally {
      GrouperSession.stopQuietly(grouperSession); 
    }

  }

  
}
