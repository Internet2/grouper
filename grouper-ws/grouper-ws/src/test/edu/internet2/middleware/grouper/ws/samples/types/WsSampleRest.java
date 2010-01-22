/*
 * @author mchyzer
 * $Id: WsSampleRest.java,v 1.1 2008-03-25 05:15:11 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.ws.samples.types;


/**
 * interface for generated sample
 */
public interface WsSampleRest extends WsSample {
  
  /** 
   * execute the sample
   * @param wsSampleRestType xhtml vs xml vs json
   */
  public void executeSample(WsSampleRestType wsSampleRestType);
  
  /** 
   * see if this is a valid type (e.g. http params only available
   * for Lite requests)
   * @param wsSampleRestType xhtml vs xml vs json
   * @return if valid
   */
  public boolean validType(WsSampleRestType wsSampleRestType);
  
}
