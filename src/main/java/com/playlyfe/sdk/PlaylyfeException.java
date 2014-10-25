package com.playlyfe.sdk;

public class PlaylyfeException extends Exception {
	private String name;
	
	 public PlaylyfeException(String name, String message) {
        super(message);
        this.name = name;
     }
	 
	 public String getName() {
		 return name;
	 }
}
