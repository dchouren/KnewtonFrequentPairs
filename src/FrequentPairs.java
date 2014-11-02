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
	private static final int NUM_THRESHOLD = 50;
	
//	private Map<Integer, String> intArtistMap = new HashMap<Integer, String>();
	
	private Map<String, Integer> artistIntMap = new HashMap<String, Integer>();
	private Map<String, Integer> artistTotalMap = 
			new HashMap<String, Integer>(); 
	
	private Map<Integer, String> totalArtistMap =
			new HashMap<Integer, String>();
	private Map<Integer, String> sortedTotalsMap = 
			new TreeMap<Integer, String>(Collections.reverseOrder());

	private List<Integer> sortedTotals = new ArrayList<Integer>();
	private List<String> sortedArtists = new ArrayList<String>();
	
	private double[][] pairCounts;
	private int numLists;
	
	private int numPairs;
	
	private List<String> artistList = new ArrayList<String>(); //1-indexed list of artists
	
	public FrequentPairs()
	{
		
	}
	
	private void initialize(String file)
	{
		readLists(file);
		int n = artistTotalMap.size();
		pairCounts = new double[n][n];
		
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

			while (line != null) {
				numLists++;
				
				// check for duplicates
				ArrayList<String> artistsInList = new ArrayList<String>();
				
				String[] artists = line.split(",");
				
				for (String artist : artists) {
					if (!this.artistTotalMap.containsKey(artist)) {
//						intArtistMap.put(index, artist);
						artistList.add(index, artist);
						artistIntMap.put(artist, index);
						index++;
						
						artistTotalMap.put(artist, 1);
					}
					else {
						if (!artistsInList.contains(artist)) {
							artistTotalMap.put(artist, 
									artistTotalMap.get(artist) + 1);
							artistsInList.add(artist);
						}
					}

				}
				
				line = br.readLine();
			}
		}
		catch (IOException e) {
			System.out.println(e.getMessage());
		}
		
		this.numLists = numLists;
		
		totalArtistMap = reverseMap(artistTotalMap);
		sortedTotalsMap.putAll(totalArtistMap);
		
		for (Map.Entry<Integer, String> entry : sortedTotalsMap.entrySet()) {
			String artist = entry.getValue();
			Integer count = entry.getKey();

			sortedTotals.add(count);
//			System.out.println(count);
			sortedArtists.add(artist);
		}
		
		
	}
	
	/**
	 * Return a map with key and values reversed.
	 * @param map
	 * @return
	 */
	private static Map<Integer, String> reverseMap(Map<String, Integer> map) 
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
	 * Get indices for a set of artists.
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
	
	private void countPairs(String file)
	{
		try(BufferedReader br = new BufferedReader(new FileReader(file))) {

			String line = br.readLine();
			int index = 0;

			while (line != null) {
				
				String[] artists = line.split(",");
				
				ArrayList<Integer> indices = filterIndices(artists);
				
				for (int i = 0; i < indices.size() - 1; i++) {
					for (int j = i + 1; j < indices.size(); j++) {
						
						int firstIndex = indices.get(i);
						int secondIndex = indices.get(j);
						
						// make sure we always increment the same pair 
						// eg always increment (3,5) and never (5,3)
						if (firstIndex >= secondIndex) {
							pairCounts[firstIndex][secondIndex]++;
						}
						else {
							pairCounts[secondIndex][firstIndex]++;
						}
					}
				}
				
				line = br.readLine();
			}
		}
		catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}
	
	private void formPairsSmart() throws FileNotFoundException 
	{
		/*for (int i = 0; i < sortedTotals.size(); i++) {
			System.out.println(sortedTotals.get(i));
		}*/
		PrintWriter writer = new PrintWriter(new File("smartPairs.txt"));
		
		for (int i = 0; i < sortedTotals.size() - 1; i++) {
			
//			System.out.println(sortedTotals.get(i));
			for (int j = i + 1; j < sortedTotals.size(); j++) {
				double percentFirst = sortedTotals.get(i) / (double) numLists;
				double percentSecond = sortedTotals.get(j) / (double) numLists;
				
//				System.out.println(percentFirst * percentSecond * numLists);
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
				
				if (percentFirst * percentSecond * numLists >= NUM_THRESHOLD * 4/5 
						|| (percentFirst * percentSecond * numLists >= NUM_THRESHOLD /2
							&& smaller > larger * .6)) {
					String artist1 = sortedArtists.get(i);
					String artist2 = sortedArtists.get(j);
					System.out.println(artist1 + "," + artist2);
					writer.println(artist1 + "," + artist2);
					
				}
				/*else {
					if (j == sortedTotals.size() - 1) {
						return;
					}
					continue;
				}*/
			}
		}
		writer.close();
	}
	
	private void formPairs()
	{
		int count = 0;
		
		for (int i = 0; i < pairCounts.length; i++) {
			for (int j = 0; j <= i; j++) {
				if (pairCounts[i][j] >= NUM_THRESHOLD) {
					count++;
					String artist1 = artistList.get(i);
					String artist2 = artistList.get(j);
//					String artist1 = intArtistMap.get(i);
//					String artist2 = intArtistMap.get(j);
					System.out.println(artist1 + "," + artist2);
				}
			}
		}
		
		numPairs = count;
	}
	
	
	private void checkPairs(String file)
	{
		for (int i = 0; i < pairCounts.length; i++) {
			for (int j = 0; j < pairCounts.length; j++) {
				if (pairCounts[i][j] >= NUM_THRESHOLD) {
//					String artist1 = intArtistMap.get(i);
//					String artist2 = intArtistMap.get(j);
					String artist1 = artistList.get(i);
					String artist2 = artistList.get(j);
					

					int count = 0;
					
					try(BufferedReader br = new BufferedReader(new FileReader(file))) {

						String line = br.readLine();

						while (line != null) {
							
							ArrayList<String> artists = 
									new ArrayList<String>(Arrays.asList(line.split(",")));
							
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
						System.out.println("wrong for " + artist1 + ", " + artist2);
					}
				}
			}
		}
		System.out.println("no false pairs");
	}
	
	
	private void printCounts() 
	{
		DecimalFormat df = new DecimalFormat("#.00");
		for (int i = 0; i < pairCounts.length; i++) {
			for (int j = 0; j < pairCounts.length; j++) {
				if (pairCounts[i][j] >= NUM_THRESHOLD) {
//					String artist1 = intArtistMap.get(i);
//					String artist2 = intArtistMap.get(j);
					String artist1 = artistList.get(i);
					String artist2 = artistList.get(j);
					
					int count1 = artistTotalMap.get(artist1);
					int count2 = artistTotalMap.get(artist2);
					double expected = count1 / 1000.0 * count2;
					
					if (count1 < count2) 
						System.out.println(count1);
					else 
						System.out.println(count2);
					
//					System.out.println(artist1 + "," + artist2 + ": " + count1 
//							+ ", " + count2 + "; " + pairCounts[i][j] + " | " + df.format(expected));
					
				}
			}
		}
					
	}
	
	private void checkPairsSmart(String file) 
	{			
		int countRight = 0;
		int countWrong = 0;

		try(BufferedReader br = new BufferedReader(new FileReader(file))) {

			String line = br.readLine();
			

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
	
	public static void main(String[] args) throws FileNotFoundException
	{
		FrequentPairs FP = new FrequentPairs();
		FP.initialize("Artist_lists_small.txt");
		
		FP.formPairs();
		
		System.out.println();

//		FP.formPairsSmart();
		
//		FP.printCounts();
		
//		System.out.println();
		
//		FP.checkPairsSmart("smartPairs.txt");
		
//		System.out.println();
//		System.out.println(FP.totalArtistMap.get(129));
//		System.out.println(FP.totalArtistMap.get(125));
//		System.out.println(FP.artistTotalMap.get("Nirvana"));
//		System.out.println();
//		FP.checkPairs("Artist_lists_small.txt");
	}
}








