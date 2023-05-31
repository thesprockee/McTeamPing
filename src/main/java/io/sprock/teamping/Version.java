package io.sprock.teamping;

import java.util.Properties;

public class Version
{

    @SuppressWarnings("unused")
    private static String version;

    static void init(Properties properties)
    {
        if (properties != null)
        {
            version = properties.getProperty("version");
        }
    }

    public static String getVersion() {
    	return version;
    }

}