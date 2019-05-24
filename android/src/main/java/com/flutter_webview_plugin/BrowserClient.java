package com.flutter_webview_plugin;

import android.graphics.Bitmap;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.annotation.TargetApi;
import android.os.Build;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by lejard_h on 20/12/2017.
 */

public class BrowserClient extends WebViewClient {
    public BrowserClient() {
        super();
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        super.onPageStarted(view, url, favicon);
        Map<String, Object> data = new HashMap<>();
        data.put("url", url);
        data.put("type", "startLoad");
        FlutterWebviewPlugin.channel.invokeMethod("onState", data);
    }

    @Override
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public void onPageFinished(WebView view, String url) {
        super.onPageFinished(view, url);
        Map<String, Object> data = new HashMap<>();
        data.put("url", url);

        FlutterWebviewPlugin.channel.invokeMethod("onUrlChanged", data);
        String listenPostMessagesScript =
                "if (!window.breezReceiveMessage) {" +
                "   window.breezReceiveMessage = function(event) {Android.getPostMessage(event.data);};" +
                "   window.addEventListener('message', window.breezReceiveMessage, false);" +
                "}";
        view.evaluateJavascript(listenPostMessagesScript, null);

        String includeWebLN =
                        "var webLnLib = document.createElement(\"script\");\n" +
                        "webLnLib.type = \"text/javascript\";\n" +
                        "webLnLib.src = \"https://unpkg.com/webln@0.2.0/dist/webln.min.js\";\n" +
                        "document.body.appendChild(webLnLib);";
        view.evaluateJavascript(includeWebLN, null);

        String initWebLN =
                        "var webln = {\n" +
                        "   enable: function () {\n" +
                        "       window.postMessage(JSON.stringify({ enable: true }));\n" +
                        "       return new Promise(function (resolve, reject) {resolve(true);});\n" +
                        "   },\n" +
                        "   sendPayment: function (paymentRequest) {\n" +
                        "       window.postMessage(JSON.stringify({ uri: paymentRequest }));\n" +
                        "       return new Promise(function (resolve, reject) { });\n" +
                        "   },\n" +
                        "};\n" +
                        "setTimeout(function () { WebLN.requestProvider();},450);";
        view.evaluateJavascript(initWebLN, null);

        String listenLNLinks =
                        "setInterval(function () {\n" +
                        "   var searchText = \"lnbc\";\n" +
                        "   var aTags = document.getElementsByTagName(\"span\");\n" +
                        "   var i;\n" +
                        "   for (i = 0; i < aTags.length; i++) {\n" +
                        "       if (aTags[i].textContent.indexOf(searchText) === 0) {\n" +
                        "           webln.sendPayment(aTags[i].textContent);\n" +
                        "       }\n" +
                        "   }\n" +
                        "   /* ------------------------- */\n" +
                        "   aTags = document.getElementsByTagName(\"input\");\n" +
                        "   for (i = 0; i < aTags.length; i++) {\n" +
                        "       if (aTags[i].value.indexOf(searchText) === 0) {\n" +
                        "           webln.sendPayment(aTags[i].value);\n" +
                        "       }\n" +
                        "   }\n" +
                        "   /* ------------------------- */\n" +
                        "   aTags = document.getElementsByTagName(\"a\");\n" +
                        "   searchText = \"lightning:lnbc\";\n" +
                        "   for (i = 0; i < aTags.length; i++) {\n" +
                        "       var href = aTags[i].getAttribute('href') + '';\n" +
                        "       if (href.indexOf(searchText) === 0) {\n" +
                        "           webln.sendPayment(href.replace('lightning:', ''));\n" +
                        "       }\n" +
                        "   }\n" +
                        "}, 500);";
        view.evaluateJavascript(listenLNLinks, null);

        data.put("type", "finishLoad");
        FlutterWebviewPlugin.channel.invokeMethod("onState", data);

    }

    @Override
    public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
        super.onReceivedHttpError(view, request, errorResponse);
        Map<String, Object> data = new HashMap<>();
        data.put("url", request.getUrl().toString());
        data.put("code", Integer.toString(errorResponse.getStatusCode()));
        FlutterWebviewPlugin.channel.invokeMethod("onHttpError", data);
    }
}