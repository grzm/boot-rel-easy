(ns com.grzm.boot-rel-easy.alpha
  "Example tasks showing various approaches."
  {:boot/export-tasks true}
  (:require
   [boot.core :as boot :refer [deftask with-pre-wrap commit!]]
   [boot.git :as git]
   [boot.util :as util]
   [clojure.edn :as edn]
   [clojure.java.io :as io]
   [clojure.java.shell :as shell]
   [clojure.string :as str]
   [com.grzm.boot-rel-easy.alpha.jgit :as jgit]))

(def ^:dynamic *version-file-name* "version.edn")
(def ^:dynamic *version-zero-tag* "v0.0")
(def ^:dynamic *initial-version* {:major 0
                                  :minor 1
                                  :incremental 0
                                  :qualifier "SNAPSHOT"})

(def version-map (atom nil))

(defn read-version-file
  "Reads `*version-file-name*` from filesystem"
  ([]
   (read-version-file *version-file-name*))
  ([vfn]
   (read-version-file vfn true))
  ([vfn force?]
   (let [version-file (io/file vfn)]
     (if (.exists version-file)
       (-> vfn slurp edn/read-string)
       (when force?
         (throw (ex-info (format "%s (No such file or directory)" vfn)
                         {:file-name vfn})))))))

(defn get-version-map
  []
  (if-let [v @version-map]
    v
    (let [v (read-version-file *version-file-name* true)]
      (reset! version-map v))))

(defn write-version-file
  "Writes the given version to `*version-file-name*`"
  ([v]
   (write-version-file v *version-file-name*))
  ([v vfn]
   (spit vfn (pr-str v))))

(deftask init
  "Set version-zero tag"
  []
  (let [vf (io/file *version-file-name*)]
    (if (.exists vf)
      identity
      (with-pre-wrap fileset
        (let [initial-tag     *version-zero-tag*
              msg             "init"
              initial-version *initial-version*]
          (util/info (format "Taggig initial version %s...\n" initial-tag))
          (git/tag initial-tag msg)
          (util/info (format "Writing version file %s...\n" *version-file-name*))
          (write-version-file initial-version)
          (util/info (format "Adding version file to repo...\n" *version-file-name*))
          (jgit/add *version-file-name*)
          fileset)))))

(defn format-version [v]
  (let [{:keys [major minor incremental]} v]
    (if-let [qualifier (:qualifier v)]
      (format "%d.%d.%d-%s" major minor incremental qualifier)
      (format "%d.%d.%d" major minor incremental))))

(defn get-version []
  (when-let [version (read-version-file *version-file-name* false)]
    (format-version version)))

(defn format-release-tag [v]
  (str "v" (format-version v)))

(defn version-string []
  (when-let [version (read-version-file *version-file-name* false)]
    (format-version version)))

(defn format-date [t]
  (.format (java.text.SimpleDateFormat. "YYYY-MM-dd") t))

(defn commits-since-zero-tag
  []
  (let [{:keys [exit out err]
         :as res} (shell/sh "git" "--no-replace-objects" "describe" "--match"
                            *version-zero-tag*)]
    (if (zero? exit)
      (->> (str/split out #"-") second Integer/parseInt)
      res)))

(defn format-release-message
  ([v]
   (format-release-message v (java.util.Date.)))
  ([v timestamp]
   (let [version-string (format-version v)
         date-string (format-date timestamp)]
     (format "Release v%s (%s)" version-string date-string))))

(defn next-snapshot [version]
  (let [{:keys [incremental qualifier]} version]
    (if qualifier
      (throw (ex-info "Snapshot version must derive from release version"
                      {:version version}))
      (assoc version
             :incremental (inc incremental)
             :qualifier "SNAPSHOT"))))

(defn release-version
  ([version]
   (release-version version (commits-since-zero-tag)))
  ([version commits]
   (let [qualifier (:qualifier version)]
     (if qualifier
       (assoc version
              :incremental commits
              :qualifier nil)
       (throw (ex-info "Release version must derive from snapshot version"
                       {:version version}))))))

(deftask cut-dev
  "Increment version to next development snapshot"
  []
  (let [vf (io/file *version-file-name*)]
    (if-not (.exists vf)
      identity
      (with-pre-wrap fileset
        (let [old-version (read-version-file)
              new-version (next-snapshot old-version)]
          (when (not= old-version new-version)
            (let [commit-message (format "Update version for development. (%s)"
                                         (format-version new-version))]
              (util/info (format "Updating development version to %s...\n"
                                 (format-version new-version)))
              (write-version-file new-version)
              (jgit/add *version-file-name*)
              (util/info (format "Committing dev snapshot (%s)...\n" commit-message))
              (jgit/commit commit-message))))
        fileset))))

(deftask cut-release
  "Set version to release"
  []
  (if-let [old-version (get-version-map)]
    (with-pre-wrap fileset
      (let [new-version (release-version old-version)]
        (when (not= old-version new-version)
          (let [commit-message (format-release-message new-version)
                version-tag    (format-release-tag new-version)]
            (util/info (format "Setting release version to %s...\n"
                               (format-version new-version)))
            (write-version-file new-version)
            (jgit/add *version-file-name*)
            (util/info (format "Committing release (%s)...\n" commit-message))
            (jgit/commit commit-message)
            (util/info (format "Tagging release %s...\n" version-tag))
            (git/tag version-tag commit-message)))
        fileset))
    identity))
