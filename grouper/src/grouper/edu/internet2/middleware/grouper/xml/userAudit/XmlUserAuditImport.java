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
/*
 * @author mchyzer
 * $Id: XmlUserAuditImport.java,v 1.3 2009-09-02 05:57:26 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.xml.userAudit;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.keyvalue.MultiKey;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.ElementHandler;
import org.dom4j.ElementPath;
import org.dom4j.io.SAXReader;
import org.hibernate.Session;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.Dom4JReader;

import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.audit.AuditEntry;
import edu.internet2.middleware.grouper.audit.AuditType;
import edu.internet2.middleware.grouper.hibernate.AuditControl;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionType;
import edu.internet2.middleware.grouper.hibernate.HibernateHandler;
import edu.internet2.middleware.grouper.hibernate.HibernateHandlerBean;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.provider.SubjectImpl;
import edu.internet2.middleware.subject.provider.SubjectTypeEnum;


/**
 *
 */
public class XmlUserAuditImport {

  /**
   * logger 
   */
  @SuppressWarnings("unused")
  private static final Log LOG = GrouperUtil.getLog(XmlUserAuditImport.class);

  /**
   * @param args
   */
  public static void main(String[] args) {
    new XmlUserAuditImport().readUserAudits(new File("c:/temp/export.xml"));
  }

  /**
   * version of grouper which exported the export file
   */
  private String importFileVersion = null;
  
  /** count of members/audits/types who have the same uuid in import file and DB */
  private int existingCount = 0;
  
  /** count of members/audits/types arent in the existing DB (will have the same uuid */
  private int newCount = 0;
  
  /** count of members/audits/types in import file */
  private int totalCount = 0;
  
  /** count of members/audits/types are in the DB under a different UUID, so there is a translation */
  private int changeCount = 0;
  
  /** xstream object which does the object conversion */
  private XStream xStream = null;

  /** all members in registry from uuid to xml member object (uuid, subjectId, sourceId) */
  private Map<String, XmlMember> allMembersInRegistry = null;
  
  /** all members in the registry from multikey(sourceId/subjectId) to xmlSubject */
  private Map<MultiKey, XmlMember> allMembersInRegistryBySubject = null;
  
  /** map from file uuid to registry uuid if there was a change */
  private Map<String, String> memberIdTranslation = null;
  
  /** list of all types in the DB before import */
  private Set<AuditType> allAuditTypesInDb = null;
  
  /** if there is a translation of audit type, then this is the old and new uuid */
  private Map<String, String> auditTypeTranslation = null;
  
  /** grouper's hibernate session */
  private HibernateSession hibernateSession = null;

  /** session (hib object) */
  private Session session = null;
  
