package cpmp;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Scanner;

public class Statistic {
	//Compute the average stacks, heights, fillratio, dirtyratio of data set New
	public static void main(String [] arg) throws FileNotFoundException{
		File dir = new File("data/NewwTRUCK");
		String date = new SimpleDateFormat("yyyyMMdd").format(Calendar.getInstance().getTime());
		new File("experiments/"+date).mkdirs();
		File summary = new File("experiments/" + date + "/summary.csv");
		PrintStream sumps = new PrintStream(summary);
		sumps.println("group, case, stack, height, con_num, dir_num");
		for(int groupcount=1;groupcount<=36;groupcount++){
			File groupdir = new File(dir, "New"+groupcount);
			for(int casecount=1; casecount<=30;casecount++){
				File casefile = new File(groupdir, "New"+groupcount+"_"+casecount+".bay");
				Scanner scn = new Scanner(casefile);
				String[] str;
				scn.nextLine();
				str = scn.nextLine().split(" ");//width
				int S = Integer.parseInt(str[2]);
				scn.nextLine();//dummystack
				str = scn.nextLine().split(" ");//tier
				int T = Integer.parseInt(str[2]);
				str = scn.nextLine().split(" ");
				int N = Integer.parseInt(str[2]);

				int Dir = 0;//dirty
				for (int s = 1; s <= S; s++)
				{
					str = scn.nextLine().split(" ");
					if (str.length == 3)
						continue;
					int bottom = Integer.parseInt(str[3]);
					boolean clean = true;
					for(int index=4;index<str.length;index++){
						int cur = Integer.parseInt(str[index]);
						if(clean==false)
							Dir++;					
						else if (bottom>=cur){
							bottom = cur;
						}
						else {
							Dir++;
							clean=false;
						}
					}
				}
				sumps.println(groupcount +"," + casecount + "," + S + "," + T + "," + N + "," + Dir);
			}
		}
		sumps.flush();
		sumps.close();
		
	}
}
