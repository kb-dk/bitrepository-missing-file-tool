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

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.UUID;

public class MissingFileTool {
    private static final Logger log = LoggerFactory.getLogger(MissingFileTool.class);
    private final FileExchange fileExchange;
    private final String tempExchangeFileID;
    private final URL exchangeUrlForFile;
    private boolean isRepairSuccess;

    public MissingFileTool() throws MalformedURLException {
        fileExchange = BitmagUtils.getFileExchange();
        tempExchangeFileID = UUID.randomUUID().toString();
        exchangeUrlForFile = fileExchange.getURL(tempExchangeFileID);
        isRepairSuccess = false;
    }

    /**
     * Attempt to repair the missing file by getting the file from a pillar that has it,
     * followed by making a put of the file to all pillars. Cleans up file-exchange when done.
     * @param fileID ID of the file to repair
     * @param checksum Checksum of file
     * @param collectionID The collection of the file
     * @return boolean signaling if the repair was a success
     */
    public boolean repairMissingFile(String fileID, String checksum, String collectionID) {
        try {
            boolean getIsSuccess = getFileToExchange(fileID, collectionID);
            if (getIsSuccess) {
                putFileFromExchange(fileID, checksum, collectionID);
            }
        } catch (Exception e) {
            log.error("Something went wrong while trying to repair file '{}' ({}) in collection '{}'",
                    fileID, checksum, collectionID);
        } finally {
            cleanUpFileExchange();
        }
        return isRepairSuccess;
    }

    /**
     * Attempts to download a file with the given ID to the file exchange.
     * @param fileID ID/name of the file to get
     * @param collectionID Name of the collection to get it from
     * @return boolean status of the operation. False if failed, true if success.
     */
    public boolean getFileToExchange(String fileID, String collectionID) {
        GetFileClient client = BitmagUtils.getFileClient();
        GetFileEventHandler eventHandler = new GetFileEventHandler();
        boolean getIsSuccess = false;

        try {
            client.getFileFromFastestPillar(collectionID, fileID, null, exchangeUrlForFile, eventHandler,
                    "GetFile from missing-file-tool");
            eventHandler.waitForFinish();

            getIsSuccess = eventHandler.isOperationSuccess();
            if (getIsSuccess) {
                log.debug("Successfully got file '{}' to file exchange.", fileID);
            } else {
                System.err.println("Failed when trying to get file '" + fileID
                        + "' from collection '" + collectionID + "'");
            }
        } catch (InterruptedException e) {
            log.error("Got interrupted while waiting for get-operation to complete");
        }
        return getIsSuccess;
    }

    /**
     * Attempts to put the given file from the file exchange into the given collection.
     * @param fileID Name of the file
     * @param checksum The files checksum
     * @param collectionID The collection to put the file in
     */
    public void putFileFromExchange(String fileID, String checksum, String collectionID) {
        PutFileClient client = BitmagUtils.getPutFileClient();
        PutFileEventHandler eventHandler = new PutFileEventHandler();
        ChecksumDataForFileTYPE checksumData = BitmagUtils.getChecksum(checksum);

        try {
            client.putFile(collectionID, exchangeUrlForFile, fileID, 0, checksumData,
                    null, eventHandler, "Put from missing-file-tool");
            eventHandler.waitForFinish();

            if (eventHandler.isOperationSuccess()) {
                isRepairSuccess = true;
                log.debug("Successfully put file '{}' ({}) in collection '{}'", fileID, checksum, collectionID);
                System.out.println("Successfully repaired missing file '" + fileID + "' (" + checksum
                        + ") in collection '" + collectionID + "'");
            } else {
                System.err.println("Failed while trying to put file '" + fileID
                        + "' (" + checksum + ") into collection '" + collectionID + "'");
            }
        } catch (InterruptedException e) {
            log.error("Got interrupted while waiting for put-operation to complete");
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
