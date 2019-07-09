package com.data.collection.data.utils;

public class LogCat {
	public void append( String log){
		System.out.println(log);
	}

	public void append(String tag,  String log){
		System.out.println("TAG_" + tag + ": " + log);
	}
}
