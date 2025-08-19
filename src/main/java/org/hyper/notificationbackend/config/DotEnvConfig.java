package org.hyper.notificationbackend.config;

import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

@Configuration
public class DotEnvConfig {

    @PostConstruct
    public void loadDotEnv() {
        try {
            File envFile = new File(".env");
            if (envFile.exists()) {
                Properties props = new Properties();
                props.load(new FileInputStream(envFile));
                
                // Set system properties so System.getenv() alternatives work
                for (String key : props.stringPropertyNames()) {
                    System.setProperty(key, props.getProperty(key));
                    // Also set as environment variable if possible
                    try {
                        setEnv(key, props.getProperty(key));
                    } catch (Exception e) {
                        // Fallback: just log that we set it as system property
                        System.out.println("Loaded from .env: " + key + " = " + props.getProperty(key));
                    }
                }
                System.out.println("Successfully loaded .env file with " + props.size() + " properties");
            } else {
                System.err.println("Warning: .env file not found in current directory");
            }
        } catch (IOException e) {
            System.err.println("Error loading .env file: " + e.getMessage());
        }
    }
    
    private void setEnv(String key, String value) throws Exception {
        Class<?> processEnvironmentClass = Class.forName("java.lang.ProcessEnvironment");
        java.lang.reflect.Field theEnvironmentField = processEnvironmentClass.getDeclaredField("theEnvironment");
        theEnvironmentField.setAccessible(true);
        java.util.Map<String, String> env = (java.util.Map<String, String>) theEnvironmentField.get(null);
        env.put(key, value);
    }
}
