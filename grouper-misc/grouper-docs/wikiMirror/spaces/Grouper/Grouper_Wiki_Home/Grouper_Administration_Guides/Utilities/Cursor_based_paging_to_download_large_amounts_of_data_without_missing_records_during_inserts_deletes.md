---
title: "Cursor based paging to download large amounts of data without missing records during inserts/deletes"
space: Grouper
pageId: 28544698
version: 13
lastUpdated: 2026-07-01T05:48:11.564Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544698/Cursor+based+paging+to+download+large+amounts+of+data+without+missing+records+during+inserts+deletes
---

## Introduction

If you are downloading a huge dataset but you need to break it into pages for performance reasons, the legacy UI-style paging can miss some records

The problem: imagine downloading a list of 30k employees in pages of 1000. When you get toward the last few pages, if an employee is removed from the beginning list, then the next page downloaded will skip someone who is an employee. So the result will not be complete and someone will get locked out or deprovisioned. Similarly if an employee is added while paging, you will get a duplicate (this might not have as negative of consequences).

Note, there is no cursor is memory or in the database. But it is called cursor-based paging and is lightweight.

Here is how it works:

1. Sort by something ascending (its better if this is unique (e.g. the uuid), but doesnt need to be (e.g. lastUpdated))
2. Get the first X records (first page)
3. Find the last <sort> field that was returned in the list of X records
4. Pass that value back, and get the second X records, after the last <sort> field. Note, if the sort field is unique then cursorFieldIncludesLastRetrieved is false ( greater than that field). If the sort field is not unique then cursorFieldIncludesLastRetrieved is true (greater than or equal to that field), and you can manually remove duplicates (since the last record value will be returned again).
5. Loop and keep getting the last record's <sort> field value and get the next cursor page

Example

| ID | Name |
| --- | --- |
| a1 | group0 |
| b2 | group6 |
| c3 | group2 |
| d4 | group8 |
| e5 | group1 |

1. Get first page of size 2 sorted by ID
  
  
  
  | ID | Name |
  | --- | --- |
  | a1 | group0 |
  | b2 | group6 |
2. See that the last ID returned is "b2". ID is unique. So get the next page of size 2 sorted by ID where lastCursorField is "b2" and cursorFieldIncludesLastRetrieved is false
  
  
  
  | ID | Name |
  | --- | --- |
  | c3 | group2 |
  | d4 | group8 |
3. See that the last ID returned is "d4". ID is unique. So get the next page of size 2 sorted by ID where lastCursorField is "d4" and cursorFieldIncludesLastRetrieved is false
  
  
  
  | ID | Name |
  | --- | --- |
  | e5 | group1 |
4. See that the number of records is less than the pageSize (2) so that is the last page. All 5 records were returned

## Workaround

To get all record from the legacy paging method, do this (assume page size of 1000) (assume less than 5 records will change while paging)

1. Loop with 5 tries
  
  1. Get the first 1100 records
  2. Loop
    
    1. Get the second page but with pageSize of 1095
    2. If there is no overlap of records, throw out the data and start over in outer loop
    3. If get through where last page has less than pageSize records, then done
    4. Throw out the overlap, and reduce the pageSize so there is some overlap

