package com.example.chocominto.data.api.response;

public class MeaningsItem{
	private String meaning;
	private boolean acceptedAnswer;
	private boolean primary;

	public String getMeaning(){
		return meaning;
	}

	public boolean isAcceptedAnswer(){
		return acceptedAnswer;
	}

	public boolean isPrimary(){
		return primary;
	}
}
