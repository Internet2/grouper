package edu.internet2.middleware.grouper.grouperUi.beans.config;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.audit.AuditEntry;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiSubject;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.pit.PITGrouperConfigHibernate;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

public class GuiPITGrouperConfigHibernate {
  
  /**
   * point in time grouper config 
   */
  private PITGrouperConfigHibernate pitGrouperConfigHibernate;
  
  /**
   * subject who made the config change
   */
  private GuiSubject guiSubject;
  
  /**
   * date time when the config was last udpated
   */
  private String lastUpdated;
  
  private final DateFormat df = new SimpleDateFormat(GrouperUtil.DATE_MINUTES_SECONDS_FORMAT);
  
  private GuiPITGrouperConfigHibernate(PITGrouperConfigHibernate pitGrouperConfigHibernate) {
    this.pitGrouperConfigHibernate = pitGrouperConfigHibernate;
    this.guiSubject = retrieveGuiSubject(pitGrouperConfigHibernate);
    this.lastUpdated = df.format(pitGrouperConfigHibernate.getLastUpdated());
  }
  
  
  public String getLastUpdated() {
    return lastUpdated;
  }


  public PITGrouperConfigHibernate getPitGrouperConfigHibernate() {
    return pitGrouperConfigHibernate;
  }

  public GuiSubject getGuiSubject() {
    return guiSubject;
  }

  
  public static List<GuiPITGrouperConfigHibernate> convertFromPITGrouperConfigsHibernate(List<PITGrouperConfigHibernate> pitCGrouperConfigsHibernate) {

    List<GuiPITGrouperConfigHibernate> results = new ArrayList<GuiPITGrouperConfigHibernate>();
    
    for (PITGrouperConfigHibernate pitGrouperConfigHibernate: pitCGrouperConfigsHibernate) {
      results.add(new GuiPITGrouperConfigHibernate(pitGrouperConfigHibernate));
    }
    
    return results;
  }
  

  private GuiSubject retrieveGuiSubject(PITGrouperConfigHibernate pitConfig) {
    String contextId = pitConfig.getContextId();
    AuditEntry auditEntry = GrouperDAOFactory.getFactory().getAuditEntry().findById(contextId, false);
    GuiSubject guiSubject = null;
    if (auditEntry != null) {
      String memberId = auditEntry.getLoggedInMemberId();
      if (memberId != null) {
        
        Subject subject = (Subject) GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
         
         @Override
         public Subject callback(GrouperSession grouperSession) throws GrouperSessionException {
           
           Member member = MemberFinder.findByUuid(grouperSession, memberId, false);
           if (member != null) {
             return member.getSubject();
           }
           
           return null;
         }
       });
        
       guiSubject = new GuiSubject(subject);
        
      }
    }
    
    return guiSubject;
  }

}
