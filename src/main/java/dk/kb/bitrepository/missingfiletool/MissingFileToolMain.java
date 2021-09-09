package dk.kb.bitrepository.missingfiletool;

import dk.kb.util.yaml.YAML;
import dk.kb.bitrepository.missingfiletool.util.BitmagUtils;
import picocli.CommandLine;

import java.nio.file.Path;
import java.util.concurrent.Callable;

@CommandLine.Command()
public class MissingFileToolMain implements Callable<Integer> {

    /* Name of the collection the file should be in */
    @CommandLine.Parameters(index = "0", type = String.class)
    private String collectionID;

    /* Checksum of the file to repair */
    @CommandLine.Parameters(index = "1", type = String.class)
    private String checksum;

    /* Name of the file that we attempt to repair */
    @CommandLine.Parameters(index = "2", type = String.class)
    private String fileID;

    @Override
    public Integer call() throws Exception {
        // Need to add -Ddk.kb.configFile=src/main/conf/config.yaml to "VM options" when running
        String configFileProp = System.getProperty("dk.kb.configFile");
        YAML config = new YAML(configFileProp);
        Path configDir = Path.of(config.getString("bitrepoConf.confDir"));
        Path clientCert = Path.of(config.getString("bitrepoConf.certFile"));

        BitmagUtils.initialize(configDir, clientCert);
        MissingFileTool tool = new MissingFileTool();
        return tool.repairMissingFile(fileID, checksum, collectionID) ? 0 : 1;
    }

    public static void main(String[] args) {
        CommandLine cmd = new CommandLine(new MissingFileToolMain());
        int exitCode = cmd.execute(args);
        System.exit(exitCode);
    }
}
