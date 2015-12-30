## nginx configuration
### nginx log config
    http {
        log_format  json  '{"remoteAddr":"$remote_addr","remoteUser":"$remote_user",'
                          '"time":"$time_iso8601","requestURI":"$request_uri",'
                          '"queryString":"$query_string","requestMethod":"$request_method",'
                          '"scheme":"$scheme","serverProtocol":"$server_protocol",'
                          '"contentType":"$sent_http_content_type","status":$status,'
                          '"bodyBytesSent":$body_bytes_sent,"httpReferer":"$http_referer",'
                          '"httpUserAgent":"$http_user_agent","httpXForwardedFor":"$http_x_forwarded_for",'
                          '"host":"$host","bytesSent":"$bytes_sent",'
                          '"requestTime":"$request_time","serverName":"$server_name",'
                          '"serverAddr":"$server_addr","serverPort":"$server_port",'
                          '"hostname":"$hostname","requestLength":"$request_length",'
                          '"proxyHost":"$proxy_host","proxyPort":"$proxy_port",'
                          '"uri":"$uri","nginxVersion":"$nginx_version"}';

        access_log  syslog:server=127.0.0.1:5140,tag=nginxjson json;
    }
### nginx server config
    server {
        listen 80;
        server_name nginxlive.domain.tld;

        location /nginxlive/ {
                proxy_pass http://127.0.0.1:5141/;
                proxy_http_version 1.1;
                proxy_set_header Upgrade $http_upgrade;
                proxy_set_header Connection "upgrade";
                include proxy_params;
        }
    }
