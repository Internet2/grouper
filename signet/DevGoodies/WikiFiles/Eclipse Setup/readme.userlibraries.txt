
The file "signet.userlibraries" will help you configure your own Eclipse
environment for Signet development. By importing it's contents, you can
create several User Libraries that make the job of managing 3rd-party and
Signet-related projects' JAR files easier.

Signet requires many 3rd-party libraries at both build-time and run-time.
This procedure only addresses the build-time needs when building Signet
within the Eclipse IDE.

Signet has dependencies on two related projects: Subject and i2mi-common.
Subject project creates a single JAR that contains classes and interfaces
used by Signet. Whereas, i2mi-common is only a collection of 3rd-party JAR
files shared among the Signet, Grouper, and Subject projects.

Follow these steps:

1. Check-out projects Signet, Subject, and i2mi-common. Personally, I prefer
to keep all development under a single root directory called Projects. So,
for example, you might create /Projects/Signet, /Projects/i2mi-commom, and
/Projects/Subject.

2. Edit /Projects/Signet/DevGoodies/signet.userlibraries and change the paths
to correspond to wherever you created your projects.

3. Edit Eclipse's preferences (in Eclipse 3.2, it's Windows | Preferences |
Java | Build Path | User Libraries). Select the Import button and choose 
your (modified) copy of signet.userlibraries. This creates 3 global variables
within Eclipse's environment that can be used to treat a collection of JAR
files as a single entity.

4. Open the Project Properties dialog for project Signet (right-click the
project in Eclipse's Package Explorer and select Properties).

5. Select Java Build Path. Then select the Libraries tab. Select the Add
Library button.

6. Select User Libraries from the list. Select the Next button.

7. Place a check next to each of the following: i2mi-common_libs,
signet_3rdParty_libs, and subjectAPI_libs. Select Finish.

That's it! You should now be able to compile Signet inside the Eclipse
environment.






