package edu.internet2.middleware.grouper.grouperUi.beans.ui;

import java.text.SimpleDateFormat;
import java.util.Date;

import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiGroup;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiSubject;
import edu.internet2.middleware.grouper.ui.util.GrouperUiConfig;

public class UserLifecycleEventContainer {
  
  private String membershipId;
  
  private GuiGroup guiGroup;
  
  private GuiSubject guiSubject;
  
  private Date eventDate;
  
  private String eventDescription;
  
  private Date membershipRemovalDate;

  
  public GuiGroup getGuiGroup() {
    return guiGroup;
  }

  
  public void setGuiGroup(GuiGroup guiGroup) {
    this.guiGroup = guiGroup;
  }

  
  public GuiSubject getGuiSubject() {
    return guiSubject;
  }

  
  public void setGuiSubject(GuiSubject guiSubject) {
    this.guiSubject = guiSubject;
  }

  
  public Date getEventDate() {
    return eventDate;
  }

  
  public void setEventDate(Date eventDate) {
    this.eventDate = eventDate;
  }

  
  public String getEventDescription() {
    return eventDescription;
  }

  
  public void setEventDescription(String eventDescription) {
    this.eventDescription = eventDescription;
  }

  
  public Date getMembershipRemovalDate() {
    return membershipRemovalDate;
  }

  
  public void setMembershipRemovalDate(Date membershipRemovalDate) {
    this.membershipRemovalDate = membershipRemovalDate;
  }
  
  
  public String getMembershipId() {
    return membershipId;
  }


  
  public void setMembershipId(String membershipId) {
    this.membershipId = membershipId;
  }


  public String getEventDateFormatted() {
    if (this.eventDate == null) {
      return null;
    }
    
    String dateFormat = GrouperUiConfig.retrieveConfig().propertyValueString("uiV2.group.Membership.dateFormat", "yyyy/MM/dd h:mm a z");
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormat);
    return simpleDateFormat.format(this.eventDate);
  }
  
  public String getMembershipRemovalDateFormatted() {
    if (this.membershipRemovalDate == null) {
      return null;
    }
    
    String dateFormat = GrouperUiConfig.retrieveConfig().propertyValueString("uiV2.group.Membership.dateFormat", "yyyy/MM/dd h:mm a z");
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormat);
    return simpleDateFormat.format(this.membershipRemovalDate);
  }
}
