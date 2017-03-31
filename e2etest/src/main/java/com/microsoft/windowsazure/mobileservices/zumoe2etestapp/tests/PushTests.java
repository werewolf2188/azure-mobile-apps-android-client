/*
Copyright (c) Microsoft Open Technologies, Inc.
All Rights Reserved
Apache 2.0 License
 
   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at
 
     http://www.apache.org/licenses/LICENSE-2.0
 
   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 
See the Apache Version 2.0 License for specific language governing permissions and limitations under the License.
 */
package com.microsoft.windowsazure.mobileservices.zumoe2etestapp.tests;

import android.util.Pair;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.authentication.MobileServiceUser;
import com.microsoft.windowsazure.mobileservices.http.HttpConstants;
import com.microsoft.windowsazure.mobileservices.notifications.MobileServicePush;
import com.microsoft.windowsazure.mobileservices.zumoe2etestapp.MainActivity;
import com.microsoft.windowsazure.mobileservices.zumoe2etestapp.framework.PushMessageManager;
import com.microsoft.windowsazure.mobileservices.zumoe2etestapp.framework.TestCase;
import com.microsoft.windowsazure.mobileservices.zumoe2etestapp.framework.TestExecutionCallback;
import com.microsoft.windowsazure.mobileservices.zumoe2etestapp.framework.TestGroup;
import com.microsoft.windowsazure.mobileservices.zumoe2etestapp.framework.TestResult;
import com.microsoft.windowsazure.mobileservices.zumoe2etestapp.framework.TestStatus;

import java.io.IOException;
import java.util.ArrayList;

public class PushTests extends TestGroup {

    public static MainActivity mainActivity;
    public static String registrationId;
    InstanceID iid;

    private final static String TOPIC_SPORTS = "topic:Sports";
    private final static String TOPIC_NEWS = "topic:News";

    public PushTests() {
        super("Push tests");

        this.addTest(createInitialDeleteRegistrationTest("Initial DeleteRegistration Test"));
        this.addTest(createRegistrationTest("Registration Test"));
        this.addTest(createLoginRegistrationTest("Login Registration Test"));
        this.addTest(createUnregistrationTest("Unregistration Test"));
        this.addTest(createPushWithTagsTest("Push Test with tags"));
        this.addTest(createTemplateRegistrationAndPushWithTagsTest("Push Test with template and tags"));
        this.addTest(createPushWithoutTagsTest("Push Test without template or tags"));
        this.addTest(createTemplateRegistrationAndPushWithoutTagsTest("Push Test with template"));
    }

    private ListenableFuture<JsonElement> deleteRegistrationsForChannel(final MobileServiceClient client, String registrationId) {

        final SettableFuture<JsonElement> resultFuture = SettableFuture.create();
        ArrayList parameters = new ArrayList<>();

        parameters.add(new Pair<>("channelUri", registrationId));
        ListenableFuture<JsonElement> serviceFilterFuture = client.invokeApi("deleteRegistrationsForChannel", HttpConstants.DeleteMethod, parameters);

        Futures.addCallback(serviceFilterFuture, new FutureCallback<JsonElement>() {
            @Override
            public void onFailure(Throwable exception) {
                resultFuture.setException(exception);
            }

            @Override
            public void onSuccess(JsonElement response) {
                resultFuture.set(response);
            }
        });

        return resultFuture;
    }

    private TestCase createInitialDeleteRegistrationTest(final String testName) {
        final TestCase deleteChannelTest = new TestCase() {
            @Override
            protected void executeTest(MobileServiceClient client, final TestExecutionCallback callback) {
                this.log("Starting InitialDeleteRegistrationTest");
                final TestResult result = new TestResult();
                result.setStatus(TestStatus.Passed);
                result.setTestCase(this);

                try {
                    String registrationId = getRegistrationId(client);
                    this.log("Acquired registrationId:" + registrationId);

                    JsonElement deleteChannelResult = deleteRegistrationsForChannel(client, registrationId).get();
                    if (!deleteChannelResult.isJsonNull()) {
                        this.log("deleteRegistrationsForChannel failed");
                        result.setStatus(TestStatus.Failed);
                        callback.onTestComplete(this, result);
                        return;
                    }

                    this.log("deleteRegistrationsForChannel successful");
                    clearRegistrationId();
                    this.log("Instance id cleared");
                    callback.onTestComplete(this, result);
                } catch (Exception e) {
                    callback.onTestComplete(this, this.createResultFromException(e));

                }
            }
        };

        deleteChannelTest.setName(testName);
        return deleteChannelTest;
    }

