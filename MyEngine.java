import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.TreeSet;

public class MyEngine implements SearchEngine {

	private static final int DEFAULT_SIZE = 5000;

	private int size = 0;
	private int max;

	// new fields
	private HashSet<String> words = new HashSet<String>();;
	private TreeMap<String, TreeSet<String>> index = new TreeMap<String, TreeSet<String>>();
	private TreeSet<String> links;
	private TreeSet<String> checked = new TreeSet<String>();
	private LinkedList<String> toDoQueue = new LinkedList<String>();
	private WebPageReader w;
	private boolean breadthFirst = false;
	private boolean depthFirst = true;
	
	public MyEngine() { // DONE
		this(DEFAULT_SIZE);
	}

	public MyEngine(int theMax) { // DONE
		setMax(theMax);
		prepereWordCheck();
	}

	private void prepereWordCheck() {
		Scanner wordsIn = null;
		Scanner stopIn = null;
		TreeSet<String> stopwords = new TreeSet<String>();
		try {
			wordsIn = new Scanner(new File("words.txt"));
			stopIn = new Scanner(new File("stopwords.txt"));
			while (wordsIn.hasNextLine())
				words.add(wordsIn.nextLine());
			while (stopIn.hasNextLine())
				stopwords.add(stopIn.nextLine());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			wordsIn.close();
			stopIn.close();
		}
		words.removeAll(stopwords);
		stopwords = null;
	}

	public void setMax(int theMax) { // DONE
		max = theMax;
	}

	public boolean setBreadthFirst() { 
		if (depthFirst) {
			breadthFirst = true;
			depthFirst = false;
		}
		return breadthFirst;
	}

	public boolean setDepthFirst() { 
		if (breadthFirst) {
			breadthFirst = false;
			depthFirst = true;
		}
		return depthFirst;
	}

	public void crawlFrom(String webAdress) { 
		
		while (this.size() < max) {
			w = new WebPageReader(webAdress);
			checked.add(webAdress);
			w.run();
			w.getLinks().removeAll(checked);
			if (breadthFirst) for(String link : w.getLinks()) toDoQueue.addLast(link);
			else for(String link : w.getLinks()) toDoQueue.addFirst(link);
			
			for (String word : w.getWords()) {
				if (words.contains(word)) {
					links = new TreeSet<String>();
					if(index.containsKey(word))
					links.addAll(index.get(word));
					links.add(webAdress);
					index.put(word, links);
					size++;
					if (this.size() == max) break;
					links = null;
					System.out.println(this.size());
				}
			}
			
			webAdress = toDoQueue.remove();
//			System.out.println(webAdress);
		} 
//				System.out.println("Number of words: " + index.size());
				
		toDoQueue = null;
		checked = null;
		links = null;
		words = null;
		w = null;
		System.gc();
	}

	public String[] searchHits(String target) { 
		String[] out;
		if (index.containsKey(target)) {
			out = new String[index.get(target).size()];
			index.get(target).toArray(out);
			return out;
		} else {
			out = new String[1];
			out[0] = "no results";
			return out;
		}
	}

	public int size() { // DONE
		return size;
	}

	/*
	 * Simple test code
	 */
	public static void main(String[] args) {
		String AFTEN = "https://en.wikipedia.org/wiki/Main_Page";
		String TARGET = "accent";

		MyEngine engine = new MyEngine();
		System.out.print("Searching, start....");
		engine.crawlFrom(AFTEN);
		System.out.printf("finish. Size of index = %d%n", engine.size());

		System.out.printf("Occurences of \"%s\":%n", TARGET);
		String[] results = engine.searchHits(TARGET);
		for (String s : results)
			System.out.println(s);
	}
}
