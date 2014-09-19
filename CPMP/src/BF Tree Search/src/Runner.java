import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by jinbo on 8/31/14.
 */
public class Runner
{
	/*
	 * args: 0: BF or HCVS or ZHU 1: 3,6-8,14 or ALL 2: PU2 or probe 3:
	 * reportName
	 */

	public static String[] AUTHORS = {"TEST", "BF", "HCVS", "New"};
	public static int[] testcount = {1, 32, 21, 36};//groups of each data
	public static String reportName = "TreeSearch";

	public static void main(String[] args) throws FileNotFoundException
	{

		String date = new SimpleDateFormat("yyyyMMdd").format(Calendar.getInstance().getTime());

		for (int whoseData = 1; whoseData <= 1; whoseData++)
		{
			String author = AUTHORS[whoseData];
			String resultpath = "experiments/" + date + "TreeSearch/";
			new File(resultpath + author).mkdirs();
			File summary = new File(resultpath + author + "summary.csv");
			PrintStream sumps = new PrintStream(summary);
			sumps.println("group,case,res,time");

			for (int testgroup = 1; testgroup <= testcount[whoseData]; testgroup++)//
			{

				File dir = new File("data/" + author + "/" + author + testgroup);
				File files[] = dir.listFiles();
				int sum = 0;
				int cnt = 0;
				int time = 0;

				for (int testcase = 1; testcase <= files.length; testcase++)//files.length
				{
					String file = ("data/" + author + "/" + author + testgroup + "/" + author + testgroup + "_" + testcase + ".bay");

					Layout inst = DataReader.read(file);
					TreeSearch ts = new TreeSearch();
					long tt = System.currentTimeMillis();
					Solution s = ts.solve(inst);
					tt=(System.currentTimeMillis() - tt) / 1000;
					sumps.println(testgroup + "," + testcase + "," + (s == null ? 0 : s.size())+","+tt);
					//		System.out.println(testgroup + "," + testcase + "," +( s == null ? 0 : s.size()));
					if (s != null)
					{
						cnt++;
						sum += s.size();
						time +=tt ;
					}
				}
				System.out.println("Group " + testgroup + ": (" + cnt + "/" + files.length + ") " + 1.0 * sum / cnt + " , " + time * 1.0 / cnt + "s");


			}

		}
	}

}