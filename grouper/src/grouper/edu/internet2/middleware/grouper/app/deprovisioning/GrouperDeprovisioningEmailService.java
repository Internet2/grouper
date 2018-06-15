package edu.internet2.middleware.grouper.app.deprovisioning;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;

import edu.emory.mathcs.backport.java.util.Arrays;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.MembershipFinder;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.misc.GrouperObject;
import edu.internet2.middleware.grouper.privs.AttributeDefPrivilege;
import edu.internet2.middleware.grouper.privs.AttributeDefResolverFactory;
import edu.internet2.middleware.grouper.util.GrouperEmail;
import edu.internet2.middleware.grouper.util.GrouperEmailUtils;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Source;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.provider.SourceManager;

public class GrouperDeprovisioningEmailService {
  
  /**
   * logger 
   */
  private static final Log LOG = GrouperUtil.getLog(GrouperDeprovisioningEmailService.class);
  
  /**
   * send email about all new deprovisioned users. 
   * @param grouperSession
   */
  public void sendEmailForAllAffiliations(GrouperSession grouperSession) {
    
    if (!GrouperDeprovisioningSettings.deprovisioningEnabled()) {
      LOG.debug("Deprovisioning is not enabled!  Quitting!!");
    }
    
    Map<String, GrouperDeprovisioningAffiliation> affiliations = GrouperDeprovisioningAffiliation.retrieveAllAffiliations();
    
    Map<String, EmailPerPerson> userEmailObjects = new HashMap<String, GrouperDeprovisioningEmailService.EmailPerPerson>();
    
    for (GrouperDeprovisioningAffiliation affiliation: affiliations.values()) {
      
      Set<Member> deprovisionedUsers = affiliation.getUsersWhoHaveBeenDeprovisioned();
      
      Set<Membership> memberships = new HashSet<Membership>();
      
      for (Member member: deprovisionedUsers) {
        
        Set<Object[]> membershipsSet = new MembershipFinder().addMemberId(member.getId()).addSource(member.getSubjectSource()).findMembershipsMembers();
        
        for (Object[] membershipArray: membershipsSet) {
          Membership membership = (Membership)membershipArray[0];
          memberships.add(membership);
        }
        
      }
      
      Map<String, EmailPerPerson> userEmailObject = buildEmailObjectForOneDeprovisionedSubject(grouperSession, memberships, affiliation, true);
      mergeEmailObjects(userEmailObjects, userEmailObject);
      
    }
    
    Set<GrouperObjectWithAffiliation> grouperObjectsWithAffiliation = sendEmailToUsers(userEmailObjects);
    
    setLastEmailedDateAttribute(grouperObjectsWithAffiliation);
    
  }
  
  /**
   * build map of email address to email per person object
   * @param grouperSession
   * @param memberships
   * @param affiliation
   * @param callFromDaemon - true means multiple emails can be sent the same day to the same people 
   * @return email address to email per person object
   */
  public Map<String, EmailPerPerson> buildEmailObjectForOneDeprovisionedSubject(GrouperSession grouperSession, Set<Membership> memberships,
      GrouperDeprovisioningAffiliation affiliation, boolean callFromDaemon) {
    
    Map<String, EmailPerPerson> userEmailObjects = new HashMap<String, EmailPerPerson>();
    
    for (Membership membership: memberships) {
      
      Group group = membership.getOwnerGroupId() != null ? membership.getOwnerGroup(): null;
      Stem stem = membership.getOwnerStemId() != null ? membership.getOwnerStem(): null;
      AttributeDef attributeDef = membership.getOwnerAttrDefId() != null ? membership.getOwnerAttributeDef(): null;
      
      Subject subject = membership.getMember().getSubject();
      
      if (group != null) {
        populatedEmailObjects(grouperSession, group, affiliation, subject, userEmailObjects, callFromDaemon, true);
      } else if (stem != null) {
        populatedEmailObjects(grouperSession, stem, affiliation, subject, userEmailObjects, callFromDaemon, true);
      } else if (attributeDef != null) {
        populatedEmailObjects(grouperSession, attributeDef, affiliation, subject, userEmailObjects, callFromDaemon, true);
      }
    }
    
    return userEmailObjects;
  }
  
