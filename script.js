const socket = new WebSocket('ws://localhost:8765');

let isParsingBook = false;
let parseMode = ''; 
let currentAsks = [];
let currentBids = [];
let totalMarketValue = 0;

socket.onopen = () => console.log("Connected to Java Engine!");

socket.onmessage = function(event) {
    const data = event.data.trim();

    // 1. POPULATE LEFT COLUMN (Stream Formatting)
    if (data.includes("[INCOMING STREAM]")) {
        const parts = data.split('-> ')[1]; 
        if (parts) {
            const tokens = parts.split(',');
            if (tokens.length >= 3) {
                const action = tokens[0];
                const side = tokens[1] !== 'NONE' ? tokens[1] : ''; 
                const name = `Client_${tokens[2]}`;
                
                const actionClass = action === 'ADD' ? 't-add' : 't-cancel';
                const html = `<div class="ticker-row">
                    <span class="${actionClass}">${action}</span>
                    <span class="${actionClass}">${side}</span>
                    <span class="t-name">${name}</span>
                </div>`;
                
                const tickerBox = document.getElementById('ticker-box');
                tickerBox.innerHTML = html + tickerBox.innerHTML;
                
                // Simulate latency fluctuation
                document.getElementById('latency').innerText = Math.floor(Math.random() * (150 - 90 + 1)) + 90;
            }
        }
    }
    
    // 2. POPULATE RIGHT COLUMN (Trades & Value)
    else if (data.includes("[TRADE]")) {
        const tradesBox = document.getElementById('trades-box');
        tradesBox.innerHTML = `<div class="trade-row">${data.replace('[TRADE]', '')}</div>` + tradesBox.innerHTML;
        
        // Extract Price and Quantity to update Total Value (Handles both bought and sold)
        const match = data.match(/(?:bought|sold) (\d+) units.*@\s*[^\d]*([\d.]+)/);
        if (match) {
            const qty = parseInt(match[1]);
            const price = parseFloat(match[2]);
            totalMarketValue += (qty * price);
            document.getElementById('total-value').innerText = `₹${totalMarketValue.toLocaleString('en-IN', {maximumFractionDigits: 0})}`;
        }
    }

    // 3. POPULATE CENTER COLUMN (Order Book & Hollow Bars)
    else if (data.includes("--- LIVE ORDER BOOK DEPTH MAP ---")) {
        isParsingBook = true;
        currentAsks = [];
        currentBids = [];
    }
    else if (isParsingBook) {
        if (data.includes("ASKS (Sells):")) parseMode = 'asks';
        else if (data.includes("-------- SPREAD LINE --------")) parseMode = 'bids';
        else if (data.includes("---------------------------------")) {
            isParsingBook = false;
            renderOrderBook(currentAsks, currentBids);
        } 
        else if (data.includes("-> Vol:")) {
            // Bulletproof regex ignoring the terminal encoding bug
            const match = data.match(/([\d.]+)\s*->\s*Vol:\s*(\d+)/);
            if (match) {
                const price = parseFloat(match[1]);
                const vol = parseInt(match[2]);
                if (parseMode === 'asks') currentAsks.push({ price, vol });
                else if (parseMode === 'bids') currentBids.push({ price, vol });
            }
        }
    }
};

function renderOrderBook(asks, bids) {
    asks.reverse(); // Asks show highest to lowest
    
    // Find max volume to calculate bar widths
    let maxVol = 0;
    asks.forEach(a => maxVol = Math.max(maxVol, a.vol));
    bids.forEach(b => maxVol = Math.max(maxVol, b.vol));
    if (maxVol === 0) maxVol = 1; 

    let asksHtml = '';
    asks.forEach(ask => {
        const widthPercent = (ask.vol / maxVol) * 100;
        asksHtml += `
            <div class="book-row">
                <span class="ask-text">₹${ask.price.toFixed(2)}</span>
                <span class="vol-text">${ask.vol}</span>
                <div class="bar-container"><div class="ask-bar" style="width: ${widthPercent}%;"></div></div>
            </div>`;
    });
    document.getElementById('asks-container').innerHTML = asksHtml;

    let bidsHtml = '';
    bids.forEach(bid => {
        const widthPercent = (bid.vol / maxVol) * 100;
        bidsHtml += `
            <div class="book-row">
                <span class="bid-text">₹${bid.price.toFixed(2)}</span>
                <span class="vol-text">${bid.vol}</span>
                <div class="bar-container"><div class="bid-bar" style="width: ${widthPercent}%;"></div></div>
            </div>`;
    });
    document.getElementById('bids-container').innerHTML = bidsHtml;

    // Update Spread Line
    if (asks.length > 0) {
        document.getElementById('spread-line').innerText = `SPREAD LINE (${asks[asks.length - 1].price.toFixed(2)})`;
    }
}