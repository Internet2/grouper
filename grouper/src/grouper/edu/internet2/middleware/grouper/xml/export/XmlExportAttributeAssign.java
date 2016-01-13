/**
 * Copyright 2014 Internet2
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
 */
/**
 * @author mchyzer
 * $Id: XmlExportGroup.java 6216 2010-01-10 04:52:30Z mchyzer $
 */
package edu.internet2.middleware.grouper.xml.export;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.dom4j.Element;
import org.dom4j.ElementHandler;
import org.dom4j.ElementPath;
import org.hibernate.Query;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.xml.CompactWriter;
import com.thoughtworks.xstream.io.xml.Dom4JReader;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignType;
import edu.internet2.middleware.grouper.attr.value.AttributeAssignValue;
import edu.internet2.middleware.grouper.hibernate.AuditControl;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionType;
import edu.internet2.middleware.grouper.hibernate.HibUtils;
import edu.internet2.middleware.grouper.hibernate.HibernateHandler;
import edu.internet2.middleware.grouper.hibernate.HibernateHandlerBean;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.misc.GrouperVersion;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.xml.importXml.XmlImportMain;


/**
 *
 */
public class XmlExportAttributeAssign {

  /**
   * 
   */
  private static final String XML_EXPORT_ATTRIBUTE_ASSIGN_XPATH = "/grouperExport/attributeAssigns/XmlExportAttributeAssign";

  /**
   * 
   */
  private static final String ATTRIBUTE_ASSIGNS_XPATH = "/grouperExport/attributeAssigns";

  /** attributeAssignActionId */
  private String attributeAssignActionId;
  
  /** uuid */
  private String uuid;
  
  /** createTime */
  private String createTime;

  /** modifierTime */
  private String modifierTime;

  /** hibernateVersionNumber */
  private long hibernateVersionNumber;

  /** contextId */
  private String contextId;

  /**
   * 
   */
  public XmlExportAttributeAssign() {
    
  }

  
  
  /**
   * attributeAssignActionId
   * @return attributeAssignActionId
   */
  public String getAttributeAssignActionId() {
    return this.attributeAssignActionId;
  }



  /**
   * attributeAssignActionId
   * @param attributeAssignActionId1
   */
  public void setAttributeAssignActionId(String attributeAssignActionId1) {
    this.attributeAssignActionId = attributeAssignActionId1;
  }

  /**
   * attributeDefNameId
   */
  private String attributeDefNameId;

  /**
   * attributeDefNameId
   * @return attributeDefNameId
   */
  public String getAttributeDefNameId() {
    return this.attributeDefNameId;
  }



  /**
   * attributeDefNameId
   * @param attributeDefNameId1
   */
  public void setAttributeDefNameId(String attributeDefNameId1) {
    this.attributeDefNameId = attributeDefNameId1;
  }

  /**
   * disabledTime
   */
  private String disabledTime;

  
  
  /**
   * disabledTime
   * @return disabledTime
   */
  public String getDisabledTime() {
    return this.disabledTime;
  }



  /**
   * disabledTime
   * @param disabledTime1
   */
  public void setDisabledTime(String disabledTime1) {
    this.disabledTime = disabledTime1;
  }

  /** enabled */
  private String enabled;

  /**
   * enabled
   * @return enabled
   */
  public String getEnabled() {
    return this.enabled;
  }



  /**
   * enabled
   * @param enabled1
   */
  public void setEnabled(String enabled1) {
    this.enabled = enabled1;
  }

  /** allowed */
  private String disallowed;

  /**
   * allowed
   * @return allowed
   */
  public String getDisallowed() {
    return this.disallowed;
  }



  /**
   * allowed
   * @param disallowed1
   */
  public void setDisallowed(String disallowed1) {
    this.disallowed = disallowed1;
  }

  /** enabledTime */
  private String enabledTime;
  

  /**
   * enabledTime
   * @return enabledTime
   */
  public String getEnabledTime() {
    return this.enabledTime;
  }



  /**
   * enabledTime
   * @param enabledTime1
   */
  public void setEnabledTime(String enabledTime1) {
    this.enabledTime = enabledTime1;
  }

