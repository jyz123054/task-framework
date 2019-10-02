package jyz.task.framework.task;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Service;

import jyz.task.framework.entity.JobInfo;

/**
 * 	工作执行池
 * @author jyz
 *
 */
@Service
public class TaskJobPool {
	
	//按系统CPU核心数指定线程数
	private static final int THREAD_COUNT = Runtime.getRuntime().availableProcessors();
	
	//队列，线程池执行任务的队列，最大5000个任务
	private static BlockingQueue<Runnable> taskQueue = new ArrayBlockingQueue<Runnable>(5000);
	
	//线程池，用于执行工作任务
	private static ExecutorService taskExecutor = 
			new ThreadPoolExecutor(THREAD_COUNT, THREAD_COUNT, 60, TimeUnit.SECONDS, taskQueue);
	
	//存放所有执行的工作
	private static ConcurrentHashMap<String, JobInfo<?>> jobConcurrentMap = new ConcurrentHashMap<String, JobInfo<?>>();

	public static Map<String, JobInfo<?>> getJobConcurrentMap() {
		return jobConcurrentMap;
	}
	
	/**
	 * 	每个任务的执行器
	 * @author jyz
	 *
	 * @param <T> 处理数据类型
	 * @param <R> 返回数据类型
	 */
	private static class TaskJobRun<T,R> implements Runnable {
		
		/**
		 *	工作任务
		 */
		private JobInfo<R> job;
		
		/**
		 * 	任务需处理的数据
		 */
		private T processData;
		
		public TaskJobRun(JobInfo<R> job, T processData) {
			super();
			this.job = job;
			this.processData = processData;
		}

		@Override
		public void run() {
			R r = null;
			//取得本次工作的处理器
			ITaskProcess<T, R> process = (ITaskProcess<T, R>) job.getProcess();
			
			TaskResult<R> result = null;
			
			try {
				//处理器执行任务，返回结果
				result = process.taskExecute(processData);
				
				//对返回结果进行稳定性检查，因为有可能处理器没有按规范填充异常结果
				if (result == null) {
					result = new TaskResult<R>(OperatorType.EXCEPTION, r, "result is null.");
				}
				
				if (result.getResultType() == null) {
					if (result.getReason() == null) {
						result = new TaskResult<R>(OperatorType.EXCEPTION, r, "result is null.");
					} else {
						result = new TaskResult<R>(OperatorType.EXCEPTION, r, "result is null. Reason is "+result.getReason());
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				result = new TaskResult<R>(OperatorType.EXCEPTION, r, e.getMessage());
			} finally {
				job.addEachResult(result);
			}
		}
		
	}
	
	/**
	 * @param <R>
	 * @param jobName 工作名称
	 * @param length 需要执行任务次数
	 * @param process 执行任务的处理器
	 * @param expireTime 结果过期时长，单位：秒
	 */
	public <R> void registerJob(String jobName, int length, ITaskProcess<?, ?> process, long expireTime) {
		JobInfo<R> job = new JobInfo<R>(jobName, length, process, expireTime);
		
		if(jobConcurrentMap.putIfAbsent(jobName, job) != null) {
			throw new RuntimeException(jobName+" 已经注册，请勿重复提交！"); 
		}
	}
	
	/**
	 * 	推送任务
	 * @param <T>
	 * @param <R>
	 * @param jobName 工作名称
	 * @param data	需要处理的数据
	 */
	public <T,R> void putTask(String jobName, T data) {
		JobInfo<R> job = getJobInfo(jobName);
		TaskJobRun<T, R> task = new TaskJobRun<>(job, data);
		taskExecutor.execute(task);
	}
	
	/**
	 * @param <R>
	 * @param jobName
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <R> JobInfo<R> getJobInfo(String jobName) {
		JobInfo<R> job = (JobInfo<R>) jobConcurrentMap.get(jobName);
		if (job == null) {
			throw new RuntimeException(jobName+" 任务不存在或者任务已过期自动清除了。");
		}
		return job;
	}
	
	/**
	 * @param jobName 查询的工作名称
	 * @return
	 */
	public <R> String getJobProgress(String jobName) {
		JobInfo<R> job = getJobInfo(jobName);
		if (job == null) {
			throw new RuntimeException(jobName+" 任务不存在或者任务已过期自动清除了。");
		}
//		return job.getTotalInteger()+"/"+job.getLength();
		return job.getProgress();
	}
	
	public <R> List<TaskResult<R>> getJobResult(String jobName) {
		JobInfo<R> job = getJobInfo(jobName);
		if (job == null) {
			throw new RuntimeException(jobName+" 任务不存在或者任务已过期自动清除了。");
		}
//		List<TaskResult<R>> list = job.getResultDueue();
		List<TaskResult<R>> list = job.getAllResultDueue();
		return list;
	}
}
