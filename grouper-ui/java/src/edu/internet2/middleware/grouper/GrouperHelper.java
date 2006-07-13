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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import edu.internet2.middleware.grouper.ui.GroupOrStem;
import edu.internet2.middleware.grouper.ui.PersonalStem;
import edu.internet2.middleware.grouper.ui.UIThreadLocal;
import edu.internet2.middleware.grouper.ui.util.GroupAsMap;
import edu.internet2.middleware.grouper.ui.util.MembershipAsMap;
import edu.internet2.middleware.grouper.ui.util.ObjectAsMap;
import edu.internet2.middleware.grouper.ui.util.StemAsMap;
import edu.internet2.middleware.grouper.ui.util.SubjectAsMap;
import edu.internet2.middleware.grouper.ui.util.SubjectPrivilegeAsMap;
import edu.internet2.middleware.subject.Source;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectNotFoundException;
import edu.internet2.middleware.subject.SubjectNotUniqueException;
import edu.internet2.middleware.subject.SubjectType;
import edu.internet2.middleware.subject.provider.SourceManager;

/**
 * High level wrapper methods for the Grouper API, including workarounds. 
 * Primarily developed for the UI.<p />
 * Occasionally takes advantage of protected access to the Grouper API, however,
 * this access should not be required once the API is fully implemented.
 * <p />
 * 
 * @author Gary Brown.
 * @version $Id: GrouperHelper.java,v 1.15 2006-07-13 19:09:46 isgwb Exp $
 */

