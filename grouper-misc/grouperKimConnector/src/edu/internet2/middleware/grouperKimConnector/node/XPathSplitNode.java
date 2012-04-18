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

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.kuali.rice.core.exception.RiceRuntimeException;
import org.kuali.rice.kew.engine.RouteContext;
import org.kuali.rice.kew.engine.RouteHelper;
import org.kuali.rice.kew.engine.node.RouteNode;
import org.kuali.rice.kew.engine.node.RouteNodeInstance;
import org.kuali.rice.kew.engine.node.SplitNode;
import org.kuali.rice.kew.engine.node.SplitResult;
import org.kuali.rice.kew.routeheader.DocumentContent;
import org.kuali.rice.kew.util.XmlHelper;
import org.kuali.rice.kns.workflow.WorkflowUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.logging.Log;

/**
 * This is a generic split node that takes an xpath expression and selects the branch that matches the text in the
 * document content for that xpath expression.
 * <pre>
 *       &lt;split name="routeOnBehalfSplit"&gt;
 *        &lt;activationType&gt;P&lt;/activationType&gt;
 *        &lt;mandatoryRoute&gt;false&lt;/mandatoryRoute&gt;
 *        &lt;finalApproval&gt;false&lt;/finalApproval&gt;
 *        
 *        &lt;!-- can be true or false, if true, then will select first matching branch, default to false --&gt;
 *        &lt;pickOneResult&gt;true&lt;/pickOneResult&gt;
 *        &lt;expression&gt;
 *          &lt;expressionXpath&gt;/documentContent/applicationContent/data/version[@current = "true"]/field[@name = "privilegeChange"]/value&lt;/expressionXpath&gt;
 *          
 *          &lt!-- defaults to 'true' --&gt;
 *          &lt;expressionValue&gt;delete&lt;/expressionValue&gt;
 *          &lt;branchName&gt;leftBranch&lt;/branchName&gt;
 *        &lt;/expression&gt;
 *        &lt;expression&gt;
 *          &lt;expressionXpath&gt;/documentContent/applicationContent/data/version[@current = "true"]/field[@name = "privilegeChange"]/value&lt;/expressionXpath&gt;
 *          &lt;expressionValue&gt;add&lt;/expressionValue&gt;
 *          &lt;branchName&gt;addBranch&lt;/branchName&gt;
 *        &lt;/expression&gt;
 *        &lt;expressionElse&gt;
 *          &lt;branchName&gt;rightBranch&lt;/branchName&gt;          
 *        &lt;/expressionElse&gt;
 *        &lt;type&gt;edu.internet2.middleware.grouperKimConnector.node.XPathSplitNode&lt;/type&gt;
 *      &lt;/split&gt;
 *      
 * </pre>
 */
public class XPathSplitNode implements SplitNode {

  /**
   * logger
   */
  private static final Log LOG = GrouperClientUtils.retrieveLog(XPathSplitNode.class);

