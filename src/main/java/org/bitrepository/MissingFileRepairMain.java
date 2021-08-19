package org.bitrepository;

import org.bitrepository.util.BitmagUtils;
import picocli.CommandLine;

import java.nio.file.Path;
import java.util.concurrent.Callable;

@CommandLine.Command()
public class MissingFileRepairMain implements Callable<Integer> {
    private final String CLIENT_CERTIFICATE_FILE = "client-certificate.pem";

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
        /* How to load a property
        //When debugging from IDEA, add -Ddk.kb.applicationConfig=src/main/conf/templateConfig.yaml to "VM options"
        String applicationConfig = System.getProperty("dk.kb.applicationConfig");
        YAML config = new YAML(applicationConfig);
        String speaker = config.getString("config.speaker"); */

        String configDirProp = System.getProperty("org.bitrepository.configDir");
        Path configDir = Path.of(configDirProp);
        Path clientCert = configDir.resolve(CLIENT_CERTIFICATE_FILE);
        BitmagUtils.initialize(configDir, clientCert);
        MissingFileRepairTool tool = new MissingFileRepairTool(fileID);
        tool.repairMissingFile(fileID, checksum, collectionID);
        return 0;
    }

    public static void main(String[] args) {
        CommandLine cmd = new CommandLine(new MissingFileRepairMain());
        int exitCode = cmd.execute(args);
        System.exit(exitCode);
    }
}
