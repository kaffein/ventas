(ns ventas.themes.clothing.components.header
  (:require
   [reagent.core :as reagent]
   [re-frame.core :as rf]
   [ventas.routes :as routes]
   [ventas.components.base :as base]
   [ventas.i18n :refer [i18n]]
   [ventas.api :as api]))

(rf/reg-sub
 :resources/logo
 (fn [db _] (-> db :resources :logo)))

(rf/reg-event-fx
 :resources/logo
 (fn [cofx [_]]
   {:dispatch [:api/resources.get
               {:params {:keyword :logo}
                :success #(rf/dispatch [:ventas/db [:resources :logo] %])}]}))

(rf/reg-sub
 ::opened
 (fn [db _]
   (-> db ::opened)))

(rf/reg-event-db
 ::toggle
 (fn [db [_]]
   (update db ::opened not)))

(rf/reg-event-db
 ::close
 (fn [db [_]]
   (assoc db ::opened false)))

(defn header []
  (rf/dispatch [:ventas/configuration.get :site.title])
  (rf/dispatch [:ventas/resources.get :logo])
  (fn []
    [:div.skeleton-header
     [:div.ui.container
      [:div.skeleton-header__logo
       (let [title @(rf/subscribe [:ventas/db [:configuration :site.title]])
             logo @(rf/subscribe [:ventas/db [:resources :logo]])]
         [:a {:title (:value title)
              :href (-> js/window (.-location) (.-origin))}
          [:img {:src "resources/logo"}]])]
      [:div.skeleton-header__right
       [:div.skeleton-header__buttons
        [:button {:on-click #(routes/go-to :frontend.cart)}
         [base/icon {:name "add to cart"}]
         (i18n ::my-cart)]
        [:button {:on-click #(routes/go-to :frontend.login)
                  :on-blur #(rf/dispatch [::close])}
         [base/icon {:name "user"}]
         (i18n ::my-account)
         [base/icon {:name "caret down"
                     :on-click (fn [e] (-> e (.stopPropagation))
                                 (rf/dispatch [::toggle]))}]
         [base/menu {:vertical true
                     :class (str "skeleton-header__user-menu "
                                 (if @(rf/subscribe [::opened])
                                   "visible"
                                   "unvisible"))}
          [base/menuItem {:on-click #(rf/dispatch [:ventas/session.stop])}
           (i18n ::logout)]]]]]]]))