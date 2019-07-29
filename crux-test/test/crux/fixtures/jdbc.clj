(ns crux.fixtures.jdbc
  (:require [next.jdbc :as jdbc]
            [crux.fixtures.api :refer [*api*]]
            [crux.io :as cio]
            [crux.jdbc :as j])
  (:import [crux.api Crux ICruxAPI]))

(def ^:dynamic *dbtype* nil)

(defn with-jdbc-node [dbtype f & [opts]]
  (let [dbtype (name dbtype)
        db-dir (str (cio/create-tmpdir "kv-store"))
        jdbc-event-log-dir (str (cio/create-tmpdir "jdbc-event-log-dir"))
        options (merge {:dbtype (name dbtype)
                        :dbname "cruxtest"
                        :db-dir db-dir
                        :kv-backend "crux.kv.memdb.MemKv"
                        :jdbc-event-log-dir jdbc-event-log-dir}
                       opts)
        ds (jdbc/get-datasource options)]
    (binding [*dbtype* dbtype]
      (j/prep-for-tests! dbtype ds)
      (try
        (with-open [standalone-node (Crux/startJDBCNode options)]
          (binding [*api* standalone-node]
            (f)))
        (finally
          (cio/delete-dir db-dir)
          (cio/delete-dir jdbc-event-log-dir))))))
