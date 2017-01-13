import java.util.*;

public class SimpleFrontEnd {

    private static final String WIKIPEDIA = "https://en.wikipedia.org/wiki/Main_Page";
    private static final int MAX = 32768;
    private static final Scanner STDIN = new Scanner(System.in);

    private static SearchEngine engine = new MyEngine();

    private static String getSearchString(){
        System.out.print("\nGive search string: ");
        try {
            return STDIN.next();
        } catch (NoSuchElementException e) {
            return null;
        }
    }

    private static void serveUser(){
        String word;
        while((word = getSearchString()) != null){
            for(String link : engine.searchHits(word))
                System.out.printf(" - %s%n",link);
        }
    }

    private static void buildIndex(){
        engine.setMax(MAX);
        System.out.println("\nBuilding search index....");
        engine.crawlFrom(WIKIPEDIA);
        System.out.printf("%n Number of occurences = %d%n Estimated memory footprint = %.1fMB%n I am ready for searching.%n",engine.size(),memoryFootprintInMegaBytes());
        for (Map.Entry<String, HashSet<String>> entry : MyEngine.index.entrySet()) {
            String key = entry.getKey();
            HashSet<String> value = entry.getValue();

            System.out.printf("%s : %s\n", key, value);
        }
        System.gc();
    }

    public static void main(String[] args){
        buildIndex();
        serveUser();    

    }
    
    private static double memoryFootprintInMegaBytes(){
        System.gc();
        Runtime runtime = Runtime.getRuntime();
        return 1e-6*( runtime.totalMemory() -  runtime.freeMemory() );
    }
}
