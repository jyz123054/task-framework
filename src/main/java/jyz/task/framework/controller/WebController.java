package jyz.task.framework.controller;

import java.util.List;
import java.util.Random;

import javax.annotation.Resource;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import jyz.task.framework.task.TaskJobPool;
import jyz.task.framework.task.TaskProcess;
import jyz.task.framework.task.TaskResult;

@RestController
@RequestMapping("/web")
public class WebController {
	
	@Resource
	private TaskJobPool pool;
	
	@Resource
	private TaskProcess process;
	
	@RequestMapping(value="/home/{length}", method=RequestMethod.GET)
	public String home(@PathVariable("length") int length) {
		pool.registerJob(TaskProcess.JOB_NAME, length, process, 100);
		
		Random ram = new Random();
		
		for(int i=0; i<length; i++) {
			pool.putTask(TaskProcess.JOB_NAME, ram.nextInt(500));
		}
		
		return "Success";
	}
	
	@RequestMapping(value="/home/url", method=RequestMethod.GET)
	public String url(String jobName, int length, long expireTime) {
		pool.registerJob(jobName, length, process, expireTime);
		
		Random ram = new Random();
		
		for(int i=0; i<length; i++) {
			pool.putTask(jobName, ram.nextInt(500));
		}
		
		return "Success";
	}
	
	/**
	 * 	查询工作进度。
	 * @param jobName 查询的工作名称。
	 * @return 完成的进度，例如：30/100，100个中完成了30个。
	 */
	@RequestMapping(value="/home/progress/{jobName}", method=RequestMethod.GET)
	public String progressByJobName(@PathVariable("jobName") String jobName) {
		return pool.getJobProgress(jobName);
	}
	
	/**
	 * 	查询工作执行结果。
	 * @return
	 */
	@RequestMapping(value="/home/result/{jobName}", method=RequestMethod.GET)
	public String resultByJobName(@PathVariable("jobName") String jobName) {
		 List<TaskResult<String>> list = pool.getJobResult(jobName);
		return list.toString();
	}
}
