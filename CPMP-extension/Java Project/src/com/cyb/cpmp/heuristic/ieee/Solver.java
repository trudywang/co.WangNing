package com.cyb.cpmp.heuristic.ieee;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.management.RuntimeErrorException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jinbostar.cpmp.common.Operation;
import com.jinbostar.cpmp.scheme.fbh.Node;

import data.io.DataIO;

public class Solver extends Node implements Cloneable {
	static Logger log = LoggerFactory.getLogger(Solver.class);
	public int LARGEST_PRIORITY = 1;
	public int IEEE_METHOD = 2;

	public int W_METHOD = 1;
	public int W_HAT_METHOD = 2;

	public int RM_LOWEST_TIER = 1;
	public int RM_LOWEST__PRIORITY_INDEX = 2;
	public int RM_MIN_MAX = 3;

	public int FILL_NONE = 1;
	public int FILL_STANDARD = 2;
	public int FILL_SAFE = 3;
	public int FILL_STOP = 4;

	int wMethod;
	int relocateMethod;
	int fillingMethod;

	int f[][][];
	int g[][];
	int w[][][];
	int _f[][][];
	int fr[][][];
	int _w[][][];
	int nwl[];
	int nw[][][];
	int _h[][];
	int dest[][];
	List<Operation> solutions;

	Random random = new Random();

	public Solver(int S, int H, int P, int N, int[][] bay) throws Exception {
		super(S, H, P, N, bay);
		// TODO Auto-generated constructor stub
		f = new int[S + 1][H + 1][S + 1];
		g = new int[S + 1][H + 1];
		w = new int[S + 1][H + 1][S + 1];
		_f = new int[S + 1][H + 1][S + 1];
		fr = new int[S + 1][H + 1][S + 1];
		_w = new int[S + 1][H + 1][S + 1];
		nwl = new int[S + 1];
		nw = new int[S + 1][H + 1][S + 1];
		_h = new int[S + 1][H + 1];
		dest = new int[S + 1][H + 1];
		updateCalculations();
	}

	private void updateHigh() {
		for (int s = 1; s <= S; s++) {
			h[s] = 0;
			while (h[s] != H && bay[s][h[s] + 1] != 0)
				h[s]++;
		}
	}

	private void updateOrderly() {
		for (int s = 1; s <= S; s++) {
			orderly[s] = 0;
			while (orderly[s] != h[s]
					&& bay[s][orderly[s]] >= bay[s][orderly[s] + 1])
				orderly[s]++;
		}
	}

	private void updateG() {
		for (int s = 1; s <= S; s++) {
			for (int j = orderly[s] + 1; j <= h[s]; j++) {
				g[s][j] = h[s] - j;
			}
		}
	}

	private void updateF() {
		for (int s = 1; s <= S; s++) {
			for (int j = orderly[s] + 1; j <= h[s]; j++) {
				for (int d = 1; d <= S; d++) {
					for (int k = orderly[d]; k >= 0; k--) {
						if (bay[d][k] >= bay[s][j]) {
							f[s][j][d] = h[d] - k;
							break;
						}
					}
				}
			}
		}
	}

	private void updateW() {
		for (int s = 1; s <= S; s++) {
			for (int j = orderly[s] + 1; j <= h[s]; j++) {
				for (int d = 1; d <= S; d++) {
					if (d != s) {
						w[s][j][d] = f[s][j][d] + g[s][j] + 1;
					} else {
						w[s][j][d] = f[s][j][d] + 1;
					}
				}
			}
		}
	}
	
	public void showWFG() {
		for (int s = 1; s <= S; s++) {
			for (int j = orderly[s] + 1; j <= h[s]; j++) {
				for (int d = 1; d <= S; d++) {
					log.info("w["+s+"]["+j+"]["+d+"]="+w[s][j][d] + ", f["+s+"]["+j+"]["+d+"]="+f[s][j][d]+ ", g["+s+"]["+j+"]="+g[s][j]);
				}
			}
		}
	}

	private int getMinW(int s, int t) {
		int minW = Integer.MAX_VALUE;
		int des = 1;
		for (int d = 1; d <= S; d++) {
			if (W(s,t,d) < minW && !(orderly[d]==H && bay[d][H]>=bay[s][t])) {
				minW = W(s,t,d);
				des = d;
			}
		}
		dest[s][t] = des;
		return des;
	}

