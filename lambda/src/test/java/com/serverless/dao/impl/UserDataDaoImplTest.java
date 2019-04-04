package com.serverless.dao.impl;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.amazonaws.services.dynamodbv2.model.ConditionalCheckFailedException;
import com.serverless.dao.UserDataDao;
import com.serverless.ext.DynamoDbServerExtension;
import com.serverless.ext.DynamoDbTableExtension;
import com.serverless.ext.SystemPropertiesExtension;
import com.serverless.model.UserData;
import com.serverless.model.UserData.DataItem;
import com.serverless.model.UserDataItem;
import com.serverless.model.UserDataItem.CollectionItem;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@DisplayName("UserDAO tests with local DynamoDB")
@ExtendWith({SystemPropertiesExtension.class, DynamoDbServerExtension.class, DynamoDbTableExtension.class})
public class UserDataDaoImplTest {

  private final static String USER_1_ID = "user-1";
  private static final String USER_2_ID = "user-2";
  public static final String USER_FOO_ID = "user-foo";
  public static final String USER_1_DATA_1_ID = "data-1";
  public static final String USER_1_DATA_1_NAME = "user-1-data-1-name";
  public static final String USER_1_DATA_2_ID = "data-2";
  public static final String USER_1_DATA_2_NAME = "user-1-data-2-name";
  public static final String USER_FOO_DATA_FOO_ID = "foo";
  public static final String USER_1_NEW_DATA_ID = "data-0";
  public static final String USER_1_NEW_DATA_NAME = "new user data item";

  private UserDataDao tested;

  @BeforeEach
  void setUp() {
    tested = new UserDataDaoImpl();
  }

  @Test
  @DisplayName("Fetch list of all users")
  void test_getAllUsers() {
    Set<String> allUsers = tested.getAllUsers();

    assertEquals(2, allUsers.size(), "There should be 2 users");
    assertTrue(allUsers.contains(USER_1_ID));
    assertTrue(allUsers.contains(USER_2_ID));
  }

  @Test
  @DisplayName("Fetch 'user-1' record")
  void test_getUser1Data() {
    Optional<UserData> user = tested.getUser(USER_1_ID);

    assertTrue(user.isPresent());
    assertEquals(USER_1_ID, user.get().getUserId());
    assertEquals(2, user.get().getDataItems().size());

    List<DataItem> items = user.get().getDataItems();

    assertEquals(USER_1_DATA_1_ID, items.get(0).getUserDataItemId());
    assertEquals(USER_1_DATA_1_NAME, items.get(0).getUserDataItemName());

    assertEquals(USER_1_DATA_2_ID, items.get(1).getUserDataItemId());
    assertEquals(USER_1_DATA_2_NAME, items.get(1).getUserDataItemName());
  }

  @Test
  @DisplayName("Fetch not existing user")
  void test_getNotExistingUserData() {
    Optional<UserData> user = tested.getUser(USER_FOO_ID);

    assertFalse(user.isPresent());
  }

  @Test
  @DisplayName("Fetch 'user-1' data item 'data-1'")
  void test_getUser1DataItem() {
    Optional<UserDataItem> result = tested.getUserDataItem(USER_1_ID, USER_1_DATA_1_ID);

    assertTrue(result.isPresent());
    assertEquals(USER_1_DATA_1_ID, result.get().getItemId());
    assertEquals(USER_1_DATA_1_NAME, result.get().getItemName());
    assertEquals(2, result.get().getCollection().size());

    List<CollectionItem> collection = result.get().getCollection();
    assertEquals("user-1-data-1-col-1", collection.get(0).getId());
    assertEquals("Headline user-1-data-1-col-1", collection.get(0).getHeadline());
    assertEquals("Sub-headline user-1-data-1-col-1", collection.get(0).getSubheadline());
    assertEquals(true, collection.get(0).isVisible());
  }

  @Test
  @DisplayName("Fetch not existing user user data item")
  void test_getNotExistingUserDataItems() {
    Optional<UserDataItem> result = tested.getUserDataItem(USER_FOO_ID, USER_FOO_DATA_FOO_ID);

    assertFalse(result.isPresent());
  }

  @Test
  @DisplayName("Add new data item 'data-0' for 'user-1' - Users count not changed")
  void test_addNewDataItem_validateUsersCount() {
    UserDataItem item = new UserDataItem()
        .setUserId(USER_1_ID)
        .setItemId(USER_1_NEW_DATA_ID)
        .setItemName(USER_1_NEW_DATA_NAME)
        .setCollection(new ArrayList<>());

    tested.addDataItem(USER_1_ID, item);

    Set<String> allUsers = tested.getAllUsers();

    assertAll("Users list should have 2 existing and one new user",
        () -> assertEquals(2, allUsers.size()),
        () -> assertTrue(allUsers.contains(USER_1_ID)),
        () -> assertTrue(allUsers.contains(USER_2_ID))
    );
  }

  @Test
  @DisplayName("Add new data item 'data-0' for 'user-1' - Data item created")
  void test_addNewDataItem_validateItemCreated() {
    List<CollectionItem> expectedCollection = new ArrayList<>();
    UserDataItem expectedItem = new UserDataItem()
        .setUserId(USER_1_ID)
        .setItemId(USER_1_NEW_DATA_ID)
        .setItemName(USER_1_NEW_DATA_NAME)
        .setCollection(expectedCollection);

    tested.addDataItem(USER_1_ID, expectedItem);

    Optional<UserDataItem> result = tested.getUserDataItem(USER_1_ID, USER_1_NEW_DATA_ID);
    assertTrue(result.isPresent());
    UserDataItem newItem = result.get();
    assertAll("User should have added new data",
        () -> assertEquals(expectedItem.getUserId(), newItem.getUserId()),
        () -> assertEquals(expectedItem.getItemId(), newItem.getItemId()),
        () -> assertEquals(expectedCollection.size(), newItem.getCollection().size())
    );
  }