    private TestCase createRegistrationTest(String testName) {

        TestCase test = new TestCase(testName) {

            @Override
            protected void executeTest(final MobileServiceClient client, final TestExecutionCallback callback) {

                try {
                    this.log("Starting Registration Test");
                    final TestResult result = new TestResult();
                    result.setStatus(TestStatus.Passed);
                    result.setTestCase(this);

                    final MobileServicePush mobileServicePush = client.getPush();
                    String registrationId = getRegistrationId(client);
                    this.log("Acquired registrationId:" + registrationId);

                    mobileServicePush.register(registrationId).get();

                    ArrayList<Pair<String, String>> parameters = new ArrayList<>();
                    parameters.add(new Pair<>("channelUri", registrationId));
                    JsonElement registerResult = verifyRegisterInstallationResult(client, parameters).get();
                    if (!registerResult.getAsBoolean()) {
                        this.log("Register failed");
                        result.setStatus(TestStatus.Failed);
                        callback.onTestComplete(this, result);
                        return;
                    }

                    this.log("Verified registration");

                    mobileServicePush.unregister().get();
                    this.log("Unregistration done");
                    this.log("Test complete");
                    callback.onTestComplete(this, result);
                } catch (Exception e) {
                    callback.onTestComplete(this, createResultFromException(e));
                    return;
                }
            }
        };

        test.setName(testName);
        return test;
    }

    private TestCase createLoginRegistrationTest(String testName) {

        TestCase test = new TestCase(testName) {

            @Override
            protected void executeTest(final MobileServiceClient client, final TestExecutionCallback callback) {

                try {
                    this.log("Starting Login Registration Test");
                    final TestResult result = new TestResult();
                    result.setStatus(TestStatus.Passed);
                    result.setTestCase(this);

                    String registrationId = getRegistrationId(client);

                    this.log("Acquired registrationId:" + registrationId);

                    final MobileServicePush mobileServicePush = client.getPush();
                    MobileServiceUser dummyUser = getDummyUser(client).get();
                    client.setCurrentUser(dummyUser);

                    this.log("Setting current user to:" + dummyUser.getUserId());

                    mobileServicePush.register(registrationId).get();
                    ArrayList<Pair<String, String>> parameters = new ArrayList<>();
                    parameters.add(new Pair<>("channelUri", registrationId));
                    JsonElement registerResult = verifyRegisterInstallationResult(client, parameters).get();

                    if (!registerResult.getAsBoolean()) {
                        this.log("Register failed");
                        result.setStatus(TestStatus.Failed);
                        callback.onTestComplete(this, result);
                        return;
                    }

                    this.log("Verified registration");

                    mobileServicePush.unregister().get();

                    this.log("Unregistration done");
                    client.setCurrentUser(null);
                    this.log("Test complete");
                    callback.onTestComplete(this, result);
                } catch (Exception e) {
                    callback.onTestComplete(this, createResultFromException(e));
                    return;
                }
            }
        };

        test.setName(testName);
        return test;
    }

    private TestCase createUnregistrationTest(String testName) {

        TestCase test = new TestCase(testName) {

            @Override
            protected void executeTest(final MobileServiceClient client, final TestExecutionCallback callback) {

                try {
                    this.log("Starting UnRegistration Test");
                    final TestResult result = new TestResult();
                    result.setStatus(TestStatus.Passed);
                    result.setTestCase(this);
                    final MobileServicePush mobileServicePush = client.getPush();
                    mobileServicePush.unregister().get();

                    JsonElement unregisterResult = verifyUnregisterInstallationResult(client).get();

                    if (!unregisterResult.getAsBoolean()) {
                        this.log("Unregister failed");
                        result.setStatus(TestStatus.Failed);
                        callback.onTestComplete(this, result);
                        return;
                    }

                    this.log("Unregister complete");
                    this.log("Test complete");
                    callback.onTestComplete(this, result);
                } catch (Exception e) {
                    callback.onTestComplete(this, createResultFromException(e));
                    return;
                }
            }
        };

        test.setName(testName);
        return test;
    }