	private int FR(int s, int t, int d, int k) {
		for (int i = 1; i <= S; i++) {
			if (i == s && i == d) {
				continue;
			}
			if (orderly[i] == h[i]) {
				return 0;
			}
			if (bay[i][h[i]] >= bay[s][k]) {
				return 0;
			}
		}
		return 1;
	}

	private void updateFR() {
		for (int s = 1; s <= S; s++) {
			for (int j = orderly[s] + 1; j <= h[s]; j++) {
				for (int d = 1; d <= S; d++) {
					fr[s][j][d] = 0;
					if (d != s) {
						for (int k = j + 1; k <= h[s]; k++) {
							fr[s][j][d] += FR(s, j, d, k);
						}
					} else {
						for (int k = h[s] - f[s][j][s] + 1; k <= h[s]; k++) {
							if (k != j) {
								fr[s][j][d] += FR(s, j, d, k);
							}
						}
					}
				}
			}
		}
	}

	private int W(int s, int t, int d) {
		if (wMethod == W_METHOD) {
			return w[s][t][d];
		} else {
			return _w[s][t][d];
		}
	}

	private void update_H() {
		for (int s = 1; s <= S; s++) {
			for (int j = orderly[s] + 1; j <= h[s]; j++) {
				int d = getMinW(s, j);
				_h[s][j] = -bay[s][j] + W(s, j, d) + fr[s][j][d];
			}
		}
	}

	private void updateNWL() {
		for (int s = 1; s <= S; s++) {
			nwl[s] = h[s] - orderly[s];
		}
	}

	private void updateNW() {
		for (int s = 1; s <= S; s++) {
			for (int j = orderly[s] + 1; j <= h[s]; j++) {
				for (int d = 1; d <= S; d++) {
					nw[s][j][d] = f[s][j][d] - nwl[d];
				}
			}
		}
	}

	private void update_F() {
		for (int s = 1; s <= S; s++) {
			for (int j = orderly[s] + 1; j <= h[s]; j++) {
				for (int d = 1; d <= S; d++) {
					_f[s][j][d] = f[s][j][d] + nw[s][j][d];
				}
			}
		}
	}

	private void update_W() {
		for (int s = 1; s <= S; s++) {
			for (int j = orderly[s] + 1; j <= h[s]; j++) {
				for (int d = 1; d <= S; d++) {
					if (d != s) {
						_w[s][j][d] = _f[s][j][d] + g[s][j] + 1;
					} else {
						_w[s][j][d] = _f[s][j][d] + 1;
					}
				}
			}
		}
	}

	private Point calculateMaxPriority() {
		Point maxPoint = new Point(0, 0);
		int maxPriorityValue = 0;
		for (int s = 1; s <= S; s++) {
			for (int j = orderly[s] + 1; j <= h[s]; j++) {
				if (bay[s][j] > maxPriorityValue) {
					maxPriorityValue = bay[s][j];
					maxPoint.setLocation(s, j);
				}
			}
		}
		if (maxPoint.x != 0) {
			getMinW(maxPoint.x, maxPoint.y);
		}

		return maxPoint;
	}

	private void updateCalculations() {
		updateHigh();
		updateOrderly();
		updateG();
		updateF();
		updateW();
		updateNWL();
		updateNW();
		update_F();
		update_W();
		updateFR();
		update_H();
	}

	private Point min_HBlock() {
		Point result = new Point(0, 0);
		int min_H = Integer.MAX_VALUE;
		for (int s = 1; s <= S; s++) {
			for (int j = orderly[s] + 1; j <= h[s]; j++) {
				if (_h[s][j] < min_H) {
					min_H = _h[s][j];
					result.setLocation(s, j);
				}
			}
		}
		return result;
	}

	private Point selectBlock(int method) {
		if (method == LARGEST_PRIORITY) {
			return calculateMaxPriority();
		} else {
			return min_HBlock();
		}
	}

