package dataio;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class NewData {
	static int[] S = {6,8,10};//Number of stacks 6,8,10
	static int[] T = {5,8};//Number of tiers 5,8
	
	
	final static int InsN = 30;//Generate 30 instances for each set
	static Random rn = new Random();
	public static void main(String[] args) throws IOException{
		File folder = new File("data/NewwTRUCK");
		folder.mkdir();
		int SetN=1;
		for(int i=0;i<S.length;i++)
			for(int j=0;j<T.length;j++)
				for(int OR=0;OR<2;OR++){
					for (int GR=0; GR<3; GR++){
						Generate(S[i],T[j],OR, GR, InsN, SetN);
						SetN++;
					}
				}
	}
	public static void Generate(final int S, final int T, final int OR, final int GR, final int ITE, final int SetN) throws IOException{
		//S: stack number, index (0~S-1); 
		//T: tier number, index (0~T-1); 
		//OR: 0: 60%-80%; 1: 80%+
		//GR: 0: 100%; 1: 60%-80%; 30%-50%
		//ite: number of instances
		String path = "data/NewwTRUCK/New"+SetN;
		File folder = new File(path);
		folder.mkdir();
		for(int ite=0; ite<ITE; ite++){
			File file = new File(path, "New"+SetN+"_"+(ite+1)+".bay");
			PrintWriter pw = new PrintWriter(new FileWriter(file,true));
			int cnubound = 0;//upper bound of container number
			int cnlbound = 0;//lower bound of container number
			switch (OR){
			case 0:
				cnlbound = (int) (0.6* S * T);
				cnubound = (int) (0.8* S * T);
				break;
			case 1:
				cnlbound = (int) (0.8* S * T);
				cnubound = (int) (S-1) * T;
			}
			int N=rn.nextInt(cnubound-cnlbound+1)+cnlbound;//Number of containers;
			
			int gnubound = 0;//upper bound of container number
			int gnlbound = 0;//lower bound of container number
			switch (GR){
			case 0:
				gnlbound = N;
				gnubound = N;
				break;
			case 1:
				gnlbound = (int) (0.6 * N);
				gnubound = (int) (0.8 * N);
				break;
			case 2:
				gnlbound = (int) (0.3 * N);
				gnubound = (int) (0.5 * N);
				break;
			}
			int G = rn.nextInt(gnubound-gnlbound+1)+gnlbound;
			ArrayList <Integer> containers = new ArrayList<Integer>();
			for(int i=1; i<=Math.min(G,N); i++){
				containers.add(i);
			}
			for(int i=G+1;i<=N;i++){
				containers.add(rn.nextInt(G)+1);
			}
			Collections.shuffle(containers);//One permutation
			int [][]yard = new int[T][S];//Yard
			int height [] = new int [S];//height of each stack
			ArrayList <Integer> ava_stack = new ArrayList<Integer>();//available stacks
			for(int i=0;i<S-1; i++)
				ava_stack.add(i);
			for(int i=0;i<N;i++){
				//assign containers to yard
				int c = containers.get(i);
				int sindex = rn.nextInt(ava_stack.size());//generate random number [0,s)
				int stack = ava_stack.get(sindex);
				int tier = height[stack];
				yard[tier][stack] = c;
				height[stack]++;
				if(height[stack]==T)
					ava_stack.remove(sindex);
			}
			//Output this instance
			pw.println("Label : New"+SetN+"_"+(ite+1));
			pw.println("Width : "+S);
			pw.println("Truck : "+ S);
			pw.println("Height : " + T);
			pw.println("Containers : " + N);
			
			for(int j=0;j<S;j++){
				pw.print("Stack " + (j+1) + " : ");
				for(int i=0;i<height[j];i++)
					pw.print(yard[i][j]+" ");
				pw.println();
			}
			pw.flush();
			pw.close();
		}

	}
}
