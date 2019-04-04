package com.serverless.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaDescription;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaTitle;
import java.io.Serializable;
import java.util.List;

@JsonSchemaTitle("User Data Item")
@JsonSchemaDescription("Object represeting a single data item for single user. It consists a collection of data for that item")
public class UserDataItem implements Serializable {

  private static final long serialVersionUID = 7476634134542128150L;

  @JsonProperty(required = true)
  @JsonSchemaDescription("User Identifier")
  private String userId;

  @JsonProperty(required = true)
  @JsonSchemaDescription("Data Item Identifier")
  private String itemId;

  @JsonProperty(required = true)
  @JsonSchemaDescription("Data Item Name")
  private String itemName;

  @JsonProperty
  @JsonSchemaDescription("Collection of data")
  private List<CollectionItem> collection;

  public String getItemId() {
    return itemId;
  }

  public UserDataItem setItemId(String itemId) {
    this.itemId = itemId;
    return this;
  }

  public String getUserId() {
    return userId;
  }

  public UserDataItem setUserId(String userId) {
    this.userId = userId;
    return this;
  }

  public String getItemName() {
    return itemName;
  }

  public UserDataItem setItemName(String itemName) {
    this.itemName = itemName;
    return this;
  }

  public List<CollectionItem> getCollection() {
    return collection;
  }

  public UserDataItem setCollection(List<CollectionItem> collection) {
    this.collection = collection;
    return this;
  }

  public static class CollectionItem {

    @JsonProperty
    @JsonSchemaDescription("Id of collection item")
    private String id;

    @JsonProperty
    @JsonSchemaDescription("Data headline")
    private String headline;

    @JsonProperty
    @JsonSchemaDescription("Data subheadline")
    private String subheadline;

    @JsonProperty
    @JsonSchemaDescription("Flag if that item is marked as visible or not")
    private boolean visible;

    public String getId() {
      return id;
    }

    public CollectionItem setId(String id) {
      this.id = id;
      return this;
    }

    public String getHeadline() {
      return headline;
    }

    public CollectionItem setHeadline(String headline) {
      this.headline = headline;
      return this;
    }

    public String getSubheadline() {
      return subheadline;
    }

    public CollectionItem setSubheadline(String subheadline) {
      this.subheadline = subheadline;
      return this;
    }

    public boolean isVisible() {
      return visible;
    }

    public CollectionItem setVisible(boolean visible) {
      this.visible = visible;
      return this;
    }
  }
}
