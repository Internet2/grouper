/*
Copyright 2004-2005 University Corporation for Advanced Internet Development, Inc.
Copyright 2004-2005 The University Of Bristol

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

package uk.ac.bris.is.grouper.quickstart; 

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GroupNotFoundException;
import edu.internet2.middleware.grouper.GrouperHelper;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.Privilege;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.StemNotFoundException;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectNotFoundException;

/**
 * Utility class which reads an XML file representing all or part of a Grouper repository. 
 * Has a main method which takes a command line argument to specify the Xml file to be
 * processed. Alternatively, call public static void load(GrouperSession s,Document doc). The Grouper API libraries
 * and configuration files must be on the classpath.
 * <p />
 <p>The following XML illustrates the format understood by XmlLoader. Subjects 
  must be available through an adapter configured in sources.xml. the getSubjectByIdentifier 
  method is used to lookup subject ids.</p>
<pre>&lt;registry&gt;
  &lt;path extension=&quot;qsuob&quot; displayExtension=&quot;QS University of Bristol&quot;&gt;
    &lt;naming priv=&quot;CREATE&quot; group=&quot;qsuob:admins&quot;/&gt;
    &lt;naming priv=&quot;STEM&quot; group=&quot;.:admins&quot;/&gt;
    &lt;group extension=&quot;admins&quot; displayExtension=&quot;UoB Administrators&quot;&gt;
      &lt;access priv=&quot;ADMIN&quot; group=&quot;*SELF*&quot;/&gt;
      &lt;access priv=&quot;READ&quot; group=&quot;..:all_staff&quot;/&gt;<br>      &lt;subject id=&quot;kebe&quot; name=&quot;Keith Benson&quot; /&gt;<br>    &lt;/group&gt;
    &lt;group extension=&quot;all_staff&quot; displayExtension=&quot;All staff&quot;&gt;
      &lt;access priv=&quot;UPDATE&quot; group=&quot;*SELF*&quot;/&gt;
      &lt;access priv=&quot;READ&quot; group=&quot;*SELF*&quot;/&gt;
      &lt;access priv=&quot;ADMIN&quot; group=&quot;..:admins&quot;/&gt;
      &lt;access priv=&quot;ADMIN&quot; subject=&quot;kebe&quot;/&gt;<br>      &lt;subject id=&quot;kebe&quot; name=&quot;Keith Porter&quot; /&gt;
      &lt;subject group=&quot;faculties:artf:staff&quot; location=&quot;relative&quot;/&gt;<br>    &lt;/group&gt;
    &lt;path extension=&quot;faulties&quot; displayExtension=&quot;Faculties&quot;&gt;
      &lt;path extension=&quot;artf&quot; displayExtension=&quot;Art faculty&quot;&gt;
        &lt;group extension=&quot;staff&quot; displayExtension=&quot;staff&quot; description=&quot;Art faculty staff&quot;&gt;
        &lt;/group&gt;
      &lt;/path&gt;
    &lt;/path&gt;
  &lt;/path&gt;
&lt;/registry&gt; </pre>
<p><em>path</em> elements represent GrouperStems and <em>group</em> elements represent 
  GrouperGroups. Both elements must have <em>extension</em> and <em>displayExtension</em> 
  attributes and may have <em>description</em> attributes.</p>
<p><em>path</em> elements may contain other<em> path </em>elements, <em>group</em> 
  elements and <em>naming</em> elements. <em>naming</em> elements designate which 
  privilege (<em>priv</em> attribute) is granted to which subject (<em>person</em> 
  or <em>group</em> attribute).</p>
<p><em>group</em> elements may contain <em>access</em> elements which may have 
  the same attributes that <em>naming</em> elements have. They may also contain 
  <em>subject</em> elements which identify members through their <em>id</em> attribute 
  (person) or <em>group</em> attribute.</p>
<p>XmlLoader checks if stems, groups, memberships exist or if access or naming 
  privileges have been granted before execising the Grouper API to create objects 
  / grant privileges. If objects exist XmlLoader does not modify attributes even 
  if they are different. Existing memberships and privileges are not revoked if 
  they are not specified, but new ones will be added.</p>
<p>When assigning access privileges <em>group=&quot;*SELF*&quot;</em> indicates 
  the privilege should be granted to the group which is the parent of the access 
  element. For both access and naming privileges, if the<em> group</em> attribute 
  starts .: or ..: a relative group name is assumed and resolved to an absolute 
  name much as you would expect in DOS or UNIX command lines when moving in the 
  file system - except : is a separator rather than \ or /..</p>
<p>When making one group a memer of another group adding an attribute<em> location=&quot;relative&quot;</em> 
  causes the group atribute value to be added to the stem of the parent group 
  to determine an absolute group name for the member.</p>
<p></p>
 * 
 * @author Gary Brown.
 * @version $Id: XmlLoader.java,v 1.3 2005-12-14 14:46:21 isgwb Exp $
 */
