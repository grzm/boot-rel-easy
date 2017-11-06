(set-env! :resource-paths #{"src"}
          :source-paths   #{}
          :dependencies   '[[boot/core "RELEASE" :scope "test"]
                            [boot/pod "RELEASE"]
                            [boot/worker "RELEASE"]
                            [clj-jgit "0.8.10"]
                            [org.clojure/clojure "RELEASE"]])

(require '[com.grzm.boot-rel-easy.alpha :as rel-easy :refer [cut-release cut-dev]])

(def project 'com.grzm/boot-rel-easy.alpha)
(def version (rel-easy/get-version))

(task-options!
 pom {:project     project
      :version     version
      :description "Version and release management made easy"
      :url         "http://github.com/grzm/boot-rel-easy.alpha"
      :scm         {:url "https://github.com/grzm/boot-rel-easy.alpha"}
      :license     {"Eclipse Public License"
                    "http://www.eclipse.org/legal/epl-v10.html"}})

(deftask build
  "Build and install the project locally."
  []
  (comp (pom) (jar) (install)))

