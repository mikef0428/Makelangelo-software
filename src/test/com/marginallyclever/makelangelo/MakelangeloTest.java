package com.marginallyclever.makelangelo;

import com.marginallyclever.convenience.log.Log;
import com.marginallyclever.util.PreferencesHelper;
import com.marginallyclever.util.PropertiesFileHelper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;

@DisabledIfEnvironmentVariable(named = "CI", matches = "true")
public class MakelangeloTest {
    @Test
    public void checkVersion() throws IllegalStateException {
        Log.start();
        PreferencesHelper.start();
        Translator.start();
        String version = PropertiesFileHelper.getMakelangeloVersionPropertyValue();
        System.out.println("version " + version);
        String[] toks = version.split("\\.");
        Assertions.assertEquals( 3, toks.length, "Version must be major.minor.tiny.");
        Assertions.assertNotNull(Integer.valueOf(toks[0]));
        Log.end();
    }
}
