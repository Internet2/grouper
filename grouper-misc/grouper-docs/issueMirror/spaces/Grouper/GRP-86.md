---
key: GRP-86
cloud_key: 
onprem_url: https://todos.internet2.edu/browse/GRP-86
type: Improvement
status: Open
resolution: Unresolved
priority: Major
reporter: Chris Hyzer <mchyzer@example.com>
assignee: Chris Hyzer <mchyzer@example.com>
created: 2008-01-24T15:26:25.327+0000
updated: 2008-10-01T16:48:00.116+0000
resolved: 
components: [API, UI]
fixVersions: [HEAD]
labels: []
links: []
---

# GRP-86  improve (and make consistent) the checkout and build process of grouper, grouper-ui, and grouper-ws 

Good suggestions. Please also run them by signet-dev to see if we can produce a more even experience across both. And, please open a jira issue to track what is decided should be done.

Re #2, I think the main installation doc is in the wiki, not in the package itself. That was the result of discussion quite some time back, with (for me) the essential argument being "docs have bugs and updates too" and we don't want to wait on subsequent releases to fix some of those. But perhaps there should be additional or modified instructions in the package too, especially if some type of example file naming convention is followed.

Tom

Chris Hyzer wrote:
> OK, thanks, my UI is up and running.
>
> Its not a huge deal, and I know we are working on a lot of things, but if someone revisits the build process/docs, here are my suggestions:
>
> 1. in Grouper, the build properties file should not be in conf, but 
> rather in the root dir.  It is not needed on the classpath, and should 
> be seen with the build.xml 2. If a file should  be customized, perhaps we could mention in the checkout doc (url from gary).  Maybe its obvious to everyone, but maybe we need to say which files need to be customized.  Granted its in the quickstart doc.  However, per #3 if we use .example. files, it is more obvious also 3. If a file should be customized, lets not commit the final file to CVS, lets commit an example file.  And lets not change the extention (since editors might not open, and eclipse wont know if binary or ascii).  E.g. buildGrouper.example.properties.  Its hard for me to know what I should commit and what I shouldn't...  or what I should update from cvs and what I shouldn't...
> 4. If an option is added to the build (e.g. Tom's test config), maybe we could put a conditional in ant that fails with a helpful error message if the option isn't in the buildGrouper.propties.  I ended up with a directory named ${src.dir.test.conf}, then had to look in the source, compare in cvs, etc to figure it out.
> 5. For the thing in the grouper-ui where if the dist/grouper.jar 
> exists it will do something, if not something else, I think an option 
> in the build.properties true/false and an ant conditional would be a 
> lot more intuitive
>
> I can chip away at these also if we agree they should be done.
>
> Thanks!
> Chris
>
>> -----Original Message-----
>> From: GW Brown, Information Systems and Computing 
>> [mailto:Gary.Brown@example.com]
>> Sent: Wednesday, January 23, 2008 4:44 AM
>> To: Chris Hyzer; Shilen Patel
>> Cc: Tom Barton; Tom Zeller; Kathryn Huxtable
>> Subject: Re: How to checkout and run grouper and grouper-ui
>>
>>
>>
>> --On 23 January 2008 02:15 -0500 Chris Hyzer <mchyzer@example.com>
>> wrote:
>>
>>> Gary (or someone),
>>>
>>> I think I asked you before, and you suggested I call you, but I was 
>>> wondering if you could post (or send link to) the steps to:
>>>
>>> 1. Checkout, build, commit, and run unit tests for grouper
>> I use WinCVS for checkout and commit. After checkout I modify config 
>> files to point at the right database and Subject sources. If starting 
>> from scratch I would do an 'ant dist' and an 'ant db.init'. 'ant 
>> test' will run all the unit tests. I think it took about 5 minutes to 
>> run the last time I tried. The main thing to remember is that almost 
>> all tests will remove your data. I think we should aim to have 
>> configuration for a test database/schema so that it would be safe to 
>> run ant test whenever you want
>> - then there are no excuses for not running the tests.
>>
>>> 2. Checkout, build, commit, and run the grouper-ui (and run unit
>> tests if
>>> applicable) 3. Build a new grouper jar in the ui
>> WinCVS as above. There aren't any unit tests.
>>
>> The UI will check the configured grouper installation for 
>> dist/lib/grouper.jar. If it is there it simply copies that along with 
>> the other JARs, otherwise it calls the grouper 'ant dist' target to 
>> create one.
>>
>> There is a build property 'grouper.compile.api' which, if set, causes 
>> the UI to compile the API classes to WEB-INF/classes. I tend to set 
>> this so that changes I make to the API are available to the UI 
>> without rebuilding the JAR. Also, I have:
>>   debug=true
>>   debug.level=lines,vars,source
>> set so that I can debug.
>>
>> The UI has various targets - which display if you type 'ant help'.
>>> For the web services, I have the eclipse project file in CVS, and a 
>>> README.txt like the quickstart.  Should be able to checkout in
>> eclipse,
>>> edit the appropriate configs, and run.
>>>
>>> I had the quickstart running, but it wasn't convenient to commit to
>> CVS,
>>> so Im just wondering what the best practice is... (checkout a create
>> a
>>> new eclipse project, or use the quickstart and share with cvs (there
>> are
>>> a lot of files that need ignoring so Im assuming you don't work this
>> way,
>>> or you just know which to ignore and don't accidentally commit?)
>> I tend to checkout grouper, grouper-ui and any custom projects into a 
>> top level 'project' folder and load everything into Eclipse as one 
>> big project.
>>
>> I also tend to set the output folder to grouper-ui/webapp/WEB- 
>> INF/classes (or uob-grouper-ui/webapp/WEB-INF/classes). See 
>> <https://wiki.internet2.edu/confluence/download/attachments/1537/envi
>> ro
>> nment.html?version=1>
>>
>>
>>> Thanks!
>>> Chris
>>
>>
>> ----------------------
>> GW Brown, Information Systems and Computing Gary.Brown@example.com


## Comments

### mchyzer - 2008-08-09T04:09:24.425+0000

Some notes:

- keep examples in config files for major db's when specifying drivers (include p6spy)
- maybe allow omitting the hibernate dialect and detect from driver?  (include support for p6spy?)
- include common db drivers in separate directory?