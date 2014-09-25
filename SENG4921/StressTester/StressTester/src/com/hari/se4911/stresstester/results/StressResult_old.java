package com.hari.se4911.stresstester.results;

import java.util.ArrayList;

public class StressResult_old {

	private float[] avgCountTurns;
	private float avgHydro;
	private float[] avgVoice;
	private boolean isStressed;
	
	private int[] accelRes;
	private ArrayList<float[]> hydroRes;
	private ArrayList<Short> voiceRes;
	
	public StressResult_old() {
		initResults();
	}

	public StressResult_old(int[] accelRes,
			ArrayList<float[]> hydroRes, 
			ArrayList<Short> voiceRes) {
		this.accelRes = accelRes;
		this.hydroRes = hydroRes;
		this.voiceRes = voiceRes;
		initResults();
	}
	
	private void initResults() {
		avgCountTurns = new float[2];
		avgHydro = 0;
		avgVoice = new float[2];
		isStressed = false;
	}
	
	public void analyze() throws NoResultsException {
		analyzeAccel();
		analyzeHydro();
		analyzeVoice();
		
		boolean isStressedAccel = isStressedAccel();
		boolean isStressedHydro = isStressedHydro();
		boolean[] isStressedVoice = isStressedVoice();
		
		boolean test1 = isStressedAccel && (isStressedHydro 
				|| isStressedVoice[1]);
		boolean test2 = isStressedVoice[0] && (isStressedHydro 
				|| isStressedAccel);
		boolean test3 = isStressedVoice[1];
		boolean test4 = isStressedHydro && isStressedVoice[0] && isStressedAccel;
		
		this.setStressed(test1 || test2 || test3 || test4);
	}

	
	private boolean isStressedAccel() {
		return avgCountTurns[0] > 2 && avgCountTurns[1] > 2;
	}

	private boolean isStressedHydro() {
		return avgHydro > 60;
	}

	private boolean[] isStressedVoice() {
		boolean[] ans = new boolean[2];
		ans[0] = avgVoice[0] > 0.75 && avgVoice[1] >= 75;
		ans[1] = avgVoice[0] > 0.75 && avgVoice[1] >= 120;
		return ans;
	}

	private void analyzeAccel() throws NoResultsException {
		int[] toComp = {0, 0, 0};
		if (accelRes == toComp) throw new NoResultsException();
		else {
			avgCountTurns[0] = accelRes[0]/accelRes[2];
			avgCountTurns[1] = accelRes[1]/accelRes[2];
		}
		
	}

	private void analyzeHydro() throws NoResultsException {
		if (hydroRes == null) throw new NoResultsException();
		else {
			int sum = 0;
			int total = hydroRes.size();
			for (float[] h: hydroRes) {
				sum += h[0];
			}
			avgHydro = sum/total;	
		}
		
	}

	private void analyzeVoice() throws NoResultsException {
		if (voiceRes == null) throw new NoResultsException();
		else if (voiceRes.equals(new ArrayList<Short>())) {
			avgVoice[0] = -1;
			avgVoice[1] = -1;
		}
		else {
			int countShout = 0;
			int sum = 0;
			int total = voiceRes.size();
			for (Short v: voiceRes) {
				if (Math.abs(v) >= 75) {
					countShout++;
				}
				sum += Math.abs(v);
			}
			avgVoice[0] = countShout/total;
			avgVoice[1] = sum/total;
		}
		
	}
	
	/*
	 * *********************GETTERS AND SETTERS**********************
	 */

	public float[] getAvgCountTurns() {
		return avgCountTurns;
	}

	public void setAvgCountTurns(float[] avgCountTurns) {
		this.avgCountTurns = avgCountTurns;
	}

	public float getAvgHydro() {
		return avgHydro;
	}

	public void setAvgHydro(float avgHydro) {
		this.avgHydro = avgHydro;
	}

	public float[] getAvgVoice() {
		return avgVoice;
	}

	public void setAvgVoice(float[] avgVoice) {
		this.avgVoice = avgVoice;
	}

	public boolean isStressed() {
		return isStressed;
	}

	public void setStressed(boolean isStressed) {
		this.isStressed = isStressed;
	}
	
	/*
	 * ***********************FOR DataAnalyzer***********************
	 */
	
	public void setAccel(String xAvg, String yAvg) 
			throws NumberFormatException {
		float[] avgs = new float[2];
		avgs[0] = Float.parseFloat(xAvg);
		avgs[1] = Float.parseFloat(yAvg);
		setAvgCountTurns(avgs);
		
	}

	public void setHydro(String next) throws NumberFormatException {
		setAvgHydro(Float.parseFloat(next));
		
	}

	public void setVoice(String fracShout, String avgAmp) 
			throws NumberFormatException {
		float[] data = new float[2];
		data[0] = Float.parseFloat(fracShout);
		data[1] = Float.parseFloat(avgAmp);
		setAvgVoice(data);
		
	}

}
