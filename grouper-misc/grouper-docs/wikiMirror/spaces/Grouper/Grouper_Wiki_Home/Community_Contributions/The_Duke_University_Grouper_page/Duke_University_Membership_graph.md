---
title: "Duke University - Membership graph"
space: Grouper
pageId: 28545296
version: 3
lastUpdated: 2026-07-19T00:32:29.830Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28545296/Duke+University+-+Membership+graph
---

## Membership graph

Duke's custom UI allows you to graph the membership history of a group. You can specify the interval and the number of occurrences to graph. And you can compare it with a second group. Here is an example of one. A Jira has been created to add something similar to the Grouper UI - [GRP-2279](https://grouper.atlassian.net/browse/GRP-2279)

Here is the code to query the membership count at a given point in time.

```
  /**
   * @param pitGroupId
   * @param date
   * @return count of unique subject ids in group at the specified time
   */
  public static int getPITMembershipCount(final String pitGroupId, final Date date) {    

    return (Integer)HibernateSession.callbackHibernateSession(GrouperTransactionType.READONLY_OR_USE_EXISTING,
        AuditControl.WILL_NOT_AUDIT, new HibernateHandler() {

      public Object callback(HibernateHandlerBean hibernateHandlerBean) throws GrouperDAOException {
        HibernateSession hibernateSession = hibernateHandlerBean.getHibernateSession();

        String timestamp = "" + date.getTime() + "000";
        Connection connection = ((SessionImpl)hibernateSession.getSession()).connection();
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
          String sql = 
              "select count(distinct pm.subject_id) from grouper_pit_memberships_all_v pma, grouper_pit_fields pf, grouper_pit_members pm " +
              " where pma.field_id = pf.id and pma.member_id = pm.id " + 
              " and NOT pm.subject_source='g:gsa' and pf.name='members' " +
              " and pma.owner_group_id = ? " + 
              " and pma.group_set_start_time < ? and pma.membership_start_time < ?" + 
              " and (pma.group_set_end_time is null or pma.group_set_end_time > ?)" + 
              " and (pma.membership_end_time is null or pma.membership_end_time > ?)" + 
              " and (pma.membership_end_time is null or pma.membership_end_time > pma.group_set_start_time)" + 
              " and (pma.group_set_end_time is null or pma.group_set_end_time > pma.membership_start_time)";
          
          ps = connection.prepareStatement(sql);
          ps.setString(1, pitGroupId);
          ps.setString(2, timestamp);
          ps.setString(3, timestamp);
          ps.setString(4, timestamp);
          ps.setString(5, timestamp);
          rs = ps.executeQuery();
          rs.next();
          return rs.getInt(1);
        } catch (SQLException e) {
          throw new RuntimeException(e);
        } finally {
          if (rs != null) {
            try {
              rs.close();
            } catch (SQLException e) {
              // ignore
            }
          }
          
          if (ps != null) {
            try {
              ps.close();
            } catch (SQLException e) {
              // ignore
            }
          }
        }
      }
    });
  }
```
