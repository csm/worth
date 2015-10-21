(ns worth.core
  (require [clj-http.client :as http]
           [hickory.core :refer :all]
           [hickory.select :as s]
           [clojure.tools.cli :as cli])
  (import [org.joda.time DateTime Period]
          [org.joda.time.format DateTimeFormat PeriodFormatterBuilder]
          [java.text NumberFormat])
  (:gen-class))

(defn parse-double
  [s]
  (try
    (Double/parseDouble s)
    (catch Exception _ nil)))

(defn fetch-price
  [ticker]
  (let [content (-> (http/get (str "http://www.google.com/finance?q=" ticker))
                    :body
                    parse
                    as-hickory)
        price-element (s/select (s/child (s/tag :div)
                                         (s/id "price-panel")
                                         (s/tag :div)
                                         (s/class "pr")
                                         (s/tag :span)) content)]
    (-> price-element first :content first parse-double)))

(defn get-time-periods
  [period interval]
  (condp = interval
    :year (.getYears period)
    :month (+ (.getMonths period) (* 12 (.getYears period)))
    :week (+ (.getWeeks period) (* 52 (.getYears period)))))

(defn get-worth
  [opts]
  (let [{:keys [ticker units strike sold start-date end-date rate]} opts
        price (fetch-price ticker)
        value (- price strike)
        total-time (Period. start-date end-date)
        total-periods (get-time-periods total-time rate)
        elapsed-periods (get-time-periods (Period. start-date (DateTime/now)) rate)
        vested-shares (* units (/ elapsed-periods total-periods))
        unvested-shares (- units vested-shares)
        exercisable-shares (- vested-shares sold)]
    {:price price
     :value value
     :value-today (* value exercisable-shares)
     :value-pending (* value unvested-shares)
     :vested-shares vested-shares
     :exercisable-shares exercisable-shares
     :unvested-shares unvested-shares
     :vested-pct (int (* 100 (/ vested-shares units)))}))

(defn parse-date
  [s]
  (.parseDateTime (DateTimeFormat/forPattern "yyyy-MM-dd") s))

(def cli-options
  [["-t" "--ticker TICKER" "Stock ticker symbol."]
   ["-u" "--units UNITS" "Number of stock units or options."
    :parse-fn #(Integer/parseInt %)]
   ["-p" "--strike PRICE" "Set strike price." :default 0.0
    :parse-fn #(Double/parseDouble %)]
   ["-s" "--sold COUNT" "Number of shares already sold." :default 0
    :parse-fn #(Integer/parseInt %)]
   ["-b" "--start-date YYYY-MM-DD" "Vesting schedule start date."
    :parse-fn parse-date]
   ["-e" "--end-date YYYY-MM-DD" "Vesting schedule end date."
    :parse-fn parse-date]
   ["-r" "--rate RATE" "Maturation rate." :default "month"
    :parse-fn keyword
    :validate [#(contains? #{:year :month :week} (keyword %)) "Rate must be 'year', 'month', or 'week'."]]
   ["-h" "--help" "Show this help and exit."]])

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (let [opts (cli/parse-opts args cli-options)
        options (:options opts)]
    (when (:help options)
      (println (:summary opts))
      (System/exit 0))
    (when (:errors opts)
      (for [e (:errors opts)] (println e))
      (System/exit 1))
    (when (some nil? [(:ticker options) (:units options) (:start-date options) (:end-date options)])
      (println "Missing an argument.")
      (System/exit 1))
    (let [worth (get-worth options)
          fmt (NumberFormat/getCurrencyInstance)
          pfmt (.toFormatter (doto (PeriodFormatterBuilder.)
                               (.printZeroNever)
                               (.appendYears)
                               (.appendSuffix " year" " years")
                               (.appendSeparator ", ")
                               (.appendMonths)
                               (.appendSuffix " month" " months")
                               (.appendSeparator ", ")
                               (.appendDays)
                               (.appendSuffix " day" " days")
                               ))]
      (printf "Today's %s price is %s; your total unsold shares are worth %s.\n"
              (:ticker options)
              (.format fmt (:price worth))
              (.format fmt (* (:value worth) (- (:units options) (:sold options)))))
      (if (= 0 (:unvested-shares worth))
        (println "You are 100% vested. Why are you still here?")
        (do
          (printf "You are %d%% vested, for a total of %d vested unsold shares (%s).\n"
                  (:vested-pct worth)
                  (int (:exercisable-shares worth))
                  (.format fmt (:value-today worth)))
          (if (< 0 (:value-pending worth))
            (printf "But if you quit today, you will walk away from %s.\nHang in there little trooper!  Only %s left!\n"
                    (.format fmt (:value-pending worth))
                    (.print pfmt (Period. (DateTime/now) (:end-date options))))
            (printf "Your shares are worthless. Why are you still here?\n")))))))
