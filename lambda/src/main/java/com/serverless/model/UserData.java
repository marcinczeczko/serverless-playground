package com.serverless.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaDescription;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaTitle;
import java.io.Serializable;
import java.util.List;
import javax.validation.constraints.NotEmpty;

@JsonSchemaTitle("User Data schema")
@JsonSchemaDescription("Object represeting a single user. It consists array of references (ids) to actual data items together with names")
public class UserData implements Serializable {

  private static final long serialVersionUID = 4797351553301671217L;

  @JsonProperty(required = true)
  @NotEmpty
  @JsonSchemaDescription("User Identifier")
  private String userId;

  @JsonProperty(required = true)
  @JsonSchemaDescription("Array of user data items")
  private List<DataItem> dataItems;

  public String getUserId() {
    return userId;
  }

  public UserData setUserId(String userId) {
    this.userId = userId;
    return this;
  }

  public List<DataItem> getDataItems() {
    return dataItems;
  }

  public UserData setDataItems(List<DataItem> dataItems) {
    this.dataItems = dataItems;
    return this;
  }

  public static class DataItem {

    @JsonProperty(required = true)
    @JsonSchemaDescription("Data Item Identifier")
    private String userDataItemId;

    @JsonProperty(required = true)
    @JsonSchemaDescription("Data Item human readable name")
    private String userDataItemName;

    public String getUserDataItemId() {
      return userDataItemId;
    }

    public DataItem setUserDataItemId(String userDataItemId) {
      this.userDataItemId = userDataItemId;
      return this;
    }

    public String getUserDataItemName() {
      return userDataItemName;
    }

    public DataItem setUserDataItemName(String userDataItemName) {
      this.userDataItemName = userDataItemName;
      return this;
    }
  }
}