There is an algorithm in GcGetMembers.java that does this, e.g. (get 70k records with page sizes approx 100k, and overlap of 50

```
Retrieving records: 0 - 1149, pageSize: 1150, pageNumber: 1
Retrieving records: 1099 - 2197, pageSize: 1099, pageNumber: 2
Retrieving records: 2146 - 3218, pageSize: 1073, pageNumber: 3
Retrieving records: 3168 - 4223, pageSize: 1056, pageNumber: 4
Retrieving records: 4172 - 5214, pageSize: 1043, pageNumber: 5
Retrieving records: 5160 - 6191, pageSize: 1032, pageNumber: 6
Retrieving records: 6138 - 7160, pageSize: 1023, pageNumber: 7
Retrieving records: 7105 - 8119, pageSize: 1015, pageNumber: 8
Retrieving records: 8050 - 9199, pageSize: 1150, pageNumber: 8
Retrieving records: 9144 - 10286, pageSize: 1143, pageNumber: 9
Retrieving records: 10233 - 11369, pageSize: 1137, pageNumber: 10
Retrieving records: 11310 - 12440, pageSize: 1131, pageNumber: 11
Retrieving records: 12386 - 13511, pageSize: 1126, pageNumber: 12
Retrieving records: 13452 - 14572, pageSize: 1121, pageNumber: 13
Retrieving records: 14521 - 15637, pageSize: 1117, pageNumber: 14
Retrieving records: 15582 - 16694, pageSize: 1113, pageNumber: 15
Retrieving records: 16635 - 17743, pageSize: 1109, pageNumber: 16
Retrieving records: 17680 - 18784, pageSize: 1105, pageNumber: 17
Retrieving records: 18734 - 19835, pageSize: 1102, pageNumber: 18
Retrieving records: 19782 - 20880, pageSize: 1099, pageNumber: 19
Retrieving records: 20824 - 21919, pageSize: 1096, pageNumber: 20
Retrieving records: 21850 - 22999, pageSize: 1150, pageNumber: 20
Retrieving records: 22940 - 24086, pageSize: 1147, pageNumber: 21
Retrieving records: 24024 - 25167, pageSize: 1144, pageNumber: 22
Retrieving records: 25102 - 26242, pageSize: 1141, pageNumber: 23
Retrieving records: 26174 - 27311, pageSize: 1138, pageNumber: 24
Retrieving records: 27240 - 28374, pageSize: 1135, pageNumber: 25
Retrieving records: 28300 - 29431, pageSize: 1132, pageNumber: 26
Retrieving records: 29380 - 30509, pageSize: 1130, pageNumber: 27
Retrieving records: 30456 - 31583, pageSize: 1128, pageNumber: 28
Retrieving records: 31528 - 32653, pageSize: 1126, pageNumber: 29
Retrieving records: 32596 - 33719, pageSize: 1124, pageNumber: 30
Retrieving records: 33660 - 34781, pageSize: 1122, pageNumber: 31
Retrieving records: 34720 - 35839, pageSize: 1120, pageNumber: 32
Retrieving records: 35776 - 36893, pageSize: 1118, pageNumber: 33
Retrieving records: 36800 - 37949, pageSize: 1150, pageNumber: 33
Retrieving records: 37884 - 39031, pageSize: 1148, pageNumber: 34
Retrieving records: 38964 - 40109, pageSize: 1146, pageNumber: 35
Retrieving records: 40040 - 41183, pageSize: 1144, pageNumber: 36
Retrieving records: 41112 - 42253, pageSize: 1142, pageNumber: 37
Retrieving records: 42180 - 43319, pageSize: 1140, pageNumber: 38
Retrieving records: 43244 - 44381, pageSize: 1138, pageNumber: 39
Retrieving records: 44304 - 45439, pageSize: 1136, pageNumber: 40
Retrieving records: 45387 - 46493, pageSize: 1107, pageNumber: 42
Retrieving records: 46412 - 47543, pageSize: 1132, pageNumber: 42
Retrieving records: 47460 - 48589, pageSize: 1130, pageNumber: 43
Retrieving records: 48532 - 49634, pageSize: 1103, pageNumber: 45
Retrieving records: 49544 - 50669, pageSize: 1126, pageNumber: 45
Retrieving records: 50600 - 51749, pageSize: 1150, pageNumber: 45
Retrieving records: 51660 - 52807, pageSize: 1148, pageNumber: 46
Retrieving records: 52716 - 53861, pageSize: 1146, pageNumber: 47
Retrieving records: 53808 - 54928, pageSize: 1121, pageNumber: 49
Retrieving records: 54864 - 56006, pageSize: 1143, pageNumber: 49
Retrieving records: 55950 - 57068, pageSize: 1119, pageNumber: 51
Retrieving records: 57000 - 58139, pageSize: 1140, pageNumber: 51
Retrieving records: 58089 - 59227, pageSize: 1139, pageNumber: 52
Retrieving records: 59176 - 60313, pageSize: 1138, pageNumber: 53
Retrieving records: 60261 - 61397, pageSize: 1137, pageNumber: 54
Retrieving records: 61344 - 62479, pageSize: 1136, pageNumber: 55
Retrieving records: 62425 - 63559, pageSize: 1135, pageNumber: 56
Retrieving records: 63504 - 64637, pageSize: 1134, pageNumber: 57
Retrieving records: 64581 - 65713, pageSize: 1133, pageNumber: 58
Retrieving records: 65656 - 66787, pageSize: 1132, pageNumber: 59
Retrieving records: 66729 - 67859, pageSize: 1131, pageNumber: 60
Retrieving records: 67791 - 68939, pageSize: 1149, pageNumber: 60
Retrieving records: 68880 - 70027, pageSize: 1148, pageNumber: 61

```

## Notes

You need to sequentially cycle through records and you cannot get multiple pages at once, since you dont know the ID to start after

## Development example

```
    Set<Group> groupsPaged = new HashSet<Group>();

    String lastId = null;
    int pageSize = 7;
    while(true) {
      
      QueryOptions queryOptions = new QueryOptions().sortAsc("id").pagingCursor(pageSize, lastId, false, false);
      
      List<Group> groups = new ArrayList<Group>(
          new GroupFinder().assignParentStemId(testStem.getId()).assignStemScope(Scope.SUB).assignQueryOptions(queryOptions).findGroups());
      
      groupsPaged.addAll(groups);
      if (groups.size() < pageSize) {
        break;
      }
      // get the last one and get records after that one
      lastId = groups.get(groups.size()-1).getId();
    }

```

## Web services changes

1. Anywhere (any object or method) where there is "pageSize" and "pageNumber"
2. Add: pageIsCursor (String) T|F default to F. if this is T then we are doing cursor paging
3. Add: pageLastCursorField (String).
4. Add: pageLastCursorFieldType: (String): could be: string, int, long, date, timestamp
5. Add: pageCursorFieldIncludesLastRetrieved (String) T|F
6. Note: the API changes are done, the only changes that should be needed are in the WS project

Convert the cursor field to whatever type it should be

```
Object lastCursorField = null;
if (pageLastCursorField != null) {
  if (StringUtils.equals(pageLastCursorFieldType, "string")) {
    lastCursorField = pageLastCursorField;
  } else if (StringUtils.equals(pageLastCursorFieldType, "int")) {
    lastCursorField = GrouperUtil.intValue(pageLastCursorField);
  } else if (StringUtils.equals(pageLastCursorFieldType, "long")) {
    lastCursorField = GrouperUtil.longValue(pageLastCursorField);
  } else if (StringUtils.equals(pageLastCursorFieldType, "date")) {
    lastCursorField = GrouperUtil.dateValue(pageLastCursorField);
  } else if (StringUtils.equals(pageLastCursorFieldType, "timestamp")) {
    lastCursorField = GrouperUtil.stringToTimestamp(pageLastCursorField);
  } else {
    throw new RuntimeException("pageLastCursorFieldType not valid should be string|int|long|date|timestamp");
  }
}
```

Example of changing a WS method

```
FROM

      
        QueryOptions queryOptions = null;
        if (pageSize != null || pageNumber != null || !StringUtils.isBlank(sortString) || ascending != null) {
          queryOptions = new QueryOptions();
          boolean hasPaging = false;
          if (pageNumber != null || pageSize != null) {
            //default page number to 1
            if (pageNumber == null) {
              pageNumber = 1;
            }
            queryOptions.paging(pageSize, pageNumber, false);
            hasPaging = true;
          }
          

TO

        QueryOptions queryOptions = null;
        if (pageSize != null || pageNumber != null || !StringUtils.isBlank(sortString) || ascending != null) {
          queryOptions = new QueryOptions();
          boolean hasPaging = false;
          
          if (pageIsCursor) {
            queryOptions.pagingCursor(pageSize, lastCursorField, cursorFieldIncludesLastRetrieved, false);
            hasPaging = true;
          } else {
            if (pageNumber != null || pageSize != null) {
              //default page number to 1
              if (pageNumber == null) {
                pageNumber = 1;
              }
              queryOptions.paging(pageSize, pageNumber, false);
              hasPaging = true;
            }
          }
          

```
