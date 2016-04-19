/*******************************************************************************
 * Copyright 2016 Internet2
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
 *******************************************************************************/
/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.tierApiAuthzServer.jsonTransform;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import edu.internet2.middleware.tierApiAuthzServer.jsonTransform.PwsNode.PwsNodeType;
import edu.internet2.middleware.tierApiAuthzServer.util.StandardApiServerUtils;
import edu.internet2.middleware.tierApiAuthzServerExt.org.apache.commons.logging.Log;
import edu.internet2.middleware.tierApiAuthzServerExt.org.apache.commons.logging.LogFactory;


/**
 * evaluate an expression against a node
 */
public class PwsNodeEvaluation {

  /** logger */
  private static final Log LOG = LogFactory.getLog(PwsNodeEvaluation.class);


  /**
   * evaluate an operation step on a node
   * @param currentNode
   * @param pwsOperationStep
   * @param autoCreate
   * @param pwsNodeEvaluationResult
   * @return the new node
   */
  public static PwsNode evaluate(PwsNode currentNode, PwsOperationStep pwsOperationStep, 
      boolean autoCreate, PwsNodeEvaluationResult pwsNodeEvaluationResult) {
    
    Map<String, Object> debugLog = LOG.isDebugEnabled() ? new LinkedHashMap<String, Object>() : null;

    try {
      if (LOG.isDebugEnabled()) {
        debugLog.put("currentNode", currentNode);
        debugLog.put("step", pwsOperationStep);
        debugLog.put("autoCreate", autoCreate);
      }
      
      PwsNode result = null;
      
      switch (pwsOperationStep.getPwsOperationStepEnum()) {
        
        case expressionLanguage:
          
          String script = pwsOperationStep.getOperationString();
          
          Map<String, Object> variableMap = new HashMap<String, Object>();
          variableMap.put("node", currentNode);
          
          String resultString = StandardApiServerUtils.substituteExpressionLanguage(script, variableMap, true, false, true, false);
          
          result = new PwsNode(PwsNodeType.string);
          result.setString(resultString);
          return result;
        
        case traverseArrayBySelector:
          
          if (LOG.isDebugEnabled()) {
            
            debugLog.put("selectorStepSize: ", StandardApiServerUtils.length(pwsOperationStep.getArraySelectorSteps()));
            debugLog.put("selector: ", StandardApiServerUtils.length(pwsOperationStep.getArraySelector()));
            
          }

          //selectors go against arrays
          currentNode.setArrayType(true);
          
          //selectors are objects
          currentNode.setPwsNodeType(PwsNodeType.object);
          
          
          PwsNodeEvaluationResult selectorResult = evaluateSelector(currentNode, pwsOperationStep.getArraySelectorSteps(), autoCreate, pwsOperationStep.getArraySelectorAttributeValue());

          if (LOG.isDebugEnabled()) {
            
            debugLog.put("selectorResult: ", selectorResult);
            
          }
          result = selectorResult.getPwsNode();
  
          if (LOG.isDebugEnabled()) {
            debugLog.put("node", result);
          }
  
          return result;

          
        case traverseArray:
  
          result = currentNode.retrieveField(pwsOperationStep.getFieldName());
  
          if (LOG.isDebugEnabled()) {
            debugLog.put("foundNode", result != null);
          }
  
          //doesnt have this field, add it, tell the result it is added
          if (result == null) {
            if (autoCreate) {
              pwsNodeEvaluationResult.setCreatedNode(true);
  
              //i dont think we really know the type at this point
              result = new PwsNode(PwsNodeType.object);
              result.setFromFieldName(pwsOperationStep.getFieldName());
              result.setFromNode(currentNode);
              currentNode.assignField(pwsOperationStep.getFieldName(), result);
              result.setArrayType(true);
            } else {
              //dont auto create, not there
              return null;
            }
          }
          if (LOG.isDebugEnabled()) {
            debugLog.put("node", result);
          }
  
          if (!result.isArrayType()) {
            if (autoCreate) {
              pwsNodeEvaluationResult.setChangedArrayType(true);
              result.setArrayType(true);
            } else {
              throw new RuntimeException("Not an array type: " 
                  + pwsOperationStep.getFieldName() + ", " + pwsOperationStep.getArrayIndex());
            }
          }
          
          //see if not enough there, and maybe auto create
          int createItemCount = (pwsOperationStep.getArrayIndex()+1) - result.getArrayLength();
          
          if (createItemCount > 0) {
            if (LOG.isDebugEnabled()) {
              debugLog.put("createItemCount", createItemCount);
            }
            if (autoCreate) {
              PwsNode currentArrayNode = null;
              //create some more nodes
              for (int i = 0; i<createItemCount; i++ ) {
                currentArrayNode = new PwsNode(result.getPwsNodeType());
                currentArrayNode.setFromNode(result);
                currentArrayNode.setFromFieldName(pwsOperationStep.getFieldName());
                result.addArrayItem(currentArrayNode);
              }
              if (LOG.isDebugEnabled()) {
                debugLog.put("return", currentArrayNode);
              }
  
              return currentArrayNode;
            }
            
            if (LOG.isDebugEnabled()) {
              debugLog.put("return", null);
            }
  
            return null;
          }
          PwsNode theResult = result.retrieveArrayItem(pwsOperationStep.getArrayIndex());
          if (LOG.isDebugEnabled()) {
            debugLog.put("theResult", theResult);
          }
          return theResult;
          
        case traverseField:

          //should be type object...
          if (currentNode.getPwsNodeType() != null) {
            result = currentNode.retrieveField(pwsOperationStep.getFieldName());
          }
          
          if (LOG.isDebugEnabled()) {
            debugLog.put("foundNode", result != null);
          }
  
          //doesnt have this field, add it, tell the result it is added
          if (result == null && autoCreate) {
  
            //if not initted, make it an object
            if (currentNode.getPwsNodeType() == null) {
              currentNode.setPwsNodeType(PwsNodeType.object);
            }
            
            pwsNodeEvaluationResult.setCreatedNode(true);
            result = new PwsNode(PwsNodeType.object);
            result.setFromFieldName(pwsOperationStep.getFieldName());
            result.setFromNode(currentNode);
            currentNode.assignField(pwsOperationStep.getFieldName(), result);
          }
  
          if (LOG.isDebugEnabled()) {
            debugLog.put("node", result);
          }
  
          return result;
        default:
          throw new RuntimeException("Not expecting PwsOperationStemEnum: " + pwsOperationStep.getPwsOperationStepEnum());
        
      }
    } finally {
      if (LOG.isDebugEnabled()) {
        LOG.debug(StandardApiServerUtils.mapToString(debugLog));
      }
    }
  }
  