/**
 * @author isgwb
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class GrouperHelper {
	public static HashMap list2privMap;
	static {
		list2privMap = new HashMap();
		list2privMap.put("admins","admin");
		list2privMap.put("optins","optin");
		list2privMap.put("optouts","optout");
		list2privMap.put("readers","read");
		list2privMap.put("updaters","update");
		list2privMap.put("viewers","view");
		list2privMap.put("stemmers","stem");
		list2privMap.put("creators","create");
		list2privMap.put("members","MEMBER");
	}

	private static Map superPrivs = null; //Privs automatically granted to the
										  // system user

	private static List personSources = null; //Subject sources which source
											  // 'people'

	public static final String HIER_DELIM = ":"; //Currently :
																// (name
																// separator)
	public static final String NS_ROOT = "Grouper.NS_ROOT";
	//List names for access privileges
	private static final String[] listNames = new String[] { "members",
			"admins", "updaters", "readers", "viewers", "optins", "optouts" };
	
	//Initialise system user privs
	static {
		superPrivs = new HashMap();
		superPrivs.put("READ", Boolean.TRUE);
		superPrivs.put("VIEW", Boolean.TRUE);
		superPrivs.put("UPDATE", Boolean.TRUE);
		superPrivs.put("ADMIN", Boolean.TRUE);
		superPrivs.put("CREATE", Boolean.TRUE);
		superPrivs.put("STEM", Boolean.TRUE);
		//superPrivs.put("OPTIN", Boolean.TRUE);
		//superPrivs.put("OPTOUT", Boolean.TRUE);
	}
	
	//Privs which relate to Groups - access privileges
	private static String[] groupPrivs = { "ADMIN", "UPDATE", "READ", "VIEW",
			"OPTIN", "OPTOUT" };
	
//	Privs which relate to Groups - access privileges + member
	private static String[] groupPrivsWithMember = { "ADMIN", "UPDATE", "READ", "VIEW",
			"OPTIN", "OPTOUT","MEMBER" };
	
	//Privs which relate to Stems - naming privileges
	private static String[] stemPrivs = { "STEM", "CREATE" };

	public static void main(String args[]) throws Exception{
		Subject subj = SubjectFinder.findById("GrouperSystem");
		GrouperSession s = GrouperSession.start(subj);

		//GroupType type = GroupType.createType(s,"teaching");
		GroupType type = GroupTypeFinder.find("committee");
		/*type.addField(s,"enforcer",FieldType.ATTRIBUTE,Privilege.getInstance("read"),Privilege.getInstance("update"),true);
		type = GroupTypeFinder.find("mailingList");
		type.addField(s,"alias",FieldType.ATTRIBUTE,Privilege.getInstance("read"),Privilege.getInstance("update"),true);
		type = GroupTypeFinder.find("studentUnion");
		type.addField(s,"campus",FieldType.ATTRIBUTE,Privilege.getInstance("read"),Privilege.getInstance("update"),true);
		type = GroupTypeFinder.find("personal");
		type.addField(s,"proxy",FieldType.ATTRIBUTE,Privilege.getInstance("read"),Privilege.getInstance("update"),true);
		*/
		type = GroupTypeFinder.find("community");
		type.addList(s,"contributors",Privilege.getInstance("read"),Privilege.getInstance("update"));
		type.addAttribute(s,"scope",Privilege.getInstance("read"),Privilege.getInstance("update"),true);
		type = GroupTypeFinder.find("staff");
		type.addAttribute(s,"dept",Privilege.getInstance("read"),Privilege.getInstance("update"),true);
		/*type.addField(s,"staff",FieldType.LIST,Privilege.getInstance("read"),Privilege.getInstance("update"),false);
		type.addField(s,"clerical",FieldType.LIST,Privilege.getInstance("read"),Privilege.getInstance("update"),false);
		type.addField(s,"faculty_code",FieldType.ATTRIBUTE,Privilege.getInstance("read"),Privilege.getInstance("update"),true);
		type.addField(s,"org_code",FieldType.ATTRIBUTE,Privilege.getInstance("read"),Privilege.getInstance("update"),true);
		
		Group g = new Group(s,StemFinder.findRootStem(s),"tGroup","Teaching Group");
		g.addType(type);
		g.setAttribute("faculty_code","ARTF");
		g.setAttribute("org_code","FREN");
		Set x = SubjectFinder.findAll("keith"); 
		Iterator it = x.iterator();
		Subject subject;
		while(it.hasNext()) {
			subject=(Subject)it.next();
			g.addMember(subject,FieldFinder.find("staff"));
		}
		
		x = SubjectFinder.findAll("fiona");
		it = x.iterator();
		
		while(it.hasNext()) {
			subject=(Subject)it.next();
			g.addMember(subject,FieldFinder.find("clerical"));
		}
		
		
		
		
		*/
		
		s.stop();
	}


	/**
	 * Given a GrouperStem id return a list of stems and groups for which the
	 * GrouperStem is an immediate parent
	 * @param s GrouperSession for authenticated user
	 * @param stemId GrouperStem id
	 * @return List of all stems and groups for stemId
	 */
	public static List getChildren(GrouperSession s, String stemId) throws StemNotFoundException{
		Stem stem =null;
		if("".equals(stemId)) {
			stem=StemFinder.findRootStem(s);
		}else{
			stem=StemFinder.findByName(s, stemId);
		}
		ArrayList res = new ArrayList();
		Set children = stem.getChildStems();
		Iterator it = children.iterator();
		Stem childStem = null;
		while(it.hasNext()) {
			childStem=(Stem)it.next();
			res.add(GroupOrStem.findByStem(s,childStem));
		}
		children=stem.getChildGroups();
		it = children.iterator();
		Group childGroup = null;
		while(it.hasNext()) {
			childGroup=(Group)it.next();
			res.add(GroupOrStem.findByGroup(s,childGroup));
		}
		return res;
	}

	/**
	 * Given a list of GrouperAttributes, return a list of GrouperStems
	 * which the attributes belong to, and load all atributes for these Stems
	 * 
	 * @param s GrouperSession for authenticated user
	 * @param list of GrouperAttributes
	 * @return List of GrouperGroups or GrouperStems
	 */
	/*public static List instantiateStems(GrouperSession s, List list) {
		return instantiateGroups(s, list);
	}*/



	/**
	 * Given a list of GrouperAttributes, return a list of GrouperGroups
	 * which the attributes belong to, and load all atributes for these groups
	 * 
	 * @param s GrouperSession for authenticated user
	 * @param list of GrouperAtributes
	 * @return List of GrouperGroups or GrouperStems
	 */
	/*public static List instantiateGroups(GrouperSession s, List list) {
		List instantiated = new ArrayList();
		Attribute attr = null;
		String key;
		Stem stem = null;
		for (int i = 0; i < list.size(); i++) {
			attr = (Attribute) list.get(i);
			key = attr.key();

			//stem=Cache.instance().getGroup(s,key);
			if (stem == null) {
				try {
				stem = (GrouperGroup) GrouperGroup.loadByKey(s, key);
				}catch(InsufficientPrivilegeException e) {
					throw new RuntimeException(e);
				}
				//Cache.instance().put(s,key,stem);
			}
			instantiated.add(stem);
		}
		return instantiated;
	}*/

	/**
	 * Given a GrouperStem id return a list of Maps representing the children
	 * of that stem. 
	 * 
	 * @param s GrouperSession for authenticated user
	 * @param stemId
	 * @return List of GrouperGroups and GrouperStems wrapped as Maps
	 */
	public static List getChildrenAsMaps(GrouperSession s, String stemId) throws StemNotFoundException{
		List stems = getChildren(s, stemId);
		List maps = new ArrayList();
		GroupOrStem groupOrStem = null;
		for (int i = 0; i < stems.size(); i++) {
			groupOrStem = (GroupOrStem)stems.get(i);
			maps.add(groupOrStem.getAsMap());
		}
		return maps;
	}

	/**
	 * Given a list of GrouperList objects return a list of instantiated
	 * GrouperGroups as Maps
	 * 
	 * @param s GrouperSession for authenticated user
	 * @param groups List of GrouperLists
	 * @return List of GrouperGroups wrapped as Maps
	 */
	public static List groups2Maps(GrouperSession s, List groups) throws GroupNotFoundException{
		List maps = new ArrayList();
		Object obj;
		for (int i = 0; i < groups.size(); i++) {
			//Just in case something goes wrong - Group doesn't exist but still
			// a pointer to it
			try {
				obj = groups.get(i);
				if (obj instanceof Membership)
					obj = ((Membership) obj).getGroup();
				maps.add(group2Map(s, (Group) obj));
			} catch (NullPointerException e) {
				//@TODO What should happen?
			}
		}
		return maps;

	}

	/**
	 * Given a ist of GrouperList objects return a list of instantiated GrouperStems
	 * as Maps
	 * @param s GrouperSession for authenticated user
	 * @param stems List of GrouperLists
	 * @return List of GrouperStems wrapped as Maps
	 */
	public static List stems2Maps(GrouperSession s, List stems) throws GroupNotFoundException{
		List maps = new ArrayList();
		Object obj;
		for (int i = 0; i < stems.size(); i++) {
			//Just in case something goes wrong - Group doesn't exist but still
			// a pointer to it
			try {
				obj = stems.get(i);
				
				maps.add(stem2Map(s, (Stem) obj));
			} catch (NullPointerException e) {
				//@TODO What should happen?
			}
		}
		return maps;

	}
	
	/**
	 * Given a GrouperStem return a Map representing it
	 * @param s GrouperSession for authenticated user
	 * @param stem GrouperStem to wrap
	 * @return GrouperStem wrapped as a Map
	 */
	public static Map stem2Map(GrouperSession s, Stem stem) {
		Map stemMap = new StemAsMap(stem,s);
		if("".equals(stem.getName())) {
			stemMap.put("isRootStem",Boolean.TRUE);
		}
		return stemMap;

	}
	
	/**
	 * Given a Stem return a Map representing it
	 * @param s GrouperSession for authenticated user
	 * @param stem GrouperStem to wrap
	 * @return Stem wrapped as a Map
	 */
	public static Map group2Map(GrouperSession s, Stem stem) {
		return new StemAsMap(stem,s);

	}
	
	/**
	 * Given a GroupOrStem return a Map representing it
	 * @param s GrouperSession for authenticated user
	 * @param stem GroupOrStem to wrap
	 * @return GroupOrStem wrapped as a Map
	 */
	public static Map group2Map(GrouperSession s, GroupOrStem groupOrStem) {
		return groupOrStem.getAsMap();
	}

	/**
	 * Given a Group  return
	 * a Map representation of it
	 * @param s GrouperSession for authenticated user
	 * @param Group to wrap
	 * @return Group wrapped as a Map
	 */
	public static Map group2Map(GrouperSession s, Group group){ 
		ObjectAsMap map = new GroupAsMap(group, s);
			if (map.get("displayExtension") == null)
				map.put("displayExtension", map.get("extension"));
		return (Map) map;
	}

	/**
	 * Given a GrouperGroup or GrouperStem return a list of ancestor GrouperStems
	 * as Maps
	 * 
	 * @param s GrouperSession for authenticated user
	 * @param groupOrStem GrouperGroup or GrouperStem
	 * @return List of ancestor GrouperStems wrapped as Maps
	 */
	public static List parentStemsAsMaps(GrouperSession s, GroupOrStem groupOrStem) throws StemNotFoundException{
		List path = new ArrayList();
		if(groupOrStem==null) return path;
		Map map = group2Map(s, groupOrStem);

		Stem curStem = null;
		while (!GrouperHelper.NS_ROOT.equals(map.get("stem"))) {
			curStem = StemFinder.findByName(s, (String) map.get("stem"));
			if (curStem != null) {
				map = stem2Map(s, curStem);
				path.add(0, map);
			}
		}
		return path;
	}

	
	/**
	 * Given a GrouperGroup or GrouperStem return a Map where the keys
	 * are access or naming privileges that s.subject() has.
	 * 
	 * @param s GrouperSession for authenticated user
	 * @param group GrouperGroup or GroupeStem for which privileges are being requested
	 * @return Map representing privileges
	 */
	public static Map hasAsMap(GrouperSession s, GroupOrStem groupOrStem) throws MemberNotFoundException{
		return hasAsMap(s, groupOrStem, false);
	}

	/**
	 * Given a GrouperGroup or GrouperStem return a Map where the keys are
	 * access or naming privileges that s.subject() has. If isMortal == false
	 * and the subject is the system user, all privileges are returned.
	 * @param s GrouperSession for authenticated user
	 * @param groupOrStem GrouperGroup or GroupeStem for which privileges are being requested
	 * @param isMortal if system user should they be teated as such
	 * @return Map representing privileges
	 */
	public static Map hasAsMap(GrouperSession s, GroupOrStem groupOrStem,
			boolean isMortal) throws MemberNotFoundException{
		Map privs = null;

		Group g = null;
		Stem stem = null;
		boolean isActiveWheelGroupMember = Boolean.TRUE.equals(UIThreadLocal.get("isActiveWheelGroupMember"));
		privs = new HashMap();
		if (!isMortal
				&& ("GrouperSystem".equals(s.getSubject().getId())
						|| isActiveWheelGroupMember)) {
			privs.putAll(superPrivs);
			if(groupOrStem==null) return privs;
			
			if (groupOrStem.isGroup()) {
				g = groupOrStem.getGroup();
				
				if (g.hasMember(s.getSubject())) {
					privs.put("MEMBER", Boolean.TRUE);
				}
			} else {
				stem = groupOrStem.getStem();
			}
			if (privs == null)
				privs = superPrivs;
			return privs;
		}
		if("GrouperSystem".equals(s.getSubject().getId())
				||isActiveWheelGroupMember) {
			privs = new HashMap();
			privs.put("STEM",Boolean.TRUE);
			if(groupOrStem!=null && groupOrStem.isStem()&& !"".equals(groupOrStem.getStem().getName())) {
				privs.put("CREATE",Boolean.TRUE);
			}
			return privs;
		}
		
		if(groupOrStem==null) return new HashMap();
		
			g = groupOrStem.getGroup();
		
			stem = groupOrStem.getStem();
		
		Set privList = null;
		if (g != null) {
			privList = g.getPrivs(s.getSubject());
		} else {
			privList = stem.getPrivs(s.getSubject());
		}
		if(privList !=null) {
			Iterator it = privList.iterator();
			Object p = null;
			while(it.hasNext()){
				p=it.next();
				if(p instanceof AccessPrivilege) {
					privs.put(((AccessPrivilege)p).getName().toUpperCase(), Boolean.TRUE);
				}else if(p instanceof NamingPrivilege) {
					privs.put(((NamingPrivilege)p).getName().toUpperCase(), Boolean.TRUE);
				}else{
					privs.put(it.next(), Boolean.TRUE);
				}
			}
		}
		if (g != null) {
			
			if (g.hasMember(s.getSubject()))
				privs.put("MEMBER", Boolean.TRUE);
		}
		return privs;
	}

	/**
	 * Given a GrouperGroup or GrouperStem return a Map where the keys
	 * are access or naming privileges that member.subject() has.
	 * 
	 * @param s GrouperSession for authenticated user
	 * @param groupOrStem GrouperGroup or GroupeStem for which privileges are being requested
	 * @param member Subject who privileges were granted to
	 * @return Map representing privileges
	 */
	public static Map hasAsMap(GrouperSession s, GroupOrStem groupOrStem,
		Member member,Field field) throws SubjectNotFoundException,SchemaException{
		Map privs = null;
		if ("GrouperSystem".equals(member.getSubjectId())) {
			//@TODO Review
			//return superPrivs;

		}
		Set privList = null;

		privs = new HashMap();
		Group group = null;
		Stem stem = null;
		if (groupOrStem.isGroup()) {
			group = groupOrStem.getGroup();
			privList = group.getPrivs(member.getSubject());
		} else {
			stem = groupOrStem.getStem();
			privList = stem.getPrivs(member.getSubject());
		}
		Iterator it = privList.iterator();
		Object obj;
		String p;
		while(it.hasNext()) {
			obj=it.next();
			if(obj instanceof AccessPrivilege) {
				p = ((AccessPrivilege)obj).getName();
			}else{
				p = ((NamingPrivilege)obj).getName();
			}
			privs.put(p.toUpperCase(), Boolean.TRUE);
		}
		if (group != null) {
			if (group.hasMember(member.getSubject(),field))
				privs.put("MEMBER", Boolean.TRUE);
		}

		return privs;
	}

	/** Given a Subject return a Map representation of it
	 * @param subject to be wrapped
	 * @return Subject wrapped as a Map
	 */
	public static Map subject2Map(Subject subject) {
		//@TODO what should happen if Group - see next method
		SubjectAsMap map = new SubjectAsMap(subject);
		return (Map) map;
	}
	
	/** Given a Subject return a Map representation of it
	 * @param subject to be wrapped
	 * @param addAttr Map of additional attributes
	 * @return Subject wrapped as a Map
	 */
	public static Map subject2Map(Subject subject,Map addAttr) {
		//@TODO what should happen if Group - see next method
		SubjectAsMap map = new SubjectAsMap(subject);
		if(addAttr !=null) map.putAll(addAttr);
		return (Map) map;
	}

	/**
	 * Given a subject id and subject type and a Map, return a Map representation of the
	 * subject and add the key/value pairs from the input Map.
	 * 
	 * @param s GrouperSession for authenticated user
	 * @param subjectId Subject id
	 * @param subjectType Subject type e.g. person, group
	 * @param addAttr Map of aditional attributes
	 * @return Subject wrapped as a Map
	 */
	public static Map subject2Map(GrouperSession s, String subjectId,
			String subjectType,Map addAttr) throws SubjectNotFoundException{
		Map subjectMap = subject2Map(s,subjectId,subjectType);
		if(addAttr !=null) subjectMap.putAll(addAttr);
		return subjectMap;
	}
	
	/**
	 * Given a subject id and subject type return a Map representation of it.
	 * 
	 * @param s GrouperSession for authenticated user
	 * @param subjectId Subject id
	 * @param subjectType Subject type e.g. person, group
	 * @return Subject wrapped as a Map
	 */
	public static Map subject2Map(GrouperSession s, String subjectId,
			String subjectType) throws SubjectNotFoundException{
		if (!"group".equals(subjectType)) {
			Subject subject = null;
			try {
				subject = SubjectFinder.findById(subjectId, subjectType);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
			SubjectAsMap map = new SubjectAsMap(subject);
			return (Map) map;
		}
		try {
		Group group = GroupFinder.findByUuid(s, subjectId);
		Map groupMap = group2Map(s, group);
		return groupMap;
		}catch(GroupNotFoundException e) {
			throw new SubjectNotFoundException(e.getMessage());
		}
	}
	
	/**
	 * Given an array of Subjects return a List of Maps representing those subjects
	 * 
	 * @param objects array of Subjects
	 * @param addAttr Map of additional attributes
	 * @return List of Subjects wrapped as Maps
	 */
	public static List subjects2Maps(Object[] objects,Map addAttr) {
		if (objects instanceof Subject[])
			return subjects2Maps((Subject[]) objects);
		Subject[] subjects = new Subject[objects.length];
		for (int i = 0; i < objects.length; i++) {
			subjects[i] = (Subject) objects[i];
		}
		return subjects2Maps(subjects,addAttr);
	}
	
	/**
	 * Given an array of Subjects return a List of Maps representing those subjects
	 * 
	 * @param objects array of Subjects
	 * @return List of Subjects wrapped as Maps
	 */
	public static List subjects2Maps(Object[] objects) {
		
		return subjects2Maps(objects,null);
	}

	/**
	 * Given an array of Subjects return a List of Maps representing those subjects
	 * 
	 * @param objects array of Subjects
	 * @param addAttr Map of aditional attributes
	 * @return List of Subjects wrapped as Maps
	 */
	public static List subjects2Maps(Subject[] subjects,Map addAttr) {
		List maps = new ArrayList();
		for (int i = 0; i < subjects.length; i++) {
			maps.add(subject2Map(subjects[i],addAttr));
		}
		return maps;
	}
	
	/**
	 * Given an array of Subjects return a List of Maps representing those subjects
	 * 
	 * @param objects array of Subjects
	 * @return List of Subjects wrapped as Maps
	 */
	public static List subjects2Maps(Subject[] subjects) {
		List maps = new ArrayList();
		for (int i = 0; i < subjects.length; i++) {
			maps.add(subject2Map(subjects[i]));
		}
		return maps;
	}

	
	/**
	 * Given a list of GrouperList objects return a sublist from 
	 * start to start + pageSize of each GrouperList.member() as a Map
	 *  
	 * @param s GrouperSession for authenticated user
	 * @param members List of GrouperLists or GrouperMembers
	 * @param start of sublist
	 * @param pageSize number of results to return
	 * @return List of Subjects wrapped as Maps
	 */
	public static List groupList2SubjectsMaps(GrouperSession s, List members,
			int start, int pageSize) 
		throws GroupNotFoundException,SubjectNotFoundException,
		MemberNotFoundException,SubjectNotUniqueException{
		return groupList2SubjectsMaps(s, members, null, start, pageSize);
	}

	/**
	 * Given a list of GrouperMembers return a list of Map representations of them
	 * @param s GrouperSession for authenticated user
	 * @param members List of GrouperLists or GrouperMembers
	 * @return List of Subjects wrapped as Maps
	 */
	public static List groupList2SubjectsMaps(GrouperSession s, List members) 
		throws GroupNotFoundException,SubjectNotFoundException,
		MemberNotFoundException,SubjectNotUniqueException{
		return groupList2SubjectsMaps(s, members, null);
	}

	/**
	 * Given a list of GrouperMembers return a list of Map representations of them
	 * where the key asMemberOf is set to the value of the parameter asMemberOf
	 * @param s GrouperSession for authenticated user
	 * @param members List of GrouperLists or GrouperMembers
	 * @param asMemberOf GrouperGroup id identifying context
	 * @return List of Subjects wrapped as Maps
	 */
	public static List groupList2SubjectsMaps(GrouperSession s, List members,
			String asMemberOf) 
			throws GroupNotFoundException,SubjectNotFoundException,
			MemberNotFoundException,SubjectNotUniqueException{
		return groupList2SubjectsMaps(s, members, asMemberOf, 0, members.size());
	}

	/**
	 * Given a list of GrouperMembers return a list of Map representations of 
	 * a sublist of them (from start to start + pageSize) where the key 
	 * asMemberOf is set to the value of the parameter asMemberOf
	 * @param s GrouperSession for authenticated user
	 * @param members List of GrouperLists or GrouperMembers
	 * @param asMemberOf GrouperGroup id identifying context
	 * @param start of sublist
	 * @param pageSize number of results to return
	 * @return List of Subjects wrapped as Maps
	 */
	public static List groupList2SubjectsMaps(GrouperSession s, List members,
			String asMemberOf, int start, int pageSize) 
			throws GroupNotFoundException,SubjectNotFoundException,
				MemberNotFoundException,SubjectNotUniqueException{
		int end = start + pageSize;
		if (end > members.size())
			end = members.size();
		List maps = new ArrayList();
		Membership list = null;
		Member member = null;
		Subject subject=null;
		Map subjMap = null;
		Object listItem;
		Group via = null;
		Set chain = null;
		Object chainItem = null;
		Group firstInChain = null;
		Member chainMember = null;
		String[] chainGroupIds = null;
		int chainSizeAdjustment=1;
		String[] emptyStrArray=new String[]{};
		boolean isChainSameAsList = false;
		for (int i = start; i < end; i++) {
			chainGroupIds = emptyStrArray;
			listItem = members.get(i);
			if (listItem instanceof Membership) {
				list = (Membership) listItem;
				try{
					via = (Group) list.getViaGroup();
				}catch(GroupNotFoundException e){via=null;}
				chain = list.getChildMemberships();

				if (chain != null && chain.size() > 0) {
					//chainGroupIds = getChainGroupIds(s,list);
					
				} else {
					firstInChain = null;
				}
				member = list.getMember();
				try {
					subject = member.getSubject();
				} catch (Exception e) {
					throw new RuntimeException(e);
				}

			} else if (listItem instanceof Membership) {
				member = (Member) list.getMember();
				try {
					subject = member.getSubject();
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}else if(listItem instanceof Subject) {
				subject = (Subject)listItem;
			}else if(listItem instanceof Group) {
				
				Subject subj = SubjectFinder.findById(asMemberOf);
				Map gSubjMap = subject2Map(subj);
				Map gMap = group2Map(s,(Group)listItem);
				gSubjMap.put("memberOfGroup",gMap);
				gSubjMap.put("asMemberOf",((Group)listItem).getUuid());
				maps.add(gSubjMap);
				continue;
			}else if(listItem instanceof Stem) {
				
				Subject subj = SubjectFinder.findById(asMemberOf);
				Map sSubjMap = subject2Map(subj);
				Map sMap = stem2Map(s,(Stem)listItem);
				sSubjMap.put("memberOfGroup",sMap);
				sSubjMap.put("asMemberOf",((Stem)listItem).getUuid());
				maps.add(sSubjMap);
				continue;
			}
			
			if (subject.getType().getName().equals("group")) {
				Group group = GroupFinder.findByUuid(s, subject.getId());
				subjMap = group2Map(s, group);
			} else {
				subjMap = subject2Map(subject);
			}
			if (firstInChain != null)
				subjMap.put("via", group2Map(s, firstInChain));
			//Group group = list.getGroup();
			
			if (asMemberOf != null) {
				try{
					subjMap.put("memberOfGroup",group2Map(s,GroupFinder.findByUuid(s,asMemberOf)));
					subjMap.put("asMemberOf", asMemberOf);
				}catch(GroupNotFoundException e){}
			}else{
				if(list !=null) {
					subjMap.put("memberOfGroup",group2Map(s,list.getGroup()));
					subjMap.put("asMemberOf", list.getGroup().getUuid());
					
				}
				//subjMap.put("asMemberOf", group.getUuid());
			}
			if (chain != null) {
				subjMap.put("chain", chain);
				//subjMap.put("chainSize", new Integer(chain.size()));
				//subjMap.put("chainGroupIds", chainGroupIds);
			}
			if(via!=null)subjMap.put("via", via);

			maps.add(subjMap);
		}
		return maps;
	}
	
	public static String[] getChainGroupIds(GrouperSession s,Membership list) 
		throws MemberNotFoundException,GroupNotFoundException{
		Set chainIds = new LinkedHashSet();
		Member chainMember;
		Set chain = list.getChildMemberships();
		String[] chainGroupIds={};
		Group via = list.getViaGroup();
		if(via ==null && (chain==null ||chain.size()==0)) return chainGroupIds;
		if (via !=null) chainIds.add(via.getUuid());
		Membership gl;
		Membership mv;
		Iterator it = chain.iterator();
		while(it.hasNext()) {
			try {
				gl =(Membership) it.next();
				//gl=mv.toList(s);
			    chainMember = gl.getMember();
			
			chainIds.add(chainMember.getSubjectId());
			}catch(NullPointerException npe) {
				//chainGroupIds[j] = "!";
			}
		}
		chainGroupIds = (String[])chainIds.toArray(chainGroupIds);
		return chainGroupIds;
	}
	
	
	/**
	 * Given a GroupeGroup or GrouperStem id, an array of subjects and an array of 
	 * privileges, grant the privileges to each subject for the GrouperStem or 
	 * GrouperGroup
	 * 
	 * @param s GrouperSession for authenticated user
	 * @param stemOrGroupId GrouperGroup or GrouperStem id
	 * @param members array of Subjects
	 * @param privileges array of privileges
	 * @param forStems indicates GrouperStem
	 */
	public static void assignPrivileges(GrouperSession s, String stemOrGroupId,
			Subject[] members, String[] privileges, boolean forStems) 
		throws SchemaException,MemberAddException,InsufficientPrivilegeException,MemberNotFoundException,
			GrantPrivilegeException{
		assignPrivileges(s,stemOrGroupId,members,privileges,forStems,FieldFinder.find("members"));
	}
	
	/**
	 * Given a GroupeGroup or GrouperStem id, an array of subjects and an array of 
	 * privileges, grant the privileges to each subject for the GrouperStem or 
	 * GrouperGroup
	 * 
	 * @param s GrouperSession for authenticated user
	 * @param stemOrGroupId GrouperGroup or GrouperStem id
	 * @param members array of Subjects
	 * @param privileges array of privileges
	 * @param forStems indicates GrouperStem
	 */
	public static void assignPrivileges(GrouperSession s, String stemOrGroupId,
			Subject[] members, String[] privileges, boolean forStems,Field field) 
		throws SchemaException,MemberAddException,InsufficientPrivilegeException,MemberNotFoundException,
			GrantPrivilegeException{
		Group group = null;
		Stem stem = null;
		Subject subject;
		GroupOrStem  groupOrStem=GroupOrStem.findByID(s,stemOrGroupId);
		
			stem = groupOrStem.getStem();
			group = groupOrStem.getGroup();
		
		for (int i = 0; i < members.length; i++) {
			subject = members[i];
			for (int j = 0; j < privileges.length; j++) {
				try {
					if ("member".equals(privileges[j].toLowerCase()) && !group.hasImmediateMember(subject,field)) {
						group.addMember(subject,field);						
				
					} else if (groupOrStem.isStem()) {
						stem.grantPriv(subject,Privilege.getInstance(privileges[j].toLowerCase()));

					} else {
						group.grantPriv(subject,Privilege.getInstance(privileges[j].toLowerCase()));

					}
				} catch (RuntimeException e) {
					//@TODO Expect different type of Exception in future
					if (e.getMessage().indexOf("List value already exists") == -1)
						throw e;
				} 
			}
		}

	}

	
	/**
	 * Given the UI browsing mode return a Map where each key is a valid stem
	 * for browsing. The Map is used to filter out stems which would lead to a 
	 * dead end.<p/>
	 * In a future version, this code should be factored into a new Class with interface
	 * so that new browse modes can be defined and implemented.
	 * 
	 * @param s GrouperSession for authenticated user
	 * @param browseMode - UI browse mode
	 * @return Map where keys are valid stems
	 */
	/*public static Map getValidStems(GrouperSession s, String browseMode) {
		Map stems = new HashMap();
		List groups = null;
		GrouperAccess accessImpl = s.access();
		GrouperNaming namingImpl = s.naming();
		if ("".equals(browseMode)) {
			GrouperMember member = null;
			try {
				member = GrouperMember.load(s, s.subject().getId(), s.subject()
						.getType().getName());
			} catch (SubjectNotFoundException e) {
				throw new RuntimeException(e);
			}
			groups = member.listVals();

		} else if ("Create".equals(browseMode)) {
			groups = namingImpl.has(s, Grouper.PRIV_CREATE);
			List stemmable = namingImpl.has(s, Grouper.PRIV_STEM);
			groups.addAll(stemmable);
		} else if ("Manage".equals(browseMode)) {
			groups = accessImpl.has(s, Grouper.PRIV_ADMIN);
			List others = accessImpl.has(s, Grouper.PRIV_UPDATE);
			groups.addAll(others);
			others = accessImpl.has(s, Grouper.PRIV_READ);
			groups.addAll(others);
			List creatable = namingImpl.has(s, Grouper.PRIV_CREATE);
			List stemmable = namingImpl.has(s, Grouper.PRIV_STEM);
			groups.addAll(creatable);
			groups.addAll(stemmable);
		} else if ("Join".equals(browseMode)) {
			groups = accessImpl.has(s, Grouper.PRIV_OPTIN);
		} else if ("All".equals(browseMode)) {
			//return new HashMap();
			groups = accessImpl.has(s, Grouper.PRIV_VIEW);
			//@TODO add OPTINS?
		}
		GrouperGroup group;
		GrouperList grouperList;
		String groupKey;
		String stem;
		int pos = 0;
		String partStem;
		String gkey;
		String name;
		
		for (int i = 0; i < groups.size(); i++) {
			grouperList = (GrouperList) groups.get(i);
			name = grouperList.group().name();

			if (!stems.containsKey(name)) {
				stems.put(name, Boolean.TRUE);

				pos = 0;
				while (name.indexOf(HIER_DELIM, pos) > -1) {
					pos = name.indexOf(HIER_DELIM, pos);
					partStem = name.substring(0, pos);
					pos++;
					stems.put(partStem, Boolean.TRUE);

				}
			}
		}
		return stems;
	}*/

	
	/**
	 * Is s.subject() the system user?
	 * 
	 * @param s GrouperSession for authenticated user
	 * @return boolean
	 */
	public static boolean isSuperUser(GrouperSession s) {
		return s.getSubject().getId().equals("GrouperSystem")||Boolean.TRUE.equals(UIThreadLocal.get("isActiveWheelGroupMember"));
	}

	/**
	 * Given a Subject id and SubjectType return a Subject - or null if one
	 * not found
	 * 
	 * @param subjectId Subject id
	 * @param subjectType Subject type
	 * @return Subject
	 */
	public static Subject getSubjectFromIdAndType(String subjectId,
			String subjectType) {
		try {
			return SubjectFinder.findById(subjectId, subjectType);
		} catch (Exception e) {
		}
		return null;

	}

	/**
	 * Given a GrouperGroup id return the GrouperGroup
	 * 
	 * @param s GrouperSession for authenticated user
	 * @param id GrouperGroup id
	 * @return GrouperGroup
	 */
	public static Group groupLoadById(GrouperSession s, String id) throws GroupNotFoundException{

		Group group = null;

		group = GroupFinder.findByUuid(s, id);
		
		return group;

	}

	/**
	 * Return an array of all access privileges + member
	 * 
	 * @param s GrouperSession for authenticated user
	 * @return array of privilege names
	 */
	public static String[] getGroupPrivsWithMember(GrouperSession s) {
		return groupPrivsWithMember;
	}
	
	/**
	 * Return an array of all access privileges
	 * 
	 * @param s GrouperSession for authenticated user
	 * @return array of privilege names
	 */
	public static String[] getGroupPrivs(GrouperSession s) {
		return groupPrivs;
	}

	/**
	 * Return an array of al naming privileges
	 * @param s GrouperSession for authenticated user
	 * @return array of privilege names
	 */
	public static String[] getStemPrivs(GrouperSession s) {
		return stemPrivs;
	}

	
	/**
	 * Given a simple query and scoping stem search for matching groups and return as List
	 * @param s GrouperSession for authenticated user
	 * @param query to search for
	 * @param from stem which scopes search
	 * @param name of attribute to search
	 * @return List of groups matched
	 */
	public static List searchGroupsByAttribute(GrouperSession s, String query, String from,String attr) throws QueryException,StemNotFoundException{

		GrouperQuery q = GrouperQuery.createQuery(s,new GroupAttributeFilter(attr,query,StemFinder.findByName(s,from)));
		Set res = q.getGroups();
		return new ArrayList(res);
			/*String type = null;

			GrouperQuery grouperQuery = new GrouperQuery(s);
			

			if (from == null && !grouperQuery.groupAttr(attr,query)) {
				List empty = new ArrayList();
				return empty;
			}
			if (from != null && !grouperQuery.groupAttr(from,attr,query)) {
				List empty = new ArrayList();
				return empty;
			}

			List res = grouperQuery.getGroups();
			return res;*/
		
	}
	
	
	/**
	 * Given a simple query and scoping stem search for matching groups and return as List
	 * @param s GrouperSession for authenticated user
	 * @param query to search for
	 * @param from stem which scopes search
	 * @param searchInDisplayNameOrExtension name=displayName / extemsion=displayExtension
	 * @param searchInNameOrExtension name=name / extemsion=extension
	 * @return List of groups matched
	 */
	public static List searchGroups(GrouperSession s, String query, String from,String searchInDisplayNameOrExtension,String searchInNameOrExtension) 
	throws StemNotFoundException,QueryException{
		List displayResults = null;
		List nonDisplayResults=null; 
		String attr = null;
		if(!"".equals(searchInDisplayNameOrExtension)) {
			if("name".equals(searchInDisplayNameOrExtension)) {
				attr="displayName";
			}else{
				attr="displayExtension";
			}
			displayResults = searchGroupsByAttribute(s,query,from,attr);	
		}
		if(!"".equals(searchInNameOrExtension)) {
			if("name".equals(searchInNameOrExtension)) {
				attr="name";
			}else{
				attr="extension";
			}
			nonDisplayResults = searchGroupsByAttribute(s,query,from,attr);	
		}
		if(displayResults==null && nonDisplayResults==null) return new ArrayList();
		if(displayResults==null && nonDisplayResults!=null) return nonDisplayResults;
		if(displayResults!=null && nonDisplayResults==null) return displayResults;
		Object obj;
		for(int i=0;i<nonDisplayResults.size();i++) {
			obj = nonDisplayResults.get(i);
			if(!displayResults.contains(obj)) displayResults.add(obj);
		}
			
		return displayResults;
		
	}

	
	/**
	 * Given simple query, scoping stem and ui browseMode return list of
	 * matching groups, pruned to give results relevant to browseMode.<p/>
	 * The browseMode filtering needs to be factored into a new Class with interface
	 * so that new browse modes can be added easily
	 * 
	 * @param s GrouperSession for authenticated user
	 * @param query to search for
	 * @param from Stem which scopes search
	 * @param searchInDisplayNameOrExtension name=displayName / extemsion=displayExtension
	 * @param searchInNameOrExtension name=name / extemsion=extension
	 * @param browseMode UI browse mode to filter results by
	 * @return List of GrouperGroups matched
	 */
	public static List searchGroups(GrouperSession s, String query,
			String from, String searchInDisplayNameOrExtension,
			String searchInNameOrExtension,String browseMode) throws Exception{
		String type = null;
		List res = searchGroups(s, query, from,searchInDisplayNameOrExtension,searchInNameOrExtension);
		if (res != null)
			return res;
		List returnRes = new ArrayList();
		Set allowedSet = null;
		if ("All".equals(browseMode)) {
			return res;
		} else if ("".equals(browseMode)) {
			allowedSet = GrouperHelper.getMembershipsSet(s);
		} else if ("Manage".equals(browseMode)) {
			allowedSet = GrouperHelper.getGroupsForPrivileges(s,
					new String[] { "admin", "update",
							"read" });
		} else if ("Join".equals(browseMode)) {
			allowedSet = GrouperHelper.getGroupsForPrivileges(s,
					new String[] { "optin" });
		}
		if (allowedSet != null) {
			Map allowed = new HashMap();
			Iterator it = allowedSet.iterator();
			Group group;
			while (it.hasNext()) {
				group = (Group) it.next();
				allowed.put(group.getUuid(),Boolean.TRUE);
			}
			
			for (int i = 0; i < res.size(); i++) {
				group = (Group) res.get(i);
				if (allowed.containsKey(group.getUuid()))
					returnRes.add(group);
			}
		}
		return returnRes;
	}

	/*
	 * public static List getNestedStemChildren(GrouperSession s,String stem) {
	 * return Group.getNestedChildren(s,stem); }
	 */

	/**
	 * Given a simple query and scoping stem and attribute, search for matching stems and return as List
	 * @param s GrouperSession for authenticated user
	 * @param query to search for
	 * @param from stem which scopes search
	 * @param name of attribute to search
	 * @return List of stems matched
	 */
	public static List searchStemsByAttribute(GrouperSession s, String query, String from,String attr) {
			
			return new ArrayList();
			/*String type = null;

			GrouperQuery grouperQuery = new GrouperQuery(s);
			

			if (from == null && !grouperQuery.stemAttr(attr,query)) {
				List empty = new ArrayList();
				return empty;
			}
			if (from != null && !grouperQuery.stemAttr(from,attr,query)) {
				List empty = new ArrayList();
				return empty;
			}

			List res = grouperQuery.getStems();
			return res;*/
		
	}
	
	
	/**
	 * Given a simple query and scoping stem search for matching stems and return as List
	 * @param s GrouperSession for authenticated user
	 * @param query to search for
	 * @param from stem which scopes search
	 * @param searchInDisplayNameOrExtension name=displayName / extemsion=displayExtension
	 * @param searchInNameOrExtension name=name / extemsion=extension
	 * @return List of stems matched
	 */
	public static List searchStems(GrouperSession s, String query, String from,String searchInDisplayNameOrExtension,String searchInNameOrExtension) throws StemNotFoundException,QueryException{
		GrouperQuery q = GrouperQuery.createQuery(s,new StemNameFilter(query,StemFinder.findByName(s,from)));
		Set res = q.getStems();
		List displayResults = new ArrayList(res);
		/*List nonDisplayResults=null; 
		String attr = null;
		if(!"".equals(searchInDisplayNameOrExtension)) {
			if("name".equals(searchInDisplayNameOrExtension)) {
				attr="displayName";
			}else{
				attr="displayExtension";
			}
			displayResults = searchStemsByAttribute(s,query,from,attr);	
		}
		if(!"".equals(searchInNameOrExtension)) {
			if("name".equals(searchInNameOrExtension)) {
				attr="name";
			}else{
				attr="extension";
			}
			nonDisplayResults = searchStemsByAttribute(s,query,from,attr);	
		}
		if(displayResults==null && nonDisplayResults==null) return new ArrayList();
		if(displayResults==null && nonDisplayResults!=null) return nonDisplayResults;
		if(displayResults!=null && nonDisplayResults==null) return displayResults;
		Object obj;
		for(int i=0;i<nonDisplayResults.size();i++) {
			obj = nonDisplayResults.get(i);
			if(!displayResults.contains(obj)) displayResults.add(obj);
		}
		*/
		return displayResults;
		
		
	}

	/**
	 * Given a GrouperSession s, determine all the GrouperGroups s.subject()
	 * is a member of and return a Set of GrouperGroups
	 * 
	 * @param s GrouperSession for authenticated user
	 * @return Set of GrouperGroups
	 */
	public static Set getMembershipsSet(GrouperSession s) {
		return getMembershipsSet(s, 0, 100000, null);

	}
	
	
	/**
	 * Given a GrouperSession s, determine all the GrouperGroups s.subject()
	 * is a member of and return a Map where the keys are GrouperGroup keys
	 * 
	 * @param s GrouperSession for authenticated user
	 * @return Map where keys are GrouperGroup keys
	 */
	public static Map getMemberships(GrouperSession s) {
		Map memberships = null;
							   

		memberships = new HashMap();
		Member member = null;
		try {
			member = MemberFinder.findBySubject(s, s.getSubject());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		Set vals = member.getGroups();
		Group group;
		Iterator it = vals.iterator();
		while(it.hasNext()){
			
			group = (Group) it.next();
			memberships.put(group.getUuid(), Boolean.TRUE);
		}

		return memberships;
	}
	
	
	/**
	 * Given a GrouperSession s, return a subset of Groups where
	 * s.subject is a member, determined by start and pageSize.<p/>
	 * totalCount is a dubious means to return a second value to allow the UI
	 * to page results
	 * 
	 * @param s GrouperSession for authenticated user
	 * @param start where subset begins
	 * @param pageSize no of groups to return
	 * @param totalCount number of overall results
	 * @return Set - subset of groups
	 */
	public static Set getMembershipsSet(GrouperSession s, int start,
			int pageSize, StringBuffer totalCount) {
		Set memberships = new LinkedHashSet();
		Member member = null;
		try {
			member = MemberFinder.findBySubject(s, s.getSubject());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		Set vals = member.getGroups();
		
		Group group;
		int end = start + pageSize;
		if (end > vals.size())
			end = vals.size();
		if (totalCount != null) {
			totalCount.setLength(0);
			totalCount.append("" + vals.size());
		}
		
		Iterator it = vals.iterator();
		while(it.hasNext()){
			
			group = (Group) it.next();
			memberships.add(group);
		}
		return memberships;
	}

	/**
	 * Given a GrouperSession s, and an array of privileges, get all groups
	 * where s.subject() has atleast one of the privileges
	 * 
	 * @param s GrouperSession for authenticated user
	 * @param privs privileges to test
	 * @return Set of Grouper Groups 
	 */
	public static Set getGroupsForPrivileges(GrouperSession s, String[] privs) throws MemberNotFoundException{

		Set groups = getGroupsForPrivileges(s, privs, 0, 100000, null);
		
		return groups;
	}

		
	/**
	 * Given a GrouperSession s, and an array of privileges, get subset of groups
	 * where s.subject() has atleast one of the privileges, determined by start and pageSize<p/>
	 * resultCount is a dubious means to return a second value to allow the UI
	 * to page results
	 * 
	 * @param s GrouperSession for authenticated user
	 * @param privs privileges to test
	 * @param start of subset
	 * @param pageSize number of GrouperGroups to return
	 * @param resultCount overall number of GrouperGroups
	 * @return Set - subset of GrouperGroups
	 */
	public static Set getGroupsForPrivileges(GrouperSession s, String[] privs,
			int start, int pageSize, StringBuffer resultCount) throws MemberNotFoundException{
		
		Set groupSet = new LinkedHashSet();
		
		Set allSet = new LinkedHashSet();
		
		
		Member member = MemberFinder.findBySubject(s,s.getSubject());
		for (int i = 0; i < privs.length; i++) {
			allSet.addAll(getGroupsOrStemsWhereMemberHasPriv(member,privs[i].toLowerCase()));
		}

		int end = start + pageSize;
		if (end > allSet.size())
			end = allSet.size();
		if (resultCount != null) {
			resultCount.setLength(0);
			resultCount.append("" + allSet.size());
		}
		
		Iterator it = allSet.iterator();
		Group group = null;
		while(it.hasNext()){
			group = (Group) it.next();
			groupSet.add(group);
		}
		return groupSet;
	}

	/**
	 * Given a GrouperSession s, and an array of privileges, get all stems
	 * where s.subject() has atleast one of the privileges
	 * 
	 * @param s GrouperSession for authenticated user
	 * @param privs privileges to test
	 * @return Set of GrouperStems
	 */
	public static Set getStemsForPrivileges(GrouperSession s, String[] privs) throws MemberNotFoundException{

		Set groups = null;//Cache.instance().getSet(s,sb.toString());
		if (groups != null)
			return null;

		groups = getGroupsForPrivileges(s, privs, 0, 100000, null);
		//Cache.instance().put(s,sb.toString(),groups);
		return groups;

	}

	/**
	 * Given a GrouperSession s, and an array of privileges, get subset of stems
	 * where s.subject() has atleast one of the privileges, determined by start and pageSize<p/>
	 * resultCount is a dubious means to return a second value to allow the UI
	 * to page results
	 * 
	 * @param s GrouperSession for authenticated user
	 * @param privs privileges to test
	 * @param start where subset begins
	 * @param pageSize number of GrouperStems to return
	 * @param resultCount overall number of GrouperStems
	 * @return
	 */
	public static Set getStemsForPrivileges(GrouperSession s, String[] privs,
			int start, int pageSize, StringBuffer resultCount) throws MemberNotFoundException{

		
		Set stemSet = new LinkedHashSet();
		Set allSet = new LinkedHashSet();
		
		Member member = MemberFinder.findBySubject(s,s.getSubject());
		for (int i = 0; i < privs.length; i++) {
			allSet.addAll(getGroupsOrStemsWhereMemberHasPriv(member,privs[i].toLowerCase()));
		}
		int end = start + pageSize;
		if (end > allSet.size())
			end = allSet.size();
		if (resultCount != null) {
			resultCount.setLength(0);
			resultCount.append("" + allSet.size());
		}
		
		Iterator it = allSet.iterator();
		Stem stem = null;
		while(it.hasNext()){
			stem = (Stem) it.next();
			stemSet.add(stem);
		}
		return stemSet;
	}
	
	
	/**
	 * Given a GrouperStem delete it and any children
	 * @TODO fix
	 * @param s GrouperSession for authenticated user
	 * @param stem GrouperStem to delete
	 * @return boolean indicating success
	 * @throws Exception
	 */
	public static boolean stemDelete(GrouperSession s, Stem stem)
			throws Exception {
		if (stem == null || !stem.hasStem(s.getSubject())) {
			return false;
		}
		String stemStr = stem.getName() + HIER_DELIM;
		//@TODO: when searching scoped by stem fix
		List children = new ArrayList();//getNestedStemChildren(s,stemStr);
		if (children.size() > 100)
			throw new Exception("Too many children (" + children.size()
					+ ") - must be <=100");
		Object[] res;
		Group g = null;
		boolean deleted = true;
		GrouperSession sysSession = null;
		try {
			sysSession = GrouperSession.start(SubjectFinder.findById("GrouperSystem"));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		for (int i = 0; i < children.size(); i++) {
			res = (Object[]) children.get(i);
			g = (Group) res[1];
			if (!groupDelete(sysSession, GroupOrStem.findByGroup(s,g))) {
				sysSession.stop();
				return false;
			}
		}

		sysSession.stop();
		return groupDelete(s, GroupOrStem.findByStem(s,stem));
	}
	
	/**
	 * Given a GrouperGroup or GroupeStem delete it. GroupeStem must not have any children.
	 * 
	 * @param s GrouperSession for authenticated user
	 * @param groupOrStem GrouperGroup or GrouperStem to delete
	 * @return boolean indicating success
	 */
	public static boolean groupDelete(GrouperSession s, GroupOrStem groupOrStem) 
		throws InsufficientPrivilegeException,MemberNotFoundException,
		SubjectNotFoundException,MemberDeleteException,SessionException{
		Group group = groupOrStem.getGroup();
		Stem stem = groupOrStem.getStem();
		boolean deleted = true;
		if (groupOrStem == null)
			return false;
		if (groupOrStem.isStem()) {
			if(!stem.hasStem(s.getSubject())) {

				return false;
			}
			try {
				//Stem.delete(s, stem);
			} catch (Exception e) {
				deleted = false;
			}
			return deleted;
		} else {
			
			if (!group.hasAdmin(s.getSubject())) {
				return false;
			}
		}

		Set memberships = group.getMemberships();//Eff?
		Member member;
		Iterator it = memberships.iterator();
		while(it.hasNext()) {
			member = (Member) it.next();
			try {
				group.deleteMember(member.getSubject());
				//group.listDelVal(member);
			} catch (RuntimeException e) {
				if (!"List value does not exist".equals(e.getMessage()))
					throw e;
			}
		}

		/*
		 * members = group.listVals();
		 * 
		 * for(int i=0;i <members.size();i++) { member = ((GrouperList)
		 * members.get(i)).member(); try { group.listDelVal(member);
		 * }catch(RuntimeException e) { if(!"List value does not
		 * exist".equals(e.getMessage())) throw e; }
		 *  }
		 */
		GrouperSession sysSession = null;
		try {
			sysSession = GrouperSession.start(SubjectFinder.findById("GrouperSystem"));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		Member groupAsMember = null;
		try {
			groupAsMember = MemberFinder.findBySubject(s, SubjectFinder.findById(group.getUuid(), "group"));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		/*for (int j = 0; j < listNames.length; j++) {
			String list = listNames[j];
			memberships = groupAsMember.;
			GrouperGroup memberOf;
			for (int i = 0; i < memberships.size(); i++) {
				memberOf = (GrouperGroup) ((GrouperList) memberships.get(i))
						.group();
				//memberOf =
				// (GrouperGroup)GrouperGroup.loadByKey(s,memberOf.key());
				String type = memberOf.type();
				memberOf.listDelVal(groupAsMember, list);
			}
		}*/
		sysSession.stop();

		try {
			//Group.delete(s, group);
		} catch (Exception e) {
			deleted = false;
		}
		return deleted;
	}

	/**
	 * Given a PersonalStem create stem in correct location - depends on
	 * configuration. PersonalStem is an Interface and new implemetations may be
	 * 'plugged' into the UI
	 * 
	 * @param s GrouperSession for authenticated user
	 * @param ps PersonalStem
	 * @return GrouperStem created
	 * @throws Exception
	 */
	public static Stem createIfAbsentPersonalStem(GrouperSession s,
			PersonalStem ps) throws Exception {
		if (s == null)
			return null;
		GrouperSession sysSession = null;
		try {
			sysSession = GrouperSession.start(SubjectFinder.findById("GrouperSystem"));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		String stemName = ps.getPersonalStemId(s.getSubject());
		String parentName=ps.getPersonalStemRoot(s.getSubject());
		Stem parent = null;
		String childName = null;
		if(NS_ROOT.equals(parentName)||"".equals(parentName)) {
			parent = StemFinder.findRootStem(sysSession);
			childName=stemName;
		}else{
			try{
				childName=parentName + HIER_DELIM + stemName;
				parent = StemFinder.findByName(sysSession,parentName);
				
			}catch(StemNotFoundException e){
				throw new IllegalStateException("Cannot find parent stem for personal stem: " + childName);
			}
		}
		
		Stem stem = null;
		
		try{
			stem=StemFinder.findByName(sysSession, childName);
		}catch(Exception e){}
		
		if (stem == null) {
			
			stem = parent.addChildStem(stemName,ps.getPersonalStemDisplayName(s
					.getSubject()));
			
			stem.setDescription(ps.getPersonalStemDescription(s
					.getSubject()));

			stem.grantPriv(s.getSubject(),Privilege.getInstance("create"));
			stem.grantPriv(s.getSubject(),Privilege.getInstance("stem"));
			sysSession.stop();
		}
		return stem;
	}
	
	/**
	 * Query Subject API for sources and determine which ones return 'people'
	 * @return List of Sources
	 * @throws Exception
	 */
	public static synchronized List getPersonSources() throws Exception {
		if (personSources != null)
			return personSources;
		personSources = new ArrayList();
		SourceManager sm = SourceManager.getInstance();
		Collection c = sm.getSources();
		Iterator it = c.iterator();
		Source source = null;
		Iterator typesIterator = null;
		String subjectType;
		while (it.hasNext()) {
			source = (Source) it.next();
			Set subjectTypes = source.getSubjectTypes();
			typesIterator = subjectTypes.iterator();
			while (typesIterator.hasNext()) {
				subjectType = (String) ((SubjectType) typesIterator.next())
						.getName();
				if (subjectType.equals("person"))
					personSources.add(source);
			}
		}
		return personSources;
	}
	
	/**
	 * API lets you get all memberships - this filters them for a specific group
	 * @param s
	 * @param subject
	 * @param group
	 * @return
	 * @throws MemberNotFoundException
	 * @throws GroupNotFoundException
	 */
	public static List getAllWaysInWhichSubjectIsMemberOFGroup(GrouperSession s,Subject subject,Group group,Field field) 
		throws MemberNotFoundException,GroupNotFoundException,SchemaException,CompositeNotFoundException{
		List ways = new ArrayList();
		Member member = MemberFinder.findBySubject(s,subject);
		Set memberships = member.getMemberships(field);
		Membership gl;
		Iterator it = memberships.iterator();
		while(it.hasNext()) {
			gl = (Membership) it.next();
			//gl.setSession(s);
			if(gl.getGroup().getUuid().equals(group.getUuid())) {
				ways.add(gl);
				/*if(group.hasComposite() && field.getName().equals("members")) {
					fixCompositeMembership(s,gl,subject,group,field);
				}*/
			}
		}
		return ways;
	}
	
	/*public static void fixCompositeMembership(GrouperSession s,Membership m,Subject subject,Group group,Field field) {
		
			String x="";
			
			if(group.hasComposite() && field.getName().equals("members")) {
				List compositeWays = new ArrayList();
				Composite comp = CompositeFinder.isOwner(group);
				
				compositeWays.addAll(getAllWaysInWhichSubjectIsMemberOFGroup(s,subject,comp.getLeftGroup(),field));
				compositeWays.addAll(getAllWaysInWhichSubjectIsMemberOFGroup(s,subject,comp.getRightGroup(),field));
				Membership pm = null;
				try {
					pm=MembershipFinder.findImmediateMembership(s,group,subject,field);
				}catch (MembershipNotFoundException e) {
					return compositeWays;
				}
				Membership m;
				for(int i=0;i<compositeWays.size();i++) {
					m=null;
					m = (Membership)compositeWays.get(i);
					try {
						if(m.getVia()==null) {
							m.via_id=comp;
						}
					}catch(OwnerNotFoundException e) {
						m.via_id=group;
					}
					
					try {
						if(m.getParentMembership()==null) {
							//m.parent_membership=pm;
						}
					}catch(MembershipNotFoundException e) {
						//m.parent_membership=pm;
					}
				}
				return compositeWays;
			
		
	}}*/
	
	/**
	 * Returns indirect privileges for member on the group or stem
	 * @param s
	 * @param groupOrStem
	 * @param member
	 * @return
	 */
	public static Map getExtendedHas(GrouperSession s,GroupOrStem groupOrStem,Member member) throws SchemaException{
		return getExtendedHas(s,groupOrStem,member,FieldFinder.find("members"));
	}
	
	/**
	 * Returns indirect privileges for member on the group or stem
	 * @param s
	 * @param groupOrStem
	 * @param member
	 * @return
	 */
	public static Map getExtendedHas(GrouperSession s,GroupOrStem groupOrStem,Member member,Field field) throws SchemaException{
		Map map  =getAllHas(s,groupOrStem,member,field);
		
		map.remove("subject");
		map.remove("effectivePrivs");
		return map;
		
	}
	
	/**
	 * Returns indirect privileges for member on the group or stem - but not how derived
	 * @param s
	 * @param groupOrStem
	 * @param member
	 * @return
	 */
	public static Map getEffectiveHas(GrouperSession s,GroupOrStem groupOrStem,Member member,Field field) throws SchemaException{
		Map map  =getAllHas(s,groupOrStem,member,field);

		return (Map)map.get("effectivePrivs");
		
	}
	
	/**
	 * Returns direct privileges for member on group or stem
	 * @param s
	 * @param groupOrStem
	 * @param member
	 * @return
	 */
	public static Map getImmediateHas(GrouperSession s,GroupOrStem groupOrStem,Member member) throws SchemaException{
		Map map = getAllHas(s,groupOrStem,member);
		
		return (Map)map.get("subject");
		
	}
	
	/**
	 * Returns direct privileges for member on group or stem
	 * @param s
	 * @param groupOrStem
	 * @param member
	 * @param field
	 * @return
	 * @throws SchemaException
	 */
	public static Map getImmediateHas(GrouperSession s,GroupOrStem groupOrStem,Member member,Field field) throws SchemaException{
		Map map = getAllHas(s,groupOrStem,member,field);
		
		return (Map)map.get("subject");
		
	}
	
	/**
	 * Returns all privileges, direct and indirect, that member has for group or stem
	 * @param s
	 * @param groupOrStem
	 * @param member
	 * @return
	 */
	public static Map getAllHas(GrouperSession s,GroupOrStem groupOrStem,Member member) throws SchemaException{
		return getAllHas(s,groupOrStem,member,FieldFinder.find("members"));
	}
	
	/**
	 * Returns all privileges, direct and indirect, that member has for group or stem
	 * @param s
	 * @param groupOrStem
	 * @param member
	 * @return
	 */
	public static Map getAllHas(GrouperSession s,GroupOrStem groupOrStem,Member member,Field field) throws SchemaException{
		Set allPrivs = null;
		Map effectivePrivs = new HashMap();
		Map effectiveMemberships = new HashMap();
		if(groupOrStem.isGroup()) {
			allPrivs = member.getPrivs(groupOrStem.getGroup());
			try {
				effectiveMemberships = getEffectiveMembershipsForGroupAndSubject(s,groupOrStem.getGroup(),member.getSubject(),field);
			}catch(Exception e){}
		}else{
			allPrivs = member.getPrivs(groupOrStem.getStem());
		}
		 
		Map results = new LinkedHashMap();
		Map privs;
		AccessPrivilege priv = null;
		NamingPrivilege nPriv=null;
		String key="subject";
		results.put(key,new HashMap());
		List tmpList = new ArrayList();
		Iterator it = allPrivs.iterator();
		boolean isEffective = false;
		if(groupOrStem.isGroup() && member.isImmediateMember(groupOrStem.getGroup(),field)) {
			privs = new HashMap();
			privs.put("MEMBER",Boolean.TRUE);
			results.put("subject",privs);
		}
		
		while(it.hasNext()) {
			if(groupOrStem.isGroup()) {
				priv = (AccessPrivilege)it.next();
				
				if(member.getSubjectId().equals(priv.getOwner().getId())) {
					key="subject";
					isEffective = false;
				}else{
					key=priv.getOwner().getId();
					isEffective = true;
				}
				privs = (Map)results.get(key);
				if(privs==null) {
					privs=new HashMap();
					results.put(key,privs);
				}
					if(isEffective) {
						try{
							privs.put("group",group2Map(s,GroupFinder.findByUuid(s,priv.getOwner().getId())));
							privs.put(priv.getName().toUpperCase(),Boolean.TRUE);
							if(effectiveMemberships.containsKey(priv.getOwner())) {
								privs.put("MEMBER",Boolean.TRUE);
								effectiveMemberships.remove(priv.getOwner());
							}
						}catch(GroupNotFoundException e){}
						effectivePrivs.put(priv.getName().toUpperCase(),Boolean.TRUE);
					}
					
				
				privs.put(priv.getName().toUpperCase()
						,Boolean.TRUE);
			}else{
				nPriv = (NamingPrivilege)it.next();
				
				if(member.getSubjectId().equals(nPriv.getOwner().getId())) {
					key="subject";
					isEffective = false;
				}else{
					key=nPriv.getOwner().getId();
					isEffective = true;
				}
					privs = (Map)results.get(key);
					if(privs==null) {
						privs=new HashMap();
						results.put(key,privs);
					}
						if(isEffective) {
							try{
								if(effectiveMemberships.containsKey(nPriv.getOwner())) {
									privs.put("MEMBER",Boolean.TRUE);
									effectiveMemberships.remove(nPriv.getOwner());
								}
								privs.put("group",group2Map(s,GroupFinder.findByUuid(s,nPriv.getOwner().getId())));
							}catch(GroupNotFoundException e){}
							effectivePrivs.put(nPriv.getName().toUpperCase(),Boolean.TRUE);
						}
						
					
					privs.put(nPriv.getName().toUpperCase(),Boolean.TRUE);
			}
		}
		Map.Entry entry;
		it = effectiveMemberships.entrySet().iterator();
		Group effGroup;
		while(it.hasNext()) {
			entry = (Map.Entry)it.next();
			effGroup = (Group)entry.getKey();
			privs=(Map)results.get(effGroup.getUuid());
			if(privs==null) {
				privs=new HashMap();
				privs.put("group",group2Map(s,effGroup));
				privs.put("MEMBER",Boolean.TRUE);
				results.put(effGroup.getUuid(),privs);
			}
			
		}
		results.put("effectivePrivs",effectivePrivs);
		return results;
		
	}
	
	/**
	 * Given a Group and Subject return the effective memberships keyed on
	 * the via group
	 * @param s
	 * @param group
	 * @param subject
	 * @param field
	 * @return
	 * @throws Exception
	 */
	public static Map getEffectiveMembershipsForGroupAndSubject(GrouperSession s,Group group,Subject subject,Field field) throws Exception{
		Member member = MemberFinder.findBySubject(s,subject);
		Map res = new HashMap();
		Set memberships = member.getEffectiveMemberships(field);
		Membership m;
		Iterator it = memberships.iterator();
		while(it.hasNext()){
			m = (Membership)it.next();
			if(m.getGroup().equals(group)) res.put(m.getViaGroup(),m);
		}
		
		return res;
	}
	
	/**
	 * Given priv name return subjects with that privilege for group
	 * @param group
	 * @param privilege
	 * @return
	 */
	public static Set getSubjectsWithPriv(Group group,String privilege) {
		privilege = privilege.toLowerCase();
		if(privilege.equals("admin")) return group.getAdmins();
		if(privilege.equals("update")) return group.getUpdaters();
		if(privilege.equals("read")) return group.getReaders();
		if(privilege.equals("view")) return group.getViewers();
		if(privilege.equals("optin")) return group.getOptins();
		if(privilege.equals("optout")) return group.getOptouts();
		return new HashSet();
	}
	
	/**
	 * Given a privilege return all the groups or stems where member has that privilege
	 * @param member
	 * @param privilege
	 * @return
	 */
	public static Set getGroupsOrStemsWhereMemberHasPriv(Member member,String privilege) {
		privilege=privilege.toLowerCase();
		if(privilege.equals("admin")) return member.hasAdmin();
		if(privilege.equals("update")) return member.hasUpdate();
		if(privilege.equals("read")) return member.hasRead();
		if(privilege.equals("view")) return member.hasView();
		if(privilege.equals("optin")) return member.hasOptin();
		if(privilege.equals("optout")) return member.hasOptout();
		if(privilege.equals("create")) return member.hasCreate();
		if(privilege.equals("stem")) return member.hasStem();
		return new HashSet();
	}
	
	/**
	 * Given priv name return subjects with that privilege for stem
	 * @param stem
	 * @param privilege
	 * @return
	 */
	public static Set getSubjectsWithPriv(Stem stem,String privilege) {
		privilege=privilege.toLowerCase();
		if(privilege.equals("stem")) return stem.getStemmers();
		if(privilege.equals("create")) return stem.getCreators();
		return new HashSet();
	}
	
	/**
	 * Determine if a given subject has been granted an Access privilege through direct assignment
	 * @param s
	 * @param subject
	 * @param group
	 * @param privilege
	 * @return
	 * @throws MemberNotFoundException
	 */
	public static boolean hasSubjectImmPrivForGroup(GrouperSession s,Subject subject,Group group,String privilege) throws MemberNotFoundException,SchemaException{
		Map privs = getImmediateHas(s,GroupOrStem.findByGroup(s,group),MemberFinder.findBySubject(s,subject));
		return privs.containsKey(privilege.toUpperCase());
	}
	
	/**
	 * Determine if a given subject has been granted a Naming privilege through direct assignment
	 * @param s
	 * @param subject
	 * @param stem
	 * @param privilege
	 * @return
	 * @throws MemberNotFoundException
	 */
	public static boolean hasSubjectImmPrivForStem(GrouperSession s,Subject subject,Stem stem,String privilege) throws MemberNotFoundException,SchemaException{
		Map privs = getImmediateHas(s,GroupOrStem.findByStem(s,stem),MemberFinder.findBySubject(s,subject));
		return privs.containsKey(privilege.toUpperCase());
	}
	
	/**
	 * Return the path by which this Membership is derived
	 * @param s
	 * @param m
	 * @return
	 * @throws GroupNotFoundException
	 * @throws MembershipNotFoundException
	 * @throws MemberNotFoundException
	 */
	public static List getChain(GrouperSession s,Membership m)throws GroupNotFoundException,MembershipNotFoundException,
	MemberNotFoundException,SchemaException{
		List chain = new ArrayList();
		Group via = null;
		Composite comp=null;
		Membership pm=null;
		try {
			via = m.getViaGroup();
		}catch(GroupNotFoundException e) {
			//Should be an immediate member, but check for composite
			return chain;
		}
		try {
			comp = CompositeFinder.findAsOwner(via);
		}catch(Exception me) {
			
		}
		Map groupMap=null;
		if(comp==null) {
			groupMap=GrouperHelper.group2Map(s,via);
		}else{
			groupMap=GrouperHelper.getCompositeMap(s,comp);
		}
		Map viaMap = groupMap;
		groupMap.put("listField","members");
		chain.add(groupMap);
		pm = null;
		Group g=null;
		try {
			pm=m.getParentMembership();
		}catch(MembershipNotFoundException e) {
			
		}
		
		while(pm!=null) {
			if(!pm.getMember().getSubjectId().equals(via.getUuid())) {
				g = GroupFinder.findByUuid(s,pm.getMember().getSubjectId());
				groupMap=GrouperHelper.group2Map(s,g);
				groupMap.put("listField",pm.getList().getName());
				chain.add(groupMap);	
			}else{
				viaMap.put("listField",pm.getList().getName());
			}
			try {
			pm=pm.getParentMembership();
			}catch(MembershipNotFoundException e){break;}
		}
		return chain;
	}
	
	
	/**
	 * 	Given a composite return a Map for use in Tiles
	 * @param grouperSession
	 * @param comp
	 * @return
	 * @throws GroupNotFoundException
	 * @throws MemberNotFoundException
	 * @throws SchemaException
	 */
	public static Map getCompositeMap(GrouperSession grouperSession,Composite comp)
		throws GroupNotFoundException,MemberNotFoundException,SchemaException{
		return getCompositeMap(grouperSession,comp,null);
	
	}
	
	/**
	 * Given a composite return a Map for use in Tiles. If a Subject is passed
	 * then the number of ways the Subject is a member of the left / right groups 
	 * is calculated
	 * @param grouperSession
	 * @param comp
	 * @param subj
	 * @return
	 * @throws GroupNotFoundException
	 * @throws MemberNotFoundException
	 * @throws SchemaException
	 */
	public static Map getCompositeMap(GrouperSession grouperSession,Composite comp,Subject subj)
		throws GroupNotFoundException,MemberNotFoundException,SchemaException{
		Map compMap = new ObjectAsMap(comp,"Composite");
		compMap.put("leftGroup",new GroupAsMap(comp.getLeftGroup(),grouperSession));
		Map membershipMap=null;
		if(subj !=null) {
			membershipMap = getMembershipAndCount(grouperSession,comp.getLeftGroup(),subj);
			if(comp.getLeftGroup().hasComposite()&& membershipMap !=null) {
				membershipMap.put("viaGroup",compMap);
			}
			((Map)compMap.get("leftGroup")).put("membership",membershipMap);
		}
		compMap.put("rightGroup",new GroupAsMap(comp.getRightGroup(),grouperSession));
		if(subj !=null) {
			membershipMap = getMembershipAndCount(grouperSession,comp.getRightGroup(),subj);
			if(comp.getRightGroup().hasComposite()&& membershipMap !=null) {
				membershipMap.put("viaGroup",compMap);
			}
			((Map)compMap.get("rightGroup")).put("membership",membershipMap);
		}
		compMap.put("compositeType",comp.getType().toString());
		compMap.put("owner",new GroupAsMap(comp.getOwnerGroup(),grouperSession));
		compMap.put("id",comp.getOwnerGroup().getUuid());
		return compMap;
	}
	
	private static Map getMembershipAndCount(GrouperSession s,Group group,Subject subject) throws MemberNotFoundException,SchemaException {
		Set memberships = null;
		memberships = MembershipFinder.findMembershipsNoPrivsNoSession(group,MemberFinder.findBySubject(subject),FieldFinder.find("members"));
		if(memberships.size()==0) return null;
		Iterator it = memberships.iterator();
		Membership m = (Membership)it.next();
		m.setSession(s);
		Map mMap = new MembershipAsMap(m);
		mMap.put("noWays",new Integer(memberships.size()));
		return mMap;
	}
	
	/**
	 * Trims down input so that the 'same' membership does not occur twice
	 * @param memberships
	 * @param type
	 * @param count - keeps track of the numbe rof times a membership occurred
	 * @return
	 * @throws MemberNotFoundException
	 * @throws GroupNotFoundException
	 */
	public static List getOneMembershipPerSubjectOrGroup(Set memberships,String type,Map count) 
		throws MemberNotFoundException,GroupNotFoundException{
		//won't pass back values but will give 'unique' list
		if(count==null) count=new HashMap();
		List res = new ArrayList();
		Iterator it = memberships.iterator();
		Membership m;
		String id=null;
		Integer curCount;
		Object obj;
		while(it.hasNext()) {
			obj=it.next();
			if(!(obj instanceof Membership)) {
				res.add(obj);
				continue;
			}
			m = (Membership)obj;
			if("subject".equals(type)){
				id = m.getGroup().getUuid();
			}else if("group".equals(type)) {
				id =m.getMember().getSubjectId();
			}else{
				throw new IllegalArgumentException("type must be 'subject' or 'group'");
			}
			curCount=(Integer)count.get(id);
			if(curCount==null) {
				curCount = new Integer(1);
				res.add(m);
			}else{
				curCount = new Integer(curCount.intValue()+1);
			}
			count.put(id,curCount);
		}
		return res;
	}
	
	/**
	 * Given a trimmed list of memberships which are now Maps, add noWays -> number of
	 * direct and indirect memberships
	 * @param membershipMaps
	 * @param type
	 * @param count
	 * @throws GroupNotFoundException
	 * @throws MemberNotFoundException
	 */
	public static void setMembershipCountPerSubjectOrGroup(List membershipMaps,String type,Map count)
		throws GroupNotFoundException,MemberNotFoundException{
		if(count==null) return;
		Map mMap;
		Map gMap;
		Map sMap;
		
		String id;
		Integer curCount;
		for(int i=0;i<membershipMaps.size();i++) {
			mMap = (Map)membershipMaps.get(i);
			gMap = (Map)mMap.get("group");
			sMap = (Map)mMap.get("subject");
		
			if("subject".equals(type)){
				id = (String)gMap.get("id");
			}else if("group".equals(type)) {
				id = (String)sMap.get("id");
			}else{
				throw new IllegalArgumentException("type must be 'subject' or 'group'");
			}
			curCount = (Integer)count.get(id);
			if(curCount!=null) {
				mMap.put("noWays",curCount);
			}
			
		}
		
	}
	
	/**
	 * Checks key groups.create.grant.all to determine pre-selected privs to
	 * be checked in the UI. If not set, checks default assignments in the Grouper API
	 * @param mediaBundle
	 * @return
	 */
	public static Map getDefaultAccessPrivsForUI(ResourceBundle mediaBundle){
		String privStr = null;
		try {
			privStr = mediaBundle.getString("groups.create.grant.all");
		}catch(Exception e){}
		if(privStr==null || "".equals(privStr)) return getDefaultAccessPrivsForGrouperAPI();
		Map privs = new HashMap();
		if("none".equals(privStr)) return privs;
		String[] privArr = privStr.split(" ");
		for(int i=0;i<privArr.length;i++) {
			privs.put(privArr[i].toLowerCase(),Boolean.TRUE);
		}
		return privs;
	}
	
	/**
	 * Queries GrouperConfig - grouper.properties - to determine which Access
	 * privs are granted to GrouperAll on group creation
	 * @return
	 */
	public static Map getDefaultAccessPrivsForGrouperAPI() {
		Map privs = new HashMap();
		String priv;
		GrouperConfig config = GrouperConfig.getInstance();
		for(int i=0;i<groupPrivs.length;i++){
			priv = groupPrivs[i].toLowerCase();
			if("true".equals(config.getProperty("groups.create.grant.all." + priv))){
				privs.put(priv,Boolean.TRUE);
			}
		}
		return privs;
	}
	
	
	/**
	 * Use to get SubjectPrivilegeMaps rather than SubjectMaps. Relevant UI now
	 * uses these objects which can be used for more fine-grained template resolution
	 * @param s
	 * @param subjects
	 * @param group
	 * @param privilege
	 * @return
	 */
	public static List subjects2SubjectPrivilegeMaps(GrouperSession s,Collection subjects,Group group, String privilege) {
		return subjects2SubjectPrivilegeMaps(s,subjects,GroupOrStem.findByGroup(s,group),privilege);
	}
	
	/**
	 * Use to get SubjectPrivilegeMaps rather than SubjectMaps. Relevant UI now
	 * uses these objects which can be used for more fine-grained template resolution
	 * @param s
	 * @param subjects
	 * @param stem
	 * @param privilege
	 * @return
	 */
	public static List subjects2SubjectPrivilegeMaps(GrouperSession s,Collection subjects,Stem stem, String privilege) {
		return subjects2SubjectPrivilegeMaps(s,subjects,GroupOrStem.findByStem(s,stem),privilege);	
	}
	
	/**
	 * Use to get SubjectPrivilegeMaps rather than SubjectMaps. Relevant UI now
	 * uses these objects which can be used for more fine-grained template resolution
	 * @param s
	 * @param subjects
	 * @param groupOrStem
	 * @param privilege
	 * @return
	 */
	public static List subjects2SubjectPrivilegeMaps(GrouperSession s,Collection subjects,GroupOrStem groupOrStem, String privilege) {
		List res = new ArrayList();
		Subject subject;
		Iterator it = subjects.iterator();
		while(it.hasNext()) {
			subject = (Subject)it.next();
			res.add(new SubjectPrivilegeAsMap(s,subject,groupOrStem,privilege));
		}
		return res;
	}
	
	/**
	 * Use to get SubjectPrivilegeMaps rather than SubjectMaps. Relevant UI now
	 * uses these objects which can be used for more fine-grained template resolution
	 * @param s
	 * @param groupsOrStems
	 * @param subject
	 * @param privilege
	 * @return
	 */
	public static List subjects2SubjectPrivilegeMaps(GrouperSession s,Collection groupsOrStems,Subject subject, String privilege) {
		List res = new ArrayList();
		GroupOrStem groupOrStem;
		Iterator it = groupsOrStems.iterator();
		Object obj;
		while(it.hasNext()) {
			obj = it.next();
			if(obj instanceof Group){
				res.add(new SubjectPrivilegeAsMap(s,subject,GroupOrStem.findByGroup(s,(Group)obj),privilege));
			}else{
				res.add(new SubjectPrivilegeAsMap(s,subject,GroupOrStem.findByStem(s,(Stem)obj),privilege));
			}
		}
		return res;
	}
	
	/**
	 * Use to get MembershipMaps rather than SubjectMaps. Relevant UI now
	 * uses these objects which can be used for more fine-grained template resolution
	 * @param s
	 * @param memberships
	 * @return
	 */
	public static List memberships2Maps(GrouperSession s,Collection memberships) {
		return memberships2Maps(s,memberships,false);
	
	}
	
	/**
	 * Use to get MembershipMaps rather than SubjectMaps. Relevant UI now
	 * uses these objects which can be used for more fine-grained template resolution
	 * @param s
	 * @param memberships
	 * @param withParents
	 * @return
	 */
	public static List memberships2Maps(GrouperSession s,Collection memberships,boolean withParents) {
		List res = new ArrayList();
		Membership membership;
		Iterator it = memberships.iterator();
		while(it.hasNext()) {
			membership = (Membership)it.next();
			res.add(new MembershipAsMap(membership,withParents)); 
		}
		return res;
	}
	
	
	/**
	 * For a given subject determine all the custom list fields they appear in
	 * @param s
	 * @param subject
	 * @return
	 * @throws Exception
	 */
	public static List getListFieldsForSubject(GrouperSession s,Subject subject) throws Exception{
		Set lists = FieldFinder.findAllByType(FieldType.LIST);
		List res = new ArrayList();
		Iterator it = lists.iterator();
		Field field;
		Set memberships;
		while(it.hasNext()) {
			field = (Field)it.next();
			if(!"members".equals(field.getName())) {
				memberships = MemberFinder.findBySubject(s,subject).getMemberships(field);
				if(memberships.size()>0) {
					res.add(field.getName());
				}
			}
		}
		return res;
	}
	
	
	/**
	 * For a group id, for all its types, return fields of type LIST
	 * @param s
	 * @param groupId
	 * @return
	 * @throws Exception
	 */
	public static List getListFieldsForGroup(GrouperSession s,String groupId) throws Exception{
		Group g = GroupFinder.findByUuid(s,groupId);
		return getListFieldsForGroup(s,g);
	}
	
	
	/**
	 * For a group, for all its types, return fields of type LIST
	 * @param s
	 * @param g
	 * @return
	 */
	public static List getListFieldsForGroup(GrouperSession s,Group g) throws SchemaException{
		List lists = new ArrayList();
		Set types = g.getTypes();
		Iterator it = types.iterator();
		Set fields;
		Field field;
		Iterator fieldsIt;
		GroupType type;
		while(it.hasNext()) {
			type = (GroupType)it.next();
			fields=type.getFields();
			fieldsIt=fields.iterator();
			while(fieldsIt.hasNext()) {
				field = (Field)fieldsIt.next();
				if(field.getType().equals(FieldType.LIST)&& !"members".equals(field.getName())) {
					if(canRead(s,field,g)) {
						lists.add(field.getName());
					}
				}
			}
		}
		
		return lists;
	}
	
	/**
	 * For a group, for all its types, return fields user can read / write
	 * @param s
	 * @param g
	 * @param priv read / write
	 * @return
	 */
	public static Map getFieldsForGroup(GrouperSession s,Group g,String priv) throws SchemaException{
		Map fieldsMap = new HashMap();
		Set types = g.getTypes();
		Iterator it = types.iterator();
		Set fields;
		Field field;
		Iterator fieldsIt;
		GroupType type;
		while(it.hasNext()) {
			type = (GroupType)it.next();
			fields=type.getFields();
			fieldsIt=fields.iterator();
			while(fieldsIt.hasNext()) {
				field = (Field)fieldsIt.next();
				if(("read".equals(priv) && canRead(s,field,g))
						||("write".equals(priv) && canWrite(s,field,g))) {
						fieldsMap.put(field.getName(),Boolean.TRUE);
				}
			}
		}
		
		return fieldsMap;
	}
	
	/**
	 * Can the current user read this field?
	 * Should probably remove, API support is there now
	 * @param s
	 * @param field
	 * @param g
	 * @return
	 */
	public static boolean canRead(GrouperSession s,Field field,Group g) throws SchemaException{
		return g.canReadField(field);
	}
	
	/**
	 * Can the current write read this field?
	 * Should probably remove, API support is there now
	 * @param s
	 * @param field
	 * @param g
	 * @return
	 */
	public static boolean canWrite(GrouperSession s,Field field,Group g) throws SchemaException{
		return g.canWriteField(field);
		
	}
	
	
	private static String noSearchFields="";//:name:displayName:extension:displayExtension:";
	
	/**
	 * Retrieve list of attributes which can be searched
	 * @return
	 * @throws SchemaException
	 */
	public static  List getSearchableFields() throws SchemaException{
		List res = new ArrayList();
		Set fields = FieldFinder.findAllByType(FieldType.ATTRIBUTE);
		Iterator it = fields.iterator();
		Field field;
		while(it.hasNext()) {
			field = (Field) it.next();
			if(noSearchFields.indexOf(":" + field.getName() + ":") == -1) {
				res.add(field);
			}
		}
		return res;
	}
}


