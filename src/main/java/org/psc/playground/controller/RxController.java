package org.psc.playground.controller;

import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Slf4j
@RestController
@RequestMapping("rx")
public class RxController {

    @GetMapping(path = "zip/results", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<String>> getResultsWithZip() {
        ExecutorService executorService = Executors.newFixedThreadPool(3);

        Observable<List<String>> first =
                Observable.fromCallable(() -> createResult(1, 10000)).subscribeOn(Schedulers.from(executorService));
        Observable<List<String>> second =
                Observable.fromCallable(() -> createResult(3, 10000)).subscribeOn(Schedulers.from(executorService));
        Observable<List<String>> third =
                Observable.fromCallable(() -> createResult(5, 500)).subscribeOn(Schedulers.from(executorService));

        List<String> result = Observable.zip(first, second, third,
                (a, b, c) -> Stream.of(a, b, c).flatMap(Collection::stream).collect(Collectors.toList()))
                .doOnError(t -> log.error(ExceptionUtils.getStackTrace(t)))
                .first(new ArrayList<>())
                .blockingGet();

        return ResponseEntity.ok(result);
    }

    private List<String> createResult(int start, long sleep) {
        List<String> result = IntStream.range(start, start + 2).mapToObj(String::valueOf).collect(Collectors.toList());
        try {
            Thread.sleep(sleep);
        } catch (InterruptedException e) {
            log.error(ExceptionUtils.getStackTrace(e));
        }
        return result;
    }
}
