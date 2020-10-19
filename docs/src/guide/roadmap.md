# Roadmap

- [x] Write unit tests, integration tests
- [x] Support Router, global response transformer 
- [x] Add logging facade
- [x] Add Keep-Alive support
- [x] Add websocket server/client support (integrate with [org.java-websocket:Java-WebSocket](https://github.com/TooTallNate/Java-WebSocket))
- [ ] Support CORS config, body compression and some default middlewares
- [ ] Support serve static files
- [x] Support wrapper to handle NPE issues (for get methods)
- [ ] Improve matching/routing performance by using [dynamic trie](http://citeseerx.ist.psu.edu/viewdoc/download?doi=10.1.1.12.7321&rep=rep1&type=pdf) (radix tree) structure
