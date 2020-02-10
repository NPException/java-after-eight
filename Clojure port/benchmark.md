All measured on my Desktop PC (Windows 10 Home)
Ryzen 7 2700X @ 4.0 GHz
32 GB RAM

```
Baseline @ REPL:

   Java - avg: 48 ms
Clojure - avg: 400 ms
(Note: pretty-printing to EDN format is damn expensive; the avg goes up to 716 ms in that case)
```

* parallelize article parsing [(Commit)](https://github.com/NPException/java-after-eight/commit/65c62bcbfa10f6c5d9b2011b9f454e5ea72060dc)  
`Clojure - avg: 336 ms`

* parallelize infer-typed-relations [(Commit)](https://github.com/NPException/java-after-eight/commit/2807f440c34d99e9a12b536155cd6825baa98dc7)  
`Clojure - avg: 199 ms`

* use transient map in aggregate-typed-relations [(Commit)](https://github.com/NPException/java-after-eight/commit/4f03d349897f2eac898b9b5b9f127126814100d4)  
`Clojure - avg: 195 ms`

* use records for more efficient field access [(Commit)](https://github.com/NPException/java-after-eight/commit/e63fd6738793fcf4c0e7bac4931154b9a222d375)  
`Clojure - avg: 137 ms`

* use Java's Files/readAllLines to ingest article files [(Commit)](https://github.com/NPException/java-after-eight/commit/a37302bd26d90cefa1ee20cbd0dc88a01f975d68) (drawback: `parse-article-from-file` is now limited to File instances)  
`Clojure - avg: 128 ms`

* use 'group-by' with 'juxt' to make the code much more clear (but sacrifice a few milliseconds for it) [(Commit)](https://github.com/NPException/java-after-eight/commit/6ca65ee9e95399bd365e2c27830fee77c7bc6bed)  
`Clojure - avg: 136 ms`

---

Took me way too long to realize: I had the silly genealogist active in my clojure runs...

* only use tag genealogist like the java version does  
`Clojure - avg: 74 ms`


---

* final measurement with Java 14  
`Java - avg: 42 ms`  
`Clojure - avg: 73 ms`

---

* execute uberjar with Java 8: ~ 1530 ms
* execute uberjar with Java 14: ~ 1050 ms
