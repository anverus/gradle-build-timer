package anverus.tools.gradle.timer

import anverus.tools.gradle.timer.reporters.AbstractBuildTimeTrackerReporter
import anverus.tools.gradle.timer.reporters.PrometheusReporterExtension
import anverus.tools.gradle.timer.reporters.ReporterExtension
import anverus.tools.gradle.timer.reporters.TopNReporterExtension
import anverus.tools.gradle.timer.reporters.TopNTypeReporterExtension
import static com.google.common.base.Preconditions.checkNotNull
import groovy.transform.builder.Builder
import groovy.transform.builder.ExternalStrategy
import org.gradle.BuildListener
import org.gradle.BuildResult
import org.gradle.api.NamedDomainObjectCollection
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.execution.TaskExecutionListener
import org.gradle.api.initialization.Settings
import org.gradle.api.invocation.Gradle
import org.gradle.api.logging.Logger
import org.gradle.api.tasks.TaskState

import java.util.concurrent.ConcurrentHashMap

class BuildTimingPlugin implements Plugin<Project> {
    def REPORTERS = [
            topNTask: TopNReporterExtension,
            topNType: TopNTypeReporterExtension,
            prometheus: PrometheusReporterExtension
    ]
    Logger logger
    NamedDomainObjectCollection<ReporterExtension> reporterExtensions

    @Override
    void apply(Project project) {
        logger = project.logger
        project.extensions.create("buildtiming", BuildTimingPluginExtension)

        reporterExtensions = project.buildtiming.extensions.reporters =
            project.container(ReporterExtension,
                { name -> checkNotNull(REPORTERS.get(name),
                    "Reporter Extention for ${name} not found").newInstance(name)})

        project.gradle.addBuildListener(new BuildTimingRecorder(this))
    }

    List<AbstractBuildTimeTrackerReporter> getReporters() {
        reporterExtensions
            .findAll { it.enabled }
            .collect { it.getReporter(logger) }
    }
}

class BuildTimingPluginExtension {
    // TODO: Add report on failure?
    // TODO: Add report if tasks include?
}

class BuildTiming {
    // Overall build duration
    final long startMillis = System.currentTimeMillis()
    long finishMillis

    // Command line tasks
    List<String> tasks

    // Task timing by task path
    final Map<String, TaskTiming> taskTimingMap = new ConcurrentHashMap<>()
}

class TaskTiming {
    String name, path
    TaskState state
    long startTime, finishTime
}

@Builder(builderStrategy = ExternalStrategy, forClass = TaskTiming)
class TaskTimingBuilder {}

class BuildTimingRecorder implements BuildListener, TaskExecutionListener {
    final BuildTimingPlugin plugin
    final BuildTiming buildTiming

    BuildTimingRecorder(BuildTimingPlugin pPlugin) {
        plugin = pPlugin
        buildTiming = new BuildTiming()
    }

    @Override
    void buildStarted(Gradle gradle) {}

    @Override
    void settingsEvaluated(Settings settings) {}

    @Override
    void projectsLoaded(Gradle gradle) {}

    @Override
    void projectsEvaluated(Gradle gradle) {}

    @Override
    void buildFinished(BuildResult buildResult) {
        buildTiming.finishMillis = System.currentTimeMillis()
        buildTiming.tasks = buildResult.gradle.startParameter.getTaskNames()

        plugin.reporters.each {
            it.run (buildTiming, buildResult)
        }
    }

    @Override
    void beforeExecute(Task task) {
        buildTiming.taskTimingMap.put(task.getPath(),
            new TaskTimingBuilder()
                .name(task.getName())
                .path(task.getPath())
                .startTime(System.currentTimeMillis() - buildTiming.startMillis)
                .build()
        )
    }

    @Override
    void afterExecute(Task task, TaskState taskState) {
        TaskTiming taskTiming = buildTiming.taskTimingMap.get(task.getPath())
        taskTiming.state = taskState
        taskTiming.finishTime = System.currentTimeMillis() - buildTiming.startMillis
    }
}