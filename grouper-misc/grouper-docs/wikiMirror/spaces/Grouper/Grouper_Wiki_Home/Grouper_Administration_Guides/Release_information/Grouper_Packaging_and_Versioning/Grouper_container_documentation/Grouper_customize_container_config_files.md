---
title: "Grouper customize container config files"
space: Grouper
pageId: 28554586
version: 22
lastUpdated: 2026-07-01T05:40:06.942Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28554586/Grouper+customize+container+config+files
---

Customizing the [container](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549678/Grouper+container+documentation) can be done in multiple ways

If you make changes to config files (e.g. apache or tomcat etc), then after making changes or on upgrade you should look at the startup logs for Grouper and make sure there are no errors when Grouper attempts to patch files or set them up.

## Set env vars

If there are common things to customize, the container should provide an env var to set this (or if not possible, then a container ARG for subimage).

For example you can set the max memory with this arg: -e GROUPER_MAX_MEMORY='3g'

If there is something you would like to change in a config file, and it is common, and there is no env var, please notify the Grouper team and we can discuss adding a variable for it. This is the best option for customizing files.

If you want a grouper properties config file to have an env var, you can have any property be an env var... just configure it in a config file as (for property a.b.c):

```
a.b.c.elConfig = ${elUtils.processEnvVarOrFile('SOME_ENV_VAR')}
```

If the env var is the configured name with _FILE on the end, then it will look at the value which is a file path, e.g. SOME_ENV_VAR_FILE=/some/file/path.txt, and look in that file and use the contents as the value. This can be helpful for secrets.

## Patch a file (v2.5.28+)

This is a good method to adjusting files since the patch will adjust files and check to make sure they havent changed.

1. Go in the container, and make a copy of a file, destination is not important
  
  
  ```
  docker exec -it container-name /bin/bash
  cd /opt/tomee/conf
  cp server.xml server.xml.nologging
  ```
2. Edit the file as needed. Note, you should not change the number of lines in the file. So if you are adding lines, try to replace existing lines. This way other patches can be applied and not mess up
  
  
  ```
  vi server.xml.nologging
  ```
3. Create the patch in container bash
  
  
  ```
  [root@d8dd1b7e30c6 conf]# diff -u server.xml server.xml.nologging > server.xml.nologging.patch
  [root@d8dd1b7e30c6 conf]# more server.xml.nologging.patch 
  --- server.xml	2020-05-13 19:09:12.000000000 +0000
  +++ server.xml.nologging	2020-05-17 15:36:47.361138000 +0000
  @@ -159,10 +159,6 @@
           <!-- Access log processes all example.
                Documentation at: /docs/config/valve.html
                Note: The pattern used is equivalent to using pattern="common" -->
  -        <Valve className="org.apache.catalina.valves.AccessLogValve" directory="logs"
  -               prefix="localhost_access_log" suffix=".txt"
  -               pattern="%h %l %u %t "%r" %s %b" />
  -
         </Host>
       </Engine>
     </Service>
  
  
  ```
4. Test apply patch in container bash
  
  
  ```
  patch server.xml server.xml.nologging.patch 
  
  # and check contents
  cat server.xml 
  
  ```
5. Copy files out of container (exit from container bash)
  
  
  ```
  docker cp grouper-ui:/opt/tomee/conf/server.xml.nologging .
  docker cp grouper-ui:/opt/tomee/conf/server.xml.nologging.patch .
  ```
6. Put those files in your subimage, and patch from Grouper startup hook. If you are editing a file like server.xml that Grouper will patch on startup, then you cannot replace or patch this file before Grouper patches it or the patch will not work and startup will fail.  
    
  Overlay this file in the container: /usr/local/bin/grouperScriptHooks.sh (change "pennContainer" to something specific to your institution)  
    
  Note, we want to have verbose output here so when the container runs we can verify that everything is happening according to plan. Each command and output is logged. If you want to exit if a patch is not applied, that would be a good idea.
  
  
  ```
  #!/bin/sh
    
  # called after the setupFiles functions is called, almost before the process starts
  grouperScriptHooks_setupFilesPost() {
  
    echo "pennContainer; INFO: (grouperScriptHooks.sh-grouperScriptHooks_setupFilesPost) starting..."
  
    cp -v /opt/tomee/conf/server.xml.nologging /opt/tomee/conf/server.xml.nologging.origGrouper
    echo "pennContainer; INFO: (grouperScriptHooks.sh-grouperScriptHooks_setupFilesPost) cp -v /opt/tomee/conf/server.xml.nologging /opt/tomee/conf/server.xml.nologging.origGrouper, result=$?"
  
    patch /opt/tomee/conf/server.xml /opt/tomee/conf/server.xml.nologging.patch 
    echo "pennContainer; INFO: (grouperScriptHooks.sh-grouperScriptHooks_setupFilesPost) patch /opt/tomee/conf/server.xml /opt/tomee/conf/server.xml.nologging.patch, result=$?"
    
  }
  
  export -f grouperScriptHooks_setupFilesPost
  
  echo "pennContainer; INFO: (grouperScriptHooks.sh-body) export -f grouperScriptHooks_setupFilesPost, result=$?"
   
  ```
