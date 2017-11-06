# com.grzm/boot-rel-easy.alpha

Version and release management made easy (for boot and git).

`boot-rel-easy` provides three tasks, `init`, `cut-release`, and `cut-dev`.

[`adzerk.bootlaces`][bootlaces-readme]

[bootlaces-readme]: https://github.com/adzerk-oss/bootlaces

## This is alpha!

If it's not clear from the project name, *this is alpha*. The API is
currently under development and breaking changes are likely!

## Usage

The project version is kept in a `version.edn` file which is read by
`build.boot`, so require it before using the version.

```clojure
(set-env!
  :dependencies '[[com.grzm/boot-rel-easy.alpha "0.1.0-SNAPSHOT" :scope "test"]])
(require '[com.grzm.boot-rel-easy.alpha :as rel-easy :refer [cut-release cut-dev]])

(def project 'com.example/your-cool.project)
(def version (rel-easy/get-version)
```

After initializing the git repo, make your initial commit. Tag this
commit and create the initial `version.edn` file. The initial version
will be `0.1.0-SNAPSHOT`.

```shell
boot rel-easy/init
```

When you're ready for a release (and everything has been committed), run

```
boot cut-release
```

This will update the version (committing the new version file), and
tag the commit.


And as you're ready for development again, bump the version by running

```
boot cut-dev
```


## It's opinionated

This is part of my experiments putting into practice the versioning
ideas Rich Hickey put forth in his ["Spec-ulation"][spec-ulation]
keynote and observations in the versioning of [Clojure
Spec][clojure-spec-alpha] and [ClojureScript][clojure-script-repo].

- The library stakes out the `boot-rel-easy` project, and the API will
  remain under the `boot-rel-easy.alpha` namespace until it's stable.
- The version numbers are maven-compatible. The major and minor parts
  wiil likely be fixed at `0.1`, with the incremental part will
  reflect the number of commits from the `v0.0` tag.
- The `SNAPSHOT` qualifier is used for development

[spec-ulation]: https://www.youtube.com/watch?v=oyLBGkS5ICk
[clojure-spec-alpha]: https://github.com/clojure/spec.alpha
[clojure-script-repo]: https://github.com/clojure/clojurescript/

## TODO

 - Add dry-run options
 - Add options similar to `boot.core/push` for release management
 - Make commit and tag messages more flexible

## License

© 2017 Michael Glaesemann

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
