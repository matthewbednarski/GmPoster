#!/bin/bash


dir=$(dirname "${BASH_SOURCE[0]}")

cdold=$(pwd)
cd "$dir"

java -jar GmPoster-1.0-SNAPSHOT-jar-with-dependencies.jar

cd $cdold


