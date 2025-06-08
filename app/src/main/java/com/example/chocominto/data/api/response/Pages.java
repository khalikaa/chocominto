package com.example.chocominto.data.api.response;

public class Pages{
	private int perPage;
	private String nextUrl;
	private Object previousUrl;

	public int getPerPage(){
		return perPage;
	}

	public String getNextUrl(){
		return nextUrl;
	}

	public Object getPreviousUrl(){
		return previousUrl;
	}
}
