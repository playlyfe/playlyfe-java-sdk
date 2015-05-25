package com.playlyfe.sdk;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.auth0.jwt.Algorithm;
import com.auth0.jwt.JWTSigner;

public class Playlyfe {

	private String version;
	private String client_id;
	private String client_secret;
	private String type;
	private String redirect_uri;
	private String code;
	private PersistAccessToken pac;

	private final String API_ENDPOINT = "api.playlyfe.com/";

	private final HttpClient client = HttpClientBuilder.create().build();
	private final Gson gson = new Gson();

	public static String createJWT(String client_id, String client_secret, String player_id, String[] scopes, int expires) {
		JWTSigner signer = new JWTSigner(client_secret);
		HashMap<String, Object> claims = new HashMap<String, Object>();
		claims.put("player_id", player_id);
		claims.put("scopes", scopes);
		String token = signer.sign(claims, new JWTSigner.Options().setExpirySeconds(expires).setAlgorithm(Algorithm.HS256));
		token = client_id + ':' + token;
		return token;
	}

	/* Use this to initialize the Playlyfe sdk in client credentials flow
	 * @params String client_id Your client id
	 * @params String client_secret Your client secret
	 * @params PersistAccessToken pac Your implementation to store and load the access token from a database
	 */
    public Playlyfe(String client_id, String client_secret, PersistAccessToken pac, String version) throws ClientProtocolException, IOException, IllegalStateException, PlaylyfeException {
    	this.client_id = client_id;
    	this.client_secret = client_secret;
    	this.type = "client";
    	this.pac = pac;
    	this.version = version;
    	getAccessToken();
    }

    public Playlyfe(String client_id, String client_secret, PersistAccessToken pac) throws ClientProtocolException, IOException, IllegalStateException, PlaylyfeException {
    	this(client_id, client_secret, pac, "v2");
    }

    /* Use this to initialize the Playlyfe sdk in authorization code flow
	 * @param String client_id Your client id
	 * @params String client_secret Your client secret
	 * @params String redirect_uri The redirect_uri
	 * @params PersistAccessToken pac Your implementation to store and load the access token from a database
	 */
    public Playlyfe(String client_id, String client_secret, String redirect_uri, PersistAccessToken pac) {
    	this(client_id, client_secret, redirect_uri, pac, "v2");
    }

    public Playlyfe(String client_id, String client_secret, String redirect_uri, PersistAccessToken pac, String version) {
    	this.client_id = client_id;
    	this.client_secret = client_secret;
    	this.redirect_uri = redirect_uri;
    	this.type = "code";
    	this.pac = pac;
    	this.version =  version;
    }

    public void getAccessToken() throws UnsupportedEncodingException,ClientProtocolException, IOException, IllegalStateException, PlaylyfeException {
    	System.out.println("Getting Access Token");
    	JsonObject json = new JsonObject();
	    json.addProperty("client_id", client_id);
	    json.addProperty("client_secret", client_secret);
    	if(type.equals("client")) {
    		json.addProperty("grant_type",  "client_credentials");
    	}
    	else {
    		json.addProperty("grant_type",  "authorization_code");
    		json.addProperty("code",  code);
    		json.addProperty("redirect_uri", redirect_uri);
    	}
  		HttpPost post = new HttpPost("https://playlyfe.com/auth/token");
  		StringEntity input = new StringEntity(json.toString());
  		input.setContentType("application/json");
  	    post.setEntity(input);
  		final Map<String, Object> token = (Map<String, Object>) parseJson(client.execute(post));
  		Long expires_at = System.currentTimeMillis() + (((Double) token.get("expires_in")).longValue() * 1000);
  		token.remove("expires_in");
  		token.put("expires_at", expires_at);
  		if(pac == null) {
    			pac = new PersistAccessToken(){
    				@Override
    				public void store(Map<String, Object> token) {
    					System.out.println("Storing Access Token");
    				}

    				@Override
    				public Map<String, Object> load() {
    					return token;
    				}

    			};
  		}
  		pac.store(token);
    }