	public void relocateLowestTier(int s, int t, int d) {
		log.info("relocateLowestTier");
		if (s != d) {
			while (true) {
				if (h[s] > t) {
					if (orderly[d] < h[d] || bay[d][orderly[d]] < bay[s][t]) {
						if (bay[s][h[s]] >= bay[d][h[d]]) {
							moveLowestTier(s, d, s, true);
						} else {
							moveLowestTier(s, d, d, true);
						}
					} else {
						moveLowestTier(s, d, s, true);
					}
				} else if (orderly[d] < h[d] || bay[d][orderly[d]] < bay[s][t]) {
					moveLowestTier(s, d, d, true);
				} else {
					simpleMove(s, d);
					break;
				}
			}
		} else {
			int p = bay[s][t];
			int tempD = 0;
			while (true) {
				if (h[s] > t) {
					moveLowestTier(s, s, s, true);
				} else if (h[s] == t) {
					tempD = moveHihestTier(s);
					log.info("tempD:" + tempD);
				} else if (orderly[s] < h[s]) {
					moveLowestTier(s, tempD, s, true);
				} else if (bay[s][orderly[s]] < p) {
					moveLowestTier(s, tempD, s, true);
				} else {
					log.info("move back tempD");
					simpleMove(tempD, s);
					break;
				}
			}
		}
	}

	private int moveHihestTier(int sF) {
		int highestT = 0;
		int highestS = 0;
		for (int s = 1; s <= S; s++) {
			if (s == sF || h[s]==H) {
				continue;
			} else if (h[s] > highestT) {
				highestS = s;
				highestT = h[s];
			}
		}
		simpleMove(sF, highestS);
		return highestS;
	}

	public void showBay() {
		log.info("show bay:");
		for (int t = H; t > 0; t--) {
			String bayS = "";
			for (int s = 1; s <= S; s++) {
				bayS += (bay[s][t] + "\t");
			}
			log.info(bayS);
		}
	}

	private boolean simpleMove(int s1, int s2) {
//		log.info(s1+ " move to "+ s2 + " before move h["+s1+"]="+h[s1]+" h["+s2+"]=" + h[s2]);
		assert s1 != s2;
		assert s1>=1 && s1<=S;
		assert s2>=1 && s2<=S;
		assert h[s1] > 0;
		assert h[s2] < H;
		if(s1==s2 || s1<1 || s1>S || s2<1 || s2>S || h[s1]<1 || h[s2]>=H) {
			log.error("error move");
			//return false;
		}
		h[s2]++;
		bay[s2][h[s2]] = bay[s1][h[s1]];
		bay[s1][h[s1]] = 0;
		h[s1]--;
		solutions.add(new Operation(s1, s2, Operation.Type.Move));
//		showBay();
		updateOrderly();
		DataIO.readLine();
		return true;
	}

	private void moveLowestTier(int sF, int sT, int sM, boolean normal) {
		int lowestS = 0;
		int lowestTier = Integer.MAX_VALUE;
		for (int s = 1; s <= S; s++) {
			if (s == sF) {
				continue;
			}
			if (s == sT) {
				continue;
			}
			if (h[s] < lowestTier) {
				lowestTier = h[s];
				lowestS = s;
			}
		}
		if(!normal && lowestS==0 && h[sT]<H) {
			lowestS = sT;
		}
		assert lowestTier < H;
		log.info("lowest S = " + lowestS);
		simpleMove(sM, lowestS);
	}

	private int compareH1(int s1, int s2) {
		if (h[s1] < H - 1) {
			if (h[s2] >= H - 1) {
				return 1;
			}
		} else if (h[s2] < H - 1) {
			return -1;
		}
		return 0;
	}

	private int compareClean(int s1, int s2) {
		if (orderly[s1] == h[s1]) {
			if (orderly[s2] < h[s2]) {
				return 1;
			}
		} else if (orderly[s2] == h[s2]) {
			return -1;
		}
		return 0;
	}

	private int getMaxNW(int s) {
		int maxNW = 0;
		for (int t = orderly[s] + 1; t <= h[s]; t++) {
			maxNW = Math.max(bay[s][t], maxNW);
		}
		return maxNW;
	}

	private int compareMaxNW(int s1, int s2) {
		int maxNW1 = getMaxNW(s1);
		int maxNW2 = getMaxNW(s2);
		if (maxNW1 > maxNW2) {
			return 1;
		} else if (maxNW1 < maxNW2) {
			return -1;
		}
		return 0;
	}

	private int comparePI(int s1, int s2) {
		if (compareH1(s1, s2) != 0) {
			return compareH1(s1, s2);
		} else if (compareClean(s1, s2) != 0) {
			return compareClean(s1, s2);
		}
		return compareMaxNW(s1, s2);
	}