  /** notes */
  private String notes;

  
  
  /**
   * notes
   * @return notes
   */
  public String getNotes() {
    return this.notes;
  }



  /**
   * notes
   * @param notes
   */
  public void setNotes(String notes) {
    this.notes = notes;
  }

  /** attributeAssignDelegatable */
  private String attributeAssignDelegatable;

  
  
  /**
   * attributeAssignDelegatable
   * @return attributeAssignDelegatable
   */
  public String getAttributeAssignDelegatable() {
    return this.attributeAssignDelegatable;
  }



  /**
   * attributeAssignDelegatable
   * @param attributeAssignDelegatable1
   */
  public void setAttributeAssignDelegatable(String attributeAssignDelegatable1) {
    this.attributeAssignDelegatable = attributeAssignDelegatable1;
  }

  /** attributeAssignType */
  private String attributeAssignType;
  
  
  
  /**
   * attributeAssignType
   * @return attributeAssignType
   */
  public String getAttributeAssignType() {
    return this.attributeAssignType;
  }



  /**
   * attributeAssignType
   * @param attributeAssignType1
   */
  public void setAttributeAssignType(String attributeAssignType1) {
    this.attributeAssignType = attributeAssignType1;
  }

  /**
   * ownerAttributeAssignId
   */
  private String ownerAttributeAssignId;
  
  

  /**
   * ownerAttributeAssignId
   * @return ownerAttributeAssignId
   */
  public String getOwnerAttributeAssignId() {
    return this.ownerAttributeAssignId;
  }



  /**
   * ownerAttributeAssignId
   * @param ownerAttributeAssignId1
   */
  public void setOwnerAttributeAssignId(String ownerAttributeAssignId1) {
    this.ownerAttributeAssignId = ownerAttributeAssignId1;
  }

  /** ownerAttributeDefId */
  private String ownerAttributeDefId;
  
  
  
  /**
   * ownerAttributeDefId
   * @return ownerAttributeDefId
   */
  public String getOwnerAttributeDefId() {
    return this.ownerAttributeDefId;
  }



  /**
   * ownerAttributeDefId
   * @param ownerAttributeDefId1
   */
  public void setOwnerAttributeDefId(String ownerAttributeDefId1) {
    this.ownerAttributeDefId = ownerAttributeDefId1;
  }

  /** ownerGroupId */
  private String ownerGroupId;
  
  
  
  /**
   * ownerGroupId
   * @return ownerGroupId
   */
  public String getOwnerGroupId() {
    return this.ownerGroupId;
  }



  /**
   * ownerGroupId
   * @param ownerGroupId1
   */
  public void setOwnerGroupId(String ownerGroupId1) {
    this.ownerGroupId = ownerGroupId1;
  }

  /** ownerMemberId */
  private String ownerMemberId;
  
  
  
  /**
   * ownerMemberId
   * @return ownerMemberId
   */
  public String getOwnerMemberId() {
    return this.ownerMemberId;
  }



  /**
   * ownerMemberId
   * @param ownerMemberId1
   */
  public void setOwnerMemberId(String ownerMemberId1) {
    this.ownerMemberId = ownerMemberId1;
  }

  /** ownerMembershipId */
  private String ownerMembershipId;

  /** ownerStemId */
  private String ownerStemId;

  /**
   * logger 
   */
  private static final Log LOG = GrouperUtil.getLog(XmlExportAttributeAssign.class);
  
  /**
   * ownerStemId
   * @return ownerStemId
   */
  public String getOwnerStemId() {
    return this.ownerStemId;
  }



  /**
   * ownerStemId
   * @param ownerStemId1
   */
  public void setOwnerStemId(String ownerStemId1) {
    this.ownerStemId = ownerStemId1;
  }



  /**
   * ownerMembershipId
   * @return ownerMembershipId
   */
  public String getOwnerMembershipId() {
    return this.ownerMembershipId;
  }



  /**
   * ownerMembershipId
   * @param ownerMembershipId1
   */
  public void setOwnerMembershipId(String ownerMembershipId1) {
    this.ownerMembershipId = ownerMembershipId1;
  }

  


