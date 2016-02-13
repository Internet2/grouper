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
/**
 * @author mchyzer
 * $Id: XmlExportGroup.java 6216 2010-01-10 04:52:30Z mchyzer $
 */
package edu.internet2.middleware.grouper.xml.export;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

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

import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefType;
import edu.internet2.middleware.grouper.attr.AttributeDefValueType;
import edu.internet2.middleware.grouper.hibernate.AuditControl;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionType;
import edu.internet2.middleware.grouper.hibernate.HibUtils;
import edu.internet2.middleware.grouper.hibernate.HibernateHandler;
import edu.internet2.middleware.grouper.hibernate.HibernateHandlerBean;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.misc.GrouperVersion;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.xml.importXml.XmlImportMain;


/**
 *
 */
public class XmlExportAttributeDef {


  /** idIndex */
  private Long idIndex;

  /**
   * 
   * @return id index
   */
  public Long getIdIndex() {
    return this.idIndex;
  }

  /**
   * id index
   * @param idIndex1
   */
  public void setIdIndex(Long idIndex1) {
    this.idIndex = idIndex1;
  }

  /**
   * 
   */
  private static final String XML_EXPORT_ATTRIBUTE_DEF_XPATH = "/grouperExport/attributeDefs/XmlExportAttributeDef";

  /**
   * 
   */
  private static final String ATTRIBUTE_DEFS_XPATH = "/grouperExport/attributeDefs";

  /** uuid */
  private String uuid;
  
  /** parentStem */
  private String parentStem;

  /** name */
  private String name;

  /** creatorId */
  private String creatorId;

  /** createTime */
  private String createTime;

  /** modifierTime */
  private String modifierTime;

  /** extension */
  private String extension;
  
  /** description */
  private String description;

  /** hibernateVersionNumber */
  private long hibernateVersionNumber;

  /** contextId */
  private String contextId;

  /** attributeDefPublic: T|F */
  private String attributeDefPublic;

  /** multiAssignable: T|F */
  private String multiAssignable;
  
  /**
   * multiAssignable: T|F
   * @return multiAssignable: T|F
   */
  public String getMultiAssignable() {
    return this.multiAssignable;
  }

  /**
   * multiAssignable: T|F
   * @param multiAssignable1
   */
  public void setMultiAssignable(String multiAssignable1) {
    this.multiAssignable = multiAssignable1;
  }

  /** multiValued: T|F */
  private String multiValued;
  
  /**
   * multiValued: T|F
   * @return multiValued: T|F
   */
  public String getMultiValued() {
    return this.multiValued;
  }

  /**
   * multiValued: T|F
   * @param multiValued1
   */
  public void setMultiValued(String multiValued1) {
    this.multiValued = multiValued1;
  }

  /** valueType */
  private String valueType;
  
  /**
   * valueType
   * @return valueType
   */
  public String getValueType() {
    return this.valueType;
  }

  /**
   * valueType
   * @param valueType1
   */
  public void setValueType(String valueType1) {
    this.valueType = valueType1;
  }

  /** assignToAttributeDef */
  private String assignToAttributeDef;
  
  /**
   * assignToAttributeDef
   * @return assignToAttributeDef
   */
  public String getAssignToAttributeDef() {
    return this.assignToAttributeDef;
  }

  /**
   * assignToAttributeDef
   * @param assignToAttributeDef1
   */
  public void setAssignToAttributeDef(String assignToAttributeDef1) {
    this.assignToAttributeDef = assignToAttributeDef1;
  }
  
  /** assignToAttributeDefAssn */
  private String assignToAttributeDefAssn;

  
  
  /**
   * assignToAttributeDefAssn
   * @return assignToAttributeDefAssn
   */
  public String getAssignToAttributeDefAssn() {
    return this.assignToAttributeDefAssn;
  }

