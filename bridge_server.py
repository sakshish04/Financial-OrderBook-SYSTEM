import asyncio
import websockets
import subprocess

async def broadcast_java_engine(websocket):
    process = subprocess.Popen(
        "python data_generator.py | java CentralOrderBookEngine",
        shell=True,
        stdout=subprocess.PIPE,
        stderr=subprocess.STDOUT,
        text=True
    )
    
    while True:
        line = process.stdout.readline()
        if not line:
            break
        await websocket.send(line.strip())
        await asyncio.sleep(0.01)

async def main():
    print("Starting Bridge Server on ws://localhost:8765")
    async with websockets.serve(broadcast_java_engine, "localhost", 8765):
        await asyncio.Future()

if __name__ == "__main__":
    asyncio.run(main())