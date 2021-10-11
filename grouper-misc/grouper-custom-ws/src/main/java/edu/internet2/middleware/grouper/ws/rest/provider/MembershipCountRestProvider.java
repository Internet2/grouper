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
        // TODO: allow filters
        String effectiveQuery = "select count(distinct gm.SUBJECT_ID)" +
                "from grouper_memberships_all_v gms," +
                "     grouper_members gm," +
                "     grouper_groups gg," +
                "     grouper_fields gfl" +
                " where gg.name = ?" +
                "  and gms.OWNER_GROUP_ID = gg.id" +
                "  and gms.FIELD_ID = gfl.ID" +
                "  and gms.MEMBER_ID = gm.ID" +
                "  and gms.IMMEDIATE_MSHIP_ENABLED = 'T'" +
                "  and gfl.TYPE = 'list' and gfl.NAME='members' and gm.SUBJECT_SOURCE <> 'g:gsa'";
        int effectiveCount = new GcDbAccess().sql(effectiveQuery).addBindVar(o.getUrlStrings().get(2)).selectList(Integer.class).get(0);


        String immediateQuery = effectiveQuery + " and gms.mship_type = 'immediate'";
        int immediateCount = new GcDbAccess().sql(immediateQuery).addBindVar(o.getUrlStrings().get(2)).selectList(Integer.class).get(0);

        return new MembershipCountRestProviderResponse(immediateCount, effectiveCount);
    }
}
