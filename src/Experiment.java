import edu.princeton.cs.algs4.Out;
import edu.princeton.cs.algs4.StdRandom;
import edu.princeton.cs.algs4.StopwatchCPU;
import java.util.ArrayList;

public class Experiment {

    private static final int[] T_VALUES = {12, 13, 14, 15, 16, 17, 18, 19};
    private static final int REPETITIONS = 30;
    private static final String CSV_HEADER = "instancia,estructura,m,purchase_total,query_total,lend_total,receive_total,dispose_total,query_successful,query_failed,lend_successful,lend_failed,receive_successful,receive_failed,final_size,final_height,elapsed_seconds";

    public static void main(String[] args) {
        int tMin = T_VALUES[0];
        int tMax = T_VALUES[T_VALUES.length - 1];
        if (args.length == 2) {
            tMin = Integer.parseInt(args[0]);
            tMax = Integer.parseInt(args[1]);
        }

        ArrayList<InventoryOperation> dummyOps = DataGenerator.generateOperations(10000, 40000, 123L);
        BSTInventoryIndex dummyBst = new BSTInventoryIndex();
        RedBlackInventoryIndex dummyRb = new RedBlackInventoryIndex();
        runOperations(dummyBst, dummyOps, new long[12]);
        runOperations(dummyRb, dummyOps, new long[12]);

        for (int t = tMin; t <= tMax; t++) {
            int m = 1 << t;
            runExperimentsForSize(m);
        }
    }

    private static void runExperimentsForSize(int m) {
        int keyUniverse = 4 * m;
        String filename = "data/inventory_experiment_" + m + ".csv";
        new java.io.File("data").mkdirs();

        Out out = new Out(filename);
        out.println(CSV_HEADER);

        for (int i = 1; i <= REPETITIONS; i++) {
            long seed = (long) m + i;
            ArrayList<InventoryOperation> operations = DataGenerator.generateOperations(m, keyUniverse, seed);

            BSTInventoryIndex bstIndex = new BSTInventoryIndex();
            long[] bstStats = new long[12];
            System.gc();
            StopwatchCPU bstTimer = new StopwatchCPU();
            runOperations(bstIndex, operations, bstStats);
            double bstElapsed = bstTimer.elapsedTime();

            RedBlackInventoryIndex rbIndex = new RedBlackInventoryIndex();
            long[] rbStats = new long[12];
            System.gc();
            StopwatchCPU rbTimer = new StopwatchCPU();
            runOperations(rbIndex, operations, rbStats);
            double rbElapsed = rbTimer.elapsedTime();

            validateResults(bstIndex, rbIndex, operations, i, m);

            writeRow(out, i, "BST", m, bstStats, bstIndex.size(), bstIndex.height(), bstElapsed);
            writeRow(out, i, "RedBlackBST", m, rbStats, rbIndex.size(), rbIndex.height(), rbElapsed);
        }

        out.close();
    }

    private static void runOperations(InventoryIndex index, ArrayList<InventoryOperation> operations, long[] stats) {
        for (InventoryOperation op : operations) {
            switch (op.getType()) {
                case PURCHASE: executePurchase(index, op, stats); break;
                case QUERY: executeQuery(index, op, stats); break;
                case LEND: executeLend(index, op, stats); break;
                case RECEIVE: executeReceive(index, op, stats); break;
                case DISPOSE: executeDispose(index, op, stats); break;
            }
        }
    }

    private static void executePurchase(InventoryIndex index, InventoryOperation op, long[] stats) {
        stats[0]++;
        if (!index.contains(op.getKey())) {
            index.put(op.getKey(), op.getItem());
        } else {
            InventoryItem item = index.get(op.getKey());
            item.addStock(op.getQuantity());
            index.put(op.getKey(), item);
        }
    }

    private static void executeQuery(InventoryIndex index, InventoryOperation op, long[] stats) {
        stats[1]++;
        InventoryItem item = index.get(op.getKey());
        if (item != null) stats[5]++;
        else stats[6]++;
    }

    private static void executeLend(InventoryIndex index, InventoryOperation op, long[] stats) {
        stats[2]++;
        InventoryItem item = index.get(op.getKey());
        if (item != null && item.lend(op.getQuantity())) {
            index.put(op.getKey(), item);
            stats[7]++;
        } else {
            stats[8]++;
        }
    }

    private static void executeReceive(InventoryIndex index, InventoryOperation op, long[] stats) {
        stats[3]++;
        InventoryItem item = index.get(op.getKey());
        if (item != null && item.receive(op.getQuantity())) {
            index.put(op.getKey(), item);
            stats[9]++;
        } else {
            stats[10]++;
        }
    }

    private static void executeDispose(InventoryIndex index, InventoryOperation op, long[] stats) {
        stats[4]++;
        index.delete(op.getKey());
    }

    private static void validateResults(BSTInventoryIndex bstIndex, RedBlackInventoryIndex rbIndex, ArrayList<InventoryOperation> operations, int instancia, int m) {
        boolean valid = true;

        if (bstIndex.size() != rbIndex.size()) {
            valid = false;
        }

        StdRandom.setSeed(instancia * 999983L);
        int keyUniverse = 4 * m;
        for (int k = 0; k < 100; k++) {
            int key = StdRandom.uniformInt(1, keyUniverse + 1);
            InventoryItem bst = bstIndex.get(key);
            InventoryItem rb = rbIndex.get(key);
            boolean bstNull = (bst == null);
            boolean rbNull = (rb == null);
            if (bstNull != rbNull) {
                valid = false;
                break;
            }
        }

        if (operations.size() != m) {
            valid = false;
        }

        for (Integer key : bstIndex.keys()) {
            InventoryItem item = bstIndex.get(key);
            if (item == null) {
                valid = false;
                break;
            }
            if (item.getStockAvailable() < 0 || item.getStockOnLoan() < 0) {
                valid = false;
                break;
            }
            if (item.getStockAvailable() + item.getStockOnLoan() != item.getStockTotal()) {
                valid = false;
                break;
            }
        }

        if (!valid) {
            System.err.println("Error: " + instancia + " " + m);
        }
    }

    private static void writeRow(Out out, int instancia, String estructura, int m, long[] stats, int finalSize, int finalHeight, double elapsedSeconds) {
        out.println(instancia + "," + estructura + "," + m + "," + stats[0] + "," + stats[1] + "," + stats[2] + "," + stats[3] + "," + stats[4] + "," + stats[5] + "," + stats[6] + "," + stats[7] + "," + stats[8] + "," + stats[9] + "," + stats[10] + "," + finalSize + "," + finalHeight + "," + elapsedSeconds);
    }
}