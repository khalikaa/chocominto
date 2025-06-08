package com.example.chocominto.data.api.response;

import java.util.List;

public class WaniKaniResponse{
	private Pages pages;
	private String dataUpdatedAt;
	private List<DataItem> data;
	private int totalCount;
	private String url;
	private String object;

	public Pages getPages(){
		return pages;
	}

	public String getDataUpdatedAt(){
		return dataUpdatedAt;
	}

	public List<DataItem> getData(){
		return data;
	}

	public int getTotalCount(){
		return totalCount;
	}

	public String getUrl(){
		return url;
	}

	public String getObject(){
		return object;
	}
}