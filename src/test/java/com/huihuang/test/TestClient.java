package com.huihuang.test;

import com.huihuang.OrderService;
import com.huihuang.config.OrderNumGenerator;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

public class TestClient {

    @Test
    public void testLock() throws InterruptedException {
        System.out.println("##模拟生成订单号开始...");
        for (int i = 0; i < 100; i++) {
            new Thread(new OrderService()).start();
        }
        Thread.sleep(20000);
        assert OrderNumGenerator.getCount() == 100;
    }


    private static final Map<String, Method> methodMap = new HashMap<String, Method>();

    @Test
    public void testFields1() throws InterruptedException {
        long start = System.currentTimeMillis();
        testField();
        long end = System.currentTimeMillis();
        System.err.println(end - start);
    }
    @Test
    public void testFields2() throws InterruptedException {
        long start = System.currentTimeMillis();
        testField();
        long end = System.currentTimeMillis();
        System.err.println(end - start);
    }
    @Test
    public void testFields3() throws InterruptedException {
        long start = System.currentTimeMillis();
        testField();
        long end = System.currentTimeMillis();
        System.err.println(end - start);
    }

    private void testField() throws InterruptedException {
        final Map<String, User> users = new ConcurrentHashMap<String, User>();
        final List<Integer> integers = new ArrayList<Integer>();
        final CountDownLatch countDownLatch = new CountDownLatch(10000);
        Method[] methods = User.class.getDeclaredMethods();
        for (Method method :methods) {
            methodMap.put(method.getName(), method);
        }
        for (int i = 0; i < 10000; i++) {
            final Map<String, Object> map = new HashMap<String, Object>();
            map.put("id", i+"");
            map.put("a", i+"");
            map.put("b", i+"");
            map.put("c", i+"");
            map.put("d", i+"");
            map.put("e", i+"");
            map.put("f", i+"");
            map.put("g", i+"");
            map.put("h", i+"");
            map.put("i", i+"");
            map.put("j", i+"");
            integers.add(i);
            new Thread(new Runnable() {
                public void run() {
                    try {
                        users.put((String) map.get("id"), map2Object(map));
                        countDownLatch.countDown();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
        countDownLatch.await();
        List<Integer> users1 = new ArrayList<Integer>();
        for (User u : users.values()) {
            users1.add(Integer.valueOf(u.getId()));
        }
        Comparator<Integer> comparator = new Comparator<Integer>() {
            public int compare(Integer o1, Integer o2) {
                return o1.compareTo(o2);
            }
        };
        users1.sort(comparator);
        assert integers.equals(users1);
    }

//    private final Field[] fields = User.class.getDeclaredFields();

    private User map2Object(Map<String, Object> map) throws IllegalAccessException, InterruptedException {
        User user = new User();
        Field[] fields = User.class.getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            field.set(user, map.get(field.getName()));
        }
        return user;
    }

    private User map2ObjectByMethod(Map<String, Object> map) throws InvocationTargetException, IllegalAccessException, InterruptedException {
        User user = new User();
        Thread.sleep(200);
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            Method method = methodMap.get("set" + upperCase(entry.getKey()));
            Object[] args = {entry.getValue()};
            method.invoke(user, args);
        }
        return user;
    }
    private String upperCase(String str) {
        char[] ch = str.toCharArray();
        if (ch[0] >= 'a' && ch[0] <= 'z') {
            ch[0] = (char) (ch[0] - 32);
        }
        return new String(ch);
    }
}
