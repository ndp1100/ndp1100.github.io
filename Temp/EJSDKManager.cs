using UnityEngine;
using System.Runtime.InteropServices;
using System.Collections;
using AOT;
using System;


public class EJSDKManager : MonoBehaviour
{
#if EJ_SDK
    public static EJSDKManager instance;

#if UNITY_ANDROID
    AndroidJavaClass jc;
#elif UNITY_IPHONE
	[DllImport("__Internal")]
	private static extern void _requestLogin(OnLoginSuccessCallback onLoginSuccess, OnLoginFailCallback onLoginFail);
	[DllImport("__Internal")]
	private static extern void _requestLogout(OnLogoutSuccessCallback onLogoutSuccess);
	[DllImport("__Internal")]
	private static extern void _requestOpenUserCenter();
	[DllImport("__Internal")]
	private static extern void _requestPayment(OnPaymentSuccessCallback onPaymentSuccess, string sku, string extra, string price, string roleID);
	[DllImport("__Internal")]
	private static extern void _requestShareFBCode(string url, string desc, string title);
#endif

    // Use this for initialization
    void Awake()
    {
        instance = this;

        Init();
    }

    public void Init()
    {
#if UNITY_ANDROID
        jc = new AndroidJavaClass("unityplugin.phuongnd.com.androidplugin.MainActivity");
        Debug.Log("JavaClass -----------------> " + jc);
#endif
    }

    public void Login()
    {
#if UNITY_ANDROID
        jc.CallStatic("requestLogin");
        Debug.Log("Call login!");
#elif UNITY_IPHONE
		_requestLogin(OnEGLoginSuccess, OnEGLoginFailed);
#endif
    }

#if UNITY_IPHONE && !BUILD_IOS_32
    delegate void OnLoginSuccessCallback(string idTokenisPlayNow);
    [MonoPInvokeCallback(typeof(OnLoginSuccessCallback))]
    public static void OnEGLoginSuccess(string idToken)
#elif UNITY_ANDROID || BUILD_IOS_32 || UNITY_EDITOR || UNITY_WP8 || UNITY_STANDALONE
    public void OnLoginSuccess(string idToken)
#endif
    {
        PopupNetworkLoading.DestroyPopup();
        PlayerPrefs.SetInt("login", 1);
        string[] strs = idToken.Split(" ".ToCharArray());
        string id = strs[0];
        string token = strs[1];
        Debug.Log("Login success! userName = " + id + " AccessToken = " + token);
        GameManager.instance.m_userName = id;
        GameManager.instance.m_accessToken = token;
        PlayerPrefs.SetString("UID", id);

        GameManager.instance.m_EntryClient.IssueConnect();
    }

#if UNITY_IOS && !BUILD_IOS_32
		delegate void OnLoginFailCallback(string errorMessage);
		[MonoPInvokeCallback(typeof(OnLoginFailCallback))]
		public void OnEGLoginFailed(string errorMessage)
#elif UNITY_ANDROID || BUILD_IOS_32 || UNITY_EDITOR || UNITY_WP8 || UNITY_STANDALONE
    public void OnEGLoginFailed(string errorMessage)

#endif
    {
        PopupNetworkLoading.DestroyPopup();
        MessagePopup.Create(Localization.instance.Get("LoginFailMessage"));
        Debug.LogWarning(errorMessage);
    }

    public void Logout()
    {
#if UNITY_ANDROID
        jc.CallStatic("switchAccount");
#elif UNITY_IPHONE
		_requestLogout(OnEGLogoutSuccess);
#endif
    }

#if UNITY_IPHONE && !BUILD_IOS_32
	delegate void OnLogoutSuccessCallback(string message);
	[MonoPInvokeCallback(typeof(OnLogoutSuccessCallback))]	
	public static void OnEGLogoutSuccess(string message)
#elif UNITY_ANDROID || BUILD_IOS_32 || UNITY_EDITOR || UNITY_WP8|| UNITY_STANDALONE
		public void OnEGLogoutSuccess(string message)
#endif
	{
		//logout sucesss here
		PlayerPrefs.SetInt("login", 0);
		Debug.Log("Logout success!");

        //ScreenLogin loginScreen = GUIManager.getScreen(GAME_SCREEN.ScreenLogin) as ScreenLogin;
        //if (loginScreen == null)
        //{
            GameManager.instance.m_GameClient.LogOffGameServer();
        //}
	}


