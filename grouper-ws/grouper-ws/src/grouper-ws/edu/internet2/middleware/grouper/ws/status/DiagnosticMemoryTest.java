/**
 * 
 */
package edu.internet2.middleware.grouper.ws.status;

import org.apache.commons.lang.builder.HashCodeBuilder;

import edu.internet2.middleware.grouper.ws.GrouperWsConfig;


/**
 * see if the server is out of memory
 * @author mchyzer
 *
 */
public class DiagnosticMemoryTest extends DiagnosticTask {

  /**
   * 
   */
  @Override
  public boolean equals(Object obj) {
    return obj instanceof DiagnosticMemoryTest;
  }
  
  /**
   * 
   */
  @Override
  public int hashCode() {
    return new HashCodeBuilder().toHashCode();
  }

  /**
   * @see edu.internet2.middleware.grouper.ws.status.DiagnosticTask#doTask()
   */
  @Override
  protected boolean doTask() {
    
    int bytesToAllocate = bytesToAllocate();
    
    @SuppressWarnings("unused")
    byte[] someArray = new byte[bytesToAllocate];
    
    this.appendSuccessTextLine("Allocating " + bytesToAllocate() + " bytes to an array to make sure not out of memory");
    
    return true;
    
  }

  /**
   * bytes to allocate
   * @return bytes
   */
  private int bytesToAllocate() {
    return GrouperWsConfig.getPropertyInt("ws.diagnostics.bytesToAllocate", 100000);
  }

  /**
   * @see edu.internet2.middleware.grouper.ws.status.DiagnosticTask#retrieveName()
   */
  @Override
  public String retrieveName() {
    
    return "memoryTest";
  }

  /**
   * @see edu.internet2.middleware.grouper.ws.status.DiagnosticTask#retrieveNameFriendly()
   */
  @Override
  public String retrieveNameFriendly() {
    return "Memory test";
  }

}
