#!/bin/bash

#
# This script is run by travis-ci and pushes the first commit
# of the day to the coverity scan service
#

COMMITS=`git log --since=today.midnight --oneline | wc -l`

if [ "$TRAVIS_BRANCH" != "develop" ]; then 
    echo "Not on develop branch -> no update of coverity_scan!"
    exit 0;
fi

if [[ "$COMMITS" -le "1" ]]; then
    echo "first commit a day - push changes to branch coverity_scan"
    
    git config --global user.email "r43ples-travis-ci@users.noreply.github.com"
    git config --global user.name "r43ples travis-ci"
    git config --global push.default simple 

    git fetch origin +coverity_scan:coverity_scan
    git checkout coverity_scan
    git merge --ff --log -m "merge from develop to coverity_scan" origin/develop
    git push https://$GITAUTH@github.com/plt-tud/r43ples
else
    echo "Already pushed to coverity_scan today"
fi 
