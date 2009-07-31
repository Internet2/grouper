/*
 * @author mchyzer
 * $Id: SimpleMembershipUpdate.java,v 1.1 2009-07-31 14:27:27 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.grouperUi.serviceLogic;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.exception.GroupNotFoundException;
import edu.internet2.middleware.grouper.exception.GroupNotFoundRuntimeException;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.exception.SchemaException;
import edu.internet2.middleware.grouper.grouperUi.GrouperUiJ2ee;
import edu.internet2.middleware.grouper.grouperUi.json.GuiMember;
import edu.internet2.middleware.grouper.grouperUi.json.GuiSubject;
import edu.internet2.middleware.grouper.grouperUi.json.SimpleMembershipList;
import edu.internet2.middleware.grouper.grouperUi.json.SimpleMembershipUpdateInit;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.internal.dao.QueryPaging;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.ws.soap.WsGroup;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectNotFoundException;


/**
 *
 */
public class SimpleMembershipUpdate {

  /**
   * 
   * @param request
   * @param response
   * @return the bean to go to the screen, or null if none
   */
  public SimpleMembershipUpdateInit init(HttpServletRequest request, HttpServletResponse response) {
    
    final SimpleMembershipUpdateInit simpleMembershipUpdateInit = new SimpleMembershipUpdateInit();
    
    final Subject loggedInSubject = GrouperUiJ2ee.retrieveSubjectLoggedIn();
    
    //lets get the group
    String id = request.getParameter("groupId");
    String name = request.getParameter("groupName");

    if (StringUtils.isBlank(id) && StringUtils.isBlank(name)) {
      throw new RuntimeException("Need to pass in name or id");
    }
    
    Group group = null;
    GrouperSession grouperSession = null;
    try {
      grouperSession = GrouperSession.start(loggedInSubject);
  
      if (!StringUtils.isBlank(id)) {
        group = GroupFinder.findByUuid(grouperSession, id);
      } else if (!StringUtils.isBlank(name)) {
        group = GroupFinder.findByName(grouperSession, name, false);
      }
    } catch (GroupNotFoundRuntimeException gnfre) {
      //ignore
    } catch (GroupNotFoundException gnfe) {
      //ignore
    } catch (Exception e) {
      throw new RuntimeException("Problem in simpleMembershipUpdateInit.init : " + id + ", " + name, e);
    }
    
    if (group == null) {
      simpleMembershipUpdateInit.setCanFindGroup(false);
    } else {
      simpleMembershipUpdateInit.setCanFindGroup(true);
      if (group.isComposite()) {
        simpleMembershipUpdateInit.setCompositeGroup(true);
      } else {
        simpleMembershipUpdateInit.setCompositeGroup(false);
        
        final Group GROUP = group;
        
        //do this as root
        GrouperSession.callbackGrouperSession(grouperSession.internal_getRootSession(), new GrouperSessionHandler() {

          public Object callback(GrouperSession theGrouperSession)
              throws GrouperSessionException {
            boolean hasRead = GROUP.hasRead(loggedInSubject);
            boolean hasUpdate = GROUP.hasUpdate(loggedInSubject);
            boolean hasAdmin = GROUP.hasAdmin(loggedInSubject);
            
            simpleMembershipUpdateInit.setCanReadGroup(hasAdmin || hasRead);
            simpleMembershipUpdateInit.setCanUpdateGroup(hasAdmin || hasUpdate);
            return null;
          }
          
        });
        
        if (simpleMembershipUpdateInit.isCanReadGroup() && simpleMembershipUpdateInit.isCanUpdateGroup()) {
          WsGroup wsGroup = new WsGroup(group, null, false);
          simpleMembershipUpdateInit.setGroup(wsGroup);
        }
        
      }
    }
    
    return simpleMembershipUpdateInit;

  }

  /**
   * retrieve members
   * @param request
   * @param response
   * @return the bean to go to the screen, or null if none
   * @throws SchemaException TODO dont throw subjectNotfoundException
   */
  public SimpleMembershipList retrieveMembers(HttpServletRequest request, HttpServletResponse response) 
      throws SchemaException, SubjectNotFoundException {
    
    final SimpleMembershipList simpleMembershipList = new SimpleMembershipList();
    
    final Subject loggedInSubject = GrouperUiJ2ee.retrieveSubjectLoggedIn();
    
    //lets get the group
    String id = request.getParameter("groupId");
    String name = request.getParameter("groupName");
  
    if (StringUtils.isBlank(id) && StringUtils.isBlank(name)) {
      throw new RuntimeException("Need to pass in name or id");
    }
    
    Group group = null;
    GrouperSession grouperSession = null;
    try {
      grouperSession = GrouperSession.start(loggedInSubject);
  
      if (!StringUtils.isBlank(id)) {
        group = GroupFinder.findByUuid(grouperSession, id);
      } else if (!StringUtils.isBlank(name)) {
        group = GroupFinder.findByName(grouperSession, name, false);
      }
    } catch (GroupNotFoundRuntimeException gnfre) {
      //ignore
    } catch (GroupNotFoundException gnfe) {
      //ignore
    } catch (Exception e) {
      throw new RuntimeException("Problem in simpleMembershipUpdateInit.init : " + id + ", " + name, e);
    }
    
    if (group == null) {
      throw new RuntimeException("Cant find group: " + id + ", " + name);
    }
    
    int[] numberOfRecords = new int[1];
    
    //we have the group, now get the members
    
    Set<Membership> allChildren;
    //get the size
    QueryOptions queryOptions = new QueryOptions().retrieveCount(true).retrieveResults(false);
    group.getMembers(Group.getDefaultList(), queryOptions);
    int totalSize = queryOptions.getCount().intValue();
    
    if (GrouperUtil.length(numberOfRecords) > 0) {
      numberOfRecords[0] = totalSize;
    }
    
    //if there are less than the sort limit, then just get all, no problem
    int sortLimit = 200;
//    if (totalSize <= 200) {
//      allChildren = group.getMemberships(Group.getDefaultList());
//    } else {
      QueryPaging queryPaging = new QueryPaging();
      queryPaging.setPageSize(100);
      queryPaging.setFirstIndexOnPage(0);

      //.sortAsc("m.subjectIdDb")   this kills performance
      queryOptions = new QueryOptions().paging(queryPaging);

      List<Member> members = new ArrayList<Member>(group.getMembers(Group.getDefaultList(), queryOptions));
      //allChildren = group.getMemberships(field, members);
//    }
//    return allChildren;

      
      GuiMember[] guiMembers = new GuiMember[members.size()];
      for (int i=0;i<guiMembers.length;i++) {
        guiMembers[i] = new GuiMember();
        //TODO update this
        guiMembers[i].setDeletable(true);
        GuiSubject guiSubject = new GuiSubject(members.get(i).getSubject());
        guiMembers[i].setSubject(guiSubject);
      }
      simpleMembershipList.setMembers(guiMembers);
    
//    Set<edu.internet2.middleware.grouper.Membership> memberships  = MembershipFinder.internal_findAllByGroupAndFieldAndPage(
//        group, Group.getDefaultList(), 0, 100, 200, numberOfRecords);
    
    return simpleMembershipList;
  
  }

}
