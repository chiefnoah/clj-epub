(ns clj-epub.core
  "input and output EPUB files"
  (:use [clj-epub epub zipf markup]
        [hiccup.util :refer [as-str raw-string]])
  (:import [java.io ByteArrayOutputStream]
           [java.util UUID]))


(defn generate-uuid
  "generate uuid for OPF element dc:identifier(BookID)"
  []
  (str (UUID/randomUUID)))


(defn- stringify-text [m]
  (assoc m :text (as-str (:text m))))

(defn- write-epub
  "write EPUB on zip file"
  [zos epub]
  (stored zos (:mimetype epub))
  (doseq [extra (:extras epub)]
    (storedb zos extra))
  (doseq [key [:meta-inf :content-opf :toc-ncx]]
    (deflated zos (stringify-text (key epub))))
  (doseq [t (:html epub)]
    (deflated zos (stringify-text t)))
  (.flush zos))


(defn text->epub
  "Generate EPUB data. Args are epub title of metadata, includes text files."
  [{input-files :inputs
    title :title
    author :author
    markup-type :markup
    book-id :id
    lang :language
    extras :extras}]
  (let [eptexts  (files->epub-texts markup-type input-files)
        metadata {:title    title
                  :author   (or author "Nobody")
                  :id       (or book-id (generate-uuid))
                  :sections eptexts
                  :language (or lang "ja")}]
    {:mimetype    (mimetype)
     :meta-inf    (meta-inf)
     :content-opf (content-opf metadata)
     :toc-ncx     (toc-ncx (:id metadata) eptexts)
     :html        eptexts
     :extras      extras}))


(defn epub->file
  "Output EPUB file from apply EPUB info"
  [epub filename]
  (with-open [zos (open-zipfile filename)]
    (write-epub zos epub)))


(defn epub->byte
  "output EPUB bytes"
  [epub]
  (with-open [baos (ByteArrayOutputStream.)
              zos (open-zipstream baos)]
    (write-epub zos epub)
    (.toByteArray baos)))

