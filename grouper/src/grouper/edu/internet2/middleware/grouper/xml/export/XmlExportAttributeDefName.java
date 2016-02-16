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

import edu.internet2.middleware.grouper.attr.AttributeDefName;
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
public class XmlExportAttributeDefName {

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
  private static final String XML_EXPORT_ATTRIBUTE_DEF_NAME_XPATH = "/grouperExport/attributeDefNames/XmlExportAttributeDefName";

  /**
   * 
   */
  private static final String ATTRIBUTE_DEF_NAMES_XPATH = "/grouperExport/attributeDefNames";

  /** attributeDefId */
  private String attributeDefId;

  /** displayExtension */
  private String displayExtension;
  
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
   * display extension
   * @return display extension
   */
  public String getDisplayExtension() {
    return this.displayExtension;
  }

  /**
   * displayExtension
   * @param displayExtension1
   */
  public void setDisplayExtension(String displayExtension1) {
    this.displayExtension = displayExtension1;
  }

  /**
   * displayName
   * @return displayName
   */
  public String getDisplayName() {
    return this.displayName;
  }

  /**
   * displayName
   * @param displayName1
   */
  public void setDisplayName(String displayName1) {
    this.displayName = displayName1;
  }

  /** displayName */
  private String displayName;
  
  /** uuid */
  private String uuid;
  
  /** parentStem */
  private String parentStem;

  /** name */
  private String name;

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

  /**
   * logger 
   */
  private static final Log LOG = GrouperUtil.getLog(XmlExportAttributeDefName.class);

