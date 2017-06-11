package org.jenkinsci.plugins.pipeline.maven.publishers;

import org.hamcrest.CoreMatchers;
import org.jenkinsci.plugins.pipeline.maven.MavenSpyLogProcessor;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.util.List;

/**
 * @author <a href="mailto:cleclerc@cloudbees.com">Cyrille Le Clerc</a>
 */
public class DependenciesFingerprintPublisherTest {
    Document doc;
    DependenciesFingerprintPublisher dependenciesFingerprintPublisher = new DependenciesFingerprintPublisher();

    @Before
    public void before() throws Exception {
        String mavenSpyLogs = "org/jenkinsci/plugins/pipeline/maven/maven-spy.xml";
        InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(mavenSpyLogs);
        doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(in);
    }

    @Test
    public void listArtifactDependencies() throws Exception {
        List<MavenSpyLogProcessor.MavenDependency> mavenArtifacts = dependenciesFingerprintPublisher.listDependencies(doc.getDocumentElement());
        System.out.println(mavenArtifacts);
        Assert.assertThat(mavenArtifacts.size(), CoreMatchers.is(2));

        MavenSpyLogProcessor.MavenArtifact dependencyArtifact = mavenArtifacts.get(0);
        Assert.assertThat(dependencyArtifact.artifactId, CoreMatchers.is("spring-test"));
        Assert.assertThat(dependencyArtifact.file, CoreMatchers.is("/path/to/spring-petclinic/spring-test/3.2.16.RELEASE/spring-test-3.2.16.RELEASE.jar"));

        dependencyArtifact = mavenArtifacts.get(1);
        Assert.assertThat(dependencyArtifact.artifactId, CoreMatchers.is("spring-core"));
        Assert.assertThat(dependencyArtifact.file, CoreMatchers.is("/path/to/spring-petclinic/3.2.16.RELEASE/spring-core-3.2.16.RELEASE.jar"));
    }
}
