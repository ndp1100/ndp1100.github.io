---
layout: post
title: How to create a Native Android Plugin for Unity3D
---

 It's awesome when you can use some native functionaly of Android (or iOS/WP) in Unity3D, such as show Toats, take a picture, show dialog...vv.vv. Today, I will show how to create a Native Android Plugin for Unity3D.

 There are all step you need to do.

 - Create Android Project. Write some native functionaly.
 - Make jar file (plugin you will add to Unity Project later).
 - Create Unity Project and use Native Android code in Unity.

That's all. Let's go step by step.

##1. Create Android Project. 

You can use Eclipse of Android Studio to create Android Project. I suggest Android Studio - offical tool for Android, in my opinion, it's better than Eclipse :).

As normal game or application, the first thing you need to do is create main activity. Diffence between Unity Game and native android app is your main activity need to be extend from *UnityPlayerActivity*. So, you need to add a library of Unity to Android Plugin Project. 

There are path to classes.jar.
- Window : C:\Program Files (x86)\Unity\Editor\Data\PlaybackEngines\androiddevelopmentplayer\bin\classes.jar.

- Mac OS: In /Applications/Unity, right click on Unity application -> show Content.
/Applications/Unity/Contents/PackageEngines/AndroidPlayer/development/bin/classes.jar
![Test Image](/images/AndroidPlugin/2.png)

 Add jar file to Android Project. Need to set Scope is Provided. Why you need to set Scope is Provided. Because when you build later in Unity later, Unity will automatic add this library, so you will get error **already added**.
[_config.yml]({{ site.baseurl }}/images/AndroidPlugin/AndroidPlugin-addclassesjar.PNG)

There are two things you can do while create Native Android Plugin for Unity.
1. Write native code in Android then call it from Unity code(C#) later. 
2. Write native code in Android to send information to Unity.

First thing, you need create a public static method in Java like this.
[_config.yml]({{ site.baseurl }}/images/AndroidPlugin/AndroidPlugin-androidcode.PNG)

Method **requestToast** will be called from Unity code. It will toast a message. Simple
Method **callbackToUnity** send information from Native code to Unity. In this case, I will send number of Toast method be called - count variable to Unity code.

##2. Export file Jar.
Next step, you need to create a jar file. It will be added to Unity folder (Plugins/Android). 
In Android Studio you can export jar file with bundle.gradle [like this post](http://stackoverflow.com/questions/16763090/how-to-export-library-to-jar-in-android-studio)
If it doesn't work with you, you can try to convert .dex file (/app/build/intermediates/dex/release/classes.dex) to jar file with [this tool](http://code.google.com/p/dex2jar/)  
[_config.yml]({{ site.baseurl }}/images/AndroidPlugin/AndroidPlugin-dex2jar.PNG)

##3. Create Unity Project
At Unity side, you will creata a new project, then put jar file which you just export in android to folder : 
Assests/Plugins/Android
[_config.yml]({{ site.baseurl }}/images/AndroidPlugin/AndroidPlugin-unityfolder.PNG)

Next, you need create AndroidManifest for your Android Game.
[_config.yml]({{ site.baseurl }}/images/AndroidPlugin/AndroidPlugin-manifest.PNG)

Next step is create class AndroidUnityManager. It's interface help you call Toast function in native android code.
[_config.yml]({{ site.baseurl }}/images/AndroidPlugin/AndroidPlugin-unitycode.PNG)

That's is. Now you can build. Final result be showed below
[_config.yml]({{ site.baseurl }}/images/AndroidPlugin/AndroidPlugin-final.jpg)
You click on button, a Toast message will be showed and you will receive information about number you call Toast method from Android native code.

That's all steps when you want to use android native code in Unity. The next guild, I will show you how to download .obb file from your own host. If you have any question or stuck at any step. Just leave your comment here. I will try to help you out.

