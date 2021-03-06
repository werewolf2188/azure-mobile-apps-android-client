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
package com.microsoft.windowsazure.mobileservices.sdk.testapp.framework.filters;

import android.net.Uri;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.gson.JsonObject;
import com.microsoft.windowsazure.mobileservices.http.NextServiceFilterCallback;
import com.microsoft.windowsazure.mobileservices.http.ServiceFilter;
import com.microsoft.windowsazure.mobileservices.http.ServiceFilterRequest;
import com.microsoft.windowsazure.mobileservices.http.ServiceFilterResponse;
import okhttp3.Headers;
import okhttp3.Protocol;
import okhttp3.internal.http.StatusLine;

public class HttpMetaEchoFilter implements ServiceFilter {

    @Override
    public ListenableFuture<ServiceFilterResponse> handleRequest(ServiceFilterRequest request, NextServiceFilterCallback nextServiceFilterCallback) {

        JsonObject jResponse = new JsonObject();

        jResponse.addProperty("method", request.getMethod());

        Headers headers = request.getHeaders();
        if (headers != null && headers.size() > 0) {
            JsonObject jHeaders = new JsonObject();

            for (int i = 0; i < headers.size(); i++) {
                jHeaders.addProperty(headers.name(i), headers.value(i));
            }

            jResponse.add("headers", jHeaders);
        }

        Uri uri = Uri.parse(request.getUrl());
        String query = uri.getQuery();

        if (query != null && query.trim() != "") {
            JsonObject jParameters = new JsonObject();

            for (String parameter : query.split("&")) {
                jParameters.addProperty(parameter.split("=")[0], parameter.split("=")[1]);
            }
            jResponse.add("parameters", jParameters);
        }

        ServiceFilterResponseMock response = new ServiceFilterResponseMock();
        response.setContent(jResponse.toString());
        response.setStatus(new StatusLine(Protocol.HTTP_2, 200, ""));

        ServiceFilterRequestMock requestMock = new ServiceFilterRequestMock(response);
        return nextServiceFilterCallback.onNext(requestMock);

        //return nextServiceFilterCallback.onNext(request);
    }
}
