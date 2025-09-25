package io.quarkiverse.langchain4j.watsonx.runtime.client;

import com.ibm.watsonx.ai.core.http.BaseHttpClient;

public interface WatsonxRestClient {

    String TRANSACTION_ID_HEADER = com.ibm.watsonx.ai.WatsonxRestClient.TRANSACTION_ID_HEADER;
    String REQUEST_ID_HEADER = BaseHttpClient.REQUEST_ID_HEADER;
}
