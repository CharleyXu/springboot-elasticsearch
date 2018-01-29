package com.xu.elasticsearch.entity;

import com.alibaba.fastjson.JSON;

/**
 * @author charlie Created on 2018/1/29.
 */
public class Book {

	private String id;

	private String title;

	private String author;

	private String releaseDate;

	public Book() {
	}

	public Book(String id, String title, String author, String releaseDate) {
		this.id = id;
		this.title = title;
		this.author = author;
		this.releaseDate = releaseDate;
	}

	public String getId() {
		return id;
	}

	public Book setId(String id) {
		this.id = id;
		return this;
	}

	public String getTitle() {
		return title;
	}

	public Book setTitle(String title) {
		this.title = title;
		return this;
	}

	public String getAuthor() {
		return author;
	}

	public Book setAuthor(String author) {
		this.author = author;
		return this;
	}

	public String getReleaseDate() {
		return releaseDate;
	}

	public Book setReleaseDate(String releaseDate) {
		this.releaseDate = releaseDate;
		return this;
	}

	@Override
	public String toString() {
		return JSON.toJSONString(this);
	}
}