	private boolean moveLPI(int sF, int sT, int sM, boolean normal) {
		int lowestS = 0;
		for (int s = 1; s <= S; s++) {
			if (s == sF || s==sT || h[s]==H) {
				continue;
			}
			if (lowestS == 0) {
				lowestS = s;
			} else if (comparePI(s, lowestS) > 0) {
				lowestS = s;
			}
		}
		if(!normal && lowestS==0 && h[sT]<H) {
			lowestS = sT;
		} 
//		else if(lowestS==0) {
//			log.error("failed to find LPI");
//			throw new RuntimeException("failed to find LPI");
//		}
//		log.info("sF = " + sF + " sT=" + sT + " lowest LPI=" + lowestS);
		simpleMove(sM, lowestS);
		return true;
	}

	private int moveHPI(int sF) {
		int highestS = 0;
		for (int s = 1; s <= S; s++) {
			if (s == sF || h[s]==H) {
				continue;
			}
			if (highestS == 0) {
				highestS = s;
			} else if (comparePI(s, highestS) < 0) {
				highestS = s;
			}
		}
		log.info("sF = " + sF + " highest HPI:" + highestS);
		simpleMove(sF, highestS);
		return highestS;
	}

	public boolean relocateLPI(int s, int t, int d) {
		if (s != d) {
			while (true) {
				if (h[s] > t) {
					if (orderly[d] < h[d] || bay[d][h[d]] < bay[s][t]) {
						if (bay[s][h[s]] >= bay[d][h[d]]) {
							if(!moveLPI(s, d, s, true)) {
								return false;
							}
						} else {
							if(!moveLPI(s, d, d, true)) {
								return false;
							}
						}
					} else {
						moveLPI(s, d, s, true);
					}
				} else if (orderly[d] < h[d] || bay[d][orderly[d]] < bay[s][t]) {
					moveLPI(s, d, d, true);
				} else {
					simpleMove(s, d);
					break;
				}
			}
		} else {
			int p = bay[s][t];
			int tempD = 0;
			while (true) {
				if (h[s] > t) {
					moveLPI(s, s, s, true);
				} else if (h[s] == t) {
					tempD = moveHPI(s);
					log.info("tempD: " + tempD);
				} else if (orderly[s] < h[s]) {
					moveLPI(s, tempD, s, true);
				} else if (bay[s][orderly[s]] < p) {
					moveLPI(s, tempD, s, true);
				} else {
					simpleMove(tempD, s);
					break;
				}
			}
		}
		return true;
	}

	private int compareMinClean(int s1, int s2) {
		if (orderly[s1] == h[s1] && orderly[s2] == h[s2]) {
			if (bay[s1][h[s1]] < bay[s2][h[s2]]) {
				return 1;
			} else if (bay[s1][h[s1]] > bay[s2][h[s2]]) {
				return -1;
			}
		} else if (orderly[s1] == h[s1]) {
			return 1;
		} else if (orderly[s2] == h[s2]) {
			return -1;
		}
		return 0;
	}

	private int compareMinMax(int s1, int s2) {
		if (compareH1(s1, s2) != 0) {
			return compareH1(s1, s2);
		} else if (compareMinClean(s1, s2) != 0) {
			return compareMinClean(s1, s2);
		}
		return compareMaxNW(s1, s2);
	}

	private void moveMinMax(int sF, int sT, int sM, boolean normal) {
		int lowestS = 0;
		for (int s = 1; s <= S; s++) {
			if (s == sF || s==sT || h[s]==H) {
				continue;
			}
			if (lowestS == 0) {
				lowestS = s;
			} else if (compareMinMax(s, lowestS) > 0) {
				lowestS = s;
			}
		}
		if(!normal && lowestS==0 && h[sT]<H) {
			lowestS = sT;
		}
		simpleMove(sM, lowestS);
	}

	private int moveMaxMin(int sF) {
		int highestS = 0;
		for (int s = 1; s <= S; s++) {
			if (s == sF || h[s]==H) {
				continue;
			}
			if (highestS == 0) {
				highestS = s;
			} else if (compareMinMax(s, highestS) < 0) {
				highestS = s;
			}
		}
		simpleMove(sF, highestS);
		return highestS;
	}

