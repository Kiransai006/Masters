package Pdp2;
import java.util.concurrent.*;
import java.util.*;

public class LargestValue {
    
    static class ArrayProcessor implements Callable<int[]> {
        
        private final int start, end;
        private final int[] array;

        public ArrayProcessor(int[] array, int start, int end) {
            this.array = array;
            this.start = start;
            this.end = end;
        }

        @Override
        public int[] call() {
            int max = Integer.MIN_VALUE;
            int evenCount = 0;

            for (int i = start; i < end; i++) {
                if (array[i] > max) {
                    max = array[i];
                }
                if (array[i] % 2 == 0) {
                    evenCount++;
                }
            }
            return new int[]{max, evenCount};
        }
    }

    public static void main(String[] args) {
        int size = 10_000_000;  
        int[] array = new Random().ints(size, 1, 100_000).toArray(); 

        int numThreads = Runtime.getRuntime().availableProcessors(); 
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        List<Future<int[]>> futures = new ArrayList<>();
        
        int chunkSize = size / numThreads;

        long startTime = System.nanoTime(); 

        for (int i = 0; i < numThreads; i++) {
            int start = i * chunkSize;
            int end = (i == numThreads - 1) ? size : start + chunkSize;
            futures.add(executor.submit(new ArrayProcessor(array, start, end)));
        }

        int globalMax = Integer.MIN_VALUE;
        int totalEvenCount = 0;

        for (Future<int[]> future : futures) {
            try {
                int[] result = future.get();
                if (result[0] > globalMax) {
                    globalMax = result[0];
                }
                totalEvenCount += result[1];
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }

        executor.shutdown();
        long endTime = System.nanoTime(); 

        long durationMillis = (endTime - startTime) / 1_000_000; 
        double durationSecs = durationMillis / 1000.0; 

        
        System.out.println("Largest Number in the Array: " + globalMax);
        System.out.println("Total Even Count: " + totalEvenCount);
        System.out.println(durationMillis + " millisecs (" + durationSecs + ")");
    }
}
