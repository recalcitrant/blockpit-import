(ns blockpit.bybit-pnl
  (:use [dk.ative.docjure.spreadsheet]
        [clojure.pprint]
        [blockpit.header]))

(defn contract-asset [contract]
  (case contract
    "BTCUSD" "BTC"
    "BTCUSDT" "USDT"
    "ETHUSD" "ETH"
    "XRPUSD" "XRP"))

(defn handle [row]
  (let [date (nth row 7)
        integration-name "Bybit CSV vera@web"
        contract (nth row 0)
        closed-pnl (nth row 5)
        profit-or-loss #(if (neg? closed-pnl) "Derivative Loss" "Derivative Profit")
        outgoing-asset #(if (neg? closed-pnl) (contract-asset contract) "")
        outgoing-amount #(if (neg? closed-pnl) closed-pnl "")
        incoming-asset #(if (neg? closed-pnl) "" (contract-asset contract))
        incoming-amount #(if (neg? closed-pnl) "" closed-pnl)
        ]
    [date integration-name (profit-or-loss) (outgoing-asset) (outgoing-amount) (incoming-asset) (incoming-amount)]))

(defn from [sheet]
  (into []
        (->>
          (load-workbook-from-resource sheet)
          (select-sheet "closedpl")
          (row-seq)
          (rest)
          (remove nil?)
          (map cell-seq)
          (map #(map read-cell %))
          (map #(handle %))
          )))

(defn to [file-name data]
  (let [wb (create-workbook "bybit trx history" data)]
    (dorun (for [sheet (sheet-seq wb)]
             (auto-size-all-columns! sheet)))
    (save-workbook! file-name wb))
  )

(to "resources/vfweb/bybit-derivatives-closedpl-2024-until-april-12-filled.xls"
    (into [] (cons header (from "vfweb/bybit-derivatives-closedpl-2024-until-april-12.xls"))))
