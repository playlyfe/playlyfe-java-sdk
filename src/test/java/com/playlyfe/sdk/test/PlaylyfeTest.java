package com.playlyfe.sdk.test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.client.ClientProtocolException;

import com.playlyfe.sdk.Playlyfe;
import com.playlyfe.sdk.PlaylyfeException;

public class PlaylyfeTest {
	
	public static void testv1() {
		Playlyfe pl = null;
		HashMap<String, String> player_id = new HashMap<String, String>();
		player_id.put("player_id", "student1");
		try {
			pl = new Playlyfe(
				"Zjc0MWU0N2MtODkzNS00ZWNmLWEwNmYtY2M1MGMxNGQ1YmQ4",
				"YzllYTE5NDQtNDMwMC00YTdkLWFiM2MtNTg0Y2ZkOThjYTZkMGIyNWVlNDAtNGJiMC0xMWU0LWI2NGEtYjlmMmFkYTdjOTI3",
				null,
				"v1"
			);
			Map<String, Object> players = (Map<String, Object>) pl.get("/game/players", null);
			System.out.println(players.get("total"));
			ArrayList data = (ArrayList) players.get("data");
			Map<String, Object> player = (Map<String, Object>) data.get(0);
			System.out.println(player.get("id"));
			System.out.println(player.get("alias"));
			
			Map<String, Object> student1 = (Map<String, Object>) pl.get("/player", player_id);
			System.out.println(student1.get("id"));
			System.out.println(student1.get("alias"));
			
			
			pl.get ("/definitions/processes", player_id);
			pl.get ("/definitions/teams", player_id);
			pl.get ("/processes", player_id);
//			pl.get ("/teams", player_id);
			
			Map<String, String> new_process = (Map<String, String>) pl.post ("/definitions/processes/module1", player_id, null);
			
			HashMap<String, String> body = new HashMap<String, String>();
			body.put("name", "patched_process");
			body.put("access", "PUBLIC");
			pl.patch ("/processes/" + new_process.get("id"), player_id, body);

			pl.delete("/processes/" + new_process.get("id"), player_id);
			
			String content = (String) pl.get("/player", player_id, true);
			
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		} catch (PlaylyfeException e) {
			e.printStackTrace();
		}
	}

	public static void testv2() {
		Playlyfe pl = null;
		HashMap<String, String> player_id = new HashMap<String, String>();
		player_id.put("player_id", "student1");
		try {
			pl = new Playlyfe(
				"Zjc0MWU0N2MtODkzNS00ZWNmLWEwNmYtY2M1MGMxNGQ1YmQ4",
				"YzllYTE5NDQtNDMwMC00YTdkLWFiM2MtNTg0Y2ZkOThjYTZkMGIyNWVlNDAtNGJiMC0xMWU0LWI2NGEtYjlmMmFkYTdjOTI3",
				null,
				"v2"
			);
			Map<String, Object> players = (Map<String, Object>) pl.get("/runtime/players", player_id);
			System.out.println(players.get("total"));
			ArrayList data = (ArrayList) players.get("data");
			Map<String, Object> player = (Map<String, Object>) data.get(0);
			System.out.println(player.get("id"));
			System.out.println(player.get("alias"));
			
			Map<String, Object> student1 = (Map<String, Object>) pl.get("/runtime/player", player_id);
			System.out.println(student1.get("id"));
			System.out.println(student1.get("alias"));
			
			
			pl.get ("/runtime/definitions/processes", player_id);
			pl.get ("/runtime/definitions/teams", player_id);
			pl.get ("/runtime/processes", player_id);
//			pl.get ("/teams", player_id);
			
			HashMap<String, String> body = new HashMap<String, String>();
			body.put("definition", "module1");
			
			Map<String, String> new_process = (Map<String, String>) pl.post ("/runtime/processes", player_id, body);
			
			body = new HashMap<String, String>();
			body.put("name", "patched_process");
			body.put("access", "PUBLIC");
			pl.patch ("/runtime/processes/" + new_process.get("id"), player_id, body);

			pl.delete("/runtime/processes/" + new_process.get("id"), player_id);
			
			String content = (String) pl.get("/runtime/player", player_id, true);
			
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		} catch (PlaylyfeException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		testv1();
		testv2();
//		try {
//			pl.get ("/unkown", "dsdsd");
//		} catch (IllegalStateException e) {
//			e.printStackTrace();
//		} catch (ClientProtocolException e) {
//			e.printStackTrace();
//		} catch (URISyntaxException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		} catch (PlaylyfeException e) {
//			System.out.println(e.getName());
//			System.out.println(e.getMessage());
//		}
	}

}
