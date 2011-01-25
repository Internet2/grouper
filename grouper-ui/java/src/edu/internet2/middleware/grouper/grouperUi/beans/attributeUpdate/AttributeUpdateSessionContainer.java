/*
 * @author mchyzer
 * $Id: SimpleMembershipUpdateContainer.java,v 1.4 2009-11-02 08:50:40 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.grouperUi.beans.attributeUpdate;

import java.io.File;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.cache.GrouperCache;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiGroup;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiMember;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouper.ui.exceptions.NoSessionException;
import edu.internet2.middleware.grouper.ui.tags.TagUtils;
import edu.internet2.middleware.grouper.ui.util.GrouperUiUtils;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;



/**
 * bean for simple membership update.  holds all state for this module
 */
public class AttributeUpdateSessionContainer implements Serializable {

  /**
   * store to session scope
   */
  public void storeToSession() {
    HttpServletRequest httpServletRequest = GrouperUiFilter.retrieveHttpServletRequest();
    httpServletRequest.getSession().setAttribute("simpleMembershipUpdateContainer", this);
  }

  /**
   * retrieveFromSession, cannot be null
   * @return the app state in request scope
   */
  public static AttributeUpdateSessionContainer retrieveFromSession() {
    HttpServletRequest httpServletRequest = GrouperUiFilter.retrieveHttpServletRequest();
    HttpSession httpSession = httpServletRequest.getSession();
    AttributeUpdateSessionContainer simpleMembershipUpdateContainer = (AttributeUpdateSessionContainer)httpSession
      .getAttribute("simpleMembershipUpdateContainer");
    if (simpleMembershipUpdateContainer == null) {
      throw new NoSessionException(GrouperUiUtils.message("simpleMembershipUpdate.noContainer"));
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
