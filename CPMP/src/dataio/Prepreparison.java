package dataio;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;

public class Prepreparison {

	public static void main(String [] args) throws IOException{
		addterm2();
	}
	
//	public static void rename() throws IOException{
//      //Rename BF dataset
//		String oldpath = "D:/Study/Pre Marshalling/data/BF";
//		String newpath = "D:/Study/Pre Marshalling/data/new_BF";
//		File main_folder = new File(oldpath);
//		File new_main_folder = new File(newpath);
//		new_main_folder.mkdir();
//		String[] foldernames = main_folder.list();
//		for(String foldername:foldernames){
//			File folder = new File(oldpath, foldername);
//			File new_folder = new File(newpath, foldername);
//			new_folder.mkdir();
//			String [] filenames = folder.list();
//			File file = null;
//			for(int i=0;i<filenames.length;i++){
//				file = new File(folder, filenames[i]);
//				File newfile = new File(new_folder,foldername+"_"+(i+1)+".bay");
//				Scanner in = new Scanner(file);
//				PrintWriter pw = new PrintWriter(new FileWriter(newfile,true));
//				in.nextLine();
//				pw.println("Label : "+ foldername+"_"+(i+1));
//				while(in.hasNextLine())
//					pw.println(in.nextLine());
//				pw.flush();
//				pw.close();
//			}			
//		}
//	}
		public static void rename2() throws IOException{
			//Rename VCS dataset
			String oldpath = "D:/Study/Pre Marshalling/data/CVS";
			File main_folder = new File(oldpath);
			String[] foldernames = main_folder.list();
			for(String foldername:foldernames){
				File folder = new File(oldpath, foldername);
				String [] filenames = folder.list();
				File file = null;
				for(String filename:filenames){
					file = new File(folder, filename);
					String filenumber = filename.substring(0,2);
					int number = Integer.parseInt(filenumber);
					
					file.renameTo(new File(folder,foldername+"_"+number+".bay"));
				}			
			}
	}
		
		
		public static void rename3() throws IOException{
			//Change the height in CVS to h+2
		String oldpath = "D:/Study/Pre Marshalling/data/CVS";
		String newpath = "D:/Study/Pre Marshalling/data/HCVS";
		File new_main_folder = new File(newpath);
		new_main_folder.mkdir();

		for(int i=1;i<22;i++){
			File folder = new File(oldpath, "CVS"+i);
			File new_folder = new File(newpath, "HCVS"+i);
			new_folder.mkdir();
			File file = null;
			for(int j=1;j<41;j++){
				file = new File(folder, "CVS"+i+"_"+j+".bay");
				File newfile = new File(new_folder,"HCVS"+i+"_"+j+".bay");
				Scanner in = new Scanner(file);
				PrintWriter pw = new PrintWriter(new FileWriter(newfile,true));
				pw.println(in.nextLine());
				String str = in.nextLine();
				String string[] = str.split(" : ");
				int width = Integer.parseInt(string[1]);
				pw.println(str);
				
				str = in.nextLine();
				string = str.split(" : ");
				int height = Integer.parseInt(string[1]);
				pw.println("Height : "+(height/width+2));
				while(in.hasNextLine())
					pw.println(in.nextLine());
				pw.flush();
				pw.close();
			}			
		}
	}
		public static void rename4() throws IOException{
			//Change the height in CVS to w+2
		String oldpath = "D:/Study/Pre Marshalling/data/CVS";
		String newpath = "D:/Study/Pre Marshalling/data/WCVS";
		File new_main_folder = new File(newpath);
		new_main_folder.mkdir();

		for(int i=1;i<22;i++){
			File folder = new File(oldpath, "CVS"+i);
			File new_folder = new File(newpath, "HCVS"+i);
			new_folder.mkdir();
			File file = null;
			for(int j=1;j<41;j++){
				file = new File(folder, "CVS"+i+"_"+j+".bay");
				File newfile = new File(new_folder,"HCVS"+i+"_"+j+".bay");
				Scanner in = new Scanner(file);
				PrintWriter pw = new PrintWriter(new FileWriter(newfile,true));
				pw.println(in.nextLine());
				String str = in.nextLine();
				String string[] = str.split(" : ");
				int width = Integer.parseInt(string[1]);
				pw.println(str);
				
				str = in.nextLine();
				string = str.split(" : ");
				int height = Integer.parseInt(string[1]);
				pw.println("Height : "+(height/width+2));
				while(in.hasNextLine())
					pw.println(in.nextLine());
				pw.flush();
				pw.close();
			}			
		}
	}
		
		public static void addterm1() throws IOException{
			//Add a line "Truck:-1" to each instance of HCVS
		String oldpath = "D:/Study/Pre Marshalling/data/HCVS";
		String newpath = "D:/Study/Pre Marshalling/data/HCVSwTRUCK";
		File new_main_folder = new File(newpath);
		new_main_folder.mkdir();

		for(int i=1;i<22;i++){
			File folder = new File(oldpath, "HCVS"+i);
			File new_folder = new File(newpath, "HCVS"+i);
			new_folder.mkdir();
			File file = null;
			for(int j=1;j<41;j++){
				file = new File(folder, "HCVS"+i+"_"+j+".bay");
				File newfile = new File(new_folder,"HCVS"+i+"_"+j+".bay");
				Scanner in = new Scanner(file);
				PrintWriter pw = new PrintWriter(new FileWriter(newfile,true));
				pw.println(in.nextLine());//Reprint label
				pw.println(in.nextLine());//Reprint width
				pw.println("Truck : -1");
				while(in.hasNextLine())
					pw.println(in.nextLine());
				pw.flush();
				pw.close();
			}			
		}
	}
		public static void addterm2() throws IOException{
			//Add a line "Truck:-1" to each instance of BF
		String oldpath = "D:/Study/Pre Marshalling/data/BF";
		String newpath = "D:/Study/Pre Marshalling/data/BFwTRUCK";
		File new_main_folder = new File(newpath);
		new_main_folder.mkdir();

		for(int i=1;i<33;i++){
			File folder = new File(oldpath, "BF"+i);
			File new_folder = new File(newpath, "BF"+i);
			new_folder.mkdir();
			File file = null;
			for(int j=1;j<21;j++){
				file = new File(folder, "BF"+i+"_"+j+".bay");
				File newfile = new File(new_folder,"BF"+i+"_"+j+".bay");
				Scanner in = new Scanner(file);
				PrintWriter pw = new PrintWriter(new FileWriter(newfile,true));
				pw.println(in.nextLine());//Reprint label
				pw.println(in.nextLine());//Reprint width
				pw.println("Truck : -1");
				while(in.hasNextLine())
					pw.println(in.nextLine());
				pw.flush();
				pw.close();
			}			
		}
	}
		
}
