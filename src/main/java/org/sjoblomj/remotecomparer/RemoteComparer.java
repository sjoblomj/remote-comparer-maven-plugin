package org.sjoblomj.remotecomparer;

import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Mojo(name = "remote-compare", defaultPhase = LifecyclePhase.VALIDATE, threadSafe = true)
public class RemoteComparer extends AbstractMojo {

	/**
	 * Path to a local file to be compared.
	 */
	@Parameter(required = true, property = "localFilePath")
	private String localFilePath;

	/**
	 * Uri to a remote file to be compared.
	 */
	@Parameter(required = true, property = "remoteFileUri")
	private String remoteFileUri;

	/**
	 * Timeout in milliseconds for fetching the remote file. A timeout of 0 is interpreted as infinite timeout.
	 */
	@Parameter(property = "timeoutMs")
	private int timeoutMs = 10_000;

	/**
	 * If set to true, the build will fail if the files differ (or if errors arise when checking if they are different).
	 * If set to false (default), the build will NOT fail but simply produce a warning.
	 */
	@Parameter(property = "failOnFileDifference")
	private boolean failOnFileDifference = false;

	/**
	 * If set to true, the build will fail if the local file can't be found or the remote file can't be downloaded.
	 * If set to false (default), the build will NOT fail but simply produce a warning.
	 */
	@Parameter(property = "failOnFilesNotFound")
	private boolean failOnFilesNotFound = false;

	/**
	 * By default, the warning message given if the local and remote files differ is large. Set this to true to instead
	 * print warning message that only contain the necessary information.
	 */
	@Parameter(property = "smallWarningMessage")
	private boolean smallWarningMessage = false;

	@Parameter(defaultValue = "${project}", required = true, readonly = true)
	MavenProject project;


	@SuppressWarnings("unused")
	public RemoteComparer() {
		// Zero-argument constructor must be present.
	}

	public RemoteComparer(String localFilePath, String remoteFileUri, int timeoutMs, boolean failOnFileDifference,
												boolean failOnFilesNotFound, boolean smallWarningMessage) {
		this.localFilePath = localFilePath;
		this.remoteFileUri = remoteFileUri;
		this.timeoutMs = timeoutMs;
		this.failOnFileDifference = failOnFileDifference;
		this.failOnFilesNotFound = failOnFilesNotFound;
		this.smallWarningMessage = smallWarningMessage;
	}


	@Override
	public void execute() throws MojoExecutionException {
		if (localFilePath == null)
			throw new MojoExecutionException("localFilePath must be specified!");
		if (remoteFileUri == null)
			throw new MojoExecutionException("remoteFileUri must be specified!");

		File localFile = getLocalFile();
		if (localFile != null) {
			File remoteFileTempLocalFile = new File("target/" + UUID.randomUUID());
			remoteFileTempLocalFile.deleteOnExit();
			if (tryDownloadingFile(remoteFileTempLocalFile)) {
				compareFiles(localFile, remoteFileTempLocalFile);
			}
		}
	}

	private File getLocalFile() throws MojoExecutionException {
		File l0 = new File(localFilePath);
		File l1 = project != null ? new File(project.getArtifactId(), localFilePath) : null;

		if (l0.isFile())
			return l0;
		else if (l1 != null && l1.isFile())
			return l1;
		else {
			String message = "The local file '" + localFilePath + "' could not be found. Looked here: '" +
				l0.getAbsolutePath() + "'" + (l1 != null ? "\n'" + l1.getAbsolutePath() + "'" : "");
			if (failOnFilesNotFound)
				throw new MojoExecutionException(message);
			else {
				getLog().error(message);
				return null;
			}
		}
	}

	private boolean tryDownloadingFile(File remoteFileTempLocalFile) throws MojoExecutionException {
		try {
			FileUtils.copyURLToFile(new URL(remoteFileUri), remoteFileTempLocalFile, timeoutMs, timeoutMs);

		} catch (IllegalArgumentException e) {
			throw e;
		} catch (Exception e) {
			String message = "Failed to download file '" + remoteFileUri + "': " + e.getMessage();
			if (failOnFilesNotFound)
				throw new MojoExecutionException(message);
			else {
				getLog().warn(message);
				return false;
			}
		}
		return true;
	}

	private void compareFiles(File localFile, File remoteFile) throws MojoExecutionException {
		try {
			if (FileUtils.contentEqualsIgnoreEOL(localFile, remoteFile, StandardCharsets.UTF_8.name())) {
				String message = "The file '" + localFilePath + "' is equal to the remote file '" + remoteFileUri + "'";
				getLog().info(message);
			} else {

				String message = "The file '" + localFilePath + "' is not equal to the remote file '" + remoteFileUri + "'";
				if (failOnFileDifference) {
					throw new MojoExecutionException(message);
				} else {

					if (!smallWarningMessage)
						getLog().warn("################################");

					getLog().warn(message);

					if (!smallWarningMessage)
						getLog().warn("################################");
				}
			}
		} catch (IOException e) {
			String message = "Exception when checking if files are equal: " + e.getMessage();
			if (failOnFileDifference) {
				throw new MojoExecutionException(message);
			} else {
				getLog().warn(message);
			}
		}
	}
}
