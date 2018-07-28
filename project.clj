(defproject puzzle "0.1.0-SNAPSHOT"
  :plugins [[lein-cljsbuild "LATEST"]]
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [org.clojure/clojurescript "1.10.339"]
                 [reagent "0.8.1"]]
  :cljsbuild {:builds
              [{:id "dev"
                :source-paths ["cljs-src"]
                :compiler
                {:output-to "static/development/index.js"
                 :source-map true
                 :output-dir "static/development/js"
                 :optimizations :none
                 :main puzzle.cljs.core
                 :asset-path "/development/js"
                 :cache-analysis true
                 :pretty-print true}}
               {:id "release"
                :source-paths ["cljs-src"]
                :compiler
                {:output-to "static/release/index.js"
                 :source-map "static/release/index.js.map"
                 :main puzzle.cljs.core
                 :output-dir "static/release/js"
                 :optimizations :advanced
                 :pseudo-names false}}]})
