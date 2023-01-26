FROM ubuntu:bionic

# ENV http_proxy http://<proxy_host>:<proxy_port>
# ENV https_proxy http://<proxy_host>:<proxy_port>

RUN apt-get update \
  && apt-get install -y \
  iputils-ping net-tools dnsutils curl \
  bind9 \
  bind9utils \
  bind9-doc

# Enable IPv4
RUN sed -i 's/OPTIONS=.*/OPTIONS="-4 -u bind"/' /etc/default/bind9

# Copy configuration files
COPY named.conf.options /etc/bind/
COPY named.conf.local /etc/bind/
COPY db.gpsghoster.votre_domaine.com /etc/bind/zones/

RUN chmod 664 /etc/bind/named.conf.options
RUN chmod 664 /etc/bind/named.conf.local
RUN chmod 664 /etc/bind/zones/db.gpsghoster.votre_domaine.com