  /**
   * populate the given userEmailObjects input parameter with email address to email per person object
   * @param grouperSession
   * @param grouperObject
   * @param affiliation
   * @param subject
   * @param userEmailObjects
   * @param callFromDaemon
   * @param lookupAtInheritedFolder
   */
  private void populatedEmailObjects(GrouperSession grouperSession, GrouperObject grouperObject, GrouperDeprovisioningAffiliation affiliation,
      Subject subject, Map<String, EmailPerPerson> userEmailObjects, boolean callFromDaemon, boolean lookupAtInheritedFolder) {

    GrouperDeprovisioningOverallConfiguration grouperDeprovisioningOverallConfiguration = GrouperDeprovisioningOverallConfiguration.retrieveConfiguration(grouperObject);
    
    if (!grouperDeprovisioningOverallConfiguration.hasConfigurationForAffiliation(affiliation.getLabel())) {
      return;
    }
    
    Map<String, GrouperDeprovisioningConfiguration> affiliationToConfiguration = grouperDeprovisioningOverallConfiguration.getAffiliationToConfiguration();
    
    GrouperDeprovisioningConfiguration deprovisioningConfiguration = affiliationToConfiguration.get(affiliation.getLabel());
    
    GrouperDeprovisioningAttributeValue attributeValue = deprovisioningConfiguration.getOriginalConfig();
    
    // don't send multiple emails same day to the same people when running daily job
    if (attributeValue.getLastEmailedDate() != null) {
      String today = new SimpleDateFormat("yyyy/MM/dd").format(new Date());

      if (callFromDaemon && StringUtils.equals(attributeValue.getLastEmailedDateString(), today)) {
        LOG.debug("For "+grouperObject.getName()+" deprovisioningLastEmailedDate is set to today so skipping sending email.");
        return;
      }
    }
    
    if (callFromDaemon) {
      Membership membership = new MembershipFinder().addGroup(affiliation.getUsersWhoHaveBeenDeprovisionedGroup()).addSubject(subject).findMembership(false);
      if (membership == null) {
        LOG.error("How come subject is not a member of deprovisioned group??? "+subject.getName());
        return;
      }
      
      // group/stem/attributeDef was certified after the subject was deprovisioned so no need to send email about the same user.
      if (attributeValue.getCertifiedMillis() != null && attributeValue.getCertifiedMillis() > membership.getCreateTimeLong()) {
        return;
      }
    }
    
    if (attributeValue.isDirectAssignment() && attributeValue.isSendEmail()) {
      populateEmailObjectsHelper(grouperSession, grouperObject, subject, attributeValue, userEmailObjects);
    }
    
    String deprovisioningInheritedFromFolderId = attributeValue.getInheritedFromFolderIdString();
    if (lookupAtInheritedFolder && StringUtils.isNotBlank(deprovisioningInheritedFromFolderId)) {
      
      try {
        Stem stemToInheritConfigurationFrom = StemFinder.findByIdIndex(Long.valueOf(deprovisioningInheritedFromFolderId), true, new QueryOptions());
        
        populatedEmailObjects(grouperSession, stemToInheritConfigurationFrom, affiliation, subject, userEmailObjects, callFromDaemon, false);
        
      } catch(Exception e) {
        LOG.error(grouperObject.getName()+" has deprovisioningInheritedFromFolderId set to invalid folder id: "+deprovisioningInheritedFromFolderId);
      }
    }
    
  }
  
  /**
   * populate map of email address to email per person object
   * @param grouperSession
   * @param grouperObject
   * @param subject
   * @param attributeValue
   * @param userEmailObjects
   */
  private void populateEmailObjectsHelper(GrouperSession grouperSession, GrouperObject grouperObject, Subject subject,
      GrouperDeprovisioningAttributeValue attributeValue, Map<String, EmailPerPerson> userEmailObjects) {
    
    Set<String> emailTos = retrieveEmailAddresses(grouperSession, grouperObject, attributeValue);
    buildEmailPerPersonObjects(attributeValue, emailTos, userEmailObjects, subject, grouperObject);
    
  }
  
