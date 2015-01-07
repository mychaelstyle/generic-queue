/**
 * 
 */
package com.mychaelstyle.common.queue;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import org.json.JSONArray;
import org.json.JSONObject;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSAsyncClient;
import com.amazonaws.services.sqs.model.CreateQueueRequest;
import com.amazonaws.services.sqs.model.CreateQueueResult;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.GetQueueAttributesRequest;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageResult;
import com.mychaelstyle.common.GenericQueue;
import com.mychaelstyle.common.GenericQueue.Provider;

/**
 * AWS AwsSQS Provider
 * 
 * You need to call 'initialize' method at first.
 * 
 * [config.json]
 * <pre>
 * {
 *     "endpoint"   : "aws sqs endpoint",
 *     "name"       : "your queue name",
 *     "access_key" : "your aws access key",
 *     "secret"     : "your aws secret"
 * }
 * </pre>
 * 
 * @author Masanori Nakashima
 */
public class AwsSQS implements Provider {

    /**
     * JSON Parameter : end point
     */
    public static final String PARAM_ENDPOINT = "endpoint";
    
    /**
     * JSON parameter : access key
     */
    public static final String PARAM_ACCESS_KEY = "access_key";

    /**
     * JSON parameter : secret
     */
    public static final String PARAM_SECRET_KEY = "secret";

    /**
     * sqs message id
     */
    public static final String ITEM_MESSAGE_ID = "sqs_id";
    /**
     * sqs receipt
     */
    public static final String ITEM_RECEIPT = "sqs_receipt";

    /** AwsSQS client */
    private AmazonSQS client = null;
    /** 最終メッセージID */
    public String lastMessageId = null;
    /** キューURL */
    public String queueUrl = null;
    /** レシート */
    public Queue<String> receipts = new LinkedList<String>();

    /**
     * Constructor
     */
    public AwsSQS() {
        super();
    }

    /* (non-Javadoc)
     * @see com.metaps.util.MQueue.Provider#initialize(org.json.JSONObject)
     */
    @Override
    public void initialize(JSONObject config) throws Exception {
        String endpoint = config.getString(PARAM_ENDPOINT);
        String queueName = config.getString(GenericQueue.PARAM_NAME);
        String accessKey = config.getString(PARAM_ACCESS_KEY);
        String secretKey = config.getString(PARAM_SECRET_KEY);
        if(accessKey==null) throw new Exception(PARAM_ACCESS_KEY+" is required!");
        if(secretKey==null) throw new Exception(PARAM_SECRET_KEY+" is required!");
        try {
            this.client = new AmazonSQSAsyncClient(new BasicAWSCredentials(accessKey,secretKey));
            this.client.setEndpoint(endpoint);
            CreateQueueRequest createQueueRequest = new CreateQueueRequest(queueName);
            CreateQueueResult createQueueResult = this.client.createQueue(createQueueRequest);
            this.queueUrl = createQueueResult.getQueueUrl();
        } catch (Throwable e){
            throw new Exception(e);
        }
    }

    /* (non-Javadoc)
     * @see com.metaps.util.MQueue.Provider#add(org.json.JSONObject)
     */
    @Override
    public void add(JSONObject json) throws Exception {
        try {
            SendMessageRequest sendMessageRequest = new SendMessageRequest()
            .withQueueUrl(this.queueUrl)
            .withMessageBody(json.toString());
            SendMessageResult sendMessageResult = this.client.sendMessage(sendMessageRequest);
            lastMessageId = sendMessageResult.getMessageId();
        } catch (Throwable e){
            throw new Exception(e);
        }
    }

    /* (non-Javadoc)
     * @see com.metaps.util.MQueue.Provider#poll()
     */
    @Override
    public JSONArray poll(int max) throws Exception {
        JSONArray array = this.peek(max);
        this.delete();
        return array;
    }

    /* (non-Javadoc)
     * @see com.metaps.util.MQueue.Provider#peek()
     */
    @Override
    public JSONArray peek(int max) throws Exception {
        ReceiveMessageRequest request = new ReceiveMessageRequest()
            .withQueueUrl(this.queueUrl).withMaxNumberOfMessages(max);
        ReceiveMessageResult result = this.client.receiveMessage(request);
        JSONArray array = new JSONArray();
        for (Message message : result.getMessages()) {
            JSONObject obj = new JSONObject(message.getBody());
            this.receipts.add(message.getReceiptHandle());
            obj.put(ITEM_RECEIPT, message.getReceiptHandle());
            obj.put(ITEM_MESSAGE_ID, message.getMessageId());
            array.put(obj);
        }
        return array;
    }

    /* (non-Javadoc)
     * @see com.metaps.util.MQueue.Provider#delete()
     */
    @Override
    public void delete() throws Exception {
        while(this.receipts.size()>0){
            try {
                String receipt = this.receipts.poll();
                DeleteMessageRequest deleteMessageRequest = new DeleteMessageRequest()
                .withQueueUrl(this.queueUrl).withReceiptHandle(receipt);
                this.client.deleteMessage(deleteMessageRequest);
            } catch(Throwable e){
                e.printStackTrace();
            }
        }
    }

    /* (non-Javadoc)
     * @see com.metaps.util.MQueue.Provider#delete(org.json.JSONObject)
     */
    @Override
    public void delete(JSONObject data) throws Exception {
        String receipt = data.getString(ITEM_RECEIPT);
        try {
            DeleteMessageRequest deleteMessageRequest = new DeleteMessageRequest()
            .withQueueUrl(this.queueUrl).withReceiptHandle(receipt);
            this.client.deleteMessage(deleteMessageRequest);
        } catch(Throwable e){
            e.printStackTrace();
        }        
    }

    @Override
    public long size() throws Exception {
        // get all the attributes of the queue 
        List<String> attributeNames = new ArrayList<String>();
        attributeNames.add("All");
        // list the attributes of the queue we are interested in
        GetQueueAttributesRequest request = new GetQueueAttributesRequest(queueUrl);
        request.setAttributeNames(attributeNames);
        Map<String, String> attributes = client.getQueueAttributes(request)
                .getAttributes();
        int messages = Integer.parseInt(attributes
                .get("ApproximateNumberOfMessages"));
        //int messagesNotVisible = Integer.parseInt(attributes
        //        .get("ApproximateNumberOfMessagesNotVisible"));
        return (long) messages;
    }

}
