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
import java.util.LinkedHashSet;
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

import edu.internet2.middleware.grouper.attr.assign.AttributeAssignAction;
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
public class XmlExportAttributeAssignAction {

  /**
   * 
   */
  private static final String XML_EXPORT_ATTRIBUTE_ASSIGN_ACTION_XPATH = "/grouperExport/attributeAssignActions/XmlExportAttributeAssignAction";

  /**
   * 
   */
  private static final String ATTRIBUTE_ASSIGN_ACTIONS_XPATH = "/grouperExport/attributeAssignActions";

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

  /** attributeDefId */
  private String attributeDefId;

  /** name */
  private String name;

  /**
   * logger 
   */
  private static final Log LOG = GrouperUtil.getLog(XmlExportAttributeAssignAction.class);
  
  /**
   * name
   * @return name
   */
  public String getName() {
    return this.name;
  }

  /**
   * name
   * @param name1
   */
  public void setName(String name1) {
    this.name = name1;
  }

  /**
   * attributeDefId
   * @return attributeDefId
   */
  public String getAttributeDefId() {
    return this.attributeDefId;
  }

  /**
   * attributeDefId
   * @param attributeDefId1
   */
  public void setAttributeDefId(String attributeDefId1) {
    this.attributeDefId = attributeDefId1;
  }

