package edu.internet2.middleware.grouper.grouperUi.beans.ui;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.authentication.GrouperPassword;
import edu.internet2.middleware.grouper.authentication.GrouperPasswordRecentlyUsed;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiMember;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiObjectBase;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.codec.binary.Base64;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.lang3.StringUtils;

public class GuiGrouperPassword {
  
  /**
   * logger 
   */
  private static final Log LOG = GrouperUtil.getLog(GuiGrouperPassword.class);
  
  private GrouperPassword grouperPassword;
  
  private GuiGrouperPassword(GrouperPassword grouperPassword) {
    this.grouperPassword = grouperPassword;
  }
  
  public GrouperPassword getGrouperPassword() {
    return grouperPassword;
  }

  public static GuiGrouperPassword convertFromGrouperPassword(GrouperPassword grouperPassword) {
    return new GuiGrouperPassword(grouperPassword);
  }
  
  public static List<GuiGrouperPassword> convertFromGrouperPassword(List<GrouperPassword> grouperPasswords) {
    
    List<GuiGrouperPassword> guiGrouperPasswords = new ArrayList<GuiGrouperPassword>();
    
    for (GrouperPassword grouperPassword: grouperPasswords) {
      guiGrouperPasswords.add(convertFromGrouperPassword(grouperPassword));
    }
    
    return guiGrouperPasswords;
    
  }
  
  /**
   * get last edited string: Tue Sep 25 12:01:07 PM CDT 2012
   * @return the string of when last edited
   */
  public String getLastEditedString() {
    Long lastEditedTimeLong = this.grouperPassword.getLastEdited();
    return (lastEditedTimeLong == null || lastEditedTimeLong <= 0) ? "" : GuiObjectBase.getDateUiFormat().format(new Date(lastEditedTimeLong));
  }
  
  public String getLastAuthenticatedString() {
    Long lastAuthenticatedTimeLong = this.grouperPassword.getLastAuthenticated();
    return (lastAuthenticatedTimeLong == null || lastAuthenticatedTimeLong <= 0) ? "" : GuiObjectBase.getDateUiFormat().format(new Date(lastAuthenticatedTimeLong));
  }
  
  public String getExpiresAtString() {
    Long expiresAtTimeLong = this.grouperPassword.getExpiresMillis();
    
    return (expiresAtTimeLong == null || expiresAtTimeLong <= 0) ? "" : new SimpleDateFormat("yyyy/MM/dd").format(new Date(expiresAtTimeLong));
  }
  
  /**
   * get created string: Tue Sep 25 12:01:07 PM CDT 2012
   * @return when created
   */
  public String getCreatedString() {
    Long createdTimeLong = this.grouperPassword.getCreatedMillis();
    return (createdTimeLong == null || createdTimeLong <= 0) ? "" : GuiObjectBase.getDateUiFormat().format(new Date(createdTimeLong));
  }
  
  public String getMemberWhoSetPasswordShortLink() {
    
    String memberIdWhoSetPassword = this.getGrouperPassword().getMemberIdWhoSetPassword();
    if (StringUtils.isNotBlank(memberIdWhoSetPassword)) {
      
      return (String) GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
        
        @Override
        public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
          Member member = MemberFinder.findByUuid(grouperSession, memberIdWhoSetPassword, false);
          if (member != null) {
            return new GuiMember(member).getShortLink();
          }
          return "";
        }
      });
      
    }
    return "";
  }
  
  public String getRecentSourceAddresses() {
    
    StringBuilder output = new StringBuilder();
    
    
    Set<GrouperPasswordRecentlyUsed> recentlyUsedGrouperPasswords = GrouperDAOFactory.getFactory().getGrouperPasswordRecentlyUsed()
        .findByGrouperPasswordIdAndStatus(this.getGrouperPassword().getId(), null, new QueryOptions());
    
    for (GrouperPasswordRecentlyUsed grouperPasswordRecentlyUsed: recentlyUsedGrouperPasswords) {
      
     Long millisLong = grouperPasswordRecentlyUsed.getAttemptMillis();
     String ip = grouperPasswordRecentlyUsed.getIpAddress();
     String timestampUserFriendly = GuiObjectBase.getDateUiFormat().format(new Date(millisLong));
     
     output.append(timestampUserFriendly+":"+ip+"</br>");
      
    }
    
    return output.toString();
    
  }
  
  public String getFailedSourceAddresses() {
    
    StringBuilder output = new StringBuilder();
    
    Set<Character> statuses = new HashSet<Character>();
    statuses.add('F');
    statuses.add('E');
    
    Set<GrouperPasswordRecentlyUsed> recentlyUsedGrouperPasswords = GrouperDAOFactory.getFactory().getGrouperPasswordRecentlyUsed()
        .findByGrouperPasswordIdAndStatus(this.getGrouperPassword().getId(), statuses, new QueryOptions());
    
    for (GrouperPasswordRecentlyUsed grouperPasswordRecentlyUsed: recentlyUsedGrouperPasswords) {
      
     Long millisLong = grouperPasswordRecentlyUsed.getAttemptMillis();
     String ip = grouperPasswordRecentlyUsed.getIpAddress();
     String timestampUserFriendly = GuiObjectBase.getDateUiFormat().format(new Date(millisLong));
     
     output.append(timestampUserFriendly+":"+ip+"</br>");
      
    }
    
    return output.toString();
    
  }
  
  public String getSampleAuthorizationHeader() throws UnsupportedEncodingException {
    
    String memberId = this.getGrouperPassword().getMemberId();
    
    String base64EncodedMemberId = new String(new Base64().encode(memberId.getBytes("UTF-8")));
    
    return "Authorization: Bearer jwtUser_"+base64EncodedMemberId+"_jwt";
  }
  

}
