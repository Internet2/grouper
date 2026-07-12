---
title: "Grouper ABAC developer notes"
space: GrIntDev
pageId: 48793105
version: 3
lastUpdated: 2026-07-12T06:46:06.257Z
url: https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48793105/Grouper+ABAC+developer+notes
---

## TO DO

- Allow common reference groups to have user friendly labels
  
  
  
  - ```
    e.g. employee instead of ref:employee
    ```
  - Allow plurals of labels? and absorb plurals. e.g. "member of employee" same as "member of employees
  - Same for case differences
  - Absorb smart quotes
  - Give a warning (validation) and suggestion when parens are needed
- alow group friendly names, uuid, idIndex, case issues, etc to be used but converted into the id-path
- Document common allowed reference groups and syntax on the script editing page
- Allow natural language to be converted into JEXL
  
  - e.g.
    
    
    ```
    FROM
    Member of employee, and member of app:jiraAdminsManualTO
    ${entity.memberOf(‘ref:employee’) && entity.memberOf(‘app:jiraAdminsManual’)}
    ```
  - e.g.
    
    
    ```
    FROM
    Has affiliation attribute with name of staff and dept of english, 
       but not a member of lockout
    
    TO
    entity.hasAttribute('affiliation', 'name==staff && dept==english') 
       && entity.notMemberOf('ref:lockout')
    ```
- Load groups as members
- Add incremental job
- Unit tests
- Better validation
- More methods to call (other than hasMember)
- (DONE) Add subject attributes e.g. from global attribute resolver
- Failsafes
- Add delegated admin based on READ of groups
- Visualization
- Add more counts of total memberships etc
- See if comments can be included in scripts
- See if we can replace composite type with immediate in membership table if replacing a composite with jexl script
- Have a confirm screen that tells the user information about what will happen (tie into visualization)? (have a progress page)
- Add UI to configure scripts?
- Test button while writing to see if valid (maybe that does counts too)
- Add dependency graph for precedence or recalcs in full sync
- Do not allow circular references
- Identify full syncs to be hourly or daily
- Allow single quotes or double quotes when parsing scripts
- Add full and incremental for each data provider
- Input in JEXL the expected population size
- Population limits for people who can edit jexl scripts
- Security
  
  - Users need to be in the "abac allowed" group
  - Users need to have READ/UPDATE on the group
  - Users need ability to run template (if applicable)
  - Users need READ on factor groups/attributes

## Entity data fields

Setup your entity data fields and use that data in JEXL scripts. This data can come from LDAP or SQL

Gail Lift: Michigan is starting to see situations where this approach would be REALLY useful. We are also interested in near real time / changelog.

We have struggled to build widely usable reference groups, because each unit has their own special needs: "Regular staff, but only in these jobcodes" and "Regular staff, but only in those jobcodes". Because a person can have multiple jobs, or multiple student programs, simple group math is not enough. If Mary has 2 jobs, {"dept":"English","jobcode":"12345"} and {"dept":"History","jobcode":"67890"}, intersecting a dept ref group with a jobcode ref group will not work as desired.  
   
We are hoping the the new approach with Entity Attribute Resolver Groups will help with these needs. But we are concerned about how to get all the needed data into the my_people_affiliation table. (you could feed from arbitrary LDAP/SQL/WS sources)

We have affiliation data for employees, emeritus, Ann Arbor students, Dearborn students, Flint students, alumni, and Sponsored Affiliates (guests). Class enrollment data will be added later. Each affiliations has its own set of attributes that need to be available for group construction. A couple of typical samples, seen as we store them in LDAP in an almost-JSON format:

umichAAAcadProgram: {acadCareer=GBA}:{acadProg=00018}:{acadPlan=0010MAC}:{campus=A}:{progStatus=AC}:{admitTerm=2410}:{admitTermBegDt=2022-08-29}:{expGradTerm=}:{degrChkoutStat=}:{acadCareerDescr=Graduate Business Admin}:{acadPlanDegree=MAC}:{acadPlanDescr=Accounting MAcc}:{acadPlanField=0010}:{acadPlanFieldDescr=}:{acadPlanType=MAJ}:{acadPlanTypeDescr=Major}:{acadGroup=BA}:{acadGroupDescr=Ross School of Business}:{acadProgDescr=Accounting MAcc}  
  
umichHR: {jobCategory=Faculty}:{campus=UM_ANN-ARBOR}:{deptId=304000}:{deptGroup=MEDICAL_SCHOOL}:{deptDescription=MM Orthopaedic Surgery}:{deptGroupDescription=Medical School}:{deptVPArea=EXEC_VP_MED_AFF}:{jobcode=201000}:{jobFamily=10}:{emplStatus=A}:{regTemp=R}:{supervisorId=12345678}:{tenureStatus=TEN}:{jobIndicator=P}  
  
For a single my_people_affiliation table, having a column for each distinct keyword would require about 70 columns. In any given row, most columns would have a null value. At the other extreme, we could use a single column for the affiliation data, so the columns would be employee_id, affiliation_name, affiliation_value. At this extreme, most queries would require substring matching. Would a structure between these make sense? Is a separate table for each affiliation better? (Dont worry about a table with all data)

## Parse expression with JEXL (for Grouper developers)

Feed the expression through this simple program

