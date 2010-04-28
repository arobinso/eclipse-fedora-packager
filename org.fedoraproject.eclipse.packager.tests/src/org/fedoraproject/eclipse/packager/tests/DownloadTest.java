package org.fedoraproject.eclipse.packager.tests;

import java.io.FileWriter;
import java.io.PrintWriter;

import org.eclipse.core.resources.IFile;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.fedoraproject.eclipse.packager.cvs.DownloadHandler;

public class DownloadTest extends AbstractTest {
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		handler = new DownloadHandler();
		handler.setDebug(true);
		handler.setResource(branch);
		Shell aShell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		handler.setShell(aShell);
		handler.execute(null);
		handler.waitForJob();
	}

	public void testNoChange() throws Exception {
		handler.execute(null);
		assertTrue(handler.waitForJob().isOK());
		assertNotNull(branch.findMember("ed-1.1.tar.bz2"));
	}

	public void testNeedDownload() throws Exception {
		assertTrue(handler.waitForJob().isOK());
		assertNotNull(branch.findMember("ed-1.1.tar.bz2"));
	}
	
	public void testMD5Mismatch() throws Exception {
		assertTrue(handler.waitForJob().isOK());
		IFile source = (IFile) branch.findMember("ed-1.1.tar.bz2");
		FileWriter out = new FileWriter(source.getLocation().toFile());
		out.write(0x90);
		out.close();
		handler.execute(null);
		assertTrue(handler.waitForJob().isOK());
		assertNotNull(branch.findMember("ed-1.1.tar.bz2"));
	}
	
	public void testBadMD5() throws Exception {
		IFile source = (IFile) branch.findMember("sources");
		PrintWriter out = new PrintWriter(new FileWriter(source.getLocation().toFile()));
		// Incorrect MD5sum
		out.println("a359451fb32097974484b5ba7c19f5fb  ed-1.1.tar.bz2");
		out.close();
		handler.execute(null);
		assertFalse(handler.waitForJob().isOK());
		assertNull(branch.findMember("ed-1.1.tar.bz2"));
	}
}
