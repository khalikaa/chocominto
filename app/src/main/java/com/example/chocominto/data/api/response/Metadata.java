package com.example.chocominto.data.api.response;

public class Metadata{
	private String gender;
	private String pronunciation;
	private int voiceActorId;
	private String voiceActorName;
	private int sourceId;
	private String voiceDescription;

	public String getGender(){
		return gender;
	}

	public String getPronunciation(){
		return pronunciation;
	}

	public int getVoiceActorId(){
		return voiceActorId;
	}

	public String getVoiceActorName(){
		return voiceActorName;
	}

	public int getSourceId(){
		return sourceId;
	}

	public String getVoiceDescription(){
		return voiceDescription;
	}
}
