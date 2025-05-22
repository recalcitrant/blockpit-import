(ns blockpit.bybit-uta
  (:use [dk.ative.docjure.spreadsheet]
        [clojure.pprint]
        [blockpit.header]))

(defn handle [row]
  (when (= 18 (count row))
    (let [date (nth row 14)
          integration-name "Bybit CSV vera@web"
          closed-pnl (nth row 16 0)
          profit-or-loss (nth row 17 0)
          outgoing-asset (if (neg? closed-pnl) "BTC" "")
          outgoing-amount (if (neg? closed-pnl) closed-pnl "")
          incoming-asset (if (neg? closed-pnl) "" "BTC")
          incoming-amount (if (neg? closed-pnl) "" closed-pnl)
          ]

      [date integration-name profit-or-loss outgoing-asset outgoing-amount incoming-asset incoming-amount])
    ))

(defn from [sheet]
  (into []
        (->>
          (load-workbook-from-resource sheet)
          (select-sheet "closedpl")
          (row-seq)
          (rest)
          (map cell-seq)
          (map #(map read-cell %))
          (map #(handle %))
          (remove nil?)
          )))

(defn to [file-name data]
  (pprint data)
  (let [wb (create-workbook "bybit trx history" data)]
    (dorun (for [sheet (sheet-seq wb)]
             (auto-size-all-columns! sheet)))
    (save-workbook! file-name wb))
  )

;(to "resources/vfweb/manual_uta_adjustments/Bybit_AssetChangeDetails_uta_30119721_20240101_20241231_0-filled.xls"
;   (into [] (cons header (from "vfweb/manual_uta_adjustments/Bybit_AssetChangeDetails_uta_30119721_20240101_20241231_0.xls"))))

(to "resources/vfweb/manual_uta_adjustments/Bybit_AssetChangeDetails_uta_30119721_20250101_20250516_0-filled.xlsx"
    (into [] (cons header (from "vfweb/manual_uta_adjustments/Bybit_AssetChangeDetails_uta_30119721_20250101_20250516_0.xls"))))
