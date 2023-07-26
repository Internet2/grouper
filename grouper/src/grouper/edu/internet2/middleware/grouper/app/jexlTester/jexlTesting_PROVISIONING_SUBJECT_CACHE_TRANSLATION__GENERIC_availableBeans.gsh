import edu.internet2.middleware.grouper.app.provisioning.*;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncMember;


Subject subject = SubjectFinder.findByIdOrIdentifierAndSource("GrouperSystem", "g:isa", true);

Map<String, Object> elVariableMap = new HashMap<String, Object>();
elVariableMap.put("subject", subject);

