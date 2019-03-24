package anverus.tools.gradle.timer

import org.gradle.testfixtures.ProjectBuilder
import org.gradle.util.ClosureBackedAction
import spock.lang.Specification

class BuildTimingPluginTest extends Specification {

    def "extension is added to project"() {
        setup:
        def project = ProjectBuilder.builder().build()
        project.pluginManager.apply 'anverus.tools.timer'

        expect:
        project.extensions.buildtiming instanceof BuildTimingPluginExtension
    }

/*
    def "test default plugin enabled"() {
        setup:
        def project = ProjectBuilder.builder().build()
        project.pluginManager.apply 'anverus.tools.timer'

        when:
        def enabled = project.extensions.buildtiming.enabled

        then:
        enabled == true
    }

    def "test disabled plugin"() {
        setup:
        def project = ProjectBuilder.builder().build()
        project.pluginManager.apply 'anverus.tools.timer'

        when:
        project.extensions.configure(BuildTimingPluginExtension, new ClosureBackedAction<BuildTimingPluginExtension>(
        {
            enabled = false
        }))

        BuildTimingPluginExtension ext = project.extensions.buildtiming

        then:
        ext.enabled == false
    }
*/
    def "test wrong reporter config"() {
        setup:
        def project = ProjectBuilder.builder().build()
        project.pluginManager.apply 'anverus.tools.timer'

        when:
        project.extensions.configure(BuildTimingPluginExtension,
            new ClosureBackedAction<BuildTimingPluginExtension>(
            {
                reporters {
                    undefinedReporter {

                    }
                }
            })
        )

        BuildTimingPluginExtension ext = project.extensions.buildtiming

        then:
        thrown NullPointerException
    }
}