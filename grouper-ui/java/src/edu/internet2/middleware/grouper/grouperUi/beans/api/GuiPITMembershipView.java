/*******************************************************************************
 * Copyright 2019 Internet2
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
package edu.internet2.middleware.grouper.grouperUi.beans.api;

import java.text.SimpleDateFormat;

import edu.internet2.middleware.grouper.pit.PITMembershipView;


/**
 * gui wrapper around pit membership view
 * @author shilen
 *
 */
public class GuiPITMembershipView {


  /**
   * start label string yyyy/mm/dd
   * @return the start label string yyyy/mm/dd
   */
  public String getStartTimeLabel() {
        
    if (this.membership == null || this.membership.getStartTime() == null) {
      return null;
    }
    
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd kk:mm aa");
    
    return simpleDateFormat.format(this.membership.getStartTime());
    
  }

  /**
   * end label string yyyy/mm/dd
   * @return the end label string yyyy/mm/dd
   */
  public String getEndTimeLabel() {
    
    
    if (this.membership == null || this.membership.getEndTime() == null) {
      return null;
    }
    
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd kk:mm aa");
    
    return simpleDateFormat.format(this.membership.getEndTime());
    
  }
  
  /**
   * 
   * @param theMembership
   */
  public GuiPITMembershipView(PITMembershipView theMembership) {
    this.membership = theMembership;
  }
  
  /**
   * membership
   */
  private PITMembershipView membership;
  
  private GuiSubject guiSubject;

  /**
   * membership
   * @return membership
   */
  public PITMembershipView getPITMembershipView() {
    return this.membership;
  }

  /**
   * membership
   * @param membership1
   */
  public void setPITMembershipView(PITMembershipView membership1) {
    this.membership = membership1;
  }

  
  /**
   * @return the guiSubject
   */
  public GuiSubject getGuiSubject() {
    return guiSubject;
  }

  
  /**
   * @param guiSubject the guiSubject to set
   */
  public void setGuiSubject(GuiSubject guiSubject) {
    this.guiSubject = guiSubject;
  }
}