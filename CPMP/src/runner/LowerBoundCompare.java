package runner;

import java.io.File;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Scanner;

import cpmp.Layout;
import cpmp.LowerBound;

public class LowerBoundCompare
{
	/*
	 * args: 0: BF or HCVS or ZHU 1: 3,6-8,14 or ALL 2: PU2 or probe 3:
	 * reportName
	 */

	public static String[] AUTHORS = { "TEST", "BF", "HCVS" };
	public static int[] testcount = { 1, 32, 21 };// groups of each data
	public static String reportName = "Lower Bound Compare";

	public static void main(String[] args)
	{
		try
		{
			for (int whoseData = 1; whoseData <= 1; whoseData++)
			{
				String author = AUTHORS[whoseData];
				String resultpath = "experiments/lowerboundcompare/";
				new File(resultpath).mkdirs();
				File summary = new File(resultpath + author + "summary.csv");
				PrintStream sumps = new PrintStream(summary);
				sumps.println("group,case,lb-dfs,t-dfs,lb-maxknap,t-maxkanap,lb-fb,t-fb");

				for (int testgroup = 1; testgroup <= testcount[whoseData]; testgroup++)
				{
					File dir = new File("data/" + author + "wTRUCK/" + author + testgroup);
					File files[] = dir.listFiles();

					for (int testcase = 1; testcase <= files.length; testcase++)
					{
						File file = new File("data/" + author + "wTRUCK/" + author + testgroup + "/" + author + testgroup + "_" + testcase + ".bay");
						Scanner scn = new Scanner(file);
						Layout inst = new Layout(scn);
						scn.close();
						long starttime = System.currentTimeMillis();

						int lb1 = LowerBound.WangNingLB2(inst);
						long t1 = System.currentTimeMillis() - starttime;

						int lb2 = LowerBound.WangNingMaxKnapsack(inst);
						long t2 = System.currentTimeMillis() - starttime - t1;
						int lb3 = LowerBound.LBFB(inst);

						long t3 = System.currentTimeMillis() - starttime - t2;

						sumps.println(testgroup + "," + testcase + "," + lb1 + "," + t1 + "," + lb2 + "," + t2 + "," + lb3 + "," + t3);

					}
				}
				sumps.close();
			}
		} catch (Exception e)
		{
			System.err.println("Find Exception: " + e);
			e.printStackTrace();
		}
	}
}
