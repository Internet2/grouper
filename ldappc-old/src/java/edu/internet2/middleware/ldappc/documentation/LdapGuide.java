/*
 Copyright 2004-2006 University Corporation for Advanced Internet Development, Inc.
 Copyright 2004-2006 The University Of Chicago
 
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

package edu.internet2.middleware.ldappc.documentation;


/**
 *
 * <p>
 * This document provides a guide to information about LDAP 
 * (Lightweight Directory Access Protocol) that is central to the purpose of
 * LDAP provisioner provide by the the Ldappc program.
 * This document is intended to provide a starting point for those interested
 * in learning more about LDAP; it is not intended to provided detailed
 * information itself. 
 * </p>
 *
 * <h2>Introduction</h2>
 *
 * <p>
 * LDAP provides a standardize method of interfacing with a hierarchical database.
 * While relational databases provide great flexibility in handling data, a 
 * hierarchical can provided much faster access to data that naturally models
 * a tree-like structure.  
 *
 * A major benefit of using LDAP is that the protocol is
 * much more of a universal standard that is the SQL used with relational
 * databases, which has many incompatable incarnations.  LDAP allows 
 * its directories to reference other LDAP directories on different operating
 * systems and different LDAP servers.
 *
 * This ability of LDAP to transparently interconnect various databases makes 
 * it ideal for allowing institutions to access information at other institutions
 * in a secure manner.  Authentication and authorization services are a primary
 * use of LDAP, in addition to rapid location of institutional data.
 *
 * </p>
 *
 * <h2>LDAP Server</h2>
 *
 * <p> 
 * The use of LDAP depends on having an LDAP server.  Many such servers are commercially
 * available and some free ones exist.  The one used in the development of  
 * the Ldappc program is the free OpenLDAP server.  
 * See <a href=http://www.openldap.org/>http://www.openldap.org</a> for additional
 * information about this server.  Many alternative servers exist and should work
 * equally well, although the Ldappc documentation will limit its discussion of
 * setting up the program for the OpenLDAP server.  Novell and Microsoft are two 
 * of the more popular commercial LDAP servers.
 * </p>
 *
 * <h2>Learning LDAP</h2>
 *
 * <p> 
 * The following sections describe where to find information for learning some
 * of the different skills involved with an LDAP application.
 * The authors could not find a small number of good
 * sources to assist in the development.  Therefore, following paragraphs 
 * provide some of the many Internet sources that were used. 
 * </p> 
 * 
 * <h3>Java Programming for LDAP</h3>
 *
 * <p> 
 * The authors of Ldappc each purchased a book on LDAP, but can not particularly
 * recommend either book for someone wishing to learn Java programming for LDAP.
 * There is a book entitled "LDAP Programming with Java".  While it has some material
 * of general interest about LDAP, most of the book is relevant only to those who
 * use the Netscape SDK for Java, which was not used for the development of Ldappc. 
 * There very well may be worthwhile books for those interested in a administering a 
 * particular LDAP server, we did not investigate these as the only server used
 * for development was the OpenLDAP server discussed below.  
 * </p>
 * 
 * <h3>LDAP and JNDI</h3>
 *
 * <p>
 * Java 2 Standard Edition (J2SE) includes a client-side Java Naming and Directory 
 * Interface(JNDI) implementation for LDAP.  This package is used to achieve 
 * communications between the Ldappc Java program and any LDAP directory server.
 * A good starting point for learning JNDI for use with LDAP is Sun Microsystem's
 * web site: <a href=http://java.sun.com/products/jndi/tutorial/>The JNDI Tutorial</a>.
 * </p>
 * <p>
 * IBM provides several good sources of information about LDAP, including
 * <a href=http://www-128.ibm.com/developerworks/java/library/j-apacheds1/index.html>
 * Storing Java objects in Apache Directory Server</a>, 
 * which, while focused on the Apache LDAP Server, contains a good set of links and,
 * in part 2, good examples of the use of JNDI.
 * </p>
 *
 * <h3>LDAP Server Guides</h3>
 *
 * <p> 
 * The OpenLDAP Administrator's Guide can be found at 
 * <a href=http://www.openldap.org/doc/admin23/>http://www.openldap.org/doc/admin23/</a>.
 * In addition to its administrative content, it contains a good short 
 * description of the structure of an LDAP directory.
 * It also contains  
 * <a href=http://www.openldap.org/pub>references to security and performance documents</a>.
 * </p>
 *
 * <h2>Other LDAP References</h2>
 * <p>
 * The following link to the Wikipedia is a good starting place for learning about LDAP in general. 
 * </p>
 * <p>
 * <a href=http://en.wikipedia.org/wiki/Lightweight_Directory_Access_Protocol#Unbind>
 * The Wikipedia on LDAP 
 * </a> 
 * </p>
 * <p>
 * Ths following is a partial list of source of information used in the project related to LDAP. 
 * </p>
 * <p>
 * <a href=http://www-unix.mcs.anl.gov/~gawor/ldap/>A free LDAP browser</a>
 * </p>
 * <a href=http://www.javaworld.com/javaworld/jw-03-2000/jw-0324-ldap.html?page=1>
 *  LDAP and JNDI: Together Forever
 * </a> 
 * <p>
 * <a href=http://www.daasi.de/staff/norbert/thesis.pdf#search=%22openldap%20versus%20netscape%20directory%20sdk%22>
 * Directory Services for Linux: in comparison with Novell NDS and Microsoft Active Directory
 * </a> 
 * </p>
 * <p>
 * <a href=http://www.novell.com/documentation/nas4nw/usnas4nw/nasnwenu/ldapsrch.html>
 * Finding Directory Entries 
 * </a> 
 * </p>
 * <p>
 * <a href=http://www.enterprisenetworkingplanet.com/netsysm/article.php/10954_3322861>
 * LDAP Searches from Darn Near Anywhere 
 * </a> 
 * </p>
 * <p>
 * <a href=http://publib.boulder.ibm.com/infocenter/tivihelp/v2r1/index.jsp?topic=/com.ibm.IBMDS.doc/progref06.htm> 
 * IBM Tivoli Directory Server information center 
 * </a> 
 * </p>
 * <p>
 * <a href=http://www.geocities.com/raoavm/ldapdesign.html >
 * LDAP Schema Design - case study 
 * </a> 
 * </p>
 
 * <a href=http://www.skills-1st.co.uk/papers/ldap-schema-design-feb-2005/ldap-schema-design-feb-2005.pdf#search=%22%22ldap%20schema%20design%22%22>
 * LDAP Schema Design 
 * </a> 
 * </p>
 * <p>
 * <a href=http://www.phptr.com/articles/article.asp?p=28786&rl=1> 
 * Principles of LDAP Schema Design 
 * </a> 
 * </p>
 * <p>
 * <a href=http://www.ietf.org/rfc/rfc2256.txt>
 * The IETF defining document for user schema LDAPv3 
 * </a> 
 * </p>
 * <p>
 * </p>
 * <p>
 *
 */

public class LdapGuide
{
}
