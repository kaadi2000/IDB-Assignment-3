package exercise3.impl;

//import java.lang.classfile.components.ClassPrinter;
import java.util.*;

public class BPlusTree<K extends Comparable<? super K>, V> {
    static final int ENTRY_COUNT = 8;

    final class Record implements Comparable<Map.Entry<K, V>>, Map.Entry<K, V> {
        final K key;
        V value;

        Record(K key) {
            this.key = key;
        }

        @Override
        public int compareTo(Map.Entry<K, V> o) {
            return this.key.compareTo(o.getKey());
        }

        @Override
        public K getKey() {
            return this.key;
        }

        @Override
        public V getValue() {
            return this.value;
        }

        @Override
        public V setValue(V value) {
            return this.value = value;
        }
    }

    abstract static class Node {
        abstract Node split();

        abstract boolean isLeaf();
    }

    final class IndexNode extends Node {
        final List<K> keys = new ArrayList<>(ENTRY_COUNT);
        final List<Node> children = new ArrayList<>(ENTRY_COUNT + 1);

        boolean isLeaf() {
            return false;
        }

        @Override
        Node split() {
            // TODO: Impl. - Done
            int mid = keys.size() / 2;
            IndexNode newI = new IndexNode();

            newI.keys.addAll(keys.subList(mid + 1, keys.size()));
            newI.children.addAll(children.subList(mid + 1, children.size()));
            keys.subList(mid, keys.size()).clear();
            children.subList(mid + 1, children.size()).clear();

            return newI;
        }
    }

    final class LeafNode extends Node {
        final List<Record> records = new ArrayList<>(ENTRY_COUNT);
        LeafNode next;

        @Override
        Node split() {
            // TODO: Impl. - Done

            int mid = records.size() / 2;
            LeafNode newLeaf = new LeafNode();

            newLeaf.records.addAll(records.subList(mid, records.size()));
            records.subList(mid, records.size()).clear();
            newLeaf.next = this.next;
            this.next = newLeaf;

            return newLeaf;

        }

        @Override
        boolean isLeaf() {
            return true;
        }
    }

    private Node root;

    public BPlusTree() {
        // TODO: Impl.
    }
    

    private IndexNode findParent(Node current, Node child) {
        if (current.isLeaf()) {
            return null;
        }
    

        @SuppressWarnings("unchecked")
        IndexNode index = (IndexNode) current;


        for (Node n : index.children) {
            if (n == child) {
                return index;
            }

            if (!n.isLeaf()) {
                IndexNode possibleParent = findParent(n, child);
                if (possibleParent != null) {
                    return possibleParent;
                }
            }
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    private void handleSplit(Node node, IndexNode parent){

        if ((node.isLeaf() && ((LeafNode) node).records.size() > ENTRY_COUNT) ||
        (!node.isLeaf() && ((IndexNode) node).keys.size() > ENTRY_COUNT)) {
        
        Node newNode = node.split();
        K middleK;

        if (node.isLeaf()) {
            LeafNode l = (LeafNode) node;
            LeafNode newLeaf = (LeafNode) newNode;
            middleK = newLeaf.records.get(0).key;

            if (parent == null) {
                IndexNode newRoot = new IndexNode();
                newRoot.keys.add(middleK);
                newRoot.children.add(l);
                newRoot.children.add(newLeaf);
                root = newRoot;

                return;
            }
        } else {
            IndexNode index = (IndexNode) node;
            IndexNode newIndex = (IndexNode) newNode;
            middleK = index.keys.remove(index.keys.size() - 1);

            if (parent == null) {
                IndexNode newRoot = new IndexNode();
                newRoot.keys.add(middleK);
                newRoot.children.add(index);
                newRoot.children.add(newIndex);
                root = newRoot;
                return;
            }

        }

        int pos = Collections.binarySearch(parent.keys, middleK);
        
        if (pos < 0) {
            pos = -pos - 1;
        }

        parent.keys.add(pos, middleK);
        parent.children.add(pos + 1, newNode);

        handleSplit(parent, findParent(root, parent));
        
        }
    }

    @SuppressWarnings("unchecked")
    public V insert(K key, V value) {
        // TODO: Impl. - Done Hopefully?

        if(root == null){
            root = new LeafNode();

        }

        Node current = root;
        IndexNode parent = null;

        while(!current.isLeaf()){
            parent = (IndexNode) current;
            int position = Collections.binarySearch(parent.keys, key);
            if(position < 0){
                position =- position - 1;
            }

            current = parent.children.get(position);
        }

        LeafNode leaf = (LeafNode) current;

        for(Record r : leaf.records){
            if(r.key.equals(key)){
                V oValue = r.value;
                r.value = value;
                return oValue;
            }
        }

        Record newR = new Record(key);
        newR.setValue(value);

        int position = Collections.binarySearch(leaf.records, newR);
        if(position < 0){
            position =- position - 1;
        }

        leaf.records.add(position, newR);

        handleSplit(leaf, parent);

        return null;
    }

    public V pointQuery(K key) {
        // TODO: Impl. - Done

        Node current = root;

        while (!current.isLeaf()) {

            @SuppressWarnings("unchecked")
            IndexNode i = (IndexNode) current;
            int pos = Collections.binarySearch(i.keys, key);
            if (pos < 0) {
                pos = -pos - 1;
            }
            current = i.children.get(pos);
        }

        @SuppressWarnings("unchecked")
        LeafNode leaf = (LeafNode) current;
        int pos = Collections.binarySearch(leaf.records, new Record(key));
        return (pos >= 0) ? leaf.records.get(pos).getValue() : null;
    }

    public List<? extends Map.Entry<K, V>> rangeQuery(K minKey, K maxKey) {
        // TODO: Impl. - Done
        List<Map.Entry<K, V>> results = new ArrayList<>();
        Node current = root;

        while (!current.isLeaf()) {
            @SuppressWarnings("unchecked")
            IndexNode index = (IndexNode) current;
            int pos = Collections.binarySearch(index.keys, minKey);
            if (pos < 0) {
                pos = -pos - 1;
            }
            current = index.children.get(pos);
        }

        @SuppressWarnings("unchecked")
        LeafNode leaf = (LeafNode) current;
        while (leaf != null) {
            for (Record r : leaf.records) {
                if (r.key.compareTo(minKey) >= 0 && r.key.compareTo(maxKey) <= 0) {
                    results.add(r);
                } else if (r.key.compareTo(maxKey) > 0) {
                    return results;
                }
            }

            leaf = leaf.next;
        }

        return results;
    }

    public static void main(String[] args) {
        BPlusTree<Integer, String> tree = new BPlusTree<>();
        for (int i = 0; i < 100; i++) {
            tree.insert(i, "" + i);

            for (int j = 0; j <= i; j++) {
                if (!tree.pointQuery(i).equals("" + i)) {
                    throw new RuntimeException("Key not found: " + j);
                }
            }

            if (tree.rangeQuery(0, i).size() != i + 1) {
                throw new RuntimeException("Range query failed at key " + i);
            }
        }
    }
}
