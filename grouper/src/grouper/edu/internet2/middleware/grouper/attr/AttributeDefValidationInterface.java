/**
 * 
 */
package edu.internet2.middleware.grouper.attr;


/**
 * @author mchyzer
 *
 */
public interface AttributeDefValidationInterface {

  /**
   * name of this validation
   * @return the name of this validation
   */
  public String name();
  
  /**
   * format an input
   * @param input
   * @param argument0
   * @param argument1
   * @return the string, integer, double, or memberId
   * @throws AttributeDefValidationNotImplemented
   */
  public Object formatToDb(Object input, String argument0, String argument1);
  
  /**
   * validate that an object is not null
   * @param input
   * @param argument0
   * @param argument1
   * @return the error string if there is one
   * @throws AttributeDefValidationNotImplemented
   */
  public String validate(Object input, String argument0, String argument1);

  /**
   * format an input
   * @param input could be integer, string, double, or memberId
   * @param argument0
   * @param argument1
   * @return the representation for a screen
   * @throws AttributeDefValidationNotImplemented
   */
  public String formatFromDb(Object input, String argument0, String argument1);

}