  /**
   * uuid
   * @return uuid
   */
  public String getUuid() {
    return this.uuid;
  }

  /**
   * uuid
   * @param uuid1
   */
  public void setUuid(String uuid1) {
    this.uuid = uuid1;
  }

  /**
   * createTime
   * @return createTime
   */
  public String getCreateTime() {
    return this.createTime;
  }

  /**
   * createTime
   * @param createTime1
   */
  public void setCreateTime(String createTime1) {
    this.createTime = createTime1;
  }

  /**
   * modifierTime
   * @return modifierTime
   */
  public String getModifierTime() {
    return this.modifierTime;
  }

  /**
   * modifierTime
   * @param modifierTime1
   */
  public void setModifierTime(String modifierTime1) {
    this.modifierTime = modifierTime1;
  }

  /**
   * hibernateVersionNumber
   * @return hibernateVersionNumber
   */
  public long getHibernateVersionNumber() {
    return this.hibernateVersionNumber;
  }

  /**
   * hibernateVersionNumber
   * @param hibernateVersionNumber1
   */
  public void setHibernateVersionNumber(long hibernateVersionNumber1) {
    this.hibernateVersionNumber = hibernateVersionNumber1;
  }

  /**
   * contextId
   * @return contextId
   */
  public String getContextId() {
    return this.contextId;
  }

  /**
   * contextId
   * @param contextId1
   */
  public void setContextId(String contextId1) {
    this.contextId = contextId1;
  }
  
  /**
   * convert to AttributeAssign
   * @return the AttributeAssign
   */
  public AttributeAssign toAttributeAssign() {
    AttributeAssign attributeAssign = new AttributeAssign();
    
    attributeAssign.setDisallowedDb(this.disallowed);
    attributeAssign.setAttributeAssignActionId(this.attributeAssignActionId);
    attributeAssign.setAttributeAssignDelegatableDb(this.attributeAssignDelegatable);
    attributeAssign.setAttributeAssignTypeDb(this.attributeAssignType);
    attributeAssign.setAttributeDefNameId(this.attributeDefNameId);
    attributeAssign.setContextId(this.contextId);
    attributeAssign.setCreatedOnDb(GrouperUtil.dateLongValue(this.createTime));
    attributeAssign.setDisabledTimeDb(GrouperUtil.dateLongValue(this.disabledTime));
    attributeAssign.setEnabledDb(this.enabled);
    attributeAssign.setEnabledTimeDb(GrouperUtil.dateLongValue(this.enabledTime));
    attributeAssign.setHibernateVersionNumber(this.hibernateVersionNumber);
    attributeAssign.setLastUpdatedDb(GrouperUtil.dateLongValue(this.modifierTime));
    attributeAssign.setId(this.uuid);
    attributeAssign.setNotes(this.notes);
    attributeAssign.setOwnerAttributeAssignId(this.ownerAttributeAssignId);
    attributeAssign.setOwnerAttributeDefId(this.ownerAttributeDefId);
    attributeAssign.setOwnerGroupId(this.ownerGroupId);
    attributeAssign.setOwnerMemberId(this.ownerMemberId);
    attributeAssign.setOwnerMembershipId(this.ownerMembershipId);
    attributeAssign.setOwnerStemId(this.ownerStemId);
    
    return attributeAssign;
  }

  /**
   * @param exportVersion
   * @return the xml string
   */
  public String toXml(GrouperVersion exportVersion) {
    StringWriter stringWriter = new StringWriter();
    this.toXml(exportVersion, stringWriter);
    return stringWriter.toString();
  }

  /**
   * @param exportVersion 
   * @param writer
   */
  public void toXml(
      @SuppressWarnings("unused") GrouperVersion exportVersion, Writer writer) {
    XStream xStream = XmlExportUtils.xstream();
  
    CompactWriter compactWriter = new CompactWriter(writer);
    
    xStream.marshal(this, compactWriter);
  
  }

