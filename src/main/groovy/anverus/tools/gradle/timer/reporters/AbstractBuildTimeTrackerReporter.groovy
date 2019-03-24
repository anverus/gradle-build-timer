package anverus.tools.gradle.timer.reporters

import anverus.tools.gradle.timer.BuildTiming
import org.gradle.BuildResult
import org.gradle.api.internal.tasks.TaskStateInternal
import org.gradle.api.logging.Logger
import org.gradle.api.tasks.TaskState

abstract class AbstractBuildTimeTrackerReporter<E extends ReporterExtension<AbstractBuildTimeTrackerReporter<E>>> {
    E reporterExtension
    Logger logger

    AbstractBuildTimeTrackerReporter(E reporterExtension, Logger logger) {
        this.reporterExtension = reporterExtension
        this.logger = logger
    }


    abstract run(BuildTiming timings, BuildResult result)

    static String getState(TaskState taskState) {
        def states = new StringJoiner(':')

        if (taskState.upToDate) {
            states.add 'UP-TO-DATE'
        }
        if (taskState.skipped) {
            states.add 'SKIPPED'
        }
        if (taskState.noSource) {
            states.add 'NO-SOURCE'
        }
        if (taskState.didWork) {
            states.add 'DID-WORK'
        }
        if (taskState.executed) {
            states.add 'EXECUTED'
        }
        if (taskState instanceof TaskStateInternal && ((TaskStateInternal)taskState).isFromCache()) {
            states.add 'FROM-CACHE'
        }
        return states.toString()
    }

    boolean isEnabled(List<String> tasks) {
        reporterExtension.enabled && tasks != ["clean"]
    }
}


abstract class ReporterExtension<R extends AbstractBuildTimeTrackerReporter<ReporterExtension<R>>> {
    final String name
    boolean enabled = true

    ReporterExtension(String name) {
        this.name = name
    }

    abstract R getReporter(Logger logger)
}
