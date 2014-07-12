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
 Copyright (C) 2004-2007 University Corporation for Advanced Internet Development, Inc.
 Copyright 2004-2006 The University Of Bristol

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

package edu.internet2.middleware.grouper.xml;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.FieldFinder;
import edu.internet2.middleware.grouper.FieldType;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GroupType;
import edu.internet2.middleware.grouper.GroupTypeFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.audit.AuditEntry;
import edu.internet2.middleware.grouper.audit.AuditTypeBuiltin;
import edu.internet2.middleware.grouper.audit.GrouperEngineBuiltin;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.exception.AttributeNotFoundException;
import edu.internet2.middleware.grouper.exception.GrantPrivilegeException;
import edu.internet2.middleware.grouper.exception.GroupAddException;
import edu.internet2.middleware.grouper.exception.GroupModifyException;
import edu.internet2.middleware.grouper.exception.GroupNotFoundException;
import edu.internet2.middleware.grouper.exception.GrouperException;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.exception.MemberAddException;
import edu.internet2.middleware.grouper.exception.MemberDeleteException;
import edu.internet2.middleware.grouper.exception.RevokePrivilegeException;
import edu.internet2.middleware.grouper.exception.SchemaException;
import edu.internet2.middleware.grouper.exception.SessionException;
import edu.internet2.middleware.grouper.exception.StemAddException;
import edu.internet2.middleware.grouper.exception.StemModifyException;
import edu.internet2.middleware.grouper.exception.StemNotFoundException;
import edu.internet2.middleware.grouper.hibernate.AuditControl;
import edu.internet2.middleware.grouper.hibernate.GrouperContext;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionType;
import edu.internet2.middleware.grouper.hibernate.HibernateHandler;
import edu.internet2.middleware.grouper.hibernate.HibernateHandlerBean;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.internal.util.Quote;
import edu.internet2.middleware.grouper.internal.util.U;
import edu.internet2.middleware.grouper.misc.CompositeType;
import edu.internet2.middleware.grouper.misc.E;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.misc.GrouperStartup;
import edu.internet2.middleware.grouper.privs.Privilege;
import edu.internet2.middleware.grouper.subj.LazySubject;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.validator.NotNullOrEmptyValidator;
import edu.internet2.middleware.grouper.xml.userAudit.XmlUserAuditImport;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectNotFoundException;
import edu.internet2.middleware.subject.SubjectNotUniqueException;

/**
 * Utility class for importing data in XML import into the Groups Registry.
 * <p>
 * This class reads an XML file representing all or part of a Groups Registry
 * and updates-or-creates the equivalent {@link Stem}s, {@link Group}s and
 * {@link Membership}s.  This class can be used to load data exported by 
 * {@link XmlExporter}.
 * <p/>
 * <p><b>The API for this class will change in future Grouper releases.</b></p>
 * @author  Gary Brown.
 * @author  blair christensen.
 * @version $Id: XmlImporter.java,v 1.24.2.2 2009-12-18 21:03:44 tzeller Exp $
 * @since   1.0
 */
public class XmlImporter {

  // PRIVATE CLASS CONSTANTS //  
  private static final String CF            = "import.properties";
  private static final Log    LOG           = GrouperUtil.getLog(XmlImporter.class);
  private static final String MODE_ADD      = "add";
  private static final String MODE_IGNORE   = "ignore";
  private static final String MODE_REPLACE  = "replace";
  private static final String SPECIAL_C     = "c"; // i'm assuming this refers to a composite mship
  private static final String SPECIAL_E     = "e"; // i'm assuming this refers to an effective mship


  // PRIVATE INSTANCE VARIABLES //
  private List            accessPrivLists = new ArrayList();
  private Document        doc;
  private Map             importedGroups  = new HashMap();
  private String          importRoot; // Anchor import here
  private List            membershipLists = new ArrayList();
  private List            namingPrivLists = new ArrayList();
  private Properties      options         = new Properties();
  private GrouperSession  s;
  private boolean         updateOnly      = false;
  private boolean         ignoreInternal  = false;
  
  
  // CONSTRUCTORS //
  
  /**
   * Import the Groups Registry from XML.
   * <p>
   * The import process is configured through the following properties.
   * </p>
   * <table width="90%" border="1">
   * <tr>
   * <td>Key</td>
   * <td>Values</td>
   * <td>Default</td>
   * <td>Description</td>
   * </tr>
   * <tr>
   * <td>import.metadata.group-types</td>
   * <td>true/false</td>
   * <td>true</td>
   * <td>If true create custom group types when importing.</td>
   * </tr>
   * <tr>
   * <td>import.metadata.group-type-attributes</td>
   * <td>true/false</td>
   * <td>true</td>
   * <td>If true create custom fields when importing.</td>
   * </tr>
   * <tr>
   * <td>import.data.ignore-internal-attributes-and-uuids=false</td>
   * <td>true/false</td>
   * <td>false</td>
   * <td>If true, do not attempt to set internal attributes or Group/Stem uuids</td>
   * </tr>
   * <tr>
   * <td>import.data.apply-new-group-types</td>
   * <td>true/false</td>
   * <td>true</td>
   * <td>If true custom group types are applied to pre-existing groups when importing.</td>
   * </tr>
   * <tr>
   * <td>import.data.update-attributes</td>
   * <td>true/false</td>
   * <td>true</td>
   * <td>If true overwrite attributes on pre-existing groups when importing.</td>
   * </tr>
   * <tr>
   * <td>import.data.fail-on-unresolvable-subject</td>
   * <td>true/false</td>
   * <td>false</td>
   * <td>If true, and the import file references a subject which cannot be resolved 
   * abort the import, otherwise, log the problem and continue.
   * </td>
   * </tr>
   * <tr>
   * <td>import.data.lists</td>
   * <td>ignore/replace/add</td>
   * <td>replace</td>
   * <td>Determines whether membership lists are ignored, replaced or appended to pre-existing memberships when importing.</td>
   * </tr>
   * <tr>
   * <td>import.data.privileges</td>
   * <td>ignore/replace/add</td>
   * <td>add</td>
   * <td>Determines whether privileges are ignored, replaced or appended to pre-existing privileges when importing.</td>
   * </tr>
   * </table>
   * @param   s           Perform import within this session.
   * @param   userOptions User-specified configuration parameters.
   * @since   1.1.0
   */
  public XmlImporter(GrouperSession s, Properties userOptions) 
  {
    try {
      this.options  = XmlUtils.internal_getSystemProperties(LOG, CF);
    }
    catch (IOException eIO) {
      throw new GrouperException(eIO.getMessage(), eIO);
    }
    this.options.putAll(userOptions); 
    this.s = s;
  } // public XmlImporter(s, userOptions)


  // MAIN //

