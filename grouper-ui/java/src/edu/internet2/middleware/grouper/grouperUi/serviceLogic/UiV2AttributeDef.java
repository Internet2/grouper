/*******************************************************************************
 * Copyright 2014 Internet2
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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefFinder;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiAttributeDef;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiResponseJs;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction.GuiMessageType;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.AttributeDefContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.GrouperRequestContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.TextContainer;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.Privilege;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * operations in the stem screen
 * @author mchyzer
 *
 */
public class UiV2AttributeDef {

  /** logger */
  protected static final Log LOG = LogFactory.getLog(UiV2AttributeDef.class);

  /**
   * results from retrieving results
   *
   */
  public static class RetrieveAttributeDefHelperResult {

    /**
     * attributeDef
     */
    private AttributeDef attributeDef;

    /**
     * attributedef
     * @return attributedef
     */
    public AttributeDef getAttributeDef() {
      return this.attributeDef;
    }

    /**
     * attributeDef
     * @param attributeDef1
     */
    public void setAttributeDef(AttributeDef attributeDef1) {
      this.attributeDef = attributeDef1;
    }
    
    /**
     * if added error to screen
     */
    private boolean addedError;

    /**
     * if added error to screen
     * @return if error
     */
    public boolean isAddedError() {
      return this.addedError;
    }

    /**
     * if added error to screen
     * @param addedError1
     */
    public void setAddedError(boolean addedError1) {
      this.addedError = addedError1;
    }
    
    
    
  }

  /**
   * get the attributeDef from the request where the attributeDef is required and require privilege is either needed or not
   * @param request
   * @param requireStemPrivilege
   * @return the stem finder result
   */
  public static RetrieveAttributeDefHelperResult retrieveStemHelper(HttpServletRequest request, 
      Privilege requireAttributeDefPrivilege) {
    return retrieveAttributeDefHelper(request, requireAttributeDefPrivilege, true);
  }

  /**
   * get the attribute def from the request
   * @param request
   * @param requirePrivilege
   * @param requireAttributeDef
   * @return the stem finder result
   */
  public static RetrieveAttributeDefHelperResult retrieveAttributeDefHelper(HttpServletRequest request, 
      Privilege requirePrivilege, boolean requireAttributeDef) {

    //initialize the bean
    GrouperRequestContainer grouperRequestContainer = GrouperRequestContainer.retrieveFromRequestOrCreate();
    
    AttributeDefContainer attributeDefContainer = grouperRequestContainer.getAttributeDefContainer();
    
    RetrieveAttributeDefHelperResult result = new RetrieveAttributeDefHelperResult();

    AttributeDef attributeDef = null;

    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();

    String attributeDefId = request.getParameter("attributeDefId");
    String attributeDefIndex = request.getParameter("attributeDefIndex");
    String nameOfAttributeDef = request.getParameter("nameOfAttributeDef");
    
    boolean addedError = false;
    
    if (!StringUtils.isBlank(attributeDefId)) {
      attributeDef = AttributeDefFinder.findById(attributeDefId, false);
    } else if (!StringUtils.isBlank(nameOfAttributeDef)) {
      attributeDef = AttributeDefFinder.findByName(nameOfAttributeDef, false);
    } else if (!StringUtils.isBlank(attributeDefIndex)) {
      long idIndex = GrouperUtil.longValue(attributeDefIndex);
      attributeDef = AttributeDefFinder.findByIdIndexSecure(idIndex, false, null);
    } else {
      
      if (!requireAttributeDef) {
        return result;
      }
      
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
          TextContainer.retrieveFromRequest().getText().get("attributeDefCantFindAttributeDefId")));
      addedError = true;
    }

    
    if (attributeDef != null) {
      attributeDefContainer.setGuiAttributeDef(new GuiAttributeDef(attributeDef));      
      boolean privsOk = true;

      if (requirePrivilege != null) {
        if (requirePrivilege.equals(AccessPrivilege.ADMIN)) {
          if (!attributeDefContainer.isCanAdmin()) {
            guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
                TextContainer.retrieveFromRequest().getText().get("attributeDefNotAllowedToAdminAttributeDef")));
            addedError = true;
            privsOk = false;
          }
        } else if (requirePrivilege.equals(AccessPrivilege.VIEW)) {
          if (!attributeDefContainer.isCanView()) {
            guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
                TextContainer.retrieveFromRequest().getText().get("attributeDefNotAllowedToViewAttributeDef")));
            addedError = true;
            privsOk = false;
          }
        } else if (requirePrivilege.equals(AccessPrivilege.READ)) {
          if (!attributeDefContainer.isCanRead()) {
            guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
                TextContainer.retrieveFromRequest().getText().get("attributeDefNotAllowedToReadAttributeDef")));
            addedError = true;
            privsOk = false;
          }
        } else if (requirePrivilege.equals(AccessPrivilege.UPDATE)) {
          if (!attributeDefContainer.isCanUpdate()) {
            guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
                TextContainer.retrieveFromRequest().getText().get("attributeDefNotAllowedToUpdateAttributeDef")));
            addedError = true;
            privsOk = false;
          }
        }  
      }
      
      if (privsOk) {
        result.setAttributeDef(attributeDef);
      }

    } else {
      
      if (!addedError && (!StringUtils.isBlank(attributeDefId) || !StringUtils.isBlank(nameOfAttributeDef) || !StringUtils.isBlank(attributeDefIndex))) {
        result.setAddedError(true);
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
            TextContainer.retrieveFromRequest().getText().get("groupCantFindGroup")));
        addedError = true;
      }
      
    }
    result.setAddedError(addedError);
  
    //go back to the main screen, cant find group
    if (addedError) {
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/index/indexMain.jsp"));
    }

    return result;
    
  }

}
