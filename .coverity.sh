!/#bin/bash

#
# This script is run by travis-ci and pushes the first commit
# of the day to the coverity scan service
#

COMMITS=`git log --since=today.midnight --oneline | wc -l`

if [[ "$COMMITS" -le "1" ]]; then
    echo "first commit a day - push changes to branch coverity_scan"
    
    git config --global user.email "r43ples-travis-ci@users.noreply.github.com"
    git config --global user.name "r43ples travis-ci"
    git config --global push.default simple 

    git fetch origin +coverity_scan:coverity_scan
    git checkout coverity_scan
    git merge --ff --log -m "merge from master to coverity_scan" origin/master
    git push https://$GITAUTH@github.com/plt-tud/r43ples
    git checkout master
else
    echo "Already pushed to coverity_scan today"
fi 