  /**
   * 
   */
  public XmlExportAttributeDefName() {
    
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
   * convert to attributeDefName
   * @return the attributeDefName
   */
  public AttributeDefName toAttributeDefName() {
    AttributeDefName attributeDefName = new AttributeDefName();
    
    attributeDefName.setAttributeDefId(this.attributeDefId);
    attributeDefName.setContextId(this.contextId);
    attributeDefName.setCreatedOnDb(GrouperUtil.dateLongValue(this.createTime));
    attributeDefName.setDescription(this.description);
    attributeDefName.setDisplayExtensionDb(this.displayExtension);
    attributeDefName.setDisplayNameDb(this.displayName);
    attributeDefName.setExtensionDb(this.extension);
    attributeDefName.setHibernateVersionNumber(this.hibernateVersionNumber);
    attributeDefName.setIdIndex(this.idIndex);
    attributeDefName.setLastUpdatedDb(GrouperUtil.dateLongValue(this.modifierTime));
    attributeDefName.setNameDb(this.name);
    attributeDefName.setStemId(this.parentStem);
    attributeDefName.setId(this.uuid);
    
    return attributeDefName;
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
    xmlImportMain.getReader().addHandler( ATTRIBUTE_DEF_NAMES_XPATH, 
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
  
    xmlImportMain.getReader().addHandler( XML_EXPORT_ATTRIBUTE_DEF_NAME_XPATH, 
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
  
                XmlExportAttributeDefName xmlExportAttributeDefName = (XmlExportAttributeDefName)xmlImportMain.getXstream().unmarshal(new Dom4JReader(row));
                
                AttributeDefName attributeDefName = xmlExportAttributeDefName.toAttributeDefName();
                
                XmlExportUtils.syncImportable(attributeDefName, xmlImportMain);
                
                xmlImportMain.incrementCurrentCount();
              } catch (RuntimeException re) {
                LOG.error("Problem importing attributeDefName: " + XmlExportUtils.toString(row), re);
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
    long result = HibernateSession.byHqlStatic().createQuery("select count(theAttributeDefName) " 
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
    return exportFromOnQuery(xmlExportMain, includeOrderBy, true);
  }

  /**
   * get the query from the FROM clause on to the end for export
   * @param xmlExportMain
   * @param includeOrderBy 
   * @param includeAttributeDefScoping
   * @return the export query
   */
  private static String exportFromOnQuery(XmlExportMain xmlExportMain, boolean includeOrderBy, boolean includeAttributeDefScoping) {
    //select all members in order
    StringBuilder queryBuilder = new StringBuilder();
    if (!xmlExportMain.filterStemsOrObjects()) {
      queryBuilder.append(" from AttributeDefName as theAttributeDefName ");
    } else {
      queryBuilder.append(
          " from AttributeDefName as theAttributeDefName where ( ");
      xmlExportMain.appendHqlStemLikeOrObjectEquals(queryBuilder, "theAttributeDefName", "nameDb", false);
      queryBuilder.append(" )");
      if (includeAttributeDefScoping) {
        queryBuilder.append(" and exists ( select theAttributeDef from AttributeDef as theAttributeDef " +
            " where theAttributeDef.id = theAttributeDefName.attributeDefId and ( ");
        xmlExportMain.appendHqlStemLikeOrObjectEquals(queryBuilder, "theAttributeDef", "nameDb", false);
        queryBuilder.append(" ) ) ");
      }
    }
    if (includeOrderBy) {
      queryBuilder.append(" order by theAttributeDefName.nameDb ");
    }
    return queryBuilder.toString();
  }

  /**
   * 
   * @param writer
   * @param xmlExportMain 
   */
  public static void exportAttributeDefNames(final Writer writer, final XmlExportMain xmlExportMain) {
    //get the members
    HibernateSession.callbackHibernateSession(GrouperTransactionType.READONLY_OR_USE_EXISTING, AuditControl.WILL_NOT_AUDIT, new HibernateHandler() {
      
      public Object callback(HibernateHandlerBean hibernateHandlerBean)
          throws GrouperDAOException {
  
        Session session = hibernateHandlerBean.getHibernateSession().getSession();
  
        //select all members in order
        Query query = session.createQuery(
            "select distinct theAttributeDefName " + exportFromOnQuery(xmlExportMain, true));
  
        GrouperVersion grouperVersion = new GrouperVersion(GrouperVersion.GROUPER_VERSION);
        try {
          writer.write("  <attributeDefNames>\n");
  
          //this is an efficient low-memory way to iterate through a resultset
          ScrollableResults results = null;
          try {
            results = query.scroll();
            while(results.next()) {
              Object object = results.get(0);
              AttributeDefName attributeDefName = (AttributeDefName)object;
              XmlExportAttributeDefName xmlExportAttributeDefName = attributeDefName.xmlToExportAttributeDefName(grouperVersion);
              writer.write("    ");
              xmlExportAttributeDefName.toXml(grouperVersion, writer);
              writer.write("\n");
              xmlExportMain.incrementRecordCount();
            }
          } finally {
            HibUtils.closeQuietly(results);
          }
          
          //end the members element 
          writer.write("  </attributeDefNames>\n");
        } catch (IOException ioe) {
          throw new RuntimeException("Problem with streaming groups", ioe);
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
  public static void exportAttributeDefNamesGsh(final Writer writer, final XmlExportMain xmlExportMain) {
    //get the members
    HibernateSession.callbackHibernateSession(GrouperTransactionType.READONLY_OR_USE_EXISTING, AuditControl.WILL_NOT_AUDIT, new HibernateHandler() {
      
      public Object callback(HibernateHandlerBean hibernateHandlerBean)
          throws GrouperDAOException {
  
        Session session = hibernateHandlerBean.getHibernateSession().getSession();
  
        
        //select all members in order
        Query query = session.createQuery(
            "select distinct "
              + "( select theAttributeDef.nameDb from AttributeDef theAttributeDef where theAttributeDef.id = theAttributeDefName.attributeDefId ), "
              + "theAttributeDefName " + exportFromOnQuery(xmlExportMain, true, false));

        final GrouperVersion grouperVersion = new GrouperVersion(GrouperVersion.GROUPER_VERSION);

        //this is an efficient low-memory way to iterate through a resultset
        ScrollableResults results = null;
        
        String previousAttributeDefId = null;
        
        try {
          results = query.scroll();
          while(results.next()) {
            final String nameOfAttributeDef = (String)results.get(0);
            final AttributeDefName attributeDefName = (AttributeDefName)results.get(1);
            final XmlExportAttributeDefName xmlExportAttributeDefName = attributeDefName.xmlToExportAttributeDefName(grouperVersion);
            final String PREVIOUS_ATTRIBUTE_DEF_ID = previousAttributeDefId;

            //writer.write("" + subjectId + ", " + sourceId + ", " + listName + ", " + groupName 
            //    + ", " + stemName + ", " + nameOfAttributeDef 
            //    + ", " + enabledTime + ", " + disabledTime  + "\n");
            
            HibernateSession.callbackHibernateSession(GrouperTransactionType.READONLY_NEW, AuditControl.WILL_NOT_AUDIT, new HibernateHandler() {
              
              public Object callback(HibernateHandlerBean hibernateHandlerBean)
                  throws GrouperDAOException {
                try {
                  xmlExportAttributeDefName.toGsh(grouperVersion, writer, nameOfAttributeDef, 
                      !StringUtils.equals(PREVIOUS_ATTRIBUTE_DEF_ID, xmlExportAttributeDefName.getAttributeDefId()));
                } catch (IOException ioe) {
                  throw new RuntimeException("Problem exporting attributeDefName to gsh: " + nameOfAttributeDef, ioe);
                }
                return null;
              }
            });
            xmlExportMain.incrementRecordCount();

            previousAttributeDefId = xmlExportAttributeDefName.getAttributeDefId();
          }
        } finally {
          HibUtils.closeQuietly(results);
        }
        
        return null;
      }
    });
  }

  /**
   * @param exportVersion 
   * @param writer
   * @param nameOfAttributeDef 
   * @param printAttributeDefFinder 
   * @throws IOException 
   */
  public void toGsh(
      @SuppressWarnings("unused") GrouperVersion exportVersion, Writer writer, String nameOfAttributeDef, boolean printAttributeDefFinder) throws IOException {

    if (printAttributeDefFinder) {
      writer.write("AttributeDef attributeDef = AttributeDefFinder.findByName(\""
        + GrouperUtil.escapeDoubleQuotesSlashesAndNewlinesForString(nameOfAttributeDef) + "\", false);\n");
    }

    writer.write("if (attributeDef != null) { ");
    writer.write(" AttributeDefNameSave attributeDefNameSave = new AttributeDefNameSave(grouperSession, attributeDef).assignName(\""
        + GrouperUtil.escapeDoubleQuotesSlashesAndNewlinesForString(this.name) 
        + "\").assignCreateParentStemsIfNotExist(true)");

    if (!StringUtils.isBlank(this.description)) {
      writer.write(".assignDescription(\""
        + GrouperUtil.escapeDoubleQuotesSlashesAndNewlinesForString(this.description)
        + "\")");
    }
    writer.write(".assignDisplayName(\""
        + GrouperUtil.escapeDoubleQuotesSlashesAndNewlinesForString(this.displayName)
        + "\");  ");

    writer.write("AttributeDefName attributeDefName = attributeDefNameSave.save();  gshTotalObjectCount++;  if (attributeDefNameSave.getSaveResultType() != SaveResultType.NO_CHANGE) {gshTotalChangeCount++;  System.out.println(\"Made change for attributeDefName: \" + attributeDefName.getName()); }  ");
    
    writer.write(" } else { gshTotalErrorCount++;  System.out.println(\"ERROR: cant find attributeDef: '" + GrouperUtil.escapeDoubleQuotesSlashesAndNewlinesForString(nameOfAttributeDef) + "'\"); } \n");
  }
  
  
  /**
   * take a reader (e.g. dom reader) and convert to an xml export group
   * @param exportVersion
   * @param hierarchicalStreamReader
   * @return the bean
   */
  public static XmlExportAttributeDefName fromXml(@SuppressWarnings("unused") GrouperVersion exportVersion, 
      HierarchicalStreamReader hierarchicalStreamReader) {
    XStream xStream = XmlExportUtils.xstream();
    
    XmlExportAttributeDefName xmlExportAttributeDefName = (XmlExportAttributeDefName)xStream.unmarshal(hierarchicalStreamReader);
  
    return xmlExportAttributeDefName;
  }

  /**
   * 
   * @param exportVersion
   * @param xml
   * @return the object from xml
   */
  public static XmlExportAttributeDefName fromXml(
      @SuppressWarnings("unused") GrouperVersion exportVersion, String xml) {
    XStream xStream = XmlExportUtils.xstream();
    
    XmlExportAttributeDefName xmlExportAttributeDefName = (XmlExportAttributeDefName)xStream.fromXML(xml);
  
    return xmlExportAttributeDefName;
  }

  /**
   * parse the xml file for attributeDefNames
   * @param xmlImportMain
   */
  public static void processXmlFirstPass(final XmlImportMain xmlImportMain) {
    xmlImportMain.getReader().addHandler( ATTRIBUTE_DEF_NAMES_XPATH, 
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

    xmlImportMain.getReader().addHandler( XML_EXPORT_ATTRIBUTE_DEF_NAME_XPATH, 
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