    private TestCase createPushWithoutTagsTest(String testName) {

        TestCase test = new TestCase(testName) {

            @Override
            protected void executeTest(final MobileServiceClient client, final TestExecutionCallback callback) {

                try {
                    this.log("Starting createPushWithoutTagsTest");
                    final TestResult result = new TestResult();
                    result.setStatus(TestStatus.Passed);
                    result.setTestCase(this);

                    final MobileServicePush mobileServicePush = client.getPush();
                    String registrationId = getRegistrationId(client);
                    this.log("Acquired registrationId:" + registrationId);

                    mobileServicePush.register(registrationId).get();
                    this.log("registration complete");

                    // push test for TOPIC_SPORTS
                    JsonObject item = new JsonObject();
                    item.addProperty("method", "send");
                    item.addProperty("token", "dummy");
                    item.addProperty("type", "gcm");

                    JsonObject sentPayload = new JsonObject();

                    JsonObject pushContent = new JsonObject();
                    pushContent.addProperty("sample", "PushTest");

                    JsonObject payload = new JsonObject();
                    payload.add("message", pushContent);

                    sentPayload.add("data", payload);
                    item.add("payload", sentPayload);

                    this.log("sending push message:" + sentPayload.toString());
                    client.invokeApi("Push", item).get();
                    if (!PushMessageManager.instance.isPushMessageReceived(30000, pushContent).get()) {
                        result.setStatus(TestStatus.Failed);
                        callback.onTestComplete(this, result);
                        return;
                    } else {
                        this.log(sentPayload.toString() + " message received");
                    }

                    mobileServicePush.unregister().get();
                    this.log("OnCompleted.");
                    callback.onTestComplete(this, result);
                } catch (Exception e) {
                    callback.onTestComplete(this, createResultFromException(e));
                    return;
                }
            }
        };

        test.setName(testName);

        return test;
    }

    private TestCase createPushWithTagsTest(String testName) {

        TestCase test = new TestCase(testName) {

            @Override
            protected void executeTest(final MobileServiceClient client, final TestExecutionCallback callback) {

                try {
                    this.log("Starting createPushWithTagsTest");
                    final TestResult result = new TestResult();
                    result.setStatus(TestStatus.Passed);
                    result.setTestCase(this);

                    final MobileServicePush mobileServicePush = client.getPush();
                    String registrationId = getRegistrationId(client);
                    this.log("Acquired registrationId:" + registrationId);

                    // Register "topic:Sports" tag, not "topic:News"
                    ArrayList<String> tags = new ArrayList<>();
                    tags.add(TOPIC_SPORTS);

                    mobileServicePush.register(registrationId, tags).get();
                    this.log("registration complete");

                    // Send push notification to device with "topic:Sports" tag,
                    // we should expect to get the notification
                    JsonObject item = new JsonObject();
                    item.addProperty("method", "send");
                    item.addProperty("token", "dummy");
                    item.addProperty("type", "gcm");

                    JsonObject sentPayload = new JsonObject();

                    JsonObject pushContent = new JsonObject();
                    pushContent.addProperty("sample", "message of sports");

                    JsonObject payload = new JsonObject();
                    payload.add("message", pushContent);

                    sentPayload.add("data", payload);
                    item.add("payload", sentPayload);
                    item.addProperty("tag", TOPIC_SPORTS);

                    this.log("sending push message:" + sentPayload.toString() + " with tag " + TOPIC_SPORTS);
                    client.invokeApi("Push", item).get();
                    if (!PushMessageManager.instance.isPushMessageReceived(30000, pushContent).get()) {
                        result.setStatus(TestStatus.Failed);
                        callback.onTestComplete(this, result);
                        return;
                    } else {
                        this.log(sentPayload.toString() + " message received because we registered tag " + TOPIC_SPORTS);
                    }

                    // Send push notification to device with "topic:News" tag,
                    // we won't get the notification because we didn't register for it
                    JsonObject item2 = new JsonObject();
                    item2.addProperty("method", "send");
                    item2.addProperty("token", "dummy");
                    item2.addProperty("type", "gcm");

                    JsonObject sentPayload2 = new JsonObject();

                    JsonObject pushContent2 = new JsonObject();
                    pushContent2.addProperty("sample", "message of news");

                    JsonObject payload2 = new JsonObject();
                    payload2.add("message", pushContent);

                    sentPayload2.add("data", payload2);
                    item2.add("payload", sentPayload2);
                    item2.addProperty("tag", TOPIC_NEWS);

                    this.log("sending push message:" + sentPayload2.toString()  + " with tag " + TOPIC_NEWS);
                    client.invokeApi("Push", item2).get();
                    if (PushMessageManager.instance.isPushMessageReceived(10000, pushContent2).get()) {
                        result.setStatus(TestStatus.Failed);
                        callback.onTestComplete(this, result);
                        return;
                    } else {
                        this.log("We don't receive message " + sentPayload2.toString() + " because we didn't register tag " + TOPIC_NEWS);
                    }

                    mobileServicePush.unregister().get();
                    this.log("OnCompleted.");
                    callback.onTestComplete(this, result);
                } catch (Exception e) {
                    callback.onTestComplete(this, createResultFromException(e));
                    return;
                }
            }
        };

        test.setName(testName);

        return test;
    }

