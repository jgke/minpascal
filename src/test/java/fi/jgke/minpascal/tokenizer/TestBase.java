package fi.jgke.minpascal.tokenizer;

import fi.jgke.minpascal.Configuration;
import org.junit.After;

public class TestBase {
    @After
    public void afterClass() {
        Configuration.STRICT_MODE = false;
    }
}
