package com.enjoygame.wxfyzft;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import com.unity3d.player.UnityPlayer;
import com.unity3d.player.UnityPlayerActivity;
import com.unity3d.player.r;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Environment;
import android.sax.StartElementListener;
import android.util.Base64;
import android.util.Log;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.TextView;
//import android.widget.TextView;
import android.widget.Toast;

import com.enjoygame.kbjhft.R;
import com.enjoygame.sdk.api.EGPay;
import com.enjoygame.sdk.api.EGSDK;
import com.enjoygame.sdk.api.EGSDK.LoginCallback;
import com.enjoygame.sdk.api.EGSDK.UserInfo;
import com.enjoygame.sdk.third.fbv4.Fb;
import com.enjoygame.sdk.third.fbv4.Fb.FeedCallback;
//import com.enjoygame.sdk.third.fbv4.Fb;
import com.enjoygame.sdk.user.LoginMgr.BindEmailCallback;
import com.enjoygame.sdk.user.LoginMgr.BindPhoneCallback;
//import com.facebook.FacebookBroadcastReceiver;


public class MainActivity extends UnityPlayerActivity {

	private static final String TAG = "Unity";
    static MainActivity ma;
    
    private static final String APPID = "107001";
	private static final String APPKEY = "27f58ad9d9e09653d856966531534148";
	private static final String CHANNELID = "wxfyzft";
	
	private String mUid;
	private String mUserName;
	
	//--------------------obbdownloader---------------------------
	ProgressBar pb;
    Dialog dialog;
    int downloadedSize = 0;
    int totalSize = 0;
    TextView cur_value;
//    static String dwnload_file_path = "http://mvlej.emobigames.com/Upload/obb/";
    static String dwnload_file_path = "http://enjoygamewxfyz.s3.amazonaws.com/";
	//------------------------------------------------------------
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Log.d(TAG, "onCreate - MainActivity");
		ma = this;
		EGPay.getInstance().init(this, APPID, APPKEY, CHANNELID);
		EGSDK.getInstance().init(this, APPID, APPKEY, CHANNELID);
		EGSDK.getInstance().onCreate(savedInstanceState);
		EGSDK.getInstance().setLoginCallback(new LoginCallback() {

			@Override
			public void onLoginResult(int code, UserInfo userInfo) {
				// TODO Auto-generated method stub
				handleLoginResult(code, userInfo);
			}
		});
		
