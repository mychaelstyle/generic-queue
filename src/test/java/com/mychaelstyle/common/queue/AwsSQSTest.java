/**
 * 
 */
package com.mychaelstyle.common.queue;

import static org.junit.Assert.*;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.mychaelstyle.common.GenericQueueTest;

/**
 * JUnit Test case for AwsSQS class
 * @author Masanori Nakashima
 */
public class AwsSQSTest {

    private AwsSQS queue = null;

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
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        queue = new AwsSQS();
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
    }

    /**
     * Test method for {@link com.mychaelstyle.common.queue.AwsSQS#initialize(org.json.JSONObject)}.
     */
    @Test
    public void testInitialize() {
        JSONObject config = GenericQueueTest.getConfig();
        try {
            queue.initialize(config);
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getClass().getName()+" "+e.getMessage());
        }
    }

    /**
     * Test method for {@link com.mychaelstyle.common.queue.AwsSQS#add(org.json.JSONObject)}.
     * Test method for {@link com.mychaelstyle.common.queue.AwsSQS#poll(int)}.
     * Test method for {@link com.mychaelstyle.common.queue.AwsSQS#peek(int)}.
     * Test method for {@link com.mychaelstyle.common.queue.AwsSQS#delete()}.
     * Test method for {@link com.mychaelstyle.common.queue.AwsSQS#delete(org.json.JSONObject)}.
     * Test method for {@link com.mychaelstyle.common.queue.AwsSQS#size()}.
     */
    @Test
    public void test() {
         try {

             this.queue.add(new JSONObject("{param:100}"));

             JSONArray arr = this.queue.poll(1);
             assertNotNull(arr);
             assertTrue(arr.length()>0);

             this.queue.add(new JSONObject("{param:100}"));
             this.queue.add(new JSONObject("{param:200}"));
             arr = this.queue.peek(2);
             assertNotNull(arr);
             assertTrue(arr.length()==2);
             this.queue.delete();
             this.queue.delete();
             assertTrue(0==this.queue.size());

             this.queue.add(new JSONObject("{param:200}"));
             arr = this.queue.peek(1);
             JSONObject obj = arr.getJSONObject(0);
             this.queue.delete(obj);
             assertTrue(0==this.queue.size());

        } catch (JSONException e) {
            e.printStackTrace();
            fail("JSONException "+e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getClass().getName()+" "+e.getMessage());
        }

    }

}