```
  public static void main(String[] args) {
    
    JexlEngine jexlEngine = new JexlEngine();

    ExpressionImpl expression = (ExpressionImpl)jexlEngine.createExpression("group.campus !~ ['palmer', 'southern'] and group.termStart - 7 > sysdate");
    
    ASTJexlScript astJexlScript = (ASTJexlScript)GrouperUtil.fieldValue(expression, "script");
    printNode(astJexlScript, "");
    
    System.out.println(expression);
  }

  public static void printNode(JexlNode jexlNode, String prefix) {
    System.out.println(prefix + jexlNode.getClass().getSimpleName() + (StringUtils.isBlank(jexlNode.image) ? "" : (": " + jexlNode.image)));
    String newPrefix = StringUtils.isBlank(prefix) ? "- " : ("  " + prefix);
    for (int i=0;i<jexlNode.jjtGetNumChildren();i++) {
      printNode(jexlNode.jjtGetChild(i), newPrefix);
    }
  }

```

Output

```
ASTJexlScript
- ASTAndNode
  - ASTNRNode
    - ASTReference
      - ASTIdentifier: group
      - ASTIdentifier: campus
    - ASTReference
      - ASTArrayLiteral
        - ASTReference
          - ASTStringLiteral: palmer
        - ASTReference
          - ASTStringLiteral: southern
  - ASTGTNode
    - ASTAdditiveNode
      - ASTReference
        - ASTIdentifier: group
        - ASTIdentifier: termStart
      - ASTAdditiveOperator: -
      - ASTNumberLiteral: 7
    - ASTReference
      - ASTIdentifier: sysdate
```

Grouper can take that object model and see which group and subject attributes are related, print out a nice analysis of the policy, and know which policies are affected by real time changes

Expression 2: campus is palmer or southern, or the term is current with some overlap

```
group.campus =~ ['palmer', 'southern'] or (group.termStart - 7 > sysdate and group.termStart - 7 < sysdate)
```

```
ASTJexlScript
- ASTOrNode
  - ASTERNode
    - ASTReference
      - ASTIdentifier: group
      - ASTIdentifier: campus
    - ASTReference
      - ASTArrayLiteral
        - ASTReference
          - ASTStringLiteral: palmer
        - ASTReference
          - ASTStringLiteral: southern
  - ASTReference
    - ASTReferenceExpression
      - ASTAndNode
        - ASTGTNode
          - ASTAdditiveNode
            - ASTReference
              - ASTIdentifier: group
              - ASTIdentifier: termStart
            - ASTAdditiveOperator: -
            - ASTNumberLiteral: 7
          - ASTReference
            - ASTIdentifier: sysdate
        - ASTLTNode
          - ASTAdditiveNode
            - ASTReference
              - ASTIdentifier: group
              - ASTIdentifier: termStart
            - ASTAdditiveOperator: -
            - ASTNumberLiteral: 7
          - ASTReference
            - ASTIdentifier: sysdate
```

Expression 3: primaryAffiliation is faculty or staff and dept is physics or math

```
person.primaryAffiliation =~ ['faculty', 'staff'] and person.dept =~ ['physics', 'math']
```

```
ASTJexlScript
- ASTAndNode
  - ASTERNode
    - ASTReference
      - ASTIdentifier: person
      - ASTIdentifier: primaryAffiliation
    - ASTReference
      - ASTArrayLiteral
        - ASTReference
          - ASTStringLiteral: faculty
        - ASTReference
          - ASTStringLiteral: staff
  - ASTERNode
    - ASTReference
      - ASTIdentifier: person
      - ASTIdentifier: dept
    - ASTReference
      - ASTArrayLiteral
        - ASTReference
          - ASTStringLiteral: physics
        - ASTReference
          - ASTStringLiteral: math
```

## Visualization

This is a complicated topic since it is parsing a programming language.

As a first pass we could have the overall group and lines from all component groups with count and the exact policy isnt there. This would apply for any unparsable policies once we have better visualization.

In a future pass, if the script follows certain standards and is "parsable" (allow-deny where parens and multiples could be involved), then I could picture a visualization for that. Note: either the visualization will be slow, or lots of data will be cached, or it will be stale from the last full sync. This is because JEXLs cannot be transformed into queries with counts, it needs the data in memory to allow JEXL to do its thing.

We need to get a list of sample policies people want to use so we can make sure we are going in the right direction.

## Full sync

A nightly full sync will occur. The incremental sync should stop. Make sure all the loaded groups are up to date.

## Incremental sync (future state)

An incremental change log consumer can

- If group attributes change, see if it affects group attributes (future state)
- If attributes change, see which policies those refer to, and incrementally adjust the membership of those groups
- Policy changes should change the population

## Attributes on groups, with groups as members

Allow attributes to be loaded on groups. and scripts could have a slightly different syntax. e.g.

```
group.hasAttribute['campus', 'main'] && group.hasAttribute['course']
```

## Attributes on groups with people as members

Maybe there is an option to control what the result is. Or if it is even possible.

```
(group.hasAttribute['campus', 'main'] && group.hasAttribute['course']) && !entity.hasAttribute['lockout']
```

## Point in time on attributes

Point in time on attributes that support it

```
entity.hasHadAttribute('dept', '234', '2w')
```

or

```
entity.hasOrHadAttribute('dept', '234', '2w')
```

## Point in time on policy

Have a config that allows point in time on the whole policy. i.e. only point in time in past, or include current members. This would be only going forward?

## Policy examples

Allow institution specific policy examples on the policy editor page. Can only see the example if can see certain attributes/groups
