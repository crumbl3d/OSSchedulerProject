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

		public SJFData(double prediction) {
			this.prediction = prediction;
		}

		public double getPrediction() {
			return prediction;
		}

		public void setPrediction(double prediction) {
			this.prediction = prediction;
		}
		
		public void setStartTime() {
			this.startTime = Pcb.getCurrentTime();
		}
		
		public double getCurrentExecutionTime() {
			return Pcb.getCurrentTime() - startTime;
		}
	}
	
	private class PcbComparator implements Comparator<Pcb> {

		@Override
		public int compare(Pcb firstPcb, Pcb secondPcb) {
			return Double.compare(
					((SJFData) firstPcb.getPcbData()).getPrediction(),
					((SJFData) secondPcb.getPcbData()).getPrediction());
		}
	}
	
	private double alpha;
	private boolean preemption;
	private Queue<Pcb> queue;
	
	public SJFScheduler(double alpha, boolean preemption) {
		if (alpha < 0) alpha = 0;
		if (alpha > 1) alpha = 1;
		this.alpha = alpha;
		this.preemption = preemption;
		PcbComparator pcbComparator = new PcbComparator();
		queue = new PriorityQueue<Pcb>(pcbComparator);
	}

	@Override
	public Pcb get(int cpuId) {
		Pcb nextPcb = queue.poll();
		if (nextPcb == null) return Pcb.IDLE;
		SJFData data = (SJFData) nextPcb.getPcbData();
		data.setStartTime();
		return nextPcb;
	}

	@Override
	public void put(Pcb pcb) {
		ProcessState prevState = pcb.getPreviousState();
		SJFData data = (SJFData) pcb.getPcbData();
		
		long executionTime = pcb.getExecutionTime();
		double prediction = 0;
		
		if (prevState == ProcessState.CREATED) {
			prediction = pcb.getPriority();
			pcb.setPcbData(data = new SJFData(prediction));
		} else {
			prediction = data.getPrediction();
		}

		prediction = alpha * executionTime + (1 - alpha) * prediction;
		data.setPrediction(prediction);
		
		if (preemption) {
			// try preempt a process and start this one
			double maxRemaining = Double.MIN_VALUE;
			for (int i = 0; i < Pcb.RUNNING.length; i++) {
				SJFData tempData = (SJFData) Pcb.RUNNING[i].getPcbData();
				double tempPrediction = tempData.prediction;
			}
		}
		
		queue.offer(pcb);
		pcb.setTimeslice(0);
//		System.out.print("PUT " + priority + ": " + pcb);
    }
}