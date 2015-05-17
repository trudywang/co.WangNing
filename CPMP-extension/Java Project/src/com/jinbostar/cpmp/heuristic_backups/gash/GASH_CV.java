package com.jinbostar.cpmp.heuristic_backups.gash;

import java.io.File;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Scanner;


public class GASH_CV
{

	public static GreedyAndSpeedy read_CV(File file) throws Exception
	{
		Scanner scn = new Scanner(file);
		scn.nextLine();
		int S = Integer.parseInt(scn.nextLine().split(" ")[2]);
		int K = Integer.parseInt(scn.nextLine().split(" ")[2]);
		int N = Integer.parseInt(scn.nextLine().split(" ")[2]);
		int P = N;
		int H = N;

		assert N == S * K && S == K;
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

		return new GreedyAndSpeedy(S, H, P, N, init);
	}

	public static void main(String[] args) throws Exception
	{
		String date = new SimpleDateFormat("yyyyMMdd-HHmmss").format(Calendar.getInstance().getTime());
		new File("result/" + date).mkdirs();


		File result = new File("result/" + date + "/result.csv");
		PrintStream ps = new PrintStream(result);

		ps.println("Group,Case,Move,Time");
		for (int group = 1; group <= 4; group++)
		{
			String dir = "data/CV(4)/CV" + group;


			int res_sum = 0;
			long tim_sum = 0;
			int cnt_sum = 0;
			for (int ins = 1; ins <= 10; ins++)
			{

				File file = new File(dir + "/CV" + group + "_" + ins + ".bay");

				//	System.err.println(file.toString());

				GreedyAndSpeedy gash = read_CV(file);


				long st = System.currentTimeMillis();
				int res = gash.solve();
				long tim = System.currentTimeMillis() - st;


				ps.println(group + "," + ins + "," + res + "," + tim);

				res_sum += res;
				tim_sum += tim;

				cnt_sum += 1;
			}

			System.out.println("Group " + group + ": " + String.format("%.2f", 1.0 * res_sum / cnt_sum) + " moves, " + String.format("%.2f", 1.0 * tim_sum / cnt_sum));
		}

		ps.close();
	}
}
