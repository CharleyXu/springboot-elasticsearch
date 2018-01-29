package com.xu.elasticsearch.util;

import com.alibaba.fastjson.JSONObject;
import com.xu.elasticsearch.entity.EsPage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.annotation.PostConstruct;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.admin.indices.alias.Alias;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexResponse;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingRequest;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingResponse;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequestBuilder;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.Requests;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author charlie Created on 2018/1/29.
 */
@Component
public class ElasticSearchUtils {

	private static final Logger LOGGER = LoggerFactory.getLogger(ElasticSearchUtils.class);
	private static TransportClient client;
	@Autowired
	private TransportClient transportClient;

	/**
	 * 创建索引 + 别名
	 */
	public static boolean createIndex(String index, String alias) {
		CreateIndexResponse indexResponse = null;
		try {
			if (isIndexExist(index)) {
				LOGGER.info("Index already exists !");
				return false;
			}
			indexResponse = client.admin().indices().prepareCreate(index).addAlias(new Alias(alias))
					.execute()
					.actionGet();

			LOGGER.info("执行建立成功？" + indexResponse.isAcknowledged());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return indexResponse.isAcknowledged();
	}

	/**
	 * 删除索引
	 */
	public static boolean deleteIndex(String index) {
		if (!isIndexExist(index)) {
			LOGGER.info("Index is not exits!");
		}
		DeleteIndexResponse dResponse = client.admin().indices().prepareDelete(index).execute()
				.actionGet();
		if (dResponse.isAcknowledged()) {
			LOGGER.info("delete index " + index + "  successfully!");
		} else {
			LOGGER.info("Fail to delete index " + index);
		}
		return dResponse.isAcknowledged();
	}

	/**
	 * 判断索引是否存在
	 */
	public static boolean isIndexExist(String index) {
		IndicesExistsResponse inExistsResponse = client.admin().indices()
				.exists(new IndicesExistsRequest(index)).actionGet();
		if (inExistsResponse.isExists()) {
			LOGGER.info("Index [" + index + "] is exist!");
		} else {
			LOGGER.info("Index [" + index + "] is not exist!");
		}
		return inExistsResponse.isExists();
	}

	/**
	 * 创建Mapping
	 *
	 * 类似于静态语言中的数据类型声明
	 *
	 * @param index 索引
	 * @param type 类型		mapping所对应的type
	 * @param xContentBuilder 构建 Mapping
	 */
	public static boolean createMapping(String index, String type, XContentBuilder xContentBuilder) {
		PutMappingRequest mapping = Requests.putMappingRequest(index).type(type)
				.source(xContentBuilder);
		PutMappingResponse putMappingResponse = client.admin().indices().putMapping(mapping)
				.actionGet();
		if (putMappingResponse.isAcknowledged()) {
			LOGGER.info("Create mapping " + index + type + "  successfully!");
		} else {
			LOGGER.info("Fail to create mapping " + index + type);
		}
		return putMappingResponse.isAcknowledged();
	}

	/**
	 * 示例	数据类型声明的构建
	 */
	public static XContentBuilder getMapping(String type) {
		XContentBuilder mapping = null;
		try {
			mapping = XContentFactory.jsonBuilder()
					.startObject()
					//开启倒计时功能
					.startObject("mappings")
					.startObject(type)
					.startObject("properties")

					.startObject("id")
					.field("type", "String")
					.endObject()

					.startObject("title")
					.field("type", "String")
					.endObject()

					.startObject("author")
					.field("type", "String")
					.endObject()

					.startObject("releaseDate")
					.field("type", "String")
					.endObject()

					.endObject()
					.endObject()
					.endObject()

					.endObject();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return mapping;
	}

	/**
	 * 数据添加，正定ID
	 *
	 * @param jsonObject 要增加的数据
	 * @param index 索引，类似数据库
	 * @param type 类型，类似表
	 * @param id 数据ID
	 */
	public static String addData(JSONObject jsonObject, String index, String type, String id) {

		IndexResponse response = client.prepareIndex(index, type, id).setSource(jsonObject).get();

		LOGGER
				.info("addData response status:{},id:{}", response.status().getStatus(), response.getId());

		return response.getId();
	}

	/**
	 * 数据添加
	 *
	 * @param jsonObject 要增加的数据
	 * @param index 索引，类似数据库
	 * @param type 类型，类似表
	 */
	public static String addData(JSONObject jsonObject, String index, String type) {
		return addData(jsonObject, index, type,
				UUID.randomUUID().toString().replaceAll("-", "").toUpperCase());
	}

	/**
	 * 通过ID删除数据
	 *
	 * @param index 索引，类似数据库
	 * @param type 类型，类似表
	 * @param id 数据ID
	 */
	public static void deleteDataById(String index, String type, String id) {

		DeleteResponse response = client.prepareDelete(index, type, id).execute().actionGet();

		LOGGER.info("deleteDataById response status:{},id:{}", response.status().getStatus(),
				response.getId());
	}

	/**
	 * 通过ID 更新数据
	 *
	 * @param jsonObject 要增加的数据
	 * @param index 索引，类似数据库
	 * @param type 类型，类似表
	 * @param id 数据ID
	 */
	public static void updateDataById(JSONObject jsonObject, String index, String type, String id) {

		UpdateRequest updateRequest = new UpdateRequest();

		updateRequest.index(index).type(type).id(id).doc(jsonObject);

		client.update(updateRequest);

	}

	/**
	 * 通过ID获取数据
	 *
	 * @param index 索引，类似数据库
	 * @param type 类型，类似表
	 * @param id 数据ID
	 * @param fields 需要显示的字段，逗号分隔（缺省为全部字段）
	 */
	public static Map<String, Object> searchDataById(String index, String type, String id,
			String fields) {

		GetRequestBuilder getRequestBuilder = client.prepareGet(index, type, id);

		if (StringUtils.isNotEmpty(fields)) {
			getRequestBuilder.setFetchSource(fields.split(","), null);
		}

		GetResponse getResponse = getRequestBuilder.execute().actionGet();

		return getResponse.getSource();
	}

	/**
	 * 使用分词查询
	 *
	 * @param index 索引名称
	 * @param type 类型名称,可传入多个type逗号分隔
	 * @param startTime 开始时间
	 * @param endTime 结束时间
	 * @param size 文档大小限制
	 * @param matchStr 过滤条件（xxx=111,aaa=222）
	 */
	public static List<Map<String, Object>> searchListData(String index, String type, long startTime,
			long endTime, Integer size, String matchStr) {
		return searchListData(index, type, startTime, endTime, size, null, null, false, null, matchStr);
	}

	/**
	 * 使用分词查询
	 *
	 * @param index 索引名称
	 * @param type 类型名称,可传入多个type逗号分隔
	 * @param size 文档大小限制
	 * @param fields 需要显示的字段，逗号分隔（缺省为全部字段）
	 * @param matchStr 过滤条件（xxx=111,aaa=222）
	 */
	public static List<Map<String, Object>> searchListData(String index, String type, Integer size,
			String fields, String matchStr) {
		return searchListData(index, type, 0, 0, size, fields, null, false, null, matchStr);
	}

	/**
	 * 使用分词查询
	 *
	 * @param index 索引名称
	 * @param type 类型名称,可传入多个type逗号分隔
	 * @param size 文档大小限制
	 * @param fields 需要显示的字段，逗号分隔（缺省为全部字段）
	 * @param sortField 排序字段
	 * @param matchPhrase true 使用，短语精准匹配
	 * @param matchStr 过滤条件（xxx=111,aaa=222）
	 */
	public static List<Map<String, Object>> searchListData(String index, String type, Integer size,
			String fields, String sortField, boolean matchPhrase, String matchStr) {
		return searchListData(index, type, 0, 0, size, fields, sortField, matchPhrase, null, matchStr);
	}

	/**
	 * 使用分词查询
	 *
	 * @param index 索引名称
	 * @param type 类型名称,可传入多个type逗号分隔
	 * @param size 文档大小限制
	 * @param fields 需要显示的字段，逗号分隔（缺省为全部字段）
	 * @param sortField 排序字段
	 * @param matchPhrase true 使用，短语精准匹配
	 * @param highlightField 高亮字段
	 * @param matchStr 过滤条件（xxx=111,aaa=222）
	 */
	public static List<Map<String, Object>> searchListData(String index, String type, Integer size,
			String fields, String sortField, boolean matchPhrase, String highlightField,
			String matchStr) {
		return searchListData(index, type, 0, 0, size, fields, sortField, matchPhrase, highlightField,
				matchStr);
	}

	/**
	 * 使用分词查询
	 *
	 * @param index 索引名称
	 * @param type 类型名称,可传入多个type逗号分隔
	 * @param startTime 开始时间
	 * @param endTime 结束时间
	 * @param size 文档大小限制
	 * @param fields 需要显示的字段，逗号分隔（缺省为全部字段）
	 * @param sortField 排序字段
	 * @param matchPhrase true 使用，短语精准匹配
	 * @param highlightField 高亮字段
	 * @param matchStr 过滤条件（xxx=111,aaa=222）
	 */
	public static List<Map<String, Object>> searchListData(String index, String type, long startTime,
			long endTime, Integer size, String fields, String sortField, boolean matchPhrase,
			String highlightField, String matchStr) {

		SearchRequestBuilder searchRequestBuilder = client.prepareSearch(index);
		if (StringUtils.isNotEmpty(type)) {
			searchRequestBuilder.setTypes(type.split(","));
		}
		BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();

		if (startTime > 0 && endTime > 0) {
			boolQuery.must(QueryBuilders.rangeQuery("timestamp")
					.format("epoch_millis")
					.from(startTime)
					.to(endTime)
					.includeLower(true)
					.includeUpper(true));
		}

		//搜索的的字段
		if (StringUtils.isNotEmpty(matchStr)) {
			for (String s : matchStr.split(",")) {
				String[] ss = s.split("=");
				if (ss.length > 1) {
					if (matchPhrase == Boolean.TRUE) {
						boolQuery.must(QueryBuilders.matchPhraseQuery(s.split("=")[0], s.split("=")[1]));
					} else {
						boolQuery.must(QueryBuilders.matchQuery(s.split("=")[0], s.split("=")[1]));
					}
				}

			}
		}

		// 高亮（xxx=111,aaa=222）
		if (StringUtils.isNotEmpty(highlightField)) {
			HighlightBuilder highlightBuilder = new HighlightBuilder();

			//highlightBuilder.preTags("<span style='color:red' >");//设置前缀
			//highlightBuilder.postTags("</span>");//设置后缀

			// 设置高亮字段
			highlightBuilder.field(highlightField);
			searchRequestBuilder.highlighter(highlightBuilder);
		}

		searchRequestBuilder.setQuery(boolQuery);

		if (StringUtils.isNotEmpty(fields)) {
			searchRequestBuilder.setFetchSource(fields.split(","), null);
		}
		searchRequestBuilder.setFetchSource(true);

		if (StringUtils.isNotEmpty(sortField)) {
			searchRequestBuilder.addSort(sortField, SortOrder.DESC);
		}

		if (size != null && size > 0) {
			searchRequestBuilder.setSize(size);
		}

		//打印的内容 可以在 Elasticsearch head 和 Kibana  上执行查询
		LOGGER.info("\n{}", searchRequestBuilder);

		SearchResponse searchResponse = searchRequestBuilder.execute().actionGet();

		long totalHits = searchResponse.getHits().totalHits;
		long length = searchResponse.getHits().getHits().length;

		LOGGER.info("共查询到[{}]条数据,处理数据条数[{}]", totalHits, length);

		if (searchResponse.status().getStatus() == 200) {
			// 解析对象
			return setSearchResponse(searchResponse, highlightField);
		}

		return null;

	}

	/**
	 * 使用分词查询,并分页
	 *
	 * @param index 索引名称
	 * @param type 类型名称,可传入多个type逗号分隔
	 * @param currentPage 当前页
	 * @param pageSize 每页显示条数
	 * @param startTime 开始时间
	 * @param endTime 结束时间
	 * @param fields 需要显示的字段，逗号分隔（缺省为全部字段）
	 * @param sortField 排序字段
	 * @param matchPhrase true 使用，短语精准匹配
	 * @param highlightField 高亮字段
	 * @param matchStr 过滤条件（xxx=111,aaa=222）
	 */
	public static EsPage searchDataPage(String index, String type, int currentPage, int pageSize,
			long startTime, long endTime, String fields, String sortField, boolean matchPhrase,
			String highlightField, String matchStr) {
		SearchRequestBuilder searchRequestBuilder = client.prepareSearch(index);
		if (StringUtils.isNotEmpty(type)) {
			searchRequestBuilder.setTypes(type.split(","));
		}
		searchRequestBuilder.setSearchType(SearchType.QUERY_THEN_FETCH);

		// 需要显示的字段，逗号分隔（缺省为全部字段）
		if (StringUtils.isNotEmpty(fields)) {
			searchRequestBuilder.setFetchSource(fields.split(","), null);
		}

		//排序字段
		if (StringUtils.isNotEmpty(sortField)) {
			searchRequestBuilder.addSort(sortField, SortOrder.DESC);
		}

		BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();

		if (startTime > 0 && endTime > 0) {
			boolQuery.must(QueryBuilders.rangeQuery("timestamp")
					.format("epoch_millis")
					.from(startTime)
					.to(endTime)
					.includeLower(true)
					.includeUpper(true));
		}

		// 查询字段
		if (StringUtils.isNotEmpty(matchStr)) {
			for (String s : matchStr.split(",")) {
				String[] ss = s.split("=");
				if (matchPhrase == Boolean.TRUE) {
					boolQuery.must(QueryBuilders.matchPhraseQuery(s.split("=")[0], s.split("=")[1]));
				} else {
					boolQuery.must(QueryBuilders.matchQuery(s.split("=")[0], s.split("=")[1]));
				}
			}
		}

		// 高亮（xxx=111,aaa=222）
		if (StringUtils.isNotEmpty(highlightField)) {
			HighlightBuilder highlightBuilder = new HighlightBuilder();

			//highlightBuilder.preTags("<span style='color:red' >");//设置前缀
			//highlightBuilder.postTags("</span>");//设置后缀

			// 设置高亮字段
			highlightBuilder.field(highlightField);
			searchRequestBuilder.highlighter(highlightBuilder);
		}

		searchRequestBuilder.setQuery(QueryBuilders.matchAllQuery());
		searchRequestBuilder.setQuery(boolQuery);

		// 分页应用
		searchRequestBuilder.setFrom(currentPage).setSize(pageSize);

		// 设置是否按查询匹配度排序
		searchRequestBuilder.setExplain(true);

		//打印的内容 可以在 Elasticsearch head 和 Kibana  上执行查询
		LOGGER.info("\n{}", searchRequestBuilder);

		// 执行搜索,返回搜索响应信息
		SearchResponse searchResponse = searchRequestBuilder.execute().actionGet();

		long totalHits = searchResponse.getHits().totalHits;
		long length = searchResponse.getHits().getHits().length;

		LOGGER.debug("共查询到[{}]条数据,处理数据条数[{}]", totalHits, length);

		if (searchResponse.status().getStatus() == 200) {
			// 解析对象
			List<Map<String, Object>> sourceList = setSearchResponse(searchResponse, highlightField);

			return new EsPage(currentPage, pageSize, (int) totalHits, sourceList);
		}

		return null;

	}

	/**
	 * 高亮结果集 特殊处理
	 */
	private static List<Map<String, Object>> setSearchResponse(SearchResponse searchResponse,
			String highlightField) {
		List<Map<String, Object>> sourceList = new ArrayList<Map<String, Object>>();
		StringBuffer stringBuffer = new StringBuffer();

		for (SearchHit searchHit : searchResponse.getHits().getHits()) {
			searchHit.getSourceAsMap().put("id", searchHit.getId());
			if (StringUtils.isNotEmpty(highlightField)) {

				System.out.println("遍历 高亮结果集，覆盖 正常结果集" + searchHit.getSourceAsMap());
				Text[] text = searchHit.getHighlightFields().get(highlightField).getFragments();

				if (text != null) {
					for (Text str : text) {
						stringBuffer.append(str.string());
					}
					//遍历 高亮结果集，覆盖 正常结果集
					searchHit.getSourceAsMap().put(highlightField, stringBuffer.toString());
				}
			}
			sourceList.add(searchHit.getSourceAsMap());
		}

		return sourceList;
	}

	@PostConstruct
	public void init() {
		client = this.transportClient;
	}
}
