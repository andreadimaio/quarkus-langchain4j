package io.quarkiverse.langchain4j.watsonx.deployment;

import static org.assertj.core.api.Assertions.assertThat;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import com.ibm.watsonx.ai.core.auth.AuthenticationProvider;
import com.ibm.watsonx.ai.core.auth.iam.IAMAuthenticator;
import io.quarkiverse.langchain4j.watsonx.runtime.client.impl.QuarkusIAMRestClient;
import io.quarkus.test.QuarkusUnitTest;

public class CustomChatTest {

    @RegisterExtension
    static QuarkusUnitTest unitTest = new QuarkusUnitTest()
        .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class));

     @Test
     public void iam_client() throws Exception {

        AuthenticationProvider authenticationProvider = IAMAuthenticator.builder()
            .apiKey("test")
            .build();

        Class<IAMAuthenticator> clazz = IAMAuthenticator.class;
        var clientField = clazz.getDeclaredField("client");
        clientField.setAccessible(true);
        var client = clientField.get(authenticationProvider);
        assertThat(client).isInstanceOf(QuarkusIAMRestClient.class);
     }
}
