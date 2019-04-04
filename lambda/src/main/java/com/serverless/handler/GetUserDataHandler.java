package com.serverless.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.serverless.RequestUtils;
import com.serverless.dao.UserDataDao;
import com.serverless.dao.impl.UserDataDaoImpl;
import com.serverless.model.UserData;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

public class GetUserDataHandler implements
    RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

  private static final Logger LOG = Logger.getLogger(GetUserDataHandler.class);

  @Override
  public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input, Context context) {
    APIGatewayProxyResponseEvent response;

    String userId = input.getPathParameters().get("userId");
    if (StringUtils.isBlank(userId)) {
      return RequestUtils.errorResponse(400, "Missing userId");
    }

    UserDataDao dao = new UserDataDaoImpl();
    LOG.debug(String.format("Get %s user from DB", userId));
    Optional<UserData> user = dao.getUser(userId);

    if (user.isPresent()) {
      response = RequestUtils.successResponse(user.get());
    } else {
      response = RequestUtils.errorResponse(404, String.format("User <%s> not found", userId));
    }

    return response;
  }
}