public class XmlLoader {
	private final static String sep=":";
	private static GrouperSession s;
	private static List memberships = new ArrayList();
	private static List accessPrivs = new ArrayList();
	private static List namingPrivs = new ArrayList();
	
	/**
	 * Process an Xml file as the 'root' user.
	 * @param args args[0] = name of Xml file to process
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception{
		Subject sys = SubjectFinder.findById("GrouperSystem");
		GrouperSession s = GrouperSession.start(sys);
		String dataFile=args[0];
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document doc = db.parse(new File(dataFile));
		XmlLoader.load(s,doc);
		s.stop();
	}
	
	/**
	 * Recurse through the XML document and create any stems / groups, accumulating 
	 * memberships and privilege assignments for later - this ensures that any groups
	 * which will become members or have privileges granted to them actually exist!
	 * Create any memberships
	 * Grant naming privileges
	 * Grant access privileges
	 * @param s
	 * @param doc
	 * @throws Exception
	 */
	public static void load(GrouperSession s,Document doc) throws Exception {
		XmlLoader.s = s;
		Element root = doc.getDocumentElement();
		System.out.println("Starting load...");
		process(root,GrouperHelper.NS_ROOT);
		processMemberships();
		processNamingPrivs();
		processAccessPrivs();
		System.out.println("Ending load...");
	}
	
	/**
	 * For each stem list and process any child stems.
	 * List and process any child groups
	 * @param e
	 * @param stem
	 * @throws Exception
	 */
	private static void process(Element e,String stem) throws Exception {	
		Collection paths = getImmediateElements(e,"path");
		Iterator it = paths.iterator();
		while(it.hasNext()) {
			Element path = (Element)it.next(); 
			processPath(path,stem);
		}
		
		Collection groups = getImmediateElements(e,"group");
		it = groups.iterator();
		while(it.hasNext()) {
			Element group = (Element)it.next();
			processGroup(group,stem);
		}
		
	}
	
	private static void processNamingPrivs() throws Exception {
		Element naming;
		String stem;
		Member member = null;
		Stem grouperStem;
		Map map;
		String group;
		String subject;
		String priv;
		Subject subj;
		String absoluteGroup;
		Group privGroup;
		
		for(int i=0;i<namingPrivs.size();i++) {
			
			map = (Map)namingPrivs.get(i);
			naming = (Element)map.get("naming");
			stem = (String) map.get("stem");
			group=naming.getAttribute("group");
			subject=naming.getAttribute("subject");
			priv=naming.getAttribute("priv").toLowerCase();
			if(!isEmpty(group)) {
				absoluteGroup = getAbsoluteName(group,stem);
				privGroup = GroupFinder.findByName(s,absoluteGroup);
				member = MemberFinder.findBySubject(s,SubjectFinder.findById(privGroup.getUuid(),"group"));
				
				System.out.println("Assigning " + priv + " to " + absoluteGroup + " for " + stem);
			}else if(!isEmpty(subject)) {
				try {
					subj = SubjectFinder.findByIdentifier(subject);
				}catch(SubjectNotFoundException e){
					subj = SubjectFinder.findById(subject);
				}
				member = MemberFinder.findBySubject(s,subj);
				System.out.println("Assigning " + priv + " to " + subj.getName() + " for " + stem);
			}
			
			grouperStem = StemFinder.findByName(s,stem);
			if(!GrouperHelper.hasSubjectImmPrivForStem(s,member.getSubject(),grouperStem,priv)) {
				grouperStem.grantPriv(member.getSubject(),Privilege.getInstance(priv));
			}
		}
	}
	
