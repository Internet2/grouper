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

import edu.internet2.middleware.grouper.attr.AttributeDefScope;
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
public class XmlExportAttributeDefScope {

  /**
   * 
   */
  private static final String XML_EXPORT_ATTRIBUTE_DEF_SCOPE_XPATH = "/grouperExport/attributeDefScopes/XmlExportAttributeDefScope";

  /**
   * 
   */
  private static final String ATTRIBUTE_DEF_SCOPES_XPATH = "/grouperExport/attributeDefScopes";

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

  /** attributeDefScopeType */
  private String attributeDefScopeType;

  
  
  /**
   * attributeDefScopeType
   * @return attributeDefScopeType
   */
  public String getAttributeDefScopeType() {
    return this.attributeDefScopeType;
  }

  /**
   * attributeDefScopeType
   * @param attributeDefScopeType1
   */
  public void setAttributeDefScopeType(String attributeDefScopeType1) {
    this.attributeDefScopeType = attributeDefScopeType1;
  }

  /** scopeString */
  private String scopeString;
  
  /**
   * scopeString
   * @return scopeString
   */
  public String getScopeString() {
    return this.scopeString;
  }

  /**
   * scopeString
   * @param scopeString1
   */
  public void setScopeString(String scopeString1) {
    this.scopeString = scopeString1;
  }

  /** scopeString2 */
  private String scopeString2;

  /**
   * logger 
   */
  private static final Log LOG = GrouperUtil.getLog(XmlExportAttributeDefScope.class);
  
  /**
   * scopeString2
   * @return scopeString2
   */
  public String getScopeString2() {
    return this.scopeString2;
  }

