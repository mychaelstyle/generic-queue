/**
 * Generic queue object class
 */
package com.mychaelstyle.common;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Generic queue object class
 * 
 * @author Masanori Nakashima
 */
public class GenericQueue {

    /** JSON field name : queue name */
    public static final String PARAM_NAME = "name";

    /** JSON parameter name : provider class name */
    public static final String PARAM_PROVIDER = "provider";

    /** Queue name */
    private String queueName = null;

    /** Queue Provider instance */
    private Provider provider = null;

    /**
     * Constructor
     * 
     * @param config org.json.JSONObject
     */
    public GenericQueue(JSONObject config) throws Exception {
        this.queueName = config.getString(PARAM_NAME);
        String cname = config.getString(PARAM_PROVIDER);
        Class<?> clazz = Class.forName(cname);
        this.provider = (Provider) clazz.newInstance();
        this.provider.initialize(config);
    }

    /**
     * Queue Provider
     * @author Masanori Nakashima
     */
    public interface Provider {
        /**
         * 設定をJSON形式で渡してプロバイダを初期化します
         * @param config
         * @throws Exception
         */
        public void initialize(JSONObject config) throws Exception;
        /**
         * キューにメッセージをJSON形式で追加します
         * @param json キューに追加するJSONオブジェクト。256kを超えないでください。
         * @throws Exception
         */
        public void add(JSONObject json) throws Exception;
        /**
         * キューをポーリングします。取得したメッセージは削除します。
         * @return 取得したキューのJSONオブジェクト
         * @throws Exception
         */
        public JSONArray poll(int max) throws Exception;
        /**
         * キューをポーリングします。取得したメッセージはdeleteを呼び出すまで削除しません.
         * 返す配列要素のJSONオブジェクトは必ずキューを削除するためのキーを含むものとします。
         * 
         * @return 取得したキューのJSONオブジェクト
         * @throws Exception
         */
        public JSONArray peek(int max) throws Exception;
        /**
         * peekで最後に取得したメッセージを削除します.
         * @throws Exception
         */
        public void delete() throws Exception;
        /**
         * peekで取得したメッセージを削除します.
         * @param key
         * @throws Exception
         */
        public void delete(JSONObject data) throws Exception;
        /**
         * 現在のキューのサイズを返す
         * @return キューのサイズ
         * @throws Exception
         */
        public long size() throws Exception;
    }

    /**
     * キューにメッセージをJSON形式で追加します
     * @param json キューに追加するJSONオブジェクト。256kを超えないでください。
     * @throws Exception
     */
    public void add(JSONObject json) throws Exception {
        this.provider.add(json);
    }

    /**
     * キューをポーリングします。取得したメッセージは削除します。
     * @return 取得したキューのJSONオブジェクト
     * @throws Exception
     */
    public JSONArray poll(int max) throws Exception {
        return this.provider.poll(max);
    }

    /**
     * キューをポーリングします。取得したメッセージはdeleteを呼び出すまで削除しません。
     * @return 取得したキューのJSONオブジェクト
     * @throws Exception
     */
    public JSONArray peek(int max) throws Exception {
        return this.provider.peek(max);
    }

    /**
     * peekで最後に取得したメッセージを削除します。
     * @throws Exception
     */
    public void delete() throws Exception {
        this.provider.delete();
    }

    /**
     * peekで取得したデータをキューから削除します
     * @param data
     * @throws Exception
     */
    public void delete(JSONObject data) throws Exception {
        this.provider.delete(data);
    }

    /**
     * @return the queueName
     */
    public String getQueueName() {
        return queueName;
    }

    /**
     * @return the provider
     */
    public Provider getProvider() {
        return provider;
    }

    /**
     * キューのサイズを取得
     * @return
     * @throws Exception
     */
    public long size() throws Exception {
        return provider.size();
    }

}