  /**
   * Process an Xml file as the 'root' user.
   * <p/>
   * @param   args    args[0] = name of Xml file to process
   * @since   1.1.0
   */
  public static void main(final String[] args) {
    
    //set this and leave it...
    GrouperContext.createNewDefaultContext(GrouperEngineBuiltin.IMPORT, false, true);

    if (XmlArgs.internal_wantsHelp(args)) {
      System.out.println( _getUsage() );
      //System.exit(0);
      return;
    }
    final Properties rc;
    try {
      rc = XmlArgs.internal_getXmlImportArgs(args);
    }
    catch (Exception e) {
      e.printStackTrace();
      System.err.println();
      System.err.println( _getUsage() );
      //System.exit(1);
      return;
    }
    
  //make sure right db
    if(!"true".equals(rc.getProperty(XmlArgs.RC_NOPROMPT))) {
      GrouperUtil.promptUserAboutDbChanges("import data from xml", true);
    }
    GrouperStartup.runFromMain = true;
    GrouperStartup.startup();
    final XmlImporter[] importer = new XmlImporter[1];
    
    GrouperSession.startRootSession();
    
    HibernateSession.callbackHibernateSession(
        GrouperTransactionType.NONE, AuditControl.WILL_AUDIT,
        new HibernateHandler() {

          public Object callback(HibernateHandlerBean hibernateHandlerBean)
              throws GrouperDAOException {
            try { 
              
              importer[0]  = new XmlImporter(
                GrouperSession.start(
                  SubjectFinder.findByIdentifier( rc.getProperty(XmlArgs.RC_SUBJ), true ), false
                ),
                XmlUtils.internal_getUserProperties(LOG, rc.getProperty(XmlArgs.RC_UPROPS))
              );
              _handleArgs(importer[0], rc);
              
              GrouperSession staticSession = GrouperSession.staticGrouperSession(false);
              if(staticSession==null) {
            	  staticSession = GrouperSession.start(SubjectFinder.findByIdentifier( rc.getProperty(XmlArgs.RC_SUBJ), true));  
              }
              AuditEntry auditEntry = new AuditEntry(AuditTypeBuiltin.XML_IMPORT, "fileName", 
                  rc.getProperty(XmlArgs.RC_IFILE), "subjectId", rc.getProperty(XmlArgs.RC_SUBJ));
              auditEntry.setDescription("Imported xml: " + GrouperUtil.toStringForLog(args));
              auditEntry.saveOrUpdate(true);
              staticSession.stop();
              
            } catch (Exception e) {
              LOG.fatal("unable to import from xml: " + e.getMessage(), e);
              System.out.println("unable to import from xml: " + e.getMessage());
              e.printStackTrace();
              //System.exit(1);
            } finally {
              if (importer[0] != null) {
                try {
                  importer[0].s.stop();
                }
                catch (SessionException eS) {
                  LOG.error(eS.getMessage());
                }
              }
            }
            return null;
          }
        });
      
    LOG.debug("Finished import of [" + rc.getProperty(XmlArgs.RC_IFILE) + "]");
    System.exit(0);
    return;
  } // public static void main(args)


  // PUBLIC INSTANCE METHODS //

  /**
   * Populate Groups Registry.
   * <pre class="eg">
   * try {
   *   importer.load( XmlReader.getDocumentFromString(s) );
   * }
   * catch (GrouperException eG) {
   *   // error importing
   * }
   * </pre>
   * @param   doc   Import this <tt>Document</tt>.
   * @throws  GrouperException
   * @throws  IllegalArgumentException if <tt>doc</tt> is null
   * @since   1.1.0
   */
  public void load(final Document doc)
    throws  GrouperException,
            IllegalArgumentException
  {
    LOG.info("starting load at root stem");
    XmlImporter.this._load( StemFinder.findRootStem(this.s), doc );
    LOG.info("finished load");
  } // public void load(xml)

  /**
   * Populate Groups Registry using the specified <tt>Stem</tt> as the root of
   * the registry.
   * <pre class="eg">
   * try {
   *   importer.load( ns, XmlReader.getDocumentFromString(s) );
   * }
   * catch (GrouperException eG) {
   *   // error importing
   * }
   * </pre>
   * @param   ns    Import using this <tt>Stem</tt> as the <i>root stem</i>.
   * @param   doc   Import this <tt>Document</tt>.
   * @throws  GrouperException
   * @throws  IllegalArgumentException if <tt>doc</tt> is null
   * @since   1.1.0
   */
  public void load(Stem ns, Document doc)
    throws  GrouperException,
            IllegalArgumentException
  {
    LOG.info("starting load at " + Quote.single(ns.getName()));
    this._load(ns, doc);
    LOG.info("finished load");
  } // public void load(ns, doc)

  /**
   * Update memberships and privileges but do not create missing stems or groups.
   * <pre class="eg">
   * try {
   *   importer.update( XmlReader.getDocumentFromString(s) );
   * }
   * catch (GrouperException eG) {
   *   // error updating
   * }
   * </pre>
   * @param   doc   Import this <tt>Document</tt>.
   * @throws  GrouperException
   * @throws  IllegalArgumentException if <tt>doc</tt> is null
   * @since   1.1.0
   */
  public void update(Document doc)
    throws  GrouperException,
            IllegalArgumentException
  {
    LOG.info("starting update");
    this._setUpdateOnly(true);
    this._load( StemFinder.findRootStem(this.s), doc );
    LOG.info("finished update");
  } // public void update(doc)

  //@since 1.4.0
  public void setIgnoreInternal(boolean ignoreInternal) {
	  this.ignoreInternal=ignoreInternal;
  }
  
  // PROTECTED INSTANCE METHODS //

  // @since   1.2.0
  public Properties internal_getOptions() {
    return (Properties) options.clone();
  } // protected Properties internal_getOptions()


  // PRIVATE CLASS METHODS //

  // @since   1.1.0
  private Subject _findSubject(String id, String idfr, String type) 
    throws  SubjectNotFoundException,
            SubjectNotUniqueException
  {
    NotNullOrEmptyValidator v = NotNullOrEmptyValidator.validate(id);
    if (v.isInvalid()) {
      if (type.equals("group")) {
        if (this._isRelativeImport(idfr)) {
          v = NotNullOrEmptyValidator.validate(this.importRoot);
          if (v.isValid()) {
            idfr = U.constructName( this.importRoot, idfr.substring(1) );
          }
          else {
            idfr = idfr.substring(1);
          }
        }
        else {
          LOG.warn("not absolutizing idfr: " + Quote.single(idfr));
        }
      }
      return this._getSubjectByIdentifier(idfr, type);
    } 
    return this._getSubjectById(id, type);
  } // private Subject _findSubject(id, idfr, type)

  // Assumes tag only occurs once and contains only text / CDATA.
  // If tag does not exist 'nullable' determines if an Exception is thrown.
  // @since   1.1.0
  private static String _getText(Element element) 
    throws  GrouperException
  {
    element.normalize();
    NodeList nl = element.getChildNodes();
    if (nl.getLength() != 1) {
      throw (new GrouperException("Cannot process " + element.getTagName() + " tag"));
    }
    Node n = nl.item(0);
    if (
      n.getNodeType() != Node.TEXT_NODE
      && n.getNodeType() != Node.CDATA_SECTION_NODE
    ) 
    {
      throw (new GrouperException("Cannot process " + element.getTagName() + " tag"));
    }
    return ((CharacterData) n).getData().trim();
  } // private static String _getText(element)

  // @since   1.1.0
  private static String _getUsage() {
    return  "Usage:"                                                                + GrouperConfig.NL
            + "args: -h,            Prints this message"                            + GrouperConfig.NL
            + "args: subjectIdentifier "             + GrouperConfig.NL
            + "      [-userAuditFilename <fileName>] [-userAuditOnly]"              + GrouperConfig.NL 
            + "      [(-id <id> | -name <name> | -list)]"                           + GrouperConfig.NL
            + "      [-ignoreInternal] [-noprompt] filename [properties]"           + GrouperConfig.NL
            + "e.g.  gsh -xmlimport -userAuditFilename f:/temp/prodAudit.xml GrouperSystem f:/temp/prod.xml"                 + GrouperConfig.NL
            +                                                                         GrouperConfig.NL
            + "  subjectIdentifier, Identifies a Subject 'who' will create a"       + GrouperConfig.NL
            + "                     GrouperSession"                                 + GrouperConfig.NL
            + "  -userAuditFilename,The file name where user audits should go"      + GrouperConfig.NL
            + "  -id,               The Uuid of a Stem, into which, data will be"   + GrouperConfig.NL
            + "                     imported"                                       + GrouperConfig.NL
            + "  -name,             The name of a Stem, into which, data will be"   + GrouperConfig.NL
            + "                     imported.  If no -id / -name is specified, "    + GrouperConfig.NL
            + "                     use=ROOT stem."                                 + GrouperConfig.NL
            + "  -list,             File contains a flat list of Stems or Groups"   + GrouperConfig.NL
            + "                     which may be updated. Missing Stems and Groups" + GrouperConfig.NL
            + "                     are not created"                                + GrouperConfig.NL
            
            + "  -ignoreInternal,   Do not attempt to import internal attributes"   + GrouperConfig.NL
            + "                     including Group/Stem uuids. Overrides property:"+ GrouperConfig.NL
            + "                     import.data.ignore-internal-attributes-and-uuids"+GrouperConfig.NL
            
            + "  -noprompt,         Do not prompt user to confirm the database that"+ GrouperConfig.NL
            + "                     will be updated"                                + GrouperConfig.NL
            
            + "  filename,          The file to import"                             + GrouperConfig.NL
            + "  properties,        The name of an optional Java properties file. " + GrouperConfig.NL
            + "                     Values specified in this properties file will " + GrouperConfig.NL
            + "                     override the default import behavior."          + GrouperConfig.NL
            ;
  } // private static String _getUsage()

