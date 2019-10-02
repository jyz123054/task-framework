package jyz.task.framework.entity;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

public class DelayItem<T> implements Delayed{

	private long activeTime;
	
	private T data;
	
	/**
	 * @param activeTime	过期时长，单位：秒
	 * @param data	业务数据
	 */
	public DelayItem(long second, T data) {
		super();
		this.activeTime = second*1000 + System.currentTimeMillis();
		this.data = data;
	}
	
	/**
	 *	Delayed接口继承了Comparable接口，按剩余时间排序，实际计算考虑精度为纳秒数
	 */
	@Override
	public int compareTo(Delayed o) {
		long d = (getDelay(TimeUnit.MILLISECONDS) - o.getDelay(TimeUnit.MILLISECONDS));
		if (d == 0){
			return 0;
		}else{
			if (d < 0){
				return -1;
			}else{
				return  1;
			}
		}
	}

	/**
	 *	这个方法返回到激活日期的剩余时间，时间单位由单位参数指定。
	 */
	@Override
	public long getDelay(TimeUnit unit) {
		return unit.convert(activeTime-System.currentTimeMillis(), unit);
	}

	public T getData() {
		return data;
	}

}
