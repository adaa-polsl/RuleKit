RapidMiner Studio
=================

  Copyright (C) 2001-2019 RapidMiner GmbH


How to start RapidMiner Studio (Windows):
----------------------------------

To start RapidMiner Studio, you can use the "RapidMiner Studio.exe" file located in the same
directory as this readme. If you do not want to/cannot use .exe file, you can also
start RapidMiner Studio via the "RapidMiner-Studio.bat" file also located in this folder.

IMPORTANT: Starting RapidMiner Studio via the rapidminer.jar is not recommended as doing so will break
the update mechanism of RapidMiner Studio.


How to start RapidMiner Studio (Linux):
--------------------------------

To start RapidMiner Studio, you can use the "RapidMiner-Studio.sh" file located in the same 
directory as this readme. In some cases you will need to set chmod +x RapidMiner-Studio via 
console first. Then call ./RapidMiner-Studio.sh inside the RapidMiner Studio directory to 
start RapidMiner Studio. 

IMPORTANT:Starting RapidMiner Studio via the rapidminer.jar is not recommended 
as doing so will break the update mechanism of RapidMiner Studio.


How to start RapidMiner Studio (Mac):
------------------------------

To start RapidMiner Studio, you can use the "RapidMiner Studio.app" file located in the same directory 
as this readme. If you do not want to/cannot use .app file, you can also start RapidMiner Studio via 
the RapidMiner-Studio.sh file also located in this folder.

IMPORTANT: Do not move the "RapidMiner Studio.app" file as this will break the launcher! 
Also starting RapidMiner Studio via the rapidminer.jar is not recommended as doing so will 
break the update mechanism of RapidMiner Studio.


Acquire extensions for RapidMiner Studio:
-----------------------------------

RapidMiner Studio ships with a huge list of operators for many purposes, however certain
functionality for a specific purpose has been moved to extensions.
If you want more operators for these specific purposes like Text Mining or Web Mining, 
you can browse through the available extensions for RapidMiner Studio via "Help" -> 
"Updates and Extensions (Marketplace)". There you will find popular extensions or
you can search for any available extension. To install them, just select them,
click "Install" in the lower right corner and follow the instructions.


Community forum:
----------------

You can visit our active community forums via "Help" -> "Community Forum".
There you can find ideas on how to achieve certain goals, tips&tricks when designing processes, 
solutions to common problems and much more.
If you are experiencing an error and want to ask for help via the forums, please remember
to copy&paste the error from the log file (see below).


Log file:
---------

In your user home folder there is a ".RapidMiner" folder. Inside it you will find a
rm.log file which contains all log messages from the latest RapidMiner Studio execution.


NOTE ON CONFIGURATION FILES:
----------------------------
RapidMiner Studio looks for the following configuration files and reads them in 
this ordering

  RAPID_MINER_HOME/rapidminer-studio-settings.cfg
  ~/.RapidMiner/rapidminer-studio-settings.cfg
  rapidminer-studio-settings.cfg


Where ~ is your home directory. You can see which files are read 
when RapidMiner Studio starts.


Please note:
------------

* The license of all 3rd party libraries can be found in the directory 
  "third-party-licenses" located in the main directory of RapidMiner Studio.
  
 