  /**
   * send email and return grouper objects with affiliations that were configured for deprovisioning
   * @param userEmailObjects
   * @return grouper objects with affiliations that were configured for deprovisioning
   */
  public Set<GrouperObjectWithAffiliation> sendEmailToUsers(Map<String, EmailPerPerson> userEmailObjects) {
    
    Set<GrouperObjectWithAffiliation> objectsAssociatedWithEmail = new HashSet<GrouperObjectWithAffiliation>();
    
    String uiUrl = GrouperConfig.getGrouperUiUrl(false);
    
    if (StringUtils.isBlank(uiUrl)) {
      LOG.error("grouper.properties grouper.ui.url is blank/null. Please fix that first. No emails have been sent.");
      return objectsAssociatedWithEmail;
    }
    
    String subject = GrouperConfig.retrieveConfig().propertyValueString("deprovisioning.reminder.email.subject");
    String body = GrouperConfig.retrieveConfig().propertyValueString("deprovisioning.reminder.email.body");
    
    if (StringUtils.isBlank(subject)) {
      subject = "You have $objectCount$ objects that have suggested users to be deprovisioned";
    }
    if (StringUtils.isBlank(body)) {
      body = "You need to review the memberships of the following objects.  Review the memberships of each object and click: More actions -> Deprovisioning -> Members of this object have been reviewed";
    }
    
    for (Map.Entry<String, EmailPerPerson> entry: userEmailObjects.entrySet()) {
      
      EmailPerPerson emailPerPerson = entry.getValue();
      
      int objectCount = emailPerPerson.deprovisioningGroupEmailObjects.size() +
          emailPerPerson.deprovisioningStemEmailObjects.size() + 
          emailPerPerson.deprovisioningAttributeDefEmailObjects.size();
      
      String sub = StringUtils.replace(subject, "$objectCount$", String.valueOf(objectCount));
      
      // build body of the email
      StringBuilder emailBody = new StringBuilder(body);
      emailBody.append("\n");
      Integer start = 1; // show only deprovisioning.email.object.count in one email
      Integer end = GrouperConfig.retrieveConfig().propertyValueInt("deprovisioning.email.object.count", 100);
      
      addLinksToEmailBody(emailPerPerson.deprovisioningGroupEmailObjects, emailBody, objectCount, start,
          end, "grouperUi/app/UiV2Main.index?operation=UiV2Group.viewGroup&groupId=");
      
      addLinksToEmailBody(emailPerPerson.deprovisioningStemEmailObjects, emailBody, objectCount, start,
          end, "grouperUi/app/UiV2Main.index?operation=UiV2Stem.viewStem&stemId=");
      
      addLinksToEmailBody(emailPerPerson.deprovisioningAttributeDefEmailObjects, emailBody, objectCount, start,
          end, "grouperUi/app/UiV2Main.index?operation=UiV2AttributeDef.viewAttributeDef&attributeDefId=");
      
      emailBody.append("\n");
      
      // concatenate custom body at the bottom of the email
      for (String customEmail: emailPerPerson.customEmailBodies) {
        emailBody.append(customEmail);
        emailBody.append("\n");
      }
      
      try {
        new GrouperEmail().setBody(emailBody.toString()).setSubject(sub).setTo(entry.getKey()).send();
        objectsAssociatedWithEmail.addAll(emailPerPerson.deprovisioningGroupEmailObjects);
        objectsAssociatedWithEmail.addAll(emailPerPerson.deprovisioningStemEmailObjects);
        objectsAssociatedWithEmail.addAll(emailPerPerson.deprovisioningAttributeDefEmailObjects);
      } catch (Exception e) {
        LOG.error("Error sending email to "+entry.getKey(), e);
      }
      
    }
    
    return objectsAssociatedWithEmail;
    
  }
  