  /**
   * evaluate steps against a node
   * @param pwsNode
   * @param pwsOperationSteps
   * @param autoCreate if should autocreate
   * @return the result
   */
  public static PwsNodeEvaluationResult evaluate(PwsNode pwsNode, 
      List<PwsOperationStep> pwsOperationSteps, boolean autoCreate) {
    
    PwsNodeEvaluationResult pwsNodeEvaluationResult = new PwsNodeEvaluationResult();
    
    PwsNode currentNode = pwsNode;

    for (PwsOperationStep pwsOperationStep : StandardApiServerUtils.nonNull(pwsOperationSteps)) {
      
      currentNode = evaluate(currentNode, pwsOperationStep, autoCreate, pwsNodeEvaluationResult);
      
      //if node is not found and not autocreate
      if (currentNode == null) {
        break;
      }
      
    }
    
    pwsNodeEvaluationResult.setPwsNode(currentNode);

    return pwsNodeEvaluationResult;
  }
  
  /**
   * evaluate selector steps against a node
   * @param pwsNode
   * @param pwsSelectorSteps
   * @param autoCreate if should autocreate
   * @param expectedValue expecting value
   * @return the result
   */
  public static PwsNodeEvaluationResult evaluateSelector(PwsNode pwsNode, 
      List<PwsOperationStep> pwsSelectorSteps, boolean autoCreate, Object expectedValue) {
    
    PwsNodeEvaluationResult pwsNodeEvaluationResult = new PwsNodeEvaluationResult();

    PwsNode arrayNodeFound = null;
    PwsNode resultArrayNodeFound = null;
    
    //lets just try to find the node...
    for (PwsNode arrayItem : StandardApiServerUtils.nonNull(pwsNode.getArray())) {
      
      //see if its there without auto create
      PwsNodeEvaluationResult arrayItemResult = evaluate(arrayItem, pwsSelectorSteps, false);

      if (arrayItemResult.getPwsNode() != null) {

        PwsNode arrayItemResultNode = arrayItemResult.getPwsNode();
        
        boolean equals = arrayItemResultNode.equalsScalar(expectedValue);
        
        if (equals) {
          if (arrayNodeFound != null) {
            throw new RuntimeException("Found multiple results for selector! " + pwsNode + ", " + StandardApiServerUtils.toStringForLog(pwsSelectorSteps));
          }
          arrayNodeFound = arrayItemResultNode;
          resultArrayNodeFound = arrayItem;
        }
      }
      
    }

    if (arrayNodeFound != null || !autoCreate) {
      pwsNodeEvaluationResult.setPwsNode(resultArrayNodeFound);
      return pwsNodeEvaluationResult;
    }

    //we are autocreating
    PwsNode arrayNode = new PwsNode();
    PwsNodeEvaluationResult createNodeResult = evaluate(arrayNode, pwsSelectorSteps, autoCreate);
    
    if (createNodeResult.getPwsNode() == null) {
      throw new RuntimeException("Why is autocreate node null? " + pwsNode + ", " + StandardApiServerUtils.toStringForLog(pwsSelectorSteps));
    }

    createNodeResult.getPwsNode().assignValueAndType(expectedValue);
    
    //this is the parent node in the array
    pwsNodeEvaluationResult.setPwsNode(arrayNode);

    //add this to the array
    pwsNode.addArrayItem(arrayNode);
    
    return pwsNodeEvaluationResult;
  }
  
}
