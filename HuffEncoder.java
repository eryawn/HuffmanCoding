/* HuffEncoder.java

   Starter code for compressed file encoder. You do not have to use this
   program as a starting point if you don't want to, but your implementation
   must have the same command line interface. Do not modify the HuffFileReader
   or HuffFileWriter classes (provided separately).
   
   B. Bird - 03/19/2019
   Name: Xuhui Wang/Eric Wang ID: V00913734 Date: 07/06/2019
*/

import java.io.*;
import java.util.LinkedList;
import java.util.Map;
import java.util.HashMap;
import java.util.PriorityQueue;

public class HuffEncoder{

    private BufferedInputStream inputFile;
    private HuffFileWriter outputWriter;

    public HuffEncoder(String inputFilename, String outputFilename) throws FileNotFoundException {
        inputFile = new BufferedInputStream(new FileInputStream(inputFilename));
        outputWriter = new HuffFileWriter(outputFilename);
    }
	
    /* encode()
       Takes an arbitrary stream of data (such as a ﬁle) and produces an encoded representation.
	   
	   The running time of this method is O(m + slogs) where m represents the number of
	   bytes in the file, s represents the number of symbols in the file. The detailed
	   justification has been written on the comment of methods below.
    */
    public void encode() throws IOException{
		
		//read every byte in the input file to create a linkedlist
        LinkedList<Byte> contentList = new LinkedList<Byte>();       
        for(int nextByte = inputFile.read(); nextByte != -1; nextByte = inputFile.read()){
            contentList.add((byte)nextByte);
        }
        inputFile.close();
		
		HashMap<Byte,Integer> freqMap = mapFrequency(contentList);		
		HuffNode root = buildHuffTree(freqMap);		
        HashMap<Byte,int[]> encodingMap = outputSymbolsToFile(root);
		outputCodeToFile(contentList, encodingMap);
    }

    /* mapFrequency(LinkedList<Byte> contentList)
       Takes a linkedlist representing every byte to be encoded in the file. Return a hash table
	   that maps every symbol to its corresponding frequency of occurrence in the file.
	   
	   Itereating through every byte in the file with an iterator costs O(m). Accessing a value 
	   from a hash table costs O(1) time. 
	   So the running time of this method is O(m).
    */	
	public HashMap<Byte,Integer> mapFrequency(LinkedList<Byte> contentList){
		
		HashMap<Byte,Integer> freqMap = new HashMap<Byte, Integer>();
		
        //the hash table that maps every symbol to its corresponding frequency of occurrence in the file
		for(byte byteKey: contentList){
            if(freqMap.containsKey(byteKey)){
				Integer byteFrequency = freqMap.get(byteKey);
                freqMap.put(byteKey, byteFrequency + 1);
            }
            else
                freqMap.put(byteKey, 1);
        }
		return freqMap;
	}

    /* buildHuffTree(HashMap<Byte, Integer> freqMap)
       Takes a hash table that maps every symbol in the file to its corresponding frequency 
	   of occurrence in the file. Returns a HuffNode which is the root node of a Huffman
	   tree.
	   
	   In this tree, every leaf node contains a unique symbol and its corresponding 
	   frequency of occurence. other nodes contain nothing.
	   
	   Iterating through the hash table costs O(s). Creating the priority queue using s add
	   operations costs O(slogs). Creating the Huffman tree using s poll operations and s/2
	   add operations costs O(slogs).
	   So the running time of this method is O(s + slogs) = slogs.
    */		
	public HuffNode buildHuffTree(HashMap<Byte, Integer> freqMap){
		
		PriorityQueue<HuffNode> freqQueue = new PriorityQueue<HuffNode>(new HuffComparator());
		
		//iterate every symbol in the hash table to create the priority queue
        for(Map.Entry<Byte,Integer> item : freqMap.entrySet()){
			HuffNode node = new HuffNode(item.getKey(), item.getValue());
			freqQueue.add(node);
		}

		//create the Huffman tree
		HuffNode root = null;
 		while(freqQueue.size() > 1){
			HuffNode x = freqQueue.poll();
			HuffNode y = freqQueue.poll();
		
			root = new HuffNode(x.frequency + y.frequency, x, y);	
			freqQueue.add(root);
		}		
		return root;
	}

