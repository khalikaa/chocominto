package com.example.chocominto.data.api.response;

import com.google.gson.annotations.SerializedName;

public class PronunciationAudiosItem{
	private Metadata metadata;

	@SerializedName("content_type")
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
