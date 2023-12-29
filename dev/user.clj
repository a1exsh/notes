(ns user
  (:require [nextjournal.clerk :as clerk]))

(comment
  ;; start without file watcher, open browser when started
  (clerk/serve! {:browse? true :port 6677})

  ;; start with file watcher for these sub-directory paths
  (clerk/serve! {:watch-paths ["notebooks"] :port 6677})

  ;; start with file watcher and a `show-filter-fn` function to watch
  ;; a subset of notebooks
  (clerk/serve! {:watch-paths ["notebooks" "src"] :show-filter-fn #(clojure.string/starts-with? % "notebooks")})

  (clerk/clear-cache!)
  ;; stop
  (clerk/halt!)

  ;; or call `clerk/show!` explicitly
  (clerk/show! "notebooks/macros.clj")
  (clerk/show! "notebooks/four_twos.clj")
  (clerk/show! "notebooks/literal_compare.clj")

  ;;(clerk/show! "index.md")
  )
