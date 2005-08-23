/*
 * Copyright (C) 2004-2005 University Corporation for Advanced Internet Development, Inc.
 * Copyright (C) 2004-2005 The University Of Bristol
 * All Rights Reserved. 
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 *  * Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *  * Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *  * Neither the name of the University of Bristol nor the names
 *    of its contributors nor the University Corporation for Advanced
 *   Internet Development, Inc. may be used to endorse or promote
 *   products derived from this software without explicit prior
 *   written permission.
 *
 * You are under no obligation whatsoever to provide any enhancements
 * to the University of Bristol, its contributors, or the University
 * Corporation for Advanced Internet Development, Inc.  If you choose
 * to provide your enhancements, or if you choose to otherwise publish
 * or distribute your enhancements, in source code form without
 * contemporaneously requiring end users to enter into a separate
 * written license agreement for such enhancements, then you thereby
 * grant the University of Bristol, its contributors, and the University
 * Corporation for Advanced Internet Development, Inc. a non-exclusive,
 * royalty-free, perpetual license to install, use, modify, prepare
 * derivative works, incorporate into the software or other computer
 * software, distribute, and sublicense your enhancements or derivative
 * works thereof, in binary and source code form.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND WITH ALL FAULTS.  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING,
 * BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE, AND NON-INFRINGEMENT ARE DISCLAIMED AND the
 * entire risk of satisfactory quality, performance, accuracy, and effort
 * is with LICENSEE. IN NO EVENT SHALL THE COPYRIGHT OWNER, CONTRIBUTORS,
 * OR THE UNIVERSITY CORPORATION FOR ADVANCED INTERNET DEVELOPMENT, INC.
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
 * BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OR DISTRIBUTION OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package edu.internet2.middleware.grouper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.internet2.middleware.grouper.ui.PersonalStem;
import edu.internet2.middleware.grouper.ui.util.GroupAsMap;
import edu.internet2.middleware.grouper.ui.util.ObjectAsMap;
import edu.internet2.middleware.grouper.ui.util.StemAsMap;
import edu.internet2.middleware.grouper.ui.util.SubjectAsMap;
import edu.internet2.middleware.subject.Source;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectNotFoundException;
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
 * @version $Id: GrouperHelper.java,v 1.1.1.1 2005-08-23 13:04:13 isgwb Exp $
 */

public class GrouperHelper {

	private static Map superPrivs = null; //Privs automatically granted to the
										  // system user

	private static List personSources = null; //Subject sources which source
											  // 'people'

