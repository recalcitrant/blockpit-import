(defproject blockpit "0.1.0-SNAPSHOT"
	:description "FIXME: write description"
	:url "http://example.com/FIXME"
	:license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
	          :url  "https://www.eclipse.org/legal/epl-2.0/"}
	:dependencies [[org.clojure/clojure "1.11.2"]
	               [org.clojure/data.json "2.4.0"]
	               [dk.ative/docjure "1.19.0"]
								 [de.blockpit/blockpit-java "1.0.0-SNAPSHOT"]]
	:main ^:skip-aot blockpit.bybit-transfers
	:target-path "target/%s"
	:profiles {:uberjar {:aot      :all
	                     :jvm-opts ["-Dclojure.compiler.direct-linking=true"]}})
