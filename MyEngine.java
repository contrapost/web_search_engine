import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class MyEngine implements SearchEngine {

    private static final int DEFAULT_SIZE = 5000;

    private int size = 0;
    private int max;
    private HashSet<String> dictionary;
    static HashMap<String, HashSet<String>> index;
    private boolean breadthFirst = true;
    private LinkedList<String> linksToCheck;
    private HashSet<String> visitedLinks;

    MyEngine(){ // DONE
        this(DEFAULT_SIZE);
    }

    private MyEngine(int theMax) { // DONE
        setMax(theMax);
        dictionary = buildDictionary();
        index = new HashMap<>();
        linksToCheck = new LinkedList<>();
        visitedLinks = new HashSet<>();
    }

    private HashSet<String> buildDictionary() {
        HashSet<String> dictionary = new HashSet<>();
        try (Scanner words = new Scanner(new File("words.txt"));
            Scanner stopwords = new Scanner(new File("stopwords.txt"))) {
            while (words.hasNextLine()) dictionary.add(words.nextLine());
            while (stopwords.hasNextLine()) dictionary.remove(stopwords.nextLine());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return dictionary;
    }

    public void setMax(int theMax){ // DONE
        max = theMax;
    }

    public boolean setBreadthFirst(){ // DONE
        if(!breadthFirst) breadthFirst = true;
        return true;
    }

    public boolean setDepthFirst(){ // DONE
        if(breadthFirst) breadthFirst = false;
        return true;
    }

    public void crawlFrom(String webAddress){ // DONE

        WebPageReader wpr = new WebPageReader(webAddress);
        visitedLinks.add(webAddress);
        wpr.run();

        addNewWords(wpr.getWords(), webAddress);

        addNewLinks(wpr.getLinks());

        if(linksToCheck.size() == 0 || size() == max) return;

        crawlFrom(linksToCheck.remove());
    }

    private void addNewLinks(Set<String> links) {
        for(String l : links) {
            if(visitedLinks.contains(l)) continue;

            if(breadthFirst) {
                linksToCheck.addLast(l);
            } else {
                linksToCheck.addFirst(l);
            }
        }
    }

    private void addNewWords(Set<String> words, String webAddress) {
        for(String w : words) {
            if(!dictionary.contains(w)) continue;
            if(size == max) return;
            if(index.containsKey(w)) {
                if(!index.get(w).contains(webAddress)){
                    HashSet<String> links = index.get(w);
                    links.add(webAddress);
                    index.replace(w, links);
                }
            } else {
                HashSet<String> newLink = new HashSet<>(1);
                newLink.add(webAddress);
                index.put(w, newLink);
            }
            size++;
            System.out.println(size);
        }
    }

    public String[] searchHits(String target){ // DONE
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

    public int size(){ // DONE
        return size;
    }


    /*
     * Simple test code
     */
    public static void main(String[] args){
        String webAddress = "http://www.bbc.com/";
        String TARGET = "og";

        MyEngine engine = new MyEngine(10000);
        engine.setDepthFirst();
        System.out.print("Searching, start....");
        engine.crawlFrom(webAddress);
        System.out.printf("finish. Size of index = %d%n",engine.size());

        System.out.printf("Occurrences of \"%s\":%n",TARGET);
        String[] results = engine.searchHits(TARGET);
        for (String s: results)
            System.out.println(s);

        for (Map.Entry<String, HashSet<String>> entry : index.entrySet()) {
            String key = entry.getKey();
            HashSet<String> value = entry.getValue();

            System.out.printf("%s : %s\n", key, value);
        }
    }
}