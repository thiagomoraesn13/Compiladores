#!/bin/bash

java -jar Coco.jar advcalc.atg

javac *.java

javac Interpret.java

java Interpret
