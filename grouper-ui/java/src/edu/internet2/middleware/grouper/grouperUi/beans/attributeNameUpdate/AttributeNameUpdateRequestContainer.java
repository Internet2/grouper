/*
 * @author mchyzer
 * $Id: SimpleMembershipUpdateContainer.java,v 1.4 2009-11-02 08:50:40 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.grouperUi.beans.attributeNameUpdate;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiMember;
import edu.internet2.middleware.grouper.privs.PrivilegeSubjectContainer;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouper.ui.tags.TagUtils;
import edu.internet2.middleware.grouper.ui.util.MapWrapper;
import edu.internet2.middleware.grouper.util.GrouperUtil;



/**
 * bean for simple attribute update.  holds all state for this module
 */
@SuppressWarnings("serial")
public class AttributeNameUpdateRequestContainer implements Serializable {

  /**
   * store to session scope
   */
  public void storeToRequest() {
    HttpServletRequest httpServletRequest = GrouperUiFilter.retrieveHttpServletRequest();
    httpServletRequest.setAttribute("attributeNameUpdateRequestContainer", this);
  }

  /**
   * retrieveFromSession, cannot be null
   * @return the app state in request scope
   */
  public static AttributeNameUpdateRequestContainer retrieveFromRequestOrCreate() {
    HttpServletRequest httpServletRequest = GrouperUiFilter.retrieveHttpServletRequest();
    AttributeNameUpdateRequestContainer attributeNameUpdateRequestContainer = 
      (AttributeNameUpdateRequestContainer)httpServletRequest.getAttribute("attributeNameUpdateRequestContainer");
    if (attributeNameUpdateRequestContainer == null) {
      attributeNameUpdateRequestContainer = new AttributeNameUpdateRequestContainer();
      attributeNameUpdateRequestContainer.storeToRequest();
    }
    return attributeNameUpdateRequestContainer;
  }

  /** 
   * text bean
   * @return text bean
   */
  public AttributeNameUpdateText getText() {
    return AttributeNameUpdateText.retrieveSingleton();
  }
  


}