  /**
   * scopeString2
   * @param _scopeString2
   */
  public void setScopeString2(String _scopeString2) {
    this.scopeString2 = _scopeString2;
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
  public XmlExportAttributeDefScope() {
    
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
   * convert to attributeDefNameSet
   * @return the attributeDefNameSet
   */
  public AttributeDefScope toAttributeDefScope() {
    AttributeDefScope attributeDefScope = new AttributeDefScope();
    
    attributeDefScope.setAttributeDefId(this.attributeDefId);
    attributeDefScope.setAttributeDefScopeTypeDb(this.attributeDefScopeType);
    attributeDefScope.setContextId(this.contextId);
    attributeDefScope.setCreatedOnDb(GrouperUtil.dateLongValue(this.createTime));
    attributeDefScope.setHibernateVersionNumber(this.hibernateVersionNumber);
    attributeDefScope.setLastUpdatedDb(GrouperUtil.dateLongValue(this.modifierTime));
    attributeDefScope.setId(this.uuid);
    attributeDefScope.setScopeString(this.scopeString);
    attributeDefScope.setScopeString2(this.scopeString2);
    
    return attributeDefScope;
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
    xmlImportMain.getReader().addHandler( ATTRIBUTE_DEF_SCOPES_XPATH, 
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
  
    xmlImportMain.getReader().addHandler( XML_EXPORT_ATTRIBUTE_DEF_SCOPE_XPATH, 
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
  
                XmlExportAttributeDefScope xmlExportAttributeDefScopeFromFile = (XmlExportAttributeDefScope)xmlImportMain.getXstream().unmarshal(new Dom4JReader(row));
                
                AttributeDefScope attributeDefScope = xmlExportAttributeDefScopeFromFile.toAttributeDefScope();
                
                XmlExportUtils.syncImportableMultiple(attributeDefScope, xmlImportMain);
                
                xmlImportMain.incrementCurrentCount();
              } catch (RuntimeException re) {
                LOG.error("Problem importing attributeDefScope: " + XmlExportUtils.toString(row), re);
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
    long result = HibernateSession.byHqlStatic().createQuery("select count(theAttributeDefScope) " 
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
      queryBuilder.append(" from AttributeDefScope as theAttributeDefScope ");
    } else {
      queryBuilder.append(
          " from AttributeDefScope as theAttributeDefScope where exists ( select theAttributeDef from AttributeDef as theAttributeDef " +
          " where theAttributeDefScope.attributeDefId = theAttributeDef.id and ( ");
      xmlExportMain.appendHqlStemLikeOrObjectEquals(queryBuilder, "theAttributeDef", "nameDb", false);
      queryBuilder.append(" ) ) ");
    }
    if (includeOrderBy) {
      queryBuilder.append(" order by theAttributeDefScope.attributeDefId, theAttributeDefScope.id ");
    }
    return queryBuilder.toString();
  }

        
  /**
   * 
   * @param writer
   * @param xmlExportMain
   */
  public static void exportAttributeDefScopes(final Writer writer, final XmlExportMain xmlExportMain) {
    //get the members
    HibernateSession.callbackHibernateSession(GrouperTransactionType.READONLY_OR_USE_EXISTING, AuditControl.WILL_NOT_AUDIT, new HibernateHandler() {
      
      public Object callback(HibernateHandlerBean hibernateHandlerBean)
          throws GrouperDAOException {
  
        Session session = hibernateHandlerBean.getHibernateSession().getSession();
  
        //select all action sets (immediate is depth = 1)
        Query query = session.createQuery(
            "select theAttributeDefScope " + exportFromOnQuery(xmlExportMain, true));
  
        GrouperVersion grouperVersion = new GrouperVersion(GrouperVersion.GROUPER_VERSION);
        try {
          writer.write("  <attributeDefScopes>\n");
  
          //this is an efficient low-memory way to iterate through a resultset
          ScrollableResults results = null;
          try {
            results = query.scroll();
            while(results.next()) {
              Object object = results.get(0);
              final AttributeDefScope attributeDefScope = (AttributeDefScope)object;
              
              
              //comments to dereference the foreign keys
              if (xmlExportMain.isIncludeComments()) {
                HibernateSession.callbackHibernateSession(GrouperTransactionType.READONLY_NEW, AuditControl.WILL_NOT_AUDIT, new HibernateHandler() {
                  
                  public Object callback(HibernateHandlerBean hibernateHandlerBean)
                      throws GrouperDAOException {
                    try {
                      writer.write("\n    <!-- ");
                      
                      XmlExportUtils.toStringAttributeDef(null, writer, attributeDefScope.getAttributeDefId(), false);

                      writer.write(" -->\n");
                      return null;
                    } catch (IOException ioe) {
                      throw new RuntimeException(ioe);
                    }
                  }
                });
              }
              
              XmlExportAttributeDefScope xmlExportAttributeDefScope = attributeDefScope.xmlToExportAttributeDefScope(grouperVersion);
              writer.write("    ");
              xmlExportAttributeDefScope.toXml(grouperVersion, writer);
              writer.write("\n");
              xmlExportMain.incrementRecordCount();
            }
          } finally {
            HibUtils.closeQuietly(results);
          }
          
          if (xmlExportMain.isIncludeComments()) {
            writer.write("\n");
          }
          
          //end the attribute def scope element 
          writer.write("  </attributeDefScopes>\n");
        } catch (IOException ioe) {
          throw new RuntimeException("Problem with streaming attributeDefScopes", ioe);
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
  public static void exportAttributeDefScopesGsh(final Writer writer, final XmlExportMain xmlExportMain) {
    //get the members
    HibernateSession.callbackHibernateSession(GrouperTransactionType.READONLY_OR_USE_EXISTING, AuditControl.WILL_NOT_AUDIT, new HibernateHandler() {
      
      public Object callback(HibernateHandlerBean hibernateHandlerBean)
          throws GrouperDAOException {
  
        Session session = hibernateHandlerBean.getHibernateSession().getSession();
  
        //select all action sets (immediate is depth = 1)
        Query query = session.createQuery(
            "select ( select theAttributeDef.nameDb from AttributeDef theAttributeDef where theAttributeDef.id = theAttributeDefScope.attributeDefId ), "
            + " theAttributeDefScope " + exportFromOnQuery(xmlExportMain, true));
        
        GrouperVersion grouperVersion = new GrouperVersion(GrouperVersion.GROUPER_VERSION);
        try {
  
          //this is an efficient low-memory way to iterate through a resultset
          ScrollableResults results = null;
          try {
            results = query.scroll();
            while(results.next()) {
              String nameOfAttributeDef = (String)results.get(0);
              Object object = results.get(1);
              AttributeDefScope attributeDefScope = (AttributeDefScope)object;
              
              XmlExportAttributeDefScope xmlExportAttributeDefScope = attributeDefScope.xmlToExportAttributeDefScope(grouperVersion);
              xmlExportAttributeDefScope.toGsh(grouperVersion, writer, nameOfAttributeDef);
              xmlExportMain.incrementRecordCount();
            }
          } finally {
            HibUtils.closeQuietly(results);
          }
          
        } catch (IOException ioe) {
          throw new RuntimeException("Problem with streaming attributeDefScopes", ioe);
        }
        return null;
      }
    });
  }
  
  /**
   * @param exportVersion 
   * @param writer
   * @param nameOfAttributeDef
   * @throws IOException 
   */
  public void toGsh(
      @SuppressWarnings("unused") GrouperVersion exportVersion, Writer writer, 
      String nameOfAttributeDef) throws IOException {

    if (nameOfAttributeDef == null) {
      throw new RuntimeException("Why is nameOfAttributeDef null?");
    }
    
    writer.write("attributeDef = AttributeDefFinder.findByName(\""
        + GrouperUtil.escapeDoubleQuotes(nameOfAttributeDef) + "\", false);\n");

    writer.write("attributeDefScopeType = AttributeDefScopeType.valueOfIgnoreCase(\"" + GrouperUtil.escapeDoubleQuotes(this.getAttributeDefScopeType()) + "\", true);\n");

    writer.write("if (attributeDef != null) { ");

    writer.write("if (attributeDefScopeType != null) { ");

    //addCompositeMember(CompositeType type, Group left, Group right)
    
    String scopeString = this.getScopeString() == null ? "null" : ("\"" + GrouperUtil.escapeDoubleQuotes(this.getScopeString()) + "\"");
    String scopeString2 = this.getScopeString2() == null ? "null" : ("\"" + GrouperUtil.escapeDoubleQuotes(this.getScopeString2()) + "\"");
    writer.write("gshTotalObjectCount++;  if (attributeDef.getAttributeDefScopeDelegate().retrieveAttributeDefScope(attributeDefScopeType, \""
        + GrouperUtil.escapeDoubleQuotes(scopeString) + "\", \"" + GrouperUtil.escapeDoubleQuotes(scopeString2) + "\") != null) { ");
    writer.write("gshTotalChangeCount++; attributeDef.getAttributeDefScopeDelegate().assignScope(attributeDefScopeType, \""
        + GrouperUtil.escapeDoubleQuotes(scopeString) + "\", \"" + GrouperUtil.escapeDoubleQuotes(scopeString2) + "\"); "
            + "System.out.println(\"Made change for attributeDefScope on attributeDef: " + GrouperUtil.escapeDoubleQuotes(nameOfAttributeDef) + ", " 
        + GrouperUtil.escapeDoubleQuotes(this.getScopeString()) + ", " + GrouperUtil.escapeDoubleQuotes(this.getScopeString2()) +  "\"); } ");
    writer.write(" } else { gshTotalErrorCount++; System.out.println(\"ERROR: cant find attributeDefScopeType: '" + GrouperUtil.escapeDoubleQuotes(this.getAttributeDefScopeType()) + "'\"); } ");

    writer.write(" } else { gshTotalErrorCount++; System.out.println(\"ERROR: cant find attributeDef: '" + GrouperUtil.escapeDoubleQuotes(nameOfAttributeDef) + "'\"); }\n");

  }


  /**
   * take a reader (e.g. dom reader) and convert to an xml export group
   * @param exportVersion
   * @param hierarchicalStreamReader
   * @return the bean
   */
  public static XmlExportAttributeDefScope fromXml(@SuppressWarnings("unused") GrouperVersion exportVersion, 
      HierarchicalStreamReader hierarchicalStreamReader) {
    XStream xStream = XmlExportUtils.xstream();
    
    XmlExportAttributeDefScope xmlExportAttributeDefScope = (XmlExportAttributeDefScope)xStream.unmarshal(hierarchicalStreamReader);
  
    return xmlExportAttributeDefScope;
  }

  /**
   * 
   * @param exportVersion
   * @param xml
   * @return the object from xml
   */
  public static XmlExportAttributeDefScope fromXml(
      @SuppressWarnings("unused") GrouperVersion exportVersion, String xml) {
    XStream xStream = XmlExportUtils.xstream();
    
    XmlExportAttributeDefScope xmlExportAttributeDefScope = (XmlExportAttributeDefScope)xStream.fromXML(xml);
  
    return xmlExportAttributeDefScope;
  }

  /**
   * parse the xml file for attributeDefScopes
   * @param xmlImportMain
   */
  public static void processXmlFirstPass(final XmlImportMain xmlImportMain) {
    xmlImportMain.getReader().addHandler( ATTRIBUTE_DEF_SCOPES_XPATH, 
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

    xmlImportMain.getReader().addHandler( XML_EXPORT_ATTRIBUTE_DEF_SCOPE_XPATH, 
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
