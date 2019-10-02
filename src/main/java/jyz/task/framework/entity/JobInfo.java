package jyz.task.framework.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicInteger;

import jyz.task.framework.task.CheckJobProcess;
import jyz.task.framework.task.ITaskProcess;
import jyz.task.framework.task.OperatorType;
import jyz.task.framework.task.TaskResult;

/**
 *	工作任务的封装实体
 * @author jyz
 *
 * @param <R> 泛型，工作类型自定义
 */
public class JobInfo<R> {
	
	/**
	 * 	工作名称
	 */
	private final String jobName;
	
	/**
	 * 	工作需要处理的数量
	 */
	private final int length;
	
	/**
	 * 	工作执行的处理器
	 */
	private final ITaskProcess<?, ?> process;
	
	/**
	 * 	整个工作任务，执行成功数量
	 */
	private AtomicInteger successInteger;
	
	/**
	 * 	整个工作任务，已执行总数量
	 */
	private AtomicInteger totalInteger;
	
	/**
	 * 	工作任务-所有结果保存在队列中
	 */
	private LinkedBlockingDeque<TaskResult<R>> resultDueue;
	
	/**
	 * 	工作任务-所有结果保存在队列中
	 */
	private List<TaskResult<R>> resultList;
	
	/**
	 * 	过期时长，单位：秒
	 */
	private final long expireTime;//保留的工作的结果信息供查询的时间
	
	/**
	 * 	定时清除过期任务的处理器
	 */
	private CheckJobProcess checkProcess = CheckJobProcess.getInstance();

	public JobInfo(String jobName, int length, ITaskProcess<?, ?> process, long expireTime) {
		super();
		this.jobName = jobName;
		this.length = length;
		this.process = process;
		this.successInteger = new AtomicInteger(0);
		this.totalInteger = new AtomicInteger(0);
		this.expireTime = expireTime;
		this.resultDueue = new LinkedBlockingDeque<TaskResult<R>>(length);
		this.resultList = new ArrayList<TaskResult<R>>(); 
	}

	public int getSuccessInteger() {
		return successInteger.get();
	}

	public int getTotalInteger() {
		return totalInteger.get();
	}
	
	public int getFailInteger() {
		return totalInteger.get() - successInteger.get();
	}
	
	public ITaskProcess<?, ?> getProcess() {
		return process;
	}
	
	/**
	 *	查询所有的工作任务执行结果集
	 * @return
	 */
	public List<TaskResult<R>> getResultDueue(){
		List<TaskResult<R>> list = new ArrayList<TaskResult<R>>();
		
		TaskResult<R> result;
		
		while((result = resultDueue.pollFirst()) != null) {
			list.add(result);
		}
		
		return list;
	}
	
	/**
	 *	查询所有的工作任务执行结果集
	 * @return
	 */
	public List<TaskResult<R>> getAllResultDueue(){
		return resultList;
	}
	
	/**
	 * 	将工作任务的执行结果放入结果队列中保存
	 * @param result
	 */
	public void addEachResult(TaskResult<R> result) {
		if (OperatorType.SUCCESS.equals(result.getResultType())) {
			successInteger.incrementAndGet();
		}
//		else {
//			resultQueue.addLast(result);
//		}
		
		totalInteger.incrementAndGet();
		
		resultDueue.addLast(result);
		
		resultList.add(result);
		
		//当本次工作全部处理完成时，将整个结果集推送到延迟队列，可在一定时间内查询到结果
		if (totalInteger.get() == length) {
			checkProcess.putJob(jobName, expireTime);
		}
	}

	public int getLength() {
		return length;
	}

	public String getProgress() {
		return "JobInfo [length=" + length + ", successInteger=" + successInteger +", failInteger="+getFailInteger()
				+ ", totalInteger=" + totalInteger + ", expireTime=" + expireTime + "]";
	}
	
}
