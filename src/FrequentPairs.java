import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;


public class FrequentPairs
{
	// how many lists we need artists to co-occur in
	private static final int NUM_THRESHOLD = 50;
	
	//1-indexed list of artists
	private List<String> artistList = new ArrayList<String>(); 
	// track artist pair counts
	private double[][] pairCounts; 
	// map artists to their index in pairCounts
	private Map<String, Integer> artistIntMap = new HashMap<String, Integer>();
	// map artists to their total 
	private Map<String, Integer> artistTotalMap = 
			new HashMap<String, Integer>(); 
	
	
	/**
	 *  Structures used for my "smart" approach.
	 */
	private int numLists;
	// map artist list appearances to artists
	private Map<Integer, String> totalArtistMap =
			new HashMap<Integer, String>();
	// store artists in descending total order
	private Map<Integer, String> sortedTotalsMap = 
			new TreeMap<Integer, String>(Collections.reverseOrder());

	private List<Integer> sortedTotals = new ArrayList<Integer>();
	private List<String> sortedArtists = new ArrayList<String>();
	

	private int numPairs;
	
	
	public FrequentPairs()
	{
		
	}
	
	private void initialize(String file)
	{
		readLists(file);
		int numArtists = artistTotalMap.size();
		pairCounts = new double[numArtists][numArtists];
		
		countPairs(file);
	}
	
	/**
	 * Read lists of artists, count how many lists each artist appears in.
	 * @param file
	 */
	private void readLists(String file)
	{
		int numLists = 0;
		
		artistList.add("0-index filler");
		
		try(BufferedReader br = new BufferedReader(new FileReader(file))) {

			String line = br.readLine();
			int index = 0;

			// loop through lists
			while (line != null) {
				numLists++;
				
				String[] artists = line.split(",");
				
				for (String artist : artists) {
					if (!this.artistTotalMap.containsKey(artist)) {
						artistList.add(index, artist);
						artistIntMap.put(artist, index);
						index++;
						
						artistTotalMap.put(artist, 1);
					}
					else {
							artistTotalMap.put(artist, 
									artistTotalMap.get(artist) + 1);
					}
				}
				
				line = br.readLine();
			}
		}
		catch (IOException e) {
			System.out.println(e.getMessage());
		}
		
		this.numLists = numLists;
		
		/**
		 * Modifications for smarter solution attempt
		 */
		totalArtistMap = flipMap(artistTotalMap);
		sortedTotalsMap.putAll(totalArtistMap);
		
		for (Map.Entry<Integer, String> entry : sortedTotalsMap.entrySet()) {
			String artist = entry.getValue();
			Integer count = entry.getKey();

			sortedTotals.add(count);
			sortedArtists.add(artist);
		}
		/**
		 * End modifications
		 */
	}
	
	/**
	 * Helper function for readLists. Return a map with key and values flipped.
	 * @param map
	 * @return
	 */
	private static Map<Integer, String> flipMap(Map<String, Integer> map) 
	{
		Map<Integer, String> reversedMap = new HashMap<Integer, String>();
		
		for (Map.Entry<String, Integer> entry : map.entrySet()) {
			String artist = entry.getKey();
			Integer count = entry.getValue();

			reversedMap.put(count, artist);
		}
		
		return reversedMap;
	}
	
	/**
	 * Get indices of artists that occur in at least NUM_THRESHOLD lists from
	 * a set of artists.
	 * @param artists
	 * @return
	 */
	private ArrayList<Integer> filterIndices(String[] artists)
	{
		ArrayList<Integer> indices = new ArrayList<Integer>();
		
		for (String artist : artists) {
			int count = artistTotalMap.get(artist);
			if (count >= NUM_THRESHOLD) {
				int index = artistIntMap.get(artist);
				indices.add(index);
			}
		}
		
		return indices;
	}
	
	/**
	 * Count how many times each pair of artists co-occurs.
	 * @param file
	 */
	private void countPairs(String file)
	{
		try(BufferedReader br = new BufferedReader(new FileReader(file))) {
			String line = br.readLine();

			while (line != null) {
				
				String[] artists = line.split(",");
				
				ArrayList<Integer> indices = filterIndices(artists);
				
				// generate each pair
				for (int i = 0; i < indices.size() - 1; i++) {
					for (int j = i + 1; j < indices.size(); j++) {
						
						int firstIndex = indices.get(i);
						int secondIndex = indices.get(j);
						
						int larger = -1;
						int smaller = -1;
						
						
						// make sure we always increment the same pair 
						// eg always increment (5,3) and never (3,5)
						if (firstIndex >= secondIndex) {
							larger = firstIndex;
							smaller = secondIndex;
						}
						else {
							larger = secondIndex;
							smaller = firstIndex;
						}
						
						if (pairCounts[larger][smaller] == -1) {
							continue;
						}
						
						pairCounts[larger][smaller]++;
							
						// pair occurs frequetly, print it out
						if (pairCounts[larger][smaller] >= NUM_THRESHOLD) {
							String artist1 = artistList.get(firstIndex);
							String artist2 = artistList.get(secondIndex);
							System.out.println(artist1 + "," + artist2);
							
							pairCounts[larger][smaller] = -1;
						}
						
						
						/*if (firstIndex >= secondIndex) {
							pairCounts[firstIndex][secondIndex]++;
						}
						else {
							pairCounts[secondIndex][firstIndex]++;
						}*/
						
					}
				}
				
				line = br.readLine();
			}
		}
		catch (IOException e) {
			System.out.println(e.getMessage());
		}
	} 
	
