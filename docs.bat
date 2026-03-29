@echo off
set JAVA_HOME=C:\Program Files\Java\jdk-21
call diagrams.bat || echo Warning: diagram generation skipped (plantuml not found)
mvnw.cmd javadoc:javadoc
echo Docs generated at target\site\apidocs\index.html
