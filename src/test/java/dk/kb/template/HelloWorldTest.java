package dk.kb.template;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import org.junit.jupiter.api.Test;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;


public class HelloWorldTest {

    /*@Tag("fast")
    @Test
    @DisplayName("Hello from Mr. Hyde")
    public void mrHyde() throws IOException{
        HelloWorld myHello = new HelloWorld();
        String message = myHello.sayHello("Mr. Hyde",50,"Dr. Jekyll",LangEnum.EN);
        System.out.println(message);       
        assertEquals("Hello Mr. Hyde, you are 50 says Dr. Jekyll", message);    
    }*/
    
    @Tag("fast")
    @Test
    @DisplayName("Mockito demonstration")
    public void mockito() throws MalformedURLException, ClassNotFoundException {
        // Mockito makes it easy to create mocked versions of classes.
        // Basically it creates an empty shell from the class and lets the test-writer fill in the methods
        // used by the test. The class constructor is bypassed. The class that is mocked here is ClassLoader,
        // to demonstrate that it is possible to mock very complex classes
        ClassLoader mockedLoader = mock(ClassLoader.class);

        // Methods that are not explicitly mocked returns null
        assertNull(mockedLoader.getName());

        // We can assign a result to a method
        when(mockedLoader.getName()).thenReturn("Mockito ClassLoader");
        assertEquals("Mockito ClassLoader", mockedLoader.getName());

        // We can assign conditionals to methods that takes arguments
        when(mockedLoader.getResource(eq("example"))).thenReturn(new URL("http://example.org/"));
        when(mockedLoader.getResource(eq("KB"))).thenReturn(new URL("https://www.kb.dk/"));
        assertNull(mockedLoader.getResource("Undefined"));
        assertEquals(new URL("http://example.org/"), mockedLoader.getResource("example"));
        assertEquals(new URL("https://www.kb.dk/"), mockedLoader.getResource("KB"));

        // Arguments need not be defined
        when(mockedLoader.getResource(anyString())).thenReturn(new URL("http://statsbiblioteket.dk/"));
        assertEquals(new URL("http://statsbiblioteket.dk/"), mockedLoader.getResource("randomString"));

        // The result can rely on the input
        when(mockedLoader.getResource(anyString())).thenAnswer(
                input -> new URL("http://" + input.getArgument(0) + ".example.org/"));
        assertEquals(new URL("http://foo.example.org/"), mockedLoader.getResource("foo"));
    }


    @Tag("slow")
    @Test
    public void sleeper() {
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        assertTrue(true);
    }
    
}
