import java.util.*;

/** Hand-built data structures (no java.util collections inside them) with complexity notes.
 *  Demonstrates generics, iterators, interfaces, recursion, hashing, heaps, trees. */
public class Main {

    // ---------- Singly linked list: O(1) addFirst, O(n) get ----------
    static class MyLinkedList<T> implements Iterable<T> {
        private class Node { T val; Node next; Node(T v) { val = v; } }
        private Node head; private int size;
        void addFirst(T v) { Node n = new Node(v); n.next = head; head = n; size++; }
        void addLast(T v) {
            Node n = new Node(v);
            if (head == null) head = n; else { Node c = head; while (c.next != null) c = c.next; c.next = n; }
            size++;
        }
        void reverse() {                                   // O(n) iterative pointer reversal
            Node prev = null, cur = head;
            while (cur != null) { Node nx = cur.next; cur.next = prev; prev = cur; cur = nx; }
            head = prev;
        }
        int size() { return size; }
        public Iterator<T> iterator() {
            return new Iterator<>() {
                Node c = head;
                public boolean hasNext() { return c != null; }
                public T next() { T v = c.val; c = c.next; return v; }
            };
        }
        @Override public String toString() {
            StringJoiner j = new StringJoiner(" -> ", "[", "]");
            for (T t : this) j.add(String.valueOf(t));
            return j.toString();
        }
    }

    // ---------- Stack (array-backed, dynamic resize): amortised O(1) push/pop ----------
    static class MyStack<T> {
        private Object[] a = new Object[2]; private int n;
        void push(T v) { if (n == a.length) a = Arrays.copyOf(a, n * 2); a[n++] = v; }
        @SuppressWarnings("unchecked") T pop() {
            if (n == 0) throw new RuntimeException("stack empty");
            T v = (T) a[--n]; a[n] = null; return v;
        }
        boolean isEmpty() { return n == 0; }
    }

    // ---------- Queue (circular buffer): O(1) enqueue/dequeue ----------
    static class MyQueue<T> {
        private Object[] a = new Object[4]; private int head, size;
        void enqueue(T v) {
            if (size == a.length) {
                Object[] b = new Object[size * 2];
                for (int i = 0; i < size; i++) b[i] = a[(head + i) % a.length];
                a = b; head = 0;
            }
            a[(head + size++) % a.length] = v;
        }
        @SuppressWarnings("unchecked") T dequeue() {
            if (size == 0) throw new RuntimeException("queue empty");
            T v = (T) a[head]; a[head] = null; head = (head + 1) % a.length; size--; return v;
        }
        int size() { return size; }
    }

    // ---------- Min-heap / priority queue: O(log n) push/pop ----------
    static class MinHeap {
        private int[] h = new int[8]; private int n;
        void push(int v) {
            if (n == h.length) h = Arrays.copyOf(h, n * 2);
            h[n] = v; int i = n++;
            while (i > 0 && h[(i - 1) / 2] > h[i]) { swap(i, (i - 1) / 2); i = (i - 1) / 2; }
        }
        int pop() {
            int top = h[0]; h[0] = h[--n]; int i = 0;
            while (true) {
                int l = 2 * i + 1, r = l + 1, m = i;
                if (l < n && h[l] < h[m]) m = l;
                if (r < n && h[r] < h[m]) m = r;
                if (m == i) break;
                swap(i, m); i = m;
            }
            return top;
        }
        boolean isEmpty() { return n == 0; }
        private void swap(int a, int b) { int t = h[a]; h[a] = h[b]; h[b] = t; }
    }

    // ---------- Hash map with separate chaining + rehash: O(1) average ----------
    static class MyHashMap<K, V> {
        private static class Entry<K, V> { K k; V v; Entry<K, V> next; Entry(K k, V v, Entry<K, V> n) { this.k = k; this.v = v; next = n; } }
        private Entry<K, V>[] table = newTable(8); private int size;
        @SuppressWarnings("unchecked") private static <K, V> Entry<K, V>[] newTable(int c) { return (Entry<K, V>[]) new Entry[c]; }
        private int idx(Object k, int cap) { return (k.hashCode() & 0x7fffffff) % cap; }
        void put(K k, V v) {
            int i = idx(k, table.length);
            for (Entry<K, V> e = table[i]; e != null; e = e.next) if (e.k.equals(k)) { e.v = v; return; }
            table[i] = new Entry<>(k, v, table[i]); size++;
            if (size > table.length * 0.75) rehash();
        }
        V get(K k) {
            for (Entry<K, V> e = table[idx(k, table.length)]; e != null; e = e.next) if (e.k.equals(k)) return e.v;
            return null;
        }
        private void rehash() {
            Entry<K, V>[] nt = newTable(table.length * 2);
            for (Entry<K, V> head : table)
                for (Entry<K, V> e = head; e != null; ) {
                    Entry<K, V> nx = e.next; int i = idx(e.k, nt.length);
                    e.next = nt[i]; nt[i] = e; e = nx;
                }
            table = nt;
        }
        int size() { return size; }
    }

