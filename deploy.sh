#!/bin/bash
if [ $# -eq 0 ]
  then
    echo "No arguments supplied"
else
    echo "Creating new release $1"
    cd ~/src/familyhub
    git tag $1
    git push github $1
fi
