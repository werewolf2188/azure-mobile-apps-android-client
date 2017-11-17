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

package com.microsoft.windowsazure.mobileservices.zumoe2etestapp.framework.log;

import android.util.Base64;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.microsoft.windowsazure.mobileservices.zumoe2etestapp.framework.TestCase;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;

public class StorageLogger {

    private String mToken;
    private String mContainerUrl;
    private String mRuntime;
    private String mPlatform = "Android";

    public StorageLogger(String containerUrl, String base64token, String runtime) {
        mToken = decodeToken(base64token);
        mContainerUrl = containerUrl;
        mRuntime = runtime;
    }

    private void uploadBlob(List<TestCase> tests, String containerUrl, String token) throws IOException {

        for (TestCase test : tests) {

            String blobName = UUID.randomUUID().toString() + ".txt";

            test.setFileName(getFullName() + "/" + blobName);

            String requestUrl = containerUrl + "/" + test.getFileName() + "?" + token;

            putBlob(requestUrl, test.getLog());
        }
    }

    private static void putBlob(String requestUrl, String log) throws IOException {
        OkHttpClient client = new OkHttpClient();

        RequestBody requestBody = RequestBody.create(MediaType.parse("UTF-8"), log);

        Request request = new Request.Builder()
                .url(requestUrl)
                .addHeader("x-ms-blob-type", "BlockBlob")
                .addHeader("Content-Type", "text/plain; charset=UTF-8")
                .put(requestBody)
                .build();

        client.newCall(request).execute();
    }

    private static String dateToString(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS'Z'", Locale.getDefault());
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

        return dateFormat.format(date);
    }

    private static String decodeToken(String base64Key) {
        byte[] valueDecoded = Base64.decode(base64Key, 0);
        return new String(valueDecoded);
    }

    private static Long getFileTime(Date date) {
        return (date.getTime() + 11644473600000L) * 10000L;
    }

    private String getFullName() {
        return mRuntime + "-" + mPlatform;
    }

    public void reportResults(final int failedTestCount, final int passedTestCount, final int skippedTestCount,
                              final Date startTime, final Date endTime, final List<TestCase> tests,
                              final Map<String, String> sourceMap) throws IOException {

        //upload individual log files
        uploadBlob(tests, mContainerUrl, mToken);

        //Post result for master test run
        String masterResultBlobUrl = mContainerUrl + "/" + getFullName() + "-master.json?" + mToken;
        String detailFileName = getFullName() + "-detail.json";

        JsonArray masterRunResult = createMasterRunResult(failedTestCount, passedTestCount, skippedTestCount, tests.size(), startTime, endTime, detailFileName);
        putBlob(masterResultBlobUrl, masterRunResult.toString());

        // post test results
        String testResultBlobUrl = mContainerUrl + "/" + detailFileName + "?" + mToken;

        JsonArray result = parseRunResult(tests, sourceMap);
        putBlob(testResultBlobUrl, result.toString());

    }

    private JsonArray createMasterRunResult(int failedTestCount, int passedTestCount, int skippedTestCount,
                                            int totalTestCount, Date startTime, Date endTime, String fileName) {
        JsonObject test = new JsonObject();
        test.addProperty("backend", mRuntime);
        test.addProperty("full_name", mPlatform);
        test.addProperty("outcome", failedTestCount == 0 ? "Passed" : "Failed");
        test.addProperty("start_time", getFileTime(startTime));
        test.addProperty("end_time", getFileTime(endTime));
        test.addProperty("reference_url", fileName);
        test.addProperty("passed", passedTestCount);
        test.addProperty("failed", failedTestCount);
        test.addProperty("skipped", skippedTestCount);
        test.addProperty("total_count", totalTestCount);

        JsonArray result = new JsonArray();
        result.add(test);

        return result;
    }

    private JsonArray parseRunResult(List<TestCase> tests, Map<String, String> sourceMap) {
        JsonArray result = new JsonArray();

        for (TestCase testCase : tests) {

            JsonObject test = new JsonObject();
            test.addProperty("full_name", testCase.getName());
            test.addProperty("source", sourceMap.get(testCase.getName()).replace(' ', '_'));
            test.addProperty("outcome", testCase.getStatus().name());
            test.addProperty("start_time", getFileTime(testCase.getStartTime()));
            test.addProperty("end_time", getFileTime(testCase.getEndTime()));
            test.addProperty("reference_url", testCase.getFileName());
            result.add(test);
        }

        return result;
    }
}
