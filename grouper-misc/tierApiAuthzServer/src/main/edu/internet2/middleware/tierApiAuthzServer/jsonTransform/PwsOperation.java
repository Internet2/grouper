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
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.internet2.middleware.tierApiAuthzServer.util.ExpirableCache;
import edu.internet2.middleware.tierApiAuthzServer.util.StandardApiServerUtils;
import edu.internet2.middleware.tierApiAuthzServerExt.org.apache.commons.lang.StringUtils;
import edu.internet2.middleware.tierApiAuthzServerExt.org.apache.commons.logging.Log;
import edu.internet2.middleware.tierApiAuthzServerExt.org.apache.commons.logging.LogFactory;


/**
 * a string operation from a config file, parsed into a java object to improve performance
 */
public class PwsOperation {

  /**
   * this should be retrieved since it might be cached
   */
  private PwsOperation() {
    
  }
  
  /**
   * operation string
   */
  private String operationString;
  
  /**
   * if we are typecasting
   */
  private Class<?> typeCastClass = null;
  
  /**
   * if we are typecasting
   * @return the typeCastClass
   */
  public Class<?> getTypeCastClass() {
    return this.typeCastClass;
  }
  
  /**
   * if we are typecasting
   * @param typeCastClass1 the typeCastClass to set
   */
  public void setTypeCastClass(Class<?> typeCastClass1) {
    this.typeCastClass = typeCastClass1;
  }

  /**
   * operation string
   * @return the operationString
   */
  public String getOperationString() {
    return this.operationString;
  }
  
  /**
   * operation string
   * @param operationString1 the operationString to set
   */
  public void setOperationString(String operationString1) {
    this.operationString = operationString1;
  }

  /**
   * cache the operation parsing for 10 hours
   */
  private static ExpirableCache<String, PwsOperation> operationParseCache = new ExpirableCache<String, PwsOperation>(60*10);

  /** logger */
  private static final Log LOG = LogFactory.getLog(PwsNode.class);


  /**
   * <pre>
   * typecast
   * 
   * ^\s*\(      start with start of string, optional whitespace, and an open paren
   * (\s*        start a capture, then some optional space
   * [a-zA-Z]+   upper or lower chars
   * )\s*        end the capture and some optional space
   * 
   * \)\s*(.*)$
   * 
   * </pre>
   */
  private static Pattern typecastPattern = Pattern.compile("^\\s*\\((\\s*[a-zA-Z]+)\\s*\\)\\s*(.*)$"); 
  
  /**
   * type case conversion
   */
  private static Map<String, Class<?>> typeCastConversionLower = null;
  
  static {
    
    typeCastConversionLower = new HashMap<String, Class<?>>();
    
    typeCastConversionLower.put("int", Long.class);
    typeCastConversionLower.put("integer", Long.class);
    typeCastConversionLower.put("long", Long.class);
    
    typeCastConversionLower.put("double", Double.class);
    typeCastConversionLower.put("float", Double.class);
    typeCastConversionLower.put("floating", Double.class);

    typeCastConversionLower.put("string", String.class);

    typeCastConversionLower.put("boolean", Boolean.class);
    typeCastConversionLower.put("bool", Boolean.class);

    typeCastConversionLower.put("object", Object.class);
    
  }
  
  /**
   * retrieve an operation from cache or parse it
   * @param operationString
   * @return the operation
   */
  public static PwsOperation retrieve(String operationString) {
    
    PwsOperation pwsOperation = operationParseCache.get(operationString);
    
    if (pwsOperation == null) {

      pwsOperation = new PwsOperation();

      try {
        //use a different string so we dont cache the wrong thing
        String workingOperationString = StringUtils.trimToEmpty(operationString);
        
        //see if it is an assignment
        if (StandardApiServerUtils.containsQuotedBracketed(workingOperationString, "=")) {
          
          String[] operationParts = StandardApiServerUtils.splitTrimQuotedBracketed(workingOperationString, "=");
          
          if (operationParts.length > 2) {
            throw new RuntimeException("Assignments cannot have more than one equals sign! " + operationParts.length + ", '" + operationString + "'" );
          }
          
          pwsOperation.pwsOperationEnum = PwsOperationEnum.assign;
          
          {
            String destinationPart = operationParts[0];
            pwsOperation.destinationPwsOperationSteps = PwsOperationStep.parseExpression(null, destinationPart);
          }
          
          {
            String sourcePart = operationParts[1];
            
            //see if there is a typecast
            Matcher matcher = typecastPattern.matcher(sourcePart);
            if (matcher.matches()) {
              String typeCastLower = matcher.group(1).toLowerCase();
              
              Class<?> typeCast = typeCastConversionLower.get(typeCastLower);

              if (typeCast == null) {
                throw new RuntimeException("Cant find typecast, expecting string, integer, floating, or boolean: " + typeCastLower);
              }
              
              pwsOperation.typeCastClass = typeCast;
              
              sourcePart = matcher.group(2);
            }
            
            pwsOperation.sourcePwsOperationSteps = PwsOperationStep.parseExpression(null, sourcePart);
          }
          
        } else {
          throw new RuntimeException("Not expecting operation");
        }
      } catch (Exception e) {
        
        //dont make one assignment ruin the whole thing
        LOG.error("Error parsing operation: '" + operationString + "'", e);
        pwsOperation.setPwsOperationEnum(PwsOperationEnum.invalidOperation);
        
      }
      operationParseCache.put(operationString, pwsOperation);
      
    }
    
    return pwsOperation;
  }

  
  
  /**
   * the operation being performed
   */
  private PwsOperationEnum pwsOperationEnum;

  
  /**
   * the operation being performed
   * @return the pwsOperationEnum
   */
  public PwsOperationEnum getPwsOperationEnum() {
    return this.pwsOperationEnum;
  }

  
  /**
   * the operation being performed
   * @param pwsOperationEnum1 the pwsOperationEnum to set
   */
  public void setPwsOperationEnum(PwsOperationEnum pwsOperationEnum1) {
    this.pwsOperationEnum = pwsOperationEnum1;
  }

  /**
   * steps to get to the source to assign from, or if there is only one set of steps
   */
  private List<PwsOperationStep> sourcePwsOperationSteps;
  
  /**
   * steps to get to the source to assign from, or if there is only one set of steps
   * @return the sourcePwsOperationSteps
   */
  public List<PwsOperationStep> getSourcePwsOperationSteps() {
    return this.sourcePwsOperationSteps;
  }
  
  /**
   * steps to get to the source to assign from, or if there is only one set of steps
   * @param sourcePwsOperationSteps1 the sourcePwsOperationSteps to set
   */
  public void setSourcePwsOperationSteps(List<PwsOperationStep> sourcePwsOperationSteps1) {
    this.sourcePwsOperationSteps = sourcePwsOperationSteps1;
  }
  
  /**
   * steps to get to the destination to assign to
   */
  private List<PwsOperationStep> destinationPwsOperationSteps;
  
  /**
   * @return the destinationPwsOperationSteps
   */
  public List<PwsOperationStep> getDestinationPwsOperationSteps() {
    return this.destinationPwsOperationSteps;
  }
  
  /**
   * @param destinationPwsOperationSteps1 the destinationPwsOperationSteps to set
   */
  public void setDestinationPwsOperationSteps(List<PwsOperationStep> destinationPwsOperationSteps1) {
    this.destinationPwsOperationSteps = destinationPwsOperationSteps1;
  }

  
}