	public static final String HIER_DELIM = Grouper.HIER_DELIM; //Currently :
																// (name
																// separator)
	
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
		superPrivs.put("OPTIN", Boolean.TRUE);
		superPrivs.put("OPTOUT", Boolean.TRUE);
	}
	
	//Privs which relate to Groups - access privileges
	private static String[] groupPrivs = { "ADMIN", "UPDATE", "READ", "VIEW",
			"OPTIN", "OPTOUT" };
	
	//Privs which relate to Stems - naming privileges
	private static String[] stemPrivs = { "STEM", "CREATE" };

	public static void main(String args[]) throws Exception{
		Subject subj = SubjectFactory.getSubject("GrouperSystem");
		GrouperSession s = GrouperSession.start(subj);
		GrouperGroup g = GrouperGroup.create(s,Grouper.NS_ROOT,"DUMMY");
		g.attribute("displayExtension","Dummy Group");
		s.stop();
	}


	/**
	 * Given a GrouperStem id return a list of stems and groups for which the
	 * GrouperStem is an immediate parent
	 * @param s GrouperSession for authenticated user
	 * @param stemId GrouperStem id
	 * @return List of all stems and groups for stemId
	 */
	public static List getChildren(GrouperSession s, String stemId) {
		if(Grouper.NS_ROOT.equals(stemId)) {
			List children = GrouperStem.getRootStems(s);
			return children;
		}
		GrouperStem stem = (GrouperStem) GrouperStem.loadByName(s, stemId);
		List children = stem.stems();
		children.addAll(stem.groups());
		return children;
	}

	/**
	 * Given a list of GrouperAttributes, return a list of GrouperStems
	 * which the attributes belong to, and load all atributes for these Stems
	 * 
	 * @param s GrouperSession for authenticated user
	 * @param list of GrouperAttributes
	 * @return List of GrouperGroups or GrouperStems
	 */
	public static List instantiateStems(GrouperSession s, List list) {
		return instantiateGroups(s, list);
	}



	/**
	 * Given a list of GrouperAttributes, return a list of GrouperGroups
	 * which the attributes belong to, and load all atributes for these groups
	 * 
	 * @param s GrouperSession for authenticated user
	 * @param list of GrouperAtributes
	 * @return List of GrouperGroups or GrouperStems
	 */
	public static List instantiateGroups(GrouperSession s, List list) {
		List instantiated = new ArrayList();
		GrouperAttribute attr = null;
		String key;
		GrouperGroup stem = null;
		for (int i = 0; i < list.size(); i++) {
			attr = (GrouperAttribute) list.get(i);
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
	}

	/**
	 * Given a GrouperStem id return a list of Maps representing the children
	 * of that stem. 
	 * 
	 * @param s GrouperSession for authenticated user
	 * @param stemId
	 * @return List of GrouperGroups and GrouperStems wrapped as Maps
	 */
	public static List getChildrenAsMaps(GrouperSession s, String stemId) {
		List stems = getChildren(s, stemId);
		List maps = new ArrayList();
		for (int i = 0; i < stems.size(); i++) {
			maps.add(group2Map(s, (Group) stems.get(i)));
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
	public static List groups2Maps(GrouperSession s, List groups) {
		List maps = new ArrayList();
		Object obj;
		for (int i = 0; i < groups.size(); i++) {
			//Just in case something goes wrong - Group doesn't exist but still
			// a pointer to it
			try {
				obj = groups.get(i);
				if (obj instanceof GrouperList)
					obj = ((GrouperList) obj).group();
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
	public static List stems2Maps(GrouperSession s, List stems) {
		return groups2Maps(s, stems);

	}
	
	/**
	 * Given a GrouperStem return a Map representing it
	 * @param s GrouperSession for authenticated user
	 * @param stem GrouperStem to wrap
	 * @return GrouperStem wrapped as a Map
	 */
	public static Map stem2Map(GrouperSession s, GrouperStem stem) {
		return group2Map(s, stem);

	}

	/**
	 * Given a Group (which can be a GrouperGroup or GrouperStem) return
	 * a Map representation of it
	 * @param s GrouperSession for authenticated user
	 * @param groupOrStem GrouperGroup or GrouperStem to wrap
	 * @return GrouperStem or GrouperGroup wrapped as a Map
	 */
	public static Map group2Map(GrouperSession s, Group groupOrStem) {
		GrouperGroup group = null;
		GrouperStem stem = null;
		if (groupOrStem instanceof GrouperStem) {
			stem = (GrouperStem) groupOrStem;
			Map attr = stem.attributes();
			if (attr.size() == 0) {
				String id = stem.id();
				stem = (GrouperStem) GrouperStem.loadByID(s, id);

			}
		} else {
			group = (GrouperGroup) groupOrStem;
			Map attr = group.attributes();
			if (attr.size() == 0) {
				String id = group.id();
				group = (GrouperGroup) GrouperGroup.loadByID(s, id);
			}
		}

		ObjectAsMap map = null;
		//If no displayExtension improvise @TODO review this
		if (stem != null) {
			map = new StemAsMap(stem, s);
			if (map.get("displayExtension") == null)
				map.put("displayExtension", map.get("extension"));
			map.put("key", stem.key());
		} else {
			map = new GroupAsMap(group, s);
			if (map.get("displayExtension") == null)
				map.put("displayExtension", map.get("extension"));
			map.put("key", group.key());
		}

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
	public static List parentStemsAsMaps(GrouperSession s, Group groupOrStem) {
		List path = new ArrayList();
		if(groupOrStem==null) return path;
		Map map = group2Map(s, groupOrStem);

		GrouperStem curStem = null;
		while (!Grouper.NS_ROOT.equals(map.get("stem"))) {
			curStem = GrouperStem.loadByName(s, (String) map.get("stem"));
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
	public static Map hasAsMap(GrouperSession s, Group group) {
		return hasAsMap(s, group, false);
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
	public static Map hasAsMap(GrouperSession s, Group groupOrStem,
			boolean isMortal) {
		Map privs = null;

		GrouperGroup g = null;
		GrouperStem stem = null;
		privs = new HashMap();
		if (!isMortal
				&& Grouper.config("member.system").equals(s.subject().getId())) {
			privs.putAll(superPrivs);
			if(groupOrStem==null) return privs;
			if (groupOrStem instanceof GrouperGroup) {
				g = (GrouperGroup) groupOrStem;
				GrouperMember member = GrouperMember.load(s.subject());
				if (g.hasMember(member)) {
					privs.put("MEMBER", Boolean.TRUE);
				}
			} else {
				stem = (GrouperStem) groupOrStem;
			}
			if (privs == null)
				privs = superPrivs;
			return privs;
		}
		if(groupOrStem==null && Grouper.config("member.system").equals(s.subject().getId())) {
			privs = new HashMap();
			privs.put(Grouper.PRIV_STEM,Boolean.TRUE);
			return privs;
		}
		if(groupOrStem==null) return new HashMap();
		if (groupOrStem instanceof GrouperGroup) {
			g = (GrouperGroup) groupOrStem;
		} else {
			stem = (GrouperStem) groupOrStem;
		}
		List privList = null;
		if (g != null) {
			privList = s.access().has(s, g);
		} else {
			privList = s.naming().has(s, stem);
		}
		for (int i = 0; i < privList.size(); i++) {
			privs.put(privList.get(i), Boolean.TRUE);
		}
		if (g != null) {
			GrouperMember member = GrouperMember.load(s.subject());
			if (g.hasMember(member))
				privs.put("MEMBER", Boolean.TRUE);
		}

		//Cache.instance().put(s,privKey,privs);

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
	public static Map hasAsMap(GrouperSession s, Group groupOrStem,
			GrouperMember member) {
		Map privs = null;
		if (Grouper.config("member.system").equals(member.subjectID())) {
			//@TODO Review
			//return superPrivs;

		}
		List privList = null;

		privs = new HashMap();
		GrouperGroup group = null;
		GrouperStem stem = null;
		if (groupOrStem instanceof GrouperGroup) {
			group = (GrouperGroup) groupOrStem;
			privList = s.access().has(s, group, member);
		} else {
			stem = (GrouperStem) groupOrStem;
			privList = s.naming().has(s, stem, member);
		}
		for (int i = 0; i < privList.size(); i++) {
			privs.put(privList.get(i), Boolean.TRUE);
		}
		if (group != null) {
			if (group.hasMember(member))
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

	
	/**
	 * Given a subject id and subject type return a Map representation of it.
	 * 
	 * @param s GrouperSession for authenticated user
	 * @param subjectId Subject id
	 * @param subjectType Subject type e.g. person, group
	 * @return Subject wrapped as a Map
	 */
	public static Map subject2Map(GrouperSession s, String subjectId,
			String subjectType) {
		if (!"group".equals(subjectType)) {
			Subject subject = null;
			try {
				subject = SubjectFactory.getSubject(subjectId, subjectType);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
			SubjectAsMap map = new SubjectAsMap(subject);
			return (Map) map;
		}
		GrouperGroup group = groupLoadById(s, subjectId);
		Map groupMap = group2Map(s, group);
		return groupMap;
	}

	/**
	 * Given an array of Subjects return a List of Maps representing those subjects
	 * 
	 * @param objects array of Subjects
	 * @return List of Subjects wrapped as Maps
	 */
	public static List subjects2Maps(Object[] objects) {
		if (objects instanceof Subject[])
			return subjects2Maps((Subject[]) objects);
		Subject[] subjects = new Subject[objects.length];
		for (int i = 0; i < objects.length; i++) {
			subjects[i] = (Subject) objects[i];
		}
		return subjects2Maps(subjects);
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
			int start, int pageSize) {
		return groupList2SubjectsMaps(s, members, null, start, pageSize);
	}

	/**
	 * Given a list of GrouperMembers return a list of Map representations of them
	 * @param s GrouperSession for authenticated user
	 * @param members List of GrouperLists or GrouperMembers
	 * @return List of Subjects wrapped as Maps
	 */
	public static List groupList2SubjectsMaps(GrouperSession s, List members) {
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
			String asMemberOf) {
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
			String asMemberOf, int start, int pageSize) {
		int end = start + pageSize;
		if (end > members.size())
			end = members.size();
		List maps = new ArrayList();
		GrouperList list = null;
		GrouperMember member = null;
		Subject subject;
		Map subjMap = null;
		Object listItem;
		GrouperGroup via = null;
		List chain = null;
		Object chainItem = null;
		GrouperGroup firstInChain = null;
		GrouperMember chainMember = null;
		String[] chainGroupIds = null;
		for (int i = start; i < end; i++) {

			listItem = members.get(i);
			if (listItem instanceof GrouperList) {
				list = (GrouperList) listItem;
				via = (GrouperGroup) list.via();
				chain = list.chain();
				if (chain != null && chain.size() > 0) {
					chainGroupIds = new String[chain.size() + 1];
					for (int j = 0; j < chain.size(); j++) {
						chainMember = (GrouperMember) ((MemberVia) chain.get(j))
								.toList(s).member();
						if (j == 0)
							firstInChain = GrouperGroup.loadByID(s, chainMember
									.subjectID());
						chainGroupIds[j] = chainMember.subjectID();
					}
					if (via != null)
						chainGroupIds[chainGroupIds.length - 1] = via.id();
				} else {
					firstInChain = null;
				}
				member = list.member();

			} else if (listItem instanceof GrouperMember)
				member = (GrouperMember) listItem;
			try {
				subject = SubjectFactory.getSubject(member.subjectID(), member
						.typeID());
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
			if (subject.getType().getName().equals("group")) {
				GrouperGroup group = groupLoadById(s, subject.getId());
				subjMap = group2Map(s, group);
			} else {
				subjMap = subject2Map(subject);
			}
			if (firstInChain != null)
				subjMap.put("via", group2Map(s, firstInChain));
			if (asMemberOf != null)
				subjMap.put("asMemberOf", asMemberOf);
			if (chain != null) {
				subjMap.put("chain", chain);
				subjMap.put("chainSize", new Integer(chain.size()));
				subjMap.put("chainGroupIds", chainGroupIds);
			}

			maps.add(subjMap);
		}
		return maps;
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
			Subject[] members, String[] privileges, boolean forStems) {
		GrouperGroup group = null;
		GrouperStem stem = null;
		GrouperAccess access = s.access();
		GrouperNaming naming = s.naming();
		Subject subject;
		if (forStems) {
			stem = (GrouperStem) GrouperStem.loadByID(s, stemOrGroupId);
		} else {
			group = (GrouperGroup) GrouperGroup.loadByID(s, stemOrGroupId);
		}
		for (int i = 0; i < members.length; i++) {
			subject = members[i];
			for (int j = 0; j < privileges.length; j++) {
				try {
					if ("member".equals(privileges[j])) {
						group.listAddVal(GrouperMember.load(s, subject.getId(),
								subject.getType().getName()));
					} else if (forStems) {
						naming.grant(s, stem, GrouperMember.load(s, subject
								.getId(), subject.getType().getName()),
								privileges[j]);
					} else {
						access.grant(s, group, GrouperMember.load(s, subject
								.getId(), subject.getType().getName()),
								privileges[j]);
					}
				} catch (RuntimeException e) {
					//@TODO Expect different type of Exception in future
					if (e.getMessage().indexOf("List value already exists") == -1)
						throw e;
				} catch (SubjectNotFoundException e) {
					throw new RuntimeException(e);
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
	public static Map getValidStems(GrouperSession s, String browseMode) {
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
	}

	
	/**
	 * Is s.subject() the system user?
	 * 
	 * @param s GrouperSession for authenticated user
	 * @return boolean
	 */
	public static boolean isSuperUser(GrouperSession s) {
		return s.subject().getId().equals(Grouper.config("member.system"));
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
			return SubjectFactory.getSubject(subjectId, subjectType);
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
	public static GrouperGroup groupLoadById(GrouperSession s, String id) {

		GrouperGroup group = null;

		group = (GrouperGroup) GrouperGroup.loadByID(s, id);
		
		return group;

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
	public static List searchGroupsByAttribute(GrouperSession s, String query, String from,String attr) {

		
			String type = null;

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
			return res;
		
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
	public static List searchGroups(GrouperSession s, String query, String from,String searchInDisplayNameOrExtension,String searchInNameOrExtension) {
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
			String from, String searchInDisplayNameOrExtension,String searchInNameOrExtension,String browseMode) {
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
					new String[] { Grouper.PRIV_ADMIN, Grouper.PRIV_UPDATE,
							Grouper.PRIV_READ });
		} else if ("Join".equals(browseMode)) {
			allowedSet = GrouperHelper.getGroupsForPrivileges(s,
					new String[] { Grouper.PRIV_OPTIN });
		}
		if (allowedSet != null) {
			Map allowed = new HashMap();
			Iterator it = allowedSet.iterator();
			GrouperList groupAsList;
			while (it.hasNext()) {
				groupAsList = (GrouperList) it.next();
				//allowed.put(groupAsList.getGroupKey(),Boolean.TRUE);
			}
			GrouperGroup group;
			for (int i = 0; i < res.size(); i++) {
				group = (GrouperGroup) res.get(i);
				if (allowed.containsKey(group.key()))
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

		
			String type = null;

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
			return res;
		
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
	public static List searchStems(GrouperSession s, String query, String from,String searchInDisplayNameOrExtension,String searchInNameOrExtension) {
		List displayResults = null;
		List nonDisplayResults=null; 
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
		GrouperMember member = null;
		try {
			member = GrouperMember.load(s, s.subject().getId(), s.subject()
					.getType().getName());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		List vals = member.listVals();
		GrouperList list;
		GrouperGroup group;

		for (int i = 0; i < vals.size(); i++) {
			list = (GrouperList) vals.get(i);
			group = (GrouperGroup) list.group();
			memberships.put(group.key(), Boolean.TRUE);
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
		GrouperMember member = null;
		try {
			member = GrouperMember.load(s, s.subject().getId(), s.subject()
					.getType().getName());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		List vals = member.listVals();
		GrouperList list;
		GrouperGroup group;
		int end = start + pageSize;
		if (end > vals.size())
			end = vals.size();
		if (totalCount != null) {
			totalCount.setLength(0);
			totalCount.append("" + vals.size());
		}
		for (int i = start; i < end; i++) {
			list = (GrouperList) vals.get(i);
			group = (GrouperGroup) list.group();
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
	public static Set getGroupsForPrivileges(GrouperSession s, String[] privs) {

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
			int start, int pageSize, StringBuffer resultCount) {
		GrouperAccess accessImpl = s.access();
		Set groupSet = new LinkedHashSet();
		List groups = new ArrayList();
		Set allSet = new LinkedHashSet();
		Iterator it;
		GrouperList gl;

		for (int i = 0; i < privs.length; i++) {
			allSet.addAll(accessImpl.has(s, privs[i]));

		}

		int end = start + pageSize;
		if (end > allSet.size())
			end = allSet.size();
		if (resultCount != null) {
			resultCount.setLength(0);
			resultCount.append("" + allSet.size());
		}
		groups.addAll(allSet);
		for (int i = start; i < end; i++) {
			gl = (GrouperList) groups.get(i);
			if (resultCount == null) {
				groupSet.add(gl);
			} else {
				groupSet.add(gl.group());
			}
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
	public static Set getStemsForPrivileges(GrouperSession s, String[] privs) {

		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < privs.length; i++) {
			sb.append(privs[i]);
		}
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
			int start, int pageSize, StringBuffer resultCount) {

		GrouperNaming namingImpl = s.naming();
		Set stemSet = new LinkedHashSet();
		Set allSet = new LinkedHashSet();
		List stems = new ArrayList();

		GrouperList gl;
		for (int i = 0; i < privs.length; i++) {
			allSet.addAll(namingImpl.has(s, privs[i]));

		}
		int end = start + pageSize;
		if (end > allSet.size())
			end = allSet.size();
		if (resultCount != null) {
			resultCount.setLength(0);
			resultCount.append("" + allSet.size());
		}
		stems.addAll(allSet);
		for (int i = start; i < end; i++) {
			gl = (GrouperList) stems.get(i);
			if (resultCount == null) {
				stemSet.add(gl);
			} else {
				stemSet.add(gl.group());
			}
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
	public static boolean stemDelete(GrouperSession s, GrouperStem stem)
			throws Exception {
		if (stem == null || !s.naming().has(s, stem, Grouper.PRIV_STEM)) {
			return false;
		}
		String stemStr = stem.name() + HIER_DELIM;
		//@TODO: when searching scoped by stem fix
		List children = new ArrayList();//getNestedStemChildren(s,stemStr);
		if (children.size() > 100)
			throw new Exception("Too many children (" + children.size()
					+ ") - must be <=100");
		Object[] res;
		GrouperGroup g = null;
		boolean deleted = true;
		GrouperSession sysSession = null;
		try {
			sysSession = GrouperSession.start(SubjectFactory.getSubject(Grouper
					.config("member.system")));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		for (int i = 0; i < children.size(); i++) {
			res = (Object[]) children.get(i);
			g = (GrouperGroup) res[1];
			if (!groupDelete(sysSession, g)) {
				sysSession.stop();
				return false;
			}
		}

		sysSession.stop();
		return groupDelete(s, stem);
	}
	
	/**
	 * Given a GrouperGroup or GroupeStem delete it. GroupeStem must not have any children.
	 * 
	 * @param s GrouperSession for authenticated user
	 * @param groupOrStem GrouperGroup or GrouperStem to delete
	 * @return boolean indicating success
	 */
	public static boolean groupDelete(GrouperSession s, Group groupOrStem) {
		GrouperGroup group = null;
		GrouperStem stem = null;
		boolean deleted = true;
		if (groupOrStem == null)
			return false;
		if (groupOrStem instanceof GrouperStem) {
			stem = (GrouperStem) groupOrStem;
			if (!s.naming().has(s, stem, Grouper.PRIV_STEM)) {
				return false;
			}
			try {
				GrouperStem.delete(s, stem);
			} catch (Exception e) {
				deleted = false;
			}
			return deleted;
		} else {
			group = (GrouperGroup) groupOrStem;
			if (!s.access().has(s, group, Grouper.PRIV_ADMIN)) {
				return false;
			}
		}

		List members = group.listImmVals();//Eff?
		GrouperMember member;
		for (int i = 0; i < members.size(); i++) {
			member = ((GrouperList) members.get(i)).member();
			try {
				group.listDelVal(member);
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
			sysSession = GrouperSession.start(SubjectFactory.getSubject(Grouper
					.config("member.system")));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		GrouperMember groupAsMember = null;
		try {
			groupAsMember = GrouperMember.load(s, group.id(), "group");
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		for (int j = 0; j < listNames.length; j++) {
			String list = listNames[j];
			List memberships = groupAsMember.listImmVals(list);
			GrouperGroup memberOf;
			for (int i = 0; i < memberships.size(); i++) {
				memberOf = (GrouperGroup) ((GrouperList) memberships.get(i))
						.group();
				//memberOf =
				// (GrouperGroup)GrouperGroup.loadByKey(s,memberOf.key());
				String type = memberOf.type();
				memberOf.listDelVal(groupAsMember, list);
			}
		}
		sysSession.stop();

		try {
			GrouperGroup.delete(s, group);
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
	public static GrouperStem createIfAbsentPersonalStem(GrouperSession s,
			PersonalStem ps) throws Exception {
		if (s == null)
			return null;

		String stemName = ps.getPersonalStemRoot(s.subject()) + HIER_DELIM
				+ ps.getPersonalStemId(s.subject());
		GrouperStem stem = GrouperStem.loadByName(s, stemName);
		if (stem == null) {
			GrouperSession sysSession = null;
			try {
				sysSession = GrouperSession.start(SubjectFactory
						.getSubject(Grouper.config("member.system")));
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
			stem = GrouperStem.create(sysSession, ps.getPersonalStemRoot(s
					.subject()), ps.getPersonalStemId(s.subject()));
			stem.attribute("displayExtension", ps.getPersonalStemDisplayName(s
					.subject()));
			stem.attribute("description", ps.getPersonalStemDescription(s
					.subject()));

			GrouperMember member = GrouperMember.load(s.subject());
			s.naming().grant(sysSession, stem, member, Grouper.PRIV_CREATE);
			s.naming().grant(sysSession, stem, member, Grouper.PRIV_STEM);

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
}

