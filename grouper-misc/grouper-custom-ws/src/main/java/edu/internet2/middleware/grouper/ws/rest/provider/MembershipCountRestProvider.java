package edu.internet2.middleware.grouper.ws.rest.provider;

import edu.internet2.middleware.grouper.ws.rest.CustomGrouperRestProvider;
import edu.internet2.middleware.grouper.ws.rest.CustomGrouperRestRequest;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;

public class MembershipCountRestProvider implements CustomGrouperRestProvider {
    @Override
    public boolean supports(CustomGrouperRestRequest o) {
        return "membershipCount".equals(o.getUrlStrings().get(o.getUrlStrings().size() - 1));
    }

    @Override
    public Object provide(CustomGrouperRestRequest o) {
        String effectiveQuery = "select count(*) from grouper_memberships_v where group_name = ? and list_type = 'list' and list_name = 'members'";
        int effectiveCount = new GcDbAccess().sql(effectiveQuery).addBindVar(o.getUrlStrings().get(2)).selectList(Integer.class).get(0);


        String immediateQuery = "select count(*) from grouper_memberships_v where group_name = ? and list_type = 'list' and list_name = 'members' and membership_type = 'immediate'";
        int immediateCount = new GcDbAccess().sql(immediateQuery).addBindVar(o.getUrlStrings().get(2)).selectList(Integer.class).get(0);

        return new MembershipCountRestProviderResponse(immediateCount, effectiveCount);
    }
}
