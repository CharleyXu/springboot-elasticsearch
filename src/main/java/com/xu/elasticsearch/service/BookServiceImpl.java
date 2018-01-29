package com.xu.elasticsearch.service;

import com.alibaba.fastjson.JSON;
import com.xu.elasticsearch.entity.Book;
import com.xu.elasticsearch.util.ElasticSearchUtils;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * @author charlie Created on 2018/1/29.
 */
@Service
public class BookServiceImpl implements BookService {

	@Override
	public String save(Book book) {
		return ElasticSearchUtils.addData(JSON.parseObject(book.toString()), "book", null);
	}

	@Override
	public void delete(Book book) {

	}

	@Override
	public Book findOne(String id) {
		return null;
	}

	@Override
	public Iterable<Book> findAll() {
		return null;
	}

	@Override
	public List<Book> findByTitle(String title) {
		return null;
	}
}
