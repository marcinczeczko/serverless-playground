package com.serverless.ext;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.PrimaryKey;
import com.amazonaws.services.dynamodbv2.document.TableWriteItems;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.CreateTableResult;
import com.amazonaws.services.dynamodbv2.model.DeleteTableRequest;
import com.amazonaws.services.dynamodbv2.model.DeleteTableResult;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.KeyType;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.model.ScalarAttributeType;
import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

public class DynamoDbTableExtension implements BeforeEachCallback, AfterEachCallback {

  private final String dynamoDbEndpoint;
  private final String usersTable;

  private static AmazonDynamoDB client;

  private static DynamoDB dynamoDB;

  private final Gson gson = new Gson();

  public DynamoDbTableExtension() {
    dynamoDbEndpoint = "http://localhost:" + System.getProperty("dynamodb.port");
    usersTable = System.getProperty("users.table.name");
    createAmazonDynamoDBClient();
  }

  @Override
  public void beforeEach(ExtensionContext context) {
    createTables(usersTable, "userId", "itemId");
    createTestData();
  }

  @Override
  public void afterEach(ExtensionContext context) {
    dropTables(usersTable);
  }

  private void createAmazonDynamoDBClient() {
    client = AmazonDynamoDBClientBuilder.standard()
        .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(dynamoDbEndpoint, "localhost"))
        .build();
    dynamoDB = new DynamoDB(client);
  }

  private DeleteTableResult dropTables(String tableName) {
    return client.deleteTable(new DeleteTableRequest()
        .withTableName(tableName));
  }

  private CreateTableResult createTables(String tableName, String hashKeyName, String rangeKeyName) {
    List<AttributeDefinition> attributeDefinitions = new ArrayList<>();
    attributeDefinitions.add(new AttributeDefinition(hashKeyName, ScalarAttributeType.S));
    if (rangeKeyName != null) {
      attributeDefinitions.add(new AttributeDefinition(rangeKeyName, ScalarAttributeType.S));
    }

    List<KeySchemaElement> ks = new ArrayList<>();
    ks.add(new KeySchemaElement(hashKeyName, KeyType.HASH));
    if (rangeKeyName != null) {
      ks.add(new KeySchemaElement(rangeKeyName, KeyType.RANGE));
    }

    ProvisionedThroughput provisionedthroughput = new ProvisionedThroughput(1000L, 1000L);

    CreateTableRequest request =
        new CreateTableRequest()
            .withTableName(tableName)
            .withAttributeDefinitions(attributeDefinitions)
            .withKeySchema(ks)
            .withProvisionedThroughput(provisionedthroughput);

    return client.createTable(request);
  }

  private void createTestData() {
    TableWriteItems dataItems = new TableWriteItems(usersTable)
        .withItemsToPut(
            createUserDataItem("user-1", "data-1", 2),
            createUserDataItem("user-1", "data-2", 1),
            createUserDataItem("user-2", "data-1", 1)
        );

    dynamoDB.batchWriteItem(dataItems);
  }

  private Item createUserDataItem(String userId, String dataItemId, int collectionCount) {
    PrimaryKey pKey = new PrimaryKey()
        .addComponent("userId", userId) //hashKey
        .addComponent("itemId", dataItemId); //rangeKey

    List<Map<String, String>> collection = createCollections(userId, dataItemId, collectionCount);

    return new Item()
        .withPrimaryKey(pKey)
        .withString("itemName", String.format("%s-%s-name", userId, dataItemId))
        .withList("itemCollection", collection);
  }

  private List<Map<String, String>> createCollections(String userId, String dataItemId, int count) {
    return IntStream.range(1, count + 1)
        .mapToObj(cnt -> {
          Map<String, String> item = new HashMap<>();
          item.put("id", String.format("%s-%s-col-%d", userId, dataItemId, cnt));
          item.put("headline", String.format("Headline %s-%s-col-%d", userId, dataItemId, cnt));
          item.put("subheadline", String.format("Sub-headline %s-%s-col-%d", userId, dataItemId, cnt));
          item.put("visible", "true");

          return item;
        })
        .collect(Collectors.toList());
  }
}
