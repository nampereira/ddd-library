#!/usr/bin/env bash
# Set JAVA_HOME to your local JDK 21 installation if it is not already in your environment.
# Examples:
#   macOS (Homebrew):  export JAVA_HOME="$(/usr/libexec/java_home -v 21)"
#   Linux:             export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64
#   Windows (Git Bash): export JAVA_HOME="C:/Program Files/Java/jdk-21"
./mvnw package
