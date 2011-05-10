package org.fedoraproject.eclipse.packager.tests.commands;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.fedoraproject.eclipse.packager.FedoraProjectRoot;
import org.fedoraproject.eclipse.packager.api.DownloadSourceCommand;
import org.fedoraproject.eclipse.packager.api.FedoraPackager;
import org.fedoraproject.eclipse.packager.api.errors.CommandMisconfiguredException;
import org.fedoraproject.eclipse.packager.rpm.api.RpmBuildCommand;
import org.fedoraproject.eclipse.packager.rpm.api.RpmBuildCommand.BuildType;
import org.fedoraproject.eclipse.packager.rpm.api.RpmBuildResult;
import org.fedoraproject.eclipse.packager.rpm.api.RpmEvalCommand;
import org.fedoraproject.eclipse.packager.tests.utils.git.GitTestProject;
import org.fedoraproject.eclipse.packager.utils.FedoraPackagerUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the RPM build command. This includes source RPM and
 * prep tests.
 *
 */
public class RpmBuildCommandTest {

	// project under test
	private GitTestProject testProject;
	// main interface class
	private FedoraPackager packager;
	// Fedora packager root
	private FedoraProjectRoot fpRoot;
	
	/**
	 * Clone a test project to be used for testing.
	 * 
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		this.testProject = new GitTestProject("eclipse-fedorapackager");
		this.fpRoot = FedoraPackagerUtils.getProjectRoot((this.testProject
				.getProject()));
		this.packager = new FedoraPackager(fpRoot);
		// need to have sources ready
		DownloadSourceCommand download = (DownloadSourceCommand) packager
				.getCommandInstance(DownloadSourceCommand.ID);
		download.call(new NullProgressMonitor());
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
		this.testProject.dispose();
	}

	/**
	 * Test method for 
	 * {@link org.fedoraproject.eclipse.packager.rpm.api.RpmBuildCommand#checkConfiguration()}.
	 */
	@Test
	public void testCheckConfiguration() throws Exception {
		RpmBuildCommand build = (RpmBuildCommand) packager
				.getCommandInstance(RpmBuildCommand.ID);
		try {
			build.call(new NullProgressMonitor());
			fail("Should have thrown an exception. Command is not properly configured.");
		} catch (CommandMisconfiguredException e) {
			// pass
		}
	}

	/**
	 *  This illustrates proper usage of {@link RpmEvalCommand}. This may
	 *  take a long time.
	 */
	@Test
	public void canBuildForLocalArchitecture() throws Exception {
		RpmBuildCommand build = (RpmBuildCommand) packager
				.getCommandInstance(RpmBuildCommand.ID);
		RpmBuildResult result;
		List<String> distDefines = new ArrayList<String>();
		distDefines.add("--define"); //$NON-NLS-1$
		distDefines.add("dist .fc15"); //$NON-NLS-1$
		distDefines.add("--define"); //$NON-NLS-1$
		distDefines.add("fedora 15");
		build.buildType(BuildType.BINARY).distDefines(distDefines);
		try {
			result = build.call(new NullProgressMonitor());
		} catch (Exception e) {
			fail("Shouldn't have thrown any exception.");
			return;
		}
		assertTrue(result.wasSuccessful());
		IResource noArchFolder = fpRoot.getContainer().findMember(new Path("noarch"));
		assertNotNull(noArchFolder);
		// there should be one RPM
		assertTrue(((IContainer)noArchFolder).members().length == 1);
	}
	
	/**
	 * Test preparing sources.
	 */
	@Test
	public void canPrepareSources() throws Exception {
		RpmBuildCommand build = (RpmBuildCommand) packager
				.getCommandInstance(RpmBuildCommand.ID);
		List<String> nodeps = new ArrayList<String>(1);
		nodeps.add(RpmBuildCommand.NO_DEPS);
		RpmBuildResult result;
		try {
			result = build.buildType(BuildType.PREP).flags(nodeps)
					.call(new NullProgressMonitor());
		} catch (Exception e) {
			fail("Shouldn't have thrown any exception.");
			return;
		}
		assertTrue(result.wasSuccessful());
		IResource expandedSourcesFolder = fpRoot.getContainer().findMember(new Path("eclipse-fedorapackager"));
		assertNotNull(expandedSourcesFolder);
		// there should be some files in that folder
		assertTrue(((IContainer)expandedSourcesFolder).members().length > 0);
		// put some confidence into returned result
		assertTrue(result.getBuildCommand().contains(RpmBuildCommand.NO_DEPS));
	}
	
	/**
	 * Test create SRPM.
	 */
	@Test
	public void canCreateSRPM() throws Exception {
		RpmBuildCommand build = (RpmBuildCommand) packager
				.getCommandInstance(RpmBuildCommand.ID);
		List<String> nodeps = new ArrayList<String>(1);
		nodeps.add(RpmBuildCommand.NO_DEPS);
		RpmBuildResult result;
		try {
			result = build.buildType(BuildType.SOURCE).flags(nodeps)
					.call(new NullProgressMonitor());
		} catch (Exception e) {
			fail("Shouldn't have thrown any exception.");
			return;
		}
		assertTrue(result.wasSuccessful());
		// should contain at least one SRPM
		boolean found = false;
		for(IResource res: fpRoot.getContainer().members()) {
			if (res.getName().contains("src.rpm")) {
				found = true;
			}
		}
		assertTrue(found);
	}
}
