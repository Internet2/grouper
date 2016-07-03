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

import edu.internet2.middleware.grouper.hibernate.AuditControl;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionType;
import edu.internet2.middleware.grouper.hibernate.HibUtils;
import edu.internet2.middleware.grouper.hibernate.HibernateHandler;
import edu.internet2.middleware.grouper.hibernate.HibernateHandlerBean;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.misc.GrouperVersion;
import edu.internet2.middleware.grouper.permissions.role.RoleSet;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.xml.importXml.XmlImportMain;


/**
 *
 */
public class XmlExportRoleSet {

  /**
   * 
   */
  private static final String XML_EXPORT_ROLE_SET_XPATH = "/grouperExport/roleSets/XmlExportRoleSet";

  /**
   * 
   */
  private static final String ROLE_SETS_XPATH = "/grouperExport/roleSets";

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

  /** depth */
  private long depth;
  
  /**
   * depth
   * @return depth
   */
  public long getDepth() {
    return this.depth;
  }

  /**
   * depth
   * @param depth1
   */
  public void setDepth(long depth1) {
    this.depth = depth1;
  }

  /** ifHasRoleId */
  private String ifHasRoleId;
  
  /**
   * ifHasRoleId
   * @return ifHasRoleId
   */
  public String getIfHasRoleId() {
    return this.ifHasRoleId;
  }

  /**
   * ifHasRoleId
   * @param ifHasRoleId1
   */
  public void setIfHasRoleId(String ifHasRoleId1) {
    this.ifHasRoleId = ifHasRoleId1;
  }

  /**
   * thenHasRoleId
   */
  private String thenHasRoleId;
  
  /**
   * thenHasRoleId
   * @return thenHasRoleId
   */
  public String getThenHasRoleId() {
    return this.thenHasRoleId;
  }

  /**
   * thenHasRoleId
   * @param thenHasRoleId1
   */
  public void setThenHasRoleId(String thenHasRoleId1) {
    this.thenHasRoleId = thenHasRoleId1;
  }

  /**
   * type
   */
  private String type;

  /**
   * logger 
   */
  private static final Log LOG = GrouperUtil.getLog(XmlExportRoleSet.class);

  /**
   * type
   * @return type
   */
  public String getType() {
    return this.type;
  }

  /**
   * type
   * @param type1
   */
  public void setType(String type1) {
    this.type = type1;
  }

