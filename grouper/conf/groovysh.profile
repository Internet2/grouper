:set verbosity QUIET
:set interpreterMode false

import org.codehaus.groovy.tools.shell.CommandSupport
import org.codehaus.groovy.tools.shell.Groovysh

import edu.internet2.middleware.grouper.*
import edu.internet2.middleware.grouper.app.gsh.*
import edu.internet2.middleware.grouper.privs.*
import edu.internet2.middleware.grouper.misc.*

class GSHFileLoad extends CommandSupport {
  protected GSHFileLoad(final Groovysh shell) {
    super(shell, ':gshFileLoad', ':gshfl')
  }

  @Override
  Object execute(final List<String> args) {
    assert args != null

    if (args.size() == 0) {
      fail("Command 'gshFileLoad' requires at least one argument")
    }

    for (source in args) {
      log.debug("Attempting to load: \"$source\"")

      def file = new File("$source")

      if (!file.exists()) {
        fail("File not found: \"$file\"")
      }

      URL url = file.toURI().toURL()

      load(url)
    }
  }

  void load(final URL url) {
    assert url != null

    if (io.verbose) {
      io.out.println("Loading: $url")
    }

    url.eachLine { String it, int lineNumber ->
      if (lineNumber == 1 && it.startsWith('#!')) {
        return
      }

      if (it.equals("exit") || it.startsWith("exit ") || it.startsWith("exit;") || it.equals("quit") || it.startsWith("quit ") || it.startsWith("quit;")) {
        return
      }

      if (!it.startsWith("#")) {
        try {
          shell << it as String
        } catch (Throwable t) {
          t.printStackTrace();
        }
      }
    }
  }
}

:register GSHFileLoad

GrouperSession.startRootSession()

def addComposite (String group, CompositeType type, String left, String right) {
  new addComposite().invoke(GrouperSession.staticGrouperSession(), group, type, left, right)
}

def addGroup (String parent, String extn, String displayExtn) {
  new addGroup().invoke(GrouperSession.staticGrouperSession(), parent, extn, displayExtn)
}

def addMember (String group, String subjectIdOrIdentifier, Field field=Group.getDefaultList()) {
  new addMember().invoke(GrouperSession.staticGrouperSession(), group, subjectIdOrIdentifier, field)
}

def addRootStem (String extn, String displayExtn) {
  new addRootStem().invoke(GrouperSession.staticGrouperSession(), extn, displayExtn)
}

def addStem (String parent, String extn, String displayExtn) {
  new addStem().invoke(GrouperSession.staticGrouperSession(), parent, extn, displayExtn)
}

def addSubject (String id, String type, String name, String description=null) {
  new addSubject().invoke(GrouperSession.staticGrouperSession(), id, type, name, description)
}

def assertTrue (String msg, boolean cond) {
  new assertTrue().invoke(GrouperSession.staticGrouperSession(), msg, cond)
}

def delComposite (String group) {
  new delComposite().invoke(GrouperSession.staticGrouperSession(), group)
}

def delGroup (String group) {
  new delGroup().invoke(GrouperSession.staticGrouperSession(), group)
}

def delMember (String group, String subjectIdOrIdentifier, Field field=Group.getDefaultList()) {
  new delMember().invoke(GrouperSession.staticGrouperSession(), group, subjectIdOrIdentifier, field)
}

def delStem (String name) {
  new delStem().invoke(GrouperSession.staticGrouperSession(), name)
}

def findBadMemberships () {
  new findBadMemberships().invoke(GrouperSession.staticGrouperSession())
}

def findSubject (String id, String type=null, String source=null) {
  new findSubject().invoke(GrouperSession.staticGrouperSession(), id, source)
}

def getGroupAttr (String name, String attr) {
  new getGroupAttr().invoke(GrouperSession.staticGrouperSession(), name, attr)
}

def getGroups (String name) {
  new getGroups().invoke(GrouperSession.staticGrouperSession(), name)
}

def getMembers (String group) {
  new getMembers().invoke(GrouperSession.staticGrouperSession(), group)
}

