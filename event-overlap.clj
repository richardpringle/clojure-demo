
(def counter (atom 0))

(defn nextId []
  (swap! counter inc))

; to simplify the test cases, all events will be on the same day
(defn getDate
  [hour minute]
  (.getTime (java.util.Date. 119 03 27 hour minute)))

(defn createEvent
  [[hourA minuteA] [hourB minuteB]]
  {
    :id (nextId)
    :start (getDate hourA minuteA)
    :end (getDate hourB minuteB)
  })

;  it's easier to prove events aren't overlapping when A ends before B starts
(defn endsBefore
  [eventA eventB]
  (<= (get-in eventA [:end]) (get-in eventB [:start])))

;  it's easier to prove events aren't overlapping when A starts after B ends
(defn startAfter
  [eventA eventB]
  (>= (get-in eventA [:start]) (get-in eventB [:end])))

(defn hasConflict
  [eventA eventB]
  (not (or (endsBefore eventA eventB) (startAfter eventA eventB))))

; compare an event to a collection of events and find any conflicts
; note:
; it probably would have been more intuitive to return a list
; of conflicting events and create the tuples outside of this function
; however, mapping inside this function actually makes the code a little cleaner
(defn getConflictsForEvent
  [event eventCollection]
  (map
    (fn [conflictingEvent] [event conflictingEvent])
    (filter
      (fn [eventFromCollection] (hasConflict event eventFromCollection))
      eventCollection)))

; The general approach is to split the collection into first and rest, comparing
; the first event to the rest and then recursing on the rest. I don't know much
; about the different collection types of clojure, so I treated the collections
; as though they are cons lists with constant time head/body retreival as well as
; appending to the head. The algorithm runs in O(n^2) time and I don't believe
; that it can be optimized any further, without guaranteeing the sort order
; of the event-list
(defn getConflicts
  ([events] (getConflicts events []))
  ([events, conflicts]
    (if (empty? events)
      conflicts
      (recur
        (rest events)
        (concat (getConflictsForEvent (first events) (rest events)) conflicts)))))

; if the start and end of respective events coincide, they are not conisdered to conflict
(def bookedBackToBackWithoutConflict
  [
    (createEvent [9 0] [10 0])
    (createEvent [10 0] [11 0])
    (createEvent [11 0] [12 0])
    (createEvent [12 0] [13 0])
    (createEvent [13 0] [14 0])
    (createEvent [14 0] [15 0])
    (createEvent [15 0] [16 0])
    (createEvent [16 0] [17 0])
  ])

; just making sure I don't have a false negative on the previous case
(def sparselyBookedWithoutConflict
  [
    (createEvent [9 0] [9 30])
    (createEvent [10 0] [10 30])
    (createEvent [11 0] [11 30])
    (createEvent [12 0] [12 30])
    (createEvent [13 0] [13 30])
    (createEvent [14 0] [14 30])
    (createEvent [15 0] [15 30])
    (createEvent [16 0] [16 30])
  ])

; here, there are 3 clonflicting pairs. One event conflicts with two others
(def bookedWithConflicts
  [
    (createEvent [9 0] [9 30])
    (createEvent [9 20] [10 0])
    (createEvent [10 0] [11 0])
    (createEvent [12 0] [13 30])
    (createEvent [13 15] [14 0])
    (createEvent [13 30] [15 0])
    (createEvent [15 0] [16 0])
    (createEvent [16 30] [17 0])
  ])



(println (getConflicts bookedBackToBackWithoutConflict))
(println)
(println (getConflicts sparselyBookedWithoutConflict))
(println)
(println (getConflicts bookedWithConflicts))
