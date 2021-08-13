//   Name: Xuhui Wang/Eric Wang ID: V00913734 Date: 07/06/2019
import java.util.Comparator;

public class HuffComparator implements Comparator<HuffNode>{
	
	public int compare(HuffNode x, HuffNode y){
		return x.frequency - y.frequency;
	}
}