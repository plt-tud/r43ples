#!/bin/bash

#
# This script is run by travis-ci and pushes the first commit
# of the day to the coverity scan service
#

COMMITS=`git log --since=today.midnight --oneline | wc -l`

if [[ "$COMMITS" -le "1" ]]; then
    #first commit a day - push changes to branch coverity_scan
    git clone -b coverity_scan https://$GITAUTH@github.com/plt-tud/r43ples
    cd r43ples
    git fetch origin
    git merge origin/master
    git config --local user.email "r43ples-travis-ci@users.noreply.github.com"
    git config --local user.name "r43ples travis-ci"
    git config --local push.default simple
    git add *
    git commit -am "push to coverity scan by travis-ci"
    git push https://$GITAUTH@github.com/plt-tud/r43ples
    cd ..
    rm -rf r43ples
else
    echo "Not the first commit of the day - no push to coverity required"
fi 
