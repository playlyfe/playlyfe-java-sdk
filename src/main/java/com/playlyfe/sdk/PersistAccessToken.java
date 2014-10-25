package com.playlyfe.sdk;

import java.util.Map;

public interface PersistAccessToken {
	public void store(Map<String, Object> token);
	public Map<String, Object> load();
}
