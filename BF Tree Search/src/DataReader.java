import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

/**
 * Created by jinbo on 8/31/14.
 */
public class DataReader
{
	public static Layout read(String file) throws FileNotFoundException
	{
		Scanner scn = new Scanner(new File(file));
		scn.nextLine();
		int S = Integer.parseInt(scn.nextLine().split(" : ")[1].trim());
		int H = Integer.parseInt(scn.nextLine().split(" : ")[1].trim());
		int N = Integer.parseInt(scn.nextLine().split(" : ")[1].trim());
		int G = 0;
		int[][] mat = new int[S + 1][H + 1];
		for (int i = 1; i <= S; i++)
		{
			String[] ss = scn.nextLine().split(" : ");
			if (ss.length == 1)
				continue;
			String[] x = ss[1].split(" ");
			for (int j = 1; j <= x.length; j++)
			{
				mat[i][j] = Integer.parseInt(x[j - 1]);
				G = Math.max(G, mat[i][j]);
			}
		}
		scn.close();
		return new Layout(S, H, N, G, mat);
	}
}
