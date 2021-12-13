package org.sjoblomj.remotecomparer;

class RemoteComparerBuilder {

	private String localFilePath;
	private String remoteFileUri;
	private int timeoutMs = 10_000;
	private boolean failOnFileDifference = false;
	private boolean failOnFilesNotFound = false;
	private boolean smallWarningMessage = false;

	RemoteComparerBuilder withLocalFilePath(String localFilePath) {
		this.localFilePath = localFilePath;
		return this;
	}

	RemoteComparerBuilder withRemoteFileUri(String remoteFileUri) {
		this.remoteFileUri = remoteFileUri;
		return this;
	}

	RemoteComparerBuilder withTimeoutMs(int timeoutMs) {
		this.timeoutMs = timeoutMs;
		return this;
	}

	RemoteComparerBuilder withFailOnFileDifference(boolean failOnFileDifference) {
		this.failOnFileDifference = failOnFileDifference;
		return this;
	}

	RemoteComparerBuilder withFailOnFilesNotFound(boolean failOnFilesNotFound) {
		this.failOnFilesNotFound = failOnFilesNotFound;
		return this;
	}

	RemoteComparerBuilder withSmallWarningMessage(boolean smallWarningMessage) {
		this.smallWarningMessage = smallWarningMessage;
		return this;
	}

	RemoteComparer build() {
		return new RemoteComparer(localFilePath, remoteFileUri, timeoutMs, failOnFileDifference, failOnFilesNotFound,
			smallWarningMessage);
	}
}
