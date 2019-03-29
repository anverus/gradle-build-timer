package anverus.tools.gradle.timer.reporters


import anverus.tools.gradle.timer.BuildTiming
import org.gradle.BuildResult
import org.gradle.api.logging.Logger

class TopNReporter extends AbstractBuildTimeTrackerReporter<TopNReporterExtension> {
    TopNReporter(TopNReporterExtension extension) {
        super(extension)
    }

    @Override
    def run(BuildTiming timings, BuildResult result, Logger logger) {
        def limit = reporterExtension.topN > 0 ? reporterExtension.topN : Long.MAX_VALUE // No limit if topN set to 0 or less
        logger.lifecycle('\n|   ===== Top tasks by execution time (millis) =====   |')
        timings.taskTimingMap.values()
            .stream()
            .sorted(Comparator.comparing { -it.duration })
            .limit(limit)
            .forEach {tt -> logger.lifecycle (String.format('|%,20d | %30s | %s', tt.duration, getState(tt.state), tt.path))}
    }
}

class TopNReporterExtension extends ReporterExtension<TopNReporter> {
    int topN

    TopNReporterExtension(String name) {
        super(name)
    }

    @Override
    TopNReporter getReporter() {
        return new TopNReporter(this)
    }
}
