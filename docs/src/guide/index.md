# Introduction

![](https://i.imgur.com/viYCs8l.png)

## What is Jiny framework?
Jiny features a lightweight, expressive and unopinionated pure HTTP Server/Client including request parser, routing, middlewares, proxy-mode and more. If you need a quick start & simple way to write a JVM server, you would love this framework. Unlike other restrictive Java web frameworks, you can structure your application as you want.

## Why I build this framework?

Java in itself is lacking some features that facilitate proper application development (unlike many features of Go/Node.js) as Java web development has traditionally been very cumbersome with verbose frameworks.

It is fun to write a library or a framework. It allows us to play with many interesting ideas that were not possible before due to the constraints in others' work. 

I build this for my LINE Bot webhook server rewritten in Java. From my perspective, Servlet / JavaEE stuff is too heavy-weight (Servlet APIs require that your application must be run within a servlet container), thus super complex and very verbose, also Java 8 SE is lacking a built-in simple HTTP handler/router.

## Focus on Minimalist & Expressive

Jiny is built for rapid development, whose intention is to provide an alternative for Java developers that want to develop their web applications as expressive as possible and with minimal boilerplate. The framework was designed not only to make you more productive, but also to make your code better, declarative and expressive syntax.
