package com.huihuang;

import com.huihuang.config.OrderNumGenerator;
import com.huihuang.lock.DistributeFairLock;
import com.huihuang.lock.DistributedLock;
import com.huihuang.lock.Lock;

// 订单生成调用业务逻辑
public class OrderService implements Runnable {
	// 生成订单号
	OrderNumGenerator orderNumGenerator = new OrderNumGenerator();
//	private Lock lock = new DistributedLock();
	private Lock lock = new DistributeFairLock();

	public void run() {
		try {
			// 上锁
			lock.lock();
			// synchronized (this) {
			// 模拟用户生成订单号
			getNumber();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			// 釋放鎖資源
			lock.unlock();
		}
	}

	public void getNumber() {
		String number = orderNumGenerator.getNumber();
		System.out.println(Thread.currentThread().getName() + ",##number:" + number);
	}
}