    private TestCase createTemplateRegistrationAndPushWithTagsTest(String testName) {
        TestCase test = new TestCase(testName) {

            @Override
            protected void executeTest(final MobileServiceClient client, final TestExecutionCallback callback) {

                try {
                    final TestResult result = new TestResult();
                    result.setStatus(TestStatus.Passed);
                    result.setTestCase(this);

                    this.log("Starting createTemplateRegistrationAndPushWithTagsTest");

                    final MobileServicePush mobileServicePush = client.getPush();

                    String registrationId = getRegistrationId(client);
                    this.log("Acquired registrationId:" + registrationId);

                    JsonObject templateJson = GetTemplate();

                    ArrayList<String> tags = new ArrayList<>();
                    tags.add(TOPIC_SPORTS);

                    mobileServicePush.register(registrationId, templateJson, tags);

                    PushMessageManager.instance.clearMessages();

                    JsonObject pushMessage = new JsonObject();
                    pushMessage.addProperty("fullName", "John Doe");

                    JsonObject item = new JsonObject();
                    item.addProperty("method", "send");
                    item.addProperty("token", "dummy");
                    item.addProperty("type", "template");
                    item.add("payload", pushMessage);

                    JsonObject expectedPushMessage = new JsonObject();
                    expectedPushMessage.addProperty("user", "John Doe");

                    this.log("sending push message:" + pushMessage.toString() + " with tag " + TOPIC_SPORTS);
                    JsonElement jsonObject = client.invokeApi("Push", item).get();

                    if (!PushMessageManager.instance.isPushMessageReceived(30000, expectedPushMessage).get()) {
                        result.setStatus(TestStatus.Failed);
                        callback.onTestComplete(this, result);
                        return;
                    }

                    this.log("push received:" + expectedPushMessage.toString());

                    mobileServicePush.unregister().get();
                    this.log("unregistration complete");
                    this.log("OnCompleted: " + jsonObject.toString());
                    callback.onTestComplete(this, result);
                } catch (Exception e) {
                    callback.onTestComplete(this, createResultFromException(e));
                    return;
                }
            }
        };

        test.setName(testName);

        return test;
    }

    private TestCase createTemplateRegistrationAndPushWithoutTagsTest(String testName) {

        TestCase test = new TestCase(testName) {

            @Override
            protected void executeTest(final MobileServiceClient client, final TestExecutionCallback callback) {

                try {
                    final TestResult result = new TestResult();
                    result.setStatus(TestStatus.Passed);
                    result.setTestCase(this);

                    this.log("Starting createTemplateRegistrationAndPushWithoutTagsTest");

                    final MobileServicePush mobileServicePush = client.getPush();

                    String registrationId = getRegistrationId(client);
                    this.log("Acquired registrationId:" + registrationId);

                    JsonObject templateJson = GetTemplate();

                    mobileServicePush.register(registrationId, templateJson);

                    PushMessageManager.instance.clearMessages();

                    JsonObject pushMessage = new JsonObject();
                    pushMessage.addProperty("fullName", "John Doe");

                    JsonObject item = new JsonObject();
                    item.addProperty("method", "send");
                    item.addProperty("token", "dummy");
                    item.addProperty("type", "template");
                    item.add("payload", pushMessage);

                    JsonObject expectedPushMessage = new JsonObject();
                    expectedPushMessage.addProperty("user", "John Doe");

                    this.log("sending push message:" + item.toString());
                    JsonElement jsonObject = client.invokeApi("Push", item).get();

                    if (!PushMessageManager.instance.isPushMessageReceived(30000, expectedPushMessage).get()) {
                        result.setStatus(TestStatus.Failed);
                        callback.onTestComplete(this, result);
                        return;
                    }

                    this.log("push received:" + expectedPushMessage.toString());

                    mobileServicePush.unregister().get();
                    this.log("unregistration complete");
                    this.log("OnCompleted: " + jsonObject.toString());
                    callback.onTestComplete(this, result);
                } catch (Exception e) {
                    callback.onTestComplete(this, createResultFromException(e));
                    return;
                }
            }
        };

        test.setName(testName);

        return test;
    }