    /* Use this to make a request to the Playlyfe API
    * @params String method The type of request ['GET', 'POST', 'PUT', 'PATCH', 'DELETE']
    * @params String route The Playlyfe API route
    * @params Map<String, String> The query params for the request
    * @params Object body The data you would like to send in your POST, PUT, PATCH requests
    * @params boolean raw  Whether you would like the response to be string or a Map (Useful for images)
    */
    public Object api(String method, String route, Map<String, String> query, Object body, boolean raw) throws URISyntaxException, IllegalStateException, ClientProtocolException, IOException, PlaylyfeException {
    	URIBuilder builder = new URIBuilder();
    	builder.setScheme("https").setHost(API_ENDPOINT).setPath(this.version+route);
    	if (query != null) {
    		for (Map.Entry<String, String> entry : query.entrySet())
        	{
        		builder.setParameter(entry.getKey(), entry.getValue());
        	}
    	}
    	Map<String, Object> token = pac.load();
    	if(System.currentTimeMillis() >= ((Long) token.get("expires_at"))){
    		getAccessToken();
    	}
    	builder.setParameter("access_token", token.get("access_token").toString());

    	URI uri = builder.build();
    	HttpRequestBase request;
    	if(method.equalsIgnoreCase("GET")) {
    		request = new HttpGet(uri);
    	}
    	else if(method.equalsIgnoreCase("POST")) {
    		request = new HttpPost(uri);
    		if (body != null) {
	    		StringEntity input = new StringEntity(gson.toJson(body));
	    		input.setContentType("application/json");
	        	((HttpPost)request).setEntity(input);
    		}
    	}
    	else if(method.equalsIgnoreCase("PUT")) {
    		request = new HttpPut(uri);
    		if (body != null) {
	    		StringEntity input = new StringEntity(gson.toJson(body));
	    		input.setContentType("application/json");
	        	((HttpPut)request).setEntity(input);
    		}
    	}
    	else if(method.equalsIgnoreCase("PATCH")) {
    		request = new HttpPatch(uri);
    		if (body != null) {
	    		StringEntity input = new StringEntity(gson.toJson(body));
	    		input.setContentType("application/json");
	        	((HttpPatch)request).setEntity(input);
    		}
    	}
    	else if(method.equalsIgnoreCase("Delete")) {
    		request = new HttpDelete(uri);
    	}
    	else {
    		request = new HttpGet(uri);
    	}
    	request.addHeader("accept", "application/json");
    	HttpResponse response = client.execute(request);
    	if(raw == true){
    		return readImage(response);
    	}
    	else {
			return parseJson(response);
    	}
    }

    public Object get(String route, Map<String, String> query) throws IllegalStateException, ClientProtocolException, URISyntaxException, IOException, PlaylyfeException {
	    return api("GET", route, query, null, false);
    }

    public byte[] getRaw(String route, Map<String, String> query) throws IllegalStateException, ClientProtocolException, URISyntaxException, IOException, PlaylyfeException {
	    return (byte[]) api("GET", route, query, null, true);
    }

    public Object post(String route, Map<String, String> query, Object body) throws IllegalStateException, ClientProtocolException, URISyntaxException, IOException, PlaylyfeException {
    	return api("POST", route, query, body, false);
    }

    public Object put(String route, Map<String, String> query, Object body) throws IllegalStateException, ClientProtocolException, URISyntaxException, IOException, PlaylyfeException {
    	return api("PUT", route, query, body, false);
    }

    public Object patch(String route, Map<String, String> query, Object body) throws IllegalStateException, ClientProtocolException, URISyntaxException, IOException, PlaylyfeException {
    	return api("PATCH", route, query, body, false);
    }

    public Object delete(String route, Map<String, String> query) throws IllegalStateException, ClientProtocolException, URISyntaxException, IOException, PlaylyfeException {
	    return api("DELETE", route, query, null, false);
    }
    
    private byte[] readImage(HttpResponse response) throws IllegalStateException, IOException {
    	try (ByteArrayOutputStream bos = new ByteArrayOutputStream();)
        {
            byte[] buffer = new byte[0xFFFF];

            for (int len; (len = response.getEntity().getContent().read(buffer)) != -1;)
            	bos.write(buffer, 0, len);

            bos.flush();

            return bos.toByteArray();
        }
    }

    private String readResponse(HttpResponse response) throws IllegalStateException, IOException {
    	BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
		  StringBuilder sb = new StringBuilder();
	    String line = "";
	    while ((line = rd.readLine()) != null) {
	       sb.append(line);
	    }
	    rd.close();
	    return sb.toString();
    }

    private Object parseJson(HttpResponse response) throws IllegalStateException, IOException, PlaylyfeException {
    	String content = readResponse(response);
    	if(content.contains("error") && content.contains("error_description")) {
    		Map<String, String> errors = (Map<String, String>) gson.fromJson(content, Object.class);
    		throw new PlaylyfeException(errors.get("error"), errors.get("error_description"));
    	}
    	else {
    		return gson.fromJson(content, Object.class);
    	}
    }

    public String get_login_url() throws URISyntaxException {
    	URIBuilder builder = new URIBuilder();
    	builder.setScheme("https").setHost("playlyfe.com/auth")
    		.setParameter("response_type", "code")
    		.setParameter("client_id", client_id)
    		.setParameter("redirect_uri", redirect_uri);
		  return builder.build().toString();
	 }

	public void exchange_code(String code) throws UnsupportedEncodingException, ClientProtocolException, IllegalStateException, IOException, PlaylyfeException {
		this.code = code;
		getAccessToken();
	}
}
