package com.example.baby.hookframework;

import com.dongnao.App;

import org.junit.Test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void testApp() throws Exception {

        App.main(null);
    }


    @Test
    public void testNewProxyInstance() throws Exception {
//        ITest test = new MyTest();
//        ITest obj = (ITest) Proxy.newProxyInstance(test.getClass().getClassLoader()
//                        , new Class[]{ITest.class}
//                        , new MyInvocationHandler(test));


        ITest obj = (ITest) Proxy.newProxyInstance(ITest.class.getClassLoader()
                , new Class[]{ITest.class}
                , new MyInvocationHandler(null));

        obj.test();

    }

    interface ITest {

        void test();
    }

    class MyTest implements ITest {
        @Override
        public void test() {
            System.out.print("###33");
        }
    }

    class MyInvocationHandler implements InvocationHandler {
        private ITest real;

        public MyInvocationHandler(ITest real) {
            this.real = real;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) {
            System.out.print("invoke proxy "+method.getName());
            if (real != null) {
                real.test();
            }
            return null;
        }
    }

}