package cpmp;

public class Constant
{
	public static final int MAX_RELOCATION_NUMBER = 10000;
	public static final int FLOOR_PRIORITYLABEL = 500;
	public static final int FewerAbove = 0;
	public static final int FewerAbove_LowerTier = 1;
	public static final int FewerAbove_LowerTier_LargerSum = 2;
	public static final int FewerAbove_LargerSum = 3;
	public static final Container FLOOR=new Container(FLOOR_PRIORITYLABEL,-1); 
}