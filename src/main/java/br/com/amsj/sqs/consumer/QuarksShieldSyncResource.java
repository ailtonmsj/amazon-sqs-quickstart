package br.com.amsj.sqs.consumer;

import java.util.List;

import javax.inject.Inject;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import io.quarkus.scheduler.Scheduled;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.Message;

public class QuarksShieldSyncResource {

    private static final Logger LOGGER = Logger.getLogger(QuarksShieldSyncResource.class);

    @Inject
    SqsClient sqs;

    @ConfigProperty(name = "queue.url")
    String queueUrl;

    @Scheduled(every = "10s")
    public void receive() {
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
    }
}