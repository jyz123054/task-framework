package jyz.task.framework.task;

import org.springframework.stereotype.Component;

@Component
public class TaskProcess implements ITaskProcess<Integer, Integer>{
	
	public final static String JOB_NAME = "test";
	
	/**
	 *	执行工作任务的处理器，实际进行处理。
	 */
	@Override
	public TaskResult<Integer> taskExecute(Integer data) {
//		Random ram = new Random();
		
//		int result = ram.nextInt(500);
		
		try {
			Thread.sleep(data.longValue());
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		if (data < 300) {
			return new TaskResult<Integer>(OperatorType.SUCCESS, data);
		} if (data >= 300 && data < 400) {
			return new TaskResult<Integer>(OperatorType.FAILED, data);
		} else {
			return new TaskResult<Integer>(OperatorType.EXCEPTION, data, "发生异常情况！");
		}
	}

}
