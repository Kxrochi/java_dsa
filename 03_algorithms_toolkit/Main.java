import java.util.*;

/** Algorithms toolkit: sorting, searching, graphs, dynamic programming, two-pointer/sliding window.
 *  Every algorithm is verified against a reference or known answer in main(). */
public class Main {

    // ---------- Sorting ----------
    static void mergeSort(int[] a) { if (a.length > 1) mergeSort(a, new int[a.length], 0, a.length - 1); }   // O(n log n), stable
    private static void mergeSort(int[] a, int[] tmp, int lo, int hi) {
        if (lo >= hi) return;
        int mid = (lo + hi) >>> 1;
        mergeSort(a, tmp, lo, mid); mergeSort(a, tmp, mid + 1, hi);
        int i = lo, j = mid + 1, k = lo;
        while (i <= mid && j <= hi) tmp[k++] = a[i] <= a[j] ? a[i++] : a[j++];
        while (i <= mid) tmp[k++] = a[i++];
        while (j <= hi) tmp[k++] = a[j++];
        System.arraycopy(tmp, lo, a, lo, hi - lo + 1);
    }
    static void quickSort(int[] a, int lo, int hi, Random rnd) {                                            // O(n log n) avg, random pivot
        if (lo >= hi) return;
        int p = a[lo + rnd.nextInt(hi - lo + 1)], i = lo, j = hi;
        while (i <= j) {
            while (a[i] < p) i++;
            while (a[j] > p) j--;
            if (i <= j) { int t = a[i]; a[i] = a[j]; a[j] = t; i++; j--; }
        }
        quickSort(a, lo, j, rnd); quickSort(a, i, hi, rnd);
    }

    // ---------- Searching ----------
    static int binarySearch(int[] a, int key) {                                                             // O(log n)
        int lo = 0, hi = a.length - 1;
        while (lo <= hi) {
            int mid = lo + (hi - lo) / 2;                 // avoids int overflow
            if (a[mid] == key) return mid;
            if (a[mid] < key) lo = mid + 1; else hi = mid - 1;
        }
        return -1;
    }

    // ---------- Graphs ----------
    static List<Integer> bfsOrder(List<List<Integer>> g, int s) {                                           // O(V+E)
        List<Integer> order = new ArrayList<>(); boolean[] seen = new boolean[g.size()];
        Deque<Integer> q = new ArrayDeque<>(); q.add(s); seen[s] = true;
        while (!q.isEmpty()) {
            int u = q.poll(); order.add(u);
            for (int v : g.get(u)) if (!seen[v]) { seen[v] = true; q.add(v); }
        }
        return order;
    }
    static void dfs(List<List<Integer>> g, int u, boolean[] seen, List<Integer> out) {
        seen[u] = true; out.add(u);
        for (int v : g.get(u)) if (!seen[v]) dfs(g, v, seen, out);
    }
    record Edge(int to, int w) {}
    static int[] dijkstra(List<List<Edge>> g, int src) {                                                    // O((V+E) log V)
        int[] dist = new int[g.size()]; Arrays.fill(dist, Integer.MAX_VALUE); dist[src] = 0;
        PriorityQueue<int[]> pq = new PriorityQueue<>((x, y) -> Integer.compare(x[1], y[1]));
        pq.add(new int[]{src, 0});
        while (!pq.isEmpty()) {
            int[] cur = pq.poll();
            if (cur[1] > dist[cur[0]]) continue;
            for (Edge e : g.get(cur[0])) {
                int nd = cur[1] + e.w();
                if (nd < dist[e.to()]) { dist[e.to()] = nd; pq.add(new int[]{e.to(), nd}); }
            }
        }
        return dist;
    }
    static List<Integer> topoSort(int n, int[][] edges) {                                                   // Kahn's algorithm
        List<List<Integer>> g = new ArrayList<>(); int[] indeg = new int[n];
        for (int i = 0; i < n; i++) g.add(new ArrayList<>());
        for (int[] e : edges) { g.get(e[0]).add(e[1]); indeg[e[1]]++; }
        Deque<Integer> q = new ArrayDeque<>(); List<Integer> out = new ArrayList<>();
        for (int i = 0; i < n; i++) if (indeg[i] == 0) q.add(i);
        while (!q.isEmpty()) { int u = q.poll(); out.add(u); for (int v : g.get(u)) if (--indeg[v] == 0) q.add(v); }
        return out.size() == n ? out : List.of();       // empty => cycle
    }

