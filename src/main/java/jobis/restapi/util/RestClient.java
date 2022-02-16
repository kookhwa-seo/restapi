package jobis.restapi.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.client.AsyncRestTemplate;

import javax.annotation.PostConstruct;

@Component
public class RestClient {
    @Autowired
    private Environment environment;

    private SimpleClientHttpRequestFactory requestFactory;

    @PostConstruct
    public void init()
    {
        requestFactory= new SimpleClientHttpRequestFactory();
        requestFactory.setTaskExecutor(new SimpleAsyncTaskExecutor());
        String timout = environment.getProperty("config.read-timeout");
        requestFactory.setReadTimeout(Integer.parseInt(timout));
    }

    public ListenableFuture<ResponseEntity<String>> request(String url, Object param){
        AsyncRestTemplate asyncRestTemplate = new AsyncRestTemplate();
        asyncRestTemplate.setAsyncRequestFactory(requestFactory);
        HttpEntity<Object> httpRequest = new HttpEntity<>(param);
        ListenableFuture<ResponseEntity<String>> entityListenableFuture = asyncRestTemplate.postForEntity(url, httpRequest, String.class);

        return entityListenableFuture;
    }
}
