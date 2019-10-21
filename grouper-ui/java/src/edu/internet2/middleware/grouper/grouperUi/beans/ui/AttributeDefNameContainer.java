package edu.internet2.middleware.grouper.grouperUi.beans.ui;

import java.util.Set;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiAttributeAssignFinderResults;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiAttributeDefName;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiPaging;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouper.ui.util.GrouperUiUserData;
import edu.internet2.middleware.grouper.userData.GrouperUserDataApi;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

/**
 * attribute definition name container in new ui
 * @author vsachdeva
 */
public class AttributeDefNameContainer {
  
  /**
   * gui version of attribute def name user is working on
   */
  private GuiAttributeDefName guiAttributeDefName;
  
  /**
   * owners of this attribute def name
   */
  private GuiAttributeAssignFinderResults guiAttributeAssignFinderResults;
  
  /**
   * keep track of the paging
   */
  private GuiPaging guiPaging = null;
  

  /**
   * gui version of attribute def name user is working on
   * @return
   */
  public GuiAttributeDefName getGuiAttributeDefName() {
    return guiAttributeDefName;
  }

  /**
   * gui version of attribute def name user is working on
   * @param guiAttributeDefName
   */
  public void setGuiAttributeDefName(GuiAttributeDefName guiAttributeDefName) {
    this.guiAttributeDefName = guiAttributeDefName;
  }
  
  /**
   * if the attribute def name is a favorite for the logged in user
   */
  private Boolean favorite;

  /**
   * if the attribute def name is a favorite for the logged in user
   * @return if favorite
   */
  public boolean isFavorite() {
    
    if (this.favorite == null) {
      
      final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
  
      this.favorite = (Boolean)GrouperSession.callbackGrouperSession(
          GrouperSession.staticGrouperSession().internal_getRootSession(), new GrouperSessionHandler() {
            
            @Override
            public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
              
              Set<AttributeDefName> favorites = GrouperUtil.nonNull(
                  GrouperUserDataApi.favoriteAttributeDefNames(GrouperUiUserData.grouperUiGroupNameForUserData(), loggedInSubject));
              return favorites.contains(AttributeDefNameContainer.this.getGuiAttributeDefName().getAttributeDefName());
                  
            }
          });
    }
    
    return this.favorite;
  }

  /**
   * owners of this attribute def name
   * @return
   */
  public GuiAttributeAssignFinderResults getGuiAttributeAssignFinderResults() {
    return guiAttributeAssignFinderResults;
  }

  /**
   * owners of this attribute def name
   * @param guiAttributeAssignFinderResults
   */
  public void setGuiAttributeAssignFinderResults(GuiAttributeAssignFinderResults guiAttributeAssignFinderResults) {
    this.guiAttributeAssignFinderResults = guiAttributeAssignFinderResults;
  }

  /**
   * keep track of the paging
   * @return
   */
  public GuiPaging getGuiPaging() {
    
    if (this.guiPaging == null) {
      this.guiPaging = new GuiPaging();
    }
    return this.guiPaging;
  }

  /**
   * keep track of the paging
   * @param guiPaging
   */
  public void setGuiPaging(GuiPaging guiPaging) {
    this.guiPaging = guiPaging;
  }


}