def getSources () {
  new getSources().invoke(GrouperSession.staticGrouperSession())
}

def getStemAttr (String name, String attr) {
  new getStemAttr().invoke(GrouperSession.staticGrouperSession(), name, attr)
}

def getStems (String name) {
  new getStems().invoke(GrouperSession.staticGrouperSession(), name)
}

def grantPriv (String name, String subjId, Privilege priv) {
  new grantPriv().invoke(GrouperSession.staticGrouperSession(), name, subjId, priv)
}

def groupAddType (String name, String type) {
  new groupAddType().invoke(GrouperSession.staticGrouperSession(), name, type)
}

def groupGetTypes (String name) {
  new groupGetTypes().invoke(GrouperSession.staticGrouperSession(), name)
}

def groupDelType (String name, String type) {
  new groupDelType().invoke(GrouperSession.staticGrouperSession(), name, type)
}

def groupHasType (String name, String type) {
  new groupHasType().invoke(GrouperSession.staticGrouperSession(), name, type)
}

def hasMember (String group, String subjId , Field field=Group.getDefaultList()) {
  new hasMember().invoke(GrouperSession.staticGrouperSession(), group, subjId, field)
}

def hasPriv (String name, String subjId , Privilege priv) {
  new hasPriv().invoke(GrouperSession.staticGrouperSession(), name, subjId, priv)
}

def help (String helpOn) {
  new help().invoke(helpOn)
}

def loaderDryRunOneJob (Group group, String fileName) {
  new loaderDryRunOneJob().invoke(GrouperSession.staticGrouperSession(), group, fileName)
}

def loaderRunOneJob (Object groupOrJobName) {
  new loaderRunOneJob().invoke(GrouperSession.staticGrouperSession(), groupOrJobName)
}

def loaderRunOneJobAttr (Object attributeDefOrJobName) {
  new loaderRunOneJobAttr().invoke(GrouperSession.staticGrouperSession(), attributeDefOrJobName)
}

def obliterateStem (String name, boolean testOnly, boolean deleteFromPointInTime) {
  new obliterateStem().invoke(GrouperSession.staticGrouperSession(), name, testOnly, deleteFromPointInTime)
}

def p (Object obj) {
  new p().invoke(obj)
}

def registryInitializeSchema (int options=0) {
  new registryInitializeSchema().invoke(GrouperSession.staticGrouperSession(), options)
}

def registryInstall () {
  new registryInstall().invoke(GrouperSession.staticGrouperSession())
}

def resetRegistry () {
  new resetRegistry().invoke(GrouperSession.staticGrouperSession())
}

def revokePriv (String name, String subjId, Privilege priv) {
  new revokePriv().invoke(GrouperSession.staticGrouperSession(), name, subjId, priv)
}

def setGroupAttr (String name, String attr, String val) {
  new setGroupAttr().invoke(GrouperSession.staticGrouperSession(), name, attr, val)
}

def setStemAttr (String name, String attr, String val) {
  new setStemAttr().invoke(GrouperSession.staticGrouperSession(), name, attr, val)
}

def sqlRun (Object scriptFileOrString) {
  new sqlRun().invoke(GrouperSession.staticGrouperSession(), scriptFileOrString)
}

def stemSave2 (Stem stem, String uuid, String name, String displayExtension, String description, String saveMode) {
  new stemSave2().invoke(GrouperSession.staticGrouperSession(), stem, uuid, name, displayExtension, description, saveMode)
}

def transactionCommit (String grouperCommitTypeString) {
  new transactionCommit().invoke(GrouperSession.staticGrouperSession(), grouperCommitTypeString)
}

def transactionRollback (String grouperRollbackTypeString) {
  new transactionRollback().invoke(GrouperSession.staticGrouperSession(), grouperRollbackTypeString)
}

def transactionStart (String grouperTransactionTypeString) {
  new transactionStart().invoke(GrouperSession.staticGrouperSession(), grouperTransactionTypeString)
}

def transactionEnd () {
  new transactionEnd().invoke(GrouperSession.staticGrouperSession())
}

