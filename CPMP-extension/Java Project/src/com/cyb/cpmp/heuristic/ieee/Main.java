package com.cyb.cpmp.heuristic.ieee;

import static com.jinbostar.cpmp.common.Parameter.DualSenderOrder.SmallerEvalFirstOrder;
import static com.jinbostar.cpmp.common.Parameter.TaskPreference.BlockingNiceInAimStack;
import static com.jinbostar.cpmp.common.Parameter.TaskPreference.DemandAffected;
import static com.jinbostar.cpmp.common.Parameter.TaskPreference.LargerPriority;
import static com.jinbostar.cpmp.common.Parameter.TaskPreference.LowerAimTier;
import static com.jinbostar.cpmp.common.Parameter.TaskPreference.MoveActual;
import static com.jinbostar.cpmp.common.Parameter.TopFilter.TrickyAvoid;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jinbostar.cpmp.common.Operation;
import com.jinbostar.cpmp.common.Parameter.TaskPreference;
import com.jinbostar.cpmp.scheme.fbh.Node;

import data.io.DataIO;

public class Main {
	static Logger log = LoggerFactory.getLogger(Main.class);
	public void ieeeMethod() {
		String date = new SimpleDateFormat("yyyyMMdd-HHmmss").format(Calendar
				.getInstance().getTime());
		new File("result/" + date).mkdirs();

		File result = new File("result/" + date + "/result.csv");
		PrintStream ps = null;
		try {
			ps = new PrintStream(result);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		ps.println("Group,Case,Move,Time");

		int res_tot = 0;
		long tim_tot = 0;
		int cnt_tot = 0;
//		int group = 20;
//		for (group = 1; group <= 21; group++)
//		{
//
//			String dir = "data/CVS(21)/CVS" + group;
//
//			// int K = -1;
//			// int S = -1;
//
//			int res_sum = 0;
//			long tim_sum = 0;
//			int cnt_sum = 0;
//			int bad_sum = 0;
//			// Counter count=new Counter();
////			int ins = 13;
//			 for (int ins = 1; ins <= 40; ins++)
//			{
//
//				File file = new File(dir + "/" + ins + ".bay");
		
		for (int group = 1; group <= 32; group++)
		{
		String dir = "data/BF(32)/BF" + group;

		int g = group - 1;
		int S = (g & (1 << 4)) == 0 ? 16 : 20;
		int H = (g & (1 << 3)) == 0 ? 5 : 8;
		int N = (int) Math.ceil(((g & 1 << 2) == 0 ? 0.6 * S * H : 0.8 * S * H));
		int P = (int) Math.ceil(((g & 1 << 1) == 0 ? 0.2 * N : 0.4 * N));
		int M = (int) Math.ceil(((g & 1) == 0 ? 0.6 * N : 0.75 * N));
		String prefix = "cpmp_" + S + "_" + H + "_" + N + "_" + P + "_" + M + "_";

		int res_sum = 0;
		long tim_sum = 0;
		int cnt_sum = 0;
		for (int ins = 1; ins <= 20; ins++)
		{

			File file = new File(dir + "/" + prefix + ins + ".bay");

				// System.err.println(file.toString());

				Solver fbh = null;
				try {
					fbh = DataIO.read_CVS(file);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				// if (K == -1)
				// {
				// K = fbh.H - 2;
				// S = fbh.S;
				// }
//				bad_sum += fbh.M();

				fbh.topFilter = TrickyAvoid;
				fbh.stability = true;
				fbh.preferences = new TaskPreference[] { MoveActual,
						BlockingNiceInAimStack, DemandAffected, LowerAimTier,
						LargerPriority, };
				fbh.order = SmallerEvalFirstOrder;
				fbh.setBottomFilter(2, fbh.S);
				// fbh.bottomFilter=null;
				int minMove = Integer.MAX_VALUE;
				long totalTime = 0;
				List<Operation> minSolution = null;
				log.info("grounp:" + group + "inst: " + ins);
				DataIO.readLine();
				for (int a = 1; a <= 2; a++) {
					for (int b = 1; b <= 2; b++) {
						for (int c = 1; c <= 3; c++) {
							for (int d = 1; d <= 4; d++) {
								log.info(a+" "+b+" "+ c + " " + d);
								DataIO.readLine();
								try {
									fbh = DataIO.read_CVS(file);
									long start = System.currentTimeMillis();
									fbh.solve_IEEE(a, b, c, d);
									totalTime += System.currentTimeMillis() - start;
									if(fbh.solutions.size() < minMove ) {
										minMove = fbh.solutions.size();
										minSolution = new ArrayList<>(fbh.solutions);
									}
								} catch (Exception e) {
									e.printStackTrace();
								}
							}
						}
					}
				}
				log.info("min Move:"+minMove);
				for(Operation operation : minSolution) {
					log.info(operation.s1+ " " + operation.s2);
				}
				fbh.solve_IEEE(2, 2, 3, 4);
				ps.println(group + "," + ins + "," + minMove + "," + totalTime);
				 res_sum += minMove;
				 tim_sum += 0;
				 cnt_sum += 1;

			}

			res_tot += res_sum;
			tim_tot += tim_sum;
			cnt_tot += cnt_sum;
			// System.out.println("Group " + group + " (" + K + "*" + S + "): "
			// + String.format("%.4f", 1.0 * res_sum / cnt_sum) + " moves, " +
			// String.format("%.2fms", 1.0 * tim_sum / cnt_sum) + " " + cnt_sum
			// + " solved");
			// System.out.println(String.format("%.4f", 1.0 * res_sum / cnt_sum)
			// +"\t" + String.format("%.2f", 1.0 * tim_sum / cnt_sum));
//			System.out.println(bad_sum * 1.0 / cnt_sum);
		}
		System.out.println("Total: "
				+ String.format("%.4f", 1.0 * res_tot / cnt_tot) + " moves, "
				+ String.format("%.2f", 1.0 * tim_tot / cnt_tot));

		ps.close();
	}

	public static void main(String[] args) {
		Main m = new Main();
		m.ieeeMethod();
	}
}
