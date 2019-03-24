package anverus.tools.gradle.timer.reporters


import anverus.tools.gradle.timer.BuildTiming
import org.gradle.BuildResult
import org.gradle.api.logging.Logger

class TopNReporter extends AbstractBuildTimeTrackerReporter<TopNReporterExtension> {
    TopNReporter(TopNReporterExtension extension, Logger logger) {
        super(extension, logger)
    }

    @Override
    run(BuildTiming timings, BuildResult result) {
        def limit = reporterExtension.topN > 0 ? reporterExtension.topN : Long.MAX_VALUE // No limit if topN set to 0 or less

        timings.taskTimingMap.values()
            .stream()
            .sorted(Comparator.comparing { it.startTime - it.finishTime })
            .limit(limit)
            .forEach {tt -> logger.lifecycle (String.format('|%,20d | %30s | %s', tt.finishTime - tt.startTime, getState(tt.state), tt.path))}
    }
}

class TopNReporterExtension extends ReporterExtension<TopNReporter> {
    int topN

    TopNReporterExtension(String name) {
        super(name)
    }

    @Override
    TopNReporter getReporter(Logger logger) {
        return new TopNReporter(this, logger)
    }
}
