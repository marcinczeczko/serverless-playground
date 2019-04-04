package com.serverless.handler;

import com.amazonaws.services.dynamodbv2.model.ConditionalCheckFailedException;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.google.gson.Gson;
import com.serverless.RequestUtils;
import com.serverless.dao.UserDataDao;
import com.serverless.dao.impl.UserDataDaoImpl;
import com.serverless.model.UserDataItem;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

public class AddUserDataHandler implements
    RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

  private static final Logger LOG = Logger.getLogger(AddUserDataHandler.class);
  private static final Gson gson = new Gson();
  private UserDataDao dao;

  public AddUserDataHandler() {
    this.dao = new UserDataDaoImpl();
  }

  /**
   * Constructor for unit testing purposes
   */
  public AddUserDataHandler(UserDataDao dao) {
    this.dao = dao;
  }

  @Override
  public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent request,
      Context context) {

    APIGatewayProxyResponseEvent response;
    String contentType = request.getHeaders().get("Content-Type");

    if (contentType == null || !contentType.matches("application/json")) {
      return RequestUtils.errorResponse(415, "[Lambda] application/json is allowed only");
    }
    String userId = request.getPathParameters().get("userId");
    if (StringUtils.isBlank(userId)) {
      return RequestUtils.errorResponse(400, "Missing userId");
    }

    UserDataItem dataItem = gson.fromJson(request.getBody(), UserDataItem.class);
    try {
      dao.addDataItem(userId, dataItem);

      response = RequestUtils.successResponse(dataItem);
    } catch (ConditionalCheckFailedException ex) {
      response = RequestUtils
          .errorResponse(409, String.format("userId<%s>, dataItem<%s> already exists", dataItem.getUserId(), dataItem.getItemId()));
    } catch (Exception ex) {
      LOG.error("Error adding user data item", ex);
      response = RequestUtils.errorResponse(400, "Couldn't add user data item");
    }

    return response;
  }


}
