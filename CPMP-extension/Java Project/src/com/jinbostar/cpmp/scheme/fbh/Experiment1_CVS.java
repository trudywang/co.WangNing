package com.jinbostar.cpmp.scheme.fbh;

import com.jinbostar.cpmp.common.Instance;
import com.jinbostar.cpmp.common.Permutation;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Scanner;

import static com.jinbostar.cpmp.common.Parameter.DualSenderOrder.SmallerEvalFirstOrder;
import static com.jinbostar.cpmp.common.Parameter.TaskPreference;
import static com.jinbostar.cpmp.common.Parameter.TaskPreference.*;
import static com.jinbostar.cpmp.common.Parameter.TopFilter.TrickyAvoid;

public class Experiment1_CVS
{
	public static Instance read_CVS(File file) throws Exception
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

		return new Instance(S, H, P, N, init);
	}

	public static void main(String[] args) throws Exception
	{
		String date = new SimpleDateFormat("yyyyMMdd-HHmmss").format(Calendar.getInstance().getTime());
		new File("result/" + date).mkdirs();


//		File result = new File("result/" + date + "/result.csv");
//		PrintStream ps = new PrintStream(result);

//		ps.println("Group,Case,Move,Time");


		TaskPreference[] list = new TaskPreference[]{
				//		MoveIdeal,
				//		MoveActual,
				//DemandAffected,
				//		Gap,
				//		RevisedGap,
				//		SmallerPriority,
				//LowerAimTier,
				//		HigherAimTier
				//LargerPriority,
				//		Capability,
				//		RevisedCapability,
				//BlockingNiceInAimStack,
				//BlockingNiceInCurrentStack,
				//MoveOverall, MoveActual,
				DemandAffected,
				LowerAimTier, LargerPriority,
		};

		int lz = list.length;

		Permutation permutation = new Permutation(lz, lz);

		double bestVal = 1000000;
		int[] bestPer = null;
		while (permutation.hasNext())
		{
			int[] next = permutation.next();


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
				//	Counter count=new Counter();
				for (int ins = 1; ins <= 40; ins++)
				{

					File file = new File(dir + "/" + ins + ".bay");

					//	System.err.println(file.toString());

					Instance instance = read_CVS(file);


					Node fbh = new Node(instance.S, instance.H, instance.P, instance.N, instance.bay);

					if (K == -1)
					{
						K = fbh.H - 2;
						S = fbh.S;
					}


					fbh.topFilter = TrickyAvoid;
					fbh.stability = true;

					fbh.order = SmallerEvalFirstOrder;
					fbh.setBottomFilter(2, fbh.S);

					fbh.preferences = new TaskPreference[lz];
					//fbh.preferences[0] = MoveActual;

					for (int i = 0; i < lz; i++)
						fbh.preferences[i] = list[next[i]];


					long st = System.currentTimeMillis();
					int res = fbh.solve();
					long tim = System.currentTimeMillis() - st;


					//			ps.println(group + "," + ins + "," + res + "," + tim);

					res_sum += res;
					tim_sum += tim;
					cnt_sum += 1;


//				for(Map.Entry<String,Integer> entry:lpf.counter.record.entrySet())
//				{
//					count.inc(entry.getKey(),entry.getValue());
//				}
				}

				res_tot += res_sum;
				tim_tot += tim_sum;
				cnt_tot += cnt_sum;

				//			System.out.println("Group " + group + " (" + K + "*" + S + "): " + String.format("%.2f", 1.0 * res_sum / cnt_sum) + " moves, " + String.format("%.2f", 1.0 * tim_sum / cnt_sum));
//			for(Map.Entry<String,Integer> entry:count.record.entrySet())
//			{
//				System.out.println(entry.getKey()+" "+entry.getValue());
//			}
			}
			//		System.out.println("Total: " + String.format("%.2f", 1.0 * res_tot / cnt_tot) + " moves, " + String.format("%.2f", 1.0 * tim_tot / cnt_tot));


			if (1.0 * res_tot / cnt_tot < bestVal)
			{
				bestVal = 1.0 * res_tot / cnt_tot;
				bestPer = next;
			}
			//	ps.close();
		}
		System.out.println(bestVal + " " + Arrays.toString(bestPer));
	}
}
