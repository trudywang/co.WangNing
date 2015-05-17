package com.jinbostar.cpmp.heuristic_backups.tgh;

import java.io.File;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Scanner;


public class TGH_BF
{

	public static TargetGuided read_BF(File file) throws Exception
	{
		Scanner scn = new Scanner(file);
		scn.nextLine();
		int S = Integer.parseInt(scn.nextLine().split(" ")[2]);
		int H = Integer.parseInt(scn.nextLine().split(" ")[2]);
		int N = Integer.parseInt(scn.nextLine().split(" ")[2]);
		int maxPriority = 0;

		int[][] init = new int[S + 1][H + 1];

		for (int s = 1; s <= S; s++)
		{
			String[] str = scn.nextLine().split(" ");

			for (int i = 3; i < str.length; i++)
			{
				int t = i - 2;
				init[s][t] = Integer.parseInt(str[i]);
				assert init[s][t] >= 1;
				maxPriority = Math.max(maxPriority, init[s][t]);
			}
		}

		int P = maxPriority;

		scn.close();

		return new TargetGuided(S, H, P, N, init);
	}

	public static void main(String[] args) throws Exception
	{
		String date = new SimpleDateFormat("yyyyMMdd-HHmmss").format(Calendar.getInstance().getTime());
		new File("result/" + date).mkdirs();


		File result = new File("result/" + date + "/result.csv");
		PrintStream ps = new PrintStream(result);

		ps.println("Group,Case,Move,Time");

		int res_tot = 0;
		long tim_tot = 0;
		int cnt_tot = 0;
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

				//	System.err.println(file.toString());

				TargetGuided tgh = read_BF(file);


				long st = System.currentTimeMillis();
				//		System.out.println(lpf.sideView());
				int res = tgh.solve();
				//		System.out.println(lpf.sideView());
				long tim = System.currentTimeMillis() - st;


				//		ps.println(group + "," + ins + "," + res + "," + tim);
				//		System.out.println(group + "," + ins + "," + res + "," + tim);

				res_sum += res;
				tim_sum += tim;
				cnt_sum += 1;


			}

			res_tot += res_sum;
			tim_tot += tim_sum;
			cnt_tot += cnt_sum;

			System.out.println("Group " + group + ": " + String.format("%.2f", 1.0 * res_sum / cnt_sum) + " moves, " + String.format("%.2f", 1.0 * tim_sum / cnt_sum));
		}
		System.out.println("Total: " + String.format("%.2f", 1.0 * res_tot / cnt_tot) + " moves, " + String.format("%.2f", 1.0 * tim_tot / cnt_tot));

		ps.close();
	}
}
