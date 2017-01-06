import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

@SuppressWarnings("WeakerAccess")
public class MyEngine implements SearchEngine {

    private static final int DEFAULT_SIZE = 5000;

    private int size = 0;
    private int max;

    private TreeMap<String, TreeSet<String>> index = new TreeMap<>();
    private boolean breadthFirst = false;
    private boolean depthFirst = true;

    public MyEngine() {
        this(DEFAULT_SIZE);
    }

    public MyEngine(int theMax) {
        setMax(theMax);
    }

    private HashSet<String> prepareWordCheck() {
        HashSet<String> words = new HashSet<>();
        TreeSet<String> stopwords = new TreeSet<>();
        try (Scanner wordsIn = new Scanner(new File("words.txt"));
             Scanner stopIn = new Scanner(new File("stopwords.txt"))) {
            while (wordsIn.hasNextLine())
                words.add(wordsIn.nextLine());
            while (stopIn.hasNextLine())
                stopwords.add(stopIn.nextLine());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        words.removeAll(stopwords);
        return words;
    }

    public void setMax(int theMax) {
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

    public void crawlFrom(String webAddress) {

        LinkedList<String> toDoQueue = new LinkedList<>();
        TreeSet<String> checked = new TreeSet<>();
        HashSet<String> words = prepareWordCheck();

        TreeSet<String> links;
        WebPageReader w;
        while (this.size() < max) {
            w = new WebPageReader(webAddress);
            checked.add(webAddress);
            w.run();
            w.getLinks().removeAll(checked);
            if (breadthFirst) for (String link : w.getLinks()) toDoQueue.addLast(link);
            else for (String link : w.getLinks()) toDoQueue.addFirst(link);

            for (String word : w.getWords()) {
                if (words.contains(word)) {
                    links = new TreeSet<>();
                    if (index.containsKey(word))
                        links.addAll(index.get(word));
                    links.add(webAddress);
                    index.put(word, links);
                    size++;
                    if (this.size() == max) break;
                    System.out.println(this.size());
                }
            }

            webAddress = toDoQueue.remove();
			System.out.println(webAddress);
        }
        System.out.println("Number of words: " + index.size());
        for (Map.Entry<String, TreeSet<String>> entry : index.entrySet()) {
            String key = entry.getKey();
            TreeSet<String> value = entry.getValue();

            System.out.printf("%s : %s\n", key, value);
        }
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

        System.out.printf("Occurrences of \"%s\":%n", TARGET);
        String[] results = engine.searchHits(TARGET);
        for (String s : results)
            System.out.println(s);
    }
}