	private static void processAccessPrivs() throws Exception {
		Element access;
		String stem;
		Member member=null;
		Group grouperGroup;
		Map map;
		String group;
		String subject;
		String priv;
		String absoluteGroup;
		Group privGroup;
		Subject subj;
		for(int i=0;i<accessPrivs.size();i++) {
			
			map = (Map)accessPrivs.get(i);
			access = (Element)map.get("access");
			stem = (String) map.get("stem");
			group=access.getAttribute("group");
			subject = access.getAttribute("subject");
			priv=access.getAttribute("priv").toLowerCase();
			grouperGroup = GroupFinder.findByName(s,stem);
			if(!isEmpty(group)) {
				absoluteGroup = getAbsoluteName(group,stem);
				privGroup = GroupFinder.findByName(s,absoluteGroup);
				member = MemberFinder.findBySubject(s,SubjectFinder.findById(privGroup.getUuid(),"group"));
				
				System.out.println("Assigning " + priv + " to " + absoluteGroup + " for " + stem);
			}else if(!isEmpty(subject)) { 
				try {
					subj = SubjectFinder.findByIdentifier(subject);
				}catch(SubjectNotFoundException e){
					subj = SubjectFinder.findById(subject);
				}
				member = MemberFinder.findBySubject(s,subj);
				System.out.println("Assigning " + priv + " to " + subj.getName() + " for " + stem);
			}
			if(!GrouperHelper.hasSubjectImmPrivForGroup(s,member.getSubject(),grouperGroup,priv)) {
				grouperGroup.grantPriv(member.getSubject(),Privilege.getInstance(priv));
			}
		}
	}
	
	public static String getAbsoluteName(String name,String stem) {
		if("*SELF*".equals(name))return stem;
		if(name!=null && name.startsWith(".")) {
			if(name.startsWith("." + sep)) {
				name=stem + name.substring(1);
			}else {
				while(name.startsWith(".." + sep)) {
					name = name.substring(3);
					stem = stem.substring(0,stem.lastIndexOf(sep));
				}
				name = stem + sep + name;
			}
		}
		return name;
	}
	
	private static void processMemberships() throws Exception {
		Element subject;
		String stem;
		Member member;
		Group group;
		Map map;
		for(int i=0;i<memberships.size();i++) {
			map = (Map)memberships.get(i);
			subject = (Element)map.get("subject");
			stem = (String) map.get("stem");
			String id = subject.getAttribute("id");
			group = GroupFinder.findByName(s,stem);
			if(id !=null && id.length()!=0) {
				member = MemberFinder.findBySubject(s,SubjectFinder.findById(id,"person"));
				
				if(group!=null && !group.hasMember(member.getSubject())) group.addMember(member.getSubject());
			}else{
				String groupName = subject.getAttribute("group");
				if(groupName!=null && groupName.length()!=0) {
					if("relative".equals(subject.getAttribute("location"))) {
						groupName = group.getParentStem().getName() + sep + groupName;	
					}
					Group groupSubj = GroupFinder.findByName(s,groupName);
					member = MemberFinder.findBySubject(s,SubjectFinder.findById(groupSubj.getUuid(),"group"));
					if(group!=null && !group.hasMember(member.getSubject())) group.addMember(member.getSubject());
				}
			}
		}
	}
	
