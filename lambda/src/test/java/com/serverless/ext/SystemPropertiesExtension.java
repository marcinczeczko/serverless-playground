package com.serverless.ext;

import org.junit.jupiter.api.extension.Extension;

public class SystemPropertiesExtension implements Extension {

  public SystemPropertiesExtension() {
    System.setProperty("dynamodb.local", "true");
    System.setProperty("users.table.name", "users-junit");
    System.setProperty("dataitems.table.name", "user-data-items-junit");
    System.setProperty("sqlite4java.library.path", "./build/libs/");
    String dynamoPort = System.getProperty("dynamodb.port");
    if (dynamoPort == null || dynamoPort.equals("")) {
      System.setProperty("dynamodb.port", "8000");
    }
  }
}
