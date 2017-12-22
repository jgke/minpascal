package fi.jgke.minpascal.parser;

import fi.jgke.minpascal.Configuration;
import org.junit.After;

public class TestBase {
    @After
    public void afterClass() {
        Configuration.STRICT_MODE = false;
    }
}
