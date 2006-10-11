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
import  java.text.DateFormat;
import  java.text.SimpleDateFormat;
import  java.text.ParseException;
import  java.util.*;
import  net.sf.hibernate.*;
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
 * @version $Id: XmlImporter.java,v 1.81 2006-10-11 16:18:13 blair Exp $
 * @since   1.0
 */
public class XmlImporter {

  // PRIVATE CLASS CONSTANTS //  
  private static final String CF            = "import.properties";
  private static final Log    LOG           = LogFactory.getLog(XmlImporter.class);
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
    if (XmlArgs.wantsHelp(args)) {
      System.out.println( _getUsage() );
      System.exit(0);
    }
    Properties rc = new Properties();
    try {
      rc = XmlArgs.getXmlImportArgs(args);
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
          SubjectFinder.findByIdentifier( rc.getProperty(XmlArgs.RC_SUBJ) )
        ),
        XmlUtils.getUserProperties(LOG, rc.getProperty(XmlArgs.RC_UPROPS))
      );
      _handleArgs(importer, rc);
      LOG.debug("Finished import of [" + rc.getProperty(XmlArgs.RC_IFILE) + "]");
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
  private Subject _findSubject(String id, String idfr, String type) 
    throws  SubjectNotFoundException,
            SubjectNotUniqueException
  {
    if (XmlUtils.isEmpty(id)) {
      if (type.equals("group")) {
        if (this._isRelativeImport(idfr)) {
          if (Validator.isNotNullOrBlank(this.importRoot)) {
            idfr = U.constructName( this.importRoot, idfr.substring(1) );
          }
          else {
            idfr = idfr.substring(1);
          }
        }
        else {
          LOG.warn("not absolutizing idfr: " + U.q(idfr));
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
            + "args: subjectIdentifier [(-id <id> | -name <name> | -list)]"         + GrouperConfig.NL
            + "      filename [properties]"                                         + GrouperConfig.NL
            +                                                                         GrouperConfig.NL
            + "  subjectIdentifier, Identifies a Subject 'who' will create a"       + GrouperConfig.NL
            + "                     GrouperSession"                                 + GrouperConfig.NL
            + "  -id,               The Uuid of a Stem, into which, data will be"   + GrouperConfig.NL
            + "                     imported"                                       + GrouperConfig.NL
            + "  -name,             The name of a Stem, into which, data will be"   + GrouperConfig.NL
            + "                     imported.  If no -id / -name is specified, "    + GrouperConfig.NL
            + "                     use=ROOT stem."                                 + GrouperConfig.NL
            + "  -list,             File contains a flat list of Stems or Groups"   + GrouperConfig.NL
            + "                     which may be updated. Missing Stems and Groups" + GrouperConfig.NL
            + "                     are not created"                                + GrouperConfig.NL
            + "  filename,          The file to import"                             + GrouperConfig.NL
            + "  properties,        The name of a standard Java properties file "   + GrouperConfig.NL
            + "                     which configures the import. Check Javadoc for" + GrouperConfig.NL
            + "                     a list of properties. If 'properties' is not "  + GrouperConfig.NL
            + "                      specified, XmlImporter will look for "         + GrouperConfig.NL
            + "                     'import.properties' in the working directory. " + GrouperConfig.NL
            + "                     If this file does not exist XmlImporter will "  + GrouperConfig.NL
            + "                     look on the classpath. If 'properties' is not " + GrouperConfig.NL
            + "                     specified and 'import.properties' cannot be "   + GrouperConfig.NL
            + "                     found and import options are not included in "  + GrouperConfig.NL
            + "                     the XML, the import will fail."                 + GrouperConfig.NL
            ;
  } // private static String _getUsage()

  // @since   1.1.0
  private static void _handleArgs(XmlImporter importer, Properties rc) 
    throws  GrouperException
  {
    Document doc = XmlReader.getDocumentFromFile( rc.getProperty(XmlArgs.RC_IFILE) );
    if (Boolean.getBoolean( rc.getProperty(XmlArgs.RC_UPDATELIST) )) {
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
            ns = StemFinder.findByUuid(importer.s, uuid);
          } catch (StemNotFoundException e) {
            throw new IllegalArgumentException(E.NO_STEM_UUID + U.q(uuid));
          }
        } 
        else if (name != null) {
          try {
            ns = StemFinder.findByName(importer.s, name);
          } catch (StemNotFoundException e) {
            throw new IllegalArgumentException(E.NO_STEM_NAME + U.q(name));
          }
        }
        if (ns == null) {
          throw new IllegalArgumentException(E.NO_STEM);
        }
        importer.load(ns, doc);
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
        name = U.constructName(stem, name);
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

  // @since   1.1.0
  private String _getDataListImportMode() {
    return this.options.getProperty("import.data.lists", MODE_IGNORE);
  } // private String _getDataListImportMode()

  // @since   1.1.0
  private String _getDataPrivilegesImportMode() {
    return this.options.getProperty("import.data.privileges", MODE_IGNORE);
  } // private String _getDataPrivilegesImportMode()

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
  private boolean _isApplyNewGroupTypesEnabled() {
    return XmlUtils.getBooleanOption(this.options, "import.data.apply-new-group-types");
  } // private boolean _isApplyNewGroupTypesEnabled()
  
  // @since   1.1.0
  private boolean _isMetadataGroupTypeImportEnabled() {
    return XmlUtils.getBooleanOption(this.options, "import.metadata.group-types");
  } // private boolean _isMetadataGroupTypeImportEnabled()

  // @since   1.1.0
  private boolean _isMetadataGroupTypeAttributeImportEnabled() {
    return XmlUtils.getBooleanOption(this.options, "import.metadata.group-typea-attributes");
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
    return Boolean.valueOf( el.getAttribute("immediate") );
  } // private boolean _isSubjectElementImmediate(el)

  // @since   1.1.0
  private boolean _isUpdatingAttributes() {
    return XmlUtils.getBooleanOption(this.options, "import.data.update-attributes");
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

      this._processMetadata(this._getImmediateElement(root, "metadata"));
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
    catch (HibernateException eH)               {
      throw new GrouperException(eH.getMessage(), eH);
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
            GrouperException,
            GroupModifyException,
            GroupNotFoundException,
            HibernateException,
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
            SchemaException
  {
    String  groupName = (String) map.get("group");
    Element privs     = (Element) map.get("privileges");
    if (this._getDataPrivilegesImportMode().equals(MODE_IGNORE)) {
      return; // Ignore privileges
    }
    Group     g       = GroupFinder.findByName(s, groupName);
    Privilege p       = Privilege.getInstance( privs.getAttribute("type") );
    if (this._getDataPrivilegesImportMode().equals(MODE_REPLACE)) {
      g.revokePriv(p);
    }
    Iterator it       = this._getImmediateElements(privs, "subject").iterator();
    while (it.hasNext()) {
      this._processAccessPrivListGrantPriv( g, p, (Element) it.next() );
    }
  } // private void _processAccessPrivList(map)

  // @since   1.1.0
  private void _processAccessPrivListGrantPriv(Group g, Privilege p, Element el)
    throws  GrantPrivilegeException,
            InsufficientPrivilegeException,
            SchemaException
  {
    if (!this._isSubjectElementImmediate(el)) {
      return;
    }
    try {
      // TODO 20061005 how should i handle subject resolution failure?
      Subject subj = this._findSubject( 
        el.getAttribute("id"), el.getAttribute("identifier"), el.getAttribute("type") 
      );
      if (!XmlUtils.hasImmediatePrivilege(subj, g, p.getName())) {
        g.grantPriv(subj, p);
      }
    }
    catch (SubjectNotFoundException eSNF)   {
      return;
    }
    catch (SubjectNotUniqueException eSNU)  {
      return;
    }
  } // private void _processAccesgPrivListGrantPriv(g, p, el)

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
    Group     g       = GroupFinder.findByName(s, group);
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
        GroupType gt = GroupTypeFinder.find(name);
        if (!g.hasType(gt)) {
          if (this._optionTrue("import.data.apply-new-group-types")) {
            g.addType(gt);
          }
        }
        this._processAttributesHandleAttributes(g, e);
      }
      catch (SchemaException eS) {
        LOG.error(eS.getMessage());
      }
    }
  } // privae void _processAttributesHandleType(g, e)

  // @since   1.1.0
  private void _processAttributesHandleAttributes(Group g, Element e) 
    throws  AttributeNotFoundException,
            GroupModifyException,
            InsufficientPrivilegeException,
            SchemaException
  {
    Element   elAttr;
    Field     f;
    String    name, orig, val;
    Iterator  it      = this._getImmediateElements(e, "attribute").iterator();
    while (it.hasNext()) {
      elAttr  = (Element) it.next();
      name    = elAttr.getAttribute("name");
      f       = FieldFinder.find(name);
      if (!g.canWriteField(f)) {
        LOG.debug("cannot write (" + name + ") on (" + g.getName() + ")");
        continue;
      }
      orig    = g.getAttribute(name); 
      val     = ( (Text) elAttr.getFirstChild() ).getData();
      if (
            Validator.isNotNullOrBlank(val)
        &&  !val.equals(orig)
        &&  ( XmlUtils.isEmpty(orig) || this._isUpdatingAttributes() )
      )
      {
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
      LOG.error("error processing composite for " + U.q(g.getName()) + ": " + eGNF.getMessage());
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
    CompositeType ctype = CompositeType.getInstance(name);
    if (ctype == null) {
      throw new IllegalStateException("could not resolve composite type: " + U.q(name));
    }
    return ctype;
  }  // private CompositeType _processCompositeType(typeE)

  // @since   1.1.0
  private void _processGroup(Element e, String stem) 
    throws  AttributeNotFoundException,
            GroupAddException,
            GrouperException,
            GroupModifyException,
            GroupNotFoundException,
            HibernateException,
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
    this._processAttributes(e, newGroup);
    this._accumulateLists(e, newGroup);
    this._accumulatePrivs(e, newGroup, "access");
  } // private void _processGroup(e, stem)

  // @since   1.1.0
  private void _processGroupCreate(Element e, String stem) 
    throws  GroupAddException,
            GroupModifyException,
            HibernateException,
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
    this._setUuid(child, e);
    this._setInternalAttributes(child, e);
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
    String name = groupE.getAttribute(GrouperConfig.ATTR_N);
    if (XmlUtils.isEmpty(name)) {
      throw new IllegalStateException("Expected 'name' atribute for <groupRef>");
    }
    return GroupFinder.findByName( s, this._getAbsoluteName(name, stem) );
  } // private Group _processGroupRef(groupE, stem)

  // @since   1.1.0
  private void _processGroups(Element e, String stem) 
    throws  AttributeNotFoundException,
            GroupAddException,
            GrouperException,
            GroupModifyException,
            GroupNotFoundException,
            HibernateException,
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
    if (this._isUpdatingAttributes()) {
      String dExtn  = e.getAttribute(GrouperConfig.ATTR_DE);
      if (!XmlUtils.isEmpty(dExtn) && !dExtn.equals(g.getDisplayExtension())) {
        g.setDisplayExtension(dExtn);
      }
      String desc   = e.getAttribute(GrouperConfig.ATTR_D);
      if (!XmlUtils.isEmpty(desc) && !desc.equals(g.getDisplayExtension())) {
        g.setDisplayExtension(desc);
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
            SubjectNotFoundException
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
            SubjectNotFoundException
  {
    Element list      = (Element) map.get("list");
    String  groupName = (String) map.get("group");
    if (this._getDataListImportMode().equals(MODE_IGNORE)) {
      return; // Ignore lists
    }
    Group   g = GroupFinder.findByName(s, groupName);
    Field   f = FieldFinder.find( list.getAttribute("field") );
    if (!f.getType().equals(FieldType.LIST)) {
      throw new SchemaException("field is not a list: " + f.getName());
    }
    this._processMembershipListAddGroupType(g, f.getGroupType());
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
            SchemaException
  {
    if (!this._isSubjectElementImmediate(el)) {
      return;
    }
    try {
      // TODO 20061004 how should i handle subject resolution failure?
      Subject subj = this._findSubject( 
        el.getAttribute("id"), el.getAttribute("identifier"), el.getAttribute("type") 
      );
      if (!g.hasImmediateMember(subj, f)) {
        g.addMember(subj, f);
      }
    }
    catch (SubjectNotFoundException eSNF)   {
      return;
    }
    catch (SubjectNotUniqueException eSNU)  {
      return;
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
    // TODO 20061004 this needs more refactoring
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
          g.deleteMember( ( (Member) it.next() ).getSubject() );
        }
      }
    }
    if (compE != null && ( !this._getDataListImportMode().equals(MODE_ADD) || hasMembers) ) {
      this._processComposite(compE, g);
      rv = false; // Omit remaining processing
    }
    if (compE != null && hasMembers) {
      LOG.warn("Cannot add composite membership to group that already has members: " + U.q(g.getName()));
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
      String    fType = el.getAttribute("type");
      Privilege read  = Privilege.getInstance( el.getAttribute("readPriv")  );
      Privilege write = Privilege.getInstance( el.getAttribute("writePriv") );
      try {
        FieldFinder.find(fName); // already exists
      } 
      catch (SchemaException eS) {
        if (fType.equals( FieldType.LIST.toString() ) )           {
          gt.addList(s, fName, read, write);
        } 
        else if (fType.equals( FieldType.ATTRIBUTE.toString() ) ) {
          gt.addAttribute( s, fName, read, write, Boolean.valueOf( el.getAttribute("required") ) );
        } 
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
      gt = GroupTypeFinder.find(gtName);
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
            SubjectNotFoundException
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
            StemNotFoundException
  {
    String    stemName  = (String) map.get("stem");
    Element   privs     = (Element) map.get("privileges");
    if (this._getDataPrivilegesImportMode().equals(MODE_IGNORE)) {
      return; // Ignore privileges
    }
    Stem      ns        = StemFinder.findByName(s, stemName);
    Privilege p         = Privilege.getInstance( privs.getAttribute("type") );
    if (this._getDataPrivilegesImportMode().equals(MODE_REPLACE)) {
      ns.revokePriv(p);
    }
    Iterator  it        = this._getImmediateElements(privs, "subject").iterator();
    while (it.hasNext()) {
      this._processNamingPrivListGrantPriv( ns, p, (Element) it.next() );
    }
  } // private void _processNamingPrivList(map)

  // @since   1.1.0
  private void _processNamingPrivListGrantPriv(Stem ns, Privilege p, Element el)
    throws  GrantPrivilegeException,
            InsufficientPrivilegeException,
            SchemaException
  {
    if (!this._isSubjectElementImmediate(el)) {
      return;
    }
    try {
      // TODO 20061005 how should i handle subject resolution failure?
      Subject subj = this._findSubject( 
        el.getAttribute("id"), el.getAttribute("identifier"), el.getAttribute("type") 
      );
      if (!XmlUtils.hasImmediatePrivilege(subj, ns, p.getName())) {
        ns.grantPriv(subj, p);
      }
    }
    catch (SubjectNotFoundException eSNF)   {
      return;
    }
    catch (SubjectNotUniqueException eSNU)  {
      return;
    }
  } // private void _processNamingPrivListGrantPriv(ns, p, el)

  // @since   1.1.0
  private void _processPath(Element e, String stem) 
    throws  AttributeNotFoundException,
            GroupAddException,
            GrouperException,
            GroupModifyException,
            GroupNotFoundException,
            HibernateException,
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
    throws  HibernateException,
            InsufficientPrivilegeException,
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
    String  description   = e.getAttribute(GrouperConfig.ATTR_D);
    if (Validator.isNotNullOrBlank(description)) {
      child.setDescription(description);
    }
    this._setUuid(child, e);
    this._setInternalAttributes(child, e);
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
    if (this._isUpdatingAttributes()) {
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
            GroupNotFoundException,
            HibernateException,
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

  // @since   1.1.0
  private boolean _setCreateSubject(Owner o, Element e) {
    Element e0  = this._getImmediateElement(e, "subject");
    String  msg = "error setting createSubject: ";
    try {
      o.setCreator_id( 
        MemberFinder.findBySubject(
          e0.getAttribute("id"), e0.getAttribute("source"), e0.getAttribute("type")
        )
      );
      return true;
    }
    catch (MemberNotFoundException eMNF) {
      msg += eMNF.getMessage();
    }
    LOG.error(msg);
    return false;
  } // private boolean _setCreateSubject(o, e)

  // @since   1.1.0
  private boolean _setCreateTime(Owner o, Element e) {
    String msg = "error setting createTime: ";
    try { 
      o.setCreate_time( this._parseTime( XmlImporter._getText(e) ).getTime() );
      return true;
    }
    catch (GrouperException eG) {
      msg += eG.getMessage();
    }
    catch (ParseException eP)   {
      msg += eP.getMessage();
    }
    LOG.error(msg);
    return false;
  } // private boolean _setCreateTime(o, e)

  // @since   1.1.0
  private void _setInternalAttributes(Owner o, Element e) 
    throws  HibernateException
  {
    String    attr;
    boolean   modified    = false;
    Element   e0;
    Iterator  it          = this._getInternalAttributes(e).iterator();
    while (it.hasNext()) {
      e0    = (Element) it.next();
      attr  = e0.getAttribute("name");
      if      (attr.equals("createSubject"))  {
        if (this._setCreateSubject(o, e0)) {
          modified = true;
        }
      }
      else if (attr.equals("createTime"))     {
        if (this._setCreateTime(o, e0)) {
          modified = true;
        }
      }
    }
    if (modified) {
      HibernateHelper.save(o);
    }
  } // private void _setInternalAttributesAttributes(ns, e)

  // @since   1.1.0
  private void _setUuid(Owner o, Element e) 
    throws  HibernateException
  {
    String uuid = e.getAttribute("id");
    if (Validator.isNotNullOrBlank(uuid)) {
      o.setUuid(uuid);
      HibernateHelper.save(o);
    }
  } // private void _setUuid(o, e)


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