    // ---------- Binary search tree: O(log n) average, O(n) worst ----------
    static class BST {
        private static class Node { int v; Node l, r; Node(int v) { this.v = v; } }
        private Node root;
        void insert(int v) { root = insert(root, v); }
        private Node insert(Node n, int v) {
            if (n == null) return new Node(v);
            if (v < n.v) n.l = insert(n.l, v); else if (v > n.v) n.r = insert(n.r, v);
            return n;
        }
        boolean contains(int v) { Node c = root; while (c != null) { if (v == c.v) return true; c = v < c.v ? c.l : c.r; } return false; }
        int height() { return height(root); }
        private int height(Node n) { return n == null ? 0 : 1 + Math.max(height(n.l), height(n.r)); }
        List<Integer> inorder() { List<Integer> out = new ArrayList<>(); inorder(root, out); return out; }
        private void inorder(Node n, List<Integer> o) { if (n == null) return; inorder(n.l, o); o.add(n.v); inorder(n.r, o); }
    }

    static int checks = 0;
    static void check(boolean cond, String name) {
        checks++;
        if (!cond) throw new AssertionError("FAILED: " + name);
        System.out.println("  ok  " + name);
    }

    public static void main(String[] args) {
        MyLinkedList<Integer> ll = new MyLinkedList<>();
        for (int i = 1; i <= 4; i++) ll.addLast(i);
        ll.addFirst(0);
        System.out.println("List: " + ll);
        ll.reverse();
        System.out.println("Reversed: " + ll);
        check(ll.toString().equals("[4 -> 3 -> 2 -> 1 -> 0]") && ll.size() == 5, "linked list add/reverse");

        MyStack<Character> st = new MyStack<>();
        String expr = "{[()()]}"; boolean balanced = true;
        for (char ch : expr.toCharArray()) {
            if ("([{".indexOf(ch) >= 0) st.push(ch);
            else { char o = st.pop(); balanced &= "([{".indexOf(o) == ")]}".indexOf(ch); }
        }
        check(balanced && st.isEmpty(), "stack: balanced brackets");

        MyQueue<Integer> q = new MyQueue<>();
        for (int i = 0; i < 10; i++) q.enqueue(i);
        int sum = 0; while (q.size() > 5) sum += q.dequeue();
        check(sum == 10 && q.dequeue() == 5, "queue: circular buffer + resize");

        MinHeap heap = new MinHeap();
        int[] data = {9, 4, 7, 1, 8, 2, 6, 3, 5, 0};
        for (int d : data) heap.push(d);
        StringBuilder sb = new StringBuilder(); while (!heap.isEmpty()) sb.append(heap.pop());
        check(sb.toString().equals("0123456789"), "min-heap sorts (heap sort)");

        MyHashMap<String, Integer> map = new MyHashMap<>();
        String[] words = "to be or not to be that is the question".split(" ");
        for (String w : words) { Integer c = map.get(w); map.put(w, c == null ? 1 : c + 1); }
        for (int i = 0; i < 100; i++) map.put("k" + i, i);          // forces rehash
        check(map.get("to") == 2 && map.get("question") == 1 && map.get("k99") == 99 && map.size() == 108,
              "hash map counts + rehash (size=" + map.size() + ")");

        BST t = new BST();
        for (int v : new int[]{50, 30, 70, 20, 40, 60, 80, 30}) t.insert(v);
        check(t.inorder().equals(List.of(20, 30, 40, 50, 60, 70, 80)) && t.contains(60) && !t.contains(65) && t.height() == 3,
              "BST insert/contains/inorder/height");
        System.out.println("All " + checks + " checks passed.");
    }
}
