import socket
import json
UDP_IP  = "127.0.0.1"
UDP_PORT = 8002
MESSAGE = json.dumps({
        "name" : "chair",
        "typeV" : "furniture",
        "inputOptions" : {
                "pressure" : 3,
                "temp" : 64,
                "gyro" : 224
            }
    })

sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
sock.sendto(MESSAGE, (UDP_IP, UDP_PORT))
