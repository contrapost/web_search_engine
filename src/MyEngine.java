package src;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

/*
* Progress bar was borrowed from http://stackoverflow.com/a/1001340/5552809
* Code to stop a thread was borrowed from http://stackoverflow.com/a/10961760/5552809
* */

public class MyEngine implements SearchEngine {

    private static final int DEFAULT_SIZE = 5000;

    private int size = 0;
    private int max;
    private HashSet<String> wordsToIndex;
    private HashMap<String, HashSet<String>> index;
    private boolean breadthFirst = true;
    private LinkedList<String> linksToCheck;
    private HashSet<String> visitedLinks;
    private Thread progressBarThread;
    private ProgressBar progressBar;
    private boolean firstStepOfCrawling = true;

    MyEngine(){ // DONE
        this(DEFAULT_SIZE);
    }

    private MyEngine(int theMax) { // DONE
        setMax(theMax);
        wordsToIndex = buildDictionary();
        index = new HashMap<>();
        linksToCheck = new LinkedList<>();
        visitedLinks = new HashSet<>();
        progressBar = new ProgressBar();
        progressBarThread = new Thread(progressBar);
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

        if(firstStepOfCrawling) {
            progressBarThread.start();
            firstStepOfCrawling = false;
        }

        WebPageReader wpr = new WebPageReader(webAddress);
        visitedLinks.add(webAddress);
        wpr.run();

        addNewWords(wpr.getWords(), webAddress);

        addNewLinks(wpr.getLinks());

        if(linksToCheck.size() == 0 || size() == max) {
            progressBar.terminate();
            try {
                progressBarThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return;
        }

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
            if(!wordsToIndex.contains(w)) continue;
            if(size == max) return;
            if(index.containsKey(w)) {
                HashSet<String> links = index.get(w);
                links.add(webAddress);
                index.replace(w, links);
            } else {
                HashSet<String> newLink = new HashSet<>(1);
                newLink.add(webAddress);
                index.put(w, newLink);
            }
            size++;
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

    @Override
    public HashMap<String, HashSet<String>> getIndex() {
        return index;
    }


    /*
     * Simple test code
     */
    public static void main(String[] args){
        String webAddress = "http://www.bbc.com/";
        String TARGET = "og";

        MyEngine engine = new MyEngine(10000);
        engine.setDepthFirst();
        System.out.print("Searching, start....\n");
        engine.crawlFrom(webAddress);
        System.out.printf("\nfinish. Size of index = %d%n",engine.size());

        System.out.printf("Occurrences of \"%s\":%n",TARGET);
        String[] results = engine.searchHits(TARGET);
        for (String s: results)
            System.out.println(s);
    }

    private class ProgressBar implements Runnable {
        private volatile boolean running = true;

        void terminate() {
            running = false;
        }

        @Override
        public void run() {
            while (running) {
                try {

                    for (double progressPercentage = 0.0; progressPercentage < 1.0; progressPercentage += 0.01) {
                        final int width = 50;

                        System.out.print("\r[");
                        int i = 0;
                        for (; i <= (int)(progressPercentage*width); i++) {
                            System.out.print(".");
                        }
                        for (; i < width; i++) {
                            System.out.print(" ");
                        }
                        System.out.print("]");
                        Thread.sleep(20);
                    }
                } catch (InterruptedException ignored) {}
            }
        }
    }
}