  /**
   * set last email date attribute 
   * @param grouperObjectWithAffiliations
   */
  public void setLastEmailedDateAttribute(Set<GrouperObjectWithAffiliation> grouperObjectWithAffiliations) {
    
    for (GrouperObjectWithAffiliation grouperObjectWithAffiliation: grouperObjectWithAffiliations) {
      
      GrouperObject grouperObject = grouperObjectWithAffiliation.getGrouperObject();
      String affiliation = grouperObjectWithAffiliation.getAffiliation();
      
      GrouperDeprovisioningOverallConfiguration grouperDeprovisioningOverallConfiguration = GrouperDeprovisioningOverallConfiguration.retrieveConfiguration(grouperObject);
      Map<String, GrouperDeprovisioningConfiguration> affiliationToConfiguration = grouperDeprovisioningOverallConfiguration.getAffiliationToConfiguration();
      if (affiliationToConfiguration.containsKey(affiliation)) {
        GrouperDeprovisioningConfiguration configuration = affiliationToConfiguration.get(affiliation);
        configuration.getOriginalConfig().setLastEmailedDate(new Date());
        configuration.storeConfiguration();
      }
      
    }
    
  }
    
  /**
   * add links to email body
   * @param grouperObjectsWithAffiliation
   * @param emailBody
   * @param objectCount
   * @param start
   * @param end
   * @param link
   */
  private void addLinksToEmailBody(List<GrouperObjectWithAffiliation> grouperObjectsWithAffiliation,
      StringBuilder emailBody, Integer objectCount, Integer start, Integer end, String link) {
    
    if (start > end) {
      return;
    }
    
    String uiUrl = GrouperConfig.getGrouperUiUrl(false);
    
    for (GrouperObjectWithAffiliation objectWithAffiliation: grouperObjectsWithAffiliation) {
      emailBody.append("\n");
      emailBody.append(start+". "+objectWithAffiliation.getGrouperObject().getName()+"  ");
      emailBody.append("\n");
      emailBody.append(uiUrl);
      emailBody.append(link+objectWithAffiliation.getGrouperObject().getId());
      start = start + 1;
      if (start > end) {
        String more = GrouperConfig.retrieveConfig().propertyValueString("deprovisioning.reminder.email.body.greaterThan100");
        if (StringUtils.isBlank(more)) {
          more = "There are $remaining$ more objects to be reviewed.";
        }
        more = StringUtils.replace(more, "$remaining$", String.valueOf(objectCount - end));
        emailBody.append("\n");
        emailBody.append(more);
        break;
      }
    }
  }
  

  /**
   * merge second map into first one
   * @param mapToMergeInto
   * @param mapToBeMerged
   */
  private void mergeEmailObjects(Map<String, EmailPerPerson> mapToMergeInto, Map<String, EmailPerPerson> mapToBeMerged) {
    
    for (Map.Entry<String, EmailPerPerson> entry: mapToBeMerged.entrySet()) {
      if (mapToMergeInto.containsKey(entry.getKey())) {
        EmailPerPerson emailPerPerson = entry.getValue();
        EmailPerPerson masterEmailPerPerson = mapToMergeInto.get(entry.getKey());
        masterEmailPerPerson.customEmailBodies.addAll(emailPerPerson.customEmailBodies);
        masterEmailPerPerson.deprovisioningGroupEmailObjects.addAll(emailPerPerson.deprovisioningGroupEmailObjects);
        masterEmailPerPerson.deprovisioningStemEmailObjects.addAll(emailPerPerson.deprovisioningStemEmailObjects);
        masterEmailPerPerson.deprovisioningAttributeDefEmailObjects.addAll(emailPerPerson.deprovisioningAttributeDefEmailObjects);
      } else {
        mapToMergeInto.put(entry.getKey(), entry.getValue());
      }
    }
    
  }
    
