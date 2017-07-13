package net.digitaledge.watchalert.rest;

import static org.elasticsearch.rest.RestStatus.OK;

import java.io.IOException;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.json.JsonXContent;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.rest.BaseRestHandler;
import org.elasticsearch.rest.BytesRestResponse;
import org.elasticsearch.rest.RestChannel;
import org.elasticsearch.rest.RestController;
import org.elasticsearch.rest.RestRequest;
import org.elasticsearch.search.SearchHit;

public class watchalertRestAction extends BaseRestHandler {

    @Inject
    public watchalertRestAction(final Settings settings, final Client client,
            final RestController controller) {
        super(settings, controller, client);

        //controller.registerHandler(RestRequest.Method.GET, "/{index}/{type}/_stats", this);
        //controller.registerHandler(RestRequest.Method.GET,"/{index}/_stats", this);
        controller.registerHandler(RestRequest.Method.GET, "/watchalert", this);
    }

    @Override
    protected void handleRequest(final RestRequest request,
            final RestChannel channel, Client client) {
        try {
            final XContentBuilder builder = JsonXContent.contentBuilder();
            if (request.hasParam("pretty")) {
                builder.prettyPrint().lfAtEnd();
            }
            builder.startObject();
            //builder.field("index", request.param("index"));
            //builder.field("type", request.param("type"));
            builder.field("description", "This is a watchalert response: " + new Date().toString());
            builder.endObject();
            
            logger.error(settings.toDelimitedString(':'));
            logger.error("1");
            QueryBuilder qb = QueryBuilders.matchQuery("speaker", "FALSTAFF");
            logger.error("2");
            //Node node = nodeBuilder().client(true).clusterName("lab-cluster").node();
            logger.error("3");
            //client = node.client();
            logger.error("4");
/*            SearchResponse response = client.prepareSearch("bank") //
            	.setQuery(qb) // Query
            	.execute().actionGet();*/
            
            SearchResponse response = client.prepareSearch()
                    .setIndices("shakespeare")
                    .setTypes("line")
                    .addFields("line_id")
                    //.setQuery(QueryBuilders.fieldQuery("text", keyword))
                    .execute().actionGet();
            
            Set<String> result = new HashSet<String>();
/*            for (SearchHit hit : response.getHits()) {
                Long id = hit.field("id").<Long>getValue();
                result.add(String.valueOf(id));
                logger.info(result.toString());
            }*/
            
            //node.close();
            
            //logger.info(builder.string(), null);
            //logger.info(builder.toString(), null);
            
            channel.sendResponse(new BytesRestResponse(OK, builder));
            
            
        } catch (final IOException e) {
            try {
                channel.sendResponse(new BytesRestResponse(channel, e));
            } catch (final IOException e1) {
                logger.error("Failed to send a failure response.", e1);
            }
        }
    }

}
