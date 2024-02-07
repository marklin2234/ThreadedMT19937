import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ThreadedMT19937 {

    private int seed;

    public ThreadedMT19937(int seed) {
        this.seed = seed;
    }

    private double[] generate(int n) throws InterruptedException {
        List<Double> ans = new ArrayList<>();
        
        int numThreads = 10;
        ExecutorService service = Executors.newFixedThreadPool(numThreads);

        for (int i = 0; i < numThreads; i++) {
            int numGen = n / numThreads + (i == 0 ? n % numThreads : 0);
            service.execute(() -> {
                int threadId = (int) Thread.currentThread().getId();
                MT19937 rand = new MT19937(seed ^ threadId);
                List<Double> nums = rand.generate(numGen);
                synchronized(this) {
                    ans.addAll(nums);
                }
            });
        }
        service.shutdown();
        service.awaitTermination(5, TimeUnit.SECONDS);
        double[] ret = new double[n];
        for (int i = 0; i < n; i++) {
            ret[i] = ans.get(i);
        }
        return ret;
    }

    private class MT19937 {
        private final int n = 624;
        private final int f = 1812433253;
        private final int w = 32;
        private int[] state = new int[n];
        private int cnt;
    
        public MT19937(int seed) {
            state[0] = seed;
            this.cnt = 0;
            for (int i = 1; i < n; i++) {
                state[i] = f * (state[i - 1] ^ (state[i - 1] >> (w - 2))) + i;
            }
            twist();
        }
    
        private int h(int x) {
            if ((x & 1) == 0) {
                return x >> 1;
            }
            return (x >> 1) ^ (0x9908B0DF);
        }
    
        private void twist() {
            for (int i = 0; i < n; i++) {
                int lowerMask = (1 << (w - 1)) - 1;
                int upperMask = (1 << (w - 1));
    
                int temp = (upperMask & state[i]) | (lowerMask & state[(i + 1) % n]);
                state[i] = state[(i + 397) % n] ^ h(temp);
            }
        }
    
        private int temper() {
            if (cnt == n) {
                twist();
            }
            int y = state[(cnt + n) % n] ^ (state[(cnt + n) % n] >> 11);
            cnt++;
            y = y ^ ((y << 7) & 0x9D2C5680);
            y = y ^ ((y << 15) & 0xEFC60000);
            return y ^ (y >> 18);
        }
    
        public List<Double> generate(int k) {
            List<Double> ans = new ArrayList<>();
    
            while(k-- > 0) {
                int xn = temper();
                double un = 0;
                for (int i = 0; i < 32; i++) {
                    int z = xn & 1;
                    xn = xn >> 1;
                    un += z * Math.pow(2, -i - 1);
                }
                ans.add(un);
            }
            return ans;
        }
    }

    public static void main(String[] args) throws InterruptedException {
        Scanner sc = new Scanner(System.in);
        // Prompt user
        System.out.println("Seed: ");
        int seed = sc.nextInt();
        System.out.println("How many PRNs? ");
        int n = sc.nextInt();
        ThreadedMT19937 prng = new ThreadedMT19937(seed);
        long start = System.currentTimeMillis();
        double[] nums = prng.generate(n);
        long end = System.currentTimeMillis();
        System.out.println(Arrays.toString(nums));
        System.out.println(n  + " PRNs generated in " + (end - start) + "ms.");
        sc.close();
    }
}
