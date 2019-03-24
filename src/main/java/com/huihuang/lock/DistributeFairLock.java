package com.huihuang.lock;

import org.I0Itec.zkclient.IZkChildListener;
import org.apache.zookeeper.CreateMode;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * 分布式公平锁
 */
public class DistributeFairLock extends AbstractLock {

    private static final String CHILD_PATH = "fairlock";
    private ThreadLocal<String> local = new ThreadLocal<>();
    @Override
    protected boolean trylock() {
        if (zkClient.exists(PATH)){
            String node = getNodeValue();
            List<String> nodes = zkClient.getChildren(PATH);
            List<Integer> serialNumbers = new ArrayList<>();
            for (String n : nodes) {
                serialNumbers.add(Integer.valueOf(n.replace(CHILD_PATH, "")));
            }
            serialNumbers.sort(Integer::compareTo);
            Integer serialNumber = Integer.valueOf(node.substring(node.indexOf(CHILD_PATH) + CHILD_PATH.length()));
            if (serialNumbers.indexOf(serialNumber) == 0){
                return true;
            }
        }else {
            try {
                zkClient.createPersistent(PATH);
            }catch (Exception e){
            }
            return trylock();
        }
        return false;
    }

    private String getNodeValue(){
        String nodeValue = null;
        if (null == (nodeValue = local.get())){
            nodeValue = zkClient.create(PATH + "/" + CHILD_PATH, Thread.currentThread().getName(), CreateMode.EPHEMERAL_SEQUENTIAL);
            local.set(nodeValue);
        }
        return nodeValue;
    }

    @Override
    protected void await() {
        countDownLatch = new CountDownLatch(1);
        IZkChildListener iZkChildListener = new IZkChildListener() {
            @Override
            public void handleChildChange(String s, List<String> list) throws Exception {
                countDownLatch.countDown();
            }
        };
        if (zkClient.exists(PATH)){
            zkClient.subscribeChildChanges(PATH, iZkChildListener);
        }
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        zkClient.unsubscribeChildChanges(PATH, iZkChildListener);
    }
}
