
import RPi.GPIO as gpio

import time

import pyrebase

from firebase import firebase

from time import sleep

config = {
  "apiKey": "AIzaSyBJ2GIuBXhF7qjWfrHqXZ_guYlkj5TQhRg",
  "authDomain": "thirdeye-3ec17.firebaseapp.com",
  "databaseURL": "https://thirdeye-3ec17.firebaseio.com",
  "projectId": "thirdeye-3ec17",
  "storageBucket": "thirdeye-3ec17.appspot.com",
  "messagingSenderId": "681490047688",
  "appId": "1:681490047688:web:70f00fac7172cdfa33bbfd"
}

firebase = pyrebase.initialize_app(config)

def init():
    
    gpio.setmode(gpio.BOARD)

    gpio.setup(7, gpio.OUT)

    gpio.setup(11, gpio.OUT)

    gpio.setup(13, gpio.OUT)

    gpio.setup(15, gpio.OUT)


def backward(num):
    
    init()
    
    gpio.output(7, False)

    gpio.output(11, True)

    gpio.output(13, True)

    gpio.output(15, False)

    time.sleep(num)
    
    gpio.cleanup()

def forward(num):
    
    init()
    
    gpio.output(7, True)

    gpio.output(11, False)

    gpio.output(13, False)

    gpio.output(15, True)

    time.sleep(num)
    
    gpio.cleanup()

def left(num):
    
    init()
    
    gpio.output(7, True)

    gpio.output(11, True)

    gpio.output(13, False)

    gpio.output(15, True)

    time.sleep(num)
    
    gpio.cleanup()

def right(num):
    
    init()
    
    gpio.output(7, True)

    gpio.output(11, False)

    gpio.output(13, False)

    gpio.output(15, False)

    time.sleep(num)
    
    gpio.cleanup()

#from firebase import firebase
#result2 = firebase.get('https://thirdeye-3ec17.firebaseio.com/SptoTx','')
#print(result2)
#backward()
#forward()
#left()
#right()
while True:

        database = firebase.database()
        status = database.child("status2").get().val()

        if status == "true":
            print(status)
            di = database.child("direction").get().val()
            print(di)
            sp = database.child("speed").get().val()
            sp=sp.strip()
            sp = float(sp.replace('"',''))
            
            if di=="F1":
                forward(sp)
                database.child("").update({"direction":"l"})
                print("front 1")
            
            if di.strip()=="F2":
                right(2)
                forward(sp)
                database.child("").update({"direction":"l"})
                print("front 2")
            
            
            if di.strip()=="R1":
                right(4)
                forward(sp)
                database.child("").update({"direction":"l"})
                print("right 1")
                
            if di.strip()=="R2":
                left(2)
                backward(sp+2)
                database.child("").update({"direction":"l"})
                print("right 2")
            
            if di=="B1":
                database.child("").update({"direction":"l"})
                backward(sp)
                print("back 1")
                
            if di=="B2":
                database.child("").update({"direction":"l"})
                left(2)
                backward(sp)
                print("back")
            
                
            if di=="L1":
                left(2)
                forward(sp)
                data = {"direction":"l"}
                database.child("").update({"direction":"l"})
                print("left 1")
            
            if di=="L2":
                left(4)
                forward(sp)
                data = {"direction":"l"}
                database.child("").update({"direction":"l"})
                print("left 2")
            
            
            
            if di.strip()=='"stop"':
                database.child("").update({"direction":"l"})
                print("stoping")
                break
            if di.strip()=='"l"':
                database.child("").update({"direction":"l"})
            print("false")
            database.child("").update({"status2":"false"})
            break




