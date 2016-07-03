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

import java.util.ArrayList;
import java.util.List;

import edu.internet2.middleware.tierApiAuthzServer.util.ExpirableCache;
import edu.internet2.middleware.tierApiAuthzServer.util.StandardApiServerUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * step of getting to the data
 */
public class PwsOperationStep {

  /** logger */
  private static final Log LOG = LogFactory.getLog(PwsOperationStep.class);

  /**
   * array selector for debug
   */
  private String arraySelector;
  
  /**
   * string for this operation
   */
  private String operationString;

  /**
   * string for this operation
   * @return the operationString
   */
  public String getOperationString() {
    return this.operationString;
  }
  
  /**
   * string for this operation
   * @param operationString1 the operationString to set
   */
  public void setOperationString(String operationString1) {
    this.operationString = operationString1;
  }


  /**
   * array selector for debug
   * @return the arraySelector
   */
  public String getArraySelector() {
    return this.arraySelector;
  }

  
  /**
   * array selector for debug
   * @param arraySelector1 the arraySelector to set
   */
  public void setArraySelector(String arraySelector1) {
    this.arraySelector = arraySelector1;
  }

  /** steps to get to a selector  */
  private List<PwsOperationStep> arraySelectorSteps;
  
  /**
   * steps to get to a selector
   * @return the arraySelectorSteps
   */
  public List<PwsOperationStep> getArraySelectorSteps() {
    return this.arraySelectorSteps;
  }
  
  /**
   * steps to get to a selector
   * @param arraySelectorSteps1 the arraySelectorSteps to set
   */
  public void setArraySelectorSteps(List<PwsOperationStep> arraySelectorSteps1) {
    this.arraySelectorSteps = arraySelectorSteps1;
  }

  /**
   * should be a Long, Double, Boolean, or String, value we are looking for in a
   * selector attribute
   */
  private Object arraySelectorAttributeValue;
  
  /**
   * should be a Long, Double, Boolean, or String, value we are looking for in a
   * selector attribute
   * @return the arraySelectorAttributeValue
   */
  public Object getArraySelectorAttributeValue() {
    return this.arraySelectorAttributeValue;
  }

  
  /**
   * should be a Long, Double, Boolean, or String, value we are looking for in a
   * selector attribute
   * @param arraySelectorAttributeValue1 the arraySelectorAttributeValue to set
   */
  public void setArraySelectorAttributeValue(Object arraySelectorAttributeValue1) {
    this.arraySelectorAttributeValue = arraySelectorAttributeValue1;
  }