    /* outputSymbolsToFile(HuffNode root)
       Takes a HuffNode which is the root node of the Huffman tree. Returns a hash table
	   that maps every symbols in the file to its corresponding encoding bits. And write 
	   the symbol-encoding table into the compressed file.
	   
	   The running time of this method is O(s). The detailed justification has been 
	   written on the comment of the helper method below.
    */	
	public HashMap<Byte,int[]> outputSymbolsToFile(HuffNode root){
		
		HashMap<Byte,int[]> encodingMap = new HashMap<Byte, int[]>();
		LinkedList<Integer> encodingList = new LinkedList<Integer>();
 		encodeSymbols(root, encodingList, encodingMap);
		outputWriter.finalizeSymbols();
		return encodingMap;
	}	

    /* encodeSymbols(HuffNode node, LinkedList<Integer> encodingList, HashMap<Byte,int[]> encodingMap)
       Takes a HuffNode which is the root node of the Huffman tree, a linkedlist for
	   encoding symbols and a hash table that maps every symbol in the file to its
	   corresponding encoding bits.
	   
	   The linkedlist records the entire process of traversing the Huffman tree.
	   
	   Iterating through every leaf node in the Huffman tree costs O(s) because there
	   are at most 2s nodes in the tree. Then getting the whole symbol-encoding table
	   costs O(sk) where k represents the maximum length of encoding bits. Because all 
	   symbols are only one byte in length, the encoding bits length is bounded by a 
	   constant. So getting the whole symbol-encoding table costs O(s) in general.
	   So the running time of this method is O(s).
    */	
	private void encodeSymbols(HuffNode node, LinkedList<Integer> encodingList, HashMap<Byte,int[]> encodingMap){
		
		//has reached a leaf node
		if(node.left == null && node.right == null){

			node.intCode = new int[encodingList.size()];
			int i = 0;
			//get the encoding bits
			for(Integer item : encodingList){
				node.intCode[i] = item;
				i++;
			}
			
			//put this pair into the symbol-encoding table
			encodingMap.put(node.byteSymbol[0], node.intCode);			
			
			//write this symbol-encoding entry into the compressed file
			HuffFileSymbol entry = new HuffFileSymbol(node.byteSymbol, node.intCode);
			outputWriter.writeSymbol(entry);			
		}

		if(node.left != null){
			encodingList.addLast(0);
			encodeSymbols(node.left, encodingList, encodingMap);
			encodingList.removeLast();
		}
		if(node.right != null){
			encodingList.addLast(1);
			encodeSymbols(node.right, encodingList, encodingMap);
			encodingList.removeLast();
		}		
	}
	
    /* outputCodeToFile(LinkedList<Byte> contentList, HashMap<Byte,int[]> encodingMap)
       Takes a linkedlist representing every byte to be encoded in the file and a
	   hash table that maps every symbol in the file to its corresponding encoding bits.
	   
	   This method is to convert every byte in the file to its correponding encoding
	   bits and write the entire encoding bit stream into the compressed file.
	   
	   Retrieving the encoding bits for a symbol from a hash table costs O(1). Reading 
	   all byte in the file and writting the entire encoding bit stream costs O(m).
	   So the running time of this method is O(m).
    */		
	public void outputCodeToFile(LinkedList<Byte> contentList, HashMap<Byte,int[]> encodingMap){
		
		for(byte byteKey: contentList){
			int[] bitStream = encodingMap.get(byteKey);
			for(int bit : bitStream){
				outputWriter.writeStreamBit(bit);
			}
		}
		outputWriter.close();		
	}

    public static void main(String[] args) throws IOException{
        if (args.length != 2){
            System.err.println("Usage: java HuffEncoder <input file> <output file>");
            return;
        }
        String inputFilename = args[0];
        String outputFilename = args[1];

        try{
            HuffEncoder encoder = new HuffEncoder(inputFilename, outputFilename);
            encoder.encode();
        } catch (FileNotFoundException e) {
            System.err.println("FileNotFoundException: "+e.getMessage());
        } catch (IOException e) {
            System.err.println("IOException: "+e.getMessage());
        }
    }
}
