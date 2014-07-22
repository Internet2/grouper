@REM
@REM Copyright 2014 Internet2
@REM
@REM Licensed under the Apache License, Version 2.0 (the "License");
@REM you may not use this file except in compliance with the License.
@REM You may obtain a copy of the License at
@REM
@REM   http://www.apache.org/licenses/LICENSE-2.0
@REM
@REM Unless required by applicable law or agreed to in writing, software
@REM distributed under the License is distributed on an "AS IS" BASIS,
@REM WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
@REM See the License for the specific language governing permissions and
@REM limitations under the License.
@REM

@echo off

rem -----------------------------------------------------------------------------
rem Script for command line util to test sources.xml config file.
rem
rem Environment Variable Prequisites
rem
rem   JAVA_HOME     Must point to your JDK.
rem -----------------------------------------------------------------------------

set JAVA_HOME=\j2sdk1.4.2_05

set CLASSPATH=build
set CLASSPATH=conf;%CLASSPATH%

for %%f in (lib\*.jar) do call cpappend.bat %%f

echo "Using classpath: %CLASSPATH%"

%JAVA_HOME%\bin\java -cp %CLASSPATH% edu.internet2.middleware.subject.provider.SourceManager %1
