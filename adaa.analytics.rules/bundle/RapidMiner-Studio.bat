@echo off
SETLOCAL ENABLEDELAYEDEXPANSION

rem ##############################################################
rem ###                                                        ###
rem ###      Windows Start Script for RapidMiner Studio        ###
rem ###                                                        ###
rem ###  This script tries to determine the location of        ###
rem ###  RapidMiner Studio, searches for a proper Java         ###
rem ###  executable and starts the GUI version.                ###
rem ###                                                        ###
rem ##############################################################

rem #############################################
rem ###                                       ###
rem ###  Setting or Guessing RAPIDMINER_HOME  ###
rem ###                                       ###
rem #############################################

rem ### remove _JAVA_OPTIONS environment variable for this run ###
rem ### it could contain stuff that break Studio launching so we ignore it completely ###
set _JAVA_OPTIONS=
set QUOTE="

rem ### Check if JAVA_HOME starts with a quote. If so, we need to remove the quotes to avoid later syntax errors
:start0
set jhome=%JAVA_HOME:~0,1%
if !jhome!==!QUOTE! goto fixjavahome
goto start1

:fixjavahome
set JAVA_HOME=%JAVA_HOME:"=%
goto start1

rem ### Check if RAPIDMINER_HOME starts with a quote. If so, we need to remove the quotes to avoid later syntax errors
:start1
set rhome=%RAPIDMINER_HOME:~0,1%
if !rhome! == !QUOTE! goto fixrmhome
goto start2

:fixrmhome
set RAPIDMINER_HOME=%RAPIDMINER_HOME:"=%
goto start2

:start2
if "%RAPIDMINER_HOME%"=="" goto guessrapidminerhome
goto javahome

rem ###  set RAPIDMINER_HOME to the correct directory if you changed the location of this start script  ###
:guessrapidminerhome
for %%? in ("%~dp0.") do set RAPIDMINER_HOME=%%~f?
echo RAPIDMINER_HOME environment variable is not set. Trying the directory '%RAPIDMINER_HOME%'...
goto javahome

rem ############################
rem ###                      ###
rem ###  Searching for Java  ###
rem ###                      ###
rem ############################

:javahome
set LOCAL_JRE_JAVA=%RAPIDMINER_HOME%\jre\bin\java.exe
if exist "%LOCAL_JRE_JAVA%" goto localjre
goto checkjavahome

:localjre
set JAVA=%LOCAL_JRE_JAVA%
echo Using local jre: %JAVA%...
goto commandlinearguments

:checkjavahome
if "%JAVA_HOME%"=="" goto checkpath
set JAVA_CHECK=%JAVA_HOME%\bin\java.exe
if exist "%JAVA_CHECK%" goto globaljre 
goto error3

:globaljre
set JAVA=%JAVA_HOME%\bin\java
echo Using global jre: %JAVA%...
goto commandlinearguments

:checkpath
java -version 2> nul:
if errorlevel 1 goto error2
goto globaljrepath

:globaljrepath
set JAVA=java
echo Using global jre found on path: %JAVA%
goto commandlinearguments

rem #########################################
rem ###                                   ###
rem ###  Handling Command Line Arguments  ###
rem ###                                   ###
rem #########################################

:commandlinearguments
set CMD_LINE_ARGS=%*
goto update

rem ###########################
rem ###                     ###
rem ###  Performing Update  ###
rem ###                     ###
rem ###########################

:update
set RUINSTALL_DIR="%HOMEDRIVE%%HOMEPATH%\.RapidMiner\update\RUinstall"
if exist %RUINSTALL_DIR% goto perform_update
goto start

:perform_update
echo Performing RapidMiner Studio Update ...
xcopy %RUINSTALL_DIR% "%RAPIDMINER_HOME%" /c /s /y /i
rmdir %RUINSTALL_DIR% /s /q
goto start

rem #############################
rem ###                       ###
rem ###  Starting RapidMiner  ###
rem ###                       ###
rem #############################

:start
set CHECK_VERSION_FILE="%APPDATA%\check_rm_java_version"
"%JAVA%" -version 2>&1 | findstr /i "version" >  %CHECK_VERSION_FILE%
for /F "tokens=3" %%g in ('type %CHECK_VERSION_FILE%') do (
    set JAVAVER=%%g
)
set JAVAVER=%JAVAVER:"=%
echo Java Version: %JAVAVER%
del %CHECK_VERSION_FILE%
goto gatherSettings

rem ##################################
rem ##                              ##
rem ##  Generate JVM start options  ##
rem ##                              ##
rem ##################################

:gatherSettings
set JVM_OPTIONS=  -XX:+UseG1GC -XX:G1HeapRegionSize=32m -XX:ParallelGCThreads=4 -XX:InitiatingHeapOccupancyPercent=55 -Xms384m -Xmx12174m -Djava.net.preferIPv4Stack=true -Dsun.java2d.dpiaware=false -Djava.net.useSystemProxies=true
goto launch

rem ##################################
rem ##                              ##
rem ##  Launching RapidMiner        ##
rem ##                              ##
rem ##################################

:launch
echo Launching RapidMiner Studio GUI now...
"%JAVA%" %JVM_OPTIONS% -cp "%RAPIDMINER_HOME%"\lib\*;"%RAPIDMINER_HOME%"\lib\jdbc\* com.rapidminer.gui.RapidMinerGUI %CMD_LINE_ARGS%
goto startEnd

:startEnd
if errorlevel 2 goto update 
goto endGUI

rem ########################
rem ###                  ###
rem ###  Error messages  ###
rem ###                  ###
rem ########################

:error1
echo.
echo ERROR: Neither 
echo %RAPIDMINER_JAR% 
echo nor 
echo %BUILD% 
echo was found.
echo If you use the source version of RapidMiner Studio, try 
echo 'ant build' or 'ant dist' first.
echo.
pause
goto end

:error2
echo.
echo ERROR: Java cannot be found. 
echo Please install Java properly (check if JAVA_HOME is 
echo correctly set or ensure that 'java' is part of the 
echo PATH environment variable).
echo.
pause
goto end

:error3
echo.
echo ERROR: Java cannot be found in the path JAVA_HOME
echo Please install Java properly (it seems that the 
echo environment variable JAVA_HOME does not point to 
echo a Java installation).
echo.
pause
goto end

:error4
echo.
echo ERROR: Launch settings could not be set
echo Please install RapidMiner Studio properly and do
echo not change the RapidMiner-Studio.bat file.
echo.
pause
goto end

rem #############
rem ###       ###
rem ###  END  ###
rem ###       ###
rem #############

:endGUI
pause

:end