  /**
   * 
   * @param importFile 
   */
  public void readUserAudits(final File importFile) {
  
    HibernateSession.callbackHibernateSession(GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, 
        AuditControl.WILL_NOT_AUDIT, new HibernateHandler() {
  
          public Object callback(HibernateHandlerBean hibernateHandlerBean)
              throws GrouperDAOException {
  
            try {
              
              xStream = XmlUserAuditExport.xstream();
              
              XmlUserAuditImport.this.hibernateSession = hibernateHandlerBean.getHibernateSession();
              XmlUserAuditImport.this.session = hibernateSession.getSession();
              
              SAXReader reader = new SAXReader();
              
              reader.addHandler( "/userAudits", 
                  new ElementHandler() {
                      public void onStart(ElementPath path) {
                        Element userAudit = path.getCurrent();
                        XmlUserAuditImport.this.importFileVersion = userAudit.attributeValue("version");
                        XmlUserAuditExport.logInfoAndPrintToScreen(
                            "userAudit import: reading document: " + GrouperUtil.fileCanonicalPath(importFile) 
                             + ", version: " + XmlUserAuditImport.this.importFileVersion);
                      }
                      public void onEnd(ElementPath path) {
                        //we done!
                      }
                  }
              );

              reader.addHandler( "/userAudits/xmlMembers", 
                  new ElementHandler() {
                      public void onStart(ElementPath path) {
                          clearCounts();    
                          initMembers();
                      }
                      public void onEnd(ElementPath path) {
                          // process a ROW element
                          Element row = path.getCurrent();
          
                          // prune the tree
                          row.detach();

                          logInfoAndPrintCounts("members");
                      }
                  }
              );

              reader.addHandler( "/userAudits/xmlMembers/XmlMember", 
                  new ElementHandler() {
                      public void onStart(ElementPath path) {
                          // do nothing here...    
                      }
                      public void onEnd(ElementPath path) {
                          // process a ROW element
                          Element row = path.getCurrent();
          
                          // prune the tree
                          row.detach();

                          XmlMember xmlMemberFromFile = (XmlMember)xStream.unmarshal(new Dom4JReader(row));
                          
                          XmlUserAuditImport.this.processXmlMemberFromFile(xmlMemberFromFile);
                          
                      }
                  }
              );

              //outer audit types element
              reader.addHandler( "/userAudits/xmlAuditTypes", 
                  new ElementHandler() {
                      public void onStart(ElementPath path) {
                        clearCounts();    
                        //get all xml audit type from the DB
                        XmlUserAuditImport.this.allAuditTypesInDb = GrouperDAOFactory.getFactory().getAuditType().findAll();
                        XmlUserAuditImport.this.auditTypeTranslation = new HashMap<String, String>();
                        
                      }
                      public void onEnd(ElementPath path) {
                          // process a ROW element
                          Element row = path.getCurrent();
          
                          // prune the tree
                          row.detach();

                          logInfoAndPrintCounts("auditTypes");
                      }
                  }
              );

              //inner audit type element
              reader.addHandler( "/userAudits/xmlAuditTypes/XmlAuditType", 
                  new ElementHandler() {
                      public void onStart(ElementPath path) {
                          // do nothing here...    
                      }
                      public void onEnd(ElementPath path) {
                          // process a ROW element
                          Element row = path.getCurrent();
          
                          // prune the tree
                          row.detach();

                          XmlAuditType xmlAuditTypeFromFile = (XmlAuditType)xStream.unmarshal(new Dom4JReader(row));
                          
                          XmlUserAuditImport.this.processXmlAuditTypeFromFile(xmlAuditTypeFromFile);
                          
                      }
                  }
              );

              //outer audit entries element
              reader.addHandler( "/userAudits/xmlAuditEntries", 
                  new ElementHandler() {
                      public void onStart(ElementPath path) {
                        clearCounts();
                        
                      }
                      public void onEnd(ElementPath path) {
                          // process a ROW element
                          Element row = path.getCurrent();
          
                          // prune the tree
                          row.detach();

                          logInfoAndPrintCounts("auditEntries");
                      }
                  }
              );

              //inner audit entry element
              reader.addHandler( "/userAudits/xmlAuditEntries/XmlAuditEntry", 
                  new ElementHandler() {
                      public void onStart(ElementPath path) {
                          // do nothing here...    
                      }
                      public void onEnd(ElementPath path) {
                          // process a ROW element
                          Element row = path.getCurrent();
          
                          // prune the tree
                          row.detach();

                          XmlAuditEntry xmlAuditEntryFromFile = (XmlAuditEntry)xStream.unmarshal(new Dom4JReader(row));
                          
                          XmlUserAuditImport.this.processXmlAuditEntryFromFile(xmlAuditEntryFromFile);
                          
                      }
                  }
              );

              reader.read(importFile);
            } catch (DocumentException de) {
              throw new RuntimeException("Problem reading file: " + GrouperUtil.fileCanonicalPath(importFile));
            }
            return null;
          }
    });
  }

  /**
   * init members from db
   */
  private void initMembers() {
    //lets cache the current members in the DB
    allMembersInRegistry = XmlMember.retrieveAllMembers(session);
    
    if (LOG.isInfoEnabled()) {
      LOG.info("userAudit import: Read in " + allMembersInRegistry.size() + " members into cache");
    }

    //lets make a reverse lookup too based on sourceId and subjectId
    allMembersInRegistryBySubject = new HashMap<MultiKey, XmlMember>();
    
    for (XmlMember xmlMember : allMembersInRegistry.values()) {
      
      allMembersInRegistryBySubject.put(new MultiKey(xmlMember.getSourceId(), xmlMember.getSubjectId()), xmlMember);
      
    }
    
    if (LOG.isInfoEnabled()) {
      LOG.info("userAudit import: Created reverse lookup member cache");
    }
    
    //map from old member id to new member id
    memberIdTranslation = new HashMap<String, String>();

  }
  