  /**
   * @param xmlExportMain
   * @param writer
   */
  public static void exportAttributeAssigns(final Writer writer, final XmlExportMain xmlExportMain) {
  
    HibernateSession.callbackHibernateSession(GrouperTransactionType.READONLY_OR_USE_EXISTING, 
        AuditControl.WILL_NOT_AUDIT, new HibernateHandler() {
      
      public Object callback(HibernateHandlerBean hibernateHandlerBean)
          throws GrouperDAOException {
  
        Session session = hibernateHandlerBean.getHibernateSession().getSession();
  
        //select all members in order
        //order by attributeAssignId so the ones not about assigns are first.
        Query query = session.createQuery(
            "select distinct theAttributeAssign " + exportFromOnQuery(xmlExportMain, true));
  
        GrouperVersion grouperVersion = new GrouperVersion(GrouperVersion.GROUPER_VERSION);
        
        Set<AttributeAssign> attributeAssignsOfAssigns = new LinkedHashSet<AttributeAssign>();
        
        try {
          writer.write("  <attributeAssigns>\n");
  
          //this is an efficient low-memory way to iterate through a resultset
          ScrollableResults results = null;
          try {
            results = query.scroll();
            while(results.next()) {
              Object object = results.get(0);
              final AttributeAssign attributeAssign = (AttributeAssign)object;
              
              if (attributeAssign.getAttributeAssignType() == AttributeAssignType.imm_mem || 
                  attributeAssign.getAttributeAssignType() == AttributeAssignType.imm_mem_asgn) {
                //do these in a future phase
                xmlExportMain.getAttributeAssignsForSecondPhase().put(attributeAssign.getId(), attributeAssign);
              } else if (!StringUtils.isBlank(attributeAssign.getOwnerAttributeAssignId())) {
                //do the attribute assigns of assigns later on (just below)
                attributeAssignsOfAssigns.add(attributeAssign);
              } else {
              
                xmlExportMain.getAttributeAssignIds().add(attributeAssign.getId());
  
                exportAttributeAssign(writer, xmlExportMain, grouperVersion,
                    attributeAssign);
              }
            }
          } finally {
            HibUtils.closeQuietly(results);
          }
  
          for (AttributeAssign attributeAssignOfAssign: attributeAssignsOfAssigns) {
            
            //if the foreign key isnt there, then dont export
            if (xmlExportMain.getAttributeAssignIds().contains(attributeAssignOfAssign.getOwnerAttributeAssignId())) {
  
              xmlExportMain.getAttributeAssignIds().add(attributeAssignOfAssign.getId());
  
              exportAttributeAssign(writer, xmlExportMain, grouperVersion,
                  attributeAssignOfAssign);
            }
          }
          
          if (xmlExportMain.isIncludeComments()) {
            writer.write("\n");
          }
          
          //end the attribute assigns element 
          writer.write("  </attributeAssigns>\n");
        } catch (IOException ioe) {
          throw new RuntimeException("Problem with streaming attributeAssigns", ioe);
        }
        return null;
      }
  
    });
  }



  /**
   * parse the xml file for groups
   * @param xmlImportMain
   */
  public static void processXmlSecondPass(final XmlImportMain xmlImportMain) {
    xmlImportMain.getReader().addHandler( ATTRIBUTE_ASSIGNS_XPATH, 
        new ElementHandler() {
            public void onStart(ElementPath path) {
            }
            public void onEnd(ElementPath path) {
                // process a ROW element
                Element row = path.getCurrent();
  
                // prune the tree
                row.detach();
            }
        }
    );
  
    xmlImportMain.getReader().addHandler( XML_EXPORT_ATTRIBUTE_ASSIGN_XPATH, 
        new ElementHandler() {
            public void onStart(ElementPath path) {
                // do nothing here...    
            }
            public void onEnd(ElementPath path) {
  
              Element row = null;
              try {
                // process a ROW element
                row = path.getCurrent();
  
                // prune the tree
                row.detach();
  
                XmlExportAttributeAssign xmlExportAttributeAssignFromFile = (XmlExportAttributeAssign)xmlImportMain.getXstream().unmarshal(new Dom4JReader(row));
                
                AttributeAssign attributeAssign = xmlExportAttributeAssignFromFile.toAttributeAssign();
                
                XmlExportUtils.syncImportableMultiple(attributeAssign, xmlImportMain);
                
                xmlImportMain.incrementCurrentCount();
              } catch (RuntimeException re) {
                LOG.error("Problem importing attributeActionAssign: " + XmlExportUtils.toString(row), re);
                throw re;
              }
            }
        }
    );
  
  }


