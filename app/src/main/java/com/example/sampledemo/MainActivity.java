package com.example.sampledemo;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.sampledemo.request.multipart.*;

public class MainActivity extends Activity {

	private static final String ARG_SECTION_NUMBER = "section_number";
	private final String TAG = this.getClass().getSimpleName();
	private TextView tvResult;
	private ProgressDialog progressDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		findViewById(R.id.btn_post).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						postRequset();
					}
				});

		findViewById(R.id.btn_upload).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						postUpload();
					}
				});

		tvResult = (TextView) findViewById(R.id.tv_result);

		Bitmap bitmap = BitmapFactory.decodeResource(getResources(),
				R.drawable.ic_launcher);

		saveBitmap2file(bitmap, "test.jpg");
	}

	private String url = "http://apis.haoservice.com/lifeservice/train/ypcx";

	// post 请求
	public void postRequset() {
		RequestQueue requestQueue = Volley.newRequestQueue(this);
		StringRequest stringRequest = new StringRequest(Request.Method.POST,
				url, new Response.Listener<String>() {

					@Override
					public void onResponse(String response) {
						Log.d(TAG, "response -> " + response);
						tvResult.setText("Result:" + response);
						progressDialog.dismiss();
					}
				}, new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError error) {
						Log.e(TAG, error.getMessage(), error);
						progressDialog.dismiss();
					}
				}) {
			@Override
			protected Map<String, String> getParams() {
				// 在这里设置需要post的参数
				Map<String, String> map = new HashMap<String, String>();
				map.put("date", "2015-12-27");
				map.put("from", "北京");
				map.put("to", "南昌");
				map.put("key", "548f8876f2c34cb9aa8b369f3eee408a");

				return map;
			}
		};
		requestQueue.add(stringRequest);
		progressDialog = ProgressDialog.show(this, "Loading...",
				"Please wait...", true, false);

	}
	String netData;
	public String uploadURL = "http://test.haivin.com:8080/kkb/smallPhotography/personal/saveInformation.action";

	// 上传文件
	public void postUpload() {
		RequestQueue queue = Volley.newRequestQueue(this,new MultiPartStack());
		final HashMap<String, String> body = new HashMap<>();
		body.put("userId", "1");
		body.put("userName", "kdfjdkfjksdjfkcdkfjd");
		final HashMap<String, File> files = new HashMap<>();
		File file = new File(Environment.getExternalStorageDirectory(), "userTempAvatar.png");
		files.put("filePath", file);
		PostMultiPartRequest<Object> multiPartRequest = new PostMultiPartRequest<Object>(
				Request.Method.POST, uploadURL,Object.class, new Response.Listener<Object>() {
			@Override
			public void onResponse(Object response) {
                Log.d("jack","dd");
				tvResult.setText("Result:" + response);
				Toast.makeText(MainActivity.this,"ok,success",Toast.LENGTH_LONG).show();
//				listener.callBack(response,200);
			}
		}, new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError volleyError) {
				if (volleyError != null) {
					if (volleyError.networkResponse != null && volleyError.networkResponse.data != null) {
						try {
							String netData = new String(volleyError.networkResponse.data,"utf-8");
							Log.d("jack",netData);
							tvResult.setText("Result:" + netData);
							Toast.makeText(MainActivity.this,netData,Toast.LENGTH_LONG).show();
//								if(isJson(netData)) {
//									Object parsedGSON = mGson.fromJson(new String(volleyError.networkResponse.data,"utf-8"), mJavaClass);
//									listener.callBack(parsedGSON,volleyError.networkResponse.statusCode);
//								} else {
//									listener.callBack(ERROR_MSG,400);
//								}
						} catch (UnsupportedEncodingException e) {
							e.printStackTrace();
						}
					} else {
						tvResult.setText("Result:" + volleyError.getMessage());
						Toast.makeText(MainActivity.this,volleyError.getMessage(),Toast.LENGTH_LONG).show();
//								listener.callBack(volleyError.getMessage(),-1);
					}
				}
			}
		}) {
			@Override
			public Map<String, File> getFileUploads() {
				return files;
			}

			@Override
			public Map<String, String> getStringUploads() {
				return body;
			}

		};

		queue.add(multiPartRequest);

	}

	static boolean saveBitmap2file(Bitmap bmp, String filename) {
		Bitmap.CompressFormat format = Bitmap.CompressFormat.JPEG;
		int quality = 100;
		OutputStream stream = null;
		try {
			stream = new FileOutputStream("/sdcard/" + filename);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		return bmp.compress(format, quality, stream);
	}
}
