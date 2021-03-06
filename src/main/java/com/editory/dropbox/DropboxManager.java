package com.editory.dropbox;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import org.springframework.web.multipart.MultipartFile;

import com.dropbox.core.DbxException;
import com.dropbox.core.NetworkIOException;
import com.dropbox.core.RetryException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.CommitInfo;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.UploadErrorException;
import com.dropbox.core.v2.files.UploadSessionCursor;
import com.dropbox.core.v2.files.UploadSessionFinishErrorException;
import com.dropbox.core.v2.files.UploadSessionLookupErrorException;
import com.dropbox.core.v2.files.WriteMode;

import groovyjarjarantlr.StringUtils;

/**
 * This class manages the access, downloading and uploading files to Dropbox as
 * well as authentication
 * 
 * @author Editory
 *
 */
public class DropboxManager {

	/*
	 * Adjust the chunk size based on your network speed and reliability. Larger
	 * chunk sizes will result in fewer network requests, which will be faster.
	 * But if an error occurs, the entire chunk will be lost and have to be
	 * re-uploaded. Use a multiple of 4MiB for your chunk size.
	 */

	private static final long CHUNKED_UPLOAD_CHUNK_SIZE = 8L << 20; // 8MiB
	private static final int CHUNKED_UPLOAD_MAX_ATTEMPTS = 5;

	public void uploadFile(DbxClientV2 dbxClient, File localFile, String dropboxPath) {
        try (InputStream in = new FileInputStream(localFile)) {
            FileMetadata metadata = dbxClient.files().uploadBuilder(dropboxPath)
                .withMode(WriteMode.ADD)
                .withClientModified(new Date(localFile.lastModified()))
                .uploadAndFinish(in);

            System.out.println(metadata.toStringMultiline());
        } catch (UploadErrorException ex) {
            System.err.println("Error uploading to Dropbox: " + ex.getMessage());
            //System.exit(1);
        } catch (DbxException ex) {
            System.err.println("Error uploading to Dropbox: " + ex.getMessage());
            //System.exit(1);
        } catch (IOException ex) {
            System.err.println("Error reading from file \"" + localFile + "\": " + ex.getMessage());
           // System.exit(1);
        }
    }

	
	
	
	/**
	 * Método que sube
	 * 
	 * @param dbxClient
	 * @param localFile
	 * @param dropboxPath
	 */
	public void uploadFile(DbxClientV2 dbxClient, MultipartFile localFile, String dropboxPath) {
		try (InputStream in = localFile.getInputStream()) {
			FileMetadata metadata = dbxClient.files().uploadBuilder(dropboxPath).withMode(WriteMode.ADD)
					.withClientModified(new Date()).uploadAndFinish(in);

			System.out.println(metadata.toStringMultiline());
		} catch (UploadErrorException ex) {
			System.err.println("Error uploading to Dropbox: " + ex.getMessage());
			//System.exit(1);
		} catch (DbxException ex) {
			System.err.println("Error uploading to Dropbox: " + ex.getMessage());
			//System.exit(1);
		} catch (IOException ex) {
			System.err.println("Error reading from file \"" + localFile + "\": " + ex.getMessage());
			//System.exit(1);
		}
	}

