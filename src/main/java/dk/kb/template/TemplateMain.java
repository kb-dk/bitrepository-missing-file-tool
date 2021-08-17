package dk.kb.template;

import java.util.Arrays;
import java.util.concurrent.Callable;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.kb.util.yaml.YAML;
import picocli.CommandLine;

//Check link below for examples.
//https://picocli.info/#_introduction
@CommandLine.Command()
public class TemplateMain implements Callable<Integer>{

    private static final Logger log = LoggerFactory.getLogger(TemplateMain.class);
    public enum LangEnum {DA, EN} // Language enums in example

    /*
     * Name of the file that is attempted to be put
     */
    @CommandLine.Parameters(index = "0", type = String.class)
    private String name;

    /*
     * Name of the collection to put the file in
     */
    @CommandLine.Parameters(index = "1", type = String.class)
    private int age;

    /*
     * Example to see specify a format with enum types
     * Both -lang=DK and --lang=DK will be accepted
     */
    @CommandLine.Option(names = {"-lang", "--lang"}, required = false, type = LangEnum.class,
                        description = "Valid values: ${COMPLETION-CANDIDATES}", defaultValue = "EN")
    private LangEnum lang;

    /*
     * Implement the normal 'main' method here
     */

    @Override
    public Integer call() throws Exception {

     // How to load a property
     //When debugging from IDEA, add -Ddk.kb.applicationConfig=src/main/conf/templateConfig.yaml to "VM options"
     String applicationConfig = System.getProperty("dk.kb.applicationConfig");
     YAML config = new YAML(applicationConfig);

     String speaker = config.getString("config.speaker");

        HelloWorld hw = new HelloWorld();
        String message= hw.sayHello(name, age, speaker, lang);
        System.out.println(message);
        return 0; //Exit code
    }


    public static void main(String... args) {
        System.out.println("Arguments passed by commandline is: " + Arrays.asList(args));
        CommandLine app = new CommandLine(new  TemplateMain());
        int exitCode = app.execute(args);
        System.exit(exitCode);
    }
}