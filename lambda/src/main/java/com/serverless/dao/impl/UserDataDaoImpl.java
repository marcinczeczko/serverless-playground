package com.serverless.dao.impl;

import com.amazonaws.services.dynamodbv2.document.AttributeUpdate;
import com.amazonaws.services.dynamodbv2.document.Expected;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.ItemCollection;
import com.amazonaws.services.dynamodbv2.document.Page;
import com.amazonaws.services.dynamodbv2.document.PrimaryKey;
import com.amazonaws.services.dynamodbv2.document.QueryOutcome;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.GetItemSpec;
import com.amazonaws.services.dynamodbv2.document.spec.PutItemSpec;
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;
import com.amazonaws.services.dynamodbv2.document.spec.ScanSpec;
import com.amazonaws.services.dynamodbv2.document.spec.UpdateItemSpec;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;
import com.amazonaws.services.dynamodbv2.model.ReturnValue;
import com.serverless.dao.UserDataDao;
import com.serverless.db.DynamoDbManager;
import com.serverless.model.UserData;
import com.serverless.model.UserData.DataItem;
import com.serverless.model.UserDataItem;
import com.serverless.model.UserDataItem.CollectionItem;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.apache.log4j.Logger;

public class UserDataDaoImpl implements UserDataDao {

  private static final Logger LOG = Logger.getLogger(UserDataDaoImpl.class);

  private final Table table;

  public UserDataDaoImpl() {
    table = DynamoDbManager.getDynamoDb().getTable(System.getProperty("users.table.name"));
  }

  @Override
  public Set<String> getAllUsers() {
    Set<String> result = new HashSet<>();

    ScanSpec scanSpec = new ScanSpec()
        .withAttributesToGet("userId");

    Iterator<Item> it = table.scan(scanSpec).iterator();
    while (it.hasNext()) {
      result.add(it.next().getString("userId"));
    }

    return result;
  }

  @Override
  public Optional<UserData> getUser(String userId) {
    QuerySpec spec = new QuerySpec()
        .withKeyConditionExpression("userId = :v_userId")
        .withValueMap(new ValueMap()
            .withString(":v_userId", userId))
        .withProjectionExpression("itemId, itemName");

    ItemCollection<QueryOutcome> items = table.query(spec);

    List<DataItem> dataItems = new ArrayList<>();
    for (Page<Item, QueryOutcome> page : items.pages()) {
      Iterator<Item> pageIt = page.iterator();
      while (pageIt.hasNext()) {
        Item item = pageIt.next();
        dataItems.add(new DataItem().setUserDataItemId(item.getString("itemId")).setUserDataItemName(item.getString("itemName")));
      }
    }
    if (dataItems.isEmpty()) {
      return Optional.empty();
    } else {
      return Optional.of(new UserData().setUserId(userId).setDataItems(dataItems));
    }
  }

  @Override
  public Optional<UserDataItem> getUserDataItem(String userId, String userDataItemId) {
    PrimaryKey pKey = new PrimaryKey("userId", userId, "itemId", userDataItemId);

    GetItemSpec dataItemSpec = new GetItemSpec()
        .withPrimaryKey(pKey)
        .withProjectionExpression("userId, itemId, itemName, itemCollection");

    Item item = table.getItem(dataItemSpec);

    if (item != null) {
      List<CollectionItem> collItems = new ArrayList<>();
      List<Map<String, String>> collection = item.getList("itemCollection");
      for (Map<String, String> collItem : collection) {
        collItems.add(
            new CollectionItem()
                .setId(collItem.get("id"))
                .setHeadline(collItem.get("headline"))
                .setSubheadline(collItem.get("subheadline"))
                .setVisible(Boolean.valueOf(collItem.get("visible")))
        );
      }
      return Optional.of(new UserDataItem()
          .setUserId(userId)
          .setItemId(userDataItemId)
          .setItemName(item.getString("itemName"))
          .setCollection(collItems));
    } else {
      return Optional.empty();
    }
  }

  @Override
  public void addDataItem(String userId, UserDataItem dataItem) {
    List<Map<String, String>> collection = extractCollection(dataItem);

    try {
      PutItemSpec spec = new PutItemSpec()
          .withItem(new Item()
              .withPrimaryKey("userId", userId, "itemId", dataItem.getItemId())
              .withString("itemName", dataItem.getItemName())
              .withList("itemCollection", collection))
          .withConditionExpression("attribute_not_exists(userId) and attribute_not_exists(itemId)");

      table.putItem(spec);
    } catch (Exception e) {
      LOG.error("Create data item failed.", e);
      throw e;
    }
  }

  @Override
  public void udpateDataItem(String userId, String dataItemId, UserDataItem dataItem) {
    List<Map<String, String>> collection = extractCollection(dataItem);

    try {
      UpdateItemSpec updateSpec = new UpdateItemSpec()
          .withPrimaryKey("userId", userId, "itemId", dataItemId)
          .withAttributeUpdate(
              new AttributeUpdate("itemName").put(dataItem.getItemName()),
              new AttributeUpdate("itemCollection").put(collection)
          )
          .withExpected(new Expected("userId").exists(), new Expected("itemId").exists())
          .withReturnValues(ReturnValue.UPDATED_NEW);

      table.updateItem(updateSpec);
    } catch (Exception e) {
      LOG.error("Update data item failed.", e);
      throw e;
    }

  }

  private List<Map<String, String>> extractCollection(UserDataItem dataItem) {
    List<Map<String, String>> collection = new ArrayList<>();
    if (dataItem.getCollection() != null) {
      for (CollectionItem item : dataItem.getCollection()) {
        Map<String, String> collItem = new HashMap<>();
        collItem.put("id", item.getId());
        collItem.put("headline", item.getHeadline());
        collItem.put("subheadline", item.getSubheadline());
        collItem.put("visible", String.valueOf(item.isVisible()));
        collection.add(collItem);
      }
    }
    return collection;
  }
}
