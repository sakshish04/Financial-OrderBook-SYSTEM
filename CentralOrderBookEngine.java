import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.TreeMap;
 
public class CentralOrderBookEngine {
 
    // 1. NODE STRUCTURE
    static class OrderNode {
        int orderId;
        String client;
        double price;
        int quantity;
        String side; 
       
        OrderNode prev;
        OrderNode next;
 
        public OrderNode(int orderId, String client, double price, int quantity, String side) {
            this.orderId = orderId;
            this.client = client;
            this.price = price;
            this.quantity = quantity;
            this.side = side;
        }
    }
 
    // 2. DOUBLY LINKED LIST
    static class OrderQueue {
        OrderNode head;
        OrderNode tail;
        int totalVolume;
 
        public OrderQueue() {
            head = new OrderNode(0, null, 0, 0, null);
            tail = new OrderNode(0, null, 0, 0, null);
            head.next = tail;
            tail.prev = head;
            this.totalVolume = 0;
        }
 
        public void append(OrderNode node) {
            OrderNode lastNode = tail.prev;
            lastNode.next = node;
            node.prev = lastNode;
            node.next = tail;
            tail.prev = node;
            totalVolume += node.quantity;
        }
 
        public void remove(OrderNode node) {
            if (node.prev != null && node.next != null) {
                node.prev.next = node.next;
                node.next.prev = node.prev;
                totalVolume -= node.quantity;
                node.prev = null;
                node.next = null;
            }
        }
 
        public boolean isEmpty() {
            return head.next == tail;
        }
    }
 
    // 3. CENTRAL ENGINE
    public static class CentralOrderBook {
        private HashMap<Integer, OrderNode> orderMap;
        private TreeMap<Double, OrderQueue> buyTree; 
        private TreeMap<Double, OrderQueue> sellTree;
 
        public CentralOrderBook() {
            orderMap = new HashMap<>();
            buyTree = new TreeMap<>(Collections.reverseOrder());
            sellTree = new TreeMap<>();
        }
 
        public void submitOrder(int orderId, String client, double price, int quantity, String side) {
            OrderNode newNode = new OrderNode(orderId, client, price, quantity, side);
 
            if ("BUY".equalsIgnoreCase(side)) {
                while (quantity > 0 && !sellTree.isEmpty() && sellTree.firstKey() <= price) {
                    double bestAskPrice = sellTree.firstKey();
                    OrderQueue askQueue = sellTree.get(bestAskPrice);
                   
                    OrderNode matchNode = askQueue.head.next;
                    int matchQty = Math.min(quantity, matchNode.quantity);
 
                    System.out.printf("   [TRADE] Match Found! %s bought %d units from %s @ ₹%.2f\n",
                                      client, matchQty, matchNode.client, bestAskPrice);
 
                    quantity -= matchQty;
                    matchNode.quantity -= matchQty;
                    askQueue.totalVolume -= matchQty;
 
                    if (matchNode.quantity == 0) {
                        askQueue.remove(matchNode);
                        orderMap.remove(matchNode.orderId);
                    }
 
                    if (askQueue.isEmpty()) {
                        sellTree.remove(bestAskPrice);
                    }
                }
 
                if (quantity > 0) {
                    newNode.quantity = quantity;
                    buyTree.putIfAbsent(price, new OrderQueue());
                    buyTree.get(price).append(newNode);
                    orderMap.put(orderId, newNode);
                }
 
            } else if ("SELL".equalsIgnoreCase(side)) {
                while (quantity > 0 && !buyTree.isEmpty() && buyTree.firstKey() >= price) {
                    double bestBidPrice = buyTree.firstKey();
                    OrderQueue bidQueue = buyTree.get(bestBidPrice);
 
                    OrderNode matchNode = bidQueue.head.next;
                    int matchQty = Math.min(quantity, matchNode.quantity);
 
                    System.out.printf("   [TRADE] Match Found! %s sold %d units to %s @ ₹%.2f\n",
                                      client, matchQty, matchNode.client, bestBidPrice);
 
                    quantity -= matchQty;
                    matchNode.quantity -= matchQty;
                    bidQueue.totalVolume -= matchQty;
 
                    if (matchNode.quantity == 0) {
                        bidQueue.remove(matchNode);
                        orderMap.remove(matchNode.orderId);
                    }
 
                    if (bidQueue.isEmpty()) {
                        buyTree.remove(bestBidPrice);
                    }
                }
 
                if (quantity > 0) {
                    newNode.quantity = quantity;
                    sellTree.putIfAbsent(price, new OrderQueue());
                    sellTree.get(price).append(newNode);
                    orderMap.put(orderId, newNode);
                }
            }
        }
 
        public void cancelOrder(int orderId) {
            if (!orderMap.containsKey(orderId)) {
                return;
            }
 
            OrderNode node = orderMap.get(orderId);
            double price = node.price;
 
            if ("BUY".equalsIgnoreCase(node.side)) {
                OrderQueue queue = buyTree.get(price);
                queue.remove(node);
                if (queue.isEmpty()) buyTree.remove(price);
            } else {
                OrderQueue queue = sellTree.get(price);
                queue.remove(node);
                if (queue.isEmpty()) sellTree.remove(price);
            }
 
            orderMap.remove(orderId);
            System.out.println("   [CANCEL] Order " + orderId + " removed from book.");
        }
 
        public void printState() {
            System.out.println("\n--- LIVE ORDER BOOK DEPTH MAP ---");
            System.out.println(" ASKS (Sells):");
            Double[] sellPrices = sellTree.keySet().toArray(new Double[0]);
            int askCount = 0;
            for (int i = 0; i < sellPrices.length && askCount < 3; i++) {
                System.out.printf("   ₹%.2f -> Vol: %d\n", sellPrices[i], sellTree.get(sellPrices[i]).totalVolume);
                askCount++;
            }
            System.out.println(" -------- SPREAD LINE --------");
            System.out.println(" BIDS (Buys):");
            int bidCount = 0;
            for (double price : buyTree.keySet()) {
                if (bidCount >= 3) break;
                System.out.printf("   ₹%.2f -> Vol: %d\n", price, buyTree.get(price).totalVolume);
                bidCount++;
            }
            System.out.println("---------------------------------\n");
        }
    }
 
    // --- STREAM RUNNER ---
    public static void main(String[] args) {
        CentralOrderBook book = new CentralOrderBook();
       
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("ACTION")) continue;
 
                String[] tokens = line.split(",");
                if (tokens.length < 5) continue;
 
                String action = tokens[0];
                String side = tokens[1];
                int orderId = Integer.parseInt(tokens[2]);
                double price = Double.parseDouble(tokens[3]);
                int quantity = Integer.parseInt(tokens[4]);
 
                System.out.println("[INCOMING STREAM] -> " + line);
 
                if ("ADD".equals(action)) {
                    book.submitOrder(orderId, "Client_" + orderId, price, quantity, side);
                } else if ("CANCEL".equals(action)) {
                    book.cancelOrder(orderId);
                }
 
                book.printState();
            }
        } catch (Exception e) {
            System.err.println("Stream error: " + e.getMessage());
        }
    }
}