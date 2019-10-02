package jyz.task.framework.task;

import java.util.Map;
import java.util.concurrent.DelayQueue;

import jyz.task.framework.entity.DelayItem;
import jyz.task.framework.entity.JobInfo;

/**
 * 	检查存放工作任务结果的处理器，过期后会自动清除。
 * @author jyz
 *
 */
public class CheckJobProcess {
	
	private static class SingleModel {
		private static CheckJobProcess singleModel = new CheckJobProcess();
	}
	
	/**
	 * @return CheckJobProcess 延时占位，单例
	 */
	public static CheckJobProcess getInstance() {
		return SingleModel.singleModel;
	}
	
	/**
	 * 	延时队列，存放已处理完成的工作任务，方便后续查询结果。一定时间后会过期清除。
	 */
	private static DelayQueue<DelayItem<String>> delayQueue = new DelayQueue<DelayItem<String>>();
	
	public void putJob(String jobName, long expireTime) {
		DelayItem<String> item = new DelayItem<String>(expireTime, jobName);
		delayQueue.offer(item);
		System.out.println("任务："+jobName+" 已经放入延迟队列中，过期时长："+expireTime+" s.");
	}
	
	private static class FetchJob implements Runnable {
		
		private DelayQueue<DelayItem<String>> queue = CheckJobProcess.delayQueue;
		
		private Map<String, JobInfo<?>> jobMap = TaskJobPool.getJobConcurrentMap();
		
		@Override
		public void run() {
			while(true) {
				try {
					DelayItem<String> itm = queue.take();
					String jobName = itm.getData();
					//任务过期后，从工作任务的ConcurrentHashMap中移除
					jobMap.remove(jobName);
					System.out.println("工作："+jobName+" 已经过期，自动清除...");
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	static {
		Thread checkJob = new Thread(new FetchJob());
		
		checkJob.setDaemon(true);
		
		checkJob.start();
	}
	
}
