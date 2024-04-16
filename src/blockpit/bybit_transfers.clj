(ns blockpit.bybit-transfers
  (:use [clojure.pprint]
        [dk.ative.docjure.spreadsheet]
        [blockpit.header])
  (:import (de.blockpit.java Converter)))

(defn create-output [row]
  ;(pprint row)
  (let [date (nth row 0)
        integration-name "Bybit CSV vera@web"
        label (nth row 1)
        asset-outgoing (nth row 2)
        amount-outgoing (nth row 3)
        asset-incoming (nth row 4)
        amount-incoming (nth row 5)
        comment (nth row 6)]
    [date integration-name label asset-outgoing amount-outgoing asset-incoming amount-incoming "" "" comment]))

(defn from [sheet]
  (map create-output
       (.convert (new Converter (into []
                                      (->>
                                        (load-workbook-from-resource sheet)
                                        (select-sheet "bybit-exchanges")
                                        (row-seq)
                                        (rest)
                                        (remove nil?)
                                        (map cell-seq)
                                        (map #(map read-cell %))
                                        )))))
  )

(defn to [file-name data]
  (let [wb (create-workbook "bybit trx history" data)]
    (dorun (for [sheet (sheet-seq wb)]
             (auto-size-all-columns! sheet)))
    (save-workbook! file-name wb)))

(to "resources/vfweb/bybit-deposit-exchange-in-out-2024-until-april-12-filled.xls"
    (into [] (cons header (from "vfweb/bybit-deposit-exchange-in-out-2024-until-april-12.xls"))))