  /**
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    
    StringBuilder result = new StringBuilder();
    
    result.append("{fieldName: ").append(this.fieldName)
      .append(", step: ").append(this.pwsOperationStepEnum)
      .append(", operationString: ").append(this.operationString)
      .append(", fromFieldName: ").append(this.fromFieldName);
    
    if (this.arrayIndex != -1) {
      result.append(", arrayIndex: ").append(this.arrayIndex);
    }
    if (!StringUtils.isBlank(this.arraySelector)) {
      result.append(", arraySelector: '").append(this.arraySelector).append("'");
      result.append(", arraySelectorValue: '").append(StandardApiServerUtils.abbreviate(this.arraySelectorAttributeValue, 30)).append("'");
      result.append(", arraySelectorStepSize: '").append(StandardApiServerUtils.length(this.arraySelectorSteps)).append("'");
    }
    
    result.append("}");
    return result.toString();
    
  }
  
  /**
   * create a pws operation step
   * @param fromFieldName 
   * @param operationExpression
   * @return the operation step
   */
  public static List<PwsOperationStep> create(String fromFieldName, String operationExpression) {

    List<PwsOperationStep> results = new ArrayList<PwsOperationStep>();
    
    PwsOperationStep pwsOperationStep = new PwsOperationStep();
    pwsOperationStep.setOperationString(operationExpression);
    pwsOperationStep.setFromFieldName(fromFieldName);
    String fieldName = null;

    int leftBracketIndex = StandardApiServerUtils.lastIndexOfQuoted(operationExpression,"[");
    if (leftBracketIndex > -1) {
      
      int rightBracketIndex = StandardApiServerUtils.lastIndexOfQuoted(operationExpression,"]");

      if (rightBracketIndex > -1) {
        
        fieldName = operationExpression.substring(0, leftBracketIndex);
        String indexString = operationExpression.substring(leftBracketIndex+1, rightBracketIndex).trim();

        if (indexString.startsWith("@")) {
          //something like @"something"."something"='something'
          pwsOperationStep.setPwsOperationStepEnum(PwsOperationStepEnum.traverseArrayBySelector);
          pwsOperationStep.setArraySelector(indexString);
          //take off the @
          indexString = indexString.substring(1);
          String[] equalsParts = StandardApiServerUtils.splitTrimQuoted(indexString, "=");
          if (StandardApiServerUtils.length(equalsParts) != 2) {
            throw new RuntimeException("Cant find one and only one unquoted equals sign: " + StandardApiServerUtils.length(equalsParts)
                + ", '@" + indexString + "'");
          }
          String leftSide = equalsParts[0];
          String rightSide = equalsParts[1];
          
          pwsOperationStep.setArraySelectorSteps(PwsOperationStep.parseExpression(fieldName, leftSide));
          
          Object value = null;
          // null just means null
          if (!StringUtils.equals(rightSide, "null")) {
            
            boolean foundValue = false;

            //try string
            if (rightSide.startsWith("\"") || rightSide.startsWith("'")) {
              value = StandardApiServerUtils.unquoteString(rightSide);
              foundValue = true;

            }
            
            if (!foundValue) {
              //try boolean
              try {
                value = StandardApiServerUtils.booleanObjectValue(rightSide);
                foundValue = true;
              } catch (Exception e) {
                
              }
            }
            //try integer
            if (!foundValue) {
              try {
                value = StandardApiServerUtils.longObjectValue(rightSide, false);
                foundValue = true;
              } catch (Exception e) {
                
              }
            }
            
            //try double
            if (!foundValue) {
              try {
                value = StandardApiServerUtils.doubleObjectValue(rightSide, false);
                foundValue = true;
              } catch (Exception e) {
                
              }
            }

            //not valid
            if (!foundValue) {
              throw new RuntimeException("The right side of an attribute selector cannot be parsed, "
                  + "needs to be null, boolean, integer, floating, or quoted string, '" 
                  + operationExpression + "', '" + rightSide + "'");
            }
          }
          
          pwsOperationStep.setArraySelectorAttributeValue(value);
          
          //from the array node
          pwsOperationStep.setFromFieldName(fieldName);

          //add a result for the simple traversal
          PwsOperationStep interimStep = StandardApiServerUtils.listPopOne(create(fromFieldName, fieldName));
          
          results.add(interimStep);
          
        } else {
          int index = StandardApiServerUtils.intValue(indexString);
  
          pwsOperationStep.setArrayIndex(index);
          pwsOperationStep.setPwsOperationStepEnum(PwsOperationStepEnum.traverseArray);
        }
      } else  {
        throw new RuntimeException("Why doesnt matcher match??? '" + operationExpression + "'");
      }
      
    } else {
    
      pwsOperationStep.setPwsOperationStepEnum(PwsOperationStepEnum.traverseField);
      fieldName = operationExpression;

    }

    fieldName = fieldName.trim();

    fieldName = StandardApiServerUtils.unquoteString(fieldName);
    
    pwsOperationStep.setFieldName(fieldName);

    if (LOG.isDebugEnabled()) {
      LOG.debug("Create Step: fromFieldName: " + fromFieldName + ", expression: " + operationExpression + ", step: " + pwsOperationStep.toString() );
    }

    results.add(pwsOperationStep);
    
    return results;

  }
  
