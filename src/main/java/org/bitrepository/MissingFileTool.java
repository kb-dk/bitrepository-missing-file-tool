package org.bitrepository;

import org.bitrepository.access.getfile.GetFileClient;
import org.bitrepository.bitrepositoryelements.ChecksumDataForFileTYPE;
import org.bitrepository.eventhandler.GetFileEventHandler;
import org.bitrepository.eventhandler.PutFileEventHandler;
import org.bitrepository.modify.putfile.PutFileClient;
import org.bitrepository.protocol.FileExchange;
import org.bitrepository.util.BitmagUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.UUID;

public class MissingFileTool {
    private static final Logger log = LoggerFactory.getLogger(MissingFileTool.class);
    private final FileExchange fileExchange;
    private final String tempExchangeFileID; // TODO: Is this even necessary? Spørg Kim om FileExchange/DAV ikke er ligeglad med, om filnavn matcher et andet/om der kan gå kludder i det.
    private final URL exchangeUrlForFile;

    public MissingFileTool(String fileID) throws MalformedURLException {
        fileExchange = BitmagUtils.getFileExchange();
        tempExchangeFileID = fileID + UUID.randomUUID();
        exchangeUrlForFile = fileExchange.getURL(tempExchangeFileID);
    }

    /**
     * Attempt to repair the missing file by getting the file from a pillar that has it,
     * followed by making a put of the file to all pillars. Cleans up file-exchange when done.
     * @param fileID ID of the file to repair
     * @param checksum Checksum of file
     * @param collectionID The collection of the file
     */
    public void repairMissingFile(String fileID, String checksum, String collectionID) {
        File file = getFile(fileID, collectionID);
        if (file != null) {
            putFile(file, fileID, checksum, collectionID);
        }
        cleanUpFileExchange();
    }

    /**
     * Attempts to get a file with the given ID.
     * @param fileID ID/name of the file to get
     * @param collectionID Name of the collection to get it from
     * @return The downloaded local file if successful, otherwise null
     */
    public File getFile(String fileID, String collectionID) {
        GetFileClient client = BitmagUtils.getFileClient();
        GetFileEventHandler eventHandler = new GetFileEventHandler();

        try {
            client.getFileFromFastestPillar(collectionID, fileID, null, exchangeUrlForFile, eventHandler,
                    "GetFile from missing-file-tool");
            eventHandler.waitForFinish();

            boolean getIsSuccess = !eventHandler.isOperationSuccess();
            if (getIsSuccess) {
                return getFileFromExchange();
            } else {
                System.out.println("Failed to get file '" + fileID + "'"); // TODO: throw stuff? Should somehow indicate 'fatal' error
            }
        } catch (InterruptedException e) {
            log.error("Got interrupted while waiting for operation to complete");
        }
        return null;
    }

    /**
     * Requests to get the file at URL specificed by {@link #exchangeUrlForFile}
     * and returns the result in a locally created file.
     * @return A local instance of the requested file
     */
    private File getFileFromExchange() {
        File intermediateLocalFile = null;
        try {
            intermediateLocalFile = File.createTempFile(UUID.randomUUID().toString(), "");
        } catch (IOException e) {
            log.error("Can't create intermediate temp file");
        }
        fileExchange.getFile(intermediateLocalFile, exchangeUrlForFile.toExternalForm());
        return intermediateLocalFile;
    }

    public void putFile(File file, String fileID, String checksum, String collectionID) {
        PutFileClient client = BitmagUtils.getPutFileClient();
        PutFileEventHandler eventHandler = new PutFileEventHandler();
        ChecksumDataForFileTYPE checksumData = BitmagUtils.getChecksum(checksum);

        try {
            client.putFile(collectionID, exchangeUrlForFile, fileID, file.length(), checksumData,
                    null, eventHandler, "Put from missing-file-tool");
            eventHandler.waitForFinish();

            // TODO print on operation success/failure
            if (eventHandler.isOperationSuccess()) {
                // stuff
            }
        } catch (InterruptedException e) {
            log.error("Got interrupted while waiting for operation to complete");
        }
    }

    /** Deletes the file at {@link #exchangeUrlForFile} from the file exchange. */
    private void cleanUpFileExchange() {
        try {
            fileExchange.deleteFile(exchangeUrlForFile);
            log.debug("Finished cleaning up '{}' at URL: '{}'..",
                    tempExchangeFileID, exchangeUrlForFile.toExternalForm());
        } catch (IOException | URISyntaxException e) {
            log.warn("Failed cleaning up after file '{}' at '{}'",
                    tempExchangeFileID, exchangeUrlForFile.toExternalForm());
        }
    }
}