# Supervisor

[Supervisor](http://supervisord.org) is a process control system that makes it easy to start, stop, and restart your Jiny app.

## Install

Supervisor can be installed through package managers on Linux.

### Ubuntu

```sh
sudo apt-get update
sudo apt-get install supervisor
```

### CentOS and Amazon Linux

```sh
sudo yum install supervisor
```

### Fedora

```sh
sudo dnf install supervisor
```

## Configure

Each Jiny app on your server should have its own configuration file. For an example `Hello` project, the configuration file would be located at `/etc/supervisor/conf.d/hello.conf`

```sh
[program:hello]
command=java -jar /home/Jiny/hello/build/lib/hello-all.jar
directory=/home/Jiny/hello/
user=Jiny
stdout_logfile=/var/log/supervisor/%(program_name)-stdout.log
stderr_logfile=/var/log/supervisor/%(program_name)-stderr.log
```

As specified in our configuration file the `Hello` project is located in the home folder for the user `Jiny`. Make sure `directory` points to the root directory of your project where the `Package.swift` file is.

The `--env production` flag will disable verbose logging.

### Environment

You can export variables to your Jiny app with supervisor.

```sh
environment=PORT=8123
```

Exported variables can be used in Jiny using `Environment.get`

```java
String port = System.getenv("PORT")
```

## Start

You can now load and start your app.

```sh
supervisorctl reread
supervisorctl add hello
supervisorctl start hello
```

::: warning Note
The `add` command may have already started your app.
:::