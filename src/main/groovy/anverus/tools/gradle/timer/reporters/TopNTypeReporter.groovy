package anverus.tools.gradle.timer.reporters


import anverus.tools.gradle.timer.BuildTiming
import org.gradle.BuildResult
import org.gradle.api.logging.Logger

import java.util.stream.Collectors

class TopNTypeReporter extends AbstractBuildTimeTrackerReporter<TopNTypeReporterExtension> {
    TopNTypeReporter(TopNTypeReporterExtension extension) {
        super(extension)
    }

    @Override
    def run(BuildTiming timings, BuildResult result, Logger logger) {
        def limit = reporterExtension.topN > 0 ? reporterExtension.topN : Long.MAX_VALUE // No limit if topN set to 0 or less
        logger.lifecycle('\n| ==== Top task types by execution time (millis) ====')
        timings.taskTimingMap.values()
                .stream()
                .collect(Collectors.groupingBy({it.name}, Collectors.summingLong({ tt -> tt.duration })))
                .entrySet()
                .stream()
                .sorted(Comparator.comparing { -it.value })
                .limit(limit)
                .forEach { logger.lifecycle (String.format('|%,20d | %s', it.value, it.key))}
    }
}

class TopNTypeReporterExtension extends ReporterExtension<TopNTypeReporter> {
    int topN

    TopNTypeReporterExtension(String name) {
        super(name)
    }

    @Override
    TopNTypeReporter getReporter() {
        return new TopNTypeReporter(this)
    }
}