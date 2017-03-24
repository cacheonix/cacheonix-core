#!/bin/sh

clear

####################################################
# MODIFYABLE PARAMETERS
#
# DEVELOPER ENVIRONMENT SETTER LOCATION
# Environment   setter  devenv.jar is  located  in a
# setter  home  directory.   The  environment setter
# home  directory  must  be always a subdirectory of
# the project home. It can't be an absolute path.
# 
# By  default  it  is  "env" subdirectory of project
# home (source line) directory. This location can be
# changed by modifying the following variable.
DEVENV_SETTER_HOME=env

# DEVELOPER ENVIRONMENT DEFINITION HOME
# Developer   environment   definition   home   sets
# location   of  devenv.xml  file.  The  environment
# definition   home   directory  must  be  always  a
# subdirectory  of  the project home. It can't be an
# absolute path.
# 
# By  default  it  is  "env" subdirectory of project
# home (source line) directory. This location can be
# changed by modifying the following variable.
DEVENV_DEFINITION_HOME=env

# END OF MODIFYABLE PARAMETERS
####################################################


# Start processing
echo 'DEVENV Info    : Setting development environment...'

# Set project home
#for /D $$i in (.) do set PROJECT_HOME=$$~fi
PROJECT_HOME=.


#
# Check if we are running under cygwin
DEVENV_CYGWIN=false;
case "`uname`" in
  CYGWIN*) DEVENV_CYGWIN=true ;;
esac


DEVENV_OK=true

# Check if JAVA_HOME set
if $DEVENV_OK && [ -z "$JAVA_HOME" ] ; then 
  echo
  echo 'DEVENV Error   : JAVA_HOME not set'
  DEVENV_OK=false;
fi

# For Cygwin, ensure paths are in UNIX format before anything is touched
DEVENV_JAVA_HOME=$JAVA_HOME
if $DEVENV_OK && $DEVENV_CYGWIN ; then
  DEVENV_JAVA_HOME=`cygpath --path --unix "$JAVA_HOME"`
fi


# Check if Java binary dir exists
if $DEVENV_OK && [ ! -d "$DEVENV_JAVA_HOME/bin" ] ; then
  echo
  echo "DEVENV Error   : Java binary dir not found : $DEVENV_JAVA_HOME/bin"
  DEVENV_OK=false
fi



# Check if setter exists
DEVENV_SETTER="$PROJECT_HOME/$DEVENV_SETTER_HOME/devenv.jar"
if $DEVENV_OK && [ ! -f "$DEVENV_SETTER" ] ; then
  echo
  echo 'DEVENV Error   : Development environment setter not found'
  DEVENV_OK=false
fi


# Check existence of environment definition
DEVENV_DEF="$DEVENV_DEFINITION_HOME/devenv.xml"
if $DEVENV_OK && [ ! -f "$DEVENV_DEF" ] ; then
  echo
  echo 'DEVENV Error   : Development environment definition not found'
  DEVENV_OK=false
fi


# Run development environment setter
if $DEVENV_OK ; then
  $JAVA_HOME/bin/java -jar -Ddevenv.project.home="$PROJECT_HOME" -Ddevenv.definition="$DEVENV_DEF" "$DEVENV_SETTER"
  [ $? -ne 0 ] && DEVENV_OK=false
fi

#   Check if environment script is in place
if $DEVENV_OK && [ ! -f "$PROJECT_HOME/setenv_helper.sh" ] ; then 
  echo
  echo 'DEVENV Error   : Generated environment script not found'
  DEVENV_OK=false
fi 

# Run
if $DEVENV_OK ; then
  . $PROJECT_HOME/setenv_helper.sh
  rm -f $PROJECT_HOME/setenv_helper.sh
  export CLASSPATH
fi

# Result
if [ ! $DEVENV_OK ] ; then
  echo;
  echo "DEVENV Error   : Development environment is NOT set"
  sleep 5
fi

# Cleanup
unset DEVENV_DEFINITION_HOME
unset DEVENV_SETTER_HOME
unset DEVENV_SETTER
unset DEVENV_DEF
unset DEVENV_OK
