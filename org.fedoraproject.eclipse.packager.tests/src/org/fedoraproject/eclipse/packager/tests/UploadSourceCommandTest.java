package org.fedoraproject.eclipse.packager.tests;


import static org.junit.Assert.fail;

import java.io.File;
import java.net.MalformedURLException;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.fedoraproject.eclipse.packager.FedoraProjectRoot;
import org.fedoraproject.eclipse.packager.api.FedoraPackager;
import org.fedoraproject.eclipse.packager.api.UploadSourceCommand;
import org.fedoraproject.eclipse.packager.api.errors.FileMissingFromLookasideCacheException;
import org.fedoraproject.eclipse.packager.tests.git.utils.GitTestProject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Eclipse plug-in test for UploadSourceCommand.
 */
public class UploadSourceCommandTest {

	// project under test
	private GitTestProject testProject;
	// main interface class
	private FedoraPackager packager;
	private static final String exampleUploadFile =
		"resources/callgraph-factorial.zip"; // $NON-NLS$
	private static final String LOOKASIDE_CACHE_URL_FOR_TESTING =
		"http://upload-cgi.yyz.redhat.com/cgi-bin/upload.cgi"; //$NON-NLS$
	
	/**
	 * Set up a Fedora project and run the command.
	 * 
	 * @throws Exception
	 */
	@Before
	public void setUp() throws Exception {
		this.testProject = new GitTestProject("eclipse-fedorapackager");
		FedoraProjectRoot fpRoot = new FedoraProjectRoot(this.testProject.getProject(), "ignore");
		this.packager = new FedoraPackager(fpRoot);
	}

	@After
	public void tearDown() throws Exception {
		this.testProject.dispose();
	}
	
	@Test
	public void shouldThrowMalformedURLException() throws Exception {
		UploadSourceCommand uploadCmd = packager.uploadSources();
		try {
			uploadCmd.setUploadURL("bad bad url");
			fail("UploadSourceCommand.setUploadURL should not accept invalid URLs!");
		} catch (MalformedURLException e) {
			// pass
		}
	}

	/**
	 * Uploading sources in Fedora entails two requests. First a POST is fired
	 * with filename and MD5 as parameters and the server returns if the
	 * resource is "missing" or "available". If and only if the server responded
	 * with "missing" a second request is created to actually upload the missing
	 * file. This test checks for the first part of this transaction.
	 * 
	 * @throws Exception
	 */
	@Test
	public void canDetermineIfSourceIsAvailable() throws Exception {
		UploadSourceCommand uploadCmd = packager.uploadSources();
		String fileName = FileLocator.toFileURL(
				FileLocator.find(TestsPlugin.getDefault().getBundle(),
						new Path(exampleUploadFile), null)).getFile();
		File file = new File(fileName);
		try {
			uploadCmd.setUploadURL(LOOKASIDE_CACHE_URL_FOR_TESTING)
				.setFileToUpload(file).call();
			// should be missing
			fail("File should not be present in lookaside cache.");
		} catch (FileMissingFromLookasideCacheException e) {
			//pass
		}
	}
	
	@Test
	public void canUploadSources() throws Exception {
		UploadSourceCommand uploadCmd = packager.uploadSources();
		String fileName = FileLocator.toFileURL(
				FileLocator.find(TestsPlugin.getDefault().getBundle(),
						new Path(exampleUploadFile), null)).getFile();
		File file = new File(fileName);
		try {
		uploadCmd.setUploadURL(LOOKASIDE_CACHE_URL_FOR_TESTING).setFileToUpload(file)
				.call();
		} catch (FileMissingFromLookasideCacheException e) {
			//pass
		}
	}

}
