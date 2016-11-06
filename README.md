# nginxlive

Reactive Logserver for the nginx webserver

## Documentation

See https://github.com/zyclonite/nginxlive/wiki/Manual for details

## Docker

```
$ docker run -d --name nginxlive -p 5140:5140/udp -p 5141:5141 zyclonite/nginxlive
```

## Source

The project's source code is hosted at:

https://github.com/zyclonite/nginxlive

## Github procedures

nginxlive accepts contributions through pull requests on GitHub. After review a pull
request should either get merged or be rejected.

When a pull request needs to be reworked, say you have missed something, the pull
request is then closed, at the time you finished the required changes you should
reopen your original Pull Request and it will then be re-evaluated. At that point if
the request is aproved we will then merge it.

Make sure you always rebase your branch on master before submitting pull requests.