  /**
   * get db count
   * @param xmlExportMain 
   * @return db count
   */
  public static long dbCount(XmlExportMain xmlExportMain) {
    long result = HibernateSession.byHqlStatic().createQuery("select count(theAttributeAssign) " 
        + exportFromOnQuery(xmlExportMain, false)).uniqueResult(Long.class);
    return result;
  }
  
  /**
   * get the query from the FROM clause on to the end for export
   * @param xmlExportMain
   * @param includeOrderBy 
   * @return the export query
   */
  private static String exportFromOnQuery(XmlExportMain xmlExportMain, boolean includeOrderBy) {
    //select all members in order
    StringBuilder queryBuilder = new StringBuilder();
    if (!xmlExportMain.filterStemsOrObjects()) {
      queryBuilder.append(" from AttributeAssign as theAttributeAssign ");
    } else {
      queryBuilder.append(
        " from AttributeAssign as theAttributeAssign, AttributeDefName theAttributeDefName, AttributeDef theAttributeDef where " +
        " theAttributeAssign.attributeDefNameId = theAttributeDefName.id and theAttributeDefName.attributeDefId = theAttributeDef.id " +
        " and ( ");
      xmlExportMain.appendHqlStemLikeOrObjectEquals(queryBuilder, "theAttributeDefName", "nameDb", false);
      queryBuilder.append(" ) and ( ");
      xmlExportMain.appendHqlStemLikeOrObjectEquals(queryBuilder, "theAttributeDef", "nameDb", false);
      queryBuilder.append(" ) " +
        " and ( theAttributeAssign.ownerAttributeAssignId is not null or " +
        " ( " +
        " exists ( select theGroup from Group as theGroup " +
        " where theAttributeAssign.ownerGroupId = theGroup.uuid and ( ");
      xmlExportMain.appendHqlStemLikeOrObjectEquals(queryBuilder, "theGroup", "nameDb", false);
      queryBuilder.append(" ) ) ) ");
      queryBuilder.append(" or ( " +
        " exists ( select theStem from Stem as theStem " +
        " where theAttributeAssign.ownerStemId = theStem.uuid and ( ");
      xmlExportMain.appendHqlStemLikeOrObjectEquals(queryBuilder, "theStem", "nameDb", true);
      queryBuilder.append(" ) ) ) ");
      queryBuilder.append(" or ( " +
          " exists ( select theAttributeDefAssn from AttributeDef as theAttributeDefAssn " +
          " where theAttributeAssign.ownerAttributeDefId = theAttributeDefAssn.id and ( ");
      xmlExportMain.appendHqlStemLikeOrObjectEquals(queryBuilder, "theAttributeDefAssn", "nameDb", true);
      queryBuilder.append(" ) ) ) ");
      queryBuilder.append(" or ( " +
          " exists ( select theMembership from ImmediateMembershipEntry as theMembership " +
          " where theAttributeAssign.ownerMembershipId = theMembership.immediateMembershipId and ( ");
      queryBuilder.append(" exists ( select theGroup2 from Group as theGroup2 where theGroup2.uuid = theMembership.ownerGroupId and ( ");
      xmlExportMain.appendHqlStemLikeOrObjectEquals(queryBuilder, "theGroup2", "nameDb", true);
      queryBuilder.append(" ) ) ");
      queryBuilder.append(" or exists ( select theStem2 from Stem as theStem2 where theStem2.uuid = theMembership.ownerStemId and ( ");
      xmlExportMain.appendHqlStemLikeOrObjectEquals(queryBuilder, "theStem2", "nameDb", true);
      queryBuilder.append(" ) ) ");
      queryBuilder.append(" or exists ( select theAttributeDef2 from AttributeDef as theAttributeDef2 where theAttributeDef2.id = theMembership.ownerAttrDefId and ( ");
      xmlExportMain.appendHqlStemLikeOrObjectEquals(queryBuilder, "theAttributeDef2", "nameDb", true);
      queryBuilder.append(" ) ) ");
      queryBuilder.append(" ) ) ) ");
      queryBuilder.append(" ) ");
    }
    if (includeOrderBy) {
      queryBuilder.append(" order by theAttributeAssign.ownerAttributeAssignId, theAttributeAssign.id");
    }
    return queryBuilder.toString();
  }

