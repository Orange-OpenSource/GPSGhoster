FROM node:latest

# ENV http_proxy http://<proxy_host>:<proxy_port>
# ENV https_proxy http://<proxy_host>:<proxy_port>

# ENV PARSE_DASHBOARD_VERSION 1.0.25
ENV PARSE_DASHBOARD_VERSION 4.1.1
RUN npm install -g parse-dashboard@${PARSE_DASHBOARD_VERSION}
ENV PORT 4040
EXPOSE $PORT

CMD ["parse-dashboard"]

