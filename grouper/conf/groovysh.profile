:set verbosity QUIET

import edu.internet2.middleware.grouper.*
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
import edu.internet2.middleware.grouper.app.gsh.*
import edu.internet2.middleware.grouper.app.misc.*
import edu.internet2.middleware.grouper.privs.*
import edu.internet2.middleware.grouper.rules.*
import edu.internet2.middleware.grouper.misc.*
import edu.internet2.middleware.grouper.hibernate.*
import edu.internet2.middleware.grouper.permissions.*
import edu.internet2.middleware.grouper.util.*
import edu.internet2.middleware.grouper.xml.export.*
import edu.internet2.middleware.subject.*
import edu.internet2.middleware.subject.provider.*
import edu.internet2.middleware.grouper.userData.*
import edu.internet2.middleware.grouper.messaging.*
import edu.internet2.middleware.grouper.filter.*
import edu.internet2.middleware.grouper.app.gsh.obliterateStem as ObliterateStem

GrouperSession.startRootSession()

obliterateStem = { name, testOnly, deleteFromPointInTime ->
  ObliterateStem.invoke(GrouperSession.staticGrouperSession(), name, testOnly, deleteFromPointInTime)
}

:set verbosity INFO
