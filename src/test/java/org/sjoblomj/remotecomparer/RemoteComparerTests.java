package org.sjoblomj.remotecomparer;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.apache.maven.plugin.MojoExecutionException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import wiremock.org.apache.commons.io.FileUtils;
import wiremock.org.eclipse.jetty.http.HttpStatus;

import java.io.File;
import java.io.IOException;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class RemoteComparerTests {
	private final static int LOCAL_PORT = 23871;
	private final static String LOCAL_FILE0 = "src/test/resources/testfile0.txt";
	private final static String LOCAL_FILE1 = "src/test/resources/testfile1.txt";

	private final static String MOCKED_SERVER = "http://localhost:" + LOCAL_PORT;
	private final static String MOCKED_PATH = "/remote/file";
	private final static String URI = MOCKED_SERVER + MOCKED_PATH;

	private final WireMockServer wireMockServer = new WireMockServer(LOCAL_PORT);

	@BeforeEach
	void setup() {
		wireMockServer.start();
	}

	@AfterEach
	void cleanup() {
		wireMockServer.resetAll();
		wireMockServer.stop();
	}


	@Test // Happy case
	void testFilesAreEqual() throws IOException, MojoExecutionException {
		wireMockServer.stubFor(
			get(MOCKED_PATH)
				.willReturn(aResponse()
					.withBody(FileUtils.readFileToString(new File(LOCAL_FILE0)))
					.withStatus(HttpStatus.OK_200)));

		RemoteComparerBuilder remoteComparerBuilder = new RemoteComparerBuilder()
			.withRemoteFileUri(URI)
			.withLocalFilePath(LOCAL_FILE0)
			.withFailOnFileDifference(true);

		execute(remoteComparerBuilder); // Test that no exception is thrown
	}

	@Test
	void testFilesAreNotEqual() throws IOException, MojoExecutionException {
		wireMockServer.stubFor(
			get(MOCKED_PATH)
				.willReturn(aResponse()
					.withBody(FileUtils.readFileToString(new File(LOCAL_FILE1)))
					.withStatus(HttpStatus.OK_200)));

		RemoteComparerBuilder remoteComparerBuilder = new RemoteComparerBuilder()
			.withRemoteFileUri(URI)
			.withLocalFilePath(LOCAL_FILE0)
			.withFailOnFileDifference(true)
			.withSmallWarningMessage(false);

		assertThrows(MojoExecutionException.class, () -> execute(remoteComparerBuilder));

		remoteComparerBuilder.withFailOnFileDifference(false).withSmallWarningMessage(true);
		execute(remoteComparerBuilder); // Test that no exception is thrown
	}

	@Test
	void testDoesNotAllowLocalOrRemoteFileToBeNull() {
		assertThrows(MojoExecutionException.class, () -> execute(new RemoteComparerBuilder().withRemoteFileUri("https://example.com")));
		assertThrows(MojoExecutionException.class, () -> execute(new RemoteComparerBuilder().withLocalFilePath(LOCAL_FILE0)));
	}

	@Test
	void testFailOnFilesNotFound() throws MojoExecutionException {
		RemoteComparerBuilder remoteComparerBuilder = new RemoteComparerBuilder()
			.withRemoteFileUri(URI)
			.withLocalFilePath("file/does/not/exist")
			.withFailOnFilesNotFound(true);

		assertThrows(MojoExecutionException.class, () -> execute(remoteComparerBuilder));

		remoteComparerBuilder.withFailOnFilesNotFound(false);
		execute(remoteComparerBuilder); // Test that no exception is thrown
	}

	@Test
	void testDownloadTimesOut() throws IOException, MojoExecutionException {
		wireMockServer.stubFor(
			get(MOCKED_PATH)
				.willReturn(aResponse()
					.withBody(FileUtils.readFileToString(new File(LOCAL_FILE0)))
					.withStatus(HttpStatus.OK_200)
					.withFixedDelay(10_000)));

		RemoteComparerBuilder remoteComparerBuilder = new RemoteComparerBuilder()
			.withRemoteFileUri(URI)
			.withLocalFilePath(LOCAL_FILE0)
			.withFailOnFilesNotFound(true)
			.withTimeoutMs(1);

		assertThrows(MojoExecutionException.class, () -> execute(remoteComparerBuilder));

		remoteComparerBuilder.withFailOnFilesNotFound(false);
		execute(remoteComparerBuilder); // Test that no exception is thrown
	}

	@Test
	void testRemoteServerGivesError() throws MojoExecutionException {
		wireMockServer.stubFor(
			get(MOCKED_PATH)
				.willReturn(aResponse()
					.withStatus(HttpStatus.INTERNAL_SERVER_ERROR_500)));

		RemoteComparerBuilder remoteComparerBuilder = new RemoteComparerBuilder()
			.withRemoteFileUri(URI)
			.withLocalFilePath(LOCAL_FILE0)
			.withFailOnFilesNotFound(true)
			.withTimeoutMs(1000);

		assertThrows(MojoExecutionException.class, () -> execute(remoteComparerBuilder));

		remoteComparerBuilder.withFailOnFilesNotFound(false);
		execute(remoteComparerBuilder); // Test that no exception is thrown
	}


	private void execute(RemoteComparerBuilder remoteComparerBuilder) throws MojoExecutionException {
		remoteComparerBuilder.build().execute();
	}
}