    public ListenableFuture<JsonElement> verifyRegisterInstallationResult(MobileServiceClient client, ArrayList<Pair<String, String>> parameters) throws IOException {

        final SettableFuture<JsonElement> resultFuture = SettableFuture.create();
        ListenableFuture<JsonElement> serviceFilterFuture = client.invokeApi("verifyRegisterInstallationResult", HttpConstants.GetMethod, parameters);

        Futures.addCallback(serviceFilterFuture, new FutureCallback<JsonElement>() {
            @Override
            public void onFailure(Throwable exception) {
                resultFuture.setException(exception);
            }

            @Override
            public void onSuccess(JsonElement response) {
                resultFuture.set(response);
            }
        });

        return resultFuture;
    }

    public ListenableFuture<MobileServiceUser> getDummyUser(MobileServiceClient client) {

        final SettableFuture<MobileServiceUser> resultFuture = SettableFuture.create();
        ListenableFuture<JsonElement> serviceFilterFuture = client.invokeApi("JwtTokenGenerator", HttpConstants.GetMethod, (ArrayList) null);

        Futures.addCallback(serviceFilterFuture, new FutureCallback<JsonElement>() {
            @Override
            public void onFailure(Throwable exception) {
                resultFuture.setException(exception);
            }

            @Override
            public void onSuccess(JsonElement response) {
                JsonObject userJsonObject = response.getAsJsonObject();
                String userId = userJsonObject.getAsJsonPrimitive("userId").getAsString();
                String authenticationToken = userJsonObject.getAsJsonPrimitive("authenticationToken").getAsString();
                MobileServiceUser user = new MobileServiceUser(userId);
                user.setAuthenticationToken(authenticationToken);
                resultFuture.set(user);

            }
        });

        return resultFuture;
    }

    public String getRegistrationId(MobileServiceClient client) throws IOException {
        if (this.iid == null) {
            this.iid = InstanceID.getInstance(client.getContext());
        }
        if (this.registrationId == null) {
            String senderId = mainActivity.getGCMSenderId();
            this.registrationId = iid.getToken(senderId, GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
        }
        return this.registrationId;
    }

    public void clearRegistrationId() throws IOException {
        if (this.iid == null || this.registrationId == null) {
            return;
        }

        registrationId = null;
        iid.deleteInstanceID();
    }

    public ListenableFuture<JsonElement> verifyUnregisterInstallationResult(final MobileServiceClient client) {

        final SettableFuture<JsonElement> resultFuture = SettableFuture.create();

        ListenableFuture<JsonElement> serviceFilterFuture = client.invokeApi("verifyUnregisterInstallationResult", HttpConstants.GetMethod, new ArrayList<Pair<String, String>>());

        Futures.addCallback(serviceFilterFuture, new FutureCallback<JsonElement>() {
            @Override
            public void onFailure(Throwable exception) {
                resultFuture.setException(exception);
            }

            @Override
            public void onSuccess(JsonElement response) {
                resultFuture.set(response);
            }
        });

        return resultFuture;
    }


    private JsonObject GetTemplate() {
        String templateName = "GcmTemplate";

        JsonObject userJson = new JsonObject();
        userJson.addProperty("user", "$(fullName)");

        JsonObject messageJson = new JsonObject();
        messageJson.add("message", userJson);

        JsonObject dataJson = new JsonObject();
        dataJson.add("data", messageJson);

        JsonObject bodyJson = new JsonObject();
        bodyJson.add("body", dataJson);

        JsonObject templateJson = new JsonObject();
        templateJson.add(templateName, bodyJson);
        return templateJson;
    }
}