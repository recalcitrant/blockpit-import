(ns blockpit.bittrex
  (:use [dk.ative.docjure.spreadsheet]
        [blockpit.header])
  (:require [clojure.string :as str])
  (:import (java.time LocalDateTime)
           (java.time.format DateTimeFormatter)))

(defn from [sheet handle-fn]
  (into []
        (->>
          (load-workbook-from-resource sheet)
          (select-sheet "bittrex")
          (row-seq)
          (rest)
          (remove nil?)
          (map cell-seq)
          (map #(map read-cell %))
          (map #(handle-fn %))
          )))

(defn to [file-name data]
  (let [wb (create-workbook "bybit trx history" data)]
    (dorun (for [sheet (sheet-seq wb)]
             (auto-size-all-columns! sheet)))
    (save-workbook! file-name wb))
  )

(defn read-write [file-to file-from handle-fn]
  (to file-to (into [] (cons header (from file-from handle-fn)))))

(defn format-date [date]
  (.format
    (LocalDateTime/parse date (DateTimeFormatter/ofPattern "yyyy-MM-dd HH:mm:ssz"))
    (DateTimeFormatter/ofPattern "dd.MM.yyyy HH:mm:ss")))

(defn handle-closed-deposits [row]
  (let [date (format-date (nth row 4))
        integration-name "Bittrex CSV"
        label "Deposit"
        asset (nth row 1)
        quantity (nth row 2)
        trx-id (nth row 5)
        address (nth row 6)
        ]
    [date integration-name label "" "" asset quantity "" "" (str "crypto-address: " address) trx-id]))

(defn handle-closed-orders [row]
  (let [date (format-date (nth row 1))
        integration-name "Bittrex CSV"
        label "Trade"
        contract (nth row 0)
        first-asset (subs contract 0 (str/index-of contract "-"))
        second-asset (subs contract (+ 1 (str/index-of contract "-")))
        type-full (nth row 3)
        type (subs type-full (+ 1 (str/index-of type-full "_")))
        quantity (nth row 6)
        price (nth row 8)
        is-sell #(= "SELL" type)
        outgoing-asset #(if (is-sell) second-asset first-asset)
        outgoing-amount #(if (is-sell) quantity price)
        incoming-asset #(if (is-sell) first-asset second-asset)
        incoming-amount #(if (is-sell) price quantity)
        ]
    [date integration-name label (outgoing-asset) (outgoing-amount) (incoming-asset) (incoming-amount)]))

(defn handle-closed-withdrawals [row]
  (let [date (format-date (nth row 4))
        integration-name "Bittrex CSV"
        label "Withdrawal"
        address (nth row 3)
        asset (nth row 1)
        quantity (nth row 2)
        trx-id (nth row 9)
        ]
    [date integration-name label asset quantity "" "" "" "" (str "sent to: " address) trx-id]))

(read-write "resources/bittrex/closed_deposits-filled.xls" "bittrex/closed_deposits.xls" handle-closed-deposits)
(read-write "resources/bittrex/closed_orders-filled.xls" "bittrex/closed_orders.xls" handle-closed-orders)
(read-write "resources/bittrex/closed_withdrawals-filled.xls" "bittrex/closed_withdrawals.xls" handle-closed-withdrawals)
