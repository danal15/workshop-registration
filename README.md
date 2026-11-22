# Workshop Registration System

A Maven multi-module project for the workshop registration system (course exercise EX2 – Maven Evolution).

```
root                (aggregator + top parent)
|-- core            (aggregator + parent of the implementation modules)
|   |-- model-impl  implementation of the model interfaces
|   `-- logic-impl  implementation of WorkshopSystem
`-- app             the runnable application (workshop-cli)
```

Requirements: JDK 25 and Maven 3.9+.

## 1. Install the course jars into the local Maven repository (once per computer)

The API jars and the tester are not on Maven Central, so they must be installed into the local
repository (`~/.m2/repository`) by hand. Run these commands in the folder that holds the three jars:

```
mvn install:install-file "-Dfile=model-2.0.jar" "-DgroupId=patmal.workshops" "-DartifactId=model" "-Dversion=2.0" "-Dpackaging=jar"
mvn install:install-file "-Dfile=logic-2.0.jar" "-DgroupId=patmal.workshops" "-DartifactId=logic" "-Dversion=2.0" "-Dpackaging=jar"
mvn install:install-file "-Dfile=workshop-tester-2.0.jar" "-DgroupId=patmal.workshops" "-DartifactId=workshop-tester" "-Dversion=2.0" "-Dpackaging=jar"
```

The quotes around each `-D...` argument are needed in PowerShell: without them PowerShell splits
`-Dversion=2.0` into `-Dversion=2` and `.0`, and Maven fails with
"The goal you specified requires a project to execute but there is no POM in this directory".
In cmd or bash the quotes are harmless.

To check that the jars were installed:

```
mvn dependency:get "-Dartifact=patmal.workshops:logic:2.0" "-Dtransitive=false"
```

## 2. Build

In the root folder:

```
mvn clean install
```

This builds all the modules in order and produces the uber jar `app/target/workshop-cli.jar`
(the application, our modules and all their runtime dependencies in one jar).

In the `verify` phase the build then runs the course tester against that uber jar
(exec-maven-plugin). If the tester reports a failure, the build fails. To build without it:

```
mvn clean install "-Dexec.skip=true"
```

## 3. Run the application

```
java -jar app/target/workshop-cli.jar
```

It fills a WorkshopSystem with demo data, logs what it does, and writes `instructors.json`,
`students.json` and `teams.json` into the current folder.

The log level is `info` by default and can be changed without rebuilding, for example:

```
java "-Dworkshop.log.level=warn" -jar app/target/workshop-cli.jar
```

## 4. Run the course tester by hand

The build already runs it (section 2); to run it yourself against the uber jar:

```
java -cp "workshop-tester-2.0.jar;app/target/workshop-cli.jar" patmal.workshops.main.Main
```

(On Linux / macOS use `:` instead of `;`.) The expected last line is `RESULT: ALL OK`.
