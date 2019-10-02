package jyz.task.framework.task;

public interface ITaskProcess<T,R> {
	public TaskResult<R> taskExecute(T data);
}
