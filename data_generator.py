import time
import random
import sys

def generate_orders():
    print("ACTION,SIDE,ORDER_ID,PRICE,QUANTITY")
    order_id = 1000
    active_orders = []
    
    base_price = 100.0
    
    while True:
        action = random.choices(['ADD', 'CANCEL'], weights=[0.85, 0.15])[0]
        
        if action == 'CANCEL' and active_orders:
            cancel_id = random.choice(active_orders)
            print(f"CANCEL,NONE,{cancel_id},0,0")
            active_orders.remove(cancel_id)
        else:
            side = random.choice(['BUY', 'SELL'])
            price = round(random.gauss(base_price, 1.5), 2)
            quantity = random.randint(10, 500)
            
            print(f"ADD,{side},{order_id},{price},{quantity}")
            active_orders.append(order_id)
            order_id += 1
            
        sys.stdout.flush()
        time.sleep(0.15) # Adjust this to make the engine run faster or slower

if __name__ == "__main__":
    generate_orders()