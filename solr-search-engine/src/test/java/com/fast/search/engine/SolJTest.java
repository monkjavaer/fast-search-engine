package com.fast.search.engine;

import com.search.solr.utils.PropertyReaderUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.request.QueryRequest;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.SuggesterResponse;
import org.apache.solr.client.solrj.response.Suggestion;
import org.apache.solr.common.params.CommonParams;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;

/**
 * @author monkjavaer
 * @version V1.0
 * @date 2019/6/21 0021 22:42
 */
public class SolJTest {
    /**
     * 日志
     */
    private static Logger logger = LoggerFactory.getLogger(SolJTest.class);

    /**
     * solr 地址
     */
    private static String SOLR_URL = PropertyReaderUtils.getProValue("solr.address_url");

    /**
     * suggest AnalyzingLookupFactory
     */
    public final static String SOLR_ANALYZINGSUGGESTER = PropertyReaderUtils.getProValue("solr.AnalyzingSuggester");

    /**
     * suggest AnalyzingInfixLookupFactory
     */
    public final static String SOLR_ANALYZINGINFIXSUGGESTER = PropertyReaderUtils.getProValue("solr.AnalyzingInfixSuggester");

    /**
     * HttpSolrClient
     */
    private HttpSolrClient httpSolrClient;

    /**
     * default socket connection timeout in ms
     */
    private static int DEFAULT_CONNECTION_TIMEOUT = 60000;

    /**
     * @return void
     * @author monkjavaer
     * @description get HttpSolrClient
     * @date 13:27 2019/6/19
     * @param: []
     **/
    @Before
    public void getHttpSolrClient() {
        logger.info("start getHttpSolrClient......");
        try {
            if (httpSolrClient == null) {
                httpSolrClient = new HttpSolrClient.Builder(SOLR_URL).build();
                httpSolrClient.setConnectionTimeout(DEFAULT_CONNECTION_TIMEOUT);
                httpSolrClient.setDefaultMaxConnectionsPerHost(100);
                httpSolrClient.setMaxTotalConnections(100);
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e.getMessage());
        }
        logger.info("end getHttpSolrClient......");
    }

    /**
     * @return void
     * @author monkjavaer
     * @description test suggester response object
     * @date 13:27 2019/6/19
     * @param: []
     **/
    @Test
    public void testSuggesterResponseObject() throws IOException, SolrServerException {
        SolrQuery query = new SolrQuery("*:*");
        query.set(CommonParams.QT, "/suggest");
        query.set("suggest.dictionary", SOLR_ANALYZINGSUGGESTER, SOLR_ANALYZINGINFIXSUGGESTER);
        query.set("suggest.q", "aoa");
        query.set("suggest.build", true);
        QueryRequest request = new QueryRequest(query);
        QueryResponse queryResponse = request.process(httpSolrClient);
        SuggesterResponse response = queryResponse.getSuggesterResponse();
        Map<String, List<Suggestion>> suggestionsMap = response.getSuggestions();
        assertTrue(suggestionsMap.keySet().contains(SOLR_ANALYZINGSUGGESTER));

        List<Suggestion> mySuggester = suggestionsMap.get(SOLR_ANALYZINGSUGGESTER);
        logger.info(mySuggester.get(0).getTerm());
        logger.info(mySuggester.get(0).getPayload());
    }

    /**
     * @return void
     * @author monkjavaer
     * @description test suggester response terms
     * @date 13:27 2019/6/19
     * @param: []
     **/
    @Test
    public void testSuggesterResponseTerms() throws Exception {
        SolrQuery query = new SolrQuery("*:*");
        query.set(CommonParams.QT, "/suggest");
        query.set("suggest.dictionary", SOLR_ANALYZINGSUGGESTER, SOLR_ANALYZINGINFIXSUGGESTER);
        query.set("suggest.q", "aoa");
//        query.set("suggest.build", true);
        QueryRequest request = new QueryRequest(query);
        QueryResponse queryResponse = request.process(httpSolrClient);
        SuggesterResponse response = queryResponse.getSuggesterResponse();
        Map<String, List<String>> dictionary2suggestions = response.getSuggestedTerms();
        assertTrue(dictionary2suggestions.keySet().contains(SOLR_ANALYZINGSUGGESTER));

        List<String> mySuggester = dictionary2suggestions.get(SOLR_ANALYZINGSUGGESTER);
        assertEquals("aoa", mySuggester.get(0));
        assertEquals("aoa bob", mySuggester.get(1));
    }

    /**
     * @return void
     * @author monkjavaer
     * @description 简单查询自动转换为bean
     * @date 13:27 2019/6/19
     * @param: []
     **/
    @Test
    public void testSolrQueryGetBeans() throws IOException, SolrServerException {
        final SolrQuery query = new SolrQuery();
        query.setQuery("Zhong Hua Yuan");
        //设置查询列
        query.addField("id");
        query.addField("name");
        //排序
        query.setSort("id", SolrQuery.ORDER.asc);

        final QueryResponse response = httpSolrClient.query("adress", query);
        final List<Adress> adresses = response.getBeans(Adress.class);

        logger.info("Found " + adresses.size() + " documents");
        for (Adress adress : adresses) {
            logger.info("id:{} ; name:{}; ", adress.getId(), adress.getName());
        }
    }

    /**
     * @return void
     * @author monkjavaer
     * @description 批量添加
     * @date 13:27 2019/6/19
     * @param: []
     **/
    @Test
    public void testAddIndex() throws IOException, SolrServerException {
        List<Adress> lists = new ArrayList<>();
        Adress adress = new Adress();
        adress.setId(1);
        adress.setName("aoa");
        lists.add(adress);
        //向solr批量添加索引数据
        long startTime = TimeUnit.MILLISECONDS.convert(System.nanoTime(), TimeUnit.NANOSECONDS);
        httpSolrClient.addBeans(lists);
        httpSolrClient.commit();
        long endTime = TimeUnit.MILLISECONDS.convert(System.nanoTime(), TimeUnit.NANOSECONDS);
        logger.info("commit solr data cost {} ms.", endTime - startTime);
    }
}