  /**
   * @since   1.1.0
   * @param importer
   * @param rc
   * @throws GrouperException
   */
  public static void _handleArgs(final XmlImporter importer, final Properties rc) 
    throws  GrouperException {
    try {
      
      String userAuditFilename = rc.getProperty("userAuditFilename");
      if (!StringUtils.isBlank(userAuditFilename)) {
        
        new XmlUserAuditImport().readUserAudits(new File(userAuditFilename));
        
      }

      String userAuditOnly = rc.getProperty("userAuditOnly");
      if (StringUtils.equalsIgnoreCase("true", userAuditOnly)) {
        //we are done
        return;
      }

      importer.ignoreInternal = Boolean.parseBoolean( rc.getProperty(XmlArgs.RC_IGNORE) );
      GrouperSession.callbackGrouperSession(importer.s, new GrouperSessionHandler() {
  
        public Object callback(GrouperSession grouperSession)
            throws GrouperSessionException {
          try {
            Document doc = XmlReader.getDocumentFromFile( rc.getProperty(XmlArgs.RC_IFILE) );
            if (Boolean.parseBoolean( rc.getProperty(XmlArgs.RC_UPDATELIST) )) {
              importer.update(doc);
            } 
            else {
              if (rc.getProperty(XmlArgs.RC_UUID) == null && rc.getProperty(XmlArgs.RC_NAME) == null) {
                importer.load(doc);
              } 
              else {
                Stem    ns    = null;
                String  uuid  = rc.getProperty(XmlArgs.RC_UUID);
                String  name  = rc.getProperty(XmlArgs.RC_NAME);
                if      (uuid != null) {
                  try {
                    ns = StemFinder.findByUuid(importer.s, uuid, true);
                  } catch (StemNotFoundException e) {
                    throw new IllegalArgumentException(E.NO_STEM_UUID + Quote.single(uuid));
                  }
                } 
                else if (name != null) {
                  try {
                    ns = StemFinder.findByName(importer.s, name, true);
                  } catch (StemNotFoundException e) {
                    throw new IllegalArgumentException(E.NO_STEM_NAME + Quote.single(name));
                  }
                }
                if (ns == null) {
                  throw new IllegalArgumentException(E.NO_STEM);
                }
                importer.load(ns, doc);
              }
            } 
          } catch (GrouperException grouperException) {
            throw new GrouperSessionException(grouperException);
          }
          return null;
        }
        
      });
    } catch (GrouperSessionException grouperSessionException) {
      if (grouperSessionException.getCause() instanceof GrouperException) {
        throw (GrouperException)grouperSessionException.getCause();
      }
      throw grouperSessionException;
    }
  } // private static void _handleArgs(importer, rc);


  // PRIVATE INSTANCE METHODS //

  // @since   1.1.0
  private void _accumulateLists(Element e, String group) 
  {
    Collection  lists = this._getImmediateElements(e, "list");
    Iterator    it    = lists.iterator();
    Element     list;
    Map map;
    while (it.hasNext()) {
      list  = (Element) it.next();
      map   = new HashMap();
      map.put("group", group);
      map.put("list", list);
      membershipLists.add(map);
    }
  } // private void _accumulateLists(e, group)

  // @since   1.1.0
  private void _accumulatePrivs(Element e, String stem, String type)
    throws  GrouperException
  {
    Collection  privileges  = this._getImmediateElements(e, "privileges");
    Iterator    it          = privileges.iterator();
    Element     privilege;
    String      priv;
    Map         map;
    boolean     isGroup     = "access".equals(type);
    while (it.hasNext()) {
      privilege = (Element) it.next();
      priv      = privilege.getAttribute("type");
      map       = new HashMap();

      map.put(type, priv);
      map.put("privileges", privilege);
      if (isGroup) {
        map.put("group", stem);
        accessPrivLists.add(map);
      } else {
        map.put("stem", stem);
        this.namingPrivLists.add(map);
      }
    }
  } // private void _accumulatePrivs(e, stem, type)

  // @since   1.0
  private String _getAbsoluteName(String name, String stem) {
	 name=name.replaceAll("^\\*",".:");
    if (name != null && (name.startsWith("."))) {
      if (name.startsWith("." + Stem.ROOT_INT)) {
        name = stem + name.substring(1);
      } 
      else {
        while (name.startsWith(".." + Stem.ROOT_INT)) {
          name = name.substring(3);
          stem = stem.substring(0, stem.lastIndexOf(Stem.ROOT_INT));
        }
        name = U.constructName(stem, name);
      }
    }
    NotNullOrEmptyValidator v = NotNullOrEmptyValidator.validate(importRoot);
    if (
      v.isValid() && this.importedGroups.containsKey(importRoot + Stem.ROOT_INT + name)
    ) 
    {
      return importRoot + Stem.ROOT_INT + name;
    }
    return name;
  } // private String _getAbsoluteName(name, stem)

  // @since   1.1.0
  private String _getDataListImportMode() {
    return this.options.getProperty("import.data.lists", MODE_IGNORE);
  } // private String _getDataListImportMode()

  // @since   1.1.0
  private String _getDataPrivilegesImportMode() {
    return this.options.getProperty("import.data.privileges", MODE_IGNORE);
  } // private String _getDataPrivilegesImportMode()

  // Returns immediate child element with given name
  // TODO 2009-12-18 tz : includes all child elements
  // @since   1.0
  private Element _getImmediateElement(Element element, String name)
  { 
    NodeList nl = element.getElementsByTagName(name);
    if (nl.getLength() < 1) {
      return null;
    } 
    if (nl.getLength() > 1) {
      throw new IllegalArgumentException(E.ELEMENT_NOT_UNIQUE + name);
    }
    return (Element) nl.item(0);
  } // private Element _getImmediateElement(element, name)
  
  // Returns immediate child elements with given name
  // @since   1.0
  private Collection _getImmediateElements(Element element, String name)
  { 
    Collection<Element> elements = new Vector<Element>();
    if (element != null) {
      Element   child;
      NodeList  nl    = element.getElementsByTagName(name);
      for (int i = 0; i < nl.getLength(); i++) {
        child = (Element) nl.item(i);
        if (child.getParentNode().equals(element)) {
          elements.add(child);
        }
      }
    }
    return elements;
  } // private Collection _getImmediateElements(element, name)

  // @since   1.1.0
  private Properties _getImportOptionsFromXml() 
    throws  GrouperException
  {
    LOG.debug("Attempting to find importOptions in XML");
    Element rootE = doc.getDocumentElement();
    Element importOptionsE = this._getImmediateElement(rootE, "importOptions");
    if (importOptionsE == null) {
      LOG.debug("No importOptions tag in XML");
      return null;
    }
    LOG.debug("Found importOptions tag in XML - loading options");
    Collection options = this._getImmediateElements(importOptionsE, "options");
    Element optionE;
    Properties props = new Properties();
    Iterator it = options.iterator();
    while (it.hasNext()) {
      optionE = (Element) it.next();
      props.put(optionE.getAttribute("key"), XmlImporter._getText(optionE));
      LOG.debug("Loading " + optionE.getAttribute("key") + "="
          + XmlImporter._getText(optionE));
    }
    LOG.debug("Finished loading options from XML");

    return props;
  } // private Properties _getImportedOptionsFromXml()

  // @since   1.1.0
  private Collection _getInternalAttributes(Element e) {
    Set   attrs = new LinkedHashSet();
    List  l     = new ArrayList( this._getImmediateElements(e, "internalAttributes" ) );
    if (l.size() == 1) {
      attrs.addAll( this._getImmediateElements( (Element) l.get(0), "internalAttribute" ) );
    }
    return attrs;
  } // private Collection _getInternalAttributes(e)

  // @since   1.0
  private Subject _getSubjectById(String id, String type) 
    throws  SubjectNotFoundException,
            SubjectNotUniqueException
  {
    NotNullOrEmptyValidator v = NotNullOrEmptyValidator.validate(type);
    if (v.isInvalid()) {
      return SubjectFinder.findById(id, true);
    }
    return SubjectFinder.findById(id, true);
  } // private Subject _getSubjectById(id, type)

