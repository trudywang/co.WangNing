package runner;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Scanner;

public class SummaryResult {
//Read the results of single files, and summarize the results into one file.
	static public void main(String[]args) throws IOException{
		File dir=new File("experiments/BS2result/BF/record");
		String[] filelist = dir.list();
		File summary = new File("experiments/BS2result/BF/first5.csv");
		PrintStream sumps = new PrintStream(summary);
		sumps.println("group,width,depth,avesol,avetime");
		
		for(int i=0; i<filelist.length; i++){
			String filename = filelist[i];
			String[] str =filename.split(" ");
			int width= Integer.parseInt(str[1].substring(1));
			int depth = Integer.parseInt(str[2].substring(1));
			File file = new File("experiments/BS2result/BF/record/"+filename);
			Scanner scn = new Scanner(file);
			scn.nextLine();//title row
			int count=0;
			
			for(int group=1; group<=32; group++){
				int sumsol = 0;
				double sumtime = 0;
				for(int ins=1; ins<=5; ins++){
					count++;
					String record = scn.nextLine();
					String[]part = record.split(",");
//						if(Integer.parseInt(part[0])!=group||Integer.parseInt(part[1])!=ins||Integer.parseInt(part[6])!=width){
//							System.out.println("error");
//							return;
//						}
					sumsol+=Integer.parseInt(part[4]);
					sumtime+=Double.parseDouble(part[5]);
				}
					sumps.println(group+","+width+","+depth+","+1.0*sumsol/5+","+sumtime/5);
					for(int ins=6; ins<=20; ins++)
						scn.nextLine();
					
				}
			sumps.flush();
			scn.close();
		}
		
		sumps.close();
	}
}