  /**
   * assignToAttributeDefAssn
   * @param assignToAttributeDefAssn1
   */
  public void setAssignToAttributeDefAssn(String assignToAttributeDefAssn1) {
    this.assignToAttributeDefAssn = assignToAttributeDefAssn1;
  }

  /** assignToEffMembership */
  private String assignToEffMembership;

  /**
   * assignToEffMembershipAssn
   */
  private String assignToEffMembershipAssn;
  
  /**
   * assignToEffMembershipAssn
   * @return assignToEffMembershipAssn
   */
  public String getAssignToEffMembershipAssn() {
    return this.assignToEffMembershipAssn;
  }

  /**
   * assignToEffMembershipAssn
   * @param assignToEffMembershipAssn1
   */
  public void setAssignToEffMembershipAssn(String assignToEffMembershipAssn1) {
    this.assignToEffMembershipAssn = assignToEffMembershipAssn1;
  }

  /**
   * assignToEffMembership
   * @return assignToEffMembership
   */
  public String getAssignToEffMembership() {
    return this.assignToEffMembership;
  }

  /**
   * assignToEffMembership
   * @param assignToEffMembership1
   */
  public void setAssignToEffMembership(String assignToEffMembership1) {
    this.assignToEffMembership = assignToEffMembership1;
  }

  /**
   * assignToGroup
   */
  private String assignToGroup;
  
  /**
   * assignToGroup
   * @return assignToGroup
   */
  public String getAssignToGroup() {
    return this.assignToGroup;
  }

  /**
   * assignToGroup
   * @param assignToGroup1
   */
  public void setAssignToGroup(String assignToGroup1) {
    this.assignToGroup = assignToGroup1;
  }

  /**
   * assignToGroup
   */
  private String assignToGroupAssn;

  /**
   * assignToGroupAssn
   * @return assignToGroupAssn
   */
  public String getAssignToGroupAssn() {
    return this.assignToGroupAssn;
  }

  /**
   * assignToGroupAssn
   * @param assignToGroupAssn1
   */
  public void setAssignToGroupAssn(String assignToGroupAssn1) {
    this.assignToGroupAssn = assignToGroupAssn1;
  }

  /** assignToImmMembership */
  private String assignToImmMembership;
  
  /**
   * assignToImmMembership
   * @return assignToImmMembership
   */
  public String getAssignToImmMembership() {
    return this.assignToImmMembership;
  }

  /**
   * assignToImmMembership
   * @param assignToImmMembership1
   */
  public void setAssignToImmMembership(String assignToImmMembership1) {
    this.assignToImmMembership = assignToImmMembership1;
  }

  /** assignToImmMembershipAssn */
  private String assignToImmMembershipAssn;
  
  
  
  /**
   * assignToImmMembershipAssn
   * @return assignToImmMembershipAssn
   */
  public String getAssignToImmMembershipAssn() {
    return this.assignToImmMembershipAssn;
  }

  /**
   * assignToImmMembershipAssn
   * @param assignToImmMembershipAssn1
   */
  public void setAssignToImmMembershipAssn(String assignToImmMembershipAssn1) {
    this.assignToImmMembershipAssn = assignToImmMembershipAssn1;
  }

  /** assignToMember */
  private String assignToMember;
  

  
  /**
   * assignToMember
   * @return assignToMember
   */
  public String getAssignToMember() {
    return this.assignToMember;
  }

  /**
   * assignToMember
   * @param assignToMember1
   */
  public void setAssignToMember(String assignToMember1) {
    this.assignToMember = assignToMember1;
  }

  /**
   * assignToMemberAssn
   */
  private String assignToMemberAssn;
  
  /**
   * assignToMemberAssn
   * @return assignToMemberAssn
   */
  public String getAssignToMemberAssn() {
    return this.assignToMemberAssn;
  }

  /**
   * assignToMemberAssn
   * @param assignToMemberAssn1
   */
  public void setAssignToMemberAssn(String assignToMemberAssn1) {
    this.assignToMemberAssn = assignToMemberAssn1;
  }

