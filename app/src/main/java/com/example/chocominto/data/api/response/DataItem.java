package com.example.chocominto.data.api.response;

public class DataItem{
	private String dataUpdatedAt;
	private Data data;
	private int id;
	private String url;
	private String object;

	public String getDataUpdatedAt(){
		return dataUpdatedAt;
	}

	public Data getData(){
		return data;
	}

	public int getId(){
		return id;
	}

	public String getUrl(){
		return url;
	}

	public String getObject(){
		return object;
	}
}