  // @since   1.0
  private Subject _getSubjectByIdentifier(String identifier, String type)
    throws  SubjectNotFoundException,
            SubjectNotUniqueException
  {
    NotNullOrEmptyValidator v = NotNullOrEmptyValidator.validate(type);
    if (v.isInvalid()) {
      return SubjectFinder.findByIdentifier(identifier, true);
    }
    return SubjectFinder.findByIdentifier(identifier, true);
  } // private Subject _getSubjectByIdentifier(identifier, type)

  // @since   1.1.0
  private boolean _isApplyNewGroupTypesEnabled() {
    return XmlUtils.internal_getBooleanOption(this.options, "import.data.apply-new-group-types");
  } // private boolean _isApplyNewGroupTypesEnabled()
  
  // @since   1.1.0
  private boolean _isMetadataGroupTypeImportEnabled() {
    return XmlUtils.internal_getBooleanOption(this.options, "import.metadata.group-types");
  } // private boolean _isMetadataGroupTypeImportEnabled()

  // @since   1.1.0
  private boolean _isMetadataGroupTypeAttributeImportEnabled() {
    return XmlUtils.internal_getBooleanOption(this.options, "import.metadata.group-type-attributes");
  } // private boolean _isMetadataGroupTypeAttributeImportEnabled()

  // @since   1.1.0
  private boolean _isRelativeImport(String idfr) {
    return (
          idfr.startsWith(  XmlUtils.SPECIAL_STAR )
      &&  !idfr.endsWith(   XmlUtils.SPECIAL_STAR )
    );
  } // private boolean _isRelativeImport(idfr)
 
  // @since   1.1.0
  private boolean _isSubjectElementImmediate(Element el) {
    return Boolean.valueOf( el.getAttribute("immediate") ).booleanValue();
  }

  //@since   1.4.0
  private boolean _isIgnoreInternalAttributes() {
    return (this.ignoreInternal || 
    	XmlUtils.internal_getBooleanOption(this.options, "import.data.ignore-internal-attributes-and-uuids"));
  } // private boolean _isIgnoreInternalAttributes()
  
  
  // @since   1.1.0
  private boolean _isUpdatingAttributes() {
    return XmlUtils.internal_getBooleanOption(this.options, "import.data.update-attributes");
  } // private boolean _isUpdatingAttributes()

//@since   1.3.1
  private boolean _isFailOnUnresolvableSubjectEnabled() {
    return XmlUtils.internal_getBooleanOption(this.options, "import.data.fail-on-unresolvable-subject");
  } // private boolean _isFailOnUnresolvableSubjectEnabled()

  // @since   1.1.0
  private void _load(final Stem ns, final Document doc) 
    throws  GrouperException,
            IllegalArgumentException
  {
    
    try {
      GrouperSession.callbackGrouperSession(this.s, new GrouperSessionHandler() {
  
        public Object callback(GrouperSession grouperSession)
            throws GrouperSessionException {
          try {
            XmlImporter.this._setDocument(doc);
            try {
              XmlImporter.this.importRoot = ns.getName();
              if (ns.isRootStem()) {
                XmlImporter.this.importRoot = GrouperConfig.EMPTY_STRING;
              }
              XmlImporter.this._processProperties();
              Element root = XmlImporter.this._getDocument().getDocumentElement();

              XmlImporter.this._processMetadata(XmlImporter.this._getImmediateElement(root, "metadata"));
              XmlImporter.this._process( XmlImporter.this._getImmediateElement(root, "data"), XmlImporter.this.importRoot );
              XmlImporter.this._processMembershipLists();
              XmlImporter.this._processNamingPrivLists();
              XmlImporter.this._processAccessPrivLists();
            }
            catch (AttributeNotFoundException eANF)     {
              throw new GrouperException(eANF.getMessage(), eANF);
            }
            catch (GrantPrivilegeException eGP)         {
              throw new GrouperException(eGP.getMessage(), eGP);
            }
            catch (GroupAddException eGA)               {
              throw new GrouperException(eGA.getMessage(), eGA);
            }
            catch (GrouperDAOException eDAO)             {
              throw new GrouperException( eDAO.getMessage(), eDAO );
            }
            catch (GroupModifyException eGM)            {
              throw new GrouperException(eGM.getMessage(), eGM);
            }
            catch (GroupNotFoundException eGNF)         {
              throw new GrouperException(eGNF.getMessage(), eGNF);
            }
            catch (InsufficientPrivilegeException eIP)  {
              throw new GrouperException(eIP.getMessage(), eIP);
            }
            catch (MemberAddException eMA)              {
              throw new GrouperException(eMA.getMessage(), eMA);
            }
            catch (MemberDeleteException eMD)           {
              throw new GrouperException(eMD.getMessage(), eMD);
            }
            catch (RevokePrivilegeException eRP)        {
              throw new GrouperException(eRP.getMessage(), eRP);
            }
            catch (SchemaException eS)                  {
              throw new GrouperException(eS.getMessage(), eS);
            }
            catch (StemAddException eNSA)               {
              throw new GrouperException(eNSA.getMessage(), eNSA);
            }
            catch (StemModifyException eNSM)            {
              throw new GrouperException(eNSM.getMessage(), eNSM);
            }
            catch (StemNotFoundException eNSNF)         {
              throw new GrouperException(eNSNF.getMessage(), eNSNF);
            }
            catch (SubjectNotFoundException eSNF)       {
              throw new GrouperException(eSNF.getMessage(), eSNF);
            }
            catch (SubjectNotUniqueException eSNU)      {
              throw new GrouperException( eSNU.getMessage(), eSNU );
            }
          } catch (GrouperException grouperException) {
            throw new GrouperSessionException(grouperException);
          }
          return null;
        }
        
      });
    } catch (GrouperSessionException grouperSessionException) {
      if (grouperSessionException.getCause() instanceof GrouperException) {
        throw (GrouperException)grouperSessionException.getCause();
      }
      throw grouperSessionException;
    }

    
  } // private void _load(ns, doc)

  // @since   1.0
  private boolean _optionTrue(String key) {
    NotNullOrEmptyValidator v = NotNullOrEmptyValidator.validate(key);
    if (v.isInvalid()) {
      options.setProperty(key, "false");
      return false;
    }
    return "true".equals( options.getProperty(key) );
  } // private boolean _optionTrue(key)

  // @since   1.1.0
  private Date _parseTime(String s) 
    throws  ParseException
  {
    Date d = null;
    try {
      // First check to see if we are using the new export date format (ms since epoch)
      d = new Date( Long.parseLong(s) );
    }
    catch (NumberFormatException eNF) {
      // Guess not.  Try to parse the old format and hope for the best.
      DateFormat df = new SimpleDateFormat("E MMM dd HH:mm:ss Z yyyy");
      d = df.parse(s);
    }
    return d;
  } // private Date _parseTime(s)

  // For each stem list and process any child stems. List and process any child groups.
  // @since   1.1.0
  private void _process(Element e, String stem) 
    throws  AttributeNotFoundException,
            GroupAddException,
            GrouperDAOException,
            GrouperException,
            GroupModifyException,
            GroupNotFoundException,
            InsufficientPrivilegeException,
            SchemaException,
            StemAddException,
            StemModifyException,
            StemNotFoundException
  {
    if (e != null) {
      this._processPaths(e, stem);
      this._processGroups(e, stem);
    }
  } // private void _process(e, stem)

  // @since   1.1.0
  private void _processAccessPrivLists() 
    throws  GrantPrivilegeException,
            GroupNotFoundException,
            InsufficientPrivilegeException,
            RevokePrivilegeException,
            SchemaException,
            SubjectNotFoundException,
            SubjectNotUniqueException
  {
    if (this.accessPrivLists != null) {
      Iterator it = this.accessPrivLists.iterator();
      while (it.hasNext()) {
        this._processAccessPrivList( (Map) it.next() );
      }
    }
    this.accessPrivLists = null;
  } // private void _processAccessPrivLists()

