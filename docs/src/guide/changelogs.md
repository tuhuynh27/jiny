# Changelogs

![Maven Central](https://img.shields.io/maven-central/v/com.jinyframework/jiny?style=flat-square)

### 0.2.5 (Current)

- Add Keep-Alive support
- Add Thread Debug mode
- Refactor code

### 0.2.4

- Add NPE handled methods
- Fix HTTP client issue
- Refactor code

### 0.2.3

- Add SLF4J facade logging
- Fix proxy issues

### 0.2.2

- Add proxy mode feature
- Rename some methods

### 0.2.1
- Update global transformer
- Add full unit tests

### 0.2.0
- Support subRouter
- Improve & refactor code

### 0.1.9-ALPHA

- Support "Catch All" handler
- Refactor & Restructure code
- Support JSON adapter support

### 0.1.7-ALPHA

- Update NIO Server to use with `AsynchronousServerSocketChannel` API
- Fix some bugs in routing handlers (duplicate middleware)
- Refactor code

### 0.1.6-ALPHA

- Added an experimental NIO Server
- Refactor code

### 0.1.5-ALPHA

- Support routing handler params, ex: /params/:categoryID/:itemID
- Support request path's slash trim
- Refactor code

### 0.1.4-ALPHA

- Support HTTP Middleware functions chain (like Node.js Express and Go)

### 0.1.3-ALPHA

- Support default error handling
- Support get query params from Context
- Improve HTTP Client response

### 0.1.2-ALPHA

- Add built-in HTTP Client
- Refactor code

### 0.1.1-ALPHA

- Add HttpResponse Object for handling response struct
- Remove redundant constant and lombok usage

### 0.1.0-ALPHA

- A very naive and basic HTTP Server
- Raw implementation, lightweight & no dependency
- Listen on a given TCP Port
- Easy to add a handler with a (Method/Path/Functional_Handler) define
- Handled inside with a Cached ThreadPool
