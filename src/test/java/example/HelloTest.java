package example;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import org.junit.Test;

public class HelloTest {
    @Test
    public void testEcho() {
        assertThat(Hello.echo("Hello World!!"), is("Hello World!!"));
    }


    @Test
    public void smokeTest() {
        assertTrue(false);
    }
}
