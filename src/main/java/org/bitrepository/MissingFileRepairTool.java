package org.bitrepository;

import org.bitrepository.access.getfile.GetFileClient;
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
    private File intermediateLocalFile;


    public void repairMissingFile(String fileID, String checksum, String collectionID) {
        getFile(fileID, collectionID);
    }

    public File getFile(String fileID, String collectionID) {
        GetFileClient client = BitmagUtils.getFileClient();
        GetFileEventHandler eventHandler = new GetFileEventHandler();
        FileExchange fileExchange = BitmagUtils.getFileExchange();
        try {
            intermediateLocalFile = File.createTempFile(UUID.randomUUID().toString(), "");
            System.out.println(intermediateLocalFile.getName() + " created");
            URL url = new URL(BitmagUtils.getFileExchangeBaseURL().toExternalForm() + UUID.randomUUID());
            client.getFileFromFastestPillar(collectionID, fileID, null, url, eventHandler, null);

            eventHandler.waitForFinish();

            boolean actionIsSuccess = !eventHandler.hasFailed();
            if (actionIsSuccess) {
                fileExchange.getFile(intermediateLocalFile, url.toExternalForm());
                try {
                    fileExchange.deleteFile(url);
                } catch (IOException | URISyntaxException e) {
                    log.error("Failed cleaning up after file '{}'", fileID);
                }
            } else {
                System.out.println("Failed to get file '" + fileID + "'");
            }
        } catch (InterruptedException e) {
            log.error("Got interrupted while waiting for operation to complete");
        } catch (MalformedURLException e) {
            log.error("Got malformed URL while trying to get file '{}'", fileID);
        } catch (IOException e) {
            log.error("Can't create intermediate temp file");
        }
        return null;
    }

    public void putFile(File file, String checksum, String collectionID) {

    }
}
