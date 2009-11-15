/*
 * @author mchyzer
 * $Id: ClientOperation.java,v 1.2 2009-03-15 08:16:36 mchyzer Exp $
 */
package edu.internet2.middleware.grouperClient;


/**
 * an operation of the grouper client
 */
public interface ClientOperation {

  /**
   * execute an operation
   * @param operationParams
   * @return the string output to go to screen or file
   */
  public String operate(OperationParams operationParams);
  
}
