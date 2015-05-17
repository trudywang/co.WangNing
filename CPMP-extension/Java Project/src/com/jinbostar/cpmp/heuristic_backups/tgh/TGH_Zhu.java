package com.jinbostar.cpmp.heuristic_backups.tgh;

import java.io.File;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Scanner;


public class TGH_Zhu
{

	public static TargetGuided read_Zhu(File file) throws Exception
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
		for (int group = 1; group <= 25; group++)
		{
			String dir = "data/Zhu(25)/Zhu" + group;

			int insCount = (group - 1) / 5 * 100 + 300;

			int res_sum = 0;
			long tim_sum = 0;
			int cnt_sum = 0;
			for (int ins = 1; ins <= insCount; ins++)
			{

				File file = new File(dir + "/" + ins + ".bay");

				//	System.err.println(file.toString());

				TargetGuided tgh = read_Zhu(file);


				long st = System.currentTimeMillis();
				//		System.out.println(lpf.sideView());
				int res = tgh.solve();
				//			System.out.println(lpf.sideView());
				long tim = System.currentTimeMillis() - st;


				//		ps.println(group + "," + ins + "," + res + "," + tim);
				//			System.out.println(group + "," + ins + "," + res + "," + tim);

				if (res != 1000000)
				{
					res_sum += res;
					tim_sum += tim;
					cnt_sum += 1;
				}
			}

			System.out.println("Group " + group + ": " + cnt_sum + " instances solved, average " + String.format("%.2f", 1.0 * res_sum / cnt_sum) + " moves, " + String.format("%.2f", 1.0 * tim_sum / cnt_sum));
		}

		ps.close();
	}
}
