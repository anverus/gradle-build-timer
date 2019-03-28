package anverus.tools.gradle.timer.reporters

import anverus.tools.gradle.timer.TimeTrackerReporter
import org.gradle.api.internal.tasks.TaskStateInternal
import org.gradle.api.tasks.TaskState


abstract class AbstractBuildTimeTrackerReporter<E extends ReporterExtension<AbstractBuildTimeTrackerReporter<E>>>
        implements TimeTrackerReporter {
    E reporterExtension

    AbstractBuildTimeTrackerReporter(E reporterExtension) {
        this.reporterExtension = reporterExtension
    }
}


abstract class ReporterExtension<R extends AbstractBuildTimeTrackerReporter<ReporterExtension<R>>> {
    final String name
    boolean enabled = true

    ReporterExtension(String name) {
        this.name = name
    }

    abstract R getReporter()
}
