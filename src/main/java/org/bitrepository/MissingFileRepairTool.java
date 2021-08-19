package org.bitrepository;

import org.bitrepository.access.getfile.GetFileClient;
import org.bitrepository.bitrepositoryelements.ChecksumDataForFileTYPE;
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

public class MissingFileRepairTool {
    private static final Logger log = LoggerFactory.getLogger(MissingFileRepairTool.class);
    private final FileExchange fileExchange;
    private final String tempExchangeFileID; // TODO: Is this even necessary? Spørg Kim om FileExchange/DAV ikke er ligeglad med, om filnavn matcher et andet/om der kan gå kludder i det.
    private final URL exchangeUrlForFile;

    public MissingFileRepairTool(String fileID) throws MalformedURLException {
        fileExchange = BitmagUtils.getFileExchange();
        tempExchangeFileID = fileID + UUID.randomUUID();
        exchangeUrlForFile = fileExchange.getURL(tempExchangeFileID);
    }

    public void repairMissingFile(String fileID, String checksum, String collectionID) {
        File file = getFile(fileID, collectionID);
        if (file != null) {
            //System.out.println(file.getAbsolutePath());
            putFile(file, fileID, checksum, collectionID);
        }
        cleanUpFileExchange();
    }

    public File getFile(String fileID, String collectionID) {
        GetFileClient client = BitmagUtils.getFileClient();
        GetFileEventHandler eventHandler = new GetFileEventHandler();

        try {
            client.getFileFromFastestPillar(collectionID, fileID, null, exchangeUrlForFile, eventHandler, null); // TODO: add audittrail stuff
            eventHandler.waitForFinish();

            boolean actionIsSuccess = !eventHandler.hasFailed();
            if (actionIsSuccess) {
                return getFileFromExchange();
            } else {
                System.out.println("Failed to get file '" + fileID + "'"); // TODO: throw stuff? Should somehow indicate 'fatal' error
            }
        } catch (InterruptedException e) {
            log.error("Got interrupted while waiting for operation to complete");
        }
        return null;
    }

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

    public void putFile(File file, String fileID, String checksum, String collectionID) {
        PutFileClient client = BitmagUtils.getPutFileClient();
        PutFileEventHandler eventHandler = new PutFileEventHandler();
        ChecksumDataForFileTYPE checksumData = BitmagUtils.getChecksum(checksum);

        try {
            client.putFile(collectionID, exchangeUrlForFile, fileID, file.length(), checksumData,
                    null, eventHandler, null); // TODO: add audittrail stuff
            eventHandler.waitForFinish();

            boolean actionIsSuccess = !eventHandler.hasFailed();
            if (actionIsSuccess) {
                System.out.println("Yay we put the file somewhere"); // TODO: fix prints
            } else {
                System.out.println("Something went wrong with put");
            }
        } catch (InterruptedException e) {
            log.error("Got interrupted while waiting for operation to complete");
        }
    }
}