    // ---------- Dynamic programming ----------
    static int lcs(String a, String b) {                                                                    // O(nm)
        int[][] dp = new int[a.length() + 1][b.length() + 1];
        for (int i = 1; i <= a.length(); i++)
            for (int j = 1; j <= b.length(); j++)
                dp[i][j] = a.charAt(i - 1) == b.charAt(j - 1) ? dp[i - 1][j - 1] + 1 : Math.max(dp[i - 1][j], dp[i][j - 1]);
        return dp[a.length()][b.length()];
    }
    static int knapsack(int[] w, int[] v, int cap) {                                                        // 0/1, O(nW), 1-D table
        int[] dp = new int[cap + 1];
        for (int i = 0; i < w.length; i++)
            for (int c = cap; c >= w[i]; c--) dp[c] = Math.max(dp[c], dp[c - w[i]] + v[i]);
        return dp[cap];
    }
    static int editDistance(String a, String b) {
        int[][] dp = new int[a.length() + 1][b.length() + 1];
        for (int i = 0; i <= a.length(); i++) dp[i][0] = i;
        for (int j = 0; j <= b.length(); j++) dp[0][j] = j;
        for (int i = 1; i <= a.length(); i++)
            for (int j = 1; j <= b.length(); j++)
                dp[i][j] = Math.min(Math.min(dp[i - 1][j], dp[i][j - 1]) + 1,
                                    dp[i - 1][j - 1] + (a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1));
        return dp[a.length()][b.length()];
    }
    static long fibMemo(int n, long[] memo) { if (n < 2) return n; if (memo[n] != 0) return memo[n]; return memo[n] = fibMemo(n - 1, memo) + fibMemo(n - 2, memo); }

    // ---------- Sliding window / two pointers ----------
    static int longestUniqueSubstring(String s) {                                                           // O(n)
        Map<Character, Integer> last = new HashMap<>(); int best = 0, start = 0;
        for (int i = 0; i < s.length(); i++) {
            Integer p = last.put(s.charAt(i), i);
            if (p != null && p >= start) start = p + 1;
            best = Math.max(best, i - start + 1);
        }
        return best;
    }
    static boolean twoSumSorted(int[] a, int target) {
        int i = 0, j = a.length - 1;
        while (i < j) { int s = a[i] + a[j]; if (s == target) return true; if (s < target) i++; else j--; }
        return false;
    }

    static void check(boolean c, String name) { if (!c) throw new AssertionError("FAILED: " + name); System.out.println("  ok  " + name); }

    public static void main(String[] args) {
        Random rnd = new Random(42);
        int[] data = rnd.ints(2000, -1000, 1000).toArray();
        int[] ref = data.clone(); Arrays.sort(ref);
        int[] m = data.clone(); mergeSort(m);
        int[] q = data.clone(); quickSort(q, 0, q.length - 1, rnd);
        check(Arrays.equals(m, ref), "merge sort == Arrays.sort");
        check(Arrays.equals(q, ref), "quick sort == Arrays.sort");
        check(binarySearch(ref, ref[777]) >= 0 && binarySearch(ref, 5000) == -1, "binary search hit/miss");

        List<List<Integer>> g = new ArrayList<>();
        for (int i = 0; i < 6; i++) g.add(new ArrayList<>());
        int[][] es = {{0,1},{0,2},{1,3},{2,3},{3,4},{4,5}};
        for (int[] e : es) { g.get(e[0]).add(e[1]); g.get(e[1]).add(e[0]); }
        List<Integer> d = new ArrayList<>(); dfs(g, 0, new boolean[6], d);
        check(bfsOrder(g, 0).equals(List.of(0,1,2,3,4,5)), "BFS order");
        check(d.equals(List.of(0,1,3,2,4,5)), "DFS order");

        List<List<Edge>> wg = new ArrayList<>();
        for (int i = 0; i < 5; i++) wg.add(new ArrayList<>());
        int[][] we = {{0,1,4},{0,2,1},{2,1,2},{1,3,1},{2,3,5},{3,4,3}};
        for (int[] e : we) wg.get(e[0]).add(new Edge(e[1], e[2]));
        check(Arrays.equals(dijkstra(wg, 0), new int[]{0, 3, 1, 4, 7}), "Dijkstra shortest paths");
        check(topoSort(4, new int[][]{{0,1},{1,2},{0,3},{3,2}}).size() == 4 && topoSort(2, new int[][]{{0,1},{1,0}}).isEmpty(),
              "topological sort + cycle detection");

        check(lcs("AGGTAB", "GXTXAYB") == 4, "LCS = 4");
        check(knapsack(new int[]{1,3,4,5}, new int[]{1,4,5,7}, 7) == 9, "0/1 knapsack = 9");
        check(editDistance("kitten", "sitting") == 3, "edit distance = 3");
        check(fibMemo(50, new long[51]) == 12586269025L, "memoised fib(50)");
        check(longestUniqueSubstring("abcabcbb") == 3 && longestUniqueSubstring("pwwkew") == 3, "sliding window longest unique");
        check(twoSumSorted(new int[]{1,3,4,6,9}, 10) && !twoSumSorted(new int[]{1,3,4,6,9}, 2), "two pointers");
        System.out.println("All algorithm checks passed.");
    }
}
