package com.xu.elasticsearch.service;

import com.xu.elasticsearch.entity.Book;
import java.util.List;

/**
 * @author charlie Created on 2018/1/29.
 */
public interface BookService {

	String save(Book book);

	void delete(Book book);

	Book findOne(String id);

	Iterable<Book> findAll();

	List<Book> findByTitle(String title);

}
