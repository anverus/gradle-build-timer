package anverus.tools.gradle.timer

import org.gradle.BuildResult
import org.gradle.api.internal.tasks.TaskStateInternal
import org.gradle.api.logging.Logger
import org.gradle.api.tasks.TaskState

@FunctionalInterface
trait TimeTrackerReporter {
    abstract def run(BuildTiming timings, BuildResult result, Logger logger)

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
}