		//uncomment to get keyhash
//		printKeyHash(this);
	}
	
	public static String printKeyHash(Activity context) {
        PackageInfo packageInfo;
        String key = null;
        try {
            //getting application package name, as defined in manifest
            String packageName = context.getApplicationContext().getPackageName();

            //Retriving package info
            packageInfo = context.getPackageManager().getPackageInfo(packageName,
                    PackageManager.GET_SIGNATURES);

            Log.e("Package Name=", context.getApplicationContext().getPackageName());

            for (Signature signature : packageInfo.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                key = new String(Base64.encode(md.digest(), 0));

                // String key = new String(Base64.encodeBytes(md.digest()));
                Log.e("Key Hash=", key);
            }
        } catch (PackageManager.NameNotFoundException e1) {
            Log.e("Name not found", e1.toString());
        }
        catch (NoSuchAlgorithmException e) {
            Log.e("No such an algorithm", e.toString());
        } catch (Exception e) {
            Log.e("Exception", e.toString());
        }

        return key;
    }
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		EGSDK.getInstance().onActivityResult(requestCode, resultCode, data);
		Log.d(TAG, "onActivityResult");
		if(requestCode == 1){ //back from mainactivity2
			Log.d(TAG, "onCreate - try to load next scene");
			UnityPlayer.UnitySendMessage("EJSDKManager", "OnEGLoadNextScene", "");
		}
	}

	@Override
	public void onSaveInstanceState(Bundle state) {
		super.onSaveInstanceState(state);
		EGSDK.getInstance().onSaveInstanceState(state);
	}

	@Override
	public void onPause() {
		super.onPause();
		Log.d(TAG, "onPause");
		EGSDK.getInstance().onPause();
	}

	@Override
	public void onResume() {
		super.onResume();
		Log.d(TAG, "onResume");
		EGSDK.getInstance().onResume();
	}

	@Override
	public void onStop() {
		super.onStop();
		Log.d(TAG, "onStop");
		EGSDK.getInstance().onStop();
	}

	@Override
	public void onStart() {
		super.onStart();
		Log.d(TAG, "onStart");
		EGSDK.getInstance().onStart();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.d(TAG, "onDestroy");
		EGSDK.getInstance().destroy();
		EGPay.getInstance().destroy();
	}
	
	private void handleLoginResult(int code, UserInfo userInfo) {
		Log.d(TAG, "handleLoginResult code=" + code);
		if (code == EGSDK.CODE_USER_LOGOUT) {
			mUid = null;
			Log.d(TAG, "handleLoginResult USER LOGOUT");
			onLogoutSuccess();
//			showToast("Log out");
		} else if (code == EGSDK.CODE_USER_LOGIN_SUCCESS) {
			// 登录�?功, 进入游�?逻辑
			mUid = userInfo.uid;
			mUserName = userInfo.account;
			String token = userInfo.token;
			Log.d(TAG, "handleLoginResult uid=" + mUid + " userName:"
					+ mUserName + " token=" + token);
//			showToast("Login Success!");
			onLoginSuccess(userInfo.uid, userInfo.token);
		} else {
//			showToast("Login Fail!");
			onLoginFailded("Login Failed with code " + code);
		}
	}
	
	private void showToast(final String info) {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				Toast.makeText(MainActivity.this, info, Toast.LENGTH_SHORT)
						.show();
			}
		});
	}
	
	/************************************ Connect with Unity **************************************/
	public static void requestLogin(){
		Log.d(TAG, "Unity call ---------------------------requestLogin");
		
		ma.runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				EGSDK.getInstance().login();
			}
		});
	}
	 
	private void onLoginSuccess(String userID, String accessToken){
		Log.e(TAG, "Call back to Unity - OnLoginSuccess");
        UnityPlayer.UnitySendMessage("EJSDKManager",
                "OnLoginSuccess", userID + " " + accessToken);
	}
	
	private void onLoginFailded(String errorMessage){
		Log.e(TAG, "Call back to Unity - OnLoginFailed");
        UnityPlayer.UnitySendMessage("EJSDKManager",
                "OnEGLoginFailed", errorMessage);
	}
	
	public static void switchAccount(){
//		EGSDK.getInstance().switchAccount();
		EGSDK.getInstance().logOut();
	}
	
	private void onLogoutSuccess(){
		Log.e(TAG, "Call back to Unity - OnLogoutSuccess");
		UnityPlayer.UnitySendMessage("EJSDKManager", "OnEGLogoutSuccess", "");
	}
	
	public static void openUserCenter(){
		Log.d(TAG, "Unity call ----------------------- openUserCenter");
		EGSDK.getInstance().openUCenter();
	}
	
	public static void buyGGProduct(String sku, String extra) {
		// 调用支付
		Map<String, String> payInfo = new HashMap<String, String>();
		payInfo.put("SKU", sku);
		payInfo.put("ExtraData", extra);
		payInfo.put("Uid", EGSDK.getInstance().getUserInfo().uid);
//		payInfo.put("RoleId", roleID);

		EGPay.getInstance().payGG(payInfo, new EGPay.PayCallback() {

			@Override
			public void onPayResult(int code) {
				Log.d(TAG, "Pay finished. CODE = " + code);
//				showToast("支付结果:" + code);
			}
		});
	}
	
	public static void buyProduct(String roleID, String extra){
		Map<String, String> payInfo = new HashMap<String, String>();
		payInfo.put("Uid", EGSDK.getInstance().getUserInfo().uid);
		payInfo.put("ExtraData", extra);
		payInfo.put("RoleId", roleID);
		payInfo.put("ProductName", "golds");

		EGPay.getInstance().pay(payInfo, new EGPay.PayCallback() {

			@Override
			public void onPayResult(int code) {
				Log.d(TAG, "Pay finished. CODE = " + code);
//				showToast("支付结果:" + code);
			}
		});
	}
	
	public static void fbshare(final String title, final String desc, final String url, final String imageUrl ) {
		
		ma.runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				Map<String, String> info = new HashMap<String, String>();
				info.put("Title", title);
				info.put("Desc", desc);
				info.put("Url", url);
				info.put("ImageUrl", imageUrl);

				Fb.getInstance().feed(info,  new FeedCallback() {
					
					@Override
					public void onFeedResult(int code) {
						// TODO Auto-generated method stub
						if(code == 0)
						{
							Log.d(TAG, "Share FB Success");
//							ma.showToast("Share FB Success");
						}
						else
						{
							Log.d(TAG, "Share FB Fail");
//							ma.showToast("Share FB Fail");
						}
					}
				});
			}
		});
	}

	/************************************ OBBDownloader **************************************/