  /**
   * @param xmlExportMain
   * @param writer
   */
  public static void exportAttributeAssignsGsh(final Writer writer, final XmlExportMain xmlExportMain) {

    HibernateSession.callbackHibernateSession(GrouperTransactionType.READONLY_OR_USE_EXISTING, 
        AuditControl.WILL_NOT_AUDIT, new HibernateHandler() {
      
      public Object callback(HibernateHandlerBean hibernateHandlerBean)
          throws GrouperDAOException {
  
        Session session = hibernateHandlerBean.getHibernateSession().getSession();
  
        //select all members in order
        //order by attributeAssignId so the ones not about assigns are first.
        Query query = session.createQuery(
            "select distinct theAttributeAssign " + exportFromOnQuery(xmlExportMain, true));
  
        GrouperVersion grouperVersion = new GrouperVersion(GrouperVersion.GROUPER_VERSION);
        
        try {
  
          //keep a set of attribute assign ids (original assigns, not the attributes on attribute assignments)
          //so when merging with existing attributes we dont clobber existing one
          writer.write("attributeAssignIdsAlreadyUsed = new HashSet<String>();\n");
          
          //this is an efficient low-memory way to iterate through a resultset
          ScrollableResults results = null;
          try {
            results = query.scroll();
            while(results.next()) {
              Object object = results.get(0);
              final AttributeAssign attributeAssign = (AttributeAssign)object;
              
              if (attributeAssign.getAttributeAssignType().isAssignmentOnAssignment()) {
                continue;
              }
              exportAttributeAssignGsh(writer, xmlExportMain, grouperVersion,
                  attributeAssign);
            }
          } finally {
            HibUtils.closeQuietly(results);
          }

        } catch (IOException ioe) {
          throw new RuntimeException("Problem with streaming attributeAssigns", ioe);
        }
        return null;
      }

    });
  }
  
  /**
   * @param xmlExportMain
   * @param writer
   */
  public static void exportAttributeAssignsSecondPhase(final Writer writer, final XmlExportMain xmlExportMain) {

    HibernateSession.callbackHibernateSession(GrouperTransactionType.READONLY_OR_USE_EXISTING, 
        AuditControl.WILL_NOT_AUDIT, new HibernateHandler() {
      
      public Object callback(HibernateHandlerBean hibernateHandlerBean)
          throws GrouperDAOException {
  
        GrouperVersion grouperVersion = new GrouperVersion(GrouperVersion.GROUPER_VERSION);
        
        Set<AttributeAssign> attributeAssignsOfAssigns = new LinkedHashSet<AttributeAssign>();
        
        try {
          writer.write("  <attributeAssigns>\n");
              
          for (AttributeAssign attributeAssign : xmlExportMain.getAttributeAssignsForSecondPhase().values()) {
            
            if (!StringUtils.isBlank(attributeAssign.getOwnerAttributeAssignId())) {
              //do the attribute assigns of assigns later on (just below)
              attributeAssignsOfAssigns.add(attributeAssign);
            } else {
            
              xmlExportMain.getAttributeAssignIds().add(attributeAssign.getId());

              exportAttributeAssign(writer, xmlExportMain, grouperVersion,
                  attributeAssign);
            }
          }

          for (AttributeAssign attributeAssignOfAssign: attributeAssignsOfAssigns) {
            
            //if the foreign key isnt there, then dont export
            if (xmlExportMain.getAttributeAssignIds().contains(attributeAssignOfAssign.getOwnerAttributeAssignId())) {

              xmlExportMain.getAttributeAssignIds().add(attributeAssignOfAssign.getId());

              exportAttributeAssign(writer, xmlExportMain, grouperVersion,
                  attributeAssignOfAssign);
            }
          }
          
          if (xmlExportMain.isIncludeComments()) {
            writer.write("\n");
          }
          
          //end the attribute assigns element 
          writer.write("  </attributeAssigns>\n");
        } catch (IOException ioe) {
          throw new RuntimeException("Problem with streaming attributeAssigns (second phase)", ioe);
        }
        return null;
      }

    });
  }

