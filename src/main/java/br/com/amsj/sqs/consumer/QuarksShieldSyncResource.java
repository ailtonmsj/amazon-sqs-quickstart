package br.com.amsj.sqs.consumer;

import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;

import io.quarkus.scheduler.Scheduled;
import io.smallrye.mutiny.Uni;
import software.amazon.awssdk.auth.credentials.WebIdentityTokenFileCredentialsProvider;
import software.amazon.awssdk.core.SdkSystemSetting;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageResponse;

public class QuarksShieldSyncResource {

    private static final Logger LOGGER = Logger.getLogger(QuarksShieldSyncResource.class);

//    @Inject
//    SqsClient sqs;

    @ConfigProperty(name = "queue.url")
    String queueUrl;

    @Scheduled(every = "10s")
    public void receive() {
    	
    	System.setProperty(
    			SdkSystemSetting.SYNC_HTTP_SERVICE_IMPL.property(),
    			"software.amazon.awssdk.http.apache.ApacheSdkHttpService");
    	
    	SqsClient sqs = SqsClient.builder()
    			.credentialsProvider(WebIdentityTokenFileCredentialsProvider.create())
    			.build();
    	
    	LOGGER.info("FILA SNS" + queueUrl);
    	
    	
        List<Message> messages = sqs.receiveMessage (m -> m.maxNumberOfMessages(10).queueUrl(queueUrl)).messages();
        
        LOGGER.info(">> MESSAGE QTIDE: " + messages.size());
        
        for(Message message : messages) {
        	
        	LOGGER.info(">> MESSAGE BODY: " + message.body());
        	
        	LOGGER.info(">> MESSAGE PREPARANDO PARA SER DELETADA...");
        	
        	DeleteMessageRequest deleteMessageRequest = DeleteMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .receiptHandle(message.receiptHandle())
                    .build();
        	
        	LOGGER.info(">> MESSAGE A SER DELETADA: " + message.receiptHandle());
        	
        	sqs.deleteMessage(deleteMessageRequest);
        	
        	LOGGER.info(">> MESSAGE DELETADA COM SUCESSO!!!");
        }
        
        LOGGER.info(">> FIM");
    }

}