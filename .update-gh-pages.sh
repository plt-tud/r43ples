#!/bin/bash

mkdir gh-pages
cp README.md gh-pages/Readme.md
cp _config.yml gh-pages/_config.yml
cp -r target/site/ gh-pages/
cp -r doc/ gh-pages/