	public void relocateMinMax(int s, int t, int d) {
		if (s != d) {
			while (true) {
				if (h[s] > t) {
					if (orderly[d] < h[d] || bay[d][orderly[d]] < bay[s][t]) {
						if (bay[s][h[s]] >= bay[d][h[d]]) {
							moveMinMax(s, d, s, true);
						} else {
							moveMinMax(s, d, d, true);
						}
					} else {
						moveMinMax(s, d, s, true);
					}
				} else if (orderly[d] < h[d] || bay[d][orderly[d]] < bay[s][t]) {
					moveMinMax(s, d, d, true);
				} else {
					simpleMove(s, d);
					break;
				}
			}
		} else {
			int p = bay[s][t];
			int tempD = 0;
			while (true) {
				if (h[s] > t) {
					moveMinMax(s, s, s, true);
				} else if (h[s] == t) {
					tempD = moveMaxMin(s);
				} else if (orderly[s] < h[s]) {
					moveMinMax(s, tempD, s, true);
				} else if (bay[s][orderly[s]] < p) {
					moveMinMax(s, tempD, s, true);
				} else {
					simpleMove(tempD, s);
					break;
				}
			}
		}
	}
	
	private boolean preCheckRelocate(int s, int t ,int d){
		if(s!=d) {
			int totalEmpty = 0;
			for(int i=1;i<=S;i++) {
				if(i==s||i==d) {
					continue;
				}
				totalEmpty += H - h[i];
			}
			return totalEmpty >= f[s][t][d] + g[s][t];
		} else {
			
		}
		return true;
	}

	private void relocate(int s, int t, int d) {
		log.info("relocate");
		if(!preCheckRelocate(s, t, d)) {
			log.info("failed in preCheck");
			doUnnormalRelocate(s, t, d);
			return ;
		}
		
		int[][] originBay = new int[S + 1][H + 1];
		for (int i = 1; i <= S; i++)
			for (int h = 0; h <= H; h++) {
				originBay[i][h] = bay[i][h];
			}
		int moveLen = solutions.size();
		try {
		if (relocateMethod == RM_LOWEST_TIER) {
			relocateLowestTier(s, t, d);
		} else if (relocateMethod == RM_LOWEST__PRIORITY_INDEX) {
			relocateLPI(s, t, d);
		} else {
			relocateMinMax(s, t, d);
		}
		} catch (Exception e) {
			for (int i = 1; i <= S; i++)
				for (int h = 0; h <= H; h++) {
					bay[i][h] = originBay[i][h];
				}
			updateHigh();
			updateOrderly();
			while (solutions.size() > moveLen) {
				solutions.remove(solutions.size() - 1);
			}
			doUnnormalRelocate(s, t, d);
		}
	}

	private void doUnnormalRelocate(int s, int t, int d) {
//		DataIO.readLine();
		log.info("do unnormal relocate");
		int p = bay[s][t];
		while (h[s] > t) {
			if (relocateMethod == RM_LOWEST_TIER) {
				moveLowestTier(s, s, s, false);
			} else if (relocateMethod == RM_LOWEST__PRIORITY_INDEX) {
				moveLPI(s, s, s, false);
			} else {
				moveMinMax(s, s, s, false);
			}
		}
		int tempS = selectRandomFull(s, d);
		while(tempS==-1) {
			if(orderly[d]<h[d] || bay[d][h[d]]<p) {
				if (relocateMethod == RM_LOWEST_TIER) {
					moveLowestTier(d, s, d, true);
				} else if (relocateMethod == RM_LOWEST__PRIORITY_INDEX) {
					moveLPI(d, s, d, true);
				} else {
					moveMinMax(d, s, d, true);
				}
			} else {
				log.error("exception case");
				throw new RuntimeException("can not move");
			}
			tempS = selectRandomFull(s, d);
		}
		while (orderly[d] < h[d] || bay[d][h[d]] < p) {
			if (relocateMethod == RM_LOWEST_TIER) {
				moveLowestTier(d, tempS, d, true);
			} else if (relocateMethod == RM_LOWEST__PRIORITY_INDEX) {
				moveLPI(d, tempS, d, true);
			} else {
				moveMinMax(d, tempS, d, true);
			}
		}
		simpleMove(tempS, d);
	}

	private int selectRandomFull(int s, int d) {
		log.info("selectRandomFull");
		List<Integer> fulls = new ArrayList<>();
		for(int i=1;i<=S;i++) {
			if(i==s||i==d) {
				continue;
			}
			if(passFullCheck(s, d, i)) {
				simpleMove(s, i);
				log.info("temp=" + i);
				return i;
			}
			if(h[i]==H) {
				fulls.add(i);
			}
		}
		if(fulls.size()==0) {
			return -1;
//			throw new RuntimeException("no full exist");
		}
		int rd = random.nextInt(fulls.size());
		int tempS = fulls.get(rd);
		log.info("temp=" + tempS);
		if (relocateMethod == RM_LOWEST_TIER) {
			moveLowestTier(tempS, s, tempS, true);
		} else if (relocateMethod == RM_LOWEST__PRIORITY_INDEX) {
			moveLPI(tempS, s, tempS, true);
		} else {
			moveMinMax(tempS, s, tempS, true);
		}
		simpleMove(s, tempS);
		return tempS;
	}
	