	/**
	 * Check that pairs marked as frequent in pairCounts are actually frequent.
	 * @param file
	 */
	private void checkPairs(String file)
	{
		for (int i = 0; i < pairCounts.length; i++) {
			for (int j = 0; j < pairCounts.length; j++) {
				if (pairCounts[i][j] >= NUM_THRESHOLD) {
					String artist1 = artistList.get(i);
					String artist2 = artistList.get(j);
					

					int count = 0;
					
					// loop through file to manually check list occurances
					try(BufferedReader br = new BufferedReader(
												new FileReader(file))) 
					{
						String line = br.readLine();

						while (line != null) {
							ArrayList<String> artists = new ArrayList<String>(
											Arrays.asList(line.split(",")));
							
							if (artists.contains(artist1)) {
								if (artists.contains(artist2)) {
									count++;
								}
							}
							
							if (count >= NUM_THRESHOLD)
								break;
							
							line = br.readLine();
						}
					}
					catch (IOException e) {
						System.out.println(e);
					}
					
					if (count < NUM_THRESHOLD) {
						System.out.println("wrong for " + artist1 + ", " 
								+ artist2);
					}
				}
			}
		}
		System.out.println("no false pairs");
	}

	
	/**
	 * Print pairs of artists that co-occur in lists with a high probability.
	 * Write pairs to a file so we can check our results.
	 * @param file
	 * @throws FileNotFoundException
	 */
	private void countPairsSmart(String file) throws FileNotFoundException 
	{
		PrintWriter writer = new PrintWriter(new File(file));
		
		for (int i = 0; i < sortedTotals.size() - 1; i++) {
			for (int j = i + 1; j < sortedTotals.size(); j++) {
				int numFirst = sortedTotals.get(i);
				int numSecond = sortedTotals.get(j);
				
				// break early when we have fewer than 50 possible lists
				if (numSecond < NUM_THRESHOLD) {
					return;
				}
				
				double percentFirst = numFirst / (double) numLists;
				double percentSecond = numSecond / (double) numLists;
				
				double smaller = 0;
				double larger = 0;
				
				if (percentFirst < percentSecond) {
					smaller = percentFirst;
					larger = percentSecond;
				}
				else {
					smaller = percentSecond;
					larger = percentFirst;
				}
				
				/**
				 * META: I attempted various models here, but none returned
				 * satisfactory results.
				 */
				/*if (percentFirst * percentSecond * numLists 
				 * 			>= NUM_THRESHOLD * 4/5 
						|| (percentFirst * percentSecond * numLists
						 	>= NUM_THRESHOLD /2
								&& smaller > larger * .6)) {*/
				if (percentFirst * percentSecond * numLists 
						> NUM_THRESHOLD / 2) 
				{
					String artist1 = sortedArtists.get(i);
					String artist2 = sortedArtists.get(j);
					System.out.println(artist1 + "," + artist2);
					writer.println(artist1 + "," + artist2);
				}
				
			}
		}
		writer.close();
	}
	
	/**
	 * Checks for false pairs and missed pairs printed to a file by the "smart"
	 * method.
	 * @param file
	 */
	private void checkPairsSmart(String file) 
	{			
		int countRight = 0;
		int countWrong = 0;

		try(BufferedReader br = new BufferedReader(new FileReader(file))) {

			String line = br.readLine();
			
			// loop through file
			while (line != null) {
				
				String[] artists = line.split(",");
				
				int index1 = artistIntMap.get(artists[0]);
				int index2 = artistIntMap.get(artists[1]);
				
				double total = Math.max(pairCounts[index1][index2],
						pairCounts[index2][index1]);
				
				if (total < NUM_THRESHOLD) {
					countWrong++;
				}
				else {
					countRight++;
				}
				
				line = br.readLine();
			}
		}
		catch (IOException e) {
			System.out.println(e);
		}
		
		System.out.println(countWrong + " false pairs");
		System.out.println(numPairs - countRight + " missed pairs");
	}
	
	
	/**
	 * Testing function which prints information about artist frequencies.
	 */
	private void printCounts() 
	{
		DecimalFormat df = new DecimalFormat("#.00");
		for (int i = 0; i < pairCounts.length; i++) {
			for (int j = 0; j < pairCounts.length; j++) {
				if (pairCounts[i][j] >= NUM_THRESHOLD) {
					String artist1 = artistList.get(i);
					String artist2 = artistList.get(j);
					
					int count1 = artistTotalMap.get(artist1);
					int count2 = artistTotalMap.get(artist2);
					double expected = count1 / 1000.0 * count2;
					
					if (count1 < count2) 
						System.out.println(count1);
					else 
						System.out.println(count2);
				}
			}
		}
	}
	
	
	public static void main(String[] args) throws FileNotFoundException
	{
		FrequentPairs FP = new FrequentPairs();
		FP.initialize("Artist_lists_small.txt");
		
//		FP.checkPairs("Artist_lists_small.txt");

//		System.out.println();

		/**
		 * META: My attempt at a smarter solution using frequency counts.
		 */
//		FP.countPairsSmart("smartPairs.txt");		
//		FP.checkPairsSmart("smartPairs.txt");
	}
}








