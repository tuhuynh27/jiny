# Introduction

## What is Jiny framework?
Jiny features a lightweight (Servlet-free and no dependency), expressive and unopinionated pure HTTP Server/Client including request parser, routing, middlewares, proxy-mode and more. If you need a quick start & simple way to write a JVM server, you would love this framework. Unlike other Java web frameworks, you can structure your application as you want.

## Why I build this framework?

I build this for my LINE Bot webhook server which was rewritten in Java, Servlet APIs / JavaEE stuff is too heavy-weight (The Servlet APIs require that your application must be run within a servlet container), super complex and very verbose, also Java 8 SE is lacking a built-in simple HTTP handler/router.

 Java in itself is lacking some features that facilitate proper application development (unlike many features of Go) as Java web development has traditionally been very cumbersome with verbose frameworks.

## Focus on Minimalist & Expressive

Jiny is built for rapid development, whose intention is to provide an alternative for Java developers that want to develop their web applications as expressive as possible and with minimal boilerplate. The framework was designed not only to make you more productive, but also to make your code better, declarative and expressive syntax.
