package jyz.task.framework.task;

public class TaskResult<R> {
	
	/**
	 * 	操作结果
	 */
	private final OperatorType resultType;
	
	/**
	 * 	返回数据
	 */
	private final R resultData;
	
	
	/**
	 *	如果失败，填充失败原因。
	 */
	private final String reason;


	public TaskResult(OperatorType resultType, R resultData, String reason) {
		super();
		this.resultType = resultType;
		this.resultData = resultData;
		this.reason = reason;
	}
	
	public TaskResult(OperatorType resultType, R resultData) {
		super();
		this.resultType = resultType;
		this.resultData = resultData;
		this.reason = "Success";
	}
	
	public OperatorType getResultType() {
		return resultType;
	}

	public String getReason() {
		return reason;
	}

	@Override
	public String toString() {
		return "TaskResult [resultType=" + resultType + ", resultData=" + resultData + ", reason=" + reason + "]";
	}
	
}
