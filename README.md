# My AWS Serverless playground

## Prerequisites

* Java 8
* Node.js v6.5.0 or later.
* Serverless framework (`npm install -g serverless`)
* An AWS account. If you don't already have one, you can [sign up](https://aws.amazon.com/s/dm/optimization/server-side-test/free-tier/free_np/) for a free trial to get 1 million Lambda req/month for free.
  * Follow the short [video tutorial](https://www.youtube.com/watch?v=KngM5bfpttA) in order to setup AWS account/users for the serverless framework purposes.

## What's inside
1. Java AWS lambda functions to manage imaginary users data stored on DynamoDB
2. API Gateway configuration exposes simple REST API for Lambdas
3. Serveless framework stitches together above to simplify AWS deployment

## Quick start
### Build java
```
$> ./gradlew
```
- It will compile Java lambda handlers and package them into zip file to be uploaded to AWS
- Generates JSON Schema (draft#4) for all POJO classes a Lambda handlers accepts and produces (requests/responses). Schemas to be used by API Gateway request validators and to document API.
- Runs the unit tests of the DynamoDB DAO implementations using **local Dynamo DB** instance
### Deploy to AWS
```
$> sls deploy -v
```
Assuming you configured AWS account as in prerequisites a commoand will provision all AWS resources needed, such as:
- Lambda
- API Gateway
- DynamoDB
- Required ACLs
- S3 bucket to store function code

You should get summary information once command successfully completes.
```
Service Information
service: users-services
stage: dev
region: eu-central-1
stack: users-services-dev
resources: 38
api keys:
  None
endpoints:
  GET - https://<API_ID>.execute-api.eu-central-1.amazonaws.com/dev/users
  GET - https://<API_ID>.execute-api.eu-central-1.amazonaws.com/dev/users/{userId}
  GET - https://<API_ID>.execute-api.eu-central-1.amazonaws.com/dev/users/{userId}/{itemId}
  POST - https://<API_ID>.execute-api.eu-central-1.amazonaws.com/dev/users/{userId}
  PUT - https://<API_ID>.execute-api.eu-central-1.amazonaws.com/dev/users/{userId}/{itemId}
functions:
  getUsers: users-services-dev-getUsers
  getUserData: users-services-dev-getUserData
  getUserDataItem: users-services-dev-getUserDataItem
  addDataItem: users-services-dev-addDataItem
  updateDataItem: users-services-dev-updateDataItem
layers:
  None

Stack Outputs
AddDataItemLambdaFunctionQualifiedArn: arn:aws:lambda:eu-central-1:<ACCOUNT_ID>:function:users-services-dev-addDataItem:3
GetUsersLambdaFunctionQualifiedArn: arn:aws:lambda:eu-central-1:<ACCOUNT_ID>:function:users-services-dev-getUsers:3
UpdateDataItemLambdaFunctionQualifiedArn: arn:aws:lambda:eu-central-1:<ACCOUNT_ID>:function:users-services-dev-updateDataItem:3
AwsDocApiId: <API_ID>
GetUserDataItemLambdaFunctionQualifiedArn: arn:aws:lambda:eu-central-1:<ACCOUNT_ID>:function:users-services-dev-getUserDataItem:3
ServiceEndpoint: https://1d8ifo0lee.execute-api.eu-central-1.amazonaws.com/dev
ServerlessDeploymentBucketName: users-services-dev-serverlessdeploymentbucket-1tagv2hj2unud
GetUserDataLambdaFunctionQualifiedArn: arn:aws:lambda:eu-central-1:<ACCOUNT_ID>:function:users-services-dev-getUserData:3
```

### Test it
The service information after sls deploy shows you a list of endpoints created on API gateway that you can use to access your functions.
> If you forgot those endpoints, you can always run `sls info` command

If your'e using IntelliJ open `/sample-data/lambdas.http` and run HTTP requests specified there.
in the request URL, change `{{apiid}}` into the value of `AwsDocApiId` or create a file `/sample-data/rest-client.env.json`
```
{
  "private-aws": {
    "apiid": "<Your AwsDocApiId>"
  }
}
```
And then, when running requests from the sheet you can pick your profile.


## TODO
- UI interface (e.g. ract app on S3)
- Authentication via API gateway authorizers + AWS Cognito
- API Gateway websockets with Lambda
- Layered lambdas (libs layer + business logic layer)
- Cold start perfomance tests
- Build as native image with Graalvm (or go towards Quarkus to make native image fiendly implementation) - to get lower cold start for java implementation