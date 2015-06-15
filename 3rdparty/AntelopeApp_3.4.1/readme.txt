README.TXT

Antelope 3.4.1

This is definitely a beta release -- I've adjusted Antelope to work with
both jEdit 4.2 (in beta) and Ant 1.6 (also in beta). Things seem to be okay,
but as neither jEdit nor Ant are final, Antelope shouldn't be considered
final either. I haven't got all the docs up to date yet. Please report bugs
at http://antelope.sourceforge.net.


Antelope is packaged in 4 files to make it easy to get only what you want:

Antelope_3.4.1_src.zip -- just the source code
AntelopeApp_3.4.1.zip -- ready to run application
AntelopePlugin_3.4.1.zip -- ready to install jEdit plugin
AntelopeTasks_3.4.1.zip -- ready to use tasks (included with app and plugin)

See the appropriate readme file for details of each.



Quick Start:

To run Antelope: ant -f run.xml



Running Antelope:

Instead of separate start scripts for starting Antelope for Windows,
Unix, Mac, or whatever, run.xml is an Ant build file that starts Antelope on
all platforms that run Ant. 

Obviously, Ant must already be installed, but Antelope is pretty much
useless without Ant, so this doesn't seem like an unreasonable
requirement.

To use run.xml, you need to have a working installation of Ant.
Antelope has been tested extensively with Ant version 1.5.x. 

To run Antelope, just do "ant -f run.xml". The first time you run run.xml,
you will be prompted to enter the location of your Ant "lib" directory. This
is the directory that contains ant.jar.


You can also use command line java:

java -jar AntelopeApp_3.4.1.jar [build_file]

You may pass a build file name on the command line or choose one from the 
GUI. In this case, ant.jar must be in your classpath.


