//   Name: Xuhui Wang/Eric Wang ID: V00913734 Date: 07/06/2019

public class HuffNode{

	int frequency;	
	byte[] byteSymbol;
	int[] intCode;		
	HuffNode left;
	HuffNode right;
	
	public HuffNode(){
	}
	
	public HuffNode(int freq, HuffNode leftNode, HuffNode rightNode){
		frequency = freq;
		left = leftNode;
		right = rightNode;
	}
	
	public HuffNode(byte symbol, int freq){
		byteSymbol = new byte[]{symbol};
		frequency = freq;
	}	
}