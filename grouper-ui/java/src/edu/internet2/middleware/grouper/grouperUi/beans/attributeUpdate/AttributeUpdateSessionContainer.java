/*
 * @author mchyzer
 * $Id: SimpleMembershipUpdateContainer.java,v 1.4 2009-11-02 08:50:40 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.grouperUi.beans.attributeUpdate;

import java.io.Serializable;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouper.ui.exceptions.NoSessionException;
import edu.internet2.middleware.grouper.ui.util.GrouperUiUtils;



/**
 * bean for simple attribute update.  holds all state for this module
 */
public class AttributeUpdateSessionContainer implements Serializable {

  /**
   * store to session scope
   */
  public void storeToSession() {
    HttpServletRequest httpServletRequest = GrouperUiFilter.retrieveHttpServletRequest();
    httpServletRequest.getSession().setAttribute("simpleAttributeUpdateContainer", this);
  }

  /**
   * retrieveFromSession, cannot be null
   * @return the app state in request scope
   */
  public static AttributeUpdateSessionContainer retrieveFromSession() {
    HttpServletRequest httpServletRequest = GrouperUiFilter.retrieveHttpServletRequest();
    HttpSession httpSession = httpServletRequest.getSession();
    AttributeUpdateSessionContainer simpleMembershipUpdateContainer = (AttributeUpdateSessionContainer)httpSession
      .getAttribute("simpleAttributeUpdateContainer");
    if (simpleMembershipUpdateContainer == null) {
      throw new NoSessionException(GrouperUiUtils.message("simpleAttributeUpdate.noContainer"));
    }
    return simpleMembershipUpdateContainer;
  }

  /** 
   * text bean
   * @return text bean
   */
  public AttributeUpdateText getText() {
    return AttributeUpdateText.retrieveSingleton();
  }
  
  
  
}