	/**
	 * Uploads a file in chunks using multiple requests. This approach is
	 * preferred for larger files since it allows for more efficient processing
	 * of the file contents on the server side and also allows partial uploads
	 * to be retried (e.g. network connection problem will not cause you to
	 * re-upload all the bytes).
	 *
	 * @param dbxClient
	 *            Dropbox user authenticated client
	 * @param localFIle
	 *            local file to upload
	 * @param dropboxPath
	 *            Where to upload the file to within Dropbox
	 */
	public void chunkedUploadFile(DbxClientV2 dbxClient, MultipartFile localFile, String dropboxPath) {
		long size = localFile.getSize();

		// assert our file is at least the chunk upload size. We make this
		// assumption in the code
		// below to simplify the logic.
		if (size < CHUNKED_UPLOAD_CHUNK_SIZE) {
			this.uploadFile(dbxClient, localFile, dropboxPath);

		} else {

			long uploaded = 0L;
			DbxException thrown = null;

			// Chunked uploads have 3 phases, each of which can accept uploaded
			// bytes:
			//
			// (1) Start: initiate the upload and get an upload session ID
			// (2) Append: upload chunks of the file to append to our session
			// (3) Finish: commit the upload and close the session
			//
			// We track how many bytes we uploaded to determine which phase we
			// should be in.
			String sessionId = null;
			for (int i = 0; i < CHUNKED_UPLOAD_MAX_ATTEMPTS; ++i) {
				if (i > 0) {
					System.out.printf("Retrying chunked upload (%d / %d attempts)\n", i + 1,
							CHUNKED_UPLOAD_MAX_ATTEMPTS);
				}

				try (InputStream in = localFile.getInputStream()) {
					// if this is a retry, make sure seek to the correct offset
					in.skip(uploaded);

					// (1) Start
					if (sessionId == null) {
						sessionId = dbxClient.files().uploadSessionStart()
								.uploadAndFinish(in, CHUNKED_UPLOAD_CHUNK_SIZE).getSessionId();
						uploaded += CHUNKED_UPLOAD_CHUNK_SIZE;
						printProgress(uploaded, size);
					}

					UploadSessionCursor cursor = new UploadSessionCursor(sessionId, uploaded);

					// (2) Append
					while ((size - uploaded) > CHUNKED_UPLOAD_CHUNK_SIZE) {
						dbxClient.files().uploadSessionAppendV2(cursor).uploadAndFinish(in, CHUNKED_UPLOAD_CHUNK_SIZE);
						uploaded += CHUNKED_UPLOAD_CHUNK_SIZE;
						printProgress(uploaded, size);
						cursor = new UploadSessionCursor(sessionId, uploaded);
					}

					// (3) Finish
					long remaining = size - uploaded;
					CommitInfo commitInfo = CommitInfo.newBuilder(dropboxPath).withMode(WriteMode.ADD)
							.withClientModified(new Date()).build();
					FileMetadata metadata = dbxClient.files().uploadSessionFinish(cursor, commitInfo)
							.uploadAndFinish(in, remaining);

					System.out.println(metadata.toStringMultiline());
					return;
				} catch (RetryException ex) {
					thrown = ex;
					// RetryExceptions are never automatically retried by the
					// client for uploads. Must
					// catch this exception even if
					// DbxRequestConfig.getMaxRetries() > 0.
					sleepQuietly(ex.getBackoffMillis());
					continue;
				} catch (NetworkIOException ex) {
					thrown = ex;
					// network issue with Dropbox (maybe a timeout?) try again
					continue;
				} catch (UploadSessionLookupErrorException ex) {
					if (ex.errorValue.isIncorrectOffset()) {
						thrown = ex;
						// server offset into the stream doesn't match our
						// offset (uploaded). Seek to
						// the expected offset according to the server and try
						// again.
						uploaded = ex.errorValue.getIncorrectOffsetValue().getCorrectOffset();
						continue;
					} else {
						// Some other error occurred, give up.
						System.err.println("Error uploading to Dropbox: " + ex.getMessage());
						//System.exit(1);
						return;
					}
				} catch (UploadSessionFinishErrorException ex) {
					if (ex.errorValue.isLookupFailed() && ex.errorValue.getLookupFailedValue().isIncorrectOffset()) {
						thrown = ex;
						// server offset into the stream doesn't match our
						// offset (uploaded). Seek to
						// the expected offset according to the server and try
						// again.
						uploaded = ex.errorValue.getLookupFailedValue().getIncorrectOffsetValue().getCorrectOffset();
						continue;
					} else {
						// some other error occurred, give up.
						System.err.println("Error uploading to Dropbox: " + ex.getMessage());
						//System.exit(1);
						return;
					}
				} catch (DbxException ex) {
					System.err.println("Error uploading to Dropbox: " + ex.getMessage());
					//System.exit(1);
					return;
				} catch (IOException ex) {
					System.err.println("Error reading from file \"" + localFile + "\": " + ex.getMessage());
					//System.exit(1);
					return;
				}
			}

			// if we made it here, then we must have run out of attempts
			System.err.println("Maxed out upload attempts to Dropbox. Most recent error: " + thrown.getMessage());
			//System.exit(1);
		}
	}
	
	public static String linkCarpeta(final String str){
		
		String a = str.replaceAll(" ", "%20");
		String b = a.replaceAll(":", "%3A");
		return "https://www.dropbox.com/home/"+b;
	}

	private static void printProgress(long uploaded, long size) {
		System.out.printf("Uploaded %12d / %12d bytes (%5.2f%%)\n", uploaded, size, 100 * (uploaded / (double) size));
	}

	private static void sleepQuietly(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException ex) {
			// just exit
			System.err.println("Error uploading to Dropbox: interrupted during backoff.");
			System.exit(1);
		}
	}
}
