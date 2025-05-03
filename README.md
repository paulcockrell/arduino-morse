# Arduino-Morse - Clojure + MorseCode + Arduino

Using firmata to issue commands to an arduino board. This is not running
clj/cljs projects 'on' the arduino board.

Original code taken from
[dayooliyide](http://dayooliyide.com/post/clojure-arduino-part1/) and updated
to work with current Clojure and the project dependencies

## Setup Arduino

I am using Elegoo nano arduino, which requires installing their own driver to
support their chips. Download and install the driver from
[here](https://www.elegoo.com/en-gb/blogs/arduino-projects/elegoo-arduino-nano-board-ch340-usb-driver)

## Prerequisits

### Install Arduion IDE

Install the [Arduino IDE](https://www.arduino.cc/en/software/), as we need to
flash the Arduino with the Firmata firmware which comes with the examples in
the IDE

### Build and install Clojure dependencies

Some of the dependencies of this project are out of data, unmaintained and
depend on stuff that no longer works with modern systems.

So we must download and install the following ourselves.

```bash
git clone https://github.com/nyholku/purejavacomm
cd purejavacomm
mvn install
```

This will install from the master branch and install the version 1.0.4, which
is not published on Maven. This version has an update in its pom.xml to use JNA
version 5.13.0 which is what we require.

## Compile and load the Firmata code

Before you can see the example listed in the Arduino IDE you must connect the
board.

1. Connect the board physically to the computer
1. Set the board config, my setup with the Elegoo nano (v3) is as follows:
1. Board: `Arduino Nano`
1. Port: `/dev/cu.usbserial-2110`
1. Processor: `ATmega328P (old bootloader)`

Now you can upload the Firmata firmware:

Go to:

1. File
1. Firmware
1. Examples
1. Firmata
1. StandardFirmata

This will open the sketch, which you can now compile and upload in the Arduino
IDE (the Right Arrow button)

## Run this project to flash morse code messages on the Arduio's LED

Start the repl in the project root

```bash
lein repl
```

Initialise the board and send some morse code

```clj
(def b (init-board! {:port-name "cu.usbserial-2110" :led-pin 13}))
(morse! b "sms") ;; dot dot dot dash dash dot dot dot
```
