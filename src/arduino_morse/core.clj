(ns arduino-morse.core
  (:require [firmata.core :as fm]
            [firmata.receiver :as fmr]
            [clojure.core.async :as async]
            [clojure.string]))

(def ^:private ONBOARD-LED-PIN 13) ;; 13 is the onboard LED

(def dot-duration 50)
(def dash-duration (* 3 dot-duration))

(defn init-board!
  ;; Find out the port by running `ls /dev/tty*|grep userserial`
  ([] (init-board! {:port-name "cu.usbserial-2110" :led-pin ONBOARD-LED-PIN}))

  ([{:keys [port-name led-pin]
     :or {port-name :auto-detect
          led-pin ONBOARD-LED-PIN}}]
   (-> (fm/open-serial-board port-name)
       (fm/set-pin-mode led-pin :output))))

(defn signal-interval
  ([]
   (Thread/sleep dot-duration))
  ([_]
   (Thread/sleep dot-duration)))

(defn char-interval []
  (Thread/sleep dash-duration))

(defn space-interval []
  (Thread/sleep (* 7 dot-duration)))

(defn blink!
  ([board led-pin duration]
   (fm/set-digital board led-pin :high)
   (Thread/sleep duration)
   (fm/set-digital board led-pin :low)
   nil))

(defn dot!
  ([board]
   (dot! board ONBOARD-LED-PIN dot-duration))

  ([board led-pin duration]
   (blink! board led-pin duration)))

(defn dash!
  ([board]
   (dash! board ONBOARD-LED-PIN dash-duration))

  ([board led-pin duration]
   (blink! board led-pin duration)))

(def signal-map {\A [dot! dash!]
                 \B [dash! dot! dot! dot!]
                 \C [dash! dot! dash! dot!]
                 \D [dash! dot! dot!]
                 \E [dot!]
                 \F [dot! dot! dash! dot!]
                 \G [dash! dash! dot!]
                 \H [dot! dot! dot! dot!]
                 \I [dot! dot!]
                 \J [dot! dash! dash! dash!]
                 \K [dash! dot! dash!]
                 \L [dot! dash! dot! dot!]
                 \M [dash! dash!]
                 \N [dash! dot!]
                 \O [dash! dash! dash!]
                 \P [dot! dash! dash! dot!]
                 \Q [dash! dash! dot! dash!]
                 \R [dot! dash! dot!]
                 \S [dot! dot! dot!]
                 \T [dash!]
                 \U [dot! dot! dash!]
                 \V [dot! dot! dot! dash!]
                 \W [dot! dash! dash!]
                 \X [dash! dot! dot! dash!]
                 \Y [dash! dot! dash! dash!]
                 \Z [dash! dash! dot! dot!]
                 \1 [dot! dash! dash! dash! dash!]
                 \2 [dot! dot! dash! dash! dash!]
                 \3 [dot! dot! dot! dash! dash!]
                 \4 [dot! dot! dot! dot! dash!]
                 \5 [dot! dot! dot! dot! dot!]
                 \6 [dash! dot! dot! dot! dot!]
                 \7 [dash! dash! dot! dot! dot!]
                 \8 [dash! dash! dash! dot! dot!]
                 \9 [dash! dash! dash! dash! dot!]
                 \0 [dash! dash! dash! dash! dash!]})

(defn signal-char!
  ([board char]
   (when-let [signals (get signal-map char)]
     (doseq [signal-call (interpose (fn [_] (signal-interval)) signals)]
       (signal-call board)))))

(defn signal-word!
  ([board word]
   (let [char-call (fn [char] (fn [] (signal-char! board char)))
         calls (map char-call word)]
     (doseq [f (interpose char-interval calls)]
       (f)))))

(defn signal-word-seq!
  ([board words-seq]
   (let [words-seq (if (sequential? words-seq) words-seq [words-seq])
         word-call (fn [word] (fn [] (signal-word! board word)))
         calls (map word-call words-seq)]
     (doseq [f (interpose space-interval calls)]
       (f)))))

(defn morse!
  ([board s]
   (assert  (re-matches #"[0-9a-zA-Z\ ]+" s) "Supporting only a subset of the Morse code i.e letters, numbers and space !")
   (let [ss (clojure.string/upper-case s)
         words (clojure.string/split ss #"\s+")]
     (signal-word-seq! board words))))

(defn get-firmware-info
  [board]
  (let [ch    (fm/event-channel board)
        _     (fm/query-firmware board)
        event (async/<!! ch)]
    (prn event)
    (fm/release-event-channel board ch)))

(defn enable-digital-pin-reporting
  "Enable a specific pin to report events"
  [board pin]
  (-> board
      (fm/enable-digital-port-reporting pin true)))

(defn register-button
  "Register a pin as an input and enable digital reporting on it"
  [board btn-pin]

  (-> board
      (fm/set-pin-mode btn-pin :input)
      (enable-digital-pin-reporting btn-pin)))

(defn register-event
  "Register a callback for events on a given pin"
  [board btn-pin callback]
  (fmr/on-digital-event board btn-pin callback))

(defn register-led
  "Register pin as an output"
  [board led-pin]
  (fm/set-pin-mode board led-pin :output))

(comment
  ;; --- General use example ---

  ;; Declare pin numbers for external components
  (def custom-led-pin 2)
  (def custom-btn-pin 4)

  ;; AUTO board initialization
  (def board (init-board!))

  ;; MANUAL board initialization
  ;; Find out the board port by running `ls /dev/tty*|grep userserial`
  (def board (init-board! {:port-name "cu.usbserial-2110" :led-pin custom-led-pin}))

  ;; Register a button
  (register-button board custom-btn-pin)

  ;; Register a led
  (register-led board custom-led-pin)

  ;; Register event on the button to flash led
  (register-event board custom-btn-pin
                  (fn [event]
                    (println "Event received:" event)
                    (when (= :high (:value event))
                      (blink! board custom-led-pin 500))))

  ;; --- Morse code stuff ---

  ;; test blink
  (blink! board ONBOARD-LED-PIN 100)
  (blink! board ONBOARD-LED-PIN 1000)

  ;; test dot dash
  (dot! board)
  (dash! board)

  ;; send code 'manually'
  (-> board
      (dot!)
      (signal-interval)
      (dot!)
      (signal-interval)
      (dash!)
      (signal-interval)
      (dash!))

  ;; send single morse-code message
  (morse! board "SMS")

  ;; Morse Code Speed
  ;; There is no agreed universal duration for the dot signal.
  ;; The minimum morse speed to qualify for a Grade II license is 5 words per minute (5 wpm).
  ;; A word is made of 5 letters/characters and the words are interposed by space.
  ;; Thus the time taken to encode "MORSE WORDS MORSE WORDS 12345" should be
  ;; less than/equal to 60s.
  (time
   (morse! board "MORSE WORDS MORSE WORDS 12345"))

  ;;(release-event-channel @BOARD ch)
  (fm/close! board))
