package io.questdb.maven.rust;

import org.apache.maven.model.Build;
import org.apache.maven.project.MavenProject;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;

public class CargoMojoBaseTest {

    @Rule
    public TemporaryFolder tmpDir = new TemporaryFolder();

    static class TestMojo extends CargoMojoBase {
        public Path callGetTargetRootDir() {
            return getTargetRootDir();
        }

        @Override
        public void execute() {
            // no-op for tests
        }
    }

    @Test
    public void testGetTargetRootDirDefault() throws Exception {
        TestMojo mojo = new TestMojo();

        MavenProject project = new MavenProject();
        Build build = new Build();
        build.setDirectory("target-dir");
        project.setBuild(build);
        mojo.project = project;

        Path expected = Paths.get("target-dir", "rust-maven-plugin");
        assertEquals(expected.toAbsolutePath().normalize(), mojo.callGetTargetRootDir().toAbsolutePath().normalize());
    }

    @Test
    public void testGetTargetRootDirConfiguredAbsolute() throws Exception {
        TestMojo mojo = new TestMojo();

        MavenProject project = new MavenProject();
        Build build = new Build();
        build.setDirectory("irrelevant");
        project.setBuild(build);
        mojo.project = project;

        String configured = tmpDir.newFolder("mycustom").getAbsolutePath();

        Field f = CargoMojoBase.class.getDeclaredField("targetRootDir");
        f.setAccessible(true);
        f.set(mojo, configured);

        Path expected = Paths.get(configured);
        assertEquals(expected.toAbsolutePath().normalize(), mojo.callGetTargetRootDir().toAbsolutePath().normalize());
    }

    @Test
    public void testGetTargetRootDirConfiguredRelative() throws Exception {
        TestMojo mojo = new TestMojo();

        MavenProject project = new MavenProject();
        Build build = new Build();
        build.setDirectory("irrelevant");
        project.setBuild(build);
        File base = tmpDir.newFolder("basedir");
        project.setFile(new File(base, "pom.xml"));
        mojo.project = project;

        String configured = "shortdir";

        Field f = CargoMojoBase.class.getDeclaredField("targetRootDir");
        f.setAccessible(true);
        f.set(mojo, configured);

        Path expected = base.toPath().resolve(configured);
        assertEquals(expected.toAbsolutePath().normalize(), mojo.callGetTargetRootDir().toAbsolutePath().normalize());
    }
}
