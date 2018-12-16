package com.example.wangweijun.hookbyme;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class HookUtil {


    private static final String TAG = "HookUtil";

    private Context context;


    public  void hookHookMh(Context context) {
        try {
            Class<?> forName = Class.forName("android.app.ActivityThread");
            Field currentActivityThreadField = forName.getDeclaredField("sCurrentActivityThread");
            currentActivityThreadField.setAccessible(true);
//            还原系统的ActivityTread   mH
            Object activityThreadObj=currentActivityThreadField.get(null);

            Field handlerField = forName.getDeclaredField("mH");
            handlerField.setAccessible(true);
//            hook点找到了
//            Handler mH= (Handler) handlerField.get(activityThreadObj);
//            Field callbackField = Handler.class.getDeclaredField("mCallback");
//            callbackField.setAccessible(true);
//            callbackField.set(mH,new ActivityMH(mH));

            Object handlerObj = handlerField.get(activityThreadObj);
            Class<?> handlerClass = Class.forName("android.os.Handler");
            Field callbackField = handlerClass.getDeclaredField("mCallback");
            callbackField.setAccessible(true);
            callbackField.set(handlerObj,new ActivityMH((Handler) handlerObj));
        } catch (Exception e) {
            e.printStackTrace();
        }


        }

    class ActivityMH implements  Handler.Callback{
        private  Handler mH;

        public ActivityMH(Handler mH) {
            this.mH = mH;
        }

        @Override
        public boolean handleMessage(Message msg) {
//LAUNCH_ACTIVITY ==100 即将要加载一个activity了
            if (msg.what == 100) {
//加工 --完  一定丢给系统  secondActivity  -hook->proxyActivity---hook->    secondeActivtiy
//                handleLuachActivity(msg);
            }
//做了真正的跳转
            mH.handleMessage(msg);
            return  true;
        }

        private void handleLuachActivity(Message msg) {
//            还原
            Object obj = msg.obj;
            try {
                Field intentField=obj.getClass().getDeclaredField("intent");
                intentField.setAccessible(true);
                //  ProxyActivity   2
                Intent realyIntent = (Intent) intentField.get(obj);
//                sconedActivity  1
                Intent oldIntent = realyIntent.getParcelableExtra("oldIntent");
                if (oldIntent != null) {
//                    集中式登录
                    SharedPreferences share = context.getSharedPreferences("david",
                            Context.MODE_PRIVATE);
                    if (share.getBoolean("login",false)||oldIntent.getComponent().getClassName().equals(SceondActivity.class.getName())) {

//                      登录  还原  把原有的意图    放到realyIntent
                        realyIntent.setComponent(oldIntent.getComponent());
                    }else {
                        ComponentName componentName = new ComponentName(context,LoginActivity.class);
                        realyIntent.putExtra("extraIntent", oldIntent.getComponent()
                                .getClassName());
                        realyIntent.setComponent(componentName);
                    }
                }


            } catch (Exception e) {
                e.printStackTrace();
            }


        }
    }

    /**
     * 目的是使用动态代理替换 android.app.IActivityManager
     *
     *
     * int result = ActivityManagerNative.getDefault()
     *                 .startActivity(whoThread, who.getBasePackageName(), intent,
     *
     * @param context
     */
    public void hookStartActivity(Context context) {
        this.context = context;

        try {
            Class activityManagerNativeClass = Class.forName("android.app.ActivityManagerNative");
            //  android.util.Singleton
            Field gDefaultField = activityManagerNativeClass.getDeclaredField("gDefault");
            gDefaultField.setAccessible(true);
            Object defaltValue = gDefaultField.get(null);

            Class singletonClass = Class.forName("android.util.Singleton");
            Field mInstanceField = singletonClass.getDeclaredField("mInstance");
            mInstanceField.setAccessible(true);

            // 原来系统的 IActivityManager
            Object iActivityManagerObj = mInstanceField.get(defaltValue);
            Log.i(TAG, "iActivityManagerObj:"+iActivityManagerObj);

//            android.app.IActivityManager

            Object object = Proxy.newProxyInstance(iActivityManagerObj.getClass().getClassLoader(),
                    new Class[]{Class.forName("android.app.IActivityManager")},
                    new startActivty(iActivityManagerObj));

            mInstanceField.set(defaltValue, object);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    class startActivty implements InvocationHandler {
        private  Object iActivityManagerObject;

        public startActivty(Object iActivityManagerObject) {
            this.iActivityManagerObject = iActivityManagerObject;
        }
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            Log.i(TAG,"invoke    "+method.getName());
            if ("startActivity".equals(method.getName())) {
                Log.i("INFO", "-----------------startActivity--------------------------");
                //                寻找传进来的intent
                Intent intent = null;
                int index=0;
                for (int i=0;i<args.length;i++) {
//                    intent
                    Object arg = args[i];
                    if (arg instanceof Intent) {
                        intent = (Intent) args[i];
                        index = i;
                    }
                }
//目的  ---载入acgtivity  将它还原
                Intent newIntent = new Intent();
                ComponentName componentName = new ComponentName(context, ProxyActivity.class);
                newIntent.setComponent(componentName);
//                真实的意图 被我隐藏到了  键值对
                newIntent.putExtra("oldIntent", intent);
                args[index] = newIntent;
            }
            return method.invoke(iActivityManagerObject, args);
        }
    }

}