  /**
   * 
   */
  public XmlExportRoleSet() {
    
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
   * convert to roleSet
   * @return the roleSet
   */
  public RoleSet toRoleSet() {
    RoleSet roleSet = new RoleSet();
    
    roleSet.setContextId(this.contextId);
    roleSet.setCreatedOnDb(GrouperUtil.dateLongValue(this.createTime));
    roleSet.setDepth((int)this.depth);
    roleSet.setHibernateVersionNumber(this.hibernateVersionNumber);
    roleSet.setIfHasRoleId(this.ifHasRoleId);
    roleSet.setLastUpdatedDb(GrouperUtil.dateLongValue(this.modifierTime));
    roleSet.setThenHasRoleId(this.thenHasRoleId);
    roleSet.setTypeDb(this.type);
    roleSet.setId(this.uuid);
    
    return roleSet;
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
   * @param ifHasRoleName 
   * @param thenHasRoleName 
   * @throws IOException 
   */
  public void toGsh(
      @SuppressWarnings("unused") GrouperVersion exportVersion, Writer writer, 
      String ifHasRoleName, String thenHasRoleName) throws IOException {
    writer.write("ifHasRole = GroupFinder.findByName(grouperSession, \""
        + GrouperUtil.escapeDoubleQuotesSlashesAndNewlinesForString(ifHasRoleName) + "\", false);\n");
    writer.write("thenHasRole = GroupFinder.findByName(grouperSession, \""
        + GrouperUtil.escapeDoubleQuotesSlashesAndNewlinesForString(thenHasRoleName) + "\", false);\n");

    writer.write("if (ifHasRole != null) { ");

    writer.write("if (thenHasRole != null) { ");

    //addCompositeMember(CompositeType type, Group left, Group right)
    writer.write("boolean changed = ifHasRole.getRoleInheritanceDelegate().addRoleToInheritFromThis(thenHasRole); if (changed) { gshTotalChangeCount++; System.out.println(\"Made change for role inheritance: \" + ifHasRole.getName() + \" then has role \" + thenHasRole.getName());  }");

    writer.write(" } else { System.out.println(\"ERROR: cant find thenHasRole: \" + thenHasRole.getName()); gshTotalErrorCount++; } ");

    writer.write(" } else { System.out.println(\"ERROR: cant find ifHasRole: \" + ifHasRole.getName()); gshTotalErrorCount++; }\n");

  }

  /**
   * parse the xml file for groups
   * @param xmlImportMain
   */
  public static void processXmlSecondPass(final XmlImportMain xmlImportMain) {
    xmlImportMain.getReader().addHandler( ROLE_SETS_XPATH, 
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
  
    xmlImportMain.getReader().addHandler( XML_EXPORT_ROLE_SET_XPATH, 
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
  
                XmlExportRoleSet xmlExportRoleSetFromFile = (XmlExportRoleSet)xmlImportMain.getXstream().unmarshal(new Dom4JReader(row));
                
                RoleSet roleSet = xmlExportRoleSetFromFile.toRoleSet();
                
                XmlExportUtils.syncImportable(roleSet, xmlImportMain);
                
                xmlImportMain.incrementCurrentCount();
              } catch (RuntimeException re) {
                LOG.error("Problem importing roleSet: " + XmlExportUtils.toString(row), re);
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
    long result = HibernateSession.byHqlStatic().createQuery("select count(theRoleSet) " + exportFromOnQuery(xmlExportMain, false)).uniqueResult(Long.class);
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
      queryBuilder.append(" from RoleSet as theRoleSet where theRoleSet.typeDb = 'immediate' ");
    } else {
      queryBuilder.append(
          " from RoleSet as theRoleSet where theRoleSet.typeDb = 'immediate' " +
          " and exists ( select theGroup from Group as theGroup " +
          " where theRoleSet.ifHasRoleId = theGroup.uuid and ( ");
      xmlExportMain.appendHqlStemLikeOrObjectEquals(queryBuilder, "theGroup", "nameDb", false);
      queryBuilder.append(" ) ) " +
          " and exists ( select theGroup from Group as theGroup " +
          " where theRoleSet.thenHasRoleId = theGroup.uuid and ( ");
      xmlExportMain.appendHqlStemLikeOrObjectEquals(queryBuilder, "theGroup", "nameDb", false);
      queryBuilder.append(" ) ) ");
    }
    if (includeOrderBy) {
      queryBuilder.append(" order by theRoleSet.id ");
    }
    return queryBuilder.toString();
  }

  /**
   * 
   * @param writer
   * @param xmlExportMain 
   */
  public static void exportRoleSets(final Writer writer, final XmlExportMain xmlExportMain) {
    //get the members
    HibernateSession.callbackHibernateSession(GrouperTransactionType.READONLY_OR_USE_EXISTING, AuditControl.WILL_NOT_AUDIT, new HibernateHandler() {
      
      public Object callback(HibernateHandlerBean hibernateHandlerBean)
          throws GrouperDAOException {
  
        Session session = hibernateHandlerBean.getHibernateSession().getSession();
  
        //select all role sets (immediate is depth = 1)
        Query query = session.createQuery(
            "select distinct theRoleSet " + exportFromOnQuery(xmlExportMain, true));
  
        GrouperVersion grouperVersion = new GrouperVersion(GrouperVersion.GROUPER_VERSION);
        try {
          writer.write("  <roleSets>\n");
  
          //this is an efficient low-memory way to iterate through a resultset
          ScrollableResults results = null;
          try {
            results = query.scroll();
            while(results.next()) {
              Object object = results.get(0);
              final RoleSet roleSet = (RoleSet)object;
              
              //comments to dereference the foreign keys
              if (xmlExportMain.isIncludeComments()) {
                HibernateSession.callbackHibernateSession(GrouperTransactionType.READONLY_NEW, AuditControl.WILL_NOT_AUDIT, new HibernateHandler() {
                  
                  public Object callback(HibernateHandlerBean hibernateHandlerBean)
                      throws GrouperDAOException {
                    try {
                      writer.write("\n    <!-- ");
                      XmlExportUtils.toStringRoleSet(writer, roleSet, false);
                      writer.write(" -->\n");
                      return null;
                    } catch (IOException ioe) {
                      throw new RuntimeException(ioe);
                    }
                  }
                });
              }
              
              XmlExportRoleSet xmlExportRoleSet = roleSet.xmlToExportRoleSet(grouperVersion);
              writer.write("    ");
              xmlExportRoleSet.toXml(grouperVersion, writer);
              writer.write("\n");
              xmlExportMain.incrementRecordCount();
            }
          } finally {
            HibUtils.closeQuietly(results);
          }
          
          
          if (xmlExportMain.isIncludeComments()) {
            writer.write("\n");
          }
          
          //end the members element 
          writer.write("  </roleSets>\n");
        } catch (IOException ioe) {
          throw new RuntimeException("Problem with streaming roleSets", ioe);
        }
        return null;
      }
    });
  }

  /**
   * 
   * @param writer
   * @param xmlExportMain
   */
  public static void exportRoleSetsGsh(final Writer writer, final XmlExportMain xmlExportMain) {
    //get the members
    HibernateSession.callbackHibernateSession(GrouperTransactionType.READONLY_OR_USE_EXISTING, AuditControl.WILL_NOT_AUDIT, new HibernateHandler() {
      
      public Object callback(HibernateHandlerBean hibernateHandlerBean)
          throws GrouperDAOException {

        Session session = hibernateHandlerBean.getHibernateSession().getSession();

        //select all members in order
        Query query = session.createQuery(
            "select distinct "
            + " ( select theGroup.nameDb from Group theGroup where theGroup.uuid = theRoleSet.ifHasRoleId ), "
            + " ( select theGroup.nameDb from Group theGroup where theGroup.uuid = theRoleSet.thenHasRoleId ), "
            + " theRoleSet "
            + exportFromOnQuery(xmlExportMain, false));

        final GrouperVersion grouperVersion = new GrouperVersion(GrouperVersion.GROUPER_VERSION);

        //this is an efficient low-memory way to iterate through a resultset
        ScrollableResults results = null;
        try {
          results = query.scroll();
          while(results.next()) {
            final String ifHasRoleName = (String)results.get(0);
            final String thenHasRoleName = (String)results.get(1);
            final RoleSet roleSet = (RoleSet)results.get(2);
            final XmlExportRoleSet xmlExportRoleSet = roleSet.xmlToExportRoleSet(grouperVersion);

            //writer.write("" + subjectId + ", " + sourceId + ", " + listName + ", " + groupName 
            //    + ", " + stemName + ", " + nameOfAttributeDef 
            //    + ", " + enabledTime + ", " + disabledTime  + "\n");
            
            HibernateSession.callbackHibernateSession(GrouperTransactionType.READONLY_NEW, AuditControl.WILL_NOT_AUDIT, new HibernateHandler() {
              
              public Object callback(HibernateHandlerBean hibernateHandlerBean)
                  throws GrouperDAOException {
                try {
                  xmlExportRoleSet.toGsh(grouperVersion, writer, ifHasRoleName, thenHasRoleName);
                } catch (IOException ioe) {
                  throw new RuntimeException("Problem exporting roleSet to gsh: " + ifHasRoleName + ", " + thenHasRoleName, ioe);
                }
                return null;
              }
            });
            xmlExportMain.incrementRecordCount();
          }
        } finally {
          HibUtils.closeQuietly(results);
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
  public static XmlExportRoleSet fromXml(@SuppressWarnings("unused") GrouperVersion exportVersion, 
      HierarchicalStreamReader hierarchicalStreamReader) {
    XStream xStream = XmlExportUtils.xstream();
    
    XmlExportRoleSet xmlExportRoleSet = (XmlExportRoleSet)xStream.unmarshal(hierarchicalStreamReader);
  
    return xmlExportRoleSet;
  }

  /**
   * 
   * @param exportVersion
   * @param xml
   * @return the object from xml
   */
  public static XmlExportRoleSet fromXml(
      @SuppressWarnings("unused") GrouperVersion exportVersion, String xml) {
    XStream xStream = XmlExportUtils.xstream();
    
    XmlExportRoleSet xmlExportRoleSet = (XmlExportRoleSet)xStream.fromXML(xml);
  
    return xmlExportRoleSet;
  }

  /**
   * parse the xml file for roleSets
   * @param xmlImportMain
   */
  public static void processXmlFirstPass(final XmlImportMain xmlImportMain) {
    xmlImportMain.getReader().addHandler( ROLE_SETS_XPATH, 
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

    xmlImportMain.getReader().addHandler( XML_EXPORT_ROLE_SET_XPATH, 
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
