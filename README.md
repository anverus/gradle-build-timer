# Gradle Build Timer Plugin

This plugin allow to time overall build along with every executed task. It collects this information and reports it at the end of the build using several predefined reporters.

## Reporters

### TopNTasks
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

Results
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
### TopNTaskTypes
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
### Prometheus Push
```groovy
buildtiming {
    reporters {
        prometheus {
            pushGatewayHost = 'prometheus-pushgateway'
            buildCustomLabels = [
                'type':'local', // ci, release
                'office': 'needham'
            ]
            taskCustomLabels = [
                'jdk': JavaVersion.current
            ]
        }
    }
}
```