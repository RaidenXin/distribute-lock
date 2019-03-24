package com.huihuang.lock;

import com.huihuang.config.Configration;
import org.I0Itec.zkclient.IZkDataListener;
import org.I0Itec.zkclient.ZkClient;

import java.util.concurrent.CountDownLatch;

/**
 * 抽象锁
 */
public abstract class AbstractLock implements Lock{

    protected static final String PATH = "/lock";
    protected ZkClient zkClient = new ZkClient(Configration.ADDRES);
    protected CountDownLatch countDownLatch;

    /**
     * 获取锁
     * @return
     */
    public void lock() {
        if (trylock()){
        }else {
            await();
            lock();
        }
    }

    /**
     * 获取锁
     * @return
     */
    protected abstract boolean trylock();

    /**
     * 休眠
     */
    protected abstract void await();


    /**
     * 释放锁
     */
    public void unlock() {
        try {
            zkClient.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