  /**
   * 
   */
  public XmlExportAttributeAssignAction() {
    
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
   * convert to AttributeAssignAction
   * @return the AttributeAssignAction
   */
  public AttributeAssignAction toAttributeAssignAction() {
    AttributeAssignAction attributeAssignAction = new AttributeAssignAction();
    
    attributeAssignAction.setAttributeDefId(this.attributeDefId);
    attributeAssignAction.setContextId(this.contextId);
    attributeAssignAction.setCreatedOnDb(GrouperUtil.dateLongValue(this.createTime));
    attributeAssignAction.setHibernateVersionNumber(this.hibernateVersionNumber);
    attributeAssignAction.setLastUpdatedDb(GrouperUtil.dateLongValue(this.modifierTime));
    attributeAssignAction.setNameDb(this.name);
    attributeAssignAction.setId(this.uuid);
    
    return attributeAssignAction;
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
    xmlImportMain.getReader().addHandler( ATTRIBUTE_ASSIGN_ACTIONS_XPATH, 
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
  
    xmlImportMain.getReader().addHandler( XML_EXPORT_ATTRIBUTE_ASSIGN_ACTION_XPATH, 
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
  
                XmlExportAttributeAssignAction xmlExportAttributeAssignActionFromFile = (XmlExportAttributeAssignAction)xmlImportMain.getXstream().unmarshal(new Dom4JReader(row));
                
                AttributeAssignAction attributeAssignAction = xmlExportAttributeAssignActionFromFile.toAttributeAssignAction();
                
                XmlExportUtils.syncImportable(attributeAssignAction, xmlImportMain);
                
                xmlImportMain.incrementCurrentCount();
              } catch (RuntimeException re) {
                LOG.error("Problem importing attributeAssignAction: " + XmlExportUtils.toString(row), re);
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
    long result = HibernateSession.byHqlStatic().createQuery("select count(theAttributeAssignAction) " + exportFromOnQuery(xmlExportMain, false)).uniqueResult(Long.class);
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
      queryBuilder.append(" from AttributeAssignAction as theAttributeAssignAction ");
    } else {
      queryBuilder.append(
          " from AttributeAssignAction as theAttributeAssignAction where exists ( " +
          " select theAttributeDef from AttributeDef as theAttributeDef " +
          " where theAttributeAssignAction.attributeDefId = theAttributeDef.id and ( ");
      xmlExportMain.appendHqlStemLikeOrObjectEquals(queryBuilder, "theAttributeDef", "nameDb", false);
      queryBuilder.append(" ) ) ");
    }
    if (includeOrderBy) {
      queryBuilder.append(" order by theAttributeAssignAction.attributeDefId, theAttributeAssignAction.nameDb ");
    }
    return queryBuilder.toString();
  }


  /**
   * 
   * @param writer
   * @param xmlExportMain
   */
  public static void exportAttributeAssignActions(final Writer writer, final XmlExportMain xmlExportMain) {
    //get the members
    HibernateSession.callbackHibernateSession(GrouperTransactionType.READONLY_OR_USE_EXISTING, AuditControl.WILL_NOT_AUDIT, new HibernateHandler() {
      
      public Object callback(HibernateHandlerBean hibernateHandlerBean)
          throws GrouperDAOException {
  
        Session session = hibernateHandlerBean.getHibernateSession().getSession();
  
        //select all role sets (immediate is depth = 1)
        Query query = session.createQuery(
            "select distinct theAttributeAssignAction " + exportFromOnQuery(xmlExportMain, true));
  
        GrouperVersion grouperVersion = new GrouperVersion(GrouperVersion.GROUPER_VERSION);
        try {
          writer.write("  <attributeAssignActions>\n");
  
          //this is an efficient low-memory way to iterate through a resultset
          ScrollableResults results = null;
          try {
            results = query.scroll();
            while(results.next()) {
              Object object = results.get(0);
              final AttributeAssignAction attributeAssignAction = (AttributeAssignAction)object;
              
              //if we are writing all, then just write
              writeAttributeAssignRecord(writer, xmlExportMain, grouperVersion,
                  attributeAssignAction);
            }
          } finally {
            HibUtils.closeQuietly(results);
          }
          
          if (xmlExportMain.isIncludeComments()) {
            writer.write("\n");
          }
          
          //end the attribute assign actions element 
          writer.write("  </attributeAssignActions>\n");
        } catch (IOException ioe) {
          throw new RuntimeException("Problem with streaming attributeAssignActions", ioe);
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
  public static void exportAttributeAssignActionsGsh(final Writer writer, final XmlExportMain xmlExportMain) {

    HibernateSession.callbackHibernateSession(GrouperTransactionType.READONLY_OR_USE_EXISTING, AuditControl.WILL_NOT_AUDIT, new HibernateHandler() {
      
      public Object callback(HibernateHandlerBean hibernateHandlerBean)
          throws GrouperDAOException {

        Session session = hibernateHandlerBean.getHibernateSession().getSession();

        //select all members in order
        Query query = session.createQuery(
            "select distinct "
            + " ( select theAttributeDef.nameDb from AttributeDef theAttributeDef where theAttributeDef.id = theAttributeAssignAction.attributeDefId ), "
            + " theAttributeAssignAction "
            + exportFromOnQuery(xmlExportMain, true));
        
        try {
  
          final GrouperVersion grouperVersion = new GrouperVersion(GrouperVersion.GROUPER_VERSION);

          //this is an efficient low-memory way to iterate through a resultset
          ScrollableResults results = null;
          try {
            results = query.scroll();
            
            String previousNameOfAttributeDef = null;
            Set<String> actionList = null;
            
            XmlExportAttributeAssignAction xmlExportAttributeAssignAction = null;
            String nameOfAttributeDef = null;
            
            while(results.next()) {
              nameOfAttributeDef = (String)results.get(0);
              
              final AttributeAssignAction attributeAssignAction = (AttributeAssignAction)results.get(1);
              xmlExportAttributeAssignAction = attributeAssignAction.xmlToExportAttributeAssignAction(grouperVersion);

              final XmlExportAttributeAssignAction XML_EXPORT_ATTRIBUTE_ASSIGN_ACTION = xmlExportAttributeAssignAction;
              final Set<String> ACTION_LIST = actionList;
              final String PREVIOUS_NAME_OF_ATTRIBUTE_DEF = previousNameOfAttributeDef;
              
              //writer.write("" + subjectId + ", " + sourceId + ", " + listName + ", " + groupName 
              //    + ", " + stemName + ", " + nameOfAttributeDef 
              //    + ", " + enabledTime + ", " + disabledTime  + "\n");
              
              if (!StringUtils.equals(nameOfAttributeDef, previousNameOfAttributeDef)) {
                if (previousNameOfAttributeDef != null && actionList != null) {

                  HibernateSession.callbackHibernateSession(GrouperTransactionType.READONLY_NEW, AuditControl.WILL_NOT_AUDIT, new HibernateHandler() {
                    
                    public Object callback(HibernateHandlerBean hibernateHandlerBean)
                        throws GrouperDAOException {
                      try {
                        XML_EXPORT_ATTRIBUTE_ASSIGN_ACTION.toGsh(grouperVersion, writer, PREVIOUS_NAME_OF_ATTRIBUTE_DEF, ACTION_LIST);
                      } catch (IOException ioe) {
                        throw new RuntimeException("Problem exporting attribute actions to gsh: " + PREVIOUS_NAME_OF_ATTRIBUTE_DEF 
                            + ", " + GrouperUtil.toStringForLog(ACTION_LIST), ioe);
                      }
                      return null;
                    }
                  });

                } 
                actionList = new LinkedHashSet<String>();
                previousNameOfAttributeDef = nameOfAttributeDef;
              }
              actionList.add(xmlExportAttributeAssignAction.getName());
              xmlExportMain.incrementRecordCount();
            }
            
            if (actionList != null && xmlExportAttributeAssignAction != null) {

              xmlExportAttributeAssignAction.toGsh(grouperVersion, writer, nameOfAttributeDef, actionList);

            }

            
          } finally {
            HibUtils.closeQuietly(results);
          }
          
        } catch (IOException ioe) {
          throw new RuntimeException("Problem with streaming memberships", ioe);
        }
        return null;
      }
    });
  }

  /**
   * @param exportVersion 
   * @param writer
   * @param nameOfAttributeDef
   * @param actionList
   * @throws IOException 
   */
  public void toGsh(
      @SuppressWarnings("unused") GrouperVersion exportVersion, Writer writer, 
      String nameOfAttributeDef, Set<String> actionList) throws IOException {

    if (GrouperUtil.length(actionList) == 0) {
      throw new RuntimeException("Why is actionList null? " + nameOfAttributeDef);
    }

    if (nameOfAttributeDef == null) {
      throw new RuntimeException("Why is nameOfAttributeDef null?");
    }
    
    writer.write("attributeDef = AttributeDefFinder.findByName(\""
        + GrouperUtil.escapeDoubleQuotesSlashesAndNewlinesForString(nameOfAttributeDef) + "\", false);\n");

    writer.write("if (attributeDef != null) { ");

    //addCompositeMember(CompositeType type, Group left, Group right)
    String actionsCommaSeparated = GrouperUtil.join(actionList.iterator(), ",");
    writer.write("int changeCount = attributeDef.getAttributeDefActionDelegate().configureActionList(\"" + actionsCommaSeparated + "\"); "
        + "gshTotalObjectCount+=" + actionList.size() + "; if (changeCount > 0) { gshTotalChangeCount+=changeCount; "
            + "System.out.println(\"Made \" + changeCount + \" changes for actionList of attributeDef: " + GrouperUtil.escapeDoubleQuotesSlashesAndNewlinesForString(nameOfAttributeDef) + "\");  }");

    writer.write(" } else { gshTotalErrorCount++;  System.out.println(\"ERROR: cant find attributeDef: '" + GrouperUtil.escapeDoubleQuotesSlashesAndNewlinesForString(nameOfAttributeDef) + "'\"); }\n");

  }

  /**
   * @param writer
   * @param xmlExportMain
   * @param grouperVersion
   * @param attributeAssignAction
   * @throws IOException
   */
  private static void writeAttributeAssignRecord(final Writer writer,
      final XmlExportMain xmlExportMain, GrouperVersion grouperVersion,
      final AttributeAssignAction attributeAssignAction) throws IOException {
    //comments to dereference the foreign keys
    if (xmlExportMain.isIncludeComments()) {
      HibernateSession.callbackHibernateSession(GrouperTransactionType.READONLY_NEW, AuditControl.WILL_NOT_AUDIT, new HibernateHandler() {
        
        public Object callback(HibernateHandlerBean hibernateHandlerBean)
            throws GrouperDAOException {
          try {
            writer.write("\n    <!-- ");
            
            XmlExportUtils.toStringAttributeDef(null, writer, attributeAssignAction.getAttributeDefId(), false);

            writer.write(" -->\n");
            return null;
          } catch (IOException ioe) {
            throw new RuntimeException(ioe);
          }
        }
      });
    }
    
    XmlExportAttributeAssignAction xmlExportAttributeAssign = attributeAssignAction.xmlToExportAttributeAssignAction(grouperVersion);
    writer.write("    ");
    xmlExportAttributeAssign.toXml(grouperVersion, writer);
    writer.write("\n");
    xmlExportMain.incrementRecordCount();
  }

  /**
   * take a reader (e.g. dom reader) and convert to an xml export group
   * @param exportVersion
   * @param hierarchicalStreamReader
   * @return the bean
   */
  public static XmlExportAttributeAssignAction fromXml(@SuppressWarnings("unused") GrouperVersion exportVersion, 
      HierarchicalStreamReader hierarchicalStreamReader) {
    XStream xStream = XmlExportUtils.xstream();
    
    XmlExportAttributeAssignAction xmlExportAttributeAssignAction = (XmlExportAttributeAssignAction)xStream.unmarshal(hierarchicalStreamReader);
  
    return xmlExportAttributeAssignAction;
  }

  /**
   * 
   * @param exportVersion
   * @param xml
   * @return the object from xml
   */
  public static XmlExportAttributeAssignAction fromXml(
      @SuppressWarnings("unused") GrouperVersion exportVersion, String xml) {
    XStream xStream = XmlExportUtils.xstream();
    
    XmlExportAttributeAssignAction xmlExportAttributeAssignAction = (XmlExportAttributeAssignAction)xStream.fromXML(xml);
  
    return xmlExportAttributeAssignAction;
  }

  /**
   * parse the xml file for attributeAssignActions
   * @param xmlImportMain
   */
  public static void processXmlFirstPass(final XmlImportMain xmlImportMain) {
    xmlImportMain.getReader().addHandler( ATTRIBUTE_ASSIGN_ACTIONS_XPATH, 
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

    xmlImportMain.getReader().addHandler( XML_EXPORT_ATTRIBUTE_ASSIGN_ACTION_XPATH, 
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
