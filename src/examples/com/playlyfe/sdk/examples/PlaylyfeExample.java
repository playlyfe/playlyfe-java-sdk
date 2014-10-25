package com.playlyfe.sdk.examples;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Map;

import org.apache.http.client.ClientProtocolException;

import spark.Spark;
import spark.Request;
import spark.Response;
import spark.Route;

import com.playlyfe.sdk.*;


public class PlaylyfeExample {
	public static Playlyfe pl = null;
	
	public static String listPlayers() {
		String html = "";
		try {
			Map<String, Object> players = (Map<String, Object>) pl.get("/game/players", null);
			ArrayList<Map<String, Object>> data = (ArrayList<Map<String, Object>>) players.get("data");
			html += "<ul>";
			for(Map<String, Object> player: data) {
				html += "<li><p>";
				html += "<bold>Player ID</bold>:   "+ player.get("id");
				html += "<bold>Player Alias</bold>:    "+ player.get("alias");
				html += "</p></li>";
			}
			html += "</ul>";
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (PlaylyfeException e) {
			e.printStackTrace();
		}
		return html;
	}

	public static void main(String[] args) {
		Spark.get(new Route("/") {
			@Override
			public Object handle(Request request, Response response) {
				request.session().removeAttribute("user");
				String html = "<h2><a href=\"/client\"> Client Credentials Flow Example</a></h2> ";
				html += "<h2><a href=\"/code\"> Authorization Code Flow Example</a></h2> ";
				return html;
			}
		});
		
		Spark.get(new Route("/client") {
			@Override
			public Object handle(Request request, Response response) {
				try {
					pl= new Playlyfe(
						"Zjc0MWU0N2MtODkzNS00ZWNmLWEwNmYtY2M1MGMxNGQ1YmQ4",
						"YzllYTE5NDQtNDMwMC00YTdkLWFiM2MtNTg0Y2ZkOThjYTZkMGIyNWVlNDAtNGJiMC0xMWU0LWI2NGEtYjlmMmFkYTdjOTI3",
						null
					);
					return listPlayers();
				} catch (ClientProtocolException e) {
					e.printStackTrace();
				} catch (IllegalStateException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (PlaylyfeException e) {
					e.printStackTrace();
				}
				return "";
			}
		});
		
		Spark.get(new Route("/login") {
			@Override
			public Object handle(Request request, Response response) {
				String login_uri;
				try {
					login_uri = pl.get_login_url();
					return "<h1> Please Login into PLaylyfe <h1>" + "<a href=\""+ login_uri + "\"> Login </a>";
				} catch (URISyntaxException e) {
					e.printStackTrace();
				}
				return "";
			}
		});
		
		Spark.get(new Route("/logout") {
			@Override
			public Object handle(Request request, Response response) {
				request.session().removeAttribute("user");
				response.redirect("/login");
				return "Loggin Out";
			}
		});
		
		Spark.get(new Route("/code") {
			@Override
			public Object handle(Request request, Response response) {
				pl= new Playlyfe(
					"OWNkMDYwYzUtNmQ1Mi00ZmE0LWFhNGItMjIyNzgyYmZlYmZi",
					"ZDVjOTkzZjktYzMyMy00NDY5LTk1MmYtMjg2Y2I5NTY0NjIyMTg5YTU0ZTAtNWMzZi0xMWU0LWJmODMtMDUyMmFkZDBmMTYx",
					"http://localhost:4567/code",
					null
				);
				if(request.queryMap().get("code").value() != null){
					try {
						pl.exchange_code(request.queryMap().get("code").value());
						request.session().attribute("user", "user1");
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					} catch (ClientProtocolException e) {
						e.printStackTrace();
					} catch (IllegalStateException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					} catch (PlaylyfeException e) {
						e.printStackTrace();
					}
				}
				if(request.session().attribute("user") != null) {
					return listPlayers();
				}
				else {
					response.redirect("/login");
					return "Please Login to your Playlyfe Account";
				}
			}
		});
	}
}
