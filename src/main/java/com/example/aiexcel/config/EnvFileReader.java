package com.example.aiexcel.config;

import org.springframework.core.io.FileSystemResource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.File;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility to read a .env file from the project root and expose values.
 */
public class EnvFileReader {

    public static Properties loadEnvFile() {
        String currentDir = System.getProperty("user.dir");
        String envFilePath = currentDir + File.separator + ".env";
        FileSystemResource resource = new FileSystemResource(envFilePath);

        if (!resource.exists()) {
            return null;
        }

        Properties properties = new Properties();
        Pattern pattern = Pattern.compile("^([A-Za-z_][A-Za-z0-9_]*)=(.*)$");

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#") || line.startsWith("!")) {
                    continue;
                }

                Matcher matcher = pattern.matcher(line);
                if (matcher.matches()) {
                    String key = matcher.group(1);
                    String value = matcher.group(2).trim();

                    if ((value.startsWith("\"") && value.endsWith("\"")) ||
                        (value.startsWith("'") && value.endsWith("'"))) {
                        value = value.substring(1, value.length() - 1);
                    } else {
                        int commentIdx = value.indexOf('#');
                        if (commentIdx >= 0) {
                            value = value.substring(0, commentIdx).trim();
                        }
                        int slashIdx = value.indexOf("//");
                        if (slashIdx >= 0) {
                            value = value.substring(0, slashIdx).trim();
                        }
                    }

                    properties.setProperty(key, value);
                }
            }
        } catch (IOException e) {
            return null;
        }

        return properties;
    }
}