  // @since   1.1.0
  private void _processAccessPrivList(Map map) 
    throws  GrantPrivilegeException,
            GroupNotFoundException,
            InsufficientPrivilegeException,
            RevokePrivilegeException,
            SchemaException,
            SubjectNotFoundException,
            SubjectNotUniqueException
  {
    String  groupName = (String) map.get("group");
    Element privs     = (Element) map.get("privileges");
    if (this._getDataPrivilegesImportMode().equals(MODE_IGNORE)) {
      return; // Ignore privileges
    }
    try {
      Group     g = GroupFinder.findByName(s, groupName, true);
      Privilege p = Privilege.getInstance( privs.getAttribute("type") );
      if (this._getDataPrivilegesImportMode().equals(MODE_REPLACE)) {
        g.revokePriv(p);
      }
      Iterator it       = this._getImmediateElements(privs, "subject").iterator();
      while (it.hasNext()) {
        this._processAccessPrivListGrantPriv( g, p, (Element) it.next() );
      }
    }
    catch (GroupNotFoundException eGNF) {
      if (!this._getUpdateOnly()) {
        throw eGNF; // if updating we can ignore, if loading we cannot
      }
    }
  } // private void _processAccessPrivList(map)

  // @since   1.1.0
  private void _processAccessPrivListGrantPriv(Group g, Privilege p, Element el)
    throws  GrantPrivilegeException,
            InsufficientPrivilegeException,
            SchemaException,
            SubjectNotFoundException,
            SubjectNotUniqueException
  {
    if ( this._isSubjectElementImmediate(el) ) {
      Subject subj = null;
      String attributeId = el.getAttribute("id");
      String attributeIdentifier = el.getAttribute("identifier");
      String attributeType = el.getAttribute("type");
      try {
        subj=this._findSubject(attributeId, attributeIdentifier, attributeType);
      }catch(Exception e) {
        String errorMessage = "Could not grant " + p.getName() + " to " + g.getName() + " for subject id=" + attributeId;
    	  if(_isFailOnUnresolvableSubjectEnabled()) {
    	    
    	    if (e instanceof SubjectNotFoundException) {
    	      throw (SubjectNotFoundException)e;
    	    }
          if (e instanceof SubjectNotUniqueException) {
            throw (SubjectNotUniqueException)e;
          }
          if (e instanceof RuntimeException) {
            throw (RuntimeException)e;
          }
    		  throw new RuntimeException(errorMessage, e);
    	  }
        LOG.error(errorMessage,e);
    		  return;
      }
      if ( !XmlUtils.internal_hasImmediatePrivilege( subj, g, p.getName() ) ) {
        g.grantPriv(subj, p, false);
      }
    }
  } // private void _processAccessPrivListGrantPriv(g, p, el)

  // @since   1.1.0
  private void _processAttributes(Element e, String group) 
    throws  AttributeNotFoundException,
            GroupModifyException,
            GroupNotFoundException,
            InsufficientPrivilegeException,
            SchemaException
  {
    Element   elTypes = this._getImmediateElement(e, "groupTypes");
    if (elTypes == null) {
      return;
    }
    Group     g       = GroupFinder.findByName(s, group, true);
    Iterator  it      = this._getImmediateElements(elTypes, "groupType").iterator();
    while (it.hasNext()) {
      this._processAttributesHandleType( g, (Element) it.next() );
    }
  } // private void _processAttributes(e, stem) 

  // @since   1.1.10
  private void _processAttributesHandleType(Group g, Element e)
    throws  AttributeNotFoundException,
            GroupModifyException,
            InsufficientPrivilegeException
  {
    String name = e.getAttribute("name");
    if (!name.equals("base")) {
      try {
        GroupType gt = GroupTypeFinder.find(name, true);
        if (!g.hasType(gt)) {
          if (this._optionTrue("import.data.apply-new-group-types")) {
            g.addType(gt);
          }
        }
        this._processAttributesHandleAttributes(g, gt, e);
      }
      catch (SchemaException eS) {
        LOG.error(eS.getMessage());
      }
    }
  } // privae void _processAttributesHandleType(g, e)

  // @since   1.1.0
  private void _processAttributesHandleAttributes(Group g, GroupType gt, Element e) 
    throws  AttributeNotFoundException,
            GroupModifyException,
            InsufficientPrivilegeException,
            SchemaException
  {
    Element   elAttr;
    String    name, orig, val;
    Iterator  it      = this._getImmediateElements(e, "attribute").iterator();
    while (it.hasNext()) {
      elAttr  = (Element) it.next();
      name    = elAttr.getAttribute("name");
      AttributeDefName f = GrouperDAOFactory.getFactory().getAttributeDefName().findLegacyAttributeByName(name, true);
      
      AttributeAssign groupTypeAssignment = g.internal_getGroupTypeAssignments().get(gt.getName());
      if (groupTypeAssignment == null) {
        throw new AttributeNotFoundException("Group " + g.getName() + " is not assigned the group type: " + gt.getName());
      }
      
      try {
        groupTypeAssignment.getAttributeDelegate().assertCanUpdateAttributeDefName(f);
      } catch (InsufficientPrivilegeException ex) {
        LOG.debug("cannot write (" + name + ") on (" + g.getName() + ")");
        continue;
      } catch (AttributeNotFoundException ex) {
        LOG.debug("cannot write (" + name + ") on (" + g.getName() + ")");
        continue;
      }
      
      orig                          = g.getAttributeValue(name, false, false); 
      try {
    	  val                           = ( (Text) elAttr.getFirstChild() ).getData();
      }catch(NullPointerException npe) {
    	  val=null;
      }
      NotNullOrEmptyValidator vOrig = NotNullOrEmptyValidator.validate(orig);
      NotNullOrEmptyValidator vVal  = NotNullOrEmptyValidator.validate(val);
      if ( vVal.isValid() && !val.equals(orig) && ( vOrig.isInvalid() || this._isUpdatingAttributes() ) ) {
        g.setAttribute(name, val);
      } 
    }
  } // private void _processAttributesHandleAttributes()

  // @since   1.1.0
  private void _processComposite(Element el, Group g)
    throws  GrouperException,
            InsufficientPrivilegeException,
            MemberAddException
  {
    if (g.hasComposite()) { 
      LOG.warn(g.getName() + " already has composite - skipping");
      return;
    }
    el.normalize(); // i'm going to assume this is import.  so be it.
    Element[]     elements    = new Element[3];
    NodeList      nl          = el.getChildNodes();
    int           elCount     = -1;
    Node          node;
    for (int i = 0; i < nl.getLength(); i++) {
      node = nl.item(i);
      if (node instanceof Element) {
        elCount++;
        if (elCount > 2) {
          throw new IllegalStateException(
            "Too many tags in <composite>. Expect <groupRef><compositeType><groupRef>"
          );
        }
        elements[elCount] = (Element) node;
      }
    }
    try {
      g.addCompositeMember(
        this._processCompositeType( elements[1] ),
        this._processGroupRef( elements[0], g.getParentStem().getName() ),
        this._processGroupRef( elements[2], g.getParentStem().getName() )
      );
    } 
    catch (GroupNotFoundException eGNF) {
      LOG.error("error processing composite for " + Quote.single(g.getName()) + ": " + eGNF.getMessage());
      return;
    }
  } // private void _processComposite(composite, group)

  // @since   1.1.0
  private CompositeType _processCompositeType(Element typeE) 
    throws  GrouperException
  {
    String tag = typeE.getTagName();
    if (!tag.equals("compositeType")) {
      throw new IllegalStateException("Expected tag: <compositeType> but found <" + tag + ">");
    }
    String name = XmlImporter._getText(typeE);
    CompositeType ctype = CompositeType.valueOfIgnoreCase(name);
    if (ctype == null) {
      throw new IllegalStateException("could not resolve composite type: " + Quote.single(name));
    }
    return ctype;
  }  // private CompositeType _processCompositeType(typeE)

  // @since   1.1.0
  private void _processGroup(Element e, String stem) 
    throws  AttributeNotFoundException,
            GroupAddException,
            GrouperDAOException,
            GrouperException,
            GroupModifyException,
            GroupNotFoundException,
            InsufficientPrivilegeException,
            SchemaException,
            StemNotFoundException
  {
    String newGroup = U.constructName( stem, e.getAttribute(GrouperConfig.ATTRIBUTE_EXTENSION) );
    try {
      this._processGroupUpdate(e, newGroup);  // Try and update
    } 
    catch (GroupNotFoundException eGNF) {
      this._processGroupCreate(e, stem);      // Otherwise create
    }
    this._processAttributes(e, newGroup);
    this._accumulateLists(e, newGroup);
    this._accumulatePrivs(e, newGroup, "access");
  } // private void _processGroup(e, stem)