7. Rebuild container and test your container to see if the patch worked

## Overlay (replace) a file via a custom shell script hook (v2.5.28+)

Grouper provides an empty shell script with some pre-defined functions. /usr/local/bin/grouperScriptHooks.sh

Original base file in container (grouperScriptHooksBase.sh)

If you have multiple tasks to do (patch a file, copy a few files, etc), just put them all in this function

After Grouper is done processing files in the container, you can overlay them if you like. You should have your file in the container or mounted, make a copy of the grouper file, and copy your file on top of the Grouper file. All of this should be logged. (change "pennContainer" to something specific to your institution)

Note that the extension of this file is .penn, that not a valid extension read by the service in a folder. For example, if you have an apache config file, name it: /etc/httpd/conf.d/grouper-www.conf.penn, and not /etc/httpd/conf.d/grouper-www.penn.conf since all *.conf files are read automatically from that directory.

```
#!/bin/sh
  
# called after the setupFiles functions is called, almost before the process starts
grouperScriptHooks_setupFilesPost() {

  echo "pennContainer; INFO: (grouperScriptHooks.sh-grouperScriptHooks_setupFilesPost) starting..."

  cp -v /opt/tomee/conf/server.xml.nologging /opt/tomee/conf/server.xml.nologging.origGrouper
  echo "pennContainer; INFO: (grouperScriptHooks.sh-grouperScriptHooks_setupFilesPost) cp -v /opt/tomee/conf/server.xml.nologging /opt/tomee/conf/server.xml.nologging.origGrouper, result=$?"

  cp -v /opt/tomee/conf/server.xml.penn /opt/tomee/conf/server.xml
  echo "pennContainer; INFO: (grouperScriptHooks.sh-grouperScriptHooks_setupFilesPost) cp -v /opt/tomee/conf/server.xml.penn /opt/tomee/conf/server.xml , result=$?"
  
}

export -f grouperScriptHooks_setupFilesPost

echo "pennContainer; INFO: (grouperScriptHooks.sh-body) export -f grouperScriptHooks_setupFilesPost, result=$?"

```

Overlay /usr/local/bin/grouperScriptHooks.sh in subimage or mount

Functions in here will be called at various points in the Grouper container startup workflow. The main function is grouperScriptHooks_setupFilesPost which can adjust config files after Grouper is done with them but before processes start. Note you still need to pay attention on upgrades but in general this should be a pretty stable way to adjust config files or run commands...

## Penn example of an overlay hook file

/usr/local/bin/grouperScriptHooks.sh (baked in image from slashRoot)

