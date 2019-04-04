package com.serverless;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.google.gson.Gson;
import com.serverless.model.ErrorResponse;

public class RequestUtils {

  private static final Gson gson = new Gson();

  public static APIGatewayProxyResponseEvent successResponse(Object object) {
    APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();

    return response.withStatusCode(200).withBody(gson.toJson(object));
  }

  public static APIGatewayProxyResponseEvent errorResponse(int errorCode, String message) {
    APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();

    ErrorResponse errorResponse = new ErrorResponse();
    errorResponse.setMessage(message);
    errorResponse.setStatusCode(errorCode);

    return response.withStatusCode(errorCode).withBody(gson.toJson(errorResponse));
  }

}