  // @since   1.1.0
  private void _processGroupCreate(Element e, String stem) 
    throws  GroupAddException,
            GrouperDAOException,
            GroupModifyException,
            InsufficientPrivilegeException,
            StemNotFoundException
  {
    if (this._getUpdateOnly()) {
      return; // do not create groups when we are only updating
    }
    Stem  parent  = StemFinder.findByName(this.s, stem, true);
    String id=null;
    if(!_isIgnoreInternalAttributes()) {
    	id=e.getAttribute("id");
    }
    Group child   = parent.internal_addChildGroup(
      e.getAttribute(GrouperConfig.ATTRIBUTE_EXTENSION),
      e.getAttribute(GrouperConfig.ATTRIBUTE_DISPLAY_EXTENSION),
      id
    );
    String desc = this._getDescription(e);
    NotNullOrEmptyValidator v     = NotNullOrEmptyValidator.validate(desc);
    if (v.isValid()) {
      child.setDescription(desc);
      child.store();
    }
    if(!_isIgnoreInternalAttributes()) {
    	this._setInternalAttributes(child, e);
    }
    this.importedGroups.put( child.getName(), SPECIAL_C );
  } // private void _processGroupCreate(e, stem)

  // @since   1.1.0
  private Group _processGroupRef(Element groupE, String stem) 
    throws  GroupNotFoundException
  {
    String tagName = groupE.getTagName();
    if (!"groupRef".equals(tagName)) {
      throw new IllegalStateException("Expected tag: <groupRef> but found <" + tagName + ">");
    }
    String                  name  = groupE.getAttribute(GrouperConfig.ATTRIBUTE_NAME);
    NotNullOrEmptyValidator v     = NotNullOrEmptyValidator.validate(name);
    if (v.isInvalid()) {
      throw new IllegalStateException("Expected 'name' atribute for <groupRef>");
    }
    return GroupFinder.findByName( s, this._getAbsoluteName(name, stem), true );
  } // private Group _processGroupRef(groupE, stem)

  // @since   1.1.0
  private void _processGroups(Element e, String stem) 
    throws  AttributeNotFoundException,
            GroupAddException,
            GrouperDAOException,
            GrouperException,
            GroupModifyException,
            GroupNotFoundException,
            InsufficientPrivilegeException,
            SchemaException,
            StemAddException,
            StemModifyException,
            StemNotFoundException
  {
    Collection  groups  = this._getImmediateElements(e, "group");
    Iterator    it      = groups.iterator();
    while (it.hasNext()) {
      this._processGroup( (Element) it.next(), stem );
    }
  } // private void _processGroups(e, stem)

  // @since   1.1.0
  private void _processGroupUpdate(Element e, String newGroup) 
    throws  GroupModifyException,
            GroupNotFoundException,
            InsufficientPrivilegeException
  {
    // We need to keep this outside the conditional so that a
    // GroupNotFoundException can be thrown if the stem does not exist.  That
    // will trigger the creation of the group.
    Group g = GroupFinder.findByName(this.s, newGroup, true);
    if (this._isUpdatingAttributes()) {
      String                  dExtn = e.getAttribute(GrouperConfig.ATTRIBUTE_DISPLAY_EXTENSION);
      NotNullOrEmptyValidator v     = NotNullOrEmptyValidator.validate(dExtn);
      if ( v.isValid() && !dExtn.equals( g.getDisplayExtension() ) ) {
        g.setDisplayExtension(dExtn);
        g.store();
      }
      String desc = this._getDescription(e);      
      v           = NotNullOrEmptyValidator.validate(desc);
      if ( v.isValid() && !desc.equals( g.getDescription() ) ) {
        g.setDescription(desc);
        g.store();
      }
    }
    this.importedGroups.put( g.getName(), SPECIAL_E );
  } // private void _processGroupUpdate(e, newGroup)

  // @since   1.1.0
  private void _processMembershipLists() 
    throws  GrouperException,
            GroupModifyException,
            GroupNotFoundException,
            InsufficientPrivilegeException,
            MemberAddException,
            MemberDeleteException,
            SchemaException,
            SubjectNotFoundException,
            SubjectNotUniqueException
  {
    if (this.membershipLists != null) {
      Iterator it = this.membershipLists.iterator();
      while (it.hasNext()) {
        this._processMembershipList( (Map) it.next() );
      }
    }
    this.membershipLists = null;
  } // private void _processMembershipLists()

  // @since   1.1.0
  private void _processMembershipList(Map map) 
    throws  GrouperException,
            GroupModifyException,
            GroupNotFoundException,
            InsufficientPrivilegeException,
            MemberAddException,
            MemberDeleteException,
            SchemaException,
            SubjectNotFoundException,
            SubjectNotUniqueException
  {
    Element list      = (Element) map.get("list");
    String  groupName = (String) map.get("group");
    if (this._getDataListImportMode().equals(MODE_IGNORE)) {
      return; // Ignore lists
    }
    try {
      Group   g = GroupFinder.findByName(s, groupName, true);
      Field   f = FieldFinder.find( list.getAttribute("field"), true );
      if (!f.getType().equals(FieldType.LIST)) {
        throw new SchemaException("field is not a list: " + f.getName());
      }
      
      if (!f.getUuid().equals(Group.getDefaultList().getUuid())) {
        this._processMembershipListAddGroupType(g, f.getGroupType(true));
      }
      if (!g.canWriteField(f)) {
        return;  // We can't write to the field so don't even bother trying
      }
      if (!this._processMembershipListHandleImportMode(g, f, list)) {
        return; // Stop processing as we've done everything already
      }
      Iterator it = this._getImmediateElements(list, "subject").iterator();
      while (it.hasNext()) {
        this._processMembershipListAddMember( g, f, (Element) it.next() );
      }
    }
    catch (GroupNotFoundException eGNF) {
      if (!this._getUpdateOnly()) {
        throw eGNF; // if updating we can ignore, if loading we cannot
      }
    }
  } // private void _processMembershipList()

  // @since   1.1.0
  private void _processMembershipListAddGroupType(Group g, GroupType gt) 
    throws  GroupModifyException,
            InsufficientPrivilegeException,
            SchemaException
  {
    if ( !g.hasType(gt) && this._isApplyNewGroupTypesEnabled() ) {
      g.addType(gt); 
    }
  } // private void _processMembershipListAddGroupType(g, gt)

  // @since   1.1.0
  private void _processMembershipListAddMember(Group g, Field f, Element el) 
    throws  InsufficientPrivilegeException,
            MemberAddException,
            SchemaException,
            SubjectNotFoundException,
            SubjectNotUniqueException
  {
    if ( this._isSubjectElementImmediate(el) ) {
      Subject subj = null;
      try {
	      subj=this._findSubject( 
        el.getAttribute("id"), el.getAttribute("identifier"), el.getAttribute("type") 
      );
      }catch(Exception e) {
        String errorMessage = "Could not add member to field " + f.getName() + " of " + g.getName() + " for subject id=" + el.getAttribute("id");
    	  if(_isFailOnUnresolvableSubjectEnabled()) {
          
          if (e instanceof SubjectNotFoundException) {
            throw (SubjectNotFoundException)e;
          }
          if (e instanceof SubjectNotUniqueException) {
            throw (SubjectNotUniqueException)e;
          }
          if (e instanceof RuntimeException) {
            throw (RuntimeException)e;
          }
          throw new RuntimeException(errorMessage, e);
        }
        LOG.error(errorMessage,e);
    		  return;
    	  }
     
      if ( !g.hasImmediateMember(subj, f) ) {
        g.addMember(subj, f);
      }
    }
  } // private void _processMembershipListAddMember(g, f, el)

