import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

/**
 * @Title: SolJ6Test
 * @Package: com.ceiec.gis.common.utils
 * @Description: TODO（添加描述）
 * @Author: tangquanbin
 * @Date: 2019/6/21 19:46
 * @Version: V1.0
 */
public class SolJ6Test {

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