	private boolean passFullCheck(int s, int d, int temp) {
		if(h[temp]==H) {
			return false;
		}
		int p = bay[s][h[s]];
		int others2Move = 0;
		int totalEmpty = 0;
		if(s==d) {
			for(int k=Math.min(h[s]-1, orderly[s]);k>=0;k--) {
				if(bay[s][k]>=p) {
					others2Move = h[s]-1 - k;
				}
			}
			for(int i=1;i<=S;i++) {
				if(i==d || i==temp) {
					continue;
				}
				totalEmpty += H - h[i];
			}
		} else {
			for(int k=orderly[d];k>=0;k--) {
				if(bay[s][k]>=p) {
					others2Move = h[d] - k;
				}
			}
			totalEmpty = 1;
			for(int i=1;i<=S;i++) {
				if(i==d || i==temp) {
					continue;
				}
				totalEmpty += H - h[i];
			}
		}
		return totalEmpty >= others2Move;
	}

	private void fillStand(int sF) {
		while (h[sF] < H) {
			int maxP = 0;
			int sM = 0;
			for (int s = 1; s <= S; s++) {
				if (s == sF) {
					continue;
				} else if (orderly[s] == h[s]) {
					continue;
				} else if (bay[s][h[s]] > bay[sF][h[sF]]) {
					continue;
				} else if (bay[s][h[s]] > maxP) {
					maxP = bay[s][h[s]];
					sM = s;
				}
			}
			if (sM == 0) {
				break;
			} else {
				simpleMove(sM, sF);
			}

		}
	}

	private void fillSafe(int sF) {
		int[][] originBay = new int[S + 1][H + 1];
		for (int s = 1; s <= S; s++)
			for (int h = 0; h <= H; h++) {
				originBay[s][h] = bay[s][h];
			}
		int moveLen = solutions.size();
		fillStand(sF);
		if (h[sF] <= H / 2) {
			for (int s = 1; s <= S; s++)
				for (int h = 0; h <= H; h++) {
					bay[s][h] = originBay[s][h];
				}
			while (solutions.size() > moveLen) {
				solutions.remove(solutions.size() - 1);
			}
			updateHigh();
			updateOrderly();
		}
	}

	private void fillStop(int sF) {
		while (h[sF] < H) {
			int maxP = 0;
			int sM = 0;
			for (int s = 1; s <= S; s++) {
				if (s == sF) {
					continue;
				} else if (orderly[s] == h[s]) {
					continue;
				} else if (bay[s][h[s]] > bay[sF][h[sF]]) {
					continue;
				} else if (bay[s][h[s]] > maxP) {
					maxP = bay[s][h[s]];
					sM = s;
				}
			}
			if (sM == 0 || h[sM] >= 2 && bay[sM][h[sM] - 1] < bay[sF][h[sF]] && bay[sM][h[sM]] < bay[sM][h[sM]-1]) {
				break;
			} else {
				simpleMove(sM, sF);
			}
		}
	}

	private void fill(int sF) {
		if (fillingMethod == FILL_NONE) {
			return;
		} else if (fillingMethod == FILL_STANDARD) {
			fillStand(sF);
		} else if (fillingMethod == FILL_SAFE) {
			fillSafe(sF);
		} else if (fillingMethod == FILL_STOP) {
			fillStop(sF);
		}
	}

	public int solve_IEEE(int a, int b, int c, int d) {

		solutions = new ArrayList<>();
		wMethod = b;
		relocateMethod = c;
		fillingMethod = d;
		while (true) {
//			DataIO.readLine();
			updateCalculations();
//			showWFG();
			calculateMaxPriority();
			Point newToMove = selectBlock(a);
			if (newToMove.x == 0) {
				break;
			}
			log.info("move block "+ newToMove.x + " " + newToMove.y + " "
					+" to "+ dest[newToMove.x][newToMove.y]);
			relocate(newToMove.x, newToMove.y, dest[newToMove.x][newToMove.y]);
			fill(dest[newToMove.x][newToMove.y]);
		}
		log.info("move length: " + solutions.size());
		return solutions.size();
	}
}