  /** assignToStem */
  private String assignToStem;

  
  
  /**
   * assignToStem
   * @return assignToStem
   */
  public String getAssignToStem() {
    return this.assignToStem;
  }

  /**
   * assignToStem
   * @param assignToStem1
   */
  public void setAssignToStem(String assignToStem1) {
    this.assignToStem = assignToStem1;
  }

  /**
   * assignToStemAssn
   */
  private String assignToStemAssn;
  
  /**
   * assignToStemAssn
   * @return assignToStemAssn
   */
  public String getAssignToStemAssn() {
    return this.assignToStemAssn;
  }

  /**
   * assignToStemAssn
   * @param assignToStemAssn1
   */
  public void setAssignToStemAssn(String assignToStemAssn1) {
    this.assignToStemAssn = assignToStemAssn1;
  }

  /**
   * attributeDefPublic: T|F
   * @return attributeDefPublic: T|F
   */
  public String getAttributeDefPublic() {
    return this.attributeDefPublic;
  }

  /**
   * attributeDefPublic: T|F
   * @param attributeDefPublic1
   */
  public void setAttributeDefPublic(String attributeDefPublic1) {
    this.attributeDefPublic = attributeDefPublic1;
  }

  /**
   * attributeDefType
   * @return attributeDefType
   */
  public String getAttributeDefType() {
    return this.attributeDefType;
  }

  /**
   * attributeDefType
   * @param attributeDefType1
   */
  public void setAttributeDefType(String attributeDefType1) {
    this.attributeDefType = attributeDefType1;
  }

  /** attributeDefType */
  private String attributeDefType;

  /**
   * logger 
   */
  private static final Log LOG = GrouperUtil.getLog(XmlExportAttributeDef.class);
  
  
  
