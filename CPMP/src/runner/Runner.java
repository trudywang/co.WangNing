package runner;

import java.io.File;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Scanner;

import cpmp.Constant;
import cpmp.Layout;
import cpmp.LowerBound;
import cpmp.SolutionReport;
import algos.beamsearch.BeamSearch;
import algos.beamsearch.BeamSearch_EvaluateAndTransmitBabyStepOffspring;
import algos.beamsearch.BeamSearch_EvaluateAndTransmitGaintStepOffspring;
import algos.beamsearch.BeamSearch_EvaluateGaintStepOffspring_TransmitBabyStepOffspring;

public class Runner
{
	/*
	 * args: 0: BF or HCVS or ZHU 1: 3,6-8,14 or ALL 2: PU2 or probe 3:
	 * reportName
	 */

	public static String[] AUTHORS = { "TEST", "BF", "HCVS", "New" };
	public static String[] ProEva = { "fa", "fa_lt", "fa_lt_gs", "fa_gs" };
	public static int[] testcount = { 1, 32, 21, 36 };//groups of each data
	public static String reportName = "HeuristicRule1";
	public static String lowerBoundMethod = "WangNingMaxKnapsack";
	public static int lookaheadDepth;
	public static int lookaheadWidth;
	public static int probingevaluation = Constant.FewerAbove_LowerTier;// The
																		// evaluation
																		// function
	public static int maxpoolSize;

	public static void main(String[] args)
	{
		try
		{
			String date = new SimpleDateFormat("yyyyMMdd").format(Calendar.getInstance().getTime());

			for (int whoseData = 3; whoseData <= 3; whoseData++)
			{
				String author = AUTHORS[whoseData];
				String resultpath = "experiments/"+date+"test/";
				new File(resultpath + author).mkdirs();
				File summary = new File(resultpath + author + "summary.csv");
				PrintStream sumps = new PrintStream(summary);
				sumps.println("group,poolsize,lookdepth,lookwidth,avesol,avetime");
				for (lookaheadDepth = 3; lookaheadDepth <= 3; lookaheadDepth++)
				{
					for (maxpoolSize = 20; maxpoolSize <= 20; maxpoolSize++)
					{
						File result = new File(resultpath + author + "/ks p" + maxpoolSize + " d" + lookaheadDepth + " fa_lt.csv");
						PrintStream indps = new PrintStream(result);
						indps.println("Group,Case,LB,IS,Res,Time,Width,Sequence");
						lookaheadWidth=maxpoolSize;
						//	for (lookaheadWidth = 1; lookaheadWidth <= 20; lookaheadWidth++)
						{
							for (int testgroup = 1; testgroup <= testcount[whoseData]; testgroup++)//
							{
								int sumsol = 0;
								double sumtime=0;
								File dir = new File("data/" + author + "wTRUCK/" + author + testgroup);
								File files[] = dir.listFiles();

								for (int testcase = 1; testcase <= files.length; testcase++)//files.length
								{
									File file = new File("data/" + author + "wTRUCK/" + author + testgroup + "/" + author + testgroup + "_" + testcase + ".bay");
									Scanner scn = new Scanner(file);
									Layout inst = new Layout(scn);
									scn.close();
									// int
									// lb=LowerBoundCumulativeRevertable.WangNingLB2(inst);
									// int
									// lb=LowerBound.WangNingMaxKnapsack(inst);
									// int
									// lb=LowerBoundCumulativeRevertable.LBFB(inst);

									// Solution solution = new Solution();
									// State state=new State();
									// state.inst=inst.copy();
									// state.sol=new Solution();
									// WangNingProbing.probing(state);

									Method lowerBound = LowerBound.class.getMethod(lowerBoundMethod, new Class[] { Layout.class });
									// GreedyFramework mp = new
									// GreedyFramework(inst,lowerBound,lookaheadDepth,
									// lookaheadWidth, probingevaluation);
									
									//BeamSearch mp = new BeamSearch_EvaluateAndTransmitGaintStepOffspring(inst, lowerBound, maxpoolSize, lookaheadDepth, lookaheadWidth, probingevaluation);
									BeamSearch mp = new BeamSearch_EvaluateGaintStepOffspring_TransmitBabyStepOffspring(inst, lowerBound, maxpoolSize, lookaheadDepth, lookaheadWidth, probingevaluation);
									//BeamSearch mp = new BeamSearch_EvaluateAndTransmitBabyStepOffspring(inst, lowerBound, maxpoolSize, lookaheadDepth, lookaheadWidth, probingevaluation);
									
									
									SolutionReport sp = mp.solve();

									int lb = sp.lowerBound;
									int is = sp.initialSolution.relocationCount;
									int bs = sp.bestEverFound.relocationCount;
									double ti = sp.timeUsed * 0.001;
									sumsol += bs;
									sumtime += ti;
									indps.println(testgroup + "," + testcase + "," + lb + "," + is + "," + bs + "," + ti + "," + lookaheadWidth + ","
											+ sp.bestEverFound);
									System.out.println(testgroup + "," + testcase + "," + lb + "," + is + "," + bs + "," + ti);
								}
								sumps.println(testgroup + "," + maxpoolSize + "," + lookaheadDepth + "," + lookaheadWidth + "," + 1.0 * sumsol
										/files.length + "," + sumtime/files.length);

							}
						}
						indps.flush();
						indps.close();
					}
				}
				sumps.flush();
				sumps.close();
			}
		} catch (Exception e)
		{
			System.err.println("Find Exception: " + e);
			e.printStackTrace();
		}
	}
}