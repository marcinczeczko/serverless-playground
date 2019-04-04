package com.serverless.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.willThrow;

import com.amazonaws.services.dynamodbv2.model.ConditionalCheckFailedException;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.google.gson.Gson;
import com.serverless.dao.UserDataDao;
import com.serverless.model.UserDataItem;
import java.util.Collections;
import java.util.HashMap;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@DisplayName("AddUserDataHandler mocked Dynamodb unit tests")
@ExtendWith(MockitoExtension.class)
class AddUserDataHandlerTest {

  private static final Gson gson = new Gson();

  @Mock
  private UserDataDao daoMock;

  @Test
  void test_addUserData_without_contentType() {
    APIGatewayProxyRequestEvent request = new APIGatewayProxyRequestEvent().withHeaders(new HashMap<>());
    AddUserDataHandler handler = new AddUserDataHandler(daoMock);
    Context context = createContext();

    APIGatewayProxyResponseEvent response = handler.handleRequest(request, context);

    assertEquals(415, response.getStatusCode().intValue());
  }

  @Test
  void test_addUserData_without_pathparam() {
    APIGatewayProxyRequestEvent request = new APIGatewayProxyRequestEvent().withHeaders(Collections.singletonMap("Content-Type", "application/json"))
        .withPathParameters(new HashMap<>());

    AddUserDataHandler handler = new AddUserDataHandler(daoMock);
    Context context = createContext();

    APIGatewayProxyResponseEvent response = handler.handleRequest(request, context);

    assertEquals(400, response.getStatusCode().intValue());
  }

  @Test
  void test_addUserData_conflict() {
    String conflictUserId = "conflict";
    APIGatewayProxyRequestEvent request = new APIGatewayProxyRequestEvent()
        .withHeaders(Collections.singletonMap("Content-Type", "application/json"))
        .withPathParameters(Collections.singletonMap("userId", conflictUserId))
        .withBody("{}");

    willThrow(ConditionalCheckFailedException.class).given(daoMock).addDataItem(eq(conflictUserId), any(UserDataItem.class));

    AddUserDataHandler handler = new AddUserDataHandler(daoMock);
    Context context = createContext();

    APIGatewayProxyResponseEvent response = handler.handleRequest(request, context);

    assertEquals(409, response.getStatusCode().intValue());
  }

  private Context createContext() {
    TestContext ctx = new TestContext();
    ctx.setFunctionName("LambdaTest");
    return ctx;
  }
}