# JSQ - java squish

A java command-line tool for compressing / decompressing text using huffman encoding


This is a subbmission for https://codingchallenges.fyi/challenges/challenge-huffman

## Building

Requires jdk >= 1.17 /java 17, gradle 7+

To build a runnable distribution:

```
./gradlew install
```

runtime jars are in build/install/lib
shell script in build/install/bin

## Running

After building, run with

```
./jsq.sh --help
```

