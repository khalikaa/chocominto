package com.example.chocominto.data.api.response;

public class PronunciationAudiosItem{
	private Metadata metadata;
	private String contentType;
	private String url;

	public Metadata getMetadata(){
		return metadata;
	}

	public String getContentType(){
		return contentType;
	}

	public String getUrl(){
		return url;
	}
}
