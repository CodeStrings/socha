package sc.plugin2014.moves;

import static org.junit.Assert.*;
import org.junit.Test;

public class DebugHintTest {

    @Test
    public void testDebugHint() {
        DebugHint debugHint = new DebugHint("test");

        assertEquals("test", debugHint.getContent());

        DebugHint debugHint2 = new DebugHint("key", "value");

        assertEquals("key = value", debugHint2.getContent());
    }

}
