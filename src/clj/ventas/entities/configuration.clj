(ns ventas.entities.configuration
  (:refer-clojure :exclude [get])
  (:require
   [clojure.spec.alpha :as spec]
   [ventas.database.entity :as entity]
   [ventas.database.generators :as generators]
   [ventas.utils :as utils]
   [clojure.set :as set]))

(spec/def :configuration/keyword ::generators/keyword)
(spec/def :configuration/value ::generators/string)
(spec/def :configuration/allowed-user-roles
  (spec/coll-of keyword?))

(spec/def :schema.type/configuration
  (spec/keys :req [:configuration/keyword
                   :configuration/value]
             :opt [:configuration/value
                   :configuration/allowed-user-roles]))

(entity/register-type!
 :configuration
 {:attributes
  [{:db/ident :configuration/keyword
    :db/valueType :db.type/keyword
    :db/cardinality :db.cardinality/one
    :db/unique :db.unique/identity}

   {:db/ident :configuration/value
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one}

   {:db/ident :configuration/allowed-user-roles
    :db/valueType :db.type/ref
    :db/cardinality :db.cardinality/many}]

  :fixtures
  (fn []
    [{:configuration/keyword :site.title
      :configuration/value "Ventas Dev Store"}])})

(defn get
  "Gets a configuration key or a collection of configuration keys.
   `user` will be used for checking the roles when the configuration key has them defined."
  [k-or-ks & [user]]
  (if (coll? k-or-ks)
    (->> k-or-ks
         (map (fn [id]
                (when-let [v (get id)]
                  [id v])))
         (remove nil?)
         (into {}))
    (let [{:configuration/keys [value allowed-user-roles]} (entity/find [:configuration/keyword k-or-ks])]
      (when (and (seq allowed-user-roles)
                 (not (set/subset? (set (:user/roles user))
                                   (set allowed-user-roles))))
        (throw (Exception. (str "Access denied to configuration key " k-or-ks))))
      (utils/swallow
       (read-string value)))))

(defn register-key!
  "Registers a configuration key.
   Only needed if you want to define `allowed-user-roles`, which makes the given
   key private except for the given roles."
  [k & [allowed-user-roles]]
  {:pre [(or (not allowed-user-roles) (set allowed-user-roles))]}
  (entity/create* (merge
                   {:schema/type :schema.type/configuration
                    :configuration/keyword k}
                   (when allowed-user-roles
                     {:configuration/allowed-user-roles allowed-user-roles}))))

(defn set! [k v]
  "Sets to `v` the `k` configuration key."
  (entity/create* {:schema/type :schema.type/configuration
                   :configuration/keyword k
                   :configuration/value (pr-str v)}))