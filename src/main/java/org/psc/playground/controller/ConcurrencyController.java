package org.psc.playground.controller;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.http.MediaType;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.LongStream;

@Slf4j
@CrossOrigin
@RestController
@RequestMapping("concurrency")
public class ConcurrencyController {

    private final ExecutorService globalFixedExecutorService = Executors.newFixedThreadPool(1);
    private final ExecutorService globalCachedExecutorService = Executors.newCachedThreadPool();

    // new ThreadPool created upon each request -> all threads of the pool are exclusive to each incoming request
    @GetMapping(path = "results", produces = MediaType.APPLICATION_JSON_VALUE)
    public DeferredResult<List<String>> getResults(@RequestParam(required = false) Long sleep) throws
            ExecutionException, InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(1);
        return calculateResult(sleep, executorService);
    }

    // "global" ThreadPool with fixed size -> threads of the pool get shared between requests
    @GetMapping(path = "globalThreadPool/fixed/results", produces = MediaType.APPLICATION_JSON_VALUE)
    public DeferredResult<List<String>> getGlobalFixedThreadPoolResults(
            @RequestParam(required = false) Long sleep) throws ExecutionException, InterruptedException {
        return calculateResult(sleep, globalFixedExecutorService);
    }

    // "global" cached ThreadPool -> threads get created and reused as necessary
    @GetMapping(path = "globalThreadPool/cached/results", produces = MediaType.APPLICATION_JSON_VALUE)
    public DeferredResult<List<String>> getGlobalCachedThreadPoolResults(
            @RequestParam(required = false) Long sleep) throws ExecutionException, InterruptedException {
        return calculateResult(sleep, globalCachedExecutorService);
    }

    private DeferredResult<List<String>> calculateResult(@Nullable Long sleep, ExecutorService executorService) throws
            ExecutionException, InterruptedException {
        long sleepTime = resolveSleepTime(sleep);

        DeferredResult<List<String>> result = new DeferredResult<>();
        result.setResult(executorService.submit(() -> calculateResults(sleepTime)).get());

        return result;
    }

    private List<String> calculateResults(long sleep) {
        List<String> results = new ArrayList<>(5);
        try {
            Thread.sleep(sleep);
            LongStream.range(0, sleep / 1000).forEach(l -> results.add(String.valueOf(l)));
        } catch (InterruptedException e) {
            log.error(ExceptionUtils.getStackTrace(e));
        }
        return results;
    }

    private long resolveSleepTime(@Nullable Long sleep) {
        return sleep == null || sleep == 0L ? 5000 : sleep;
    }

}
