#!/bin/bash

#
# This script is run by travis-ci and pushes the first commit
# of the day to the coverity scan service
#

COMMITS=`git log --since=today.midnight --oneline | wc -l`

if [[ "$COMMITS" -le "1" ]]; then
    #first commit a day - push changes to branch coverity_scan
    
    git config --global user.email "r43ples-travis-ci@users.noreply.github.com"
    git config --global user.name "r43ples travis-ci"
    git config --global push.default simple 

    git clone -b coverity_scan https://$GITAUTH@github.com/plt-tud/r43ples coverity_scan
    cd coverity_scan
    git fetch origin
    git merge --ff --log -m "merge from master to coverity_scan" origin/master
    git commit -am "push to coverity scan by travis-ci"
    git push https://$GITAUTH@github.com/plt-tud/r43ples
    cd ..
    rm -rf coverity_scan
else
    echo "Not the first commit of the day - no push to coverity required"
fi 
