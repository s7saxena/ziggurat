(ns ziggurat.messaging.producer_test
  (:require [clojure.test :refer :all]
            [ziggurat.fixtures :as fix]
            [ziggurat.messaging.producer :as producer]
            [ziggurat.util.rabbitmq :as rmq]))

(use-fixtures :once fix/init-rabbit-mq)

(deftest retry-test
  (testing "message with a retry count of greater than 0 will publish to delay queue"
    (fix/with-clear-data
      (let [message {:foo "bar" :retry-count 5}
            expected-message {:foo "bar" :retry-count 4}
            topic "booking"]
        (producer/retry message topic)
        (let [message-from-mq (rmq/get-msg-from-delay-queue "booking")]
          (is (= expected-message message-from-mq))))))

  (testing "message with a retry count of 0 will publish to dead queue"
    (fix/with-clear-data
      (let [message {:foo "bar" :retry-count 0}
            expected-message (dissoc message :retry-count)
            topic "booking"]
        (producer/retry message topic)
        (let [message-from-mq (rmq/get-msg-from-dead-queue "booking")]
          (is (= expected-message message-from-mq))))))

  (testing "message with no retry count will publish to delay queue"
    (fix/with-clear-data
      (let [message {:foo "bar"}
            expected-message {:foo "bar" :retry-count 5}
            topic "booking"]
        (producer/retry message topic)
        (let [message-from-mq (rmq/get-msg-from-delay-queue "booking")]
          (is (= message-from-mq expected-message)))))))