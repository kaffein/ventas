(ns ventas.themes.clothing.pages.frontend.cart
  (:require
   [re-frame.core :as rf]
   [ventas.themes.clothing.components.skeleton :refer [skeleton]]
   [ventas.utils :as util]
   [ventas.components.cart :as cart]
   [ventas.routes :as routes]
   [ventas.components.base :as base]
   [ventas.i18n :refer [i18n]]
   [ventas.events :as events]
   [ventas.themes.clothing.components.heading :as theme.heading]
   [ventas.events.backend :as backend]
   [ventas.utils :as utils]
   [ventas.utils.formatting :as utils.formatting]))

(rf/reg-event-fx
 ::add-voucher
 (fn [cofx event]
   ;; @TODO
   ))

(rf/reg-event-fx
 ::checkout
 (fn [cofx event]
   ;; @TODO
   ))

(rf/reg-sub
 ::line-count
 (fn [_]
   (rf/subscribe [::cart/main]))
 (fn [{:keys [lines]}]
   (count lines)))

(rf/reg-sub
 ::subtotal
 (fn [_]
   (rf/subscribe [::cart/main]))
 (fn [{:keys [lines]}]
   (reduce +
           (map #(get-in % [:product-variation :price :amount])
                lines))))

(rf/reg-sub
 ::shipping
 (fn [_]
   (rf/subscribe [::cart/main]))
 (fn [cart]
   ;; @TODO
   0))

(rf/reg-sub
 ::total
 (fn [_]
   [(rf/subscribe [::subtotal])
    (rf/subscribe [::shipping])])
 (fn [[subtotal shipping]]
   (+ subtotal shipping)))

(defn cart-sidebar []
  (let [voucher (atom nil)]
    (fn []
      [:div.cart-page__sidebar
       [:div.cart-page__sidebar-heading (i18n ::total)]
       [:div.cart-page__sidebar-content
        [:div.cart-page__discount
         [:p (i18n ::add-voucher)]
         [base/form-input {:on-change (utils/value-handler
                                       #(reset! voucher %))}]
         [base/button {:type "button"
                       :on-click #(rf/dispatch [::add-voucher])}
          (i18n ::add)]]
        [:table.cart-page__totals
         [:tbody
          [:tr
           [:td (str (i18n ::subtotal))]
           [:td (let [subtotal @(rf/subscribe [::subtotal])]
                  (utils.formatting/format-number subtotal))]]
          [:tr
           [:td (str (i18n ::shipping))]
           [:td (let [amount @(rf/subscribe [::shipping])]
                  (if (pos? amount)
                    (utils.formatting/format-number amount)
                    (i18n ::free)))]]
          [:tr
           [:td (str (i18n ::total))]
           [:td (let [total @(rf/subscribe [::total])]
                  (utils.formatting/format-number total))]]]]

        [base/button {:type "button"
                      :class "cart-page__checkout"
                      :on-click #(rf/dispatch [::checkout])}
         (i18n ::checkout)]]])))

(defn cart-line-view [{:keys [product-variation quantity]}]
  [:div.cart-page__line
   [:div.cart-page__line-thumbnail
    [:img {:src (str "/images/" (-> product-variation :images first :id) "/resize/cart-page-line")}]]
   [:div.cart-page__line-content
    [:div.cart-page__name
     [:h4 (:name product-variation)]]
    [:div.cart-page__price
     [:h4 (str (utils.formatting/format-number (get-in product-variation [:price :amount]))
               " " (get-in product-variation [:price :currency :symbol]))]]
    [:div.cart-page__price
     [:h4 (str (i18n ::total) ": "
               (utils.formatting/format-number (get-in product-variation [:price :amount]))
               " " (get-in product-variation [:price :currency :symbol]))]]
    [:table.cart-page__terms
     [:tbody
      (let [variation-data (get product-variation :variation)]
        (for [{:keys [taxonomy selected]} variation-data]
          [:tr
           [:td (:name taxonomy)]
           [:td (:name selected)]]))]]

    [:div.cart-page__actions
     [base/button {:icon true
                   :basic true
                   :color "red"
                   :on-click #(rf/dispatch [::remove])}
      [base/icon {:name "trash"}]]
     [base/button {:icon true
                   :basic true
                   :color "red"
                   :on-click #(rf/dispatch [::remove])}
      [base/icon {:name "heart empty"}]]
     [base/select {:default-value quantity
                   :options (clj->js (for [n (range 1 16)]
                                       {:value n :text (str n)}))}]]]])

(defn page []
  (rf/dispatch [::cart/get])
  (fn []
    [skeleton
     [base/container
      [:div.cart-page
       [:h2 (i18n ::cart)]
       [:div.cart-page__content
        [:div.cart-page__lines
         (let [{:keys [lines]} @(rf/subscribe [::cart/main])]
           (for [line lines]
             [cart-line-view line]))]
        [cart-sidebar]]]]]))

(routes/define-route!
 :frontend.cart
 {:name ::page
  :url ["cart"]
  :component page})