package com.serverless.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaDescription;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaTitle;
import java.util.Set;

@JsonSchemaTitle("Users list")
public class Users {

  @JsonProperty(required = true)
  @JsonSchemaDescription("A set of user Ids available")
  private Set<String> userIds;

  public Set<String> getUserIds() {
    return userIds;
  }

  public Users setUserIds(Set<String> userIds) {
    this.userIds = userIds;
    return this;
  }
}
