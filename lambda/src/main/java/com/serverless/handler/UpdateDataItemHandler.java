package com.serverless.handler;

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

public class UpdateDataItemHandler implements
    RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

  private static final Logger LOG = Logger.getLogger(UpdateDataItemHandler.class);
  private static final Gson gson = new Gson();

  @Override
  public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent request, Context context) {
    APIGatewayProxyResponseEvent response;
    String contentType = request.getHeaders().get("Content-Type");

    if (!contentType.matches("application/json")) {
      return RequestUtils.errorResponse(415, "[Lambda] application/json is allowed only");
    }
    String userId = request.getPathParameters().get("userId");
    String itemId = request.getPathParameters().get("itemId");
    if (StringUtils.isBlank(userId) || StringUtils.isBlank(itemId)) {
      return RequestUtils.errorResponse(400, "Missing userId or itemId");
    }

    UserDataDao dao = new UserDataDaoImpl();
    UserDataItem dataItem = gson.fromJson(request.getBody(), UserDataItem.class);
    try {
      dao.udpateDataItem(userId, itemId, dataItem);

      response = RequestUtils.successResponse(dataItem);
    } catch (Exception ex) {
      LOG.error("Error updating user data item", ex);
      response = RequestUtils.errorResponse(400, "Couldn't update user data item");
    }

    return response;

  }
}
