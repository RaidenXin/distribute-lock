package com.huihuang.lock;

import org.I0Itec.zkclient.IZkDataListener;
import org.apache.zookeeper.CreateMode;

import java.util.concurrent.CountDownLatch;

/**
 * 分布式锁(非公平锁)
 */
public class DistributedLock extends AbstractLock{

    //获取锁
    protected boolean trylock() {
        try {
            //创建节点 如果成功就是成功获得锁 如果失败报错就说明没有获得锁 返回false
            zkClient.createEphemeral(PATH);
            return true;
        }catch (Exception e){
            return false;
        }
    }

    //休眠
    protected void await() {
        //创建监听事件
        IZkDataListener zkDataListener = new IZkDataListener() {
            public void handleDataChange(String s, Object o) throws Exception {
            }

            public void handleDataDeleted(String s) throws Exception {
                countDownLatch.countDown();
            }
        };
        //注册监听事件
        zkClient.subscribeDataChanges(PATH, zkDataListener);
        if (zkClient.exists(PATH)){
            countDownLatch = new CountDownLatch(1);
            try {
                countDownLatch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        //被唤醒后删除监听事件
        zkClient.unsubscribeDataChanges(PATH, zkDataListener);
    }
}
