#!/bin/bash

############################################################
##                                                        ##
##       Unix Start Script for RapidMiner Studio          ##
##                                                        ##
##  This script tries to determine the location of        ##
##  RapidMiner Studio, searches for a proper Java         ##
##  executable and starts the program.                    ##
##                                                        ##
############################################################

## remove _JAVA_OPTIONS environment variable for this run ##
## it could contain stuff that break Studio launching so we ignore it completely ##
unset _JAVA_OPTIONS

if [ -z "${RAPIDMINER_HOME}" ] ; then
	RAPIDMINER_HOME="$(cd "$(dirname "$0")" 2>/dev/null && pwd)"
    echo "RAPIDMINER_HOME is not set. Trying the directory '${RAPIDMINER_HOME}'..."
fi 

##########################
##                      ##
##  Searching for Java  ##
##                      ##
##########################

# JAVA_HOME set, so use it
if [ ! -z "${JAVA_HOME}" ] ; then
    if [ -x "${JAVA_HOME}/bin/java" ]; then
        JAVA="${JAVA_HOME}/bin/java"
    fi
fi

# otherwise, try to find java using which
if [ -z "${JAVA}" ] ; then
    _jfnd="`which java`"
    if [ -x "${_jfnd}" ]; then
        JAVA="${_jfnd}"
    else
        echo 'Could not find the java executable in default path or ${JAVA_HOME}/bin/java.'
        echo "Edit $0 and/or your local startup files."
        exit 1
    fi
    unset _jfnd
fi



###############################################
##                                           ##
##  Launch RapidMiner and check for updates  ##
##                                           ##
###############################################

update_root=~/.RapidMiner/update
update_dir=${update_root}/RUinstall
update_script=${update_root}/UPDATE

LAUNCH=1
while [ ${LAUNCH} -eq 1 ]
do
    # Performing possible update
    if [ -d "${update_dir}" ]; then
        if [ -w "${RAPIDMINER_HOME}" ] ; then
            echo "======================================================================="
            echo "Performing update. Copying files from '${update_dir}' to '${RAPIDMINER_HOME}'."
            cp -rf "${update_dir}"/* "${RAPIDMINER_HOME}"
            rm -rf "${update_dir}"
            echo "Copy complete."
            if [ -f "${update_script}" ] ; then
                echo "Deleting obsolete files listed in ${update_script}."
                {
                    while read COMMAND FILE
                    do
                        if [ "DELETE" = "${COMMAND}" ] ; then
                            # Strip rapidminer/ prefix
                            FILE=`echo ${FILE} | sed -e 's/^rapidminer\///'`
                            TO_DELETE=${RAPIDMINER_HOME}/${FILE}
                            if [ -f "${TO_DELETE}" ] ; then
                                echo "Deleting regular file ${TO_DELETE}"
                                rm "${TO_DELETE}"
                            elif [ -d "${TO_DELETE}" ] ; then
                                echo "Deleting directory ${TO_DELETE}"
                                rmdir "${TO_DELETE}"
                            else
                                echo "Cannot delete file ${TO_DELETE} (does not exist)"
                            fi                          
                        else
                            echo "Unknown update command: ${COMMAND}"
                        fi
                    done
                } < ${update_script}
                rm "${update_script}"
                echo "Completed deletion of obsolete files."
            else
                echo "No update script found in ${update_script}."
            fi
            rm -rf "${update_root}"
            echo "Update complete"
            echo "======================================================================="
        else
            echo "======================================================================="
            echo "ATTENTION: An update was downloaded, but we cannot write to"
            echo "           ${RAPIDMINER_HOME}. "
            echo "           Ignoring update. Please restart as super user."
            echo "======================================================================="
        fi
    fi

    # Compile launch parmateres
    rmClasspath="${RAPIDMINER_HOME}"/lib/*:"${RAPIDMINER_HOME}"/lib/jdbc/*    
    JVM_OPTIONS=  -XX:+UseG1GC -XX:G1HeapRegionSize=32m -XX:ParallelGCThreads=4 -XX:InitiatingHeapOccupancyPercent=55 -Xms384m -Xmx12174m -Djava.net.preferIPv4Stack=true -Dsun.java2d.dpiaware=false -Djava.net.useSystemProxies=true
    
    # Launch Studio
    LAUNCH=0
    if [ $# -gt 0 ]; then
      eval \"$JAVA\" $JVM_OPTIONS -cp \"${rmClasspath}\" com.rapidminer.gui.RapidMinerGUI \"$@\"
    else
      eval \"$JAVA\" $JVM_OPTIONS -cp \"${rmClasspath}\" com.rapidminer.gui.RapidMinerGUI
    fi


    if [ $? -eq 2 ]
    then
        echo RapidMiner Studio will now relaunch 
        LAUNCH=1
    fi
done