  @Test
  @DisplayName("Add new data item 'data-0' for 'user-1' with collection - Data item with collection created")
  void test_addNewDataItem_validateCollectionCreated() {
    List<CollectionItem> expectedCollection = new ArrayList<>();
    expectedCollection.add(new CollectionItem()
        .setId("1")
        .setHeadline("1h1")
        .setSubheadline("1h2")
        .setVisible(false)
    );
    expectedCollection.add(new CollectionItem()
        .setId("2")
        .setHeadline("2h1")
        .setSubheadline("2h2")
        .setVisible(false)
    );

    UserDataItem item = new UserDataItem()
        .setUserId(USER_1_ID)
        .setItemId(USER_1_NEW_DATA_ID)
        .setItemName(USER_1_NEW_DATA_NAME)
        .setCollection(expectedCollection);

    tested.addDataItem(USER_1_ID, item);

    UserDataItem result = tested.getUserDataItem(USER_1_ID, USER_1_NEW_DATA_ID).get();

    List<CollectionItem> collItems = result.getCollection();
    assertAll("Collection items",
        () -> assertEquals(expectedCollection.get(0).getId(), collItems.get(0).getId()),
        () -> assertEquals(expectedCollection.get(0).getHeadline(), collItems.get(0).getHeadline()),
        () -> assertEquals(expectedCollection.get(0).getSubheadline(), collItems.get(0).getSubheadline()),
        () -> assertEquals(expectedCollection.get(0).isVisible(), collItems.get(0).isVisible())
    );
  }

  @Test
  @DisplayName("Add new data item 'data-0' for 'user-1' - User Data shows new item")
  void test_addNewDataItem_validateUserData() {
    UserDataItem expectedDataItem = new UserDataItem()
        .setUserId(USER_1_ID)
        .setItemId(USER_1_NEW_DATA_ID) //add as first to list of items for user
        .setItemName(USER_1_NEW_DATA_NAME);

    tested.addDataItem(USER_1_ID, expectedDataItem);

    UserData user = tested.getUser(expectedDataItem.getUserId()).get();

    List<DataItem> items = user.getDataItems();
    assertAll("New user data 0",
        () -> assertEquals(expectedDataItem.getItemId(), items.get(0).getUserDataItemId()),
        () -> assertEquals(expectedDataItem.getItemName(), items.get(0).getUserDataItemName())
    );
    assertAll("Existing user data 1",
        () -> assertEquals(USER_1_DATA_1_ID, items.get(1).getUserDataItemId()),
        () -> assertEquals(USER_1_DATA_1_NAME, items.get(1).getUserDataItemName())
    );
    assertAll("Existing user data 2",
        () -> assertEquals(USER_1_DATA_2_ID, items.get(2).getUserDataItemId()),
        () -> assertEquals(USER_1_DATA_2_NAME, items.get(2).getUserDataItemName())
    );
  }


  @Test
  @DisplayName("Add data item for existing userId & itemId - Write conflict exception")
  void test_addConflict() {
    UserDataItem item = new UserDataItem()
        .setUserId(USER_1_ID)
        .setItemId(USER_1_DATA_1_ID)
        .setItemName("new user data item xxx");

    assertThrows(ConditionalCheckFailedException.class,
        () -> tested.addDataItem(item.getUserId(), item));
  }

  @Test
  @DisplayName("Update/replace collection for user 'user-1' and dataitem 'data-1'")
  void test_updateDataItem_validateNewCollection() {
    List<CollectionItem> expectedCollection = new ArrayList<>();
    expectedCollection.add(new CollectionItem()
        .setId("col-1")
        .setHeadline(USER_FOO_DATA_FOO_ID)
        .setSubheadline("bar")
        .setVisible(true)
    );

    UserDataItem item = new UserDataItem()
        .setUserId(USER_1_ID)
        .setItemId(USER_1_DATA_1_ID)
        .setItemName("new user data item xxx")
        .setCollection(expectedCollection);

    tested.udpateDataItem(item.getUserId(), item.getItemId(), item);

    Optional<UserDataItem> result = tested.getUserDataItem(item.getUserId(), item.getItemId());

    assertTrue(result.isPresent());
    assertAll("User collection should have only new collection items",
        () -> assertTrue(result.isPresent()),
        () -> assertEquals(expectedCollection.size(), result.get().getCollection().size()),
        () -> assertEquals(expectedCollection.get(0).getId(), result.get().getCollection().get(0).getId()),
        () -> assertEquals(expectedCollection.get(0).getHeadline(),
            result.get().getCollection().get(0).getHeadline()),
        () -> assertEquals(expectedCollection.get(0).getSubheadline(),
            result.get().getCollection().get(0).getSubheadline()),
        () -> assertEquals(expectedCollection.get(0).isVisible(), result.get().getCollection().get(0).isVisible())
    );
  }

  @Test
  @DisplayName("Update failed if userId/itemId pair does not exist")
  void test_updateNotExistingDataItem_throwsException() {
    UserDataItem updateItem = new UserDataItem()
        .setUserId(USER_1_ID)
        .setItemId(USER_1_DATA_1_ID)
        .setItemName("new user data item xxx")
        .setCollection(new ArrayList<>());

    assertThrows(ConditionalCheckFailedException.class,
        () -> tested.udpateDataItem("foo", updateItem.getItemId(), updateItem));

    assertThrows(ConditionalCheckFailedException.class,
        () -> tested.udpateDataItem(updateItem.getUserId(), "bar", updateItem));
  }

}