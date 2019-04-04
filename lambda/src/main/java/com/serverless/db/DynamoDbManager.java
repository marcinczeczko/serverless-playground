package com.serverless.db;

import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;

public class DynamoDbManager {

  private static final DynamoDB dynamoDb;

  static {
    AmazonDynamoDB client;
    String isLocal = System.getProperty("dynamodb.local");
    if (isLocal != null && isLocal.equals("true")) {
      client = AmazonDynamoDBClientBuilder.standard()
          .withEndpointConfiguration(new EndpointConfiguration("http://localhost:" + System.getProperty("dynamodb.port"), "localhost"))
          .build();
    } else {
      client = AmazonDynamoDBClientBuilder.defaultClient();
    }
    dynamoDb = new DynamoDB(client);
  }

  public static DynamoDB getDynamoDb() {
    return dynamoDb;
  }

  private DynamoDbManager() {
    //Prevent from creating instance of that class
  }

  /**
   * Avoid object cloning (assuring singleton) by overriding clone() method.
   */
  public Object clone() throws CloneNotSupportedException {
    throw new CloneNotSupportedException();
  }
}