	private static void processPath(Element e,String stem) throws Exception {
		String extension = e.getAttribute("extension");
		String displayExtension = e.getAttribute("displayExtension");
		String description = e.getAttribute("description");
		String newStem = joinStem(stem,extension);
		Stem existingStem = null;
		try {
			existingStem = StemFinder.findByName(s,newStem);
		}catch(StemNotFoundException ex) {}
		if(existingStem==null) {
			Stem parent = null;
			try {
				parent = StemFinder.findByName(s,stem);
			}catch(StemNotFoundException ex) {
				if(GrouperHelper.NS_ROOT.equals(stem)) parent = StemFinder.findRootStem(s);
			}
			Stem gs = parent.addChildStem(extension,displayExtension);
			if(description!=null && description.length()!=0) gs.setDescription(description);
		}
		processNaming(e,newStem.replaceAll(GrouperHelper.NS_ROOT + sep,""));
		process(e,newStem.replaceAll(GrouperHelper.NS_ROOT + sep,""));
	}
	
	private static void processGroup(Element e,String stem) throws Exception {
		String extension = e.getAttribute("extension");
		String displayExtension = e.getAttribute("displayExtension");
		String description = e.getAttribute("description");
		String newGroup = joinStem(stem,extension);
		Group existingGroup=null;
		try {
			existingGroup=GroupFinder.findByName(s,newGroup);
		}catch(GroupNotFoundException ex){}
		if(existingGroup==null) {
			Stem parent = StemFinder.findByName(s,stem);
			Group gg = parent.addChildGroup(extension,displayExtension);
			if(description!=null && description.length()!=0) gg.setDescription(description);		}
		processSubjects(e,newGroup);
		processAccess(e,newGroup);
		
	}
	
	
	private static void processSubjects(Element e,String stem) throws Exception{
		Collection subjects = getImmediateElements(e,"subject");
		Iterator it = subjects.iterator();
		Element subject;
		Map map;
		while(it.hasNext()) {
			subject = (Element)it.next();
			map = new HashMap();
			map.put("stem",stem);
			map.put("subject",subject);
			memberships.add(map);
		}
		
	}
	
	private static void processAccess(Element e,String stem) throws Exception{
		Collection accesses = getImmediateElements(e,"access");
		Iterator it = accesses.iterator();
		Element access;
		Map map;
		while(it.hasNext()) {
			access = (Element)it.next();
			map = new HashMap();
			map.put("stem",stem);
			map.put("access",access);
			accessPrivs.add(map);
		}
	}
	
	private static void processNaming(Element e,String stem) throws Exception{
		Collection namings = getImmediateElements(e,"naming");
		Iterator it = namings.iterator();
		Element naming;
		Map map;
		while(it.hasNext()) {
			naming = (Element)it.next();
			map = new HashMap();
			map.put("stem",stem);
			map.put("naming",naming);
			namingPrivs.add(map);
		}
	}
	
	
	
	private static String joinStem(String stem,String extension) {
		if(stem.equals(GrouperHelper.NS_ROOT)) return extension;
		return stem + sep + extension;
	}
	
	/**
    * Returns immediate child elements with given name
    */
    public static Collection getImmediateElements(Element element,String elementName) throws Exception {
		NodeList nl = element.getElementsByTagName(elementName);
		Collection elements = new Vector();
		if(nl.getLength()<1) {
			return elements;
		}			
		Element child;
		for(int i=0;i<nl.getLength();i++) {
			child = (Element) nl.item(i);
			if(child.getParentNode().equals(element)) {
				elements.add(child);	
			}	
		}
		return elements;	
	 }
    
    private static boolean isEmpty(Object obj) {
    	if(obj==null || "".equals(obj))return true;
    	return false;
    }
}
