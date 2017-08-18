(ns ventas.entities.order
  (:require [clojure.spec :as s]
            [clojure.test.check.generators :as gen]
            [com.gfredericks.test.chuck.generators :as gen']
            [ventas.database :as db]
            [ventas.database.entity :as entity]))

#_"
  Orders have:
  - An user
  - A list of products with their variations and quantities
  - A status
  - Billing and shipping addresses
  - A shipping method (maybe with comments)
  - A payment method
"

(s/def :order/user
  (s/with-gen integer? #(gen/elements (map :id (entity/query :user)))))

(s/def :order/status #{:order.status/unpaid
                       :order.status/paid
                       :order.status/acknowledged
                       :order.status/ready
                       :order.status/shipped})

(s/def :order/shipping-address
  (s/with-gen integer? #(gen/elements (map :id (entity/query :address)))))

(s/def :order/billing-address
  (s/with-gen integer? #(gen/elements (map :id (entity/query :address)))))

(s/def :order/shipping-method keyword?)
(s/def :order/shipping-comments string?)

(s/def :order/payment-method keyword?)
(s/def :order/payment-reference string?)

(s/def :schema.type/order
  (s/keys :req [:order/user
                :order/status
                :order/shipping-address
                :order/billing-address
                :order/shipping-method
                :order/payment-method]
          :opt [:order/shipping-comments
                :order/payment-reference]))

(defmethod entity/json :order [entity]
  (as-> entity entity
        (dissoc entity :type)
        (if-let [user (:user entity)]
          (assoc entity :user (entity/json (entity/find user)))
          entity)
        (if-let [shipping-address (:shipping-address entity)]
          (assoc entity :shipping-address (entity/json (entity/find shipping-address)))
          entity)
        (if-let [billing-address (:billing-address entity)]
          (assoc entity :billing-address (entity/json (entity/find billing-address)))
          entity)
        (if-let [lines (:lines entity)]
          (assoc entity :lines (map #(entity/json (entity/find %)) lines))
          entity)))