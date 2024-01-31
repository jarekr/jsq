#!/bin/sh
classpath="build/install"

for jar in $(ls build/install/lib); do
  classpath="$classpath:build/install/lib/$jar"
done
echo $classpath
java -cp $classpath jarekr.jsq.Jsq $@
