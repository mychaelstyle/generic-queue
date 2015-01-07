/**
 * 
 */
package com.mychaelstyle.common;

import static org.junit.Assert.*;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.mychaelstyle.common.queue.AwsSQS;

/**
 * JUnit Test case for GenericQueue
 * @author Masanori Nakashima
 *
 */
public class GenericQueueTest {

    public static final String QUEUE_NAME = "testqueue";

    private GenericQueue queue = null;

    /**
     * @throws java.lang.Exception
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    /**
     * @throws java.lang.Exception
     */
    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    /**
     * common configuration for testing
     * @return
     */
    public static JSONObject getConfig(){
        JSONObject config = new JSONObject();
        config.put(GenericQueue.PARAM_NAME, QUEUE_NAME);
        config.put(GenericQueue.PARAM_PROVIDER, "com.mychaelstyle.common.queue.AwsSQS");
        config.put(AwsSQS.PARAM_ACCESS_KEY,System.getenv("AWS_ACCESS_KEY"));
        config.put(AwsSQS.PARAM_SECRET_KEY, System.getenv("AWS_SECRET_KEY"));
        config.put(AwsSQS.PARAM_ENDPOINT, System.getenv("AWS_ENDPOINT_SQS"));
        return config;
    }

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        JSONObject config = getConfig();
        try {
            queue = new GenericQueue(config);
             while(this.queue.size()>0){
                 this.queue.poll(10);
             }
        } catch (Exception e) {
            fail(e.getMessage());
        }

    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
        if(null!=this.queue){
             while(this.queue.size()>0){
                 this.queue.poll(10);
             }
        }
    }

    /**
     * Test method for {@link com.mychaelstyle.common.GenericQueue#GenericQueue(org.json.JSONObject)}.
     */
    @Test
    public void testGenericQueue() {
    }

    /**
     * Test method for {@link com.mychaelstyle.common.GenericQueue#size()}.
     * Test method for {@link com.mychaelstyle.common.GenericQueue#getQueueName()}.
     * Test method for {@link com.mychaelstyle.common.GenericQueue#add(org.json.JSONObject)}.
     * Test method for {@link com.mychaelstyle.common.GenericQueue#poll(int)}.
     * Test method for {@link com.mychaelstyle.common.GenericQueue#peek(int)}.
     * Test method for {@link com.mychaelstyle.common.GenericQueue#delete()}.
     * Test method for {@link com.mychaelstyle.common.GenericQueue#delete(org.json.JSONObject)}.
     * Test method for {@link com.mychaelstyle.common.GenericQueue#getProvider()}.
     */
    @Test
    public void test() {
         try {
             String name = this.queue.getQueueName();
             assertEquals(name,QUEUE_NAME);

             this.queue.add(new JSONObject("{param:100}"));
             Thread.sleep(2000);

             JSONArray arr = this.queue.poll(1);
             assertNotNull(arr);
             assertTrue(arr.length()>0);

             this.queue.add(new JSONObject("{param:100}"));
             Thread.sleep(2000);

             arr = this.queue.peek(1);
             assertNotNull(arr);
             this.queue.delete();
             assertTrue(0==this.queue.size());

             this.queue.add(new JSONObject("{param:200}"));
             Thread.sleep(2000);

             arr = this.queue.peek(1);
             JSONObject obj = arr.getJSONObject(0);
             this.queue.delete(obj);
             Thread.sleep(2000);

             assertTrue(0==this.queue.size());

             GenericQueue.Provider provider = this.queue.getProvider();
             assertEquals(AwsSQS.class.getName(), provider.getClass().getName());

        } catch (JSONException e) {
            e.printStackTrace();
            fail("JSONException "+e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getClass().getName()+" "+e.getMessage());
        }
    }

}