def transactionStatus () {
  new transactionStatus().invoke(GrouperSession.staticGrouperSession())
}

def typeAdd (String name) {
  new typeAdd().invoke(GrouperSession.staticGrouperSession(), name)
}

def typeDel (String name) {
  new typeDel().invoke(GrouperSession.staticGrouperSession(), name)
}

def typeFind (String name) {
  new typeFind().invoke(GrouperSession.staticGrouperSession(), name)
}

def typeGetFields (String name) {
  new typeGetFields().invoke(GrouperSession.staticGrouperSession(), name)
}

def typeAddAttr (String type, String name) {
  new typeAddAttr().invoke(GrouperSession.staticGrouperSession(), type, name)
}

def typeDelField (String type, String name) {
  new typeDelField().invoke(GrouperSession.staticGrouperSession(), type, name)
}

def typeAddList (String type, String name, Privilege read, Privilege write) {
  new typeAddList().invoke(GrouperSession.staticGrouperSession(), type, name, read, write)
}

def usdu (int options=0) {
  new usdu().invoke(GrouperSession.staticGrouperSession(), options)
}

def usduByMember (Member member, int options=0) {
  new usduByMember().invoke(GrouperSession.staticGrouperSession(), member, options)
}

def usduBySource (String sourceName, int options=0) {
  new usduBySource().invoke(GrouperSession.staticGrouperSession(), sourceName, options)
}

def version () {
  new version().invoke(GrouperSession.staticGrouperSession())
}

def xmlFromFile (String file) {
  new xmlFromFile().invoke(GrouperSession.staticGrouperSession(), file)
}

def xmlFromString (String xml) {
  new xmlFromString().invoke(GrouperSession.staticGrouperSession(), xml)
}

def xmlFromURL (URL url) {
  new xmlFromURL().invoke(GrouperSession.staticGrouperSession(), url)
}

def xmlToFile (String file) {
  new xmlToFile().invoke(GrouperSession.staticGrouperSession(), file)
}

def xmlToString () {
  new xmlToString().invoke(GrouperSession.staticGrouperSession())
}

def xmlUpdateFromFile (String file) {
  new xmlUpdateFromFile().invoke(GrouperSession.staticGrouperSession(), file)
}

def xmlUpdateFromString (String xml) {
  new xmlUpdateFromString().invoke(GrouperSession.staticGrouperSession(), xml)
}

def xmlUpdateFromURL (URL url) {
  new xmlUpdateFromURL().invoke(GrouperSession.staticGrouperSession(), url)
}

// keeping these imports down here seems to improve load time of this file
import edu.internet2.middleware.grouper.app.loader.ldap.*
import edu.internet2.middleware.grouper.attr.*
import edu.internet2.middleware.grouper.attr.assign.*
import edu.internet2.middleware.grouper.attr.finder.*
import edu.internet2.middleware.grouper.attr.value.*
import edu.internet2.middleware.grouper.audit.*
import edu.internet2.middleware.grouper.client.*
import edu.internet2.middleware.grouper.entity.*
import edu.internet2.middleware.grouper.externalSubjects.*
import edu.internet2.middleware.grouper.group.*
import edu.internet2.middleware.grouper.ldap.*
import edu.internet2.middleware.grouper.app.loader.*
import edu.internet2.middleware.grouper.xml.*
import edu.internet2.middleware.grouper.registry.*
import edu.internet2.middleware.grouper.app.usdu.*
import edu.internet2.middleware.grouper.app.misc.*
import edu.internet2.middleware.grouper.rules.*
import edu.internet2.middleware.grouper.hibernate.*
import edu.internet2.middleware.grouper.permissions.*
import edu.internet2.middleware.grouper.util.*
import edu.internet2.middleware.grouper.xml.export.*
import edu.internet2.middleware.subject.*
import edu.internet2.middleware.subject.provider.*
import edu.internet2.middleware.grouper.userData.*
import edu.internet2.middleware.grouper.messaging.*
import edu.internet2.middleware.grouper.filter.*

:set verbosity INFO
:set interpreterMode true