  /**
   * @param writer
   * @param xmlExportMain
   * @param grouperVersion
   * @param attributeAssign
   * @throws IOException
   */
  private static void exportAttributeAssign(final Writer writer,
      final XmlExportMain xmlExportMain, GrouperVersion grouperVersion,
      final AttributeAssign attributeAssign) throws IOException {
    //comments to dereference the foreign keys
    if (xmlExportMain.isIncludeComments()) {
      HibernateSession.callbackHibernateSession(GrouperTransactionType.READONLY_NEW, AuditControl.WILL_NOT_AUDIT, new HibernateHandler() {
        
        public Object callback(HibernateHandlerBean hibernateHandlerBean)
            throws GrouperDAOException {
          try {
            writer.write("\n    <!--  ");
            
            XmlExportUtils.toStringAttributeAssign(null, writer, attributeAssign, false);

            writer.write(" -->\n");
            return null;
          } catch (IOException ioe) {
            throw new RuntimeException(ioe);
          }
        }
      });
    }
    
    XmlExportAttributeAssign xmlExportAttributeAssign = attributeAssign.xmlToExportAttributeAssign(grouperVersion);
    writer.write("    ");
    xmlExportAttributeAssign.toXml(grouperVersion, writer);
    writer.write("\n");
    xmlExportMain.incrementRecordCount();
  }

  /**
   * @param writer
   * @param xmlExportMain
   * @param grouperVersion
   * @param attributeAssign
   * @throws IOException
   */
  private static void exportAttributeAssignGsh(final Writer writer,
      final XmlExportMain xmlExportMain, GrouperVersion grouperVersion,
      final AttributeAssign attributeAssign) throws IOException {

    XmlExportAttributeAssign xmlExportAttributeAssign = attributeAssign.xmlToExportAttributeAssign(grouperVersion);
    xmlExportAttributeAssign.toXml(grouperVersion, writer);
    xmlExportMain.incrementRecordCount();

    HibernateSession.callbackHibernateSession(GrouperTransactionType.READONLY_NEW, AuditControl.WILL_NOT_AUDIT, new HibernateHandler() {
      
      public Object callback(HibernateHandlerBean hibernateHandlerBean)
          throws GrouperDAOException {
        try {

          //lets get values
          Set<AttributeAssign> attributeAssignsOnAssigns = GrouperUtil.nonNull(GrouperDAOFactory.getFactory().getAttributeAssign().findAssignmentsFromAssignments(GrouperUtil.toSet(attributeAssign), null, null, false));
          
          Set<String> attributeAssignIdsForValues = new HashSet<String>();
          
          attributeAssignIdsForValues.add(attributeAssign.getId());
          
          for (AttributeAssign currentAttributeAssign : attributeAssignsOnAssigns) {
            
            attributeAssignIdsForValues.add(currentAttributeAssign.getId());
            
          }
          
          Set<AttributeAssignValue> attributeAssignValues = GrouperDAOFactory.getFactory().getAttributeAssignValue().findByAttributeAssignIds(attributeAssignIdsForValues);

          //convert the values with id of the attribute these values are attached to as key, and set of values as the values
          Map<String, Set<AttributeAssignValue>> mapOfAttributeAssignIdToValues = new HashMap<String, Set<AttributeAssignValue>>();
          
          for (AttributeAssignValue attributeAssignValue : attributeAssignValues) {
            
            String key = attributeAssignValue.getAttributeAssignId();
            
            Set<AttributeAssignValue> theseAttributeAssignValues = mapOfAttributeAssignIdToValues.get(key);
            if (theseAttributeAssignValues == null) {
              theseAttributeAssignValues = new HashSet<AttributeAssignValue>();
              mapOfAttributeAssignIdToValues.put(key, theseAttributeAssignValues);
            }

            theseAttributeAssignValues.add(attributeAssignValue);
          }

          
          
          //now we have all the attributes on attributes, and all values for all
          if (!StringUtils.isBlank(attributeAssign.getOwnerStemId())) {
            
            Stem stem = StemFinder.findByUuid(GrouperSession.staticGrouperSession(), attributeAssign.getOwnerStemId(), true);
            
            writer.write("stem = StemFinder.findByName(grouperSession, \""
                + GrouperUtil.escapeDoubleQuotes(stem.getName()) + "\", false);\n");

            writer.write("if (stem != null) { ");
            
              
            writer.write(" } else { System.out.println(\"ERROR: cant find stem: '" + stem.getName() + "'\"); } ");
            
          }
          if (!StringUtils.isBlank(attributeAssign.getOwnerGroupId())) {
            XmlExportUtils.toStringGroup(null, writer, attributeAssign.getOwnerGroupId(), false);
          }
          if (!StringUtils.isBlank(attributeAssign.getOwnerMemberId())) {
            XmlExportUtils.toStringMember(null, writer, attributeAssign.getOwnerMemberId(), false);
          }
          if (!StringUtils.isBlank(attributeAssign.getOwnerMembershipId())) {
            XmlExportUtils.toStringMembership(null, writer, attributeAssign.getOwnerMembershipId(), false);
          }
          if (!StringUtils.isBlank(attributeAssign.getOwnerAttributeDefId())) {
            XmlExportUtils.toStringAttributeDef(null, writer, attributeAssign.getOwnerAttributeDefId(), false);
          }
          if (!StringUtils.isBlank(attributeAssign.getOwnerAttributeAssignId())) {
            XmlExportUtils.toStringAttributeAssign("attrOn", writer, attributeAssign.getOwnerAttributeAssignId(), false);
          }

          return null;
        } catch (IOException ioe) {
          throw new RuntimeException(ioe);
        }
      }
    });

  }

