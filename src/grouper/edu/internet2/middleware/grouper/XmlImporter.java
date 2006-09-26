/*
 Copyright 2004-2006 University Corporation for Advanced Internet Development, Inc.
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

package edu.internet2.middleware.grouper;
import  edu.internet2.middleware.subject.*;
import  java.io.*;
import  java.net.*;
import  java.util.*;
import  javax.xml.parsers.*;
import  org.apache.commons.logging.*;
import  org.w3c.dom.*;

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
 * @version $Id: XmlImporter.java,v 1.51 2006-09-26 14:17:41 blair Exp $
 * @since   1.0
 */
public class XmlImporter {

  // PRIVATE CLASS CONSTANTS //  
  private static final String CF            = "import.properties";
  private static final Log    LOG           = LogFactory.getLog(XmlImporter.class);
  private static final String RC_IFILE      = "import.file";
  private static final String RC_NAME       = "owner.name";
  private static final String RC_SUBJ       = "subject.identifier";
  private static final String RC_UPROPS     = "properties.user";
  private static final String RC_UPDATELIST = "update.list";
  private static final String RC_UUID       = "owner.uuid";


  // PRIVATE INSTANCE VARIABLES //
  private List            accessPrivLists = new ArrayList();
  private Document        doc;
  private Map             importedGroups  = new HashMap();
  private String          importRoot; // Anchor import here
  private List            membershipLists = new ArrayList();
  private List            memberships     = new ArrayList();
  private List            namingPrivLists = new ArrayList();
  private Properties      options         = new Properties();
  private GrouperSession  s;
  private boolean         updateOnly      = false;  // add (or not) groups + stems when importing

 
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
      this.options  = XmlUtils.getSystemProperties(LOG, CF);
    }
    catch (IOException eIO) {
      throw new GrouperRuntimeException(eIO.getMessage(), eIO);
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
  public static void main(String[] args) {
    if (XmlUtils.wantsHelp(args)) {
      System.out.println( _getUsage() );
      System.exit(0);
    }
    Properties rc = new Properties();
    try {
      rc = _getArgs(args);
    }
    catch (Exception e) {
      e.printStackTrace();
      System.err.println();
      System.err.println( _getUsage() );
      System.exit(1);
    }
    XmlImporter importer = null;
    try {
      importer  = new XmlImporter(
        GrouperSession.start(
          SubjectFinder.findByIdentifier( rc.getProperty(RC_SUBJ) )
        ),
        XmlUtils.getUserProperties(LOG, rc.getProperty(RC_UPROPS))
      );
      _handleArgs(importer, rc);
      LOG.debug("Finished import of [" + rc.getProperty(RC_IFILE) + "]");
    }
    catch (Exception e) {
      LOG.fatal("unable to import from xml: " + e.getMessage());
      System.exit(1);
    }
    finally {
      if (importer != null) {
        try {
          importer.s.stop();
        }
        catch (SessionException eS) {
          LOG.error(eS.getMessage());
        }
      }
    }
    System.exit(0);
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
  public void load(Document doc)
    throws  GrouperException,
            IllegalArgumentException
  {
    LOG.info("starting load at root stem");
    this._load( StemFinder.findRootStem(this.s), doc );
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
    LOG.info("starting load at " + U.q(ns.getName()));
    this._load(ns, doc);
    LOG.info("finished load");
  } // public void load(ns, doc)

  /**
   * Update memberships and privileges but do not create stems or groups.
   * <p>
   * <b>NOTE:</b> This method does not currently work properly as groups and
   * stems <b>ARE</b> created by it.
   * </p>
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
    this.updateOnly = true;
    this._load( StemFinder.findRootStem(this.s), doc );
    LOG.info("finished update");
  } // public void update(doc)


  // PROTECTED INSTANCE METHODS //

  // @since   1.1.0
  protected Properties getOptions() {
    return (Properties) options.clone();
  } // protected Properties getOptions()


  // PRIVATE CLASS METHODS //

  // @since   1.1.0
  private static Properties _getArgs(String[] args) 
    throws  IllegalArgumentException,
            IllegalStateException
  {
    Properties rc = new Properties();

    String  arg;
    int     inputPos  = 0;
    int     pos       = 0;

    while (pos < args.length) {
      arg = args[pos];
      if (arg.startsWith("-")) {
        if (arg.equals("-id")) {
          if (rc.getProperty(RC_NAME) != null) {
            throw new IllegalArgumentException(XmlUtils.E_NAME_AND_UUID);
          }
          rc.setProperty(RC_UUID, args[pos + 1]);
          pos += 2;
          continue;
        } 
        else if (arg.equals("-name")) {
          if (rc.getProperty(RC_UUID) != null) {
            throw new IllegalArgumentException(XmlUtils.E_NAME_AND_UUID);
          }
          rc.setProperty(RC_NAME, args[pos + 1]);
          pos += 2;
          continue;
        } 
        else if (arg.equals("-list")) {
          rc.setProperty(RC_UPDATELIST, "true");
          pos++;
          continue;
        } 
        else {
          throw new IllegalArgumentException(XmlUtils.E_UNKNOWN_OPTION + arg);
        }
      }
      switch (inputPos) {
      case 0:
        rc.setProperty(RC_SUBJ, arg);
        break;
      case 1:
        rc.setProperty(RC_IFILE, arg);
        break;
      case 2:
        rc.setProperty(RC_UPROPS, arg);
        break;
      case 3:
        throw new IllegalArgumentException("Too many arguments - " + arg);
      }
      pos++;
      inputPos++;
    }
    if (inputPos < 1) {
      throw new IllegalStateException("Too few arguments");
    }
    return rc;
  } // private static Properties _getArgs(args)

  // @since   1.1.0
  private static Document _getDocument(String filename) 
    throws  IOException,
            org.xml.sax.SAXException,
            ParserConfigurationException
  {
    DocumentBuilderFactory  dbf = DocumentBuilderFactory.newInstance();
    DocumentBuilder         db  = dbf.newDocumentBuilder();
    Document                doc = db.parse(new File(filename));
    return doc;
  } // private static Document _getDocument(filename)

  // @since   1.1.0
  private static Document _getDocument(URL url) 
    throws  IOException,
            org.xml.sax.SAXException,
            ParserConfigurationException
  {
    DocumentBuilderFactory  dbf = DocumentBuilderFactory.newInstance();
    DocumentBuilder         db  = dbf.newDocumentBuilder();
    Document                doc = db.parse(url.openStream());
    return doc;
  } // private static Document _getDocument(url)

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
    return  "Usage:"
            + "args: -h,            Prints this message"
            + "args: subjectIdentifier [(-id <id> | -name <name> | -list)]"
            + "      filename [properties]"
            + ""
            + "  subjectIdentifier, Identifies a Subject 'who' will create a"
            + "                     GrouperSession"
            + "  -id,               The Uuid of a Stem, into which, data will be"
            + "                     imported*"
            + "  -name,             The name of a Stem, into which, data will be"
            + "                     imported*"
            + "                     *If no -id / -name is specified, use=ROOT stem"
            + "  -list,             File contains a flat list of Stems or Groups"
            + "                     which may be updated. Missing Stems and Groups"
            + "                     are not created"
            + "  filename,          The file to import"
            + "  properties,        The name of a standard Java properties file "
            + "                     which configures the import. Check Javadoc for"
            + "                     a list of properties. If 'properties' is not "
            + "                      specified, XmlImporter will look for "
            + "                     'import.properties' in the working directory. "
            + "                     If this file does not exist XmlImporter will "
            + "                     look on the classpath. If 'properties' is not "
            + "                     specified and 'import.properties' cannot be "
            + "                     found and import options are not included in "
            + "                     the XML, the import will fail."
            ;
  } // private static String _getUsage()

  // @since   1.1.0
  private static void _handleArgs(XmlImporter importer, Properties rc) 
    throws  GrouperException,
            IOException,
            org.xml.sax.SAXException,
            ParserConfigurationException
  {
    Document doc = _getDocument( rc.getProperty(RC_IFILE) );
    if (Boolean.getBoolean( rc.getProperty(RC_UPDATELIST) )) {
      importer.update(doc);
    } 
    else {
      if (rc.getProperty(RC_UUID) == null && rc.getProperty(RC_NAME) == null) {
        importer.load(doc);
      } 
      else {
        Stem    stem  = null;
        String  uuid  = rc.getProperty(RC_UUID);
        String  name  = rc.getProperty(RC_NAME);
        if (uuid != null) {
          try {
            stem = StemFinder.findByUuid(importer.s, uuid);
            LOG.debug("Found stem with uuid [" + uuid + "]");
          } catch (StemNotFoundException e) {
            // TODO 20060920 empty catch
          }
        } 
        else {
          try {
            stem = StemFinder.findByName(importer.s, name);
            LOG.debug("Found stem with name [" + name + "]");
          } catch (StemNotFoundException e) {
            // TODO 20060920 empty catch
          }
        }
        if (stem == null) {
          if (name != null) {
            throw new IllegalArgumentException(
              "Could not find stem with name [" + name + "]"
            );
          }
          throw new IllegalArgumentException(
            "Could not find stem with id [" + uuid + "]"
          );
        }
        importer.load(stem, doc);
      }
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
    if (name != null && name.startsWith(".")) {
      if (name.startsWith("." + Stem.ROOT_INT)) {
        name = stem + name.substring(1);
      } 
      else {
        while (name.startsWith(".." + Stem.ROOT_INT)) {
          name = name.substring(3);
          stem = stem.substring(0, stem.lastIndexOf(Stem.ROOT_INT));
        }
        name = stem + Stem.ROOT_INT + name;
      }
    }
    if (
      !XmlUtils.isEmpty(importRoot)
      && this.importedGroups.containsKey(importRoot + Stem.ROOT_INT + name)
    ) 
    {
      return importRoot + Stem.ROOT_INT + name;
    }
    return name;
  } // private String _getAbsoluteName(name, stem)

  // Returns immediate child element with given name
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
    Collection elements = new Vector();
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
      props.put(optionE.getAttribute("key"), _getText(optionE));
      LOG.debug("Loading " + optionE.getAttribute("key") + "="
          + _getText(optionE));
    }
    LOG.debug("Finished loading options from XML");

    return props;
  } // private Properties _getImportedOptionsFromXml()

  // @since   1.0
  private Subject _getSubjectById(String id, String type) 
    throws  SubjectNotFoundException,
            SubjectNotUniqueException
  {
    if (XmlUtils.isEmpty(type)) {
      return SubjectFinder.findById(id);
    }
    return SubjectFinder.findById(id, type);
  } // private Subject _getSubjectById(id, type)

  // @since   1.0
  private Subject _getSubjectByIdentifier(String identifier, String type)
    throws  SubjectNotFoundException,
            SubjectNotUniqueException
  {
    if (XmlUtils.isEmpty(type)) {
      return SubjectFinder.findByIdentifier(identifier);
    }
    return SubjectFinder.findByIdentifier(identifier, type);
  } // private Subject _getSubjectByIdentifier(identifier, type)

  // @since   1.1.0
  private boolean _isRelativeImport(String idfr) {
    return (
          idfr.startsWith(  XmlUtils.SPECIAL_STAR )
      &&  !idfr.endsWith(   XmlUtils.SPECIAL_STAR )
    );
  } // private boolean _isRelativeImport(idfr)
 
  // @since   1.1.0
  private boolean _isUpdatingAttributes(Element e) {
    // TODO 20060922 switch over to this method
    String update = e.getAttribute("updateAttributes");
    if (XmlUtils.isEmpty(update)) {
      update = this.options.getProperty("import.data.update-attributes");
    }
    return Boolean.getBoolean(update); 
  } // private boolean _isUpdatingAttributes()

  // @since   1.1.0
  private void _load(Stem ns, Document doc) 
    throws  GrouperException,
            IllegalArgumentException
  {
    this._setDocument(doc);
    try {
      this.importRoot = ns.getName();
      if (ns.isRootStem()) {
        this.importRoot = GrouperConfig.EMPTY_STRING;
      }
      this._processProperties();
      Element root = this._getDocument().getDocumentElement();

      this._processMetaData(this._getImmediateElement(root, "metadata"));
      this._process( this._getImmediateElement(root, "data"), this.importRoot );
      this._processMembershipLists();
      this._processNamingPrivLists();
      this._processAccessPrivLists();
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
  } // private void _load(ns, doc)

  // @since   1.0
  private boolean _optionTrue(String key) {
    if (XmlUtils.isEmpty(key)) {
      options.setProperty(key, "false");
      return false;
    }
    return "true".equals(options.getProperty(key));
  } // private boolean _optionTrue(key)

  // For each stem list and process any child stems. List and process any child groups.
  // @since   1.1.0
  private void _process(Element e, String stem) 
    throws  AttributeNotFoundException,
            GroupAddException,
            GrouperException,
            GroupModifyException,
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
            SubjectNotFoundException
  {
    if (accessPrivLists == null || accessPrivLists.size() == 0) {
      return;
    }
    Collection  subjects;
    Iterator    subjectsIterator;
    Element     subjectE;
    Element     privileges;
    Map         map;
    String      group;
    Subject     subject           = null;
    String      subjectType;
    boolean     isImmediate       = false;
    Group       privGroup;
    Group       focusGroup        = null;
    String      subjectId;
    String      subjectIdentifier;
    String      privilege;
    Privilege   grouperPrivilege;
    String      importOption;
    String      lastGroup         = "";
    for (int i = 0; i < accessPrivLists.size(); i++) {
      map   = (Map) accessPrivLists.get(i);
      group = (String) map.get("group");

      //Save a call if we are dealing with same group
      if (!group.equals(lastGroup)) {
        if (XmlUtils.isEmpty(lastGroup)) {
          LOG.debug("Finished loading Access privs for " + lastGroup);
        }
        focusGroup = GroupFinder.findByName(s, group);
        LOG.debug("Loading Access privs for " + group);
      }

      lastGroup     = group;
      privileges    = (Element) map.get("privileges");
      privilege     = privileges.getAttribute("type");
      importOption  = privileges.getAttribute("importOption");
      if (XmlUtils.isEmpty(importOption))
        importOption = options.getProperty("import.data.privileges");

      if (XmlUtils.isEmpty(importOption) || "ignore".equals(importOption)) {
        LOG.debug("Ignoring any '" + privilege + "' privileges");
        continue; //No instruction so ignore
      }
      grouperPrivilege = Privilege.getInstance(privilege);
      if ("replace".equals(importOption)) {
        LOG.debug("Revoking current '" + privilege + "' privileges");
        focusGroup.revokePriv(grouperPrivilege);
      }
      subjects          = this._getImmediateElements(privileges, "subject");
      subjectsIterator  = subjects.iterator();
      while (subjectsIterator.hasNext()) {
        subjectE    = (Element) subjectsIterator.next();
        isImmediate = "true".equals(subjectE.getAttribute("immediate"));
        if (XmlUtils.isEmpty(subjectE.getAttribute("immediate"))) {
          isImmediate = true; //default is to assign
        }
        if (!isImmediate) {
          continue;
        }

        subjectId         = subjectE.getAttribute("id");
        subjectIdentifier = subjectE.getAttribute("identifier");
        subjectType       = subjectE.getAttribute("type");
        if ("group".equals(subjectType)) {
          if (this._isRelativeImport(subjectIdentifier)) {
            if (XmlUtils.isEmpty(importRoot)) {
              subjectIdentifier = importRoot + Stem.ROOT_INT + subjectIdentifier.substring(1);
            }
            else {
              subjectIdentifier = subjectIdentifier.substring(1);
            }
          } 
          else {
              subjectIdentifier = this._getAbsoluteName(
                subjectIdentifier, focusGroup.getParentStem().getName()
              );
          }
          try {
            privGroup = GroupFinder.findByName(s, subjectIdentifier);
          } 
          catch (Exception e) {
            LOG.warn("Could not find Group identified by " + subjectIdentifier);
            continue;
          }
          subject = privGroup.toSubject();
        } 
        else {
          try {
            subject = this._processMembershipListsFindSubject(subjectId, subjectIdentifier, subjectType);
          }
          catch (SubjectNotFoundException eSNF) {
            LOG.error(eSNF.getMessage());
            continue;
          }
          catch (SubjectNotUniqueException eSNU) {
            LOG.error(eSNU.getMessage());
            continue;
          }
        }

        if (
          !XmlExporter.hasImmediatePrivilege( subject, focusGroup, privilege)
        ) 
        {
          LOG.debug("Assigning " + privilege + " to " + subject.getName() + " for " + group);
          focusGroup.grantPriv(subject, Privilege.getInstance(privilege));
          LOG.debug("... finished assignment");
        } 
        else {
          LOG.debug(privilege + " already assigned to " + subject.getName() + " so skipping");
        }
      }
    }
    LOG.debug("Finished assigning Access privs");
    accessPrivLists = null;
  } // private void _processAccessPrivLists()

  // @since   1.1.0
  private void _processAttributes(Element e, String stem) 
    throws  AttributeNotFoundException,
            GroupModifyException,
            GroupNotFoundException,
            InsufficientPrivilegeException,
            SchemaException
  {
    Element groupTypes = this._getImmediateElement(e, "groupTypes");
    if (groupTypes == null) {
      return;
    }
    Collection  types               = this._getImmediateElements(groupTypes, "groupType");
    Element     groupType;
    Iterator    typesIterator       = types.iterator();
    Collection  attributes;
    Element     attribute;
    String      name;
    String      value;
    String      origValue           = null;
    Iterator    attributesIterator;
    Group       group               = GroupFinder.findByName(s, stem);
    GroupType   grouperGroupType    = null;
    while (typesIterator.hasNext()) {
      groupType = (Element) typesIterator.next();
      if ("base".equals(groupType.getAttribute("name"))) {
        continue;
      }
      try {
        grouperGroupType = GroupTypeFinder.find(groupType
            .getAttribute("name"));
      } 
      catch (Exception ex) {
        continue;
      }
      if (!group.hasType(grouperGroupType)) {
        if (_optionTrue("import.metadata.apply-new-group-types")) {
          group.addType(grouperGroupType);
        } 
        else {
          continue;
        }
      }
      attributes = this._getImmediateElements(groupType, "attribute");
      attributesIterator = attributes.iterator();
      Field field = null;
      while (attributesIterator.hasNext()) {
        attribute = (Element) attributesIterator.next();
        name      = attribute.getAttribute("name");
        field     = FieldFinder.find(name);
        if (!group.canWriteField(field)) {
          LOG.debug(
            "No write privilege. Attribute [" + name + "] for [" + group.getName() + "] ignored"
          );
          continue;
        }
        value = ((Text) attribute.getFirstChild()).getData();
        try {
          origValue = group.getAttribute(name);
        }   
        catch (Exception ex) {
          // TODO Figure out why this is ignored once I refactor this class
        }
        if (
          value != null
          && !value.equals(origValue)
          && (XmlUtils.isEmpty(origValue) || _optionTrue("import.data.update-attributes"))
        )
        {
          group.setAttribute(name, value);
        }
      }
    }
  } // private void _processAttributes(e, stem) 

  // @since   1.1.0
  private void _processComposite(Element composite, Group group)
    throws  GrouperException
  {
    LOG.debug("Processing composite for " + group.getName());
    if (group.hasComposite()) { 
      LOG.warn(group.getName() + " already has composite - skipping");
      return;
    }
    Group         leftGroup   =  null;
    CompositeType compType    = null;
    Group         rightGroup  = null;
    composite.normalize();
    Element[]     elements    = new Element[3];
    NodeList      nl          = composite.getChildNodes();
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
      Element leftE   = elements[0];
      leftGroup       = _processGroupRef(leftE, group.getParentStem().getName());
      Element typeE   = elements[1];
      compType        = _processCompositeType(typeE);
      Element rightE  = elements[2];
      rightGroup      = _processGroupRef(rightE, group.getParentStem() .getName());
    } 
    catch (Exception e) {
      LOG.error("Error processing composite for " + group.getName(), e);
      return;
    }
    try {
      group.addCompositeMember(compType, leftGroup, rightGroup);
    } 
    catch (Exception e) {
      LOG.error("Error adding composite for " + group.getName(), e);
    }
  } // private void _processComposite(composite, group)

  // @since   1.1.0
  private CompositeType _processCompositeType(Element typeE) 
    throws  GrouperException
  {
    CompositeType type    = null;
    String        tagName = typeE.getTagName();
    if (!"compositeType".equals(tagName)) {
      throw new IllegalStateException(
          "Expected tag: <compositeType> but found <" + tagName + ">"
      );
    }
    String name = _getText(typeE);
    if ("intersection".equals(name)) {
      type = CompositeType.INTERSECTION;
    } else if ("union".equals(name)) {
      type = CompositeType.UNION;
    } else if ("complement".equals(name)) {
      type = CompositeType.COMPLEMENT;
    } else {
      throw new IllegalStateException(
        "Invalid CompositeType [" + name + "]. union, intersection or complement allowed"
      );
    }
    return type;
  }  // private CompositeType _processCompositeType(typeE)

  // @since   1.1.0
  private void _processGroup(Element e, String stem) 
    throws  AttributeNotFoundException,
            GroupAddException,
            GrouperException,
            GroupModifyException,
            InsufficientPrivilegeException,
            SchemaException,
            StemNotFoundException
  {
    String newGroup = U.constructName( stem, e.getAttribute(GrouperConfig.ATTR_E) );
    try {
      this._processGroupUpdate(e, newGroup);  // Try and update
    } 
    catch (GroupNotFoundException eGNF) {
      // TODO 20060922 honor `updateOnly`
      this._processGroupCreate(e, stem);      // Otherwise create
    }
    this._accumulateLists(e, newGroup);
    this._accumulatePrivs(e, newGroup, "access");
  } // private void _processGroup(e, stem)

  // @since   1.1.0
  private void _processGroupCreate(Element e, String stem) 
    throws  GroupAddException,
            GroupModifyException,
            InsufficientPrivilegeException,
            StemNotFoundException
  {
    Stem  parent  = StemFinder.findByName(this.s, stem);
    Group child   = parent.addChildGroup(
      e.getAttribute(GrouperConfig.ATTR_E),
      e.getAttribute(GrouperConfig.ATTR_DE)
    );
    String description = e.getAttribute(GrouperConfig.ATTR_D);
    if (Validator.isNotNullOrBlank(description)) {
      child.setDescription(description);
    }
    // TODO 20060922 "c"?
    this.importedGroups.put(child.getName(), "c");
  } // private void _processGroupCreate(e, stem)

  // @since   1.1.0
  private Group _processGroupRef(Element groupE, String stem) 
    throws  GroupNotFoundException
  {
    Group   group   = null;
    String  tagName = groupE.getTagName();
    if (!"groupRef".equals(tagName)) {
      throw new IllegalStateException(
        "Expected tag: <groupRef> but found <" + tagName + ">"
      );
    }
    String name = groupE.getAttribute(GrouperConfig.ATTR_N);
    if (XmlUtils.isEmpty(name)) {
      throw new IllegalStateException(
        "Expected 'name' atribute for <groupRef>"
      );
    }
    String actualName = this._getAbsoluteName(name, stem);
    group             = GroupFinder.findByName(s, actualName);
    return group;
  } // private Group _processGroupRef(groupE, stem)

  // @since   1.1.0
  private void _processGroups(Element e, String stem) 
    throws  AttributeNotFoundException,
            GroupAddException,
            GrouperException,
            GroupModifyException,
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
    Group g = GroupFinder.findByName(this.s, newGroup);
    if (this._isUpdatingAttributes(e)) {
      String dExtn  = e.getAttribute(GrouperConfig.ATTR_DE);
      if (!XmlUtils.isEmpty(dExtn) && !dExtn.equals(g.getDisplayExtension())) {
        g.setDisplayExtension(dExtn);
      }
      String desc   = e.getAttribute(GrouperConfig.ATTR_D);
      if (!XmlUtils.isEmpty(desc) && !desc.equals(g.getDisplayExtension())) {
        g.setDisplayExtension(desc);
      }
    }
    // TODO 20060922 "e"?
    this.importedGroups.put( g.getName(), "e" );
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
            SubjectNotFoundException
  {
    if (membershipLists == null || membershipLists.size() == 0) {
      return;
    }
    Collection  subjects;
    Iterator    subjectsIterator;
    Element     subjectE;
    Element     list;
    String      listName;
    Field       field               = null;
    Subject     subject;
    String      groupName;
    String      lastGroupName       = "";
    Group       group               = null;
    Map         map;
    String      subjectId;
    String      subjectIdentifier;
    String      subjectType;
    Group       privGroup;
    boolean     isImmediate;
    String      importOption;
    for (int i = 0; i < membershipLists.size(); i++) {
      map = (Map) membershipLists.get(i);
      list = (Element) map.get("list");
      importOption = list.getAttribute("importOption");
      if (XmlUtils.isEmpty(importOption)) {
        importOption = options.getProperty("import.data.lists");
      }
      if (XmlUtils.isEmpty(importOption) || "ignore".equals(importOption)) {
        continue; //No instruction so ignore
      }
      groupName = (String) map.get("group");

      //Save a call if we are dealing with same group
      if (!groupName.equals(lastGroupName)) {
        if (!XmlUtils.isEmpty(lastGroupName)) {
          LOG.debug("Finished loading memberships for " + lastGroupName);
        }
        group = GroupFinder.findByName(s, groupName);
        LOG.debug("Loading memberships for " + groupName);
      }

      lastGroupName = groupName;

      listName = list.getAttribute("field");
      try {
        field = FieldFinder.find(listName);
        if (!field.getType().equals(FieldType.LIST)) {
          LOG.error(listName + " is not a list");
          continue;
        }
      } 
      catch (Exception e) {
        LOG.error("Cannot find list " + listName);
        continue;
      }
      //TODO add admin check?
      if (!group.hasType(field.getGroupType())) {
        if (_optionTrue("import.data.apply-new-group-types")) {
          LOG.debug("Adding group type " + field.getGroupType());
          group.addType(field.getGroupType());
        } 
        else {
          LOG.debug("Ignoring field " + field.getName());
          continue;
        }
      }
      if (!group.canReadField(field)) {
        LOG.debug("No write privilege - ignoring field " + field.getName());
        continue;
      }
      boolean hasComposite  = group.hasComposite();
      boolean hasMembers    = false;
      if (!hasComposite && group.getImmediateMembers().size() > 0) {
        hasMembers = true;
      }
      Element compE = this._getImmediateElement(list, "composite");

      if ("replace".equals(importOption)) {
        if (hasComposite) {
          group.deleteCompositeMember();
        } 
        else {
          Set       members         = group.getImmediateMembers(field);
          Iterator  membersIterator = members.iterator();
          Member    memb;
          LOG.debug("Removing all memberships for " + groupName);
          while (membersIterator.hasNext()) {
            memb = (Member) membersIterator.next();
            group.deleteMember(memb.getSubject());
          }
        }
      }
      if (compE != null && (!"add".equals(importOption) || hasMembers)) {
        this._processComposite(compE, group);
        continue;
      }
      if (compE != null && hasMembers) {
        LOG.warn("Skipping composite - cannot ad to existing members for " + groupName);
        continue;
      }
      subjects          = this._getImmediateElements(list, "subject");
      subjectsIterator  = subjects.iterator();
      while (subjectsIterator.hasNext()) {
        subjectE    = (Element) subjectsIterator.next();
        isImmediate = "true".equals(subjectE.getAttribute("immediate"));
        if (XmlUtils.isEmpty(subjectE.getAttribute("immediate"))) {
          isImmediate = true;
        }
        if (!isImmediate) {
          continue;
        }

        subjectId         = subjectE.getAttribute("id");
        subjectIdentifier = subjectE.getAttribute("identifier");
        subjectType       = subjectE.getAttribute("type");
        if ("group".equals(subjectType)) {
          if (this._isRelativeImport(subjectIdentifier)) {
            if (!XmlUtils.isEmpty(importRoot)) {
              subjectIdentifier = importRoot + Stem.ROOT_INT + subjectIdentifier.substring(1);
            }
            else {
              subjectIdentifier = subjectIdentifier.substring(1);
            }
          } 
          else {
            subjectIdentifier = this._getAbsoluteName(
              subjectIdentifier, group.getParentStem() .getName()
            );
          }
          try {
            privGroup = GroupFinder.findByName(s, subjectIdentifier);
          } 
          catch (Exception e) {
            LOG.warn("Could not find Group identified by " + subjectIdentifier);
            continue;
          }
          subject = privGroup.toSubject();
        } 
        else {
          try {
            subject = this._processMembershipListsFindSubject(subjectId, subjectIdentifier, subjectType);
          }
          catch (SubjectNotFoundException eSNF) {
            LOG.error(eSNF.getMessage());
            continue;
          }
          catch (SubjectNotUniqueException eSNU) {
            LOG.error(eSNU.getMessage());
            continue;
          }
        }
        this._processMembershipListsAddMember(group, subject, field);
      }
    }
    this.membershipLists = null;
  } // private void _processMembershipLists()

  // @since   1.1.0
  private void _processMembershipListsAddMember(Group g, Subject subj, Field f) 
    throws  InsufficientPrivilegeException,
            MemberAddException,
            SchemaException
  {
    String msg = " a member of " + U.q(g.getName()) + " (list=" + U.q(f.getName()) + ")";
    if (!g.hasImmediateMember(subj, f)) {
      LOG.debug("making " + U.q(subj.getName()) + msg);
      g.addMember(subj, f);
      LOG.debug("...assigned");
    } 
    else {
      LOG.debug(U.q(subj.getName()) + " is " + msg + " - skipping");
    }
  } // private void _processMembershipListsAddMember(g, subj, f)

  // @since   1.1.0
  private Subject _processMembershipListsFindSubject(String id, String idfr, String type) 
    throws  SubjectNotFoundException,
            SubjectNotUniqueException
  {
    if (XmlUtils.isEmpty(id)) {
      return this._getSubjectByIdentifier(idfr, type);
    } 
    return this._getSubjectById(id, type);
  } // private Subject _processMembershipListsFindSubject(id, idfr, type)

  // @since   1.1.0
  private void _processMetaData(Element e) 
    throws  InsufficientPrivilegeException,
            SchemaException
  {
    if (!_optionTrue("import.metadata.group-types") || e == null) {
      return;
    }
    LOG.debug("import.metadata.group-types=true - loading group-types");
    Element     groupTypesMetaData  = this._getImmediateElement(e, "groupTypesMetaData");
    Collection  groupTypes          = this._getImmediateElements(groupTypesMetaData, "groupTypeDef");
    Iterator    groupTypesIterator  = groupTypes.iterator();
    Element     groupType;
    GroupType   grouperGroupType;
    Collection  fields;
    Iterator    fieldsIterator;
    Field       grouperField;
    Element     field;
    String      groupTypeName;
    String      fieldName;
    String      readPriv;
    String      writePriv;
    String      fieldType;
    boolean     required;
    boolean     isNew               = false;
    while (groupTypesIterator.hasNext()) {
      isNew         = false;
      groupType     = (Element) groupTypesIterator.next();
      groupTypeName = groupType.getAttribute("name");
      try {
        grouperGroupType = GroupTypeFinder.find(groupTypeName);
        LOG.debug("Found existing GroupType - " + groupTypeName);
      } 
      catch (SchemaException ex) {
        grouperGroupType  = GroupType.createType(s, groupTypeName);
        isNew             = true;
        LOG.debug("Found and created new GroupType - " + groupTypeName);
      }
      fields = this._getImmediateElements(groupType, "field");
      if (fields.size() > 0) {
        LOG.debug("import.metadata.group-type-attributes=true");
      }
      fieldsIterator = fields.iterator();
      while (fieldsIterator.hasNext()) {
        field     = (Element) fieldsIterator.next();
        fieldName = field.getAttribute("name");
        fieldType = field.getAttribute("type");
        required  = "true".equals(field.getAttribute("required"));
        readPriv  = field.getAttribute("readPriv");
        writePriv = field.getAttribute("writePriv");
        try {
          grouperField = FieldFinder.find(fieldName);
          LOG.debug("Found existing Field - " + fieldName);
        } 
        catch (SchemaException ex) {
          grouperField = null;
        }
        if (
          (isNew || _optionTrue("import.metadata.group-type-attributes"))
          && grouperField == null
        ) 
        {
          LOG.debug("Found new Field - "  + fieldName + " - now adding");
          LOG.debug("Field Type="         + fieldType);
          LOG.debug("Field readPriv="     + readPriv);
          LOG.debug("Field writePriv="    + writePriv);
          LOG.debug("Field required="     + required);

          if (fieldType.equals("list")) {
            grouperGroupType.addList(
              s, fieldName, 
              Privilege.getInstance(readPriv), 
              Privilege.getInstance(writePriv)
            );
          } 
          else if (fieldType.equals("attribute")) {
            grouperGroupType.addAttribute(
              s, fieldName, 
              Privilege.getInstance(readPriv), 
              Privilege.getInstance(writePriv), required
            );
          } 
          else {

          }
        }
      }
    }
    LOG.debug("Finished processing group types and fields");
  } // private void _processMetaData(e)

  // @since   1.1.0
  private void _processNamingPrivLists() 
    throws  GrantPrivilegeException,
            GroupModifyException,
            InsufficientPrivilegeException,
            RevokePrivilegeException,
            SchemaException,
            StemNotFoundException,
            SubjectNotFoundException
  {
    if (this.namingPrivLists == null || this.namingPrivLists.size() == 0) {
      return;
    }
    Collection  subjects;
    Iterator    subjectsIterator;
    Element     subjectE;
    Element     privileges;
    Map         map;
    String      stem;
    String      lastStem          = "";
    Subject     subject           = null;
    String      subjectType;
    boolean     isImmediate       = false;
    Group       privGroup;
    Stem        focusStem         = null;
    String      subjectId;
    String      subjectIdentifier;
    String      privilege;
    Privilege   grouperPrivilege;
    String      importOption;
    for (int i = 0; i < this.namingPrivLists.size(); i++) {
      map   = (Map) this.namingPrivLists.get(i);
      stem  = (String) map.get("stem");

      //Save a call if we are dealing with same group
      if (!stem.equals(lastStem)) {
        if (!XmlUtils.isEmpty(lastStem)) {
          LOG.debug("Finished loading Naming privs for " + lastStem);
        }
        focusStem = StemFinder.findByName(s, stem);
        LOG.debug("Loading Naming privs for " + stem);
      }

      lastStem = stem;

      privileges    = (Element) map.get("privileges");
      privilege     = privileges.getAttribute("type");
      importOption  = privileges.getAttribute("importOption");
      if (XmlUtils.isEmpty(importOption)) {
        importOption = options.getProperty("import.data.privileges");
      }
      if (XmlUtils.isEmpty(importOption) || "ignore".equals(importOption)) {
        LOG.debug("Ignoring any '" + privilege + "' privileges");
        continue; //No instruction so ignore
      }

      grouperPrivilege = Privilege.getInstance(privilege);
      if ("replace".equals(importOption)) {
        LOG.debug("Revoking current '" + privilege + "' privileges");
        focusStem.revokePriv(grouperPrivilege);
      }

      subjects          = this._getImmediateElements(privileges, "subject");
      subjectsIterator  = subjects.iterator();
      while (subjectsIterator.hasNext()) {
        subjectE    = (Element) subjectsIterator.next();
        isImmediate = "true".equals(subjectE.getAttribute("immediate"));
        if (XmlUtils.isEmpty(subjectE.getAttribute("immediate"))) {
          isImmediate = true; //default is to assign
        }
        if (!isImmediate) {
          continue;
        }

        subjectId         = subjectE.getAttribute("id");
        subjectIdentifier = subjectE.getAttribute("identifier");
        subjectType       = subjectE.getAttribute("type");
        if ("group".equals(subjectType)) {
          if (this._isRelativeImport(subjectIdentifier)) {
            if (!XmlUtils.isEmpty(importRoot)) {
              subjectIdentifier = importRoot + Stem.ROOT_INT + subjectIdentifier.substring(1);
            }
            else {
              subjectIdentifier = subjectIdentifier.substring(1);
            }
          } 
          else {
            subjectIdentifier = this._getAbsoluteName(subjectIdentifier, stem);
          }
          try {
            privGroup = GroupFinder.findByName(s, subjectIdentifier);
          } 
          catch (Exception e) {
            LOG.warn("Could not find Stem identified by " + subjectIdentifier);
            continue;
          }
          subject = privGroup.toSubject();
        } 
        else {
          try {
            subject = this._processMembershipListsFindSubject(subjectId, subjectIdentifier, subjectType);
          }
          catch (SubjectNotFoundException eSNF) {
            LOG.error(eSNF.getMessage());
            continue;
          }
          catch (SubjectNotUniqueException eSNU) {
            LOG.error(eSNU.getMessage());
            continue;
          }
        }

        if (!XmlExporter.hasImmediatePrivilege(subject, focusStem, privilege)) {
          LOG.debug("Assigning " + privilege + " to " + subject.getName() + " for " + stem);
          focusStem.grantPriv(subject, Privilege.getInstance(privilege));
          LOG.debug("...assigned");
        } 
        else {
          LOG.debug(privilege + " already assigned to " + subject.getName() + " so skipping");
        }
      }
    }
    LOG.debug("Finished assigning Naming privs");
    this.namingPrivLists = null;
  } // private void _processNamingPrivLists()

  // @since   1.1.0
  private void _processPath(Element e, String stem) 
    throws  AttributeNotFoundException,
            GroupAddException,
            GrouperException,
            GroupModifyException,
            InsufficientPrivilegeException,
            SchemaException,
            StemAddException,
            StemModifyException,
            StemNotFoundException
  {
    String newStem = U.constructName( stem, e.getAttribute(GrouperConfig.ATTR_E) );
    try {
      this._processPathUpdate(e, newStem);  // Try and update
    } 
    catch (StemNotFoundException eNSNF) {
      // TODO 20060922 honor `updateOnly`
      this._processPathCreate(e, stem);     // Otherwise create
    }
    this._accumulatePrivs(e, newStem, "naming");
    this._process(e, newStem); // And now handle the child
  } // private void _processPath(e, stem)

  // @since   1.1.0
  private void _processPathCreate(Element e, String stem) 
    throws  InsufficientPrivilegeException,
            StemAddException,
            StemModifyException,
            StemNotFoundException
  {
    Stem parent = null;
    if (stem.equals(GrouperConfig.EMPTY_STRING)) {
      parent = StemFinder.findRootStem(this.s);
    }
    else {
      parent = StemFinder.findByName(this.s, stem);
    } 
    Stem child = parent.addChildStem(
      e.getAttribute(GrouperConfig.ATTR_E),
      e.getAttribute(GrouperConfig.ATTR_DE)
    );
    String description = e.getAttribute(GrouperConfig.ATTR_D);
    if (Validator.isNotNullOrBlank(description)) {
      child.setDescription(description);
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
    Stem ns = StemFinder.findByName(this.s, newStem);
    if (this._isUpdatingAttributes(e)) {
      String dExtn  = e.getAttribute(GrouperConfig.ATTR_DE);
      if (!XmlUtils.isEmpty(dExtn) && !dExtn.equals(ns.getDisplayExtension())) {
        ns.setDisplayExtension(dExtn);
      }
      String desc   = e.getAttribute(GrouperConfig.ATTR_D);
      if (!XmlUtils.isEmpty(desc) && !desc.equals(ns.getDisplayExtension())) {
        ns.setDisplayExtension(desc);
      }
    }
  } // private void _processPathUpdate(e, newStem)

  // @since   1.1.0
  private void _processPaths(Element e, String stem) 
    throws  AttributeNotFoundException,
            GroupAddException,
            GrouperException,
            GroupModifyException,
            InsufficientPrivilegeException,
            SchemaException,
            StemAddException,
            StemModifyException,
            StemNotFoundException
  {
    // TODO 20060922 *path* does not appear to be used 
    Collection paths  = this._getImmediateElements(e, "path");
    paths.addAll(this._getImmediateElements(e, "stem"));
    Iterator  it      = paths.iterator();
    while (it.hasNext()) {
      this._processPath( (Element) it.next(), stem );
    }
  } // private void _processPaths(e, stem)

  // @since   1.1.0
  // TODO 20060921 test
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
    LOG.debug("Merging user supplied options with XML options. Former take precedence");
    xmlOptions.putAll(this.options);  // add current to xml
    this.options = xmlOptions;        // replace current with merged options
  } // private void _processProperties()


  // GETTERS //

  // @since   1.1.0
  private Document _getDocument() {
    return this.doc;
  } // private Document _getDocument()


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

} // public class XmlImporter

