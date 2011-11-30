/*
 * @author mchyzer
 * $Id: AttributeHooksImplExampleEmail.java,v 1.1 2009-03-24 17:12:08 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.hooks.examples;

import java.util.Set;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.Attribute;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.hooks.AttributeHooks;
import edu.internet2.middleware.grouper.hooks.beans.HooksAttributeBean;
import edu.internet2.middleware.grouper.hooks.beans.HooksContext;
import edu.internet2.middleware.grouper.hooks.logic.HookVeto;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectNotFoundException;
import edu.internet2.middleware.subject.SubjectNotUniqueException;


/**
 * test implementation of group hooks for test.  checks to make sure group attribute
 * is not in use by another group, subject, or subject id (for email prefix)
 */
public class AttributeHooksImplExampleEmail extends AttributeHooks {

  /**
   * if there is a changed emailAddress, make sure it doesnt already existin another group or subject
   * @see edu.internet2.middleware.grouper.hooks.AttributeHooks#attributePreUpdate(HooksContext, edu.internet2.middleware.grouper.hooks.beans.HooksAttributeBean)
   */
  @Override
  public void attributePreUpdate(HooksContext hooksContext, HooksAttributeBean preUpdateBean) {
    //if its an update, see if the emailAddress changed
    Attribute attribute = preUpdateBean.getAttribute();
    if (StringUtils.equals(attribute.getAttrName(), "emailAddress")) {
      String emailAddress = attribute.getValue();
      if (!StringUtils.isBlank(emailAddress)) {
        checkEmailAddress(emailAddress);
      }
    }
  }

  /**
   * <pre>
   * check a new or changed email address
   * attribute that there is not an existing email address in a subject attribute, or group attribute
   * 
   * note: this is example code and is not tested
   * </pre>
   * @param emailAddress is the new email address (changed if update), should not be blank
   */
  static void checkEmailAddress(final String emailAddress) {

    try {
      //start session, dont clobber existing session
      GrouperSession grouperSession = GrouperSession.start(SubjectFinder.findRootSubject(), false);
      GrouperSession.callbackGrouperSession(grouperSession, new GrouperSessionHandler() {
  
        public Object callback(GrouperSession grouperSession)
            throws GrouperSessionException {
          
          //first see if there is a group with that attribute
          Set<Group> groups = GroupFinder.findAllByAttribute(grouperSession, "emailAddress", emailAddress);
          if (GrouperUtil.length(groups) > 0) {
            //note, dont show the group who is using it if that is a security problem
            throw new HookVeto("memphis.group.email.attribute.usedByGroup", "The email address: " + emailAddress
                + " is already being used by another group.  Please pick another email address.");
          }
          
          //next see if a subject has that attribute value.  note, this will only work if the subject adapter is 
          //conducive to it
          Set<Subject> subjects = SubjectFinder.findPage(emailAddress).getResults();
          if (GrouperUtil.length(subjects) > 0) {
            //note, dont show the user who is using it if that is a security problem
            throw new HookVeto("memphis.group.email.attribute.usedBySubjectEmail", "The email address: " + emailAddress
                + " is already being used by another entity.  Please pick another email address.");
          }
          
          //next see if a subject has an identifier with the beginning of the email address.  could also filter by subject type
          String emailPrefix = GrouperUtil.prefixOrSuffix(emailAddress, "@", true);
          boolean foundSubject = false;
          try {
            SubjectFinder.findByIdentifier(emailPrefix, true);
            foundSubject = true;
          } catch (SubjectNotFoundException snfe) {

          } catch (SubjectNotUniqueException snue) {
            //data problem, but also others using the email address
            foundSubject = true;
          }
          if (foundSubject) {
            //note, dont show the user who is using it if that is a security problem
            throw new HookVeto("memphis.group.email.attribute.usedBySubjectEmailPrefix", "The email prefix: " + emailPrefix
                + " is already being used by another entity.  Please pick another email address.");
          }
          
          return null;
        }
        
      });
      GrouperSession.stopQuietly(grouperSession);
    } catch (Exception e) {
      throw new RuntimeException("Problem checking email address: '" + emailAddress + "'", e);
    }
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.hooks.AttributeHooks#attributePreInsert(HooksContext, HooksAttributeBean)
   */
  @Override
  public void attributePreInsert(HooksContext hooksContext, HooksAttributeBean preInsertBean) {
    //if its an update, see if the emailAddress changed
    Attribute attribute = preInsertBean.getAttribute();
    if (StringUtils.equals(attribute.getAttrName(), "emailAddress")) {
      String emailAddress = attribute.getValue();
      if (!StringUtils.isBlank(emailAddress)) {
        checkEmailAddress(emailAddress);
      }
    }
  }

}
