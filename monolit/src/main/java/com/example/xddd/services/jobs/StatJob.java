package com.example.xddd.services.jobs;

import com.example.xddd.services.ItemsService;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class StatJob implements Job {

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        System.out.println(ItemsService.getSuccessfulOrders());
        ItemsService.setSuccessfulOrders(0);
    }
}
