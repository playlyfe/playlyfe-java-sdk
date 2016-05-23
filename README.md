![Playlyfe Java and Android SDK](https://dev.playlyfe.com/images/assets/pl-java-sdk.png "Playlyfe Java SDK")

Playlyfe Java and Android SDK [![Maven](http://img.shields.io/maven-central/v/com.playlyfe/playlyfe-java-sdk.svg)](http://search.maven.org/#artifactdetails|com.playlyfe|playlyfe-java-sdk|0.2.0|jar)
=================

This is the official OAuth 2.0 Java and Android client SDK for the Playlyfe API. It has support for both synchronous and asynchronous calls.
It supports the `client_credentials` and `authorization code` OAuth 2.0 flows.
For a complete API Reference checkout [Playlyfe Developers](https://dev.playlyfe.com/docs/api.html) for more information.

# Examples
The Playlyfe class allows you to make rest api calls like GET, POST, .. etc.
To get started create a new playlyfe object using client credentials flow and then start making requests
**For api v2**
```java
Playlyfe playlyfe = new Playlyfe("Your client id", "Your client secret", null, "v2");
HashMap<String, String> player_id = new HashMap<String, String>();
player_id.put("player_id", "student1");
// To get infomation of a  player
Map<String, Object> player = (Map<String, Object>)playlyfe.get("/runtime/player", player_id);
System.out.println(student1.get("id"));
System.out.println(student1.get("alias"));

// To get all available processes
Object processes = playlyfe.get("/runtime/processes", player_id);
System.out.println(processes);
// To start a process
HashMap<String, String> body = new HashMap<String, String>();
body.put("name", "patched_process");
body.put("definition", "collect");
process =  playlyfe.post("/runtime/processes/",player_id, body);

//To play a process
HashMap<String, String> body = new HashMap<String, String>();
body.put("trigger", "collect");
playlyfe.post("/runtime/processes/"+process_id+"/play", player_id, body);

// A PLaylyfeException is thrown when an error from the playlyfe platform is returned on a request
try {
  playlyfe.get("/unknown", null);
}
catch(PlaylyfeException err) {
  System.out.println (err.getName()); // route_not_found
  System.out.println (err.getMessage()); // This route does not exist
}
```

## Requires
Java >= 1.7
or
Android >= 2.3

## Install
if you are using gradle then
```java
compile "com.playlyfe:playlyfe-java-sdk:1.1.0"
```
or if you prefer to use maven
```xml
<dependency>
    <groupId>com.playlyfe</groupId>
    <artifactId>playlyfe-java-sdk</artifactId>
    <version>1.1.0</version>
</dependency>
```
or if you just want the jar then copy `playlyfe-java-sdk-all.jar` to your libs folder. It contains the sdk with all dependencies baked in it.
```yaml
-build
  - libs
    - playlyfe-java-sdk-all.jar # use this
```

## Using
### Create a client
  If you haven't created a client for your game yet just head over to [Playlyfe](http://playlyfe.com) and login into your account, and go to the game settings and click on client.

###1. Client Credentials Flow
In the client page select Yes for both the first and second questions
![client](https://cloud.githubusercontent.com/assets/1687946/7930229/2c2f14fe-0924-11e5-8c3b-5ba0c10f066f.png)
```java
import com.playlyfe.sdk.Playlyfe;

Playlyfe playlyfe = new Playlyfe("Your client id", "Your client secret", null)
```
###2. Authorization Code Flow
In the client page select yes for the first question and no for the second
![auth](https://cloud.githubusercontent.com/assets/1687946/7930231/2c31c1fe-0924-11e5-8cb5-73ca0a002bcb.png)
```java
import com.playlyfe.sdk.Playlyfe;

Playlyfe playlyfe = new Playlyfe("Your client id", "Your client secret", "Your redirect URI", null)
```
In development the sdk caches the access token in memory so you don"t need to  the persist access token object. But in production it is highly recommended to persist the token to a database. It is very simple and easy to do it with redis. You can see the test cases for more examples.
You need to return a HashMap<String, Object> which has the keys access_token and expires_at.
The access token in fetched on the first request you make and so in the load method you need to return null if your database hasn't stored an access token yet.
```java
import com.playlyfe.sdk.Playlyfe;
import com.playlyfe.sdk.Playlyfe.PersistAccessToken;

final HashMap<String, Object> access_token = new HashMap<String, Object>();
Playlyfe pl = new Playlyfe("Your client id", "Your client secret", new PersistAccessToken(){

    // This will persist the access token to a database. You have to persist the token to a database if you want the access token to remain the same in every request
    @Override
    public void store(Map<String, Object> token) {
        System.out.println("Storing Access Token");
        access_token.put("expires_at", token.get("expires_at"));
        access_token.put("access_token", token.get("access_token"));
    }

    // This will load the access token. This is called internally by the sdk on every request so that the persisted access token can be used between requests
    @Override
    public Map<String, Object> load() {
        System.out.println("Loading Access Token");
        System.out.println("Current Time: "+System.currentTimeMillis());
        System.out.println("Expires At: "+access_token.get("expires_at"));
        if (access_token.get("expires_at") == null) { // This means there is not access token, so this will trigger the sdk to get an access token
            return null;
        }
        return access_token;
    }

});
```
## 3. Custom Login Flow using JWT(JSON Web Token)
In the client page select no for the first question and yes for the second
![jwt](https://cloud.githubusercontent.com/assets/1687946/7930230/2c2f2caa-0924-11e5-8dcf-aed914a9dd58.png)
```java
import com.playlyfe.sdk.Playlyfe;

String[] scopes = {"player.runtime.read", "player.runtime.write"};
String token = Playlyfe.createJWT("your client_id", "your client_secret",
    "player_id", // The player id associated with your user
    scopes, // The scopes the player has access to
    3600; // 1 hour expiry Time
);
```
This is used to create jwt token which can be created when your user is authenticated. This token can then be sent to the frontend and or stored in your session. With this token the user can directly send requests to the Playlyfe API as the player.

# Client Scopes
![Client](https://cloud.githubusercontent.com/assets/1687946/9349193/e00fe91c-465f-11e5-8094-6e03c64a662c.png)

Your client has certain access control restrictions. There are 3 kind of resources in the Playlyfe REST API they are,

1.`/admin` -> routes for you to perform admin actions like making a player join a team

2.`/design` -> routes for you to make design changes programmatically

3.`/runtime` -> routes which the users will generally use like getting a player profile, playing an action

The resources accessible to this client can be configured to have a read permission that means only `GET` requests will work.

The resources accessible to this client can be configured to have a write permission that means only `POST`, `PATCH`, `PUT`, `DELETE` requests will work.

The version restriction is only for the design resource and can be used to restrict the client from accessing any version of the game design other than the one specified. By default it allows all.

If access to a route is not allowed and then you make a request to that route then you will get an error like this,
```json
{
  "error": "access_denied",
  "error_description": "You are not allowed to access this api route"
}
```

# Documentation
## Sync Methods
**API**
```java
Object api("GET", // The request method can be GET/POST/PUT/PATCH/DELETE
    "", // The api route to get data from
    HashMap<string, string>, // The query params that you want to send to the route
    Object or HashMap<string, string> ,// The data you want to post to the api
    false // Whether you want the response to be in raw string form or json
)
```

**Get**
```java
Object get("", // The api route to get data from
    HashMap<string, string>, // The query params that you want to send to the
)
```
**Get Raw**
```java
byte[] getRaw("", // The api route to get data from
    HashMap<string, string>, // The query params that you want to send to the
)
```
**Post**
```java
Object post("", // The api route to post data to
    HashMap<string, string>, // The query params that you want to send to the route
    Object or HashMap<string, string> // The data you want to post to the api this will be automagically converted to json
)
```
**Patch**
```java
Object patch(
    "" // The api route to patch data
    HashMap<string, string> // The query params that you want to send to the route
    Object or HashMap<string, string> // The data you want to update in the api this will be automagically converted to json
)
```
**Put**
```java
Object put("" // The api route to put data
    HashMap<string, string>, // The query params that you want to send to the route
    Object or HashMap<string, string> // The data you want to update in the api this will be automagically converted to json
)
```
**Delete**
```java
Object delete("" // The api route to delete the component
    HashMap<string, string> // The query params that you want to send to the route
)
```
**Get Login Url**
```java
String get_login_url()
//This will return the url to which the user needs to be redirected for the user to login.
```

**Exchange Code**
```java
void exchange_code(String code)
//This is used in the auth code flow so that the sdk can get the access token.
//Before any request to the playlyfe api is made this has to be called atleast once.
//This should be called in the the route/controller which you specified in your redirect_uri
```

## Async Methods
For the async methods you first need to pass a callback inteface to the all the methods. The Callback Interface has 3 methods,

**1.onSucess** called when the request succeeds with a 200 OK status

**2.onPlaylyfeError** called when request gets status other than 200 and has err field in the response body

**3.onIOError** called when a io error occurs
```java
import com.playlyfe.sdk.Playlyfe;
import com.playlyfe.sdk.Playlyfe.Callback;

Playlyfe playlyfe = new Playlyfe("Your client id", "Your client secret", "Your redirect URI", null)
playlyfe.getAsync("/runtime/player", player_id, new Callback(){
    @Override
    void onSuccess(Object data) {
        Map<String, Object> player = (Map<String, Object>) data;
        System.out.println(student1.get("id"));
        System.out.println(student1.get("alias"));
    }
    @Override
    void onPlaylyfeError(PlaylyfeException e) {
        System.out.println(e.getName());
        System.out.println(e.getMessage());
    }
    @Override
    void onIOError(IOException e) {
    }
});
```
You can just create a class that implements the Callback Interface and handle all the errors from that one class and initiate that class on subsequent requests to make things simpler.
```java
public class ErrorHandler implements Callback {
    @Override
    void onSuccess(Object data) {
    }
    @Override
    void onPlaylyfeError(PlaylyfeException e) {
        // show message dialog
    }
    @Override
    void onIOError(IOException e) {
        // show network error dialog
    }
}
```

## Android
In android apps you cannot make a network request in the main thread. So you can use the async methods to make requests or you can create your own AsyncTask Class to use the normal sync methods.

### 1. Using AsyncTask Class
```java
class PlayerProfile extends AsyncTask<String, Void, Object> {

    protected Exception exception;
    private TextView tv;

    PlayerProfile(TextView tv) {
        this.tv = tv;
    }

    protected Object doInBackground(String... params) {
        Playlyfe pl = new Playlyfe(
            "Zjc0MWU0N2MtODkzNS00ZWNmLWEwNmYtY2M1MGMxNGQ1YmQ4",
            "YzllYTE5NDQtNDMwMC00YTdkLWFiM2MtNTg0Y2ZkOThjYTZkMGIyNWVlNDAtNGJiMC0xMWU0LWI2NGEtYjlmMmFkYTdjOTI3",
            null,
            "v2"
        );
        HashMap<String, String> player_id = new HashMap<String, String>();
        player_id.put("player_id", "student1");
        try {
            return pl.get("/runtime/player", player_id);
        } catch (PlaylyfeException e) {
            this.exception = e;
            return null;
        } catch (IOException e) {
            this.exception = e;
            return null;
        }
    }

    protected void onPostExecute(Object data) {
        Map<String, Object> student1 = (Map<String, Object>) data;
        System.out.println(student1.get("id"));
        System.out.println(student1.get("alias"));
        tv.setText(student1.get("alias").toString());
    }
}

// And to use this in your Main Activity Class
@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    final TextView tv = (TextView) this.findViewById(R.id.hello);
    new PlayerProfile(tv).execute();
}

```
### 2. Using Async methods
The advantage of the async methods is that you can make more that 1 request at a time and thereby reducing the latency but handling errors becomes a problem as you have to manually check them.
```java
@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    final TextView tv = (TextView) this.findViewById(R.id.hello);
    final ImageView iv = (ImageView) this.findViewById(R.id.imageView);
    Playlyfe pl = new Playlyfe(
        "Zjc0MWU0N2MtODkzNS00ZWNmLWEwNmYtY2M1MGMxNGQ1YmQ4",
        "YzllYTE5NDQtNDMwMC00YTdkLWFiM2MtNTg0Y2ZkOThjYTZkMGIyNWVlNDAtNGJiMC0xMWU0LWI2NGEtYjlmMmFkYTdjOTI3",
        null,
        "v2"
    );
    HashMap<String, String> player_id = new HashMap<String, String>();
    player_id.put("player_id", "student1");
    pl.getAsync("/runtime/player", player_id, new Playlyfe.Callback() {
        Handler mainHandler = new Handler(Looper.getMainLooper());
        @Override
        public void onSuccess(final Object data) {
            mainHandler.post(new Runnable() {

                @Override
                public void run() {
                    Map<String, Object> student1 = (Map<String, Object>) data;
                    System.out.println(student1.get("id"));
                    System.out.println(student1.get("alias"));
                    tv.setText(student1.get("alias").toString());
                }
            });

        }

        @Override
        public void onPlaylyfeError(PlaylyfeException e) {
        }

        @Override
        public void onIOError(IOException e) {
        }
    });
    pl.getRawAsync("/runtime/player/image", player_id, new Playlyfe.Callback() {
        Handler mainHandler = new Handler(Looper.getMainLooper());

        @Override
        public void onSuccess(final Object data) {
            mainHandler.post(new Runnable() {

                @Override
                public void run() {
                    byte[] imageData = (byte[]) data;
                }
            });

        }

        @Override
        public void onPlaylyfeError(PlaylyfeException e) {

        }

        @Override
        public void onIOError(IOException e) {

        }
    });
}
```

**API**
```java
Object apiAsync("GET", // The request method can be GET/POST/PUT/PATCH/DELETE
    "", // The api route to get data from
    HashMap<string, string>, // The query params that you want to send to the route
    Object or HashMap<string, string> ,// The data you want to post to the api
    false // Whether you want the response to be in raw string form or json,
    callback
)
```

**Get**
```java
Object getAsync("", // The api route to get data from
    HashMap<string, string>, // The query params that you want to send to the
    callback
)
```
**Get Raw**
```java
byte[] getRawAsync("", // The api route to get data from
    HashMap<string, string>, // The query params that you want to send to the
    callback
)
```
**Post**
```java
Object postAsync("", // The api route to post data to
    HashMap<string, string>, // The query params that you want to send to the route
    Object or HashMap<string, string> // The data you want to post to the api this will be automagically converted to json
    callback
)
```
**Patch**
```java
Object patchAsync(
    "" // The api route to patch data
    HashMap<string, string> // The query params that you want to send to the route
    Object or HashMap<string, string> // The data you want to update in the api this will be automagically converted to json
    callback
)
```
**Put**
```java
Object putAsync("" // The api route to put data
    HashMap<string, string>, // The query params that you want to send to the route
    Object or HashMap<string, string> // The data you want to update in the api this will be automagically converted to json
    callback
)
```
**Delete**
```java
Object deleteAsync("" // The api route to delete the component
    HashMap<string, string> // The query params that you want to send to the route
    callback
)
```

**Errors**
A ```PlaylyfeException``` is thrown whenever an error occurs in each call.The Error contains a name and message field which can be used to determine the type of error that occurred.

## Development
You need to have these tools,

1.`gradle >= 2.2`

2.`jdk >= 7`

To install all dependencies run `gradle build`

If you are using eclipse then run this gradle task `gradle eclipse`

## GraphQL SDK

**For api v3**
```java
import com.playlyfe.sdk.Playlyfe.PlaylyfeException;
import com.playlyfe.sdk.PlaylyfeGraphQL;

PlaylyfeGraphQL pl = new PlaylyfeGraphQL(
	"your user secret", // the secret used to encrypt the token with
	"http://localhost:3212/graphql" // the graphql api endpoint
);
String token = pl.createJWT("your user_id", 3600);
String query =  "query K {"
 		+ " root {"
 		+ "    games { "
 		+ "      edges { "
 		+ "        node {"
 		+ "          id"
 		+ "          name"
 		+ "        }"
 		+ "      }"
 		+ "    }"
 		+ " }}";
try {
	Map<String, Object> games = (Map<String, Object>) pl.graphql(token, query, null);
	System.out.println(games);
} catch(PlaylyfeException e) {
	System.out.println(e);
}
```


License
=======
Playlyfe Java SDK
http://dev.playlyfe.com/
Copyright(c) 2014-2015, Playlyfe IT Solutions Pvt. Ltd, support@playlyfe.com

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.

Contributing
============
1. Run `gradle build` to pull in dependencies
2. Run `gradle eclipse` to make the eclipse project
3. Create PGP Key using `gpg --gen-key`.
4. Find the keyId using `gpg --list-keys`.
5. Upload the key to a public key server using this `gpg --armor --export keyID`. http://pgp.mit.edu/
6. If you are using gpg2 then you need to export your key `gpg --export-secret-key keyId > secring.gpg`
7. Change this to the gradle build file with your own key
    ```gradle
    allprojects { ext."signing.keyId" = '1AD528DD' }
    allprojects { ext."signing.secretKeyRingFile" = '/path/to/secring.gpg' }
    allprojects { ext."signing.password" = 'keypass' }
    ```
8. Change this under repository(url)
  ```gradle
  authentication(userName: 'myusername', password: 'yoursupersecretpassword')
  ```
9. Run `gradle uploadArchives`
10. Go to https://oss.sonatype.org/ Staging Repository and find com.playlyfe Click Close and then Release

References:

http://central.sonatype.org/pages/releasing-the-deployment.html

http://www.feedhenry.com/publishing-jar-maven-central-repository/
