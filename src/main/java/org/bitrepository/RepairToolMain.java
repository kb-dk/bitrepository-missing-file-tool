package org.bitrepository;

import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command()
public class RepairToolMain implements Callable<Integer> {
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
        System.out.println("Tool ran");
        //File file = Bitrepository.get(fileID, checksum, collectionID)
        //Bitrepository.put(file, checksum, collectionID)
        return 0;
    }

    public static void main(String[] args) {
        CommandLine tool = new CommandLine(new RepairToolMain());
        int exitCode = tool.execute(args);
        System.exit(exitCode);
    }
}
