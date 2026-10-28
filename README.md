# 📈 NEO MATCH: Real-Time Triad Matching Engine

A full-stack, real-time High-Frequency Trading (HFT) order book engine and visualizer. This project demonstrates the practical application of advanced data structures to achieve lightning-fast order matching, price priority, and time priority (FIFO) execution.

## 🏗️ Architecture: The "Triad" Data Structure
At the core of the Java backend is a highly optimized Hybrid Triad architecture designed to handle thousands of concurrent orders with minimal latency:

1. **Red-Black Tree (`TreeMap`):** Maintains Price Priority. Asks (Sells) are sorted lowest-to-highest, and Bids (Buys) are sorted highest-to-lowest, ensuring O(log n) time complexity for finding the best available market price.
2. **Doubly Linked List (Custom `OrderQueue`):** Maintains Time Priority (FIFO). Orders at the same price point are appended to a linked list, allowing for O(1) constant-time execution and removal of matched trades.
3. **Hash Map (`HashMap`):** Enables O(1) constant-time order lookups and cancellations across the entire engine using unique Order IDs.

## 🚀 Features
* **Real-Time Matching:** Instantly crosses compatible Bid and Ask orders.
* **Live WebSocket Bridge:** A Python asynchronous server (`bridge_server.py`) that pipes the compiled Java engine's execution stream directly to the web browser.
* **Crypto-Exchange Visualizer:** A strictly vanilla HTML/CSS/JS dashboard featuring a dynamic spread line, hollow volume depth bars, and a live incoming stream ticker.
* **Automated Market Data:** Python-driven generator simulating rapid, randomized HFT market conditions.

## 💻 Tech Stack
* **Backend Engine:** Java (Vanilla)
* **Data Stream & Bridge:** Python 3.11 (`websockets`, `asyncio`, `subprocess`)
* **Frontend Dashboard:** HTML5, CSS3, Vanilla JavaScript (Zero frameworks)

## ⚙️ How to Run Locally

**1. Clone the repository:**
`git clone https://github.com/sakshish04/Financial-OrderBook-SYSTEM.git`
`cd Financial-OrderBook-SYSTEM`

**2. Compile the Java Engine:**
`javac CentralOrderBookEngine.java`

**3. Set up the Python Environment:**
`python -m venv .venv`
`.\.venv\Scripts\activate`
`pip install websockets`

**4. Start the Application:**
`python bridge_server.py`
*Leave the terminal running. Finally, open `index.html` in any modern web browser to view the live dashboard.*
