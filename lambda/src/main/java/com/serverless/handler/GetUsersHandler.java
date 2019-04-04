package com.serverless.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.serverless.RequestUtils;
import com.serverless.dao.impl.UserDataDaoImpl;
import com.serverless.model.Users;

public class GetUsersHandler implements
    RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

  @Override
  public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input,
      Context context) {
    return RequestUtils.successResponse(new Users().setUserIds(new UserDataDaoImpl().getAllUsers()));
  }

}
