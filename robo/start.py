import RPi.GPIO as gpio

import time

def init():
    
    gpio.setmode(gpio.BOARD)

    gpio.setup(7, gpio.OUT)

    gpio.setup(11, gpio.OUT)

    gpio.setup(13, gpio.OUT)

    gpio.setup(15, gpio.OUT)
def forward(num):
    
    init()
    
    gpio.output(7, True)

    gpio.output(11, False)

    gpio.output(13, False)

    gpio.output(15, True)

    time.sleep(num)
    
    gpio.cleanup()
forward(2)