//	public static void requestMainPath(int versionCode){
//		String obbPath = Environment.getExternalStorageDirectory().getPath() + "/Android/obb/" + ma.getPackageName();
//		String fileName = "main." + versionCode + "." + ma.getPackageName() + ".obb";
//		String mainPath = obbPath + "/" + fileName;
//		UnityPlayer.UnitySendMessage("EJSDKManager", "OnEGRequestMainPath", mainPath);
//	}
	
	
	public static void requestCheckObbAvaiable(int versionCode){
		Log.d(TAG, "RequestCheckObbAvaiable");
		File file = ma.checkObbFile(versionCode);
		
		if(file.exists())
			UnityPlayer.UnitySendMessage("EJSDKManager", "OnEGCheckObbAvaiable", file.getPath());
		else
			UnityPlayer.UnitySendMessage("EJSDKManager", "OnEGCheckObbAvaiable", "");
	}
	
	File checkObbFile(int versionCode){
		String obbPath = Environment.getExternalStorageDirectory().getPath() + "/Android/obb/" + this.getPackageName();
		String fileName = "main." + versionCode + "." + this.getPackageName() + ".obb";
		File file = new File(obbPath, fileName);
		Log.d(TAG, "Call back to Unity - CheckObbFile " + file.exists());
		return file;
	}
	
	public static void requestDownloadObb(int versionCode){
		Log.d(TAG, "RequestDownloadObb");
		
		final int version = versionCode;
		
		ma.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				String fileName = "main." + version + "." + ma.getPackageName() + ".obb";
				ma.showProgress(dwnload_file_path + fileName);
				
				new Thread(new Runnable() {
		            public void run() {
		            	 ma.downloadFile(version);
		            }
		          }).start();
			}
		});
	}
	
	void OnEGDownloadObb(boolean success){
		Log.d(TAG, "Call back to Unity - OnEGDownloadObb");
		if(success)
			UnityPlayer.UnitySendMessage("EJSDKManager", "OnEGDownloadObb", "success");
		else
			UnityPlayer.UnitySendMessage("EJSDKManager", "OnEGDownloadObb", "fail");
	}
	
    void downloadFile(int versionCode){
    	
    	try {
    		//set the path where we want to save the file    
    		String obbPath = Environment.getExternalStorageDirectory().getPath() + "/Android/obb/" + this.getPackageName();
    		Log.d(TAG, "PATH = " + obbPath);
    		//delete all files
    		File dir = new File(obbPath);
    		if (dir.isDirectory()) 
    		{
    		    String[] children = dir.list();
    		    for (int i = 0; i < children.length; i++)
    		    {
    		    	Log.d(TAG, "Delete file : " + children[i].toString());
    		    	new File(dir, children[i]).delete();
    		    }
    		}
    		
    		File saveFile = new File(obbPath);
    		String fileName = "main." + versionCode + "." + this.getPackageName() + ".obb";
    		
    		URL url = new URL(dwnload_file_path + fileName);
    		HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
    		urlConnection.setRequestMethod("GET");
//    		urlConnection.setDoOutput(true);

    		//connect
    		urlConnection.connect();
    		Log.d(TAG, "Response Code = " + urlConnection.getResponseCode());

    		//create a new file, to save the downloaded file 
    		File file = new File(saveFile, fileName);
    		
    		FileOutputStream fileOutput = new FileOutputStream(file);

    		//Stream used for reading the data from the internet
    		InputStream inputStream = urlConnection.getInputStream();
    		
    		//this is the total size of the file which we are downloading
    		downloadedSize = 0;
    		totalSize = urlConnection.getContentLength();

    		runOnUiThread(new Runnable() {
			    public void run() {
			    	pb.setMax(totalSize);
			    }			    
			});
    		
    		//create a buffer...
    		byte[] buffer = new byte[1024];
    		int bufferLength = 0;

    		while ( (bufferLength = inputStream.read(buffer)) > 0 ) {
    			fileOutput.write(buffer, 0, bufferLength);
    			downloadedSize += bufferLength;
    			// update the progressbar //
    			runOnUiThread(new Runnable() {
    			    public void run() {
    			    	pb.setProgress(downloadedSize);
    			    	float per = ((float)downloadedSize/totalSize) * 100;
    			    	cur_value.setText("下載完畢 " + downloadedSize / 1024 + "KB / " + totalSize / 1024 + "KB (" + (int)per + "%)" );
    			    					//Downloaded
    			    }
    			});
    		}
    		//close the output stream when complete //
    		fileOutput.close();
    		//Call back to Unity
    		OnEGDownloadObb(true);
    		startMainActivity2();
    		
    		
    		runOnUiThread(new Runnable() {
			    public void run() {
			    	dialog.dismiss(); // if you want close it..
			    }
			});    		
    	
    	} catch (final MalformedURLException e) {
//    		showError("Error : MalformedURLException " + e);
    		showError("錯誤：請檢查妳當地的網絡！");
    		e.printStackTrace();
    		OnEGDownloadObb(false);
    		runOnUiThread(new Runnable() {
			    public void run() {
			    	dialog.dismiss(); // if you want close it..
			    }
			});
    	} catch (final IOException e) {
//    		showError("Error : IOException " + e);
    		showError("錯誤：請檢查妳當地的網絡！");
    		e.printStackTrace();
    		OnEGDownloadObb(false);
    		runOnUiThread(new Runnable() {
			    public void run() {
			    	dialog.dismiss(); // if you want close it..
			    }
			});
    	}
    	catch (final Exception e) {
    		showError("錯誤：請檢查妳當地的網絡！"); //Error : Please check your internet connection 
    		OnEGDownloadObb(false);
    		runOnUiThread(new Runnable() {
			    public void run() {
			    	dialog.dismiss(); // if you want close it..
			    }
			});
    	}    	
    }
    
    void showError(final String err){
    	runOnUiThread(new Runnable() {
		    public void run() {
		    	Toast.makeText(MainActivity.this, err, Toast.LENGTH_LONG).show();
		    }
		});
    }
    
    void showProgress(String file_path){
    	dialog = new Dialog(MainActivity.this);
    	dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
    	dialog.setContentView(R.layout.myprogressdialog);
    	dialog.setCanceledOnTouchOutside(false);
    	dialog.setTitle("下載進度"); //Download Progress

    	TextView text = (TextView) dialog.findViewById(R.id.tv1);
    	text.setText("下載中"); //Downloading
    	cur_value = (TextView) dialog.findViewById(R.id.cur_pg_tv);
    	cur_value.setText("開始下載。。。"); //Starting download...
    	dialog.show();
    	
    	pb = (ProgressBar)dialog.findViewById(R.id.progress_bar);
    	pb.setProgress(0);
    	pb.setProgressDrawable(getResources().getDrawable(R.drawable.green_progress));  
    }
	
    void startMainActivity2(){
    	Intent i = new Intent(getApplicationContext(), MainActivity2.class);
    	Log.d("Unity", "start Mainactivity2");
//    	startActivity(i);
    	startActivityForResult(i, 1);
    }
}