```
#!/bin/sh
  
# called after the setupFiles functions is called, almost before the process starts
grouperScriptHooks_setupFilesPost() {

  echo "pennContainer; INFO: (grouperScriptHooks.sh-grouperScriptHooks_setupFilesPost) starting..."

  # Penn runs in AWS ECS and we want sshd to run, so we append that to supervisord.conf, make a copy of existing, then append
  cp -v /opt/tier-support/supervisord.conf /opt/tier-support/supervisord.conf.origGrouper
  echo "pennContainer; INFO: (grouperScriptHooks.sh-grouperScriptHooks_setupFilesPost) cp /opt/tier-support/supervisord.conf /opt/tier-support/supervisord.conf.origGrouper, result=$?"

  cat /opt/tier-support/supervisord_penn.conf >> /opt/tier-support/supervisord.conf
  echo "pennContainer; INFO: (grouperScriptHooks.sh-grouperScriptHooks_setupFilesPost) cat /opt/tier-support/supervisord_penn.conf >> /opt/tier-support/supervisord.conf, result=$?"

  # This talks to secrets manager and sets some env vars for passwords and endpoints and stuff
  source /src/script/creds.sh
  echo "pennContainer; INFO: (grouperScriptHooks.sh-grouperScriptHooks_setupFilesPost) /src/script/creds.sh , result=$?"

  source /etc/profile.d/awsvars.sh
  echo "pennContainer; INFO: (grouperScriptHooks.sh-grouperScriptHooks_setupFilesPost) /src/script/awsvars.sh , result=$?"
  echo "pennContainer; INFO: (grouperScriptHooks.sh-grouperScriptHooks_setupFilesPost) $ DB_HOST=$DB_HOST, $ SHIBBVAR=$SHIBBVAR"

  # Probably not a good security design, but this is a good hook example, get the root password for the container from secrets manager and assign to user for ssh
  if [[ -z "${PASS}" ]]; then
    echo "pennContainer; ERROR: (grouperScriptHooks.sh-grouperScriptHooks_setupFilesPost) $ PASS is not set!"
  else
    echo "root:${PASS}" | chpasswd
    echo "pennContainer; INFO: (grouperScriptHooks.sh-grouperScriptHooks_setupFilesPost) echo root:$ { PASS } s | chpasswd , result=$?"
  fi

  # We edit some logging directives and other things in the httpd.conf file
  cp -v /etc/httpd/conf/httpd.conf /etc/httpd/conf/httpd.conf.origGrouper
  echo "pennContainer; INFO: (grouperScriptHooks.sh-grouperScriptHooks_setupFilesPost) cp -v /etc/httpd/conf/httpd.conf /etc/httpd/conf/httpd.conf.origGrouper , result=$?"

  cp -v /etc/httpd/conf/httpd.conf.penn /etc/httpd/conf/httpd.conf
  echo "pennContainer; INFO: (grouperScriptHooks.sh-grouperScriptHooks_setupFilesPost) cp -v /etc/httpd/conf/httpd.conf.penn /etc/httpd/conf/httpd.conf , result=$?"

  # Change the shib config to have some variables in there  
  mv -f /etc/shibboleth/shibboleth2.xml /etc/shibboleth/shibboleth2.xml.grouperOrig
  echo "pennContainer; INFO: (grouperScriptHooks.sh-grouperScriptHooks_setupFilesPost) mv -f /etc/shibboleth/shibboleth2.xml /etc/shibboleth/shibboleth2.xml.grouperOrig , result=$?"
  
  cp /etc/shibboleth/shibboleth2.xml.penn /etc/shibboleth/shibboleth2.xml
  echo "pennContainer; INFO: (grouperScriptHooks.sh-grouperScriptHooks_setupFilesPost) mv -f /etc/shibboleth/shibboleth2.xml.penn /etc/shibboleth/shibboleth2.xml , result=$?"

  #Replace entityID with parameter from AWS secrets manager
  if [[ -z "${SHIBBVAR}" ]]; then
    echo "pennContainer; ERROR: (grouperScriptHooks.sh-grouperScriptHooks_setupFilesPost) $ SHIBBVAR is not set!"
  else
    # dont blank this out if the var isnt there
    sed -i "s|replace_me_entitiyID|$SHIBBVAR|g" /etc/shibboleth/shibboleth2.xml
    echo "pennContainer; INFO: (grouperScriptHooks.sh-grouperScriptHooks_setupFilesPost) sed -i ''s|replace_me_entitiyID|$SHIBBVAR|g'' /etc/shibboleth/shibboleth2.xml , result=$?"
  fi

  # copy the attribute map for shib to be penn specific  
  mv -f /etc/shibboleth/attribute-map.xml /etc/shibboleth/attribute-map.xml.grouperOrig
  echo "pennContainer; INFO: (grouperScriptHooks.sh-grouperScriptHooks_setupFilesPost) mv -f /etc/shibboleth/attribute-map.xml /etc/shibboleth/attribute-map.xml.grouperOrig , result=$?"
  
  cp /etc/shibboleth/attribute-map.xml.penn /etc/shibboleth/attribute-map.xml
  echo "pennContainer; INFO: (grouperScriptHooks.sh-grouperScriptHooks_setupFilesPost) cp /etc/shibboleth/attribute-map.xml.penn /etc/shibboleth/attribute-map.xml , result=$?"
  
}

export -f grouperScriptHooks_setupFilesPost

echo "pennContainer; INFO: (grouperScriptHooks.sh-body) export -f grouperScriptHooks_setupFilesPost, result=$?"
 
```

## Overlay files

You can overlay files in a subimage or by mounting them into the image. This works great for new files or to remove files. This is risky to replace existing config files since if Grouper changes the file in a subsequent container, and you do not incorporate those changes in your overlay, then the configs will diverge and bad things can happen. You should keep a copy of the original file and when you upgrade you should check to see if Grouper's file has changed, and incorporate those changes. e.g. the grouper-[www.conf](http://www.conf) file.

In v2.5.36+ you can [overlay any file and Grouper will not try to patch or replace](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28560318/Grouper+v2.5+container+keep+track+of+file+overlays) if it this is the case.

## Adjust files in subimage

This works great for new files or to remove files. For existing files, in your dockerfile you could RUN a command to sed a file or patch a file. This is a good approach but still needs to be checked on upgrades and cannot be used on files that are generated or massaged by the Grouper startup. As we add more env vars then more files will be adjusted by Grouper. You can use a script hook in that case.

## Change the docker entrypoint

If you want to change the docker entrypoint, you need to pay careful attention to how Grouper uses the entrypoint script and how things evolve in the Grouper container on upgrades. The GTE (Grouper Training Environment) does this, but this is not a recommended approach since Grouper makes certain assumptions in its entrypoint. This is advanced and should be documented well internally so others in your institution can support your customizations.
