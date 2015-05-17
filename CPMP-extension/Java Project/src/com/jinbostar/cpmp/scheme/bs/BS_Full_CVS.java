package com.jinbostar.cpmp.scheme.bs;


import com.jinbostar.cpmp.scheme.fbh.Node;

import java.io.File;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Scanner;

import static com.jinbostar.cpmp.common.Parameter.DualSenderOrder.SmallerEvalFirstOrder;
import static com.jinbostar.cpmp.common.Parameter.TaskPreference;
import static com.jinbostar.cpmp.common.Parameter.TaskPreference.*;
import static com.jinbostar.cpmp.common.Parameter.TopFilter.TrickyAvoid;

public class BS_Full_CVS
{

	public static Node read_CVS(File file) throws Exception
	{
		Scanner scn = new Scanner(file);
		scn.nextLine();
		int S = Integer.parseInt(scn.nextLine().split(" ")[2]);
		int K = Integer.parseInt(scn.nextLine().split(" ")[2]);
		int N = Integer.parseInt(scn.nextLine().split(" ")[2]);
		int P = N;
		int H = K + 2;

		assert N == S * K;

		int[][] init = new int[S + 1][H + 1];

		for (int s = 1; s <= S; s++)
		{
			String[] str = scn.nextLine().split(" ");

			for (int i = 3; i < str.length; i++)
			{
				int t = i - 2;
				init[s][t] = Integer.parseInt(str[i]);
				assert init[s][t] >= 1;
			}
		}
		scn.close();

		return new Node(S, H, P, N, init);
	}

	public static void main(String[] args) throws Exception
	{
		String date = new SimpleDateFormat("yyyyMMdd-HHmmss").format(Calendar.getInstance().getTime());
		new File("result/" + date).mkdirs();


		File result = new File("result/" + date + "/result.csv");
		PrintStream ps = new PrintStream(result);

		ps.println("Group,Case,Move,Time,Iter,BestAt");

		int res_tot = 0;
		long tim_tot = 0;
		int cnt_tot = 0;
		for (int group = 1; group <= 21; group++)
		{

			String dir = "data/CVS(21)/CVS" + group;


			int K = -1;
			int S = -1;

			int res_sum = 0;
			long tim_sum = 0;
			int cnt_sum = 0;
			int bad_sum = 0;
			//	Counter count=new Counter();
			for (int ins = 1; ins <= 40; ins++)
			{

				File file = new File(dir + "/" + ins + ".bay");

				//	System.err.println(file.toString());

				Node fbh = read_CVS(file);
				if (K == -1)
				{
					K = fbh.H - 2;
					S = fbh.S;
				}
				bad_sum += fbh.M();

				fbh.topFilter = TrickyAvoid;
				fbh.stability = true;
				fbh.preferences = new TaskPreference[]{
						MoveActual, BlockingNiceInAimStack, DemandAffected,
						LowerAimTier, LargerPriority,
				};
				fbh.order = SmallerEvalFirstOrder;
				fbh.setBottomFilter(2, fbh.S);
				//fbh.bottomFilter=null;


				long st = System.currentTimeMillis();
				BeamSearch bs = new BeamSearch();
				int res = bs.solve(fbh, 20);
				long tim = System.currentTimeMillis() - st;


				ps.println(group + "," + ins + "," + res + "," + tim + "," + bs.iteration + "," + bs.bestFountAtIteration);


				res_sum += res;
				tim_sum += tim;
				cnt_sum += 1;


			}

			res_tot += res_sum;
			tim_tot += tim_sum;
			cnt_tot += cnt_sum;
			System.out.println("Group " + group + " (" + K + "*" + S + "): " + String.format("%.4f", 1.0 * res_sum / cnt_sum) + " moves, " + String.format("%.2fms", 1.0 * tim_sum / cnt_sum) + " " + cnt_sum + " solved");
			//		System.out.println(String.format("%.4f", 1.0 * res_sum / cnt_sum) +"\t" + String.format("%.2f", 1.0 * tim_sum / cnt_sum));

		}
		System.out.println("Total: " + String.format("%.4f", 1.0 * res_tot / cnt_tot) + " moves, " + String.format("%.2f", 1.0 * tim_tot / cnt_tot));

		ps.close();
	}
}
