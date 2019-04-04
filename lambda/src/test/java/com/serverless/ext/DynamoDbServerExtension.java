package com.serverless.ext;

import com.amazonaws.services.dynamodbv2.local.main.ServerRunner;
import com.amazonaws.services.dynamodbv2.local.server.DynamoDBProxyServer;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

public class DynamoDbServerExtension implements BeforeAllCallback, AfterAllCallback {

  private DynamoDBProxyServer server;
  
  @Override
  public void beforeAll(ExtensionContext context) throws Exception {
    server = ServerRunner.createServerFromCommandLineArgs(new String[]{"-inMemory", "-port", System.getProperty("dynamodb.port")});
    server.start();
  }

  @Override
  public void afterAll(ExtensionContext context) {
    if (server != null) {
      try {
        server.stop();
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
  }
}
