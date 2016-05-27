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

import java.util.List;

/**
 *
 */
public class PwsNodeTranslation {

  /**
   * assign a field from one object to another, can rename
   * @param fromNode
   * @param toNode
   * @param pwsOperation
   * @return the assignment object
   */
  static PwsNodeAssignmentResult assign(PwsNode toNode, PwsNode fromNode, PwsOperation pwsOperation) {
    
    PwsNodeAssignmentResult pwsNodeAssignmentResult = new PwsNodeAssignmentResult();

    switch (pwsOperation.getPwsOperationEnum()) {
      case assign:

        List<PwsOperationStep> sourcePwsOperationSteps = pwsOperation.getSourcePwsOperationSteps();
        PwsNodeEvaluationResult pwsNodeEvaluationResult = PwsNodeEvaluation.evaluate(fromNode, sourcePwsOperationSteps, false);

        if (pwsNodeEvaluationResult.getPwsNode() == null) {
          pwsNodeAssignmentResult.setFoundSourceLocation(false);
        } else {

          PwsNode sourceNode = pwsNodeEvaluationResult.getPwsNode();
          
          List<PwsOperationStep> destinationPwsOperationSteps = pwsOperation.getDestinationPwsOperationSteps();
          pwsNodeEvaluationResult = PwsNodeEvaluation.evaluate(toNode, destinationPwsOperationSteps, true);
          
          PwsNode destinationNode = pwsNodeEvaluationResult.getPwsNode();
          if (pwsNodeEvaluationResult.isCreatedNode()) {
            pwsNodeAssignmentResult.setCreatedDestinationLocation(true);
          }
          
          assignNode(destinationNode, sourceNode, pwsOperation.getTypeCastClass());
          
        }
         
         
        break;
      case invalidOperation:
      case nullOperation:
        break;
      default:
        throw new RuntimeException("Not expecting operation: " + pwsOperation.getPwsOperationEnum());
    }
    
    
    return pwsNodeAssignmentResult;
  }

  /**
   * assign (clone?) whats in the from node to the to node
   * @param toNode
   * @param fromNode
   * @param typeCastClass 
   * @return true if changed type
   */
  static boolean assignNode(PwsNode toNode, PwsNode fromNode, Class<?> typeCastClass) {

    {
      PwsNode copyFromNode = fromNode;
      
      if (typeCastClass != null) {
        copyFromNode = new PwsNode();
        
        PwsNode.typeCast(copyFromNode, fromNode, typeCastClass);
        
      }
      //copy back
      fromNode = copyFromNode;
    }
    
    
    boolean changedType = false;
    if (fromNode.getPwsNodeType() != toNode.getPwsNodeType()) {
      if (toNode.getPwsNodeType() != null) {
        //this is only a changed type if it wasnt equal and wasnt null
        changedType = true;
      }
      toNode.setPwsNodeType(fromNode.getPwsNodeType());
    }

    if (fromNode.isArrayType()) {
      toNode.cloneNode(fromNode);
    }

    {
      PwsNode parentToNode = toNode.getFromNode();
      //if we made room for arrays, then the type might not be set correctly, so set it
      if (parentToNode != null && parentToNode.isArrayType()) {
        if (parentToNode.getPwsNodeType() != fromNode.getPwsNodeType()) {
          //change type of parent
          changedType = true;
          parentToNode.setPwsNodeType(fromNode.getPwsNodeType());
          //set the type of all children in the array
          for (PwsNode arrayItemNode : parentToNode.getArray()) {
            
            arrayItemNode.setPwsNodeType(fromNode.getPwsNodeType());
            
          }
        }
      }
    }
    
    //copy all the data over
    switch(fromNode.getPwsNodeType()) {
      case bool:
        toNode.setBool(fromNode.getBool());
        break;
      case floating:
        toNode.setFloating(fromNode.getFloating());
        break;
      case integer:
        toNode.setInteger(fromNode.getInteger());
        break;
      case string:
        toNode.setString(fromNode.getString());
        break;
      case object:
        //already cloned if array
        if (!fromNode.isArrayType()) {
          toNode.cloneNode(fromNode);
        }
        break;
      default: 
        throw new RuntimeException("Not expecting node type: " + fromNode.getPwsNodeType());
        
    }

    
    return changedType;
  }
  
  /**
   * assign a field from one object to another, can rename
   * @param fromNode
   * @param toNode
   * @param assignment
   * @return the assignment object
   */
  public static PwsNodeAssignmentResult assign(PwsNode toNode, PwsNode fromNode, String assignment) {
    
    //get the assignment, note, these are cached
    PwsOperation pwsOperation = PwsOperation.retrieve(assignment);

    PwsNodeAssignmentResult pwsNodeAssignmentResult = assign(toNode, fromNode, pwsOperation);    
    
    return pwsNodeAssignmentResult;
  }
  
}
