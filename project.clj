(defproject sandpit "0.1.0-SNAPSHOT"
  :description "Experimental Clojure project for serial and hardware interaction"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}

  :dependencies [[org.clojure/clojure "1.12.0"]
                 ;; you must clone and compile/install the master branch of the
                 ;; following dep locally as the version 1.0.4 isn't published
                 ;; to maven.
                 [com.github.purejavacomm/purejavacomm "1.0.4"]
                 [clj-serial "2.0.5" :exclusions [com.github.purejavacomm/purejavacomm]]
                 [org.clojure/core.async "1.6.681"]
                 [clj-firmata "2.1.1" :exclusions [clj-serial org.clojure/core.async]]]

  :repl-options {:init-ns sandpit.core})
