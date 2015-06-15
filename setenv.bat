echo off
cls

rem ####################################################
rem # MODIFYABLE PARAMETERS
rem #
rem # DEVELOPER ENVIRONMENT SETTER LOCATION
rem # Environment   setter  devenv.jar is  located  in a
rem # setter  home  directory.   The  environment setter
rem # home  directory  must  be always a subdirectory of
rem # the project home. It can't be an absolute path.
rem # 
rem # By  default  it  is  "env" subdirectory of project
rem # home (source line) directory. This location can be
rem # changed by modifying the following variable.
    set DEVENV_SETTER_HOME=env

rem # DEVELOPER ENVIRONMENT DEFINITION HOME
rem # Developer   environment   definition   home   sets
rem # location   of  devenv.xml  file.  The  environment
rem # definition   home   directory  must  be  always  a
rem # subdirectory  of  the project home. It can't be an
rem # absolute path.
rem # 
rem # By  default  it  is  "env" subdirectory of project
rem # home (source line) directory. This location can be
rem # changed by modifying the following variable.
    set DEVENV_DEFINITION_HOME=env

rem # END OF MODIFYABLE PARAMETERS
rem ####################################################


rem Start processing
echo DEVENV Info    : Setting development environment...

rem Set project home
set PROJECT_HOME=%~dp0

rem Check if JAVA_HOME set
if  "%JAVA_HOME%" == "" goto error_no_java_home

rem Check existence of java
if  NOT exist "%JAVA_HOME%\bin\java.exe" goto error_no_java

rem Check existence of java runtime
if  NOT exist "%JAVA_HOME%\jre\lib\rt.jar" goto error_no_java_runtime

rem Check existence of environment setter
set DEVENV_SETTER=%PROJECT_HOME%\%DEVENV_SETTER_HOME%\devenv.jar
if  NOT exist %DEVENV_SETTER% goto error_no_setter

rem Check existence of environment definition
set DEVENV_DEF=%DEVENV_DEFINITION_HOME%\devenv.xml
if  NOT exist %PROJECT_HOME%\%DEVENV_DEF% goto error_no_definition


rem Run development environment setter
:run_devenv
"%JAVA_HOME%\bin\java" -jar -Ddevenv.project.home="%PROJECT_HOME%\" -Ddevenv.definition="%DEVENV_DEF%" "%DEVENV_SETTER%"
if errorlevel 1 goto error_exit_wait

rem   Check if environment script is in place
if    NOT exist "%PROJECT_HOME%\setenv_helper.cmd" goto error_setter_failure
call  "%PROJECT_HOME%\setenv_helper.cmd"
erase "%PROJECT_HOME%\setenv_helper.cmd"
goto  end

rem Error messages section
:error_no_java_home
  echo DEVENV Error   : JAVA_HOME not set
  goto error_exit
  
:error_no_java
  echo DEVENV Error   : Java executable not found
  goto error_exit

:error_no_java_runtime
  echo DEVENV Error   : Java runtime not found
  goto error_exit

:error_no_definition
  echo DEVENV Error   : Development environment definition not found
  goto error_exit

:error_no_setter
  echo DEVENV Error   : Development environment setter not found
  goto error_exit
  
:error_setter_failure
  echo DEVENV Error   : Generated environment script not found
  goto error_exit

:error_exit
echo DEVENV Error   : Development environment NOT set
:error_exit_wait
rem  Pause if possible
if   exist %PROJECT_HOME%\%DEVENV_SETTER_HOME%\sleep.exe %PROJECT_HOME%\%DEVENV_SETTER_HOME%\sleep.exe 10

:end
rem Cleanup
set DEVENV_DEFINITION_HOME=
set DEVENV_SETTER_HOME=
set DEVENV_SETTER=
set DEVENV_DEF=
