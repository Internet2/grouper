/*******************************************************************************
 * Copyright 2012 Internet2
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
 ******************************************************************************/
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
