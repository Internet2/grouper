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

import edu.internet2.middleware.grouper.GroupType;
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
public class XmlExportGroupType {

  /**
   * 
   */
  private static final String XML_EXPORT_GROUP_TYPE_XPATH = "/grouperExport/groupTypes/XmlExportGroupType";

  /**
   * 
   */
  private static final String GROUP_TYPES_XPATH = "/grouperExport/groupTypes";

  /** assignable, T|F */
  private String assignable;
  
  /** internal, T|F */
  private String internal;
  
  /**
   * assignable, T|F
   * @return assignable
   */
  public String getAssignable() {
    return this.assignable;
  }

  /**
   * assignable, T|F
   * @param assignable1
   */
  public void setAssignable(String assignable1) {
    this.assignable = assignable1;
  }

  /**
   * internal, T|F
   * @return internal
   */
  public String getInternal() {
    return this.internal;
  }

  /**
   * internal, T|F
   * @param internal1
   */
  public void setInternal(String internal1) {
    this.internal = internal1;
  }

  /** uuid */
  private String uuid;
  
  /** name */
  private String name;

  /** creatorId */
  private String creatorId;

  /** createTime */
  private String createTime;

  /** hibernateVersionNumber */
  private long hibernateVersionNumber;

  /** contextId */
  private String contextId;

  /**
   * logger 
   */
  private static final Log LOG = GrouperUtil.getLog(XmlExportGroupType.class);

  /**
   * 
   */
  public XmlExportGroupType() {
    
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
   * convert to group
   * @return the group
   */
  public GroupType toGroupType() {
    GroupType groupType = new GroupType();
    
    groupType.setIsAssignable(GrouperUtil.booleanValue(this.assignable, false));
    groupType.setContextId(this.contextId);
    groupType.setCreateTime(GrouperUtil.defaultIfNull(GrouperUtil.dateLongValue(this.createTime), 0L));
    groupType.setCreatorUuid(this.creatorId);
    groupType.setHibernateVersionNumber(this.hibernateVersionNumber);
    groupType.setIsInternal(GrouperUtil.booleanValue(this.internal, false));
    groupType.setName(this.name);
    groupType.setUuid(this.uuid);
    
    return groupType;
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
   * parse the xml file for groups
   * @param xmlImportMain
   */
  public static void processXmlSecondPass(final XmlImportMain xmlImportMain) {
    xmlImportMain.getReader().addHandler( GROUP_TYPES_XPATH, 
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
  
    xmlImportMain.getReader().addHandler( XML_EXPORT_GROUP_TYPE_XPATH, 
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
  
                XmlExportGroupType xmlExportGroupTypeFromFile = (XmlExportGroupType)xmlImportMain.getXstream().unmarshal(new Dom4JReader(row));
                
                GroupType groupType = xmlExportGroupTypeFromFile.toGroupType();
                
                XmlExportUtils.syncImportable(groupType, xmlImportMain);
                
                xmlImportMain.incrementCurrentCount();
              } catch (RuntimeException re) {
                LOG.error("Problem importing groupType: " + XmlExportUtils.toString(row), re);
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
    long result = HibernateSession.byHqlStatic().createQuery("select count(theGroupType) " 
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
      queryBuilder.append(" from GroupType as theGroupType ");
    } else {
      queryBuilder.append(
          " from GroupType as theGroupType where exists ( select theGroupTypeTuple from GroupTypeTuple as theGroupTypeTuple, Group as theGroup " +
          " where theGroupTypeTuple.groupUuid = theGroup.uuid and theGroupTypeTuple.typeUuid = theGroupType.uuid and ( ");
      xmlExportMain.appendHqlStemLikeOrObjectEquals(queryBuilder, "theGroup", "nameDb", false);
      queryBuilder.append(" ) ) ");
    }
    if (includeOrderBy) {
      queryBuilder.append(" order by theGroupType.name ");
    }
    return queryBuilder.toString();
  }


  /**
   * 
   * @param writer
   * @param xmlExportMain 
   */
  public static void exportGroupTypes(final Writer writer, final  XmlExportMain xmlExportMain) {
    //get the members
    HibernateSession.callbackHibernateSession(GrouperTransactionType.READONLY_OR_USE_EXISTING, AuditControl.WILL_NOT_AUDIT, new HibernateHandler() {
      
      public Object callback(HibernateHandlerBean hibernateHandlerBean)
          throws GrouperDAOException {
  
        Session session = hibernateHandlerBean.getHibernateSession().getSession();
  
        //select all members in order
        Query query = session.createQuery(
            "select distinct theGroupType " + exportFromOnQuery(xmlExportMain, true));
  
        GrouperVersion grouperVersion = new GrouperVersion(GrouperVersion.GROUPER_VERSION);
        try {
          writer.write("  <groupTypes>\n");
  
          //this is an efficient low-memory way to iterate through a resultset
          ScrollableResults results = null;
          try {
            results = query.scroll();
            while(results.next()) {
              Object object = results.get(0);
              GroupType groupType = (GroupType)object;
              XmlExportGroupType xmlExportGroupType = groupType.xmlToExportGroupType(grouperVersion);
              writer.write("    ");
              xmlExportGroupType.toXml(grouperVersion, writer);
              writer.write("\n");
              xmlExportMain.incrementRecordCount();
            }
          } finally {
            HibUtils.closeQuietly(results);
          }
          
          //end the members element 
          writer.write("  </groupTypes>\n");
        } catch (IOException ioe) {
          throw new RuntimeException("Problem with streaming group types", ioe);
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
  public static XmlExportGroupType fromXml(@SuppressWarnings("unused") GrouperVersion exportVersion, 
      HierarchicalStreamReader hierarchicalStreamReader) {
    XStream xStream = XmlExportUtils.xstream();
    
    XmlExportGroupType xmlExportGroupType = (XmlExportGroupType)xStream.unmarshal(hierarchicalStreamReader);
  
    return xmlExportGroupType;
  }

  /**
   * 
   * @param exportVersion
   * @param xml
   * @return the object from xml
   */
  public static XmlExportGroupType fromXml(
      @SuppressWarnings("unused") GrouperVersion exportVersion, String xml) {
    XStream xStream = XmlExportUtils.xstream();
    
    XmlExportGroupType xmlExportGroupType = (XmlExportGroupType)xStream.fromXML(xml);
  
    return xmlExportGroupType;
  }

  /**
   * parse the xml file for grouptypes
   * @param xmlImportMain
   */
  public static void processXmlFirstPass(final XmlImportMain xmlImportMain) {
    xmlImportMain.getReader().addHandler( GROUP_TYPES_XPATH, 
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

    xmlImportMain.getReader().addHandler( XML_EXPORT_GROUP_TYPE_XPATH, 
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
