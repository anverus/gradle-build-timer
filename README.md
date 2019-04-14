[![Codacy Badge](https://api.codacy.com/project/badge/Grade/f4497b5ed59446db8f2580b47aae84c0)](https://app.codacy.com/app/anverus/gradle-build-timer?utm_source=github.com&utm_medium=referral&utm_content=anverus/gradle-build-timer&utm_campaign=Badge_Grade_Dashboard)
# Gradle Build Timer Plugin [![Build Status](https://travis-ci.org/anverus/gradle-build-timer.svg?branch=master)](https://travis-ci.org/anverus/gradle-build-timer)  [![Download](https://api.bintray.com/packages/anverus/maven/gradle-build-timer/images/download.svg?version=0.1.3) ](https://bintray.com/anverus/maven/gradle-build-timer/0.1.3/link) 

This plugin allow to time overall build along with every executed task. It collects this information and reports it at the end of the build using several predefined reporters.

## Applying plugin
```groovy
buildscript {
    repositories {
        mavenLocal()
        jcenter()
    }
    dependencies {
        classpath 'anverus.tools:gradle-build-timer:0.1-SNAPSHOT'
    }
}
apply plugin: 'anverus.tools.timer'
```
or use new style 
```groovy
plugins {
    id 'anverus.tools.timer' version '0.1-SNAPSHOT'
}
```
Do not forget to add configuration
```groovy
// Plugin configuration 
buildtiming {
    reporters {
        // Pick reporters you want to use
        topNTask {
            topN = 25
        }
        prometheus {
            pushGatewayHost = 'prometheus-pushgateway'
        }
   }
}
```

## Reporters
In order to time the build and tasks one or more reporters should be specified

### Top *n* tasks Reporter
This reporter outputs timing for tasks sorted by time descending
Number of tasks shown is controlled by *topN* configuration parameter 

Plugin configuration
```groovy
buildtiming {
    reporters {
        topNTask {
            topN = 25
        }
    }
}
```

Sample results
```text
|               3,192 |              DID-WORK:EXECUTED | :test
|               2,123 |              DID-WORK:EXECUTED | :groovydoc
|               1,438 |              DID-WORK:EXECUTED | :compileGroovy
|                 314 |              DID-WORK:EXECUTED | :compileTestGroovy
|                  30 |              DID-WORK:EXECUTED | :validateTaskProperties
|                  19 |              DID-WORK:EXECUTED | :publishPluginGroovyDocsJar
|                  15 |              DID-WORK:EXECUTED | :jar
|                   8 |              DID-WORK:EXECUTED | :pluginUnderTestMetadata
|                   7 |              DID-WORK:EXECUTED | :publishPluginJar
|                   4 |     SKIPPED:NO-SOURCE:EXECUTED | :compileJava
|                   2 |    UP-TO-DATE:SKIPPED:EXECUTED | :processResources
|                   1 |     SKIPPED:NO-SOURCE:EXECUTED | :processTestResources
|                   1 |     SKIPPED:NO-SOURCE:EXECUTED | :javadoc
|                   1 |    UP-TO-DATE:SKIPPED:EXECUTED | :publishPluginJavaDocsJar
|                   0 |                       EXECUTED | :testClasses
|                   0 |                       EXECUTED | :build
|                   0 |                       EXECUTED | :classes
|                   0 |    UP-TO-DATE:SKIPPED:EXECUTED | :pluginDescriptors
|                   0 |                       EXECUTED | :check
|                   0 |                       EXECUTED | :assemble
|                   0 |     SKIPPED:NO-SOURCE:EXECUTED | :compileTestJava

BUILD SUCCESSFUL in 7s
12 actionable tasks: 9 executed, 3 up-to-date

``` 
### Top *n* task type (by task name) Reporter
This reporter outputs timing for tasks aggregated by task name sorted by time descending
Number of output rows is controlled by *topN* configuration parameter
Used for multi-project builds to see what types of tasks take most of the time 

Plugin configuration
```groovy
buildtiming {
    reporters {
        topNType {
            topN = 10
        }
    }
}
```
Sample results
```text
|                  17 | groovydoc
|                  13 | compileTestGroovy
|                  11 | compileGroovy
|                   9 | test
|                   4 | pluginUnderTestMetadata
|                   2 | publishPluginJar
|                   2 | jar
|                   1 | publishPluginJavaDocsJar
|                   1 | compileJava
|                   1 | compileTestJava

BUILD SUCCESSFUL in 0s

```
### Prometheus PushGateway Reporter
Configuration
```groovy
buildtiming {
    reporters {
        prometheus {
            pushGatewayHost = 'prometheus-pushgateway'
            buildCustomLabels = [       // add these labels to build_overall_timing metric
                'type':'local', // ci, release
                'office': 'needham'
            ]
            taskCustomLabels = [            // add these lebels to each task timing metric
                'jdk': JavaVersion.current
            ]
            taskTimingThresholdMillis = 50 // skip reporting tasks taking less then 50 milliseconds
            skipReportingTasks = [         // skip reporting builds consisting any subset of these (help) tasks
                        'clean',
                        'buildEnvironment',
                        'components',
                        'dependencies',
                        'dependencyInsight',
                        'dependentComponents',
                        'help',
                        'model',
                        'projects',
                        'properties',
                        'tasks'
                ]
        }
    }
}
```
Reporter publishes two metrics
Values are time taken (in milliseconds) for given build or tasks

* build_overall_timing pushed with following labels 

label name | value
---------- | -----
instance | hostname of machine running the build
job | name of the project
status | whether build is successful or not
tasks | list of tasks passed as argument to build

Additional custom labels can be added with buildCustomLabels

* build_task_timing each executed task is pushed with following labels

label name | value
---------- | -----
instance | hostname of machine running the build
job | name of the project
name | name of a task
status | aggregated status of task execution

Additional custom labels can be added with taskCustomLabels.

### Custom reporters
If you realize that your needs are not covered by provided reporters you can configure your own custom reporter to handle timing results.

Configuration
```groovy
buildtiming {
    reporters {
        customReporters {
            reporters = [
                { anverus.tools.gradle.timer.BuildTiming timings, BuildResult result, Logger logger ->
                    logger.lifecycle('Hello custom reporter')

                    // Output top 5 tasks 
                    timings.taskTimingMap.values()
                        .stream()
                        // Want to add filters?
                        .filter { it.name == 'compileJava' }
                        .filter { it.finishTime - it.startTime > 1000 }
                        .sorted(Comparator.comparing { it.startTime - it.finishTime })
                        .limit(5)
                        .forEach { tt ->
                            logger.lifecycle (String.format('|%,20d | %10s | %s',
                                tt.finishTime - tt.startTime,
                                tt.state.didWork ? 'WORKED' : ((TaskStateInternal)tt.state).fromCache ? 'FROM-CACHE' : tt.state.upToDate ? 'UP-TO-DATE' : 'MEH',
                                tt.path))
                        }
                } as anverus.tools.gradle.timer.TimeTrackerReporter
            ]
        }
    }
}
```

And if you are feeling generous and think others can benefit from your contribution submit a pull request to include your reporter into distribution.