  /**
   * @param xmlMemberFromFile
   */
  private void processXmlMemberFromFile(XmlMember xmlMemberFromFile) {
    
    this.totalCount++;
    
    XmlMember xmlMemberInDb = allMembersInRegistry.get(xmlMemberFromFile.getUuid());
    
    if (xmlMemberInDb != null) {
      LOG.debug("userAudit import: read existing member: " + xmlMemberFromFile.toString());
      XmlUserAuditImport.this.existingCount++;
      return;
    }
    
    xmlMemberInDb = allMembersInRegistryBySubject.get(
        new MultiKey(xmlMemberFromFile.getSourceId(), xmlMemberFromFile.getSubjectId()));
    
    if (xmlMemberInDb != null) {
      //change in uuid
      LOG.debug("userAudit import: read member with changed uuid: " 
          + xmlMemberFromFile.toString() + ", to uuid: " + xmlMemberInDb.getUuid());
      
      XmlUserAuditImport.this.changeCount++;
      memberIdTranslation.put(xmlMemberFromFile.getUuid(), xmlMemberInDb.getUuid());
      return;
    }

    XmlUserAuditImport.this.newCount++;
    
    //this is a new member
    LOG.debug("userAudit import: read new member: " 
        + xmlMemberFromFile.toString());
    
    SubjectImpl simpleSubject = new SubjectImpl(
        xmlMemberFromFile.getSubjectId(), xmlMemberFromFile.getSubjectId(), 
        xmlMemberFromFile.getSubjectId(), SubjectTypeEnum.PERSON.getName(), xmlMemberFromFile.getSourceId());
    Member member = MemberFinder.internal_findBySubject(simpleSubject, xmlMemberFromFile.getUuid(), true);
 
    if (!StringUtils.equals(member.getUuid(), xmlMemberFromFile.getUuid())) {
      throw new RuntimeException("Why is member uuid not inserted??? " + xmlMemberFromFile);
    }
  }

  /**
   * @param xmlAuditTypeFromFile
   */
  private void processXmlAuditTypeFromFile(XmlAuditType xmlAuditTypeFromFile) {
    AuditType auditTypeInDb = null;
    
    for (AuditType auditType : this.allAuditTypesInDb) {
      
      if (StringUtils.equals(xmlAuditTypeFromFile.getId(), auditType.getId())) {
        auditTypeInDb = auditType;
        break;
      }
      
      if (StringUtils.equals(xmlAuditTypeFromFile.getAuditCategory(), auditType.getAuditCategory())
          && StringUtils.equals(xmlAuditTypeFromFile.getActionName(), auditType.getActionName())) {
        auditTypeInDb = auditType;
        break;
      }
      
    }
    
    AuditType auditTypeFromFile = xmlAuditTypeFromFile.toAuditType();
  
    GrouperUtil.substituteStrings(this.memberIdTranslation, auditTypeFromFile);
    
    XmlUserAuditImport.this.totalCount++;
    
    //if new entry
    if (auditTypeInDb == null) {
      LOG.debug("userAudit import: read new auditType: " + auditTypeFromFile.toStringDeep()); 
      GrouperDAOFactory.getFactory().getAuditType().saveOrUpdate(auditTypeFromFile);
      XmlUserAuditImport.this.newCount++;
      return;
    }
    
    //see if translating
    boolean translatingId = !StringUtils.equals(auditTypeFromFile.getId(), auditTypeInDb.getId());
    if (translatingId) {
      this.auditTypeTranslation.put(auditTypeFromFile.getId(), auditTypeInDb.getId());
    }
    
    if (auditTypeFromFile.equalsDeep(auditTypeInDb)) {
      XmlUserAuditImport.this.existingCount++;
      if (!translatingId) {
        //they are equal, let it be
        LOG.debug("userAudit import: auditType exists in DB: " + auditTypeFromFile); 
        return;
      }
      
      //they are equal except for id
      LOG.debug("userAudit import: auditType exists in DB with different UUID: " + auditTypeFromFile 
          + ", new id: " + auditTypeInDb.getId()); 
      
      return;
    }
  
    //at this point, it exists, but its not the same
    XmlUserAuditImport.this.changeCount++;
    LOG.debug("userAudit import: auditType exists in DB but is different, will be: " + auditTypeFromFile); 
    
    auditTypeFromFile.setId(auditTypeInDb.getId());
    auditTypeFromFile.setHibernateVersionNumber(auditTypeInDb.getHibernateVersionNumber());
    GrouperDAOFactory.getFactory().getAuditType().saveOrUpdate(auditTypeFromFile);
    
  }

