package com.starlettech.enums;

/**
 * Enum for different test environments
 */
public enum Environment {
    DEV("dev", "Development Environment"),
    TEST("test", "Test Environment"),
    STAGING("staging", "Staging Environment"),
    PROD("prod", "Production Environment"),
    LOCAL("local", "Local Environment");

    private final String environmentName;
    private final String description;

    Environment(String environmentName, String description) {
        this.environmentName = environmentName;
        this.description = description;
    }

    public String getEnvironmentName() {
        return environmentName;
    }

    public String getDescription() {
        return description;
    }

    public static Environment fromString(String env) {
        for (Environment environment : Environment.values()) {
            if (environment.environmentName.equalsIgnoreCase(env) || environment.name().equalsIgnoreCase(env)) {
                return environment;
            }
        }
        throw new IllegalArgumentException("Unknown environment: " + env);
    }
}
