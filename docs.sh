#!/usr/bin/env bash
export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-21.0.2.jdk/Contents/Home
./diagrams.sh || echo "Warning: diagram generation skipped (plantuml not found)"
./mvnw javadoc:javadoc
echo "Docs generated at target/site/apidocs/index.html"
