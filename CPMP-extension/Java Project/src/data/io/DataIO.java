package data.io;

import java.io.File;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyb.cpmp.heuristic.ieee.Solver;

public class DataIO {
	static Logger log = LoggerFactory.getLogger(DataIO.class);
	public static Solver read_CVS(File file) throws Exception
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

		Solver solver = new Solver(S, H, P, N, init);
		log.info("after read");
		solver.showBay();
		return solver;
	}
	
	public static void readLine() {
//		try {
//			 Scanner sc =new Scanner(System.in);
//			 sc.nextLine();
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}

}
