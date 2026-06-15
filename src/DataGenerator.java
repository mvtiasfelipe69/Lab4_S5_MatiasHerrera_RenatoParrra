import edu.princeton.cs.algs4.StdRandom;
import java.util.ArrayList;
import java.util.HashSet;

public class DataGenerator {

    private static final String[] CATEGORIES = {
            "Sensor", "Motor", "Microcontrolador", "Cable", "Bateria", "Herramienta", "Modulo", "Kit"
    };

    private static final String[] LOCATIONS = {
            "Estante_A", "Estante_B", "Caja_1", "Caja_2", "Laboratorio", "Bodega"
    };

    private static final double P_PURCHASE = 0.35;
    private static final double P_QUERY = 0.65;
    private static final double P_LEND = 0.80;
    private static final double P_RECEIVE = 0.90;

    public static InventoryItem generateItem(int id) {
        String category = CATEGORIES[StdRandom.uniformInt(CATEGORIES.length)];
        String location = LOCATIONS[StdRandom.uniformInt(LOCATIONS.length)];
        int stockTotal = StdRandom.uniformInt(1, 21);
        return new InventoryItem(id, "Componente_" + id, category, location, stockTotal, stockTotal, 0);
    }

    public static ArrayList<InventoryOperation> generateOperations(int m, int keyUniverse, long seed) {
        StdRandom.setSeed(seed);
        ArrayList<InventoryOperation> ops = new ArrayList<>(m);
        HashSet<Integer> presentKeys = new HashSet<>();

        for (int i = 0; i < m; i++) {
            int key = StdRandom.uniformInt(1, keyUniverse + 1);
            double r = StdRandom.uniformDouble();

            if (r < P_PURCHASE) {
                int quantity = StdRandom.uniformInt(1, 6);
                if (!presentKeys.contains(key)) {
                    InventoryItem newItem = generateItem(key);
                    ops.add(new InventoryOperation(OperationType.PURCHASE, key, quantity, newItem));
                    presentKeys.add(key);
                } else {
                    ops.add(new InventoryOperation(OperationType.PURCHASE, key, quantity, null));
                }
            } else if (r < P_QUERY) {
                ops.add(new InventoryOperation(OperationType.QUERY, key, 0, null));
            } else if (r < P_LEND) {
                int quantity = StdRandom.uniformInt(1, 6);
                ops.add(new InventoryOperation(OperationType.LEND, key, quantity, null));
            } else if (r < P_RECEIVE) {
                int quantity = StdRandom.uniformInt(1, 6);
                ops.add(new InventoryOperation(OperationType.RECEIVE, key, quantity, null));
            } else {
                ops.add(new InventoryOperation(OperationType.DISPOSE, key, 0, null));
                presentKeys.remove(key);
            }
        }
        return ops;
    }
}