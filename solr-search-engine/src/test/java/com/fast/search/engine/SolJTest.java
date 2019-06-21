package com.fast.search.engine;

import java.io.IOException;

/**
 * @author monkjavaer
 * @version V1.0
 * @date 2019/6/21 0021 22:42
 */
public class SolJTest {
    private static Logger logger = LoggerFactory.getLogger(SolJ6Test.class);

    private final String solrUrl = "http://localhost:8080/adress-test/";

    private HttpSolrClient httpSolrClient;

    @Before
    public void getHttpSolrClient() {
        logger.info("start getHttpSolrClient......");
        try {
            if (httpSolrClient == null) {
                httpSolrClient = new HttpSolrClient.Builder(solrUrl)
                        .withConnectionTimeout(10000)
                        .withSocketTimeout(60000)
                        .build();
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e.getMessage());
        }
        logger.info("end getHttpSolrClient......");
    }

    /**
     * @return void
     * @author tangquanbin
     * @description 简单查询自动转换为bean
     * @date 13:27 2019/6/19
     * @param: []
     **/
    @Test
    public void testSolrQueryGetBeans() throws IOException, SolrServerException {
        final SolrQuery query = new SolrQuery();
        query.setQuery("Zhong Hua Yuan");
        //设置查询列
        query.addField("gid");
        query.addField("adress_name");
        //排序
        query.setSort("id", SolrQuery.ORDER.asc);

        final QueryResponse response = httpSolrClient.query("adress", query);
        final List<Adress> adresses = response.getBeans(Adress.class);

        logger.info("Found " + adresses.size() + " documents");
        for (Adress adress : adresses) {
            logger.info("gid:{} ; adress_name:{}; district_code:{}", adress.getGid(), adress.getAdress_name(), adress.getDistrict_code());
        }
    }
}
