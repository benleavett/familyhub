#!/bin/bash
if [ $# -eq 0 ]
  then
    echo "No arguments supplied"
else
    name=v$1
    echo "Creating new release $name"
    cd ~/src/familyhub
    git tag $name
    git push github $name
fi
