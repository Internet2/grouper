/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.tierApiAuthzServer.jsonTransform;


/**
 * what type of step are we talking about
 */
public enum PwsOperationStepEnum {

  /**
   * expression language
   */
  expressionLanguage,
  
  /**
   * traverse array
   */
  traverseArray,
  
  /**
   * traverse array by selector
   */
  traverseArrayBySelector,
  
  /**
   * simple traverse field
   */
  traverseField;
  
}
