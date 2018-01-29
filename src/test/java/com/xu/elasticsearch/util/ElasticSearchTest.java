package com.xu.elasticsearch.util;

import java.io.IOException;
import org.elasticsearch.common.xcontent.NamedXContentRegistry;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.common.xcontent.XContentType;
import org.junit.Test;

/**
 * @author charlie Created on 2018/1/29.
 */
public class ElasticSearchTest {

	@Test
	public void test01() {
		XContentBuilder mapping = ElasticSearchUtils.getMapping("doc");
		System.out.println(mapping);
		String json = "{\n"
				+ "  \"mappings\": {\n"
				+ "    \"doc\": { \n"
				+ "      \"properties\": { \n"
				+ "        \"id\":       { \"type\": \"String\"  }, \n"
				+ "        \"title\":     { \"type\": \"String\"  }, \n"
				+ "        \"author\":   { \"type\": \"String\" },  \n"
				+ "        \"releaseDate\":  { \"type\": \"String\" }\n"
				+ "        \n"
				+ "      }\n"
				+ "    }\n"
				+ "  }\n"
				+ "}";
		System.out.println(json);
		try {
			XContentParser parser = XContentFactory.xContent(XContentType.JSON)
					.createParser(NamedXContentRegistry.EMPTY, json);
			XContentBuilder xContentBuilder = XContentFactory.jsonBuilder().prettyPrint();
			xContentBuilder.copyCurrentStructure(parser);

			System.out.println(xContentBuilder.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
