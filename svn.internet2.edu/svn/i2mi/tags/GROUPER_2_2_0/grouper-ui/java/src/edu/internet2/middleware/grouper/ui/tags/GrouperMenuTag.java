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
/*
 * Copyright (C) 2004-2007 University Corporation for Advanced Internet Development, Inc.
 * Copyright (C) 2004-2007 The University Of Pennsylvania
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under
 * the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package edu.internet2.middleware.grouper.ui.tags;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.SimpleTagSupport;

import org.apache.commons.lang.StringUtils;

/**
 * <pre>
 * This will generate a menu.  You need something to attach the context click to, e.g.
 * 
 * &lt;a id="advancedLink" href="#" class="smallLink" onclick="this.oncontextmenu(event); return false"&gt;Advanced&lt;/a&gt;
 * 
 * </pre>
 * @author mchyzer
 *
 */
public class GrouperMenuTag extends SimpleTagSupport {

  /**
   * the jquery handle (e.g. #someId) which this menu should be attached to.  note
   * that any element you are attaching to must have an id attribute defined
   */
  private String contextZoneJqueryHandle;
  
  /**
   * the jquery handle (e.g. #someId) which this menu should be attached to.  note
   * that any element you are attaching to must have an id attribute defined
   * @param contextZoneJqueryHandle1 the contextZoneJqueryHandle to set
   */
  public void setContextZoneJqueryHandle(String contextZoneJqueryHandle1) {
    this.contextZoneJqueryHandle = contextZoneJqueryHandle1;
  }

  /** true if context menu, false if not */
  private boolean contextMenu;
  
  /**
   * true if context menu, false if not
   * @param contextMenu1 the contextMenu to set
   */
  public void setContextMenu(boolean contextMenu1) {
    this.contextMenu = contextMenu1;
  }

  /** the operation called to define the structure of the menu */
  private String structureOperation;
  
  /**
   * the operation called to define the structure of the menu
   * @param structureOperation1 the structureOperation to set
   */
  public void setStructureOperation(String structureOperation1) {
    this.structureOperation = structureOperation1;
  }

  /** when events occur (onclick), then that operation is called via ajax */
  private String operation;
  
  /**
   * when events occur (onclick), then that operation is called via ajax
   * @param operation1 the operation to set
   */
  public void setOperation(String operation1) {
    this.operation = operation1;
  }
  /** the id of the HTML element of the menu */
  private String menuId;
  
  /**
   * the id of the HTML element of the menu
   * @param menuId1 the menuId to set
   */
  public void setMenuId(String menuId1) {
    this.menuId = menuId1;
  }
  /**
   * init fields on construct
   */
  public GrouperMenuTag() {
  }

  /**
   * @see javax.servlet.jsp.tagext.SimpleTagSupport#doTag()
   */
  @Override
  public void doTag() throws JspException, IOException {

    StringBuilder result = new StringBuilder();
    
//    <span id="advancedMenu" ></span>
//    <script type="text/javascript">
//    guiInitDhtmlxMenu("advancedMenu", "SimpleMembershipUpdate.advancedMenu", 
//        "SimpleMembershipUpdate.advancedMenuStructure", true, "#advancedLink");
//    </script>
    
    //if it was shorthand, prefix with the full path
    if (!StringUtils.contains(this.operation, "/")) {
      this.operation = "../app/" + this.operation;
    }
    if (!StringUtils.contains(this.structureOperation, "/")) {
      this.structureOperation = "../app/" + this.structureOperation;
    }
    
    result.append("<span id=\"").append(this.menuId).append("\"");
    result.append("></span>\n");
    result.append("<script type=\"text/javascript\"> \n");
    result.append("guiInitDhtmlxMenu('").append(this.menuId).append("', '")
      .append(this.operation).append("', '").append(this.structureOperation)
      .append("', ").append(this.contextMenu).append(", '");
    result.append(this.contextZoneJqueryHandle).append("' );\n");
    result.append("</script>\n");

    this.getJspContext().getOut().print(result.toString());
  }

}
