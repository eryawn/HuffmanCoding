/* HuffDecoder.java

   Starter code for compressed file decoder. You do not have to use this
   program as a starting point if you don't want to, but your implementation
   must have the same command line interface. Do not modify the HuffFileReader
   or HuffFileWriter classes (provided separately).
   
   B. Bird - 03/19/2019
   Name: Xuhui Wang/Eric Wang ID: V00913734 Date: 07/06/2019
*/

import java.io.*;

public class HuffDecoder{

    private HuffFileReader inputReader;
    private BufferedOutputStream outputFile;

    /* Basic constructor to open input and output files. */
    public HuffDecoder(String inputFilename, String outputFilename) throws FileNotFoundException {
        inputReader = new HuffFileReader(inputFilename);
        outputFile = new BufferedOutputStream(new FileOutputStream(outputFilename));
		
    }

    /* decode()
       This method is to reads an encoded representation and recovers the original data stream.
	   
	   The running time of this method is O(s + n) where s represents the number of entries 
	   in the symbol table and n represents the number of bits in the bit stream. The detailed
	   justification has been written on the comment of methods below.
    */
    public void decode() throws IOException{
		
		HuffNode root = new HuffNode();		
		HuffFileSymbol entry = inputReader.readSymbol();
		
		//The running time of iterating through the symbol table is O(s).
		//The method rebuildHuffTree costs constant time.
		//So the running time of this code block is O(s).
		while(entry != null){
			rebuildHuffTree(root, entry.symbolBits, entry.symbol);
			entry = inputReader.readSymbol();
		}
		
		//The running time of this line is O(n).
		outputToFile(root);
    }

    /* rebuildHuffTree(HuffNode node, int[] direction, byte[] symbol)
       Takes a HuffNode which is the root node of the Huffman tree, an int array that
	   contains the entire path from the root node to a leaf node where the symbol exists,
	   and a byte array for the value of the symbol.
	   
	   In order to rebuild the exactly correct Huffman tree, we have to create every path 
	   from the root node to every leaf node. So for any leaf node, we have to iterate through 
	   every bit in the encoding bits. Let the average length of symbol' encoding bits be
	   k. We need O(k) time to create a path for a leaf node. However, given that all
	   symbols are only one byte in length, the encoding bit length is bounded by a constant.
	   So the running time of this method is constant time.
    */		
	public void rebuildHuffTree(HuffNode node, int[] direction, byte[] symbol){
		
		for(int i = 0; i < direction.length; i++){
			//create the path
			if(direction[i] == 0){
				if(node.left == null)
					node.left = new HuffNode();
				node = node.left;
			}
			else{
				if(node.right == null)
					node.right = new HuffNode();
				node = node.right;
			}

			//put the symbol into the leaf node if we reach the last bit in encoding bits
			if(i == direction.length - 1){
				node.byteSymbol = symbol;
				node.intCode = direction;				
			}
		}		
	}

    /* outputToFile(HuffNode root)
	   Takes a HuffNode which is the root node of the rebuilt Huffman tree.
       This method is to write the original date into the decompressed file.
	   
	   To decompress, we use the compressed bits as directions in a walk through the tree.
	   Once we read 1 bit from the compressed bit stream, we traverse the rebuilt Huffman 
	   tree deeper by 1 level.
	   At each step, the walk will be positioned at one node, starting at the root. 
	   As each compressed bit is read, the walk moves either to the left child (a 0 bit) 
	   or the right child (a 1 bit). If the walk arrives at a leaf, the symbol in that leaf 
	   is output.
	   After the symbol is output, the walk returns to the root before reading the next 
	   compressed bit. 
	   Each step requires constant time, so the entire process is O(n) on n bits of 
	   compressed data. So the running time of this method is O(n).
	   
    */	
	public void outputToFile(HuffNode root) throws IOException{
		
		int bit = inputReader.readStreamBit();
		HuffNode temp = root;		
		
		//keep reading bits from the compressed bit stream
		while(bit != -1){
			
			if(bit == 0)
				temp = temp.left;
			else
				temp = temp.right;
			
			//output the symbol if the node is a leaf node
			if(temp.left == null && temp.right == null){
				for(byte item : temp.byteSymbol){
					outputFile.write(item);
				}
				temp = root;
			}
			
			bit = inputReader.readStreamBit();
		}
		
		inputReader.close();		
		outputFile.close();
	}

    public static void main(String[] args) throws IOException{
        if (args.length != 2){
            System.err.println("Usage: java HuffDecoder <input file> <output file>");
            return;
        }
        String inputFilename = args[0];
        String outputFilename = args[1];

        try {
            HuffDecoder decoder = new HuffDecoder(inputFilename, outputFilename);
            decoder.decode();
        } catch (FileNotFoundException e) {
            System.err.println("Error: "+e.getMessage());
        }
    }
}
