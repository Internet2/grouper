/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.tierApiAuthzServer.jsonTransform;


/**
 * json that is transformable on a per object basis (the base object)
 */
public interface JsonTransformable {

  /**
   * translate json from something to something else
   * @param input
   * @return the translated json
   */
  public String translateJson(String input);
  
}
