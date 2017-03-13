Background:
	Google recently started to disallow OAuth flow using webview on all Mobile platforms (iOS/Android/Windows) for usability and security reasons. The recommended way on Android is to use Chrome Custom Tabs when they're available, and fallback to a regular browser otherwise. Mobile Apps Android SDK uses webview in server-direct login flow in @{link LoginMananger}. We implements @{link CustomTabsLoginMananger} using Custom Tabs to replace webview login. We also support the PKCE extension (https://tools.ietf.org/html/rfc7636) to OAuth which was created to secure authorization codes in public clients when custom URI scheme redirects are used.

Diagram illustrates the server login flow using Custom Tabs:

 +--------------------------------+             bottom of backstack
 | Initiating Activity            |                     +
 |                                |                     |
 +--------------------------------+                     |
         |                    ^                         |
         |(1)                 |(7)                      |
         v                    |                         |
 +--------------------------------+                     |
 | CustomTabsIntermediateActivity |                     |
 |                                |                     |
 +--------------------------------+                     |
         |                    ^                         |
         |(2)                 |(6)                      |
         v                    |                         |
 +--------------------------------+                     |
 | CustomTabsLoginActivity        |                     |
 |                                |                     |
 +--------------------------------+                     |
         |                    ^                         |
         |(3)                 |                         |
         v                    |                         |
 +---------------------+      |                         |
 | CustomTabs Activity |      |                         |
 |         OR          |      |                         |
 | Browser Tab Activity|      |(5)                      |
 |                     |      |                         |
 +---------------------+      |                         |
         |                    |                         |
         |(4)                 |                         |
         v                    |                         |
 +--------------------------------+                     |
 | RedirectUrlActivity            |                     |
 |                                |                     V
 +--------------------------------+                 top of backstack
 
 
	(1) Your activity initiates the login flow by invoking {@link MobileServiceClient.login(String provider, String urlScheme, int requestCode)}. This will launch {@link CustomTabsIntermediateActivity} atop of your activity.
	(2) {@link CustomTabsIntermediateActivity} will launch {@link CustomTabsLoginActivity} in singleTask mode. SingleTask mode makes it possible to re-launch the same instance of {@link CustomTabsLoginActivity} later in the flow. {@link CustomTabsLoginActivity} constructs the proper url for login and calls Azure Mobile Apps backend by launching a Chrome CustomTab. If Chrome CustomTab is not available on the device, a regular browser will be launched instead.
	(3) A native login UI (from the authentication provider) will be presented to the user for username and password. User enters username and password and clicks login button. 
	(4) Azure Mobile Apps backend handles the login with the authentication provider. When login succeeds in the backend, Mobile Apps backend calls the redirectUrl with authorization_code. {@link RedirectUrlActivity} is registered with an intent-filter in AndroidManifest.xml to handle the redirectUrl coming from Mobile Apps backend. authorization_code will be used later for exchange of token.
	(5) {@link RedirectUrlActivity} launchs {@link CustomTabsLoginActivity} again with authorization_code. Because {@link CustomTabsLoginActivity} is a singleTask activity, any activities (RedirectUrlActivity and CustomTabs activity) on top of it will be closed.
	(6) {@link CustomTabsLoginActivity} now has the authorization_code. It will use the authorization_code to exchange for the x-zumo-auth JWT token with the backend. Once the code exchange succeeds and the client received the authenticated MobileServiceUser from the backend, {@link CustomTabsIntermediateActivity} will be launched again using FLAG_ACTIVITY_SINGLE_TOP and FLAG_ACTIVITY_CLEAR_TOP.
	(7) {@link CustomTabsIntermediateActivity} will pass the authenticated MobileServiceUser back to your acitivity that initiates the login flow. If code exchange failed in the previous step, an error message will be sent back to your activity. This comletes the server login flow.
 
 
Implementation details:

	(1) Allow only one login flow at a time. But multiple non-interleaving login flow in a row is okay.
	(2) When Chrome custom tabs is not available on the device, fallback to user's default browser. Tested on Samsung "Internet", Firefox & Opera browser.
	(3) In the event of system killing activities for memory conservation, this login flow still works for Custom Tabs and regular browser.
	(4) Your activity that used to start login flow can be sitting on any task, not limiting to the main/default task of your app.
 