package com.dongnao;

import java.lang.reflect.Field;

public class App {
	static App app;
	public static void main(String[] args) {
		//系统实例化了
		ActivityManagerNative activityManagerNative=new ActivityManagerNative();
		app=new App();
		
		
		//我们调用
		try {
			//还原
			Class activityManagerNativeClass=Class.forName("com.dongnao.ActivityManagerNative");
			Field sigletoneField=activityManagerNativeClass.getDeclaredField("sigletone");
			sigletoneField.setAccessible(true);
			Object sigletoneObj=sigletoneField.get(null);// 就是这里可以传null，就可以获取到对象
			Sigletone sigletone=(Sigletone) sigletoneObj;
			System.out.println("---   "+sigletone.name);
		
			//替换
			Sigletone sigletone2=new Sigletone();
			sigletone2.setName("lance");
			sigletoneField.set(activityManagerNative,sigletone2);
			
			
			
			//系统自己调用
			System.out.println("-->"+ActivityManagerNative.sigletone.name);
			
		} catch ( Exception e) {
			e.printStackTrace();
		}
		
	}
}
