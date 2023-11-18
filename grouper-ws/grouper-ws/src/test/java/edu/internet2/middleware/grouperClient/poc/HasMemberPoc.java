/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouperClient.poc;

import edu.internet2.middleware.grouperClient.api.GcGetMembers;
import edu.internet2.middleware.grouperClient.api.GcHasMember;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetMembersResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsHasMemberResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubject;


/**
 *
 */
public class HasMemberPoc {

  /**
   * 
   */
  public HasMemberPoc() {
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    
    WsHasMemberResults wsHasMemberResults = new GcHasMember()
      .addSubjectId("test.subject.0").assignGroupName("test:testGroup").execute();
    
    System.out.println(wsHasMemberResults.getResults()[0].getResultMetadata().getResultCode());
    

    wsHasMemberResults = new GcHasMember()
      .addSubjectId("test.subject.1").assignGroupName("test:testGroup").execute();
  
    System.out.println(wsHasMemberResults.getResults()[0].getResultMetadata().getResultCode());
  
    wsHasMemberResults = new GcHasMember()
      .addSubjectId("test.subject.0").assignGroupName("test2:testGroup").execute();
  
    System.out.println(wsHasMemberResults.getResults()[0].getResultMetadata().getResultCode());
  
    wsHasMemberResults = new GcHasMember()
      .addSubjectId("test.subject.1").assignGroupName("test2:testGroup").execute();
  
    System.out.println(wsHasMemberResults.getResults()[0].getResultMetadata().getResultCode());

    WsGetMembersResults wsGetMembersResults = new GcGetMembers()
      .addGroupName("test:testGroup").execute();

    System.out.println(wsGetMembersResults.getResults()[0].getResultMetadata().getResultCode());

    for (WsSubject wsSubject : wsGetMembersResults.getResults()[0].getWsSubjects()) {
      System.out.println(wsSubject.getId());
    }

  }

}
