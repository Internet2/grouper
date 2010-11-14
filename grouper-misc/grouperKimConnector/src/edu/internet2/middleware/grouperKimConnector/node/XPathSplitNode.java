package edu.internet2.middleware.grouperKimConnector.node;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.lang.StringUtils;
import org.kuali.rice.core.exception.RiceRuntimeException;
import org.kuali.rice.core.util.XmlJotter;
import org.kuali.rice.kew.engine.RouteContext;
import org.kuali.rice.kew.engine.RouteHelper;
import org.kuali.rice.kew.engine.node.RouteNode;
import org.kuali.rice.kew.engine.node.RouteNodeInstance;
import org.kuali.rice.kew.engine.node.RouteNodeUtils;
import org.kuali.rice.kew.engine.node.SplitNode;
import org.kuali.rice.kew.engine.node.SplitResult;
import org.kuali.rice.kew.routeheader.DocumentContent;
import org.kuali.rice.kew.rule.xmlrouting.XPathHelper;
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
          &lt;activationType&gt;P&lt;/activationType&gt;
          &lt;mandatoryRoute&gt;false&lt;/mandatoryRoute&gt;
          &lt;finalApproval&gt;false&lt;/finalApproval&gt;
          &lt;expression&gt;
            &lt;expressionXpath&gt;/documentContent/applicationContent/data/version[@current = "true"]/field[@name = "privilegeChange"]/value&lt;/expressionXpath&gt;
            &lt;expressionValue&gt;delete&lt;/expressionValue&gt;
            &lt;routeNode&gt;leftBranch&lt;/routeNode&gt;
          &lt;/expression&gt;
          &lt;expressionElse&gt;
            &lt;routeNode&gt;rightBranch&lt;/routeNode&gt;          
          &lt;/expressionElse&gt;
          &lt;type&gt;edu.internet2.middleware.grouperKimConnector.node.XPathSplitNode&lt;/type&gt;
        &lt;/split&gt;

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
    
    DocumentContent documentContent = context.getDocumentContent();
    Document document = documentContent == null ? null : documentContent.getDocument();
    XPath xpath = WorkflowUtils.getXPath(document);
    DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
    //XmlHelper.writeNode(node)
    RouteNodeUtils.getValueOfCustomProperty(routeNode, "expression");
    
    NodeList expressions = (NodeList) xpath.evaluate("//expression", document, XPathConstants.NODESET);
    String expressionElseRouteNode = xpath.evaluate("//expressionElse/routeNode", document);
    
    String routeNodeContentFragment = routeNode.getContentFragment();
    if (!StringUtils.isBlank(routeNodeContentFragment)) {
        Document routeNodeContentDocument = db.parse(new InputSource(new StringReader(routeNodeContentFragment))); 
        if (expressions != null) {
          for (int i=0;i<expressions.getLength();i++) {
            Node node = expressions.item(i);
            String expressionXpath = null;
            try {
              expressionXpath = GrouperClientUtils.trimToEmpty(XPathHelper.newXPath().evaluate("//expressionXpath", routeNodeContentDocument));
            } catch (Exception e) {
              throw new RiceRuntimeException("Error when attempting to parse Document Type content fragment for property name: expressionXpath", e);
            }
            String expectedValueFromDoctype = null;
            try {
              expectedValueFromDoctype = GrouperClientUtils.trimToEmpty(XPathHelper.newXPath().evaluate("//expressionValue", routeNodeContentDocument));
            } catch (Exception e) {
              throw new RiceRuntimeException("Error when attempting to parse Document Type content fragment for property name: expressionValue", e);
            }
            
            String routeNodeName = null;
            try {
              routeNodeName = GrouperClientUtils.trimToEmpty(XPathHelper.newXPath().evaluate("//routeNodeName", routeNodeContentDocument));
            } catch (Exception e) {
              throw new RiceRuntimeException("Error when attempting to parse Document Type content fragment for property name: routeNodeName", e);
            }
            
            //see what the xpath is
            String evaluatedValue = evaluateXPathExpression(document, expressionXpath);
            
            if (GrouperClientUtils.equals(expectedValueFromDoctype, evaluatedValue)) {
              if (LOG.isDebugEnabled()) {
                LOG.debug("Found match in XPathSplitNode for " + expressionXpath + ", expected and got: '" 
                    + expectedValueFromDoctype + "', returning branch: '" + routeNodeName + "'");
              }
              return toSplitResult(routeNode, routeNodeName);
            }
            
            if (LOG.isDebugEnabled()) {
              LOG.debug("Did not find match in XPathSplitNode for " + expressionXpath + ", expected: '" 
                  + expectedValueFromDoctype + "', but got: '" + evaluatedValue + "'");
            }
          }
        }
        
        if (!GrouperClientUtils.isBlank(expressionElseRouteNode)) {
          if (LOG.isDebugEnabled()) {
            LOG.debug("Returning else value in XPathSplitNode: " + expressionElseRouteNode);
          }
          return toSplitResult(routeNode, expressionElseRouteNode);
        }
    }

    
    
    LOG.error("Could not find a result for splitnode, and does not contain an expressionElse: " + XmlJotter.jotNode(document, true));
    throw new RuntimeException("Could not find a result for splitnode, and does not contain an expressionElse");
  }

  protected SplitResult toSplitResult(RouteNode routeNode, String branchName) {
    List<String> branches = new ArrayList<String>();
    branches.add(branchName);
    return new SplitResult(branches);
  }
  
  protected String evaluateXPathExpression(Document document, String expression) throws XPathExpressionException {
    String xstreamSafeXPath = WorkflowUtils.xstreamSafeXPath(expression);
    //NodeList recipientIds = (NodeList) xpath.evaluate("//notification/recipients/recipient/recipientId", root, XPathConstants.NODESET);

    return (String) WorkflowUtils.getXPath(document).evaluate(xstreamSafeXPath, document, XPathConstants.STRING);
  }

}

