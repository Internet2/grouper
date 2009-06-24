package edu.internet2.middleware.grouper.attr;

/**
 * 
 * @author mchyzer
 *
 */
@SuppressWarnings("serial")
public class AttributeDefValidationNotImplemented extends RuntimeException {

  /** 
   * keep an instance of this
   */
  private static AttributeDefValidationNotImplemented instance 
    = new AttributeDefValidationNotImplemented();

  /**
   * 
   * @return the singleton
   */
  public static AttributeDefValidationNotImplemented instance() {
    return instance;
  }
  
  /**
   * 
   */
  private AttributeDefValidationNotImplemented() {
  }
}
