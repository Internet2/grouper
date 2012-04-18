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
package edu.internet2.middleware.grouperKimConnector.node;
 /*
 * Copyright 2007 The Kuali Foundation
 *
 * Licensed under the Educational Community License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.opensource.org/licenses/ecl1.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.util.ArrayList;
import java.util.List;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import org.kuali.rice.kew.engine.RouteContext;
import org.kuali.rice.kew.engine.RouteHelper;
import org.kuali.rice.kew.engine.node.SplitNode;
import org.kuali.rice.kew.engine.node.SplitResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.logging.Log;

/**
 * this is a custom split node which decides to go left or right
 * based on a radio in the form (if remove, go right, else go left)
 *
 * @author mchyzer
 *
 */
public class RemoveGoRightSplitNode implements SplitNode {

  /**
   * logger
   */
  private static final Log LOG = GrouperClientUtils.retrieveLog(RemoveGoRightSplitNode.class);

  /**
   * This overridden method ...
   *
   * @see org.kuali.rice.kew.engine.node.SplitNode#process(org.kuali.rice.kew.engine.RouteContext, org.kuali.rice.kew.engine.RouteHelper)
   */
  public SplitResult process(RouteContext routeContext, RouteHelper routeHelper) throws Exception {
    //System.out.println("In custom branch thing");
    Document document = routeContext.getDocumentContent().getDocument();
    Element root = document.getDocumentElement();
    XPath xpath = XPathFactory.newInstance().newXPath();
    String checkboxValue = null;
    List<String> branchNames = new ArrayList<String>();
    try {
      //Here is the doc: routeContext.getDocumentContent().getDocContent()
      //<documentContent>
      //  <applicationContent>
      //    <data edlName="sampleRouteOnBehalf.doctype">
      //      <version current="false" date="Mon Feb 15 14:54:20 EST 2010" version="0" />
      //      <version current="false" date="Mon Feb 15 14:54:24 EST 2010" version="1">
      //        <field name="privilegeChange">
      //          <value>remove</value>
      //        </field>
      //      </version>
      //     <version current="true" date="Mon Feb 15 14:56:25 EST 2010" version="5">
      //        <field name="privilegeChange">
      //          <value>remove</value>
      //        </field>
      //      </version>
      //    </data>
      //  </applicationContent>
      //</documentContent>
      //this xpath will give the value element for the field with name privilegeChange radio
      //in the version tag where current is true
      checkboxValue = xpath.evaluate(
          "/documentContent/applicationContent/data/version[@current = \"true\"]" +
          "/field[@name = \"privilegeChange\"]/value", root);
      if (GrouperClientUtils.equals("remove", checkboxValue)) {
        branchNames.add("rightBranch");
        //System.out.println("Going with right branch: '" + checkboxValue + "'");
      }
    } catch (Exception e) {
      LOG.error("error in split node", e);
      checkboxValue = null;
    }

    //if we didnt hit left, we are in right
    if (branchNames.size() == 0) {
      branchNames.add("leftBranch");
      //System.out.println("Going with left branch: '" + checkboxValue + "'");
    }

    return new SplitResult(branchNames);
  }

}
