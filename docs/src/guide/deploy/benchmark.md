# Benchmark

::: warning WIP
This page is WIP
:::

Benchmark tool to use: [wrk tool](https://github.com/wg/wrk)

## Configuration

I use 2 GCP VM to test, one for running server and one for running wrk benchmark client.

Server (GCP SG):

```
➜ neofetch
            .-/+oossssoo+/-.               odd@odd-vm
        `:+ssssssssssssssssss+:`           ----------
      -+ssssssssssssssssssyyssss+-         OS: Ubuntu 20.04.1 LTS x86_64
    .ossssssssssssssssssdMMMNysssso.       Host: Google Compute Engine
   /ssssssssssshdmmNNmmyNMMMMhssssss/      Kernel: 5.4.0-1024-gcp
  +ssssssssshmydMMMMMMMNddddyssssssss+     Uptime: 2 hours, 11 mins
 /sssssssshNMMMyhhyyyyhmNMMMNhssssssss/    Packages: 737 (dpkg), 5 (snap)
.ssssssssdMMMNhsssssssssshNMMMdssssssss.   Shell: bash 5.0.17
+sssshhhyNMMNyssssssssssssyNMMMysssssss+   Terminal: /dev/pts/0
ossyNMMMNyMMhsssssssssssssshmmmhssssssso   CPU: AMD EPYC 7B12 (2) @ 2.250GHz
ossyNMMMNyMMhsssssssssssssshmmmhssssssso   Memory: 714MiB / 3935MiB
+sssshhhyNMMNyssssssssssssyNMMMysssssss+
.ssssssssdMMMNhsssssssssshNMMMdssssssss.
 /sssssssshNMMMyhhyyyyhdNMMMNhssssssss/
  +sssssssssdmydMMMMMMMMddddyssssssss+
   /ssssssssssshdmNNNNmyNMMMMhssssss/
    .ossssssssssssssssssdMMMNysssso.
      -+sssssssssssssssssyyyssss+-
        `:+ssssssssssssssssss+:`
            .-/+oossssoo+/-.


```

Client: (GCP SG) wrk - same options as above

## Results

### Jiny Async Mode

```
root@wrk-vm:~/wrk# ./wrk -t8 -c5000 -d30s http://nginx.test.jinyframework.com/
Running 30s test @ http://nginx.test.jinyframework.com/
  8 threads and 5000 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency    16.37ms   94.71ms   1.87s    99.16%
    Req/Sec     1.76k     1.37k    5.20k    60.83%
  186493 requests in 30.08s, 33.03MB read
  Socket errors: connect 3987, read 9027, write 0, timeout 83
  Non-2xx or 3xx responses: 812
Requests/sec:   6198.89
Transfer/sec:      1.10MB

root@wrk-vm:~/wrk# ./wrk -t8 -c5000 -d60s http://nginx.test.jinyframework.com/
Running 1m test @ http://nginx.test.jinyframework.com/
  8 threads and 5000 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency    71.93ms  250.24ms   1.90s    94.74%
    Req/Sec   751.52    465.63     3.11k    62.57%
  246698 requests in 1.00m, 45.94MB read
  Socket errors: connect 3987, read 1168837, write 0, timeout 2087
  Non-2xx or 3xx responses: 14722
Requests/sec:   4104.89
Transfer/sec:    782.75KB
```

Total memory allocated after tests: `172MB RAM`

### [Spring WebFlux](https://docs.spring.io/spring-framework/docs/current/spring-framework-reference/web-reactive.html)

```
root@wrk-vm:~/wrk# ./wrk -t8 -c5000 -d30s http://nginx.test.jinyframework.com/
Running 30s test @ http://nginx.test.jinyframework.com/
  8 threads and 5000 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency   349.93ms  279.99ms   2.00s    69.25%
    Req/Sec   301.62    249.18     1.97k    75.21%
  60918 requests in 30.10s, 13.06MB read
  Socket errors: connect 3987, read 608039, write 0, timeout 612
  Non-2xx or 3xx responses: 14645
Requests/sec:   2024.18
Transfer/sec:    444.43KB

root@wrk-vm:~/wrk# ./wrk -t8 -c5000 -d60s http://nginx.test.jinyframework.com/
Running 1m test @ http://nginx.test.jinyframework.com/
  8 threads and 5000 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency   142.73ms   88.79ms 925.09ms   80.34%
    Req/Sec   693.32    458.44     2.51k    68.90%
  325915 requests in 1.00m, 57.68MB read
  Socket errors: connect 3987, read 821056, write 0, timeout 0
  Non-2xx or 3xx responses: 4848
Requests/sec:   5423.01
Transfer/sec:      0.96MB
```

Total memory allocated after tests: `384MB RAM`

### [Armeria](https://armeria.dev/)

```
root@wrk-vm:~/wrk# ./wrk -t8 -c5000 -d30s http://nginx.test.jinyframework.com/
Running 30s test @ http://nginx.test.jinyframework.com/
  8 threads and 5000 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency   352.26ms  291.10ms   1.99s    78.30%
    Req/Sec   385.25    263.64     1.46k    67.74%
  59367 requests in 30.09s, 11.75MB read
  Socket errors: connect 3987, read 619897, write 0, timeout 601
  Non-2xx or 3xx responses: 7751
Requests/sec:   1972.70
Transfer/sec:    399.66KB

root@wrk-vm:~/wrk# ./wrk -t8 -c5000 -d60s http://nginx.test.jinyframework.com/
Running 1m test @ http://nginx.test.jinyframework.com/
  8 threads and 5000 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency   145.99ms  119.60ms   2.00s    85.61%
    Req/Sec   778.09    572.45     2.65k    66.79%
  320121 requests in 1.00m, 57.18MB read
  Socket errors: connect 3987, read 796825, write 0, timeout 89
  Non-2xx or 3xx responses: 4304
Requests/sec:   5326.64
Transfer/sec:      0.95MB
```

Total memory allocated after tests: `426MB RAM`
