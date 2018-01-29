package com.xu.elasticsearch.controller;

import com.xu.elasticsearch.entity.Book;
import com.xu.elasticsearch.service.BookServiceImpl;
import com.xu.elasticsearch.util.ElasticSearchUtils;
import java.util.Date;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author charlie Created on 2018/1/29.
 */
@RestController
public class BookController {

	@Autowired
	private BookServiceImpl bookService;

	@RequestMapping("/test01")
	public String test01() {
		Book book = new Book("id1", "title1", "author1", new Date().toString());
		return bookService.save(book);
	}

	@PostMapping("/test02")
	public String test02(String index, String alias) {
		boolean flag = ElasticSearchUtils.createIndex(index, alias);
		return "1" + flag;
	}

	@PostMapping("/test03")
	public String test03(String index, String type) {
		boolean mapping = ElasticSearchUtils
				.createMapping(index, type, ElasticSearchUtils.getMapping(type));
		return "3" + mapping;
	}


}