    public void Payment(string sku, string price)
    {
#if UNITY_ANDROID
        if (jc != null)
        {
            int gid = GameManager.instance.GamerID;
            int gameserverId = GameManager.instance.ServerID;
            string extra = string.Format("{{\"ServerId\":{0}, \"GID\":{1}}}", gameserverId, gid);
            Debug.Log("EXTRA = " + extra + "------------------------------------------");
            jc.CallStatic("buyGGProduct", sku, extra);
        }
        else
        {

        }
#elif UNITY_IPHONE
		int gid = GameManager.instance.GamerID;
		int gameserverId = GameManager.instance.ServerID;
		string extra = string.Format("{{\"ServerId\":{0}, \"GID\":{1}}}", gameserverId, gid);
		Debug.Log("EXTRA = " + extra + "------------------------------------------");
		_requestPayment(OnEGPaymentSuccess, sku, extra, price, gid.ToString());
#endif
    }

    public void ThirdPayment()
    {
#if UNITY_ANDROID
        if(jc != null){
            int gid = GameManager.instance.GamerID;
            int gameserverId = GameManager.instance.ServerID;
            string extra = string.Format("{{\"ServerId\":{0}, \"GID\":{1}}}", gameserverId, gid);
            Debug.Log("EXTRA = " + extra + "------------------------------------------");
            jc.CallStatic("buyProduct", gid.ToString(), extra);
        }
#endif
    }

#if UNITY_IPHONE && !BUILD_IOS_32
	delegate void OnPaymentSuccessCallback(string message);
	[MonoPInvokeCallback(typeof(OnPaymentSuccessCallback))]	
	public static void OnEGPaymentSuccess(string message)
#elif UNITY_ANDROID || BUILD_IOS_32 || UNITY_EDITOR || UNITY_WP8|| UNITY_STANDALONE
		public void OnEGPaymentSuccess(string message)
#endif
	{
		//payment sucesss here
		Debug.Log("Payment success! ---------------");
	}


    public void OpenUserCenter()
    {
#if UNITY_ANDROID
        jc.CallStatic("openUserCenter");
#elif UNITY_IPHONE
		_requestOpenUserCenter();
#endif
    }

    public void ShareFBCode()
    {
        string title = Localization.instance.Get("ShareFBTitle");
        string invite_code = ConfigManager.instance.GenerateInviteCode(GameManager.instance.ServerID, GameManager.instance.GamerID);
        string disc = string.Format(Localization.instance.Get("ShareFBDisc"), invite_code);
        string url = Localization.instance.Get("ShareFBUrl");
        string imageUrl = Localization.instance.Get("ShareFbImageUrl");
#if UNITY_ANDROID
        jc.CallStatic("fbshare", title, disc, url, imageUrl);
#elif UNITY_IPHONE
		_requestShareFBCode(url, disc, title);
#endif
    }

    //--------------------------------------DownloadObb (Android Only)-----------------------------------------
#if UNITY_ANDROID
    public void RequestCheckObbAvaiable(int versionCode)
    {
        jc.CallStatic("requestCheckObbAvaiable", versionCode);
    }

    public void OnEGCheckObbAvaiable(string message)
    {
        Debug.Log("OnEGCheckObbAvaiable + message = " + message);
        if (string.IsNullOrEmpty(message))
        {
            DownloadObbExample.instance.OnEGCheckObbAvaiable(false);
        }
        else
        {
            DownloadObbExample.instance.OnEGCheckObbAvaiable(true);
        }
    }

    public void RequestDownloadObb(int versionCode)
    {
        jc.CallStatic("requestDownloadObb", versionCode);
    }

    public void OnEGDownloadObb(string message)
    {
        if (message.Equals("success"))
        {
            DownloadObbExample.instance.OnDownload(true);
        }
        else if (message.Equals("fail"))
        {
            DownloadObbExample.instance.OnDownload(false);
        }
    }

    public void OnEGLoadNextScene(string message)
    {
        Debug.Log("OnEGLoadNextScene - In Unity");
        DownloadObbExample.instance.GoToGameScene();
    }

#endif




#endif
}
