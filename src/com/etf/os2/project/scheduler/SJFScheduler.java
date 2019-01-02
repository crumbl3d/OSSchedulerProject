package com.etf.os2.project.scheduler;

import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Queue;

import com.etf.os2.project.process.Pcb;
import com.etf.os2.project.process.PcbData;
import com.etf.os2.project.process.Pcb.ProcessState;

public class SJFScheduler extends Scheduler {

	private class SJFData extends PcbData {

		private double prediction;
		private double startTime;

		SJFData(double prediction) {
			this.prediction = prediction;
		}

		double getPrediction() {
			return prediction;
		}

		void setPrediction(double prediction) {
			this.prediction = prediction;
		}
		
		void setStartTime() {
			this.startTime = Pcb.getCurrentTime();
		}
		
		double getCurrentExecutionTime() {
			return Pcb.getCurrentTime() - startTime;
		}
	}
	
	private double alpha;
	private boolean preemption;
	private Queue<Pcb> queue;
	
	SJFScheduler(double alpha, boolean preemption) {
		if (alpha < 0) alpha = 0;
		if (alpha > 1) alpha = 1;
		this.alpha = alpha;
		this.preemption = preemption;
		queue = new PriorityQueue<>(Comparator.comparingDouble(x -> ((SJFData) x.getPcbData()).getPrediction()));
	}

	@Override
	public Pcb get(int cpuId) {
		Pcb nextPcb = queue.poll();
		double prediction = 0;
        if (nextPcb == null) {
            nextPcb = Pcb.IDLE;
        } else {
            SJFData data = (SJFData) nextPcb.getPcbData();
            data.setStartTime();
            prediction = data.prediction;
        }
        System.out.println("GET CPU" + cpuId + " prediction = " + prediction + ": " + nextPcb.getId());
		return nextPcb;
	}

	@Override
	public void put(Pcb pcb) {
		ProcessState prevState = pcb.getPreviousState();
		SJFData data = (SJFData) pcb.getPcbData();
		
		long executionTime = pcb.getExecutionTime();
		double prediction = 10 * pcb.getPriority();
		
		if (prevState == ProcessState.CREATED) {
			pcb.setPcbData(data = new SJFData(prediction));
		} else {
			prediction = data.getPrediction();
		}

		prediction = alpha * executionTime + (1 - alpha) * prediction;
		data.setPrediction(prediction);
		
		queue.offer(pcb);
		pcb.setTimeslice(0);

        System.out.println("PUT prediction = " + prediction + ": " + pcb.getId());
		
        if (preemption) {
            double maxRemaining = Double.MIN_VALUE;
            int preemptCpu = -1;
            for (int i = 0; i < Pcb.RUNNING.length; i++) {
                if (Pcb.RUNNING[i] == Pcb.IDLE) continue;
                SJFData tempData = (SJFData) Pcb.RUNNING[i].getPcbData();
                double timeLeft = tempData.prediction - tempData.getCurrentExecutionTime();
                if (timeLeft > maxRemaining) {
                    maxRemaining = timeLeft;
                    preemptCpu = i;
                }
            }
            if (preemptCpu > -1) {
                Pcb.RUNNING[preemptCpu].preempt();
                System.out.println("Preempting CPU" + preemptCpu + "!");
            }
        }
    }
}