  // @since   1.1.0
  private boolean _processMembershipListHandleImportMode(Group g, Field f, Element list) 
    throws  GrouperException,
            InsufficientPrivilegeException,
            MemberAddException,
            MemberDeleteException,
            SchemaException,
            SubjectNotFoundException
  {
    // TODO 20070321 this needs more refactoring
    //      So this handles elminating current members if in replace mode and
    //      then adding a composite mship if that's our thing?  Why are they
    //      combined?
    boolean rv            = true; // If true continue processing 
    boolean hasComposite  = g.hasComposite();
    boolean hasMembers    = false;
    if (!hasComposite && g.getImmediateMembers().size() > 0) {
      hasMembers = true;
    }
    Element compE = this._getImmediateElement(list, "composite");

    if (this._getDataListImportMode().equals(MODE_REPLACE)) {
      if (hasComposite) {
        g.deleteCompositeMember();
      } 
      else {
        Iterator it = g.getImmediateMembers(f).iterator();
        while (it.hasNext()) {
          Member m = (Member) it.next();
          Subject subj = new LazySubject(m);
          g.deleteMember(subj, f);
          //g.deleteMember( ( (Member) it.next() ).getSubject() );
        }
      }
    }
    if (compE != null && ( !this._getDataListImportMode().equals(MODE_ADD) || hasMembers) ) {
      this._processComposite(compE, g);
      rv = false; // Omit remaining processing
    }
    if (compE != null && hasMembers) {
      LOG.warn("Cannot add composite membership to group that already has members: " + Quote.single(g.getName()));
      rv = false; // Omit remaining processing
    }
    return rv;
  } // private boolean _processMembershipListHandleImportMode(g, f, list)

  // @since   1.1.0
  private void _processMetadata(Element el) 
    throws  InsufficientPrivilegeException,
            SchemaException
  {
    if ( el == null || !this._isMetadataGroupTypeImportEnabled() ) {
      return;
    }
    Iterator it = this._getImmediateElements(
      this._getImmediateElement(el, "groupTypesMetaData"), "groupTypeDef"
    ).iterator();
    while (it.hasNext()) {
      this._processMetadataGroupType( (Element) it.next() );  
    }
  } // private void _processMetadata(e)

  // @since   1.1.0
  private void _processMetadataField(GroupType gt, boolean isNew, Element el) 
    throws  InsufficientPrivilegeException,
            SchemaException
  {
    if (isNew || this._isMetadataGroupTypeAttributeImportEnabled()) {
      // if a new group type or we have enabled group type attr importing // continue
      String    fName = el.getAttribute("name");

      //these were moved from attributes to other things
      if (StringUtils.equals(gt.getName(), "base") &&
          (
          StringUtils.equals("name", fName)
          || StringUtils.equals("description", fName)
          || StringUtils.equals("extension", fName)
          || StringUtils.equals("displayExtension", fName)
          || StringUtils.equals("displayName", fName)
          )) {
        return;
      }
      
      String    fType = el.getAttribute("type");
      
      if (fType.equals(FieldType.LIST.toString())) {
        Privilege read  = Privilege.getInstance( el.getAttribute("readPriv")  );
        Privilege write = Privilege.getInstance( el.getAttribute("writePriv") );
        try {
          FieldFinder.find(fName, true); // already exists
        } 
        catch (SchemaException eS) {
          gt.addList(s, fName, read, write);
        }
      } else if (fType.equals("attribute")) {
        gt.addAttribute(s, fName, false);
      } else {
        // previous code was ignoring this case..
      }
    }
  } // private void _processMetadataField(gt, isNew, el)

  // @since   1.1.0
  private void _processMetadataGroupType(Element el) 
    throws  InsufficientPrivilegeException,
            SchemaException
  {
    boolean   isNew   = false;
    String    gtName  = el.getAttribute("name");
    GroupType gt      = null;
    try {
      gt = GroupTypeFinder.find(gtName, true);
    } 
    catch (SchemaException ex) {
      gt    = GroupType.createType(s, gtName);
      isNew = true;
    }
    Iterator it = this._getImmediateElements(el, "field").iterator();
    while (it.hasNext()) {
      this._processMetadataField( gt, isNew, (Element) it.next() );
    }
  } // private void _processMetadataGroupType(el)

  // @since   1.1.0
  private void _processNamingPrivLists() 
    throws  GrantPrivilegeException,
            GroupModifyException,
            InsufficientPrivilegeException,
            RevokePrivilegeException,
            SchemaException,
            StemNotFoundException,
            SubjectNotFoundException,
            SubjectNotUniqueException
  {
    if (this.namingPrivLists != null) {
      Iterator it = this.namingPrivLists.iterator();
      while (it.hasNext()) {
        this._processNamingPrivList( (Map) it.next() );
      }
    }
    this.namingPrivLists = null;
  } // private void _processNamingPrivLists()

  // @since   1.1.0
  private void _processNamingPrivList(Map map) 
    throws  GrantPrivilegeException,
            InsufficientPrivilegeException,
            RevokePrivilegeException,
            SchemaException,
            StemNotFoundException,
            SubjectNotFoundException,
            SubjectNotUniqueException
  {
    String    stemName  = (String) map.get("stem");
    Element   privs     = (Element) map.get("privileges");
    if (this._getDataPrivilegesImportMode().equals(MODE_IGNORE)) {
      return; // Ignore privileges
    }
    try {
      Stem      ns        = StemFinder.findByName(s, stemName, true);
      Privilege p         = Privilege.getInstance( privs.getAttribute("type") );
      if (this._getDataPrivilegesImportMode().equals(MODE_REPLACE)) {
        ns.revokePriv(p);
      }
      Iterator  it        = this._getImmediateElements(privs, "subject").iterator();
      while (it.hasNext()) {
        this._processNamingPrivListGrantPriv( ns, p, (Element) it.next() );
      }
    }
    catch (StemNotFoundException eNSNF) {
      if (!this._getUpdateOnly()) {
        throw eNSNF; // if updating we can ignore, if loading we cannot
      }
    }
  } // private void _processNamingPrivList(map)

  // @since   1.1.0
  private void _processNamingPrivListGrantPriv(Stem ns, Privilege p, Element el)
    throws  GrantPrivilegeException,
            InsufficientPrivilegeException,
            SchemaException,
            SubjectNotFoundException,
            SubjectNotUniqueException
  {
    if ( this._isSubjectElementImmediate(el) ) {
      Subject subj = null;
      try {
	      subj=this._findSubject( 
        el.getAttribute("id"), el.getAttribute("identifier"), el.getAttribute("type") 
      );
      }catch(Exception e) {
        String errorMessage = "Could not grant " + p.getName() + " to " + ns.getName() + " for subject id=" + el.getAttribute("id");
    	  if(_isFailOnUnresolvableSubjectEnabled()) {
          
          if (e instanceof SubjectNotFoundException) {
            throw (SubjectNotFoundException)e;
          }
          if (e instanceof SubjectNotUniqueException) {
            throw (SubjectNotUniqueException)e;
          }
          if (e instanceof RuntimeException) {
            throw (RuntimeException)e;
          }
          throw new RuntimeException(errorMessage, e);
        }
        LOG.error(errorMessage,e);
    		  return;

      }
      
      if ( !XmlUtils.internal_hasImmediatePrivilege( subj, ns, p.getName() ) ) {
        ns.grantPriv(subj, p, false);
      }
    }
  } // private void _processNamingPrivListGrantPriv(ns, p, el)

  // @since   1.1.0
  private void _processPath(Element e, String stem) 
    throws  AttributeNotFoundException,
            GroupAddException,
            GrouperDAOException,
            GrouperException,
            GroupModifyException,
            GroupNotFoundException,
            InsufficientPrivilegeException,
            SchemaException,
            StemAddException,
            StemModifyException,
            StemNotFoundException
  {
    String newStem = U.constructName( stem, e.getAttribute(GrouperConfig.ATTRIBUTE_EXTENSION) );
    try {
      this._processPathUpdate(e, newStem);  // Try and update
    } 
    catch (StemNotFoundException eNSNF) {
      this._processPathCreate(e, stem);     // Otherwise create
    }
    this._accumulatePrivs(e, newStem, "naming");
    this._process(e, newStem); // And now handle the child
  } // private void _processPath(e, stem)

