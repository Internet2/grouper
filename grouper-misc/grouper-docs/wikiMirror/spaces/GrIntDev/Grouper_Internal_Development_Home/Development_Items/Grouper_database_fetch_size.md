---
title: "Grouper database fetch size"
space: GrIntDev
pageId: 48792725
version: 11
lastUpdated: 2026-07-12T07:01:14.130Z
url: https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48792725/Grouper+database+fetch+size
---

Fetch size is how many rows are delivered in a "chunk" when selecting data.

Grouper is setting fetch size 1000 in Grouper in 2.5.44+. [GRP-3167](https://todos.internet2.edu/browse/GRP-3167).

This does not need to be configurable since Grouper gets all data in queries and does not stop from cursors. Even if it did occasionally the penalty of getting 1000 rows for a few hundred millis is not worth worrying about. i.e. it should not be changed.

Oracle default fetch size is 10. This means a lot of round trips when getting lots of data (if getting 1000 rows, its 100 chunks). Matters most when there is network latency or bandwidth issues.

Postgres default fetch size is all.

Mysql (not tested here as much) seems to also not have a default fetch size.

There are two aspects to this in Grouper: hibernate, JDBC.

Note these numbers are done where database is not near app server (not a recommended configuration) so the performance expectations are exaggerated here. Note, this is just one run to get order of magnitude, so there is uncertainty on close numbers, so consider those the same (e.g. 7000ms is similar to 9000ms)

| Database | Hibernate? | Fetch size | Query | Time in mills |
| --- | --- | --- | --- | --- |
| Postgres | hibernate | default | 100k groups | 8293 |
| Oracle | hibernate | default | 100k groups | 109451 |
| Postgres | hibernate | default | 100k groups | 7180 |
| Oracle | hibernate | 1000 | 100k groups | 9529 |
| Postgres | jdbc | default | 100k groups | 4161 |
| Oracle | jdbc | default | 100k groups | 111855 |
| Postgres | jdbc | 1000 | 100k groups | 3606 |
| Oracle | jdbc | 1000 | 100k groups | 5878 |
| Mysql | jdbc | default | 10k mships | 7097 |
| Mysql | jdbc | 1000 | 10k mships | 7760 |

code

These are just internal notes in case we need to do tests again... ignore

main

```
    jdbcExample(1);
    
    long start = System.currentTimeMillis();

    jdbcExample(100000);

    System.out.println("Took millis: " + (System.currentTimeMillis()-start));

    if (factory != null) {
      factory.close();
    }

```

JDBC code

```
  public static void jdbcExample(int count) {
    
    GrouperStartup.startup();
    
    //     List list = new GcDbAccess().sql("select * from grouper_groups where rownum <= " + count).selectList(Object[].class);
    List list = new GcDbAccess().connectionName("pennCommunity").sql("select * from grouper_groups limit " + count).selectList(Object[].class);
    
    System.out.println("found " + list.size());
    
    
  }

```

Hibernate code

```
    // prime the pump
    HibernateSession.byHqlStatic()
      .createQuery("from Group")
      .options(QueryOptions.create(null, null, 1, 1)).listSet(Group.class);
    
    long start = System.currentTimeMillis();
    Set<Group> groups = HibernateSession.byHqlStatic()
      .createQuery("from Group")
      .options(QueryOptions.create(null, null, 1, 100000)).listSet(Group.class);
    
    System.out.println("Rows: " + groups.size() + ", took millis: " + (System.currentTimeMillis()-start));

```

Raw hibernate

```
{
    SessionFactory factory = new Configuration().addProperties(GrouperUtil.propertiesFromResourceName("grouper.hibernate.properties"))
        .addResource("edu/internet2/middleware/grouper/internal/dao/hib3/Hib3GroupDAO.hbm.xml")
        .buildSessionFactory(); 

    hibernateExample(factory, 1);
    
    long start = System.currentTimeMillis();

    hibernateExample(factory, 100000);

    System.out.println("Rows: 100000, took millis: " + (System.currentTimeMillis()-start));

    factory.close();
//    Set<Group> groups = HibernateSession.byHqlStatic()
//        .createQuery("from Group")
//        .options(QueryOptions.create(null, null, 1, 100000)).listSet(Group.class);

  }

  public static void hibernateExample(SessionFactory factory, int count) {
       
    Session session = factory.openSession();
    
    List list = session.createQuery("FROM Group").setMaxResults(count).list();
    System.out.println("found " + list.size());
    session.close();
    
  }
  

```
