#!/bin/bash

: "${SERVER_NAME:=localhost}"

# Replace the placeholder in the Nginx configuration template with the actual server name
sed "s/\${SERVER_NAME}/$SERVER_NAME/g" /etc/nginx/nginx.conf.template > /etc/nginx/nginx.conf