  // @since   1.1.0
  private void _processPathCreate(Element e, String stem) 
    throws  GrouperDAOException,
            InsufficientPrivilegeException,
            StemAddException,
            StemModifyException,
            StemNotFoundException
  {
    if (this._getUpdateOnly()) {
      return; // do not create stems when we are only updating
    }
    Stem parent = null;
    if (stem.equals(GrouperConfig.EMPTY_STRING)) {
      parent = StemFinder.findRootStem(this.s);
    }
    else {
      parent = StemFinder.findByName(this.s, stem, true);
    }
    String id = null;
    if(!_isIgnoreInternalAttributes()) {
    	id=e.getAttribute("id");
    }
    Stem child = parent.internal_addChildStem(
      e.getAttribute(GrouperConfig.ATTRIBUTE_EXTENSION),
      e.getAttribute(GrouperConfig.ATTRIBUTE_DISPLAY_EXTENSION),
      id
    );
    String                  desc  = this._getDescription(e);
    NotNullOrEmptyValidator v     = NotNullOrEmptyValidator.validate(desc);
    if (v.isValid()) {
      child.setDescription(desc);
      child.store();
    }
    if(!_isIgnoreInternalAttributes()) {
    	this._setInternalAttributes(child, e);
    }
  } // private void _processPathCreate(e, stem)

  // @since   1.1.0
  private void _processPathUpdate(Element e, String newStem) 
    throws  InsufficientPrivilegeException,
            StemModifyException,
            StemNotFoundException
  {
    // We need to keep this outside the conditional so that a
    // StemNotFoundException can be thrown if the stem does not exist.  That
    // will trigger the creation of the stem.
    Stem ns = StemFinder.findByName(this.s, newStem, true);
    if (this._isUpdatingAttributes()) {
      String                  dExtn = e.getAttribute(GrouperConfig.ATTRIBUTE_DISPLAY_EXTENSION);
      NotNullOrEmptyValidator v     = NotNullOrEmptyValidator.validate(dExtn);
      if ( v.isValid() && !dExtn.equals( ns.getDisplayExtension() ) ) {
        ns.setDisplayExtension(dExtn);
        ns.store();
      }
      String desc = this._getDescription(e);
      v           = NotNullOrEmptyValidator.validate(desc);
      if ( v.isValid() && !desc.equals( ns.getDescription() ) ) {
        ns.setDescription(desc);
        ns.store();
      }
    }
  } // private void _processPathUpdate(e, newStem)

  // @since   1.1.0
  private void _processPaths(Element e, String stem) 
    throws  AttributeNotFoundException,
            GroupAddException,
            GrouperDAOException,
            GrouperException,
            GroupModifyException,
            GroupNotFoundException,
            InsufficientPrivilegeException,
            SchemaException,
            StemAddException,
            StemModifyException,
            StemNotFoundException
  {
    Iterator it = this._getImmediateElements(e, "stem").iterator();
    while (it.hasNext()) {
      this._processPath( (Element) it.next(), stem );
    }
  } // private void _processPaths(e, stem)

  // @since   1.1.0
  private void _processProperties() 
    throws  GrouperException
  {
    Properties xmlOptions = this._getImportOptionsFromXml();
    if (xmlOptions == null && this.options.isEmpty()) {
      throw new IllegalStateException("No options have been set");
    }
    if (xmlOptions == null) {
      return;
    }
    xmlOptions.putAll(this.options);  // add current to xml
    this.options = xmlOptions;        // replace current with merged options
  } // private void _processProperties()

  // @since   1.2.0
  private void _setCreateSubject(Group g, Element e) {
    Element elSubj = this._getImmediateElement(e, "subject");
    g.setCreatorUuid(
      MemberFinder.internal_findOrCreateBySubject(
        elSubj.getAttribute("id"), elSubj.getAttribute("source"), elSubj.getAttribute("type")
      ).getUuid()
    );
  } // private void setCreateSubject(g, e)
  
  // @since   1.2.0
  private void _setCreateSubject(Stem ns, Element e) {
    Element elSubj = this._getImmediateElement(e, "subject");
    ns.setCreatorUuid(
      MemberFinder.internal_findOrCreateBySubject(
        elSubj.getAttribute("id"), elSubj.getAttribute("source"), elSubj.getAttribute("type")
      ).getUuid()
    );
  } // private void setCreateSubject(ns, e)

  // @since   1.2.0
  private boolean _setCreateTime(Group g, Element e) {
    String msg = "error setting createTime: ";
    try {
      g.setCreateTimeLong( this._parseTime( XmlImporter._getText(e) ).getTime() );
      return true;
    } catch (GrouperException eG) {
       msg += eG.getMessage();
    } catch (ParseException eP) {
      msg += eP.getMessage();
    }
    LOG.error(msg);
    return false;
  } // private boolean _setCreateTime(g, e)

  // @since   1.2.0
  private boolean _setCreateTime(Stem ns, Element e) {
    String msg = "error setting createTime: ";
    try {
      ns.setCreateTimeLong( this._parseTime( XmlImporter._getText(e) ).getTime() );
      return true;
    } catch (GrouperException eG) {
       msg += eG.getMessage();
    } catch (ParseException eP) {
      msg += eP.getMessage();
    }
    LOG.error(msg);
    return false;
  } // private boolean _setCreateTime(ns, e)

  // @since   1.2.0
  private void _setInternalAttributes(Group g, Element e) 
    throws  GrouperDAOException
  {
    String    attr;
    boolean   modified    = false;
    Element   e0;
    Iterator  it          = this._getInternalAttributes(e).iterator();
    while (it.hasNext()) {
      e0    = (Element) it.next();
      attr  = e0.getAttribute("name");
      if      ( "createSubject".equals(attr) ) {
        this._setCreateSubject(g, e0);
        modified = true;
      }
      else if ( "createTime".equals(attr) ) {
        if ( this._setCreateTime(g, e0) ) {
          modified = true;
        }
      }
    }
    if (modified) {
      GrouperDAOFactory.getFactory().getGroup().update( g);
    }
  } // private void _setInternalAttributesAttributes(g, e)
  
  // @since   1.2.0
  private void _setInternalAttributes(Stem ns, Element e) 
    throws  GrouperDAOException
  {
    String    attr;
    boolean   modified    = false;
    Element   e0;
    Iterator  it          = this._getInternalAttributes(e).iterator();
    while (it.hasNext()) {
      e0    = (Element) it.next();
      attr  = e0.getAttribute("name");
      if      ( "createSubject".equals(attr) ) {
        this._setCreateSubject(ns, e0);
        modified = true;
      }
      else if ( "createTime".equals(attr) ) {
        if ( this._setCreateTime(ns, e0) ) {
          modified = true;
        }
      }
    }
    if (modified) {
      ns.store();
    }
  } // private void _setInternalAttributesAttributes(ns, e)

  
  // GETTERS //

  // @since   1.1.0
  private Document _getDocument() {
    return this.doc;
  } // private Document _getDocument()

  // @since   1.1.0
  private boolean _getUpdateOnly() {
    return this.updateOnly;
  } // private boolean _getUpdateOnly()


  // SETTERS //

  // @since   1.1.0
  private void _setDocument(Document doc) 
    throws  IllegalArgumentException
  {
    if (doc == null) {
      throw new IllegalArgumentException(E.INVALID_DOC);
    }
    this.doc = doc;
  } // private void _setDocument(doc)

  // @since   1.1.0
  private void _setUpdateOnly(boolean updateOnly) {
    this.updateOnly = updateOnly;
  } // private void _setUpdateOnly(updateOnly)

  // @since 1.5.1
  /**
   * Return the value of the immediate child <description> element or null. Throws a
   * RuntimeException if there is more than one description found.
   * 
   * @param e
   *          the element
   * @return the description or null
   */
  private String _getDescription(Element e) {
    String desc = null;   
    Collection elements = this._getImmediateElements(e, GrouperConfig.ATTRIBUTE_DESCRIPTION);    
    if (elements.isEmpty()) {
      return desc;
    }
    if (elements.size() > 1) {
      throw new IllegalArgumentException(E.ELEMENT_NOT_UNIQUE + GrouperConfig.ATTRIBUTE_DESCRIPTION);
    }
    Element elDesc = (Element) elements.iterator().next();    
    if (elDesc != null) {
      try {
        desc = ((Text) elDesc.getFirstChild()).getData();
      } catch (NullPointerException npe) {
        // ignore
      }
    }
    return desc;
  }
  
} // public class XmlImporter

