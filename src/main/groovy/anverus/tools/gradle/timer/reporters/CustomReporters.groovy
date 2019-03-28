package anverus.tools.gradle.timer.reporters

import anverus.tools.gradle.timer.BuildTiming
import anverus.tools.gradle.timer.TimeTrackerReporter
import org.gradle.BuildResult
import org.gradle.api.logging.Logger

class CustomReporters extends AbstractBuildTimeTrackerReporter<CustomReportersExtension> {
    CustomReporters(CustomReportersExtension reporterExtension) {
        super(reporterExtension)
    }

    @Override
    def run(BuildTiming timings, BuildResult result, Logger logger) {
        reporterExtension.reporters.stream().forEach { customReporter ->
            customReporter.run(timings, result, logger)
        }
    }
}

class CustomReportersExtension extends ReporterExtension<CustomReporters> {
    List<TimeTrackerReporter> reporters = []

    CustomReportersExtension(String name) {
        super(name)
    }

    @Override
    CustomReporters getReporter() {
        return new CustomReporters(this)
    }
}
