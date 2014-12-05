#!/bin/bash

# This scripts updates the javadoc and pushes all changes together 
# with README.md to gh-pages in order to update th website

sudo apt-get install -qq graphviz
mvn javadoc:javadoc

git config --global user.email "r43ples-travis-ci@users.noreply.github.com"
git config --global user.name "r43ples travis-ci"
git config --global push.default simple 

git clone -b gh-pages https://$GITAUTH@github.com/plt-tud/r43ples
rm -rf r43ples/javadoc
cp -r target/site/apidocs r43ples/javadoc
echo -e '---\nlayout: index\n---\n' > r43ples/index.md
cat README.md >> r43ples/index.md
cd r43ples/javadoc
git add -A *
git commit -am "javadoc updated by travis-ci"
git push https://$GITAUTH@github.com/plt-tud/r43ples
cd ..
rm -rf r43ples