  /**
   * @param text
   * @param subject
   * @param grouperObject
   * @return custom email body
   */
  private String replaceTemplateVariables(String text, Subject subject, GrouperObject grouperObject) {
    
    String result = new String(text);
    
    result = result.replace("$$userSubjectId$$", subject.getId());
    result = result.replace("$$name$$", subject.getName());
    result = result.replace("$$userDescription$$", subject.getDescription());
    
    String emailAttributeName = GrouperEmailUtils.emailAttributeNameForSource(subject.getSourceId());
    
    if (!StringUtils.isBlank(emailAttributeName)) {
      String emailAddress = subject.getAttributeValue(emailAttributeName);
      if (!StringUtils.isBlank(emailAddress)) {
        result = result.replace("$$userEmailAddress$$", emailAddress);
      }
    }
    
    Source source = SourceManager.getInstance().getSource(subject.getSourceId());
    String netIdAttributeName = source.getInitParam("netId");
    
    if (!StringUtils.isBlank(netIdAttributeName)) {
      String netId = subject.getAttributeValue(netIdAttributeName);
      if (!StringUtils.isBlank(netId)) {
        result = result.replace("$$netId$$", netId);
      }
    }
    
    String uiUrl = GrouperConfig.getGrouperUiUrl(false);
    if (StringUtils.isNotBlank(uiUrl)) {
      if (grouperObject instanceof Group) {
        result = result.replace("$$grouperObjectLink$$", uiUrl+"grouperUi/app/UiV2Main.index?operation=UiV2Group.viewGroup&groupId="+grouperObject.getId());
      }
      if (grouperObject instanceof Stem) {
        result = result.replace("$$grouperObjectLink$$", uiUrl+"grouperUi/app/UiV2Main.index?operation=UiV2Stem.viewStem&stemId="+grouperObject.getId());
      }
      if (grouperObject instanceof AttributeDef) {
        result = result.replace("$$grouperObjectLink$$", uiUrl+"grouperUi/app/UiV2Main.index?operation=UiV2AttributeDef.viewAttributeDef&attributeDefId="+grouperObject.getId());
      }
    }
    
    return result;
    
  }
  
  /**
   * retrieve a set of email addresses from grouper objects that have deprovisioning configured. 
   * @param grouperSession
   * @param grouperObject
   * @param attributeValue
   * @return
   */
  private Set<String> retrieveEmailAddresses(GrouperSession grouperSession, GrouperObject grouperObject, GrouperDeprovisioningAttributeValue attributeValue) {
    
    Set<String> emailAddresses = new HashSet<String>();
    
    if (attributeValue.isEmailManagers()) {
      
      Set<Subject> subjects = new HashSet<Subject>();
      
     if (grouperObject instanceof Group) {
       Group group = (Group) grouperObject;
       subjects.addAll(group.getUpdaters());
       subjects.addAll(group.getAdmins());
     } else if (grouperObject instanceof Stem) {
       Stem stem = (Stem) grouperObject;
       subjects.addAll(stem.getStemAdmins());
       subjects.addAll(stem.getStemmers());
     } else if (grouperObject instanceof AttributeDef) {
       AttributeDef attributeDef = (AttributeDef) grouperObject;
       subjects.addAll(AttributeDefResolverFactory.getInstance(grouperSession).getSubjectsWithPrivilege(attributeDef, AttributeDefPrivilege.ATTR_ADMIN));
       subjects.addAll(AttributeDefResolverFactory.getInstance(grouperSession).getSubjectsWithPrivilege(attributeDef, AttributeDefPrivilege.ATTR_UPDATE));
     }
     
     emailAddresses.addAll(GrouperEmailUtils.getEmails(subjects));
    } else if (StringUtils.isNotBlank(attributeValue.getMailToGroupString())) {
      String groupIdOrName = attributeValue.getMailToGroupString();
      Group group = GroupFinder.findByName(grouperSession, groupIdOrName, false);
      if (group == null) {
        group = GroupFinder.findByUuid(grouperSession, groupIdOrName, false);
      }
      
      if (group == null) {
        LOG.error("For "+grouperObject.getName()+" mailToGroup doesn't exist.");
        return emailAddresses;
      }
      
      Set<Member> mailToMembers = group.getMembers();
      Set<Subject> emailToSubjects = new HashSet<Subject>();
      for (Member mailToMember: mailToMembers) {
        emailToSubjects.add(mailToMember.getSubject());
      }
      emailAddresses.addAll(GrouperEmailUtils.getEmails(emailToSubjects));
    } else if (StringUtils.isNotBlank(attributeValue.getEmailAddressesString())) {
      
      String emailAddressesString = attributeValue.getEmailAddressesString();

      List<String> emailList = Arrays.asList(GrouperUtil.splitTrim(emailAddressesString, ","));
      emailAddresses.addAll(emailList); 
      
    } else {
      LOG.error("For "+grouperObject.getName()+" At least one of the email properties need to be set.");
    }
    return emailAddresses;
  }
  