  /**
   * 
   */
  public XmlExportAttributeDef() {
    
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
   * parentStem
   * @return parentStem
   */
  public String getParentStem() {
    return this.parentStem;
  }

  /**
   * parentStem
   * @param parentStem1
   */
  public void setParentStem(String parentStem1) {
    this.parentStem = parentStem1;
  }

  /**
   * name
   * @return name
   */
  public String getName() {
    return this.name;
  }

  /**
   * name
   * @param name
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * creatorId
   * @return creatorId
   */
  public String getCreatorId() {
    return this.creatorId;
  }

  /**
   * creatorId
   * @param creatorId1
   */
  public void setCreatorId(String creatorId1) {
    this.creatorId = creatorId1;
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
   * extension
   * @return extension
   */
  public String getExtension() {
    return this.extension;
  }

  /**
   * extension
   * @param extension1
   */
  public void setExtension(String extension1) {
    this.extension = extension1;
  }

  /**
   * description
   * @return description
   */
  public String getDescription() {
    return this.description;
  }

  /**
   * description
   * @param description1
   */
  public void setDescription(String description1) {
    this.description = description1;
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
   * convert to attributeDef
   * @return the attributeDef
   */
  public AttributeDef toAttributeDef() {
    AttributeDef attributeDef = new AttributeDef();
    
    attributeDef.setAssignToAttributeDef(GrouperUtil.booleanValue(this.assignToAttributeDef, false));
    attributeDef.setAssignToAttributeDefAssn(GrouperUtil.booleanValue(this.assignToAttributeDefAssn, false));
    attributeDef.setAssignToEffMembership(GrouperUtil.booleanValue(this.assignToEffMembership, false));
    attributeDef.setAssignToEffMembershipAssn(GrouperUtil.booleanValue(this.assignToEffMembershipAssn, false));
    attributeDef.setAssignToGroup(GrouperUtil.booleanValue(this.assignToGroup, false));
    attributeDef.setAssignToGroupAssn(GrouperUtil.booleanValue(this.assignToGroupAssn, false));
    attributeDef.setAssignToImmMembership(GrouperUtil.booleanValue(this.assignToImmMembership, false));
    attributeDef.setAssignToImmMembershipAssn(GrouperUtil.booleanValue(this.assignToImmMembershipAssn, false));
    attributeDef.setAssignToMember(GrouperUtil.booleanValue(this.assignToMember, false));
    attributeDef.setAssignToMemberAssn(GrouperUtil.booleanValue(this.assignToMemberAssn, false));
    attributeDef.setAssignToStem(GrouperUtil.booleanValue(this.assignToStem, false));
    attributeDef.setAssignToStemAssn(GrouperUtil.booleanValue(this.assignToStemAssn, false));
    attributeDef.setAttributeDefPublic(GrouperUtil.booleanValue(this.attributeDefPublic, false));
    attributeDef.setAttributeDefTypeDb(this.attributeDefType);
    attributeDef.setContextId(this.contextId);
    attributeDef.setCreatedOnDb(GrouperUtil.dateLongValue(this.createTime));
    attributeDef.setCreatorId(this.creatorId);
    attributeDef.setDescription(this.description);
    attributeDef.setExtensionDb(this.extension);
    attributeDef.setHibernateVersionNumber(this.hibernateVersionNumber);
    attributeDef.setIdIndex(this.idIndex);
    attributeDef.setLastUpdatedDb(GrouperUtil.dateLongValue(this.modifierTime));
    attributeDef.setMultiAssignable(GrouperUtil.booleanValue(this.multiAssignable, false));
    attributeDef.setMultiValued(GrouperUtil.booleanValue(this.multiValued, false));
    attributeDef.setNameDb(this.name);
    attributeDef.setStemId(this.parentStem);
    attributeDef.setId(this.uuid);
    attributeDef.setValueTypeDb(this.valueType);
    
    return attributeDef;
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
   * @param exportVersion 
   * @param writer
   * @throws IOException 
   */
  public void toGsh(
      @SuppressWarnings("unused") GrouperVersion exportVersion, Writer writer) throws IOException {
    //new AttributeDefSave(grouperSession).assignName(this.name).assignCreateParentStemsIfNotExist(true)
    //.assignDescription(this.description).assignDisplayName(this.displayName).save();

    writer.write("AttributeDefSave attributeDefSave = new AttributeDefSave(grouperSession).assignName(\""
        + GrouperUtil.escapeDoubleQuotes(this.name) 
        + "\").assignCreateParentStemsIfNotExist(true)");
    if (!StringUtils.isBlank(this.description)) {
      writer.write(".assignDescription(\""
        + GrouperUtil.escapeDoubleQuotes(this.description)
        + "\")");
    }
      
    if (GrouperUtil.booleanValue(this.assignToAttributeDef, false)) {
      writer.write(".assignToAttributeDef(true)");
    }
    if (GrouperUtil.booleanValue(this.assignToAttributeDefAssn, false)) {
      writer.write(".assignToAttributeDefAssn(true)");
    }
    if (GrouperUtil.booleanValue(this.assignToEffMembership, false)) {
      writer.write(".assignToEffMembership(true)");
    }
    if (GrouperUtil.booleanValue(this.assignToEffMembershipAssn, false)) {
      writer.write(".assignToEffMembershipAssn(true)");
    }
    if (GrouperUtil.booleanValue(this.assignToGroup, false)) {
      writer.write(".assignToGroup(true)");
    }
    if (GrouperUtil.booleanValue(this.assignToGroupAssn, false)) {
      writer.write(".assignToGroupAssn(true)");
    }
    if (GrouperUtil.booleanValue(this.assignToImmMembership, false)) {
      writer.write(".assignToImmMembership(true)");
    }
    if (GrouperUtil.booleanValue(this.assignToImmMembershipAssn, false)) {
      writer.write(".assignToImmMembershipAssn(true)");
    }
    if (GrouperUtil.booleanValue(this.assignToMember, false)) {
      writer.write(".assignToMember(true)");
    }
    if (GrouperUtil.booleanValue(this.assignToMemberAssn, false)) {
      writer.write(".assignToMemberAssn(true)");
    }
    if (GrouperUtil.booleanValue(this.assignToStem, false)) {
      writer.write(".assignToStem(true)");
    }
    if (GrouperUtil.booleanValue(this.assignToStemAssn, false)) {
      writer.write(".assignToStemAssn(true)");
    }

    if (GrouperUtil.booleanValue(this.assignToImmMembership, false)) {
      writer.write(".assignToImmMembership(true)");
    }

    if (GrouperUtil.booleanValue(this.attributeDefPublic, false)) {
      writer.write(".assignAttributeDefPublic(true)");
    }
    
    
    writer.write(".assignAttributeDefType(AttributeDefType." + AttributeDefType.valueOfIgnoreCase(this.attributeDefType, true).name() + ")");
    
    writer.write(".assignMultiAssignable(" + GrouperUtil.booleanValue(this.multiAssignable, false) + ")");
    writer.write(".assignMultiValued(" + GrouperUtil.booleanValue(this.multiValued, false) + ")");
    writer.write(".assignValueType(AttributeDefValueType." + AttributeDefValueType.valueOfIgnoreCase(this.valueType, true).name() + ");\n");
    
    writer.write("AttributeDef attributeDef = attributeDefSave.save();\n");
    writer.write("gshTotalObjectCount++;\nif (attributeDefSave.getSaveResultType() != SaveResultType.NO_CHANGE) {"
        + "System.out.println(\"Made change for attributeDef: \" + attributeDef.getName()); gshTotalChangeCount++;}\n");
  }

  /**
   * 
   * @param writer
   * @param xmlExportMain 
   */
  public static void exportAttributeDefsGsh(final Writer writer, final XmlExportMain xmlExportMain) {
    //get the members
    HibernateSession.callbackHibernateSession(GrouperTransactionType.READONLY_OR_USE_EXISTING, AuditControl.WILL_NOT_AUDIT, new HibernateHandler() {
      
      public Object callback(HibernateHandlerBean hibernateHandlerBean)
          throws GrouperDAOException {
  
        Session session = hibernateHandlerBean.getHibernateSession().getSession();
  
        //select all attributeDefs in order
        Query query = session.createQuery(
            "select distinct theAttributeDef " + exportFromOnQuery(xmlExportMain, true));
  
        GrouperVersion grouperVersion = new GrouperVersion(GrouperVersion.GROUPER_VERSION);
        try {
  
          //this is an efficient low-memory way to iterate through a resultset
          ScrollableResults results = null;
          try {
            results = query.scroll();
            while(results.next()) {
              Object object = results.get(0);
              AttributeDef attributeDef = (AttributeDef)object;
              XmlExportAttributeDef xmlExportAttributeDef = attributeDef.xmlToExportAttributeDef(grouperVersion);
              xmlExportAttributeDef.toGsh(grouperVersion, writer);
              xmlExportMain.incrementRecordCount();
            }
          } finally {
            HibUtils.closeQuietly(results);
          }
          
        } catch (IOException ioe) {
          throw new RuntimeException("Problem with streaming stems", ioe);
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
    xmlImportMain.getReader().addHandler( ATTRIBUTE_DEFS_XPATH, 
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
  
    xmlImportMain.getReader().addHandler( XML_EXPORT_ATTRIBUTE_DEF_XPATH, 
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
  
                XmlExportAttributeDef xmlExportAttributeDefFromFile = (XmlExportAttributeDef)xmlImportMain.getXstream().unmarshal(new Dom4JReader(row));
                
                AttributeDef attributeDef = xmlExportAttributeDefFromFile.toAttributeDef();
                
                XmlExportUtils.syncImportable(attributeDef, xmlImportMain);
                
                xmlImportMain.incrementCurrentCount();
              } catch (RuntimeException re) {
                LOG.error("Problem importing attributeDef: " + XmlExportUtils.toString(row), re);
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
    long result = HibernateSession.byHqlStatic().createQuery("select count(theAttributeDef) " 
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
      queryBuilder.append(" from AttributeDef as theAttributeDef ");
    } else {
      queryBuilder.append(
          " from AttributeDef as theAttributeDef where ");
      xmlExportMain.appendHqlStemLikeOrObjectEquals(queryBuilder, "theAttributeDef", "nameDb", false);
      queryBuilder.append(" ");
    }
    if (includeOrderBy) {
      queryBuilder.append(" order by theAttributeDef.nameDb ");
    }
    return queryBuilder.toString();
  }
        
  /**
   * 
   * @param writer
   * @param xmlExportMain 
   */
  public static void exportAttributeDefs(final Writer writer, final XmlExportMain xmlExportMain) {
    //get the members
    HibernateSession.callbackHibernateSession(GrouperTransactionType.READONLY_OR_USE_EXISTING, AuditControl.WILL_NOT_AUDIT, new HibernateHandler() {
      
      public Object callback(HibernateHandlerBean hibernateHandlerBean)
          throws GrouperDAOException {
  
        Session session = hibernateHandlerBean.getHibernateSession().getSession();
  
        //select all members in order
        Query query = session.createQuery(
            "select distinct theAttributeDef " + exportFromOnQuery(xmlExportMain, true));
  
        GrouperVersion grouperVersion = new GrouperVersion(GrouperVersion.GROUPER_VERSION);
        try {
          writer.write("  <attributeDefs>\n");
  
          //this is an efficient low-memory way to iterate through a resultset
          ScrollableResults results = null;
          try {
            results = query.scroll();
            while(results.next()) {
              Object object = results.get(0);
              AttributeDef attributeDef = (AttributeDef)object;
              XmlExportAttributeDef xmlExportAttributeDef = attributeDef.xmlToExportAttributeDef(grouperVersion);
              writer.write("    ");
              xmlExportAttributeDef.toXml(grouperVersion, writer);
              writer.write("\n");
              xmlExportMain.incrementRecordCount();
            }
          } finally {
            HibUtils.closeQuietly(results);
          }
          
          //end the attributeDefs element 
          writer.write("  </attributeDefs>\n");
        } catch (IOException ioe) {
          throw new RuntimeException("Problem with streaming attributeDefs", ioe);
        }
        return null;
      }
    });
  }

  /**
   * take a reader (e.g. dom reader) and convert to an xml export group
   * @param exportVersion
   * @param hierarchicalStreamReader
   * @return the bean
   */
  public static XmlExportAttributeDef fromXml(@SuppressWarnings("unused") GrouperVersion exportVersion, 
      HierarchicalStreamReader hierarchicalStreamReader) {
    XStream xStream = XmlExportUtils.xstream();
    
    XmlExportAttributeDef xmlExportAttributeDef = (XmlExportAttributeDef)xStream.unmarshal(hierarchicalStreamReader);
  
    return xmlExportAttributeDef;
  }

  /**
   * 
   * @param exportVersion
   * @param xml
   * @return the object from xml
   */
  public static XmlExportAttributeDef fromXml(
      @SuppressWarnings("unused") GrouperVersion exportVersion, String xml) {
    XStream xStream = XmlExportUtils.xstream();
    
    XmlExportAttributeDef xmlExportAttributeDef = (XmlExportAttributeDef)xStream.fromXML(xml);
  
    return xmlExportAttributeDef;
  }

  /**
   * parse the xml file for attributeDefs
   * @param xmlImportMain
   */
  public static void processXmlFirstPass(final XmlImportMain xmlImportMain) {
    xmlImportMain.getReader().addHandler( ATTRIBUTE_DEFS_XPATH, 
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

    xmlImportMain.getReader().addHandler( XML_EXPORT_ATTRIBUTE_DEF_XPATH, 
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
