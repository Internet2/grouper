/*
 * @author mchyzer
 * $Id: ClientOperation.java,v 1.1.2.1 2009-01-26 02:56:09 mchyzer Exp $
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