  /**
   * Go through all email addresses and build email per person object for each of them.
   * @param attributeValue
   * @param emailsTo
   * @param userEmailObjects
   * @param subject
   * @param grouperObject
   */
  private void buildEmailPerPersonObjects(GrouperDeprovisioningAttributeValue attributeValue,
      Set<String> emailsTo, Map<String, EmailPerPerson> userEmailObjects, Subject subject, GrouperObject grouperObject) {
    
    String emailBody = attributeValue.getEmailBodyString();
    String affiliation = attributeValue.getAffiliationString();
    
    for (String emailTo: emailsTo) {
      EmailPerPerson emailPerPerson = null;
      if (userEmailObjects.containsKey(emailTo)) {
        emailPerPerson = userEmailObjects.get(emailTo);
      } else {
        emailPerPerson = new EmailPerPerson();
        userEmailObjects.put(emailTo, emailPerPerson);
      }
      
      if (StringUtils.isNotBlank(emailBody)) {
        emailBody = replaceTemplateVariables(emailBody, subject, grouperObject);
        emailPerPerson.customEmailBodies.add(emailBody);
      }
      buildEmailPerPersonObjectHelper(grouperObject, affiliation, emailPerPerson);
    }
    
  }
 
  /**
   * add to the correct list based on the grouper object type
   * @param grouperObject
   * @param affiliation
   * @param emailPerPerson
   */
  private void buildEmailPerPersonObjectHelper(GrouperObject grouperObject, String affiliation,
      EmailPerPerson emailPerPerson) {
    
    if (grouperObject instanceof Group) {
      emailPerPerson.deprovisioningGroupEmailObjects.add(new GrouperObjectWithAffiliation(affiliation, grouperObject));
    } else if (grouperObject instanceof Stem) {
      emailPerPerson.deprovisioningStemEmailObjects.add(new GrouperObjectWithAffiliation(affiliation, grouperObject));
    } else if (grouperObject instanceof AttributeDef) {
      emailPerPerson.deprovisioningAttributeDefEmailObjects.add(new GrouperObjectWithAffiliation(affiliation, grouperObject));
    }
  }
  

  public class EmailPerPerson {
    
    public List<String> customEmailBodies = new ArrayList<String>();
    
    public List<GrouperObjectWithAffiliation> deprovisioningGroupEmailObjects = new ArrayList<GrouperObjectWithAffiliation>();
    public List<GrouperObjectWithAffiliation> deprovisioningStemEmailObjects = new ArrayList<GrouperObjectWithAffiliation>();
    public List<GrouperObjectWithAffiliation> deprovisioningAttributeDefEmailObjects = new ArrayList<GrouperObjectWithAffiliation>();
    
  }
  
  private class GrouperObjectWithAffiliation {
    
    public String affiliation;
    public GrouperObject grouperObject;
    
    public GrouperObjectWithAffiliation(String affiliation, GrouperObject grouperObject) {
      this.affiliation = affiliation;
      this.grouperObject = grouperObject;
    }

    public String getAffiliation() {
      return affiliation;
    }
    
    public GrouperObject getGrouperObject() {
      return grouperObject;
    }

  }
  
}