  /**
   * cache the operation parsing for 10 hours
   */
  private static ExpirableCache<String, List<PwsOperationStep>> operationStepParseCache = new ExpirableCache<String, List<PwsOperationStep>>(
      60 * 10);

  /**
   * parse an expression part that gives an operation
   * @param fromFieldName where this field is coming from, for debugging purposes
   * @param expression
   * @return the list of step
   */
  public static List<PwsOperationStep> parseExpression(String fromFieldName, String expression) {

    List<PwsOperationStep> pwsOperationSteps = operationStepParseCache.get(expression);
    
    if (pwsOperationSteps == null) {

      pwsOperationSteps = new ArrayList<PwsOperationStep>();

      //simple case, no dot, no nonsense
      if (!StringUtils.isBlank(expression)) {

        //lets see what kind of operation step this is
        if (StandardApiServerUtils.containsQuoted(expression, "${")) {

          //this is EL, just keep and evaluate at runtime.  The EL engine will parse the expressions
          PwsOperationStep pwsOperationStep = new PwsOperationStep();
          pwsOperationStep.setPwsOperationStepEnum(PwsOperationStepEnum.expressionLanguage);
          pwsOperationStep.setOperationString(expression);
          pwsOperationSteps.add(pwsOperationStep);
          
        } else {

          //lets traverse down
          String[] expressionParts = StandardApiServerUtils.splitTrimQuotedBracketed(expression, ".");
          PwsOperationStep previousStep = null;
          for (String expressionPart : expressionParts) {
            
            List<PwsOperationStep> pwsOperationSubSteps = PwsOperationStep.create(
                previousStep == null ? 
                    (StringUtils.isBlank(fromFieldName) ? null : fromFieldName) 
                        : previousStep.getFieldName(), expressionPart);
            pwsOperationSteps.addAll(pwsOperationSubSteps);
            previousStep = pwsOperationSubSteps.get(pwsOperationSubSteps.size()-1);
          }
        }
        
        
      }

      operationStepParseCache.put(expression, pwsOperationSteps);
      
    }
    
    return pwsOperationSteps;

    
  }
  
  /**
   * pws operation step enum is the type of step to take
   */
  private PwsOperationStepEnum pwsOperationStepEnum;
  
  
  /**
   * pws operation step enum is the type of step to take
   * @return the pwsOperationStepEnum
   */
  public PwsOperationStepEnum getPwsOperationStepEnum() {
    return this.pwsOperationStepEnum;
  }
  
  /**
   * pws operation step enum is the type of step to take
   * @param pwsOperationStepEnum1 the pwsOperationStepEnum to set
   */
  public void setPwsOperationStepEnum(PwsOperationStepEnum pwsOperationStepEnum1) {
    this.pwsOperationStepEnum = pwsOperationStepEnum1;
  }

  /**
   * array index
   */
  private int arrayIndex = -1;
  
  /**
   * array index
   * @return the arrayIndex
   */
  public int getArrayIndex() {
    return this.arrayIndex;
  }
  
  /**
   * array index
   * @param arrayIndex1 the arrayIndex to set
   */
  public void setArrayIndex(int arrayIndex1) {
    this.arrayIndex = arrayIndex1;
  }

  /**
   * fieldName of the stem
   */
  private String fieldName;
  
  /**
   * field name we are coming from (for debugging reasons)
   */
  private String fromFieldName;

  
  /**
   * @return the fieldName
   */
  public String getFieldName() {
    return this.fieldName;
  }

  
  /**
   * @param fieldName1 the fieldName to set
   */
  public void setFieldName(String fieldName1) {
    this.fieldName = fieldName1;
  }

  /**
   * field name we are coming from (for debugging reasons)
   * @return the fromFieldName
   */
  public String getFromFieldName() {
    return this.fromFieldName;
  }

  /**
   * field name we are coming from (for debugging reasons)
   * @param fromFieldName1 the fromFieldName to set
   */
  public void setFromFieldName(String fromFieldName1) {
    this.fromFieldName = fromFieldName1;
  }
  
  
  
}
