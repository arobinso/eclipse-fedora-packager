/*******************************************************************************
 * Copyright (c) 2010 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *  
 * Contributors:
 *      Red Hat - initial API and implementation
 *******************************************************************************/
package org.fedoraproject.eclipse.packager;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;

/**
 * Class responsible for Upload/Download Url management of the
 * lookaside cache.
 *
 */
public class LookasideCache {
	
	private String uploadUrl;	// Lookaside upload URL
	private String downloadUrl;	// Lookaside download URL
	private IPreferenceStore prefStore; // Preferences store to use
	
	/**
	 * Common command prefix for Eclipse Fedorapackager command IDs.
	 */
	public static final String FEDORA_PACKAGER_CMD_PREFIX = "org.fedoraproject.eclipse.packager"; //$NON-NLS-1$
	
	/**
	 * Create lookaside cache object for the provided Eclipse command.
	 * 
	 * @param commandId The ID of the command for which a LookasideCache object
	 * 					should be created.
	 */
	public LookasideCache(String commandId) {
		this.prefStore = PackagerPlugin.getDefault().getPreferenceStore();
		if (commandId.startsWith(LookasideCache.FEDORA_PACKAGER_CMD_PREFIX)) {
			// We want to listen for lookaside preference changes
			final IPropertyChangeListener propertyChangeListener = new IPropertyChangeListener() {				
				@Override
				public void propertyChange(PropertyChangeEvent event) {
					if (event.getProperty().equals(org.fedoraproject.eclipse.packager.preferences.PreferencesConstants.PREF_LOOKASIDE_DOWNLOAD_URL)) {
						// update download URL
						setDownloadUrl();
					}
					if (event.getProperty().equals(org.fedoraproject.eclipse.packager.preferences.PreferencesConstants.PREF_LOOKASIDE_UPLOAD_URL)) {
						// update upload URL
						setUploadUrl();
					}
				}
			};
			// attach listener
			this.prefStore.addPropertyChangeListener(propertyChangeListener);
			// Set the download URL
			setDownloadUrl();
			// Set the upload URL
			setUploadUrl();
		}
	}

	/**
	 * @return the downloadUrl
	 */
	public String getDownloadUrl() {
		return downloadUrl;
	}

	/**
	 * Set the downloadUrl. Uses default URL if empty.
	 */
	private void setDownloadUrl() {
		String downloadUrl = this.prefStore.getString(org.fedoraproject.eclipse.packager.preferences.PreferencesConstants.PREF_LOOKASIDE_DOWNLOAD_URL);
		// If download URL isn't set yet, use default URL
		if (downloadUrl.equals("")) { //$NON-NLS-1$
			downloadUrl = org.fedoraproject.eclipse.packager.preferences.PreferencesConstants.DEFAULT_LOOKASIDE_DOWNLOAD_URL;
		}
		this.downloadUrl = downloadUrl;
	}

	/**
	 * @return the uploadUrl
	 */
	public String getUploadUrl() {
		return uploadUrl;
	}

	/**
	 * Set the uploadUrl. Uses default URL if empty.
	 */
	private void setUploadUrl() {
		String uploadUrl = this.prefStore.getString(org.fedoraproject.eclipse.packager.preferences.PreferencesConstants.PREF_LOOKASIDE_UPLOAD_URL);
		// If upload URL isn't set yet, use default URL
		if (uploadUrl.equals("")) { //$NON-NLS-1$
			uploadUrl = org.fedoraproject.eclipse.packager.preferences.PreferencesConstants.DEFAULT_LOOKASIDE_UPLOAD_URL;
		}
		this.uploadUrl = uploadUrl;
	}
}
