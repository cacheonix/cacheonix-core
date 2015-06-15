cd ..\war
%JAVA_HOME%/bin/java -cp ../../../lib/ant/ant.jar;../../../lib/ant/ant-launcher.jar org.apache.tools.ant.Main -buildfile ../standalone/run.xml run -DnrOfCalls=%1
cd ..\standalone
