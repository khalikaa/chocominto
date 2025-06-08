package com.example.chocominto.data.api.response;

public class ReadingsItem{
	private String reading;
	private boolean acceptedAnswer;
	private boolean primary;

	public String getReading(){
		return reading;
	}

	public boolean isAcceptedAnswer(){
		return acceptedAnswer;
	}

	public boolean isPrimary(){
		return primary;
	}
}
