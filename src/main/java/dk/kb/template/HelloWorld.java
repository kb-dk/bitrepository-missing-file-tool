package dk.kb.template;

import dk.kb.template.TemplateMain.LangEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class HelloWorld {

    private final Logger log = LoggerFactory.getLogger(getClass());

    public HelloWorld() {
        log.debug("Running constructor HelloWorld()");
    }

    public String sayHello(String name, int age,  String speaker, LangEnum lang) throws IOException{

        String message = null;
        switch (lang) {
            case DA:
                message="Hejsa "+ name +", du er "+age +" siger "+speaker;
                break;
            case EN:
                message="Hello "+ name +", you are "+age +" says "+speaker;
                break;

            //No default, since API guaranatee a LangEnum is given as argument

        }
        return message;
    }
}