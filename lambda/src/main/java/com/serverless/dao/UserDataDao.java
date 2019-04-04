package com.serverless.dao;

import com.serverless.model.UserData;
import com.serverless.model.UserDataItem;
import java.util.Optional;
import java.util.Set;

public interface UserDataDao {

  /**
   * Gets all user Ids
   */
  Set<String> getAllUsers();

  /**
   * Gets user data
   */
  Optional<UserData> getUser(String userId);

  /**
   * Get userDataItem for a given userId and dataItemId
   */
  Optional<UserDataItem> getUserDataItem(String userId, String userDataItemId);


  /**
   * Adds dataItem for a given user
   */
  void addDataItem(String userId, UserDataItem dataItem);

  /**
   * Adds collectionItem to given dataItem for a given user
   */
  void udpateDataItem(String userId, String dataItemId, UserDataItem dataItem);

}