  /**
   * @see org.kuali.rice.kew.engine.node.SplitNode#process(org.kuali.rice.kew.engine.RouteContext, org.kuali.rice.kew.engine.RouteHelper)
   */
  public SplitResult process(RouteContext context, RouteHelper helper) throws Exception {
    if (context == null)
      throw new RuntimeException("Unable to find document context");
    
    RouteNodeInstance routeNodeInstance = context == null ? null : context.getNodeInstance();
    RouteNode routeNode = routeNodeInstance == null ? null : routeNodeInstance.getRouteNode();
    
    DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();

    String routeNodeXml = routeNode == null ? null : routeNode.getContentFragment();
    
    try {
      Document routeNodeDocument = GrouperClientUtils.isBlank(routeNodeXml) ? null : db.parse(new InputSource(new StringReader(routeNodeXml))); 
      DocumentContent documentContent = context == null ? null : context.getDocumentContent();
      Document document = documentContent == null ? null : documentContent.getDocument();
      XPath xpath = document == null ? null : WorkflowUtils.getXPath(document);
  
      NodeList expressions = routeNodeDocument == null ? null : (NodeList) xpath.evaluate("//expression", routeNodeDocument, XPathConstants.NODESET);
      String expressionElseBranchName = routeNodeDocument == null ? null : evaluateXPathExpression(routeNodeDocument, "//expressionElse/branchName");
      String pickOneResult = routeNodeDocument == null ? null : evaluateXPathExpression(routeNodeDocument, "//pickOneResult");
      boolean pickOneResultBoolean = false;

      if (!GrouperClientUtils.isBlank(pickOneResult)) {
        
        if ("true".equalsIgnoreCase(pickOneResult) || "t".equalsIgnoreCase(pickOneResult) 
            || "y".equalsIgnoreCase(pickOneResult) || "yes".equalsIgnoreCase(pickOneResult)) {
          pickOneResultBoolean = true;
        } else if ("false".equalsIgnoreCase(pickOneResult) || "f".equalsIgnoreCase(pickOneResult) 
            || "n".equalsIgnoreCase(pickOneResult) || "no".equalsIgnoreCase(pickOneResult)) {
          pickOneResultBoolean = false;
        } else {
          //invalid value
          throw new RuntimeException("Invalid pickOneResult: '" + pickOneResult + "'");
        }
      }

      if (LOG.isDebugEnabled()) {
        LOG.debug("pickOneResult is: '" + pickOneResult + "', pickOneResultBoolean is: '" + pickOneResultBoolean + "'");
      }

      List<String> branchNamesToFollow = new ArrayList<String>();
      
      if (expressions != null) {
        for (int i=0;i<expressions.getLength();i++) {
          Node expressionNode = expressions.item(i);
          String expressionXml = XmlHelper.writeNode(expressionNode);
          Document expressionDocument = db.parse(new InputSource(new StringReader(expressionXml))); 
  
          String expressionXpath = evaluateXPathExpression(expressionDocument, "//expressionXpath");
          String expectedValueFromDoctype = evaluateXPathExpression(expressionDocument, "//expressionValue");
          String branchName = evaluateXPathExpression(expressionDocument, "//branchName");
          
          //see what the xpath is
          String evaluatedValue = evaluateXPathExpression(document, expressionXpath);
          
          if (GrouperClientUtils.equals(expectedValueFromDoctype, evaluatedValue)) {
            if (LOG.isDebugEnabled()) {
              LOG.debug("Found match in XPathSplitNode for " + expressionXpath + ", expected and got: '" 
                  + expectedValueFromDoctype + "', returning branch: '" + branchName + "'");
            }
            branchNamesToFollow.add(branchName);
          } else {
            if (LOG.isDebugEnabled()) {
              LOG.debug("Did not match in XPathSplitNode for " + expressionXpath + ", expected '" + expectedValueFromDoctype + "' but got: '" 
                  + evaluatedValue + "', not returning branch: '" + branchName + "'");
            }
            
          }
          
        }
        
      }
      if (!GrouperClientUtils.isBlank(expressionElseBranchName) && branchNamesToFollow.size() == 0) {
        if (LOG.isDebugEnabled()) {
          LOG.debug("Returning else value in XPathSplitNode: " + expressionElseBranchName);
        }
        
        branchNamesToFollow.add(expressionElseBranchName);
      }
      if (branchNamesToFollow.size() > 0) {
        
        //are we supposed to only have one?
        if (pickOneResultBoolean && branchNamesToFollow.size() > 1) {
          if (LOG.isDebugEnabled()) {
            LOG.debug("Picking the first result out of " + branchNamesToFollow.size());
          }
          String firstOne = branchNamesToFollow.get(0);
          branchNamesToFollow.clear();
          branchNamesToFollow.add(firstOne);
        }
        return new SplitResult(branchNamesToFollow);
      }
    } catch (RuntimeException re) {
      LOG.warn("Problem with routeNode: " + routeNodeXml, re);
      throw re;
    }      
    
    LOG.error("Could not find a result for splitnode, and does not contain an expressionElse: " + routeNodeXml);
    throw new RuntimeException("Could not find a result for splitnode, and does not contain an expressionElse");
  }

  /**
   * evaluate a string xpath expression with an explicit error message
   * @param document
   * @param expression
   * @return the string
   * @throws XPathExpressionException
   */
  protected String evaluateXPathExpression(Document document, String expression) throws XPathExpressionException {

    try {
      String xstreamSafeXPath = WorkflowUtils.xstreamSafeXPath(expression);

      return GrouperClientUtils.trim((String) WorkflowUtils.getXPath(document).evaluate(xstreamSafeXPath, document, XPathConstants.STRING));
    } catch (RuntimeException re) {
      throw new RiceRuntimeException("Error when attempting to parse Document Type content fragment for property name: " + expression, re);
    }
  }


}

