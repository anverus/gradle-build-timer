package anverus.tools.gradle.timer.reporters


import anverus.tools.gradle.timer.BuildTiming
import io.prometheus.client.CollectorRegistry
import io.prometheus.client.Gauge
import io.prometheus.client.exporter.PushGateway
import org.gradle.BuildResult
import org.gradle.api.logging.Logger

class PrometheusReporter extends AbstractBuildTimeTrackerReporter<PrometheusReporterExtension> {
    PrometheusReporter(PrometheusReporterExtension extension) {
        super(extension)
    }

    @Override
    def run(BuildTiming timings, BuildResult result, Logger logger) {
        CollectorRegistry registry = new CollectorRegistry()

        def hostName = InetAddress.localHost.hostName

        def buildSummary = Gauge.build()
            .name('build_overall_timing')
            .help('Overall build time in millis')
            .labelNames ((['instance', 'tasks', 'status'] + reporterExtension.buildCustomLabels.keySet()).toArray(new String[0]))
            .register(registry)

        buildSummary
            .labels(
                ([hostName,
                result.gradle.startParameter.taskNames.toString(),
                result.failure == null ? 'SUCCESS' : 'FAILURE'] + reporterExtension.buildCustomLabels.values()).toArray(new String[0]))
            .set(timings.finishMillis - timings.startMillis)

        def taskSummary = Gauge.build()
            .name('build_task_timing')
            .help('Specific task build time in millis')
            .labelNames ((['instance', 'path', 'name', 'status'] + reporterExtension.taskCustomLabels.keySet()).toArray(new String[0]))
            .register(registry)

        timings.taskTimingMap.values()
            .stream()
            .forEach { tt ->
            taskSummary
                .labels(([hostName, tt.path, tt.name, getState(tt.state)] + reporterExtension.taskCustomLabels.values()).toArray(new String[0]))
                .set(tt.finishTime - tt.startTime)
        }

        PushGateway pg = new PushGateway(reporterExtension.pushGatewayHost)
        pg.pushAdd(registry, reporterExtension.jobName != null ? reporterExtension.jobName : result.gradle.rootProject.name)

        logger.lifecycle("\nPushed build timing to Prometheus host ${reporterExtension.pushGatewayHost}")
    }
}

class PrometheusReporterExtension extends ReporterExtension<PrometheusReporter> {
    String pushGatewayHost
    String jobName
    Map<String, String> buildCustomLabels = [:]
    Map<String, String> taskCustomLabels = [:]

    PrometheusReporterExtension(String name) {
        super(name)
    }

    @Override
    PrometheusReporter getReporter() {
        return new PrometheusReporter(this)
    }
}

