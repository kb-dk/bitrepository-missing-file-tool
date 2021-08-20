package org.bitrepository;

import org.bitrepository.util.BitmagUtils;
import picocli.CommandLine;

import java.nio.file.Path;
import java.util.concurrent.Callable;

@CommandLine.Command()
public class MissingFileToolMain implements Callable<Integer> {

    /* Name of the file that we attempt to repair */
    @CommandLine.Parameters(index = "0", type = String.class)
    private String fileID;

    /* Name of the collection the file should be in */
    @CommandLine.Parameters(index = "1", type = String.class)
    private String checksum;

    /* Name of the collection the file should be in */
    @CommandLine.Parameters(index = "2", type = String.class)
    private String collectionID;

    @Override
    public Integer call() throws Exception {
        // Need to add -Dorg.bitrepository.configDir=src/main/conf to "VM options" when running
        String clientCertFileName = "client-certificate.pem";
        String configDirProp = System.getProperty("org.bitrepository.configDir");
        Path configDir = Path.of(configDirProp);
        Path clientCert = configDir.resolve(clientCertFileName);

        BitmagUtils.initialize(configDir, clientCert);
        MissingFileTool tool = new MissingFileTool(fileID);
        return tool.repairMissingFile(fileID, checksum, collectionID) ? 0 : 1;
    }

    public static void main(String[] args) {
        CommandLine cmd = new CommandLine(new MissingFileToolMain());
        int exitCode = cmd.execute(args);
        System.exit(exitCode);
    }
}
