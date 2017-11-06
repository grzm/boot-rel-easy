(ns com.grzm.boot-rel-easy.alpha.jgit
  (:require
   [clojure.java.io :as io]
   [clj-jgit.porcelain :as jgit]
   [boot.util :as util]))

(def repo-dir
  (delay (let [repo #(and (util/guard (jgit/with-repo % repo)) %)]
           (loop [d (.getCanonicalFile (io/file "."))]
             (when d (or (repo d) (recur (.getParentFile d))))))))

(defmacro with-repo
  [& body]
  `(do (assert @repo-dir "This does not appear to be a git repo.")
       (jgit/with-repo @repo-dir ~@body)))

(defn add [file-name]
  (with-repo (jgit/git-add repo file-name)))

(defn commit [message]
  (with-repo (jgit/git-commit repo message)))
