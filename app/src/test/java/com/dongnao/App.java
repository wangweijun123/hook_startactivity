package com.dongnao;

import java.lang.reflect.Field;

public class App {
	static App app;
	public static void main(String[] args) {
		//ϵͳʵ������
		ActivityManagerNative activityManagerNative=new ActivityManagerNative();
		app=new App();
		
		
		//���ǵ���
		try {
			//��ԭ
			Class activityManagerNativeClass=Class.forName("com.dongnao.ActivityManagerNative");
			Field sigletoneField=activityManagerNativeClass.getDeclaredField("sigletone");
			sigletoneField.setAccessible(true);
			Object sigletoneObj=sigletoneField.get(null);// ����������Դ�null���Ϳ��Ի�ȡ������
			Sigletone sigletone=(Sigletone) sigletoneObj;
			System.out.println("---   "+sigletone.name);
		
			//�滻
			Sigletone sigletone2=new Sigletone();
			sigletone2.setName("lance");
			sigletoneField.set(activityManagerNative,sigletone2);
			
			
			
			//ϵͳ�Լ�����
			System.out.println("-->"+ActivityManagerNative.sigletone.name);
			
		} catch ( Exception e) {
			e.printStackTrace();
		}
		
	}
}
