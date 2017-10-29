(ns client
  "Client defstates"
  (:require
   [figwheel-sidecar.repl-api :as figwheel]
   [figwheel-sidecar.components.css-watcher :as figwheel.css-watcher]
   [ventas.util :as util]
   [clojure.java.shell]
   [me.raynes.conch.low-level :as sh]
   [async-watch.core :as watch]
   [mount.core :as mount :refer [defstate]]))

;; Figwheel

(alter-var-root
 #'figwheel.css-watcher/handle-css-notification
 (fn [_]
   (fn ventas-css-handler [watcher files]
     (when-let [changed-css-files (not-empty (filter #(.endsWith % ".css") (map str files)))]
       (let [figwheel-server (:figwheel-server watcher)
             sendable-files (map #(figwheel.css-watcher/make-css-file %) changed-css-files)]
         (figwheel.css-watcher/send-css-files figwheel-server sendable-files))))))

(defn figwheel-start []
  (util/print-info "Starting Figwheel")
  (figwheel/start-figwheel!))

(defn figwheel-stop []
  (util/print-info "Stopping Figwheel")
  (figwheel/stop-figwheel!))

(defstate figwheel :start (figwheel-start) :stop (figwheel-stop))


;; FQCSS

#_(defn fqcss-start []
    (print-info "Starting fqcss")
    (let [changes (watch/changes-in "src/fqcss")]
      (go (while true
            (let [[op filename] (<! changes)]
              (when (and (clojure.string/ends-with? filename ".scss") (or (= (name op) "modify")
                                                                          (= (name op) "create")))
                (print-info (str "Processing fqcss (" (name op) ")"))
                (let [new-path (clojure.string/replace filename "fqcss" "scss")]
                  (print-info (str "\tSpitting to: " new-path))
                  (spit new-path (fqcss/replace-css (slurp filename))))))))))

#_(defstate fqcss
            :start
            (fqcss-start)
            :stop
            (do
              (watch/cancel-changes)))


;; Sass

(def sass-process)
(defn sass-start []
  (future
   (util/print-info "Starting SASS")
   (alter-var-root #'sass-process (sh/proc "lein" "auto" "sassc" "once"))))

(defn sass-stop []
  (future
   (util/print-info "Stopping SASS")
   (sh/destroy sass-process)))

(defstate sass :start (sass-start) :stop (sass-stop))