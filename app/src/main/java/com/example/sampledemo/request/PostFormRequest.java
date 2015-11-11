package com.example.sampledemo.request;

import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.android.volley.Response.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;



/**
 * Created by mjliu on 15/3/25.
 */
public class PostFormRequest<T> extends Request<T> {

    /**
     * 正确数据的时候回掉用
     */
//    private ResponseListener mListener ;
    private final Listener<T> mListener;
    /*用来解析 json 用的*/
    private Gson mGson ;
    /*在用 gson 解析 json 数据的时候，需要用到这个参数*/
    private Type mClazz ;//= new TypeToken<T>() {}.getType(); ;
    /*请求 数据通过参数的形式传入*/
    private Map<String, String> params;

    private String BOUNDARY = UUID.randomUUID().toString(); //数据分隔线
    private String MULTIPART_FORM_DATA = "multipart/form-data";

    public PostFormRequest(String url, Type type,Map<String, String> param, Listener<T> listener,
                           Response.ErrorListener errorListener) {
        super(Method.POST, url,errorListener);
        this.mListener = listener;
        params = param;
        mClazz = type;

        mGson = new GsonBuilder()
//                .setExclusionStrategies(new ExclusionStrategy() {
//                    @Override
//                    public boolean shouldSkipField(FieldAttributes f) {
//                        return f.getDeclaringClass().equals(RealmObject.class);
//                    }
//
//                    @Override
//                    public boolean shouldSkipClass(Class<?> clazz) {
//                        return false;
//                    }
//                })
                .create();
    }

    /**
     * 这里开始解析数据
     * @param response Response from the network
     * @return
     */
    @Override
    protected Response<T> parseNetworkResponse(NetworkResponse response) {
        try {
            T result ;
            String jsonString =
                    new String(response.data, "utf-8");
            Log.v("zgy", "====SearchResult===" + jsonString);
            result = mGson.fromJson(jsonString,mClazz) ;
            return Response.success(result,
                    HttpHeaderParser.parseCacheHeaders(response));
        } catch (UnsupportedEncodingException e) {
            return Response.error(new ParseError(e));
        }
    }

    /**
     * 回调正确的数据
     * @param response The parsed response returned by
     */
    @Override
    protected void deliverResponse(T response) {
        mListener.onResponse(response);
    }

    @Override
    public byte[] getBody() throws AuthFailureError {
        if (params == null||params.size() == 0){
            return super.getBody() ;
        }
        ByteArrayOutputStream bos = new ByteArrayOutputStream() ;
        Iterator<String> it = params.keySet().iterator();
        while (it.hasNext()){
            String key = it.next();
            String value = params.get(key);
            StringBuffer sb= new StringBuffer() ;
            /*第一行:"--" + boundary + "\r\n" ;*/
            sb.append("--"+BOUNDARY);
            sb.append("\r\n") ;
            /*第二行:"Content-Disposition: form-data; name="参数的名称"" + "\r\n" ;*/
            sb.append("Content-Disposition: form-data;");
            sb.append("name=\"");
            sb.append(key) ;
            sb.append("\"") ;
            sb.append("\r\n") ;
            /*第三行:"\r\n" ;*/
            sb.append("\r\n") ;
            /*第四行:"参数的值" + "\r\n" ;*/
            sb.append(value) ;
            sb.append("\r\n") ;
            try {
                bos.write(sb.toString().getBytes("utf-8"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        /*结尾行:"--" + boundary + "--" + "\r\n" ;*/
        String endLine = "--" + BOUNDARY + "--"+ "\r\n" ;
        try {
            bos.write(endLine.toString().getBytes("utf-8"));
            bos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.v("zgy","=====formText====\n"+bos.toString()) ;
        return bos.toByteArray();
    }

    /*获取内容类型，这里为表单类型*/
    @Override
    public String getBodyContentType() {
        return MULTIPART_FORM_DATA+"; boundary="+BOUNDARY;
    }
}
