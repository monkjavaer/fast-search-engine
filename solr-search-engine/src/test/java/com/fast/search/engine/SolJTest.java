package com.fast.search.engine;

import org.apache.solr.client.solrj.SolrClient;
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
import java.util.List;
import java.util.Map;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;

/**
 * @author monkjavaer
 * @version V1.0
 * @date 2019/6/21 0021 22:42
 */
public class SolJTest {
    private static Logger logger = LoggerFactory.getLogger(SolJTest.class);

    private final String solrUrl = "http://localhost:8983/solr/adress-test/";

    private HttpSolrClient httpSolrClient;

    public static int DEFAULT_CONNECTION_TIMEOUT = 60000;  // default socket connection timeout in ms

    @Before
    public void getHttpSolrClient() {
        logger.info("start getHttpSolrClient......");
        try {
            if (httpSolrClient == null) {
                httpSolrClient = new HttpSolrClient.Builder(solrUrl).build();
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


    @Test
    public void testSuggesterResponseObject() throws IOException, SolrServerException {
        SolrQuery query = new SolrQuery("*:*");
        query.set(CommonParams.QT, "/suggest");
        query.set("suggest.dictionary", "AnalyzingSuggester");
        query.set("suggest.q", "Em");
        query.set("suggest.build", true);
        QueryRequest request = new QueryRequest(query);
        QueryResponse queryResponse = request.process(httpSolrClient);
        SuggesterResponse response = queryResponse.getSuggesterResponse();
        Map<String, List<Suggestion>> suggestionsMap = response.getSuggestions();
        assertTrue(suggestionsMap.keySet().contains("AnalyzingSuggester"));

        List<Suggestion> mySuggester = suggestionsMap.get("AnalyzingSuggester");
        logger.info(mySuggester.get(0).getTerm());
    }
    @Test
    public void testSuggesterResponseTerms() throws Exception {
        SolrQuery query = new SolrQuery("*:*");
        query.set(CommonParams.QT, "/suggest");
        query.set("suggest.dictionary", "AnalyzingSuggester");
        query.set("suggest.q", "Em");
        query.set("suggest.build", true);
        QueryRequest request = new QueryRequest(query);
        QueryResponse queryResponse = request.process(httpSolrClient);
        SuggesterResponse response = queryResponse.getSuggesterResponse();
        Map<String, List<String>> dictionary2suggestions = response.getSuggestedTerms();
        assertTrue(dictionary2suggestions.keySet().contains("AnalyzingSuggester"));

        List<String> mySuggester = dictionary2suggestions.get("AnalyzingSuggester");
        assertEquals("Computational framework", mySuggester.get(0));
        assertEquals("Computer", mySuggester.get(1));
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
}
