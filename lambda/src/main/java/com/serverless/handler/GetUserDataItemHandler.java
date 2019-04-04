package com.serverless.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.serverless.RequestUtils;
import com.serverless.dao.UserDataDao;
import com.serverless.dao.impl.UserDataDaoImpl;
import com.serverless.model.UserDataItem;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

public class GetUserDataItemHandler implements
    RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

  private static final Logger LOG = Logger.getLogger(GetUserDataItemHandler.class);

  @Override
  public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input, Context context) {
    APIGatewayProxyResponseEvent response;

    String userId = input.getPathParameters().get("userId");
    String itemId = input.getPathParameters().get("itemId");
    if (StringUtils.isBlank(userId) || StringUtils.isBlank(itemId)) {
      return RequestUtils.errorResponse(400, "Missing userId or itemId");
    }

    UserDataDao dao = new UserDataDaoImpl();
    LOG.debug(String.format("Get %s user data %s from DB", userId, itemId));
    Optional<UserDataItem> userDataItem = dao.getUserDataItem(userId, itemId);

    if (userDataItem.isPresent()) {
      response = RequestUtils.successResponse(userDataItem.get());
    } else {
      response = RequestUtils.errorResponse(404, String.format("UserId <%s> ItemId <%s> not found", userId, itemId));
    }

    return response;
  }
}