  /**
   * take a reader (e.g. dom reader) and convert to an xml export group
   * @param exportVersion
   * @param hierarchicalStreamReader
   * @return the bean
   */
  public static XmlExportAttributeAssign fromXml(@SuppressWarnings("unused") GrouperVersion exportVersion, 
      HierarchicalStreamReader hierarchicalStreamReader) {
    XStream xStream = XmlExportUtils.xstream();
    
    XmlExportAttributeAssign xmlExportAttributeAssign = (XmlExportAttributeAssign)xStream.unmarshal(hierarchicalStreamReader);
  
    return xmlExportAttributeAssign;
  }

  /**
   * 
   * @param exportVersion
   * @param xml
   * @return the object from xml
   */
  public static XmlExportAttributeAssign fromXml(
      @SuppressWarnings("unused") GrouperVersion exportVersion, String xml) {
    XStream xStream = XmlExportUtils.xstream();
    
    XmlExportAttributeAssign xmlExportAttributeAssign = (XmlExportAttributeAssign)xStream.fromXML(xml);
  
    return xmlExportAttributeAssign;
  }

  /**
   * parse the xml file for attributeAssigns
   * @param xmlImportMain
   */
  public static void processXmlFirstPass(final XmlImportMain xmlImportMain) {
    xmlImportMain.getReader().addHandler( ATTRIBUTE_ASSIGNS_XPATH, 
        new ElementHandler() {
            public void onStart(ElementPath path) {
            }
            public void onEnd(ElementPath path) {
                // process a ROW element
                Element row = path.getCurrent();

                // prune the tree
                row.detach();
            }
        }
    );

    xmlImportMain.getReader().addHandler( XML_EXPORT_ATTRIBUTE_ASSIGN_XPATH, 
        new ElementHandler() {
            public void onStart(ElementPath path) {
                // do nothing here...    
            }
            public void onEnd(ElementPath path) {
                // process a ROW element
                Element row = path.getCurrent();

                // prune the tree
                row.detach();

                xmlImportMain.incrementTotalImportFileCount();
            }
        }
    );
 
  }
}
