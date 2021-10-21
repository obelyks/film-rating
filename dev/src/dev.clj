(ns dev
  (:refer-clojure :exclude [test])
  (:require [clojure.repl :refer :all]
            [fipp.edn :refer [pprint]]
            [clojure.tools.namespace.repl :refer [refresh]]
            [clojure.java.io :as io]
            [duct.core :as duct]
            [duct.core.repl :as duct-repl :refer [auto-reset]]
            [eftest.runner :as eftest]
            [integrant.core :as ig]
            [integrant.repl :refer [clear halt go init prep reset]]
            [integrant.repl.state :refer [config system]]))

(duct/load-hierarchy)

(defn read-config []
  (duct/read-config (io/resource "film_ratings/config.edn")))

(defn test []
  (eftest/run-tests (eftest/find-tests "test")))

(def profiles
  [:duct.profile/dev :duct.profile/local])

(clojure.tools.namespace.repl/set-refresh-dirs "dev/src" "src" "test")

(when (io/resource "local.clj")
  (load "local"))

;;(integrant.repl/set-prep! #(duct/prep-config (read-config) profiles))

(defn remove-prod-database-attributes
  "The prepared config is a merge of dev and prod config and the prod attributes for
  everything except :jdbc-url need to be dropped or the sqlite db is
  configured with postgres attributes"
  [config]
  (update config :duct.database.sql/hikaricp
          (fn [db-config] (->> (find db-config :jdbc-url) (apply hash-map)))))

(integrant.repl/set-prep!
  (comp remove-prod-database-attributes #(duct/prep-config (read-config) profiles)))