  /**
   * @param xmlAuditEntryFromFile
   */
  private void processXmlAuditEntryFromFile(XmlAuditEntry xmlAuditEntryFromFile) {

    AuditEntry auditEntryInDb = GrouperDAOFactory.getFactory().getAuditEntry()
      .findById(xmlAuditEntryFromFile.getId(), false);

    AuditEntry auditEntryFromFile = xmlAuditEntryFromFile.toAuditEntry();

    boolean newMemberId = GrouperUtil.substituteStrings(this.memberIdTranslation, auditEntryFromFile);

    XmlUserAuditImport.this.totalCount++;
    
    //if new entry
    if (auditEntryInDb == null) {
      LOG.debug("userAudit import: read new auditEntry: " + auditEntryFromFile.toStringDeep()); 
      GrouperDAOFactory.getFactory().getAuditEntry().saveOrUpdate(auditEntryFromFile);
      XmlUserAuditImport.this.newCount++;
      return;
    }
    
    if (auditEntryFromFile.equalsDeep(auditEntryInDb)) {
      //they are equal, let it be
      XmlUserAuditImport.this.existingCount++;
      LOG.debug("userAudit import: auditEntry exists in DB: " + auditEntryFromFile); 
      return;
    }
      
    XmlUserAuditImport.this.changeCount++;
      
    //lets see if the auditType changed
    String newAuditTypeId = this.auditTypeTranslation.get(auditEntryFromFile.getAuditTypeId());
    boolean needsNewAuditTypeId = !StringUtils.isBlank(newAuditTypeId);
    if (needsNewAuditTypeId) {
      auditEntryFromFile.setAuditTypeId(newAuditTypeId);
    }

    //see if there are member uuids to change

    
    //they are equal except for id
    LOG.debug("userAudit import: auditEntry exists in DB with same UUID and different values " +
    		"(typeChange? " + (needsNewAuditTypeId ? "T" : "F") + ", memberIdChange? " 
    		+ (newMemberId ? "T" : "F") + "): " + auditEntryFromFile); 
    
    auditEntryFromFile.setId(auditEntryInDb.getId());
    auditEntryFromFile.setHibernateVersionNumber(auditEntryInDb.getHibernateVersionNumber());
    GrouperDAOFactory.getFactory().getAuditEntry().saveOrUpdate(auditEntryFromFile);
    
  }

  /**
   * 
   */
  private void clearCounts() {
    XmlUserAuditImport.this.changeCount = 0;    
    XmlUserAuditImport.this.newCount = 0;    
    XmlUserAuditImport.this.existingCount = 0;    
    XmlUserAuditImport.this.totalCount = 0;
  }

  /**
   * @param label
   */
  private void logInfoAndPrintCounts(String label) {
    XmlUserAuditExport.logInfoAndPrintToScreen(
        "userAudit import: completed with " + StringUtils.rightPad(label + ":", 13) + " total: " 
        + XmlUserAuditImport.this.totalCount + ", new: " 
        + XmlUserAuditImport.this.newCount + ", existing: " 
        + XmlUserAuditImport.this.existingCount + ", changed: "
        + XmlUserAuditImport.this.changeCount);
  }

}
